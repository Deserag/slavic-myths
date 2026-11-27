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
 public static final DeferredHolder<MobEffect,MobEffect> DUNGEON_GUARDIAN=EFFECTS.register("dungeon_guardian",()->new MobEffect(MobEffectCategory.BENEFICIAL,0xB69555){});
 public static final DeferredHolder<MobEffect,MobEffect> CURSE=EFFECTS.register("kurgan_curse",CurseEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> ILL_FATE=EFFECTS.register("durnaya_dolya",()->new MobEffect(MobEffectCategory.HARMFUL,0x6C3233){});
 public static final DeferredHolder<MobEffect,MobEffect> VIY_GAZE=EFFECTS.register("viy_gaze",org.slavicmyths.sisters.ViyGazeEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> DEATH_MARK=EFFECTS.register("death_mark",org.slavicmyths.sisters.NeveyaDeathMarkEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> FRACTURE=EFFECTS.register("fracture",org.slavicmyths.sisters.FractureEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> CUT=EFFECTS.register("cut",org.slavicmyths.combat.CutEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> WOLF_CUT=EFFECTS.register("wolf_cut",org.slavicmyths.wolf.WolfBleeding::new);
 public static final DeferredHolder<MobEffect,MobEffect> AUKA_FROST=EFFECTS.register("auka_frost",()->new org.slavicmyths.auka.AukaColdEffect(false));
 public static final DeferredHolder<MobEffect,MobEffect> AUKA_FROST_BLOOD=EFFECTS.register("auka_frost_blood",()->new org.slavicmyths.auka.AukaColdEffect(true));
 public static final DeferredHolder<MobEffect,MobEffect> BAD_BLOOD=EFFECTS.register("bad_blood",org.slavicmyths.volkolak.BadBlood::new);
 // Visible phase marker; WorldBoss.hurt owns the single damage multiplier.
 public static final DeferredHolder<MobEffect,MobEffect> SERPENT_GUARD=EFFECTS.register("serpent_guard",()->new MobEffect(MobEffectCategory.BENEFICIAL,0xD98835){});
 public static final DeferredHolder<MobEffect,MobEffect> RARE_BLEED=EFFECTS.register("rare_bleeding",org.slavicmyths.combat.RareBleedEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> DISORIENTATION=EFFECTS.register("disorientation",org.slavicmyths.combat.DisorientationEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> CLASS_ROOT=EFFECTS.register("class_root",org.slavicmyths.rpg.classes.ClassControl.RootEffect::new);
    public static final DeferredHolder<MobEffect, MobEffect> MOROVA_PLAGUE = EFFECTS.register("morova_plague", org.slavicmyths.netherboss.MorovaPlagueEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> SOLAR_BURN=EFFECTS.register("solar_burn",org.slavicmyths.desert.SolarBurnEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> LAMYA_DROUGHT=EFFECTS.register("lamya_drought",org.slavicmyths.desert.LamyaDroughtEffect::new);
 public static final DeferredHolder<MobEffect,MobEffect> SKARAPEYA_STONE=EFFECTS.register("skarapeya_stone",org.slavicmyths.savanna.SkarapeyaPoison.Stone::new);
 public static final DeferredHolder<MobEffect,MobEffect> SKARAPEYA_BLOOD=EFFECTS.register("skarapeya_black_blood",org.slavicmyths.savanna.SkarapeyaPoison.BlackBlood::new);
 private static final class CurseEffect extends MobEffect {
  CurseEffect(){super(MobEffectCategory.HARMFUL,0x657780);
   addAttributeModifier(Attributes.MOVEMENT_SPEED,ResourceLocation.fromNamespaceAndPath("slavicmyths","4fe2072b-4d7e-4fe9-b8a6-3c925b5817da"),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,amplifier->amplifier>0?-.15:-.10);
   addAttributeModifier(Attributes.ATTACK_DAMAGE,ResourceLocation.fromNamespaceAndPath("slavicmyths","157d7501-d09e-4eaf-973c-9725920872e9"),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL,amplifier->amplifier>0?-.15:-.10);
  }
 }
 private ModEffects(){}
}
