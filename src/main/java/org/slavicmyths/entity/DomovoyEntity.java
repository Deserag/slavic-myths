package org.slavicmyths.entity;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.slavicmyths.registry.ModSounds;

public final class DomovoyEntity extends CreatureEntity {
    private final java.util.Map<java.util.UUID, Relation> relations = new java.util.HashMap<>();
    private net.minecraft.util.math.BlockPos home;
    private java.util.UUID owner;
    private String homeDimension = "";
    private long nextBlessing;
    private static final class Relation {
        int reputation, attacks;
        long nextGift;
    }
    public int reputation(PlayerEntity player) {
        Relation r = relations.get(player.getUUID());
        return r == null ? 0 : r.reputation;
    }
    public boolean canBind(PlayerEntity player) {
        return reputation(player) >= 40 && (owner == null || owner.equals(player.getUUID()));
    }
    public void bind(PlayerEntity player, net.minecraft.util.math.BlockPos pos) {
        if (level.isClientSide || !canBind(player)) return;
        owner = player.getUUID(); home = pos.immutable();
        homeDimension = level.dimension().location().toString();
        restrictTo(home, 8); setPersistenceRequired();
        org.slavicmyths.progression.Knowledge.award(player, "bind_hearth");
        player.displayClientMessage(new net.minecraft.util.text.TranslationTextComponent("domovoy.slavicmyths.bound"), true);
    }
    public DomovoyEntity(EntityType<? extends DomovoyEntity> type, World world) { super(type, world); }
    public static AttributeModifierMap.MutableAttribute attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 12).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 12).add(Attributes.ATTACK_DAMAGE, 2);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new SwimGoal(this));
        goalSelector.addGoal(1, new PanicGoal(this, 1.4));
        goalSelector.addGoal(2, new AvoidEntityGoal<PlayerEntity>(this, PlayerEntity.class, 6, 1.0, 1.3,
                candidate -> candidate instanceof PlayerEntity && reputation((PlayerEntity) candidate) < 0));
        goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(3, new WaterAvoidingRandomWalkingGoal(this, 0.7));
        goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        goalSelector.addGoal(5, new LookRandomlyGoal(this));
    }
    @Override protected ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        if (level.isClientSide) return ActionResultType.SUCCESS;
        org.slavicmyths.progression.Knowledge.award(player, "meet_domovoy");
        net.minecraft.item.ItemStack stack = player.getItemInHand(hand);
        net.minecraft.item.Item item = stack.getItem();
        boolean food = item == net.minecraft.item.Items.BREAD || item == net.minecraft.item.Items.MILK_BUCKET
                || item == net.minecraft.item.Items.HONEY_BOTTLE || item == net.minecraft.item.Items.COOKIE;
        Relation r = relations.computeIfAbsent(player.getUUID(), id -> new Relation());
        String message;
        long now = level.getGameTime();
        if (food && r.reputation <= -40) {
            message = "refused"; playSound(ModSounds.DOMOVOY_HURT.get(), 0.4F, 0.9F);
        } else if (food && now < r.nextGift) message = "wait";
        else if (food) {
            r.nextGift = now + 1200;
            r.reputation = Math.min(100, r.reputation + (org.slavicmyths.rpg.PathData.has(player,"offering")?12:10));
            if (!player.abilities.instabuild) {
                stack.shrink(1);
                net.minecraft.item.Item container = item == net.minecraft.item.Items.MILK_BUCKET ? net.minecraft.item.Items.BUCKET
                        : item == net.minecraft.item.Items.HONEY_BOTTLE ? net.minecraft.item.Items.GLASS_BOTTLE : null;
                if (container != null) {
                    net.minecraft.item.ItemStack empty = new net.minecraft.item.ItemStack(container);
                    if (!player.inventory.add(empty)) player.drop(empty, false);
                }
            }
            message = "accepted";
            playSound(net.minecraft.util.SoundEvents.VILLAGER_YES, 0.4F, 1.3F);
            ((net.minecraft.world.server.ServerWorld) level).sendParticles(net.minecraft.particles.ParticleTypes.HEART,
                    getX(), getY() + 1, getZ(), 3, 0.2, 0.2, 0.2, 0);
            org.slavicmyths.progression.Knowledge.award(player, "offer_domovoy");
            if (r.reputation >= 40) org.slavicmyths.progression.Knowledge.award(player, "friend_domovoy");
        } else {
            message = r.reputation <= -40 ? "hostile" : r.reputation < 0 ? "wary" : r.reputation >= 80 ? "devoted" : r.reputation >= 40 ? "friendly" : "neutral";
            if (stack.isEmpty() && home != null && owner != null && owner.equals(player.getUUID()) && r.reputation >= 80
                    && homeDimension.equals(level.dimension().location().toString())
                    && now >= nextBlessing && home.distSqr(blockPosition()) <= 64
                    && level.hasChunkAt(home) && level.getBlockState(home).is(org.slavicmyths.registry.ModBlocks.HEARTH.get())) {
                nextBlessing = now + 12000;
                player.addEffect(new net.minecraft.potion.EffectInstance(net.minecraft.potion.Effects.REGENERATION, 100, 0));
                message = "blessing";
            }
        }
        player.displayClientMessage(new net.minecraft.util.text.TranslationTextComponent("domovoy.slavicmyths." + message), true);
        return ActionResultType.sidedSuccess(level.isClientSide);
    }
    @Override public boolean hurt(DamageSource damage, float amount) {
        boolean hit = super.hurt(damage, amount);
        if (hit && !level.isClientSide && damage.getEntity() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) damage.getEntity();
            Relation r = relations.computeIfAbsent(player.getUUID(), id -> new Relation());
            r.attacks = Math.min(5, r.attacks + 1);
            r.reputation = Math.max(-100, r.reputation - 10 - r.attacks * 5);
        }
        return hit;
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundNBT tag) {
        super.addAdditionalSaveData(tag);
        net.minecraft.nbt.ListNBT entries = new net.minecraft.nbt.ListNBT();
        relations.forEach((id, r) -> {
            net.minecraft.nbt.CompoundNBT entry = new net.minecraft.nbt.CompoundNBT();
            entry.putUUID("Player", id); entry.putInt("Reputation", r.reputation);
            entry.putInt("Attacks", r.attacks); entry.putLong("NextGift", r.nextGift); entries.add(entry);
        });
        tag.put("Relations", entries); tag.putLong("NextBlessing", nextBlessing);
        if (home != null && owner != null) {
            tag.putLong("Home", home.asLong()); tag.putUUID("Owner", owner); tag.putString("HomeDimension", homeDimension);
        }
    }
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundNBT tag) {
        super.readAdditionalSaveData(tag); relations.clear();
        for (net.minecraft.nbt.INBT value : tag.getList("Relations", 10)) {
            net.minecraft.nbt.CompoundNBT entry = (net.minecraft.nbt.CompoundNBT) value;
            if (!entry.hasUUID("Player")) continue;
            Relation r = new Relation(); r.reputation = Math.max(-100, Math.min(100, entry.getInt("Reputation")));
            r.attacks = Math.max(0, Math.min(5, entry.getInt("Attacks"))); r.nextGift = entry.getLong("NextGift");
            relations.put(entry.getUUID("Player"), r);
        }
        nextBlessing = tag.getLong("NextBlessing"); home = null; owner = null;
        if (tag.contains("Home") && tag.hasUUID("Owner")) {
            home = net.minecraft.util.math.BlockPos.of(tag.getLong("Home")); owner = tag.getUUID("Owner");
            homeDimension = tag.contains("HomeDimension") ? tag.getString("HomeDimension") : level.dimension().location().toString();
            if (homeDimension.equals(level.dimension().location().toString())) restrictTo(home, 8);
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
