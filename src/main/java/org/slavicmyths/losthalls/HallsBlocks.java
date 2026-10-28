package org.slavicmyths.losthalls;

import java.util.*;
import java.util.function.Supplier;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;

/** Natural stone and ordinary construction forms; registry IDs are save identities. */
public final class HallsBlocks {
 public static final Map<String,DeferredHolder<Block,Block>> ALL=new LinkedHashMap<>();
 static {
  for(String name:List.of("lost_hall_stone","cracked_lost_hall_stone","polished_lost_hall_stone","luminous_vein_stone"))
   add(name,()->new Block(stone().lightLevel(s->name.equals("luminous_vein_stone")?10:0)));
  for(String base:List.of("lost_hall_stone","polished_lost_hall_stone")){
   add(base+"_stairs",()->new StairBlock(get(base).defaultBlockState(),stone()));
   add(base+"_slab",()->new SlabBlock(stone()));
   add(base+"_wall",()->new WallBlock(stone()));
  }
  add("cave_coating",Coating::new);
  add("lost_halls_waymark",Waymark::new);
 }
 private static BlockBehaviour.Properties stone(){return BlockBehaviour.Properties.ofFullCopy(Blocks.BLACKSTONE).strength(2.5F,6).requiresCorrectToolForDrops();}
 private static void add(String id,Supplier<Block> factory){var b=ModBlocks.BLOCKS.register(id,factory);ALL.put(id,b);ModItems.ITEMS.register(id,()->new BlockItem(b.get(),new Item.Properties()));}
 public static Block get(String id){return ALL.get(id).get();}
 public static void init(){}
 public static final class Coating extends MultifaceBlock {
  public Coating(){super(BlockBehaviour.Properties.ofFullCopy(Blocks.GLOW_LICHEN).noCollission().lightLevel(s->0));}
  @Override protected MapCodec<Coating> codec(){return simpleCodec(p->new Coating());}
  @Override public MultifaceSpreader getSpreader(){return new MultifaceSpreader(this);}
 }
 public static final class Waymark extends HorizontalDirectionalBlock {
  public static final EnumProperty<AttachFace> FACE=BlockStateProperties.ATTACH_FACE;
  public Waymark(){super(stone().noOcclusion().lightLevel(s->10));registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(FACE,AttachFace.FLOOR));}
  @Override protected MapCodec<Waymark> codec(){return simpleCodec(p->new Waymark());}
  @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,FACE);}
  private Direction support(BlockState s){return s.getValue(FACE)==AttachFace.FLOOR?Direction.DOWN:s.getValue(FACE)==AttachFace.CEILING?Direction.UP:s.getValue(FACING).getOpposite();}
  @Override public BlockState getStateForPlacement(BlockPlaceContext c){Direction face=c.getClickedFace();BlockState s=defaultBlockState().setValue(FACING,face.getAxis().isHorizontal()?face:c.getHorizontalDirection().getOpposite()).setValue(FACE,face==Direction.UP?AttachFace.FLOOR:face==Direction.DOWN?AttachFace.CEILING:AttachFace.WALL);return s.canSurvive(c.getLevel(),c.getClickedPos())?s:null;}
  @Override protected boolean canSurvive(BlockState s,LevelReader w,BlockPos p){Direction d=support(s);return w.getBlockState(p.relative(d)).isFaceSturdy(w,p.relative(d),d.getOpposite());}
  @Override protected BlockState updateShape(BlockState s,Direction d,BlockState n,LevelAccessor w,BlockPos p,BlockPos q){return !s.canSurvive(w,p)?Blocks.AIR.defaultBlockState():super.updateShape(s,d,n,w,p,q);}
  @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
  @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
  @Override protected VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){if(s.getValue(FACE)==AttachFace.WALL)return switch(s.getValue(FACING)){case NORTH->Block.box(3,3,5,13,13,16);case SOUTH->Block.box(3,3,0,13,13,11);case EAST->Block.box(0,3,3,11,13,13);case WEST->Block.box(5,3,3,16,13,13);default->Shapes.empty();};return s.getValue(FACE)==AttachFace.CEILING?Block.box(3,4,3,13,16,13):Block.box(3,0,3,13,12,13);}
 }
 private HallsBlocks(){}
}
