package org.slavicmyths.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import org.slavicmyths.savanna.SteppeWeapons;

public final class SteppeWeaponClient {
    public static void properties() {
        ItemProperties.register(SteppeWeapons.item(SteppeWeapons.Kind.SKARAPEYA_BLADE),
            ResourceLocation.fromNamespaceAndPath("slavicmyths", "steppe_split"),
            (s,l,e,seed) -> SteppeWeapons.split(s) ? 1 : 0);
        for (var k : SteppeWeapons.Kind.values()) {
            if (!k.bow() && k != SteppeWeapons.Kind.STEPPE_CROSSBOW) continue;
            var item = SteppeWeapons.item(k);
            boolean crossbow = item instanceof CrossbowItem;
            ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pulling"),
                (s,l,e,seed) -> e != null && e.isUsingItem() && e.getUseItem() == s
                    && (!crossbow || !CrossbowItem.isCharged(s)) ? 1 : 0);
            ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pull"),
                (s,l,e,seed) -> e == null || !e.isUsingItem() || e.getUseItem() != s
                    || crossbow && CrossbowItem.isCharged(s) ? 0
                    : Mth.clamp((s.getUseDuration(e) - e.getUseItemRemainingTicks())
                        / (float)(crossbow ? Math.max(1, CrossbowItem.getChargeDuration(s,e)) : 20), 0, 1));
            if (crossbow) {
                ItemProperties.register(item, ResourceLocation.withDefaultNamespace("charged"),
                    (s,l,e,seed) -> CrossbowItem.isCharged(s) ? 1 : 0);
                ItemProperties.register(item, ResourceLocation.withDefaultNamespace("firework"),
                    (s,l,e,seed) -> CrossbowItem.isCharged(s) && s.getOrDefault(DataComponents.CHARGED_PROJECTILES,
                        ChargedProjectiles.EMPTY).contains(Items.FIREWORK_ROCKET) ? 1 : 0);
            }
        }
    }
    private SteppeWeaponClient() {}
}
