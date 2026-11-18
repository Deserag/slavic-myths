package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.poloz.*;

public final class PolozTrophyRenderer implements BlockEntityRenderer<PolozTrophy.Tile> {
 private static final net.minecraft.resources.ResourceLocation TEXTURE=PolozContent.id("textures/entity/great_poloz_head.png");
 private final ModelPart head=PolozRenderer.head();
 public PolozTrophyRenderer(BlockEntityRendererProvider.Context c){}

 @Override public void render(PolozTrophy.Tile tile,float partial,PoseStack pose,MultiBufferSource buffer,int light,int overlay){
  float time=tile.getLevel()==null?0:tile.getLevel().getGameTime()+partial;
  var facing=tile.getBlockState().getValue(PolozTrophy.FACING);
  var offset=PolozTrophy.offset(facing,13);
  head.yRot=(float)Math.sin(time*.026F)*.046F;
  head.getChild("jaw").xRot=-.08F-.24F*(.5F+.5F*(float)Math.sin(time*.078F));
  pose.pushPose();
  try{
   pose.translate(.5+offset.getX(),0,.5+offset.getZ());
   pose.mulPose(Axis.YP.rotationDegrees(180-facing.toYRot()));
   pose.translate(0,0,.2);
   pose.scale(3,3.5F,3);
   head.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),light,overlay);
   for(int side:new int[]{-1,1})head.getChild("eye"+side).render(pose,buffer.getBuffer(RenderType.eyes(TEXTURE)),0xF000F0,overlay);
  }finally{pose.popPose();head.yRot=0;head.getChild("jaw").xRot=0;}
  if(tile.getLevel() instanceof ClientLevel world&&!Minecraft.getInstance().isPaused()&&Minecraft.getInstance().player!=null){
   double cx=tile.getBlockPos().getX()+.5+offset.getX(),cy=tile.getBlockPos().getY()+1.65,cz=tile.getBlockPos().getZ()+.5+offset.getZ();
   if(Minecraft.getInstance().player.distanceToSqr(cx,cy,cz)<24*24){
    long tick=world.getGameTime();
    if(TrophyHeadFxRules.particleDue(tick,tile.clientLastFxTick,14)){
     tile.clientLastFxTick=tick;
     double dx=(world.random.nextDouble()-.5)*1.5,dz=(world.random.nextDouble()-.5)*1.5;
     world.addParticle(ParticleTypes.SOUL,cx+dx,cy+.1,cz+dz,0,.022,0);
     world.addParticle(ParticleTypes.ASH,cx-dx*.6,cy+.25,cz-dz*.6,0,.012,0);
    }
   }
  }
 }
 @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(PolozTrophy.Tile t){return t.getRenderBoundingBox();}
 @Override public boolean shouldRenderOffScreen(PolozTrophy.Tile t){return true;}

 public static final class ItemRenderer extends BlockEntityWithoutLevelRenderer {
  private final ModelPart itemHead=PolozRenderer.head();
  private final float[] bounds;
  private final float fit;
  public ItemRenderer(){
   super(Minecraft.getInstance().getBlockEntityRenderDispatcher(),Minecraft.getInstance().getEntityModels());
   TrophyHeadsClient.Capture capture=new TrophyHeadsClient.Capture();
   itemHead.render(new PoseStack(),capture,15728880,0);
   bounds=capture.bounds();
   fit=Math.min(.80F/Math.max(bounds[3]-bounds[0],bounds[5]-bounds[2]),.80F/(bounds[4]-bounds[1]));
  }
  @Override public void renderByItem(ItemStack stack,ItemDisplayContext context,PoseStack pose,MultiBufferSource buffer,int light,int overlay){
   pose.pushPose();
   try{
    if(context==ItemDisplayContext.GUI){
     pose.translate(.5F,.5F,.5F);
     pose.mulPose(Axis.YP.rotationDegrees(25));
     pose.scale(fit,-fit,-fit);
     pose.translate(-(bounds[0]+bounds[3])*.5F,-(bounds[1]+bounds[4])*.5F,-(bounds[2]+bounds[5])*.5F);
    }else{
     pose.translate(.5F,.45F,.5F);
     pose.scale(.65F,-.65F,-.65F);
     pose.translate(-(bounds[0]+bounds[3])*.5F,-(bounds[1]+bounds[4])*.5F,-(bounds[2]+bounds[5])*.5F);
    }
    itemHead.getChild("jaw").xRot=-.14F;
    itemHead.render(pose,buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),light,overlay);
    for(int side:new int[]{-1,1})itemHead.getChild("eye"+side).render(pose,buffer.getBuffer(RenderType.eyes(TEXTURE)),0xF000F0,overlay);
   }finally{itemHead.getChild("jaw").xRot=0;pose.popPose();}
  }
 }
}
