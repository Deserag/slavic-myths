package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import org.slavicmyths.flight.FlyingVessel;
import org.slavicmyths.registry.ModItems;
public final class FlightRenderer extends EntityRenderer<FlyingVessel>{
 private final ItemStack broom=new ItemStack(ModItems.FLYING_BROOM.get()),mortar=new ItemStack(ModItems.FLYING_MORTAR.get());
 public FlightRenderer(EntityRendererManager m){super(m);shadowRadius=.55F;}
 @Override public void render(FlyingVessel e,float yaw,float partial,MatrixStack m,IRenderTypeBuffer b,int light){m.pushPose();m.translate(0,.5,0);m.mulPose(Vector3f.YP.rotationDegrees(180-yaw));m.mulPose(Vector3f.ZP.rotationDegrees(e.roll()));m.mulPose(Vector3f.XP.rotationDegrees(e.pitch()));Minecraft.getInstance().getItemRenderer().renderStatic(e.mortar?mortar:broom,ItemCameraTransforms.TransformType.NONE,light,OverlayTexture.NO_OVERLAY,m,b);m.popPose();super.render(e,yaw,partial,m,b,light);}
 @Override public ResourceLocation getTextureLocation(FlyingVessel e){return net.minecraft.client.renderer.texture.AtlasTexture.LOCATION_BLOCKS;}
}
