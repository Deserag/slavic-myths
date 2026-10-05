package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.WildlifeEntity;
public final class WildlifeRenderer extends MobRenderer<WildlifeEntity,WildlifeModel>{
 private final ResourceLocation texture;
 public WildlifeRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context manager,WildlifeEntity.Kind kind,String id,float shadow){super(manager,new WildlifeModel(kind),shadow);texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+id+".png");}
 @Override public ResourceLocation getTextureLocation(WildlifeEntity entity){return texture;}
}
