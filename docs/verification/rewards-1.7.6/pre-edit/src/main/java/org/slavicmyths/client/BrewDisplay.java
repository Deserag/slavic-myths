package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.world.item.ItemDisplayContext;
import org.slavicmyths.brewing.*;
public final class BrewDisplay implements BlockEntityRenderer<BrewTile>{
 public BrewDisplay(BlockEntityRendererProvider.Context context){}
 public void render(BrewTile t,float partial,PoseStack pose,MultiBufferSource buffer,int light,int overlay){if(!t.kind.equals("fruit_press")||t.items.get(0).isEmpty())return;pose.pushPose();pose.translate(.5,.6,.5);pose.scale(.35F,.35F,.35F);pose.mulPose(Axis.XP.rotationDegrees(90));Minecraft.getInstance().getItemRenderer().renderStatic(t.items.get(0),ItemDisplayContext.FIXED,light,overlay,pose,buffer,Minecraft.getInstance().level,0);pose.popPose();}
}
