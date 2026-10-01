package org.slavicmyths.combat;

import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

/** Charged, line-of-sight strike at 4 blocks; no global reach attribute or chain physics. */
public final class FlailItem extends MaceItem {
    public FlailItem(Properties p){super(ItemTier.IRON,8,.7F,.22F,.65F,p);}
    @Override public int getUseDuration(ItemStack s){return 72000;}
    @Override public UseAction getUseAnimation(ItemStack s){return UseAction.SPEAR;}
    @Override public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand hand){if(hand!=Hand.MAIN_HAND)return ActionResult.pass(p.getItemInHand(hand));p.startUsingItem(hand);return ActionResult.consume(p.getItemInHand(hand));}
    @Override public void releaseUsing(ItemStack stack,World world,LivingEntity user,int remaining) {
        if(world.isClientSide||!(user instanceof PlayerEntity)||getUseDuration(stack)-remaining<18)return;
        PlayerEntity player=(PlayerEntity)user;if(player.getCooldowns().isOnCooldown(this))return;
        player.getCooldowns().addCooldown(this,40);player.resetAttackStrengthTicker();
        Vector3d eye=player.getEyePosition(1),end=eye.add(player.getLookAngle().scale(4));
        BlockRayTraceResult block=world.clip(new RayTraceContext(eye,end,RayTraceContext.BlockMode.COLLIDER,RayTraceContext.FluidMode.NONE,player));
        if(block.getType()!=RayTraceResult.Type.MISS)end=block.getLocation();
        LivingEntity target=null;double best=eye.distanceToSqr(end);
        for(LivingEntity candidate:world.getEntitiesOfClass(LivingEntity.class,new AxisAlignedBB(eye,end).inflate(.4),e->e!=player&&e.isAlive()&&!e.isSpectator()&&!player.isAlliedTo(e))) {
            java.util.Optional<Vector3d> hit=candidate.getBoundingBox().inflate(.2).clip(eye,end);
            if(hit.isPresent()&&eye.distanceToSqr(hit.get())<=best){target=candidate;best=eye.distanceToSqr(hit.get());}
        }
        if(target!=null&&target.hurt(DamageSource.playerAttack(player),9)){hurtEnemy(stack,target,player);player.setLastHurtMob(target);}
        player.swing(Hand.MAIN_HAND,true);
    }
}
