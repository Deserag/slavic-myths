package org.slavicmyths.savanna;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.registry.ModEntities;

/** Persistent encounter membership and health; runtime viewers and attacker history are not saved. */
public final class HyenaPacks extends SavedData {
    public static final int SIZE=11, LEADER_HP=400, GUARD_HP=150, RETINUE_HP=29;
    static int hp(int slot){return slot==0?LEADER_HP:slot<=2?GUARD_HP:RETINUE_HP;}
    private static final int ALL=(1<<SIZE)-1;
    private final Map<UUID,Pack> packs=new HashMap<>();
    private static final class Pack {
        final UUID[] ids=new UUID[SIZE];
        final float[] hp=new float[SIZE];
        int dead;
        final Map<UUID,Long> threats=new HashMap<>();
        long updated=Long.MIN_VALUE, roamUntil, roarUntil, nextRoar, protectUntil;
        UUID leaderAttacker;
        Vec3 roam;
        final ServerBossEvent bar=new ServerBossEvent(Component.translatableWithFallback("boss.slavicmyths.black_hyena","Чёрная гиена"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
        Pack(){for(int i=0;i<SIZE;i++)hp[i]=HyenaPacks.hp(i);}
    }
    public static HyenaPacks get(ServerLevel w){
        return w.getDataStorage().computeIfAbsent(new SavedData.Factory<>(HyenaPacks::new,HyenaPacks::load),"slavicmyths_hyena_packs");
    }
    private static HyenaPacks load(CompoundTag n,HolderLookup.Provider lookup){
        HyenaPacks data=new HyenaPacks();
        CompoundTag rows=n.getCompound("Packs");
        for(String key:rows.getAllKeys()){
            try{
                UUID id=UUID.fromString(key);CompoundTag row=rows.getCompound(key);Pack p=new Pack();
                p.dead=row.getInt("Dead")&ALL;p.roarUntil=row.getLong("RoarUntil");p.nextRoar=row.getLong("NextRoar");
                boolean valid=true;
                for(int i=0;i<SIZE;i++){
                    if(!row.hasUUID("Id"+i)){valid=false;break;}
                    p.ids[i]=row.getUUID("Id"+i);
                    p.hp[i]=(p.dead&(1<<i))!=0?0:Math.max(0,Math.min(hp(i),row.getFloat("Hp"+i)));
                }
                if(valid)data.packs.put(id,p);
            }catch(IllegalArgumentException ignored){}
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider lookup){
        CompoundTag rows=new CompoundTag();
        packs.forEach((id,p)->{
            CompoundTag row=new CompoundTag();row.putInt("Dead",p.dead);row.putLong("RoarUntil",p.roarUntil);row.putLong("NextRoar",p.nextRoar);
            for(int i=0;i<SIZE;i++){row.putUUID("Id"+i,p.ids[i]);row.putFloat("Hp"+i,p.hp[i]);}
            rows.put(id.toString(),row);
        });
        n.put("Packs",rows);return n;
    }
    boolean spawn(SavannaAnimal seed,ServerLevel w){return spawn(seed,w,null);}
    boolean spawn(SavannaAnimal seed,ServerLevel w,ServerPlayer summoner){
        if(summoner==null&&!w.getEntitiesOfClass(SavannaAnimal.class,seed.getBoundingBox().inflate(128),a->a!=seed&&a.hyenaRole()==SavannaAnimal.HyenaRole.LEADER&&a.packId!=null&&a.isAlive()).isEmpty())return false;
        if(seed.hyenaRole()!=SavannaAnimal.HyenaRole.LEADER||!w.canSeeSky(seed.blockPosition())||!w.noCollision(seed))return false;
        List<SavannaAnimal> members=new ArrayList<>();members.add(seed);
        for(int i=1;i<SIZE;i++){
            SavannaAnimal member=(i<=2?ModEntities.HYENA_BODYGUARD:ModEntities.HYENA_RETINUE).get().create(w);
            if(member==null)return false;
            boolean found=false;
            for(int attempt=0;attempt<5&&!found;attempt++){
                double angle=(i-1)*2*Math.PI/(SIZE-1)+attempt*.17,radius=(i<=2?4:6)+attempt*2;
                BlockPos column=BlockPos.containing(seed.getX()+Math.cos(angle)*radius,seed.getY(),seed.getZ()+Math.sin(angle)*radius);
                if(!w.hasChunkAt(column))continue;
                BlockPos p=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column);
                if(summoner!=null&&!w.mayInteract(summoner,p))continue;
                if(Math.abs(p.getY()-seed.getY())>4||!w.canSeeSky(p)||!w.getFluidState(p).isEmpty()
                    ||!w.getBlockState(p.below()).isFaceSturdy(w,p.below(),net.minecraft.core.Direction.UP))continue;
                member.moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,seed.getYRot(),0);
                if(!w.noCollision(member)||members.stream().anyMatch(a->a.getBoundingBox().intersects(member.getBoundingBox())))continue;
                found=true;
            }
            if(!found)return false;
            members.add(member);
        }
        UUID id=UUID.randomUUID();Pack p=new Pack();
        for(int i=0;i<SIZE;i++){SavannaAnimal a=members.get(i);a.packId=id;a.packSlot=i;p.ids[i]=a.getUUID();a.setHealth(hp(i));}
        packs.put(id,p);setDirty();
        List<SavannaAnimal> added=new ArrayList<>();
        for(int i=1;i<SIZE;i++){
            SavannaAnimal a=members.get(i);
            if(!w.addFreshEntity(a)){
                packs.remove(id);setDirty();added.forEach(Entity::discard);seed.packId=null;seed.packSlot=-1;return false;
            }
            added.add(a);
        }
        return true;
    }
    boolean validMember(SavannaAnimal a){
        Pack p=packs.get(a.packId);int i=a.packSlot;
        return p!=null&&i>=0&&i<SIZE&&a.getUUID().equals(p.ids[i])&&a.hyenaRole()==(i==0?SavannaAnimal.HyenaRole.LEADER:i<=2?SavannaAnimal.HyenaRole.GUARD:SavannaAnimal.HyenaRole.RETINUE)&&(p.dead&(1<<i))==0;
    }
    boolean enraged(UUID id){Pack p=packs.get(id);return p!=null&&(p.dead&6)==6&&(p.dead&1)==0;}
    boolean leaderDead(UUID id){Pack p=packs.get(id);return p==null||(p.dead&1)!=0;}
    boolean buffed(UUID id,long now){Pack p=packs.get(id);return p!=null&&(p.dead&1)==0&&now<p.roarUntil;}
    void attacked(SavannaAnimal victim,LivingEntity attacker){
        Pack p=packs.get(victim.packId);if(p==null||(p.dead&1)!=0)return;
        long now=attacker.level().getGameTime();
        p.threats.put(attacker.getUUID(),now+600);
        if(victim.packSlot==0){p.leaderAttacker=attacker.getUUID();p.protectUntil=now+100;}
    }
    boolean tryRoar(SavannaAnimal leader,ServerLevel w){
        Pack p=packs.get(leader.packId);
        if(p==null||leader.packSlot!=0||leaderDead(leader.packId)||leader.getTarget()==null||w.getGameTime()<p.nextRoar)return false;
        int guards=Integer.bitCount(p.dead&6);long now=w.getGameTime();
        p.nextRoar=now+(guards==0?240:guards==1?160:120);
        p.roarUntil=now+80;setDirty();return true;
    }
    private List<SavannaAnimal> loaded(Pack p,ServerLevel w){
        List<SavannaAnimal> list=new ArrayList<>();
        for(int i=0;i<SIZE;i++)if((p.dead&(1<<i))==0&&w.getEntity(p.ids[i]) instanceof SavannaAnimal a&&a.isAlive())list.add(a);
        return list;
    }
    LivingEntity target(SavannaAnimal a,ServerLevel w){
        Pack p=packs.get(a.packId);if(p==null)return null;
        if(leaderDead(a.packId)){
            LivingEntity old=a.getTarget();
            if(a.packTargetAllowed(old)&&a.distanceToSqr(old)<32*32)return old;
            return w.getEntitiesOfClass(LivingEntity.class,a.getBoundingBox().inflate(18),x->a.packPrey(x)&&a.hasLineOfSight(x))
                .stream().min(Comparator.comparingDouble(a::distanceToSqr)).orElse(null);
        }
        if(a.packSlot<=2&&a.packSlot>0&&p.leaderAttacker!=null&&w.getGameTime()<p.protectUntil
            &&w.getEntity(p.leaderAttacker) instanceof LivingEntity attacker&&a.packTargetAllowed(attacker)&&a.distanceToSqr(attacker)<36*36)return attacker;
        p.threats.values().removeIf(until->until<w.getGameTime());
        List<LivingEntity> candidates=new ArrayList<>();
        for(UUID id:p.threats.keySet())if(w.getEntity(id) instanceof LivingEntity x&&a.packTargetAllowed(x)
            &&a.distanceToSqr(x)<=36*36)candidates.add(x);
        candidates.sort(Comparator.comparing(Entity::getUUID));
        if(!candidates.isEmpty())return candidates.get(Math.floorMod(a.packSlot,candidates.size()));
        if(a.packSlot!=0&&w.getEntity(p.ids[0]) instanceof SavannaAnimal leader&&leader.isAlive()){
            LivingEntity prey=leader.getTarget();
            if(a.packPrey(prey)&&a.distanceToSqr(prey)<32*32)return prey;
        }
        LivingEntity previous=a.getTarget();
        if(previous!=null&&a.packPrey(previous)&&a.distanceToSqr(previous)<=32*32)return previous;
        return w.getEntitiesOfClass(LivingEntity.class,a.getBoundingBox().inflate(24),x->a.packPrey(x)&&a.hasLineOfSight(x))
            .stream().min(Comparator.comparingDouble(a::distanceToSqr)).orElse(null);
    }
    Vec3 roamPoint(SavannaAnimal a,ServerLevel w){
        Pack p=packs.get(a.packId);if(p==null||leaderDead(a.packId))return null;
        List<SavannaAnimal> members=loaded(p,w);if(members.isEmpty())return null;
        SavannaAnimal anchor=members.get(0);
        if(a.distanceToSqr(anchor)>16*16)return anchor.position();
        if(p.roam==null||w.getGameTime()>=p.roamUntil||anchor.position().distanceToSqr(p.roam)<9){
            p.roamUntil=w.getGameTime()+200;p.roam=null;
            for(int i=0;i<8;i++){
                BlockPos column=anchor.blockPosition().offset(w.random.nextInt(25)-12,0,w.random.nextInt(25)-12);
                if(!w.hasChunkAt(column))continue;
                BlockPos dest=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column);
                if(Math.abs(dest.getY()-anchor.getY())>5||!w.canSeeSky(dest)||!w.getFluidState(dest).isEmpty())continue;
                p.roam=Vec3.atBottomCenterOf(dest);break;
            }
        }
        if(p.roam==null)return null;
        double angle=a.packSlot*Math.PI*2/SIZE;
        double radius=a.packSlot==0?0:a.packSlot<=2?3.5:6;
        Vec3 offset=p.roam.add(Math.cos(angle)*radius,0,Math.sin(angle)*radius);
        BlockPos column=BlockPos.containing(offset);
        if(!w.hasChunkAt(column))return null;
        return Vec3.atBottomCenterOf(w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,column));
    }
    void update(SavannaAnimal a,ServerLevel w){
        Pack p=packs.get(a.packId);if(p==null)return;
        float hp=a.getHealth();if(p.hp[a.packSlot]!=hp){p.hp[a.packSlot]=hp;setDirty();}
        if(p.updated==w.getGameTime())return;p.updated=w.getGameTime();
        refresh(p,w,null);
    }
    private void refresh(Pack p,ServerLevel w,SavannaAnimal excluded){
        List<SavannaAnimal> members=loaded(p,w);members.remove(excluded);
        p.bar.setProgress(Math.max(0,Math.min(1,p.hp[0]/LEADER_HP)));
        if((p.dead&1)!=0||members.stream().noneMatch(a->a.packSlot==0)){p.bar.removeAllPlayers();return;}
        Set<ServerPlayer> viewers=new HashSet<>();
        for(ServerPlayer player:w.players())if(!player.isSpectator()&&members.stream().anyMatch(a->a.packSlot==0&&a.distanceToSqr(player)<=48*48))viewers.add(player);
        for(ServerPlayer player:List.copyOf(p.bar.getPlayers()))if(!viewers.contains(player))p.bar.removePlayer(player);
        for(ServerPlayer player:viewers)if(!p.bar.getPlayers().contains(player))p.bar.addPlayer(player);
    }
    void died(SavannaAnimal a,ServerLevel w){
        if(!validMember(a))return;
        Pack p=packs.get(a.packId);p.dead|=1<<a.packSlot;p.hp[a.packSlot]=0;setDirty();
        // Role-specific rewards are deferred. Keep the roster after leader death for future individual claims.
        if(a.packSlot==0){p.roarUntil=0;p.threats.clear();p.leaderAttacker=null;}
        else if(a.packSlot<=2)p.nextRoar=Math.min(p.nextRoar,w.getGameTime()+(((p.dead&6)==6)?120:160));
        refresh(p,w,a);
    }
    void removed(SavannaAnimal a,ServerLevel w){
        Pack p=packs.get(a.packId);if(p!=null)refresh(p,w,a);
    }
}
