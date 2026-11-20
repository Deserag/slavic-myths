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
@EventBusSubscriber(modid = "slavicmyths")
public final class ZhelvaShield extends ShieldItem {
    public static final DeferredHolder<Item, Item> ITEM = ModItems.ITEMS.register(
        "zhelva_shield", () -> new ZhelvaShield());
    static final int COOLDOWN = 400;
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
        lines.add(Component.translatable("item.slavicmyths.zhelva_shield.desc")
            .withStyle(ChatFormatting.DARK_AQUA));
    }

    static boolean guarding(ServerPlayer p) {
        return p.isBlocking() && p.getUseItem().is(ITEM.get());
    }

    @SubscribeEvent public static void blocked(LivingShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !guarding(p)
                || !event.getBlocked() || event.getBlockedDamage() <= 0) return;
        long now = p.serverLevel().getGameTime();
        var data = p.getPersistentData();
        if (event.getBlockedDamage() >= 7F && now >= data.getLong(READY)) {
            data.putLong(READY, now + COOLDOWN);
            data.putLong(UNTIL, now + DURATION);
            p.serverLevel().sendParticles(ParticleTypes.NAUTILUS, p.getX(),
                p.getY() + 1D, p.getZ(), 18, .5, .6, .5, .025);
        }
        if (data.getLong(UNTIL) > now) {
            event.setShieldDamage(event.shieldDamage() * .65F);
        }
    }

    @SubscribeEvent public static void knockback(LivingKnockBackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer p) || !guarding(p)) return;
        long now = p.serverLevel().getGameTime();
        boolean active = p.getPersistentData().getLong(UNTIL) > now;
        event.setStrength(event.getStrength() * (active ? .35F : .65F));
    }
}
