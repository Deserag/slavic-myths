package org.slavicmyths.marine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Client prediction and server both honor the same finite water-surface support. */
@EventBusSubscriber(modid="slavicmyths")
public final class MarineArmorEffects {
    private static final ResourceLocation SWIM =
        ResourceLocation.fromNamespaceAndPath("slavicmyths", "marine_swim_speed");

    public static boolean canStand(boolean full, boolean sneak, boolean swimming, boolean submerged) {
        return full && !sneak && !swimming && !submerged;
    }
    /** Cannot support a player above air, lava or below the top water layer. */
    public static double surfaceHeight(Level level, double x, double feetY, double z) {
        BlockPos below = BlockPos.containing(x, feetY - .20D, z);
        FluidState fluid = level.getFluidState(below);
        if (!fluid.is(FluidTags.WATER)) return Double.NaN;
        double top = below.getY() + fluid.getHeight(level, below);
        return feetY >= top - .30D && feetY <= top + .24D ? top + .025D : Double.NaN;
    }

    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        boolean full = player.isAlive() && !player.isSpectator() && MarineArmor.full(player);
        boolean canStand = canStand(full, player.isShiftKeyDown(), player.isSwimming(),
            player.isEyeInFluid(FluidTags.WATER));
        double support = canStand ? surfaceHeight(player.level(), player.getX(), player.getY(),
                player.getZ()) : Double.NaN;
        boolean onWater = !Double.isNaN(support);
        if (onWater) {
            if (player.getY() < support) player.setPos(player.getX(), support, player.getZ());
            var velocity = player.getDeltaMovement();
            if (velocity.y < 0D) player.setDeltaMovement(velocity.x, 0D, velocity.z);
            player.setOnGround(true);
            player.fallDistance = 0F;
        }
        boolean wet = full && (player.isInWaterOrBubble() || player.isEyeInFluid(FluidTags.WATER));
        swimSpeed(player, wet);
        if (!(player instanceof ServerPlayer server)) return;
        boolean underwater = wet && player.isEyeInFluid(FluidTags.WATER);
        effect(server, "speed", MobEffects.MOVEMENT_SPEED, onWater, 3, 32);
        effect(server, "breath", MobEffects.WATER_BREATHING, wet, 0, 45);
        effect(server, "vision", MobEffects.NIGHT_VISION, wet, 0, 260);
        effect(server, "dolphin", MobEffects.DOLPHINS_GRACE, underwater, 0, 45);
        effect(server, "haste", MobEffects.DIG_SPEED, underwater, 0, 45);
    }

    private static void swimSpeed(Player player, boolean enable) {
        var attr = player.getAttribute(NeoForgeMod.SWIM_SPEED);
        if (attr == null) return;
        if (enable && attr.getModifier(SWIM) == null)
            attr.addTransientModifier(new AttributeModifier(SWIM, .35D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (!enable && attr.getModifier(SWIM) != null) attr.removeModifier(SWIM);
    }

    /** Each marker is present only for a potion added by this armor. */
    private static void effect(ServerPlayer player, String name, Holder<MobEffect> effect,
            boolean active, int amplifier, int ticks) {
        CompoundTag nbt = player.getPersistentData();
        String key = "Marine520_" + name;
        MobEffectInstance old = player.getEffect(effect);
        if (!active) {
            if (nbt.getBoolean(key)) {
                if (old != null && old.getAmplifier() == amplifier && old.getDuration() <= ticks)
                    player.removeEffect(effect);
                nbt.remove(key);
            }
            return;
        }
        if (old == null) {
            player.addEffect(new MobEffectInstance(effect, ticks, amplifier, true, false, true));
            nbt.putBoolean(key, true);
        } else if (nbt.getBoolean(key) && old.getAmplifier() == amplifier
                && old.getDuration() < (ticks == 260 ? 220 : 15)) {
            player.addEffect(new MobEffectInstance(effect, ticks, amplifier, true, false, true));
        }
    }
    private MarineArmorEffects() {}
}
