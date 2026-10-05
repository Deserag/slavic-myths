package org.slavicmyths.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModItems;

@net.neoforged.fml.common.EventBusSubscriber(modid = SlavicMyths.MOD_ID)
public final class StormProtection {
    private static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> FOREST_SPIRITS =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath(SlavicMyths.MOD_ID, "forest_spirits"));
    @SubscribeEvent public static void onHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        if (player.getOffhandItem().getItem() == ModItems.AMBER_CHARM.get() && event.getSource().is(net.minecraft.tags.DamageTypeTags.IS_FIRE)) {
            event.setAmount(event.getAmount() * (org.slavicmyths.rpg.PathData.has(player,"charm_power")?0.8F:0.85F));
        }
        if (player.getOffhandItem().getItem() == ModItems.TRAVELER_CHARM.get() && event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL)) {
            event.setAmount(event.getAmount() * (org.slavicmyths.rpg.PathData.has(player,"charm_power")?0.8F:0.85F));
        }
        if (player.getOffhandItem().getItem() == ModItems.FOREST_CHARM.get()
                && event.getSource().getEntity() != null && event.getSource().getEntity().getType().is(FOREST_SPIRITS)) {
            event.setAmount(event.getAmount() * (org.slavicmyths.rpg.PathData.has(player,"charm_power")?0.8F:0.85F));
        }
        if (!event.getSource().is(net.minecraft.world.damagesource.DamageTypes.LIGHTNING_BOLT)) return;
        if (player.getOffhandItem().getItem() == ModItems.PERUN_CHARM.get()) {
            event.setAmount(event.getAmount() * (org.slavicmyths.rpg.PathData.has(player,"charm_power")?0.65F:0.7F));
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).getItem() == ModItems.PERUNITE_HELMET.get()
                && player.getItemBySlot(EquipmentSlot.CHEST).getItem() == ModItems.PERUNITE_CHESTPLATE.get()
                && player.getItemBySlot(EquipmentSlot.LEGS).getItem() == ModItems.PERUNITE_LEGGINGS.get()
                && player.getItemBySlot(EquipmentSlot.FEET).getItem() == ModItems.PERUNITE_BOOTS.get()) {
            event.setAmount(event.getAmount() * 0.75F);
        }
    }
    private StormProtection() { }
}
