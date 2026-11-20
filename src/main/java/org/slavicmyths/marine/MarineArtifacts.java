package org.slavicmyths.marine;

import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;

/** #523: bounded loaded-entity compass and manually activated underwater artifacts. */
public final class MarineArtifacts {
    public static final DeferredHolder<Item, Item> COMPASS =
        ModItems.ITEMS.register("marine_compass", Compass::new);
    public static final DeferredHolder<Item, Item> LANTERN =
        ModItems.ITEMS.register("depths_lantern", Lantern::new);
    public static final DeferredHolder<Item, Item> BOWL =
        ModItems.ITEMS.register("breathing_bowl", Bowl::new);
    private static final String COMPASS_READY = "SlavicMarineCompassReady";
    private static final String LANTERN_READY = "SlavicMarineLanternReady";
    private static final String BOWL_READY = "SlavicMarineBowlReady";

    public static void init() {}

    private static long now(ServerPlayer owner) {
        return owner.server.overworld().getGameTime();
    }

    private static boolean ready(ServerPlayer player, String key) {
        return now(player) >= player.getPersistentData().getLong(key);
    }
    private static void cooldown(ServerPlayer player, Item item, String key, int ticks) {
        player.getPersistentData().putLong(key, now(player) + ticks);
        player.getCooldowns().addCooldown(item, ticks);
    }

    private abstract static class Artifact extends Item {
        protected Artifact(Item.Properties properties) { super(properties); }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                List<Component> lines, TooltipFlag flags) {
            super.appendHoverText(stack, context, lines, flags);
            lines.add(Component.translatable(stack.getDescriptionId() + ".desc")
                .withStyle(ChatFormatting.AQUA));
        }
    }

    private static boolean boss(LivingEntity entity) {
        return entity instanceof PharaohkaEntity pharaohka && pharaohka.leader()
            || entity instanceof DeepSeaSerpentEntity
            || entity instanceof OceanZmeyEntity
            || entity instanceof StratimBirdEntity
            || entity instanceof TsarPikeEntity
            || entity instanceof ZhelvaEntity;
    }

    private static final class Compass extends Artifact {
        private Compass() { super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)); }

        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
            if (player instanceof ServerPlayer owner) {
                if (!ready(owner, COMPASS_READY)) return InteractionResultHolder.fail(stack);
                // getEntitiesOfClass searches loaded entities only, never forces a chunk.
                ServerLevel world = owner.serverLevel();
                var closest = world.getEntitiesOfClass(LivingEntity.class,
                        owner.getBoundingBox().inflate(96),
                        candidate -> candidate.isAlive() && boss(candidate))
                    .stream().min(Comparator.comparingDouble(owner::distanceToSqr));
                if (closest.isPresent()) {
                    var target = closest.get();
                    owner.displayClientMessage(Component.translatable(
                        "item.slavicmyths.marine_compass.found", target.getDisplayName(),
                        (int)Math.sqrt(owner.distanceToSqr(target)),
                        target.blockPosition().getX(), target.blockPosition().getZ()), true);
                    world.sendParticles(ParticleTypes.NAUTILUS, owner.getX(), owner.getEyeY(),
                        owner.getZ(), 15, .5, .5, .5, .03);
                } else owner.displayClientMessage(Component.translatable(
                    "item.slavicmyths.marine_compass.none"), true);
                cooldown(owner, this, COMPASS_READY, 100);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    }

    private static final class Lantern extends Artifact {
        private Lantern() { super(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)); }

        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
            if (player instanceof ServerPlayer owner) {
                if (!owner.isInWaterOrBubble() || !ready(owner, LANTERN_READY))
                    return InteractionResultHolder.fail(stack);
                owner.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1800, 0));
                owner.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 700, 0));
                owner.serverLevel().sendParticles(ParticleTypes.GLOW, owner.getX(),
                    owner.getEyeY(), owner.getZ(), 22, .5, .5, .5, .03);
                cooldown(owner, this, LANTERN_READY, 1800);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    }

    private static final class Bowl extends Artifact {
        private Bowl() { super(new Item.Properties().durability(24).rarity(Rarity.RARE)); }

        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
            if (player instanceof ServerPlayer owner) {
                if (!ready(owner, BOWL_READY)) return InteractionResultHolder.fail(stack);
                owner.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 2400, 0));
                stack.hurtAndBreak(1, owner, LivingEntity.getSlotForHand(hand));
                cooldown(owner, this, BOWL_READY, 6000);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    }
    private MarineArtifacts() {}
}
