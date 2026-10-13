package org.slavicmyths.poloz;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;

/** Final boss rewards; the existing core ID remains the heart for saved worlds. */
public final class PolozRewards {
 public static final Map<EquipmentSlot,DeferredHolder<Item,Item>> ARMOR=new EnumMap<>(EquipmentSlot.class);
 public static final DeferredHolder<Item,Item> RUNE=ModItems.ITEMS.register("rune_earth_lord_iv",()->new PolozRelic("rune_earth_lord_iv",new Item.Properties().rarity(Rarity.EPIC)));
 public static final DeferredHolder<Item,Item> WHIP=ModItems.ITEMS.register("poloz_whip",PolozWhip::new);
 static {for(var type:new ArmorItem.Type[]{ArmorItem.Type.HELMET,ArmorItem.Type.CHESTPLATE,ArmorItem.Type.LEGGINGS,ArmorItem.Type.BOOTS}){
  ARMOR.put(type.getSlot(),ModItems.ITEMS.register("poloz_"+type.getName(),()->new ArmorItem(ArmorMaterials.NETHERITE,type,new Item.Properties().durability(type.getDurability(37)).fireResistant().rarity(Rarity.EPIC)){
   @Override public ResourceLocation getArmorTexture(ItemStack s,Entity e,EquipmentSlot slot,ArmorMaterial.Layer layer,boolean inner){return PolozContent.id("textures/models/armor/poloz.png");}
   @Override public void appendHoverText(ItemStack s,Item.TooltipContext c,List<Component> l,TooltipFlag f){l.add(Component.translatable(getDescriptionId()+".desc"));l.add(Component.translatable("poloz.full_set"));}
  }));
 }}
 public static boolean wears(LivingEntity e,EquipmentSlot slot){return e.getItemBySlot(slot).is(ARMOR.get(slot).get());}
 public static boolean full(LivingEntity e){return ARMOR.keySet().stream().allMatch(s->wears(e,s));}
 public static double reduction(LivingEntity e){double n=0;for(var s:ARMOR.keySet())if(wears(e,s))n+=switch(s){case HEAD,FEET->.04;case CHEST->.08;default->.06;};return n;}
 public static double knockback(LivingEntity e){double n=0;for(var s:ARMOR.keySet())if(wears(e,s))n+=switch(s){case HEAD,FEET->.10;case CHEST->.20;default->.15;};return n;}
 public static Item upgrade(ItemStack s){return s.is(Items.NETHERITE_HELMET)?ARMOR.get(EquipmentSlot.HEAD).get():s.is(Items.NETHERITE_CHESTPLATE)?ARMOR.get(EquipmentSlot.CHEST).get():s.is(Items.NETHERITE_LEGGINGS)?ARMOR.get(EquipmentSlot.LEGS).get():s.is(Items.NETHERITE_BOOTS)?ARMOR.get(EquipmentSlot.FEET).get():null;}
 public static ItemStack upgradeCopy(ItemStack s){var item=upgrade(s);return item==null?ItemStack.EMPTY:s.transmuteCopy(item,1);}
 public static void init(){}
 private PolozRewards(){}
}
