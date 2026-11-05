package org.slavicmyths.sisters;

import java.util.ArrayDeque;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.combat.CreatureHealthBalance;
import org.slavicmyths.registry.ModEffects;

/** Four-block Gogol Viy. Gaze is checked on server against the EYE target, not his whole hitbox. */
public final class ViyBoss extends Monster {
    public static final int IDLE=0,FIST=1,STOMP=2,RIFT=3,IRON_HAND=4,OPEN_EYES=5,SUMMON=6,FINGER=7,EYE_WAVE=8,VERDICT=9;
    public static final int GAZE_MEMORY=3600,LOOK_TICKS=40,GLANCE_COUNT=10,GLANCE_MIN_TICKS=3;
    private static final EntityDataAccessor<Integer> MOVE=SynchedEntityData.defineId(ViyBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FRAME=SynchedEntityData.defineId(ViyBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PHASE_TWO=SynchedEntityData.defineId(ViyBoss.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> EYES_OPEN=SynchedEntityData.defineId(ViyBoss.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> EYE_PROGRESS=SynchedEntityData.defineId(ViyBoss.class,EntityDataSerializers.INT);
    private final ServerBossEvent bossBar=new ServerBossEvent(Component.literal("Вий"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.NOTCHED_10);
    private final Map<UUID,GazeMeter> meters=new HashMap<>();
    private UUID heldPlayer;
    private int cooldown=48,attackIndex,verdictCooldown=520,summonCooldown=120;
    private Vec3 lockedTarget=Vec3.ZERO;
    private boolean changedPhase;
    private static final class GazeMeter {
        int on,off;
        long lastContact;
        final ArrayDeque<Long> glances=new ArrayDeque<>();
    }
    public ViyBoss(EntityType<? extends ViyBoss> type,Level level){
        super(type,level);
        setPersistenceRequired();
        setCustomName(Component.literal("Вий"));
        setCustomNameVisible(true);
        setHealth(getMaxHealth());
        xpReward=0;
    }
    public static AttributeSupplier.Builder attributes(){
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,2800)
            .add(Attributes.ATTACK_DAMAGE,30).add(Attributes.MOVEMENT_SPEED,.18)
            .add(Attributes.ARMOR,19).add(Attributes.KNOCKBACK_RESISTANCE,1.0)
            .add(Attributes.FOLLOW_RANGE,48);
    }
    @Override public float getMaxHealth(){return (float)CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){
        super.defineSynchedData(b);b.define(MOVE,IDLE);b.define(FRAME,0);b.define(PHASE_TWO,false);b.define(EYES_OPEN,false);b.define(EYE_PROGRESS,0);
    }
    public int action(){return entityData.get(MOVE);}
    public int actionFrame(){return entityData.get(FRAME);}
    public boolean phaseTwo(){return entityData.get(PHASE_TWO);}
    public boolean eyesOpen(){return entityData.get(EYE_PROGRESS)>=22;}
    public float eyelidProgress(){return entityData.get(EYE_PROGRESS)/24.0F;}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bossBar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bossBar.removePlayer(p);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new Pursuit());
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,28));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    private boolean valid(Player p){return p.isAlive()&&!p.isCreative()&&!p.isSpectator();}
    private Vec3 faceCenter(){
        Vec3 forward=Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();
        return position().add(0,3.14,0).add(forward.scale(1.05));
    }
    private boolean seesEye(ServerLevel world,ServerPlayer p){
        if(!eyesOpen()||!valid(p))return false;
        Vec3 start=p.getEyePosition(),end=faceCenter(),delta=end.subtract(start);
        double distance=delta.length();
        if(distance<1.5||distance>24)return false;
        if(p.getViewVector(1.0F).normalize().dot(delta.scale(1.0/distance))<1.0-0.30/distance)return false;
        return world.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS;
    }
    private void curse(ServerPlayer p){
        var previous=p.getEffect(ModEffects.VIY_GAZE);
        int oldStage=previous==null?0:previous.getAmplifier()+1,stage=Math.min(5,oldStage+1);
        p.addEffect(new MobEffectInstance(ModEffects.VIY_GAZE,GAZE_MEMORY,stage-1,false,true,true));
        if(stage==1)p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,600,1));
        if(stage==2){
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,600,2));
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,600,1));
            p.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,600,1));
        }
        if(stage==3){
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,600,3));
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,600,2));
            p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,160,0));
            p.addEffect(new MobEffectInstance(MobEffects.WITHER,200,1));
        }
        if(stage==4){
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,600,4));
            p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,600,3));
            p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,240,0));
            p.addEffect(new MobEffectInstance(MobEffects.WITHER,200,2));
        }
        if(stage==5&&oldStage<5){
            p.addEffect(new MobEffectInstance(MobEffects.WITHER,200,2));
            p.getPersistentData().putLong("SlavicViyPetrifiedUntil",p.level().getGameTime()+60);
            p.hurt(damageSources().magic(),16F);
        }
        playSound(SoundEvents.ELDER_GUARDIAN_CURSE,.9F,.9F+stage*.12F);
    }
    private void processLooks(ServerLevel world){
        long now=world.getGameTime();
        for(ServerPlayer p:world.getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(25,7,25),this::valid)){
            GazeMeter meter=meters.computeIfAbsent(p.getUUID(),id->new GazeMeter());
            while(!meter.glances.isEmpty()&&now-meter.glances.peekFirst()>GAZE_MEMORY)meter.glances.removeFirst();
            if(heldPlayer!=null&&heldPlayer.equals(p.getUUID()))continue;
            boolean looking=seesEye(world,p);
            if(looking){
                meter.lastContact=now;meter.off=0;meter.on++;
                if(meter.on>=LOOK_TICKS){curse(p);meter.on=0;meter.glances.clear();}
            }else{
                meter.off++;
                if(meter.off==3){
                    if(meter.on>=GLANCE_MIN_TICKS&&meter.on<LOOK_TICKS)meter.glances.addLast(now);
                    meter.on=0;
                    if(meter.glances.size()>=GLANCE_COUNT){curse(p);meter.glances.clear();}
                }
            }
        }
        if(now%100==0)meters.entrySet().removeIf(e->now-e.getValue().lastContact>GAZE_MEMORY);
    }
    private void start(int move,Player target){
        entityData.set(MOVE,move);entityData.set(FRAME,0);
        lockedTarget=target.position();getNavigation().stop();
        playSound(SoundEvents.IRON_GOLEM_ATTACK,.9F,.65F);
    }
    private void finish(){
        if(heldPlayer!=null)heldPlayer=null;
        entityData.set(MOVE,IDLE);entityData.set(FRAME,0);
        cooldown=phaseTwo()?21:34;
    }
    private int duration(int move){return switch(move){
        case FIST->40;case STOMP->54;case RIFT->60;case IRON_HAND->90;
        case OPEN_EYES->42;case SUMMON->70;case FINGER->42;case EYE_WAVE->65;case VERDICT->120;
        default->0;};}
    private boolean facing(Player p,double reach){
        Vec3 d=p.position().subtract(position()).multiply(1,0,1);
        Vec3 f=Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();
        return d.lengthSqr()<=reach*reach&&d.lengthSqr()>.001&&d.normalize().dot(f)>.1;
    }
    private void sweep(ServerLevel world,float amount,double radius){
        for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius,2.8,radius),this::valid))
            if(facing(p,radius))p.hurt(damageSources().mobAttack(this),amount);
    }
    private void area(ServerLevel world,Vec3 center,float radius,float amount){
        for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(21,5,21),this::valid))
            if(Math.abs(p.getY()-center.y)<3&&p.position().multiply(1,0,1).distanceToSqr(center.multiply(1,0,1))<=radius*radius)
                p.hurt(damageSources().mobAttack(this),amount);
    }
    private void warning(ServerLevel world,Vec3 center,double radius){
        for(int j=0;j<24;j++){
            double a=j*Math.PI/12;
            world.sendParticles(ParticleTypes.ASH,center.x+Math.cos(a)*radius,center.y+.15,center.z+Math.sin(a)*radius,1,.1,.01,.1,.01);
        }
    }
    private void reachAndHold(ServerLevel world){
        if(heldPlayer!=null)return;
        for(ServerPlayer p:world.getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(5,3,5),this::valid)){
            if(!facing(p,5)||!getSensing().hasLineOfSight(p))continue;
            heldPlayer=p.getUUID();
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,76,6,false,false,false));
            break;
        }
    }
    private ServerPlayer captive(ServerLevel world){
        if(heldPlayer==null)return null;
        ServerPlayer p=world.getServer().getPlayerList().getPlayer(heldPlayer);
        return p!=null&&p.level()==world&&valid(p)?p:null;
    }
    private void maintainHold(ServerLevel world,int frame){
        ServerPlayer p=captive(world);
        if(p==null){heldPlayer=null;return;}
        Vec3 forward=Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();
        Vec3 place=position().add(forward.scale(3.2)).add(0,.2,0);
        Vec3 toEye=faceCenter().subtract(place.add(0,p.getEyeHeight(),0));
        float yaw=(float)(-Math.toDegrees(Math.atan2(toEye.x,toEye.z)));
        float pitch=(float)(-Math.toDegrees(Math.atan2(toEye.y,Math.sqrt(toEye.x*toEye.x+toEye.z*toEye.z))));
        if(frame%5==0)p.connection.teleport(place.x,place.y,place.z,yaw,pitch);
        p.setDeltaMovement(Vec3.ZERO);
        if(frame==64)curse(p);
        if(frame==76){
            p.hurt(damageSources().mobAttack(this),14F);
            p.push(forward.x*.85,.28,forward.z*.85);
            heldPlayer=null;
        }
    }
    private void summon(ServerLevel world,Player target){
        int alive=world.getEntitiesOfClass(ViyServant.class,getBoundingBox().inflate(64,20,64),
            s->s.isAlive()&&getUUID().equals(s.ownerId())).size();
        if(alive>=4)return;
        EntityType<? extends ViyServant>[] types=new EntityType[]{
            org.slavicmyths.viy.ViyContent.VIY_UPYR.get(),org.slavicmyths.viy.ViyContent.VIY_NAV.get(),
            org.slavicmyths.viy.ViyContent.VIY_KORNELAP.get(),org.slavicmyths.viy.ViyContent.VIY_KORNELAP.get()};
        for(int i=0;i<Math.min(4-alive,types.length);i++){
            ViyServant servant=types[i].create(world);
            if(servant==null)continue;
            double a=i*1.57+getYRot()*Math.PI/180;
            Vec3 point=position().add(Math.cos(a)*4.6,0,Math.sin(a)*4.6);
            servant.setPos(point.x,getY(),point.z);
            if(!world.noCollision(servant))continue;
            servant.link(this,target);
            world.addFreshEntity(servant);
            world.sendParticles(ParticleTypes.SOUL,point.x,point.y+.6,point.z,12,.6,.4,.6,.03);
        }
    }
    private void finger(ServerLevel world,Player target){
        if(!valid(target))return;
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING,120,0));
        for(ViyServant s:world.getEntitiesOfClass(ViyServant.class,getBoundingBox().inflate(64),v->getUUID().equals(v.ownerId())))
            s.setTarget(target);
    }
    private void stepAction(ServerLevel world,Player target){
        int frame=actionFrame()+1,move=action();entityData.set(FRAME,frame);
        if(frame%8==0)world.sendParticles(ParticleTypes.ASH,getX(),getY()+1.4,getZ(),5,1.0,.7,1.0,.02);
        switch(move){
            case FIST->{if(frame==22)sweep(world,30,5);}
            case STOMP->{if(frame==21)warning(world,position(),7);if(frame==37)area(world,position(),7,24);}
            case RIFT->{
                if(frame==15||frame==25)warning(world,lockedTarget.add(0,0,0),3.0);
                if(frame==36||frame==45)area(world,lockedTarget,3.0,26);
            }
            case IRON_HAND->{
                if(frame==24)reachAndHold(world);
                if(frame>=25&&frame<=76&&heldPlayer!=null)maintainHold(world,frame);
            }
            case OPEN_EYES->{if(frame==20)entityData.set(EYES_OPEN,true);}
            case SUMMON->{if(frame==33)summon(world,target);}
            case FINGER->{if(frame==27)finger(world,target);}
            case EYE_WAVE->{
                if(frame==18)world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,faceCenter().x,faceCenter().y,faceCenter().z,20,.7,.6,.7,.05);
                if(frame==43){
                    sweep(world,25,20);
                    for(ServerPlayer p:world.getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(24),this::valid))
                        if(facing(p,20)&&seesEye(world,p))curse(p);
                }
            }
            case VERDICT->{
                if(frame==25||frame==40||frame==55){
                    int wave=(frame-25)/15;
                    for(int i=0;i<6;i++){
                        double a=i*Math.PI/3+wave*.38;
                        Vec3 center=lockedTarget.add(Math.cos(a)*(3+wave*2),0,Math.sin(a)*(3+wave*2));
                        warning(world,center,2.7);
                    }
                }
                if(frame==54||frame==69||frame==84){
                    int wave=(frame-54)/15;
                    for(int i=0;i<6;i++){
                        double a=i*Math.PI/3+wave*.38;
                        Vec3 center=lockedTarget.add(Math.cos(a)*(3+wave*2),0,Math.sin(a)*(3+wave*2));
                        area(world,center,2.7,29);
                    }
                }
                if(frame==106)area(world,lockedTarget,5,43);
            }
            default->{}
        }
        if(frame>=duration(move))finish();
    }
    @Override protected void customServerAiStep(){
        super.customServerAiStep();
        if(!(level() instanceof ServerLevel world)||!isAlive())return;
        bossBar.setProgress(Math.clamp(getHealth()/Math.max(1,getMaxHealth()),0,1));
        if(!phaseTwo()&&getHealth()<=getMaxHealth()*.5F){
            entityData.set(PHASE_TWO,true);changedPhase=true;
            playSound(SoundEvents.WITHER_SPAWN,1.25F,.5F);
        }
        boolean open=phaseTwo()||action()==IRON_HAND||(tickCount%500>=250&&tickCount%500<430);
        entityData.set(EYES_OPEN,open);
        entityData.set(EYE_PROGRESS,Math.clamp(entityData.get(EYE_PROGRESS)+(open?1:-1),0,24));
        processLooks(world);
        if(verdictCooldown>0)verdictCooldown--;
        if(summonCooldown>0)summonCooldown--;
        Player target=getTarget() instanceof Player p&&valid(p)?p:null;
        if(target==null){if(action()!=IDLE)finish();return;}
        getLookControl().setLookAt(target,28,20);
        if(action()!=IDLE){getNavigation().stop();stepAction(world,target);return;}
        if(changedPhase){changedPhase=false;cooldown=45;return;}
        if(cooldown-->0)return;
        double range=distanceToSqr(target);
        if(phaseTwo()&&verdictCooldown<=0&&range<576){start(VERDICT,target);verdictCooldown=760;return;}
        if(phaseTwo()&&summonCooldown<=0&&range<576){start(SUMMON,target);summonCooldown=900;return;}
        if(phaseTwo()&&range>100){start(attackIndex++%2==0?FINGER:EYE_WAVE,target);return;}
        if(range>49){start(phaseTwo()?RIFT:STOMP,target);return;}
        int option=attackIndex++%(phaseTwo()?7:4);
        int attack=phaseTwo()?switch(option){
            case 0->FIST;case 1->STOMP;case 2->IRON_HAND;case 3->RIFT;
            case 4->EYE_WAVE;case 5->FINGER;default->IRON_HAND;
        }:switch(option){case 0->FIST;case 1->STOMP;case 2->IRON_HAND;default->RIFT;};
        start(attack,target);
    }
    @Override public void knockback(double strength,double x,double z){if(action()==IDLE)super.knockback(strength,x,z);}
    private final class Pursuit extends Goal {
        Pursuit(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){return getTarget()!=null&&getTarget().isAlive();}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){
            LivingEntity t=getTarget();
            if(t==null||action()!=IDLE){getNavigation().stop();return;}
            if(distanceToSqr(t)>22)getNavigation().moveTo(t,phaseTwo()?1.15:1.0);
        }
    }
}
