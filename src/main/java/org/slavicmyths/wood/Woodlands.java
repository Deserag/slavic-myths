package org.slavicmyths.wood;

import java.util.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import java.util.function.Supplier;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** Ordered wood families; existing registries and creative tab are retained. */
public final class Woodlands {
    public static final Map<String,Set> SETS = new LinkedHashMap<>();
    public static final DeferredHolder<Item, Item> BERRIES;
    public static final DeferredHolder<Item, Item> DATES;
    public static final DeferredHolder<Item, Item> MARULA_FRUIT;
    public static final DeferredHolder<Block, Block> PALM_HANGING;
    public static final DeferredHolder<Block, Block> HANGING;
    public static final DeferredHolder<Block, Block> FROST_HANGING;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodSignTile>> SIGN_TILE;
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WoodHangingSignTile>> HANGING_SIGN_TILE;
    static {
        for(String name:new String[]{"linden","rowan","willow","pine","ice_pine","frost_willow","date_palm","tamarisk","baobab","marula"}) SETS.put(name,new Set(name));
        BERRIES=ModItems.ITEMS.register("rowan_berries",()->new Item(props().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(1).saturationModifier(.2F).build())));
        DATES=ModItems.ITEMS.register("date",()->new Item(props().food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(3).saturationModifier(.3F).build())));
        MARULA_FRUIT=ModItems.ITEMS.register("marula_fruit",()->new Item(props()));
        PALM_HANGING=ModBlocks.BLOCKS.register("hanging_date_palm_leaves",()->new HangingLeaves("date_palm"));
        ModItems.ITEMS.register("hanging_date_palm_leaves",()->new BlockItem(PALM_HANGING.get(),props()));
        HANGING=ModBlocks.BLOCKS.register("hanging_willow_leaves",()->new HangingLeaves());
        ModItems.ITEMS.register("hanging_willow_leaves",()->new BlockItem(HANGING.get(),props()));
        FROST_HANGING=ModBlocks.BLOCKS.register("hanging_frost_willow_leaves",()->new HangingLeaves("frost_willow"));
        ModItems.ITEMS.register("hanging_frost_willow_leaves",()->new BlockItem(FROST_HANGING.get(),props()));
        SIGN_TILE=ModTiles.TILES.register("wood_sign",()->BlockEntityType.Builder.of(WoodSignTile::new,SETS.values().stream().flatMap(s->java.util.stream.Stream.of(s.get("sign"),s.get("wall_sign"))).toArray(Block[]::new)).build(null));
        HANGING_SIGN_TILE=ModTiles.TILES.register("winter_hanging_sign",()->BlockEntityType.Builder.of(WoodHangingSignTile::new,SETS.values().stream().filter(s->s.blocks.containsKey("hanging_sign")).flatMap(s->java.util.stream.Stream.of(s.get("hanging_sign"),s.get("wall_hanging_sign"))).toArray(Block[]::new)).build(null));
    }
    public static void init() { WoodlandWorldgen.init(); }
    public static Item.Properties props(){return new Item.Properties();}
    public static final class Set {
        public final String name; public final WoodType type;
        public final Map<String,DeferredHolder<Block, Block>> blocks=new LinkedHashMap<>();
        Set(String n){name=n;type=WoodType.register(new WoodType("slavicmyths:"+n,BlockSetType.OAK));
            add("log",()->new TimberLog(this,false,false)); add("wood",()->new TimberLog(this,true,false));
            add("stripped_log",()->new TimberLog(this,false,true)); add("stripped_wood",()->new TimberLog(this,true,true));
            add("planks",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
            add("leaves",()->n.equals("rowan")?new RowanLeaves():n.equals("date_palm")?new RowanLeaves(()->DATES.get()):n.equals("marula")?new RowanLeaves(()->MARULA_FRUIT.get()):new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 60;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 30;}});
            add("sapling",()->new SaplingBlock(WoodlandGrower.create(n),BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)){@Override protected boolean mayPlaceOn(BlockState state,net.minecraft.world.level.BlockGetter level,net.minecraft.core.BlockPos pos){return super.mayPlaceOn(state,level,pos)||((n.equals("date_palm")||n.equals("tamarisk"))&&state.is(Blocks.SAND));}});
            add("stairs",()->new StairBlock(get("planks").defaultBlockState(),BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
            add("slab",()->new SlabBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
            add("fence",()->new FenceBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
            add("fence_gate",()->new FenceGateBlock(WoodType.OAK,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE_GATE)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
            add("door",()->new DoorBlock(BlockSetType.OAK,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_DOOR)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
            add("trapdoor",()->new TrapDoorBlock(BlockSetType.OAK,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_TRAPDOOR)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
            add("pressure_plate",()->new PressurePlateBlock(BlockSetType.OAK,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PRESSURE_PLATE)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
            add("button",()->new ButtonBlock(BlockSetType.OAK,30,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_BUTTON)){@Override public int getFlammability(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 20;} @Override public int getFireSpreadSpeed(BlockState s,net.minecraft.world.level.BlockGetter w,net.minecraft.core.BlockPos p,net.minecraft.core.Direction d){return 5;}});
            blocks.put("sign",ModBlocks.BLOCKS.register(n+"_sign",()->new StandingSignBlock(type,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SIGN)){@Override public BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,BlockState state){return new WoodSignTile(pos,state);}}));
            blocks.put("wall_sign",ModBlocks.BLOCKS.register(n+"_wall_sign",()->new WallSignBlock(type,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_SIGN).dropsLike(get("sign"))){@Override public BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,BlockState state){return new WoodSignTile(pos,state);}}));
            ModItems.ITEMS.register(n+"_sign",()->new SignItem(props().stacksTo(16),get("sign"),get("wall_sign")));
            if(n.equals("ice_pine")||n.equals("frost_willow")||n.equals("date_palm")||n.equals("tamarisk")||n.equals("baobab")||n.equals("marula")){
                blocks.put("hanging_sign",ModBlocks.BLOCKS.register(n+"_hanging_sign",()->new CeilingHangingSignBlock(type,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_HANGING_SIGN)){@Override public BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,BlockState state){return new WoodHangingSignTile(pos,state);}}));
                blocks.put("wall_hanging_sign",ModBlocks.BLOCKS.register(n+"_wall_hanging_sign",()->new WallHangingSignBlock(type,BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WALL_HANGING_SIGN).dropsLike(get("hanging_sign"))){@Override public BlockEntity newBlockEntity(net.minecraft.core.BlockPos pos,BlockState state){return new WoodHangingSignTile(pos,state);}}));
                ModItems.ITEMS.register(n+"_hanging_sign",()->new HangingSignItem(get("hanging_sign"),get("wall_hanging_sign"),props().stacksTo(16)));
            }
        }
        private void add(String suffix,Supplier<Block> factory){DeferredHolder<Block, Block>b=ModBlocks.BLOCKS.register(name+"_"+suffix,factory);blocks.put(suffix,b);ModItems.ITEMS.register(name+"_"+suffix,()->new BlockItem(b.get(),props()));}
        public Block get(String suffix){return blocks.get(suffix).get();}
    }
    public static final class WoodSignTile extends SignBlockEntity {
        public WoodSignTile(net.minecraft.core.BlockPos pos,BlockState state){super(SIGN_TILE.get(),pos,state);}
        @Override public BlockEntityType<?> getType(){return SIGN_TILE.get();}
    }
    public static final class WoodHangingSignTile extends HangingSignBlockEntity {
        public WoodHangingSignTile(net.minecraft.core.BlockPos pos,BlockState state){super(pos,state);}
        @Override public BlockEntityType<?> getType(){return HANGING_SIGN_TILE.get();}
    }
    public static void setup(){WoodlandFuel.setup();}
}
