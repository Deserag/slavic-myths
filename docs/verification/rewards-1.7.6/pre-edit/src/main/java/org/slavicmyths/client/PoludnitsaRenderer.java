package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.PoludnitsaEntity;
public final class PoludnitsaRenderer extends MobRenderer<PoludnitsaEntity,PoludnitsaModel> {
 public PoludnitsaRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m){super(m,new PoludnitsaModel(),.4F);}
 @Override public ResourceLocation getTextureLocation(PoludnitsaEntity e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(e.form()>10?"poludnitsa_revealed.png":"poludnitsa.png"));}
 @Override protected void scale(PoludnitsaEntity e,com.mojang.blaze3d.vertex.PoseStack p,float partial){shadowRadius=.3F+.08F*e.form()/20F;}
}
