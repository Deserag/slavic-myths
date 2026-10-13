package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.KikimoraEntity;
public final class KikimoraRenderer extends MobRenderer<KikimoraEntity,KikimoraModel> {
 public KikimoraRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m){super(m,new KikimoraModel(),.31F);}
 @Override public ResourceLocation getTextureLocation(KikimoraEntity e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+("kikimora_"+(e.variant()%4)+".png"));}

}
