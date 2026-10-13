package org.slavicmyths.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModItems;

/** Add the discharge to the accepted melee hit, so hurt invulnerability cannot swallow it. */
@net.neoforged.fml.common.EventBusSubscriber(modid = SlavicMyths.MOD_ID)
public final class ThunderSpearCombat {
    @SubscribeEvent public static void onHurt(LivingIncomingDamageEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel) || event.getAmount() <= 0
                || !(event.getSource().getDirectEntity() instanceof Player)
                || !"player".equals(event.getSource().getMsgId())) return;
        Player player = (Player) event.getSource().getDirectEntity();
        if (player.getMainHandItem().getItem() != ModItems.THUNDER_SPEAR.get()
                || player.getCooldowns().isOnCooldown(ModItems.THUNDER_SPEAR.get())) return;
        event.setAmount(event.getAmount() + 2.0F);
        player.getCooldowns().addCooldown(ModItems.THUNDER_SPEAR.get(), 120);
        ServerLevel world = (ServerLevel) player.level();
        world.sendParticles(ParticleTypes.ENCHANT, event.getEntity().getX(),
                event.getEntity().getY() + 0.8, event.getEntity().getZ(), 10, 0.2, 0.3, 0.2, 0.02);
        world.playSound(null, event.getEntity().blockPosition(), SoundEvents.TRIDENT_THUNDER.value(),
                SoundSource.PLAYERS, 0.25F, 1.6F);
    }
    private ThunderSpearCombat() { }
}
