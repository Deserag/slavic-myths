package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.slavicmyths.gorynych.*;

@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class WeaponHands {
 private static LocalPlayer motionPlayer;
 private static Level motionLevel;
 private static final ItemStack[] observedCrossbows=new ItemStack[2];
 private static final boolean[] observedCharged=new boolean[2];
 private static final double[] lastShotClock={Double.NEGATIVE_INFINITY,Double.NEGATIVE_INFINITY};
 @SubscribeEvent public static void click(InputEvent.InteractionKeyMappingTriggered e){
  var p=Minecraft.getInstance().player;if(p==null||!e.isAttack()||!p.isShiftKeyDown())return;
  if(p.getMainHandItem().getItem() instanceof SerpentCrossbow bow&&bow.special){e.setCanceled(true);e.setSwingHand(false);net.neoforged.neoforge.network.PacketDistributor.sendToServer(new CrossbowVolley.Toggle());}
 }
 @SubscribeEvent public static void hand(RenderHandEvent e){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null||!(e.getItemStack().getItem() instanceof SerpentCrossbow))return;
  e.setCanceled(true);var pose=e.getPoseStack();boolean left=(e.getHand()==InteractionHand.MAIN_HAND)==(p.getMainArm()==HumanoidArm.LEFT);int sign=left?-1:1;
  pose.pushPose();float swing=net.minecraft.util.Mth.sin((float)Math.sqrt(e.getSwingProgress())*(float)Math.PI);
  float pull=p.isUsingItem()&&p.getUsedItemHand()==e.getHand()?Math.clamp((e.getItemStack().getUseDuration(p)-p.getUseItemRemainingTicks()+e.getPartialTick())/(net.minecraft.world.item.CrossbowItem.getChargeDuration(e.getItemStack(),p)+10f),0,1):0;
  float kick=crossbowKick(p,mc.level,e.getItemStack(),e.getHand(),e.getPartialTick());
  crossbowWrist(pose,sign,swing,pull);
  if(kick>0){pose.translate(0,-.035* kick,.13*kick);pose.mulPose(Axis.XP.rotationDegrees(-9*kick));}
  renderGrip(e,sign);
  pose.pushPose();crossbowModel(pose);
  drawGeometry(e,mc.getItemRenderer().getModel(e.getItemStack(),mc.level,p,0),left);
  pose.popPose();pose.popPose();
 }
 /** Detect charged -> fired only for the same real stack in a visible hand; never changes charge, ammo or damage. */
 static float crossbowKick(LocalPlayer player,Level level,ItemStack stack,InteractionHand hand,float partial){
  if(level==null)return 0;
  if(player!=motionPlayer||level!=motionLevel){
   motionPlayer=player;motionLevel=level;
   for(int i=0;i<2;i++){observedCrossbows[i]=null;observedCharged[i]=false;lastShotClock[i]=Double.NEGATIVE_INFINITY;}
  }
  int index=hand==InteractionHand.MAIN_HAND?0:1;
  ItemStack held=player.getItemInHand(hand);
  if(!(held.getItem() instanceof SerpentCrossbow)){observedCrossbows[index]=null;observedCharged[index]=false;lastShotClock[index]=Double.NEGATIVE_INFINITY;return 0;}
  stack=held;
  boolean charged=CrossbowItem.isCharged(stack);
  double now=level.getGameTime()+partial;
  if(observedCrossbows[index]!=stack){
   observedCrossbows[index]=stack;observedCharged[index]=charged;lastShotClock[index]=Double.NEGATIVE_INFINITY;return 0;
  }
  if(observedCharged[index]&&!charged&&!player.isUsingItem())lastShotClock[index]=now;
  observedCharged[index]=charged;
  double elapsed=now-lastShotClock[index];
  if(elapsed<0||elapsed>=7)return 0;
  double phase=elapsed/7.0;
  return (float)(Math.sin(Math.PI*phase)*(1.0-.25*phase));
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
 static void crossbowWrist(com.mojang.blaze3d.vertex.PoseStack pose,int sign,float swing,float pull){pose.translate(sign*(.44-.08*swing),-.48-.08*swing,-.92);pose.translate(-sign*.06*pull,.02*pull,.04*pull);pose.mulPose(Axis.YP.rotationDegrees(sign*18));pose.mulPose(Axis.XP.rotationDegrees(-11*pull));pose.mulPose(Axis.ZP.rotationDegrees(sign*5*pull));}
 static void crossbowModel(com.mojang.blaze3d.vertex.PoseStack pose){pose.scale(.85f,.85f,.85f);pose.translate(0,2.5/16,-4.5/16);}
 static void scytheWrist(com.mojang.blaze3d.vertex.PoseStack pose,int sign,float motion,float horizontal,float direction){float windup=Math.max(0,-motion),follow=Math.max(0,motion);pose.translate(0,-.42-.09*windup+.05*follow,-.3);pose.mulPose(Axis.YP.rotationDegrees(sign*motion*35*direction));pose.mulPose(Axis.XP.rotationDegrees(-18*windup+20*follow));pose.translate(sign*(.56-.20*horizontal),-.12,-.78+.12*follow);}
 static void gripArm(com.mojang.blaze3d.vertex.PoseStack pose,net.minecraft.client.model.geom.ModelPart arm){pose.mulPose(Axis.XP.rotationDegrees(-55));pose.translate(-arm.x/16.0,-(arm.y+10)/16.0,-arm.z/16.0);}
 private WeaponHands(){}
}
