package org.slavicmyths.sisters;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class LedeyaBoss extends Monster {
    public static final int RELEASE_TICK = 14;
    public static final int CAST_TICKS = 26;
    private static final double RANGE = 24;
    private static final EntityDataAccessor<Integer> CAST_STARTED =
        SynchedEntityData.defineId(LedeyaBoss.class, EntityDataSerializers.INT);
    private int cooldown;

    public LedeyaBoss(EntityType<? extends LedeyaBoss> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 240)
            .add(Attributes.MOVEMENT_SPEED, .26)
            .add(Attributes.ARMOR, 6)
            .add(Attributes.FOLLOW_RANGE, 32)
            .add(Attributes.KNOCKBACK_RESISTANCE, .45);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CAST_STARTED, -1);
    }

    public float castTicks(float partialTick) {
        int start = entityData.get(CAST_STARTED);
        return start == -1 ? -1 : (int)level().getGameTime() - start + partialTick;
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new FrostCastGoal());
        goalSelector.addGoal(5, new RandomStrollGoal(this, .75));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private boolean canCastAt(LivingEntity target) {
        return target instanceof Player player && player.isAlive() && !player.isCreative()
            && !player.isSpectator() && distanceToSqr(player) <= RANGE * RANGE
            && getSensing().hasLineOfSight(player);
    }

    @Override public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (cooldown > 0) cooldown--;
        float elapsed = castTicks(0);
        LivingEntity target = getTarget();
        if (elapsed == RELEASE_TICK && isAlive() && onGround() && canCastAt(target)) {
            SisterBolt bolt = SistersContent.FROST_BOLT.get().create(level());
            if (bolt != null) {
                Vec3 origin = position().add(0, 3.6, 0);
                Vec3 aim = target.getEyePosition().subtract(origin);
                bolt.configure(this, 10);
                bolt.setPos(origin.x, origin.y, origin.z);
                bolt.shoot(aim.x, aim.y, aim.z, 1.15F, 0);
                if (level().addFreshEntity(bolt)) playSound(SoundEvents.SNOWBALL_THROW, 1, .75F);
            }
        }
        if (elapsed >= CAST_TICKS) {
            entityData.set(CAST_STARTED, -1);
            cooldown = 30;
        }
    }

    @Override protected SoundEvent getHurtSound(DamageSource damage) { return SoundEvents.GLASS_HIT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GLASS_BREAK; }
    @Override protected void playStepSound(BlockPos pos, BlockState state) {
        super.playStepSound(pos, state);
        playSound(SoundEvents.SNOW_STEP, .2F, .85F);
    }

    private final class FrostCastGoal extends Goal {
        FrostCastGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return getTarget() != null && getTarget().isAlive(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void stop() {
            getNavigation().stop();
            if (entityData.get(CAST_STARTED) != -1) {
                entityData.set(CAST_STARTED, -1);
                cooldown = 30;
            }
        }
        @Override public void tick() {
            LivingEntity target = getTarget();
            if (target == null) return;
            getLookControl().setLookAt(target, 30, 30);
            if (entityData.get(CAST_STARTED) != -1) {
                getNavigation().stop();
            } else if (canCastAt(target)) {
                getNavigation().stop();
                if (cooldown == 0 && onGround()) {
                    entityData.set(CAST_STARTED, (int)level().getGameTime());
                    playSound(SoundEvents.AMETHYST_BLOCK_CHIME, .85F, .8F);
                }
            } else if (tickCount % 10 == 0) {
                getNavigation().moveTo(target, 1);
            }
        }
    }
}
