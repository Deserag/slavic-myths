package org.slavicmyths.client;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.*;
import org.slavicmyths.marine.MarineArmor;
@EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
public final class MarineArmorModel implements IClientItemExtensions {
 private final java.util.Map<EquipmentSlot,HumanoidModel<?>> models=new java.util.EnumMap<>(EquipmentSlot.class);
 public MarineArmorModel(){for(var s:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET})models.put(s,create(s));}
 static HumanoidModel<?> create(EquipmentSlot slot){
  var mesh=HumanoidModel.createMesh(new CubeDeformation(slot==EquipmentSlot.LEGS?.5F:1F),0);var root=mesh.getRoot();
  root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
  if(slot==EquipmentSlot.HEAD){
   root.getChild("head").addOrReplaceChild("sea_mask",CubeListBuilder.create().texOffs(32,0)
    .addBox(-4.6F,-3.4F,-5.2F,9.2F,3.8F,1F)
    .addBox(-4.6F,-6.5F,-5.2F,1.7F,3.1F,1F).addBox(2.9F,-6.5F,-5.2F,1.7F,3.1F,1F)
    .addBox(-.6F,-6.5F,-5.4F,1.2F,4.2F,1.2F),PartPose.ZERO);
   for(int sign:new int[]{-1,1})root.getChild("head").addOrReplaceChild("gills"+sign,CubeListBuilder.create().texOffs(32,0).addBox(sign<0?-6:4.5F,-6,0,1.5F,1,4).addBox(sign<0?-6:4.5F,-3,1,1.5F,1,3),PartPose.ZERO);
  }
  if(slot==EquipmentSlot.HEAD)root.getChild("head").addOrReplaceChild("bronze_mask",CubeListBuilder.create().texOffs(48,8).addBox(-4.6F,-6.8F,-5.5F,4.6F,1,1).addBox(0,-6.8F,-5.5F,4.6F,1,1).addBox(-.5F,-6,-5.7F,1,4,1),PartPose.ZERO);
  if(slot==EquipmentSlot.CHEST){
   root.getChild("body").addOrReplaceChild("shell",CubeListBuilder.create().texOffs(32,0).addBox(-2,-7+12,-3.4F,4,3,1),PartPose.ZERO);
   for(String name:new String[]{"left_arm","right_arm"}){
    float x=name.equals("left_arm")?3:-5;
    root.getChild(name).addOrReplaceChild("fins",CubeListBuilder.create().texOffs(32,0).addBox(x,-2,-1,2,2,4).addBox(x,0,0,2,2,3),PartPose.ZERO);
   }
  }
  for(String name:new String[]{"left_leg","right_leg"}){
   if(slot==EquipmentSlot.LEGS)root.getChild(name).addOrReplaceChild("shell_knee",CubeListBuilder.create().texOffs(32,0).addBox(-2,5,-3,4,3,1),PartPose.ZERO);
   if(slot==EquipmentSlot.FEET)root.getChild(name).addOrReplaceChild("flipper",CubeListBuilder.create().texOffs(32,0).addBox(-2.6F,10,-7,5.2F,2,6).addBox(-3,11,-8,6,1,3),PartPose.ZERO);
  }
  return new HumanoidModel<>(LayerDefinition.create(mesh,64,32).bakeRoot());
 }
 @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){return ArmorSlotVisibility.apply(models.getOrDefault(slot,original),slot);}
 @SubscribeEvent public static void register(RegisterClientExtensionsEvent e){e.registerItem(new MarineArmorModel(),MarineArmor.HELMET.get(),MarineArmor.CHESTPLATE.get(),MarineArmor.LEGGINGS.get(),MarineArmor.BOOTS.get());}
}