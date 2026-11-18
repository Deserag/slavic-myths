package org.slavicmyths.navya.mixin;
import net.minecraft.server.players.PlayerList;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
/** Save only the changed recipient, never all online players. */
@Mixin(PlayerList.class)
public interface PlayerListSaveInvoker {@Invoker("save") void slavicmyths$save(ServerPlayer player);}
