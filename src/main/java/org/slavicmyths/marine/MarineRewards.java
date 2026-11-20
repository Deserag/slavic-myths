package org.slavicmyths.marine;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.ModItems;

/**
 * Six marine boss crafting resources and six princely trade tokens.
 * Intentionally does not replace existing trophy heads, crest or Zhelva charm.
 * See GitHub issue #519 for ownership, prices and test requirements.
 */
public final class MarineRewards {
    public static final DeferredHolder<Item, Item> PHARAOHKA_MATERIAL = register("pharaohka_tide_pearl", "Жемчужина прилива");
    public static final DeferredHolder<Item, Item> DEEP_SERPENT_MATERIAL = register("deep_sea_serpent_scale", "Чешуя глубин");
    public static final DeferredHolder<Item, Item> OCEAN_ZMEY_MATERIAL = register("ocean_zmey_storm_heart", "Сердце бури");
    public static final DeferredHolder<Item, Item> STRATIM_MATERIAL = register("stratim_bird_feather", "Перо Стратим");
    public static final DeferredHolder<Item, Item> TSAR_PIKE_MATERIAL = register("tsar_pike_fang", "Клык Царя-Щуки");
    public static final DeferredHolder<Item, Item> ZHELVA_MATERIAL = register("zhelva_shell_plate", "Пластина Желвы");
    public static final DeferredHolder<Item, Item> PHARAOHKA_TOKEN = register("pharaohka_victory_token", "Знак победы над Фараонкой");
    public static final DeferredHolder<Item, Item> DEEP_SERPENT_TOKEN = register("deep_sea_serpent_victory_token", "Знак победы над Морским змеем");
    public static final DeferredHolder<Item, Item> OCEAN_ZMEY_TOKEN = register("ocean_zmey_victory_token", "Знак победы над Змеем бурь");
    public static final DeferredHolder<Item, Item> STRATIM_TOKEN = register("stratim_bird_victory_token", "Знак победы над Стратим");
    public static final DeferredHolder<Item, Item> TSAR_PIKE_TOKEN = register("tsar_pike_victory_token", "Знак победы над Царём-Щукой");
    public static final DeferredHolder<Item, Item> ZHELVA_TOKEN = register("zhelva_victory_token", "Знак победы над Желвой");

    private static DeferredHolder<Item, Item> register(String id, String ru) {
        return ModItems.ITEMS.register(id, () -> new Item(new Item.Properties().rarity(Rarity.RARE)) {
            @Override
            public Component getName(ItemStack stack) {
                return Component.translatableWithFallback("item.slavicmyths." + id, ru);
            }
        });
    }

    public static void init() {
        NeoForge.EVENT_BUS.addListener(MarineRewards::drops);
    }

    private static void drops(LivingDropsEvent event) {
        LivingEntity boss = event.getEntity();
        if (boss.level().isClientSide || boss.isAlive()) return;
        if (boss instanceof PharaohkaEntity pharaohka) {
            if (pharaohka.leader()) grant(event, PHARAOHKA_MATERIAL.get(), 3, PHARAOHKA_TOKEN.get());
        } else if (boss instanceof DeepSeaSerpentEntity) {
            grant(event, DEEP_SERPENT_MATERIAL.get(), 5, DEEP_SERPENT_TOKEN.get());
        } else if (boss instanceof OceanZmeyEntity) {
            grant(event, OCEAN_ZMEY_MATERIAL.get(), 3, OCEAN_ZMEY_TOKEN.get());
        } else if (boss instanceof StratimBirdEntity) {
            grant(event, STRATIM_MATERIAL.get(), 4, STRATIM_TOKEN.get());
        } else if (boss instanceof TsarPikeEntity) {
            grant(event, TSAR_PIKE_MATERIAL.get(), 3, TSAR_PIKE_TOKEN.get());
        } else if (boss instanceof ZhelvaEntity) {
            grant(event, ZHELVA_MATERIAL.get(), 6, ZHELVA_TOKEN.get());
        }
    }

    private static void grant(LivingDropsEvent event, Item material, int count, Item token) {
        // Own two items only: never remove the boss's existing decorative/usable trophies.
        event.getDrops().removeIf(drop -> drop.getItem().is(material) || drop.getItem().is(token));
        LivingEntity boss = event.getEntity();
        event.getDrops().add(new ItemEntity(
            boss.level(), boss.getX(), boss.getY() + 0.8, boss.getZ(),
            new ItemStack(material, count)));
        event.getDrops().add(new ItemEntity(
            boss.level(), boss.getX(), boss.getY() + 0.8, boss.getZ(),
            new ItemStack(token)));
    }

    private MarineRewards() {}
}
