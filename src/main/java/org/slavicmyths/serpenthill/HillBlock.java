package org.slavicmyths.serpenthill;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;
public final class HillBlock extends Block {
 public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
 public static final BooleanProperty HANGING=BlockStateProperties.HANGING;
 private final String kind;
 private static BlockBehaviour.Properties properties(String kind,int light){var p=BlockBehaviour.Properties.ofFullCopy(Blocks.POLISHED_BLACKSTONE_BRICKS).strength(3.5f,12).lightLevel(s->light).sound(kind.contains("lamp")||kind.contains("brazier")?SoundType.METAL:kind.equals("serpent_torch")?SoundType.WOOD:SoundType.STONE);return kind.contains("masonry")||kind.equals("serpent_bas_relief")?p:p.noOcclusion();}
 public HillBlock(String kind,int light){super(properties(kind,light));this.kind=kind;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(HANGING,false));}
 @Override protected MapCodec<? extends Block> codec(){return MapCodec.unit(this);}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,HANGING);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,kind.equals("serpent_torch")&&c.getClickedFace().getAxis().isHorizontal()?c.getClickedFace():c.getHorizontalDirection().getOpposite()).setValue(HANGING,kind.equals("serpent_torch")?c.getClickedFace().getAxis().isHorizontal():kind.equals("ancient_hanging_lamp")&&c.getClickedFace()==Direction.DOWN);}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(FACING)));}
 @Override protected boolean canSurvive(BlockState s,LevelReader w,BlockPos p){if(!kind.equals("serpent_torch"))return true;var support=s.getValue(HANGING)?p.relative(s.getValue(FACING).getOpposite()):p.below();return w.getBlockState(support).isFaceSturdy(w,support,s.getValue(HANGING)?s.getValue(FACING):Direction.UP);}
 @Override protected BlockState updateShape(BlockState s,Direction d,BlockState neighbour,LevelAccessor w,BlockPos p,BlockPos other){return kind.equals("serpent_torch")&&!canSurvive(s,w,p)?Blocks.AIR.defaultBlockState():super.updateShape(s,d,neighbour,w,p,other);}
 @Override protected VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext c){if(kind.contains("ritual_slab"))return Block.box(0,0,0,16,8,16);if(kind.contains("brazier"))return Block.box(1,0,1,15,12,15);if(kind.equals("serpent_torch"))return Block.box(5,0,5,11,14,11);if(kind.contains("lamp"))return Block.box(3,0,3,13,16,13);if(kind.equals("mor_crystal"))return Block.box(2,0,2,14,16,14);return Shapes.block();}
 @Override public void animateTick(BlockState s,Level w,BlockPos p,net.minecraft.util.RandomSource r){boolean mor=kind.contains("defiled")||kind.equals("mor_crystal");if((kind.contains("brazier")||kind.equals("serpent_torch")||kind.equals("mor_crystal"))&&r.nextInt(8)==0)w.addParticle(mor?net.minecraft.core.particles.DustParticleOptions.REDSTONE:net.minecraft.core.particles.ParticleTypes.FLAME,p.getX()+.5,p.getY()+.8,p.getZ()+.5,0,.01,0);if((kind.contains("brazier")||kind.equals("mor_crystal"))&&r.nextInt(800)==0)w.playLocalSound(p,HillContent.SOUNDS.get(kind.equals("mor_crystal")?"crystal":"brazier").get(),net.minecraft.sounds.SoundSource.BLOCKS,.18f,1,false);}
}
