package org.slavicmyths.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.util.DamageSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModItems;

@Mod.EventBusSubscriber(modid = SlavicMyths.MOD_ID)
public final class StormProtection {
    private static final net.minecraftforge.common.Tags.IOptionalNamedTag<net.minecraft.entity.EntityType<?>> FOREST_SPIRITS =
            EntityTypeTags.createOptional(new ResourceLocation(SlavicMyths.MOD_ID, "forest_spirits"));
    @SubscribeEvent public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
        if (player.getOffhandItem().getItem() == ModItems.AMBER_CHARM.get() && event.getSource().isFire()) {
            event.setAmount(event.getAmount() * 0.85F);
        }
        if (player.getOffhandItem().getItem() == ModItems.TRAVELER_CHARM.get() && event.getSource() == DamageSource.FALL) {
            event.setAmount(event.getAmount() * 0.85F);
        }
        if (player.getOffhandItem().getItem() == ModItems.FOREST_CHARM.get()
                && event.getSource().getEntity() != null && event.getSource().getEntity().getType().is(FOREST_SPIRITS)) {
            event.setAmount(event.getAmount() * 0.85F);
        }
        if (event.getSource() != DamageSource.LIGHTNING_BOLT) return;
        if (player.getOffhandItem().getItem() == ModItems.PERUN_CHARM.get()) {
            event.setAmount(event.getAmount() * 0.7F);
        }
        if (player.getItemBySlot(EquipmentSlotType.HEAD).getItem() == ModItems.PERUNITE_HELMET.get()
                && player.getItemBySlot(EquipmentSlotType.CHEST).getItem() == ModItems.PERUNITE_CHESTPLATE.get()
                && player.getItemBySlot(EquipmentSlotType.LEGS).getItem() == ModItems.PERUNITE_LEGGINGS.get()
                && player.getItemBySlot(EquipmentSlotType.FEET).getItem() == ModItems.PERUNITE_BOOTS.get()) {
            event.setAmount(event.getAmount() * 0.75F);
        }
    }
    private StormProtection() { }
}
