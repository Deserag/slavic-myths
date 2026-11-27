package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.savanna.SavannaAnimal;
public final class SavannaAnimalRenderer extends MobRenderer<SavannaAnimal,SavannaAnimalModel>{
    private final ResourceLocation texture;
    public SavannaAnimalRenderer(EntityRendererProvider.Context ctx,SavannaAnimal.Kind kind,float shadow){
        super(ctx,new SavannaAnimalModel(kind),shadow);
        texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/savanna/"+kind.id+".png");
    }
    @Override protected void scale(SavannaAnimal e,com.mojang.blaze3d.vertex.PoseStack pose,float partialTick){float scale=e.encounterScale();pose.scale(scale,scale,scale);}
    @Override public ResourceLocation getTextureLocation(SavannaAnimal e){return texture;}
}
