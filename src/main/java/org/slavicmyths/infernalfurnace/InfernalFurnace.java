package org.slavicmyths.infernalfurnace;
import net.minecraft.core.registries.Registries;import net.minecraft.world.item.*;import net.minecraft.world.level.block.*;import net.minecraft.world.level.block.entity.*;import net.minecraft.world.level.block.state.BlockBehaviour;import net.minecraft.world.inventory.MenuType;import net.minecraft.world.flag.FeatureFlags;import net.neoforged.neoforge.registries.DeferredHolder;import org.slavicmyths.registry.*;
/** Functional prototype. Recipe, speed and fuel economy remain BALANCE_OWNER_PENDING. */
public final class InfernalFurnace {
 public static final int TICKS_PER_TICK=2,FUEL_INTERNAL_TICKS=3200;
 public static final DeferredHolder<Block,InfernalFurnaceBlock> BLOCK=ModBlocks.BLOCKS.register("infernal_furnace",()->new InfernalFurnaceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.FURNACE).strength(4).lightLevel(s->s.getValue(AbstractFurnaceBlock.LIT)?13:0)));
 public static final DeferredHolder<Item,Item> ITEM=ModItems.ITEMS.register("infernal_furnace",()->new BlockItem(BLOCK.get(),new Item.Properties().fireResistant()));
 public static final DeferredHolder<Item,Item> COAL=ModItems.ITEMS.register("infernal_coal",()->new Item(new Item.Properties().fireResistant()));
 public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<InfernalFurnaceTile>> TILE=ModTiles.TILES.register("infernal_furnace",()->BlockEntityType.Builder.of(InfernalFurnaceTile::new,BLOCK.get()).build(null));
 public static final DeferredHolder<MenuType<?>,MenuType<InfernalFurnaceMenu>> MENU=org.slavicmyths.rpg.RpgMenu.MENUS.register("infernal_furnace",()->new MenuType<>(InfernalFurnaceMenu::new,FeatureFlags.VANILLA_SET));
 public static boolean fuel(ItemStack s){return s.is(COAL.get());}
 public static void init(){}
 private InfernalFurnace(){}
}
