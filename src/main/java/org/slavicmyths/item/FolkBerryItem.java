package org.slavicmyths.item;
import net.minecraft.item.*;
import net.minecraft.block.Block;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import org.slavicmyths.registry.ModBlocks;
public final class FolkBerryItem extends Item {
 private final boolean raspberry;
 public FolkBerryItem(Properties p,boolean raspberry){super(p);this.raspberry=raspberry;}
 @Override public ActionResultType useOn(ItemUseContext c){BlockPos pos=c.getClickedPos().relative(c.getClickedFace());Block b=raspberry?ModBlocks.RASPBERRY_BUSH.get():ModBlocks.BLUEBERRY_BUSH.get();
  if(c.getClickedFace()!=Direction.UP||!c.getLevel().getBlockState(pos).getMaterial().isReplaceable()||!b.defaultBlockState().canSurvive(c.getLevel(),pos)||c.getPlayer()==null||!c.getPlayer().mayUseItemAt(pos,c.getClickedFace(),c.getItemInHand()))return ActionResultType.PASS;
  if(!c.getLevel().isClientSide){c.getLevel().setBlock(pos,b.defaultBlockState(),3);org.slavicmyths.progression.Knowledge.award(c.getPlayer(),"berry_garden");if(!c.getPlayer().abilities.instabuild)c.getItemInHand().shrink(1);}return ActionResultType.sidedSuccess(c.getLevel().isClientSide);
 }
}
