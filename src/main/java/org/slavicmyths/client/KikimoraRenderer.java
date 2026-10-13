package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.KikimoraEntity;
public final class KikimoraRenderer extends MobRenderer<KikimoraEntity,KikimoraModel> {
 private final KikimoraModel normal=new KikimoraModel(),blighted=new KikimoraModel(true);
 @Override public void render(KikimoraEntity e,float yaw,float partial,com.mojang.blaze3d.vertex.PoseStack p,net.minecraft.client.renderer.MultiBufferSource out,int light){model=MorVisual.shown(e)?blighted:normal;super.render(e,yaw,partial,p,out,light);}

 public KikimoraRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m){super(m,new KikimoraModel(),.31F);}
 @Override public ResourceLocation getTextureLocation(KikimoraEntity e){if(MorVisual.shown(e))return MorVisual.texture(e,"kikimora");return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+("kikimora_"+(e.variant()%4)+".png"));}

}
