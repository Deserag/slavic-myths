package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.water.RusalkaEntity;
public final class RusalkaRenderer extends MobRenderer<RusalkaEntity,RusalkaModel> {
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/rusalka.png");
 public RusalkaRenderer(EntityRendererProvider.Context context){super(context,new RusalkaModel(),.35F);}
 public ResourceLocation getTextureLocation(RusalkaEntity entity){return TEXTURE;}
}
