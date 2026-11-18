package org.slavicmyths.ships.mixin;

import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.slavicmyths.ships.FirstShipEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A native ship's server physics owns its pose. Vanilla boat rider packets
 * contain an older client pose and must not move the detached hull backward. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MovingShipVehiclePacketMixin {
    @Shadow public net.minecraft.server.level.ServerPlayer player;

    @Inject(method = "handleMoveVehicle", at = @At("HEAD"), cancellable = true)
    private void slavicmyths$ignoreNativeShipVehiclePose(ServerboundMoveVehiclePacket packet,
            CallbackInfo callback) {
        if (player.getVehicle() instanceof FirstShipEntity ship && ship.isNativeHull())
            callback.cancel();
    }
}
