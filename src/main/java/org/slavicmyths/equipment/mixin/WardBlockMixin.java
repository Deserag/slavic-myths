package org.slavicmyths.equipment.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** The final accepted block, after all event subscribers have finished. */
@Mixin(LivingEntity.class)
public abstract class WardBlockMixin {
    @Redirect(method="hurt",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/common/damagesource/DamageContainer;setBlockedDamage(Lnet/neoforged/neoforge/event/entity/living/LivingShieldBlockEvent;)V"))
    private void slavicmyths$blocked(DamageContainer container,LivingShieldBlockEvent event){container.setBlockedDamage(event);org.slavicmyths.rpg.AdvancedRunes.blocked((LivingEntity)(Object)this,event);org.slavicmyths.rewards.RewardEvents.blocked((LivingEntity)(Object)this,event);org.slavicmyths.equipment.UniqueEquipmentEvents.reflected((LivingEntity)(Object)this,event);}
}
