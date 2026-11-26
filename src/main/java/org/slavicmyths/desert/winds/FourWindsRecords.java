package org.slavicmyths.desert.winds;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.desert.StormIdolContent;
import org.slavicmyths.desert.StormIdolEntity;
import static org.slavicmyths.desert.winds.FourWindsPlan.*;

@EventBusSubscriber(modid="slavicmyths")
public final class FourWindsRecords extends SavedData {
    private static final String ORIGIN="SlavicFourWindsOrigin";
    private static final class Site {
        final BlockPos o;
        UUID boss;
        boolean dead;
        Site(BlockPos origin){o=origin.immutable();}
    }
    private final Map<BlockPos,Site> sites=new HashMap<>();
    public static FourWindsRecords get(ServerLevel w){
        return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(FourWindsRecords::new,(n,r)->load(n)),"slavicmyths_four_winds_palaces");
    }
    void register(BlockPos o){
        if(!sites.containsKey(o)){sites.put(o.immutable(),new Site(o));setDirty();}
    }
    boolean overlaps(BlockPos o){
        var box=FourWindsBuild.bounds(o);
        for(Site s:sites.values()){
            var b=FourWindsBuild.bounds(s.o);
            if(box.minX()<=b.maxX()&&box.maxX()>=b.minX()&&box.minZ()<=b.maxZ()&&box.maxZ()>=b.minZ())return true;
        }
        return false;
    }
    public static void bind(Entity boss,BlockPos origin){boss.getPersistentData().putLong(ORIGIN,origin.asLong());}
    public static boolean bound(Entity boss){return boss.getPersistentData().contains(ORIGIN);}
    private static BlockPos origin(Entity boss){return BlockPos.of(boss.getPersistentData().getLong(ORIGIN));}
    private static boolean inside(BlockPos o,Entity e){
        return encounter(e.getX()-o.getX()-.5,e.getY()-o.getY(),e.getZ()-o.getZ()-.5);
    }
    public static boolean allows(Entity boss,Entity target){return !bound(boss)||target!=null&&inside(origin(boss),target);}
    public static Vec3 constrain(Entity boss,Vec3 dest){
        if(!bound(boss))return dest;
        BlockPos o=origin(boss);double cx=o.getX()+.5,cz=o.getZ()+.5;
        double dx=dest.x-cx,dz=dest.z-cz,r=Math.hypot(dx,dz),scale=r>15?15/r:1;
        return new Vec3(cx+dx*scale,Math.max(o.getY()+FLOOR+1,Math.min(o.getY()+FLOOR+8,dest.y)),cz+dz*scale);
    }
    private boolean spawn(ServerLevel w,Site s){
        if(s.dead||s.boss!=null||w.getDifficulty()==Difficulty.PEACEFUL)return false;
        // Wait for the entire combat platform, not merely the chunk with the spawn position.
        for(int x=(s.o.getX()+MIN_X)>>4;x<=(s.o.getX()+MAX_X)>>4;x++)
            for(int z=(s.o.getZ()-23)>>4;z<=(s.o.getZ()+23)>>4;z++)if(!w.hasChunk(x,z))return false;
        BlockPos at=s.o.above(FLOOR+1);
        if(!w.getBlockState(at.below()).isSolidRender(w,at.below()))return false;
        for(int y=0;y<7;y++)if(!w.isEmptyBlock(at.above(y)))return false;
        var q=StormIdolContent.BOSS.get().create(w);
        if(q==null)return false;
        q.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);
        bind(q,s.o);q.setPersistenceRequired();
        if(!w.noCollision(q)||!w.addFreshEntity(q))return false;
        s.boss=q.getUUID();setDirty();return true;
    }
    @SubscribeEvent public static void visit(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.isSpectator()||p.tickCount%20!=0)return;
        ServerLevel w=p.serverLevel();
        if(w.getDifficulty()==Difficulty.PEACEFUL)return;
        var data=get(w);
        for(Site s:data.sites.values())if(!s.dead&&s.boss==null&&inside(s.o,p))data.spawn(w,s);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent event){
        if(event.getEntity() instanceof StormIdolEntity q&&q.level() instanceof ServerLevel w&&bound(q)){
            var data=get(w);Site s=data.sites.get(origin(q));
            if(s!=null&&q.getUUID().equals(s.boss)){s.dead=true;data.setDirty();}
        }
    }
    private static FourWindsRecords load(CompoundTag n){
        var data=new FourWindsRecords();
        for(Tag tag:n.getList("Sites",10)){
            CompoundTag t=(CompoundTag)tag;
            Site s=new Site(BlockPos.of(t.getLong("Origin")));
            s.dead=t.getBoolean("Dead");if(t.hasUUID("Boss"))s.boss=t.getUUID("Boss");
            data.sites.put(s.o,s);
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider registries){
        ListTag list=new ListTag();
        for(Site s:sites.values()){
            CompoundTag t=new CompoundTag();t.putLong("Origin",s.o.asLong());t.putBoolean("Dead",s.dead);
            if(s.boss!=null)t.putUUID("Boss",s.boss);list.add(t);
        }
        n.put("Sites",list);return n;
    }
}
