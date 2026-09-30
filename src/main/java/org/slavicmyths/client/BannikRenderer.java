package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.entity.BannikEntity;
public final class BannikRenderer extends MobRenderer<BannikEntity,BannikModel> {
 public BannikRenderer(EntityRendererManager m){super(m,new BannikModel(),.55F);}
 @Override public ResourceLocation getTextureLocation(BannikEntity e){return new ResourceLocation("slavicmyths","textures/entity/"+("bannik.png"));}
 
}
