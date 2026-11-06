package org.slavicmyths.sisters;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.registry.ModEffects;

/** Short petrification at stage V; stage IV+ amplifies post-armor health damage. */
@EventBusSubscriber(modid="slavicmyths")
public final class ViyCurseHooks {
    private ViyCurseHooks(){}
    @SubscribeEvent public static void onPlayer(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p)||!p.isAlive())return;
        if(p.getPersistentData().getLong("SlavicViyPetrifiedUntil")>p.level().getGameTime()
            &&p.hasEffect(ModEffects.VIY_GAZE)){
            p.setDeltaMovement(0,p.getDeltaMovement().y,0);
            p.hurtMarked=true;
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,5,9,false,false,false));
        }
    }
    @SubscribeEvent public static void onDamage(LivingDamageEvent.Pre event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        var effect=p.getEffect(ModEffects.VIY_GAZE);
        if(effect!=null&&effect.getAmplifier()>=3)
            event.setNewDamage(event.getNewDamage()*1.25F);
    }
}
