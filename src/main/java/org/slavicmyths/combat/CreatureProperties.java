package org.slavicmyths.combat;

import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

/** Registry tags describe physiology; no owner, hostility, NBT migration or world scan is cached. */
@EventBusSubscriber(modid="slavicmyths")
public final class CreatureProperties {
 public enum Nature {HUMANOID,ANIMAL,MYTHICAL_BEAST,SPIRIT,UNDEAD,ELEMENTAL,CONSTRUCT,PLANT,UNCLASSIFIED}
 public enum Trait {BLOODLESS,FIRE_AFFINITY,ICE_AFFINITY,WATER_AFFINITY,EARTH_AFFINITY,SILVER_VULNERABLE,CONTROL_RESISTANT,BOSS,SUMMONED,STORY_PROTECTED,PROJECTION}
 private static TagKey<EntityType<?>> tag(String s){return TagKey.create(Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath("slavicmyths",s));}
 private static final Map<Nature,TagKey<EntityType<?>>> NATURE=new EnumMap<>(Nature.class);
 private static final Map<Trait,TagKey<EntityType<?>>> TRAITS=new EnumMap<>(Trait.class);
 private static final Map<EntityType<?>,Nature> CACHE=Collections.synchronizedMap(new IdentityHashMap<>());
 private static final TagKey<EntityType<?>> OLD_BLOODLESS=tag("class_bloodless"),SILVER=tag("silver_vulnerable"),CONTROL=tag("class_control_resistant");
 static {for(var n:Nature.values())if(n!=Nature.UNCLASSIFIED)NATURE.put(n,tag("nature_"+n.name().toLowerCase(Locale.ROOT)));for(var t:Trait.values())TRAITS.put(t,tag("trait_"+t.name().toLowerCase(Locale.ROOT)));}
 @SubscribeEvent public static void tags(TagsUpdatedEvent event){CACHE.clear();}
 public static Nature taggedNature(EntityType<?> type){return CACHE.computeIfAbsent(type,t->{Nature found=Nature.UNCLASSIFIED;for(var e:NATURE.entrySet())if(t.is(e.getValue())){if(found!=Nature.UNCLASSIFIED)return Nature.UNCLASSIFIED;found=e.getKey();}return found;});}
 public static Nature nature(LivingEntity e){var n=taggedNature(e.getType());if(n!=Nature.UNCLASSIFIED)return n;
  // Vanilla animals are anatomy-based fallbacks; unknown mod types remain explicit audit failures.
  if(net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getType()).getNamespace().equals("minecraft")&&(e instanceof Animal||e instanceof WaterAnimal))return Nature.ANIMAL;
  return Nature.UNCLASSIFIED;
 }
 public static EnumSet<Trait> traits(LivingEntity e){var out=EnumSet.noneOf(Trait.class);for(var entry:TRAITS.entrySet())if(e.getType().is(entry.getValue()))out.add(entry.getKey());
  if(e.getType().is(OLD_BLOODLESS))out.add(Trait.BLOODLESS);
  if(e.getType().is(SILVER))out.add(Trait.SILVER_VULNERABLE);
  if(e.getType().is(CONTROL)||e.getType().is(CombatTargeting.CONTROL_IMMUNE))out.add(Trait.CONTROL_RESISTANT);
  if(CombatTargeting.affiliation(e)!=null)out.add(Trait.SUMMONED);
  if(e instanceof org.slavicmyths.marinka.MarinkaEntity witch&&witch.falseBody()){out.add(Trait.PROJECTION);out.add(Trait.BLOODLESS);}
  if(org.slavicmyths.mor.HigherMor.higher(e))out.add(Trait.BOSS);
  if(out.contains(Trait.BOSS)&&!CombatTargeting.bossMode(e))out.remove(Trait.BOSS);
  return out;
 }
 public static boolean biological(Nature n){return n==Nature.HUMANOID||n==Nature.ANIMAL||n==Nature.MYTHICAL_BEAST||n==Nature.UNCLASSIFIED;}
 public static boolean canBleed(Nature n,Set<Trait> traits,boolean legacyBleedImmune,boolean vanillaUndead){return biological(n)&&!traits.contains(Trait.BLOODLESS)&&!legacyBleedImmune&&!vanillaUndead;}
 public static boolean canBleed(LivingEntity e){return canBleed(nature(e),traits(e),e.getType().is(CutEffect.IMMUNE),e.getType().is(EntityTypeTags.UNDEAD));}
 public static boolean isPhysiologicalPoisonTarget(Nature n,Set<Trait> traits,boolean vanillaUndead){return biological(n)&&!traits.contains(Trait.BLOODLESS)&&!vanillaUndead;}
 public static boolean isPhysiologicalPoisonTarget(LivingEntity e){return isPhysiologicalPoisonTarget(nature(e),traits(e),e.getType().is(EntityTypeTags.UNDEAD));}
 public static float fireMultiplier(Nature n,Set<Trait> traits){if(traits.contains(Trait.FIRE_AFFINITY)&&traits.contains(Trait.ICE_AFFINITY))return 1; // Inventory rejects this ambiguity; runtime stays safe.
  if(traits.contains(Trait.FIRE_AFFINITY))return .5F;if(traits.contains(Trait.ICE_AFFINITY))return 1.5F;
  return n==Nature.SPIRIT?1.25F:n==Nature.PLANT?1.5F:1;
 }
 public static float fireMultiplier(Nature n,Set<Trait> traits,boolean nativeImmune){return nativeImmune?0:fireMultiplier(n,traits);}
 public static float fireMultiplier(LivingEntity e){return fireMultiplier(nature(e),traits(e),e.fireImmune());}
 private CreatureProperties(){}
}
