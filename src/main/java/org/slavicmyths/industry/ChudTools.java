package org.slavicmyths.industry;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;

/** Native tool families and stack components remain authoritative; no second mining queue. */
public final class ChudTools {
 public static final Tier TIER=new Tier(){
  public int getUses(){return 2304;}public float getSpeed(){return 10;}public float getAttackDamageBonus(){return 4;}
  public int getEnchantmentValue(){return 18;}public net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> getIncorrectBlocksForDrops(){return BlockTags.INCORRECT_FOR_NETHERITE_TOOL;}
  public Ingredient getRepairIngredient(){return Ingredient.of(Industry.IRON.get());}
 };
 public static final DeferredHolder<Item,Item> LANTERN=ModItems.ITEMS.register("chud_mining_lantern",()->new Described(new Item.Properties().stacksTo(1),"lantern"));
 public static final DeferredHolder<Item,Item> PICKAXE=ModItems.ITEMS.register("chud_master_pickaxe",MasterPickaxe::new);
 public static final DeferredHolder<Item,Item> AXE=ModItems.ITEMS.register("chud_multitool_axe",MultiAxe::new);
 public static final DeferredHolder<Item,Item> MULTI_PICK=ModItems.ITEMS.register("chud_multitool_pickaxe",MultiPick::new);
 public static final DeferredHolder<Item,Item> SHOVEL=ModItems.ITEMS.register("chud_multitool_shovel",MultiShovel::new);
 public static final DeferredHolder<Item,Item> LIFT=ModItems.ITEMS.register("chud_mining_lift",ChudLiftItem::new);
 public static void init(){}
 public static boolean multi(ItemStack s){return s.is(AXE.get())||s.is(MULTI_PICK.get())||s.is(SHOVEL.get());}
 public static boolean treeAxe(ItemStack s){return s.is(AXE.get());}
 public static boolean silk(ItemStack s){return s.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getBoolean("ChudSilk");}
 private static InteractionResultHolder<ItemStack> switchTool(Level w,Player p,InteractionHand hand){
  ItemStack stack=p.getItemInHand(hand);if(!p.isShiftKeyDown())return InteractionResultHolder.pass(stack);
  // Only air use: item.use is not invoked for accepted native block actions.
  if(!w.isClientSide){
   org.slavicmyths.rpg.tools.ToolRuneRuntime.clear((net.minecraft.server.level.ServerPlayer)p);
   if(stack.is(PICKAXE.get())){boolean nextSilk=!silk(stack);CustomData.update(DataComponents.CUSTOM_DATA,stack,n->n.putBoolean("ChudSilk",nextSilk));}
   else {Item next=stack.is(AXE.get())?MULTI_PICK.get():stack.is(MULTI_PICK.get())?SHOVEL.get():AXE.get();stack=stack.transmuteCopy(next);p.setItemInHand(hand,stack);}
   w.playSound(null,p.blockPosition(),SoundEvents.IRON_TRAPDOOR_CLOSE,SoundSource.PLAYERS,.5F,1.2F);
   p.displayClientMessage(Component.translatable("chud_tools.mode."+(stack.is(PICKAXE.get())?(silk(stack)?"silk":"fortune"):stack.is(AXE.get())?"axe":stack.is(MULTI_PICK.get())?"pickaxe":"shovel")),true);
  }
  return InteractionResultHolder.sidedSuccess(stack,w.isClientSide);
 }
 private static void hint(ItemStack s,List<Component> lines,String key){lines.add(Component.translatable("chud_tools.hint."+key));if(s.is(PICKAXE.get()))lines.add(Component.translatable("chud_tools.mode."+(silk(s)?"silk":"fortune")));}
 static class Described extends Item {private final String hint;Described(Properties p,String hint){super(p);this.hint=hint;}public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){hint(s,t,hint);}}
 static final class MasterPickaxe extends PickaxeItem {
  MasterPickaxe(){super(TIER,new Properties().attributes(PickaxeItem.createAttributes(TIER,1,-2.8F)));}
  @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){return switchTool(w,p,h);}
  // Virtual active enchantments use both NeoForge hooks. Inactive raw enchants are retained,
  // never simultaneously applied, so Fortune III survives Silk mode and native anvil repair.
  @Override public int getEnchantmentLevel(ItemStack s,Holder<Enchantment> e){
   if(e.is(Enchantments.FORTUNE))return silk(s)?0:Math.max(2,s.getTagEnchantments().getLevel(e));
   if(e.is(Enchantments.SILK_TOUCH))return silk(s)?1:0;
   return s.getTagEnchantments().getLevel(e);
  }
  @Override public ItemEnchantments getAllEnchantments(ItemStack s,HolderLookup.RegistryLookup<Enchantment> lookup){
   var out=new ItemEnchantments.Mutable(s.getTagEnchantments());var fortune=lookup.getOrThrow(Enchantments.FORTUNE);var silk=lookup.getOrThrow(Enchantments.SILK_TOUCH);
   out.set(fortune,getEnchantmentLevel(s,fortune));out.set(silk,getEnchantmentLevel(s,silk));return out.toImmutable();
  }
  public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){hint(s,t,"pickaxe");}
 }
 static final class MultiAxe extends AxeItem {MultiAxe(){super(TIER,new Properties().attributes(AxeItem.createAttributes(TIER,5,-3)));}public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){return switchTool(w,p,h);}public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){hint(s,t,"multi");}}
 static final class MultiPick extends PickaxeItem {MultiPick(){super(TIER,new Properties().attributes(PickaxeItem.createAttributes(TIER,1,-2.8F)));}public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){return switchTool(w,p,h);}public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){hint(s,t,"multi");}}
 static final class MultiShovel extends ShovelItem {MultiShovel(){super(TIER,new Properties().attributes(ShovelItem.createAttributes(TIER,1.5F,-3)));}public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){return switchTool(w,p,h);}public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){hint(s,t,"multi");}}
 private ChudTools(){}
}
