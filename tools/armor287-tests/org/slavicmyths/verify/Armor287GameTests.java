package org.slavicmyths.verify;

import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.equipment.*;

@GameTestHolder("slavicmyths_armor287") @PrefixGameTestTemplate(false)
public final class Armor287GameTests {
 private static final EquipmentSlot[] BODY={EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
 private static final class Connection extends net.minecraft.network.Connection {private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}@Override public io.netty.channel.Channel channel(){return channel;}}
 private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}@Override public void send(net.minecraft.network.protocol.Packet<?> p){}@Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}}
 private static ServerPlayer player(GameTestHelper t){var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"Armor287"),ClientInformation.createDefault());p.connection=new Listener(p);p.setGameMode(GameType.SURVIVAL);p.moveTo(t.absoluteVec(new Vec3(3,20,3)));p.setNoGravity(true);for(int i=0;i<61;i++)p.tick();return p;}
 private static Item item(String id){return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("slavicmyths",id));}
 private static void equip(ServerPlayer p,EquipmentSlot slot,ItemStack stack){p.setItemSlot(slot,stack);p.doTick();}
 private static void near(GameTestHelper t,double actual,double expected,String what){t.assertTrue(Math.abs(actual-expected)<.0002,what+" actual="+actual+" expected="+expected);}
 private static JsonObject attrs(ServerPlayer p){var n=new JsonObject();for(var attr:List.of(Attributes.ARMOR,Attributes.ARMOR_TOUGHNESS,Attributes.KNOCKBACK_RESISTANCE,Attributes.MOVEMENT_SPEED,Attributes.MAX_HEALTH,Attributes.JUMP_STRENGTH))n.addProperty(BuiltInRegistries.ATTRIBUTE.getKey(attr.value()).getPath(),p.getAttributeValue(attr));return n;}
 private static List<String> ids()throws Exception{var root=JsonParser.parseString(Files.readString(Path.of("../docs/implementation/armor287-20261026/catalog.json"))).getAsJsonObject();var out=new ArrayList<String>();for(var e:root.getAsJsonArray("items"))out.add(e.getAsJsonObject().get("id").getAsString());return out;}
 @GameTest(template="empty",timeoutTicks=200) public static void all34RegistryAttributesSlotsRepeatAndCodec(GameTestHelper t)throws Exception{
  var rows=new JsonArray();int armor=0,clothes=0,shields=0,belts=0;
  for(String id:ids()){
   var p=player(t);var i=item(id);t.assertTrue(i!=Items.AIR,"missing "+id);var s=new ItemStack(i);var row=new JsonObject();row.addProperty("id",id);row.addProperty("class",i.getClass().getName());row.addProperty("durability",s.getMaxDamage());row.addProperty("enchantability",i.getEnchantmentValue());row.addProperty("rune_capacity",org.slavicmyths.rpg.Runes.capacity(s));row.add("before",attrs(p));
   EquipmentSlot slot=null;
   if(i instanceof ArmorItem a){armor++;slot=a.getEquipmentSlot();row.addProperty("defense",a.getDefense());row.addProperty("toughness",a.getToughness());var material=a.getMaterial().value();row.addProperty("material_knockback",material.knockbackResistance());var repairs=new JsonArray();for(var candidate:material.repairIngredient().get().getItems()){t.assertTrue(i.isValidRepairItem(s,candidate),"repair rejected "+id);repairs.add(BuiltInRegistries.ITEM.getKey(candidate.getItem()).toString());}row.add("repair",repairs);}
   else if(i instanceof org.slavicmyths.textile.ClothingItem clothing){clothes++;slot=clothing.getEquipmentSlot();t.assertTrue(!s.isDamageableItem(),"clothing acquired durability "+id);}
   else if(i instanceof ShieldItem){shields++;slot=EquipmentSlot.OFFHAND;}
   else {belts++;t.assertTrue(id.equals("woven_belt"),"unknown armor category "+id);}
   if(slot!=null){
    row.addProperty("slot",slot.getName());equip(p,slot,s);row.add("equipped",attrs(p));
    if(i instanceof ArmorItem a){near(t,p.getArmorValue(),a.getDefense(),"defense "+id);near(t,p.getAttributeValue(Attributes.ARMOR_TOUGHNESS),a.getToughness(),"toughness "+id);}
    else near(t,p.getArmorValue(),0,"noncombat protection "+id);
    var current=attrs(p);for(int n=0;n<8;n++){equip(p,slot,ItemStack.EMPTY);equip(p,slot,s.copy());}t.assertTrue(attrs(p).equals(current),"stacked re-equip "+id);
    if(s.isDamageableItem()){s.setDamageValue(1);s.enchant(t.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING),1);}
    s.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("287-"+id));var restored=ItemStack.parseOptional(t.getLevel().registryAccess(),(net.minecraft.nbt.CompoundTag)s.save(t.getLevel().registryAccess()));t.assertTrue(ItemStack.matches(s,restored),"stack codec "+id);
    equip(p,slot,ItemStack.EMPTY);row.add("removed",attrs(p));t.assertTrue(attrs(p).equals(row.getAsJsonObject("before")),"stale modifiers "+id);
   }else{row.addProperty("slot","BeltData attachment");p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,s);i.use(p.level(),p,net.minecraft.world.InteractionHand.MAIN_HAND);t.assertTrue(p.getData(org.slavicmyths.textile.BeltData.BELT).is(i)&&s.isEmpty(),"woven belt use");p.setData(org.slavicmyths.textile.BeltData.BELT,ItemStack.EMPTY);row.add("equipped",attrs(p));row.add("removed",attrs(p));}
   rows.add(row);
  }
  t.assertTrue(armor==22&&clothes==7&&shields==4&&belts==1,"classification "+armor+"/"+clothes+"/"+shields+"/"+belts);var out=new JsonObject();out.add("items",rows);out.addProperty("limits","server API operations and stack codec; not GUI/natural acquisition/real relog/MP");Files.writeString(Path.of("../docs/implementation/armor287-20261026/native.json"),new GsonBuilder().setPrettyPrinting().create().toJson(out));t.succeed();
 }
 @GameTest(template="empty") public static void all22NativeAnvilRepairAndEnchantmentEligibility(GameTestHelper t)throws Exception{
  int count=0;for(String id:ids())if(item(id) instanceof ArmorItem a){var p=player(t);p.giveExperienceLevels(100);var s=new ItemStack(a);s.setDamageValue(Math.max(2,s.getMaxDamage()/2));org.slavicmyths.item.ItemState.runes(s,1,List.of("heat"));s.set(DataComponents.CUSTOM_NAME,net.minecraft.network.chat.Component.literal("Donor287"));var ench=t.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.UNBREAKING);t.assertTrue(ench.value().canEnchant(s),"Unbreaking eligibility "+id);s.enchant(ench,2);var material=a.getMaterial().value().repairIngredient().get().getItems()[0].copy();var menu=new AnvilMenu(1,p.getInventory());menu.getSlot(0).set(s);menu.getSlot(1).set(material);menu.setItemName("Donor287");menu.createResult();var result=menu.getSlot(2).getItem();t.assertTrue(!result.isEmpty()&&result.getDamageValue()<s.getDamageValue(),"native anvil result "+id);t.assertTrue(result.getEnchantments().getLevel(ench)==2&&result.getHoverName().getString().equals("Donor287")&&org.slavicmyths.rpg.Runes.has(result,"heat"),"repair lost components "+id);menu.getSlot(1).set(new ItemStack(Items.DIRT));menu.createResult();t.assertTrue(menu.getSlot(2).getItem().isEmpty(),"dirt repairs armor "+id);count++;}t.assertTrue(count==22,"repair count");t.succeed();
 }
 private static List<Item> set(String family){return switch(family){case "iron"->List.of(Items.IRON_HELMET,Items.IRON_CHESTPLATE,Items.IRON_LEGGINGS,Items.IRON_BOOTS);case "diamond"->List.of(Items.DIAMOND_HELMET,Items.DIAMOND_CHESTPLATE,Items.DIAMOND_LEGGINGS,Items.DIAMOND_BOOTS);case "netherite"->List.of(Items.NETHERITE_HELMET,Items.NETHERITE_CHESTPLATE,Items.NETHERITE_LEGGINGS,Items.NETHERITE_BOOTS);default->List.of(item(family+"_helmet"),item(family+"_chestplate"),item(family+"_leggings"),item(family+"_boots"));};}
 @GameTest(template="empty") public static void sixFullSetsDamageHealthAndNoFreeHeal(GameTestHelper t)throws Exception{
  var rows=new JsonArray();for(String family:List.of("iron","diamond","netherite","perunite","dragon","poloz")){var p=player(t);p.setHealth(20);var gear=set(family);for(int k=0;k<4;k++)equip(p,BODY[k],new ItemStack(gear.get(k)));double max=family.equals("perunite")?25:family.equals("dragon")?40:family.equals("poloz")?34:20;near(t,p.getMaxHealth(),max,"set health "+family);near(t,p.getHealth(),20,"free equip heal "+family);var row=attrs(p);row.addProperty("family",family);p.setHealth(p.getMaxHealth());float before=p.getHealth();p.invulnerableTime=0;p.hurt(p.damageSources().mobAttack(EntityType.COW.create(t.getLevel())),10);row.addProperty("raw10_damage",before-p.getHealth());double armor=p.getArmorValue(),tough=p.getAttributeValue(Attributes.ARMOR_TOUGHNESS);double effective=Math.min(20,Math.max(armor*.2,armor-10/(2+tough/4)));double expected=10*(1-effective/25)*(family.equals("poloz")?.78:1);near(t,before-p.getHealth(),expected,"native damage vs formula "+family);for(int k=0;k<4;k++)equip(p,BODY[k],ItemStack.EMPTY);near(t,p.getMaxHealth(),20,"remove set health "+family);near(t,p.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),0,"remove set knockback "+family);rows.add(row);}Files.writeString(Path.of("../docs/implementation/armor287-20261026/comparison.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));t.succeed();
 }
 @GameTest(template="empty") public static void uniqueBoots(GameTestHelper t){UniqueEquipmentGameTests.bootsNativeAttributesJumpFallAndNoFlight(t);}
 @GameTest(template="empty") public static void uniqueChestThreshold(GameTestHelper t){UniqueEquipmentGameTests.chestHealthThresholdAndIdempotentModifiers(t);}
 @GameTest(template="empty") public static void uniqueChestDamage(GameTestHelper t){UniqueEquipmentGameTests.chestActualDamageReduction(t);}
 @GameTest(template="empty") public static void uniqueLegControl(GameTestHelper t){UniqueEquipmentGameTests.controlNativeEffectsAndUntaggedEffects(t);}
 @GameTest(template="empty",timeoutTicks=150) public static void uniqueLegTimer(GameTestHelper t){UniqueEquipmentGameTests.leggingsCooldownAndExpiration(t);}
 @GameTest(template="empty") public static void uniqueSight(GameTestHelper t)throws Exception{UniqueEquipmentGameTests.sightNativeIntentCooldownAndExclusions(t);}
 @GameTest(template="empty") public static void uniqueUpgradeComponents(GameTestHelper t){UniqueEquipmentGameTests.upgradesPreserveComponentsAndRejectWrongBases(t);}
 @GameTest(template="empty") public static void uniqueWorkbench(GameTestHelper t){UniqueEquipmentGameTests.nativeWorkbenchConsumption(t);}
 @GameTest(template="empty") public static void uniqueDimensionDeathClone(GameTestHelper t){UniqueEquipmentGameTests.cooldownSurvivesNativeDimensionAndClone(t);}
 @GameTest(template="empty") public static void dragonPiecesEffectRemovalAndSetProc(GameTestHelper t){
  var p=player(t);equip(p,EquipmentSlot.HEAD,new ItemStack(item("dragon_helmet")));p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS,100));t.assertTrue(p.getEffect(net.minecraft.world.effect.MobEffects.BLINDNESS).getDuration()==75,"dragon head duration");p.removeEffect(net.minecraft.world.effect.MobEffects.BLINDNESS);equip(p,EquipmentSlot.HEAD,ItemStack.EMPTY);p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.BLINDNESS,100));t.assertTrue(p.getEffect(net.minecraft.world.effect.MobEffects.BLINDNESS).getDuration()==100,"head bonus remained");
  equip(p,EquipmentSlot.CHEST,new ItemStack(item("dragon_chestplate")));p.setHealth(20);p.invulnerableTime=0;p.hurt(p.damageSources().onFire(),10);near(t,20-p.getHealth(),8.5,"dragon chest fire");equip(p,EquipmentSlot.CHEST,ItemStack.EMPTY);p.setHealth(20);p.invulnerableTime=0;p.hurt(p.damageSources().onFire(),10);near(t,20-p.getHealth(),10,"chest removed");
  equip(p,EquipmentSlot.FEET,new ItemStack(item("dragon_boots")));p.setHealth(20);p.invulnerableTime=0;p.hurt(p.damageSources().fall(),10);near(t,20-p.getHealth(),6.5,"dragon fall");equip(p,EquipmentSlot.FEET,ItemStack.EMPTY);p.setHealth(20);p.invulnerableTime=0;p.hurt(p.damageSources().fall(),10);near(t,20-p.getHealth(),10,"boots removed");
  equip(p,EquipmentSlot.LEGS,new ItemStack(item("dragon_leggings")));p.setDeltaMovement(Vec3.ZERO);p.knockback(.5,1,0);near(t,p.getDeltaMovement().horizontalDistance(),.315,"dragon knockback plus native material");equip(p,EquipmentSlot.LEGS,ItemStack.EMPTY);p.setDeltaMovement(Vec3.ZERO);p.knockback(.5,1,0);near(t,p.getDeltaMovement().horizontalDistance(),.5,"legs removed");
  var gear=set("dragon");for(int k=0;k<4;k++)equip(p,BODY[k],new ItemStack(gear.get(k)));p.setHealth(10);p.tickCount=20;org.slavicmyths.gorynych.GorynychEquipment.player(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));t.assertTrue(p.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE),"full set proc");p.removeEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE);org.slavicmyths.gorynych.GorynychEquipment.player(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(p));t.assertTrue(!p.hasEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE),"set cooldown bypass");t.succeed();
 }
 @GameTest(template="empty") public static void loadedSetAttributesNoDuplicateAndUnequip(GameTestHelper t){
  for(String family:List.of("perunite","dragon","poloz")){var p=player(t);var gear=set(family);for(int k=0;k<4;k++)equip(p,BODY[k],new ItemStack(gear.get(k)));var expected=attrs(p);var n=new net.minecraft.nbt.CompoundTag();p.saveWithoutId(n);var loaded=player(t);loaded.load(n);loaded.doTick();org.slavicmyths.poloz.PolozRewardEvents.refresh(loaded);t.assertTrue(attrs(loaded).equals(expected),"player load attrs "+family);for(int k=0;k<4;k++)equip(loaded,BODY[k],ItemStack.EMPTY);near(t,loaded.getMaxHealth(),20,"loaded unequip health "+family);near(t,loaded.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE),0,"loaded unequip KB "+family);}t.succeed();
 }
}
