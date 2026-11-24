package org.slavicmyths.owl.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.owl.*;
public final class WinterOwlRenderer extends MobRenderer<WinterOwl,WinterOwlModel>{
 private static final ResourceLocation[] TEXTURES={id("winter_owl"),id("winter_owl_blue"),id("winter_owl_cyan"),id("winter_owl_spirit")};
 private static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+name+".png");}
 public WinterOwlRenderer(EntityRendererProvider.Context c){super(c,new WinterOwlModel(),1.2F);}
 @Override public ResourceLocation getTextureLocation(WinterOwl e){return TEXTURES[e.spirit()?3:e.belly()];}
 @Override protected void scale(WinterOwl e,PoseStack p,float partial){if(e.spirit()){p.translate(0,1.5F*.7F,0);p.scale(.3F,.3F,.3F);}}
 public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(WinterOwlContent.BOSS.get(),WinterOwlRenderer::new);e.registerEntityRenderer(WinterOwlContent.SPIRIT.get(),WinterOwlRenderer::new);}
}