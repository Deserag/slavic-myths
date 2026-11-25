package org.slavicmyths.desert;
import java.util.*;
import java.util.function.Supplier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
public final class DesertBlocks {
    public static final Map<String, DeferredHolder<Block, Block>> ALL = new LinkedHashMap<>();
    static {
        family("ancient_sandstone", Blocks.SANDSTONE);
        family("sandstone_bricks", Blocks.SANDSTONE);
        family("cracked_sandstone_bricks", Blocks.SANDSTONE);
        family("dark_sandstone", Blocks.SANDSTONE);
        solid("carved_solar_sandstone", Blocks.SANDSTONE);
        solid("carved_serpent_sandstone", Blocks.SANDSTONE);
        solid("carved_wind_sandstone", Blocks.SANDSTONE);
        solid("compacted_sand", Blocks.SAND);
        solid("cracked_dry_earth", Blocks.DIRT);
        add("quicksand", () -> new QuicksandBlock(props(Blocks.SAND).noCollission().noOcclusion()));
        solid("salt_crust", Blocks.SAND);
        solid("sandstone_rubble", Blocks.SANDSTONE);
        solid("fossilized_bones", Blocks.SANDSTONE);
        add("solar_seal", () -> new DesertSealBlock(props(Blocks.SANDSTONE).noOcclusion().dynamicShape().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
        add("desert_serpent_seal", () -> new DesertSealBlock(props(Blocks.SANDSTONE).noOcclusion().dynamicShape().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
        add("wind_seal", () -> new DesertSealBlock(props(Blocks.SANDSTONE).noOcclusion().dynamicShape().pushReaction(net.minecraft.world.level.material.PushReaction.BLOCK)));
    }
    private static BlockBehaviour.Properties props(Block b){return BlockBehaviour.Properties.ofFullCopy(b);}
    private static void add(String id,Supplier<Block> factory){
        var block=ModBlocks.BLOCKS.register(id,factory);ALL.put(id,block);
        ModItems.ITEMS.register(id,()->new BlockItem(block.get(),new Item.Properties()));
    }
    private static void solid(String id,Block b){add(id,()->new Block(props(b)));}
    private static void family(String id,Block b){
        solid(id,b);
        add(id+"_stairs",()->new StairBlock(ALL.get(id).get().defaultBlockState(),props(b)));
        add(id+"_slab",()->new SlabBlock(props(b)));
        add(id+"_wall",()->new WallBlock(props(b)));
    }
    public static void init(){}
    private DesertBlocks(){}
}
