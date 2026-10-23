package org.slavicmyths.sanctuary;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.phys.BlockHitResult;
/** Ordinary breakable decoration, deliberately not an EntityBlock. */
public final class SanctuaryDecor extends Block {
 private final String kind;
 public SanctuaryDecor(String kind){super(BlockBehaviour.Properties.ofFullCopy(kind.startsWith("carved_fire")||kind.startsWith("fire_idol")?Blocks.OAK_LOG:Blocks.STONE_BRICKS).mapColor(kind.startsWith("carved_fire")||kind.startsWith("fire_idol")?net.minecraft.world.level.material.MapColor.WOOD:net.minecraft.world.level.material.MapColor.STONE).strength(3.5f,10));this.kind=kind;registerDefaultState(stateDefinition.any().setValue(SanctuaryBlock.FACING,Direction.NORTH).setValue(SanctuaryBlock.LIT,false));}
 @Override protected MapCodec<? extends Block> codec(){return MapCodec.unit(this);}
 @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){b.add(SanctuaryBlock.FACING,SanctuaryBlock.LIT);}
 @Override public BlockState getStateForPlacement(BlockPlaceContext c){return defaultBlockState().setValue(SanctuaryBlock.FACING,c.getHorizontalDirection().getOpposite());}
 @Override protected BlockState rotate(BlockState s,Rotation r){return s.setValue(SanctuaryBlock.FACING,r.rotate(s.getValue(SanctuaryBlock.FACING)));}
 @Override protected BlockState mirror(BlockState s,Mirror m){return s.rotate(m.getRotation(s.getValue(SanctuaryBlock.FACING)));}
 @Override protected InteractionResult useWithoutItem(BlockState s,Level w,BlockPos at,Player p,BlockHitResult h){if(!kind.startsWith("seal_"))return InteractionResult.PASS;if(!w.isClientSide&&w.getGameTime()>=p.getPersistentData().getLong("Sanctuary193SealNotice")){p.getPersistentData().putLong("Sanctuary193SealNotice",w.getGameTime()+30);p.displayClientMessage(SanctuaryContent.text("sealed"),true);SanctuaryContent.sound(w,at,"door_blocked");}return InteractionResult.sidedSuccess(w.isClientSide);}
 @Override public void animateTick(BlockState s,Level w,BlockPos p,net.minecraft.util.RandomSource r){if(kind.startsWith("seal_")&&r.nextInt(120)==0)w.addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,p.getX()+.5,p.getY()+.5,p.getZ()+.5,0,.01,0);if(kind.equals("seal_fire_door")&&r.nextInt(1800)==0)w.playLocalSound(p,SanctuaryContent.SOUNDS.get(r.nextBoolean()?"fire_seal":"fire_relic_hint").get(),net.minecraft.sounds.SoundSource.BLOCKS,.12f,1,false);}
}
