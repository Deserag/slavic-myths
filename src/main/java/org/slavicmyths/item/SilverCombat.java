package org.slavicmyths.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModItems;

@Mod.EventBusSubscriber(modid = SlavicMyths.MOD_ID)
public final class SilverCombat {
    private static final net.minecraftforge.common.Tags.IOptionalNamedTag<net.minecraft.entity.EntityType<?>> VULNERABLE =
            EntityTypeTags.createOptional(new ResourceLocation(SlavicMyths.MOD_ID, "silver_vulnerable"));
    @SubscribeEvent public static void onHurt(LivingHurtEvent event) {
        if (event.getEntityLiving().level.isClientSide || !(event.getSource().getEntity() instanceof PlayerEntity)) return;
        PlayerEntity attacker = (PlayerEntity) event.getSource().getEntity();
        if (attacker.getOffhandItem().getItem() == ModItems.HUNTER_CHARM.get()
                && event.getEntityLiving() instanceof net.minecraft.entity.passive.AnimalEntity) {
            event.setAmount(event.getAmount() + 0.5F);
        }
        if (!event.getEntityLiving().getType().is(VULNERABLE)) return;
        net.minecraft.item.Item held = attacker.getMainHandItem().getItem();
        if (held == ModItems.SILVER_SWORD.get() || held == ModItems.SILVER_DAGGER.get()
                || held == ModItems.SILVER_SPEAR.get() || held == ModItems.RITUAL_KNIFE.get()) {
            event.setAmount(event.getAmount() + 1.5F);
        }
    }
    private SilverCombat() { }
}
