package org.slavicmyths.marine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;

/** Real 2-wide x 2-high x 3-long skull: one owner tile, six cells, no empty head sections. */
public final class StratimBirdHeadBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final IntegerProperty SIDE=IntegerProperty.create("side",0,1);
    public static final IntegerProperty LAYER=IntegerProperty.create("layer",0,1);
    public static final IntegerProperty DEPTH=IntegerProperty.create("depth",0,2);
    private static final ThreadLocal<Boolean> REMOVING=ThreadLocal.withInitial(()->false);
    private static final MapCodec<StratimBirdHeadBlock> CODEC=simpleCodec(StratimBirdHeadBlock::new);
    public StratimBirdHeadBlock(){
        this(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY)
            .strength(3.5F).sound(SoundType.BONE_BLOCK).noOcclusion());
    }
    public StratimBirdHeadBlock(Properties properties){
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH)
            .setValue(SIDE,0).setValue(LAYER,0).setValue(DEPTH,0));
    }
    @Override protected MapCodec<StratimBirdHeadBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){
        b.add(FACING,SIDE,LAYER,DEPTH);
    }
    @Override public BlockState rotate(BlockState s,Rotation r){return s.setValue(FACING,r.rotate(s.getValue(FACING)));}
    @Override public BlockState mirror(BlockState s,Mirror m){return rotate(s,m.getRotation(s.getValue(FACING)));}
    private static Direction right(Direction facing){return facing.getClockWise();}
    private static BlockPos at(BlockPos owner,Direction facing,int side,int layer,int depth){
        return owner.relative(right(facing),side).above(layer).relative(facing,depth);
    }
    private static BlockPos owner(BlockPos pos,BlockState state){
        Direction face=state.getValue(FACING);
        return pos.relative(right(face).getOpposite(),state.getValue(SIDE))
            .below(state.getValue(LAYER)).relative(face.getOpposite(),state.getValue(DEPTH));
    }
    private static boolean isOwner(BlockState s){
        return s.getValue(SIDE)==0&&s.getValue(LAYER)==0&&s.getValue(DEPTH)==0;
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx){
        Level level=ctx.getLevel();BlockPos pos=ctx.getClickedPos();
        Direction face=ctx.getHorizontalDirection().getOpposite();
        for(int x=0;x<2;x++)for(int y=0;y<2;y++)for(int z=0;z<3;z++){
            BlockPos p=at(pos,face,x,y,z);
            if(!level.hasChunkAt(p)||!level.getWorldBorder().isWithinBounds(p)
                ||!level.getBlockState(p).canBeReplaced(ctx)
                ||ctx.getPlayer()!=null&&!ctx.getPlayer().mayUseItemAt(p,face,ctx.getItemInHand()))
                return null;
        }
        return defaultBlockState().setValue(FACING,face);
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,
        LivingEntity placer,ItemStack stack){
        if(level.isClientSide)return;
        Direction face=state.getValue(FACING);
        for(int x=0;x<2;x++)for(int y=0;y<2;y++)for(int z=0;z<3;z++){
            if(x==0&&y==0&&z==0)continue;
            level.setBlock(at(pos,face,x,y,z),
                state.setValue(SIDE,x).setValue(LAYER,y).setValue(DEPTH,z),3);
        }
    }
    @Override public RenderShape getRenderShape(BlockState s){return RenderShape.ENTITYBLOCK_ANIMATED;}
    @Override public VoxelShape getShape(BlockState s,BlockGetter w,BlockPos p,CollisionContext ctx){
        return Block.box(0,0,0,16,16,16);
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){
        return isOwner(state)?new Tile(pos,state):null;
    }
    @Override protected void onRemove(BlockState s,Level level,BlockPos pos,BlockState replacement,boolean moving){
        if(!s.is(replacement.getBlock()) && !level.isClientSide && !REMOVING.get()){
            REMOVING.set(true);
            try{
                BlockPos base=owner(pos,s);
                Direction face=s.getValue(FACING);
                // Breaking any auxiliary cell first destroys the owning cell exactly once.
                // Only that owner has a loot-table drop; other 11 cells never yield an item.
                if(!pos.equals(base)){
                    BlockState root=level.getBlockState(base);
                    if(root.is(this)&&isOwner(root)&&root.getValue(FACING)==face)
                        level.destroyBlock(base,true);
                }
                for(int x=0;x<2;x++)for(int y=0;y<2;y++)for(int z=0;z<3;z++){
                    BlockPos otherPos=at(base,face,x,y,z);
                    if(otherPos.equals(pos))continue;
                    BlockState other=level.getBlockState(otherPos);
                    if(other.is(this)&&other.getValue(FACING)==face
                        &&other.getValue(SIDE)==x&&other.getValue(LAYER)==y
                        &&other.getValue(DEPTH)==z)
                        level.removeBlock(otherPos,false);
                }
            }finally{
                REMOVING.set(false);
            }
        }
        super.onRemove(s,level,pos,replacement,moving);
    }
    @Override public BlockState playerWillDestroy(Level l,BlockPos p,BlockState s,Player player){
        if(!l.isClientSide&&player.isCreative()){
            BlockPos root=owner(p,s);
            if(!root.equals(p)&&l.getBlockState(root).is(this))l.destroyBlock(root,false);
        }
        return super.playerWillDestroy(l,p,s,player);
    }
    public static final class Tile extends BlockEntity {
        public Tile(BlockPos p,BlockState s){super(StratimBirdContent.HEAD_TILE.get(),p,s);}
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){return saveWithoutMetadata(lookup);}
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){
            return ClientboundBlockEntityDataPacket.create(this);
        }
    }
}
