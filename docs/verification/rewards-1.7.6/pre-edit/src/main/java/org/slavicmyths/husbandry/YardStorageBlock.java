package org.slavicmyths.husbandry;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public abstract class YardStorageBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final IntegerProperty FILL_LEVEL=IntegerProperty.create("fill_level",0,3), EGG_COUNT=IntegerProperty.create("egg_count",0,3);
    public final boolean nest;
    protected YardStorageBlock(Properties properties,boolean nest){super(properties);this.nest=nest;registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(countProperty(),0));}
    protected abstract IntegerProperty countProperty();
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(FACING,countProperty());}
    public static final class Feeder extends YardStorageBlock {
        private static final MapCodec<Feeder> CODEC=simpleCodec(Feeder::new);
        public Feeder(){this(Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(1.5F).noOcclusion());}
        private Feeder(Properties p){super(p,false);}
        @Override protected IntegerProperty countProperty(){return FILL_LEVEL;}
        @Override protected MapCodec<Feeder> codec(){return CODEC;}
    }
    public static final class Nest extends YardStorageBlock {
        private static final MapCodec<Nest> CODEC=simpleCodec(Nest::new);
        public Nest(){this(Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(1.0F).noOcclusion());}
        private Nest(Properties p){super(p,true);}
        @Override protected IntegerProperty countProperty(){return EGG_COUNT;}
        @Override protected MapCodec<Nest> codec(){return CODEC;}
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(FACING,c.getHorizontalDirection().getOpposite());}
    @Override public BlockEntity newBlockEntity(BlockPos p,BlockState s){return new YardStorage(p,s);}
    @Override public VoxelShape getShape(BlockState s,BlockGetter l,BlockPos p,CollisionContext c){return nest?box(1,0,1,15,5,15):s.getValue(FACING).getAxis()==Direction.Axis.Z?box(1,0,3,15,8,13):box(3,0,1,13,8,15);}
    @Override public ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level l,BlockPos p,Player player,InteractionHand hand,BlockHitResult hit){
        if(stack.isEmpty())return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!(l.getBlockEntity(p) instanceof YardStorage storage)||!storage.accepts(stack))return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!l.isClientSide && l.mayInteract(player,p) && player.mayUseItemAt(p,hit.getDirection(),stack)){
            int inserted=storage.insert(stack,player.isShiftKeyDown()?stack.getCount():1);stack.consume(inserted,player);
        }
        return ItemInteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override public InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){
        if(!player.getMainHandItem().isEmpty())return InteractionResult.PASS;
        if(!l.isClientSide && l.mayInteract(player,p) && l.getBlockEntity(p) instanceof YardStorage storage){
            for(ItemStack item:storage.extract(player.isShiftKeyDown()))if(!player.getInventory().add(item))player.drop(item,false);
        }
        return InteractionResult.sidedSuccess(l.isClientSide);
    }
    @Override public void onRemove(BlockState old,Level l,BlockPos p,BlockState next,boolean moving){
        if(!old.is(next.getBlock()) && l.getBlockEntity(p) instanceof YardStorage storage && !l.isClientSide)for(ItemStack item:storage.extract(true))popResource(l,p,item);
        super.onRemove(old,l,p,next,moving);
    }
}
