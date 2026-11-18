package org.slavicmyths.accessories;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** A one-time anti-lethal Curios ward, with player-bound cooldown to prevent swap abuse. */
@EventBusSubscriber(modid="slavicmyths")
public final class GneteyaAmuletEvents {
    @SubscribeEvent public static void protect(LivingDamageEvent.Pre event) {
        if(!(event.getEntity() instanceof ServerPlayer player)||event.getNewDamage()<player.getHealth()
                ||!player.isAlive()||player.isCreative()||player.isSpectator()
                ||!Accessories.wears(player,"gneteya_amulet"))return;
        long now=player.serverLevel().getGameTime(),until=player.getPersistentData().getLong("GneteyaAmuletReadyAt");
        if(now<until)return;
        player.getPersistentData().putLong("GneteyaAmuletReadyAt",now+1800);
        event.setNewDamage(Math.max(0F,player.getHealth()-1F));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,160,2),player);
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,1),player);
        player.displayClientMessage(Component.literal("Оберег Гнетеи спас от смертельного удара!"),true);
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,player.getX(),player.getY()+1,player.getZ(),45,.65,1,.65,.1);
    }
    private GneteyaAmuletEvents(){}
}
