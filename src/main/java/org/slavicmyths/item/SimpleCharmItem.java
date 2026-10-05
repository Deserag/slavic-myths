package org.slavicmyths.item;

import java.util.List;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public final class SimpleCharmItem extends Item {
    private final String kind;
    public SimpleCharmItem(String kind, Properties properties) { super(properties); this.kind = kind; }
    @Override public net.minecraft.world.InteractionResultHolder<ItemStack> use(Level w,net.minecraft.world.entity.player.Player p,net.minecraft.world.InteractionHand hand){if(!kind.equals("hunter_charm"))return super.use(w,p,hand);ItemStack stack=p.getItemInHand(hand);if(p.getCooldowns().isOnCooldown(this))return net.minecraft.world.InteractionResultHolder.fail(stack);if(!w.isClientSide){p.getCooldowns().addCooldown(this,400);org.slavicmyths.hunt.HuntItems.detect(p);}return net.minecraft.world.InteractionResultHolder.consume(stack);}
    @Override public void appendHoverText(ItemStack stack, Item.TooltipContext world, List<Component> text, TooltipFlag flag) {
        text.add(Component.translatable("tooltip.slavicmyths." + kind));
    }
}
