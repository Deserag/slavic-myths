package org.slavicmyths.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import org.slavicmyths.marine.MarineWeapons;

/** Item overrides are per-item: vanilla rod/trident predicates are not inherited. */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class MarineItemClient {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(MarineWeapons.ANCHOR.get(),
                ResourceLocation.withDefaultNamespace("cast"),
                (stack, world, entity, seed) ->
                    entity instanceof Player player && player.fishing != null
                        && (player.getMainHandItem() == stack || player.getOffhandItem() == stack)
                        ? 1F : 0F);
            ItemProperties.register(MarineWeapons.HARPOON.get(),
                ResourceLocation.withDefaultNamespace("throwing"),
                (stack, world, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack
                        ? 1F : 0F);
            ItemProperties.register(org.slavicmyths.marine.PharaohkaTrident.ITEM.get(),
                ResourceLocation.withDefaultNamespace("throwing"),
                (stack, world, entity, seed) ->
                    entity != null && entity.isUsingItem() && entity.getUseItem() == stack
                        ? 1F : 0F);
        });
    }
    private MarineItemClient() {}
}
