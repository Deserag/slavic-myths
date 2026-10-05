package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.depth.ElderVodyanoy;
public final class ElderVodyanoyRenderer extends MobRenderer<ElderVodyanoy,ElderVodyanoyModel>{
 private static final ResourceLocation TEXTURE=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/elder_vodyanoy.png");
 public ElderVodyanoyRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m){super(m,new ElderVodyanoyModel(),.9F);}
 public ResourceLocation getTextureLocation(ElderVodyanoy e){return TEXTURE;}
}
