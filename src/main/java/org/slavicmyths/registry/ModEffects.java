package org.slavicmyths.registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Existing effect IDs/colors and the original non-linear curse penalties. */
public final class ModEffects {
 public static final DeferredRegister<MobEffect> EFFECTS=DeferredRegister.create(Registries.MOB_EFFECT,"slavicmyths");
 public static final DeferredHolder<MobEffect,MobEffect> CURSE=EFFECTS.register("kurgan_curse",CurseEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> ILL_FATE=EFFECTS.register("durnaya_dolya",()->new MobEffect(MobEffectCategory.HARMFUL,0x6C3233){});
 private static final class CurseEffect extends MobEffect {
  CurseEffect(){super(MobEffectCategory.HARMFUL,0x657780);
   addAttributeModifier(Attributes.MOVEMENT_SPEED,ResourceLocation.fromNamespaceAndPath("slavicmyths","4fe2072b-4d7e-4fe9-b8a6-3c925b5817da"),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,amplifier->amplifier>0?-.15:-.10);
   addAttributeModifier(Attributes.ATTACK_DAMAGE,ResourceLocation.fromNamespaceAndPath("slavicmyths","157d7501-d09e-4eaf-973c-9725920872e9"),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,amplifier->amplifier>0?-.15:-.10);
  }
 }
 private ModEffects(){}
}
