package org.slavicmyths.sisters;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.slavicmyths.combat.CreatureHealthBalance;

/** First cooperative encounter. No ordinary health threshold phases; rage only on a recorded death. */
public final class SisterBoss extends Monster implements RangedAttackMob {
    public static final int IDLE=0,HEAVY=1,SLAM=2,WALL=3,PULL=4,SPIKES=5,FEVER=6,FLASH=7,SPIRITS=8,ZONE=9,EVADE=10;
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(SisterBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FRAME=SynchedEntityData.defineId(SisterBoss.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> RAGE=SynchedEntityData.defineId(SisterBoss.class,EntityDataSerializers.BOOLEAN);
    private final ServerBossEvent bar;
    private UUID encounter,partner;
    private int cooldown=70,attackNumber,actionLength;
    private boolean impactDone,deathRecorded;
    private Vec3 lockedTarget=Vec3.ZERO;

    public SisterBoss(EntityType<? extends SisterBoss> type,Level level) {
        super(type,level);
        if (isTryaseya()) {
            setNoGravity(true);
            moveControl = new FlyingMoveControl(this,20,true);
        } else {
            setNoGravity(false);
        }
        bar = new ServerBossEvent(Component.literal(isTryaseya()?"Трясея":"Гнетея"),org.slavicmyths.combat.BossBarPalette.colorFor(getType(),isTryaseya()?BossEvent.BossBarColor.PURPLE:BossEvent.BossBarColor.RED),BossEvent.BossBarOverlay.PROGRESS);
        xpReward=0; // Rewards intentionally deferred to a separate approved patch.
        setPersistenceRequired();
        setHealth(getMaxHealth());
    }
    @Override protected SoundEvent getAmbientSound() { return isTryaseya() ? TryaseyaSounds.AMBIENT.get() : super.getAmbientSound(); }
    @Override protected SoundEvent getHurtSound(DamageSource damage) { return isTryaseya() ? TryaseyaSounds.HURT.get() : super.getHurtSound(damage); }
    @Override protected SoundEvent getDeathSound() { return isTryaseya() ? TryaseyaSounds.DEATH.get() : super.getDeathSound(); }
    public boolean isTryaseya() { return getType()==SistersContent.TRYASEYA.get(); }
    public boolean enraged() { return entityData.get(RAGE); }
    public int action() { return entityData.get(ACTION); }
    public int actionFrame() { return entityData.get(FRAME); }
    public UUID encounterId() { return encounter; }
    public UUID partnerId() { return partner; }
    public void link(UUID encounterId,UUID partnerId) { encounter=encounterId;partner=partnerId; }
    @Override public float getMaxHealth() { return (float)CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH)); }
    public static AttributeSupplier.Builder attributes(boolean tryaseya) {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH,tryaseya?900:1400).add(Attributes.MOVEMENT_SPEED,tryaseya?.32:.205).add(Attributes.FLYING_SPEED,tryaseya?.5:.08).add(Attributes.FOLLOW_RANGE,48).add(Attributes.ATTACK_DAMAGE,tryaseya?7:19).add(Attributes.ARMOR,tryaseya?5:17).add(Attributes.KNOCKBACK_RESISTANCE,tryaseya?.25:.85);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION,0);builder.define(FRAME,0);builder.define(RAGE,false);
    }
    @Override protected PathNavigation createNavigation(Level world) {
        if (isTryaseya()) {
            FlyingPathNavigation nav=new FlyingPathNavigation(this,world);
            nav.setCanFloat(true);return nav;
        }
        return super.createNavigation(world);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        if (isTryaseya()) goalSelector.addGoal(2,new RangedAttackGoal(this,1.12,42,20F));
        else goalSelector.addGoal(2,new MeleeAttackGoal(this,1.25,true));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,24));
        goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true,p -> p instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }
    @Override public void startSeenByPlayer(ServerPlayer player) {super.startSeenByPlayer(player);bar.addPlayer(player);}
    @Override public void stopSeenByPlayer(ServerPlayer player) {super.stopSeenByPlayer(player);bar.removePlayer(player);}
    @Override public boolean removeWhenFarAway(double distance) {return false;}

    private SisterBoss loadedPartner(ServerLevel world) {
        if (partner==null) return null;
        return world.getEntity(partner) instanceof SisterBoss sister && sister.isAlive() && encounter!=null && encounter.equals(sister.encounter) ? sister : null;
    }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();
        if (!(level() instanceof ServerLevel world) || !isAlive()) return;
        if (!isTryaseya() && isNoGravity()) setNoGravity(false);
        if (encounter!=null && !enraged() && SistersLedger.get(world).partnerDied(encounter,isTryaseya())) { entityData.set(RAGE,true); if (isTryaseya()) world.playSound(null,blockPosition(),TryaseyaSounds.ENRAGE.get(),SoundSource.HOSTILE,1.35F,.86F); }
        bar.setProgress(Math.clamp(getHealth()/Math.max(1,getMaxHealth()),0,1));
        SisterBoss ally=loadedPartner(world);
        Player victim=getTarget() instanceof Player p && p.isAlive() && !p.isCreative() && !p.isSpectator() ? p : null;
        if (victim==null) { if (action()!=IDLE) resetAttack();return; }
        if (ally!=null && ally.getTarget() instanceof Player p && p.isAlive() && distanceToSqr(p)<distanceToSqr(victim)) victim=p;
        if (action()!=IDLE) {
            getNavigation().stop();
            int frame=actionFrame()+1;
            entityData.set(FRAME,frame);
            telegraph(world,frame);
            if (!impactDone && frame==impactFrame()) { impactDone=true;resolve(world,victim,ally); if (isTryaseya()) world.playSound(null,blockPosition(),TryaseyaSounds.IMPACT.get(),SoundSource.HOSTILE,1.3F,.94F); }
            if (frame>=actionLength) { resetAttack();cooldown=enraged()?34:55; }
            return;
        }
        double d=distanceToSqr(victim);
        if (isTryaseya()) {
            // RangedAttackGoal owns approach/aiming; retreat at 12 blocks and
            // keep a preferred annulus of 12-20 blocks without melee pursuit.
            double horizontal=position().subtract(victim.position()).multiply(1,0,1).length();
            if (horizontal<12) {
                Vec3 away=position().subtract(victim.position()).multiply(1,0,1).normalize();
                if (away.lengthSqr()>.001) {
                    Vec3 retreat=position().add(away.scale(6));
                    if (world.hasChunkAt(BlockPos.containing(retreat))) getNavigation().moveTo(retreat.x,Math.max(victim.getY()+1.5,getY()),retreat.z,1.4);
                }
            } else if (horizontal<=20) getNavigation().stop();
        } else {
            // MeleeAttackGoal handles charging and melee hits.
            if (ally!=null && ally.isTryaseya() && ally.getTarget() instanceof Player threatened && valid(threatened) && distanceToSqr(threatened)<900) setTarget(threatened);
        }
        if (cooldown-->0 || d>900 || !getSensing().hasLineOfSight(victim)) return;
        int next = isTryaseya()?new int[]{FEVER,SPIRITS,FLASH,ZONE,EVADE}[attackNumber++%5]:new int[]{HEAVY,WALL,SLAM,PULL,SPIKES}[attackNumber++%5];
        if (isTryaseya() && ally!=null && ally.action()==WALL) next=FEVER;
        if (!isTryaseya() && ally!=null && ally.action()==SPIRITS) next=HEAVY;
        if (!isTryaseya() && next==PULL && ally!=null && ally.action()==FLASH) next=PULL;
        begin(next,victim);
    }
    @Override public void performRangedAttack(LivingEntity target,float distanceFactor) {
        if (!isTryaseya() || !(level() instanceof ServerLevel world) || !target.isAlive() || action()!=IDLE || !clearSight(target)) return;
        SisterBolt bolt=SistersContent.BOLT.get().create(world);
        if (bolt==null) return;
        Vec3 start=lanternOrigin();
        Vec3 direction=target.getEyePosition().subtract(start).normalize();
        bolt.setPos(start.x,start.y,start.z);
        bolt.configure(this,(enraged()?9F:7F));
        bolt.shoot(direction.x,direction.y,direction.z,1.25F,1.2F);
        world.addFreshEntity(bolt);
    }
    private void begin(int next,Player victim) {
        entityData.set(ACTION,next);
        if (isTryaseya() && !level().isClientSide()) level().playSound(null,blockPosition(),TryaseyaSounds.CAST.get(),SoundSource.HOSTILE,1.2F,.94F);
        entityData.set(FRAME,0);
        lockedTarget=victim.position();
        impactDone=false;
        actionLength=next==WALL||next==SPIRITS?54:next==SLAM?46:next==EVADE?34:42;
        getNavigation().stop();
        getLookControl().setLookAt(victim,30,30);
    }
    private int impactFrame() {return action()==WALL?30:action()==SPIRITS?27:action()==SLAM?32:action()==EVADE?20:26;}
    private void resetAttack() {entityData.set(ACTION,IDLE);entityData.set(FRAME,0);actionLength=0;impactDone=false;}
    private void telegraph(ServerLevel world,int frame) {
        if (frame%6!=0 || frame>impactFrame()) return;
        Vector3f hue=isTryaseya()?new Vector3f(.55F,.19F,.78F):new Vector3f(.72F,.65F,.53F);
        world.sendParticles(new DustParticleOptions(hue,1.5F),getX(),getY()+Math.min(3,getBbHeight()*.55),getZ(),5,.7,.6,.7,.015);
        if (action()==WALL || action()==SPIKES || action()==ZONE) world.sendParticles(new DustParticleOptions(hue,1.1F),lockedTarget.x,lockedTarget.y+.07,lockedTarget.z,7,1.3,.02,1.3,.01);
    }
    private boolean clearSight(LivingEntity target) {return getSensing().hasLineOfSight(target) && level().clip(new ClipContext(getEyePosition(),target.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS;}
    private boolean valid(Player p) {return p.isAlive() && !p.isCreative() && !p.isSpectator();}
    private void resolve(ServerLevel world,Player target,SisterBoss ally) {
        float power=enraged()?1.22F:1F;
        switch (action()) {
            case HEAVY -> {
                Vec3 front=Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();
                for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(5.0,2.5,5.0),this::valid)) {
                    Vec3 delta=p.position().subtract(position()).multiply(1,0,1);
                    if(delta.lengthSqr()>24 || delta.lengthSqr()<.1 || front.dot(delta.normalize())<-.1 || !clearSight(p)) continue;
                    if(p.hurt(damageSources().mobAttack(this),19*power)) p.push(delta.normalize().x*.65,.25,delta.normalize().z*.65);
                }
            }
            case SLAM -> {
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,getX(),getY()+.2,getZ(),38,3,.25,3,.14);
                for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(5,1.5,5),this::valid)) {
                    if(Math.abs(p.getY()-getY())>2.1 || !clearSight(p))continue;
                    if(p.hurt(damageSources().mobAttack(this),12*power)) p.push((p.getX()-getX())*.14,.32,(p.getZ()-getZ())*.14);
                }
            }
            case WALL -> createWall(world,target);
            case PULL -> {
                if(distanceToSqr(target)>100 || !clearSight(target) || target.isBlocking())break;
                Vec3 towards=position().subtract(target.position()).multiply(1,0,1).normalize().scale(.85);
                target.setDeltaMovement(target.getDeltaMovement().add(towards.x,.14,towards.z));
                target.hurtMarked=true;
            }
            case SPIKES -> {
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,lockedTarget.x,lockedTarget.y+.2,lockedTarget.z,25,1,.65,1,.05);
                for(Player p:world.getEntitiesOfClass(Player.class,new AABB(lockedTarget,lockedTarget).inflate(1.5,2,1.5),this::valid)) if(clearSight(p))p.hurt(damageSources().mobAttack(this),8*power);
            }
            case FEVER -> {
                SisterBolt bolt=SistersContent.BOLT.get().create(world);
                if (bolt==null)break;
                Vec3 start=lanternOrigin();
                Vec3 end=target.getEyePosition().subtract(start).normalize();
                bolt.setPos(start.x,start.y,start.z);
                bolt.configure(this,7*power);
                bolt.shoot(end.x,end.y,end.z,1.1F,.5F);
                world.addFreshEntity(bolt);
            }
            case FLASH -> {
                world.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,getX(),getY()+2.0,getZ(),48,2.2,1.5,2.2,.1);
                for(Player p:world.getEntitiesOfClass(Player.class,getBoundingBox().inflate(4,2,4),this::valid)) {
                    if(!clearSight(p))continue;
                    if(p.hurt(damageSources().mobAttack(this),6*power))p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,38,0));
                }
            }
            case SPIRITS -> summonSpirits(world,target);
            case ZONE -> {
                SistersZone zone=SistersContent.ZONE.get().create(world);
                if(zone!=null){zone.setPos(lockedTarget.x,lockedTarget.y+.05,lockedTarget.z);zone.bind(this);world.addFreshEntity(zone);}
            }
            case EVADE -> spectralEvasion(world,target,ally);
            default -> { }
        }
    }
    private void createWall(ServerLevel world,Player target) {
        if (world.getEntitiesOfClass(BoneBarrier.class,getBoundingBox().inflate(35)).size()>=10)return;
        Vec3 backwards=target.getLookAngle().multiply(-1,0,-1).normalize();
        if (backwards.lengthSqr()<.01) backwards=target.position().subtract(position()).multiply(1,0,1).normalize();
        Vec3 center=target.position().add(backwards.scale(3.0));
        Vec3 side=new Vec3(-backwards.z,0,backwards.x);
        for (int i=-2;i<=2;i++) {
            Vec3 site=center.add(side.scale(i*.96));
            BlockPos ground=BlockPos.containing(site);
            if(!world.hasChunkAt(ground) || !world.getBlockState(ground).getCollisionShape(world,ground).isEmpty())continue;
            BoneBarrier part=SistersContent.BARRIER.get().create(world);
            if(part==null)continue;
            part.setPos(site.x,site.y,site.z);
            if(!world.noCollision(part) || !world.getEntitiesOfClass(Player.class,part.getBoundingBox().inflate(.3)).isEmpty())continue;
            part.bind(this,Math.abs(i)*2);
            world.addFreshEntity(part);
        }
    }
    /** Corresponds to the swinging modeled lantern attached to the right hand. */
    private Vec3 lanternOrigin() {
        Vec3 right=Vec3.directionFromRotation(0,getYRot()+90).multiply(1,0,1).normalize();
        Vec3 front=Vec3.directionFromRotation(0,getYRot()).multiply(1,0,1).normalize();
        return position().add(right.scale(.75)).add(front.scale(.35)).add(0,1.35,0);
    }
    private void summonSpirits(ServerLevel world,Player target) {
        int active=world.getEntitiesOfClass(FeverSpirit.class,getBoundingBox().inflate(36),e -> getUUID().equals(e.ownerId())).size();
        for(int i=0;i<Math.min(enraged()?3:2,6-active);i++) {
            FeverSpirit spirit=SistersContent.SPIRIT.get().create(world);
            if(spirit==null)break;
            Vec3 spawn=lanternOrigin().add(Math.cos(i*2.3)*.35,-.25,Math.sin(i*2.3)*.35);
            spirit.setPos(spawn.x,spawn.y,spawn.z);
            if(!world.noCollision(spirit))continue;
            spirit.bind(this,i);spirit.setTarget(target);world.addFreshEntity(spirit);
        }
    }
    private void evadeMovement(ServerLevel world,Player target,SisterBoss ally) {
        if(ally==null)return;
        Vec3 direction=position().subtract(target.position()).multiply(1,0,1).normalize();
        Vec3 offset=position().add(direction.scale(.18));
        if(world.hasChunkAt(BlockPos.containing(offset)) && world.noCollision(this,getBoundingBox().move(offset.subtract(position())))) setDeltaMovement(direction.scale(.18).add(0,.02,0));
    }
    private void spectralEvasion(ServerLevel world,Player target,SisterBoss ally) {
        if(ally==null)return;
        Vec3 axis=position().subtract(target.position()).multiply(1,0,1).normalize();
        Vec3 candidate=ally.position().add(axis.scale(5)).add(0,1.5,0);
        if(!world.hasChunkAt(BlockPos.containing(candidate)))return;
        if(!world.noCollision(this,getBoundingBox().move(candidate.subtract(position()))))return;
        if(world.clip(new ClipContext(position().add(0,1,0),candidate.add(0,1,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,this)).getType()!=HitResult.Type.MISS)return;
        world.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,getX(),getY()+1,getZ(),36,.6,1,.6,.09);
        teleportTo(candidate.x,candidate.y,candidate.z);
        world.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH,getX(),getY()+1,getZ(),36,.6,1,.6,.09);
    }
    @Override public void die(DamageSource source) {
        if(!deathRecorded && level() instanceof ServerLevel world) {deathRecorded=true;SistersLedger.get(world).recordDeath(encounter,isTryaseya());}
        super.die(source);
        bar.removeAllPlayers();
    }
    @Override public void remove(RemovalReason reason) {if(reason==RemovalReason.KILLED||reason==RemovalReason.DISCARDED)bar.removeAllPlayers();super.remove(reason);}
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if(encounter!=null)tag.putUUID("SistersEncounter",encounter);
        if(partner!=null)tag.putUUID("SistersPartner",partner);
        tag.putBoolean("SistersRage",enraged());tag.putInt("SistersAction",action());tag.putInt("SistersFrame",actionFrame());
        tag.putInt("SistersLength",actionLength);tag.putInt("SistersCooldown",cooldown);tag.putInt("SistersCounter",attackNumber);
        tag.putBoolean("SistersHit",impactDone);tag.putBoolean("SistersDeath",deathRecorded);
        tag.putDouble("SistersTargetX",lockedTarget.x);tag.putDouble("SistersTargetY",lockedTarget.y);tag.putDouble("SistersTargetZ",lockedTarget.z);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setNoGravity(isTryaseya());
        encounter=tag.hasUUID("SistersEncounter")?tag.getUUID("SistersEncounter"):null;
        partner=tag.hasUUID("SistersPartner")?tag.getUUID("SistersPartner"):null;
        entityData.set(RAGE,tag.getBoolean("SistersRage"));
        entityData.set(ACTION,Math.clamp(tag.getInt("SistersAction"),0,10));
        entityData.set(FRAME,Math.clamp(tag.getInt("SistersFrame"),0,54));
        actionLength=Math.clamp(tag.getInt("SistersLength"),0,54);
        cooldown=Math.clamp(tag.getInt("SistersCooldown"),0,200);
        attackNumber=Math.max(0,tag.getInt("SistersCounter"));impactDone=tag.getBoolean("SistersHit");deathRecorded=tag.getBoolean("SistersDeath");
        lockedTarget=new Vec3(tag.getDouble("SistersTargetX"),tag.getDouble("SistersTargetY"),tag.getDouble("SistersTargetZ"));
        if(action()==IDLE || actionLength==0)resetAttack();
    }
}
