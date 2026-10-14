package org.slavicmyths.rpg.tools;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import org.slavicmyths.rpg.Runes;

/** Existing RuneRework registry and ItemState sockets remain authoritative. */
public final class ToolRuneContent {
 public static boolean supports(ItemStack s,String id){return switch(ToolRuneRules.family(id)){
  case "miner"->s.getItem() instanceof PickaxeItem;
  case "excavator"->s.getItem() instanceof ShovelItem;
  case "farmer"->s.getItem() instanceof HoeItem;
  case "lumberjack"->s.getItem() instanceof AxeItem;
  default->false;
 };}
 public static Component effect(String id){return Component.translatable("tool_runes.effect."+id);}
 public static Component compatible(String id){return Component.translatable("tool_runes.compatible."+ToolRuneRules.family(id));}
 public static List<Component> tooltip(String id){return List.of(Component.translatable("rune_foundation.slavicmyths.tier",ToolRuneRules.tier(id)==4?"IV":"III"),effect(id),compatible(id),Component.translatable("tool_runes.crafting"));}
 public static List<Component> equipped(ItemStack s,String id){var lines=new ArrayList<>(tooltip(id));if(!supports(s,id)||ToolRuneRules.tier(Runes.list(s),ToolRuneRules.family(id))!=ToolRuneRules.tier(id))lines.add(Component.translatable("tool_runes.inactive"));return lines;}
 private ToolRuneContent(){}
}
