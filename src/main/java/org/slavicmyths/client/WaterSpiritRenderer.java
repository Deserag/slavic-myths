package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.water.WaterSpirit;
public final class WaterSpiritRenderer extends MobRenderer<WaterSpirit,WaterSpiritModel>{
 private final ResourceLocation texture;
 public WaterSpiritRenderer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context m,boolean r){super(m,new WaterSpiritModel(r),r?.35F:.6F);texture=ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/entity/"+(r?"rusalka":"vodyanoy")+".png");}
 public ResourceLocation getTextureLocation(WaterSpirit e){return texture;}
}
