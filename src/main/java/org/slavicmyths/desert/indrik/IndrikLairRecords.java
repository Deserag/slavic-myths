package org.slavicmyths.desert.indrik;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.desert.IndrikContent;
import org.slavicmyths.desert.IndrikEntity;
import static org.slavicmyths.desert.indrik.IndrikLairPlan.*;

@EventBusSubscriber(modid="slavicmyths")
public final class IndrikLairRecords extends SavedData {
    private static final String ORIGIN="SlavicIndrikLairOrigin";
    private static final class Site {
        final BlockPos o; UUID boss; boolean dead;
        Site(BlockPos o){this.o=o.immutable();}
    }
    private final Map<BlockPos,Site> sites=new HashMap<>();
    public static IndrikLairRecords get(ServerLevel w){
        return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(IndrikLairRecords::new,(n,r)->load(n)),"slavicmyths_indrik_lairs");
    }
    void register(BlockPos o){if(!sites.containsKey(o)){sites.put(o.immutable(),new Site(o));setDirty();}}
    boolean overlaps(BlockPos o){
        var b=IndrikLairBuild.bounds(o);
        for(Site s:sites.values())if(b.intersects(IndrikLairBuild.bounds(s.o)))return true;
        return false;
    }
    Optional<BlockPos> nearest(BlockPos p){
        return sites.keySet().stream().min(Comparator.comparingDouble(o->o.distSqr(p)));
    }
    private boolean spawn(ServerLevel w,Site s){
        if(s.dead||s.boss!=null||w.getDifficulty()==Difficulty.PEACEFUL||!IndrikLairBuild.loaded(w,IndrikLairBuild.bounds(s.o)))return false;
        BlockPos at=s.o.offset(0,FLOOR+1,ARENA_Z);
        if(!w.getBlockState(at.below()).isSolidRender(w,at.below()))return false;
        var q=IndrikContent.BOSS.get().create(w);if(q==null)return false;
        q.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);
        q.getPersistentData().putLong(ORIGIN,s.o.asLong());q.setPersistenceRequired();
        if(!w.noCollision(q)||!w.addFreshEntity(q))return false;
        s.boss=q.getUUID();setDirty();return true;
    }
    @SubscribeEvent public static void visit(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.isSpectator()||p.tickCount%20!=0)return;
        ServerLevel w=p.serverLevel();if(w.getDifficulty()==Difficulty.PEACEFUL)return;
        var data=get(w);
        for(Site s:data.sites.values()){
            if(s.dead||s.boss!=null)continue;
            int x=p.getBlockX()-s.o.getX(),y=p.getBlockY()-s.o.getY(),z=p.getBlockZ()-s.o.getZ();
            if(Math.abs(x)<=RADIUS&&Math.abs(z-ARENA_Z)<=RADIUS&&arena(x,z)&&y>FLOOR&&y<-3)data.spawn(w,s);
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent e){
        if(e.getEntity() instanceof IndrikEntity q&&q.level() instanceof ServerLevel w&&q.getPersistentData().contains(ORIGIN)){
            var data=get(w);Site s=data.sites.get(BlockPos.of(q.getPersistentData().getLong(ORIGIN)));
            if(s!=null&&q.getUUID().equals(s.boss)){s.dead=true;data.setDirty();}
        }
    }
    private static IndrikLairRecords load(CompoundTag n){
        var d=new IndrikLairRecords();
        for(Tag tag:n.getList("Sites",10)){CompoundTag t=(CompoundTag)tag;Site s=new Site(BlockPos.of(t.getLong("Origin")));
            s.dead=t.getBoolean("Dead");if(t.hasUUID("Boss"))s.boss=t.getUUID("Boss");d.sites.put(s.o,s);}
        return d;
    }
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider registries){
        ListTag l=new ListTag();for(Site s:sites.values()){CompoundTag t=new CompoundTag();t.putLong("Origin",s.o.asLong());
            t.putBoolean("Dead",s.dead);if(s.boss!=null)t.putUUID("Boss",s.boss);l.add(t);}n.put("Sites",l);return n;
    }
}
