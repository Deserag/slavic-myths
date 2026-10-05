package org.slavicmyths.bandit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.network.chat.Component;

import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.network.syncher.*;
import net.minecraft.nbt.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.*;
import org.slavicmyths.registry.ModSounds;
import org.slavicmyths.progression.Knowledge;

/** Finite encounter controller. Attacks lock their direction before release. */
public final class NightingaleEntity extends PathfinderMob {
    public static final int IDLE=0,NOTICE=1,QUICK=2,HEAVY=3,SHOVE=4,RETREAT=5,INHALE=6,DEEP_INHALE=7,RADIAL=8,EXHALE=9,DEEP_EXHALE=10,DYING=11,QUICK_RELEASE=12,HEAVY_RELEASE=13,SHOVE_RELEASE=14;
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(NightingaleEntity.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DRAWN=SynchedEntityData.defineId(NightingaleEntity.class,EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> IMPACT=SynchedEntityData.defineId(NightingaleEntity.class,EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> START=SynchedEntityData.defineId(NightingaleEntity.class,EntityDataSerializers.INT);
    private final ServerBossEvent bar=new ServerBossEvent(Component.translatable("entity.slavicmyths.nightingale"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
    private final Set<UUID> participants=new HashSet<>();
    public UUID camp;public BlockPos home;
    private int remaining,recovery,sequence,closeTicks,radialCooldown,whistleCooldown,deathTicks;
    private boolean engaged,lootReleased;private DamageSource fatal;
    private Vec3 aim=new Vec3(0,0,1);
    public NightingaleEntity(EntityType<? extends NightingaleEntity> type,Level world){super(type,world);fatal=damageSources().generic();setPersistenceRequired();xpReward=50;getNavigation().setCanFloat(true);}
    public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,250).add(Attributes.MOVEMENT_SPEED,.265).add(Attributes.ATTACK_DAMAGE,8).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.65);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(ACTION,0);builder.define(START,0);builder.define(DRAWN,false);builder.define(IMPACT,0F);}
    public boolean drawn(){return entityData.get(DRAWN);}
    public float impact(){return entityData.get(IMPACT);}
    public int action(){return entityData.get(ACTION);}
    public float elapsed(float partial){return (int)level().getGameTime()-entityData.get(START)+partial;}
    private void begin(int action,int ticks){entityData.set(ACTION,action);entityData.set(START,(int)level().getGameTime());remaining=ticks;getNavigation().stop();
        LivingEntity t=getTarget();if(t!=null){Vec3 d=t.position().subtract(position());aim=new Vec3(d.x,0,d.z).normalize();setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));yBodyRot=getYRot();yHeadRot=getYRot();}
        if(action==INHALE||action==RADIAL)playSound(ModSounds.NIGHTINGALE_INHALE.get(),1,.95F);
        if(action==DEEP_INHALE)playSound(ModSounds.NIGHTINGALE_DEEP_INHALE.get(),1.4F,1);
        if(action==HEAVY)playSound(ModSounds.NIGHTINGALE_BREATH.get(),.8F,.9F);
    }
    private boolean valid(Player p){return p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&(home==null||p.blockPosition().distSqr(home)<48*48);}
    private void notice(Player p){if(!valid(p))return;setTarget(p);participants.add(p.getUUID());if(!engaged&&action()!=NOTICE){begin(NOTICE,35);playSound(ModSounds.NIGHTINGALE_NOTICE.get(),1,1);}}
    @Override public void aiStep(){super.aiStep();if(level().isClientSide||!isAlive())return;if(home==null)home=blockPosition();
        if(radialCooldown>0)radialCooldown--;if(whistleCooldown>0)whistleCooldown--;if(recovery>0)recovery--;
        if(tickCount%20==0){
            Player nearest=null;double best=32*32;
            for(ServerPlayer p:new ArrayList<>(bar.getPlayers()))if(!valid(p)||p.level()!=level()||distanceToSqr(p)>36*36)bar.removePlayer(p);
            for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(36))){
                boolean in=valid(p)&&p.distanceToSqr(this)<36*36;
                if(engaged&&in){participants.add(p.getUUID());Knowledge.award(p,"meet_nightingale");if(p instanceof ServerPlayer)bar.addPlayer((ServerPlayer)p);}
                if(in&&hasLineOfSight(p)&&distanceToSqr(p)<best){nearest=p;best=distanceToSqr(p);}
            }
            if(getTarget()==null||!(getTarget() instanceof Player)||!valid((Player)getTarget())||distanceToSqr(getTarget())>40*40)setTarget(null);
            if(getTarget()==null&&nearest!=null)notice(nearest);
            if(engaged&&getTarget()!=null){participants.add(getTarget().getUUID());Knowledge.award((Player)getTarget(),"meet_nightingale");}
            bar.setProgress(getHealth()/getMaxHealth());
        }
        LivingEntity target=getTarget();
        if(target==null){if(engaged){engaged=false;entityData.set(DRAWN,false);bar.removeAllPlayers();begin(IDLE,0);}if(distanceToSqr(Vec3.atCenterOf(home))>16&&tickCount%20==0)getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.8);return;}
        if(remaining>0){getNavigation().stop();if(action()!=RETREAT){setYRot((float)Math.toDegrees(Math.atan2(-aim.x,aim.z)));yBodyRot=getYRot();yHeadRot=getYRot();}
            if(action()==RETREAT&&onGround())setDeltaMovement(aim.scale(-.16).add(0,getDeltaMovement().y,0));
            if((action()==INHALE||action()==DEEP_INHALE||action()==RADIAL)&&remaining%5==0){ServerLevel w=(ServerLevel)level();Vec3 p=getEyePosition(1).add(aim.scale(1.1));w.sendParticles(ParticleTypes.CLOUD,p.x,p.y,p.z,0,-aim.x*.06,0,-aim.z*.06,1);}
            if(action()==EXHALE||action()==DEEP_EXHALE)WindAttack.particles((ServerLevel)level(),position().add(0,1,0),aim,(13-remaining)*(action()==DEEP_EXHALE?1.65:1.3),false);
            if(--remaining==0)resolve();return;
        }
        // Walk off the open southern platform edge; normal gravity handles descent.
        if(getY()>target.getY()+3&&getY()>=home.getY()-1&&Math.abs(getX()-home.getX())<9&&Math.abs(getZ()-home.getZ())<9){
            if(tickCount%10==0)getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+4.5,1);
            if(onGround()&&getNavigation().isDone())setDeltaMovement(new Vec3(0,getDeltaMovement().y,.15));return;
        }
        getLookControl().setLookAt(target,25,25);
        double distance=distanceToSqr(target);closeTicks=distance<6?closeTicks+1:Math.max(0,closeTicks-2);
        if(recovery>0){if(tickCount%10==0)getNavigation().moveTo(target,getHealth()<75?1.12:1);return;}
        if(closeTicks>=65&&radialCooldown==0){begin(RADIAL,16);radialCooldown=200;closeTicks=0;return;}
        if(whistleCooldown==0&&(distance>18||sequence%3==2)) {begin(RETREAT,14);return;}
        if(distance<11&&hasLineOfSight(target)){begin(distance<3&&sequence%3==1?SHOVE:sequence%3==0?HEAVY:QUICK,sequence%3==0?23:11);sequence++;}
        else if(tickCount%10==0)getNavigation().moveTo(target,getHealth()<75?1.12:1);
    }
    private void resolve(){int a=action();
        if(a==NOTICE){engaged=true;entityData.set(DRAWN,true);begin(IDLE,0);recovery=15;
            if(camp!=null)for(BanditEntity b:level().getEntitiesOfClass(BanditEntity.class,getBoundingBox().inflate(22),b->camp.equals(b.camp)))if(getTarget()!=null&&b.hasLineOfSight(getTarget()))b.engage(getTarget());
            return;}
        if(a==RETREAT){boolean heavy=sequence%(getHealth()>150?4:2)==1;begin(heavy?DEEP_INHALE:INHALE,heavy?40:27);sequence++;return;}
        if(a==INHALE||a==DEEP_INHALE||a==RADIAL){boolean deep=a==DEEP_INHALE,radial=a==RADIAL;
            playSound(deep?ModSounds.NIGHTINGALE_DESTRUCTIVE.get():ModSounds.NIGHTINGALE_WHISTLE.get(),deep?2:1.2F,radial?.8F:1);
            playSound(ModSounds.NIGHTINGALE_WAVE.get(),1,.9F);
            WindAttack.release(this,aim,radial?5.5:deep?20:16,radial?3:deep?15:10,radial?2:deep?2.8F:1.9F,radial,false);
            if(deep)WindAttack.terrain(this,aim,20,true);
            if(radial){for(int i=1;i<=5;i+=2)WindAttack.particles((ServerLevel)level(),position().add(0,1,0),aim,i,true);begin(IDLE,0);}
            else begin(deep?DEEP_EXHALE:EXHALE,12);
            whistleCooldown=getHealth()>150?150:getHealth()>75?110:85;recovery=30;return;
        }
        if(a==QUICK||a==HEAVY||a==SHOVE){LivingEntity t=getTarget();playSound(ModSounds.NIGHTINGALE_MELEE.get(),.85F,a==HEAVY?.8F:1.1F);
            if(t!=null&&distanceToSqr(t)<12&&hasLineOfSight(t)&&WindAttack.inCone(t.position().subtract(position()),aim,3.5,false)){t.hurt(damageSources().mobAttack(this),a==HEAVY?12:a==SHOVE?3:7);t.knockback(a==SHOVE?1.1F:a==HEAVY?.5F:.15F,getX()-t.getX(),getZ()-t.getZ());t.hurtMarked=true;}
            recovery=getHealth()<75?13:23;begin(a==QUICK?QUICK_RELEASE:a==HEAVY?HEAVY_RELEASE:SHOVE_RELEASE,8);return;
        }
        begin(IDLE,0);
    }
    @Override public boolean hurt(DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit&&!level().isClientSide)entityData.set(IMPACT,Math.min(1,amount/12));if(hit&&!level().isClientSide&&isAlive()&&source.getEntity() instanceof Player)notice((Player)source.getEntity());return hit;}
    @Override public boolean isAlliedTo(Entity other){return other instanceof BanditEntity||other instanceof NightingaleEntity||super.isAlliedTo(other);}
    @Override public boolean canChangeDimensions(net.minecraft.world.level.Level from,net.minecraft.world.level.Level to){return false;}
    @Override public int getMaxFallDistance(){return 9;}
    @Override public boolean causeFallDamage(float distance,float multiplier,net.minecraft.world.damagesource.DamageSource source){return super.causeFallDamage(Math.max(0,distance-7),multiplier,source);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public int getAmbientSoundInterval(){return 600;}
    @Override protected SoundEvent getAmbientSound(){return action()!=IDLE?null:getHealth()<75?ModSounds.NIGHTINGALE_VOICE.get():ModSounds.NIGHTINGALE_IDLE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource s){return ModSounds.NIGHTINGALE_HURT.get();}
    @Override protected SoundEvent getDeathSound(){return ModSounds.NIGHTINGALE_DEATH.get();}
    @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override public void remove(Entity.RemovalReason reason){bar.removeAllPlayers();super.remove(reason);}
    @Override public void onRemovedFromLevel(){bar.removeAllPlayers();super.onRemovedFromLevel();}
    @Override public void die(DamageSource source){
        boolean wasDead=dead;super.die(source);
        // Forge death cancellation must not resolve the camp or award a victory.
        if(!level().isClientSide&&!wasDead&&dead){
            fatal=source;if(source.getEntity() instanceof Player)participants.add(source.getEntity().getUUID());
            begin(DYING,0);bar.removeAllPlayers();
            if(camp!=null)StrongholdRecords.get((ServerLevel)level()).nightingaleDied(camp);
            for(UUID id:participants){ServerPlayer p=((ServerLevel)level()).getServer().getPlayerList().getPlayer(id);if(p!=null&&p.level()==level()&&distanceToSqr(p)<64*64){Knowledge.award(p,"meet_nightingale");Knowledge.award(p,"defeat_nightingale");}}
        }
    }
    @Override protected void dropAllDeathLoot(ServerLevel world,DamageSource source){fatal=source;} // deferred until the collapse
    @Override protected void tickDeath(){deathTicks++;deathTime=deathTicks;
        if(!level().isClientSide&&deathTicks==32)WindAttack.particles((ServerLevel)level(),position().add(0,.5,0),aim,2,true);
        if(!level().isClientSide&&deathTicks>=55){if(!lootReleased){lootReleased=true;super.dropAllDeathLoot((ServerLevel)level(),fatal);}discard();}
    }
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(camp!=null)n.putUUID("NightingaleCamp",camp);if(home!=null)n.putLong("NightingaleHome",home.asLong());n.putBoolean("NightingaleLoot",lootReleased);n.putInt("CollapseTicks",deathTicks);ListTag list=new ListTag();for(UUID id:participants)list.add(StringTag.valueOf(id.toString()));n.put("Witnesses",list);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);camp=n.hasUUID("NightingaleCamp")?n.getUUID("NightingaleCamp"):null;home=n.contains("NightingaleHome")?BlockPos.of(n.getLong("NightingaleHome")):null;lootReleased=n.getBoolean("NightingaleLoot");deathTicks=n.getInt("CollapseTicks");for(Tag v:n.getList("Witnesses",8))try{participants.add(UUID.fromString(v.getAsString()));}catch(IllegalArgumentException ignored){}begin(getHealth()<=0?DYING:IDLE,0);if(getHealth()<=0)entityData.set(START,(int)level().getGameTime()-deathTicks);recovery=40;}
}
