package org.slavicmyths.client;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
/** Four separately authored wearable silhouettes using standard vanilla bones. */
public final class DragonArmor implements IClientItemExtensions {
 private final java.util.Map<EquipmentSlot,HumanoidModel<?>> models=new java.util.EnumMap<>(EquipmentSlot.class);
 public DragonArmor(){for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})models.put(slot,create(slot));}
 public static HumanoidModel<?> create(EquipmentSlot slot){var mesh=HumanoidModel.createMesh(new CubeDeformation(.55f),0);var r=mesh.getRoot();r.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
  if(slot==EquipmentSlot.HEAD){r.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4.7f,-9,-4.7f,9.4f,2.5f,9.4f).addBox(-4.7f,-7,3.3f,9.4f,7,1.4f).addBox(-4.7f,-7,-4.7f,1.5f,7,8).addBox(3.2f,-7,-4.7f,1.5f,7,8).addBox(-2,-7.5f,-5.6f,4,2,1.4f),PartPose.ZERO);for(int s:new int[]{-1,1}){var head=r.getChild("head");head.addOrReplaceChild("horn"+s,CubeListBuilder.create().texOffs(42,0).addBox(-.8f,-6,-.8f,1.6f,7,1.6f).addBox(-.6f,-9,0,1.2f,4,1.2f),PartPose.offsetAndRotation(s*4,-8,1,0,0,s*.5f));}}
  if(slot==EquipmentSlot.CHEST){r.getChild("body").addOrReplaceChild("breastplate",CubeListBuilder.create().texOffs(16,16).addBox(-4.7f,0,-3.2f,9.4f,11,1.4f).texOffs(48,48).addBox(-2,2,-4,4,5,1).texOffs(16,16).addBox(-4.7f,0,2,9.4f,10,1.4f),PartPose.ZERO);for(String arm:new String[]{"left_arm","right_arm"}){var part=r.getChild(arm);part.addOrReplaceChild("pauldron",CubeListBuilder.create().texOffs(32,0).addBox(-3.5f,-3,-3.5f,7,5,7),PartPose.ZERO);for(int i=0;i<3;i++)part.addOrReplaceChild("spike"+i,CubeListBuilder.create().texOffs(42,0).addBox(-.7f,-5,-.7f,1.4f,6,1.4f),PartPose.offsetAndRotation(arm.equals("left_arm")?2:-2,-2,i*2-2,0,0,arm.equals("left_arm")?.6f:-.6f));}}
  for(String leg:new String[]{"left_leg","right_leg"}){if(slot==EquipmentSlot.LEGS)r.getChild(leg).addOrReplaceChild("thigh",CubeListBuilder.create().texOffs(0,32).addBox(-2.7f,0,-2.7f,5.4f,9,5.4f).addBox(-2.8f,5,-3.2f,5.6f,3,1.5f).texOffs(48,48).addBox(-1.5f,1,-3.1f,3,3,1),PartPose.ZERO);if(slot==EquipmentSlot.FEET)r.getChild(leg).addOrReplaceChild("boot",CubeListBuilder.create().texOffs(0,32).addBox(-2.8f,7,-2.8f,5.6f,5,5.6f).addBox(-2.8f,10,-5.4f,5.6f,2,3).texOffs(48,48).addBox(-1.5f,7,-3.1f,3,2,1),PartPose.ZERO);}
  return new HumanoidModel<>(LayerDefinition.create(mesh,64,64).bakeRoot());}
 @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity e,ItemStack s,EquipmentSlot slot,HumanoidModel<?> original){return models.getOrDefault(slot,original);}
}
