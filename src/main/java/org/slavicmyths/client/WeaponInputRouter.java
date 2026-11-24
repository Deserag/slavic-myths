package org.slavicmyths.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.combat.WeaponInputHandler;
import org.slavicmyths.combat.WeaponInputNetwork;

/**
 * The only mouse-combination entry point for weapons opting into the input
 * contract. Cancel vanilla interactions before mining, melee and block use.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class WeaponInputRouter {
    private static boolean attackHeld;
    private static boolean useHeld;

    @SubscribeEvent
    public static void interaction(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() && !event.isUseItem()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null
                || !(mc.player.getMainHandItem().getItem() instanceof WeaponInputHandler handler)) return;

        boolean shift = mc.player.isShiftKeyDown();
        WeaponInputHandler.Action action = event.isAttack()
                ? (shift ? WeaponInputHandler.Action.SHIFT_LEFT_CLICK
                         : WeaponInputHandler.Action.LEFT_CLICK)
                : (shift ? WeaponInputHandler.Action.SHIFT_RIGHT_CLICK
                         : WeaponInputHandler.Action.RIGHT_CLICK);
        // A weapon can preserve ordinary melee or held right-click by opting out
        // of interception for that individual action only.
        if (!handler.intercepts(mc.player.getMainHandItem(), action, mc.hitResult == null
                ? net.minecraft.world.phys.HitResult.Type.MISS : mc.hitResult.getType())) return;
        // NeoForge calls right-click once per hand even when the other call
        // was cancelled. Never let the offhand handle the same click.
        event.setCanceled(true);
        event.setSwingHand(false);
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        if (event.isAttack()) {
            if (attackHeld) return;
            attackHeld = true;
        } else {
            if (useHeld) return;
            useHeld = true;
        }
        PacketDistributor.sendToServer(new WeaponInputNetwork.Input(action.id()));
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null
                || !(mc.player.getMainHandItem().getItem() instanceof WeaponInputHandler)) {
            attackHeld = false;
            useHeld = false;
            return;
        }
        if (!mc.options.keyAttack.isDown()) attackHeld = false;
        if (!mc.options.keyUse.isDown()) useHeld = false;
    }

    private WeaponInputRouter() {}
}
