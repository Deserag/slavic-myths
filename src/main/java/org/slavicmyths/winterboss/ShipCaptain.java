package org.slavicmyths.winterboss;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.combat.CreatureBossBar;
import org.slavicmyths.registry.ModEffects;
import org.slavicmyths.registry.ModSounds;
import org.slavicmyths.wolf.WolfBleeding;

public final class ShipCaptain extends Monster {
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(ShipCaptain.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FRAME=SynchedEntityData.defineId(ShipCaptain.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> AXES=SynchedEntityData.defineId(ShipCaptain.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AIM=SynchedEntityData.defineId(ShipCaptain.class,EntityDataSerializers.FLOAT);
    private final ShipCaptainCombat combat=new ShipCaptainCombat();
    private final java.util.Random selection=new java.util.Random();
    private final UUID[] flying=new UUID[2];
    private final int[] timeout=new int[2];
    private final Set<UUID> dashHits=new HashSet<>();
    private final ServerBossEvent bar;
    private Vec3 direction=new Vec3(0,0,1);
    private boolean hadTarget,dashing;
    private int spinPulse;
    public ShipCaptain(EntityType<? extends ShipCaptain> type,Level level){
        super(type,level);xpReward=200;
        bar=new CreatureBossBar(this,Component.literal("Капитан корабля"),BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
    }
    public static AttributeSupplier.Builder attributes(){return createMonsterAttributes().add(Attributes.MAX_HEALTH,900).add(Attributes.ARMOR,14).add(Attributes.ATTACK_DAMAGE,12).add(Attributes.MOVEMENT_SPEED,.27).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.75);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,0);b.define(FRAME,0);b.define(AXES,3);b.define(AIM,0F);}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.75));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,12));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    public int action(){return entityData.get(ACTION);}
    public int actionFrame(){return entityData.get(FRAME);}
    public int axes(){return entityData.get(AXES);}
    public float attackYaw(){return entityData.get(AIM);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public void startSeenByPlayer(ServerPlayer player){super.startSeenByPlayer(player);bar.addPlayer(player);}
    @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);bar.removePlayer(player);}
    public boolean eligible(LivingEntity entity){return entity instanceof ServerPlayer p&&p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&!isAlliedTo(p)&&!p.isInvulnerable();}
    private void cue(String key){if(key!=null)playSound(ModSounds.boss(key.startsWith("ship_captain.")?key:"ship_captain."+key),1.15F,1F);}
    @Override protected SoundEvent getAmbientSound(){return ModSounds.boss("ship_captain.ambient");}
    @Override protected SoundEvent getHurtSound(DamageSource source){return ModSounds.boss("ship_captain.hurt");}
    @Override protected SoundEvent getDeathSound(){return ModSounds.boss("ship_captain.death");}
    @Override public boolean canFreeze(){return false;}
    @Override protected void playStepSound(BlockPos pos,net.minecraft.world.level.block.state.BlockState state){playSound(ModSounds.boss("ship_captain.step"),.65F,.9F);}
    @Override protected void customServerAiStep(){
        super.customServerAiStep();
        if(!(level() instanceof ServerLevel world)||!isAlive())return;
        bar.setProgress(getHealth()/getMaxHealth());
        for(int s=0;s<2;s++)if(flying[s]!=null&&--timeout[s]<=0){flying[s]=null;entityData.set(AXES,axes()|(1<<s));cue("axe_return");}
        var target=getTarget();boolean valid=target!=null&&eligible(target)&&distanceToSqr(target)<=32*32;
        if(valid&&!hadTarget)cue("aggro");hadTarget=valid;
        if(!valid&&target!=null)setTarget(null);
        for(var event:combat.tick())resolve(world,event);
        var attack=combat.active();
        entityData.set(ACTION,attack==null?0:attack.ordinal()+1);
        entityData.set(FRAME,combat.frame());
        if(attack!=null){
            getNavigation().stop();
            setYRot(attackYaw());setYBodyRot(attackYaw());
            if(dashing){
                setDeltaMovement(direction.scale(.78).add(0,getDeltaMovement().y,0));
                for(var p:players(world,2.1,false))if(dashHits.add(p.getUUID())&&hit(p,14,false,false))p.push(direction.x*.7,.25,direction.z*.7);
                if(horizontalCollision)stopMotion();
                world.sendParticles(ParticleTypes.SNOWFLAKE,getX(),getY()+.2,getZ(),9,.5,.15,.5,.02);
            }else if(attack==ShipCaptainCombat.Attack.AXE_SPIN&&combat.frame()>=10&&combat.frame()<50){
                Vec3 move=valid?target.position().subtract(position()).multiply(1,0,1).normalize():Vec3.ZERO;
                setDeltaMovement(move.scale(.14).add(0,getDeltaMovement().y,0));
                if(combat.frame()%4==0)ring(world,4.3,1.2,24);
            }else stopMotion();
            if(combat.frame()%4==0&&combat.frame()<12)ring(world,attack.maxRange<=7?attack.maxRange:1.2,.1,20);
            return;
        }
        if(!valid){getNavigation().stop();return;}
        double distance=distanceTo(target);
        var next=combat.choose(distance,Integer.bitCount(axes()),hasLineOfSight(target),selection);
        if(next==null){if(distance>3.1)getNavigation().moveTo(target,1.0);else getNavigation().stop();return;}
        direction=target.position().subtract(position()).multiply(1,0,1).normalize();
        if(direction.lengthSqr()<.01)direction=getLookAngle().multiply(1,0,1).normalize();
        float aim=(float)Math.toDegrees(Math.atan2(-direction.x,direction.z));
        entityData.set(AIM,aim);setYRot(aim);setYBodyRot(aim);spinPulse=0;dashHits.clear();
        for(var event:combat.start(next,distance,Integer.bitCount(axes()),hasLineOfSight(target)))resolve(world,event);
        entityData.set(ACTION,next.ordinal()+1);entityData.set(FRAME,0);getNavigation().stop();
    }
    private void stopMotion(){setDeltaMovement(0,getDeltaMovement().y,0);}
    private List<ServerPlayer> players(ServerLevel w,double radius,boolean cone){
        return w.getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(radius,1,radius),
            p->eligible(p)&&distanceToSqr(p)<=radius*radius&&hasLineOfSight(p)&&(!cone||p.position().subtract(position()).multiply(1,0,1).normalize().dot(direction)>=.35));
    }
    private boolean hit(ServerPlayer p,float damage,boolean bleed,boolean fracture){
        if(!eligible(p)||!p.hurt(damageSources().mobAttack(this),damage))return false;
        if(bleed&&random.nextFloat()<.60F)WolfBleeding.apply(this,p);
        if(fracture&&random.nextFloat()<.65F)p.addEffect(new MobEffectInstance(ModEffects.FRACTURE,160,0),this);
        return true;
    }
    boolean projectileHit(ServerPlayer p,boolean volley){
        boolean ok=hit(p,volley?8:15,true,false);if(ok)cue("throw_hit");return ok;
    }
    private void ring(ServerLevel w,double radius,double y,int count){
        for(int i=0;i<count;i++){double a=2*Math.PI*i/count;w.sendParticles(ParticleTypes.SNOWFLAKE,getX()+Math.cos(a)*radius,getY()+y,getZ()+Math.sin(a)*radius,1,0,0,0,0);}
    }
    private void resolve(ServerLevel w,ShipCaptainCombat.Event event){
        switch(event.kind()){
            case SOUND -> cue(event.soundKey());
            case MELEE -> {for(var p:players(w,3.5,true))if(hit(p,12,true,false))cue(event.soundKey());}
            case DOUBLE_MELEE -> {for(var p:players(w,3.5,true))if(hit(p,22,false,true))cue(event.soundKey());}
            case THROW -> {int slot=(axes()&1)!=0?0:1;if((axes()&(1<<slot))!=0)launch(w,slot,direction,false);cue(event.soundKey());}
            case SPIN_HIT -> {cue(event.soundKey());spinPulse++;for(var p:players(w,5,false))hit(p,7,true,false);ring(w,4.5,1.1,36);}
            case SLAM -> {cue(event.soundKey());for(var p:players(w,7,false))if(hit(p,18,false,true)){p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,80,1),this);p.push(0,.35,0);}for(int i=2;i<=6;i+=2)ring(w,i,.2,28);}
            case VOLLEY -> {cue(event.soundKey());for(int i=-2;i<=2;i++)launch(w,-1,direction.yRot((float)(i*Math.PI/8)),true);}
            case GRASP -> {cue(event.soundKey());for(var p:players(w,12,true)){Vec3 v=position().subtract(p.position()).multiply(1,0,1).normalize();p.push(v.x*1.35,.15,v.z*1.35);p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0),this);}}
            case DASH_START -> {cue(event.soundKey());dashing=true;dashHits.clear();}
            case DASH_END, COMPLETE -> {dashing=false;stopMotion();}
        }
    }
    public Vec3 hand(int slot){Vec3 forward=direction;if(action()==0)forward=getLookAngle().multiply(1,0,1).normalize();Vec3 side=new Vec3(forward.z,0,-forward.x);return position().add(0,1.55,0).add(side.scale(slot==0?.76:-.76)).add(forward.scale(.15));}
    private void launch(ServerLevel w,int slot,Vec3 heading,boolean volley){
        var axe=ShipCaptainContent.AXE.get().create(w);if(axe==null)return;
        Vec3 start=hand(slot<0?0:slot);axe.setPos(start);axe.launch(this,slot,heading.scale(1.15),volley);
        if(w.addFreshEntity(axe)&&slot>=0){flying[slot]=axe.getUUID();timeout[slot]=100;entityData.set(AXES,axes()&~(1<<slot));}
    }
    boolean ownsAxe(int slot,UUID id){return slot>=0&&slot<2&&id.equals(flying[slot]);}
    void returnAxe(int slot,UUID id){if(ownsAxe(slot,id)){flying[slot]=null;timeout[slot]=0;entityData.set(AXES,axes()|(1<<slot));cue("axe_return");}}
    @Override public void die(DamageSource source){bar.removeAllPlayers();combat.cancel();super.die(source);}
    @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);combat.cancel();entityData.set(ACTION,0);entityData.set(FRAME,0);entityData.set(AXES,3);}
}
