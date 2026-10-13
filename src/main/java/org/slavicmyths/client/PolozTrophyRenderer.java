package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.poloz.*;
public final class PolozTrophyRenderer implements BlockEntityRenderer<PolozTrophy.Tile> {
 private final ModelPart head=PolozRenderer.head();
 public PolozTrophyRenderer(BlockEntityRendererProvider.Context c){}
 @Override public void render(PolozTrophy.Tile tile,float partial,PoseStack p,MultiBufferSource b,int light,int overlay){p.pushPose();var facing=tile.getBlockState().getValue(PolozTrophy.FACING);var offset=PolozTrophy.offset(facing,13);p.translate(.5+offset.getX(),0,.5+offset.getZ());p.mulPose(Axis.YP.rotationDegrees(180-facing.toYRot()));p.translate(0,0,.2);p.scale(3,3.5f,3);head.render(p,b.getBuffer(RenderType.entityCutoutNoCull(PolozContent.id("textures/entity/great_poloz_head.png"))),light,overlay);for(int s:new int[]{-1,1})head.getChild("eye"+s).render(p,b.getBuffer(RenderType.eyes(PolozContent.id("textures/entity/great_poloz_head.png"))),0xF000F0,overlay);p.popPose();}
 @Override public net.minecraft.world.phys.AABB getRenderBoundingBox(PolozTrophy.Tile t){return t.getRenderBoundingBox();}
 @Override public boolean shouldRenderOffScreen(PolozTrophy.Tile t){return true;}
}
