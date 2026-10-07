package org.slavicmyths.garden;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;

/** Five perennial types, with raspberry halves sharing one authoritative lower state. */
public class PerennialBush extends BushBlock implements BonemealableBlock {
    // Preserve the serialized age name of the two existing berry blocks.
    public static final IntegerProperty PHASE = IntegerProperty.create("age", 0, 4);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final TagKey<Block> SOIL = TagKey.create(Registries.BLOCK,ResourceLocation.fromNamespaceAndPath("slavicmyths","berry_soil"));
    private static final MapCodec<PerennialBush> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.INT.fieldOf("kind").forGetter(b -> b.kind),propertiesCodec()).apply(i,PerennialBush::new));
    public final int kind;
    public PerennialBush(int kind) { this(kind,Properties.of().noCollission().noOcclusion().instabreak().randomTicks().sound(SoundType.SWEET_BERRY_BUSH)); }
    private PerennialBush(int kind,Properties p) { super(p);this.kind=kind;registerDefaultState(stateDefinition.any().setValue(PHASE,0).setValue(HALF,DoubleBlockHalf.LOWER)); }
    @Override protected MapCodec<? extends PerennialBush> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b) { b.add(PHASE,HALF); }
    @Override protected boolean mayPlaceOn(BlockState state,BlockGetter level,BlockPos pos) { return state.is(SOIL); }
    @Override public boolean canSurvive(BlockState s,LevelReader level,BlockPos pos) {
        if (s.getValue(HALF)==DoubleBlockHalf.UPPER) { var below=level.getBlockState(pos.below());return kind==0 && below.is(this) && below.getValue(HALF)==DoubleBlockHalf.LOWER && below.getValue(PHASE)>=1; }
        return level.getBlockState(pos.below()).is(SOIL);
    }
    @Override public BlockState updateShape(BlockState s,Direction dir,BlockState neighbor,LevelAccessor level,BlockPos pos,BlockPos next) {
        if (s.getValue(HALF)==DoubleBlockHalf.UPPER) return canSurvive(s,level,pos)?s.setValue(PHASE,level.getBlockState(pos.below()).getValue(PHASE)):Blocks.AIR.defaultBlockState();
        if (dir==Direction.DOWN && !canSurvive(s,level,pos))return Blocks.AIR.defaultBlockState();
        if (kind==0 && dir==Direction.UP && s.getValue(PHASE)>0 && !neighbor.is(this))return s.setValue(PHASE,1);
        return s;
    }
    public boolean growTo(LevelAccessor level,BlockPos pos,int phase) {
        var current=level.getBlockState(pos);if(current.is(this)&&current.getValue(HALF)==DoubleBlockHalf.UPPER)pos=pos.below();
        if(kind==0&&phase>0) {
            var above=level.getBlockState(pos.above());if(!above.isAir() && !(above.is(this)&&above.getValue(HALF)==DoubleBlockHalf.UPPER))return false;
            level.setBlock(pos,defaultBlockState().setValue(PHASE,phase),2);
            level.setBlock(pos.above(),defaultBlockState().setValue(PHASE,phase).setValue(HALF,DoubleBlockHalf.UPPER),2);
            level.setBlock(pos,defaultBlockState().setValue(PHASE,phase),3);return true;
        }
        return level.setBlock(pos,defaultBlockState().setValue(PHASE,phase),3);
    }
    @Override public void randomTick(BlockState s,ServerLevel level,BlockPos pos,RandomSource random) {
        if(s.getValue(HALF)!=DoubleBlockHalf.LOWER || !level.isAreaLoaded(pos,1) || level.getRawBrightness(pos.above(),0)<8 || random.nextInt(18)!=0)return;
        if(kind==0 && s.getValue(PHASE)>0 && level.isEmptyBlock(pos.above()))growTo(level,pos,1);
        else if(s.getValue(PHASE)<4)growTo(level,pos,s.getValue(PHASE)+1);
    }
    private InteractionResult harvest(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit) {
        if(s.getValue(PHASE)!=4 || !level.mayInteract(player,pos) || !player.mayUseItemAt(pos,hit.getDirection(),player.getMainHandItem()))return InteractionResult.PASS;
        var base=s.getValue(HALF)==DoubleBlockHalf.UPPER?pos.below():pos;
        if(!level.isClientSide && growTo(level,base,1)) {
            int min=kind<3?2:1,max=kind==1?5:kind<3?4:3;
            popResource(level,base,new ItemStack(Gardens.berry(kind),min+level.random.nextInt(max-min+1)));
            level.playSound(null,base,SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,SoundSource.BLOCKS,.7F,1);
            ((ServerLevel)level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,s),base.getX()+.5,base.getY()+.5,base.getZ()+.5,4,.2,.2,.2,.02);
            org.slavicmyths.progression.Knowledge.award(player,"berry_garden");
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit) { return harvest(s,level,pos,player,hit); }
    @Override public ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(stack.is(Items.BONE_MEAL)||stack.is(org.slavicmyths.registry.ModItems.ORGANIC_FERTILIZER.get()))return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if(s.getValue(PHASE)!=4)return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return harvest(s,level,pos,player,hit)==InteractionResult.PASS?ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION:ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public VoxelShape getShape(BlockState s,BlockGetter level,BlockPos pos,CollisionContext c) {
        int h=s.getValue(PHASE)==0?4:new int[]{16,12,16,8,5}[kind];return box(1,0,1,15,h,15);
    }
    @Override public boolean isValidBonemealTarget(LevelReader level,BlockPos pos,BlockState s) {
        if(s.getValue(HALF)==DoubleBlockHalf.UPPER){pos=pos.below();s=level.getBlockState(pos);}
        return s.is(this)&&s.getValue(PHASE)<4&&(kind!=0||level.isEmptyBlock(pos.above())||level.getBlockState(pos.above()).is(this));
    }
    @Override public boolean isBonemealSuccess(Level level,RandomSource r,BlockPos pos,BlockState s){return true;}
    @Override public void performBonemeal(ServerLevel level,RandomSource r,BlockPos pos,BlockState s){growTo(level,pos,Math.min(4,s.getValue(PHASE)+1));}
}
