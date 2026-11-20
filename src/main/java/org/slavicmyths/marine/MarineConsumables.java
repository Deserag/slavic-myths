package org.slavicmyths.marine;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;

@EventBusSubscriber(modid = "slavicmyths")
public final class MarineConsumables {
    private static final String STORM_END = "SlavicMarineStormProtectionUntil";
    public static final DeferredHolder<Item, Item> ABYSS_ELIXIR = ModItems.ITEMS.register("abyss_elixir",
        () -> new Drink("abyss_elixir", 240));
    public static final DeferredHolder<Item, Item> STORM_TINCTURE = ModItems.ITEMS.register("storm_tincture",
        () -> new Drink("storm_tincture", 180));
    public static final DeferredHolder<Item, Item> TSAR_PIKE_MEAT = ModItems.ITEMS.register("tsar_pike_meat",
        () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)
            .food(new FoodProperties.Builder().nutrition(6).saturationModifier(.65F).build())));
    public static void init() {}

    private static final class Drink extends Item {
        private final String id;
        private final int seconds;
        Drink(String id, int seconds) {
            super(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
            this.id = id;
            this.seconds = seconds;
        }
        @Override public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            if (!level.isClientSide && !player.getCooldowns().isOnCooldown(this)) {
                if (id.equals("abyss_elixir")) {
                    player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, seconds * 20, 0));
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, seconds * 20, 0));
                } else {
                    player.getPersistentData().putLong(STORM_END, level.getGameTime() + seconds * 20L);
                }
                player.getCooldowns().addCooldown(this, 20);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                    if (!player.getInventory().add(bottle)) player.drop(bottle, false);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context,
                List<Component> lines, TooltipFlag flags) {
            super.appendHoverText(stack, context, lines, flags);
            lines.add(Component.translatable("item.slavicmyths." + id + ".desc")
                .withStyle(ChatFormatting.AQUA));
        }
    }

    @SubscribeEvent public static void lightning(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide
                || !event.getSource().is(DamageTypes.LIGHTNING_BOLT)) return;
        long until = player.getPersistentData().getLong(STORM_END);
        if (until > player.level().getGameTime())
            event.setNewDamage(event.getNewDamage() * .6F);
        else player.getPersistentData().remove(STORM_END);
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        event.getEntity().getPersistentData().remove(STORM_END);
    }
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        event.getEntity().getPersistentData().remove(STORM_END);
    }
    private MarineConsumables() {}
}
