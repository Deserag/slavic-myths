package org.slavicmyths.item;

import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public final class SimpleCharmItem extends Item {
    private final String kind;
    public SimpleCharmItem(String kind, Properties properties) { super(properties); this.kind = kind; }
    @Override public net.minecraft.util.ActionResult<ItemStack> use(World w,net.minecraft.entity.player.PlayerEntity p,net.minecraft.util.Hand hand){if(!kind.equals("hunter_charm"))return super.use(w,p,hand);ItemStack stack=p.getItemInHand(hand);if(p.getCooldowns().isOnCooldown(this))return net.minecraft.util.ActionResult.fail(stack);if(!w.isClientSide){p.getCooldowns().addCooldown(this,400);org.slavicmyths.hunt.HuntItems.detect(p);}return net.minecraft.util.ActionResult.consume(stack);}
    @Override public void appendHoverText(ItemStack stack, World world, List<ITextComponent> text, ITooltipFlag flag) {
        text.add(new TranslationTextComponent("tooltip.slavicmyths." + kind));
    }
}
