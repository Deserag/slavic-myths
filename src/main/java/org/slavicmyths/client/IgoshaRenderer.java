package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.entity.IgoshaEntity;
public final class IgoshaRenderer extends MobRenderer<IgoshaEntity,IgoshaModel> {
 public IgoshaRenderer(EntityRendererManager m){super(m,new IgoshaModel(),.4F);}
 @Override public ResourceLocation getTextureLocation(IgoshaEntity e){return new ResourceLocation("slavicmyths","textures/entity/"+("igosha.png"));}
 
}
