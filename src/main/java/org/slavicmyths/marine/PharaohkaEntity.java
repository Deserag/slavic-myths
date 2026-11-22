package org.slavicmyths.marine;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;

/** Amphibious-surface visual, but water-only survival and combat. Server owns all damage. */
public final class PharaohkaEntity extends Monster {
    // Aquatic encounters may occupy water; retain block and entity obstruction checks.
    @Override public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader world) {
        return world.isUnobstructed(this) && world.noCollision(this);
    }

    public static final int IDLE=0, STRIKE=1, DASH=2, SUMMON=3, CURRENT=4, BLOCK=5, BASH=6;
    private static final EntityDataAccessor<Integer> ACTION =
        SynchedEntityData.defineId(PharaohkaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION_STARTED =
        SynchedEntityData.defineId(PharaohkaEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossBar = new org.slavicmyths.combat.CreatureBossBar(this,
        Component.translatable("entity.slavicmyths.pharaohka_leader"),
        BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private int dryTicks, meleeCooldown, dashCooldown=55, summonCooldown=170, currentCooldown=95, shieldCooldown=70;
    private int dashTicks, currentTicks, aiWander, strikeTicks, shieldTicks;
    // Tier V provisional balance: A4=18, A5=1.15*A4, A6=1.35*A4.
    public static final float BOSS_BASE_DAMAGE=18F;
    public static final float DASH_DAMAGE=BOSS_BASE_DAMAGE*1.15F;
    public static final float SHIELD_BASH_DAMAGE=BOSS_BASE_DAMAGE*1.35F;
    private boolean dashHit;
    private UUID summoner;
    private int minionLife;

    public PharaohkaEntity(EntityType<? extends PharaohkaEntity> type, Level level) {
        super(type, level);
        moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.045F, 0.18F, true);
        setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, 0);
        xpReward = 0;
    }

    public boolean leader() { return getType() == PharaohkaContent.LEADER.get(); }
    public static AttributeSupplier.Builder attributes(boolean leader) {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, leader ? 550.0 : 32.0)
            .add(Attributes.MOVEMENT_SPEED, leader ? .44 : .35)
            .add(Attributes.ATTACK_DAMAGE, leader ? BOSS_BASE_DAMAGE : 5.0)
            .add(Attributes.ARMOR, leader ? 12.0 : 3.0)
            .add(Attributes.FOLLOW_RANGE, leader ? 40.0 : 28.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, leader ? .65 : .15);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION, IDLE);
        builder.define(ACTION_STARTED, 0);
    }
    public int action() { return entityData.get(ACTION); }
    public int actionTicks() { return Math.max(0, (int)level().getGameTime()-entityData.get(ACTION_STARTED)); }
    private void animate(int action) {
        if (entityData.get(ACTION) != action) {
            entityData.set(ACTION, action);
            entityData.set(ACTION_STARTED, (int)level().getGameTime());
        }
    }

    @Override protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean inHabitat() {
        BlockPos pos=blockPosition();
        return isInWaterOrBubble()
            || level().getFluidState(pos).is(FluidTags.WATER)
            || level().getFluidState(pos.below()).is(FluidTags.WATER);
    }
    @Override public boolean removeWhenFarAway(double distance) { return !leader() && super.removeWhenFarAway(distance); }

    @Override public void travel(Vec3 input) {
        if (isEffectiveAi() && isInWater()) {
            moveRelative(leader() ? .085F : .075F, input);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(.89));
        } else {
            super.travel(input);
        }
    }

    @Override public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (leader()) bossBar.setProgress(getHealth()/getMaxHealth());
        boolean wet=inHabitat();
        setNoGravity(wet);
        if (!wet) {
            strikeTicks=0;
            shieldTicks=0;
            if (++dryTicks > 100) hurt(damageSources().dryOut(), getMaxHealth()+1F);
            getNavigation().stop();
            return;
        }
        dryTicks=0;
        if (summoner!=null) {
            if (--minionLife<=0) { discard(); return; }
            if (level() instanceof ServerLevel server && tickCount%10==0) {
                if (server.getEntity(summoner) instanceof PharaohkaEntity owner && owner.isAlive()) {
                    LivingEntity ownerTarget=owner.getTarget();
                    if (ownerTarget!=null && validTarget(ownerTarget) && getTarget()!=ownerTarget)
                        setTarget(ownerTarget);
                } else {
                    discard();
                    return;
                }
            }
        }
        if (isInWater() && getTarget()==null) setDeltaMovement(getDeltaMovement().add(0, .003, 0));
        if (meleeCooldown>0) meleeCooldown--;
        if (dashCooldown>0) dashCooldown--;
        if (summonCooldown>0) summonCooldown--;
        if (currentCooldown>0) currentCooldown--;
        if (shieldCooldown>0) shieldCooldown--;
        if (dashTicks>0) { dash(); return; }
        if (currentTicks>0) { currentPulse(); }
        LivingEntity target=getTarget();
        if (target != null && !validTarget(target)) {
            setTarget(null);
            target=null;
        }
        if (strikeTicks>0) {
            if (target!=null) followTarget(target);
            --strikeTicks;
            if (strikeTicks==3 && target!=null && hasLineOfSight(target)
                    && inMeleeRange(target,leader()?1.45:0.8))
                doHurtTarget(target);
            if (strikeTicks==0) animate(IDLE);
            return;
        }
        if (shieldTicks>0) {
            if (target!=null) followTarget(target);
            --shieldTicks;
            if (shieldTicks==4 && target!=null && hasLineOfSight(target)
                    && inMeleeRange(target,1.7)) {
                target.hurt(damageSources().mobAttack(this),SHIELD_BASH_DAMAGE);
                if (level() instanceof ServerLevel world)
                    world.sendParticles(ParticleTypes.BUBBLE,target.getX(),target.getY()+.8,target.getZ(),12,.55,.5,.55,.06);
                Vec3 away=target.position().subtract(position()).multiply(1,0,1);
                if (away.lengthSqr()>.01) {
                    Vec3 impulse=away.normalize();
                    target.push(impulse.x*1.1, .24, impulse.z*1.1);
                    target.hurtMarked=true;
                }
            }
            if (shieldTicks==0) animate(IDLE);
            return;
        }
        if (target==null) {
            if (--aiWander<=0) {
                aiWander=35+random.nextInt(55);
                BlockPos near=blockPosition().offset(random.nextInt(13)-6, random.nextInt(7)-3, random.nextInt(13)-6);
                if (level().getFluidState(near).is(FluidTags.WATER))
                    getNavigation().moveTo(near.getX()+.5,near.getY()+.5,near.getZ()+.5,1.0);
            }
            if (action()!=IDLE && actionTicks()>20) animate(IDLE);
            return;
        }

        followTarget(target);
        double distance=distanceToSqr(target);
        // Direct combat always takes priority over summons and area control.
        if (meleeCooldown==0 && inMeleeRange(target,leader()?1.45:0.8)
                && hasLineOfSight(target)) {
            animate(STRIKE);
            meleeCooldown=leader()?18:22;
            strikeTicks=10;
            return;
        }
        if (leader() && shieldCooldown==0 && inMeleeRange(target,1.7)
                && hasLineOfSight(target)) {
            shieldTicks=12;
            shieldCooldown=80+random.nextInt(30);
            animate(BASH);
            playSound(SoundEvents.SHIELD_BLOCK,.85F,.65F);
            return;
        }
        if (leader() && dashCooldown==0 && isTargetInWater(target)
                && distance>12 && distance<256) {
            beginDash(target);
            return;
        }
        if (leader() && summonCooldown==0 && distance<400) {
            summon();
            return;
        }
        if (leader() && currentCooldown==0 && distance<144) {
            currentTicks=20;
            currentCooldown=125;
            animate(CURRENT);
            playSound(SoundEvents.CONDUIT_ATTACK_TARGET,1.1F,.75F);
        } else if ((action()==SUMMON || action()==BLOCK || action()==CURRENT)
                && actionTicks()>20) animate(IDLE);
    }

    private boolean validTarget(LivingEntity candidate) {
        return candidate.isAlive() && !candidate.isSpectator()
            && !(candidate instanceof Player player && player.isCreative())
            && (candidate == getLastHurtByMob() && org.slavicmyths.combat.CombatTargeting.canAttack(this,candidate,org.slavicmyths.combat.CombatTargeting.Context.ACQUIRE)
                || isTargetInWater(candidate)
                || candidate instanceof Player
                    && distanceToSqr(candidate)<144
                    && Math.abs(candidate.getY()-getY())<5);
    }

    private boolean inMeleeRange(LivingEntity target,double reach) {
        return getBoundingBox().inflate(reach,0.65,reach).intersects(target.getBoundingBox());
    }

    private void followTarget(LivingEntity target) {
        getLookControl().setLookAt(target,35,35);
        boolean underwaterTarget=isTargetInWater(target);
        if (underwaterTarget && (tickCount%8==0 || getNavigation().isDone())) {
            getNavigation().moveTo(target,leader()?1.4:1.2);
        } else if (!underwaterTarget && !getNavigation().isDone()) {
            // Follow from water at the shoreline, not onto the dry dock.
            getNavigation().stop();
        }
        double targetY=underwaterTarget
            ? Mth.clamp(target.getY()+target.getBbHeight()*.45,getY()-1.7,getY()+1.7)
            : Mth.clamp(target.getY()-1.0,getY()-1.2,getY()+1.15);
        getMoveControl().setWantedPosition(target.getX(),targetY,target.getZ(),
            leader()?1.4:1.2);
        // Water pathfinding alone can stall at the surface or next to a dock.
        // Apply bounded, collision-aware steering when there is a visible route.
        if (isInWater() && hasLineOfSight(target) && !inMeleeRange(target,0.55)) {
            Vec3 toward=new Vec3(target.getX()-getX(),targetY-getY(),target.getZ()-getZ());
            if (toward.lengthSqr()>.04) {
                Vec3 velocity=getDeltaMovement().scale(.78)
                    .add(toward.normalize().scale(leader()?.07:.055));
                double max=leader()?.35:.28;
                if (velocity.lengthSqr()>max*max) velocity=velocity.normalize().scale(max);
                BlockPos next=BlockPos.containing(getX()+velocity.x*2.0,
                    getY()+velocity.y*2.0,getZ()+velocity.z*2.0);
                if (level().getFluidState(next).is(FluidTags.WATER)
                        || level().getFluidState(next.below()).is(FluidTags.WATER)) {
                    setDeltaMovement(org.slavicmyths.combat.LargeMobClearance.steer(this,velocity));
                    hasImpulse=true;
                }
            }
        }
    }

    private boolean isTargetInWater(LivingEntity target) {
        BlockPos pos=target.blockPosition();
        return target.isInWaterOrBubble() || level().getFluidState(pos.below()).is(FluidTags.WATER);
    }

    private void beginDash(LivingEntity target) {
        // A visible windup (7 ticks) precedes the fast swimming charge.
        dashTicks=24;
        dashCooldown=65+random.nextInt(20);
        dashHit=false;
        animate(DASH);
        playSound(SoundEvents.TRIDENT_RIPTIDE_1.value(),1.0F,.85F);
        if (level() instanceof ServerLevel world)
            world.sendParticles(ParticleTypes.BUBBLE,getX(),getY()+1,getZ(),16,.7,.8,.7,.08);
        getNavigation().stop();
    }

    private void dash() {
        LivingEntity target=getTarget();
        if (target==null || !isInWater() || !isTargetInWater(target)) {dashTicks=0;animate(IDLE);return;}
        getLookControl().setLookAt(target,30,30);
        if (dashTicks>17) {
            setDeltaMovement(getDeltaMovement().scale(.8));
            --dashTicks;
            return;
        }
        Vec3 direction=target.getEyePosition().subtract(getEyePosition());
        if (direction.lengthSqr()>.1) {
            Vec3 impulse=direction.normalize().scale(leader()?.57:.4);
            setDeltaMovement(org.slavicmyths.combat.LargeMobClearance.steer(this,getDeltaMovement().scale(.24).add(impulse)));
            hasImpulse=true;
        }
        if (!dashHit && inMeleeRange(target,1.5) && hasLineOfSight(target)) {
            target.hurt(damageSources().mobAttack(this), DASH_DAMAGE);
            target.push(target.getX()-getX(), .12, target.getZ()-getZ());
            dashHit=true;
        }
        if (--dashTicks<=0) animate(IDLE);
    }

    private void summon() {
        if (!(level() instanceof ServerLevel world)) return;
        List<PharaohkaEntity> allies=world.getEntitiesOfClass(PharaohkaEntity.class,
            getBoundingBox().inflate(36), entity->!entity.leader() && getUUID().equals(entity.summoner) && entity.isAlive());
        summonCooldown=195+random.nextInt(45);
        if (allies.size()>=4) return;
        animate(SUMMON);
        playSound(SoundEvents.CONDUIT_ACTIVATE,.95F,.8F);
        world.sendParticles(ParticleTypes.BUBBLE,getX(),getY()+1.2,getZ(),20,1.4,1.4,1.4,.04);
        int count=Math.min(2,4-allies.size());
        for (int i=0;i<count;i++) {
            PharaohkaEntity minion=PharaohkaContent.PHARAOHKA.get().create(world);
            if (minion==null) continue;
            for (int attempt=0;attempt<12;attempt++) {
                double ox=(random.nextDouble()-.5)*9, oz=(random.nextDouble()-.5)*9;
                double oy=(random.nextDouble()-.5)*3;
                minion.setPos(getX()+ox,getY()+oy,getZ()+oz);
                if (!world.hasChunkAt(minion.blockPosition()) ||
                    !world.getFluidState(minion.blockPosition()).is(FluidTags.WATER) ||
                    !world.noCollision(minion)) continue;
                minion.summoner=getUUID();
                minion.minionLife=1200;
                if (world.addFreshEntity(minion)) {
                    minion.setTarget(getTarget());
                    minion.setPersistenceRequired();
                    break;
                }
            }
        }
    }

    private void currentPulse() {
        if (currentTicks-- <= 0) return;
        if (tickCount % 4 != 0) return;
        if (tickCount%8==0 && level() instanceof ServerLevel server)
            server.sendParticles(ParticleTypes.BUBBLE,getX(),getY()+1,getZ(),12,1.6,1.3,1.6,.045);
        for (Player player:level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(9),
                p->!p.isCreative()&&!p.isSpectator()&&isTargetInWater(p)&&hasLineOfSight(p))) {
            Vec3 delta=position().subtract(player.position());
            if (delta.lengthSqr()>.3) {
                player.setDeltaMovement(player.getDeltaMovement().add(delta.normalize().scale(.24)));
                player.hurtMarked=true;
            }
        }
        if (currentTicks<=0) animate(IDLE);
    }

    @Override public boolean hurt(DamageSource source, float damage) {
        if (leader() && isInWater() && source.is(DamageTypeTags.IS_PROJECTILE)) {
            animate(BLOCK);
            damage*=.50F;
        }
        return super.hurt(source, damage);
    }

    @Override public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (leader()) bossBar.addPlayer(player);
    }
    @Override public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DryTicks",dryTicks);
        tag.putInt("MeleeCooldown",meleeCooldown);
        tag.putInt("ShieldCooldown",shieldCooldown);
        if(summoner!=null) {tag.putUUID("Summoner",summoner);tag.putInt("MinionLife",minionLife);}
        tag.putInt("DashCooldown",dashCooldown);
        tag.putInt("SummonCooldown",summonCooldown);
        tag.putInt("CurrentCooldown",currentCooldown);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        dryTicks=tag.getInt("DryTicks");
        meleeCooldown=Math.max(0,tag.getInt("MeleeCooldown"));
        shieldCooldown=Math.max(0,tag.getInt("ShieldCooldown"));
        summoner=tag.hasUUID("Summoner")?tag.getUUID("Summoner"):null;
        minionLife=Math.max(0,tag.getInt("MinionLife"));
        strikeTicks=0;
        shieldTicks=0;
        dashCooldown=Math.max(0,tag.getInt("DashCooldown"));
        summonCooldown=Math.max(0,tag.getInt("SummonCooldown"));
        currentCooldown=Math.max(0,tag.getInt("CurrentCooldown"));
        bossBar.setProgress(getHealth()/getMaxHealth());
    }
    @Override protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable() {
        return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;
    }
}