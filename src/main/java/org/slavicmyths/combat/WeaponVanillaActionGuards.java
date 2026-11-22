package org.slavicmyths.combat;

import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Defense in depth: neither a vanilla attack packet nor held block mining can
 * add a second physical hit to a routed weapon action.
 */
@EventBusSubscriber(modid = "slavicmyths")
public final class WeaponVanillaActionGuards {
    @SubscribeEvent
    public static void attackEntity(PlayerInteractEvent.AttackEntity event) {
        var player = event.getEntity();
        if (!(player.getMainHandItem().getItem() instanceof WeaponInputHandler handler)) return;
        WeaponInputHandler.Action action = player.isShiftKeyDown()
                ? WeaponInputHandler.Action.SHIFT_LEFT_CLICK
                : WeaponInputHandler.Action.LEFT_CLICK;
        if (handler.intercepts(action, HitResult.Type.ENTITY)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void attackBlock(PlayerInteractEvent.LeftClickBlock event) {
        var player = event.getEntity();
        if (!(player.getMainHandItem().getItem() instanceof WeaponInputHandler handler)) return;
        WeaponInputHandler.Action action = player.isShiftKeyDown()
                ? WeaponInputHandler.Action.SHIFT_LEFT_CLICK
                : WeaponInputHandler.Action.LEFT_CLICK;
        if (handler.intercepts(action, HitResult.Type.BLOCK)) event.setCanceled(true);
    }

    private WeaponVanillaActionGuards() {}
}
