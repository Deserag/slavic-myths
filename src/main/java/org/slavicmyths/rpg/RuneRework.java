package org.slavicmyths.rpg;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;

/** Canonical extension of existing Runes, not another installed-rune component. */
public final class RuneRework {
    public static final TagKey<Item> MELEE=ItemTags.create(ResourceLocation.fromNamespaceAndPath("slavicmyths","rune_melee_weapons"));
    public static final TagKey<Item> HEAVY=ItemTags.create(ResourceLocation.fromNamespaceAndPath("slavicmyths","rune_heavy"));
    public static final Map<String,DeferredHolder<Item,Item>> NEW_ITEMS=new LinkedHashMap<>();
    static {for(var d:RuneBalance.DEFINITIONS)if(!Set.of("heat","wind").contains(d.id()))NEW_ITEMS.put(d.id(),ModItems.ITEMS.register("rune_"+d.id(),()->new Item(new Item.Properties())));}
    public static void init(){}
    public static boolean boots(ItemStack s){return s.getItem() instanceof ArmorItem a&&a.getType()==ArmorItem.Type.BOOTS;}
    public static boolean melee(ItemStack s){return s.is(MELEE)&&!(s.getItem() instanceof BowItem||s.getItem() instanceof CrossbowItem||s.getItem() instanceof ShieldItem||s.getItem() instanceof ArmorItem);}
    public static boolean ranged(ItemStack s){return s.getItem() instanceof BowItem||s.getItem() instanceof CrossbowItem;}
    public static boolean supports(ItemStack s,String id){return switch(id){
        case "strength","crushing","blood"->melee(s);
        case "speed"->melee(s)||boots(s);
        case "resilience","fortitude"->Runes.armor(s)||s.getItem() instanceof ShieldItem;
        case "heat"->melee(s)||Runes.armor(s);
        case "wind"->boots(s)||ranged(s);
        default->false;
    };}
    public static int copies(ItemStack s,String id){return (int)Runes.list(s).stream().filter(id::equals).count();}
    public static int legacyCopies(ItemStack s,String id){return copies(s,id)>0&&org.slavicmyths.item.ItemState.legacyRuneSpecials(s).contains(id)?1:0;}
    public static Component compatibility(String id){return Component.translatable("rune2.slavicmyths.compatible_"+id);}
    private static Component line(String key,double... values){Object[] args=Arrays.stream(values).mapToObj(RuneBalance::format).toArray();return Component.translatable("rune2.slavicmyths."+key,args);}
    public static List<Component> effects(String id,int copies,ItemStack equipment){
        double p=RuneBalance.effective(copies),n=RuneBalance.penalty(copies);boolean general=equipment==null;var out=new ArrayList<Component>();
        switch(id){
            case "strength"->out.add(line("damage",Math.min(RuneBalance.DAMAGE_CAP,RuneBalance.STRENGTH*p)*100));
            case "speed"->{if(general||melee(equipment))out.add(line("attack",Math.min(RuneBalance.ATTACK_BONUS_CAP,RuneBalance.SPEED_ATTACK*p)*100));if(general||boots(equipment))out.add(line("movement",Math.min(RuneBalance.MOVEMENT_CAP,RuneBalance.SPEED_MOVE*p)*100));}
            case "resilience"->{if(general||Runes.armor(equipment))out.add(line("toughness",Math.min(RuneBalance.TOUGHNESS_CAP,RuneBalance.RESILIENCE_TOUGHNESS*p)));if(general||equipment.getItem() instanceof ShieldItem)out.add(line("shield",Math.min(RuneBalance.SHIELD_CAP,RuneBalance.SHIELD_EFFICIENCY*p)*100));}
            case "heat"->{if(general||melee(equipment))out.add(line("fire",Math.min(RuneBalance.PROC_CAP,RuneBalance.FIRE_CHANCE*p)*100,RuneBalance.FIRE_SECONDS));if(general||Runes.armor(equipment))out.add(line("fire_defense",Math.min(RuneBalance.FIRE_CAP,RuneBalance.FIRE_REDUCTION*p)*100));}
            case "crushing"->{out.add(line("damage",Math.min(RuneBalance.DAMAGE_CAP,RuneBalance.CRUSHING_DAMAGE*p)*100));out.add(line("attack_penalty",Math.min(RuneBalance.ATTACK_PENALTY_CAP,RuneBalance.CRUSHING_SLOW*n)*100).copy().withStyle(ChatFormatting.RED));}
            case "wind"->{if(general||boots(equipment)){out.add(line("movement",Math.min(RuneBalance.MOVEMENT_CAP,RuneBalance.WIND_MOVE*p)*100));out.add(line("jump",Math.min(RuneBalance.JUMP_CAP,RuneBalance.WIND_JUMP*p)*100));}if(general||ranged(equipment))out.add(line("projectile",Math.min(RuneBalance.PROJECTILE_CAP,RuneBalance.WIND_PROJECTILE*p)*100));}
            case "blood"->out.add(line("bleed",Math.min(RuneBalance.PROC_CAP,RuneBalance.BLOOD_CHANCE*p)*100,RuneBalance.RUNE_BLEED_MAX,RuneBalance.BLEED_HP,RuneBalance.BLEED_INTERVAL/20.0,RuneBalance.BLEED_DURATION/20.0));
            case "fortitude"->{if(general||Runes.armor(equipment)){out.add(line("toughness",Math.min(RuneBalance.TOUGHNESS_CAP,RuneBalance.FORT_TOUGHNESS*p)));out.add(line("kb",Math.min(RuneBalance.KB_CAP,RuneBalance.FORT_KB*p)*100));out.add(line("movement_penalty",Math.min(RuneBalance.MOVEMENT_PENALTY_CAP,RuneBalance.FORT_SLOW*n)*100).copy().withStyle(ChatFormatting.RED));}if(general||equipment.getItem() instanceof ShieldItem)out.add(line("shield",Math.min(RuneBalance.SHIELD_CAP,RuneBalance.SHIELD_EFFICIENCY*p)*100));}
        }return out;
    }
    public static List<Component> runeTooltip(String id){var d=RuneBalance.definition(id);var out=new ArrayList<Component>();out.add(Component.translatable("rune_foundation.slavicmyths.tier",d.tier()==1?"I":"II"));out.add(Component.translatable("rune_foundation.slavicmyths.base",Component.translatable("rune_foundation.slavicmyths.material."+d.base())));out.addAll(effects(id,1,null));return out;}
    public static List<Component> equipmentTooltip(ItemStack s){var out=new ArrayList<Component>();var ids=new LinkedHashSet<>(Runes.list(s));for(String id:ids){int count=copies(s,id);Component name=Component.translatable("item.slavicmyths.rune_"+id);out.add(count>1?name.copy().append(" ×"+count):name);
        if(RareRunes.rare(id))out.addAll(RareRunes.tooltip(id));
        else if(id.equals("wind")&&ranged(s)){out.add(line("projectile",RuneRuntime.projectileBonus(s)*100));if(legacyCopies(s,id)>0){out.add(Component.translatable("rune2.slavicmyths.legacy_special"));out.add(RuneDefinition.ALL.get(id).legacyEffect());}}
        else if(id.equals("heat")&&melee(s)&&legacyCopies(s,id)>0){out.add(Component.translatable("rune2.slavicmyths.legacy_special"));out.add(RuneDefinition.ALL.get(id).legacyEffect());if(count>1)out.add(line("fire",Math.min(RuneBalance.PROC_CAP,RuneBalance.FIRE_CHANCE*(RuneBalance.effective(count)-1))*100,RuneBalance.FIRE_SECONDS));}
        else if(RuneBalance.modern(id)&&supports(s,id))out.addAll(effects(id,count,s));else if(RuneBalance.modern(id)){out.add(Component.translatable("rune2.slavicmyths.legacy_special"));out.add(RuneDefinition.ALL.get(id).legacyEffect());}
        else if(Runes.armor(s))out.add(Component.translatable("rpg.slavicmyths.armor_runes_inactive"));
    }return out;}
    private RuneRework(){}
}
