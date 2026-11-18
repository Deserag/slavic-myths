package org.slavicmyths.posad.boss;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.registry.ModItems;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/** Server-authoritative equipment modifiers for the Bauk cloak. */
@EventBusSubscriber(modid="slavicmyths")
public final class BaukCloakItem extends Item implements ICurioItem {
    private static final ResourceLocation HEALTH = ResourceLocation.fromNamespaceAndPath("slavicmyths", "bauk_cloak_health");

    public BaukCloakItem(Properties properties) { super(properties.stacksTo(1)); }
    @Override public boolean canEquip(SlotContext context, ItemStack stack) { return "charm".equals(context.identifier()); }

    @Override public void onEquip(SlotContext context, ItemStack previous, ItemStack stack) {
        if (!context.entity().level().isClientSide) updateHealth(context.entity(), true);
    }
    @Override public void onUnequip(SlotContext context, ItemStack next, ItemStack stack) {
        if (!context.entity().level().isClientSide && !next.is(this)) updateHealth(context.entity(), false);
    }
    @Override public void curioTick(SlotContext context, ItemStack stack) {
        if (!context.entity().level().isClientSide && context.entity().tickCount % 20 == 0) updateHealth(context.entity(), worn(context.entity()));
    }
    @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("item.slavicmyths.bauk_cloak.effect").withStyle(ChatFormatting.GRAY));
    }

    private static boolean worn(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity).map(inv ->
                inv.findCurios(stack -> stack.is(ModItems.BAUK_CLOAK.get())).stream()
                        .anyMatch(r -> "charm".equals(r.slotContext().identifier()))).orElse(false);
    }
    private static void updateHealth(LivingEntity entity, boolean enabled) {
        AttributeInstance attribute = entity.getAttribute(Attributes.MAX_HEALTH);
        if (attribute == null) return;
        if (enabled && attribute.getModifier(HEALTH) == null) {
            attribute.addTransientModifier(new AttributeModifier(HEALTH, 6.0D, AttributeModifier.Operation.ADD_VALUE));
        } else if (!enabled && attribute.getModifier(HEALTH) != null) {
            attribute.removeModifier(HEALTH);
            if (entity.getHealth() > entity.getMaxHealth()) entity.setHealth(entity.getMaxHealth());
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) updateHealth(player, worn(player));
    }
    @SubscribeEvent public static void onDamage(LivingDamageEvent.Pre event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide || event.getNewDamage() <= 0F) return;
        float amount = event.getNewDamage();
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != victim && worn(attacker)) amount *= 1.15F;
        if (victim instanceof ServerPlayer player && worn(player)) amount *= 0.85F;
        if (amount != event.getNewDamage()) event.setNewDamage(amount);
    }
}
