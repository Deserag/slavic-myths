package org.slavicmyths.sisters;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.slavicmyths.morovoy.MorovoyFireZmeyContent;
import org.slavicmyths.viy.ViyContent;

/** Boss-specific progression materials; vanilla loot and existing PosadBoss rewards remain untouched. */
@EventBusSubscriber(modid = "slavicmyths")
public final class BossLootV25 {
    private static void add(LivingDropsEvent event, ServerLevel level, Item item, int amount) {
        if (amount < 1 || event.getDrops().stream().anyMatch(drop -> drop.getItem().is(item))) return;
        var entity = event.getEntity();
        event.getDrops().add(new ItemEntity(level, entity.getX(), entity.getY() + .45, entity.getZ(), new ItemStack(item, amount)));
    }

    @SubscribeEvent
    public static void drops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel world)) return;
        EntityType<?> type = event.getEntity().getType();
        int ingots = 2 + world.random.nextInt(2);
        if (type == SistersContent.TRYASEYA.get()) {
            add(event, world, SistersContent.TRYASEYA_INGOT.get(), ingots);
            add(event, world, SistersContent.NETHER_SOUL_IV.get(), 1);
        } else if (type == SistersContent.GNETEYA.get()) {
            add(event, world, SistersContent.GNETEYA_INGOT.get(), ingots);
            add(event, world, SistersContent.NETHER_SOUL_IV.get(), 1);
        } else if (type == SistersContent.LEDEYA.get()) {
            add(event, world, SistersContent.LEDEYA_INGOT.get(), ingots);
            add(event, world, SistersContent.NETHER_SOUL_IV.get(), 1);
        } else if (type == SistersContent.AGNEYA.get()) {
            add(event, world, SistersContent.AGNEYA_INGOT.get(), ingots);
            add(event, world, SistersContent.NETHER_SOUL_IV.get(), 1);
        } else if (type == SistersContent.NEVEYA.get()) {
            add(event, world, SistersContent.NEVEYA_INGOT.get(), ingots);
            add(event, world, SistersContent.NETHER_SOUL_V.get(), 1);
        } else if (type == SistersContent.LOMEYA.get()) {
            add(event, world, SistersContent.LOMEYA_INGOT.get(), ingots);
            add(event, world, SistersContent.NETHER_SOUL_V.get(), 1);
            add(event, world, SistersContent.HUNTER_BOW_CORE.get(), 1);
        } else if (type == ViyContent.VIY.get()) {
            add(event, world, ViyContent.VIY_EYE.get(), 1);
            add(event, world, ViyContent.VIY_RING.get(), 1);
        } else if (type == MorovoyFireZmeyContent.BOSS.get()) {
            add(event, world, MorovoyFireZmeyContent.BONE.get(), 2 + world.random.nextInt(2));
            add(event, world, MorovoyFireZmeyContent.CORE.get(), 1);
        }
    }

    private BossLootV25() {}
}
