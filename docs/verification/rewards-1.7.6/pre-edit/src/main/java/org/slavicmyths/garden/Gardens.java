package org.slavicmyths.garden;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

public final class Gardens {
    public static final String[] BERRIES={"raspberry","blueberry","blackcurrant","lingonberry","cranberry"};
    public static final Map<String,DeferredHolder<Block,Block>> BLOCKS=new LinkedHashMap<>();
    public static final Map<String,DeferredHolder<Item,Item>> ITEMS=new LinkedHashMap<>();
    public static final TagKey<Block> APPLE_LOGS=TagKey.create(Registries.BLOCK,ResourceLocation.fromNamespaceAndPath("slavicmyths","apple_logs"));
    public static final WoodType APPLE_TYPE=WoodType.register(new WoodType("slavicmyths:apple",BlockSetType.OAK));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<AppleSign>> SIGN;
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<AppleHangingSign>> HANGING_SIGN;
    static {
        for(int n=0;n<5;n++){
            int kind=n;String id=BERRIES[n];
            if(n<2)BLOCKS.put(id+"_bush",n==0?ModBlocks.RASPBERRY_BUSH:ModBlocks.BLUEBERRY_BUSH);
            else BLOCKS.put(id+"_bush",ModBlocks.BLOCKS.register(id+"_bush",()->new PerennialBush(kind)));
            if(n<2)ITEMS.put(id,n==0?ModItems.RASPBERRY:ModItems.BLUEBERRY);
            else ITEMS.put(id,ModItems.ITEMS.register(id,()->new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(kind<3?2:1).saturationModifier(kind==4?.15F:.2F).build()))));
            ITEMS.put(id+"_sapling",ModItems.ITEMS.register(id+"_sapling",()->new ItemNameBlockItem(block(id+"_bush"),new Item.Properties())));
        }
        add("apple_log",()->new AppleLog("stripped_apple_log",p(Blocks.OAK_LOG).randomTicks()));
        add("stripped_apple_log",()->new AppleLog("",p(Blocks.STRIPPED_OAK_LOG)));
        add("apple_wood",()->new AppleLog("stripped_apple_wood",p(Blocks.OAK_WOOD)));
        add("stripped_apple_wood",()->new AppleLog("",p(Blocks.STRIPPED_OAK_WOOD)));
        add("apple_planks",()->new Block(p(Blocks.OAK_PLANKS)));
        add("apple_stairs",()->new StairBlock(block("apple_planks").defaultBlockState(),p(Blocks.OAK_STAIRS)));
        add("apple_slab",()->new SlabBlock(p(Blocks.OAK_SLAB)));
        add("apple_fence",()->new FenceBlock(p(Blocks.OAK_FENCE)));
        add("apple_fence_gate",()->new FenceGateBlock(APPLE_TYPE,p(Blocks.OAK_FENCE_GATE)));
        add("apple_door",()->new DoorBlock(BlockSetType.OAK,p(Blocks.OAK_DOOR)));
        add("apple_trapdoor",()->new TrapDoorBlock(BlockSetType.OAK,p(Blocks.OAK_TRAPDOOR)));
        add("apple_pressure_plate",()->new PressurePlateBlock(BlockSetType.OAK,p(Blocks.OAK_PRESSURE_PLATE)));
        add("apple_button",()->new ButtonBlock(BlockSetType.OAK,30,p(Blocks.OAK_BUTTON)));
        BLOCKS.put("apple_sign",ModBlocks.BLOCKS.register("apple_sign",()->new StandingSignBlock(APPLE_TYPE,p(Blocks.OAK_SIGN)){@Override public BlockEntity newBlockEntity(BlockPos pos,BlockState s){return new AppleSign(pos,s);}}));
        BLOCKS.put("apple_wall_sign",ModBlocks.BLOCKS.register("apple_wall_sign",()->new WallSignBlock(APPLE_TYPE,p(Blocks.OAK_WALL_SIGN).dropsLike(block("apple_sign"))){@Override public BlockEntity newBlockEntity(BlockPos pos,BlockState s){return new AppleSign(pos,s);}}));
        BLOCKS.put("apple_hanging_sign",ModBlocks.BLOCKS.register("apple_hanging_sign",()->new CeilingHangingSignBlock(APPLE_TYPE,p(Blocks.OAK_HANGING_SIGN)){@Override public BlockEntity newBlockEntity(BlockPos pos,BlockState s){return new AppleHangingSign(pos,s);}}));
        BLOCKS.put("apple_wall_hanging_sign",ModBlocks.BLOCKS.register("apple_wall_hanging_sign",()->new WallHangingSignBlock(APPLE_TYPE,p(Blocks.OAK_WALL_HANGING_SIGN).dropsLike(block("apple_hanging_sign"))){@Override public BlockEntity newBlockEntity(BlockPos pos,BlockState s){return new AppleHangingSign(pos,s);}}));
        ITEMS.put("apple_sign",ModItems.ITEMS.register("apple_sign",()->new SignItem(new Item.Properties().stacksTo(16),block("apple_sign"),block("apple_wall_sign"))));
        ITEMS.put("apple_hanging_sign",ModItems.ITEMS.register("apple_hanging_sign",()->new HangingSignItem(block("apple_hanging_sign"),block("apple_wall_hanging_sign"),new Item.Properties().stacksTo(16))));
        add("apple_leaves",()->new AppleLeaves(p(Blocks.OAK_LEAVES)));
        add("apple_sapling",()->new SaplingBlock(org.slavicmyths.wood.WoodlandGrower.create("apple"),p(Blocks.OAK_SAPLING)));
        SIGN=ModTiles.TILES.register("apple_sign",()->BlockEntityType.Builder.of(AppleSign::new,block("apple_sign"),block("apple_wall_sign")).build(null));
        HANGING_SIGN=ModTiles.TILES.register("apple_hanging_sign",()->BlockEntityType.Builder.of(AppleHangingSign::new,block("apple_hanging_sign"),block("apple_wall_hanging_sign")).build(null));
        ModFeatures.FEATURES.register("apple_tree",AppleTreeFeature::new);
        for(int n=0;n<5;n++){int kind=n;ModFeatures.FEATURES.register(BERRIES[n]+"_garden_patch",()->new GardenPatchFeature(kind));}
    }
    private static BlockBehaviour.Properties p(Block b){return BlockBehaviour.Properties.ofFullCopy(b);}
    private static void add(String id,Supplier<Block> factory){var holder=ModBlocks.BLOCKS.register(id,factory);BLOCKS.put(id,holder);ITEMS.put(id,ModItems.ITEMS.register(id,()->new BlockItem(holder.get(),new Item.Properties())));}
    public static Block block(String id){return BLOCKS.get(id).get();}
    public static Item berry(int kind){return ITEMS.get(BERRIES[kind]).get();}
    public static void init(){}
    public static final class AppleSign extends SignBlockEntity {public AppleSign(BlockPos pos,BlockState state){super(SIGN.get(),pos,state);}@Override public BlockEntityType<?> getType(){return SIGN.get();}}
    public static final class AppleHangingSign extends HangingSignBlockEntity {public AppleHangingSign(BlockPos pos,BlockState state){super(pos,state);}@Override public BlockEntityType<?> getType(){return HANGING_SIGN.get();}}
    private Gardens(){}
}
