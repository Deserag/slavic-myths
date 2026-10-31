package org.slavicmyths.posad;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Distinct Nether citizen: 12 professions, city-scoped retaliation, bounded guard duty. */
public final class PosadResidentEntity extends PathfinderMob implements Merchant {
    private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.defineId(PosadResidentEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WEAPON = SynchedEntityData.defineId(PosadResidentEntity.class, EntityDataSerializers.INT);
    private UUID city;
    private String memberKey = "";
    private BlockPos home;
    private int shotCooldown;
    private final Map<String,Integer> tradeUses = new HashMap<>();
    private final IdentityHashMap<MerchantOffer,String> offerIdentities = new IdentityHashMap<>();
    private MerchantOffers trades = new MerchantOffers();
    private Player tradingPlayer;
    private boolean tradesInitialized;
    private int tradeXp;
    private long tradeRestockDay = -1;
    private int tradeRestocksToday;

    public PosadResidentEntity(EntityType<? extends PosadResidentEntity> type, Level level) {
        super(type, level);
        xpReward = 0;
        setPersistenceRequired();
        if (getNavigation() instanceof GroundPathNavigation nav) nav.setCanOpenDoors(true);
    }
    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 70).add(Attributes.MOVEMENT_SPEED, .26)
                .add(Attributes.ATTACK_DAMAGE, 5).add(Attributes.FOLLOW_RANGE, 24)
                .add(Attributes.KNOCKBACK_RESISTANCE, .12);
    }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ROLE, PosadPopulationPlan.Role.CIVILIAN.ordinal());
        builder.define(WEAPON, PosadPopulationPlan.Weapon.NONE.ordinal());
    }
    public PosadPopulationPlan.Role role() { return PosadPopulationPlan.Role.values()[Math.floorMod(entityData.get(ROLE), PosadPopulationPlan.Role.values().length)]; }
    public PosadPopulationPlan.Weapon weapon() { return PosadPopulationPlan.Weapon.values()[Math.floorMod(entityData.get(WEAPON), PosadPopulationPlan.Weapon.values().length)]; }
    public PosadResidentTraits traits() { return PosadResidentTraits.of(getUUID(), role()); }
    public UUID city() { return city; }
    @Override public Component getName() { return hasCustomName() ? super.getName() : Component.translatable("entity.slavicmyths.posad_resident." + role().name().toLowerCase(Locale.ROOT)); }
    public void configure(PosadPopulationPlan.WorldMember member, UUID settlement, BlockPos spawn) {
        city = Objects.requireNonNull(settlement);
        memberKey = member.member().key();
        entityData.set(ROLE, member.member().role().ordinal());
        entityData.set(WEAPON, member.member().weapon().ordinal());
        setUUID(member.identity());
        home = spawn.immutable();
        restrictTo(home, 32);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(role().health());
        setHealth(role().health());
        equip();
        tradesInitialized = false;
    }
    private void equip() {
        Item weapon = switch (weapon()) {
            case SWORD, SWORD_SHIELD -> Items.IRON_SWORD;
            case SPEAR, SPEAR_SHIELD -> Items.TRIDENT;
            case BOW -> Items.BOW;
            case NONE -> Items.AIR;
        };
        setItemSlot(EquipmentSlot.MAINHAND, weapon == Items.AIR ? ItemStack.EMPTY : new ItemStack(weapon));
        setItemSlot(EquipmentSlot.OFFHAND, weapon() == PosadPopulationPlan.Weapon.SWORD_SHIELD || weapon() == PosadPopulationPlan.Weapon.SPEAR_SHIELD ? new ItemStack(Items.SHIELD) : ItemStack.EMPTY);
        for (EquipmentSlot slot : EquipmentSlot.values()) setDropChance(slot, 0);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(1, new PanicGoal(this, 1.25) { @Override public boolean canUse() { return !role().guard() && super.canUse(); } });
        goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 10, .85, 1.1, candidate -> !role().guard() && candidate instanceof Monster attacker && attacker.getTarget() instanceof PosadResidentEntity));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true) {
            @Override public boolean canUse() { return role().guard() && weapon() != PosadPopulationPlan.Weapon.BOW && super.canUse(); }
            @Override public boolean canContinueToUse() { return role().guard() && weapon() != PosadPopulationPlan.Weapon.BOW && super.canContinueToUse(); }
        });
        goalSelector.addGoal(2, new RangedDuty());
        goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, .85));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, .70));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 8, true, false, target -> target instanceof Player p && canFightPlayer(p)));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, target -> target instanceof Monster m && mayDefendAgainst(m)));
    }
    private boolean canFightPlayer(Player player) {
        return role().guard() && city != null && !player.isCreative() && !player.isSpectator()
                && level() instanceof ServerLevel server && PosadGrievances.get(server).hostile(city, player.getUUID())
                && (home == null || player.blockPosition().distSqr(home) <= 48 * 48);
    }
    private boolean mayDefendAgainst(Monster monster) {
        if (!role().guard() || city == null || !monster.isAlive()) return false;
        if (home != null && monster.blockPosition().distSqr(home) > 40 * 40) return false;
        if (monster.getTarget() instanceof PosadResidentEntity resident) return Objects.equals(city, resident.city());
        if (monster.getTarget() instanceof Player) return true;
        return monster instanceof net.minecraft.world.entity.monster.Blaze
                || monster instanceof net.minecraft.world.entity.monster.WitherSkeleton
                || monster instanceof net.minecraft.world.entity.monster.Skeleton
                ;
    }
    @Override public void setTarget(LivingEntity target) {
        if (target instanceof Player player && !canFightPlayer(player)) target = null;
        if (target instanceof PosadResidentEntity || !role().guard()) target = null;
        super.setTarget(target);
    }
    @Override public boolean canAttack(LivingEntity target) {
        if (target instanceof PosadResidentEntity) return false;
        if (target instanceof Player player) return canFightPlayer(player) && super.canAttack(target);
        return role().guard() && super.canAttack(target);
    }
    @Override public boolean isAlliedTo(Entity entity) { return entity instanceof PosadResidentEntity || super.isAlliedTo(entity); }
    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof PosadResidentEntity) return false;
        boolean actuallyHit = super.hurt(source, amount);
        if (actuallyHit && amount > 0 && source.getEntity() instanceof Player p && !p.isCreative() && !p.isSpectator()
                && level() instanceof ServerLevel server && city != null) {
            PosadGrievances.get(server).attacked(city, p.getUUID());
            if (tradingPlayer == p) p.closeContainer();
            for (PosadResidentEntity guard : server.getEntitiesOfClass(PosadResidentEntity.class, getBoundingBox().inflate(48),
                    e -> e.role().guard() && Objects.equals(e.city(), city))) guard.setTarget(p);
        }
        return actuallyHit;
    }
    @Override public void tick() {
        super.tick();
        if (!level().isClientSide) tickEconomy();
        if (level().isClientSide || !role().guard() || city == null || !(level() instanceof ServerLevel server)) return;
        if (getTarget() instanceof Player player && !canFightPlayer(player)) setTarget(null);
        boolean shield = weapon() == PosadPopulationPlan.Weapon.SWORD_SHIELD || weapon() == PosadPopulationPlan.Weapon.SPEAR_SHIELD;
        boolean defend = shield && getTarget() != null && distanceToSqr(getTarget()) <= 25 && tickCount % 36 < 18;
        if (defend && !isUsingItem()) startUsingItem(InteractionHand.OFF_HAND);
        if (!defend && isUsingItem()) stopUsingItem();
        if (home != null && blockPosition().distSqr(home) > 48 * 48) {
            setTarget(null);
            getNavigation().moveTo(home.getX() + .5, home.getY(), home.getZ() + .5, .9);
        }
        if (tickCount % 40 == 0 && getTarget() == null) {
            for (Player player : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(32), this::canFightPlayer)) {
                setTarget(player);
                break;
            }
        }
    }
    private final class RangedDuty extends Goal {
        private int warmup;
        RangedDuty() { setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return role().guard() && weapon() == PosadPopulationPlan.Weapon.BOW && getTarget() != null && getTarget().isAlive(); }
        @Override public boolean canContinueToUse() { return canUse(); }
        @Override public void tick() {
            LivingEntity target = getTarget();
            if (target == null) return;
            getLookControl().setLookAt(target, 30, 30);
            double distance = distanceToSqr(target);
            if (distance > 144 || !getSensing().hasLineOfSight(target)) {
                warmup = 0;
                getNavigation().moveTo(target, 1.0);
                return;
            }
            getNavigation().stop();
            if (shotCooldown > 0) { shotCooldown--; return; }
            if (++warmup < 24) return;
            warmup = 0;
            if (!clearShot(target)) { shotCooldown = 10; return; }
            Arrow arrow = new Arrow(level(), PosadResidentEntity.this, new ItemStack(Items.ARROW), getMainHandItem());
            double dx = target.getX() - getX(), dz = target.getZ() - getZ();
            arrow.setBaseDamage(2);
            arrow.shoot(dx, target.getY(.5) - arrow.getY() + Math.sqrt(dx * dx + dz * dz) * .18, dz, 1.6F, 5);
            level().addFreshEntity(arrow);
            playSound(SoundEvents.SKELETON_SHOOT, .6F, 1.1F);
            shotCooldown = 35;
        }
        @Override public void stop() { warmup = 0; }
        private boolean clearShot(LivingEntity target) {
            Vec3 from = getEyePosition(), to = target.getEyePosition(), delta = to.subtract(from);
            double denominator = Math.max(0.001, delta.lengthSqr());
            for (PosadResidentEntity ally : level().getEntitiesOfClass(PosadResidentEntity.class, getBoundingBox().inflate(16), e -> e != PosadResidentEntity.this && e.isAlive())) {
                Vec3 offset = ally.getEyePosition().subtract(from);
                double t = Math.max(0, Math.min(1, offset.dot(delta) / denominator));
                if (t > 0 && t < 1 && ally.getEyePosition().distanceToSqr(from.add(delta.scale(t))) < 1.7) return false;
            }
            return true;
        }
    }
    /** Trade XP is per NPC, never city trust or a quest. */
    private int tradeTier() { return PosadTradeCatalog.tier(role(), tradeXp); }
    private boolean refuses(Player player) {
        return city != null && level() instanceof ServerLevel server && PosadGrievances.get(server).hostile(city, player.getUUID());
    }
    private void rebuildTrades() {
        trades = PosadTradeCatalog.create(role(), tradeTier(), tradeUses, offerIdentities, level().registryAccess());
        tradesInitialized = true;
    }
    @Override public MerchantOffers getOffers() {
        if (!tradesInitialized) rebuildTrades();
        return trades;
    }
    @Override public void overrideOffers(MerchantOffers replacement) {
        if (level().isClientSide) {
            trades = replacement.copy();
            tradesInitialized = true;
        }
    }
    @Override public void setTradingPlayer(Player player) { tradingPlayer = player; }
    @Override public Player getTradingPlayer() { return tradingPlayer; }
    @Override public int getVillagerXp() { return tradeXp; }
    @Override public void overrideXp(int xp) {
        if (level().isClientSide) return;
        int oldTier = tradeTier();
        tradeXp = Math.max(tradeXp, Math.min(1_000_000, xp));
        if (tradeTier() != oldTier) rebuildTrades();
        if (tradingPlayer instanceof ServerPlayer player && player.containerMenu instanceof MerchantMenu menu) {
            player.sendMerchantOffers(menu.containerId, getOffers().copy(), tradeTier(), tradeXp, showProgressBar(), false);
        }
    }
    @Override public boolean showProgressBar() { return !PosadTradeCatalog.fixed(role()); }
    @Override public SoundEvent getNotifyTradeSound() { return SoundEvents.VILLAGER_YES; }
    @Override public boolean isClientSide() { return level().isClientSide; }
    @Override public boolean canRestock() { return false; } // Controlled workday restock, never unlimited vanilla refresh.
    @Override public void notifyTradeUpdated(ItemStack stack) {
        if (!stack.isEmpty() && !level().isClientSide) playSound(SoundEvents.VILLAGER_YES, .5F, 1.0F);
    }
    @Override public void notifyTrade(MerchantOffer offer) {
        if (level().isClientSide || !isAlive() || tradingPlayer == null || refuses(tradingPlayer) || distanceToSqr(tradingPlayer) > 64) return;
        String id = offerIdentities.get(offer);
        if (id == null || offer.isOutOfStock()) return;
        offer.increaseUses();
        tradeUses.put(id, offer.getUses());
    }
    @Override protected net.minecraft.world.InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !isAlive() || city == null || player.isSpectator() || refuses(player)) return super.mobInteract(player, hand);
        if (!level().isClientSide) {
            if (tradingPlayer != null && tradingPlayer != player) tradingPlayer.closeContainer();
            getOffers();
            openTradingScreen(player, getName(), tradeTier());
        }
        return net.minecraft.world.InteractionResult.sidedSuccess(level().isClientSide);
    }
    private void tickEconomy() {
        if (!(level() instanceof ServerLevel server)) return;
        if (tradingPlayer != null && (!isAlive() || tradingPlayer.isRemoved() || tradingPlayer.distanceToSqr(this) > 64
                || refuses(tradingPlayer) || !(tradingPlayer.containerMenu instanceof MerchantMenu))) {
            Player previous = tradingPlayer;
            tradingPlayer = null;
            if (previous.containerMenu instanceof MerchantMenu) previous.closeContainer();
        }
        if (tickCount % 1200 != 0 || city == null || home == null || tradingPlayer != null || blockPosition().distSqr(home) > 64) return;
        long day = server.getDayTime() / 24000L;
        if (day > tradeRestockDay) { tradeRestockDay = day; tradeRestocksToday = 0; }
        if (tradeRestocksToday >= 2) return;
        MerchantOffers offers = getOffers();
        boolean needed = false;
        for (MerchantOffer offer : offers) if (offer.needsRestock()) { needed = true; break; }
        if (!needed) return;
        for (MerchantOffer offer : offers) {
            if (!offer.needsRestock()) continue;
            offer.resetUses();
            String id = offerIdentities.get(offer);
            if (id != null) tradeUses.put(id, 0);
        }
        tradeRestocksToday++;
    }
    @Override public void die(DamageSource source) {
        if (tradingPlayer != null) {
            Player previous = tradingPlayer;
            tradingPlayer = null;
            previous.closeContainer();
        }
        super.die(source);
    }
    @Override protected SoundEvent getAmbientSound() { return SoundEvents.PIGLIN_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.PIGLIN_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.PIGLIN_DEATH; }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PosadRole", role().ordinal());
        tag.putInt("PosadWeapon", weapon().ordinal());
        tag.putString("PosadMember", memberKey);
        if (city != null) tag.putUUID("PosadCity", city);
        if (home != null) tag.putLong("PosadHome", home.asLong());
        tag.putInt("PosadTradeXp", tradeXp);
        tag.putLong("PosadTradeDay", tradeRestockDay);
        tag.putInt("PosadTradeRestocks", tradeRestocksToday);
        ListTag stock = new ListTag();
        for (var entry : tradeUses.entrySet()) {
            if (entry.getValue() <= 0) continue;
            CompoundTag row = new CompoundTag();
            row.putString("Offer", entry.getKey());
            row.putInt("Uses", entry.getValue());
            stock.add(row);
        }
        tag.put("PosadTradeStock", stock);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ROLE, Math.floorMod(tag.getInt("PosadRole"), PosadPopulationPlan.Role.values().length));
        entityData.set(WEAPON, Math.floorMod(tag.getInt("PosadWeapon"), PosadPopulationPlan.Weapon.values().length));
        memberKey = tag.getString("PosadMember");
        city = tag.hasUUID("PosadCity") ? tag.getUUID("PosadCity") : null;
        home = tag.contains("PosadHome") ? BlockPos.of(tag.getLong("PosadHome")) : null;
        if (home != null) restrictTo(home, 32);
        tradeXp = Math.max(0, tag.getInt("PosadTradeXp"));
        tradeRestockDay = tag.contains("PosadTradeDay") ? tag.getLong("PosadTradeDay") : -1;
        tradeRestocksToday = Math.max(0, Math.min(2, tag.getInt("PosadTradeRestocks")));
        tradeUses.clear();
        for (Tag raw : tag.getList("PosadTradeStock", 10)) {
            CompoundTag row = (CompoundTag) raw;
            if (!row.getString("Offer").isBlank()) tradeUses.put(row.getString("Offer"), Math.max(0, row.getInt("Uses")));
        }
        tradesInitialized = false;
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(role().health());
        equip();
    }
}
