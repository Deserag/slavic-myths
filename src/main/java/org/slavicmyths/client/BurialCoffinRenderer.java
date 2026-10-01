package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import org.slavicmyths.kurgan.*;
/** Static remains belong to the block. Only the wooden lid moves. */
public final class BurialCoffinRenderer extends TileEntityRenderer<BurialCoffinTile>{
 private static final ResourceLocation TEXTURE=new ResourceLocation("slavicmyths","textures/entity/burial_coffin.png");
 private final ModelRenderer hardware=new ModelRenderer(256,256,128,128);
 private final ModelRenderer body=new ModelRenderer(256,256,0,0),lid=new ModelRenderer(256,256,0,64),bones=new ModelRenderer(256,256,128,0),cloth=new ModelRenderer(256,256,128,64),warrior=new ModelRenderer(256,256,128,128);
 public BurialCoffinRenderer(TileEntityRendererDispatcher d){super(d);
  body.addBox(-6,0,-23,12,3,30);body.addBox(-7,2,-24,14,2,32);body.addBox(-7,4,-24,2,6,32);body.addBox(5,4,-24,2,6,32);body.addBox(-5,4,-24,10,6,2);body.addBox(-5,4,6,10,6,2);
  for(int z:new int[]{-20,2}){hardware.addBox(-7.15F,3,z,.3F,7,1.5F);hardware.addBox(6.85F,3,z,.3F,7,1.5F);}
  lid.setPos(-7,10,0);lid.addBox(0,0,-24,14,1,32);lid.addBox(1,1,-24,12,2,32);lid.addBox(3,3,-23,8,1,30);
  bones.addBox(-2,4,-20,4,3,4);bones.addBox(-.6F,4,-16,1.2F,1,10);for(int i=0;i<4;i++)bones.addBox(-3,4,-15+i*2,6,.8F,.8F);for(int side:new int[]{-1,1}){bones.addBox(side*2-1,4,-5,1.3F,1,10);bones.addBox(side*4-.5F,4,-14,1,1,9);}bones.addBox(-2,4,-6,4,1,2);
  cloth.addBox(-3.2F,4.9F,-13,6.4F,.4F,6);cloth.addBox(-3,4.8F,-6,2,.3F,7);
  warrior.addBox(-2.3F,6.4F,-20.3F,4.6F,1.5F,4.6F);warrior.addBox(-3.3F,5,-14,6.6F,.5F,6);warrior.addBox(3.6F,4,-17,.8F,.8F,17);warrior.addBox(2.5F,4,-3,3,.7F,.8F);
 }
 @Override public void render(BurialCoffinTile tile,float partial,MatrixStack p,IRenderTypeBuffer buffer,int light,int overlay){p.pushPose();p.translate(.5,0,.5);p.mulPose(Vector3f.YP.rotationDegrees(180-tile.getBlockState().getValue(BurialCoffinBlock.FACING).toYRot()));IVertexBuilder out=buffer.getBuffer(RenderType.entityCutout(TEXTURE));body.render(p,out,light,overlay);hardware.render(p,out,light,overlay);if(tile.remains>0)bones.render(p,out,light,overlay);if(tile.remains>=2)cloth.render(p,out,light,overlay);if(tile.remains==3)warrior.render(p,out,light,overlay);lid.zRot=(tile.oldLid+(tile.lid-tile.oldLid)*partial)*1.35F;lid.render(p,out,light,overlay);p.popPose();}
}
