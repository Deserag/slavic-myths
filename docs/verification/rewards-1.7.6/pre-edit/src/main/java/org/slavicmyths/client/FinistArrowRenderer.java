package org.slavicmyths.client;
public final class FinistArrowRenderer extends net.minecraft.client.renderer.entity.ArrowRenderer<org.slavicmyths.finist.FinistArrow> {
 public FinistArrowRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context c){super(c);}
 @Override public net.minecraft.resources.ResourceLocation getTextureLocation(org.slavicmyths.finist.FinistArrow a){return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/finist_magic_arrow.png");}
}
