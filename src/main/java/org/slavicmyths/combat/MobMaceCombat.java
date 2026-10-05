package org.slavicmyths.combat;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** NPC weapon knockback retained independently of the removed HP overlay. */
@EventBusSubscriber(modid = "slavicmyths")
public final class MobMaceCombat {
    @SubscribeEvent public static void damage(LivingDamageEvent.Pre event) {
        var victim = event.getEntity();
        if (victim.level().isClientSide || event.getNewDamage() <= 0 || event.getSource().is(DamageTypeTags.IS_PROJECTILE)) return;
        if (event.getSource().getEntity() instanceof Mob attacker && attacker.getMainHandItem().getItem() instanceof MaceItem item)
            victim.knockback(item.push, attacker.getX() - victim.getX(), attacker.getZ() - victim.getZ());
    }
    private MobMaceCombat() { }
}
