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
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class StratimBirdEntity extends Monster {
    // Aquatic encounters may occupy water; retain block and entity obstruction checks.
    @Override public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader world) {
        return world.isUnobstructed(this) && world.noCollision(this);
    }

    public static final int PATROL=0,CHASE=1,BEAK=2,TALONS=3,DIVE=4,GUST=5,STORM=6,RETURN=7;
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(StratimBirdEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> START=SynchedEntityData.defineId(StratimBirdEntity.class,EntityDataSerializers.INT);
    private final ServerBossEvent bar=new org.slavicmyths.combat.CreatureBossBar(this,
        Component.translatableWithFallback("entity.slavicmyths.stratim_bird","Стратим-птица"),
        BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
    private final Set<UUID> damaged=new HashSet<>();
    private int beakCd=30,talonsCd=90,diveCd=190,gustCd=120,stormCd=420;
    private int phaseTicks,patrolTicks,unseenTicks,unattendedTicks;
    private Vec3 home;
    private boolean landedOnSpawn;
    private final HitSection[] sections=new HitSection[8];
    /** Separate pickable zones follow the long wings, skull and tail rather than the root hitbox. */
    public static final class HitSection extends net.neoforged.neoforge.entity.PartEntity<StratimBirdEntity> {
        private final float width,height;
        private HitSection(StratimBirdEntity parent,float width,float height){
            super(parent);this.width=width;this.height=height;this.noPhysics=true;refreshDimensions();
        }
        @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
        @Override protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag){}
        @Override protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag){}
        @Override public EntityDimensions getDimensions(Pose pose){
            return EntityDimensions.scalable(width,height);
        }
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean isPickable(){return true;}
        @Override public boolean is(Entity e){return e==this||e==getParent();}
        @Override public boolean hurt(DamageSource src,float damage){
            return getParent().hurt(src,damage);
        }
    }
    @Override public boolean isMultipartEntity(){return true;}
    @Override public net.neoforged.neoforge.entity.PartEntity<?>[] getParts(){return sections;}
    @Override public void setId(int id){
        super.setId(id);
        if(sections!=null)for(int i=0;i<sections.length;i++)
            if(sections[i]!=null)sections[i].setId(id+i+1);
    }
    private static void place(HitSection part,Vec3 center){
        part.setPos(center.x,center.y-part.getBbHeight()*.5,center.z);
    }
    private void updateHitSections(){
        if(sections[0]==null)return;
        Vec3 facing=Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();
        if(facing.lengthSqr()<.1)facing=new Vec3(0,0,-1);
        Vec3 right=new Vec3(-facing.z,0,facing.x);
        Vec3 origin=position().add(0,2.7,0);
        place(sections[0],origin.add(facing.scale(4.1)).add(0,1.1,0));
        for(int sign:new int[]{-1,1})
            for(int j=0;j<3;j++){
                double distance=3.4+j*4.8;
                double flap=(action()==STORM?1.5:.45)*Math.sin(tickCount*.13+sign*.2);
                Vec3 center=origin.add(right.scale(sign*distance))
                    .add(facing.scale(j==2?-.5:0)).add(0,flap,0);
                place(sections[sign<0?1+j:4+j],center);
            }
        place(sections[7],origin.subtract(facing.scale(4.8)).add(0,-.4,0));
    }
    public StratimBirdEntity(EntityType<? extends StratimBirdEntity> type,Level level){
        super(type,level);xpReward=120;setNoGravity(true);
        sections[0]=new HitSection(this,2.0F,2.1F);
        for(int i=1;i<=6;i++)sections[i]=new HitSection(this,i%3==0?3.1F:4.3F,1.85F);
        sections[7]=new HitSection(this,2.5F,1.5F);
        updateHitSections();
    }
    public static AttributeSupplier.Builder attributes(){
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH,1024).add(Attributes.ARMOR,8)
            .add(Attributes.ARMOR_TOUGHNESS,2).add(Attributes.ATTACK_DAMAGE,22)
            .add(Attributes.MOVEMENT_SPEED,.65).add(Attributes.FLYING_SPEED,.65)
            .add(Attributes.FOLLOW_RANGE,64).add(Attributes.KNOCKBACK_RESISTANCE,.75);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){
        super.defineSynchedData(builder);builder.define(ACTION,PATROL);builder.define(START,0);
    }
    @Override public Component getName(){
        return hasCustomName()?super.getName():
            Component.translatableWithFallback("entity.slavicmyths.stratim_bird","Стратим-птица");
    }
    @Override protected void registerGoals(){}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source){return false;}
    @Override public boolean isPushable(){return false;}
    public int action(){return entityData.get(ACTION);}
    public int actionTicks(){return Math.max(0,(int)level().getGameTime()-entityData.get(START));}
    private void begin(int state,int ticks){
        entityData.set(ACTION,state);entityData.set(START,(int)level().getGameTime());
        phaseTicks=ticks;damaged.clear();
    }
    private boolean prey(LivingEntity target){
        return target!=this && target.isAlive() && !target.isSpectator()
            && !(target instanceof Player p&&(p.isCreative()||p.isSpectator()))
            && !(target instanceof StratimBirdEntity)
            && target.level()==level();
    }
    private void findTarget(){
        LivingEntity old=getTarget();
        if(old!=null && (!prey(old)||distanceToSqr(old)>80*80)){
            setTarget(null);old=null;
        }
        if(tickCount%20!=0 || old!=null)return;
        LivingEntity best=null;double near=64*64;
        for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,
            getBoundingBox().inflate(64),this::prey)){
            // The bird pursues players aboard ships and creatures at the water surface.
            if(!(e instanceof Player) && !e.isInWaterOrBubble())continue;
            double d=distanceToSqr(e);
            if(d<near && hasLineOfSight(e)){near=d;best=e;}
        }
        if(best!=null)setTarget(best);
    }
    static BlockPos wingSample(Vec3 center,Vec3 heading,int side,int radius,int height){
        Vec3 horizontal=new Vec3(heading.x,0,heading.z);
        if(horizontal.lengthSqr()<.0001)horizontal=new Vec3(0,0,1);
        else horizontal=horizontal.normalize();
        Vec3 sideways=new Vec3(-horizontal.z,0,horizontal.x);
        return BlockPos.containing(center.add(sideways.scale(side*radius)).add(0,height,0));
    }
    static boolean hasWingClearance(Level level,Vec3 destination,Vec3 heading){
        if(destination.y<level.getMinBuildHeight()+5
            ||destination.y>level.getMaxBuildHeight()-7)return false;
        // The 4.5-wide root hitbox does not cover the articulated wings.
        // Include their low flap position and high stroke, in flight-relative axes.
        for(int side=-1;side<=1;side+=2)
            for(int radius=0;radius<=16;radius+=4)
                for(int height=0;height<=6;height+=2){
                    BlockPos pos=wingSample(destination,heading,side,radius,height);
                    if(!level.hasChunkAt(pos)
                        ||!level.getBlockState(pos).getCollisionShape(level,pos).isEmpty()
                        ||!level.getFluidState(pos).isEmpty())return false;
                }
        return true;
    }
    private boolean clear(Vec3 destination){
        Vec3 movement=destination.subtract(position());
        int steps=Math.max(1,Mth.ceil(movement.length()/1.5));
        for(int i=1;i<=steps;i++){
            Vec3 delta=movement.scale(i/(double)steps);
            Vec3 sample=position().add(delta);
            if(!hasWingClearance(level(),sample,movement))return false;
            if(!level().noCollision(this,getBoundingBox().expandTowards(delta).inflate(.35)))return false;
        }
        return true;
    }
    private void fly(Vec3 destination,double speed){
        Vec3 delta=destination.subtract(position());
        if(delta.lengthSqr()<.02){setDeltaMovement(getDeltaMovement().scale(.72));return;}
        Vec3 desired=delta.normalize().scale(speed);
        Vec3 next=getDeltaMovement().scale(.70).add(desired.scale(.30));
        if(next.lengthSqr()>speed*speed)next=next.normalize().scale(speed);
        if(!clear(position().add(next.scale(3)))){
            Vec3 raised=position().add(0,4,0);
            Vec3 lateral=position().add(-next.z*5,1,next.x*5);
            if(clear(raised))next=new Vec3(0,.16,0);
            else if(clear(lateral))next=new Vec3(-next.z*.6,.06,next.x*.6);
            else next=Vec3.ZERO;
        }
        setDeltaMovement(next);hasImpulse=true;
        if(next.horizontalDistanceSqr()>.001){
            setYRot((float)(Mth.atan2(-next.x,next.z)*Mth.RAD_TO_DEG));
            yBodyRot=getYRot();yHeadRot=getYRot();
        }
    }
    private void hitArea(double radius,float damage,double push,int limit){
        if(!(level() instanceof ServerLevel))return;
        for(LivingEntity e:level().getEntitiesOfClass(LivingEntity.class,
            getBoundingBox().inflate(radius),this::prey)){
            if(damaged.size()>=limit)break;
            if(distanceToSqr(e)>radius*radius || !hasLineOfSight(e)||!damaged.add(e.getUUID()))continue;
            if(e.hurt(damageSources().mobAttack(this),damage)){
                Vec3 v=e.position().subtract(position());
                if(v.lengthSqr()>.01){v=v.normalize().scale(push);e.push(v.x,Math.max(.12,v.y*.4),v.z);e.hurtMarked=true;}
            }
        }
    }
    private void stormParticles(){
        if(level() instanceof ServerLevel s){
            for(int i=0;i<9;i++){
                double a=tickCount*.19+i*Math.PI*2/9;
                double r=3+actionTicks()*.12;
                s.sendParticles(ParticleTypes.CLOUD,getX()+Math.cos(a)*r,
                    getY()+Math.sin(a*2)*1.2,getZ()+Math.sin(a)*r,2,.3,.2,.3,.04);
            }
        }
    }
    private void tickAction(LivingEntity target){
        int elapsed=actionTicks();
        switch(action()){
            case BEAK -> {if(elapsed==11)hitArea(5,22,.65,1);}
            case TALONS -> {if(elapsed>=13&&elapsed<=17)hitArea(6,25,1,2);}
            case DIVE -> {
                if(target!=null && elapsed<29)fly(target.position().add(0,1,0),1.25);
                if(elapsed>=20&&elapsed<=32)hitArea(5,33,1.35,2);
            }
            case GUST -> {
                if(elapsed==15)hitArea(13,17,1.6,24);
                if(elapsed>8&&elapsed<25&&tickCount%3==0)stormParticles();
            }
            case STORM -> {
                if(elapsed%4==0)stormParticles();
                if(elapsed==25||elapsed==45||elapsed==65)hitArea(17,12,.6,30);
                if(elapsed==35||elapsed==55)damaged.clear();
            }
            default -> {}
        }
        if(--phaseTicks<=0)begin(target==null?RETURN:CHASE,0);
    }
    @Override public void travel(Vec3 input){
        // Server flies by steering velocity; clients interpolate the synchronized movement.
        move(MoverType.SELF,getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(.96));
        fallDistance=0;
    }
    @Override public void aiStep(){
        super.aiStep();
        updateHitSections();
        setNoGravity(true);fallDistance=0;
        if(level().isClientSide)return;
        bar.setProgress(getHealth()/Math.max(1,getMaxHealth()));
        if(home==null)home=position();
        if(!landedOnSpawn){
            landedOnSpawn=true;
            // Natural spawn placement is at the sea surface: ascend before patrolling.
            if(level().getFluidState(blockPosition()).is(net.minecraft.tags.FluidTags.WATER))
                setPos(getX(),getY()+12,getZ());
        }
        if(beakCd>0)beakCd--;if(talonsCd>0)talonsCd--;
        if(diveCd>0)diveCd--;if(gustCd>0)gustCd--;if(stormCd>0)stormCd--;
        findTarget();
        LivingEntity target=getTarget();
        // Persistent ocean bosses must not accumulate indefinitely in unattended regions.
        // A real player within the arena keeps the encounter active.
        boolean playerNearby=!level().getEntitiesOfClass(Player.class,
            getBoundingBox().inflate(96),p->!p.isSpectator()).isEmpty();
        unattendedTicks=(target==null&&!playerNearby)?unattendedTicks+1:0;
        if(unattendedTicks>12000){discard();return;}
        if(target!=null&&!hasLineOfSight(target)){
            if(++unseenTicks>100){setTarget(null);target=null;unseenTicks=0;}
        }else unseenTicks=0;
        if(phaseTicks>0){tickAction(target);return;}
        if(target!=null){
            double dist=distanceToSqr(target);
            fly(target.position().add(0,dist>256?11:4,0),dist>300?.65:.42);
            if(dist<18*18 && stormCd==0){
                stormCd=600;begin(STORM,82);return;
            }
            if(dist<15*15 && gustCd==0){gustCd=240;begin(GUST,32);return;}
            if(dist>9*9 && dist<27*27 && diveCd==0){
                diveCd=300;begin(DIVE,39);return;
            }
            if(dist<6*6 && talonsCd==0){talonsCd=140;begin(TALONS,25);return;}
            if(dist<5*5 && beakCd==0){beakCd=50;begin(BEAK,20);return;}
            if(action()!=CHASE)begin(CHASE,0);
        }else{
            if(++patrolTicks%180==0 || action()!=PATROL){
                Vec3 goal=home.add(Math.sin(tickCount*.003)*28,9+Math.sin(tickCount*.012)*5,
                    Math.cos(tickCount*.003)*28);
                if(clear(goal))home=home.add(0,0,0);
                begin(PATROL,0);
            }
            Vec3 circle=home.add(Math.sin(tickCount*.008)*28,10+Math.sin(tickCount*.019)*3,
                Math.cos(tickCount*.008)*28);
            fly(circle,.33);
        }
    }
    @Override public boolean hurt(DamageSource src,float amount){
        if (src.is(net.minecraft.world.damagesource.DamageTypes.DROWN)) return false;
        return super.hurt(src,amount);
    }
    @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
}
