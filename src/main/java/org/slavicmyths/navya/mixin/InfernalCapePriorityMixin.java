package org.slavicmyths.navya.mixin;
import org.spongepowered.asm.mixin.Mixin;import org.spongepowered.asm.mixin.injection.*;import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(net.minecraft.client.renderer.entity.layers.CapeLayer.class)
public abstract class InfernalCapePriorityMixin {
 @Inject(method="render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V",at=@At("HEAD"),cancellable=true)
 private void slavicmyths$royal(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.renderer.MultiBufferSource buffer,int light,net.minecraft.client.player.AbstractClientPlayer p,float a,float b,float c,float d,float e,float f,CallbackInfo ci){if(org.slavicmyths.nethersmithing.NetherArmor.king(p,net.minecraft.world.entity.EquipmentSlot.CHEST))ci.cancel();}
}
