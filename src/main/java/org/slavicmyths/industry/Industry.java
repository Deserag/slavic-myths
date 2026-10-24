package org.slavicmyths.industry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.*;
import org.slavicmyths.registry.*;

/** Only the two commissioned machines and the bog ore; existing registries remain canonical. */
public final class Industry {
    public static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath("slavicmyths",name);}
    public static final DeferredHolder<Block,Block> ORE=ModBlocks.BLOCKS.register("bog_iron_ore",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)));
    public static final DeferredHolder<Block,MachineBlock> BLOOMERY=ModBlocks.BLOCKS.register("chud_bloomery",()->new MachineBlock(false,BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE).lightLevel(s->s.getValue(MachineBlock.LIT)?11:0)));
    public static final DeferredHolder<Block,MachineBlock> HEARTH=ModBlocks.BLOCKS.register("refining_hearth",()->new MachineBlock(true,BlockBehaviour.Properties.ofFullCopy(Blocks.BLAST_FURNACE).lightLevel(s->s.getValue(MachineBlock.LIT)?11:0)));
    public static final DeferredHolder<Item,Item> ORE_ITEM=ModItems.ITEMS.register("bog_iron_ore",()->new BlockItem(ORE.get(),new Item.Properties()));
    public static final DeferredHolder<Item,Item> BLOOMERY_ITEM=ModItems.ITEMS.register("chud_bloomery",()->new BlockItem(BLOOMERY.get(),new Item.Properties()));
    public static final DeferredHolder<Item,Item> HEARTH_ITEM=ModItems.ITEMS.register("refining_hearth",()->new BlockItem(HEARTH.get(),new Item.Properties()));
    public static final DeferredHolder<Item,Item> LUMP=item("bog_iron_lump");
    public static final DeferredHolder<Item,Item> FLUX=item("purification_flux");
    public static final DeferredHolder<Item,Item> IRON=item("purified_iron_ingot");
    public static final DeferredHolder<Item,Item> COPPER=item("purified_copper_ingot");
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<MachineTile>> BLOOMERY_TILE=ModTiles.TILES.register("chud_bloomery",()->BlockEntityType.Builder.of((p,s)->new MachineTile(p,s,false),BLOOMERY.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<MachineTile>> HEARTH_TILE=ModTiles.TILES.register("refining_hearth",()->BlockEntityType.Builder.of((p,s)->new MachineTile(p,s,true),HEARTH.get()).build(null));
    private static DeferredHolder<Item,Item> item(String name){return ModItems.ITEMS.register(name,()->new IndustryItem(new Item.Properties(),name));}
    public static void register(IEventBus bus){ChudTools.init();MachineRecipe.TYPES.register(bus);MachineRecipe.SERIALIZERS.register(bus);MachineMenu.init();MiningRunes.init();IndustryAccessories.init();ChudPeople.init(bus);}
    private Industry(){}
}
