package org.slavicmyths.savanna;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
public final class SteppeCloak extends Item implements ICurioItem {
 public static final DeferredHolder<Item,Item> ITEM=ModItems.ITEMS.register("steppe_prince_cloak",()->new SteppeCloak());
 public static void init(){}
 private SteppeCloak(){super(new Properties().stacksTo(1).rarity(Rarity.EPIC));}
 @Override public Component getName(ItemStack stack){return Component.translatableWithFallback(getDescriptionId(),"Плащ князя земель");}
 @Override public boolean canEquip(SlotContext c,ItemStack s){return "charm".equals(c.identifier());}
 public static boolean worn(LivingEntity e){return CuriosApi.getCuriosInventory(e).map(i->i.findCurios(s->s.is(ITEM.get())).stream().anyMatch(r->"charm".equals(r.slotContext().identifier()))).orElse(false);}
}
