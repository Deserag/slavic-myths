package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.network.chat.Component;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.ModSounds;

public final class DomovoyEntity extends PathfinderMob {
    private final java.util.Map<java.util.UUID, Relation> relations = new java.util.HashMap<>();
    private net.minecraft.core.BlockPos home;
    private java.util.UUID owner;
    private String homeDimension = "";
    private long nextBlessing;
    private static final class Relation {
        int reputation, attacks;
        long nextGift;
    }
    public int reputation(Player player) {
        Relation r = relations.get(player.getUUID());
        return r == null ? 0 : r.reputation;
    }
    public boolean canBind(Player player) {
        return reputation(player) >= 40 && (owner == null || owner.equals(player.getUUID()));
    }
    public void bind(Player player, net.minecraft.core.BlockPos pos) {
        if (level().isClientSide || !canBind(player)) return;
        owner = player.getUUID(); home = pos.immutable();
        homeDimension = level().dimension().location().toString();
        restrictTo(home, 8); setPersistenceRequired();
        org.slavicmyths.progression.Knowledge.award(player, "bind_hearth");
        player.displayClientMessage(Component.translatable("domovoy.slavicmyths.bound"), true);
    }
    public DomovoyEntity(EntityType<? extends DomovoyEntity> type, Level world) { super(type, world); }
    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 12).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 12).add(Attributes.ATTACK_DAMAGE, 2);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        goalSelector.addGoal(2, new AvoidEntityGoal<Player>(this, Player.class, 6, 1.0, 1.3,
                candidate -> candidate instanceof Player && reputation((Player) candidate) < 0));
        goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
    }
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level().isClientSide) return InteractionResult.SUCCESS;
        org.slavicmyths.progression.Knowledge.award(player, "meet_domovoy");
        net.minecraft.world.item.ItemStack stack = player.getItemInHand(hand);
        net.minecraft.world.item.Item item = stack.getItem();
        boolean food = item == net.minecraft.world.item.Items.BREAD || item == net.minecraft.world.item.Items.MILK_BUCKET
                || item == net.minecraft.world.item.Items.HONEY_BOTTLE || item == net.minecraft.world.item.Items.COOKIE;
        Relation r = relations.computeIfAbsent(player.getUUID(), id -> new Relation());
        String message;
        long now = level().getGameTime();
        if (food && r.reputation <= -40) {
            message = "refused"; playSound(ModSounds.DOMOVOY_HURT.get(), 0.4F, 0.9F);
        } else if (food && now < r.nextGift) message = "wait";
        else if (food) {
            r.nextGift = now + 1200;
            r.reputation = Math.min(100, r.reputation + (org.slavicmyths.rpg.PathData.has(player,"offering")?12:10));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                net.minecraft.world.item.Item container = item == net.minecraft.world.item.Items.MILK_BUCKET ? net.minecraft.world.item.Items.BUCKET
                        : item == net.minecraft.world.item.Items.HONEY_BOTTLE ? net.minecraft.world.item.Items.GLASS_BOTTLE : null;
                if (container != null) {
                    net.minecraft.world.item.ItemStack empty = new net.minecraft.world.item.ItemStack(container);
                    if (!player.getInventory().add(empty)) player.drop(empty, false);
                }
            }
            message = "accepted";
            playSound(net.minecraft.sounds.SoundEvents.VILLAGER_YES, 0.4F, 1.3F);
            ((net.minecraft.server.level.ServerLevel) level()).sendParticles(net.minecraft.core.particles.ParticleTypes.HEART,
                    getX(), getY() + 1, getZ(), 3, 0.2, 0.2, 0.2, 0);
            org.slavicmyths.progression.Knowledge.award(player, "offer_domovoy");
            if (r.reputation >= 40) org.slavicmyths.progression.Knowledge.award(player, "friend_domovoy");
        } else {
            message = r.reputation <= -40 ? "hostile" : r.reputation < 0 ? "wary" : r.reputation >= 80 ? "devoted" : r.reputation >= 40 ? "friendly" : "neutral";
            if (stack.isEmpty() && home != null && owner != null && owner.equals(player.getUUID()) && r.reputation >= 80
                    && homeDimension.equals(level().dimension().location().toString())
                    && now >= nextBlessing && home.distSqr(blockPosition()) <= 64
                    && level().hasChunkAt(home) && level().getBlockState(home).is(org.slavicmyths.registry.ModBlocks.HEARTH.get())) {
                nextBlessing = now + 12000;
                player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 100, 0));
                message = "blessing";
            }
        }
        player.displayClientMessage(Component.translatable("domovoy.slavicmyths." + message), true);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
    @Override public boolean hurt(DamageSource damage, float amount) {
        boolean hit = super.hurt(damage, amount);
        if (hit && !level().isClientSide && damage.getEntity() instanceof Player) {
            Player player = (Player) damage.getEntity();
            Relation r = relations.computeIfAbsent(player.getUUID(), id -> new Relation());
            r.attacks = Math.min(5, r.attacks + 1);
            r.reputation = Math.max(-100, r.reputation - 10 - r.attacks * 5);
        }
        return hit;
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        net.minecraft.nbt.ListTag entries = new net.minecraft.nbt.ListTag();
        relations.forEach((id, r) -> {
            net.minecraft.nbt.CompoundTag entry = new net.minecraft.nbt.CompoundTag();
            entry.putUUID("Player", id); entry.putInt("Reputation", r.reputation);
            entry.putInt("Attacks", r.attacks); entry.putLong("NextGift", r.nextGift); entries.add(entry);
        });
        tag.put("Relations", entries); tag.putLong("NextBlessing", nextBlessing);
        if (home != null && owner != null) {
            tag.putLong("Home", home.asLong()); tag.putUUID("Owner", owner); tag.putString("HomeDimension", homeDimension);
        }
    }
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag); relations.clear();
        for (net.minecraft.nbt.Tag value : tag.getList("Relations", 10)) {
            net.minecraft.nbt.CompoundTag entry = (net.minecraft.nbt.CompoundTag) value;
            if (!entry.hasUUID("Player")) continue;
            Relation r = new Relation(); r.reputation = Math.max(-100, Math.min(100, entry.getInt("Reputation")));
            r.attacks = Math.max(0, Math.min(5, entry.getInt("Attacks"))); r.nextGift = entry.getLong("NextGift");
            relations.put(entry.getUUID("Player"), r);
        }
        nextBlessing = tag.getLong("NextBlessing"); home = null; owner = null;
        if (tag.contains("Home") && tag.hasUUID("Owner")) {
            home = net.minecraft.core.BlockPos.of(tag.getLong("Home")); owner = tag.getUUID("Owner");
            homeDimension = tag.contains("HomeDimension") ? tag.getString("HomeDimension") : level().dimension().location().toString();
            if (homeDimension.equals(level().dimension().location().toString())) restrictTo(home, 8);
            setPersistenceRequired();
        }
    }
    @Override public boolean removeWhenFarAway(double distance) { return home == null; }
    @Override public int getMaxSpawnClusterSize() { return 1; }
    @Override public int getAmbientSoundInterval() { return 240; }
    @Override protected float getSoundVolume() { return 0.35F; }
    @Override protected SoundEvent getAmbientSound() { return ModSounds.DOMOVOY_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource damage) { return ModSounds.DOMOVOY_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.DOMOVOY_DEATH.get(); }
}
