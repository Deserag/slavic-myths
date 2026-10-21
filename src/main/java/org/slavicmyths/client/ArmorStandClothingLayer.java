package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.layers.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.slavicmyths.textile.*;

/** The same seven garment meshes on armor stands; no equipment/gameplay changes. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public final class ArmorStandClothingLayer extends RenderLayer<ArmorStand,ArmorStandArmorModel> {
 private final Map<String,HumanoidModel<ArmorStand>> garments=new LinkedHashMap<>();
 private ArmorStandClothingLayer(ArmorStandRenderer r){super(r);for(String id:List.of("linen_headscarf","linen_shirt","linen_ports","bast_shoes","sarafan","linen_apron","folk_vest"))garments.put(id,TextileClothingLayer.mesh(id,false));}
 @SubscribeEvent public static void layers(EntityRenderersEvent.AddLayers e){
  if(e.getRenderer(EntityType.ARMOR_STAND) instanceof ArmorStandRenderer r){
   r.layers.removeIf(layer->layer instanceof CustomHeadLayer);
   r.addLayer(new CustomHeadLayer<ArmorStand,ArmorStandArmorModel>(r,e.getContext().getModelSet(),e.getContext().getItemInHandRenderer()){
    @Override public void render(PoseStack p,MultiBufferSource b,int light,ArmorStand stand,float a,float f,float partial,float age,float yaw,float pitch){if(!(stand.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof ClothingItem))super.render(p,b,light,stand,a,f,partial,age,yaw,pitch);}
   });
   r.addLayer(new ArmorStandClothingLayer(r));
  }
 }
 @Override public void render(PoseStack p,MultiBufferSource buffers,int light,ArmorStand stand,float walk,float amount,float partial,float age,float yaw,float pitch){
  for(EquipmentSlot slot:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET)){
   var stack=stand.getItemBySlot(slot);if(!(stack.getItem() instanceof ClothingItem))continue;
   String id=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();var model=garments.get(id);if(model==null)continue;
   getParentModel().copyPropertiesTo(model);
   // Stand arm visibility is a property of its wooden sticks, not garment sleeves.
   model.leftArm.visible=true;model.rightArm.visible=true;
   var source=org.slavicmyths.client.runeglow.RuneGlowBuffers.clothing(stack,buffers,p);
   model.renderToBuffer(p,source.getBuffer(RenderType.entityCutoutNoCull(Textiles.id("textures/entity/clothing/"+id+".png"))),light,OverlayTexture.NO_OVERLAY,-1);
  }
 }
}
