package org.slavicmyths.village.mixin;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Runs synchronously after the cancellable NeoForge hook and committed mode assignment. */
@Mixin(ServerPlayer.class)
public abstract class YagaGameModeMixin {
 @Inject(method="setGameMode",at=@At("RETURN"))
 private void slavicmyths$yagaAccess(GameType requested,CallbackInfoReturnable<Boolean> ci){
  var p=(ServerPlayer)(Object)this;
  if(ci.getReturnValueZ()&&p.isCreative())org.slavicmyths.yaga.YagaDiscovery.refresh(p);
 }
}
