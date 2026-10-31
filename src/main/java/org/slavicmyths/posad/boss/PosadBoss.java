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
    private boolean naturalEncounter;
    private int healedFromCoins;
    private final java.util.Set<java.util.UUID> participants = new java.util.HashSet<>();
    private final java.util.Map<java.util.UUID, Long> stealReady = new java.util.HashMap<>();
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
    public void markNaturalEncounter() { naturalEncounter = true; }
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
        float before = target.getHealth();
        boolean hit = target.hurt(damageSources().mobAttack(this), damage);
        if (hit && target.getHealth() < before && kind == Kind.BAUK && move() == Move.WINDUP_PAW) target.knockback(.8D, getX() - target.getX(), getZ() - target.getZ());
        if (hit && target.getHealth() < before && kind == Kind.MYTAR && target instanceof ServerPlayer playerTarget && level() instanceof net.minecraft.server.level.ServerLevel server) {
            long now = server.getGameTime();
            if (now >= stealReady.getOrDefault(playerTarget.getUUID(), 0L)) {
                stealReady.put(playerTarget.getUUID(), now + 100);
                int amount = PosadBossLedger.get(server).steal(this, playerTarget, 1 + random.nextInt(2));
                if (amount > 0) { int restored = Math.min(240 - healedFromCoins, 8 * amount); healedFromCoins += restored; heal(restored); }
            }
        }
    }
    @Override public void die(DamageSource source) {
        if (!level().isClientSide && !rewardDropped && level() instanceof net.minecraft.server.level.ServerLevel server) {
            rewardDropped = true;
            PosadBossLedger ledger = PosadBossLedger.get(server);
            ledger.settle(server, getUUID(), true);
            if (naturalEncounter && source.getEntity() instanceof ServerPlayer killer && !killer.isCreative() && !killer.isSpectator()) participants.add(killer.getUUID());
            if (naturalEncounter) for (java.util.UUID playerId : participants) {
                ServerPlayer winner = server.getServer().getPlayerList().getPlayer(playerId);
                if (winner == null || winner.distanceToSqr(this) > 128D * 128D) continue;
                if (kind == Kind.MYTAR) {
                    spawnAtLocation(ledger.mint(org.slavicmyths.registry.ModItems.MYTAR_SEAL.get(), getUUID(), playerId));
                    spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.MYTAR_CHAIN.get()));
                    if (ledger.sickle(winner, random)) spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.MYTAR_SICKLE.get()));
                    if (random.nextFloat() < .15F) spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.MYTAR_PURSE.get()));
                } else {
                    spawnAtLocation(ledger.mint(org.slavicmyths.registry.ModItems.BAUK_FANG.get(), getUUID(), playerId));
                    spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.BAUK_HIDE.get()));
                    spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.BAUK_CLAW.get(), 2));
                    spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.BAUK_HEAD_ITEM.get()));
                }
            }
        }
        super.die(source);
    }
    @Override public boolean hurt(DamageSource source, float damage) { boolean accepted = super.hurt(source, damage); if (accepted && naturalEncounter && source.getEntity() instanceof ServerPlayer player && !player.isCreative() && !player.isSpectator()) participants.add(player.getUUID()); return accepted; }
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
        tag.putBoolean("PosadNatural", naturalEncounter);
        tag.putInt("PosadCoinHeal", healedFromCoins);
        net.minecraft.nbt.ListTag players = new net.minecraft.nbt.ListTag();
        for (java.util.UUID id : participants) { CompoundTag row = new CompoundTag(); row.putUUID("Player", id); players.add(row); }
        tag.put("PosadParticipants", players);
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
        naturalEncounter = tag.getBoolean("PosadNatural");
        healedFromCoins = Math.max(0, Math.min(240, tag.getInt("PosadCoinHeal")));
        participants.clear();
        for (net.minecraft.nbt.Tag raw : tag.getList("PosadParticipants", 10)) { CompoundTag row = (CompoundTag) raw; if (row.hasUUID("Player")) participants.add(row.getUUID("Player")); }
        entityData.set(ENRAGED, kind == Kind.BAUK && tag.getBoolean("PosadEnraged"));
        if (enraged()) getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.27D);
    }
}
