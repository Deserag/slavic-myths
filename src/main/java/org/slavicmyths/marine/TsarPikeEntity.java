package org.slavicmyths.marine;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative solitary aquatic predator, hostile to every attackable living creature. */
public final class TsarPikeEntity extends Monster {
    // Aquatic encounters may occupy water; retain block and entity obstruction checks.
    @Override public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader world) {
        return world.isUnobstructed(this) && world.noCollision(this);
    }

    public static final int IDLE=0, SWIM=1, FAST=2, BITE=3, GRAB=4, HOLD=5,
        SHAKE=6, RELEASE=7, TAIL_SWEEP=8, LEAP=9;
    public static final float BASE_DAMAGE=18F, BITE_DAMAGE=BASE_DAMAGE*.65F,
        TAIL_DAMAGE=BASE_DAMAGE*.70F;
    private static final EntityDataAccessor<Integer> ACTION =
        SynchedEntityData.defineId(TsarPikeEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION_START =
        SynchedEntityData.defineId(TsarPikeEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> OPEN =
        SynchedEntityData.defineId(TsarPikeEntity.class,EntityDataSerializers.FLOAT);

    private final ServerBossEvent bar=new org.slavicmyths.combat.CreatureBossBar(this,
        Component.translatable("entity.slavicmyths.tsar_pike"),org.slavicmyths.combat.BossBarPalette.colorFor(getType(),BossEvent.BossBarColor.BLUE),
        BossEvent.BossBarOverlay.PROGRESS);

    private int biteCooldown=20, grabCooldown=140, tailCooldown=60, leapCooldown=80;
    private int phaseTicks, wanderTicks, dryTicks, holdTicks, hitCount;
    private float heldDamage, holdTotalDamage;
    private UUID victimId;
    private boolean actionHit;
    private final Set<UUID> swept = new HashSet<>();
    private final Map<UUID,Long> releasedAt = new HashMap<>();

    public TsarPikeEntity(EntityType<? extends TsarPikeEntity> type,Level level) {
        super(type,level);
        moveControl=new SmoothSwimmingMoveControl(this,85,10,.06F,.23F,true);
        setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,0);
        xpReward=55;
    }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH,600)
            .add(Attributes.MOVEMENT_SPEED,.46)
            .add(Attributes.ATTACK_DAMAGE,BASE_DAMAGE)
            .add(Attributes.ARMOR,6)
            .add(Attributes.FOLLOW_RANGE,32)
            .add(Attributes.KNOCKBACK_RESISTANCE,.75);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION,IDLE);
        builder.define(ACTION_START,0);
        builder.define(OPEN,0F);
    }
    public int action(){return entityData.get(ACTION);}
    public int actionTicks(){return Math.max(0,(int)level().getGameTime()-entityData.get(ACTION_START));}
    public float jawOpen(){return entityData.get(OPEN);}
    private void action(int id,int ticks){
        entityData.set(ACTION,id);
        entityData.set(ACTION_START,(int)level().getGameTime());
        phaseTicks=ticks;
        actionHit=false;
    }
    @Override protected PathNavigation createNavigation(Level level){
        return new WaterBoundPathNavigation(this,level);
    }
    @Override protected void registerGoals(){
        // All-target selection is done explicitly. Standard team/category goals exclude prey.
    }
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public void travel(Vec3 input){
        if (isEffectiveAi() && isInWater()) {
            moveRelative(.09F,input);
            move(MoverType.SELF,getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(.88));
        } else super.travel(input);
    }

    private boolean prey(LivingEntity e){
        return e!=this && e.isAlive() && !e.isSpectator()
            && !(e instanceof Player p && p.isCreative())
            && !e.isInvulnerable()
            && !e.isRemoved()
            && e.level()==level();
    }
    private void searchPrey() {
        if (org.slavicmyths.combat.BossRetaliation.select(this, 48)) return;
        if (tickCount%12!=0) return;
        LivingEntity current=getTarget();
        if (current!=null && (!prey(current)||distanceToSqr(current)>48*48)) {
            current=null;setTarget(null);
        }
        LivingEntity nearest=null;
        double best=32*32;
        for (LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(32),this::prey)) {
            double distance=distanceToSqr(e);
            if (distance>=best || !hasLineOfSight(e)) continue;
            // A player on the nearby bank is reachable only if it is close to the surface.
            if (!e.isInWaterOrBubble() &&
                (distance>100 || Math.abs(e.getY()-getY())>4.0)) continue;
            nearest=e;best=distance;
        }
        if (nearest==null) {if(current==null)setTarget(null);return;}
        if (current==null || !hasLineOfSight(current)
                || (nearest!=current && best<distanceToSqr(current)*.55))
            setTarget(nearest);
    }
    private boolean inWater(){return isInWaterOrBubble() ||
        level().getFluidState(blockPosition()).is(FluidTags.WATER);}
    private void countdown(){if(biteCooldown>0)biteCooldown--;if(grabCooldown>0)grabCooldown--;
        if(tailCooldown>0)tailCooldown--;if(leapCooldown>0)leapCooldown--;}
    private Vec3 forward(){
        Vec3 dir=getLookAngle();
        Vec3 flat=new Vec3(dir.x,0,dir.z);
        return flat.lengthSqr()<.0001?new Vec3(0,0,-1):flat.normalize();
    }
    private Vec3 mouthCenter(){
        Vec3 front=forward().scale(2.15);
        return position().add(front).add(0,getBbHeight()*.52,0);
    }
    private boolean mouthContact(LivingEntity e){
        return prey(e) && hasLineOfSight(e) &&
            e.getBoundingBox().intersects(new AABB(mouthCenter(),mouthCenter()).inflate(1.35,1.05,1.35))
            && e.position().subtract(position()).dot(forward())>0;
    }
    private void splash(int count){
        if(level() instanceof ServerLevel server)
            server.sendParticles(ParticleTypes.BUBBLE,getX(),getY()+1,getZ(),
                count,1.1,.6,1.1,.11);
    }

    @Override public void aiStep() {
        super.aiStep();
        if(level().isClientSide) return;
        if(inWater())setAirSupply(getMaxAirSupply());
        bar.setProgress(getHealth()/Math.max(1,getMaxHealth()));
        countdown();
        boolean wet=inWater();
        setNoGravity(wet);
        if(!wet){
            if (++dryTicks>105 && tickCount%20==0) hurt(damageSources().dryOut(),4F);
            if(action()!=LEAP && victimId==null) returnToWater();
        } else dryTicks=0;
        if(victimId!=null){holdVictim();return;}
        searchPrey();
        LivingEntity target=getTarget();
        if(phaseTicks>0){
            int action=action();
            phaseTicks--;
            switch(action){
                case BITE->tickBite(target);
                case GRAB->tickGrab(target);
                case TAIL_SWEEP->tickSweep();
                case LEAP->tickLeap(target);
                case RELEASE->{entityData.set(OPEN,Math.max(0,jawOpen()-.11F));}
                default->{}
            }
            if(phaseTicks==0 && victimId==null) action(IDLE,0);
            return;
        }
        entityData.set(OPEN,Math.max(0,jawOpen()-.1F));
        if(target==null){
            idleWander();
            if(action()!=IDLE && action()!=SWIM) action(IDLE,0);
            return;
        }
        double distance=distanceToSqr(target);
        getLookControl().setLookAt(target,30,30);
        if(wet && tickCount%6==0) getNavigation().moveTo(target,1.45);
        if(wet && hasLineOfSight(target) && distance>4){
            Vec3 toward=target.position().add(0,target.getBbHeight()*.5,0)
                .subtract(position().add(0,getBbHeight()*.5,0));
            if(toward.lengthSqr()>.01){
                Vec3 candidate=getDeltaMovement().scale(.70).add(toward.normalize().scale(.08));
                if(candidate.lengthSqr()>.32*.32)candidate=candidate.normalize().scale(.32);
                BlockPos ahead=BlockPos.containing(position().add(candidate.scale(3)));
                if(level().getFluidState(ahead).is(FluidTags.WATER)){
                    setDeltaMovement(org.slavicmyths.combat.LargeMobClearance.steer(this,candidate));hasImpulse=true;
                }
            }
        }
        if(wet && tailCooldown==0 && rearOrSideTarget(target) && distance<27){
            tailCooldown=180;action(TAIL_SWEEP,19);swept.clear();
            playSound(SoundEvents.GENERIC_SPLASH,1.1F,.7F);return;
        }
        if(wet && grabCooldown==0 && distance>6 && distance<144
            && target.getBbWidth()<1.7F && target.getBbHeight()<2.4F
            && !coolingDown(target) && hasLineOfSight(target)){
            grabCooldown=280;
            action(GRAB,26);splash(12);return;
        }
        if(wet && leapCooldown==0 && distance<80 && nearSurface()
            && target.getY()>=getY()-1 && target.getY()<getY()+4
            && random.nextInt(5)==0 && hasLineOfSight(target)
            && level().getBlockState(blockPosition().above(2)).isAir()){
            leapCooldown=160;
            action(LEAP,24);splash(16);return;
        }
        if(wet && biteCooldown==0 && distance<36 && hasLineOfSight(target)){
            biteCooldown=40+random.nextInt(12);
            action(BITE,12);return;
        }
        // Fast pursuit changes the articulated body cadence without introducing extra damage.
        int locomotion=distance>36 && distance<256?FAST:SWIM;
        if(action()!=locomotion) action(locomotion,0);
    }
    private void tickBite(LivingEntity target){
        entityData.set(OPEN,Math.min(1F,jawOpen()+.24F));
        if(phaseTicks>=5 && phaseTicks<=9 && target!=null && prey(target)){
            rush(target,.35);
            if(!actionHit && mouthContact(target)){
                actionHit=true;
                target.hurt(damageSources().mobAttack(this),BITE_DAMAGE);
                splash(12);
                playSound(SoundEvents.COD_HURT,1.3F,.57F);
            }
        }
    }
    private void tickGrab(LivingEntity target){
        entityData.set(OPEN,Math.min(1F,jawOpen()+.18F));
        if(phaseTicks>17) {setDeltaMovement(getDeltaMovement().scale(.75));return;}
        if(target==null || !prey(target))return;
        rush(target,.46);
        if(!actionHit && mouthContact(target)){
            actionHit=true;
            victimId=target.getUUID();holdTicks=90;hitCount=0;heldDamage=0;holdTotalDamage=0;
            getNavigation().stop();
            action(HOLD,0);
            splash(18);
        }
    }
    private void tickSweep(){
        if(phaseTicks>11)return;
        Vec3 behind=position().subtract(forward().scale(2.6));
        AABB area=new AABB(behind,behind).inflate(3.3,1.6,3.3);
        for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,area,this::prey)){
            if(swept.contains(e.getUUID()) || !hasLineOfSight(e))continue;
            if(e.position().subtract(position()).dot(forward())>1.0)continue;
            swept.add(e.getUUID());
            e.hurt(damageSources().mobAttack(this),TAIL_DAMAGE);
            Vec3 away=e.position().subtract(position()).multiply(1,0,1);
            if(away.lengthSqr()>.01){
                Vec3 push=away.normalize().scale(1.25);
                e.push(push.x,.25,push.z);e.hurtMarked=true;
            }
        }
        if(phaseTicks==11)splash(24);
    }
    private boolean rearOrSideTarget(LivingEntity e){
        Vec3 to=e.position().subtract(position()).multiply(1,0,1);
        return to.lengthSqr()>.1 && forward().dot(to.normalize())<.4;
    }
    private void rush(LivingEntity target,double strength){
        Vec3 dir=target.position().add(0,target.getBbHeight()*.5,0)
            .subtract(position().add(0,getBbHeight()*.52,0));
        if(dir.lengthSqr()>.1){
            Vec3 velocity=dir.normalize().scale(strength);
            BlockPos next=BlockPos.containing(position().add(velocity.scale(2)));
            if(level().getFluidState(next).is(FluidTags.WATER)
                || (action()==LEAP && level().getBlockState(next).isAir())){
                setDeltaMovement(org.slavicmyths.combat.LargeMobClearance.steer(this,getDeltaMovement().scale(.25).add(velocity)));hasImpulse=true;
            }
        }
    }
    private boolean nearSurface(){
        return level().getFluidState(blockPosition()).is(FluidTags.WATER)
            && (!level().getFluidState(blockPosition().above(2)).is(FluidTags.WATER)
                || !level().getFluidState(blockPosition().above(3)).is(FluidTags.WATER));
    }
    private void tickLeap(LivingEntity target){
        if(phaseTicks==23){
            Vec3 dir=target!=null?target.position().subtract(position()).multiply(1,0,1):forward();
            if(dir.lengthSqr()<.01)dir=forward();
            Vec3 leap=dir.normalize().scale(.45).add(0,.7,0);
            setNoGravity(false);
            setDeltaMovement(org.slavicmyths.combat.LargeMobClearance.steer(this,leap));hasImpulse=true;
            playSound(SoundEvents.GENERIC_SPLASH,1.2F,.7F);
        }
        if(phaseTicks<=12 && !inWater()){
            setNoGravity(false);
            setDeltaMovement(getDeltaMovement().add(0,-.075,0));
        }
        if(target!=null && !actionHit && mouthContact(target)){
            actionHit=true;target.hurt(damageSources().mobAttack(this),BITE_DAMAGE);
        }
        if(phaseTicks==1) returnToWater();
    }
    private void returnToWater(){
        for(int d=0;d<=5;d++)for(int x=-d;x<=d;x++)for(int z=-d;z<=d;z++){
            BlockPos p=blockPosition().offset(x,-1,z);
            if(level().getFluidState(p).is(FluidTags.WATER)){
                Vec3 to=Vec3.atCenterOf(p).subtract(position());
                if(to.lengthSqr()>.1){
                    Vec3 push=to.normalize().scale(.16);
                    setDeltaMovement(getDeltaMovement().scale(.7).add(push));hasImpulse=true;
                }
                return;
            }
        }
    }
    private void holdVictim(){
        if(!(level() instanceof ServerLevel server)){releaseVictim();return;}
        Entity found=server.getEntity(victimId);
        if(!(found instanceof LivingEntity victim) || !prey(victim) || !hasLineOfSight(victim)){
            releaseVictim();return;
        }
        if(holdTicks--<=0){releaseVictim();return;}
        int elapsed=90-holdTicks;
        entityData.set(ACTION,elapsed%17<8?HOLD:SHAKE);
        entityData.set(OPEN,.72F+.08F*(float)Math.sin(elapsed*.35));
        // Center the victim *between articulated teeth*, not inside the model's origin.
        Vec3 center=mouthCenter().add(forward().scale(.25));
        double x=center.x,y=center.y-victim.getBbHeight()*.45,z=center.z;
        AABB desired=victim.getBoundingBox().move(x-victim.getX(),y-victim.getY(),z-victim.getZ());
        if(level().noCollision(victim,desired)){
            victim.setPos(x,y,z);
            victim.setDeltaMovement(Vec3.ZERO);
            victim.hurtMarked=true;
            victim.fallDistance=0;
        }else{releaseVictim();return;}
        if(elapsed%20==0 && elapsed>0 && holdTotalDamage<18F){
            float amount=Math.min(3F,18F-holdTotalDamage);
            // A held target can still retaliate, so damage is gradual and capped.
            if(victim.hurt(damageSources().mobAttack(this),amount))holdTotalDamage+=amount;
        }
        if(elapsed%18==0)splash(5);
    }
    private boolean coolingDown(LivingEntity e){
        return releasedAt.getOrDefault(e.getUUID(),0L)>level().getGameTime();
    }
    private void releaseVictim(){
        if(victimId!=null && level() instanceof ServerLevel server
            && server.getEntity(victimId) instanceof LivingEntity v && v.isAlive()){
            releasedAt.put(victimId,level().getGameTime()+240);
            Vec3 away=forward().scale(1.2);
            v.push(away.x,.25,away.z);v.hurtMarked=true;
        }
        victimId=null;holdTicks=0;grabCooldown=Math.max(grabCooldown,140);
        action(RELEASE,12);entityData.set(OPEN,.8F);
    }
    @Override public boolean hurt(DamageSource source,float damage){
        float hp=getHealth();
        boolean accepted=super.hurt(source,damage);
        if(accepted && victimId!=null && level() instanceof ServerLevel server){
            Entity attacker=source.getEntity();
            if(attacker instanceof LivingEntity && attacker!=this){
                float applied=Math.max(0,hp-getHealth());
                if(applied>0){
                    heldDamage+=applied;hitCount++;
                    if(heldDamage>=14F || hitCount>=3)releaseVictim();
                }
            }
        }
        return accepted;
    }
    private void idleWander(){
        if(!inWater())return;
        if(--wanderTicks>0)return;
        wanderTicks=40+random.nextInt(60);
        BlockPos p=blockPosition().offset(random.nextInt(17)-8,random.nextInt(7)-3,random.nextInt(17)-8);
        if(level().getFluidState(p).is(FluidTags.WATER)){
            getNavigation().moveTo(p.getX()+.5,p.getY()+.5,p.getZ()+.5,1.0);
            action(SWIM,0);
        }
    }
    @Override public void startSeenByPlayer(ServerPlayer player){super.startSeenByPlayer(player);bar.addPlayer(player);}
    @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);bar.removePlayer(player);}
    @Override public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        tag.putInt("BiteCd",biteCooldown);tag.putInt("GrabCd",grabCooldown);
        tag.putInt("TailCd",tailCooldown);tag.putInt("LeapCd",leapCooldown);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        biteCooldown=Math.max(0,tag.getInt("BiteCd"));
        grabCooldown=Math.max(0,tag.getInt("GrabCd"));
        tailCooldown=Math.max(0,tag.getInt("TailCd"));
        leapCooldown=Math.max(0,tag.getInt("LeapCd"));
        // Hold state is not resurrected across a reload; captives are never stranded.
        victimId=null;entityData.set(OPEN,0F);action(IDLE,0);
    }
}
