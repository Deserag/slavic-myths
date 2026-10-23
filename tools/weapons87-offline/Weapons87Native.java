package org.slavicmyths.verify;
import java.nio.file.*;import java.util.*;import com.google.gson.*;
import net.minecraft.world.item.*;import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.ai.attributes.*;import org.slavicmyths.item.ModGear;
public final class Weapons87Native {
static Item factory(String id){return switch(id){case "bone_knife" -> new SwordItem(ModGear.BONE, new Item.Properties().attributes(SwordItem.createAttributes(ModGear.BONE, 2, -1.5F)));
case "silver_dagger" -> new SwordItem(ModGear.SILVER, new Item.Properties().attributes(SwordItem.createAttributes(ModGear.SILVER, 1, -1.5F)));
case "nightingale_dagger" -> new org.slavicmyths.bandit.NightingaleDagger(new Item.Properties().rarity(Rarity.RARE));
case "ancient_carolingian_sword" -> new org.slavicmyths.kurgan.BurialWeapon(false,true,new Item.Properties());
case "restored_carolingian_sword" -> new org.slavicmyths.kurgan.BurialWeapon(false,false,new Item.Properties());
case "silver_sword" -> new SwordItem(ModGear.SILVER, new Item.Properties().attributes(SwordItem.createAttributes(ModGear.SILVER, 3, -2.4F)));
case "perunite_sword" -> new SwordItem(ModGear.PERUNITE, new Item.Properties().attributes(SwordItem.createAttributes(ModGear.PERUNITE, 3, -2.4F)));
case "tugarin_sword" -> new SwordItem(Tiers.DIAMOND,
        new Item.Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(Tiers.DIAMOND, 6, -2.2F)));
case "trophy_kalin_sabre" -> new SwordItem(Tiers.DIAMOND,new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND,5,-2.7F)).rarity(Rarity.RARE));
case "club" -> new org.slavicmyths.item.ClubItem(new Item.Properties());
case "wooden_mace" -> new org.slavicmyths.combat.MaceItem(Tiers.WOOD,5,1.05F,.2F,.3F,new Item.Properties());
case "stone_mace" -> new org.slavicmyths.combat.MaceItem(Tiers.STONE,7,.85F,.3F,.4F,new Item.Properties());
case "mace" -> new org.slavicmyths.combat.MaceItem(Tiers.IRON, 8, .9F, .35F, .45F, new Item.Properties());
case "silver_mace" -> new org.slavicmyths.combat.MaceItem(ModGear.SILVER,7,1F,.3F,.4F,new Item.Properties());
case "perunite_mace" -> new org.slavicmyths.combat.MaceItem(ModGear.PERUNITE, 9.5F, .8F, .4F, .4F, new Item.Properties().rarity(Rarity.RARE));
case "ancient_chekan" -> new org.slavicmyths.combat.MaceItem(Tiers.STONE,5,1.2F,.22F,.12F,new Item.Properties().durability(90));
case "chekan" -> new org.slavicmyths.combat.MaceItem(Tiers.IRON,6,1.3F,.3F,.15F,new Item.Properties().durability(400));
case "battle_axe" -> new AxeItem(Tiers.IRON, new Item.Properties().attributes(AxeItem.createAttributes(Tiers.IRON, 7.0F, -3.3F)));
case "thunder_axe" -> new org.slavicmyths.item.ThunderAxeItem(ModGear.PERUNITE, new Item.Properties().rarity(Rarity.RARE));
case "stone_flail" -> new org.slavicmyths.combat.FlailItem(Tiers.STONE,new Item.Properties());
case "gold_flail" -> new org.slavicmyths.combat.FlailItem(Tiers.GOLD,new Item.Properties());
case "flail" -> new org.slavicmyths.combat.FlailItem(new Item.Properties());
case "silver_flail" -> new org.slavicmyths.combat.FlailItem(ModGear.SILVER,new Item.Properties());
case "diamond_flail" -> new org.slavicmyths.combat.FlailItem(Tiers.DIAMOND,new Item.Properties());
case "perunite_flail" -> new org.slavicmyths.combat.FlailItem(ModGear.PERUNITE,new Item.Properties());
case "netherite_flail" -> new org.slavicmyths.combat.FlailItem(Tiers.NETHERITE,new Item.Properties());
case "ataman_flail" -> new org.slavicmyths.combat.MaceItem(Tiers.DIAMOND, 12, .7F, .22F, .65F, new Item.Properties().rarity(Rarity.RARE));
case "ancient_spear" -> new org.slavicmyths.kurgan.BurialWeapon(true,true,new Item.Properties());
case "restored_spear" -> new org.slavicmyths.kurgan.BurialWeapon(true,false,new Item.Properties());
case "spear" -> new org.slavicmyths.item.SpearItem(Tiers.STONE, new Item.Properties());
case "gold_spear" -> new org.slavicmyths.item.SpearItem(Tiers.GOLD,new Item.Properties());
case "iron_spear" -> new org.slavicmyths.item.SpearItem(Tiers.IRON,new Item.Properties());
case "silver_spear" -> new org.slavicmyths.item.SpearItem(ModGear.SILVER, new Item.Properties());
case "diamond_spear" -> new org.slavicmyths.item.SpearItem(Tiers.DIAMOND,new Item.Properties());
case "perunite_spear" -> new org.slavicmyths.item.SpearItem(ModGear.PERUNITE,new Item.Properties());
case "netherite_spear" -> new org.slavicmyths.item.SpearItem(Tiers.NETHERITE,new Item.Properties());
case "pool_spear" -> new org.slavicmyths.depth.PoolSpear(new Item.Properties().rarity(Rarity.RARE));
case "thunder_spear" -> new org.slavicmyths.item.HeavyWeaponItem(ModGear.PERUNITE, 4, -2.9F, 0.18, new Item.Properties().rarity(Rarity.RARE));
case "victor_spear" -> new org.slavicmyths.item.SpearItem(Tiers.DIAMOND, 12, new Item.Properties().rarity(Rarity.RARE));
case "berdysh" -> new org.slavicmyths.item.HeavyWeaponItem(Tiers.IRON, 6, -3.3F, 0, new Item.Properties());
case "noon_sickle" -> new SwordItem(ModGear.SILVER, new Item.Properties().rarity(Rarity.UNCOMMON).attributes(SwordItem.createAttributes(ModGear.SILVER, 3, -2.5F)));
case "karachun_scythe" -> new org.slavicmyths.karachun.NightScythe();
case "poloz_whip" -> new org.slavicmyths.poloz.PolozWhip();
case "stone_throwing_knife" -> new org.slavicmyths.combat.ThrowingWeaponItem(Tiers.STONE,true,new Item.Properties());
case "gold_throwing_knife" -> new org.slavicmyths.combat.ThrowingWeaponItem(Tiers.GOLD,true,new Item.Properties());
case "iron_throwing_knife" -> new org.slavicmyths.combat.ThrowingWeaponItem(Tiers.IRON,true,new Item.Properties());
case "silver_throwing_knife" -> new org.slavicmyths.combat.ThrowingWeaponItem(ModGear.SILVER,true,new Item.Properties());
case "diamond_throwing_knife" -> new org.slavicmyths.combat.ThrowingWeaponItem(Tiers.DIAMOND,true,new Item.Properties());
case "perunite_throwing_knife" -> new org.slavicmyths.combat.ThrowingWeaponItem(ModGear.PERUNITE,true,new Item.Properties());
case "netherite_throwing_knife" -> new org.slavicmyths.combat.ThrowingWeaponItem(Tiers.NETHERITE,true,new Item.Properties());
case "stone_sulitsa" -> new org.slavicmyths.combat.ThrowingWeaponItem(Tiers.STONE,false,new Item.Properties());
case "gold_sulitsa" -> new org.slavicmyths.combat.ThrowingWeaponItem(Tiers.GOLD,false,new Item.Properties());
case "iron_sulitsa" -> new org.slavicmyths.combat.ThrowingWeaponItem(Tiers.IRON,false,new Item.Properties());
case "silver_sulitsa" -> new org.slavicmyths.combat.ThrowingWeaponItem(ModGear.SILVER,false,new Item.Properties());
case "diamond_sulitsa" -> new org.slavicmyths.combat.ThrowingWeaponItem(Tiers.DIAMOND,false,new Item.Properties());
case "perunite_sulitsa" -> new org.slavicmyths.combat.ThrowingWeaponItem(ModGear.PERUNITE,false,new Item.Properties());
case "netherite_sulitsa" -> new org.slavicmyths.combat.ThrowingWeaponItem(Tiers.NETHERITE,false,new Item.Properties());
case "short_bow" -> new org.slavicmyths.combat.FieldBowItem(false,new Item.Properties().durability(300));
case "heavy_bow" -> new org.slavicmyths.combat.FieldBowItem(true,new Item.Properties().durability(500));
case "finist_bow" -> new org.slavicmyths.finist.FinistBow(new Item.Properties().durability(640).rarity(Rarity.RARE));
case "bone_arrow" -> new ArrowItem(new Item.Properties());
case "serpent_crossbow" -> new org.slavicmyths.gorynych.SerpentCrossbow(false);
case "three_breath_crossbow" -> new org.slavicmyths.gorynych.SerpentCrossbow(true);
case "carved_staff" -> new SwordItem(Tiers.WOOD, new Item.Properties().attributes(SwordItem.createAttributes(Tiers.WOOD, 2, -2.8F)));
case "storm_staff" -> new org.slavicmyths.item.StormStaffItem(new Item.Properties().stacksTo(1).durability(240).rarity(Rarity.RARE));
case "veles_staff" -> new org.slavicmyths.artifact.VelesStaffItem(new Item.Properties().rarity(Rarity.EPIC));
case "likho_staff" -> new org.slavicmyths.combat.LikhoStaffItem();
case "morozko_staff" -> new org.slavicmyths.morozko.FrostStaff(); default -> throw new IllegalArgumentException(id);};}
public static void main(String[] args)throws Exception{
net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
// Isolated offline fixture only: no loader exists to open the item registration phase.
var itemRegistry=(net.minecraft.core.MappedRegistry<Item>)net.minecraft.core.registries.BuiltInRegistries.ITEM;itemRegistry.unfreeze();
var intrusive=net.minecraft.core.MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");intrusive.setAccessible(true);intrusive.set(itemRegistry,new IdentityHashMap<>());
var items=new LinkedHashMap<String,Item>();
for(String line:Files.readAllLines(Path.of("src/main/resources/assets/slavicmyths/creative_catalog.tsv"))){
 if(!line.contains("\tweapons\t"))continue;String id=line.split("\t")[0];Item item=factory(id);
 net.minecraft.core.Registry.register(itemRegistry,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths",id),item);items.put(id,item);
}
boolean after=Arrays.asList(args).contains("--after");
var beforeComponents=new LinkedHashMap<String,net.minecraft.core.component.DataComponentMap>();items.forEach((id,item)->beforeComponents.put(id,item.components()));
if(after){
 var phase=net.neoforged.neoforge.internal.RegistrationEvents.class.getDeclaredField("canModifyComponents");phase.setAccessible(true);phase.setBoolean(null,true);
 try{org.slavicmyths.combat.WeaponDefaults.modify(new net.neoforged.neoforge.event.ModifyDefaultComponentsEvent());}finally{phase.setBoolean(null,false);}
 for(var entry:items.entrySet()){
  String id=entry.getKey();var old=beforeComponents.get(id);var current=entry.getValue().components();
  if(org.slavicmyths.combat.WeaponDefaults.REACH_ITEMS.contains(id)){
   var modifiers=current.get(DataComponents.ATTRIBUTE_MODIFIERS);
   if(!modifiers.equals(org.slavicmyths.combat.WeaponDefaults.spearReach(modifiers)))throw new AssertionError("Reach modifier duplicates on reload "+id);
  }
  for(var component:old.keySet())if(component!=DataComponents.MAX_DAMAGE&&!Objects.equals(old.get(component),current.get(component))){
   if(component!=DataComponents.ATTRIBUTE_MODIFIERS||!org.slavicmyths.combat.WeaponDefaults.REACH_ITEMS.contains(id)||!Objects.equals(current.get(component),org.slavicmyths.combat.WeaponDefaults.spearReach(old.get(DataComponents.ATTRIBUTE_MODIFIERS))))throw new AssertionError("Unapproved component changed: "+id+component);
  }
  int expected=org.slavicmyths.combat.WeaponDefaults.DURABILITY.getOrDefault(id,Optional.ofNullable(old.get(DataComponents.MAX_DAMAGE)).orElse(0));
  if(Optional.ofNullable(current.get(DataComponents.MAX_DAMAGE)).orElse(0)!=expected)throw new AssertionError("Wrong approved durability "+id);
 }
}
var rows=new JsonArray();
for(String line:Files.readAllLines(Path.of("src/main/resources/assets/slavicmyths/creative_catalog.tsv"))){
 if(!line.contains("\tweapons\t"))continue;String id=line.split("\t")[0];Item item=items.get(id);
 var row=new JsonObject();row.addProperty("id",id);row.addProperty("class",item.getClass().getName());
 var attrs=item.components().get(DataComponents.ATTRIBUTE_MODIFIERS);double damage=1,speed=4,reach=3;
 var raw=new JsonArray();if(attrs!=null)for(var e:attrs.modifiers()){
 var a=new JsonObject();a.addProperty("attribute",e.attribute().getRegisteredName());a.addProperty("amount",e.modifier().amount());a.addProperty("operation",e.modifier().operation().toString());a.addProperty("slot",e.slot().toString());raw.add(a);
 if(e.modifier().operation()==AttributeModifier.Operation.ADD_VALUE){if(e.attribute().equals(Attributes.ATTACK_DAMAGE))damage+=e.modifier().amount();if(e.attribute().equals(Attributes.ATTACK_SPEED))speed+=e.modifier().amount();if(e.attribute().equals(Attributes.ENTITY_INTERACTION_RANGE))reach+=e.modifier().amount();}}
 row.add("modifiers",raw);row.addProperty("attack_damage_total",damage);row.addProperty("attack_speed_total",speed);row.addProperty("reach_total",reach);row.addProperty("unarmored_full_charge_melee_dps_ceiling",damage*speed);
 row.addProperty("max_damage",Optional.ofNullable(item.components().get(DataComponents.MAX_DAMAGE)).orElse(0));
 row.addProperty("measurement","native production constructors/components; no entities/world/client; not measured combat DPS");rows.add(row);
}
if(rows.size()!=68)throw new AssertionError(rows.size());
if(after)for(var raw:rows){var row=raw.getAsJsonObject();if(org.slavicmyths.combat.WeaponDefaults.REACH_ITEMS.contains(row.get("id").getAsString())&&Math.abs(row.get("reach_total").getAsDouble()-4.25)>1e-9)throw new AssertionError("Wrong approved spear reach");}
Files.writeString(Path.of(System.getProperty("weapons87NativeOutput","docs/implementation/weapons87/"+(after?"native-attributes-after.json":"native-attributes.json"))),new GsonBuilder().setPrettyPrinting().create().toJson(rows));
System.out.println("WEAPONS87_NATIVE: 68 production constructor attributes exported; no game launches");
}}
