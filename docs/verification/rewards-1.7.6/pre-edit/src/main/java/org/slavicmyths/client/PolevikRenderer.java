package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.entity.PolevikEntity;
public final class PolevikRenderer extends MobRenderer<PolevikEntity,PolevikModel> {
 public PolevikRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m){super(m,new PolevikModel(),.27F);}
 @Override public ResourceLocation getTextureLocation(PolevikEntity e){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+("polevik.png"));}

}
