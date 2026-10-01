package org.slavicmyths.wood;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.tileentity.*;
import net.minecraftforge.fml.RegistryObject;
import org.slavicmyths.registry.*;

/** Ordered wood families; existing registries and creative tab are retained. */
public final class Woodlands {
    public static final Map<String,Set> SETS = new LinkedHashMap<>();
    public static final RegistryObject<Item> BERRIES;
    public static final RegistryObject<Block> HANGING;
    public static final RegistryObject<TileEntityType<WoodSignTile>> SIGN_TILE;
    static {
        for(String name:new String[]{"linden","rowan","willow","pine"}) SETS.put(name,new Set(name));
        BERRIES=ModItems.ITEMS.register("rowan_berries",()->new Item(props().food(new Food.Builder().nutrition(1).saturationMod(.2F).build())));
        HANGING=ModBlocks.BLOCKS.register("hanging_willow_leaves",HangingLeaves::new);
        ModItems.ITEMS.register("hanging_willow_leaves",()->new BlockItem(HANGING.get(),props()));
        SIGN_TILE=ModTiles.TILES.register("wood_sign",()->TileEntityType.Builder.of(WoodSignTile::new,SETS.values().stream().flatMap(s->java.util.stream.Stream.of(s.get("sign"),s.get("wall_sign"))).toArray(Block[]::new)).build(null));
    }
    public static void init() { WoodlandWorldgen.init(); }
    public static Item.Properties props(){return new Item.Properties().tab(ModItemGroup.TAB);}
    public static final class Set {
        public final String name; public final WoodType type;
        public final Map<String,RegistryObject<Block>> blocks=new LinkedHashMap<>();
        Set(String n){name=n;type=WoodType.register(WoodType.create("slavicmyths:"+n));
            add("log",()->new TimberLog(this,false,false)); add("wood",()->new TimberLog(this,true,false));
            add("stripped_log",()->new TimberLog(this,false,true)); add("stripped_wood",()->new TimberLog(this,true,true));
            add("planks",()->new Block(AbstractBlock.Properties.copy(Blocks.OAK_PLANKS)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
            add("leaves",()->n.equals("rowan")?new RowanLeaves():new LeavesBlock(AbstractBlock.Properties.copy(Blocks.OAK_LEAVES)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 60;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 30;}});
            add("sapling",()->new SaplingBlock(new WoodlandGrower(n),AbstractBlock.Properties.copy(Blocks.OAK_SAPLING)));
            add("stairs",()->new StairsBlock(()->get("planks").defaultBlockState(),AbstractBlock.Properties.copy(Blocks.OAK_STAIRS)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
            add("slab",()->new SlabBlock(AbstractBlock.Properties.copy(Blocks.OAK_SLAB)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
            add("fence",()->new FenceBlock(AbstractBlock.Properties.copy(Blocks.OAK_FENCE)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
            add("fence_gate",()->new FenceGateBlock(AbstractBlock.Properties.copy(Blocks.OAK_FENCE_GATE)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
            add("door",()->new DoorBlock(AbstractBlock.Properties.copy(Blocks.OAK_DOOR)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
            add("trapdoor",()->new TrapDoorBlock(AbstractBlock.Properties.copy(Blocks.OAK_TRAPDOOR)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
            add("pressure_plate",()->new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING,AbstractBlock.Properties.copy(Blocks.OAK_PRESSURE_PLATE)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
            add("button",()->new WoodButtonBlock(AbstractBlock.Properties.copy(Blocks.OAK_BUTTON)){@Override public int getFlammability(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.IBlockReader w,net.minecraft.util.math.BlockPos p,net.minecraft.util.Direction d){return 5;}});
            blocks.put("sign",ModBlocks.BLOCKS.register(n+"_sign",()->new StandingSignBlock(AbstractBlock.Properties.copy(Blocks.OAK_SIGN),type){@Override public TileEntity newBlockEntity(net.minecraft.world.IBlockReader w){return new WoodSignTile();}}));
            blocks.put("wall_sign",ModBlocks.BLOCKS.register(n+"_wall_sign",()->new WallSignBlock(AbstractBlock.Properties.copy(Blocks.OAK_WALL_SIGN).dropsLike(get("sign")),type){@Override public TileEntity newBlockEntity(net.minecraft.world.IBlockReader w){return new WoodSignTile();}}));
            ModItems.ITEMS.register(n+"_sign",()->new SignItem(props().stacksTo(16),get("sign"),get("wall_sign")));
        }
        private void add(String suffix,Supplier<Block> factory){RegistryObject<Block>b=ModBlocks.BLOCKS.register(name+"_"+suffix,factory);blocks.put(suffix,b);ModItems.ITEMS.register(name+"_"+suffix,()->new BlockItem(b.get(),props()));}
        public Block get(String suffix){return blocks.get(suffix).get();}
    }
    public static final class WoodSignTile extends SignTileEntity {
        @Override public TileEntityType<?> getType(){return SIGN_TILE.get();}
    }
    public static void setup(){WoodlandWorldgen.setup();WoodlandFuel.setup();}
}
