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
    private final Map<Long,UUID> homes=new HashMap<>();
    public static BayunHomes get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(BayunHomes::new,BayunHomes::load),"slavicmyths_bayun_homes");}
    public record Home(BlockPos position,UUID entity){}
    public Home nearest(BlockPos origin){return homes.entrySet().stream().min(Comparator.comparingDouble(e->BlockPos.of(e.getKey()).distSqr(origin))).map(e->new Home(BlockPos.of(e.getKey()),e.getValue())).orElse(null);}
    public boolean reconcile(BlockPos home,UUID entity){UUID old=homes.putIfAbsent(home.asLong(),entity);if(old==null)setDirty();return old==null||old.equals(entity);}
    @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(e.getEntity() instanceof BayunEntity b&&e.getLevel() instanceof ServerLevel w){if(b.home==null)b.home=b.blockPosition();if(!get(w).reconcile(b.home,b.getUUID()))e.setCanceled(true);}}
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider registries){n.putInt("Version",1);ListTag list=new ListTag();homes.forEach((pos,id)->{CompoundTag t=new CompoundTag();t.putLong("Home",pos);t.putUUID("Entity",id);list.add(t);});n.put("Homes",list);return n;}
    public static BayunHomes load(CompoundTag n,HolderLookup.Provider registries){BayunHomes data=new BayunHomes();for(Tag raw:n.getList("Homes",10)){CompoundTag t=(CompoundTag)raw;if(t.hasUUID("Entity"))data.homes.put(t.getLong("Home"),t.getUUID("Entity"));}return data;}
}
