package org.slavicmyths.mor;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public final class MorFlaskItem extends Item {
    public MorFlaskItem(Properties p){super(p);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){ItemStack stack=player.getItemInHand(hand);if(!level.isClientSide){MorFlaskProjectile flask=new MorFlaskProjectile(MorContent.FLASK.get(),level);flask.setOwner(player);flask.setPos(player.getX(),player.getEyeY()-.1,player.getZ());flask.setItem(stack.copyWithCount(1));flask.shootFromRotation(player,player.getXRot(),player.getYRot(),-20,.5F,1);if(!level.addFreshEntity(flask))return InteractionResultHolder.fail(stack);player.getCooldowns().addCooldown(this,10);stack.consume(1,player);level.playSound(null,player.blockPosition(),SoundEvents.SPLASH_POTION_THROW,SoundSource.PLAYERS,.5F,1); }return InteractionResultHolder.sidedSuccess(stack,level.isClientSide);}
    @Override public void appendHoverText(ItemStack stack,TooltipContext context,List<Component> lines,TooltipFlag flag){lines.add(Component.translatable("item.slavicmyths.mor_flask.tooltip"));}
}
