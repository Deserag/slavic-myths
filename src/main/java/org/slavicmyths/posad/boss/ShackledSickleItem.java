package org.slavicmyths.posad.boss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;

public final class ShackledSickleItem extends SwordItem {
    public ShackledSickleItem(Tier tier,Properties properties){super(tier,properties);}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand){
        ItemStack weapon=player.getItemInHand(hand);
        if(player.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(weapon);
        if(!(level instanceof ServerLevel world))return InteractionResultHolder.sidedSuccess(weapon,true);
        ShackledSickleProjectile projectile=ShackledSickleContent.PROJECTILE.get().create(world);
        if(projectile==null)return InteractionResultHolder.fail(weapon);
        projectile.setOwner(player);
        projectile.setPos(player.getX(),player.getEyeY()-.18,player.getZ());
        projectile.shoot(player.getLookAngle().x,player.getLookAngle().y,player.getLookAngle().z,1.45F,0);
        if(!world.addFreshEntity(projectile))return InteractionResultHolder.fail(weapon);
        player.getCooldowns().addCooldown(this,240);
        weapon.hurtAndBreak(3,player,hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
        player.playSound(net.minecraft.sounds.SoundEvents.CHAIN_PLACE,1F,.9F);
        return InteractionResultHolder.success(weapon);
    }
}
