package org.slavicmyths.dungeon;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Persistent protection for generated geometry, including across server restarts. */
@EventBusSubscriber(modid="slavicmyths")
public final class DungeonRecords extends SavedData {
    private record Site(BlockPos origin,long seed,int floors,DungeonPlan plan,boolean complete){}
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
        sites.add(new Site(origin.immutable(),plan.seed,plan.floors.size(),plan,false));setDirty();return true;
    }
    void registerNatural(BlockPos origin,DungeonPlan plan){
        if(sites.stream().anyMatch(s->s.origin.equals(origin)))return;
        if(reserve(origin,plan))complete(origin);
    }
    void complete(BlockPos origin){
        for(int i=0;i<sites.size();i++){Site s=sites.get(i);if(s.origin.equals(origin)){sites.set(i,new Site(s.origin,s.seed,s.floors,s.plan,true));setDirty();return;}}
    }
    public boolean contains(BlockPos p){
        for(Site s:sites){int x=p.getX()-s.origin.getX(),y=p.getY()-s.origin.getY(),z=p.getZ()-s.origin.getZ();
            for(var b:s.plan.boxes)if(b.contains(x,y,z))return true;
        }return false;
    }
    @SubscribeEvent public static void breakBlock(BlockEvent.BreakEvent e){
        if(e.getLevel() instanceof ServerLevel w&&!e.getPlayer().isCreative()&&get(w).contains(e.getPos()))e.setCanceled(true);
    }
    private static DungeonRecords load(CompoundTag n){
        DungeonRecords d=new DungeonRecords();
        for(Tag t:n.getList("Sites",10)){CompoundTag s=(CompoundTag)t;long seed=s.getLong("Seed");int floors=s.getInt("Floors");
            d.sites.add(new Site(BlockPos.of(s.getLong("Origin")),seed,floors,DungeonPlan.create(seed,floors),s.getBoolean("Complete")));
        }return d;
    }
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider r){
        ListTag list=new ListTag();for(Site s:sites){CompoundTag t=new CompoundTag();t.putLong("Origin",s.origin.asLong());t.putLong("Seed",s.seed);t.putInt("Floors",s.floors);t.putBoolean("Complete",s.complete);list.add(t);}n.put("Sites",list);return n;
    }
}
