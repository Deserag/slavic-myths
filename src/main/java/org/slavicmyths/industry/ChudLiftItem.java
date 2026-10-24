package org.slavicmyths.industry;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.kurgan.BurialRecords;

/** Finite loaded-column surface search; a failed ascent never consumes charge/cooldown. */
public final class ChudLiftItem extends Item {
 public ChudLiftItem(){super(new Properties().durability(64));}
 public int getEnchantmentValue(){return 10;}
 public int getUseDuration(ItemStack s,LivingEntity e){return 30;}
 public UseAnim getUseAnimation(ItemStack s){return UseAnim.BOW;}
 public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){if(p.isPassenger()||p.isSpectator())return InteractionResultHolder.fail(p.getItemInHand(h));p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
 public static boolean locked(ServerPlayer p){
  if(p.level().dimension()!=Level.OVERWORLD||!p.isAlive()||p.isPassenger()||!p.mayBuild()||p.isSpectator())return true;
  var instance=BurialRecords.get(p.serverLevel()).at(p.blockPosition());
  if(instance!=null&&instance.plan.tier==2&&!instance.bossDefeated){var room=instance.plan.rooms.get(instance.plan.finalRoom);if(room.box().contains(p.getBlockX()-instance.origin.getX(),p.getBlockY()-instance.origin.getY(),p.getBlockZ()-instance.origin.getZ()))return true;}
  for(var arena:org.slavicmyths.earth.EarthArena.get(p.serverLevel()).entries.values())if(arena.gateClosed&&arena.contains(p.position()))return true;
  if(org.slavicmyths.bogatyr.BogatyrLedger.active(p))return true;
  // Explicit integration escape hatches for other arena/claim providers; native arenas above
  // use their actual authoritative records, not these optional tags.
  return p.getPersistentData().getBoolean("NoTeleport")||p.getPersistentData().getBoolean("ArenaLocked");
 }
 public static boolean safe(ServerPlayer p,BlockPos feet){
  var w=p.serverLevel();if(feet.getY()<=p.getY()+2||feet.getY()<w.getMinBuildHeight()+1||feet.getY()+2>=w.getMaxBuildHeight()||w.getChunkSource().getChunkNow(feet.getX()>>4,feet.getZ()>>4)==null||!w.getWorldBorder().isWithinBounds(feet)||!w.mayInteract(p,feet)||p.blockActionRestricted(w,feet,p.gameMode.getGameModeForPlayer()))return false;
  var floor=w.getBlockState(feet.below());if(!floor.isCollisionShapeFullBlock(w,feet.below())||!floor.getFluidState().isEmpty()||floor.is(BlockTags.LEAVES)||floor.is(Blocks.MAGMA_BLOCK)||floor.is(Blocks.CACTUS)||floor.is(Blocks.CAMPFIRE)||floor.is(Blocks.SOUL_CAMPFIRE)||floor.is(Blocks.POWDER_SNOW))return false;
  if(!w.getBlockState(feet).isAir()||!w.getBlockState(feet.above()).isAir()||!w.canSeeSky(feet.above()))return false;
  return w.noCollision(p,p.getBoundingBox().move(Vec3.atBottomCenterOf(feet).subtract(p.position())));
 }
 public static BlockPos destination(ServerPlayer p){
  if(locked(p))return null;var w=p.serverLevel();
  for(int r=0;r<=4;r++)for(int x=-r;x<=r;x++)for(int z=-r;z<=r;z++){
   if(Math.max(Math.abs(x),Math.abs(z))!=r)continue;var pos=p.blockPosition().offset(x,0,z);
   if(w.getChunkSource().getChunkNow(pos.getX()>>4,pos.getZ()>>4)==null)continue;
   var feet=new BlockPos(pos.getX(),w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,pos.getX(),pos.getZ()),pos.getZ());if(safe(p,feet))return feet;
  }return null;
 }
 public ItemStack finishUsingItem(ItemStack s,Level w,LivingEntity entity){
  if(!(entity instanceof ServerPlayer p))return s;var feet=destination(p);
  if(feet==null){p.displayClientMessage(Component.translatable("chud_tools.lift.fail"),true);return s;}
  var at=Vec3.atBottomCenterOf(feet);var event=net.neoforged.neoforge.event.EventHooks.onEntityTeleportCommand(p,at.x,at.y,at.z);
  // Cancellation and rerouting cannot bypass the surface/safety policy.
  if(event.isCanceled()||!safe(p,BlockPos.containing(event.getTargetX(),event.getTargetY(),event.getTargetZ()))||event.getTargetX()!=at.x||event.getTargetY()!=at.y||event.getTargetZ()!=at.z)return s;
  p.teleportTo(p.serverLevel(),at.x,at.y,at.z,p.getYRot(),p.getXRot());p.fallDistance=0;p.setDeltaMovement(Vec3.ZERO);
  s.hurtAndBreak(1,p,LivingEntity.getSlotForHand(p.getUsedItemHand()));p.getCooldowns().addCooldown(this,1200);
  w.playSound(null,feet,SoundEvents.CHAIN_PLACE,SoundSource.PLAYERS,.8F,.7F);return s;
 }
 public void appendHoverText(ItemStack s,TooltipContext c,List<Component> t,TooltipFlag f){t.add(Component.translatable("chud_tools.hint.lift"));}
}
