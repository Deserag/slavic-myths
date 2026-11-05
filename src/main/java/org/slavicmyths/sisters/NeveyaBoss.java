package org.slavicmyths.sisters;

import java.util.EnumSet;
import java.util.HashMap;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.combat.CreatureHealthBalance;
import org.slavicmyths.registry.ModEffects;

/** 2.5.2 Neveya — two phases, grounded and flight attacks, no natural spawn or rewards. */
public final class NeveyaBoss extends Monster {
    public static final int IDLE=0, COMBO=1, SWEEP=2, MARK=3, HARVEST=4, BOLT=5, CIRCLES=6, DASH=7, DIVE=8, VERDICT=9;
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(NeveyaBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FRAME=SynchedEntityData.defineId(NeveyaBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PHASE_TWO=SynchedEntityData.defineId(NeveyaBoss.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FLYING=SynchedEntityData.defineId(NeveyaBoss.class,EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bar=new ServerBossEvent(Component.literal("Невея"),BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.NOTCHED_10);
    private final Map<UUID,Integer> victims=new HashMap<>();
    private int cooldown=36,sequence,verdictCooldown=480;
    private Vec3 markedCenter=Vec3.ZERO;
    private boolean transition;
    public NeveyaBoss(EntityType<? extends NeveyaBoss> type,Level level) {
        super(type,level);
        setPersistenceRequired();
        setCustomName(Component.literal("Невея"));
        setCustomNameVisible(true);
        setHealth(getMaxHealth());
        xpReward=0;
    }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,2400)
            .add(Attributes.ATTACK_DAMAGE,26).add(Attributes.MOVEMENT_SPEED,.28)
            .add(Attributes.ARMOR,14).add(Attributes.FOLLOW_RANGE,48)
            .add(Attributes.KNOCKBACK_RESISTANCE,1.0);
    }
    @Override public float getMaxHealth() { return (float)CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH)); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);b.define(ACTION,IDLE);b.define(FRAME,0);b.define(PHASE_TWO,false);b.define(FLYING,false);
    }
    public int action(){return entityData.get(ACTION);}
    public int actionFrame(){return entityData.get(FRAME);}
    public boolean phaseTwo(){return entityData.get(PHASE_TWO);}
    public boolean flying(){return entityData.get(FLYING);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new Pursue());
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,24));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    private boolean targetable(Player p){return p.isAlive()&&!p.isCreative()&&!p.isSpectator();}
    private void start(int kind,Player target){
        victims.clear();markedCenter=target.position();
        entityData.set(ACTION,kind);entityData.set(FRAME,0);
        boolean air=kind==DASH||kind==DIVE;entityData.set(FLYING,air);setNoGravity(air);
        getNavigation().stop();setDeltaMovement(Vec3.ZERO);
        playSound(SoundEvents.WITHER_AMBIENT,.9F,1.28F);
    }
    private void finish(){
        entityData.set(ACTION,IDLE);entityData.set(FRAME,0);
        entityData.set(FLYING,false);setNoGravity(false);setDeltaMovement(Vec3.ZERO);
        cooldown=phaseTwo()?18:28;
        victims.clear();
    }
    private int duration(int act){return switch(act){
        case COMBO->49;case SWEEP->44;case MARK->37;case HARVEST->50;
        case BOLT->36;case CIRCLES->65;case DASH->55;case DIVE->65;case VERDICT->113;
        default->0;};}
    private float hit(Player p,float amount,int maxHits){
        if(!targetable(p)||!getSensing().hasLineOfSight(p))return 0;
        if(victims.getOrDefault(p.getUUID(),0)>=maxHits)return 0;
        float before=p.getHealth();
        if(!p.hurt(damageSources().mobAttack(this),amount))return 0;
        victims.merge(p.getUUID(),1,Integer::sum);
        return Math.max(0,before-p.getHealth());
    }
    private boolean withinArc(Player p,double radius){
        Vec3 dir=p.position().subtract(position()).multiply(1,0,1);
        Vec3 forward=Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();
        return dir.lengthSqr()>0.01&&dir.lengthSqr()<=radius*radius&&dir.normalize().dot(forward)>.08;
    }
    private void arc(ServerLevel world,float amount,double radius,int maxHits,boolean wither,boolean heal){
        for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius,3,radius),this::targetable)){
            if(!withinArc(p,radius))continue;
            float real=hit(p,amount,maxHits);
            if(real>0&&wither)p.addEffect(new MobEffectInstance(MobEffects.WITHER,160,1));
            if(real>0&&heal)setHealth(Math.min(getMaxHealth(),getHealth()+real*.20F));
        }
    }
    private void circle(ServerLevel world,Vec3 center,double radius,float damage,int maxHits){
        for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(22,7,22),this::targetable)){
            if(p.position().multiply(1,0,1).distanceToSqr(center.multiply(1,0,1))<=radius*radius&&Math.abs(p.getY()-center.y)<3.5)
                hit(p,damage,maxHits);
        }
    }
    private Vec3 minorCircle(int wave,int index){
        double a=(Math.PI*2*index/9.0)+(wave*.55);
        double r=3.0+(index%3)*4.35;
        return markedCenter.add(Math.cos(a)*r,0,Math.sin(a)*r);
    }
    private void telegraph(ServerLevel world,Vec3 center,double radius){
        for(int i=0;i<20;i++){
            double a=i*Math.PI/10.0;
            world.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,center.x+radius*Math.cos(a),center.y+.16,center.z+radius*Math.sin(a),1,.04,.02,.04,.002);
        }
    }
    private void floatToward(Player target,int frame,boolean dive){
        if(frame<16){setDeltaMovement(0,.17,0);return;}
        if(frame<32){
            Vec3 aim=target.position().add(0,dive?5.5:1.4,0).subtract(position());
            if(aim.lengthSqr()>.05){
                Vec3 motion=aim.normalize().scale(dive?.42:.53);
                if(level().noCollision(this,getBoundingBox().move(motion)))setDeltaMovement(motion);
                else setDeltaMovement(Vec3.ZERO);
            }
            return;
        }
        if(dive&&frame<43)setDeltaMovement(0,-.52,0);
        else setDeltaMovement(Vec3.ZERO);
    }
    private void actionStep(ServerLevel world,Player target){
        int f=actionFrame()+1,act=action();entityData.set(FRAME,f);
        if((act==DASH||act==DIVE)&&target!=null)floatToward(target,f,act==DIVE);
        if(f%6==0&&f<duration(act)-8){
            world.sendParticles(ParticleTypes.SOUL,getX(),getY()+2.4,getZ(),5,.35,.35,.35,.025);
        }
        switch(act){
            case COMBO->{if(f==16||f==29||f==40)arc(world,f==40?26F:16.9F,4.5,3,false,false);}
            case SWEEP->{if(f==29)arc(world,18.2F,5.8,1,false,false);}
            case MARK->{if(f==19)telegraph(world,markedCenter,2.8);if(f==29){
                for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(17,5,17),this::targetable))
                    if(p.distanceToSqr(markedCenter)<25&&getSensing().hasLineOfSight(p))
                        p.addEffect(new MobEffectInstance(ModEffects.DEATH_MARK,600,0,false,true,true));
            }}
            case HARVEST->{if(f==30)arc(world,26F,5.1,1,true,true);}
            case BOLT->{if(f==20&&target!=null&&getSensing().hasLineOfSight(target)){
                NeveyaBolt bolt=new NeveyaBolt(SistersContent.NEVEYA_BOLT.get(),world);
                Vec3 origin=position().add(getLookAngle().scale(1.8)).add(0,2.8,0);
                bolt.setPos(origin.x,origin.y,origin.z);bolt.configure(this,20.8F);
                Vec3 direction=target.getEyePosition().subtract(origin).normalize();
                bolt.shoot(direction.x,direction.y,direction.z,1.35F,0);
                world.addFreshEntity(bolt);
            }}
            case CIRCLES->{if(f==15||f==25||f==35)telegraph(world,markedCenter.add(f==25?3:f==35?-3:0,0,f==25?2:f==35?-2:0),2.9);
                if(f==29||f==39||f==49)circle(world,markedCenter.add(f==39?3:f==49?-3:0,0,f==39?2:f==49?-2:0),2.9,18.2F,2);}
            case DASH->{if(f==32||f==36)arc(world,29.9F,4.8,1,false,false);}
            case DIVE->{if(f==43)circle(world,position(),5.0,35.1F,1);}
            case VERDICT->{
                if(f==20)telegraph(world,markedCenter,16.0);
                if(f>=26&&f<=74&&f%4==2){
                    int wave=(f-26)/16;int index=((f-26)%16)/4;
                    if(wave<3&&index<4)telegraph(world,minorCircle(wave,index+wave*2),2.25);
                }
                if(f==50||f==66||f==82){
                    int wave=(f-50)/16;
                    for(int i=0;i<9;i++)circle(world,minorCircle(wave,i),2.2,18.2F,2);
                }
                if(f==95){telegraph(world,markedCenter,4.4);circle(world,markedCenter,4.4,39F,3);}
            }
            default->{}
        }
        if(f>=duration(act))finish();
    }
    @Override protected void customServerAiStep(){
        super.customServerAiStep();
        if(!(level() instanceof ServerLevel world)||!isAlive())return;
        bar.setProgress(Math.clamp(getHealth()/Math.max(1,getMaxHealth()),0,1));
        if(!phaseTwo()&&getHealth()<=getMaxHealth()*.5F){
            entityData.set(PHASE_TWO,true);transition=true;
            finish();cooldown=55;playSound(SoundEvents.WITHER_SPAWN,1.2F,.9F);
        }
        if(verdictCooldown>0)verdictCooldown--;
        Player p=getTarget() instanceof Player target&&targetable(target)?target:null;
        if(p==null){if(action()!=IDLE)finish();return;}
        if(action()!=IDLE){getNavigation().stop();actionStep(world,p);return;}
        getLookControl().setLookAt(p,30,25);
        if(transition){transition=false;return;}
        if(cooldown>0){cooldown--;return;}
        double distance=distanceToSqr(p);
        if(phaseTwo()&&verdictCooldown==0&&distance<650){
            start(VERDICT,p);verdictCooldown=600;return;
        }
        if(distance>250){start(BOLT,p);return;}
        if(distance>64){start(sequence++%2==0?DASH:BOLT,p);return;}
        int choice=sequence++%(phaseTwo()?7:5);
        int kind=phaseTwo()?switch(choice){
            case 0->COMBO;case 1->MARK;case 2->HARVEST;case 3->CIRCLES;
            case 4->DIVE;case 5->SWEEP;default->BOLT;
        }:switch(choice){
            case 0->COMBO;case 1->MARK;case 2->SWEEP;case 3->BOLT;default->DASH;
        };
        start(kind,p);
    }
    @Override public void knockback(double strength,double x,double z){
        if(action()==IDLE)super.knockback(strength,x,z);
    }
    private final class Pursue extends Goal{
        Pursue(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){return getTarget()!=null&&getTarget().isAlive();}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public void tick(){
            LivingEntity p=getTarget();if(p==null)return;
            if(action()!=IDLE){getNavigation().stop();return;}
            if(distanceToSqr(p)>16)getNavigation().moveTo(p,phaseTwo()?1.24:1.02);
        }
    }
}
