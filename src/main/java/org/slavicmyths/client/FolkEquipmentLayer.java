package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.item.FolkAccessoryItem;
import org.slavicmyths.registry.ModItems;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class FolkEquipmentLayer extends RenderLayer<AbstractClientPlayer,PlayerModel<AbstractClientPlayer>> {
 public FolkEquipmentLayer(PlayerRenderer renderer){super(renderer);}
 public static void layers(net.neoforged.neoforge.client.event.EntityRenderersEvent.AddLayers e){for(net.minecraft.client.resources.PlayerSkin.Model skin:e.getSkins()){PlayerRenderer r=e.getSkin(skin);if(r!=null)r.addLayer(new FolkEquipmentLayer(r));}}
 @SubscribeEvent public static void hidden(RenderPlayerEvent.Pre e){
  if(FolkAccessoryItem.equipped(e.getEntity(),ModItems.INVISIBILITY_CAP.get()).isEmpty())return;
  if(e.getEntity().isInvisible()){e.setCanceled(true);return;}
  int fade=e.getEntity().getPersistentData().getInt("SlavicCapFade");
  if(fade<=0)return;
  // During the transition armor and held equipment are concealed; skin fades over one second.
  e.setCanceled(true);AbstractClientPlayer p=(AbstractClientPlayer)e.getEntity();
  PoseStack pose=e.getPoseStack();pose.pushPose();
  pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180-net.minecraft.util.Mth.rotLerp(e.getPartialTick(),p.yBodyRotO,p.yBodyRot)));
  pose.scale(-1,-1,1);pose.translate(0,-1.501,0);
  PlayerModel<AbstractClientPlayer> model=e.getRenderer().getModel();model.setAllVisible(true);model.crouching=p.isCrouching();model.young=false;
  model.setupAnim(p,p.walkAnimation.position(),p.walkAnimation.speed(),p.tickCount+e.getPartialTick(),p.yHeadRot-p.yBodyRot,p.getXRot());
  model.renderToBuffer(pose,e.getMultiBufferSource().getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(p.getSkin().texture())),e.getPackedLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,((int)(Math.max(0,1-(fade+e.getPartialTick())/20F)*255)<<24)|0xFFFFFF);pose.popPose();
 }
 @Override public void render(PoseStack pose,MultiBufferSource buffer,int light,AbstractClientPlayer p,float swing,float amount,float partial,float age,float yaw,float pitch){
  if(p.isInvisible())return;
  show(ModItems.INVISIBILITY_CAP.get(),p,pose,buffer,light,0,-.62,0,.65F,true);
  show(ModItems.VELES_AMULET.get(),p,pose,buffer,light,0,.3,-.165,.38F,false);
  show(ModItems.HUNTER_BELT.get(),p,pose,buffer,light,0,.65,0,.62F,false);
 }
 private void show(Item item,AbstractClientPlayer p,PoseStack pose,MultiBufferSource buffer,int light,double x,double y,double z,float scale,boolean head){
  ItemStack stack=FolkAccessoryItem.equipped(p,item);if(stack.isEmpty())return;
  pose.pushPose();if(head)getParentModel().head.translateAndRotate(pose);else getParentModel().body.translateAndRotate(pose);
  pose.translate(x,y,z);pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180));pose.scale(scale,scale,scale);
  Minecraft.getInstance().getItemRenderer().renderStatic(stack,ItemDisplayContext.NONE,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,pose,buffer,p.level(),0);pose.popPose();
 }
}
