package org.slavicmyths.posad.boss;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slavicmyths.item.FolkAccessoryItem;
import org.slavicmyths.registry.ModItems;

/** First physical hit crossing <=35% HP activates Resistance I for 5s; 60s server cooldown. */
@EventBusSubscriber(modid="slavicmyths")
public final class BaukCharmEffect {
    private static final String READY = "SlavicBaukCharmNext";
    @SubscribeEvent public static void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getNewDamage() <= 0F || !player.isAlive()) return;
        if (!(event.getSource().getEntity() instanceof net.minecraft.world.entity.LivingEntity) && !(event.getSource().getDirectEntity() instanceof net.minecraft.world.entity.projectile.Projectile)) return;
        if (FolkAccessoryItem.equipped(player, ModItems.BAUK_HIDE_CHARM_ITEM.get()).isEmpty()) return;
        if (player.getHealth() > player.getMaxHealth() * .35F) return;
        long now = player.serverLevel().getGameTime();
        if (now < player.getPersistentData().getLong(READY)) return;
        player.getPersistentData().putLong(READY, now + 1200L);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 0));
    }
    private BaukCharmEffect() {}
}
