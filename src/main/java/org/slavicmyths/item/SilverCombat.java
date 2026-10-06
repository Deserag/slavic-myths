package org.slavicmyths.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModItems;

@net.neoforged.fml.common.EventBusSubscriber(modid = SlavicMyths.MOD_ID)
public final class SilverCombat {
    public static final net.minecraft.tags.TagKey<net.minecraft.world.item.Item> SILVER=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ITEM,ResourceLocation.fromNamespaceAndPath(SlavicMyths.MOD_ID,"silver_weapons"));
    public static boolean silverStrike(net.minecraft.world.damagesource.DamageSource source){return source.getEntity() instanceof net.minecraft.world.entity.LivingEntity&&source.getDirectEntity()==source.getEntity()&&((net.minecraft.world.entity.LivingEntity)source.getEntity()).getMainHandItem().is(SILVER);}
    private static final net.minecraft.tags.TagKey<net.minecraft.world.entity.EntityType<?>> VULNERABLE =
            net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,ResourceLocation.fromNamespaceAndPath(SlavicMyths.MOD_ID, "silver_vulnerable"));
    @SubscribeEvent public static void onHurt(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide || !(event.getSource().getEntity() instanceof Player)) return;
        Player attacker = (Player) event.getSource().getEntity();
        if (!event.getSource().getMsgId().equals("slavic_cut") && attacker.getOffhandItem().getItem() == ModItems.HUNTER_CHARM.get()
                && event.getEntity() instanceof net.minecraft.world.entity.animal.Animal) {
            event.setAmount(event.getAmount() + 0.5F);
        }
        if (!event.getEntity().getType().is(VULNERABLE)) return;
        if (silverStrike(event.getSource())) {
            event.setAmount(event.getAmount() + 1.5F);
        }
    }
    private SilverCombat() { }
}
