package org.slavicmyths.depth;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
public final class PoolStoneBlock extends Block {
 public PoolStoneBlock(){super(Properties.of(Material.STONE).strength(-1,3600000).noOcclusion());}
 public boolean hasTileEntity(BlockState s){return true;}public TileEntity createTileEntity(BlockState s,IBlockReader w){return new PoolStoneTile();}
 public ActionResultType use(BlockState s,World w,BlockPos p,PlayerEntity player,Hand h,BlockRayTraceResult hit){if(!w.isClientSide&&w.getBlockEntity(p) instanceof PoolStoneTile){org.slavicmyths.progression.Knowledge.award(player,"find_deep_pool");((PoolStoneTile)w.getBlockEntity(p)).activate(player,h);}return ActionResultType.sidedSuccess(w.isClientSide);}
 public void tick(BlockState s,ServerWorld w,BlockPos p,java.util.Random r){if(w.getBlockEntity(p) instanceof PoolStoneTile)((PoolStoneTile)w.getBlockEntity(p)).step();}
}
