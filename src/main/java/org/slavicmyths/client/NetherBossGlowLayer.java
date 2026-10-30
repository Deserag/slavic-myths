package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.netherboss.NetherSummonBoss;

/** A real, selectively masked emissive pass: never fullbright armor or a billboard. */
public final class NetherBossGlowLayer extends RenderLayer<NetherSummonBoss,NetherSummonBossModel> {
 public NetherBossGlowLayer(RenderLayerParent<NetherSummonBoss,NetherSummonBossModel> parent){super(parent);}
 @Override public void render(PoseStack pose,MultiBufferSource buffers,int light,NetherSummonBoss boss,float walk,float amount,float partial,float age,float yaw,float pitch){
  var texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(boss.isMorova()?"morova_bride":"trizna_flaming_prince")+"_glow.png");
  float opacity=getParentModel().opacity;getParentModel().opacity=opacity*(boss.phase()==1?.35F:boss.phase()==2?.65F:1F);
  getParentModel().renderToBuffer(pose,buffers.getBuffer(RenderType.eyes(texture)),15728880,OverlayTexture.NO_OVERLAY,0xffffffff);
  getParentModel().opacity=opacity;
 }
}
