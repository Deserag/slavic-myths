package org.slavicmyths.depth;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
public final class PoolSpear extends SwordItem {
 public PoolSpear(Properties p){super(Tiers.IRON,p.durability(420).attributes(SwordItem.createAttributes(Tiers.IRON,4,-2.8F)));}
 public int getUseDuration(ItemStack s,net.minecraft.world.entity.LivingEntity user){return 72000;}public UseAnim getUseAnimation(ItemStack s){return UseAnim.SPEAR;}
 public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){if(!p.isInWater()||p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(p.getItemInHand(h));p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
 public void releaseUsing(ItemStack s,Level w,LivingEntity user,int left){if(w.isClientSide||!(user instanceof Player)||!user.isInWater()||getUseDuration(s,user)-left<16)return;Player p=(Player)user;if(p.getCooldowns().isOnCooldown(this))return;p.getCooldowns().addCooldown(this,120);Vec3 look=p.getLookAngle();p.setDeltaMovement(p.getDeltaMovement().add(look.scale(.65)));p.hurtMarked=true;LivingEntity target=null;double nearest=9;
  for(LivingEntity e:w.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().expandTowards(look.scale(3)).inflate(.6),e->e!=p&&e.isAlive()&&!p.isAlliedTo(e))){Vec3 to=e.getBoundingBox().getCenter().subtract(p.getEyePosition(1));if(to.normalize().dot(look)>.75&&to.lengthSqr()<nearest&&p.hasLineOfSight(e)){nearest=to.lengthSqr();target=e;}}
  if(target!=null)target.hurt(p.damageSources().playerAttack(p),9);s.hurtAndBreak(2,p,net.minecraft.world.entity.LivingEntity.getSlotForHand(p.getUsedItemHand()));w.playSound(null,p.blockPosition(),SoundEvents.PLAYER_SPLASH,SoundSource.PLAYERS,.5F,.8F);
 }
 public boolean hurtEnemy(ItemStack s,LivingEntity victim,LivingEntity user){if(!user.level().isClientSide&&user.isInWater())victim.setAirSupply(Math.max(0,victim.getAirSupply()-10));return super.hurtEnemy(s,victim,user);}
}
