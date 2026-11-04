package org.slavicmyths.sisters;

import java.util.EnumSet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Ground-bound Agneya with a telegraphed mace attack; no flight AI or natural spawn. */
public final class AgneyaBoss extends Monster {
    public static final int MACE_ATTACK_TICKS = 26;
    private static final EntityDataAccessor<Integer> ATTACK_STARTED = SynchedEntityData.defineId(AgneyaBoss.class, EntityDataSerializers.INT);
    private int swingTick = -1;
    private int cooldown;

    public AgneyaBoss(EntityType<? extends AgneyaBoss> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        setNoGravity(false);
    }

    @Override protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ATTACK_STARTED, -100000);
    }

    /** Elapsed ticks since the server started a real mace wind-up, interpolated for rendering. */
    public float attackPoseTicks(float partialTick) {
        float elapsed = (float)(level().getGameTime() - entityData.get(ATTACK_STARTED)) + Math.max(0F, Math.min(partialTick, 1F));
        return elapsed >= 0F && elapsed < MACE_ATTACK_TICKS ? elapsed : -1F;
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 300)
            .add(Attributes.MOVEMENT_SPEED, .27)
            .add(Attributes.ATTACK_DAMAGE, 14)
            .add(Attributes.ARMOR, 10)
            .add(Attributes.FOLLOW_RANGE, 32)
            .add(Attributes.KNOCKBACK_RESISTANCE, .65);
    }

    private double reachSq(LivingEntity target) {
        double reach = getBbWidth()*.5D + target.getBbWidth()*.5D + 1.65D;
        return reach*reach;
    }

    private void startMaceAttack() {
        if (swingTick >= 0 || cooldown > 0 || level().isClientSide) return;
        swingTick = 0;
        entityData.set(ATTACK_STARTED, (int)level().getGameTime());
        getNavigation().stop();
    }

    @Override public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (cooldown > 0) --cooldown;
        if (swingTick >= 0) {
            if (swingTick == 14) {
                LivingEntity target = getTarget();
                if (target != null && target.isAlive() && distanceToSqr(target) <= reachSq(target))
                    doHurtTarget(target);
            }
            if (++swingTick >= MACE_ATTACK_TICKS) {
                swingTick = -1;
                cooldown = 15;
            }
        }
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new GroundMaceGoal());
        goalSelector.addGoal(5, new RandomStrollGoal(this, .75));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private final class GroundMaceGoal extends Goal {
        GroundMaceGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return getTarget() != null && getTarget().isAlive(); }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void stop() { getNavigation().stop(); }
        @Override public void tick() {
            LivingEntity target = getTarget();
            if (target == null || !target.isAlive()) return;
            getLookControl().setLookAt(target, 30F, 30F);
            if (swingTick >= 0) {
                getNavigation().stop();
            } else if (distanceToSqr(target) > reachSq(target)) {
                getNavigation().moveTo(target, 1.05D);
            } else {
                getNavigation().stop();
                startMaceAttack();
            }
        }
    }
}