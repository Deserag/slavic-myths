package org.slavicmyths.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

/** Raised plates follow vanilla armor bones and visibility, including crouch and swim poses. */
public final class PeruniteArmor implements IClientItemExtensions {
 private final java.util.Map<EquipmentSlot,HumanoidModel<?>> models=new java.util.EnumMap<>(EquipmentSlot.class);
 public PeruniteArmor(){for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})models.put(slot,create(slot));}
 private static HumanoidModel<?> create(EquipmentSlot slot){
  var mesh=HumanoidModel.createMesh(new CubeDeformation(.55F),0);
  var root=mesh.getRoot();root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
  if(slot==EquipmentSlot.LEGS)root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-4.5F,9.5F,-2.5F,9,2.5F,5),PartPose.ZERO);
  if(slot==EquipmentSlot.FEET)root.addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);
  root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0)
   .addBox(-4.5F,-9,-4.5F,9,1.5F,9).texOffs(0,0)
   .addBox(-4.5F,-7.5F,3.5F,9,7.5F,1).addBox(-4.5F,-7.5F,-4.5F,1,5,8)
   .addBox(3.5F,-7.5F,-4.5F,1,5,8).addBox(-4,-7.5F,-4.7F,8,1,1)
   .addBox(-.6F,-10,-4.5F,1.2F,1,8),PartPose.ZERO);
  if(slot==EquipmentSlot.CHEST)root.getChild("body").addOrReplaceChild("breastplate",CubeListBuilder.create().texOffs(16,0)
   .addBox(-3.5F,1,-3.5F,7,8,1.5F).addBox(-4.8F,3,-2.5F,1,7,5)
   .addBox(3.8F,3,-2.5F,1,7,5),PartPose.ZERO);
  for(String arm:new String[]{"left_arm","right_arm"}){
   float x=arm.equals("left_arm")?-1: -3;
   root.getChild(arm).addOrReplaceChild("pauldron",CubeListBuilder.create().texOffs(40,0)
    .addBox(x-.65F,-2.8F,-2.8F,5.3F,3,5.6F).texOffs(40,16)
    .addBox(x-.4F,5,-2.5F,4.8F,4.5F,5),PartPose.ZERO);
  }
  for(String leg:new String[]{"left_leg","right_leg"}){
   var boxes=CubeListBuilder.create().texOffs(0,16);
   if(slot==EquipmentSlot.LEGS)boxes.addBox(-2.4F,0,-2.4F,4.8F,6,4.8F).addBox(-2.5F,5,-3,5,2,1.5F);
   if(slot==EquipmentSlot.FEET)boxes.addBox(-2.5F,8,-2.6F,5,4,5.2F).addBox(-2.5F,10.2F,-3.7F,5,2,1.7F);
   root.addOrReplaceChild(leg,boxes,PartPose.offset(leg.equals("left_leg")?1.9F:-1.9F,12,0));
  }
  return new HumanoidModel<>(LayerDefinition.create(mesh,64,32).bakeRoot());
 }
 @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity e,ItemStack s,EquipmentSlot slot,HumanoidModel<?> original){return models.getOrDefault(slot,original);}
}
