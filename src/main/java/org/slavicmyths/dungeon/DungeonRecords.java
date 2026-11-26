package org.slavicmyths.dungeon;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Persistent geometry and encounter state. Unloaded entities are never counted as kills. */
@EventBusSubscriber(modid="slavicmyths")
public final class DungeonRecords extends SavedData {
    private static final class Fight {
        final List<UUID> spawned=new ArrayList<>();
        final Set<UUID> dead=new HashSet<>();
        boolean cleared(){return !spawned.isEmpty()&&dead.containsAll(spawned);}
    }
    private record Site(BlockPos origin,long seed,int floors,DungeonPlan plan,boolean complete,Map<Integer,Fight> fights){}
    private final List<Site> sites=new ArrayList<>();
    public static DungeonRecords get(ServerLevel w){
        return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(DungeonRecords::new,(n,r)->load(n)),"slavicmyths_trial_dungeons");
    }
    private static DungeonPlan.Box absolute(BlockPos p,DungeonPlan.Box b){
        return new DungeonPlan.Box(b.x0()+p.getX(),b.y0()+p.getY(),b.z0()+p.getZ(),b.x1()+p.getX(),b.y1()+p.getY(),b.z1()+p.getZ(),b.kind(),b.floor());
    }
    boolean reserve(BlockPos origin,DungeonPlan plan){
        for(Site site:sites)for(var a:plan.boxes)for(var b:site.plan.boxes)
            if(absolute(origin,a).overlaps(absolute(site.origin,b)))return false;
        sites.add(new Site(origin.immutable(),plan.seed,plan.floors.size(),plan,false,new HashMap<>()));setDirty();return true;
    }
    void registerNatural(BlockPos origin,DungeonPlan plan){
        if(sites.stream().anyMatch(s->s.origin.equals(origin)))return;
        if(reserve(origin,plan))complete(origin);
    }
    void complete(BlockPos origin){
        for(int i=0;i<sites.size();i++){Site s=sites.get(i);if(s.origin.equals(origin)){sites.set(i,new Site(s.origin,s.seed,s.floors,s.plan,true,s.fights));setDirty();return;}}
    }
    public boolean contains(BlockPos p){
        for(Site s:sites){int x=p.getX()-s.origin.getX(),y=p.getY()-s.origin.getY(),z=p.getZ()-s.origin.getZ();
            for(var b:s.plan.boxes)if(b.contains(x,y,z))return true;
        }return false;
    }
    private void openGate(ServerLevel w,Site s,int floor){
        if(floor+1>=s.floors)return;
        for(var b:s.plan.boxes)if(b.kind()==DungeonPlan.Kind.SHAFT&&b.floor()==floor+1){
            for(int x=b.x0()+1;x<b.x1();x++)for(int z=b.z0()+1;z<b.z1();z++){
                BlockPos p=s.origin.offset(x,b.y1(),z);if(!w.hasChunkAt(p))continue;
                var state=x==b.x0()+2&&z==b.z0()+1?DungeonBuild.ladder():Blocks.AIR.defaultBlockState();
                if(!w.getBlockState(p).equals(state))w.setBlock(p,state,Block.UPDATE_CLIENTS);
            }
        }
    }
    void defeated(ServerLevel w,BlockPos origin,int floor,UUID entity){
        for(Site s:sites)if(s.origin.equals(origin)){
            Fight fight=s.fights.get(floor);
            if(fight==null||!fight.spawned.contains(entity)||!fight.dead.add(entity))return;
            setDirty();if(fight.cleared())openGate(w,s,floor);return;
        }
    }
    private void activate(ServerPlayer player,Site s,DungeonPlan.Floor f){
        ServerLevel w=player.serverLevel();
        Fight existing=s.fights.get(f.index());
        if(existing!=null){if(existing.cleared())openGate(w,s,f.index());return;}
        if(w.getDifficulty()==Difficulty.PEACEFUL)return;
        if(f.index()>0){
            Fight previous=s.fights.get(f.index()-1);
            if(previous==null||!previous.cleared())return;
        }
        var arena=f.rooms().get(f.boss());
        int x=(arena.x0()+arena.x1())/2,z=(arena.z0()+arena.z1())/2,y=arena.y0()+1;
        List<Mob> prepared=new ArrayList<>();UUID group=UUID.randomUUID();
        int count=f.bosses().size();
        for(int i=0;i<count;i++){
            int dx=count==1?0:i==0?-13:13;
            BlockPos at=s.origin.offset(x+dx,y,z+9);
            if(!w.hasChunkAt(at)||!w.isEmptyBlock(at)||!w.isEmptyBlock(at.above()))return;
            Mob boss=DungeonGuardians.create(w,f.bosses().get(i),at,s.origin,f.index(),group);
            if(boss==null)return;
            prepared.add(boss);
        }
        Fight fight=new Fight();
        for(Mob boss:prepared){
            if(!w.addFreshEntity(boss)){
                for(Mob other:prepared)if(fight.spawned.contains(other.getUUID()))other.discard();
                return;
            }
            fight.spawned.add(boss.getUUID());boss.setTarget(player);
        }
        s.fights.put(f.index(),fight);setDirty();
        player.displayClientMessage(Component.literal("Хранитель подземелий: победите всех боссов этажа"),true);
    }
    private void visit(ServerPlayer player){
        BlockPos p=player.blockPosition();
        for(Site s:sites){
            if(!s.complete)continue;
            int x=p.getX()-s.origin.getX(),y=p.getY()-s.origin.getY(),z=p.getZ()-s.origin.getZ();
            for(var f:s.plan.floors){
                var arena=f.rooms().get(f.boss());
                if(arena.contains(x,y,z)&&x>arena.x0()+2&&x<arena.x1()-2&&z>arena.z0()+2&&z<arena.z1()-2)activate(player,s,f);
            }
        }
    }
    @SubscribeEvent public static void playerTick(PlayerTickEvent.Post e){
        if(e.getEntity() instanceof ServerPlayer p&&!p.isSpectator()&&!p.isCreative()&&p.tickCount%20==0)get(p.serverLevel()).visit(p);
    }
    @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent e){
        if(e.getLevel() instanceof ServerLevel w&&!e.getPlayer().isCreative()&&get(w).contains(e.getPos()))e.setCanceled(true);
    }
    @SubscribeEvent public static void explosion(ExplosionEvent.Detonate e){
        if(e.getLevel() instanceof ServerLevel w)e.getAffectedBlocks().removeIf(get(w)::contains);
    }
    private static DungeonRecords load(CompoundTag n){
        DungeonRecords d=new DungeonRecords();
        for(Tag t:n.getList("Sites",10)){
            CompoundTag s=(CompoundTag)t;long seed=s.getLong("Seed");int floors=s.getInt("Floors");
            Map<Integer,Fight> fights=new HashMap<>();
            for(Tag value:s.getList("Fights",10)){
                CompoundTag f=(CompoundTag)value;Fight fight=new Fight();
                for(Tag id:f.getList("Spawned",8))fight.spawned.add(UUID.fromString(id.getAsString()));
                for(Tag id:f.getList("Dead",8))fight.dead.add(UUID.fromString(id.getAsString()));
                fights.put(f.getInt("Floor"),fight);
            }
            d.sites.add(new Site(BlockPos.of(s.getLong("Origin")),seed,floors,DungeonPlan.create(seed,floors),s.getBoolean("Complete"),fights));
        }return d;
    }
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider r){
        ListTag list=new ListTag();
        for(Site s:sites){
            CompoundTag t=new CompoundTag();t.putLong("Origin",s.origin.asLong());t.putLong("Seed",s.seed);t.putInt("Floors",s.floors);t.putBoolean("Complete",s.complete);
            ListTag fights=new ListTag();
            for(var entry:s.fights.entrySet()){
                CompoundTag f=new CompoundTag();f.putInt("Floor",entry.getKey());ListTag spawned=new ListTag(),dead=new ListTag();
                for(UUID id:entry.getValue().spawned)spawned.add(StringTag.valueOf(id.toString()));
                for(UUID id:entry.getValue().dead)dead.add(StringTag.valueOf(id.toString()));
                f.put("Spawned",spawned);f.put("Dead",dead);fights.add(f);
            }
            t.put("Fights",fights);list.add(t);
        }
        n.put("Sites",list);return n;
    }
}
