package org.slavicmyths.combat;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
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

/** Charged, line-of-sight strike at 4 blocks; no global reach attribute or chain physics. */
public class FlailItem extends MaceItem {
    private final float directMultiplier;
    public FlailItem(Properties p){super(Tiers.IRON,8,.7F,.22F,.65F,p);directMultiplier=1;}
    public FlailItem(Tier tier,Properties p){this(tier,p,1);}
    public FlailItem(Tier tier,Properties p,float directMultiplier){this(tier,p,directMultiplier,.7F,.65F);}
    protected FlailItem(Tier tier,Properties p,float directMultiplier,float speed,float push){super(tier,(6+tier.getAttackDamageBonus())*directMultiplier,speed,.22F,push,p);this.directMultiplier=directMultiplier;}
    public static float chargedDamage(Tier tier,float multiplier){return (7+tier.getAttackDamageBonus())*multiplier;}
    public float chargedDamage(){return chargedDamage(getTier(),directMultiplier);}
    @Override public boolean canDisableShield(ItemStack stack,ItemStack shield,LivingEntity entity,LivingEntity attacker){return true;}
    @Override public int getUseDuration(ItemStack s,LivingEntity user){return 72000;}
    @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.SPEAR;}
    @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){if(hand!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(p.getItemInHand(hand));p.startUsingItem(hand);return InteractionResultHolder.consume(p.getItemInHand(hand));}
    @Override public void releaseUsing(ItemStack stack,Level world,LivingEntity user,int remaining) {
        if(world.isClientSide||!(user instanceof Player)||getUseDuration(stack,user)-remaining<18)return;
        Player player=(Player)user;if(player.getCooldowns().isOnCooldown(this))return;
        player.getCooldowns().addCooldown(this,40);player.resetAttackStrengthTicker();
        Vec3 eye=player.getEyePosition(1),end=eye.add(player.getLookAngle().scale(4));
        BlockHitResult block=world.clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        if(block.getType()!=HitResult.Type.MISS)end=block.getLocation();
        LivingEntity target=null;double best=eye.distanceToSqr(end);
        for(LivingEntity candidate:world.getEntitiesOfClass(LivingEntity.class,new AABB(eye,end).inflate(.4),e->e!=player&&e.isAlive()&&!e.isSpectator()&&!player.isAlliedTo(e))) {
            java.util.Optional<Vec3> hit=candidate.getBoundingBox().inflate(.2).clip(eye,end);
            if(hit.isPresent()&&eye.distanceToSqr(hit.get())<=best){target=candidate;best=eye.distanceToSqr(hit.get());}
        }
        if(target!=null&&org.slavicmyths.rpg.Abilities.canHit(player,target)&&target.hurt(player.damageSources().playerAttack(player),chargedDamage())){hurtEnemy(stack,target,player);player.setLastHurtMob(target);}
        player.swing(InteractionHand.MAIN_HAND,true);
    }
}
