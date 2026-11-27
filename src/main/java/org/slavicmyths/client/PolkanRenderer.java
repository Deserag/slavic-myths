package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.savanna.Polkan;
public final class PolkanRenderer extends MobRenderer<Polkan,PolkanModel>{
    static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/savanna/polkan_palette.png");
    public PolkanRenderer(EntityRendererProvider.Context c){super(c,new PolkanModel(),.8F);}
    @Override protected void scale(Polkan e,PoseStack p,float partial){p.scale(1.08F,1.08F,1.08F);}
    @Override public ResourceLocation getTextureLocation(Polkan e){return TEXTURE;}
}