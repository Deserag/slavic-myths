package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.volkolak.CursedVolkolak;
/** Only the two tiny authored eye UV islands emit light, at a restrained tint/opacity. */
public final class CursedVolkolakEyes extends RenderLayer<CursedVolkolak,CursedVolkolakModel>{
 private static final RenderType EYES=RenderType.eyes(ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/cursed_volkolak_eyes.png"));
 public CursedVolkolakEyes(CursedVolkolakRenderer parent){super(parent);}
 @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,CursedVolkolak entity,float walk,float amount,float partial,float age,float yaw,float pitch){if(entity.move()==CursedVolkolak.Move.ESCAPE){float t=entity.animationTicks(partial);if(t>=19&&t<49)return;}getParentModel().renderToBuffer(pose,buffers.getBuffer(EYES),15728880,OverlayTexture.NO_OVERLAY,0x4DFFFFFF);}
}
