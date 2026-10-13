package org.slavicmyths.industry;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.util.RandomSource;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.*;
public final class MachineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT=BlockStateProperties.LIT;
    private final boolean refining;
    public MachineBlock(boolean refining,Properties p){super(p);this.refining=refining;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(LIT,false));}
    protected MapCodec<? extends BaseEntityBlock> codec(){return simpleCodec(p->new MachineBlock(refining,p));}
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,LIT);}
    public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    protected BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    protected BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
    protected RenderShape getRenderShape(BlockState s){return RenderShape.MODEL;}
    protected VoxelShape getShape(BlockState s,BlockGetter world,BlockPos pos,CollisionContext context){return refining?Block.box(1,0,1,15,15,15):Shapes.block();}
    public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new MachineTile(p,s,refining);}
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return l.isClientSide?null:createTickerHelper(t,refining?Industry.HEARTH_TILE.get():Industry.BLOOMERY_TILE.get(),MachineTile::tick);}
    protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){if(!l.isClientSide&&l.getBlockEntity(p) instanceof MachineTile tile)player.openMenu(tile,p);return InteractionResult.sidedSuccess(l.isClientSide);}
    protected void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())){if(l.getBlockEntity(p) instanceof MachineTile tile){Containers.dropContents(l,p,tile);l.updateNeighbourForOutputSignal(p,this);}super.onRemove(s,l,p,next,moving);}}
    protected boolean hasAnalogOutputSignal(BlockState s){return true;}
    protected int getAnalogOutputSignal(BlockState s,Level l,BlockPos p){return l.getBlockEntity(p) instanceof MachineTile tile?net.minecraft.world.inventory.AbstractContainerMenu.getRedstoneSignalFromContainer(tile):0;}
    public void animateTick(BlockState s,Level l,BlockPos p,RandomSource r){if(!s.getValue(LIT))return;if(r.nextDouble()<.08)l.playLocalSound(p.getX()+.5,p.getY()+.5,p.getZ()+.5,SoundEvents.FURNACE_FIRE_CRACKLE,SoundSource.BLOCKS,.35F,1,false);var d=s.getValue(FACING);l.addParticle(ParticleTypes.SMOKE,p.getX()+.5+d.getStepX()*.48,p.getY()+.5,p.getZ()+.5+d.getStepZ()*.48,0,.01,0);}
}
