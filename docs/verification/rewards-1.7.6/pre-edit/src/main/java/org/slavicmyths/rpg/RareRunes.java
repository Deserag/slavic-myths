package org.slavicmyths.rpg;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;

public final class RareRunes {
    public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
    static {
        for(String id:RareRuneRules.IDS)ITEMS.put("rune_"+id,ModItems.ITEMS.register("rune_"+id,()->new Item(new Item.Properties().rarity(Rarity.RARE))));
        for(String id:List.of("flight","rare_protection","death","berserker"))ITEMS.put("rune_"+id+"_fragment",ModItems.ITEMS.register("rune_"+id+"_fragment",()->new Item(new Item.Properties().rarity(Rarity.UNCOMMON))));
    }
    public static void init(){}
    public static boolean rare(String id){return RareRuneRules.IDS.contains(id);}
    public static boolean supports(ItemStack s,String id){return switch(id){case "flight"->RuneRework.boots(s);case "rare_protection"->s.getItem() instanceof ArmorItem a&&a.getType()==ArmorItem.Type.CHESTPLATE;default->rare(id)&&RuneRework.melee(s);};}
    public static boolean equipped(Player p,String id){return Runes.has(equipment(p,id),id);}
    public static ItemStack equipment(Player p,String id){return id.equals("flight")?p.getItemBySlot(EquipmentSlot.FEET):id.equals("rare_protection")?p.getItemBySlot(EquipmentSlot.CHEST):p.getMainHandItem();}
    public static List<String> active(Player p){return RareRuneRules.ACTIVE.stream().filter(id->equipped(p,id)&&supports(equipment(p,id),id)).toList();}
    public static Component text(String key,Object...args){return Component.translatable("rare_runes.slavicmyths."+key,args);}
    public static Component compatibility(String id){return text(id.equals("flight")?"boots":id.equals("rare_protection")?"chest":"melee");}
    public static List<Component> tooltip(String id){return List.of(text("rare"),text("source_"+id),text("effect_"+id),compatibility(id));}
    public static int equippedCopies(Player p,String id){int n=0;for(var slot:EquipmentSlot.values()){if(slot==EquipmentSlot.BODY)continue;for(String rune:Runes.list(p.getItemBySlot(slot)))if(rune.equals(id))n++;}return n;}
    private RareRunes(){}
}
