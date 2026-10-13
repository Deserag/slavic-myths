package org.slavicmyths.item;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import org.slavicmyths.registry.ModBlocks;
public final class FolkBerryItem extends Item {
 private final boolean raspberry;
 public FolkBerryItem(Properties p,boolean raspberry){super(p);this.raspberry=raspberry;}
 @Override public InteractionResult useOn(UseOnContext c){BlockPos pos=c.getClickedPos().relative(c.getClickedFace());Block b=raspberry?ModBlocks.RASPBERRY_BUSH.get():ModBlocks.BLUEBERRY_BUSH.get();
  if(c.getClickedFace()!=Direction.UP||!c.getLevel().getBlockState(pos).canBeReplaced()||!b.defaultBlockState().canSurvive(c.getLevel(),pos)||c.getPlayer()==null||!c.getPlayer().mayUseItemAt(pos,c.getClickedFace(),c.getItemInHand()))return InteractionResult.PASS;
  if(!c.getLevel().isClientSide){c.getLevel().setBlock(pos,b.defaultBlockState(),3);org.slavicmyths.progression.Knowledge.award(c.getPlayer(),"berry_garden");if(!c.getPlayer().getAbilities().instabuild)c.getItemInHand().shrink(1);}return InteractionResult.sidedSuccess(c.getLevel().isClientSide);
 }
}
