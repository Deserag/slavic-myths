package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.husbandry.*;

public final class HusbandryRenderer extends MobRenderer<YardAnimal,HusbandryModel> {
    private final HusbandryModel adult,baby;
    private final ResourceLocation adultTexture,babyTexture;
    public HusbandryRenderer(EntityRendererProvider.Context c,int kind,String id){
        super(c,new HusbandryModel(kind,false),kind==2?.45F:.30F);adult=model;baby=new HusbandryModel(kind,true);
        adultTexture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+id+".png");
        babyTexture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+id+"_baby.png");
    }
    @Override public void render(YardAnimal e,float yaw,float partial,PoseStack pose,MultiBufferSource buffers,int light){model=e.isBaby()?baby:adult;super.render(e,yaw,partial,pose,buffers,light);}
    @Override public ResourceLocation getTextureLocation(YardAnimal e){return e.isBaby()?babyTexture:adultTexture;}
    public static void register(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(Husbandry.GOOSE.get(),c->new HusbandryRenderer(c,0,"goose"));e.registerEntityRenderer(Husbandry.DUCK.get(),c->new HusbandryRenderer(c,1,"duck"));e.registerEntityRenderer(Husbandry.GOAT.get(),c->new HusbandryRenderer(c,2,"domestic_goat"));}
}
