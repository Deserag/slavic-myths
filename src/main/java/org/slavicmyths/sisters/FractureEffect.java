package org.slavicmyths.sisters;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/** One visible effect: I slows movement, II also slows attack, III restricts the left hand. */
public final class FractureEffect extends MobEffect {
    public FractureEffect() {
        super(MobEffectCategory.HARMFUL, 0xD6C5AB);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, ResourceLocation.fromNamespaceAndPath("slavicmyths", "fracture_movement"), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, amplifier -> -.20);
        addAttributeModifier(Attributes.ATTACK_SPEED, ResourceLocation.fromNamespaceAndPath("slavicmyths", "fracture_attack"), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, amplifier -> amplifier >= 1 ? -.25 : 0);
    }
}
