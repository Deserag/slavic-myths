package org.slavicmyths.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.core.registries.BuiltInRegistries;
import com.mojang.math.Axis;
/** A single two-handed model, with both wrists attached to the same moving grip. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class DesertTwoHandClient {
 @SubscribeEvent public static void hand(RenderHandEvent e){var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return;
  String id=BuiltInRegistries.ITEM.getKey(p.getMainHandItem().getItem()).getPath();
  if(!java.util.Set.of("buried_hammer","desert_halberd","desert_scythe","lamya_weapon").contains(id)||!p.getOffhandItem().isEmpty())return;
  e.setCanceled(true);if(e.getHand()!=InteractionHand.MAIN_HAND)return;
  boolean left=p.getMainArm()==HumanoidArm.LEFT;int sign=left?-1:1;float arc=(float)Math.sin(Math.sqrt(e.getSwingProgress())*Math.PI);
  var pose=e.getPoseStack();pose.pushPose();
  pose.translate(sign*.38,-.5,-1.02);pose.mulPose(Axis.ZP.rotationDegrees(sign*-14));pose.mulPose(Axis.XP.rotationDegrees(-38*arc));
  WeaponHands.renderGrip(e,sign);
  pose.pushPose();pose.translate(sign*.02,.2,0);pose.mulPose(Axis.ZP.rotationDegrees(-sign*50));WeaponHands.renderGrip(e,-sign);pose.popPose();
  mc.getEntityRenderDispatcher().getItemInHandRenderer().renderItem(p,p.getMainHandItem(),left?ItemDisplayContext.FIRST_PERSON_LEFT_HAND:ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,left,pose,e.getMultiBufferSource(),e.getPackedLight());pose.popPose();
 }
 private DesertTwoHandClient(){}
}
