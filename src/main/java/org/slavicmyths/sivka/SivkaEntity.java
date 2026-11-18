package org.slavicmyths.sivka;

import java.util.*;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Server-driven transport. Null controlling passenger keeps vanilla vehicle packets from owning physics. */
public final class SivkaEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> MODE=SynchedEntityData.defineId(SivkaEntity.class,EntityDataSerializers.INT), REACTION=SynchedEntityData.defineId(SivkaEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> REACT_START=SynchedEntityData.defineId(SivkaEntity.class,EntityDataSerializers.LONG),MODE_START=SynchedEntityData.defineId(SivkaEntity.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Float> PHASE=SynchedEntityData.defineId(SivkaEntity.class,EntityDataSerializers.FLOAT), SPEED=SynchedEntityData.defineId(SivkaEntity.class,EntityDataSerializers.FLOAT), BANK=SynchedEntityData.defineId(SivkaEntity.class,EntityDataSerializers.FLOAT), LIFT=SynchedEntityData.defineId(SivkaEntity.class,EntityDataSerializers.FLOAT);
    public UUID owner,bond;public long generation;public int scared,lastHint=-100;
    private int modeTicks,readyTicks,slowTicks,departTicks,idleTicks,reactionEnd,arrivalTicks,lastSequence=-1,nextAmbient=400;
    private long inputTime=-100;private float forward,strafe,wantedYaw;private boolean sprint,jump,down,priorJump,wasMounted;
    private double speed,vertical,phase;private int previousMode;
    public SivkaEntity(EntityType<? extends SivkaEntity> type,Level w){super(type,w);setPersistenceRequired();setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER,-1);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DANGER_FIRE,-1);setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.DAMAGE_FIRE,-1);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(MODE,0);b.define(REACTION,0);b.define(REACT_START,0L);b.define(MODE_START,0L);b.define(PHASE,0F);b.define(SPEED,0F);b.define(BANK,0F);b.define(LIFT,0F);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new PanicGoal(this,1.7));goalSelector.addGoal(4,new WaterAvoidingRandomStrollGoal(this,.7,80));goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,16));goalSelector.addGoal(6,new RandomLookAroundGoal(this));}
    public int mode(){return entityData.get(MODE);}public float gait(){return entityData.get(PHASE);}public float actualSpeed(){return entityData.get(SPEED);}public float bank(){return entityData.get(BANK);}public float lift(){return entityData.get(LIFT);}public int reaction(){return entityData.get(REACTION);}public int reactionAge(){return (int)(level().getGameTime()-entityData.get(REACT_START));}public int modeAge(){return (int)(level().getGameTime()-entityData.get(MODE_START));}public boolean flying(){return mode()==1||mode()==2;}
    public void react(int kind,int ticks){entityData.set(REACTION,kind);entityData.set(REACT_START,level().getGameTime());reactionEnd=tickCount+ticks;}
    public void cue(String cue,float volume){playSound(SivkaContent.sound(cue),volume,1+(random.nextFloat()-.5F)*.08F);}
    public void spark(int count){if(level() instanceof ServerLevel w)w.sendParticles(ParticleTypes.WAX_ON,getX(),getY()+1.6,getZ(),count,.7,.6,.7,.04);}
    public boolean owns(Player p){if(owner==null||!owner.equals(p.getUUID())||!(level() instanceof ServerLevel w))return false;var b=SivkaBonds.get(w).bonds.get(owner);return b!=null&&b.id.equals(bond)&&b.generation==generation&&b.stage==7&&b.valid();}
    public void arrive(ServerPlayer p){navigation.moveTo(p,1.2);arrivalTicks=160;departTicks=0;idleTicks=0;}
    public void renewedInteraction(){idleTicks=departTicks=0;}
    @Override protected void addPassenger(Entity passenger){super.addPassenger(passenger);lastSequence=-1;inputTime=-100;forward=strafe=0;sprint=jump=down=priorJump=false;wantedYaw=passenger.getYRot();navigation.stop();renewedInteraction();}
    public void input(SivkaNetwork.Input i){long now=level().getGameTime();if(now-inputTime<2||i.sequence()<=lastSequence)return;lastSequence=i.sequence();inputTime=now;forward=i.forward();strafe=i.strafe();wantedYaw=Mth.wrapDegrees(i.yaw());sprint=i.sprint();jump=i.jump();down=i.down();}
    @Override public InteractionResult mobInteract(Player p,InteractionHand hand){var s=p.getItemInHand(hand);if(s.is(Items.LEAD)||s.is(Items.SADDLE)||s.is(Items.IRON_HORSE_ARMOR)||s.is(Items.GOLDEN_HORSE_ARMOR)||s.is(Items.DIAMOND_HORSE_ARMOR)||s.is(Items.LEATHER_HORSE_ARMOR)){if(!level().isClientSide)SivkaBonds.message(p,"free_horse");return InteractionResult.sidedSuccess(level().isClientSide);}
        if(level().isClientSide)return InteractionResult.SUCCESS;
        if(!(p instanceof ServerPlayer sp)||p.isSpectator())return InteractionResult.FAIL;
        var d=SivkaBonds.get(sp.serverLevel());if(d.feed(sp,this,s))return InteractionResult.CONSUME;
        if(owns(p)&&!isVehicle()&&!p.isSecondaryUseActive()){if(!level().noCollision(p,p.getBoundingBox().move(position().add(0,2.18,0).subtract(p.position())))){SivkaBonds.message(p,"no_room");return InteractionResult.FAIL;}p.startRiding(this);wantedYaw=p.getYRot();idleTicks=0;react(4,10);return InteractionResult.CONSUME;}
        if(owner!=null&&owner.equals(p.getUUID())&&s.isEmpty()&&p.isSecondaryUseActive()){var b=d.bonds.get(owner);if(b!=null&&b.valid()&&b.play-b.lastFeed>=24){b.token++;b.lastFeed=b.play;SivkaBonds.give(sp,b);d.setDirty();SivkaBonds.message(p,"reissued");}}
        return InteractionResult.CONSUME;
    }
    @Override public boolean canBeLeashed(){return false;}
    @Override public boolean startRiding(Entity e,boolean force){return false;}
    @Override protected boolean canAddPassenger(Entity e){return !isVehicle()&&e instanceof Player p&&owns(p);}
    @Override @Nullable public LivingEntity getControllingPassenger(){return null;}
    @Override public Vec3 getPassengerRidingPosition(Entity rider){
        // Follow the actual body animation rather than leaving the rider at a fixed world height.
        // SivkaMesh body pivot is 25.92/16 above the feet; vanilla seated player hips are
        // .75 above player feet, with a .6 vehicle attachment. Model/UV and gait curves stay intact.
        boolean air=flying();double fast=Math.clamp((actualSpeed()-6)/3,0,1);
        double bob=SivkaPoses.sample(air,gait(),2)*(air?1:fast);
        double pitch=-SivkaPoses.sample(air,gait(),1)*(air?1:fast)-lift()*50;
        if(mode()==1)pitch-=14*Math.sin(Math.clamp(modeAge()/16.0,0,1)*Math.PI);
        double tilt=Math.toRadians(pitch),roll=Math.toRadians(bank()),yaw=Math.toRadians(getYRot());
        double hip=.71*Math.cos(roll),side=-.71*Math.sin(roll);
        double back=-.08*Math.cos(tilt)+hip*Math.sin(tilt);
        double height=1.47+bob+hip*Math.cos(tilt)+.08*Math.sin(tilt);
        return position().add(side*Math.cos(yaw)-back*Math.sin(yaw),height,side*Math.sin(yaw)+back*Math.cos(yaw));
    }
    @Override protected void positionRider(Entity e,Entity.MoveFunction f){super.positionRider(e,f);e.fallDistance=0;}
    @Override public Vec3 getDismountLocationForPassenger(LivingEntity rider){
        if(flying()&&level() instanceof ServerLevel){rider.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,80,0,false,true));}
        for(int radius=2;radius<=4;radius++)for(int i=0;i<8;i++){double a=i*Math.PI/4;var p=position().add(Math.sin(a)*radius,0,Math.cos(a)*radius);var q=BlockPos.containing(p);if(level().hasChunkAt(q)&&SivkaWhistle.safeFloor(level(),q.below())&&level().noCollision(rider,rider.getBoundingBox().move(p.subtract(rider.position()))))return p;}
        return super.getDismountLocationForPassenger(rider);
    }
    @Override public void travel(Vec3 v){if(isVehicle()||flying()){if(!level().isClientSide)drive();}else super.travel(v);}
    private void setMode(int value){if(mode()!=value){previousMode=mode();entityData.set(MODE,value);entityData.set(MODE_START,level().getGameTime());modeTicks=0;}}
    private boolean headClear(double dy){var box=getBoundingBox().inflate(.6,0,.85).expandTowards(0,dy,0);return level().noCollision(this,box)&&level().hasChunkAt(BlockPos.containing(box.maxX,box.maxY,box.maxZ))&&level().hasChunkAt(BlockPos.containing(box.minX,box.minY,box.minZ));}
    private void drive(){
        boolean mounted=isVehicle();if(!mounted||level().getGameTime()-inputTime>10){forward=strafe=0;sprint=jump=down=false;}
        boolean edge=jump&&!priorJump;priorJump=jump;double actual=actualSpeed();
        if(onGround()&&sprint&&forward>.5&&actual>=9){if(readyTicks++==0)cue("gallop_burst",.4F);}else if(!flying())readyTicks=0;
        if(!flying()&&edge&&onGround()){
            if(forward>.5&&SivkaRules.ready(actual,readyTicks,true,sprint)&&!isInWater()&&headClear(1.8)){setMode(1);vertical=.24;}
            else {setDeltaMovement(getDeltaMovement().x,SivkaRules.GROUND_JUMP,getDeltaMovement().z);}
        }
        float turn=Mth.clamp(Mth.wrapDegrees(wantedYaw-getYRot()),flying()?-6F:-12F,flying()?6F:12F);if(mounted){setYRot(getYRot()+turn);yBodyRot=getYRot();yHeadRot=getYRot();}
        double target=flying()?(forward>0?SivkaRules.AIR_SPEED:forward<0?6:0):(forward>0?(sprint?12:6):forward<0?2:0);
        speed=SivkaRules.approach(speed,target,(flying()?(target>speed?SivkaRules.AIR_ACCELERATION:SivkaRules.AIR_BRAKING):target>speed?4:5.5)/20);
        if(flying()&&speed<=3.1)slowTicks++;else slowTicks=0;
        double rad=Math.toRadians(getYRot()),vx=-Math.sin(rad)*speed/20,vz=Math.cos(rad)*speed/20;if(forward<0){vx=-vx;vz=-vz;}
        if(mounted&&Math.abs(strafe)>0){double side=(flying()?6:3)*strafe/20;vx+=Math.cos(rad)*side;vz+=Math.sin(rad)*side;}
        double vy;
        if(flying()){
            if(mode()==1&&modeTicks>=16)setMode(2);
            double desired=mode()==1?.24:down||!mounted?-.18:jump?.18:0;vertical=SivkaRules.approach(vertical,desired,.025);
            double nextPhase=(phase+1/14.4)%1;vy=vertical+SivkaRules.physicalWave(nextPhase)-SivkaRules.physicalWave(phase);
            if(getY()>level().getMaxBuildHeight()-5&&vy>0)vy=0;
            if(horizontalCollision){vx*=.3;vz*=.3;vy=-.08;}
            if(vy<0&&!SivkaWhistle.safeFloor(level(),blockPosition().below())&&!level().getFluidState(blockPosition().below()).isEmpty()){vy=.05;}
        }else vy=getDeltaMovement().y-.08;
        var destination=BlockPos.containing(getX()+vx,getY()+vy,getZ()+vz);if(!level().hasChunkAt(destination)){vx=vz=0;vy=Math.max(0,vy);}
        if(getY()<level().getMinBuildHeight()+10&&vy<0&&flying())vy=.05;
        // The long head/mane overhang needs extra clearance beyond the square native hitbox.
        if((vx!=0||vz!=0)&&!level().noCollision(this,getBoundingBox().inflate(.35,0,.65).move(vx,Math.max(0,vy),vz))){vx=vz=0;if(flying())vy=Math.min(vy,0);}
        var motion=new Vec3(vx,vy,vz);setDeltaMovement(motion);move(MoverType.SELF,motion);hasImpulse=true;
        if(verticalCollision){setDeltaMovement(vx,0,vz);if(flying()&&onGround()&&SivkaWhistle.safeFloor(level(),blockPosition().below())){setMode(3);vertical=0;cue("magic_land",.65F);spark(6);}}
        if(mode()==3&&modeTicks>=20)setMode(0);
        if(flying())fallDistance=0;entityData.set(BANK,bank()+(-turn*1.8F-bank())*.2F);entityData.set(LIFT,(float)vertical);
    }
    @Override public void tick(){if(!level().isClientSide){boolean ai=!isVehicle()&&!flying();for(var flag:new Goal.Flag[]{Goal.Flag.MOVE,Goal.Flag.LOOK,Goal.Flag.JUMP})goalSelector.setControlFlag(flag,ai);}super.tick();if(level().isClientSide)return;
        modeTicks++;if(scared>0)scared--;if(reaction()!=0&&tickCount>=reactionEnd)entityData.set(REACTION,0);
        if(owner!=null&&level() instanceof ServerLevel w){var d=SivkaBonds.get(w);var b=d.bonds.get(owner);if(b==null||!b.id.equals(bond)||b.generation!=generation||!getUUID().equals(b.entity)||!b.valid()){ejectPassengers();discard();return;}
            if(tickCount%20==0){b.health=getHealth();b.last=blockPosition();d.setDirty();}
            if(wasMounted&&!isVehicle()){idleTicks=0;react(5,7);}wasMounted=isVehicle();
            if(!isVehicle()&&w.getServer().getPlayerList().getPlayer(owner)!=null)idleTicks++;else {idleTicks=departTicks=0;navigation.stop();}
            if(idleTicks>=SivkaRules.DEPART_DELAY&&!isVehicle()){
                if(departTicks++==0){react(6,48);cue("depart",.5F);var away=net.minecraft.world.entity.ai.util.DefaultRandomPos.getPos(this,8,2);if(away!=null)navigation.moveTo(away.x,away.y,away.z,1.4);}
                if(departTicks==32)spark(12);if(departTicks>=48){b.entity=null;d.setDirty();discard();return;}
            }
            if(arrivalTicks>0){arrivalTicks--;if(tickCount%20==0){var p=w.getServer().getPlayerList().getPlayer(owner);if(p!=null&&p.level()==level()&&!isVehicle())navigation.moveTo(p,1.15);}}
        }
        if(!isVehicle()&&!flying()&&tickCount%20==0){var p=level().getNearestPlayer(this,16);if(p!=null){var b=owner==null?null:SivkaBonds.get((ServerLevel)level()).bonds.get(owner);int stage=b==null?0:b.stage;var food=net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(SivkaRules.FOODS[Math.min(stage,6)]));if((p.getMainHandItem().is(food)||p.getOffhandItem().is(food))&&scared==0)navigation.moveTo(p,.8);else if(owner==null&&distanceToSqr(p)<9&&random.nextInt(3)==0){var away=net.minecraft.world.entity.ai.util.DefaultRandomPos.getPosAway(this,5,2,p.position());if(away!=null)navigation.moveTo(away.x,away.y,away.z,.8);}}}
        double actual=Math.sqrt((getX()-xo)*(getX()-xo)+(getZ()-zo)*(getZ()-zo))*20;entityData.set(SPEED,(float)actual);
        double old=phase,cycle=flying()?14.4:actual>6.5?12.6:actual>3.2?15.5:23;phase=(phase+(flying()?1:Math.min(1,actual/3))/cycle)%1;entityData.set(PHASE,(float)phase);
        if(flying()){for(double mark:new double[]{.12,.70})if(crossed(old,phase,mark)){cue("air_stride",.36F);spark(2);}}
        else if(actual>.3&&onGround()){double[] marks=actual>6.5?new double[]{0,.13,.47,.65}:actual>3.2?new double[]{0,.5}:new double[]{0,.25,.5,.75};for(double m:marks)if(crossed(old,phase,m))hoof();}
        if(mode()==1&&modeTicks==3)hoof();if(mode()==1&&modeTicks==5){cue("magic_takeoff",.65F);spark(16);}if(mode()==3&&modeTicks==3)hoof();
        if(tickCount>=nextAmbient){nextAmbient=tickCount+300+random.nextInt(401);cue(random.nextInt(4)==0?"whinny_soft":"breath",.4F);}
        if(owner==null&&actual<.1&&tickCount%180==0)react(7,45);
    }
    public static boolean crossed(double old,double next,double marker){return next>=old?marker>old&&marker<=next:marker>old||marker<=next;}
    private void hoof(){var s=level().getBlockState(blockPosition().below());String cue=s.is(net.minecraft.tags.BlockTags.PLANKS)||s.is(net.minecraft.tags.BlockTags.LOGS)?"hoof_wood":s.is(net.minecraft.tags.BlockTags.SNOW)?"hoof_snow":s.is(net.minecraft.tags.BlockTags.DIRT)||s.is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK)?"hoof_grass":"hoof_stone";cue(cue,.35F);}
    @Override protected void playStepSound(BlockPos p,net.minecraft.world.level.block.state.BlockState s){}
    @Override public boolean hurt(DamageSource source,float amount){if(!level().isClientSide){scared=400;react(8,10);cue("whinny_alarm",.8F);
        if(owner!=null&&amount>=getHealth()&&!source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)&&level() instanceof ServerLevel w){var d=SivkaBonds.get(w);var b=d.bonds.get(owner);if(b!=null){b.recovery=b.play+6000;b.health=80;b.entity=null;b.generation++;d.setDirty();ejectPassengers();spark(8);cue("depart",.5F);discard();return true;}}
    }return super.hurt(source,amount);}
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source){return flying()||mode()==3?false:super.causeFallDamage(distance,multiplier,source);}
    @Override protected boolean shouldDropLoot(){return false;}
    @Override public void die(DamageSource source){super.die(source);if(owner!=null&&level() instanceof ServerLevel w){var d=SivkaBonds.get(w);var b=d.bonds.get(owner);if(b!=null&&getUUID().equals(b.entity)){b.entity=null;b.recovery=b.play+6000;b.health=80;d.setDirty();}}}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("SivkaOwner",owner);if(bond!=null)n.putUUID("SivkaBond",bond);n.putLong("SivkaGeneration",generation);n.putInt("Scared",scared);n.putBoolean("SivkaAirborne",flying());}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("SivkaOwner")?n.getUUID("SivkaOwner"):null;bond=n.hasUUID("SivkaBond")?n.getUUID("SivkaBond"):null;generation=n.getLong("SivkaGeneration");scared=n.getInt("Scared");setMode(n.getBoolean("SivkaAirborne")?2:0);speed=n.getBoolean("SivkaAirborne")?3:0;inputTime=-100;}
}
