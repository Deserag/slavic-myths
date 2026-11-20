package org.slavicmyths.marine;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.EntityDimensions;
import net.neoforged.neoforge.entity.PartEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
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

/** Level VI independent aquatic predator; all damage, capture and targeting is server authoritative. */
public final class DeepSeaSerpentEntity extends Monster {
    public static final int IDLE=0,SWIM=1,FAST=2,BITE=3,GRAB=4,HOLD=5,RELEASE=6,
        VORTEX=7,HURT=8,DIE=9,RETURN=10;
    public static final float BASE_DAMAGE=20F,BITE_DAMAGE=20F,VORTEX_DAMAGE=14F;
    private static final EntityDataAccessor<Integer> ACTION=
        SynchedEntityData.defineId(DeepSeaSerpentEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> START=
        SynchedEntityData.defineId(DeepSeaSerpentEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> JAW=
        SynchedEntityData.defineId(DeepSeaSerpentEntity.class,EntityDataSerializers.FLOAT);
    private static final int MAX_HOLD=80;
    private static final int GRAB_COOLDOWN=300;
    private static final int VORTEX_COOLDOWN=400;

    private final ServerBossEvent bar=new ServerBossEvent(
        Component.translatableWithFallback("entity.slavicmyths.deep_sea_serpent","Морской змей"),
        BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
    private int biteCooldown=20,grabCooldown=100,vortexCooldown=180;
    private int phaseTicks,wanderTicks,dryTicks,holdTicks;
    private float rescueDamage;
    private UUID victimId;
    private boolean actionHit;
    private final Set<UUID> vortexHit=new HashSet<>();
    private final Map<UUID,Long> releasedUntil=new HashMap<>();
    private static final int MODEL_SEGMENTS=16;
    private static final double MODEL_SECTION=20.0/16.0;
    private final Segment[] segments=new Segment[MODEL_SEGMENTS+2];

    /** Pickable hitboxes follow the same moving joints as the visible voxel body. */
    public static final class Segment extends PartEntity<DeepSeaSerpentEntity> {
        private final float width,height;
        private Segment(DeepSeaSerpentEntity parent,float width,float height){
            super(parent);
            this.width=width;
            this.height=height;
            this.noPhysics=true;
            refreshDimensions();
        }
        @Override protected void defineSynchedData(SynchedEntityData.Builder builder){}
        @Override protected void addAdditionalSaveData(CompoundTag tag){}
        @Override protected void readAdditionalSaveData(CompoundTag tag){}
        @Override public EntityDimensions getDimensions(Pose pose){
            return EntityDimensions.scalable(Math.max(.1F,width),Math.max(.1F,height));
        }
        @Override public boolean isPickable(){return true;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean is(Entity entity){return entity==this||entity==getParent();}
        @Override public boolean hurt(DamageSource source,float amount){
            return getParent().hurt(source,amount);
        }
    }
    @Override public boolean isMultipartEntity(){return true;}
    @Override public PartEntity<?>[] getParts(){return segments;}
    @Override public void setId(int id){
        super.setId(id);
        if(segments!=null)
            for(int i=0;i<segments.length;i++)
                if(segments[i]!=null)segments[i].setId(id+i+1);
    }
    private static double sectionRadius(double t){
        return (2.8+(18.5+6.0*Math.sin(Math.PI*t))*
            Math.pow(Math.max(0,1-t),.67))/16.0;
    }
    /** Matches the articulated yaw law in DeepSeaSerpentModel.setupAnim. */
    private void updateSegmentPositions(){
        if(segments[0]==null)return;
        Vec3 direction=forward();
        Vec3 back=direction.scale(-1);
        Vec3 right=new Vec3(back.z,0,-back.x);
        Vec3 cursor=position().add(0,1.5,0);
        placeSegment(segments[0],center().add(direction.scale(3.2)));
        double age=tickCount*.085;
        double yaw=0;
        for(int i=0;i<MODEL_SEGMENTS;i++){
            double wave=age*(action()==FAST?1.8:1)-i*.34;
            double arc=.075*Math.sin(wave)*(.3+i*1.0/(MODEL_SEGMENTS-1));
            if(action()==GRAB||action()==HOLD){
                double progress=action()==HOLD?1:Math.min(1,actionTicks()/26.0);
                if(i>2)arc+=.42*progress;
            }
            if(action()==VORTEX && i>1)
                arc+=.35*Math.min(1,actionTicks()/22.0);
            if(deathTime>0)
                arc=.12*Math.sin(i*.43)*Math.min(1,deathTime/20.0);
            yaw+=arc;
            Vec3 tangent=back.scale(Math.cos(yaw)).add(right.scale(Math.sin(yaw)));
            placeSegment(segments[i+1],cursor.add(tangent.scale(MODEL_SECTION*.5)));
            cursor=cursor.add(tangent.scale(MODEL_SECTION));
        }
        Vec3 tangent=back.scale(Math.cos(yaw)).add(right.scale(Math.sin(yaw)));
        placeSegment(segments[MODEL_SEGMENTS+1],cursor.add(tangent.scale(.8)));
    }
    private static void placeSegment(Segment part,Vec3 center){
        part.setPos(center.x,center.y-part.getBbHeight()*.5,center.z);
    }

    public DeepSeaSerpentEntity(EntityType<? extends DeepSeaSerpentEntity> type,Level level) {
        super(type,level);
        moveControl=new SmoothSwimmingMoveControl(this,90,10,.05F,.23F,true);
        setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,0);
        xpReward=90;
        segments[0]=new Segment(this,2.5F,1.85F);
        for(int i=0;i<MODEL_SEGMENTS;i++){
            double r=sectionRadius((i+.5)/MODEL_SEGMENTS);
            segments[i+1]=new Segment(this,(float)Math.max(.55,2*r),
                (float)Math.max(.5,1.55*r));
        }
        segments[MODEL_SEGMENTS+1]=new Segment(this,1.55F,.7F);
        updateSegmentPositions();
    }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH,1000)
            .add(Attributes.MOVEMENT_SPEED,.46)
            .add(Attributes.ATTACK_DAMAGE,BASE_DAMAGE)
            .add(Attributes.ARMOR,9)
            .add(Attributes.ARMOR_TOUGHNESS,2)
            .add(Attributes.FOLLOW_RANGE,48)
            .add(Attributes.KNOCKBACK_RESISTANCE,.85);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION,IDLE);
        builder.define(START,0);
        builder.define(JAW,0F);
    }
    @Override public Component getName() {
        return hasCustomName()?super.getName():
            Component.translatableWithFallback("entity.slavicmyths.deep_sea_serpent","Морской змей");
    }
    public int action(){return entityData.get(ACTION);}
    public float jawOpen(){return entityData.get(JAW);}
    public int actionTicks(){return Math.max(0,(int)level().getGameTime()-entityData.get(START));}
    private void begin(int a,int duration){
        entityData.set(ACTION,a);
        entityData.set(START,(int)level().getGameTime());
        phaseTicks=duration;
        actionHit=false;
    }
    @Override protected PathNavigation createNavigation(Level l){return new WaterBoundPathNavigation(this,l);}
    @Override protected void registerGoals(){}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public void travel(Vec3 input) {
        if(isEffectiveAi() && isInWater()){
            moveRelative(.09F,input);
            move(MoverType.SELF,getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(.87));
        } else super.travel(input);
    }
    private boolean prey(LivingEntity e) {
        return e!=this && e.isAlive() && !e.isSpectator()
            && !(e instanceof Player p && p.isCreative()) && !e.isInvulnerable()
            && !e.isRemoved() && e.level()==level();
    }
    private boolean wet(){return isInWaterOrBubble()
        || level().getFluidState(blockPosition()).is(FluidTags.WATER);}
    private Vec3 forward(){
        Vec3 v=getLookAngle().multiply(1,0,1);
        return v.lengthSqr()<1E-4 ? Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize():v.normalize();
    }
    private Vec3 center(){return position().add(0,getBbHeight()*.47,0);}
    private void findPrey(){
        if(tickCount%15!=0)return;
        LivingEntity old=getTarget();
        if(old!=null && (!prey(old)||distanceToSqr(old)>64*64)){old=null;setTarget(null);}
        LivingEntity best=null;
        double dist=48*48;
        for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,
            getBoundingBox().inflate(48),this::prey)){
            double d=distanceToSqr(e);
            if(d>=dist || !hasLineOfSight(e))continue;
            if(!e.isInWaterOrBubble() && (d>144 || Math.abs(e.getY()-getY())>4))continue;
            best=e;dist=d;
        }
        if(best!=null && (old==null || best==old || dist<distanceToSqr(old)*.55))
            setTarget(best);
    }
    private void countdown(){
        if(biteCooldown>0)biteCooldown--;
        if(grabCooldown>0)grabCooldown--;
        if(vortexCooldown>0)vortexCooldown--;
        if(tickCount%300==0)releasedUntil.entrySet().removeIf(e->e.getValue()<level().getGameTime());
    }
    private void bubbles(int n){
        if(level() instanceof ServerLevel server)
            server.sendParticles(ParticleTypes.BUBBLE,getX(),getY()+1,getZ(),n,1.7,.9,1.7,.09);
    }
    private boolean freeWater(Vec3 candidate){
        BlockPos base=BlockPos.containing(candidate);
        for(int y=-1;y<=1;y++)
            for(int x=-1;x<=1;x++)
                for(int z=-1;z<=1;z++){
                    BlockPos pos=base.offset(x,y,z);
                    if(!level().getFluidState(pos).is(FluidTags.WATER)
                        || !level().getBlockState(pos).getCollisionShape(level(),pos).isEmpty())
                        return false;
                }
        AABB target=getBoundingBox().move(candidate.subtract(position()));
        return level().noCollision(this,target);
    }
    private void steer(Vec3 toward,double speed) {
        if(!wet()||toward.lengthSqr()<.0001)return;
        Vec3 wish=toward.normalize().scale(speed);
        Vec3 desired=getDeltaMovement().scale(.72).add(wish.scale(.28));
        if(desired.lengthSqr()>speed*speed)desired=desired.normalize().scale(speed);
        if(!freeWater(position().add(desired.scale(3)))){
            // Broaden the turn away from the shore/ships/bottom before moving.
            Vec3 side=new Vec3(-toward.z,.12,toward.x);
            if(freeWater(position().add(side.normalize().scale(2))))
                desired=side.normalize().scale(speed*.65);
            else desired=Vec3.ZERO;
        }
        setDeltaMovement(desired);hasImpulse=true;
    }
    @Override public void aiStep(){
        super.aiStep();
        updateSegmentPositions();
        if(level().isClientSide)return;
        bar.setProgress(getHealth()/Math.max(1,getMaxHealth()));
        countdown();
        boolean water=wet();
        if(water)setAirSupply(getMaxAirSupply());
        setNoGravity(water);
        if(!water){
            if(++dryTicks>90 && tickCount%20==0)hurt(damageSources().dryOut(),5);
            releaseVictim();
            seekWater();
            return;
        }
        dryTicks=0;
        if(victimId!=null){holdVictim();return;}
        findPrey();
        LivingEntity target=getTarget();
        if(phaseTicks>0){
            phaseTicks--;
            switch(action()){
                case BITE->tickBite(target);
                case GRAB->tickGrab(target);
                case VORTEX->tickVortex();
                case RELEASE->entityData.set(JAW,Math.max(0,jawOpen()-.11F));
                default->{}
            }
            if(phaseTicks==0)begin(IDLE,0);
            return;
        }
        entityData.set(JAW,Math.max(0,jawOpen()-.11F));
        if(target==null){wander();return;}
        if(!target.isInWaterOrBubble() && target.getY()>getY()+4){
            getNavigation().stop();
            setDeltaMovement(getDeltaMovement().scale(.72));
            if(action()!=IDLE)begin(IDLE,0);
            return;
        }
        getLookControl().setLookAt(target,25,25);
        Vec3 to=target.getEyePosition().subtract(center());
        double d=distanceToSqr(target);
        if(water && tickCount%10==0)getNavigation().moveTo(target,1.3);
        if(d>12)steer(to,d>90?.26:.17);
        if(vortexCooldown==0 && d<16*16 && d>7*7 && hasLineOfSight(target)){
            vortexCooldown=VORTEX_COOLDOWN;
            vortexHit.clear();
            begin(VORTEX,48);getNavigation().stop();return;
        }
        if(grabCooldown==0 && d<5*5 && d>2*2 && target.getBbWidth()<1.7
            && target.getBbHeight()<2.5 && target.isInWaterOrBubble()
            && releasedUntil.getOrDefault(target.getUUID(),0L)<=level().getGameTime()
            && hasLineOfSight(target)){
            grabCooldown=GRAB_COOLDOWN;
            begin(GRAB,26);getNavigation().stop();bubbles(16);return;
        }
        if(biteCooldown==0 && d<5.5*5.5 && hasLineOfSight(target)){
            biteCooldown=50;begin(BITE,16);return;
        }
        if(action()!=FAST && action()!=SWIM)begin(d>36?FAST:SWIM,0);
    }
    private boolean mouthContact(LivingEntity e){
        if(!prey(e)||!hasLineOfSight(e))return false;
        Vec3 mouth=center().add(forward().scale(3.35));
        return e.getBoundingBox().intersects(new AABB(mouth,mouth).inflate(1.35,1.0,1.35))
            && e.position().subtract(position()).dot(forward())>0;
    }
    private void tickBite(LivingEntity e){
        float p=1F-phaseTicks/16F;
        entityData.set(JAW,p<.48F?p/.48F:Math.max(0,(1-p)/.52F));
        if(phaseTicks<11 && phaseTicks>5 && e!=null){
            steer(e.position().subtract(position()),.35);
            if(!actionHit && mouthContact(e)){
                actionHit=true;
                e.hurt(damageSources().mobAttack(this),BITE_DAMAGE);
                bubbles(11);playSound(SoundEvents.COD_HURT,1.4F,.54F);
            }
        }
    }
    private void tickGrab(LivingEntity e){
        float p=1F-phaseTicks/26F;
        entityData.set(JAW,Math.min(.6F,p*1.5F));
        if(e==null || !prey(e))return;
        if(phaseTicks>16)return;
        steer(e.position().subtract(position()),.24);
        if(!actionHit && e.isInWaterOrBubble() && distanceToSqr(e)<4.2*4.2 && hasLineOfSight(e)){
            actionHit=true;victimId=e.getUUID();holdTicks=MAX_HOLD;rescueDamage=0;
            begin(HOLD,0);getNavigation().stop();bubbles(19);
        }
    }
    private Vec3 ringCenter(){return center().subtract(forward().scale(1.1));}
    private void holdVictim(){
        if(!(level() instanceof ServerLevel server)) {releaseVictim();return;}
        Entity caught=server.getEntity(victimId);
        if(!(caught instanceof LivingEntity e)||!prey(e)||!e.isInWaterOrBubble()){
            releaseVictim();return;
        }
        if(holdTicks--<=0){releaseVictim();return;}
        Vec3 at=ringCenter().add(0,-e.getBbHeight()*.45,0);
        AABB moved=e.getBoundingBox().move(at.subtract(e.position()));
        // Only solid world geometry blocks the captive: the boss's own ring must not cancel its grab.
        if(!level().noBlockCollision(e,moved)){releaseVictim();return;}
        e.setPos(at.x,at.y,at.z);
        e.setDeltaMovement(Vec3.ZERO);
        e.hurtMarked=true;
        e.fallDistance=0;
        if(holdTicks%16==0)bubbles(6);
        // No automatic DoT during a grab; escape depends on actual damage to the boss.
    }
    private void releaseVictim(){
        if(victimId==null)return;
        if(level() instanceof ServerLevel server && server.getEntity(victimId) instanceof LivingEntity e){
            releasedUntil.put(victimId,level().getGameTime()+240);
            Vec3 push=forward().scale(.9);
            e.push(push.x,.2,push.z);
            e.hurtMarked=true;
        }
        victimId=null;holdTicks=0;
        grabCooldown=Math.max(grabCooldown,150);
        begin(RELEASE,12);
        entityData.set(JAW,.55F);
    }
    private void tickVortex(){
        int elapsed=48-phaseTicks;
        if(elapsed==1)bubbles(15);
        if(elapsed==20)bubbles(30);
        // A rotating ring of bubble trails distinguishes the vortex from a plain splash.
        if(elapsed%3==0 && level() instanceof ServerLevel server){
            for(int i=0;i<10;i++){
                double angle=elapsed*.27+i*Math.PI*2/10.0;
                double radius=3.0+Math.min(6.0,elapsed*.16);
                Vec3 point=center().add(Math.cos(angle)*radius,.3*Math.sin(angle*2),
                    Math.sin(angle)*radius);
                server.sendParticles(ParticleTypes.BUBBLE,point.x,point.y,point.z,
                    2,.12,.12,.12,.025);
            }
        }
        if(elapsed>=20 && elapsed<34){
            for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(12),this::prey)){
                Vec3 to=center().subtract(e.getEyePosition());
                if(to.lengthSqr()>144 || !hasLineOfSight(e))continue;
                if(e.isInWaterOrBubble() && to.lengthSqr()>.3){
                    Vec3 drift=to.normalize().scale(.055);
                    e.push(drift.x,Math.max(-.05,Math.min(.05,drift.y)),drift.z);
                    e.hurtMarked=true;
                }
            }
        }
        if(elapsed>=34 && elapsed<39){
            for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(10),this::prey)){
                Vec3 away=e.position().subtract(position());
                if(away.lengthSqr()>121 || !hasLineOfSight(e)
                    || !vortexHit.add(e.getUUID()))continue;
                e.hurt(damageSources().mobAttack(this),VORTEX_DAMAGE);
                if(away.lengthSqr()>.1){
                    Vec3 push=away.normalize().scale(1.0);
                    e.push(push.x,.32,push.z);e.hurtMarked=true;
                }
            }
            if(elapsed==34)bubbles(45);
        }
    }
    private void seekWater(){
        for(int d=1;d<=7;d++)for(int x=-d;x<=d;x++)for(int z=-d;z<=d;z++){
            BlockPos p=blockPosition().offset(x,-1,z);
            if(level().getFluidState(p).is(FluidTags.WATER)){
                Vec3 move=Vec3.atCenterOf(p).subtract(position());
                if(move.lengthSqr()>.01){
                    setDeltaMovement(getDeltaMovement().scale(.65).add(move.normalize().scale(.12)));
                    hasImpulse=true;
                }
                return;
            }
        }
    }
    private void wander(){
        if(--wanderTicks>0)return;
        wanderTicks=55+random.nextInt(95);
        BlockPos target=blockPosition().offset(random.nextInt(21)-10,random.nextInt(9)-4,random.nextInt(21)-10);
        if(freeWater(Vec3.atCenterOf(target))){
            getNavigation().moveTo(target.getX()+.5,target.getY()+.5,target.getZ()+.5,1);
            begin(SWIM,0);
        }
    }
    @Override public boolean hurt(DamageSource src,float amount) {
        float before=getHealth();
        boolean applied=super.hurt(src,amount);
        if(applied && victimId!=null && src.getEntity() instanceof LivingEntity attacker
            && attacker!=this && level() instanceof ServerLevel){
            rescueDamage+=Math.max(0,before-getHealth());
            if(rescueDamage>=24)releaseVictim();
        }
        return applied;
    }
    @Override public void die(DamageSource source){
        releaseVictim();
        super.die(source);
    }
    @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);
        tag.putInt("BiteCd",biteCooldown);
        tag.putInt("GrabCd",grabCooldown);
        tag.putInt("VortexCd",vortexCooldown);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        biteCooldown=Math.max(0,tag.getInt("BiteCd"));
        grabCooldown=Math.max(0,tag.getInt("GrabCd"));
        vortexCooldown=Math.max(0,tag.getInt("VortexCd"));
        victimId=null;entityData.set(JAW,0F);
        begin(IDLE,0);
    }
}
