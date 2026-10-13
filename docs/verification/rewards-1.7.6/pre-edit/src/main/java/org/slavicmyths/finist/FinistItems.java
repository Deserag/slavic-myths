package org.slavicmyths.finist;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;
public final class FinistItems {
 public static final DeferredHolder<Item,Item> RING=ModItems.ITEMS.register("ring_of_grey_pack",()->new GreyPackRing(new Item.Properties().rarity(Rarity.RARE)));
 public static final DeferredHolder<Item,Item> BOW=ModItems.ITEMS.register("finist_bow",()->new FinistBow(new Item.Properties().durability(640).rarity(Rarity.RARE)));
 public static final DeferredHolder<Item,Item> QUIVER=ModItems.ITEMS.register("finist_quiver",()->new FinistQuiver(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
 public static void init(){}
 private FinistItems(){}
}
