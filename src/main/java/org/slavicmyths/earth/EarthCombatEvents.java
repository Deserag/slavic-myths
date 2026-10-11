package org.slavicmyths.earth;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
@EventBusSubscriber(modid="slavicmyths")
public final class EarthCombatEvents {
    @SubscribeEvent public static void pre(LivingDamageEvent.Pre e){if(e.getEntity() instanceof EarthSpirit b)e.setNewDamage(e.getNewDamage()*b.armorMultiplier(e.getSource()));}
    @SubscribeEvent public static void post(LivingDamageEvent.Post e){if(e.getEntity() instanceof EarthSpirit b)b.registeredHit(e.getSource(),e.getNewDamage());if(e.getSource().getEntity() instanceof EarthSpirit b&&e.getEntity() instanceof net.minecraft.server.level.ServerPlayer player){var resistance=player.getEffect(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE);var text=String.format(java.util.Locale.ROOT,"source=%s raw=%.3f actualHP=%.3f armor=%d toughness=%.1f difficulty=%s resistance=%d armorReduction=%.3f enchantmentReduction=%.3f absorption=%.3f",e.getSource().getMsgId(),e.getOriginalDamage(),e.getNewDamage(),player.getArmorValue(),player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR_TOUGHNESS),player.level().getDifficulty(),resistance==null?0:resistance.getAmplifier()+1,e.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ARMOR),e.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ENCHANTMENTS),e.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION));b.lastDamageReport=text;}}
    private EarthCombatEvents(){}
}
