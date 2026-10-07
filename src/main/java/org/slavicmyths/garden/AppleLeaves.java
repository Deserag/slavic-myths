package org.slavicmyths.garden;

import com.mojang.serialization.MapCodec;
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

public final class AppleLeaves extends LeavesBlock implements BonemealableBlock {
    public static final IntegerProperty FRUIT_STAGE=IntegerProperty.create("fruit_stage",0,3);
    public static final BooleanProperty FRUITABLE=BooleanProperty.create("fruitable");
    private static final MapCodec<AppleLeaves> CODEC=simpleCodec(AppleLeaves::new);
    public AppleLeaves(Properties p){super(p);registerDefaultState(defaultBlockState().setValue(FRUIT_STAGE,0).setValue(FRUITABLE,false));}
    @Override public MapCodec<AppleLeaves> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(FRUIT_STAGE,FRUITABLE);}
    @Override protected boolean isRandomlyTicking(BlockState s){return super.isRandomlyTicking(s)||(s.getValue(FRUITABLE)&&s.getValue(FRUIT_STAGE)<3)||(!s.getValue(FRUITABLE)&&s.getValue(FRUIT_STAGE)!=0);}
    @Override public void randomTick(BlockState s,ServerLevel level,BlockPos pos,RandomSource random){
        super.randomTick(s,level,pos,random);if(!level.getBlockState(pos).is(this))return;
        if(!s.getValue(FRUITABLE)){if(s.getValue(FRUIT_STAGE)!=0)level.setBlock(pos,s.setValue(FRUIT_STAGE,0),2);return;}
        if(s.getValue(FRUIT_STAGE)<3&&random.nextInt(24)==0)level.setBlock(pos,s.setValue(FRUIT_STAGE,s.getValue(FRUIT_STAGE)+1),2);
    }
    @Override public InteractionResult useWithoutItem(BlockState s,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(!s.getValue(FRUITABLE)||s.getValue(FRUIT_STAGE)!=3||!level.mayInteract(player,pos)||!player.mayUseItemAt(pos,hit.getDirection(),player.getMainHandItem()))return InteractionResult.PASS;
        if(!level.isClientSide){
            level.setBlock(pos,s.setValue(FRUIT_STAGE,0),2);popResource(level,pos,new ItemStack(Items.APPLE,level.random.nextFloat()<.25F?2:1));
            level.playSound(null,pos,SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES,SoundSource.BLOCKS,.7F,1);
            ((ServerLevel)level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK,s),pos.getX()+.5,pos.getY()+.5,pos.getZ()+.5,4,.2,.2,.2,.02);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public ItemInteractionResult useItemOn(ItemStack stack,BlockState s,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        if(stack.is(Items.BONE_MEAL))return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        if(!s.getValue(FRUITABLE)||s.getValue(FRUIT_STAGE)!=3)return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        return useWithoutItem(s,level,pos,player,hit)==InteractionResult.PASS?ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION:ItemInteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override public boolean isValidBonemealTarget(LevelReader l,BlockPos p,BlockState s){return s.getValue(FRUITABLE)&&s.getValue(FRUIT_STAGE)<3;}
    @Override public boolean isBonemealSuccess(Level l,RandomSource r,BlockPos p,BlockState s){return true;}
    @Override public void performBonemeal(ServerLevel l,RandomSource r,BlockPos p,BlockState s){if(isValidBonemealTarget(l,p,s))l.setBlock(p,s.setValue(FRUIT_STAGE,s.getValue(FRUIT_STAGE)+1),2);}
}
