package org.slavicmyths.registry;

import java.util.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.garden.Gardens;

/** Standard NeoForge tabs, one deterministic category per displayed mod item. */
public final class ModItemGroup {
    public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,"slavicmyths");
    public static final String[] IDS={"slavicmyths","resources","farming","food","tools","armor","decor","magic","mobs"};
    public static final List<DeferredHolder<CreativeModeTab,CreativeModeTab>> THEMATIC=new ArrayList<>();
    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> TAB;
    static {
        for(int n=0;n<9;n++){
            int index=n;String id=IDS[n];
            THEMATIC.add(TABS.register(id,()->{
                var b=CreativeModeTab.builder(index<5?CreativeModeTab.Row.TOP:CreativeModeTab.Row.BOTTOM,index<5?index:index-5)
                    .title(Component.translatable("itemGroup.slavicmyths."+id)).icon(()->new ItemStack(icon(index)))
                    .displayItems((parameters,output)->items(index).forEach(output::accept));
                b.withTabsBefore(index==0?ResourceLocation.fromNamespaceAndPath("minecraft","spawn_eggs"):ResourceLocation.fromNamespaceAndPath("slavicmyths",IDS[index-1]));
                if(index<8)b.withTabsAfter(ResourceLocation.fromNamespaceAndPath("slavicmyths",IDS[index+1]));
                else b.withTabsAfter(ResourceLocation.fromNamespaceAndPath("curios","curios"));
                return b.build();
            }));
        }
        TAB=THEMATIC.getFirst();
    }
    private static Item icon(int n){return switch(n){case 0->ModItems.LORE_BOOK.get();case 1->ModItems.SILVER_INGOT.get();case 2->ModItems.RYE_SEEDS.get();case 3->ModItems.KARAVAI.get();case 4->ModItems.SICKLE.get();case 5->ModItems.PERUNITE_CHESTPLATE.get();case 6->Gardens.ITEMS.get("apple_planks").get();case 7->ModItems.THUNDER_STONE.get();default->ModItems.DOMOVOY_SPAWN_EGG.get();};}
    private static final Set<String> WEAR=Set.of("invisibility_cap","hunter_belt","seven_league_boots","volchiy_poyas","poyas_tugarina","gambeson","plate_cuirass");
    private static final Set<String> MAGIC=Set.of("thunder_stone","fern_flower","leshy_heart","ancient_sign","ancient_idol","altar","ritual_bowl","ritual_knife","ritual_charcoal","ritual_candle","path_stone","runic_anvil","storm_staff","veles_staff","flying_broom","flying_mortar","pestle","gusli","skatert","badnyak","perun_ring","resin_ring","grave_ward","lunula","grivna","ember_heart","pool_pearl","pool_stone","ancient_water_sign","vodyanoy_net","nav_essence","volkhv_amulet","pustoy_ritualny_sosud","sosud_ognennogo_dyhaniya","sosud_podveya","putevodny_klubok","oko_likha","uzel_durnoy_doli","odnoglazyy_obereg","otvar_ochishcheniya","otvar_lesnoy_zorkosti","yagin_nastoy_stoykosti","otvar_bodrosti","lesnoy_nastoy_vosstanovleniya","letuchaya_maz","uzel_podveya","vetrovoy_uzel","obereg_ohotnika","zolny_obereg","obereg_padayuschey_zvezdy");
    private static final Set<String> PLANTS=Set.of("flax","wormwood","st_johns_wort","nettle","fireweed","juniper_berries","rowan_berries","reed","water_grass","white_lily","field_bundle","svyazka_trav_yagi","organic_fertilizer","turnip","cabbage","pea_pod","flax_stalk");
    private static final Set<String> KITCHEN=Set.of("hearth","kitchen_table","rolling_pin","metal_pot","flour","ground_wormwood","ground_st_johns_wort","juniper_blend");
    private static final Set<String> MATERIAL=Set.of("birch_bark","linen_thread","linen_cloth","perunite","amber","large_animal_bone","weapon_wrap","silver_fitting","iron_rings","old_hook","pearl_fragment","kikimora_lock","noon_ear","ovinnik_claw","nightingale_lock","upyr_fang","grave_cloth_scrap","torn_burial_ribbon","shield_boss_fragment","druzhinnik_blade_fragment","ovinnaya_zola","iskra_ovinnika","klyk_volkolaka","nochnoy_kogot","pylayuschaya_cheshuya","ognennoe_pero","vihrevaya_nit","nit_durnoy_doli","kost_likha","us_tugarina","tugarinova_kozha","zmeinaya_pryazhka","naboyka_tugarina");
    private static final Set<String> TOOLS=Set.of("sickle","field_hoe","watering_can","club","berdysh","flail","fishing_net","armorer_table","bath_broom","bath_stone","herb_pouch","meshochek_trofeev","ohotnichiy_rog","nightingale_whistle","bandit_horn","carved_staff");
    public static int category(Item item){
        if(item==ModItems.BEAR_CUB_SPAWN_EGG.get()||item==ModItems.DOE_SPAWN_EGG.get())return -1;
        String id=BuiltInRegistries.ITEM.getKey(item).getPath();
        if(item instanceof SpawnEggItem||id.endsWith("_spawn_egg"))return 8;
        if(item instanceof ArmorItem||WEAR.contains(id)||id.endsWith("_helmet")||id.endsWith("_chestplate")||id.endsWith("_leggings")||id.endsWith("_boots"))return 5;
        if(MAGIC.contains(id)||id.startsWith("rune_")||id.endsWith("_charm")||id.endsWith("_amulet")||id.startsWith("obereg_"))return 7;
        if(TOOLS.contains(id)||item instanceof TieredItem||item instanceof ShieldItem||item instanceof BowItem||item instanceof CrossbowItem||id.matches(".*_(sword|dagger|spear|axe|pickaxe|shovel|hoe|knife|mace|shield|arrow)$")||id.equals("mace")||id.equals("chekan"))return 4;
        if(PLANTS.contains(id)||id.endsWith("_seeds")||id.endsWith("_grain")||id.endsWith("_sapling")||Arrays.asList(Gardens.BERRIES).contains(id))return 2;
        if(KITCHEN.contains(id)||item.components().has(net.minecraft.core.component.DataComponents.FOOD))return 3;
        if(MATERIAL.contains(id)||id.endsWith("_ore")||id.endsWith("_ingot")||id.endsWith("_nugget"))return 1;
        if(item instanceof BlockItem||item instanceof SignItem||item instanceof HangingSignItem)return 6;
        return 0;
    }
    public static List<Item> items(int category){
        var items=ModItems.ITEMS.getEntries().stream().map(e->e.get()).filter(i->category(i)==category).sorted(Comparator.comparing(i->BuiltInRegistries.ITEM.getKey(i).getPath())).collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        var preferred=new ArrayList<Item>();
        if(category==2){
            for(var item:org.slavicmyths.farming.Farming.creativeItems())if(category(item)==2)preferred.add(item);
            for(var id:Gardens.BERRIES)preferred.add(Gardens.ITEMS.get(id).get());
            for(var id:Gardens.BERRIES)preferred.add(Gardens.ITEMS.get(id+"_sapling").get());
            preferred.add(Gardens.ITEMS.get("apple_sapling").get());
        }
        if(category==6)for(var id:new String[]{"apple_log","stripped_apple_log","apple_wood","stripped_apple_wood","apple_planks","apple_stairs","apple_slab","apple_fence","apple_fence_gate","apple_door","apple_trapdoor","apple_pressure_plate","apple_button","apple_sign","apple_hanging_sign","apple_leaves"})preferred.add(Gardens.ITEMS.get(id).get());
        items.removeAll(preferred);preferred.addAll(items);return preferred;
    }
    private ModItemGroup(){}
}
