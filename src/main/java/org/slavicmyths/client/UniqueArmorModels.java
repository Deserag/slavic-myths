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
        if(slot==EquipmentSlot.LEGS)root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-4.3F,9.5F,-2.36F,8.6F,2.5F,4.72F),PartPose.ZERO);
        if(slot==EquipmentSlot.HEAD){
            root.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,0).addBox(-4.5F,-8.7F,-4.5F,9,4,9).texOffs(0,0)
                .addBox(-4.5F,-4.7F,-2.5F,1,5,7).addBox(3.5F,-4.7F,-2.5F,1,5,7).texOffs(56,0)
                .addBox(-.5F,-5.5F,-4.8F,1,1.5F,1).texOffs(40,0)
                .addBox(-5.3F,-9.7F,-.5F,1,5,2).addBox(4.3F,-9.7F,-.5F,1,5,2),PartPose.ZERO);
        }
        if(slot==EquipmentSlot.CHEST){
            // Leather/red backing, discrete large lamellae: never full-width stripes.
            var chest=root.addOrReplaceChild("body",CubeListBuilder.create().texOffs(16,16).addBox(-4,0,-2,8,11,4,new CubeDeformation(.36F)),PartPose.ZERO);
            for(int row=0;row<2;row++)for(int side:new int[]{-1,1})chest.addOrReplaceChild("lamella_"+row+"_"+side,CubeListBuilder.create().texOffs(0,0).addBox(side<0?-3.9F:.35F,1+row*4,-3.0F,3.55F,3.4F,.55F),PartPose.ZERO);
            chest.addOrReplaceChild("back_lamellae",CubeListBuilder.create().texOffs(0,0).addBox(-3.8F,2,2.5F,3.3F,6,.5F).addBox(.5F,2,2.5F,3.3F,6,.5F),PartPose.ZERO);
            chest.addOrReplaceChild("sun_clasp",CubeListBuilder.create().texOffs(48,0).addBox(-.8F,3.2F,-3.7F,1.6F,1.6F,.7F),PartPose.ZERO);
            for(String arm:List.of("left_arm","right_arm")){
                boolean left=arm.equals("left_arm");var shoulder=root.addOrReplaceChild(arm,CubeListBuilder.create().texOffs(40,16).addBox(left?-1:-3,-2,-2,4,6,4,new CubeDeformation(.32F)),PartPose.offset(left?5:-5,2,0));
                shoulder.addOrReplaceChild("plates",CubeListBuilder.create().texOffs(0,0).addBox(left?-1.5F:-3.5F,-2.6F,-2.6F,5,2.8F,5.2F),PartPose.ZERO);
            }
        }
        for(String leg:List.of("left_leg","right_leg")){
            if(slot==EquipmentSlot.LEGS)root.addOrReplaceChild(leg,CubeListBuilder.create().texOffs(0,16).addBox(leg.equals("left_leg")?-1.5F:-2,0,-2,3.5F,12,4,new CubeDeformation(.30F)),PartPose.offset(leg.equals("left_leg")?1.9F:-1.9F,12,0));
            if(slot==EquipmentSlot.LEGS)root.getChild(leg).addOrReplaceChild("knee",CubeListBuilder.create().texOffs(48,8).addBox(leg.equals("left_leg")?-1.8F:-2.4F,4.8F,-3,4.2F,2.8F,1).texOffs(48,16).addBox(leg.equals("left_leg")?-1.8F:-2.3F,.8F,-2.8F,4.1F,3,1),PartPose.ZERO);
            if(slot==EquipmentSlot.FEET)root.addOrReplaceChild(leg,CubeListBuilder.create().texOffs(0,16).addBox(leg.equals("left_leg")?-1.8F:-2.4F,5,-2.5F,4.2F,7,5).texOffs(0,16).addBox(leg.equals("left_leg")?-1.8F:-2.4F,10,-3.5F,4.2F,2,1.5F).texOffs(24,16).addBox(leg.equals("left_leg")?2.1F:-3.1F,6,.3F,1,3,2),PartPose.offset(leg.equals("left_leg")?1.9F:-1.9F,12,0));
        }
        if(slot==EquipmentSlot.FEET)for(String leg:List.of("left_leg","right_leg")){
            boolean left=leg.equals("left_leg");root.getChild(leg).addOrReplaceChild("feather_tips",CubeListBuilder.create().texOffs(24,16).addBox(left?2.4F:-3.5F,4.4F,.6F,1.1F,2.6F,1.2F).addBox(left?2.5F:-3.3F,3.4F,1.5F,.8F,1.9F,1.1F),PartPose.ZERO);
        }
        return new HumanoidModel<>(LayerDefinition.create(mesh,64,32).bakeRoot());
    }
    @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity,ItemStack stack,EquipmentSlot slot,HumanoidModel<?> original){return models.getOrDefault(slot,original);}
}
