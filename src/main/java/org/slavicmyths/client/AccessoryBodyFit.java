package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

/** Body ornaments clear the normal outer chestplate (x=5,z=3 model pixels). */
public final class AccessoryBodyFit {
    public static boolean armored(LivingEntity wearer){return !wearer.getItemBySlot(EquipmentSlot.CHEST).isEmpty();}
    public static boolean headArmored(LivingEntity wearer){return !wearer.getItemBySlot(EquipmentSlot.HEAD).isEmpty();}
    public static void applyHead(PoseStack pose,boolean armored){if(armored)pose.scale(1.35F,1,1.4F);}
    public static void apply(PoseStack pose,boolean armored){if(armored)pose.scale(1.15F,1,1.25F);}
    private AccessoryBodyFit(){}
}
