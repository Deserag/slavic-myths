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
    public enum Move { IDLE, WINDUP_BITE, WINDUP_PAW, RECOVERY, RAGE_TRANSITION, WINDUP_SICKLE, WINDUP_SLAM, WINDUP_ROAR, WINDUP_CHARGE, WINDUP_CHAIN, WINDUP_COINS, WINDUP_GRAB }
    private static final EntityDataAccessor<Integer> ACTION = SynchedEntityData.defineId(PosadBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(PosadBoss.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> ENRAGED = SynchedEntityData.defineId(PosadBoss.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> ACTION_REMAINING = SynchedEntityData.defineId(PosadBoss.class, EntityDataSerializers.INT);
    private final Kind kind;
    private final ServerBossEvent bar;
    private int actionTicks;
    private int cooldown;
    private int nextPathTick;
    private boolean connected;
    private ServerPlayer chainedPlayer;
    private PosadBoss chainDestination;
    private boolean rewardDropped;
    private int specialCooldown;
    private int chainCooldown;
    private int coinsCooldown;
    private int grabCooldown;
    private long bellImmuneUntil;
    private net.minecraft.world.phys.Vec3 chargeDirection = net.minecraft.world.phys.Vec3.ZERO;
    private boolean naturalEncounter;
    private int healedFromCoins;
    private final java.util.Set<java.util.UUID> participants = new java.util.HashSet<>();
    private final java.util.Map<java.util.UUID, Long> stealReady = new java.util.HashMap<>();
    public PosadBoss(EntityType<? extends PosadBoss> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        this.bar = new ServerBossEvent(Component.translatable(kind == Kind.MYTAR ? "entity.slavicmyths.cursed_mytar" : "entity.slavicmyths.bauk"),org.slavicmyths.combat.BossBarPalette.colorFor(getType(),kind == Kind.MYTAR ? BossEvent.BossBarColor.PURPLE : BossEvent.BossBarColor.RED), BossEvent.BossBarOverlay.PROGRESS);
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
    public void interruptByBell() {
        if (kind != Kind.BAUK || level().isClientSide || level().getGameTime() < bellImmuneUntil) return;
        if (move() != Move.WINDUP_CHARGE && move() != Move.WINDUP_GRAB) return;
        if (level().getMaxLocalRawBrightness(blockPosition()) < 11) return;
        bellImmuneUntil = level().getGameTime() + 800L;
        begin(Move.RECOVERY, 30, 30);
    }
    public Kind kind() { return kind; }
    public Move move() { Move[] moves = Move.values(); int index = entityData.get(ACTION); return moves[index >= 0 && index < moves.length ? index : Move.IDLE.ordinal()]; }
    public boolean enraged() { return entityData.get(ENRAGED); }
    public int phase() { return entityData.get(PHASE); }
    public int actionRemaining() { return entityData.get(ACTION_REMAINING); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION, Move.IDLE.ordinal());
        builder.define(PHASE, 1);
        builder.define(ENRAGED, false);
        builder.define(ACTION_REMAINING, 0);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 20F));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, entity -> entity instanceof Player player && !player.isCreative() && !player.isSpectator()));
    }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void checkDespawn() {
        if (!level().isClientSide && level().getDifficulty() == net.minecraft.world.Difficulty.PEACEFUL && level() instanceof net.minecraft.server.level.ServerLevel server) {
            PosadBossLedger.get(server).settle(server, getUUID(), false);
            discard();
            return;
        }
        super.checkDespawn();
    }
    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player); bar.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player); bar.removePlayer(player); }
    @Override protected void customServerAiStep() {
        if (!(level() instanceof net.minecraft.server.level.ServerLevel)) return;
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
        if (chainCooldown > 0) chainCooldown--;
        if (coinsCooldown > 0) coinsCooldown--;
        if (grabCooldown > 0) grabCooldown--;
        if (actionTicks > 0) {
            actionTicks--;
            entityData.set(ACTION_REMAINING, actionTicks);
            getNavigation().stop();
            if (move() == Move.WINDUP_CHARGE && actionTicks <= 14) chargeStep();
            if (move() == Move.WINDUP_COINS && (actionTicks == 16 || actionTicks == 8 || actionTicks == 1)) applyHit();
            if (!connected && actionTicks == impactTick(move())) {
                connected = true;
                applyHit();
            }
            if (move() == Move.WINDUP_CHAIN && chainedPlayer != null) pullChainedPlayer();
            if (actionTicks == 0) {
                chainedPlayer = null;
                chainDestination = null;
                entityData.set(ACTION, Move.IDLE.ordinal());
            }
            return;
        }
        LivingEntity target = getTarget();
        if (!(target instanceof Player player) || !player.isAlive() || player.isCreative() || player.isSpectator()) { getNavigation().stop(); return; }
        getLookControl().setLookAt(target, 30F, 30F);
        double distance = distanceToSqr(target);
        if (cooldown == 0 && getSensing().hasLineOfSight(target)) {
            if (kind == Kind.MYTAR && distance >= 25D && distance <= 196D && chainCooldown == 0) { chainCooldown = phase() == 2 ? 120 : 160; begin(Move.WINDUP_CHAIN, 40, 25); return; }
            if (kind == Kind.MYTAR && distance >= 16D && distance <= 144D && coinsCooldown == 0) { coinsCooldown = 220; begin(Move.WINDUP_COINS, 40, 25); return; }
            if (kind == Kind.BAUK && distance >= 25D && distance <= 169D && specialCooldown == 0 && canCharge(target)) { specialCooldown = enraged() ? 120 : 160; begin(Move.WINDUP_CHARGE, 44, 30); return; }
        }
        if (distance > 12.25D) { if (tickCount >= nextPathTick || getNavigation().isDone()) { nextPathTick = tickCount + 10; getNavigation().moveTo(target, kind == Kind.BAUK && enraged() ? 1.2D : 1D); } return; }
        getNavigation().stop();
        if (cooldown > 0) return;
        if (kind == Kind.BAUK && grabCooldown == 0 && distance <= 7.84D && random.nextInt(4) == 0) { grabCooldown = 160; begin(Move.WINDUP_GRAB, 38, 45); }
        else if (kind == Kind.BAUK && specialCooldown == 0 && random.nextInt(5) == 0) { specialCooldown = 220; begin(Move.WINDUP_SLAM, 55, 50); }
        else if (kind == Kind.BAUK && specialCooldown == 0 && random.nextInt(4) == 0) { specialCooldown = 260; begin(Move.WINDUP_ROAR, 48, 45); }
        else if (kind == Kind.BAUK && random.nextBoolean()) begin(Move.WINDUP_PAW, 38, 70);
        else if (kind == Kind.BAUK) begin(Move.WINDUP_BITE, 32, 42);
        else begin(Move.WINDUP_SICKLE, 38, 36);
    }
    private void begin(Move next, int ticks, int after) {
        entityData.set(ACTION, next.ordinal());
        actionTicks = ticks;
        entityData.set(ACTION_REMAINING, ticks);
        cooldown = after;
        connected = false;
        chainedPlayer = null;
        chainDestination = null;
        if (next == Move.WINDUP_CHARGE && getTarget() != null) chargeDirection = getTarget().position().subtract(position()).multiply(1D, 0D, 1D).normalize();
        switch (next) {
            case WINDUP_CHAIN -> playSound(net.minecraft.sounds.SoundEvents.CHAIN_PLACE, 1.1F, .7F);
            case WINDUP_COINS -> playSound(net.minecraft.sounds.SoundEvents.EXPERIENCE_ORB_PICKUP, 1F, .55F);
            case WINDUP_CHARGE, WINDUP_ROAR, RAGE_TRANSITION -> playSound(net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, 1.5F, .7F);
            case WINDUP_SICKLE, WINDUP_PAW, WINDUP_SLAM -> playSound(net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, 1F, .65F);
            default -> {}
        }
        if (level() instanceof net.minecraft.server.level.ServerLevel server && next != Move.IDLE) {
            server.sendParticles(kind == Kind.MYTAR ? net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME : net.minecraft.core.particles.ParticleTypes.CRIT, getX(), getY() + 1.5D, getZ(), 12, .7D, .8D, .7D, .02D);
        }
    }
    private int impactTick(Move move) { return switch (move) { case WINDUP_BITE -> 18; case WINDUP_PAW -> 20; case WINDUP_SLAM -> 25; case WINDUP_ROAR -> 20; case WINDUP_SICKLE -> 14; case WINDUP_CHAIN -> 14; case WINDUP_GRAB -> 16; default -> -1; }; }
    private boolean canCharge(LivingEntity target) {
        net.minecraft.world.phys.Vec3 direction = target.position().subtract(position()).multiply(1D, 0D, 1D).normalize();
        if (direction.lengthSqr() < .1D) return false;
        for (int distance = 2; distance <= 13; distance += 2) {
            net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(position().add(direction.scale(distance)));
            if (!level().hasChunkAt(pos) || !level().getBlockState(pos).isAir() || !level().getBlockState(pos.above()).isAir() || !level().getBlockState(pos.below()).isSolid()) return false;
        }
        return true;
    }
    private void chargeStep() {
        if (chargeDirection.lengthSqr() < .1D || !(level() instanceof net.minecraft.server.level.ServerLevel server)) return;
        net.minecraft.world.phys.Vec3 step = chargeDirection.scale(1.15D);
        net.minecraft.core.BlockPos next = net.minecraft.core.BlockPos.containing(position().add(step));
        if (!server.hasChunkAt(next) || !server.getBlockState(next.below()).isSolid() || !server.noCollision(this, getBoundingBox().move(step))) { begin(Move.RECOVERY, 30, 30); return; }
        move(net.minecraft.world.entity.MoverType.SELF, step);
        for (Player victim : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(.5D), p -> p.isAlive() && !p.isCreative() && !p.isSpectator())) {
            if (victim.hurt(damageSources().mobAttack(this), 18F)) victim.knockback(1.2D, -step.x, -step.z);
            begin(Move.RECOVERY, 25, 45);
            break;
        }
    }
    private void pullChainedPlayer() {
        ServerPlayer player = chainedPlayer;
        if (!player.isAlive() || player.isRemoved() || player.level() != level() || player.isCreative()
                || player.isSpectator() || distanceToSqr(player) > 196D || !getSensing().hasLineOfSight(player)
                || chainDestination.isRemoved() || !chainDestination.isAlive()) {
            chainedPlayer = null;
            chainDestination = null;
            return;
        }
        var offset = chainDestination.position().subtract(player.position()).multiply(1D, 0D, 1D);
        double stopDistance = (chainDestination.getBbWidth() + player.getBbWidth()) / 2D + .5D;
        double distance = offset.length();
        if (distance <= stopDistance) return;
        var pull = offset.scale(Math.min(.48D, distance - stopDistance) / distance);
        if (level().noCollision(player, player.getBoundingBox().move(pull))) {
            player.setDeltaMovement(pull.x, player.getDeltaMovement().y, pull.z);
            player.hurtMarked = true;
        }
    }

    private void applyHit() {
        if (move() == Move.WINDUP_CHAIN || move() == Move.WINDUP_COINS) {
            LivingEntity distant = getTarget();
            if (!(distant instanceof Player p) || !p.isAlive() || p.isCreative() || p.isSpectator() || !getSensing().hasLineOfSight(distant) || distanceToSqr(distant) > (move() == Move.WINDUP_CHAIN ? 196D : 144D)) return;
            if (move() == Move.WINDUP_COINS) { distant.hurt(damageSources().mobAttack(this), 5F); return; }
            if (distant instanceof ServerPlayer player && distant.hurt(damageSources().mobAttack(this), 6F)) {
                chainedPlayer = player;
                chainDestination = this;
                for (PosadBoss candidate : level().getEntitiesOfClass(PosadBoss.class, getBoundingBox().inflate(16D),
                        boss -> boss.kind == Kind.BAUK && boss.isAlive() && boss.getTarget() == player)) {
                    if (chainDestination == this || candidate.distanceToSqr(player) < chainDestination.distanceToSqr(player)) {
                        chainDestination = candidate;
                    }
                }
            }
            return;
        }
        if (move() == Move.WINDUP_GRAB) {
            LivingEntity victim = getTarget();
            if (!(victim instanceof Player p) || p.isCreative() || p.isSpectator() || distanceToSqr(p) > 7.84D || !getSensing().hasLineOfSight(p)) return;
            if (victim.hurt(damageSources().mobAttack(this), 14F)) victim.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 16, 1));
            return;
        }
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
                    spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.BAUK_WOOL.get()));
                    spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.BAUK_CLAW.get(), 2));
                    spawnAtLocation(new net.minecraft.world.item.ItemStack(org.slavicmyths.registry.ModItems.BAUK_HEAD_ITEM.get()));
                }
            }
        }
        super.die(source);
    }
    @Override public boolean hurt(DamageSource source, float damage) { boolean accepted = super.hurt(source, damage); if (accepted && naturalEncounter && source.getEntity() instanceof ServerPlayer player && !player.isCreative() && !player.isSpectator()) participants.add(player.getUUID()); return accepted; }
    @Override public boolean fireImmune() { return true; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PosadAction", entityData.get(ACTION));
        tag.putInt("PosadPhase", phase());
        tag.putInt("PosadActionTicks", actionTicks);
        tag.putInt("PosadCooldown", cooldown);
        tag.putBoolean("PosadEnraged", enraged());
        tag.putBoolean("PosadHitDone", connected);
        tag.putInt("PosadSpecialCooldown", specialCooldown);
        tag.putInt("PosadChainCooldown", chainCooldown);
        tag.putInt("PosadCoinsCooldown", coinsCooldown);
        tag.putInt("PosadGrabCooldown", grabCooldown);
        tag.putLong("PosadBellImmunity", bellImmuneUntil);
        tag.putDouble("PosadChargeX", chargeDirection.x);
        tag.putDouble("PosadChargeZ", chargeDirection.z);
        tag.putBoolean("PosadRewardDropped", rewardDropped);
        tag.putBoolean("PosadNatural", naturalEncounter);
        tag.putInt("PosadCoinHeal", healedFromCoins);
        net.minecraft.nbt.ListTag players = new net.minecraft.nbt.ListTag();
        for (java.util.UUID id : participants) { CompoundTag row = new CompoundTag(); row.putUUID("Player", id); players.add(row); }
        tag.put("PosadParticipants", players);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ACTION, Math.max(0, Math.min(Move.values().length - 1, tag.getInt("PosadAction"))));
        entityData.set(PHASE, kind == Kind.MYTAR && tag.getInt("PosadPhase") >= 2 ? 2 : 1);
        actionTicks = Math.max(0, Math.min(80, tag.getInt("PosadActionTicks")));
        cooldown = Math.max(0, Math.min(600, tag.getInt("PosadCooldown")));
        connected = tag.getBoolean("PosadHitDone");
        specialCooldown = Math.max(0, Math.min(600, tag.getInt("PosadSpecialCooldown")));
        chainCooldown = Math.max(0, Math.min(600, tag.getInt("PosadChainCooldown")));
        coinsCooldown = Math.max(0, Math.min(600, tag.getInt("PosadCoinsCooldown")));
        grabCooldown = Math.max(0, Math.min(600, tag.getInt("PosadGrabCooldown")));
        bellImmuneUntil = tag.getLong("PosadBellImmunity");
        chargeDirection = new net.minecraft.world.phys.Vec3(tag.getDouble("PosadChargeX"), 0D, tag.getDouble("PosadChargeZ"));
        rewardDropped = tag.getBoolean("PosadRewardDropped");
        naturalEncounter = tag.getBoolean("PosadNatural");
        healedFromCoins = Math.max(0, Math.min(240, tag.getInt("PosadCoinHeal")));
        participants.clear();
        for (net.minecraft.nbt.Tag raw : tag.getList("PosadParticipants", 10)) { CompoundTag row = (CompoundTag) raw; if (row.hasUUID("Player")) participants.add(row.getUUID("Player")); }
        entityData.set(ENRAGED, kind == Kind.BAUK && tag.getBoolean("PosadEnraged"));
        if (enraged()) getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(.27D);
    }
}
