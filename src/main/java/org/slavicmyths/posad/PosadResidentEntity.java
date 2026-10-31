package org.slavicmyths.posad;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
public final class PosadResidentEntity extends PathfinderMob {
    private static final EntityDataAccessor<Integer> ROLE = SynchedEntityData.defineId(PosadResidentEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WEAPON = SynchedEntityData.defineId(PosadResidentEntity.class, EntityDataSerializers.INT);
    private UUID city;
    private String memberKey = "";
    private BlockPos home;
    private int shotCooldown;

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
        if (!role().guard() || monster instanceof PosadResidentEntity || city == null || !monster.isAlive()) return false;
        if (home != null && monster.blockPosition().distSqr(home) > 40 * 40) return false;
        if (monster.getTarget() instanceof PosadResidentEntity resident) return Objects.equals(city, resident.city());
        if (monster.getTarget() instanceof Player) return true;
        return monster instanceof net.minecraft.world.entity.monster.Blaze
                || monster instanceof net.minecraft.world.entity.monster.WitherSkeleton
                || monster instanceof net.minecraft.world.entity.monster.Skeleton
                || monster instanceof net.minecraft.world.entity.monster.Ghast
                || monster instanceof net.minecraft.world.entity.monster.MagmaCube;
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
            for (PosadResidentEntity guard : server.getEntitiesOfClass(PosadResidentEntity.class, getBoundingBox().inflate(48),
                    e -> e.role().guard() && Objects.equals(e.city(), city))) guard.setTarget(p);
        }
        return actuallyHit;
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide || !role().guard() || city == null || !(level() instanceof ServerLevel server)) return;
        if (getTarget() instanceof Player player && !canFightPlayer(player)) setTarget(null);
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
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ROLE, Math.floorMod(tag.getInt("PosadRole"), PosadPopulationPlan.Role.values().length));
        entityData.set(WEAPON, Math.floorMod(tag.getInt("PosadWeapon"), PosadPopulationPlan.Weapon.values().length));
        memberKey = tag.getString("PosadMember");
        city = tag.hasUUID("PosadCity") ? tag.getUUID("PosadCity") : null;
        home = tag.contains("PosadHome") ? BlockPos.of(tag.getLong("PosadHome")) : null;
        if (home != null) restrictTo(home, 32);
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(role().health());
        equip();
    }
}
