package org.slavicmyths.client;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.texture.OverlayTexture;
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
  try{
   pose.translate(sign*.27,-.55,-1.20);
   pose.mulPose(Axis.XP.rotationDegrees(-14-38*arc));
   pose.mulPose(Axis.ZP.rotationDegrees(sign*-28));
   float size=id.equals("buried_hammer")?.69F:.64F;
   wrist(e,sign,-2.0F*size/16F,sign*22F);
   wrist(e,-sign,3.0F*size/16F,-sign*39F);
   pose.pushPose();
   try{
    pose.mulPose(Axis.YP.rotationDegrees(id.equals("buried_hammer")?55F:-40F));
    pose.scale(size,size,size);
    var model=mc.getModelManager().getModel(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath("slavicmyths","item/"+id+"_held")));
    mc.getItemRenderer().render(p.getMainHandItem(),ItemDisplayContext.NONE,left,pose,e.getMultiBufferSource(),e.getPackedLight(),OverlayTexture.NO_OVERLAY,model);
   }finally{pose.popPose();}
  }finally{pose.popPose();}

 }
 private static void wrist(RenderHandEvent e,int sign,float along,float yaw){
  var pose=e.getPoseStack();pose.pushPose();
  try{
   pose.translate(0,along,0);
   pose.mulPose(Axis.YP.rotationDegrees(yaw));
   pose.mulPose(Axis.ZP.rotationDegrees(sign*-26F));
   WeaponHands.renderGrip(e,sign);
  }finally{pose.popPose();}
 }
 private DesertTwoHandClient(){}
}
