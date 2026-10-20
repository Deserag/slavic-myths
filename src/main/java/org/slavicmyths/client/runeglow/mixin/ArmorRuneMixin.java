package org.slavicmyths.client.runeglow.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.slavicmyths.client.runeglow.RuneGlowBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HumanoidArmorLayer.class)
public abstract class ArmorRuneMixin {
    @ModifyVariable(method="renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V",
            at=@At("HEAD"), argsOnly=true, ordinal=0)
    private MultiBufferSource slavicmyths$runeArmor(MultiBufferSource source, PoseStack pose,
            MultiBufferSource original, LivingEntity entity, EquipmentSlot slot, int light,
            HumanoidModel<?> model, float walk, float amount, float partial, float age, float yaw, float pitch) {
        return RuneGlowBuffers.armor(entity.getItemBySlot(slot),source,pose);
    }
}
