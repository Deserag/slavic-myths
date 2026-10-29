package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;import com.mojang.math.Axis;import net.minecraft.client.Minecraft;import net.minecraft.client.player.LocalPlayer;import net.minecraft.client.renderer.entity.player.PlayerRenderer;import net.minecraft.world.entity.HumanoidArm;import net.minecraft.world.item.ItemStack;import net.neoforged.api.distmarker.Dist;import net.neoforged.fml.common.EventBusSubscriber;import net.neoforged.bus.api.SubscribeEvent;import net.neoforged.neoforge.client.extensions.common.*;
/** Both actual player arms and sleeves follow the single held greatsword; no duplicate item draw. */
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class GreatswordClient {
 @SubscribeEvent public static void extensions(RegisterClientExtensionsEvent e){e.registerItem(new IClientItemExtensions(){
  @Override public boolean applyForgeHandTransform(PoseStack pose,LocalPlayer player,HumanoidArm arm,ItemStack stack,float partial,float equip,float swing){
   if(!player.getOffhandItem().isEmpty()||player.getMainHandItem()!=stack)return false;
   var mc=Minecraft.getInstance();var renderer=(PlayerRenderer)mc.getEntityRenderDispatcher().getRenderer(player);var buffer=mc.renderBuffers().bufferSource();int light=renderer.getPackedLightCoords(player,partial);
   float wave=(float)Math.sin(Math.sqrt(swing)*Math.PI);int sign=arm==HumanoidArm.LEFT?-1:1;
   if(!player.isInvisible())for(HumanoidArm hand:HumanoidArm.values()){
    int h=hand==HumanoidArm.LEFT?-1:1;pose.pushPose();pose.translate(h*(.42-.08*wave),-.55-equip*.6+.12*wave,-.7-.18*wave);
    pose.mulPose(Axis.YP.rotationDegrees(h*45));pose.translate(h*-1,3.6,3.5);pose.mulPose(Axis.ZP.rotationDegrees(h*120));pose.mulPose(Axis.XP.rotationDegrees(200));pose.mulPose(Axis.YP.rotationDegrees(h*-135));pose.translate(h*5.6,0,0);
    if(hand==HumanoidArm.LEFT)renderer.renderLeftHand(pose,buffer,light,player);else renderer.renderRightHand(pose,buffer,light,player);pose.popPose();
   }
   pose.translate(sign*.20,-.25-equip*.6+.12*wave,-.95-.18*wave);pose.mulPose(Axis.XP.rotationDegrees(-20-45*wave));return true;
  }
 },org.slavicmyths.nethersmithing.NetherWeapons.ITEMS.get("ashen_greatsword").get());}
 private GreatswordClient(){}
}
