package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.entity.PoludnitsaEntity;
public final class PoludnitsaRenderer extends MobRenderer<PoludnitsaEntity,PoludnitsaModel> {
 public PoludnitsaRenderer(EntityRendererManager m){super(m,new PoludnitsaModel(),.4F);}
 @Override public ResourceLocation getTextureLocation(PoludnitsaEntity e){return new ResourceLocation("slavicmyths","textures/entity/"+(e.form()>10?"poludnitsa_revealed.png":"poludnitsa.png"));}
 @Override protected void scale(PoludnitsaEntity e,com.mojang.blaze3d.matrix.MatrixStack p,float partial){shadowRadius=.3F+.08F*e.form()/20F;}
}
