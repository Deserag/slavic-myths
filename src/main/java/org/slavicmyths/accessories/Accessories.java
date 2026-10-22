package org.slavicmyths.accessories;

import java.util.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.slavicmyths.registry.ModItems;
import top.theillusivec4.curios.api.*;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Six real accessories, using existing slots and the normal item registration bus. */
public final class Accessories {
    public static final Map<String,net.neoforged.neoforge.registries.DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
    public static final Map<String,String> SLOTS=Map.of("pool_ring","ring","bath_ward","charm","harvest_ring","ring",
            "podvey_belt","belt","leshy_necklace","necklace","spark_ring","ring");
    public static final ResourceLocation SWIM=ResourceLocation.fromNamespaceAndPath("slavicmyths","pool_ring_swim");
    static {for(var id:List.of("pool_ring","bath_ward","harvest_ring","podvey_belt","leshy_necklace","spark_ring"))
        ITEMS.put(id,ModItems.ITEMS.register(id,()->new Accessory(id,SLOTS.get(id))));}
    public static void init(){}
    public static boolean wears(net.minecraft.world.entity.LivingEntity e,String id){
        return CuriosApi.getCuriosInventory(e).map(h->h.findCurios(s->s.is(ITEMS.get(id).get())).stream()
                .anyMatch(r->r.slotContext().identifier().equals(SLOTS.get(id)))).orElse(false);
    }
    private static void swim(net.minecraft.world.entity.LivingEntity e,boolean enabled){
        var attr=e.getAttribute(NeoForgeMod.SWIM_SPEED);if(attr==null)return;
        if(enabled&&attr.getModifier(SWIM)==null)attr.addTransientModifier(new AttributeModifier(SWIM,.15,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if(!enabled)attr.removeModifier(SWIM);
    }
    public static final class Accessory extends Item implements ICurioItem {
        private final String id,slot;
        Accessory(String id,String slot){super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));this.id=id;this.slot=slot;}
        @Override public boolean canEquip(SlotContext c,ItemStack s){return c.identifier().equals(slot);}
        @Override public void onEquip(SlotContext c,ItemStack old,ItemStack s){
            if(!c.entity().level().isClientSide)org.slavicmyths.item.FolkAccessoryItem.award(c.entity(),"first_accessory");
        }
        @Override public void curioTick(SlotContext c,ItemStack s){
            if(id.equals("pool_ring")&&!c.entity().level().isClientSide)
                swim(c.entity(),c.entity().isAlive()&&c.entity().isInWater()&&wears(c.entity(),id));
        }
        @Override public void onUnequip(SlotContext c,ItemStack next,ItemStack s){
            if(!id.equals("pool_ring")||c.entity().level().isClientSide)return;
            boolean other=CuriosApi.getCuriosInventory(c.entity()).map(h->h.findCurios(v->v.is(this)).stream()
                    .anyMatch(r->r.slotContext().identifier().equals(slot)&&(!r.slotContext().identifier().equals(c.identifier())||r.slotContext().index()!=c.index()))).orElse(false);
            swim(c.entity(),c.entity().isInWater()&&(next.is(this)||other));
        }
        @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> text,TooltipFlag flags){
            text.add(Component.translatable(getDescriptionId()+".effect").withStyle(net.minecraft.ChatFormatting.GRAY));
        }
    }
    private Accessories(){}
}
