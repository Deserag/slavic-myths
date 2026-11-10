package org.slavicmyths.posad.boss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import java.util.List;
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
        boolean empowered=player.isShiftKeyDown();
        projectile.setRupture(empowered);
        projectile.setPos(player.getX(),player.getEyeY()-.18,player.getZ());
        projectile.shoot(player.getLookAngle().x,player.getLookAngle().y,player.getLookAngle().z,1.45F,0);
        if(!world.addFreshEntity(projectile))return InteractionResultHolder.fail(weapon);
        player.getCooldowns().addCooldown(this,empowered?320:170);
        weapon.hurtAndBreak(empowered?5:3,player,hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
        player.playSound(net.minecraft.sounds.SoundEvents.CHAIN_PLACE,1F,.9F);
        return InteractionResultHolder.success(weapon);
    }
    @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag flag){
        super.appendHoverText(stack,context,lines,flag);
        lines.add(Component.literal("ПКМ: бросить серп на цепи (24 урона), притянуть врага").withStyle(net.minecraft.ChatFormatting.AQUA));
        lines.add(Component.literal("Захват: цель до 4 блоков высотой, до 22 блоков вдали").withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.add(Component.literal("Shift + ПКМ: разрыв цепи (36 урона + волна 14)").withStyle(net.minecraft.ChatFormatting.GOLD));
        lines.add(Component.literal("Цепь возвращается к владельцу; 8.5 / 16 сек. перезарядка").withStyle(net.minecraft.ChatFormatting.YELLOW));
        lines.add(Component.literal("Рунные слоты: "+org.slavicmyths.rpg.Runes.slots(stack)+" / 3").withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}
