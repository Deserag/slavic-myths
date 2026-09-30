package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.slavicmyths.water.WaterSpirit;
public final class WaterSpiritRenderer extends MobRenderer<WaterSpirit,WaterSpiritModel>{
 private final ResourceLocation texture;
 public WaterSpiritRenderer(EntityRendererManager m,boolean r){super(m,new WaterSpiritModel(r),r?.35F:.6F);texture=new ResourceLocation("slavicmyths","textures/entity/"+(r?"rusalka":"vodyanoy")+".png");}
 public ResourceLocation getTextureLocation(WaterSpirit e){return texture;}
}
