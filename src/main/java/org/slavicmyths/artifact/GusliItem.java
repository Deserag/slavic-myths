package org.slavicmyths.artifact;
import net.minecraft.item.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
public final class GusliItem extends Item {
 public GusliItem(Properties p){super(p.stacksTo(1));}
 @Override public int getUseDuration(ItemStack s){return 72000;}
 @Override public UseAction getUseAnimation(ItemStack s){return UseAction.BOW;}
 @Override public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand h){if(p.isPassenger())return ActionResult.fail(p.getItemInHand(h));p.startUsingItem(h);return ActionResult.consume(p.getItemInHand(h));}
 @Override public void onUseTick(World w,LivingEntity e,ItemStack s,int left){if(!(e instanceof PlayerEntity))return;PlayerEntity p=(PlayerEntity)e;p.setSprinting(false);if(!w.isClientSide&&getUseDuration(s)-left>=10&&left%10==0)ArtifactEvents.music(p);}
 @Override public void releaseUsing(ItemStack s,World w,LivingEntity e,int left){if(!w.isClientSide&&e instanceof PlayerEntity)((PlayerEntity)e).getCooldowns().addCooldown(this,getUseDuration(s)-left>=20?600:100);}
}
