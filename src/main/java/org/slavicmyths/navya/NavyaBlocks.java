package org.slavicmyths.navya;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** One Nether wood family and eight distinct geological materials. */
public final class NavyaBlocks {
    public static final Map<String,DeferredHolder<Block,Block>> ALL = new LinkedHashMap<>();
    public static final WoodType WOOD = WoodType.register(new WoodType("slavicmyths:navya",BlockSetType.CRIMSON));
    public static final BooleanProperty WEATHERED = BooleanProperty.create("weathered");
    static {
        add("navya_soil", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_SOIL)));
        add("mossy_navya_soil", () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_SOIL)));
        add("navya_stone", () -> new Block(stone()));
        add("cracked_navya_stone", () -> new Block(stone()));
        add("grove_bone_block", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BONE_BLOCK)));
        add("grove_bone_column", () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BONE_BLOCK)));
        add("grove_bone_spike", BoneSpike::new);
        add("rooted_navya_stone", () -> new Block(stone()));
        add("navya_log", () -> new Log("stripped_navya_log"));
        add("navya_wood", () -> new Log("stripped_navya_wood"));
        add("stripped_navya_log", () -> new Log(null));
        add("stripped_navya_wood", () -> new Log(null));
        add("navya_planks", () -> new Block(wood()));
        add("navya_stairs", () -> new StairBlock(get("navya_planks").defaultBlockState(),wood()));
        add("navya_slab", () -> new SlabBlock(wood()));
        add("navya_fence", () -> new FenceBlock(wood()));
        add("navya_fence_gate", () -> new FenceGateBlock(WOOD,wood()));
        add("navya_door", () -> new DoorBlock(BlockSetType.CRIMSON,BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_DOOR)));
        add("navya_trapdoor", () -> new TrapDoorBlock(BlockSetType.CRIMSON,BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_TRAPDOOR)));
        add("navya_button", () -> new ButtonBlock(BlockSetType.CRIMSON,30,BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_BUTTON)));
        add("navya_pressure_plate", () -> new PressurePlateBlock(BlockSetType.CRIMSON,BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PRESSURE_PLATE)));
        sign("navya_sign",false,false); sign("navya_wall_sign",true,false);
        sign("navya_hanging_sign",false,true); sign("navya_wall_hanging_sign",true,true);
        ModItems.ITEMS.register("navya_sign",()->new SignItem(new Item.Properties().stacksTo(16),get("navya_sign"),get("navya_wall_sign")));
        ModItems.ITEMS.register("navya_hanging_sign",()->new HangingSignItem(get("navya_hanging_sign"),get("navya_wall_hanging_sign"),new Item.Properties().stacksTo(16)));
        for(String id:List.of("navya_dry_branches","navya_roots","navya_hanging_roots","navya_shrub","ghost_fungus","navya_growth"))
            add(id,()->new Flora(id));
        add("navya_dead_leaves",()->new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_WART_BLOCK).strength(.2F).noOcclusion().sound(SoundType.AZALEA_LEAVES)));
        add("navya_sapling",()->new SaplingBlock(new TreeGrower("slavicmyths:navya",Optional.empty(),
                Optional.of(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.CONFIGURED_FEATURE,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","navya_tree"))),Optional.empty()),
                BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_FUNGUS)) {
            @Override protected boolean mayPlaceOn(BlockState state,BlockGetter level,BlockPos pos) { return soil(state); }
        });
    }
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<SignTile>> SIGN = ModTiles.TILES.register("navya_sign",
            ()->BlockEntityType.Builder.of(SignTile::new,get("navya_sign"),get("navya_wall_sign")).build(null));
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<HangingTile>> HANGING_SIGN = ModTiles.TILES.register("navya_hanging_sign",
            ()->BlockEntityType.Builder.of(HangingTile::new,get("navya_hanging_sign"),get("navya_wall_hanging_sign")).build(null));
    private static BlockBehaviour.Properties stone() { return BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE); }
    private static BlockBehaviour.Properties wood() { return BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS); }
    private static void add(String id,Supplier<Block> factory) {
        var block=ModBlocks.BLOCKS.register(id,factory); ALL.put(id,block);
        ModItems.ITEMS.register(id,()->new BlockItem(block.get(),new Item.Properties()));
    }
    private static void sign(String id,boolean wall,boolean hanging) {
        ALL.put(id,ModBlocks.BLOCKS.register(id,()-> {
            var properties=BlockBehaviour.Properties.ofFullCopy(hanging?Blocks.CRIMSON_HANGING_SIGN:Blocks.CRIMSON_SIGN);
            if(wall)properties.dropsLike(get(hanging?"navya_hanging_sign":"navya_sign"));
            if(hanging && wall) return new WallHangingSignBlock(WOOD,properties) {
                @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new HangingTile(pos,state); }
            };
            if(hanging) return new CeilingHangingSignBlock(WOOD,properties) {
                @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new HangingTile(pos,state); }
            };
            if(wall) return new WallSignBlock(WOOD,properties) {
                @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new SignTile(pos,state); }
            };
            return new StandingSignBlock(WOOD,properties) {
                @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state) { return new SignTile(pos,state); }
            };
        }));
    }
    public static Block get(String id) { return ALL.get(id).get(); }
    public static boolean soil(BlockState state) { return state.is(get("navya_soil")) || state.is(get("mossy_navya_soil")); }
    public static void init() { }
    public static final class SignTile extends SignBlockEntity {
        public SignTile(BlockPos pos,BlockState state) { super(SIGN.get(),pos,state); }
    }
    public static final class HangingTile extends HangingSignBlockEntity {
        public HangingTile(BlockPos pos,BlockState state) { super(pos,state); }
        @Override public BlockEntityType<?> getType() { return HANGING_SIGN.get(); }
    }
    public static final class Log extends RotatedPillarBlock {
        private final String stripped;
        Log(String stripped) {
            super(BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_STEM)); this.stripped=stripped;
            registerDefaultState(defaultBlockState().setValue(WEATHERED,false));
        }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) {
            super.createBlockStateDefinition(builder); builder.add(WEATHERED);
        }
        @Override public BlockState getToolModifiedState(BlockState state,net.minecraft.world.item.context.UseOnContext context,
                net.neoforged.neoforge.common.ItemAbility ability,boolean simulate) {
            if(stripped!=null && ability==net.neoforged.neoforge.common.ItemAbilities.AXE_STRIP && context.getItemInHand().canPerformAction(ability))
                return get(stripped).defaultBlockState().setValue(AXIS,state.getValue(AXIS));
            return super.getToolModifiedState(state,context,ability,simulate);
        }
    }
    public static final class BoneSpike extends Block {
        public static final EnumProperty<Direction> TIP=EnumProperty.create("tip_direction",Direction.class,Direction.UP,Direction.DOWN);
        private static final VoxelShape UP=Shapes.or(Block.box(0,0,0,16,3,16),Block.box(2,3,3,7,11,8),Block.box(3,11,4,6,16,7),Block.box(9,3,9,14,9,14));
        private static final VoxelShape DOWN=Shapes.or(Block.box(0,13,0,16,16,16),Block.box(2,5,8,7,13,13),Block.box(3,0,9,6,5,12),Block.box(9,7,2,14,13,7));
        BoneSpike() { super(BlockBehaviour.Properties.ofFullCopy(Blocks.BONE_BLOCK).noOcclusion()); registerDefaultState(defaultBlockState().setValue(TIP,Direction.UP)); }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder) { builder.add(TIP); }
        @Override protected VoxelShape getShape(BlockState state,BlockGetter world,BlockPos pos,CollisionContext context) { return state.getValue(TIP)==Direction.UP?UP:DOWN; }
        @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext context) {
            return defaultBlockState().setValue(TIP,context.getClickedFace()==Direction.DOWN?Direction.DOWN:Direction.UP);
        }
    }
    public static final class Flora extends BushBlock {
        public static final com.mojang.serialization.MapCodec<Flora> CODEC = com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(i->i.group(
                com.mojang.serialization.Codec.STRING.fieldOf("kind").forGetter(f->f.kind),propertiesCodec()).apply(i,Flora::new));
        private final boolean hanging;
        private final String kind;
        Flora(String id) {
            this(id,BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_ROOTS).noCollission().instabreak()
                    .lightLevel(state->id.equals("ghost_fungus")?5:id.equals("navya_growth")?4:0));
        }
        Flora(String id,BlockBehaviour.Properties properties) { super(properties); kind=id; hanging=id.equals("navya_hanging_roots"); }
        @Override protected com.mojang.serialization.MapCodec<? extends BushBlock> codec() { return CODEC; }
        @Override protected boolean mayPlaceOn(BlockState state,BlockGetter world,BlockPos pos) {
            return soil(state) || state.is(get("rooted_navya_stone")) || state.is(get("navya_stone")) || state.is(net.minecraft.tags.BlockTags.LOGS);
        }
        @Override public boolean canSurvive(BlockState state,LevelReader world,BlockPos pos) {
            return hanging?world.getBlockState(pos.above()).isFaceSturdy(world,pos.above(),Direction.DOWN):super.canSurvive(state,world,pos);
        }
    }
    private NavyaBlocks() { }
}
