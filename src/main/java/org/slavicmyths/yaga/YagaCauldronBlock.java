package org.slavicmyths.yaga;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
/** World-only cauldron service, no portable drop or block entity inventory. */
public final class YagaCauldronBlock extends Block{
 private static final VoxelShape SHAPE=Block.box(1,0,1,15,15,15);
 public YagaCauldronBlock(){super(AbstractBlock.Properties.of(Material.METAL).strength(-1,3600000).sound(SoundType.METAL).noOcclusion().noDrops());}
 @Override public VoxelShape getShape(BlockState s,IBlockReader w,BlockPos p,ISelectionContext c){return SHAPE;}
 @Override public ActionResultType use(BlockState s,World w,BlockPos pos,PlayerEntity player,Hand hand,BlockRayTraceResult hit){if(w.isClientSide)return ActionResultType.SUCCESS;if(!(player instanceof ServerPlayerEntity))return ActionResultType.PASS;YagaData d=YagaData.get((ServerWorld)w);if(d.npc==null)return ActionResultType.FAIL;net.minecraft.entity.Entity e=((ServerWorld)w).getEntity(d.npc);if(!(e instanceof BabaYaga))return ActionResultType.FAIL;BabaYaga y=(BabaYaga)e;if(y.home==null||!pos.equals(y.home.offset(-2,0,1))||player.distanceToSqr(y)>64)return ActionResultType.FAIL;YagaData.Progress p=d.progress(player.getUUID());if(p.intro<2||p.blocked(w.getGameTime()))return ActionResultType.FAIL;YagaMenu.open((ServerPlayerEntity)player,y,3);y.gesture(4);return ActionResultType.CONSUME;}
}
