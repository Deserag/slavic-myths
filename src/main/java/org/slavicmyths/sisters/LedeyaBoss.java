package org.slavicmyths.sisters;

import java.util.EnumSet;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class LedeyaBoss extends Monster {
    public enum Attack {
        SHOT(28), RAIN(49), RAPID(44);

        public final int duration;
        Attack(int duration) { this.duration = duration; }

        public float draw(float tick) {
            if (tick < 0 || tick >= duration) return 0;
            if (this == RAPID) {
                if (tick < 10) return tick / 10;
                if (tick >= 34) return Math.max(0, 1 - (tick - 34) / 8);
                return (tick - 10) % 6 / 6;
            }
            int release = this == RAIN ? 18 : 16;
            return tick < release ? tick / release : Math.max(0, 1 - (tick - release) / 8);
        }
    }

    private static final double RANGE = 24;
    private static final EntityDataAccessor<Integer> ATTACK_STARTED =
        SynchedEntityData.defineId(LedeyaBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_KIND =
        SynchedEntityData.defineId(LedeyaBoss.class, EntityDataSerializers.INT);
    private int cooldown, turns;
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.slavicmyths.ledeya"),
        org.slavicmyths.combat.BossBarPalette.colorFor(getType(),BossEvent.BossBarColor.BLUE),BossEvent.BossBarOverlay.PROGRESS);
    @Override public void startSeenByPlayer(ServerPlayer player){super.startSeenByPlayer(player);bossBar.addPlayer(player);}
    @Override public void stopSeenByPlayer(ServerPlayer player){super.stopSeenByPlayer(player);bossBar.removePlayer(player);}
    @Override public void onRemovedFromLevel(){bossBar.removeAllPlayers();super.onRemovedFromLevel();}

    private Vec3 rainCenter;

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
        builder.define(ATTACK_STARTED, -1);
        builder.define(ATTACK_KIND, Attack.SHOT.ordinal());
    }

    public Attack attack() { return Attack.values()[entityData.get(ATTACK_KIND)]; }

    public float attackTicks(float partialTick) {
        int start = entityData.get(ATTACK_STARTED);
        return start == -1 ? -1 : (int)level().getGameTime() - start + partialTick;
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new BowGoal());
        goalSelector.addGoal(5, new RandomStrollGoal(this, .75));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    private boolean canShoot(LivingEntity target) {
        return target instanceof Player player && player.isAlive() && !player.isCreative()
            && !player.isSpectator() && distanceToSqr(player) <= RANGE * RANGE
            && getSensing().hasLineOfSight(player);
    }

    private Arrow arrow(Vec3 origin, double damage, boolean tipped) {
        ItemStack stack = new ItemStack(tipped ? Items.TIPPED_ARROW : Items.ARROW);
        if (tipped) {
            MobEffectInstance effect = switch (getRandom().nextInt(4)) {
                case 0 -> new MobEffectInstance(MobEffects.POISON, 90);
                case 1 -> new MobEffectInstance(MobEffects.WITHER, 65);
                case 2 -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 90, 1);
                default -> new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 70, 1);
            };
            stack.set(DataComponents.POTION_CONTENTS, new PotionContents(
                Optional.empty(), Optional.empty(), List.of(effect)));
        }
        Arrow arrow = new Arrow(level(), this, stack, new ItemStack(Items.BOW));
        arrow.setPos(origin.x, origin.y, origin.z);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.setBaseDamage(damage);
        return arrow;
    }

    private void fireAt(LivingEntity target, boolean rapid) {
        Vec3 origin = position().add(0, 3.55, 0);
        Vec3 aim = target.getEyePosition().subtract(origin);
        double lift = aim.horizontalDistance() * .11;
        Arrow arrow = arrow(origin, rapid ? 1.4 : 2.6, true);
        arrow.shoot(aim.x, aim.y + lift, aim.z, rapid ? 2.1F : 2.4F, rapid ? 3F : .7F);
        if (level().addFreshEntity(arrow)) playSound(SoundEvents.ARROW_SHOOT, 1F, rapid ? 1.22F : .9F);
    }

    private boolean clearColumn(Vec3 bottom, Vec3 top) {
        return level().hasChunkAt(BlockPos.containing(top))
            && level().clip(new ClipContext(bottom, top, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }

    private void warnRain(ServerLevel world) {
        for (int i = 0; i < 12; i++) {
            double angle = i * Math.PI / 6;
            world.sendParticles(ParticleTypes.SNOWFLAKE, rainCenter.x + 2.3 * Math.cos(angle),
                rainCenter.y + .1, rainCenter.z + 2.3 * Math.sin(angle), 1, 0, 0, 0, 0);
        }
    }

    private void releaseRain(ServerLevel world) {
        for (int i = 0; i < 9; i++) {
            double angle = i * Math.PI / 4;
            double radius = i == 8 ? 0 : 1.75;
            Vec3 floor = rainCenter.add(radius * Math.cos(angle), 0, radius * Math.sin(angle));
            Vec3 origin = floor.add(0, 8, 0);
            if (!clearColumn(floor, origin)) continue;
            Arrow arrow = arrow(origin, 1.9, true);
            arrow.shoot(0, -1, 0, 1.25F, 0);
            world.addFreshEntity(arrow);
        }
        world.playSound(null, BlockPos.containing(rainCenter), SoundEvents.ARROW_SHOOT, getSoundSource(), 1F, .78F);
    }

    @Override public void aiStep() {
        super.aiStep();
        if(!level().isClientSide)bossBar.setProgress(Math.clamp(getHealth()/Math.max(1F,getMaxHealth()),0F,1F));
        if (!(level() instanceof ServerLevel world)) return;
        if (cooldown > 0) cooldown--;
        int tick = (int)attackTicks(0);
        if (tick < 0) return;
        Attack attack = attack();
        LivingEntity target = getTarget();
        if (attack == Attack.RAIN && rainCenter != null && isAlive()) {
            if (tick < 33 && tick % 4 == 0) warnRain(world);
            if (tick == 18) {
                Vec3 origin = position().add(0, 3.55, 0);
                if (onGround() && canShoot(target) && clearColumn(origin, origin.add(0, 6, 0))) {
                    for (int i = -1; i <= 1; i++) {
                        Arrow signal = arrow(origin, 0, false);
                        signal.shoot(i * .13, 1, 0, 1.15F, 0);
                        if (world.addFreshEntity(signal)) signals.add(signal);
                    }
                    playSound(SoundEvents.ARROW_SHOOT, 1F, .82F);
                } else rainCenter = null;
            }
            if (tick == 33 && !signals.isEmpty()) releaseRain(world);
        } else if (isAlive() && onGround() && canShoot(target)) {
            if (attack == Attack.SHOT && tick == 16) fireAt(target, false);
            if (attack == Attack.RAPID && tick >= 10 && tick <= 34 && (tick - 10) % 6 == 0
                && distanceToSqr(target) >= 8 * 8 && distanceToSqr(target) <= 18 * 18) {
                fireAt(target, true);
            }
        }
        if (tick >= 33) clearSignals();
        if (tick >= attack.duration) finishAttack();
    }

    private final java.util.ArrayList<Arrow> signals = new java.util.ArrayList<>(3);

    private void clearSignals() {
        signals.forEach(Arrow::discard);
        signals.clear();
    }

    private void finishAttack() {
        clearSignals();
        entityData.set(ATTACK_STARTED, -1);
        rainCenter = null;
        cooldown = 32;
    }

    @Override protected SoundEvent getHurtSound(DamageSource damage) { return SoundEvents.GLASS_HIT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GLASS_BREAK; }
    @Override protected void playStepSound(BlockPos pos, BlockState state) {
        super.playStepSound(pos, state);
        playSound(SoundEvents.SNOW_STEP, .2F, .85F);
    }

    private final class BowGoal extends Goal {
        BowGoal() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return getTarget() != null && getTarget().isAlive(); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void stop() {
            getNavigation().stop();
            if (attackTicks(0) >= 0) finishAttack();
        }
        @Override public void tick() {
            LivingEntity target = getTarget();
            if (target == null) return;
            getLookControl().setLookAt(target, 30, 30);
            if (attackTicks(0) >= 0) {
                getNavigation().stop();
            } else if (canShoot(target)) {
                getNavigation().stop();
                if (cooldown == 0 && onGround()) {
                    Attack next = Attack.SHOT;
                    double rangeSq = distanceToSqr(target);
                    Vec3 floor = target.position().add(0, .15, 0);
                    if (++turns % 4 == 0 && rangeSq >= 8 * 8 && rangeSq <= 18 * 18) next = Attack.RAPID;
                    if (turns % 4 == 2 && rangeSq >= 6 * 6 && clearColumn(floor, floor.add(0, 8, 0))) {
                        next = Attack.RAIN;
                        rainCenter = floor;
                    }
                    entityData.set(ATTACK_KIND, next.ordinal());
                    entityData.set(ATTACK_STARTED, (int)level().getGameTime());
                    playSound(SoundEvents.CROSSBOW_LOADING_START.value(), .65F, 1.2F);
                }
            } else if (tickCount % 10 == 0) {
                getNavigation().moveTo(target, 1);
            }
        }
    }
}
