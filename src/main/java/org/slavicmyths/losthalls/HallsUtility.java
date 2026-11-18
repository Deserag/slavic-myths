package org.slavicmyths.losthalls;

import net.minecraft.core.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.MultifaceBlock;

/** Tool use reads only completed room records and already-resident blocks. */
public final class HallsUtility {
 public static final class TuningFork extends Item {
  public TuningFork(){super(new Properties().stacksTo(1));}
  @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){
   ItemStack stack=p.getItemInHand(h);if(p.getCooldowns().isOnCooldown(this)||org.slavicmyths.rpg.PathData.data(p).getLong("HallsForkReady")>org.slavicmyths.rpg.RareRuneRuntime.now(p))return InteractionResultHolder.fail(stack);
   if(w instanceof ServerLevel server){
    var found=server.dimension().equals(Level.NETHER)?LostHallsRooms.get(server).nearest(p.blockPosition(),at->server.hasChunkAt(at)&&server.isEmptyBlock(at)&&server.isEmptyBlock(at.above())&&server.getFluidState(at).isEmpty()&&server.getBlockState(at.below()).isFaceSturdy(server,at.below(),Direction.UP)):java.util.Optional.<LostHallsRooms.Node>empty();
    p.displayClientMessage(found.map(n->Component.translatable("halls.fork.found",n.anchor().getX(),n.anchor().getY(),n.anchor().getZ())).orElseGet(()->Component.translatable("halls.fork.none")),true);
    w.playSound(null,p.blockPosition(),found.isPresent()?SoundEvents.AMETHYST_BLOCK_CHIME:SoundEvents.AMETHYST_BLOCK_HIT,SoundSource.PLAYERS,.6F,found.isPresent()?1.1F:.6F);p.getCooldowns().addCooldown(this,400);org.slavicmyths.rpg.PathData.data(p).putLong("HallsForkReady",org.slavicmyths.rpg.RareRuneRuntime.now(p)+400);
   }return InteractionResultHolder.sidedSuccess(stack,w.isClientSide);
  }
 }
 public static final class SporeItem extends Item {
  public SporeItem(){super(new Properties());}
  @Override public InteractionResult useOn(UseOnContext c){var w=c.getLevel();var at=c.getClickedPos().relative(c.getClickedFace());var coating=HallsBlocks.get("cave_coating");var old=w.getBlockState(at);Direction face=c.getClickedFace().getOpposite();
   if(!w.hasChunkAt(at)||!w.getWorldBorder().isWithinBounds(at)||(!old.isAir()&&!old.is(coating))||!w.getFluidState(at).isEmpty())return InteractionResult.FAIL;
   var state=(old.is(coating)?old:coating.defaultBlockState()).setValue(MultifaceBlock.getFaceProperty(face),true);
   if(!w.getBlockState(at.relative(face)).isFaceSturdy(w,at.relative(face),face.getOpposite()))return InteractionResult.FAIL;
   if(old==state||!state.canSurvive(w,at)||c.getPlayer()!=null&&!c.getPlayer().mayUseItemAt(at,c.getClickedFace(),c.getItemInHand()))return InteractionResult.FAIL;
   if(!w.isClientSide&&w.setBlock(at,state,3)){if(c.getPlayer()==null||!c.getPlayer().getAbilities().instabuild)c.getItemInHand().shrink(1);}
   return InteractionResult.sidedSuccess(w.isClientSide);
  }
 }
 private HallsUtility(){}
}
