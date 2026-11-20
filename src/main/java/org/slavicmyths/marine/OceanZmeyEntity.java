package org.slavicmyths.marine;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Ocean Zmey: separate from the existing deep_sea_serpent.
 * Attack windows and flight are server authoritative; animations read synced ACTION and START.
 */
public final class OceanZmeyEntity extends Monster {
    public static final int IDLE=0,SWIM=1,BITE=2,TAIL=3,GRAB=4,STORM=5,TAKEOFF=6,FLIGHT=7,LAND=8;
    private static final EntityDataAccessor<Integer> ACTION=
        SynchedEntityData.defineId(OceanZmeyEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> START=
        SynchedEntityData.defineId(OceanZmeyEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> JAW=
        SynchedEntityData.defineId(OceanZmeyEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> AIRBORNE=
        SynchedEntityData.defineId(OceanZmeyEntity.class,EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bar=new ServerBossEvent(
        Component.translatableWithFallback("entity.slavicmyths.ocean_zmey","Змей морских бурь"),
        BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
    private final Set<UUID> hit=new HashSet<>();
    private int biteCd=20,tailCd=85,grabCd=135,stormCd=190,flightCd=200,phaseTicks;
    private int airborneTicks;
    private UUID grabbed;
    private int holdTicks;
    private Vec3 home;
    private int dryTicks;
    private static final int SEGMENTS=16;
    public static final float BODY_LENGTH=26F;
    private final Segment[] parts=new Segment[SEGMENTS+1];

    public static float radius(float fraction) {
        float t=Mth.clamp(fraction,0F,1F);
        if(t<.12F)return Mth.lerp(t/.12F,1.5F,2.06F);
        if(t<.33F)return Mth.lerp((t-.12F)/.21F,2.06F,1.88F);
        if(t<.61F)return Mth.lerp((t-.33F)/.28F,1.88F,.98F);
        return Mth.lerp((t-.61F)/.39F,.98F,.09F);
    }
    public static final class Segment extends net.neoforged.neoforge.entity.PartEntity<OceanZmeyEntity> {
        private final float width,height;
        Segment(OceanZmeyEntity parent,float w,float h){
            super(parent);width=w;height=h;noPhysics=true;refreshDimensions();
        }
        @Override protected void defineSynchedData(SynchedEntityData.Builder builder){}
        @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag){}
        @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag){}
        @Override public EntityDimensions getDimensions(Pose pose){
            return EntityDimensions.scalable(width,height);
        }
        @Override public boolean isPickable(){return true;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean is(Entity entity){return entity==this||entity==getParent();}
        @Override public boolean hurt(DamageSource source,float amount){return getParent().hurt(source,amount);}
    }
    @Override public boolean isMultipartEntity(){return true;}
    @Override public net.neoforged.neoforge.entity.PartEntity<?>[] getParts(){return parts;}
    @Override public void setId(int id){
        super.setId(id);
        if(parts!=null)for(int i=0;i<parts.length;i++)if(parts[i]!=null)parts[i].setId(id+i+1);
    }

    public OceanZmeyEntity(EntityType<? extends OceanZmeyEntity> type,Level level){
        super(type,level);xpReward=170;
        if(getAttribute(Attributes.MAX_HEALTH)!=null){
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(1500);
            setHealth(1500);
        }
        if(getAttribute(Attributes.ATTACK_DAMAGE)!=null)
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(26);
        for(int i=0;i<parts.length;i++){
            float r=radius(i/(float)SEGMENTS);
            parts[i]=new Segment(this,Math.max(.55F,r*2),Math.max(.55F,r*1.65F));
        }
    }
    public static AttributeSupplier.Builder attributes(){
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH,1500).add(Attributes.MOVEMENT_SPEED,.38)
            .add(Attributes.ATTACK_DAMAGE,26).add(Attributes.ARMOR,13)
            .add(Attributes.ARMOR_TOUGHNESS,4).add(Attributes.FOLLOW_RANGE,64)
            .add(Attributes.KNOCKBACK_RESISTANCE,.95);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){
        super.defineSynchedData(builder);
        builder.define(ACTION,IDLE);builder.define(START,0);builder.define(JAW,0F);
        builder.define(AIRBORNE,false);
    }
    @Override protected void registerGoals(){}
    @Override public boolean removeWhenFarAway(double d){return false;}
    @Override public Component getName(){
        return hasCustomName()?super.getName():
            Component.translatableWithFallback("entity.slavicmyths.ocean_zmey","Змей морских бурь");
    }
    public int action(){return entityData.get(ACTION);}
    public int actionTicks(){return Math.max(0,(int)level().getGameTime()-entityData.get(START));}
    public float jawOpen(){return entityData.get(JAW);}
    public boolean airborne(){return entityData.get(AIRBORNE);}
    private void begin(int next,int ticks){
        entityData.set(ACTION,next);
        entityData.set(START,(int)level().getGameTime());
        phaseTicks=ticks;hit.clear();
    }
    private boolean attackable(LivingEntity e){
        return e!=this && e.isAlive()&&!e.isSpectator()&&!e.isInvulnerable()
            && !(e instanceof Player p && p.isCreative());
    }
    private Vec3 forward(){
        Vec3 v=getLookAngle().multiply(1,0,1);
        return v.lengthSqr()<.001?Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize():v.normalize();
    }
    private Vec3 headPosition(){return position().add(forward().scale(3.0)).add(0,1.8,0);}
    private void findTarget(){
        if(tickCount%12!=0)return;
        LivingEntity previous=getTarget();
        if(previous!=null&&(!attackable(previous)||distanceToSqr(previous)>80*80))
            setTarget(null);
        Player nearest=null;double nearestDistance=64*64;
        // Track survival players in the air, not just targets submerged in water.
        for(Player player:level().getEntitiesOfClass(Player.class,
            getBoundingBox().inflate(64,48,64),this::attackable)){
            double distance=distanceToSqr(player);
            if(distance<nearestDistance&&(hasLineOfSight(player)||distance<14*14)){
                nearest=player;nearestDistance=distance;
            }
        }
        if(nearest!=null){setTarget(nearest);return;}
        if(previous instanceof Player&&attackable(previous))return;
        LivingEntity closest=null;double distance=48*48;
        for(LivingEntity mob:level().getEntitiesOfClass(LivingEntity.class,
            getBoundingBox().inflate(48,20,48),this::attackable)){
            if(mob instanceof Player||mob instanceof OceanZmeyEntity)continue;
            double d=distanceToSqr(mob);
            if(d<distance&&(mob.isInWaterOrBubble()||d<14*14)
                &&(hasLineOfSight(mob)||d<10*10)){closest=mob;distance=d;}
        }
        if(closest!=null)setTarget(closest);
    }
    private void faceTarget(LivingEntity target){
        Vec3 direction=target.position().subtract(position());
        if(direction.horizontalDistanceSqr()<.0001)return;
        float goal=(float)(Mth.atan2(-direction.x,direction.z)*Mth.RAD_TO_DEG);
        setYRot(getYRot()+Mth.clamp(Mth.wrapDegrees(goal-getYRot()),-15F,15F));
        setYHeadRot(getYRot());setYBodyRot(getYRot());
    }
    private void moveToward(Vec3 destination,double speed){
        Vec3 delta=destination.subtract(position());
        if(delta.lengthSqr()<.01)return;
        float goal=(float)(Mth.atan2(-delta.x,delta.z)*Mth.RAD_TO_DEG);
        setYRot(getYRot()+Mth.clamp(Mth.wrapDegrees(goal-getYRot()),-13F,13F));
        setYHeadRot(getYRot());setYBodyRot(getYRot());
        double inertia=delta.lengthSqr()<8*8?.48:.70;
        Vec3 motion=getDeltaMovement().scale(inertia)
            .add(delta.normalize().scale(speed*(1-inertia)));
        if(airborne()&&!clearFlight(motion)){
            setDeltaMovement(getDeltaMovement().scale(.35));return;
        }
        BlockPos next=BlockPos.containing(position().add(motion.scale(3)));
        if(!level().hasChunkAt(next))return;
        if(!level().getBlockState(next).getCollisionShape(level(),next).isEmpty())
            motion=new Vec3(-motion.z,.1,motion.x).scale(.45);
        setDeltaMovement(motion);hasImpulse=true;
    }
    private boolean clearFlight(Vec3 velocity){
        if(!level().noCollision(this,getBoundingBox().move(velocity).inflate(.25)))return false;
        Vec3 horizontal=velocity.multiply(1,0,1);
        if(horizontal.lengthSqr()<.0001)horizontal=forward();
        else horizontal=horizontal.normalize();
        Vec3 side=new Vec3(-horizontal.z,0,horizontal.x);
        for(int step=1;step<=3;step++){
            Vec3 center=position().add(velocity.scale(step));
            for(int sign:new int[]{-1,1})
                for(int span=4;span<=8;span+=4){
                    BlockPos p=BlockPos.containing(center.add(side.scale(sign*span)).add(0,2,0));
                    if(!level().hasChunkAt(p)
                        ||!level().getBlockState(p).getCollisionShape(level(),p).isEmpty())
                        return false;
                }
        }
        return true;
    }
    private void damageArc(double distance,double width,float damage,boolean rear){
        Vec3 center=rear?position().subtract(forward().scale(5)):headPosition();
        for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,
            new AABB(center,center).inflate(distance,3,distance),this::attackable)){
            if(!hit.add(e.getUUID()))continue;
            Vec3 offset=e.position().subtract(center);
            if(offset.lengthSqr()>distance*distance+9 || Math.abs(offset.y)>4){
                hit.remove(e.getUUID());continue;
            }
            Vec3 d=offset.multiply(1,0,1);
            if(d.lengthSqr()>.1 && forward().dot(d.normalize())*(rear?-1:1)<width){
                hit.remove(e.getUUID());continue;
            }
            e.hurt(damageSources().mobAttack(this),damage);
            Vec3 impulse=d.lengthSqr()>.1?d.normalize().scale(rear?1.7:.9):forward();
            e.push(impulse.x,.35,impulse.z);
        }
    }
    private LivingEntity victim(){
        if(grabbed==null || !(level() instanceof ServerLevel server))return null;
        Entity e=server.getEntity(grabbed);
        return e instanceof LivingEntity living && attackable(living)?living:null;
    }
    private void release(){
        LivingEntity v=victim();
        if(v!=null){Vec3 push=forward().scale(1.4);v.push(push.x,.5,push.z);}
        grabbed=null;holdTicks=0;
    }
    private void doGrab(){
        Vec3 mouth=headPosition();
        for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,
            new AABB(mouth,mouth).inflate(2.7,2.5,2.7),this::attackable)){
            if(!e.isPassenger() && grabbed==null){
                grabbed=e.getUUID();holdTicks=30;
                e.hurt(damageSources().mobAttack(this),12F);
                break;
            }
        }
    }
    static Vec3 stormCenter(Vec3 origin,int age,int index){
        double angle=index*Mth.TWO_PI/5+age*.03;
        return origin.add(Math.cos(angle)*9,0,Math.sin(angle)*9);
    }
    private void storm(){
        if(!(level() instanceof ServerLevel server))return;
        Vec3[] centers=new Vec3[5];
        for(int i=0;i<centers.length;i++){
            Vec3 center=stormCenter(position(),tickCount,i);
            centers[i]=center;
            server.sendParticles(ParticleTypes.SPLASH,center.x,center.y+2,center.z,
                30,2.4,3,2.4,.18);
            server.sendParticles(ParticleTypes.CLOUD,center.x,center.y+4,center.z,
                18,1.4,2,1.4,.06);
        }
        // Only the five visible funnels can damage or pull a victim.
        // Previously every entity in a 15-block sphere was hit, including
        // players standing safely between the telegraphed storm funnels.
        for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,
            getBoundingBox().inflate(15,8,15),this::attackable)){
            for(Vec3 center:centers){
                Vec3 from=e.position().subtract(center);
                Vec3 horizontal=from.multiply(1,0,1);
                double radius=horizontal.length();
                if(radius>4.5 || Math.abs(from.y)>5)continue;
                double strength=.12+.16*(1-radius/4.5);
                Vec3 inward=radius>.01?horizontal.scale(-strength/radius):Vec3.ZERO;
                Vec3 orbit=radius>.01?new Vec3(-horizontal.z,0,horizontal.x)
                    .scale(.16/radius):Vec3.ZERO;
                e.setDeltaMovement(e.getDeltaMovement().add(
                    inward.x+orbit.x,.10,inward.z+orbit.z));
                e.hurtMarked=true;
                if(tickCount%20==0)e.hurt(damageSources().mobAttack(this),8);
                break;
            }
        }
    }
    private void updateParts(){
        Vec3 backwards=forward().scale(-1);
        Vec3 side=new Vec3(-backwards.z,0,backwards.x);
        double yaw=0,age=tickCount*.085;
        Vec3 cursor=position().add(0,1.6,0);
        for(int i=0;i<parts.length;i++){
            double t=i/(double)SEGMENTS;
            double boost=action()==FLIGHT?2.1:action()==TAIL?2.7:1;
            double arc=.075*Math.sin(age*boost-i*.34)*(.3+i/(double)(SEGMENTS-1));
            if(action()==TAIL)arc+=.22*Math.sin(Math.PI*Mth.clamp(actionTicks()/42F,0,1))*i/(double)SEGMENTS;
            if(action()==GRAB && i>2)arc+=.11*Mth.clamp(actionTicks()/26F,0,1);
            if(action()==STORM && i>1)arc+=.14*Mth.clamp(actionTicks()/22F,0,1);
            yaw+=arc;
            Vec3 tangent=backwards.scale(Math.cos(yaw)).add(side.scale(Math.sin(yaw)));
            Vec3 at=cursor.add(tangent.scale(BODY_LENGTH/SEGMENTS*.5));
            parts[i].setPos(at.x,at.y-parts[i].getBbHeight()*.5,at.z);
            cursor=cursor.add(tangent.scale(BODY_LENGTH/SEGMENTS));
        }
    }
    @Override public void aiStep(){
        super.aiStep();updateParts();
        if(level().isClientSide)return;
        if(level().getDifficulty()==Difficulty.PEACEFUL){discard();return;}
        if(home==null)home=position();
        bar.setProgress(getHealth()/getMaxHealth());findTarget();
        if(biteCd>0)biteCd--;if(tailCd>0)tailCd--;
        if(grabCd>0)grabCd--;if(stormCd>0)stormCd--;if(flightCd>0)flightCd--;
        int state=action(),ticks=actionTicks();
        LivingEntity target=getTarget();
        if(target!=null&&state!=LAND)faceTarget(target);
        if(!isInWaterOrBubble()&&!airborne()&&state!=LAND){
            if(++dryTicks>100)moveToward(home,.16);
        }else dryTicks=0;
        if(airborne()&&state!=LAND){
            setNoGravity(true);
            if(state!=TAKEOFF&&--airborneTicks<=0){
                if(state==GRAB)release();
                begin(LAND,48);state=LAND;ticks=0;
            }
        }
        entityData.set(JAW,(state==BITE||state==GRAB)
            ?Mth.sin(Mth.PI*Mth.clamp(ticks/(state==BITE?26F:40F),0,1)):0F);
        if(state==BITE&&ticks==17)damageArc(6.2,-.1F,30F,false);
        if(state==TAIL&&ticks==24)damageArc(9,-.45F,38F,true);
        if(state==GRAB&&ticks==19)doGrab();
        if(state==GRAB&&grabbed!=null){
            LivingEntity v=victim();
            if(v==null)release();
            else{
                Vec3 mouth=headPosition();
                v.teleportTo(mouth.x,mouth.y-.7,mouth.z);
                v.setDeltaMovement(Vec3.ZERO);
                if(--holdTicks<=0){v.hurt(damageSources().mobAttack(this),18);release();}
            }
        }
        if(state==STORM&&ticks>=18&&ticks<=90&&ticks%5==0)storm();
        if(state==TAKEOFF){
            setNoGravity(true);moveToward(position().add(0,8,0),.30);
        }
        if(state==FLIGHT){
            setNoGravity(true);
            if(target!=null){
                Vec3 destination=target.position().add(0,target.getBbHeight()*.45,0);
                if(position().distanceToSqr(destination)>5*5)moveToward(destination,.36);
                else setDeltaMovement(getDeltaMovement().scale(.60));
            }else moveToward(home.add(0,8,0),.28);
            if(ticks%18==0&&level() instanceof ServerLevel server)
                server.sendParticles(ParticleTypes.CLOUD,getX(),getY(),getZ(),18,2,1,2,.11);
        }
        if(state==LAND){setNoGravity(false);moveToward(home,.24);}
        if(airborne()&&(state==BITE||state==GRAB)&&target!=null
            &&distanceToSqr(target)>3.7*3.7)
            moveToward(target.position().add(0,target.getBbHeight()*.45,0),.30);
        if(phaseTicks>0&&--phaseTicks==0){
            if(state==TAKEOFF)begin(FLIGHT,0);
            else if(state==LAND){
                entityData.set(AIRBORNE,false);begin(IDLE,0);
            }else{
                if(state==GRAB)release();
                begin(airborne()?FLIGHT:IDLE,0);
            }
        }
        if(action()!=IDLE&&action()!=SWIM&&action()!=FLIGHT)return;
        if(target==null){
            if(!airborne()&&distanceToSqr(home)>20*20)moveToward(home,.17);
            else if(!airborne()&&isInWater())setDeltaMovement(getDeltaMovement().scale(.86));
            return;
        }
        double distance=distanceToSqr(target);
        if(!airborne()&&isInWater()&&level().canSeeSky(blockPosition().above(3))
            &&(flightCd==0||(target.getY()>getY()+6&&flightCd<90))){
            flightCd=450;airborneTicks=190;entityData.set(AIRBORNE,true);
            begin(TAKEOFF,28);return;
        }
        if(biteCd==0&&distance<9*9){biteCd=55;begin(BITE,32);return;}
        if(grabCd==0&&distance<7*7){grabCd=175;begin(GRAB,55);return;}
        if(tailCd==0&&distance<11*11){tailCd=130;begin(TAIL,42);return;}
        if(stormCd==0&&distance<23*23){stormCd=270;begin(STORM,105);return;}
        if(distance<8*8){setDeltaMovement(getDeltaMovement().scale(.58));return;}
        if(!airborne()&&action()!=SWIM)begin(SWIM,0);
        if(!airborne())moveToward(target.position().add(0,target.getBbHeight()*.4,0),.29);
    }
    @Override public void startSeenByPlayer(ServerPlayer player){
        super.startSeenByPlayer(player);bar.addPlayer(player);
    }
    @Override public void stopSeenByPlayer(ServerPlayer player){
        super.stopSeenByPlayer(player);bar.removePlayer(player);
    }
    @Override public void die(DamageSource source){
        release();setNoGravity(false);super.die(source);
    }
    @Override public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type){
        return type != net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value();
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag){
        super.addAdditionalSaveData(tag);
        tag.putInt("BiteCooldown",biteCd);tag.putInt("TailCooldown",tailCd);
        tag.putInt("GrabCooldown",grabCd);tag.putInt("StormCooldown",stormCd);
        tag.putInt("FlightCooldown",flightCd);
        if(home!=null){tag.putDouble("HomeX",home.x);tag.putDouble("HomeY",home.y);tag.putDouble("HomeZ",home.z);}
    }
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag){
        super.readAdditionalSaveData(tag);
        biteCd=tag.getInt("BiteCooldown");tailCd=tag.getInt("TailCooldown");
        grabCd=tag.getInt("GrabCooldown");stormCd=tag.getInt("StormCooldown");
        flightCd=tag.getInt("FlightCooldown");
        if(tag.contains("HomeX"))home=new Vec3(tag.getDouble("HomeX"),tag.getDouble("HomeY"),tag.getDouble("HomeZ"));
        release();setNoGravity(false);airborneTicks=0;
        entityData.set(AIRBORNE,false);begin(IDLE,0);
    }
}
