package org.slavicmyths.netherboss;

import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

public final class NetherBossItems {
 public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
 public static final List<String> COMPONENTS=List.of("morova_thread","faded_rite_seal","ancestor_bone_charm","nameless_ashes","broken_watch_horn","darkened_waymark","mourning_circlet","charred_signal_ribbon");
 static {
  register("morova_deva_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(org.slavicmyths.registry.ModEntities.MOROVA_DEVA,0xC9E1D8,0x8F1C52,new Item.Properties()));
  register("trizna_prince_spawn_egg",()->new net.neoforged.neoforge.common.DeferredSpawnEggItem(org.slavicmyths.registry.ModEntities.TRIZNA_PRINCE,0x1D202A,0xE6681E,new Item.Properties()));
  for(String id:COMPONENTS)register(id,()->new Item(new Item.Properties().rarity(Rarity.RARE)));
  for(String id:List.of("legendary_soul","scroll_of_waning","scroll_of_last_call","rune_morova_lingering","rune_trizna_ember_ward"))register(id,()->new Item(new Item.Properties().rarity(Rarity.EPIC)));
  register("amulet_of_the_faded",()->new Summoner(true));register("last_watch_horn",()->new Summoner(false));
  register("morova_silence_pendant",()->new Trophy(true));register("fallen_druzhina_horn",()->new Trophy(false));
 }
 private static void register(String id,java.util.function.Supplier<Item> factory){ITEMS.put(id,ModItems.ITEMS.register(id,factory));}
 public static Item item(String id){return ITEMS.get(id).get();}
 public static void init(){}
 private static final class Summoner extends Item {
  final boolean morova;Summoner(boolean m){super(new Properties().stacksTo(1).rarity(Rarity.EPIC));morova=m;}
  @Override public void appendHoverText(ItemStack stack,TooltipContext c,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){lines.add(net.minecraft.network.chat.Component.translatable(morova?"netherboss.tooltip.morova_rite":"netherboss.tooltip.trizna_rite"));}
  @Override public InteractionResult useOn(UseOnContext c){
   if(c.getLevel().isClientSide)return InteractionResult.SUCCESS;
   return c.getPlayer() instanceof ServerPlayer p&&NetherRituals.begin(p,c.getClickedPos(),c.getHand(),morova)?InteractionResult.CONSUME:InteractionResult.FAIL;
  }
 }
 public static final class Trophy extends Item implements ICurioItem {
  final boolean pendant;Trophy(boolean p){super(new Properties().stacksTo(1).rarity(Rarity.EPIC));pendant=p;}
  @Override public void appendHoverText(ItemStack stack,TooltipContext c,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){lines.add(net.minecraft.network.chat.Component.translatable(pendant?"netherboss.tooltip.pendant":"netherboss.tooltip.horn"));lines.add(net.minecraft.network.chat.Component.translatable("netherboss.tooltip.controls"));}
  @Override public boolean canEquip(SlotContext c,ItemStack stack){return c.identifier().equals(pendant?"necklace":"belt");}
  @Override public InteractionResultHolder<ItemStack> use(Level world,Player player,InteractionHand hand){
   if(world.isClientSide)return InteractionResultHolder.success(player.getItemInHand(hand));
   return player instanceof ServerPlayer p&&NetherTrophies.activate(p,pendant)?InteractionResultHolder.consume(player.getItemInHand(hand)):InteractionResultHolder.fail(player.getItemInHand(hand));
  }
 }
 private NetherBossItems(){}
}
