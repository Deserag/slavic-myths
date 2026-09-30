package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.entity.KikimoraEntity;
public final class KikimoraRenderer extends MobRenderer<KikimoraEntity,KikimoraModel> {
 public KikimoraRenderer(EntityRendererManager m){super(m,new KikimoraModel(),.31F);}
 @Override public ResourceLocation getTextureLocation(KikimoraEntity e){return new ResourceLocation("slavicmyths","textures/entity/"+("kikimora_"+(e.variant()%4)+".png"));}
 
}
