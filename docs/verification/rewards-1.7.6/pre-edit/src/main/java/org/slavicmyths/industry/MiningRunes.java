package org.slavicmyths.industry;
import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.rpg.*;
public final class MiningRunes {
    public static final Set<String> IDS=Set.of("mining_1","mining_2","fortune_1");
    public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
    static {for(String id:List.of("mining_1","mining_2","fortune_1")){String item=itemId(id);ITEMS.put(id,ModItems.ITEMS.register(item,()->new IndustryItem(new Item.Properties(),item)));}}
    public static void init(){for(String id:IDS)RuneDefinition.ALL.put(id,new RuneDefinition(id,Map.of(),Set.of("pickaxe")));}
    public static boolean mining(String id){return IDS.contains(id);}
    public static String itemId(String id){return id.startsWith("mining")?"mining_rune_"+id.charAt(id.length()-1):"fortune_rune_1";}
    public static Component name(String id){return Component.translatable("item.slavicmyths."+itemId(id));}
    public static List<Component> tooltip(String id){return List.of(Component.translatable("rune_foundation.slavicmyths.tier",id.equals("mining_2")?"II":"I"),Component.translatable("industry.slavicmyths.hint."+itemId(id)),Component.translatable("industry.slavicmyths.miner_sells_runes"));}
    public static double speed(List<String> runes,boolean ring){return IndustryRules.speed(runes,ring);}
    public static int fortune(int level,boolean rune,boolean silk){return IndustryRules.fortune(level,rune,silk);}
    private MiningRunes(){}
}
