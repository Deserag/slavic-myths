package org.slavicmyths.artifact;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
public final class GusliItem extends Item {
 public GusliItem(Properties p){super(p.stacksTo(1));}
 @Override public int getUseDuration(ItemStack s,net.minecraft.world.entity.LivingEntity user){return 72000;}
 @Override public UseAnim getUseAnimation(ItemStack s){return UseAnim.BOW;}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){if(p.isPassenger())return InteractionResultHolder.fail(p.getItemInHand(h));p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
 @Override public void onUseTick(Level w,LivingEntity e,ItemStack s,int left){if(!(e instanceof Player))return;Player p=(Player)e;p.setSprinting(false);if(!w.isClientSide&&getUseDuration(s,e)-left>=10&&left%10==0)ArtifactEvents.music(p);}
 @Override public void releaseUsing(ItemStack s,Level w,LivingEntity e,int left){if(!w.isClientSide&&e instanceof Player)((Player)e).getCooldowns().addCooldown(this,getUseDuration(s,e)-left>=20?600:100);}
}
