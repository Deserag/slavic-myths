package org.slavicmyths.wolverine.client;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.wolverine.NorthernWolverine;
import org.slavicmyths.wolverine.WolverineContent;

public final class WolverineRenderer extends MobRenderer<NorthernWolverine,WolverineModel>{
    private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/northern_wolverine.png");
    public WolverineRenderer(EntityRendererProvider.Context context){super(context,new WolverineModel(),1.1F);}
    @Override protected void scale(NorthernWolverine entity,com.mojang.blaze3d.vertex.PoseStack pose,float partial){
        pose.scale(1.0F,.91F,.61F);
    }
    @Override public ResourceLocation getTextureLocation(NorthernWolverine entity){return TEXTURE;}
    public static void register(EntityRenderersEvent.RegisterRenderers event){
        event.registerEntityRenderer(WolverineContent.BOSS.get(),WolverineRenderer::new);
    }
}
