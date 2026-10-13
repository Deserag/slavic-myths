package org.slavicmyths.depths;

import java.util.*;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.saveddata.maps.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.*;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.registry.ModItems;

/** One explicit bounded Overworld job per server. No native/random generation or idle world scans. */
@EventBusSubscriber(modid="slavicmyths")
public final class DepthsService {
    private static final org.slf4j.Logger LOG=LogUtils.getLogger();
    private static final TicketType<Long> TICKET=TicketType.create("slavicmyths_depths",Long::compare);
    private static final Map<MinecraftServer,Job> ACTIVE=new WeakHashMap<>();
    private static final int WORK=4096;
    static BlockPos pos(DepthsPlan.Point p){return new BlockPos(p.x(),p.y(),p.z());}
    static BoundingBox box(DepthsPlan.Box b){return new BoundingBox(b.x0(),b.y0(),b.z0(),b.x1(),b.y1(),b.z1());}
    static Component message(String key,Object... arguments){return Component.translatable("depths.slavicmyths."+key,arguments);}
    public static void give(ServerPlayer player,ItemStack stack){if(!player.getInventory().add(stack))player.drop(stack,false);player.containerMenu.broadcastChanges();}
    public static boolean start(ServerPlayer player,ItemStack map){
        if(!player.level().dimension().equals(Level.OVERWORLD)){player.sendSystemMessage(message("dimension"));return false;}
        if(ACTIVE.containsKey(player.server)){player.sendSystemMessage(message("busy"));return false;}
        if(map!=null){UUID old=DepthsMapItem.operation(map);if(old!=null){var e=DepthsSavedData.get(player.serverLevel()).entries.get(old);if(e!=null){player.sendSystemMessage(message(e.status.equals("COMPLETE")?"bound":"failed"));return false;}}}
        var job=new Job(player,map);ACTIVE.put(player.server,job);if(map!=null)DepthsMapItem.pending(map,job.id);
        player.sendSystemMessage(message("searching"));LOG.info("Ancient depths start {} player={} source={}",job.id,player.getUUID(),map==null?"COMMAND":"MAP");return true;
    }
    public static int status(CommandSourceStack source){var job=ACTIVE.get(source.getServer());source.sendSuccess(()->job==null?message("idle"):message("progress",job.candidate,job.phase,job.entry==null?0:job.entry.cursor,job.cells.size()),false);return job==null?0:1;}
    @SubscribeEvent public static void tick(ServerTickEvent.Post event){var job=ACTIVE.get(event.getServer());if(job==null)return;try{job.step();}catch(Exception ex){LOG.error("Ancient depths operation {} failed",job.id,ex);job.fail(true);}}
    @SubscribeEvent public static void started(ServerStartedEvent event){
        var level=event.getServer().getLevel(Level.OVERWORLD);if(level==null)return;var data=DepthsSavedData.get(level);
        for(var e:data.entries.values())if(e.status.equals("PENDING")){
            if(!ACTIVE.containsKey(event.getServer())){try{ACTIVE.put(event.getServer(),new Job(level,e));LOG.info("Ancient depths resume {} phase={} cursor={}",e.plan.id(),e.phase,e.cursor);}catch(RuntimeException ex){e.status="FAILED_NEEDS_OPERATOR";data.setDirty();LOG.error("Cannot restore depths {}",e.plan.id(),ex);}}
            else{e.status="FAILED_NEEDS_OPERATOR";data.setDirty();}
        }
    }
    @SubscribeEvent public static void stopping(ServerStoppingEvent event){var job=ACTIVE.remove(event.getServer());if(job!=null)job.release();}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent event){if(event.getEntity() instanceof ServerPlayer player){var world=player.server.getLevel(Level.OVERWORLD);for(var e:DepthsSavedData.get(world).entries.values())if(e.player.equals(player.getUUID())&&e.status.equals("COMPLETE")&&!e.rewardDelivered)deliver(world,e,player);}}
    static ItemStack boundMap(ServerLevel level,DepthsSavedData.Entry entry){
        if(!entry.status.equals("COMPLETE"))throw new IllegalStateException("map before completion");
        var o=entry.plan.origin();ItemStack map;
        if(entry.mapId<0){
            map=MapItem.create(level,o.x(),o.z(),(byte)2,true,true);entry.mapId=map.get(DataComponents.MAP_ID).id();
            MapItem.renderBiomePreviewMap(level,map);DepthsSavedData.get(level).setDirty();
        }else{map=new ItemStack(Items.FILLED_MAP);map.set(DataComponents.MAP_ID,new MapId(entry.mapId));}
        map.set(DataComponents.CUSTOM_NAME,Component.translatable("item.slavicmyths.ancient_depths_map"));
        map.set(DataComponents.LORE,new ItemLore(List.of(message("coordinates",o.x(),o.y()+1,o.z()),message("bound"))));
        var n=new CompoundTag();n.putUUID("DepthsId",entry.plan.id());n.putString("Dimension","minecraft:overworld");n.putIntArray("Entrance",new int[]{o.x(),o.y()+1,o.z()});n.putString("DepthsState","BOUND");map.set(DataComponents.CUSTOM_DATA,CustomData.of(n));
        MapItemSavedData.addTargetDecoration(map,pos(o),"slavicmyths_depths",MapDecorationTypes.RED_X);
        var data=MapItem.getSavedData(map,level);if(data!=null){data.addDecoration(MapDecorationTypes.RED_X,level,"slavicmyths_depths",o.x(),o.z(),180,message("location"));data.setDirty();}
        return map;
    }
    private static void deliver(ServerLevel level,DepthsSavedData.Entry e,ServerPlayer player){
        var map=boundMap(level,e);boolean replaced=false;
        if(e.source.equals("MAP"))for(int slot=0;slot<player.getInventory().getContainerSize();slot++){
            var used=player.getInventory().getItem(slot);if(e.plan.id().equals(DepthsMapItem.operation(used))){player.getInventory().setItem(slot,map);replaced=true;break;}
        }
        if(!replaced)give(player,map);e.rewardDelivered=true;DepthsSavedData.get(level).setDirty();player.containerMenu.broadcastChanges();
        var o=e.plan.origin();player.sendSystemMessage(message("success",o.x(),o.y()+1,o.z(),Math.round(e.plan.radius())));
        level.playSound(null,player.blockPosition(),SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,SoundSource.PLAYERS,.5F,1);
    }
    private static boolean vegetation(BlockState s){return s.is(BlockTags.LEAVES)||s.is(BlockTags.LOGS)||s.getBlock() instanceof BushBlock||s.is(Blocks.SNOW)||s.is(Blocks.VINE)||s.is(Blocks.MOSS_CARPET);}
    private static int ground(ServerLevel w,int x,int z){
        int y=w.getHeight(Heightmap.Types.WORLD_SURFACE,x,z)-1;
        while(y>w.getMinBuildHeight()&&vegetation(w.getBlockState(new BlockPos(x,y,z))))y--;
        return y;
    }
    private static boolean natural(BlockState s){
        if(s.hasBlockEntity()||s.is(Blocks.BEDROCK)||s.is(BlockTags.BEDS)||s.is(Blocks.END_PORTAL)||s.is(Blocks.END_PORTAL_FRAME)||s.is(Blocks.NETHER_PORTAL))return false;
        return s.isAir()||!s.getFluidState().isEmpty()||vegetation(s)||s.is(BlockTags.BASE_STONE_OVERWORLD)||s.is(BlockTags.DIRT)||s.is(BlockTags.LOGS)||s.is(BlockTags.LEAVES)||
            s.is(BlockTags.COAL_ORES)||s.is(BlockTags.IRON_ORES)||s.is(BlockTags.COPPER_ORES)||s.is(BlockTags.GOLD_ORES)||s.is(BlockTags.REDSTONE_ORES)||s.is(BlockTags.LAPIS_ORES)||s.is(BlockTags.DIAMOND_ORES)||s.is(BlockTags.EMERALD_ORES)||
            s.is(Blocks.GRAVEL)||s.is(Blocks.SAND)||s.is(Blocks.RED_SAND)||s.is(Blocks.SANDSTONE)||s.is(Blocks.RED_SANDSTONE)||s.is(Blocks.CLAY)||s.is(Blocks.CALCITE)||s.is(Blocks.DRIPSTONE_BLOCK)||s.is(Blocks.POINTED_DRIPSTONE)||s.is(Blocks.ICE)||s.is(Blocks.PACKED_ICE)||s.is(Blocks.BLUE_ICE)||s.is(Blocks.SNOW_BLOCK)||s.is(Blocks.MOSS_BLOCK)||s.is(Blocks.ROOTED_DIRT)||s.is(Blocks.GLOW_LICHEN);
    }
    private static final class Job {
        final ServerLevel level;
        final DepthsSavedData data;
        final UUID id,player;
        final DepthsPlan.Point start;
        final String source;
        final long seed,born,wallStart=System.nanoTime();
        final ItemStack mapSource;
        final Set<ChunkPos> tickets=new LinkedHashSet<>();
        final List<ChunkPos> chunks=new ArrayList<>();
        final Map<String,BlockState> states=new HashMap<>();
        List<Map.Entry<DepthsLayout.Cell,String>> cells=List.of();
        DepthsPlan.Point target;
        DepthsPlan plan;
        DepthsSavedData.Entry entry;
        int candidate,loadIndex,scan;
        long changed;
        String phase="CANDIDATE";
        final Map<String,Integer> rejected=new LinkedHashMap<>();
        boolean aboveSurface(){return level.getChunkSource().getGenerator() instanceof FlatLevelSource;}
        boolean resumed;
        Job(ServerPlayer p,ItemStack map){mapSource=map;level=p.serverLevel();data=DepthsSavedData.get(level);id=UUID.randomUUID();player=p.getUUID();start=new DepthsPlan.Point(p.blockPosition().getX(),p.blockPosition().getY(),p.blockPosition().getZ());source=map==null?"COMMAND":"MAP";seed=level.getSeed()^id.getMostSignificantBits()^id.getLeastSignificantBits();born=level.getGameTime();}
        Job(ServerLevel level,DepthsSavedData.Entry e){mapSource=null;this.level=level;data=DepthsSavedData.get(level);entry=e;plan=e.plan;id=plan.id();player=e.player;start=plan.start();source=e.source;seed=plan.seed();born=level.getGameTime();resumed=true;blueprint();prepare(box(plan.bounds()));phase="LOAD_FULL";}
        void tell(String key,Object... args){var p=level.getServer().getPlayerList().getPlayer(player);if(p!=null)p.sendSystemMessage(message(key,args));}
        void release(){for(var p:tickets)level.getChunkSource().removeRegionTicket(TICKET,p,0,p.toLong());tickets.clear();}
        void prepare(BoundingBox b){chunks.clear();loadIndex=0;for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++)chunks.add(new ChunkPos(x,z));if(chunks.size()>96)throw new IllegalStateException("depths footprint exceeds load bound");}
        boolean loaded(){
            for(int count=0;count<4&&loadIndex<chunks.size();count++,loadIndex++){
                var p=chunks.get(loadIndex);if(tickets.add(p))level.getChunkSource().addRegionTicket(TICKET,p,0,p.toLong());
            }
            return loadIndex==chunks.size()&&chunks.stream().allMatch(p->level.getChunkSource().getChunkNow(p.x,p.z)!=null);
        }
        void next(){release();plan=null;cells=List.of();scan=0;phase="CANDIDATE";}
        void reject(String reason){rejected.merge(reason,1,Integer::sum);next();}
        Component rejectionDetails(){var result=Component.empty();rejected.forEach((reason,count)->{if(!result.getString().isEmpty())result.append("; ");result.append(message("reason_"+reason)).append(" ×"+count);});return result;}
        boolean border(BoundingBox b){return level.getWorldBorder().isWithinBounds(new BlockPos(b.minX(),0,b.minZ()))&&level.getWorldBorder().isWithinBounds(new BlockPos(b.maxX(),0,b.maxZ()));}
        void blueprint(){
            cells=new ArrayList<>(DepthsLayout.build(plan).entrySet());
            cells.sort(Comparator.<Map.Entry<DepthsLayout.Cell,String>>comparingInt(e->order(e.getValue())).thenComparingInt(e->e.getKey().u()).thenComparingInt(e->-e.getKey().h()).thenComparingInt(e->e.getKey().v()));
        }
        int order(String s){if(s.equals("air"))return 1;if(s.startsWith("lantern")||s.startsWith("moss_carpet")||s.startsWith("pointed_dripstone")||s.startsWith("cobweb"))return 3;return s.startsWith("chain")||s.contains("stairs")||s.contains("wall")||s.contains("fence")?2:0;}
        BlockState state(String token){return states.computeIfAbsent(token,key->{try{return BlockStateParser.parseForBlock(level.registryAccess().lookupOrThrow(Registries.BLOCK),"minecraft:"+key,false).blockState();}catch(com.mojang.brigadier.exceptions.CommandSyntaxException ex){throw new IllegalStateException("invalid block "+key,ex);}});}
        void step(){
            if(level.getGameTime()-born>2400){tell("timeout",candidate,phase);fail(entry!=null,true);return;}
            if((level.getGameTime()-born)%100==0)tell("progress",candidate,phase,entry==null?0:entry.cursor,cells.size());
            switch(phase){
                case "CANDIDATE"->{
                    if(candidate>=16){fail(false);return;}target=DepthsPlan.candidate(start,seed,candidate++);
                    double r=Math.hypot(target.x()-start.x(),target.z()-start.z());if(r<1500||r>2000)return;
                    var b=new BoundingBox(target.x()-7,level.getMinBuildHeight(),target.z()-7,target.x()+7,level.getMaxBuildHeight()-1,target.z()+7);
                    if(!border(b)){reject("border");return;}prepare(b);phase="LOAD_ENTRANCE";
                }
                case "LOAD_ENTRANCE"->{if(loaded())phase="SURVEY";}
                case "SURVEY"->{
                    int min=Integer.MAX_VALUE,max=Integer.MIN_VALUE;
                    for(int dx:new int[]{-6,0,6})for(int dz:new int[]{-6,0,6}){
                        int y=ground(level,target.x()+dx,target.z()+dz);var s=level.getBlockState(new BlockPos(target.x()+dx,y,target.z()+dz));
                        if(!s.isSolid()||!s.getFluidState().isEmpty()||s.is(Blocks.ICE)||!level.getFluidState(new BlockPos(target.x()+dx,y+1,target.z()+dz)).isEmpty()){reject("wet");return;}
                        min=Math.min(min,y);max=Math.max(max,y);
                    }
                    if(max-min>5){reject("slope");return;}
                    target=new DepthsPlan.Point(target.x(),DepthsPlan.placementOriginY(aboveSurface()?max:ground(level,target.x(),target.z()),aboveSurface()),target.z());
                    plan=new DepthsPlan(id,start,target,DepthsPlan.outward(start,target),seed,false,0);var b=box(plan.bounds());
                    if(!border(b)){reject("border");return;}
                    if(b.minY()<=level.getMinBuildHeight()||b.maxY()>=level.getMaxBuildHeight()){reject("height");return;}
                    if(data.overlaps(plan.bounds())){reject("occupied");return;}
                    prepare(b);phase="LOAD_FULL";
                }
                case "LOAD_FULL"->{if(loaded())phase=resumed?"RESUME":"ROOFS";}
                case "ROOFS"->{
                    if(!org.slavicmyths.worldgen.ManualStructureRecords.get(level).overlapping(box(plan.bounds())).isEmpty()||!org.slavicmyths.kurgan.BurialRecords.get(level).overlapping(box(plan.bounds())).isEmpty()){reject("occupied");return;}
                    // Local roof probes only, not a flat-surface requirement over the whole dungeon.
                    if(!aboveSurface())for(int u:new int[]{45,54,64,92,104,115,135,156,177})for(int v:new int[]{-7,0,7}){
                        var w=plan.world(u,0,v);if(ground(level,w.x(),w.z())<plan.origin().y()){reject("cover");return;}
                    }
                    for(var chunk:chunks){var full=level.getChunkSource().getChunkNow(chunk.x,chunk.z);
                        for(var nativeStart:full.getAllStarts().values())if(nativeStart.isValid()&&nativeStart.getBoundingBox().intersects(box(plan.bounds()))){reject("occupied");return;}
                        // Native references are cheap conservative evidence of another occupied footprint.
                        if(!aboveSurface()&&full.getAllReferences().values().stream().anyMatch(ref->!ref.isEmpty())){reject("occupied");return;}
                    }
                    if(aboveSurface()){blueprint();phase="PREFLIGHT";tell("flat",target.x(),target.y()+1,target.z());break;}
                    boolean sky=true;int skyTop=Integer.MIN_VALUE;
                    skyCheck:for(int u=161;u<=169;u++)for(int v=15;v<=23;v++){
                        var w=plan.world(u,0,v);int top=level.getHeight(Heightmap.Types.WORLD_SURFACE,w.x(),w.z())-1;skyTop=Math.max(skyTop,top-plan.origin().y());
                        if(top-(plan.origin().y()-2)>12||level.getHeight(Heightmap.Types.WORLD_SURFACE,w.x(),w.z())-(plan.origin().y()-3)>16){sky=false;break skyCheck;}
                        for(int y=plan.origin().y()-3;y<=level.getHeight(Heightmap.Types.WORLD_SURFACE,w.x(),w.z());y++){
                            var s=level.getBlockState(new BlockPos(w.x(),y,w.z()));if(!natural(s)||!s.getFluidState().isEmpty()||s.is(Blocks.SAND)||s.is(Blocks.GRAVEL)){sky=false;break skyCheck;}
                        }
                    }
                    plan=new DepthsPlan(id,start,target,plan.direction(),seed,sky,sky?skyTop:0);if(!border(box(plan.bounds()))||plan.bounds().y1()>=level.getMaxBuildHeight()||data.overlaps(plan.bounds())){plan=new DepthsPlan(id,start,target,plan.direction(),seed,false,0);}
                    blueprint();phase="PREFLIGHT";
                }
                case "PREFLIGHT"->{
                    for(int count=0;count<WORK&&scan<cells.size();count++,scan++){
                        var cell=cells.get(scan).getKey();var b=pos(plan.world(cell.u(),cell.h(),cell.v()));var old=level.getBlockState(b);
                        if(!natural(old)||(cell.u()<=10&&!old.getFluidState().isEmpty())){reject("protected");return;}
                    }
                    if(scan==cells.size()){
                        entry=new DepthsSavedData.Entry(plan,player,source);data.entries.put(id,entry);data.setDirty();phase="BLOCKS";
                        LOG.info("Ancient depths selected {} origin={} direction={} top={} cells={}",id,target,plan.direction(),plan.skylight()?"SKYLIGHT":"CLOSED",cells.size());
                    }
                }
                case "RESUME"->{phase=entry.phase;resumed=false;if(!Set.of("BLOCKS","CHECK","CHESTS","MOBS").contains(phase))throw new IllegalStateException("unknown saved phase");}
                case "BLOCKS"->{
                    for(int count=0;count<WORK&&entry.cursor<cells.size();count++,entry.cursor++){
                        var cell=cells.get(entry.cursor);var at=pos(plan.world(cell.getKey().u(),cell.getKey().h(),cell.getKey().v()));var desired=state(cell.getValue());var old=level.getBlockState(at);
                        if(old.equals(desired))continue;
                        if(old.hasBlockEntity()||old.is(Blocks.BEDROCK)||old.is(BlockTags.BEDS)||old.is(Blocks.END_PORTAL)||old.is(Blocks.END_PORTAL_FRAME)||old.is(Blocks.NETHER_PORTAL))throw new IllegalStateException("protected block changed during construction");
                        if(!level.setBlock(at,desired,2))throw new IllegalStateException("block placement rejected");changed++;
                    }
                    data.setDirty();if(entry.cursor==cells.size()){entry.phase=phase="CHECK";scan=0;}
                }
                case "CHECK"->{
                    for(int count=0;count<WORK&&scan<cells.size();count++,scan++){
                        var cell=cells.get(scan);var c=cell.getKey();if(cell.getValue().equals("air")&&(Math.abs(c.v())<=1||c.u()>=142&&c.u()<=170&&Math.abs(c.v())<=14))
                            if(!level.getBlockState(pos(plan.world(c.u(),c.h(),c.v()))).isAir())throw new IllegalStateException("blocked completed path");
                    }
                    if(scan==cells.size()){entry.phase=phase="CHESTS";data.setDirty();}
                }
                case "CHESTS"->{for(int i=0;i<2;i++)if((entry.chests&(1<<i))==0){chest(i);entry.chests|=1<<i;data.setDirty();}entry.phase=phase="MOBS";data.setDirty();}
                case "MOBS"->{
                    for(int i=0;i<DepthsPlan.SPAWNS.size();i++)if((entry.mobs&(1<<i))==0){spawn(i);entry.mobs|=1<<i;data.setDirty();}
                    org.slavicmyths.earth.EarthArena.install(level,plan);
                    entry.status="COMPLETE";entry.phase="COMPLETE";data.setDirty();
                    var p=level.getServer().getPlayerList().getPlayer(player);if(p!=null)deliver(level,entry,p);
                    LOG.info("Ancient depths complete {} origin={} edits={} elapsedMs={}",id,plan.origin(),changed,(System.nanoTime()-wallStart)/1_000_000);finish();
                }
                default->throw new IllegalStateException("phase "+phase);
            }
        }
        void chest(int index){
            var anchor=DepthsPlan.CHESTS.get(index);var at=pos(plan.world(anchor.x(),anchor.y(),anchor.z()));
            if(level.getBlockEntity(at) instanceof ChestBlockEntity old&&id.toString().equals(old.getPersistentData().getString("DepthsId")))return;
            if(level.getBlockEntity(at)!=null)throw new IllegalStateException("foreign loot container");
            level.setBlock(at,state("chest[facing="+plan.facing(true)+"]"),2);
            if(!(level.getBlockEntity(at) instanceof ChestBlockEntity chest))throw new IllegalStateException("missing chest");
            chest.getPersistentData().putString("DepthsId",id.toString());var r=new Random(seed^index*773L);int slot=0;
            if(index==0){chest.setItem(slot++,new ItemStack(Items.IRON_INGOT,1+r.nextInt(3)));chest.setItem(slot++,new ItemStack(Items.TORCH,2+r.nextInt(4)));chest.setItem(slot++,new ItemStack(Items.BREAD,1+r.nextInt(2)));chest.setItem(slot++,new ItemStack(Items.GOLD_NUGGET,1+r.nextInt(3)));}
            else{boolean goldIngot=r.nextBoolean();chest.setItem(slot++,new ItemStack(goldIngot?Items.GOLD_INGOT:Items.GOLD_NUGGET,goldIngot?2+r.nextInt(3):5+r.nextInt(5)));chest.setItem(slot++,new ItemStack(Items.AMETHYST_SHARD,2+r.nextInt(3)));chest.setItem(slot++,new ItemStack(Items.IRON_INGOT,1+r.nextInt(2)));}
            if(r.nextFloat()<.3F)chest.setItem(slot,new ItemStack(ModItems.ANCIENT_COIN.get(),index==0?1+r.nextInt(2):1));chest.setChanged();
        }
        void spawn(int index){
            UUID entityId=UUID.nameUUIDFromBytes((id+":mob:"+index).getBytes(java.nio.charset.StandardCharsets.UTF_8));if(level.getEntity(entityId)!=null)return;
            var s=DepthsPlan.SPAWNS.get(index);var at=pos(plan.world(s.u(),DepthsPlan.floor(s.u())+1,s.v()));
            Mob mob=s.type().equals("zombie")?EntityType.ZOMBIE.create(level):EntityType.SKELETON.create(level);if(mob==null)throw new IllegalStateException("entity factory");
            mob.setUUID(entityId);boolean safe=false;
            for(int[] offset:new int[][]{{0,0},{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1},{2,0},{-2,0},{0,2},{0,-2}}){
                int u=s.u()+offset[0],v=s.v()+offset[1];
                int low=s.u()<=43?7:s.u()<=65?44:s.u()<=90?66:s.u()<=116?91:133;
                int high=s.u()<=43?43:s.u()<=65?65:s.u()<=90?90:s.u()<=116?116:179;
                int radius=s.u()<=43?2:s.u()<=65?10:s.u()<=90?5:s.u()<=116?12:21;
                if(u<low||u>high||Math.abs(v)>radius)continue;
                at=pos(plan.world(u,DepthsPlan.floor(u)+1,v));mob.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,plan.direction()*90,0);
                if(level.noCollision(mob)&&level.getBlockState(at.below()).isSolid()&&level.getEntities(mob,mob.getBoundingBox()).isEmpty()){safe=true;break;}
            }
            if(!safe)throw new IllegalStateException("unsafe planned spawn "+index);
            mob.setPersistenceRequired();mob.setCanPickUpLoot(false);mob.getPersistentData().putUUID("DepthsInstanceId",id);
            if(mob instanceof Zombie z)z.setBaby(false);if(mob instanceof Skeleton)mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));
            if(s.guardian()>0){
                mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(s.guardian()==1?50:44);mob.setHealth(s.guardian()==1?50:44);
                mob.setCustomName(message(s.guardian()==1?"guardian_stone":"guardian_depths"));mob.setCustomNameVisible(true);
                mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.IRON_HELMET));mob.setItemSlot(EquipmentSlot.CHEST,new ItemStack(s.guardian()==1?Items.IRON_CHESTPLATE:Items.CHAINMAIL_CHESTPLATE));
                if(s.guardian()==1)mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_AXE));
                for(var equipment:EquipmentSlot.values())if(equipment.getType()==EquipmentSlot.Type.HAND||equipment.getType()==EquipmentSlot.Type.HUMANOID_ARMOR)mob.setDropChance(equipment,0);
                mob.setItemSlot(EquipmentSlot.OFFHAND,new ItemStack(ModItems.ANCIENT_COIN.get()));mob.setDropChance(EquipmentSlot.OFFHAND,1);
            }
            if(!level.addFreshEntity(mob))throw new IllegalStateException("entity rejected "+index);
        }
        void fail(boolean partial){
            fail(partial,false);
        }
        void fail(boolean partial,boolean timeout){
            if(entry==null&&source.equals("MAP")){
                if(mapSource!=null)DepthsMapItem.unbound(mapSource);
                var p=level.getServer().getPlayerList().getPlayer(player);
                if(p!=null)for(int slot=0;slot<p.getInventory().getContainerSize();slot++){var stack=p.getInventory().getItem(slot);if(id.equals(DepthsMapItem.operation(stack)))DepthsMapItem.unbound(stack);}
            }
            if(entry!=null){entry.status="FAILED_NEEDS_OPERATOR";data.setDirty();}
            if(partial)tell("failed");else if(!timeout)tell("no_site_details",candidate,rejectionDetails());
            LOG.warn("Ancient depths failed {} phase={} candidates={} partial={} timeout={} rejected={}",id,phase,candidate,partial,timeout,rejected);finish();}
        void finish(){release();ACTIVE.remove(level.getServer());}
    }
    private DepthsService(){}
}
