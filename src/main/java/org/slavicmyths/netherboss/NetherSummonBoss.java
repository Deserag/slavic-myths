package org.slavicmyths.netherboss;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/** First WIP runtime slice for the two approved humanoid Nether bosses.
 * Summoning rites, custom minion AI, rune rewards and unique recorded sounds are
 * tracked in issues #353/#354 and MUST NOT be described as implemented.
 */
public final class NetherSummonBoss extends Monster {
    public static final int MOROVA=0, TRIZNA=1;
    private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(NetherSummonBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(NetherSummonBoss.class,EntityDataSerializers.INT);
    private final int kind;
    private final ServerBossEvent bossBar;
    private int actionTicks=0, actionCooldown=80;
    private boolean actionResolved;
    public int actionTicks(){return actionTicks;}
    public NetherSummonBoss(EntityType<? extends NetherSummonBoss> type,Level level,int kind){
        super(type,level);this.kind=kind;
        this.bossBar=new ServerBossEvent(Component.translatable(kind==MOROVA?"entity.slavicmyths.morova_deva":"entity.slavicmyths.trizna_prince"),
                kind==MOROVA?BossEvent.BossBarColor.GREEN:BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
        this.xpReward=kind==MOROVA?200:300;setHealth(getMaxHealth());
    }
    @Override public float getMaxHealth(){return (float)org.slavicmyths.combat.CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH));}
    public boolean isMorova(){return kind==MOROVA;}
    public int phase(){return entityData.get(PHASE);}
    public int action(){return entityData.get(ACTION);}
    public static AttributeSupplier.Builder attributes(boolean morova){
        return createMonsterAttributes().add(Attributes.MAX_HEALTH,morova?1350:1800)
            .add(Attributes.MOVEMENT_SPEED,morova?0.28:0.235)
            .add(Attributes.ATTACK_DAMAGE,morova?6:12)
            .add(Attributes.ARMOR,morova?8:17)
            .add(Attributes.KNOCKBACK_RESISTANCE,morova?0.45:0.8)
            .add(Attributes.FOLLOW_RANGE,36);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){
        super.defineSynchedData(builder);builder.define(PHASE,1);builder.define(ACTION,0);
    }
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        // Scripted hits own damage; vanilla melee must never bypass the windup.
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.8));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,12));
        goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    @Override public void startSeenByPlayer(ServerPlayer player){
        super.startSeenByPlayer(player);bossBar.addPlayer(player);
    }
    @Override public void stopSeenByPlayer(ServerPlayer player){
        super.stopSeenByPlayer(player);bossBar.removePlayer(player);
    }
    @Override protected void customServerAiStep(){
        super.customServerAiStep();
        bossBar.setProgress(Math.max(0,Math.min(1,getHealth()/getMaxHealth())));
        int next=isMorova()?(getHealth()>getMaxHealth()*.7?1:getHealth()>getMaxHealth()*.35?2:3)
                :(getHealth()>getMaxHealth()*.65?1:getHealth()>getMaxHealth()*.3?2:3);
        entityData.set(PHASE,next);
        if(actionTicks>0){
            actionTicks--;
            if(actionTicks==12&&!actionResolved){actionResolved=true;resolveAction();}
            if(actionTicks==0)entityData.set(ACTION,0);
            return;
        }
        if(actionCooldown>0){actionCooldown--;if(getTarget()!=null&&action()==0)getNavigation().moveTo(getTarget(),1.0);return;}
        LivingEntity victim=getTarget();
        if(victim==null||!victim.isAlive()||distanceToSqr(victim)>144){actionCooldown=25;return;}
        getNavigation().stop();
        getLookControl().setLookAt(victim,30,30);
        entityData.set(ACTION,isMorova()?1:2);
        actionResolved=false;actionTicks=isMorova()?42:48;actionCooldown=next==3?70:next==2?100:130;
        playSound(isMorova()?SoundEvents.EVOKER_PREPARE_ATTACK:SoundEvents.WITHER_SKELETON_AMBIENT,1.3F,isMorova()?.65F:.78F);
    }
    private void resolveAction(){
        if(!(level() instanceof ServerLevel world))return;
        LivingEntity victim=getTarget();
        if(victim==null||!victim.isAlive())return;
        double range=isMorova()?6:4.3;
        AABB box=getBoundingBox().inflate(range,2.0,range);
        for(Player player:world.getEntitiesOfClass(Player.class,box,p->p.isAlive()&&!p.isSpectator()&&!p.isCreative())){
            if(player.distanceToSqr(this)>range*range||!hasLineOfSight(player))continue;
            player.hurt(damageSources().mobAttack(this),isMorova()?8:14);
            if(isMorova())player.addEffect(new net.minecraft.world.effect.MobEffectInstance(org.slavicmyths.registry.ModEffects.MOROVA_PLAGUE,200,phase()-1));
            else player.igniteForSeconds(phase()>=3?4:2);
        }
        world.sendParticles(isMorova()?ParticleTypes.SOUL:ParticleTypes.FLAME,getX(),getY(0.6),getZ(),
                phase()>=3?90:45,range*.6,1.2,range*.6,.03);
        playSound(isMorova()?SoundEvents.WITCH_CELEBRATE:SoundEvents.BLAZE_SHOOT,1.4F,.75F);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag){
        super.addAdditionalSaveData(tag);tag.putInt("NetherBossPhase",phase());
        tag.putInt("NetherBossAction",action());tag.putInt("NetherBossActionTicks",actionTicks);
        tag.putInt("NetherBossCooldown",actionCooldown);tag.putBoolean("NetherBossResolved",actionResolved);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);entityData.set(PHASE,Math.max(1,Math.min(3,tag.getInt("NetherBossPhase"))));
        actionTicks=Math.clamp(tag.getInt("NetherBossActionTicks"),0,48);
        actionCooldown=Math.clamp(tag.getInt("NetherBossCooldown"),0,130);actionResolved=tag.getBoolean("NetherBossResolved");
        entityData.set(ACTION,actionTicks>0?Math.clamp(tag.getInt("NetherBossAction"),0,2):0);
    }
    @Override protected SoundEvent getAmbientSound(){return isMorova()?SoundEvents.WITCH_AMBIENT:SoundEvents.WITHER_SKELETON_AMBIENT;}
    @Override protected SoundEvent getHurtSound(DamageSource source){return isMorova()?SoundEvents.WITCH_HURT:SoundEvents.WITHER_SKELETON_HURT;}
    @Override protected SoundEvent getDeathSound(){return isMorova()?SoundEvents.WITCH_DEATH:SoundEvents.WITHER_SKELETON_DEATH;}
}
