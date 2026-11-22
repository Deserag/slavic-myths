package org.slavicmyths.marine;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;

/** Vanilla ShieldItem animation/block behavior, with a bounded server-side guard. */
public final class ZhelvaShield extends ShieldItem {
    public static final DeferredHolder<Item, Item> ITEM = ModItems.ITEMS.register(
        "zhelva_shield", () -> new ZhelvaShield());
    static final int COOLDOWN = 240;
    static final int DURATION = 60;
    private static final String READY = "SlavicZhelvaShellGuardReady";
    private static final String UNTIL = "SlavicZhelvaShellGuardUntil";

    private ZhelvaShield() {
        super(new Item.Properties().durability(1200).rarity(Rarity.EPIC).fireResistant());
    }
    public static void init() {}

    @Override public void appendHoverText(ItemStack stack, TooltipContext context,
            List<Component> lines, TooltipFlag flags) {
        super.appendHoverText(stack, context, lines, flags);
        if(context.level()!=null)lines.add(Component.translatable(ready(stack,context.level().getGameTime())?"item.slavicmyths.zhelva_shield.ready":"item.slavicmyths.zhelva_shield.recharging").withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("item.slavicmyths.zhelva_shield.desc")
            .withStyle(ChatFormatting.DARK_AQUA));
    }

    static boolean guarding(ServerPlayer p) {
        return p.isBlocking() && p.getUseItem().is(ITEM.get());
    }

    public static boolean ready(ItemStack stack,long now) {
        return stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag().getLong("MarineGuardReady")<=now;
    }
    @Override public void inventoryTick(ItemStack stack,net.minecraft.world.level.Level level,net.minecraft.world.entity.Entity entity,int slot,boolean selected){
        super.inventoryTick(stack,level,entity,slot,selected);
        if(entity instanceof ServerPlayer p){
            long until=p.getPersistentData().getLong("MarineWeapon_perfectBlock");
            var tag=stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
            if(tag.getLong("MarineGuardReady")!=until){tag.putLong("MarineGuardReady",until);stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(tag));}
        }
    }

}
