package org.slavicmyths.depth;
import net.minecraft.item.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
public final class PoolSpear extends SwordItem {
 public PoolSpear(Properties p){super(ItemTier.IRON,4,-2.8F,p.durability(420));}
 public int getUseDuration(ItemStack s){return 72000;}public UseAction getUseAnimation(ItemStack s){return UseAction.SPEAR;}
 public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand h){if(!p.isInWater()||p.getCooldowns().isOnCooldown(this))return ActionResult.fail(p.getItemInHand(h));p.startUsingItem(h);return ActionResult.consume(p.getItemInHand(h));}
 public void releaseUsing(ItemStack s,World w,LivingEntity user,int left){if(w.isClientSide||!(user instanceof PlayerEntity)||!user.isInWater()||getUseDuration(s)-left<16)return;PlayerEntity p=(PlayerEntity)user;if(p.getCooldowns().isOnCooldown(this))return;p.getCooldowns().addCooldown(this,120);Vector3d look=p.getLookAngle();p.setDeltaMovement(p.getDeltaMovement().add(look.scale(.65)));p.hurtMarked=true;LivingEntity target=null;double nearest=9;
  for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().expandTowards(look.scale(3)).inflate(.6),e->e!=p&&e.isAlive()&&!p.isAlliedTo(e))){Vector3d to=e.getBoundingBox().getCenter().subtract(p.getEyePosition(1));if(to.normalize().dot(look)>.75&&to.lengthSqr()<nearest&&p.canSee(e)){nearest=to.lengthSqr();target=e;}}
  if(target!=null)target.hurt(DamageSource.playerAttack(p),9);s.hurtAndBreak(2,p,e->e.broadcastBreakEvent(p.getUsedItemHand()));w.playSound(null,p.blockPosition(),SoundEvents.PLAYER_SPLASH,SoundCategory.PLAYERS,.5F,.8F);
 }
 public boolean hurtEnemy(ItemStack s,LivingEntity victim,LivingEntity user){if(!user.level.isClientSide&&user.isInWater())victim.setAirSupply(Math.max(0,victim.getAirSupply()-10));return super.hurtEnemy(s,victim,user);}
}
