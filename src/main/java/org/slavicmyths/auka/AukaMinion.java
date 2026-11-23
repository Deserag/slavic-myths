package org.slavicmyths.auka;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.*;

/** Encounter-owned bear or white wolf. No autonomous player aggro or world spawning. */
public final class AukaMinion extends PathfinderMob {
    public enum Kind { BEAR, WOLF }
    public enum Motion { IDLE, MOVE, THREATEN, BITE, RETREAT }
    private static final EntityDataAccessor<Integer> MOTION = SynchedEntityData.defineId(AukaMinion.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START = SynchedEntityData.defineId(AukaMinion.class,EntityDataSerializers.LONG);
    private final Kind kind;
    private UUID ownerId;
    private int role, orphans;
    private long lastHit, biteAt, biteUntil;
    private float biteDamage;
    private boolean biteDelivered;

    public AukaMinion(EntityType<? extends AukaMinion> type,Level level,Kind kind){
        super(type,level);this.kind=kind;xpReward=0;setPersistenceRequired();
    }
    public static AttributeSupplier.Builder attributes(Kind kind){
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,kind==Kind.BEAR?70:45)
            .add(Attributes.ATTACK_DAMAGE,kind==Kind.BEAR?7:5)
            .add(Attributes.ARMOR,kind==Kind.BEAR?3:2)
            .add(Attributes.MOVEMENT_SPEED,kind==Kind.BEAR?.25:.34)
            .add(Attributes.FOLLOW_RANGE,32);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){
        super.defineSynchedData(builder);
        builder.define(MOTION,Motion.IDLE.ordinal());builder.define(START,0L);
    }
    public Kind kind(){return kind;}
    public UUID ownerId(){return ownerId;}
    public Motion motion(){return Motion.values()[entityData.get(MOTION)];}
    public float actionTicks(float partial){return Math.max(0,level().getGameTime()-entityData.get(START)+partial);}
    public void bind(UUID boss,int assignedRole){ownerId=boss;role=assignedRole;setHealth(getMaxHealth());}
    private void state(Motion value){
        if(motion()!=value){entityData.set(MOTION,value.ordinal());entityData.set(START,level().getGameTime());}
    }
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
    private AukaBoss master(){
        if(ownerId==null||!(level() instanceof ServerLevel w))return null;
        var entity=w.getEntity(ownerId);
        return entity instanceof AukaBoss boss?boss:null;
    }
    private void navigate(Vec3 point,double speed){
        getNavigation().moveTo(point.x,point.y,point.z,speed);
        state(Motion.MOVE);
    }
    private void hit(ServerPlayer target,float damage){
        long now=level().getGameTime();
        if(now-lastHit<44||distanceToSqr(target)>(kind==Kind.BEAR?9:5.5)||!hasLineOfSight(target))return;
        lastHit=now;biteAt=now+10;biteUntil=now+19;biteDamage=damage;biteDelivered=false;
        getNavigation().stop();state(Motion.BITE);
        playSound(kind==Kind.BEAR?SoundEvents.POLAR_BEAR_WARNING:SoundEvents.WOLF_GROWL,.75F,1);
    }
    @Override public void tick(){
        super.tick();
        if(!(level() instanceof ServerLevel world))return;
        AukaBoss boss=master();
        if(boss==null){
            if(++orphans>180)discard();
            return;
        }
        orphans=0;
        if(!boss.active()||!boss.isAlive()||(kind==Kind.BEAR&&boss.form()!=AukaBoss.Form.HUT_SPIRIT)
            ||(kind==Kind.WOLF&&!boss.wolf())){discard();return;}
        ServerPlayer target=boss.victim();
        if(target==null||!target.isAlive()||target.level()!=level()||distanceToSqr(target)>48*48){
            getNavigation().stop();state(Motion.IDLE);return;
        }
        if(motion()==Motion.BITE){
            long now=world.getGameTime();
            if(!biteDelivered&&now>=biteAt){
                biteDelivered=true;
                if(distanceToSqr(target)<(kind==Kind.BEAR?9:5.5)&&hasLineOfSight(target)){
                    target.hurt(damageSources().mobAttack(this),biteDamage);
                }
            }
            if(now<biteUntil){getNavigation().stop();return;}
            state(Motion.IDLE);
        }
        if(kind==Kind.BEAR){
            if(tickCount%10==0)navigate(target.position(),1.0);
            if(distanceToSqr(target)<8)hit(target,7);
            return;
        }
        // Role 0: left-hand driver; role 1: right-hand interceptor. One attack slot at a time.
        int time=Math.floorMod(boss.packElapsed(),180);
        Vec3 toward=boss.position().subtract(target.position()).multiply(1,0,1).normalize();
        if(toward.lengthSqr()<.001)toward=new Vec3(0,0,1);
        Vec3 side=new Vec3(-toward.z,0,toward.x).scale(role==0?1:-1);
        double radius=time<46?5.7:time<78?3.8:time<126?5.1:6.0;
        Vec3 flank=target.position().add(side.scale(radius)).add(toward.scale(1.5));
        if(time<47){
            if(tickCount%8==0)navigate(flank,1.15);
        } else if(time<76){
            if(role==0){
                if(tickCount%8==0)navigate(target.position().add(side.scale(2.2)),1.18);
                if(time>=59&&time<=73&&distanceToSqr(target)<5.5)hit(target,5);
            } else if(tickCount%8==0)navigate(flank,1.0);
        } else if(time<126){
            if(tickCount%8==0)navigate(flank,1.06);
            state(Motion.THREATEN);
        } else if(time<159){
            if(tickCount%8==0)navigate(flank.add(toward.scale(4)),1.2);
            state(Motion.RETREAT);
        } else {
            if(role==1){
                if(tickCount%8==0)navigate(target.position().add(side.scale(2.4)),1.18);
                if(time>=164&&time<=178&&distanceToSqr(target)<5.5)hit(target,5);
            } else if(tickCount%8==0)navigate(flank,1.0);
        }
    }
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override protected int getBaseExperienceReward(){return 0;}
    @Override public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        if(ownerId!=null)tag.putUUID("AukaOwner",ownerId);
        tag.putInt("AukaRole",role);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        ownerId=tag.hasUUID("AukaOwner")?tag.getUUID("AukaOwner"):null;
        role=Math.clamp(tag.getInt("AukaRole"),0,1);
        state(Motion.IDLE);
        biteAt=biteUntil=0;biteDelivered=true;orphans=0;
    }
}
