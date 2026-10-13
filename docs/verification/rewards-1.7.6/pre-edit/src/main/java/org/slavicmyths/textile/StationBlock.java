package org.slavicmyths.textile;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public abstract class StationBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty LOADED=BooleanProperty.create("loaded"),ACTIVE=BooleanProperty.create("active");
    public static final IntegerProperty WORK_STEP=IntegerProperty.create("work_step",0,2);
    public final int kind;
    protected StationBlock(Properties p,int kind){super(p);this.kind=kind;BlockState s=stateDefinition.any().setValue(FACING,Direction.NORTH);if(s.hasProperty(LOADED))s=s.setValue(LOADED,false);if(s.hasProperty(WORK_STEP))s=s.setValue(WORK_STEP,0);if(s.hasProperty(ACTIVE))s=s.setValue(ACTIVE,false);registerDefaultState(s);}
    public static final class Breaker extends StationBlock {
        private static final MapCodec<Breaker> CODEC=simpleCodec(Breaker::new);
        public Breaker(){this(Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2).noOcclusion());}private Breaker(Properties p){super(p,0);}
        @Override protected MapCodec<Breaker> codec(){return CODEC;}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,LOADED,WORK_STEP);}
    }
    public static final class Wheel extends StationBlock {
        private static final MapCodec<Wheel> CODEC=simpleCodec(Wheel::new);
        public Wheel(){this(Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2).noOcclusion());}private Wheel(Properties p){super(p,1);}
        @Override protected MapCodec<Wheel> codec(){return CODEC;}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,LOADED,ACTIVE);}
    }
    public static final class Loom extends StationBlock {
        private static final MapCodec<Loom> CODEC=simpleCodec(Loom::new);
        public Loom(){this(Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(2).noOcclusion());}private Loom(Properties p){super(p,2);}
        @Override protected MapCodec<Loom> codec(){return CODEC;}
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,ACTIVE);}
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new TextileStation(p,s);}
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level l,BlockState s,BlockEntityType<T> t){return !l.isClientSide&&kind>0&&t==Textiles.STATION.get()?(world,pos,state,tile)->((TextileStation)tile).tick():null;}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return kind==0?box(1,0,2,15,13,14):box(1,0,2,15,kind==1?22:25,14);}
    @Override public ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
        if(!(l.getBlockEntity(p) instanceof TextileStation station)||!station.accepts(stack))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!l.isClientSide&&l.mayInteract(player,p)&&player.mayUseItemAt(p,hit.getDirection(),stack))stack.consume(station.insert(stack,player.isShiftKeyDown()?stack.getCount():1),player);
        return ItemInteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override public InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        if(!player.getMainHandItem().isEmpty())return InteractionResult.PASS;
        if(!l.isClientSide&&l.mayInteract(player,p)&&l.getBlockEntity(p) instanceof TextileStation station)station.workOrExtract(player);
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override public void onRemove(BlockState s,Level l,BlockPos p,BlockState next,boolean moving){if(!s.is(next.getBlock())&&!l.isClientSide&&l.getBlockEntity(p) instanceof TextileStation station)station.dropContents();super.onRemove(s,l,p,next,moving);}
}
