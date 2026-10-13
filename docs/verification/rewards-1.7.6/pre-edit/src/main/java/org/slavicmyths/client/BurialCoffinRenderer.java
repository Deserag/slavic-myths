package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.slavicmyths.kurgan.*;
/** Static remains belong to the block. Only the wooden lid moves. */
public final class BurialCoffinRenderer implements BlockEntityRenderer<BurialCoffinTile>{
 private final FolkModelGeometry geometry=new FolkModelGeometry();
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/burial_coffin.png");
 private final ModelPart hardware=geometry.part(256,256,128,128);
 private final ModelPart body=geometry.part(256,256,0,0),lid=geometry.part(256,256,0,64),bones=geometry.part(256,256,128,0),cloth=geometry.part(256,256,128,64),warrior=geometry.part(256,256,128,128);
 public BurialCoffinRenderer(BlockEntityRendererProvider.Context d){
  geometry.box(body,-6,0,-23,12,3,30);geometry.box(body,-7,2,-24,14,2,32);geometry.box(body,-7,4,-24,2,6,32);geometry.box(body,5,4,-24,2,6,32);geometry.box(body,-5,4,-24,10,6,2);geometry.box(body,-5,4,6,10,6,2);
  for(int z:new int[]{-20,2}){geometry.box(hardware,-7.15F,3,z,.3F,7,1.5F);geometry.box(hardware,6.85F,3,z,.3F,7,1.5F);}
  lid.setPos(-7,10,0);geometry.box(lid,0,0,-24,14,1,32);geometry.box(lid,1,1,-24,12,2,32);geometry.box(lid,3,3,-23,8,1,30);
  geometry.box(bones,-2,4,-20,4,3,4);geometry.box(bones,-.6F,4,-16,1.2F,1,10);for(int i=0;i<4;i++)geometry.box(bones,-3,4,-15+i*2,6,.8F,.8F);for(int side:new int[]{-1,1}){geometry.box(bones,side*2-1,4,-5,1.3F,1,10);geometry.box(bones,side*4-.5F,4,-14,1,1,9);}geometry.box(bones,-2,4,-6,4,1,2);
  geometry.box(cloth,-3.2F,4.9F,-13,6.4F,.4F,6);geometry.box(cloth,-3,4.8F,-6,2,.3F,7);
  geometry.box(warrior,-2.3F,6.4F,-20.3F,4.6F,1.5F,4.6F);geometry.box(warrior,-3.3F,5,-14,6.6F,.5F,6);geometry.box(warrior,3.6F,4,-17,.8F,.8F,17);geometry.box(warrior,2.5F,4,-3,3,.7F,.8F);
 }
 @Override public void render(BurialCoffinTile tile,float partial,PoseStack p,MultiBufferSource buffer,int light,int overlay){p.pushPose();p.translate(.5,0,.5);p.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180-tile.getBlockState().getValue(BurialCoffinBlock.FACING).toYRot()));VertexConsumer out=buffer.getBuffer(RenderType.entityCutout(TEXTURE));body.render(p,out,light,overlay);hardware.render(p,out,light,overlay);if(tile.remains>0)bones.render(p,out,light,overlay);if(tile.remains>=2)cloth.render(p,out,light,overlay);if(tile.remains==3)warrior.render(p,out,light,overlay);lid.zRot=(tile.oldLid+(tile.lid-tile.oldLid)*partial)*1.35F;lid.render(p,out,light,overlay);p.popPose();}
 @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(BurialCoffinTile tile){return new net.minecraft.world.phys.AABB(tile.getBlockPos()).inflate(2);}
}
