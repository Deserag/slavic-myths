package org.slavicmyths.marine;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** True 1x2 horizontal trophy. The rear owns the item drop and the animated tile. */
public final class TsarPikeHeadBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty FRONT=BooleanProperty.create("front");
    private static final MapCodec<TsarPikeHeadBlock> CODEC=simpleCodec(TsarPikeHeadBlock::new);
    private static final VoxelShape REAR_SHAPE=Block.box(1,0,1,15,14,16);
    private static final VoxelShape FRONT_SHAPE=Block.box(2,2,0,14,12,16);

    public TsarPikeHeadBlock() {
        this(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN)
            .strength(3.5F).sound(SoundType.BONE_BLOCK).noOcclusion());
    }
    public TsarPikeHeadBlock(Properties properties){
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(FRONT,false));
    }
    @Override protected MapCodec<TsarPikeHeadBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){
        b.add(FACING,FRONT);
    }
    @Override public BlockState rotate(BlockState state,Rotation rotation){
        return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));
    }
    @Override public BlockState mirror(BlockState state,Mirror mirror){
        return rotate(state,mirror.getRotation(state.getValue(FACING)));
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level=ctx.getLevel();BlockPos rear=ctx.getClickedPos();
        Direction face=ctx.getHorizontalDirection().getOpposite();
        BlockPos front=rear.relative(face);
        if(!level.hasChunkAt(front) || !level.getWorldBorder().isWithinBounds(front)
            || !level.getBlockState(front).canBeReplaced(ctx)
            || ctx.getPlayer()!=null && !ctx.getPlayer().mayUseItemAt(front,face,ctx.getItemInHand()))
            return null;
        return defaultBlockState().setValue(FACING,face);
    }
    @Override public void setPlacedBy(Level level,BlockPos pos,BlockState state,
                                     LivingEntity placer,ItemStack stack){
        if(!level.isClientSide)
            level.setBlock(pos.relative(state.getValue(FACING)),state.setValue(FRONT,true),3);
    }
    @Override public RenderShape getRenderShape(BlockState state){
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
    @Override public VoxelShape getShape(BlockState state,BlockGetter world,BlockPos p,CollisionContext ctx){
        return state.getValue(FRONT)?FRONT_SHAPE:REAR_SHAPE;
    }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){
        return state.getValue(FRONT)?null:new Tile(pos,state);
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
        Level level,BlockState state,BlockEntityType<T> type){
        if(state.getValue(FRONT))return null;
        return (world,pos,blockState,blockEntity) -> {
            if(blockEntity instanceof Tile head && !world.isClientSide)head.serverTick();
        };
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,
                                      BlockState replacement,boolean moving){
        if(!state.is(replacement.getBlock()) && !level.isClientSide){
            Direction face=state.getValue(FACING);
            if(state.getValue(FRONT)){
                BlockPos rear=pos.relative(face.getOpposite());
                BlockState other=level.getBlockState(rear);
                if(other.is(this)&&!other.getValue(FRONT)&&other.getValue(FACING)==face)
                    level.destroyBlock(rear,true);
            }else{
                BlockPos front=pos.relative(face);
                BlockState other=level.getBlockState(front);
                if(other.is(this)&&other.getValue(FRONT)&&other.getValue(FACING)==face)
                    level.removeBlock(front,false);
            }
        }
        super.onRemove(state,level,pos,replacement,moving);
    }
    @Override public BlockState playerWillDestroy(Level level,BlockPos pos,BlockState state,Player player){
        if(!level.isClientSide && player.isCreative() && state.getValue(FRONT)){
            BlockPos rear=pos.relative(state.getValue(FACING).getOpposite());
            BlockState other=level.getBlockState(rear);
            if(other.is(this)&&!other.getValue(FRONT))level.destroyBlock(rear,false);
        }
        return super.playerWillDestroy(level,pos,state,player);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,
        Player player,BlockHitResult hit){
        BlockPos rear=state.getValue(FRONT)?pos.relative(state.getValue(FACING).getOpposite()):pos;
        if(level.getBlockEntity(rear) instanceof Tile tile){
            if(!level.isClientSide){
                tile.activate();
                level.playSound(null,rear,SoundEvents.COD_FLOP,
                    net.minecraft.sounds.SoundSource.BLOCKS,1.1F,.65F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }
    @Override public void animateTick(BlockState state,Level level,BlockPos pos,RandomSource random) {
        if(state.getValue(FRONT))return;
        if(random.nextInt(9)==0){
            level.addParticle(ParticleTypes.DRIPPING_WATER,
                pos.getX()+.1+random.nextDouble()*.8,pos.getY()+.4,
                pos.getZ()+.1+random.nextDouble()*.8,0,0,0);
        }
        if(random.nextInt(14)==0 && (
            level.getFluidState(pos.below()).is(FluidTags.WATER)
            || level.getFluidState(pos.north()).is(FluidTags.WATER)
            || level.getFluidState(pos.south()).is(FluidTags.WATER)
            || level.getFluidState(pos.east()).is(FluidTags.WATER)
            || level.getFluidState(pos.west()).is(FluidTags.WATER))) {
            level.addParticle(ParticleTypes.BUBBLE,
                pos.getX()+.5,pos.getY()+.35,pos.getZ()+.5,
                (random.nextDouble()-.5)*.03,.025,(random.nextDouble()-.5)*.03);
        }
        if(random.nextInt(110)==0)
            level.playLocalSound(pos.getX()+.5,pos.getY()+.6,pos.getZ()+.5,
                SoundEvents.COD_AMBIENT,net.minecraft.sounds.SoundSource.BLOCKS,
                .25F,.5F+random.nextFloat()*.2F,false);
    }

    public static final class Tile extends BlockEntity {
        private long activeUntil;
        private long reactiveUntil;
        public Tile(BlockPos pos,BlockState state){
            super(TsarPikeContent.HEAD_TILE.get(),pos,state);
        }
        public boolean active(){return level!=null && level.getGameTime()<activeUntil;}
        public boolean reactive(){return level!=null && level.getGameTime()<reactiveUntil;}
        public void activate(){
            if(level==null)return;
            activeUntil=level.getGameTime()+400;
            setChanged();
            level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
        }
        private void serverTick(){
            if(level==null || level.getGameTime()%20!=0)return;
            long now=level.getGameTime();
            boolean nearby=!level.getEntitiesOfClass(LivingEntity.class,
                new net.minecraft.world.phys.AABB(worldPosition).inflate(5),
                e->e.isAlive() && !e.isSpectator()).isEmpty();
            // Visual response is automatic. The gameplay aura remains click-activated.
            if(nearby && reactiveUntil<now+20){
                reactiveUntil=now+55;
                setChanged();
                level.sendBlockUpdated(worldPosition,getBlockState(),getBlockState(),3);
            }
            if(now%40!=0 || !active())return;
            for(Player p:level.getEntitiesOfClass(Player.class,
                new net.minecraft.world.phys.AABB(worldPosition).inflate(8),
                target->!target.isSpectator() && target.isInWaterOrBubble())){
                p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE,100,0,true,false));
            }
            if(level instanceof net.minecraft.server.level.ServerLevel s)
                s.sendParticles(ParticleTypes.BUBBLE,worldPosition.getX()+.5,
                    worldPosition.getY()+.65,worldPosition.getZ()+.5,9,.7,.35,.7,.035);
        }
        @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider lookup){
            super.saveAdditional(tag,lookup);
            tag.putLong("ActiveUntil",activeUntil);
            tag.putLong("ReactiveUntil",reactiveUntil);
        }
        @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider lookup){
            super.loadAdditional(tag,lookup);
            activeUntil=tag.getLong("ActiveUntil");
            reactiveUntil=tag.getLong("ReactiveUntil");
        }
        @Override public CompoundTag getUpdateTag(HolderLookup.Provider lookup){
            return saveWithoutMetadata(lookup);
        }
        @Override public ClientboundBlockEntityDataPacket getUpdatePacket(){
            return ClientboundBlockEntityDataPacket.create(this);
        }
        public net.minecraft.world.phys.AABB getRenderBoundingBox(){
            if(getBlockState().getBlock() instanceof TsarPikeHeadBlock){
                Direction f=getBlockState().getValue(FACING);
                return new net.minecraft.world.phys.AABB(worldPosition)
                    .expandTowards(f.getStepX(),0,f.getStepZ()).inflate(.3);
            }
            return new net.minecraft.world.phys.AABB(worldPosition).inflate(.3);
        }
    }
}
