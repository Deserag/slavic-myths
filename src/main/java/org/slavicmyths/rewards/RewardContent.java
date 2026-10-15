package org.slavicmyths.rewards;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.registry.ModSounds;

/** Exactly nineteen source-specific rewards, in the existing item and sound registries. */
public final class RewardContent {
 public static final List<String> RUNES=List.of("stomp","dispelling","pursuit","suppression","battle_line");
 public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
 public static final Map<String,DeferredHolder<SoundEvent,SoundEvent>> SOUNDS=new LinkedHashMap<>();
 static {
  for(String id:RUNES)register("rune_"+id,()->new Described(new Item.Properties().rarity(Rarity.RARE)));
  register("gift_ilya_shield",()->new ShieldItem(new Item.Properties().durability(672).rarity(Rarity.EPIC)){
   @Override public void appendHoverText(ItemStack s,Item.TooltipContext c,List<Component> l,TooltipFlag f){l.add(Component.translatable(getDescriptionId()+".desc"));}
  });
  for(String id:List.of("gift_dobrynya_ring","gift_alyosha_belt","gift_finist_amulet","trophy_skim_necklace"))register(id,()->new org.slavicmyths.item.FolkAccessoryItem(new Item.Properties().rarity(Rarity.RARE),id.contains("ring")?"ring":id.contains("belt")?"belt":"necklace"));
  for(String id:List.of("trophy_idol_chest","trophy_marinka_cloak","trophy_kartaus_mantle"))register(id,()->new ArmorItem(id.equals("trophy_idol_chest")?ArmorMaterials.DIAMOND:ArmorMaterials.LEATHER,ArmorItem.Type.CHESTPLATE,new Item.Properties().durability(id.equals("trophy_idol_chest")?528:240).rarity(Rarity.RARE)){
   @Override public ResourceLocation getArmorTexture(ItemStack s,net.minecraft.world.entity.Entity e,net.minecraft.world.entity.EquipmentSlot slot,ArmorMaterial.Layer layer,boolean inner){return ResourceLocation.fromNamespaceAndPath("slavicmyths","textures/models/armor/"+id+".png");}
   @Override public void appendHoverText(ItemStack s,Item.TooltipContext c,List<Component> l,TooltipFlag f){l.add(Component.translatable(getDescriptionId()+".desc"));}
  });
  register("trophy_kalin_sabre",()->new SwordItem(Tiers.DIAMOND,new Item.Properties().attributes(SwordItem.createAttributes(Tiers.DIAMOND,org.slavicmyths.combat.RewardWeaponBalance.swordParameter(Tiers.DIAMOND,org.slavicmyths.combat.RewardWeaponBalance.KALIN),-2.7F)).rarity(Rarity.RARE)){
   @Override public void appendHoverText(ItemStack s,Item.TooltipContext c,List<Component> l,TooltipFlag f){l.add(Component.translatable(getDescriptionId()+".desc"));}
  });
  for(String id:List.of("mat_bone_plates","mat_mirror_shards","mat_skim_fang","mat_banner_remnant","mat_royal_iron"))register(id,()->new Described(new Item.Properties()));
  for(String id:List.of("rune_stomp_proc","rune_dispelling_proc","rune_pursuit_proc","rune_suppression_proc","rune_battle_line_proc","gift_ilya_block_reflect","gift_ilya_wave","gift_dobrynya_ring_ready","gift_dobrynya_counter","gift_alyosha_dodge","gift_finist_detection","gift_finist_warning","trophy_idol_heavy_guard","trophy_marinka_blink","trophy_skim_execute","trophy_kartaus_resistance","trophy_kalin_momentum_1","trophy_kalin_momentum_3","trophy_kalin_sweep","reward_claim_success","reward_rare_loot","rune_anvil_socket")){
   String key="reward."+id;SOUNDS.put(id,ModSounds.SOUNDS.register(key,()->SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath("slavicmyths",key))));
  }
 }
 private static void register(String id,java.util.function.Supplier<Item> factory){ITEMS.put(id,ModItems.ITEMS.register(id,factory));}
 public static Item item(String id){return ITEMS.get(id).get();}
 public static void init(){}
 private static class Described extends Item {
  Described(Properties p){super(p);}
  @Override public void appendHoverText(ItemStack s,Item.TooltipContext c,List<Component> l,TooltipFlag f){l.add(Component.translatable(getDescriptionId()+".desc"));}
 }
 private RewardContent(){}
}
