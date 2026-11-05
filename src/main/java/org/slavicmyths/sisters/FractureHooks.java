package org.slavicmyths.sisters;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slavicmyths.registry.ModEffects;

/** A third-stage fracture stops only the physically left arm, not all controls. */
@EventBusSubscriber(modid = "slavicmyths")
public final class FractureHooks {
    public static final int DURATION = 600;
    public static void apply(Player player) {
        if (player.level().isClientSide || player.isCreative() || player.isSpectator()) return;
        MobEffectInstance current = player.getEffect(ModEffects.FRACTURE);
        int amplifier = current == null ? 0 : Math.min(2, current.getAmplifier() + 1);
        player.addEffect(new MobEffectInstance(ModEffects.FRACTURE, DURATION, amplifier, false, true, true));
    }
    public static int stage(Player player) {
        MobEffectInstance instance = player.getEffect(ModEffects.FRACTURE);
        return instance == null ? 0 : Math.min(3, instance.getAmplifier() + 1);
    }
    public static boolean disabled(Player player, InteractionHand hand) {
        return stage(player) >= 3 && (player.getMainArm() == HumanoidArm.LEFT ? hand == InteractionHand.MAIN_HAND : hand == InteractionHand.OFF_HAND);
    }
    @SubscribeEvent public static void interactItem(PlayerInteractEvent.RightClickItem event) {
        if (disabled(event.getEntity(), event.getHand())) event.setCanceled(true);
    }
    @SubscribeEvent public static void interactBlock(PlayerInteractEvent.RightClickBlock event) {
        if (disabled(event.getEntity(), event.getHand())) event.setCanceled(true);
    }
    @SubscribeEvent public static void interactEntity(PlayerInteractEvent.EntityInteract event) {
        if (disabled(event.getEntity(), event.getHand())) event.setCanceled(true);
    }
    @SubscribeEvent public static void attack(AttackEntityEvent event) {
        if (disabled(event.getEntity(), InteractionHand.MAIN_HAND)) event.setCanceled(true);
    }
    @SubscribeEvent public static void stopDisabledUse(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post event) {
        Player player=event.getEntity();
        if (player.isUsingItem() && disabled(player,player.getUsedItemHand())) player.stopUsingItem();
    }
    @SubscribeEvent public static void mine(PlayerInteractEvent.LeftClickBlock event) {
        if (disabled(event.getEntity(), InteractionHand.MAIN_HAND)) event.setCanceled(true);
    }
    private FractureHooks() {}
}
