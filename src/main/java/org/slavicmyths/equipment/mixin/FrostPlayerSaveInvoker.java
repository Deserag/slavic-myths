package org.slavicmyths.equipment.mixin;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
/** Persist inventory and delivery receipt in the same player file before acknowledging mail. */
@Mixin(PlayerList.class)
public interface FrostPlayerSaveInvoker {
 @Invoker("save") void slavicmyths$saveFrost(ServerPlayer player);
}
