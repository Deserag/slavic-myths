package org.slavicmyths.equipment.mixin;
import net.minecraft.world.entity.player.*;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Lightweight inventory-change reaction; leaves vanilla inventory transactions intact. */
@Mixin(Inventory.class)
public abstract class WintersInventoryMixin {
 @Shadow @Final public Player player;
 @Inject(method="setChanged",at=@At("RETURN"))
 private void winters$changed(CallbackInfo ci){
  if(player instanceof ServerPlayer p&&p.server.getPlayerList().getPlayer(p.getUUID())==p){
   boolean before=org.slavicmyths.winters.WintersEvents.present(p);
   boolean after=p.isAlive()&&org.slavicmyths.winters.WintersEvents.inventory(p);
   if(before!=after){org.slavicmyths.winters.WintersEvents.refresh(p);org.slavicmyths.winters.WintersEvents.aura(p);}
  }
 }
}
