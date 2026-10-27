package org.slavicmyths.blackcinder;

import java.util.*;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** Non-damaging geology and one fireproof Nether timber family. No sapling. */
public final class CinderBlocks {
 public static final Map<String,DeferredHolder<Block,Block>> ALL=new LinkedHashMap<>();
 public static final WoodType WOOD=WoodType.register(new WoodType("slavicmyths:blackwood",BlockSetType.CRIMSON));
 static {
  add("black_cinder_soil",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_SOIL)));
  add("ash_crust",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.SOUL_SOIL)));
  add("smoking_cinder",()->new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE)) {
   @Override public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random){
    if(random.nextInt(12)==0 && level.isEmptyBlock(pos.above()))level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,pos.getX()+.5,pos.getY()+1.05,pos.getZ()+.5,0,.015,0);
    if(random.nextInt(240)==0)level.playLocalSound(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,net.minecraft.sounds.SoundEvents.FIRE_AMBIENT,net.minecraft.sounds.SoundSource.BLOCKS,.12F,.8F,false);
   }
  });
  for(String id:List.of("charred_stone","cracked_charred_stone")) {
   add(id,()->new Block(stone()));
   add(id+"_stairs",()->new StairBlock(get(id).defaultBlockState(),stone()));
   add(id+"_slab",()->new SlabBlock(stone()));
   add(id+"_wall",()->new WallBlock(stone()));
  }
  for(String id:List.of("blackwood_log","charred_blackwood_log","blackwood_wood","charred_blackwood_wood"))add(id,()->new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_STEM)));
  add("blackwood_planks",()->new Block(wood()));
  add("blackwood_stairs",()->new StairBlock(get("blackwood_planks").defaultBlockState(),wood()));
  add("blackwood_slab",()->new SlabBlock(wood()));
  add("blackwood_fence",()->new FenceBlock(wood()));
  add("blackwood_fence_gate",()->new FenceGateBlock(WOOD,wood()));
  add("blackwood_door",()->new DoorBlock(BlockSetType.CRIMSON,BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_DOOR)));
  add("blackwood_trapdoor",()->new TrapDoorBlock(BlockSetType.CRIMSON,BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_TRAPDOOR)));
  add("blackwood_button",()->new ButtonBlock(BlockSetType.CRIMSON,30,BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_BUTTON)));
  add("blackwood_pressure_plate",()->new PressurePlateBlock(BlockSetType.CRIMSON,BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PRESSURE_PLATE)));
  add("charred_blackwood_leaves",()->new LeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.NETHER_WART_BLOCK).strength(.2F).noOcclusion().sound(SoundType.AZALEA_LEAVES)));
  add("zharenitsa_head",()->new Head());
 }
 public static final class Head extends HorizontalDirectionalBlock {
  private static final Map<net.minecraft.core.Direction,net.minecraft.world.phys.shapes.VoxelShape> SHAPES=shapes();
  private static Map<net.minecraft.core.Direction,net.minecraft.world.phys.shapes.VoxelShape> shapes(){
   var map=new EnumMap<net.minecraft.core.Direction,net.minecraft.world.phys.shapes.VoxelShape>(net.minecraft.core.Direction.class);
   var shape=Block.box(3,0,3,13,10,13);int[] xs={2,5,9,12},heights={14,16,15,13};
   for(int i=0;i<4;i++){shape=net.minecraft.world.phys.shapes.Shapes.or(shape,Block.box(xs[i],8,2,xs[i]+2,heights[i],4),Block.box(xs[i],8,12,xs[i]+2,heights[i],14));}
   for(var direction:new net.minecraft.core.Direction[]{net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.WEST}){
    map.put(direction,shape);var next=net.minecraft.world.phys.shapes.Shapes.empty();for(var box:shape.toAabbs())next=net.minecraft.world.phys.shapes.Shapes.or(next,Block.box((1-box.maxZ)*16,box.minY*16,box.minX*16,(1-box.minZ)*16,box.maxY*16,box.maxX*16));shape=next;
   }return map;
  }
  public Head(){super(BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE).noOcclusion());registerDefaultState(stateDefinition.any().setValue(FACING,net.minecraft.core.Direction.NORTH));}
  @Override protected com.mojang.serialization.MapCodec<? extends HorizontalDirectionalBlock> codec(){return simpleCodec(p->new Head());}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING);}
  @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
  public static net.minecraft.world.phys.shapes.VoxelShape shape(net.minecraft.core.Direction facing){return SHAPES.get(facing);}
  @Override protected net.minecraft.world.phys.shapes.VoxelShape getShape(BlockState state,net.minecraft.world.level.BlockGetter level,BlockPos pos,net.minecraft.world.phys.shapes.CollisionContext context){return shape(state.getValue(FACING));}
 }
 private static BlockBehaviour.Properties stone(){return BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE);}
 private static BlockBehaviour.Properties wood(){return BlockBehaviour.Properties.ofFullCopy(Blocks.CRIMSON_PLANKS);}
 private static void add(String id,Supplier<Block> factory){var block=ModBlocks.BLOCKS.register(id,factory);ALL.put(id,block);ModItems.ITEMS.register(id,()->new BlockItem(block.get(),new Item.Properties()));}
 public static Block get(String id){return ALL.get(id).get();}
 public static void init(){}
 private CinderBlocks(){}
}
