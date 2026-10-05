package org.slavicmyths.depth;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
public final class VodyanoyNetItem extends Item {
 public VodyanoyNetItem(Properties p){super(p.durability(64));}
 public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){ItemStack s=p.getItemInHand(h);if(p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(s);if(!w.isClientSide){ThrownNet projectile=new ThrownNet(w,p);projectile.shootFromRotation(p,p.getXRot(),p.getYRot(),0,1.2F,1);if(w.addFreshEntity(projectile)){p.getCooldowns().addCooldown(this,160);s.hurtAndBreak(1,p,net.minecraft.world.entity.LivingEntity.getSlotForHand(h));w.playSound(null,p.blockPosition(),SoundEvents.FISHING_BOBBER_THROW,SoundSource.PLAYERS,.6F,.7F);}}return InteractionResultHolder.consume(s);}
}
