package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.item.FolkAccessoryItem;
import org.slavicmyths.registry.ModItems;
@Mod.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT)
public final class FolkEquipmentLayer extends LayerRenderer<AbstractClientPlayerEntity,PlayerModel<AbstractClientPlayerEntity>> {
 public FolkEquipmentLayer(PlayerRenderer renderer){super(renderer);}
 public static void setup(){for(PlayerRenderer r:Minecraft.getInstance().getEntityRenderDispatcher().getSkinMap().values())r.addLayer(new FolkEquipmentLayer(r));}
 @SubscribeEvent public static void hidden(RenderPlayerEvent.Pre e){
  if(FolkAccessoryItem.equipped(e.getPlayer(),ModItems.INVISIBILITY_CAP.get()).isEmpty())return;
  if(e.getPlayer().isInvisible()){e.setCanceled(true);return;}
  int fade=e.getPlayer().getPersistentData().getInt("SlavicCapFade");
  if(fade<=0)return;
  // During the transition armor and held equipment are concealed; skin fades over one second.
  e.setCanceled(true);AbstractClientPlayerEntity p=(AbstractClientPlayerEntity)e.getPlayer();
  MatrixStack pose=e.getMatrixStack();pose.pushPose();
  pose.mulPose(Vector3f.YP.rotationDegrees(180-net.minecraft.util.math.MathHelper.rotLerp(e.getPartialRenderTick(),p.yBodyRotO,p.yBodyRot)));
  pose.scale(-1,-1,1);pose.translate(0,-1.501,0);
  PlayerModel<AbstractClientPlayerEntity> model=e.getRenderer().getModel();model.setAllVisible(true);model.crouching=p.isCrouching();model.young=false;
  model.setupAnim(p,p.animationPosition,p.animationSpeed,p.tickCount+e.getPartialRenderTick(),p.yHeadRot-p.yBodyRot,p.xRot);
  model.renderToBuffer(pose,e.getBuffers().getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(p.getSkinTextureLocation())),e.getLight(),net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,Math.max(0,1-(fade+e.getPartialRenderTick())/20F));pose.popPose();
 }
 @Override public void render(MatrixStack pose,IRenderTypeBuffer buffer,int light,AbstractClientPlayerEntity p,float swing,float amount,float partial,float age,float yaw,float pitch){
  if(p.isInvisible())return;
  show(ModItems.INVISIBILITY_CAP.get(),p,pose,buffer,light,0,-.62,0,.65F,true);
  show(ModItems.VELES_AMULET.get(),p,pose,buffer,light,0,.3,-.165,.38F,false);
  show(ModItems.HUNTER_BELT.get(),p,pose,buffer,light,0,.65,0,.62F,false);
 }
 private void show(Item item,AbstractClientPlayerEntity p,MatrixStack pose,IRenderTypeBuffer buffer,int light,double x,double y,double z,float scale,boolean head){
  ItemStack stack=FolkAccessoryItem.equipped(p,item);if(stack.isEmpty())return;
  pose.pushPose();if(head)getParentModel().head.translateAndRotate(pose);else getParentModel().body.translateAndRotate(pose);
  pose.translate(x,y,z);pose.mulPose(Vector3f.ZP.rotationDegrees(180));pose.scale(scale,scale,scale);
  Minecraft.getInstance().getItemRenderer().renderStatic(stack,ItemCameraTransforms.TransformType.NONE,light,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,pose,buffer);pose.popPose();
 }
}
