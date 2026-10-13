package org.slavicmyths.kurgan;
import net.minecraft.world.item.context.BlockPlaceContext;

import java.util.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.world.level.BlockGetter;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

public final class KurganBlocks {
    public static final Map<String,DeferredHolder<Block, Block>> BLOCKS=new LinkedHashMap<>();
    static {
        for(String family:new String[]{"pale_blue","bog_green"})for(String type:new String[]{"stone","cobblestone","cracked","mossy"}){
            String id=type.equals("stone")||type.equals("cobblestone")?family+"_kurgan_"+type:type+"_"+family+"_kurgan_cobblestone";
            add(id,()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(2.5F,8F)));
        }
        add("sealed_kurgan_masonry",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(-1F,3600000F).pushReaction(PushReaction.BLOCK)){

        });
        // Keep all eight existing public IDs; add genuine vanilla construction shapes alongside them.
        for(String family:new String[]{"pale_blue","bog_green"})for(String type:new String[]{"stone","cobblestone","cracked","mossy"}){
            String base=type.equals("stone")||type.equals("cobblestone")?family+"_kurgan_"+type:type+"_"+family+"_kurgan_cobblestone";
            add(base+"_slab",()->new SlabBlock(BlockBehaviour.Properties.ofFullCopy(get(base))));
            add(base+"_stairs",()->new StairBlock(get(base).defaultBlockState(),BlockBehaviour.Properties.ofFullCopy(get(base))));
            add(base+"_wall",()->new WallBlock(BlockBehaviour.Properties.ofFullCopy(get(base))));
        }
        add("carved_burial_stone",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICKS)));
        add("carved_burial_stone_slab",()->new SlabBlock(BlockBehaviour.Properties.ofFullCopy(get("carved_burial_stone"))));
        for(String id:new String[]{"burial_stone_slab","kurgan_column_fragment","kurgan_ritual_bowl","kurgan_pottery","kurgan_stone_altar","kurgan_wall_torch_holder","kurgan_hanging_brazier","kurgan_rubble"})add(id,()->new Decor(id));
    }
    private static void add(String id,java.util.function.Supplier<Block> factory){DeferredHolder<Block, Block> b=ModBlocks.BLOCKS.register(id,factory);BLOCKS.put(id,b);ModItems.ITEMS.register(id,()->new BlockItem(b.get(),new Item.Properties()));}
    public static void init(){}
    public static Block get(String id){return BLOCKS.get(id).get();}
    public static BlockState stone(int palette,int variant){String f=palette==0?"pale_blue":"bog_green";return get(variant==0?f+"_kurgan_stone":variant==1?f+"_kurgan_cobblestone":(variant==2?"cracked_":"mossy_")+f+"_kurgan_cobblestone").defaultBlockState();}
    public static final class Decor extends HorizontalDirectionalBlock {
        private static final com.mojang.serialization.MapCodec<Decor> CODEC=com.mojang.serialization.codecs.RecordCodecBuilder.mapCodec(instance->instance.group(
            com.mojang.serialization.Codec.STRING.fieldOf("decor_id").forGetter(block->block.id),propertiesCodec()).apply(instance,Decor::new));
        @Override protected com.mojang.serialization.MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
        public final String id;private final VoxelShape shape;
        Decor(String id){this(id,BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).strength(1.5F,5F).noOcclusion().lightLevel(s->id.contains("torch")?12:id.contains("brazier")?13:0));}
        Decor(String id,BlockBehaviour.Properties properties){super(properties);this.id=id;
            shape=id.contains("rubble")?box(1,0,1,15,5,15):id.contains("slab")?box(0,0,0,16,5,16):id.contains("torch")?box(4,2,8,12,15,16):id.contains("brazier")?box(2,0,2,14,16,14):box(2,0,2,14,id.contains("bowl")?9:15,14);
            registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState>b){b.add(FACING);}
        @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
        @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){if(!id.contains("torch"))return shape;switch(s.getValue(FACING)){case SOUTH:return box(4,2,0,12,15,8);case EAST:return box(0,2,4,8,15,12);case WEST:return box(8,2,4,16,15,12);default:return shape;}}
    }
}
