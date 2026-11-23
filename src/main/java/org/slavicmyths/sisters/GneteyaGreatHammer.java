package org.slavicmyths.sisters;

import java.util.List;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.slavicmyths.rpg.Abilities;
import org.slavicmyths.rpg.PathData;

/** Two-handed Gneteya reward: three independent, server-authoritative skills. */
@EventBusSubscriber(modid = "slavicmyths")
public final class GneteyaGreatHammer extends SwordItem {
    private static final String SLAM_READY = "GneteyaGreatHammerReady";
    private static final String FRACTURE_READY = "GneteyaGreatHammerFractureReady";
    private static final String SENTENCE_READY = "GneteyaGreatHammerSentenceReady";
    private static final String SLAM_HUD = "GneteyaSkillSlamReady";
    private static final String FRACTURE_HUD = "GneteyaSkillFractureReady";
    private static final String SENTENCE_HUD = "GneteyaSkillSentenceReady";
    private static final String CAST_SLAM = "GneteyaCastingSlam";
    private static final String HAMMER_DEBUFF = "GneteyaHammerDebuffUntil";
    private static final int SLAM_COOLDOWN = 240;
    private static final int FRACTURE_COOLDOWN = 280;
    private static final int SENTENCE_COOLDOWN = 360;

    public GneteyaGreatHammer() {
        super(Tiers.NETHERITE, new Properties().durability(1800).rarity(Rarity.EPIC)
            .attributes(SwordItem.createAttributes(Tiers.NETHERITE, 14, -3.2F)));
    }

    private static boolean ready(ServerPlayer player, String key) {
        return PathData.data(player).getLong(key) <= player.level().getGameTime();
    }

    private static boolean valid(ServerPlayer player, LivingEntity target) {
        return target != player && target.isAlive() && Abilities.canHit(player, target)
            && player.hasLineOfSight(target);
    }

    private static boolean validOwner(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator()
            && player.getMainHandItem().getItem() instanceof GneteyaGreatHammer
            && player.getOffhandItem().isEmpty();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (!selected || !(entity instanceof ServerPlayer player)
                || player.getMainHandItem() != stack) return;
        CompoundTag persistent = PathData.data(player);
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String[] keys = {SLAM_READY, FRACTURE_READY, SENTENCE_READY};
        String[] hudKeys = {SLAM_HUD, FRACTURE_HUD, SENTENCE_HUD};
        boolean modified = false;
        for (int i = 0; i < keys.length; i++) {
            long expires = persistent.getLong(keys[i]);
            if (data.getLong(hudKeys[i]) != expires) {
                data.putLong(hudKeys[i], expires);
                modified = true;
            }
        }
        if (modified) stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    @SubscribeEvent
    public static void preventOffhandAttack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && player.getMainHandItem().getItem() instanceof GneteyaGreatHammer
                && !player.getOffhandItem().isEmpty()) {
            event.setCanceled(true);
            player.displayClientMessage(
                Component.translatable("tooltip.slavicmyths.gneteya_great_hammer.two_handed"), true);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        if (!player.getOffhandItem().isEmpty() || !player.isAlive()
                || player.isSpectator()) return InteractionResultHolder.fail(stack);

        boolean slam = player.isShiftKeyDown();
        String key = slam ? SLAM_READY : SENTENCE_READY;
        String hudKey = slam ? SLAM_HUD : SENTENCE_HUD;
        if (stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
                .copyTag().getLong(hudKey) > level.getGameTime()) {
            return InteractionResultHolder.fail(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (!ready(serverPlayer, key)) return InteractionResultHolder.fail(stack);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putBoolean(CAST_SLAM, slam));
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.SPEAR;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (!(entity instanceof ServerPlayer player) || !validOwner(player)
                || player.getMainHandItem().getItem() != this) return;
        boolean slam = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            .copyTag().getBoolean(CAST_SLAM);
        int heldTicks = getUseDuration(stack, player) - remaining;
        if (slam) {
            if (heldTicks >= 20 && ready(player, SLAM_READY)) crushingBlow(player, stack);
        } else {
            if (heldTicks >= 14 && ready(player, SENTENCE_READY)) sentence(player, stack);
        }
    }

    /** Shift + LMB: radial attack, max six targets, no block changes. */
    public static void boneFracture(ServerPlayer player) {
        if (!validOwner(player) || !player.isShiftKeyDown() || player.isUsingItem()
                || player.containerMenu != player.inventoryMenu
                || !ready(player, FRACTURE_READY)) return;
        long now = player.serverLevel().getGameTime();
        PathData.data(player).putLong(FRACTURE_READY, now + FRACTURE_COOLDOWN);
        int hits = 0;
        for (LivingEntity target : player.serverLevel().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(5.0D, 3.0D, 5.0D),
                target -> valid(player, target) && player.distanceToSqr(target) <= 25.0D)) {
            if (!target.hurt(player.damageSources().playerAttack(player), 34.0F)) continue;
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), player);
            target.getPersistentData().putLong(HAMMER_DEBUFF, now + 60);
            target.push(0.0D, 0.46D, 0.0D);
            target.hurtMarked = true;
            if (++hits >= 6) break;
        }
        for (int ring = 0; ring < 3; ring++) {
            double radius = 1.3D + 1.7D * ring;
            int steps = 12 + ring * 8;
            for (int i = 0; i < steps; i++) {
                double angle = i * Math.PI * 2.0D / steps;
                double x = player.getX() + Math.cos(angle) * radius;
                double z = player.getZ() + Math.sin(angle) * radius;
                player.serverLevel().sendParticles(
                    new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                    x, player.getY() + 0.2D, z, 3, 0.08D, 0.24D, 0.08D, 0.02D);
            }
        }
        ItemStack stack = player.getMainHandItem();
        stack.hurtAndBreak(3, player, EquipmentSlot.MAINHAND);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    /** Shift + RMB: retained original 26 HP area impact with its own 12 s cooldown. */
    private static void crushingBlow(ServerPlayer player, ItemStack stack) {
        long now = player.serverLevel().getGameTime();
        PathData.data(player).putLong(SLAM_READY, now + SLAM_COOLDOWN);
        int hits = 0;
        for (LivingEntity target : player.serverLevel().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(4.0D),
                target -> valid(player, target) && player.distanceToSqr(target) <= 16.0D)) {
            if (!target.hurt(player.damageSources().playerAttack(player), 26.0F)) continue;
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), player);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), player);
            target.getPersistentData().putLong(HAMMER_DEBUFF, now + 80);
            if (++hits >= 6) break;
        }
        player.serverLevel().sendParticles(ParticleTypes.CRIT,
            player.getX(), player.getY() + 0.2D, player.getZ(), 40, 2.0D, 0.1D, 2.0D, 0.05D);
        stack.hurtAndBreak(3, player, EquipmentSlot.MAINHAND);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    /** RMB hold for at least 0.7 s: precision hit on the first aimed entity up to four blocks. */
    private static void sentence(ServerPlayer player, ItemStack stack) {
        long now = player.serverLevel().getGameTime();
        PathData.data(player).putLong(SENTENCE_READY, now + SENTENCE_COOLDOWN);
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(4.0D));
        LivingEntity closest = null;
        double closestDistance = 16.0D;
        for (LivingEntity candidate : player.serverLevel().getEntitiesOfClass(
                LivingEntity.class, player.getBoundingBox().inflate(4.5D),
                candidate -> valid(player, candidate))) {
            var intersect = candidate.getBoundingBox().inflate(0.25D).clip(eye, end);
            if (intersect.isEmpty()) continue;
            double distance = intersect.get().distanceToSqr(eye);
            if (distance > closestDistance) continue;
            closestDistance = distance;
            closest = candidate;
        }
        if (closest != null) {
            boolean weakenedByHammer = closest.getPersistentData().getLong(HAMMER_DEBUFF) > now
                && (closest.hasEffect(MobEffects.WEAKNESS)
                    || closest.hasEffect(MobEffects.MOVEMENT_SLOWDOWN));
            if (closest.hurt(player.damageSources().playerAttack(player),
                    weakenedByHammer ? 40.0F : 34.0F)) {
                for (int i = 0; i < 20; i++) {
                    double angle = 2.0D * Math.PI * i / 20.0D;
                    player.serverLevel().sendParticles(
                        new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BONE_BLOCK.defaultBlockState()),
                        closest.getX() + Math.cos(angle) * 0.9D,
                        closest.getY() + 0.15D,
                        closest.getZ() + Math.sin(angle) * 0.9D,
                        2, 0.03D, 0.15D, 0.03D, 0.02D);
                }
                player.serverLevel().sendParticles(ParticleTypes.SOUL,
                    closest.getX(), closest.getY() + closest.getBbHeight() * 0.5D,
                    closest.getZ(), 16, 0.3D, 0.25D, 0.3D, 0.02D);
            }
        }
        stack.hurtAndBreak(3, player, EquipmentSlot.MAINHAND);
        player.swing(InteractionHand.MAIN_HAND, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.slavicmyths.gneteya_great_hammer.two_handed"));
        lines.add(Component.translatable("tooltip.slavicmyths.gneteya_great_hammer.skill"));
        lines.add(Component.literal("Shift+ЛКМ — Костяной перелом: 34 HP, круг 5 блоков, 14 с."));
        lines.add(Component.literal("ПКМ (удержание 0,7 с) — Приговор Гнетеи: 34–40 HP, 18 с."));
    }
}
