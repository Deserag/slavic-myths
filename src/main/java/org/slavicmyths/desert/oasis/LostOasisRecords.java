package org.slavicmyths.desert.oasis;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.slavicmyths.desert.LamyaContent;
import org.slavicmyths.desert.LamyaEntity;
import static org.slavicmyths.desert.oasis.LostOasisPlan.*;
@EventBusSubscriber(modid="slavicmyths")
public final class LostOasisRecords extends SavedData {
    private static final String ORIGIN="SlavicLostOasisOrigin";
    private static final class Site{
        final BlockPos o;boolean ready,dead,manual;UUID boss;
        Site(BlockPos o){this.o=o.immutable();}
    }
    private final Map<BlockPos,Site> sites=new HashMap<>();
    public static LostOasisRecords get(ServerLevel w){return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(LostOasisRecords::new,(n,r)->load(n)),"slavicmyths_lost_oases");}
    private boolean overlaps(BlockPos o){return overlaps(o,W,D);}
    public boolean overlaps(BlockPos o,int width,int depth){for(Site s:sites.values())if(o.getX()<s.o.getX()+W&&s.o.getX()<o.getX()+width&&o.getZ()<s.o.getZ()+D&&s.o.getZ()<o.getZ()+depth)return true;return false;}
    boolean reserveManual(BlockPos o){
        Site old=sites.get(o);
        if(old!=null)return old.manual&&!old.ready;
        if(overlaps(o))return false;
        Site s=new Site(o);s.manual=true;sites.put(s.o,s);setDirty();return true;
    }
    void complete(BlockPos o){Site s=sites.get(o);if(s!=null){s.ready=true;setDirty();}}
    void natural(BlockPos o){if(!overlaps(o)){Site s=new Site(o);s.ready=true;sites.put(s.o,s);setDirty();}}
    public static boolean bound(Entity e){return e.getPersistentData().contains(ORIGIN);}
    public static BlockPos origin(Entity e){return BlockPos.of(e.getPersistentData().getLong(ORIGIN));}
    private static boolean inside(BlockPos o,BlockPos p){return arena(p.getX()-o.getX(),p.getY()-o.getY(),p.getZ()-o.getZ());}
    public static boolean allows(Entity boss,Entity target){
        if(!bound(boss))return true;BlockPos o=origin(boss),p=target.blockPosition();
        return encounter(p.getX()-o.getX(),p.getY()-o.getY(),p.getZ()-o.getZ());
    }
    public static Vec3 clampMovement(Entity boss,Vec3 dest){
        if(!bound(boss))return dest;
        BlockPos o=origin(boss);double cx=o.getX()+CX+.5,cz=o.getZ()+CZ+.5,dx=dest.x-cx,dz=dest.z-cz,r=Math.hypot(dx,dz/1.08);
        double scale=r>15?15/r:1;
        return new Vec3(cx+dx*scale,o.getY()+FLOOR+1,cz+dz*scale);
    }
    public static boolean barVisible(LamyaEntity boss,ServerPlayer p){return boss.isAlive()&&!boss.isNoAi()&&!p.isSpectator()&&boss.level()==p.level()&&boss.distanceToSqr(p)<=900&&allows(boss,p);}
    public static BlockPos center(BlockPos o){return o.offset(CX,FLOOR+1,CZ);}
    public static BlockPos balcony(BlockPos o){return o.offset(CX,LOJA_FLOOR+1,CZ+29);}
    private static boolean clear(ServerLevel w,BlockPos at){
        if(!w.hasChunkAt(at)||!w.getBlockState(at.below()).isSolidRender(w,at.below()))return false;
        for(int y=0;y<8;y++)if(!w.isEmptyBlock(at.above(y)))return false;
        return true;
    }
    boolean spawn(ServerLevel w,BlockPos o){
        Site s=sites.get(o);if(s==null||!s.ready||s.dead||s.boss!=null||w.getDifficulty()==Difficulty.PEACEFUL)return false;
        BlockPos at=balcony(o);
        if(!clear(w,at)||!clear(w,center(o)))return false;
        LamyaEntity q=LamyaContent.BOSS.get().create(w);if(q==null)return false;
        q.moveTo(at.getX()+.5,at.getY(),at.getZ()+.5,180,0);q.setNoAi(true);q.setInvulnerable(true);
        q.getPersistentData().putLong(ORIGIN,o.asLong());q.setPersistenceRequired();
        if(!w.noCollision(q)||!w.addFreshEntity(q))return false;
        s.boss=q.getUUID();setDirty();return true;
    }
    @SubscribeEvent public static void visit(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||p.isSpectator()||p.tickCount%20!=0)return;
        ServerLevel w=p.serverLevel();if(w.getDifficulty()==Difficulty.PEACEFUL)return;
        var data=get(w);
        for(Site s:data.sites.values()){
            if(!s.ready||s.dead)continue;
            BlockPos rel=p.blockPosition().subtract(s.o);
            if(!visitor(rel.getX(),rel.getY(),rel.getZ()))continue;
            if(s.boss==null)data.spawn(w,s.o);
            if(s.boss==null||!inside(s.o,p.blockPosition()))continue;
            if(w.getEntity(s.boss) instanceof LamyaEntity q&&q.isNoAi()){
                BlockPos at=center(s.o);if(clear(w,at)){
                    q.releaseFromOasis(at);
                }
            }
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent e){
        if(e.getEntity() instanceof LamyaEntity q&&q.level() instanceof ServerLevel w&&bound(q)){
            var data=get(w);Site s=data.sites.get(origin(q));
            if(s!=null&&q.getUUID().equals(s.boss)){s.dead=true;data.setDirty();}
        }
    }
    private static LostOasisRecords load(CompoundTag n){
        var d=new LostOasisRecords();
        for(Tag tag:n.getList("Sites",10)){CompoundTag t=(CompoundTag)tag;Site s=new Site(BlockPos.of(t.getLong("Origin")));s.ready=t.getBoolean("Ready");s.dead=t.getBoolean("Dead");s.manual=t.getBoolean("Manual");if(t.hasUUID("Boss"))s.boss=t.getUUID("Boss");d.sites.put(s.o,s);}
        return d;
    }
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider r){
        ListTag list=new ListTag();
        for(Site s:sites.values()){CompoundTag t=new CompoundTag();t.putLong("Origin",s.o.asLong());t.putBoolean("Ready",s.ready);t.putBoolean("Dead",s.dead);t.putBoolean("Manual",s.manual);if(s.boss!=null)t.putUUID("Boss",s.boss);list.add(t);}
        n.put("Sites",list);return n;
    }
}
