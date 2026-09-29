package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.entity.OvinnikEntity;
public final class OvinnikRenderer extends MobRenderer<OvinnikEntity,OvinnikModel> {
 public OvinnikRenderer(EntityRendererManager m){super(m,new OvinnikModel(),.7F);}
 @Override public ResourceLocation getTextureLocation(OvinnikEntity e){return new ResourceLocation("slavicmyths","textures/entity/"+("ovinnik.png"));}
 
}
