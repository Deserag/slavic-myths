package org.slavicmyths.bandit;

import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.datasync.*;
import net.minecraft.nbt.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.server.*;
import org.slavicmyths.registry.ModSounds;
import org.slavicmyths.progression.Knowledge;

/** Finite encounter controller. Attacks lock their direction before release. */
public final class NightingaleEntity extends CreatureEntity {
    public static final int IDLE=0,NOTICE=1,QUICK=2,HEAVY=3,SHOVE=4,RETREAT=5,INHALE=6,DEEP_INHALE=7,RADIAL=8,EXHALE=9,DEEP_EXHALE=10,DYING=11,QUICK_RELEASE=12,HEAVY_RELEASE=13,SHOVE_RELEASE=14;
    private static final DataParameter<Integer> ACTION=EntityDataManager.defineId(NightingaleEntity.class,DataSerializers.INT);
    private static final DataParameter<Boolean> DRAWN=EntityDataManager.defineId(NightingaleEntity.class,DataSerializers.BOOLEAN);
    private static final DataParameter<Float> IMPACT=EntityDataManager.defineId(NightingaleEntity.class,DataSerializers.FLOAT);
    private static final DataParameter<Integer> START=EntityDataManager.defineId(NightingaleEntity.class,DataSerializers.INT);
    private final ServerBossInfo bar=new ServerBossInfo(new net.minecraft.util.text.TranslationTextComponent("entity.slavicmyths.nightingale"),BossInfo.Color.RED,BossInfo.Overlay.PROGRESS);
    private final Set<UUID> participants=new HashSet<>();
    public UUID camp;public BlockPos home;
    private int remaining,recovery,sequence,closeTicks,radialCooldown,whistleCooldown,deathTicks;
    private boolean engaged,lootReleased;private DamageSource fatal=DamageSource.GENERIC;
    private Vector3d aim=new Vector3d(0,0,1);
    public NightingaleEntity(EntityType<? extends NightingaleEntity> type,World world){super(type,world);setPersistenceRequired();xpReward=50;getNavigation().setCanFloat(true);}
    public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,250).add(Attributes.MOVEMENT_SPEED,.265).add(Attributes.ATTACK_DAMAGE,8).add(Attributes.FOLLOW_RANGE,32).add(Attributes.KNOCKBACK_RESISTANCE,.65);}
    @Override protected void registerGoals(){goalSelector.addGoal(0,new SwimGoal(this));}
    @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(ACTION,0);entityData.define(START,0);entityData.define(DRAWN,false);entityData.define(IMPACT,0F);}
    public boolean drawn(){return entityData.get(DRAWN);}
    public float impact(){return entityData.get(IMPACT);}
    public int action(){return entityData.get(ACTION);}
    public float elapsed(float partial){return (int)level.getGameTime()-entityData.get(START)+partial;}
    private void begin(int action,int ticks){entityData.set(ACTION,action);entityData.set(START,(int)level.getGameTime());remaining=ticks;getNavigation().stop();
        LivingEntity t=getTarget();if(t!=null){Vector3d d=t.position().subtract(position());aim=new Vector3d(d.x,0,d.z).normalize();yRot=(float)Math.toDegrees(Math.atan2(-aim.x,aim.z));yBodyRot=yRot;yHeadRot=yRot;}
        if(action==INHALE||action==RADIAL)playSound(ModSounds.NIGHTINGALE_INHALE.get(),1,.95F);
        if(action==DEEP_INHALE)playSound(ModSounds.NIGHTINGALE_DEEP_INHALE.get(),1.4F,1);
        if(action==HEAVY)playSound(ModSounds.NIGHTINGALE_BREATH.get(),.8F,.9F);
    }
    private boolean valid(PlayerEntity p){return p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&(home==null||p.blockPosition().distSqr(home)<48*48);}
    private void notice(PlayerEntity p){if(!valid(p))return;setTarget(p);participants.add(p.getUUID());if(!engaged&&action()!=NOTICE){begin(NOTICE,35);playSound(ModSounds.NIGHTINGALE_NOTICE.get(),1,1);}}
    @Override public void aiStep(){super.aiStep();if(level.isClientSide||!isAlive())return;if(home==null)home=blockPosition();
        if(radialCooldown>0)radialCooldown--;if(whistleCooldown>0)whistleCooldown--;if(recovery>0)recovery--;
        if(tickCount%20==0){
            PlayerEntity nearest=null;double best=32*32;
            for(ServerPlayerEntity p:new ArrayList<>(bar.getPlayers()))if(!valid(p)||p.level!=level||distanceToSqr(p)>36*36)bar.removePlayer(p);
            for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(36))){
                boolean in=valid(p)&&p.distanceToSqr(this)<36*36;
                if(engaged&&in){participants.add(p.getUUID());Knowledge.award(p,"meet_nightingale");if(p instanceof ServerPlayerEntity)bar.addPlayer((ServerPlayerEntity)p);}
                if(in&&canSee(p)&&distanceToSqr(p)<best){nearest=p;best=distanceToSqr(p);}
            }
            if(getTarget()==null||!(getTarget() instanceof PlayerEntity)||!valid((PlayerEntity)getTarget())||distanceToSqr(getTarget())>40*40)setTarget(null);
            if(getTarget()==null&&nearest!=null)notice(nearest);
            if(engaged&&getTarget()!=null){participants.add(getTarget().getUUID());Knowledge.award((PlayerEntity)getTarget(),"meet_nightingale");}
            bar.setPercent(getHealth()/getMaxHealth());
        }
        LivingEntity target=getTarget();
        if(target==null){if(engaged){engaged=false;entityData.set(DRAWN,false);bar.removeAllPlayers();begin(IDLE,0);}if(distanceToSqr(Vector3d.atCenterOf(home))>16&&tickCount%20==0)getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,.8);return;}
        if(remaining>0){getNavigation().stop();if(action()!=RETREAT){yRot=(float)Math.toDegrees(Math.atan2(-aim.x,aim.z));yBodyRot=yRot;yHeadRot=yRot;}
            if(action()==RETREAT&&isOnGround())setDeltaMovement(aim.scale(-.16).add(0,getDeltaMovement().y,0));
            if((action()==INHALE||action()==DEEP_INHALE||action()==RADIAL)&&remaining%5==0){ServerWorld w=(ServerWorld)level;Vector3d p=getEyePosition(1).add(aim.scale(1.1));w.sendParticles(ParticleTypes.CLOUD,p.x,p.y,p.z,0,-aim.x*.06,0,-aim.z*.06,1);}
            if(action()==EXHALE||action()==DEEP_EXHALE)WindAttack.particles((ServerWorld)level,position().add(0,1,0),aim,(13-remaining)*(action()==DEEP_EXHALE?1.65:1.3),false);
            if(--remaining==0)resolve();return;
        }
        // Walk off the open southern platform edge; normal gravity handles descent.
        if(getY()>target.getY()+3&&getY()>=home.getY()-1&&Math.abs(getX()-home.getX())<9&&Math.abs(getZ()-home.getZ())<9){
            if(tickCount%10==0)getNavigation().moveTo(home.getX()+.5,home.getY(),home.getZ()+4.5,1);
            if(isOnGround()&&getNavigation().isDone())setDeltaMovement(new Vector3d(0,getDeltaMovement().y,.15));return;
        }
        getLookControl().setLookAt(target,25,25);
        double distance=distanceToSqr(target);closeTicks=distance<6?closeTicks+1:Math.max(0,closeTicks-2);
        if(recovery>0){if(tickCount%10==0)getNavigation().moveTo(target,getHealth()<75?1.12:1);return;}
        if(closeTicks>=65&&radialCooldown==0){begin(RADIAL,16);radialCooldown=200;closeTicks=0;return;}
        if(whistleCooldown==0&&(distance>18||sequence%3==2)) {begin(RETREAT,14);return;}
        if(distance<11&&canSee(target)){begin(distance<3&&sequence%3==1?SHOVE:sequence%3==0?HEAVY:QUICK,sequence%3==0?23:11);sequence++;}
        else if(tickCount%10==0)getNavigation().moveTo(target,getHealth()<75?1.12:1);
    }
    private void resolve(){int a=action();
        if(a==NOTICE){engaged=true;entityData.set(DRAWN,true);begin(IDLE,0);recovery=15;
            if(camp!=null)for(BanditEntity b:level.getEntitiesOfClass(BanditEntity.class,getBoundingBox().inflate(22),b->camp.equals(b.camp)))if(getTarget()!=null&&b.canSee(getTarget()))b.engage(getTarget());
            return;}
        if(a==RETREAT){boolean heavy=sequence%(getHealth()>150?4:2)==1;begin(heavy?DEEP_INHALE:INHALE,heavy?40:27);sequence++;return;}
        if(a==INHALE||a==DEEP_INHALE||a==RADIAL){boolean deep=a==DEEP_INHALE,radial=a==RADIAL;
            playSound(deep?ModSounds.NIGHTINGALE_DESTRUCTIVE.get():ModSounds.NIGHTINGALE_WHISTLE.get(),deep?2:1.2F,radial?.8F:1);
            playSound(ModSounds.NIGHTINGALE_WAVE.get(),1,.9F);
            WindAttack.release(this,aim,radial?5.5:deep?20:16,radial?3:deep?15:10,radial?2:deep?2.8F:1.9F,radial,false);
            if(deep)WindAttack.terrain(this,aim,20,true);
            if(radial){for(int i=1;i<=5;i+=2)WindAttack.particles((ServerWorld)level,position().add(0,1,0),aim,i,true);begin(IDLE,0);}
            else begin(deep?DEEP_EXHALE:EXHALE,12);
            whistleCooldown=getHealth()>150?150:getHealth()>75?110:85;recovery=30;return;
        }
        if(a==QUICK||a==HEAVY||a==SHOVE){LivingEntity t=getTarget();playSound(ModSounds.NIGHTINGALE_MELEE.get(),.85F,a==HEAVY?.8F:1.1F);
            if(t!=null&&distanceToSqr(t)<12&&canSee(t)&&WindAttack.inCone(t.position().subtract(position()),aim,3.5,false)){t.hurt(DamageSource.mobAttack(this),a==HEAVY?12:a==SHOVE?3:7);t.knockback(a==SHOVE?1.1F:a==HEAVY?.5F:.15F,getX()-t.getX(),getZ()-t.getZ());t.hurtMarked=true;}
            recovery=getHealth()<75?13:23;begin(a==QUICK?QUICK_RELEASE:a==HEAVY?HEAVY_RELEASE:SHOVE_RELEASE,8);return;
        }
        begin(IDLE,0);
    }
    @Override public boolean hurt(DamageSource source,float amount){boolean hit=super.hurt(source,amount);if(hit&&!level.isClientSide)entityData.set(IMPACT,Math.min(1,amount/12));if(hit&&!level.isClientSide&&isAlive()&&source.getEntity() instanceof PlayerEntity)notice((PlayerEntity)source.getEntity());return hit;}
    @Override public boolean isAlliedTo(Entity other){return other instanceof BanditEntity||other instanceof NightingaleEntity||super.isAlliedTo(other);}
    @Override public boolean canChangeDimensions(){return false;}
    @Override public int getMaxFallDistance(){return 9;}
    @Override public boolean causeFallDamage(float distance,float multiplier){return super.causeFallDamage(Math.max(0,distance-7),multiplier);}
    @Override public boolean removeWhenFarAway(double distance){return false;}
    @Override public int getAmbientSoundInterval(){return 600;}
    @Override protected SoundEvent getAmbientSound(){return action()!=IDLE?null:getHealth()<75?ModSounds.NIGHTINGALE_VOICE.get():ModSounds.NIGHTINGALE_IDLE.get();}
    @Override protected SoundEvent getHurtSound(DamageSource s){return ModSounds.NIGHTINGALE_HURT.get();}
    @Override protected SoundEvent getDeathSound(){return ModSounds.NIGHTINGALE_DEATH.get();}
    @Override public void stopSeenByPlayer(ServerPlayerEntity p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
    @Override public void remove(boolean keepData){bar.removeAllPlayers();super.remove(keepData);}
    @Override public void onRemovedFromWorld(){bar.removeAllPlayers();super.onRemovedFromWorld();}
    @Override public void die(DamageSource source){
        boolean wasDead=dead;super.die(source);
        // Forge death cancellation must not resolve the camp or award a victory.
        if(!level.isClientSide&&!wasDead&&dead){
            fatal=source;if(source.getEntity() instanceof PlayerEntity)participants.add(source.getEntity().getUUID());
            begin(DYING,0);bar.removeAllPlayers();
            if(camp!=null)StrongholdRecords.get((ServerWorld)level).nightingaleDied(camp);
            for(UUID id:participants){ServerPlayerEntity p=((ServerWorld)level).getServer().getPlayerList().getPlayer(id);if(p!=null&&p.level==level&&distanceToSqr(p)<64*64){Knowledge.award(p,"meet_nightingale");Knowledge.award(p,"defeat_nightingale");}}
        }
    }
    @Override protected void dropAllDeathLoot(DamageSource source){fatal=source;} // deferred until the collapse
    @Override protected void tickDeath(){deathTicks++;deathTime=deathTicks;
        if(!level.isClientSide&&deathTicks==32)WindAttack.particles((ServerWorld)level,position().add(0,.5,0),aim,2,true);
        if(!level.isClientSide&&deathTicks>=55){if(!lootReleased){lootReleased=true;super.dropAllDeathLoot(fatal);}remove();}
    }
    @Override public void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);if(camp!=null)n.putUUID("NightingaleCamp",camp);if(home!=null)n.putLong("NightingaleHome",home.asLong());n.putBoolean("NightingaleLoot",lootReleased);n.putInt("CollapseTicks",deathTicks);ListNBT list=new ListNBT();for(UUID id:participants)list.add(StringNBT.valueOf(id.toString()));n.put("Witnesses",list);}
    @Override public void readAdditionalSaveData(CompoundNBT n){super.readAdditionalSaveData(n);camp=n.hasUUID("NightingaleCamp")?n.getUUID("NightingaleCamp"):null;home=n.contains("NightingaleHome")?BlockPos.of(n.getLong("NightingaleHome")):null;lootReleased=n.getBoolean("NightingaleLoot");deathTicks=n.getInt("CollapseTicks");for(INBT v:n.getList("Witnesses",8))try{participants.add(UUID.fromString(v.getAsString()));}catch(IllegalArgumentException ignored){}begin(getHealth()<=0?DYING:IDLE,0);if(getHealth()<=0)entityData.set(START,(int)level.getGameTime()-deathTicks);recovery=40;}
}
