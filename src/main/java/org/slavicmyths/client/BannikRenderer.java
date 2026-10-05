package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.BannikEntity;
public final class BannikRenderer extends MobRenderer<BannikEntity,BannikModel> {
 public BannikRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m){super(m,new BannikModel(),.55F);}
 @Override public ResourceLocation getTextureLocation(BannikEntity e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+("bannik.png"));}

}
