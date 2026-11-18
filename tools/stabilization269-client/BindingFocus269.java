package org.slavicmyths.verify.client.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** Only the mod packet sender sees synthetic test focus; Minecraft never grabs desktop input. */
@Mixin(org.slavicmyths.client.SivkaClient.class)
public abstract class BindingFocus269 {
 @Redirect(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/client/Minecraft;isWindowActive()Z"))
 private static boolean qaBindingFocus(net.minecraft.client.Minecraft mc){return org.slavicmyths.verify.client.Client269.CONTROLS?org.slavicmyths.verify.client.Controls269.driving:mc.isWindowActive();}
}
