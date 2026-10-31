package org.slavicmyths.posad.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Initial server-side combat foundation for the two distinct 2.2 roaming bosses.
 * No coin theft, loot or teleports are performed until persistent reward ledgers exist.
 */
public final class PosadBoss extends Monster {
    public enum Kind { MYTAR, BAUK }
    public enum Move { IDLE, WINDUP_BITE, WINDUP_PAW, RECOVERY, RAGE_TRANSITION, WINDUP_SICKLE, WINDUP_SLAM, WINDUP_ROAR, WINDUP_CHARGE }
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(PosadBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(PosadBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(PosadBoss.class, EntityDataSerializers.BOOLEAN);
    private final Kind kind;
    private final ServerBossEvent bar;
    private int actionTicks;
    private int cooldown;
    private boolean connected;
    private boolean rewardDropped;
    private int specialCooldown;
    public PosadBoss(EntityType<? extends PosadBoss> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.bar = new ServerBossEvent(Component.translatable(kind == Kind.MYTAR ? "entity.slavicmyths.cursed_mytar" : "entity.slavicmyths.bauk"), kind == Kind.MYTAR ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
        this.xpReward = 250;
        setPersistenceRequired();
        setHealth(getMaxHealth());
    }
    public static AttributeSupplier.Builder attributes(Kind kind) {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, kind == Kind.MYTAR ? 1350D : 1800D).add(Attributes.MOVEMENT_SPEED, kind == Kind.MYTAR ? .27D : .22D).add(Attributes.ATTACK_DAMAGE, kind == Kind.MYTAR ? 10D : 15D).add(Attributes.ARMOR, kind == Kind.MYTAR ? 10D : 8D).add(Attributes.ARMOR_TOUGHNESS, kind == Kind.MYTAR ? 4D : 2D).add(Attributes.KNOCKBACK_RESISTANCE, kind == Kind.MYTAR ? .65D : .85D).add(Attributes.FOLLOW_RANGE, kind == Kind.MYTAR ? 36D : 28D);
    }
    @Override public float getMaxHealth() { return (float) org.slavicmyths.combat.CreatureHealthBalance.uncappedMaximum(getAttribute(Attributes.MAX_HEALTH)); }
    @Override public void onRemovedFromLevel() { bar.removeAllPlayers(); super.onRemovedFromLevel(); }
    public Kind kind() { return kind; }
    public Move move() { return Move.values()[entityData.get(ACTION)]; }
    public boolean enraged() { return entityData.get(ENRAGED); }
    public int phase() { return entityData.get(PHASE); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION, Move.IDLE.ordinal());
        builder.define(PHASE, 1);
        builder.define(ENRAGED, false);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 20F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); bar.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); bar.removePlayer(player); }
    @Override protected void customServerAiStep() {
        super.customServerAiStep();
        bar.setProgress(Math.max(0F, Math.min(1F, getHealth() / getMaxHealth())));
        if (kind == Kind.MYTAR && phase() == 1 && getHealth() <= 607.5F) { entityData.set(PHASE, 2); begin(Move.RECOVERY, 35, 35); return; }
        if (kind == Kind.BAUK && !enraged() && getHealth() <= 630F) {
            entityData.set(ENRAGED, true);
            getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.27D);
            begin(Move.RAGE_TRANSITION, 40, 40);
            return;
        }
        if (cooldown > 0) cooldown--;
        if (specialCooldown > 0) specialCooldown--;
        if (actionTicks > 0) {
            actionTicks--;
            getNavigation().stop();
            if (!connected && actionTicks == impactTick(move())) {
                connected = true;
                applyHit();
            }
            if (actionTicks == 0) entityData.set(ACTION, Move.IDLE.ordinal());
            return;
        }
        LivingEntity target = getTarget();
        if (!(target instanceof Player player) || !player.isAlive() || player.isCreative() || player.isSpectator()) { getNavigation().stop(); return; }
        getLookControl().setLookAt(target, 30F, 30F);
        if (distanceToSqr(target) > 12.25D) { getNavigation().moveTo(target, kind == Kind.BAUK && enraged() ? 1.2D : 1D); return; }
        getNavigation().stop();
        if (cooldown > 0) return;
        if (kind == Kind.BAUK && specialCooldown == 0 && random.nextInt(5) == 0) { specialCooldown = 220; begin(Move.WINDUP_SLAM, 55, 50); }
        else if (kind == Kind.BAUK && specialCooldown == 0 && random.nextInt(4) == 0) { specialCooldown = 260; begin(Move.WINDUP_ROAR, 48, 45); }
        else if (kind == Kind.BAUK && random.nextBoolean()) begin(Move.WINDUP_PAW, 38, 70);
        else if (kind == Kind.BAUK) begin(Move.WINDUP_BITE, 32, 42);
        else begin(Move.WINDUP_SICKLE, 38, 36);
    }
    private void begin(Move next, int ticks, int after) {
        entityData.set(ACTION, next.ordinal());
        actionTicks = ticks;
        cooldown = after;
        connected = false;
    }
    private int impactTick(Move move) { return switch (move) { case WINDUP_BITE -> 18; case WINDUP_PAW -> 20; case WINDUP_SLAM -> 25; case WINDUP_ROAR -> 20; case WINDUP_SICKLE -> 14; default -> -1; }; }
    private void applyHit() {
        if (move() == Move.WINDUP_SLAM || move() == Move.WINDUP_ROAR) {
            for (Player victim : level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(move() == Move.WINDUP_ROAR ? 6 : 4), p -> p.isAlive() && !p.isCreative() && !p.isSpectator())) {
                if (!getSensing().hasLineOfSight(victim)) continue;
                if (move() == Move.WINDUP_SLAM) victim.hurt(damageSources().mobAttack(this), 10F);
                else victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 40, 0));
                victim.knockback(move() == Move.WINDUP_SLAM ? .6D : .5D, getX() - victim.getX(), getZ() - victim.getZ());
            }
            return;
        }
        LivingEntity target = getTarget();
        if (!(target instanceof Player player) || !player.isAlive() || player.isCreative() || player.isSpectator()) return;
        if (!getSensing().hasLineOfSight(target) || distanceToSqr(target) > 12.25D) return;
        float damage = kind == Kind.MYTAR ? 10F : move() == Move.WINDUP_PAW ? 12F : 15F;
        if (target.hurt(damageSources().mobAttack(this), damage) && kind == Kind.BAUK && move() == Move.WINDUP_PAW) target.knockback(.8D, getX() - target.getX(), getZ() - target.getZ());
    }
    @Override public void die(DamageSource source) {
        if (!level().isClientSide && !rewardDropped && source.getEntity() instanceof Player) {
            rewardDropped = true;
            if (kind == Kind.MYTAR) {
                spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.MYTAR_SEAL.get()));
                spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.MYTAR_CHAIN.get()));
                if (random.nextFloat() < .20F) spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.MYTAR_SICKLE.get()));
                if (random.nextFloat() < .15F) spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.MYTAR_PURSE.get()));
            } else {
                spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.BAUK_FANG.get()));
                spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.BAUK_HIDE.get()));
                spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.BAUK_CLAW.get(), 2));
            }
        }
        super.die(source);
    }
    @Override public boolean hurt(DamageSource source, float damage) { return super.hurt(source, damage); }
    @Override public boolean fireImmune() { return true; }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PosadAction", entityData.get(ACTION));
        tag.putInt("PosadPhase", phase());
        tag.putInt("PosadActionTicks", actionTicks);
        tag.putInt("PosadCooldown", cooldown);
        tag.putBoolean("PosadEnraged", enraged());
        tag.putBoolean("PosadHitDone", connected);
        tag.putInt("PosadSpecialCooldown", specialCooldown);
        tag.putBoolean("PosadRewardDropped", rewardDropped);
    }
    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ACTION, Math.max(0, Math.min(Move.values().length - 1, tag.getInt("PosadAction"))));
        entityData.set(PHASE, kind == Kind.MYTAR && tag.getInt("PosadPhase") >= 2 ? 2 : 1);
        actionTicks = Math.max(0, Math.min(80, tag.getInt("PosadActionTicks")));
        cooldown = Math.max(0, Math.min(600, tag.getInt("PosadCooldown")));
        connected = tag.getBoolean("PosadHitDone");
        specialCooldown = Math.max(0, Math.min(600, tag.getInt("PosadSpecialCooldown")));
        rewardDropped = tag.getBoolean("PosadRewardDropped");
        entityData.set(ENRAGED, kind == Kind.BAUK && tag.getBoolean("PosadEnraged"));
        if (enraged()) getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.27D);
    }
}
