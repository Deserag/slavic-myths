package org.slavicmyths.posad.boss;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.slavicmyths.combat.IndependentWeaponCooldowns;
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
        if(hand!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(weapon);
        if(!(level instanceof ServerLevel world))return InteractionResultHolder.sidedSuccess(weapon,true);
        if(!(player instanceof ServerPlayer server)||!server.isAlive()||server.isSpectator())return InteractionResultHolder.fail(weapon);
        boolean empowered=player.isShiftKeyDown();
        if(!IndependentWeaponCooldowns.ready(server,IndependentWeaponCooldowns.MYTAR,empowered))return InteractionResultHolder.fail(weapon);
        ShackledSickleProjectile projectile=ShackledSickleContent.PROJECTILE.get().create(world);
        if(projectile==null)return InteractionResultHolder.fail(weapon);
        projectile.setOwner(player);projectile.setItem(weapon.copyWithCount(1));
        projectile.setRupture(empowered);
        projectile.setPos(player.getX(),player.getEyeY()-.18,player.getZ());
        projectile.shoot(player.getLookAngle().x,player.getLookAngle().y,player.getLookAngle().z,1.45F*(float)Math.sqrt(org.slavicmyths.combat.RangeEnchantment.multiplier(level,weapon)),0);
        if(!world.addFreshEntity(projectile))return InteractionResultHolder.fail(weapon);
        IndependentWeaponCooldowns.start(server,weapon,IndependentWeaponCooldowns.MYTAR,empowered,empowered?320:170);
        weapon.hurtAndBreak(empowered?5:3,player,hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND);
        player.playSound(net.minecraft.sounds.SoundEvents.CHAIN_PLACE,1F,.9F);
        return InteractionResultHolder.success(weapon);
    }
    @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag flag){
        super.appendHoverText(stack,context,lines,flag);
        lines.add(Component.translatable("tooltip.slavicmyths.mytar.throw").withStyle(net.minecraft.ChatFormatting.AQUA));
        lines.add(Component.translatable("tooltip.slavicmyths.mytar.grab").withStyle(net.minecraft.ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.slavicmyths.mytar.rupture").withStyle(net.minecraft.ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.slavicmyths.mytar.cooldowns").withStyle(net.minecraft.ChatFormatting.YELLOW));
        lines.add(Component.translatable("tooltip.slavicmyths.weapon.rune_slots", org.slavicmyths.rpg.Runes.slots(stack)).withStyle(net.minecraft.ChatFormatting.GRAY));
    }
}