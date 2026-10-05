package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.IgoshaEntity;
public final class IgoshaRenderer extends MobRenderer<IgoshaEntity,IgoshaModel> {
 public IgoshaRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m){super(m,new IgoshaModel(),.4F);}
 @Override public ResourceLocation getTextureLocation(IgoshaEntity e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+("igosha.png"));}

}
