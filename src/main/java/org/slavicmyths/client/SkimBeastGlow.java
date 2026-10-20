package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.skim.SkimBeastEntity;
/** Only the two eye UV islands and the existing red scars emit; no new particles, gameplay or sounds. */
public final class SkimBeastGlow extends RenderLayer<SkimBeastEntity,SkimModel<SkimBeastEntity>> {
 private static final RenderType EYES=RenderType.eyes(ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/skim_beast_eyes.png"));
 private static final RenderType RAGE=RenderType.eyes(ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/skim_beast_rage.png"));
 public SkimBeastGlow(SkimClient.BeastRenderer renderer){super(renderer);}
 @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,SkimBeastEntity entity,float walk,float amount,float partial,float age,float yaw,float pitch){
  float t=entity.clipAge(age-entity.tickCount),eye=SkimModel.eyeGlow(entity.action(),t),rage=SkimModel.rageGlow(entity.action(),t);
  if(eye>0)getParentModel().renderToBuffer(pose,buffers.getBuffer(EYES),15728880,OverlayTexture.NO_OVERLAY,(Math.round(eye*255)<<24)|0xFFFFFF);
  if(rage>0)getParentModel().renderToBuffer(pose,buffers.getBuffer(RAGE),15728880,OverlayTexture.NO_OVERLAY,(Math.round(rage*.45F*255)<<24)|0xFFFFFF);
 }
}
