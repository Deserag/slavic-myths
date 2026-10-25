package org.slavicmyths.bayun;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class BayunHomes extends SavedData {
    private static final Map<ServerLevel,BayunHomes> LOADED=new java.util.concurrent.ConcurrentHashMap<>();
    private final Map<Long,UUID> homes=new HashMap<>();
    public static synchronized BayunHomes get(ServerLevel w){return LOADED.computeIfAbsent(w,level->level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(BayunHomes::new,BayunHomes::load),"slavicmyths_bayun_homes"));}
    @SubscribeEvent public static void levelLoad(net.neoforged.neoforge.event.level.LevelEvent.Load e){if(e.getLevel() instanceof ServerLevel w)get(w);}
    @SubscribeEvent public static void levelUnload(net.neoforged.neoforge.event.level.LevelEvent.Unload e){if(e.getLevel() instanceof ServerLevel w)LOADED.remove(w);}
    public record Home(BlockPos position,UUID entity){}
    public synchronized Home nearest(BlockPos origin){return homes.entrySet().stream().min(Comparator.comparingDouble(e->BlockPos.of(e.getKey()).distSqr(origin))).map(e->new Home(BlockPos.of(e.getKey()),e.getValue())).orElse(null);}
    /** Atomic natural/on-demand reservation; existing saves keep every original home/UUID. */
    public synchronized boolean reserveNew(BlockPos home,UUID entity){
        var own=homes.get(home.asLong());if(own!=null)return own.equals(entity);
        for(long key:homes.keySet()){var p=BlockPos.of(key);long dx=(long)p.getX()-home.getX(),dz=(long)p.getZ()-home.getZ();if(dx*dx+dz*dz<1024L*1024)return false;}
        homes.put(home.asLong(),entity);setDirty();return true;
    }
    public synchronized boolean reconcile(BlockPos home,UUID entity){UUID old=homes.putIfAbsent(home.asLong(),entity);if(old==null)setDirty();return old==null||old.equals(entity);}
    public synchronized void forget(BlockPos home,UUID entity){if(homes.remove(home.asLong(),entity))setDirty();}
    @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(e.getEntity() instanceof BayunEntity b&&e.getLevel() instanceof ServerLevel w){if(b.home==null)b.home=b.blockPosition();if(!get(w).reconcile(b.home,b.getUUID()))e.setCanceled(true);}}
    @Override public synchronized CompoundTag save(CompoundTag n,HolderLookup.Provider registries){n.putInt("Version",1);ListTag list=new ListTag();homes.forEach((pos,id)->{CompoundTag t=new CompoundTag();t.putLong("Home",pos);t.putUUID("Entity",id);list.add(t);});n.put("Homes",list);return n;}
    public static BayunHomes load(CompoundTag n,HolderLookup.Provider registries){BayunHomes data=new BayunHomes();for(Tag raw:n.getList("Homes",10)){CompoundTag t=(CompoundTag)raw;if(t.hasUUID("Entity"))data.homes.put(t.getLong("Home"),t.getUUID("Entity"));}return data;}
}
