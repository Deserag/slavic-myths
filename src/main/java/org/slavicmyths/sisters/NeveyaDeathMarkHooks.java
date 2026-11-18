package org.slavicmyths.sisters;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.registry.ModEffects;

/** Server-authoritative death-mark threshold, including WITHER, fall and external damage. */
@EventBusSubscriber(modid="slavicmyths")
public final class NeveyaDeathMarkHooks{
    private NeveyaDeathMarkHooks(){}
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        if(!player.isAlive()||player.isCreative()||player.isSpectator())return;
        if(player.hasEffect(ModEffects.DEATH_MARK)&&player.getHealth()>0&&player.getHealth()<4.0F)
            player.kill();
    }
}
