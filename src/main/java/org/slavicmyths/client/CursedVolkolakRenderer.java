package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.volkolak.CursedVolkolak;
public final class CursedVolkolakRenderer extends MobRenderer<CursedVolkolak,CursedVolkolakModel>{
 public static final float DEPTH_SCALE=.85F;
 public CursedVolkolakRenderer(EntityRendererProvider.Context context){super(context,new CursedVolkolakModel(),.8F);addLayer(new CursedVolkolakEyes(this));}
 @Override protected void scale(CursedVolkolak entity,com.mojang.blaze3d.vertex.PoseStack pose,float partialTick){pose.scale(1,1,DEPTH_SCALE);}
 @Override public ResourceLocation getTextureLocation(CursedVolkolak entity){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/cursed_volkolak.png");}
}
