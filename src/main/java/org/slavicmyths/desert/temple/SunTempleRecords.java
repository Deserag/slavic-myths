package org.slavicmyths.desert.temple;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.slavicmyths.desert.HalaContent;
import org.slavicmyths.desert.HalaEntity;

@EventBusSubscriber(modid="slavicmyths")
public final class SunTempleRecords extends SavedData {
    public static final String ORIGIN="SlavicSunTempleOrigin";
    private static final class Site{
        final BlockPos origin;boolean ready,dead;UUID boss;
        Site(BlockPos origin){this.origin=origin.immutable();}
    }
    private final Map<BlockPos,Site> sites=new HashMap<>();
    public static SunTempleRecords get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(SunTempleRecords::new,(n,r)->load(n)),"slavicmyths_sun_temples");}
    boolean reserve(BlockPos o){
        for(Site s:sites.values())if(Math.abs(s.origin.getX()-o.getX())<SunTemplePlan.W&&Math.abs(s.origin.getZ()-o.getZ())<SunTemplePlan.D)return false;
        sites.put(o.immutable(),new Site(o));setDirty();return true;
    }
    void complete(BlockPos o){Site s=sites.get(o);if(s!=null){s.ready=true;setDirty();}}
    void natural(BlockPos o){if(!sites.containsKey(o)&&reserve(o))complete(o);}
    public static boolean bound(Entity e){return e.getPersistentData().contains(ORIGIN);}
    public static BlockPos origin(Entity e){return BlockPos.of(e.getPersistentData().getLong(ORIGIN));}
    public static boolean inside(BlockPos o,BlockPos p){return SunTemplePlan.arena(p.getX()-o.getX(),p.getY()-o.getY(),p.getZ()-o.getZ());}
    public static boolean allows(Entity boss,Entity target){return !bound(boss)||inside(origin(boss),target.blockPosition());}
    public static boolean barVisible(HalaEntity boss,ServerPlayer player){return boss.isAlive()&&!player.isSpectator()&&player.level()==boss.level()&&boss.distanceToSqr(player)<=900&&allows(boss,player);}
    public static BlockPos center(BlockPos o){return o.offset(SunTemplePlan.CX,SunTemplePlan.FLOOR+1,SunTemplePlan.CZ);}
    boolean spawn(ServerLevel w,BlockPos o){
        Site s=sites.get(o);if(s==null||!s.ready||s.dead||s.boss!=null||w.getDifficulty()==Difficulty.PEACEFUL)return false;
        BlockPos at=center(o);
        for(int dx=-16;dx<=16;dx+=16)for(int dz=-16;dz<=16;dz+=16)if(!w.hasChunkAt(at.offset(dx,0,dz)))return false;
        if(!w.getBlockState(at.below()).isSolidRender(w,at.below())||!w.isEmptyBlock(at)||!w.isEmptyBlock(at.above()))return false;
        HalaEntity boss=HalaContent.BOSS.get().create(w);if(boss==null)return false;
        boss.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);boss.setPersistenceRequired();boss.getPersistentData().putLong(ORIGIN,o.asLong());
        if(!w.noCollision(boss)||!w.addFreshEntity(boss))return false;
        s.boss=boss.getUUID();setDirty();return true;
    }
    @SubscribeEvent public static void visit(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.isSpectator()||p.tickCount%20!=0)return;
        var records=get(p.serverLevel());
        for(Site s:records.sites.values())if(s.ready&&!s.dead&&s.boss==null&&inside(s.origin,p.blockPosition()))records.spawn(p.serverLevel(),s.origin);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent e){
        if(e.getEntity() instanceof HalaEntity boss&&boss.level() instanceof ServerLevel w&&bound(boss)){
            var records=get(w);Site s=records.sites.get(origin(boss));
            if(s!=null&&boss.getUUID().equals(s.boss)){s.dead=true;records.setDirty();}
        }
    }
    private static SunTempleRecords load(CompoundTag n){
        SunTempleRecords d=new SunTempleRecords();
        for(Tag tag:n.getList("Sites",10)){CompoundTag t=(CompoundTag)tag;Site s=new Site(BlockPos.of(t.getLong("Origin")));s.ready=t.getBoolean("Ready");s.dead=t.getBoolean("Dead");if(t.hasUUID("Boss"))s.boss=t.getUUID("Boss");d.sites.put(s.origin,s);}
        return d;
    }
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider r){
        ListTag list=new ListTag();
        for(Site s:sites.values()){CompoundTag t=new CompoundTag();t.putLong("Origin",s.origin.asLong());t.putBoolean("Ready",s.ready);t.putBoolean("Dead",s.dead);if(s.boss!=null)t.putUUID("Boss",s.boss);list.add(t);}
        n.put("Sites",list);return n;
    }
}
