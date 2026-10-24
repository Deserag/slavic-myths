package org.slavicmyths.guides;

import java.util.*;
import java.util.concurrent.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.structure.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.bayun.BayunEntity;
import org.slavicmyths.hunt.*;
import org.slavicmyths.navigation.*;
import org.slavicmyths.tales.*;
import org.slavicmyths.yaga.*;
import org.slavicmyths.worldgen.*;

/** Explicit NPC requests only. Quests live in TaleData; markers in NavigationRecords.
 * Reservations never load chunks or place blocks. Native generation materializes sites during travel. */
@EventBusSubscriber(modid="slavicmyths")
public final class GuideQuests {
    public static final String[] TARGETS={"likho_one_eyed","tugarin_zmey","nightingale"};
    private static final String[] ADVANCEMENTS={"name_your_misfortune","steppe_champion","defeat_nightingale"};
    private static final Set<String> TERMINAL=Set.of("DONE","RETURN","UNAVAILABLE","INTERRUPTED");
    private static final Map<MinecraftServer,LinkedHashMap<UUID,Job>> JOBS=new HashMap<>();
    private static final ExecutorService WORKER=Executors.newSingleThreadExecutor(r->{var t=new Thread(r,"slavicmyths-guide-planner");t.setDaemon(true);return t;});
    private static final class Job {
        final UUID player,id;final int target;final BlockPos origin;final long deadline=System.nanoTime()+TimeUnit.SECONDS.toNanos(40);
        int attempt;CompletableFuture<Plan> future;
        Job(ServerPlayer p,int target,UUID id){player=p.getUUID();origin=p.blockPosition().immutable();this.target=target;this.id=id;}
    }
    private record Plan(BlockPos position,ChunkPos owner,UUID camp,BoundingBox box){}
    private static CompoundTag root(ServerPlayer p){return TaleData.compound(TaleData.get(p.serverLevel()).player(p.getUUID()),"GuideQuests");}
    private static CompoundTag quest(ServerPlayer p,int target){return root(p).getCompound(TARGETS[target]);}
    private static String entry(int target){return "slavicmyths:guide_"+TARGETS[target];}
    public static int victories(ServerPlayer p){int n=0;for(String id:ADVANCEMENTS)if(YagaServices.killed(p,id))n++;return n;}
    public static boolean permitted(ServerPlayer p,String giver,BayunEntity cat,int target){
        if(target<0||target>=3||!p.isAlive()||p.isSpectator()||p.level().dimension()!=Level.OVERWORLD||!TaleData.get(p.serverLevel()).compatible())return false;
        if(giver.equals("yaga")){var progress=YagaData.get(p.server.overworld()).progress(p.getUUID());return progress.stage>=1&&!progress.blocked(p.server.overworld().getGameTime());}
        return giver.equals("bayun")&&cat!=null&&cat.level()==p.level()&&cat.canTalk(p)&&cat.trust(p)>=new int[]{25,60,85}[target];
    }
    /** Invoked only after the caller validates the actual NPC menu/session and nonce. */
    public static boolean request(ServerPlayer p,String giver,BayunEntity cat,int target){
        if(!permitted(p,giver,cat,target)){p.displayClientMessage(Component.translatable("guides.locked"),false);return false;}
        long now=p.server.overworld().getGameTime();if(now<p.getPersistentData().getLong("GuideRequestReady"))return false;
        p.getPersistentData().putLong("GuideRequestReady",now+20);
        var old=quest(p,target);reconcile(p);
        if(!old.isEmpty()&&!Set.of("UNAVAILABLE","INTERRUPTED").contains(old.getString("Status"))){
            if(old.getString("Status").equals("RETURN")){if(!giver.equals(old.getString("Giver"))||cat!=null&&(!old.hasUUID("GiverEntity")||!cat.getUUID().equals(old.getUUID("GiverEntity")))){p.displayClientMessage(Component.translatable("guides.return_original"),false);return false;}old.putString("Status","DONE");old.putBoolean("Claimed",true);p.displayClientMessage(Component.translatable("guides.status.DONE"),false);if(cat!=null)cat.rewardTrust(p,5);TaleData.get(p.serverLevel()).setDirty();YagaServices.give(p,new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.ANCIENT_SIGN.get()));}
            else if(old.hasUUID("Id")){var nav=NavigationRecords.get(p.serverLevel());if(nav.player(p.getUUID()).markers.containsKey(old.getUUID("Id"))){nav.player(p.getUUID()).track(old.getUUID("Id"));NavigationManager.changed(p);}}
            sync(p);return true;
        }
        if(old.hasUUID("Id")){NavigationRecords.get(p.serverLevel()).player(p.getUUID()).end(old.getUUID("Id"),false);NavigationManager.endQuest(p,"guide:"+TARGETS[target],false);TaleData.get(p.serverLevel()).player(p.getUUID()).getCompound("Hints").remove(entry(target));NavigationManager.changed(p);}
        if(YagaServices.killed(p,ADVANCEMENTS[target])){p.displayClientMessage(Component.translatable("guides.already_done"),false);return false;}
        var jobs=JOBS.computeIfAbsent(p.server,k->new LinkedHashMap<>());if(jobs.size()>=4){p.displayClientMessage(Component.translatable("guides.busy"),false);return false;}
        var q=new CompoundTag();UUID id=UUID.randomUUID();q.putUUID("Id",id);q.putString("Giver",giver);if(cat!=null)q.putUUID("GiverEntity",cat.getUUID());q.putString("Status","SEARCHING");q.putString("Dimension",Level.OVERWORLD.location().toString());root(p).put(TARGETS[target],q);TaleData.get(p.serverLevel()).setDirty();TaleService.promote(p,entry(target),1,false);
        jobs.put(id,new Job(p,target,id));p.displayClientMessage(Component.translatable("guides.status.SEARCHING"),false);sync(p);return true;
    }
    public static CompoundTag snapshot(ServerPlayer p){
        var out=new CompoundTag();for(String target:TARGETS){var q=root(p).getCompound(target);if(q.isEmpty())continue;var view=new CompoundTag();if(q.hasUUID("Id"))view.putUUID("Marker",q.getUUID("Id"));view.putString("Status",q.getString("Status"));view.putString("Giver",q.getString("Giver"));out.put(target,view);}return out;
    }
    private static void sync(ServerPlayer p){var delta=new CompoundTag();delta.put("GuideQuests",snapshot(p));delta.put("Hints",TaleService.hints(p));TaleNetwork.send(p,delta,false,false);}
    @SubscribeEvent public static void stopping(net.neoforged.neoforge.event.server.ServerStoppingEvent event){var jobs=JOBS.remove(event.getServer());if(jobs!=null)for(var job:jobs.values())if(job.future!=null)job.future.cancel(true);}
    @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event){
        var jobs=JOBS.get(event.getServer());if(jobs==null||jobs.isEmpty())return;
        // One job step per server tick, at most four queued requests. No player/world scans.
        var job=jobs.values().iterator().next();var p=event.getServer().getPlayerList().getPlayer(job.player);
        if(p==null||p.level().dimension()!=Level.OVERWORLD||System.nanoTime()>job.deadline){if(p!=null)finish(p,job,"INTERRUPTED");if(job.future!=null)job.future.cancel(true);jobs.remove(job.id);return;}
        var q=quest(p,job.target);if(!q.hasUUID("Id")||!job.id.equals(q.getUUID("Id"))){jobs.remove(job.id);return;}
        try{
            if(job.attempt==0&&bindExisting(p,job)){jobs.remove(job.id);return;}
            if(job.target<2){
                if(job.attempt>=64){finish(p,job,"UNAVAILABLE");jobs.remove(job.id);return;}
                var kind=job.target==0?BossKind.LIKHO:BossKind.TUGARIN;
                double angle=job.attempt*2.399963229728653;int radius=1200+job.attempt*36;job.attempt++;
                int rx=Math.floorDiv(job.origin.getX()+(int)(Math.cos(angle)*radius),512),rz=Math.floorDiv(job.origin.getZ()+(int)(Math.sin(angle)*radius),512);
                var world=p.server.overworld();if(!world.getWorldBorder().isWithinBounds(new BlockPos(rx*512+256,64,rz*512+256)))return;
                BossAnchors.prepare(world,rx,rz,kind);var boss=HuntRecords.get(world).bosses.get(BossAnchors.key(kind,rx,rz));
                if(usable(boss,p)&&horizontal(job.origin,boss.anchor)>=1200*1200)reserveBoss(p,job,boss);
                if(!q.getString("Status").equals("SEARCHING"))jobs.remove(job.id);
            }else{
                if(job.future==null){job.attempt++;job.future=planCamp(p,job.origin);return;}
                if(!job.future.isDone())return;var plan=job.future.join();
                if(plan==null)finish(p,job,"UNAVAILABLE");else if(!border(p.server.overworld(),plan.box))finish(p,job,"UNAVAILABLE");
                else{
                    q.putLong("Start",plan.owner.toLong());q.putUUID("Camp",plan.camp);q.putLong("Anchor",plan.position.asLong());
                    var chunk=p.server.overworld().getChunkSource().getChunkNow(plan.owner.x,plan.owner.z);
                    var structure=org.slavicmyths.swamp.SwampStructures.get(p.server.overworld(),"bandit_camp_large");
                    var start=chunk==null?null:chunk.getStartForStructure(structure);
                    if(chunk!=null&&(start==null||!start.isValid()))finish(p,job,"UNAVAILABLE");
                    else if(org.slavicmyths.bandit.StrongholdRecords.get(p.server.overworld()).nightingaleDead(plan.camp))finish(p,job,"UNAVAILABLE");
                    else{q.putString("Status","WAITING");guide(p,job.target,q);}
                }jobs.remove(job.id);
            }
        }catch(RuntimeException failure){com.mojang.logging.LogUtils.getLogger().warn("Guide search failed safely for {}",job.id,failure);finish(p,job,"UNAVAILABLE");jobs.remove(job.id);}
    }
    private static double horizontal(BlockPos a,BlockPos b){return Math.pow((double)a.getX()-b.getX(),2)+Math.pow((double)a.getZ()-b.getZ(),2);}
    private static boolean usable(HuntRecords.Boss b,ServerPlayer p){return b!=null&&(!b.active||!p.server.overworld().hasChunkAt(b.anchor)||p.server.overworld().getEntity(b.target)!=null)&&(b.owner==null||b.owner.equals(p.getUUID()))&&(b.active||b.natural&&b.ready<=p.server.overworld().getGameTime())&&p.server.overworld().getWorldBorder().isWithinBounds(b.anchor);}
    private static boolean bindExisting(ServerPlayer p,Job job){
        var world=p.server.overworld();
        if(job.target<2){var kind=job.target==0?BossKind.LIKHO:BossKind.TUGARIN;var nearest=HuntRecords.get(world).bosses.values().stream().limit(4096).filter(b->b.kind==kind&&usable(b,p)).min(Comparator.comparingDouble(b->horizontal(job.origin,b.anchor))).orElse(null);if(nearest!=null){reserveBoss(p,job,nearest);return true;}}
        else{
            var nearest=NavigationRecords.get(world).encounters.values().stream().limit(4096).filter(e->e.type().equals("nightingale")&&e.dimension().equals(Level.OVERWORLD.location())).filter(e->{var entity=world.getEntity(e.entity());return entity instanceof org.slavicmyths.bandit.NightingaleEntity n&&n.camp!=null&&entity.isAlive()&&!org.slavicmyths.bandit.StrongholdRecords.get(world).nightingaleDead(n.camp);}).min(Comparator.comparingDouble(e->horizontal(job.origin,e.anchor()))).orElse(null);
            if(nearest!=null){var q=quest(p,job.target);q.putUUID("Target",nearest.entity());if(world.getEntity(nearest.entity()) instanceof org.slavicmyths.bandit.NightingaleEntity n&&n.camp!=null)q.putUUID("Camp",n.camp);q.putLong("Anchor",nearest.anchor().asLong());q.putString("Status","ACTIVE");guide(p,job.target,q);return true;}
        }return false;
    }
    private static void reserveBoss(ServerPlayer p,Job job,HuntRecords.Boss boss){var q=quest(p,job.target);q.putString("BossKey",boss.key);q.putLong("Anchor",boss.anchor.asLong());q.putString("Status",boss.active?"ACTIVE":"WAITING");if(boss.active&&boss.target!=null)q.putUUID("Target",boss.target);guide(p,job.target,q);}
    private static boolean border(ServerLevel w,BoundingBox box){return w.getWorldBorder().isWithinBounds(new BlockPos(box.minX(),64,box.minZ()))&&w.getWorldBorder().isWithinBounds(new BlockPos(box.maxX(),64,box.maxZ()));}
    private static CompletableFuture<Plan> planCamp(ServerPlayer p,BlockPos origin){
        var w=p.server.overworld();if(!p.server.getWorldData().worldGenOptions().generateStructures())return CompletableFuture.completedFuture(null);
        var structure=org.slavicmyths.swamp.SwampStructures.get(w,"bandit_camp_large");var placement=StructureCandidates.placement(w,structure);if(placement==null)return CompletableFuture.completedFuture(null);
        var registry=w.registryAccess();var generator=w.getChunkSource().getGenerator();var random=w.getChunkSource().randomState();var templates=w.getStructureManager();long seed=w.getSeed();var height=LevelHeightAccessor.create(w.getMinBuildHeight(),w.getHeight());
        return CompletableFuture.supplyAsync(()->{
            org.slavicmyths.chud.ChudSearchBudget.begin(System.nanoTime()+TimeUnit.SECONDS.toNanos(25));
            try{
                var candidates=new ArrayList<ChunkPos>();int gx=Math.floorDiv(origin.getX()>>4,placement.spacing()),gz=Math.floorDiv(origin.getZ()>>4,placement.spacing());
                for(int x=gx-3;x<=gx+3;x++)for(int z=gz-3;z<=gz+3;z++)candidates.add(placement.getPotentialStructureChunk(seed,x*placement.spacing(),z*placement.spacing()));
                candidates.sort(Comparator.comparingDouble(c->horizontal(origin,c.getMiddleBlockPosition(64))));int checked=0;
                for(var chunk:candidates){if(++checked>24)break;org.slavicmyths.chud.ChudSearchBudget.check();if(horizontal(origin,chunk.getMiddleBlockPosition(64))<1200*1200)continue;
                    var context=new Structure.GenerationContext(registry,generator,generator.getBiomeSource(),random,templates,seed,chunk,height,structure.biomes()::contains);
                    try(var samples=StructureCoverageService.sampleTerrain(context)){
                        var start=StructureCoverageService.neighborPlan(context,structure,chunk);if(!start.isValid())continue;
                        for(var piece:start.getPieces())if(piece instanceof org.slavicmyths.bandit.LargeCampPiece large&&large.nightingaleArrival()!=null)return new Plan(large.nightingaleArrival(),chunk,large.campId(),start.getBoundingBox());
                    }
                }return null;
            }finally{org.slavicmyths.chud.ChudSearchBudget.end();}
        },WORKER);
    }
    private static void finish(ServerPlayer p,Job job,String status){p.displayClientMessage(Component.translatable("guides.status."+status),false);quest(p,job.target).putString("Status",status);TaleData.get(p.serverLevel()).setDirty();sync(p);}
    private static void guide(ServerPlayer p,int target,CompoundTag q){
        var pos=BlockPos.of(q.getLong("Anchor"));UUID id=q.getUUID("Id");
        // No invented target UUID: an unmaterialized reservation has only a public search area.
        var area=SearchArea.generate(pos.getX()+.5,pos.getZ()+.5,SearchArea.Scale.WORLD,id,p.server.overworld().getSeed(),0,(x,z,r)->p.server.overworld().getWorldBorder().isWithinBounds(new BlockPos((int)x,64,(int)z)));
        if(area==null){q.putString("Status","UNAVAILABLE");TaleData.get(p.serverLevel()).setDirty();sync(p);return;}
        var nav=NavigationRecords.get(p.serverLevel());var state=nav.player(p.getUUID());boolean newMarker=!state.markers.containsKey(id);
        if(state.upsert(new SlavicMarker(id,"guide:"+id,MarkerCategory.QUEST,SlavicMarker.Kind.SEARCH_AREA,SlavicMarker.Lifetime.UNTIL_QUEST_END,"entity.slavicmyths."+TARGETS[target],Level.OVERWORLD.location(),(int)area.centerX(),0,(int)area.centerZ(),p.server.overworld().getGameTime(),0,area))){if(newMarker&&state.preferences.contains(NavigationState.Preference.AUTO_QUEST))state.track(id);NavigationManager.changed(p);}
        if(q.hasUUID("Target"))NavigationManager.assign(p,new NavigationRecords.Encounter(q.getUUID("Target"),Level.OVERWORLD.location(),pos,TARGETS[target],null),"guide:"+TARGETS[target],id,0,false);
        var hint=new CompoundTag();hint.putString("Dimension",Level.OVERWORLD.location().toString());hint.putDouble("X",area.centerX());hint.putDouble("Z",area.centerZ());hint.putInt("Radius",area.radius());hint.putLong("Expires",Long.MAX_VALUE);hint.putString("Source","entity.slavicmyths."+(q.getString("Giver").equals("yaga")?"baba_yaga":"kot_bayun"));
        double dx=area.centerX()-p.getX(),dz=area.centerZ()-p.getZ();hint.putInt("Direction",Math.floorMod((int)Math.round(Math.atan2(dx,-dz)/(Math.PI/4)),8));hint.putInt("Distance",Math.hypot(dx,dz)<500?0:Math.hypot(dx,dz)<1500?1:2);
        TaleData.compound(TaleData.get(p.serverLevel()).player(p.getUUID()),"Hints").put(entry(target),hint);TaleData.get(p.serverLevel()).setDirty();p.displayClientMessage(Component.translatable("guides.status."+q.getString("Status")),false);sync(p);
    }
    /** Three lookups from existing book/login/proximity events; never generates chunks. */
    public static void reconcile(ServerPlayer p){
        if(!TaleData.get(p.serverLevel()).compatible())return;boolean changed=false;
        for(int target=0;target<3;target++){
            var q=quest(p,target);if(q.isEmpty())continue;String status=q.getString("Status");if(TERMINAL.contains(status))continue;
            if(YagaServices.killed(p,ADVANCEMENTS[target])){q.putString("Status","RETURN");NavigationManager.endQuest(p,"guide:"+TARGETS[target],true);NavigationRecords.get(p.serverLevel()).player(p.getUUID()).end(q.getUUID("Id"),true);TaleData.get(p.serverLevel()).player(p.getUUID()).getCompound("Hints").remove(entry(target));NavigationManager.changed(p);changed=true;continue;}
            if(status.equals("SEARCHING")&&!(JOBS.get(p.server)!=null&&JOBS.get(p.server).containsKey(q.getUUID("Id")))){q.putString("Status","INTERRUPTED");changed=true;continue;}
            if(status.equals("ACTIVE")){
                boolean lost=false;
                if(target<2){var boss=HuntRecords.get(p.server.overworld()).bosses.get(q.getString("BossKey"));lost=boss==null||!boss.active||p.server.overworld().hasChunkAt(boss.anchor)&&p.server.overworld().getEntity(boss.target)==null||!q.hasUUID("Target")||!q.getUUID("Target").equals(boss.target);}
                else if(q.hasUUID("Camp"))lost=org.slavicmyths.bandit.StrongholdRecords.get(p.server.overworld()).nightingaleDead(q.getUUID("Camp"));
                if(lost){q.putString("Status","UNAVAILABLE");NavigationManager.endQuest(p,"guide:"+TARGETS[target],false);NavigationRecords.get(p.serverLevel()).player(p.getUUID()).end(q.getUUID("Id"),false);TaleData.get(p.serverLevel()).player(p.getUUID()).getCompound("Hints").remove(entry(target));NavigationManager.changed(p);changed=true;}
            }
            if(!status.equals("WAITING")||p.level().dimension()!=Level.OVERWORLD)continue;
            if(target<2){var boss=HuntRecords.get(p.server.overworld()).bosses.get(q.getString("BossKey"));if(boss==null){q.putString("Status","UNAVAILABLE");changed=true;}else if(boss.active&&boss.target!=null){q.putUUID("Target",boss.target);q.putLong("Anchor",boss.anchor.asLong());q.putString("Status","ACTIVE");guide(p,target,q);}}
            else if(q.contains("Start")){var at=new ChunkPos(q.getLong("Start"));var chunk=p.server.overworld().getChunkSource().getChunkNow(at.x,at.z);if(chunk==null)continue;var start=chunk.getStartForStructure(org.slavicmyths.swamp.SwampStructures.get(p.server.overworld(),"bandit_camp_large"));
                if(start==null||!start.isValid()||org.slavicmyths.bandit.StrongholdRecords.get(p.server.overworld()).nightingaleDead(q.getUUID("Camp"))){q.putString("Status","UNAVAILABLE");changed=true;}
                else{UUID boss=UUID.nameUUIDFromBytes((q.getUUID("Camp")+":nightingale").getBytes(java.nio.charset.StandardCharsets.UTF_8));if(p.server.overworld().getEntity(boss)!=null){q.putUUID("Target",boss);q.putString("Status","ACTIVE");guide(p,target,q);}}
            }
        }
        if(changed){TaleData.get(p.serverLevel()).setDirty();sync(p);}
    }
    private GuideQuests(){}
}
