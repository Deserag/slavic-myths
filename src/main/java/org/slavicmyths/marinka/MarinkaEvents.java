package org.slavicmyths.marinka;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
/** Armor is still the engine's responsibility. No final damage floors or client claims. */
@EventBusSubscriber(modid="slavicmyths")
public final class MarinkaEvents {
 @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getSource().getEntity() instanceof MarinkaEntity)p.getPersistentData().putFloat("WitchHpBefore",p.getHealth());}
 @SubscribeEvent public static void pre(LivingDamageEvent.Pre e){if(e.getEntity().level().isClientSide)return;if(e.getEntity() instanceof DobrynyaEntity h)e.setNewDamage(e.getNewDamage()*h.mitigation(e.getSource()));if(e.getEntity() instanceof MarinkaEntity m)e.setNewDamage(e.getNewDamage()*m.mitigation());if(e.getSource().getEntity() instanceof MarinkaEntity m&&m.falseBody())e.setNewDamage(e.getNewDamage()*.45F);}
 @SubscribeEvent public static void post(LivingDamageEvent.Post e){if(e.getEntity() instanceof MarinkaEntity m)m.registeredHit(e.getSource(),e.getNewDamage());if(e.getEntity() instanceof ServerPlayer p&&e.getSource().getEntity() instanceof MarinkaEntity m){var r=m.root();if(r!=null)r.contribution(p);if(m.debug)org.slf4j.LoggerFactory.getLogger("Boss172").info("[Boss172] target={} hit={} armor={} toughness={} hpBefore={} hpAfter={} raw={} final={} effects={}",p.getUUID(),e.getSource().getMsgId(),p.getArmorValue(),p.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS),p.getPersistentData().getFloat("WitchHpBefore"),p.getHealth(),e.getOriginalDamage(),e.getNewDamage(),p.getActiveEffects());}}
 private MarinkaEvents(){}
}
