package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.depth.ElderVodyanoy;
public final class ElderVodyanoyRenderer extends MobRenderer<ElderVodyanoy,ElderVodyanoyModel>{
 private static final ResourceLocation TEXTURE=new ResourceLocation("slavicmyths","textures/entity/elder_vodyanoy.png");
 public ElderVodyanoyRenderer(EntityRendererManager m){super(m,new ElderVodyanoyModel(),.9F);}
 public ResourceLocation getTextureLocation(ElderVodyanoy e){return TEXTURE;}
}
