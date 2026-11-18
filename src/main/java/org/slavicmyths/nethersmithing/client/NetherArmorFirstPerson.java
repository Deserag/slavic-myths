package org.slavicmyths.nethersmithing.client;
import net.neoforged.api.distmarker.Dist;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.fml.common.EventBusSubscriber;import org.slavicmyths.nethersmithing.NetherArmor;import net.minecraft.world.entity.*;import net.minecraft.client.renderer.*;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class NetherArmorFirstPerson {
 @SubscribeEvent public static void arm(net.neoforged.neoforge.client.event.RenderArmEvent e){var player=e.getPlayer();var stack=player.getItemBySlot(EquipmentSlot.CHEST);if(!(stack.getItem() instanceof NetherArmor.Piece piece)||player.isInvisible())return;var model=piece.king?NetherArmorClient.model(EquipmentSlot.CHEST,NetherArmorClient.slim(player)):NetherArmorGlow.standard(EquipmentSlot.CHEST);if(model instanceof NetherArmorClient.Model m)m.animate(player);var arm=e.getArm()==HumanoidArm.LEFT?model.leftArm:model.rightArm;arm.xRot=arm.yRot=arm.zRot=0;arm.visible=true;
  arm.render(e.getPoseStack(),net.minecraft.client.renderer.entity.ItemRenderer.getArmorFoilBuffer(e.getMultiBufferSource(),RenderType.armorCutoutNoCull(NetherArmorGlow.texture(piece,false)),stack.hasFoil()),e.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);arm.render(e.getPoseStack(),e.getMultiBufferSource().getBuffer(RenderType.eyes(NetherArmorGlow.texture(piece,true))),LightTexture.FULL_BRIGHT,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
 }
 private NetherArmorFirstPerson(){}
}
