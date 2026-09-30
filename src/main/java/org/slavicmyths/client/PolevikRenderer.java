package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.entity.PolevikEntity;
public final class PolevikRenderer extends MobRenderer<PolevikEntity,PolevikModel> {
 public PolevikRenderer(EntityRendererManager m){super(m,new PolevikModel(),.27F);}
 @Override public ResourceLocation getTextureLocation(PolevikEntity e){return new ResourceLocation("slavicmyths","textures/entity/"+("polevik.png"));}
 
}
