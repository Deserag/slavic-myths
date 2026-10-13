package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.texture.TextureAtlas;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import org.slavicmyths.flight.FlyingVessel;
import org.slavicmyths.registry.ModItems;
public final class FlightRenderer extends EntityRenderer<FlyingVessel>{
 private final ItemStack broom=new ItemStack(ModItems.FLYING_BROOM.get()),mortar=new ItemStack(ModItems.FLYING_MORTAR.get());
 public FlightRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m){super(m);shadowRadius=.55F;}
 @Override public void render(FlyingVessel e,float yaw,float partial,PoseStack m,MultiBufferSource b,int light){m.pushPose();m.translate(0,.5,0);m.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180-yaw));m.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(e.roll()));m.mulPose(com.mojang.math.Axis.XP.rotationDegrees(e.pitch()));Minecraft.getInstance().getItemRenderer().renderStatic(e.mortar?mortar:broom,ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,m,b,e.level(),e.getId());m.popPose();super.render(e,yaw,partial,m,b,light);}
 @Override public ResourceLocation getTextureLocation(FlyingVessel e){return net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS;}
}
