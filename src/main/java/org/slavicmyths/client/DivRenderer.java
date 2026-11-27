package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.savanna.Div;
public final class DivRenderer extends MobRenderer<Div,DivModel>{
    static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/savanna/div_palette.png");
    public DivRenderer(EntityRendererProvider.Context c){super(c,new DivModel(),2.2F);}
    @Override protected void scale(Div e,PoseStack p,float partial){p.scale(3.2F,3.2F,3.2F);}
    @Override public ResourceLocation getTextureLocation(Div e){return TEXTURE;}
}
