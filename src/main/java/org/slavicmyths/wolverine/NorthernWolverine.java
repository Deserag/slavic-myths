package org.slavicmyths.wolverine;

import java.util.List;
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
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.trophy.TrophyHeads;

public final class NorthernWolverine extends Monster {
    public static final int IDLE=0, CLAWS=1, BITE=2, SWEEP=3, SPIKES=4, BURROW=5, GRIP=6, ROAR=7, RECOVERY=8;
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(NorthernWolverine.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CLOCK=SynchedEntityData.defineId(NorthernWolverine.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PHASE_TWO=SynchedEntityData.defineId(NorthernWolverine.class,EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bar=new org.slavicmyths.combat.CreatureBossBar(this, Component.literal("Северная росомаха"),BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
    private int attackCooldown=35,roarCooldown=120,buffTicks=0;
    private boolean hitA,hitB;
    private Vec3 burrowDestination=Vec3.ZERO;

    public NorthernWolverine(EntityType<? extends NorthernWolverine> type,Level level) {
        super(type,level);
        setPersistenceRequired();
        xpReward=100;
    }
    public static AttributeSupplier.Builder attributes(){
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,800)
            .add(Attributes.ATTACK_DAMAGE,12).add(Attributes.ARMOR,10).add(Attributes.ARMOR_TOUGHNESS,4)
            .add(Attributes.MOVEMENT_SPEED,.29).add(Attributes.FOLLOW_RANGE,48)
            .add(Attributes.KNOCKBACK_RESISTANCE,.85).add(Attributes.STEP_HEIGHT,1.1);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){
        super.defineSynchedData(builder);
        builder.define(ACTION,IDLE);builder.define(CLOCK,0);builder.define(PHASE_TWO,false);
    }
    public int action(){return entityData.get(ACTION);}
    public int actionTicks(){return Math.max(0,(int)level().getGameTime()-entityData.get(CLOCK));}
    public boolean phaseTwo(){return entityData.get(PHASE_TWO);}
    public boolean empowered(){return buffTicks>0;}
    @Override protected void registerGoals(){
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new MeleeAttackGoal(this,1.0,false));
        goalSelector.addGoal(6,new RandomStrollGoal(this,.7));
        goalSelector.addGoal(7,new LookAtPlayerGoal(this,Player.class,16));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    private void begin(int kind){
        entityData.set(ACTION,kind);
        entityData.set(CLOCK,(int)level().getGameTime());
        hitA=hitB=false;
        getNavigation().stop();
        playSound(kind==ROAR?SoundEvents.RAVAGER_ROAR:SoundEvents.POLAR_BEAR_WARNING,1.4F,kind==ROAR?.68F:.85F);
    }
    private float strength(float amount){return buffTicks>0?amount*1.2F:amount;}
    private void strike(float amount,double reach,double arc,float knock){
        if(!(level() instanceof ServerLevel world))return;
        Vec3 facing=getLookAngle().multiply(1,0,1).normalize();
        for(LivingEntity victim:world.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(reach), e->e!=this&&e.isAlive()&&!(e instanceof Player p&&(p.isCreative()||p.isSpectator())))){
            Vec3 to=victim.position().subtract(position()).multiply(1,0,1);
            if(to.lengthSqr()>reach*reach||to.lengthSqr()<.001||facing.dot(to.normalize())<arc||!getSensing().hasLineOfSight(victim))continue;
            if(victim.hurt(damageSources().mobAttack(this),strength(amount))&&knock>0)victim.knockback(knock,getX()-victim.getX(),getZ()-victim.getZ());
        }
    }
    private void roar(){
        if(!(level() instanceof ServerLevel world))return;
        world.sendParticles(ParticleTypes.SNOWFLAKE,getX(),getY()+1.4,getZ(),120,4.5,1.1,4.5,.18);
        for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(12),e->!e.isCreative()&&!e.isSpectator())){
            double distance=Math.sqrt(distanceToSqr(p));
            if(distance>12||!getSensing().hasLineOfSight(p))continue;
            float damage=distance<=3?16:distance<=6?10:distance<=9?6:3;
            p.hurt(damageSources().mobAttack(this),damage);
            if(distance<=6)p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,distance<=3?100:80,distance<=3?2:1),this);
            else p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,distance<=9?80:40,0),this);
        }
        buffTicks=200;
        roarCooldown=700;
    }
    private void animateCombat(){
        int age=actionTicks(),a=action();
        if(a==IDLE)return;
        if(a==CLAWS){if(age==11||age==21)strike(9,3.7,.1F,.15F);}
        if(a==BITE&&age==16)strike(16,3.3,.65F,.7F);
        if(a==SWEEP&&age==17)strike(10,4.2,-.3F,.9F);
        if(a==SPIKES&&age==22)strike(14,3.8,-.6F,.85F);
        if(a==GRIP&&age==20)strike(22,2.9,.45F,1.25F);
        if(a==ROAR&&age==30)roar();
        if(a==BURROW){
            if(age==15&&getTarget()!=null)burrowDestination=getTarget().position();
            if(age==24&&level() instanceof ServerLevel world){
                Vec3 target=burrowDestination;
                if(level().hasChunkAt(net.minecraft.core.BlockPos.containing(target))&&level().noCollision(this,getBoundingBox().move(target.subtract(position())))){
                    teleportTo(target.x,target.y,target.z);
                    world.sendParticles(ParticleTypes.SNOWFLAKE,getX(),getY()+.4,getZ(),70,1.6,.5,1.6,.08);
                }
            }
            if(age==30)strike(17,3.6,-.2F,.8F);
        }
        int duration=switch(a){case CLAWS->36;case BITE->32;case SWEEP->34;case SPIKES->38;case GRIP->40;case BURROW->46;case ROAR->52;default->20;};
        if(age>=duration){
            entityData.set(ACTION,IDLE);
            attackCooldown=(int)((phaseTwo()?28:38)*(buffTicks>0?.8333333:1));
        }
    }
    @Override public void tick(){
        super.tick();
        if(level().isClientSide||!isAlive())return;
        bar.setProgress(getHealth()/getMaxHealth());
        if(!phaseTwo()&&getHealth()<=400){entityData.set(PHASE_TWO,true);attackCooldown=35;playSound(SoundEvents.POLAR_BEAR_WARNING,2F,.6F);}
        if(buffTicks>0)buffTicks--;
        if(roarCooldown>0)roarCooldown--;
        if(attackCooldown>0)attackCooldown--;
        if(action()!=IDLE){getNavigation().stop();animateCombat();return;}
        LivingEntity target=getTarget();
        if(target==null||!target.isAlive())return;
        double distance=distanceTo(target);
        if(phaseTwo()&&roarCooldown==0&&distance<=12){begin(ROAR);return;}
        if(attackCooldown>0)return;
        if(phaseTwo()&&distance>=5&&distance<14){begin(BURROW);return;}
        if(distance<=3.6){
            int roll=random.nextInt(phaseTwo()?5:3);
            begin(switch(roll){case 0->CLAWS;case 1->BITE;case 2->SWEEP;case 3->SPIKES;default->GRIP;});
        }
    }
    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity victim){return false;}
    @Override public void startSeenByPlayer(ServerPlayer player){super.startSeenByPlayer(player);bar.addPlayer(player);}
    @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);bar.removePlayer(player);}
    @Override public void die(DamageSource source){
        if(!level().isClientSide&&getHealth()<=0)spawnAtLocation(TrophyHeads.ITEMS.get(TrophyHeads.Kind.NORTH_WOLVERINE).get());
        super.die(source);
        bar.removeAllPlayers();
    }
    @Override public void addAdditionalSaveData(CompoundTag data){super.addAdditionalSaveData(data);data.putInt("WolverineRoar",roarCooldown);data.putInt("WolverineBuff",buffTicks);data.putInt("WolverineAttack",attackCooldown);data.putBoolean("WolverinePhaseTwo",phaseTwo());}
    @Override public void readAdditionalSaveData(CompoundTag data){super.readAdditionalSaveData(data);roarCooldown=Math.max(0,data.getInt("WolverineRoar"));buffTicks=Math.max(0,data.getInt("WolverineBuff"));attackCooldown=Math.max(0,data.getInt("WolverineAttack"));entityData.set(PHASE_TWO,data.getBoolean("WolverinePhaseTwo")||getHealth()<=400);entityData.set(ACTION,IDLE);}
}
