package org.slavicmyths.rpg;
import java.util.*;
import net.minecraft.network.chat.Component;
/** Existing rune parameters shared by gameplay, anvil and rune-item tooltips. */
public record RuneDefinition(String id,Map<String,Double> parameters,Set<String> categories) {
 public static final Map<String,RuneDefinition> ALL=new LinkedHashMap<>();
 static {for(var d:List.of(new RuneDefinition("thunder",Map.ofEntries(Map.entry("hit_cooldown",140.0),Map.entry("heavy_damage",2.0),Map.entry("dagger_damage",1.0),Map.entry("hit_damage",1.5),Map.entry("chain_radius",2.0),Map.entry("chain_damage",1.0),Map.entry("shield_cooldown",180.0),Map.entry("shield_damage",1.5),Map.entry("staff_bonus",1.15)),Set.of("sword","dagger","spear","heavy","bow","shield","staff")),new RuneDefinition("heat",Map.ofEntries(Map.entry("hit_cooldown",180.0),Map.entry("fire_seconds",2.0),Map.entry("shield_cooldown",200.0),Map.entry("shield_fire_seconds",1.0),Map.entry("staff_cooldown",200.0),Map.entry("staff_fire_seconds",2.0)),Set.of("sword","dagger","spear","heavy","bow","shield","staff")),new RuneDefinition("forest",Map.ofEntries(Map.entry("melee_damage",0.75),Map.entry("shield_cooldown",240.0),Map.entry("shield_duration",30.0),Map.entry("shield_level",0.0),Map.entry("staff_cooldown",900.0),Map.entry("staff_regeneration",50.0),Map.entry("staff_level",0.0),Map.entry("arrow_speed",0.04)),Set.of("sword","dagger","spear","heavy","bow","shield","staff")),new RuneDefinition("midday",Map.ofEntries(Map.entry("melee_damage",0.6),Map.entry("shield_cooldown",240.0),Map.entry("shield_duration",25.0),Map.entry("shield_level",0.0),Map.entry("bow_multiplier",1.08),Map.entry("staff_cooldown",0.9)),Set.of("sword","dagger","spear","heavy","bow","shield","staff")),new RuneDefinition("shadow",Map.ofEntries(Map.entry("light_threshold",7.0),Map.entry("staff_cooldown",0.9),Map.entry("dagger_damage",1.0),Map.entry("speed_cooldown",200.0),Map.entry("speed_duration",40.0),Map.entry("speed_level",0.0),Map.entry("bow_multiplier",1.06)),Set.of("sword","dagger","bow","staff")),new RuneDefinition("protection",Map.ofEntries(Map.entry("hit_cooldown",300.0),Map.entry("hit_duration",30.0),Map.entry("hit_level",0.0),Map.entry("shield_cooldown",240.0),Map.entry("shield_duration",45.0),Map.entry("shield_level",0.0),Map.entry("staff_cooldown",300.0),Map.entry("staff_duration",30.0),Map.entry("staff_level",0.0)),Set.of("sword","dagger","spear","heavy","bow","shield","staff")),new RuneDefinition("wind",Map.ofEntries(Map.entry("hit_cooldown",140.0),Map.entry("spear_force",0.45),Map.entry("weapon_force",0.25),Map.entry("shield_cooldown",160.0),Map.entry("shield_force",0.4),Map.entry("staff_cooldown",160.0),Map.entry("staff_force",0.5),Map.entry("arrow_speed",0.08)),Set.of("sword","dagger","spear","heavy","bow","shield","staff")),new RuneDefinition("life",Map.ofEntries(Map.entry("shield_health_threshold",0.4),Map.entry("shield_cooldown",900.0),Map.entry("shield_regeneration",60.0),Map.entry("shield_level",0.0),Map.entry("staff_cooldown",1200.0),Map.entry("staff_healing",1.0),Map.entry("kill_cooldown",900.0),Map.entry("kill_healing",2.0)),Set.of("sword","dagger","spear","heavy","bow","shield","staff"))))ALL.put(d.id,d);}
 public static double n(String id,String key){return ALL.get(id).parameters.get(key);}
 public static int i(String id,String key){return (int)n(id,key);}
 public static float f(String id,String key){return (float)n(id,key);}
 private static String format(double n){return java.math.BigDecimal.valueOf(n).setScale(3,java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();}
 public Component name(){return Component.translatable("item.slavicmyths.rune_"+id);}
 private String v(String key){return format(parameters.get(key));}
 private String sec(String key){return format(parameters.get(key)/20);}
 private String pct(String key){return format(parameters.get(key)*100);}
 private String bonus(String key){return format((parameters.get(key)-1)*100);}
 private String discount(String key){return format((1-parameters.get(key))*100);}
 private Object[] effectArguments(){return switch(id){
  case "thunder"->new Object[]{v("hit_damage"),v("heavy_damage"),v("dagger_damage"),sec("hit_cooldown"),v("chain_damage"),v("chain_radius"),v("shield_damage"),sec("shield_cooldown"),bonus("staff_bonus")};
  case "heat"->new Object[]{v("fire_seconds"),sec("hit_cooldown"),v("shield_fire_seconds"),sec("shield_cooldown"),v("staff_fire_seconds"),sec("staff_cooldown")};
  case "forest"->new Object[]{v("melee_damage"),pct("arrow_speed"),sec("shield_duration"),sec("shield_cooldown"),sec("staff_regeneration"),sec("staff_cooldown")};
  case "midday"->new Object[]{v("melee_damage"),bonus("bow_multiplier"),sec("shield_duration"),sec("shield_cooldown"),discount("staff_cooldown")};
  case "shadow"->new Object[]{v("light_threshold"),v("dagger_damage"),sec("speed_duration"),sec("speed_cooldown"),bonus("bow_multiplier"),discount("staff_cooldown")};
  case "protection"->new Object[]{sec("hit_duration"),sec("hit_cooldown"),sec("shield_duration"),sec("shield_cooldown")};
  case "life"->new Object[]{v("kill_healing"),sec("kill_cooldown"),pct("shield_health_threshold"),sec("shield_regeneration"),sec("shield_cooldown"),v("staff_healing"),sec("staff_cooldown")};
  case "wind"->new Object[]{v("weapon_force"),v("spear_force"),sec("hit_cooldown"),v("shield_force"),sec("shield_cooldown"),v("staff_force"),sec("staff_cooldown"),pct("arrow_speed")};
  default->new Object[0];};
 }
 public Component effect(){return Component.translatable("rpg.slavicmyths.effect_"+id,effectArguments());}
 public List<Component> tooltip(){return java.util.stream.IntStream.rangeClosed(1,3).mapToObj(i->(Component)Component.translatable("rpg.slavicmyths.rune_hint_"+id+"_"+i,effectArguments())).toList();}
 public Component compatible(){var names=categories.stream().sorted().map(c->Component.translatable("rpg.slavicmyths.category_"+c)).toList();var text=Component.empty();for(var c:names){if(!text.getString().isEmpty())text.append(", ");text.append(c);}return Component.translatable("rpg.slavicmyths.compatible",text);}
}
