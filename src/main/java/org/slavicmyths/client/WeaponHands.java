package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.gorynych.*;

@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class WeaponHands {
 @SubscribeEvent public static void click(InputEvent.InteractionKeyMappingTriggered e){
  var p=Minecraft.getInstance().player;if(p==null||!e.isAttack()||!p.isShiftKeyDown())return;
  if(p.getMainHandItem().getItem() instanceof SerpentCrossbow bow&&bow.special){e.setCanceled(true);e.setSwingHand(false);net.neoforged.neoforge.network.PacketDistributor.sendToServer(new CrossbowVolley.Toggle());}
 }
 @SubscribeEvent public static void hand(RenderHandEvent e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null||!(e.getItemStack().getItem() instanceof SerpentCrossbow))return;
  e.setCanceled(true);var pose=e.getPoseStack();boolean left=(e.getHand()==InteractionHand.MAIN_HAND)==(p.getMainArm()==HumanoidArm.LEFT);int sign=left?-1:1;
  pose.pushPose();float swing=net.minecraft.util.Mth.sin((float)Math.sqrt(e.getSwingProgress())*(float)Math.PI);
  float pull=p.isUsingItem()&&p.getUseItem()==e.getItemStack()?Math.clamp((e.getItemStack().getUseDuration(p)-p.getUseItemRemainingTicks()+e.getPartialTick())/(net.minecraft.world.item.CrossbowItem.getChargeDuration(e.getItemStack(),p)+10f),0,1):0;
  crossbowWrist(pose,sign,swing,pull);renderGrip(e,sign);
  pose.pushPose();crossbowModel(pose);
  drawGeometry(e,mc.getItemRenderer().getModel(e.getItemStack(),mc.level,p,0),left);
  pose.popPose();pose.popPose();
 }
 static void renderScythe(RenderHandEvent e,float motion,float horizontal,float direction){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null)return;boolean left=(e.getHand()==InteractionHand.MAIN_HAND)==(p.getMainArm()==HumanoidArm.LEFT);int sign=left?-1:1;
  var pose=e.getPoseStack();pose.pushPose();
  // Shoulder pivot moves the wrist and the complete pole together across the view.
  scytheWrist(pose,sign,motion,horizontal,direction);renderGrip(e,sign);
  pose.pushPose();ScytheGrip.firstPerson(pose,left,horizontal);
  var model=mc.getModelManager().getModel(net.minecraft.client.resources.model.ModelResourceLocation.standalone(org.slavicmyths.karachun.NightContent.id("item/karachun_scythe_held")));
  drawGeometry(e,model,sign<0);pose.popPose();pose.popPose();
 }
 private static void drawGeometry(RenderHandEvent e,net.minecraft.client.resources.model.BakedModel model,boolean left){
  // Held baked geometry is drawn once. No builtin/entity recursion or display-transform reapplication.
  Minecraft.getInstance().getItemRenderer().render(e.getItemStack(),ItemDisplayContext.NONE,left,e.getPoseStack(),e.getMultiBufferSource(),e.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,model);
 }
 static void renderGrip(RenderHandEvent e,int sign){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null||p.isInvisible())return;
  var pose=e.getPoseStack();var model=((PlayerRenderer)mc.getEntityRenderDispatcher().getRenderer(p)).getModel();
  var arm=sign<0?model.leftArm:model.rightArm;var sleeve=sign<0?model.leftSleeve:model.rightSleeve;
  float ax=arm.xRot,ay=arm.yRot,az=arm.zRot,sx=sleeve.xRot,sy=sleeve.yRot,sz=sleeve.zRot;
  pose.pushPose();try{
   // Place the wrist at the grip, with the forearm extending toward the camera.
   gripArm(pose,arm);
   arm.xRot=arm.yRot=arm.zRot=sleeve.xRot=sleeve.yRot=sleeve.zRot=0;
   var texture=p.getSkin().texture();arm.render(pose,e.getMultiBufferSource().getBuffer(net.minecraft.client.renderer.RenderType.entitySolid(texture)),e.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
   if(p.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.RIGHT_SLEEVE)&&sign>0||p.isModelPartShown(net.minecraft.world.entity.player.PlayerModelPart.LEFT_SLEEVE)&&sign<0)sleeve.render(pose,e.getMultiBufferSource().getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(texture)),e.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY);
  }finally{arm.xRot=ax;arm.yRot=ay;arm.zRot=az;sleeve.xRot=sx;sleeve.yRot=sy;sleeve.zRot=sz;pose.popPose();}
 }
 static void crossbowWrist(com.mojang.blaze3d.vertex.PoseStack pose,int sign,float swing,float pull){pose.translate(sign*(.44-.08*swing),-.48-.08*swing,-.92);pose.translate(-sign*.06*pull,.02*pull,.04*pull);pose.mulPose(Axis.YP.rotationDegrees(sign*18));}
 static void crossbowModel(com.mojang.blaze3d.vertex.PoseStack pose){pose.scale(.85f,.85f,.85f);pose.translate(0,2.5/16,-4.5/16);}
 static void scytheWrist(com.mojang.blaze3d.vertex.PoseStack pose,int sign,float motion,float horizontal,float direction){pose.translate(0,-.42,-.3);pose.mulPose(Axis.YP.rotationDegrees(sign*motion*35*direction));pose.translate(sign*(.56-.20*horizontal),-.12,-.78);}
 static void gripArm(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.model.geom.ModelPart arm){pose.mulPose(Axis.XP.rotationDegrees(-55));pose.translate(-arm.x/16.0,-(arm.y+10)/16.0,-arm.z/16.0);}
 private WeaponHands(){}
}
