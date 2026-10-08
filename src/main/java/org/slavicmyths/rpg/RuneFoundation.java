package org.slavicmyths.rpg;

import java.util.*;
import net.minecraft.world.item.*;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.item.ItemState;

public final class RuneFoundation {
    public static final Map<String,DeferredHolder<Item,Item>> ITEMS = new LinkedHashMap<>();
    static {
        ITEMS.put("rune_chisel",ModItems.ITEMS.register("rune_chisel",()->new FoundationItem("rune_chisel",new Item.Properties().durability(192))));
        for(String id:List.of("iron_dust","gold_dust","coal_dust","lapis_dust","diamond_dust","perunite_dust","soul_fragment","soul","empowered_soul","bound_soul"))
            ITEMS.put(id,ModItems.ITEMS.register(id,()->new FoundationItem(id,new Item.Properties())));
        for(int tier=1;tier<=3;tier++) {
            int t=tier;
            ITEMS.put("blank_rune_"+t,ModItems.ITEMS.register("blank_rune_"+t,()->new BlankItem(t)));
        }
    }
    public static void init() { }
    public static Item item(String id) { return ITEMS.get(id).get(); }
    public static ItemStack blank(int tier,String material) {
        ItemStack s=new ItemStack(item("blank_rune_"+tier));s.set(ItemState.RUNE_BASE.get(),new RuneBase(tier,material));return s;
    }
    public static List<ItemStack> creative() {
        List<ItemStack> out=new ArrayList<>();
        for(var entry:ITEMS.entrySet()) {
            if(entry.getKey().equals("blank_rune_1")){out.add(blank(1,"iron"));out.add(blank(1,"gold"));}
            else if(entry.getKey().equals("blank_rune_2")){out.add(blank(2,"diamond"));out.add(blank(2,"perunite"));}
            else out.add(new ItemStack(entry.getValue().get()));
        }return out;
    }
    private static final class BlankItem extends Item {
        BlankItem(int tier) {super(new Item.Properties().component(ItemState.RUNE_BASE.get(),new RuneBase(tier,tier==1?"iron":tier==2?"diamond":"perunite")));}
        @Override public void appendHoverText(ItemStack s,TooltipContext context,List<Component> lines,TooltipFlag flag) {
            RuneBase b=s.get(ItemState.RUNE_BASE.get());
            if(b!=null){lines.add(Component.translatable("rune_foundation.slavicmyths.tier",new String[]{"","I","II","III"}[b.tier()]));
                lines.add(Component.translatable("rune_foundation.slavicmyths.base",Component.translatable("rune_foundation.slavicmyths.material."+b.material())));}
            lines.add(Component.translatable("rune_foundation.slavicmyths.no_effect"));
        }
    }
    private static final class FoundationItem extends Item {
        private final String id;
        FoundationItem(String id,Properties props){super(props);this.id=id;}
        @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("rune_foundation.slavicmyths.info."+id));}
    }
    private RuneFoundation() { }
}
