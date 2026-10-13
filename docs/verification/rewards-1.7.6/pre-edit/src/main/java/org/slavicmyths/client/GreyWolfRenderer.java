package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.wolf.GreyWolf;
public final class GreyWolfRenderer extends MobRenderer<GreyWolf,GreyWolfModel>{
 public GreyWolfRenderer(EntityRendererProvider.Context context){super(context,new GreyWolfModel(),1.1F);}
 @Override public ResourceLocation getTextureLocation(GreyWolf entity){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/grey_wolf.png");}
}
