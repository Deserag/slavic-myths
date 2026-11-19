package org.slavicmyths.marine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.util.RandomSource;

/**
 * Six actual footprint cells (2 x 3) for the head, two actual cells (1 x 2) for the crest.
 * Part 0, rear-left, owns the BlockEntity and exactly one item drop.
 */
public final class OceanZmeyHeadBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final IntegerProperty PART = IntegerProperty.create("part",0,5);
    private static final MapCodec<OceanZmeyHeadBlock> HEAD_CODEC =
        simpleCodec(p -> new OceanZmeyHeadBlock(p,true));
    private static final MapCodec<OceanZmeyHeadBlock> CREST_CODEC =
        simpleCodec(p -> new OceanZmeyHeadBlock(p,false));
    private static final VoxelShape SHAPE=Block.box(0,0,0,16,15,16);
    private static final ThreadLocal<Boolean> REMOVING=ThreadLocal.withInitial(()->false);
    private final boolean head;

    public OceanZmeyHeadBlock(boolean head) {
        this(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN)
            .strength(4F).sound(SoundType.CORAL_BLOCK).noOcclusion(),head);
    }
    public OceanZmeyHeadBlock(Properties p,boolean head) {
        super(p);
        this.head=head;
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(PART,0));
    }
    public boolean isHead(){return head;}
    public int width(){return head?2:1;}
    public int length(){return head?3:2;}
    @Override protected MapCodec<? extends OceanZmeyHeadBlock> codec(){
        return head?HEAD_CODEC:CREST_CODEC;
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,net.minecraft.world.level.block.state.BlockState> b){
        b.add(FACING,PART);
    }
    @Override public net.minecraft.world.level.block.state.BlockState rotate(
        net.minecraft.world.level.block.state.BlockState state,Rotation r) {
        return state.setValue(FACING,r.rotate(state.getValue(FACING)));
    }
    @Override public net.minecraft.world.level.block.state.BlockState mirror(
        net.minecraft.world.level.block.state.BlockState state,Mirror m) {
        return rotate(state,m.getRotation(state.getValue(FACING)));
    }
    public BlockPos at(BlockPos anchor,Direction face,int index) {
        return anchor.relative(face,index/width()).relative(face.getClockWise(),index%width());
    }
    public BlockPos origin(BlockPos position,Direction face,int index) {
        return position.relative(face,-index/width()).relative(face.getClockWise(),-index%width());
    }
    @Override public net.minecraft.world.level.block.state.BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction face=ctx.getHorizontalDirection().getOpposite();
        BlockPos anchor=ctx.getClickedPos();
        Level level=ctx.getLevel();
        for(int part=0;part<width()*length();part++){
            BlockPos p=at(anchor,face,part);
            if(!level.hasChunkAt(p) || !level.getWorldBorder().isWithinBounds(p)
                || !level.getBlockState(p).canBeReplaced(ctx)
                || (ctx.getPlayer()!=null && !ctx.getPlayer().mayUseItemAt(p,face,ctx.getItemInHand())))
                return null;
        }
        return defaultBlockState().setValue(FACING,face);
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,
        net.minecraft.world.level.block.state.BlockState state,LivingEntity placer,ItemStack stack) {
        if(level.isClientSide)return;
        Direction f=state.getValue(FACING);
        for(int part=1;part<width()*length();part++)
            level.setBlock(at(pos,f,part),state.setValue(PART,part),3);
    }
    @Override public RenderShape getRenderShape(net.minecraft.world.level.block.state.BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    @Override public VoxelShape getShape(net.minecraft.world.level.block.state.BlockState s,
        BlockGetter w,BlockPos p,CollisionContext c){return SHAPE;}
    @Override public BlockEntity newBlockEntity(BlockPos p,net.minecraft.world.level.block.state.BlockState s) {
        return s.getValue(PART)==0?new Tile(p,s):null;
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level,net.minecraft.world.level.block.state.BlockState state,BlockEntityType<T> type) {
        if(level.isClientSide || state.getValue(PART)!=0)return null;
        return (world,p,s,be)->{
            if(be instanceof Tile tile)tile.serverTick();
        };
    }
    @Override protected void onRemove(net.minecraft.world.level.block.state.BlockState old,
        Level world,BlockPos pos,net.minecraft.world.level.block.state.BlockState next,boolean moved) {
        if(!old.is(next.getBlock()) && !world.isClientSide && !REMOVING.get()) {
            REMOVING.set(true);
            try {
                Direction f=old.getValue(FACING);
                int part=old.getValue(PART);
                BlockPos anchor=origin(pos,f,part);
                // When an auxiliary cell is broken, destroy the owner once for its item drop.
                if(part!=0){
                    var owner=world.getBlockState(anchor);
                    if(owner.is(this) && owner.getValue(PART)==0 && owner.getValue(FACING)==f)
                        world.destroyBlock(anchor,true);
                }
                for(int i=0;i<width()*length();i++){
                    BlockPos other=at(anchor,f,i);
                    if(other.equals(pos))continue;
                    var check=world.getBlockState(other);
                    if(check.is(this) && check.getValue(PART)==i && check.getValue(FACING)==f)
                        world.removeBlock(other,false);
                }
            } finally {REMOVING.set(false);}
        }
        super.onRemove(old,world,pos,next,moved);
    }
    @Override public net.minecraft.world.level.block.state.BlockState playerWillDestroy(
        Level level,BlockPos pos,net.minecraft.world.level.block.state.BlockState state,Player player) {
        if(!level.isClientSide && player.isCreative() && state.getValue(PART)!=0) {
            BlockPos origin=origin(pos,state.getValue(FACING),state.getValue(PART));
            var owner=level.getBlockState(origin);
            if(owner.is(this) && owner.getValue(PART)==0)level.destroyBlock(origin,false);
        }
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override protected InteractionResult useWithoutItem(net.minecraft.world.level.block.state.BlockState state,
        Level level,BlockPos pos,Player player,BlockHitResult hit) {
        BlockPos anchor=origin(pos,state.getValue(FACING),state.getValue(PART));
        if(level.getBlockEntity(anchor) instanceof Tile tile) {
            if(!level.isClientSide)tile.activate();
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
    @Override public void animateTick(net.minecraft.world.level.block.state.BlockState state,
        Level level,BlockPos pos,RandomSource rng) {
        if(state.getValue(PART)!=0)return;
        if(rng.nextInt(6)==0)
            level.addParticle(ParticleTypes.BUBBLE,pos.getX()+rng.nextDouble(),
                pos.getY()+.75,pos.getZ()+rng.nextDouble(),0,.02,0);
        if(rng.nextInt(110)==0)
            level.playLocalSound(pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,
                SoundEvents.GENERIC_SPLASH,SoundSource.BLOCKS,.18F,.65F,false);
    }

    public static final class Tile extends BlockEntity {
        private long activeUntil, reactiveUntil;
        public Tile(BlockPos pos,net.minecraft.world.level.block.state.BlockState state) {
            super(OceanZmeyContent.HEAD_TILE.get(),pos,state);
        }
        public boolean active(){return level!=null && level.getGameTime()<activeUntil;}
        public boolean reactive(){return level!=null && level.getGameTime()<reactiveUntil;}
        public void activate(){
            if(level==null)return;
            activeUntil=level.getGameTime()+300;
            setChanged();
            level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
        }
        private void serverTick(){
            if(level==null || level.getGameTime()%20!=0)return;
            boolean nearby=!level.getEntitiesOfClass(LivingEntity.class,
                new AABB(worldPosition).inflate(5),
                e->e.isAlive() && !e.isSpectator()).isEmpty();
            if(nearby){
                reactiveUntil=level.getGameTime()+60;
                setChanged();
                level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
            }
        }
        @Override protected void saveAdditional(CompoundTag nbt,HolderLookup.Provider provider) {
            super.saveAdditional(nbt,provider);
            nbt.putLong("ActiveUntil",activeUntil);
            nbt.putLong("ReactiveUntil",reactiveUntil);
        }
        @Override protected void loadAdditional(CompoundTag nbt,HolderLookup.Provider provider) {
            super.loadAdditional(nbt,provider);
            activeUntil=nbt.getLong("ActiveUntil");
            reactiveUntil=nbt.getLong("ReactiveUntil");
        }
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
            return saveWithoutMetadata(provider);
        }
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket() {
            return ClientboundBlockEntityDataPacket.create(this);
        }
        public AABB getRenderBoundingBox() {
            if(getBlockState().getBlock() instanceof OceanZmeyHeadBlock trophy) {
                Direction f=getBlockState().getValue(FACING);
                BlockPos last=trophy.at(worldPosition,f,trophy.width()*trophy.length()-1);
                return new AABB(
                    Math.min(worldPosition.getX(),last.getX()),
                    worldPosition.getY(),
                    Math.min(worldPosition.getZ(),last.getZ()),
                    Math.max(worldPosition.getX(),last.getX())+1,
                    Math.max(worldPosition.getY(),last.getY())+2,
                    Math.max(worldPosition.getZ(),last.getZ())+1).inflate(.75);
            }
            return new AABB(worldPosition).inflate(2);
        }
    }
}
