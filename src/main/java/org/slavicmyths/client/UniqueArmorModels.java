package org.slavicmyths.client;

import java.util.*;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import org.slavicmyths.equipment.UniqueEquipment;

/** Native armor bones and standard trim rendering; proportions follow the approved reference. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths",value=Dist.CLIENT,bus=net.neoforged.fml.common.EventBusSubscriber.Bus.MOD)
public final class UniqueArmorModels implements IClientItemExtensions {
    private final Map<EquipmentSlot,HumanoidModel<?>> models=new EnumMap<>(EquipmentSlot.class);
    private UniqueArmorModels(){for(var s:List.of(EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET))models.put(s,create(s));}
    @SubscribeEvent public static void register(RegisterClientExtensionsEvent event){event.registerItem(new UniqueArmorModels(),UniqueEquipment.WIND_BOOTS.get(),UniqueEquipment.BOGATYR_CHESTPLATE.get(),UniqueEquipment.CLEAR_SIGHT_HELMET.get(),UniqueEquipment.INDOMITABILITY_LEGGINGS.get());}
    private static HumanoidModel<?> create(EquipmentSlot slot){
        var mesh=HumanoidModel.createMesh(new CubeDeformation(slot==EquipmentSlot.LEGS?.30F:.55F),0);var root=mesh.getRoot();root.addOrReplaceChild("hat",CubeListBuilder.create(),PartPose.ZERO);
        if(slot==EquipmentSlot.LEGS)root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-4.3F,9.5F,-2.3F,8.6F,2.5F,4.6F),PartPose.ZERO);
        if(slot==EquipmentSlot.HEAD){
            root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4.5F,-8.7F,-4.5F,9,4,9).texOffs(0,0)
                .addBox(-4.5F,-4.7F,-2.5F,1,5,7).addBox(3.5F,-4.7F,-2.5F,1,5,7).texOffs(24,0)
                .addBox(-.5F,-5.5F,-4.8F,1,1.5F,1).texOffs(0,0)
                .addBox(-5.3F,-9.7F,-.5F,1,5,2).addBox(4.3F,-9.7F,-.5F,1,5,2),PartPose.ZERO);
        }
        if(slot==EquipmentSlot.CHEST){
            root.getChild("body").addOrReplaceChild("lamellar",CubeListBuilder.create().texOffs(16,16).addBox(-4.3F,1,-3,8.6F,9,1).texOffs(16,16).addBox(-4.3F,9,-2.6F,8.6F,2,5.2F),PartPose.ZERO);
            for(String arm:List.of("left_arm","right_arm"))root.getChild(arm).addOrReplaceChild("plates",CubeListBuilder.create().texOffs(40,16).addBox(arm.equals("left_arm")?-1.6F:-3.6F,-2.5F,-2.6F,5.2F,3,5.2F),PartPose.ZERO);
        }
        for(String leg:List.of("left_leg","right_leg")){
            if(slot==EquipmentSlot.LEGS)root.getChild(leg).addOrReplaceChild("knee",CubeListBuilder.create().texOffs(0,16).addBox(-2.4F,4.8F,-3,4.8F,2.8F,1).texOffs(0,16).addBox(-2.3F,.8F,-2.8F,4.6F,3,1),PartPose.ZERO);
            if(slot==EquipmentSlot.FEET)root.addOrReplaceChild(leg,CubeListBuilder.create().texOffs(0,16).addBox(-2.4F,5,-2.5F,4.8F,7,5).texOffs(0,16).addBox(-2.4F,10,-3.5F,4.8F,2,1.5F).texOffs(24,16).addBox(leg.equals("left_leg")?2.1F:-3.1F,6,.3F,1,3,2),PartPose.offset(leg.equals("left_leg")?1.9F:-1.9F,12,0));
        }
        return new HumanoidModel<>(LayerDefinition.create(mesh,64,32).bakeRoot());
    }
    @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){return models.getOrDefault(slot,original);}
}
