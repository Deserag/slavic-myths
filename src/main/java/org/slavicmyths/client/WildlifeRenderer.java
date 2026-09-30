package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.entity.WildlifeEntity;
public final class WildlifeRenderer extends MobRenderer<WildlifeEntity,WildlifeModel>{
 private final ResourceLocation texture;
 public WildlifeRenderer(EntityRendererManager manager,WildlifeEntity.Kind kind,String id,float shadow){super(manager,new WildlifeModel(kind),shadow);texture=new ResourceLocation("slavicmyths","textures/entity/"+id+".png");}
 @Override public ResourceLocation getTextureLocation(WildlifeEntity entity){return texture;}
}
