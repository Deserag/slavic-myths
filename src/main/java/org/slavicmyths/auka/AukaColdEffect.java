package org.slavicmyths.auka;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slavicmyths.registry.ModEffects;

/** Shared periodic frost damage; exclusive with other frost variants and wolf bleeding. */
public final class AukaColdEffect extends MobEffect {
    private final boolean blood;

    public AukaColdEffect(boolean blood) {
        super(MobEffectCategory.HARMFUL, blood ? 0x9F6572 : 0x8AC9DB);
        this.blood = blood;
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
            ResourceLocation.fromNamespaceAndPath("slavicmyths", blood ? "auka_frost_blood_speed" : "auka_frost_speed"),
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, amplifier -> blood ? -.20 : -.15);
    }

    @Override public boolean shouldApplyEffectTickThisTick(int remaining, int amplifier) {
        return remaining % 40 == 0;
    }

    @Override public boolean applyEffectTick(LivingEntity target, int amplifier) {
        if (!target.isAlive()) return false;
        if (blood && target.hasEffect(ModEffects.WOLF_CUT)) return true;
        if (!blood && target.hasEffect(ModEffects.AUKA_FROST_BLOOD)) return true;
        return target.hurt(target.damageSources().freeze(), 1.0F);
    }
}
