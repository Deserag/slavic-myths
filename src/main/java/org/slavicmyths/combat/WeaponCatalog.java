package org.slavicmyths.combat;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.item.ModGear;

/** Actual material families; old spear, silver_spear and flail IDs stay canonical. */
public final class WeaponCatalog {
    public record Material(String id, Tier tier, Supplier<Item> ingredient, int sockets) { }
    public static final List<Material> MATERIALS=List.of(
        new Material("stone",Tiers.STONE,()->Items.COBBLESTONE,1),
        new Material("iron",Tiers.IRON,()->Items.IRON_INGOT,1),
        new Material("gold",Tiers.GOLD,()->Items.GOLD_INGOT,1),
        new Material("diamond",Tiers.DIAMOND,()->Items.DIAMOND,2),
        new Material("netherite",Tiers.NETHERITE,()->Items.NETHERITE_INGOT,2),
        new Material("silver",ModGear.SILVER,()->ModItems.SILVER_INGOT.get(),1),
        new Material("perunite",ModGear.PERUNITE,()->ModItems.PERUNITE.get(),3));
    public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
    public static String weaponId(String material,String type) {
        if(type.equals("spear")&&material.equals("stone"))return "spear";
        if(type.equals("flail")&&material.equals("iron"))return "flail";
        return material+"_"+type;
    }
    private static Item.Properties properties(String material) {
        Item.Properties p=new Item.Properties();return material.equals("netherite")?p.fireResistant():p;
    }
    public static void register() {
        if(!ITEMS.isEmpty())return;
        for(String id:List.of("iron_blank","spear_shaft","short_shaft","flail_handle","flail_chain"))
            ITEMS.put(id,ModItems.ITEMS.register(id,()->new Item(new Item.Properties())));
        for(Material m:MATERIALS) {
            for(String part:List.of("spearhead","light_spearhead","flail_ball")) {
                String id=m.id()+"_"+part;
                ITEMS.put(id,ModItems.ITEMS.register(id,()->new Item(properties(m.id()))));
            }
            for(String type:List.of("spear","sulitsa","flail","throwing_knife")) {
                String id=weaponId(m.id(),type);
                if(id.equals("spear")||id.equals("silver_spear")||id.equals("flail"))continue;
                ITEMS.put(id,ModItems.ITEMS.register(id,()->switch(type) {
                    case "spear" -> new org.slavicmyths.item.SpearItem(m.tier(),properties(m.id()));
                    case "sulitsa" -> new ThrowingWeaponItem(m.tier(),false,properties(m.id()));
                    case "throwing_knife" -> new ThrowingWeaponItem(m.tier(),true,properties(m.id()));
                    default -> new FlailItem(m.tier(),properties(m.id()));
                }));
            }
        }
        ITEMS.put("short_bow",ModItems.ITEMS.register("short_bow",()->new FieldBowItem(false,new Item.Properties().durability(300))));
        ITEMS.put("heavy_bow",ModItems.ITEMS.register("heavy_bow",()->new FieldBowItem(true,new Item.Properties().durability(500))));
    }
    public static Item item(String id) {
        return switch(id) {case "spear"->ModItems.SPEAR.get();case "silver_spear"->ModItems.SILVER_SPEAR.get();case "flail"->ModItems.FLAIL.get();default->ITEMS.get(id).get();};
    }
    private WeaponCatalog() { }
}
