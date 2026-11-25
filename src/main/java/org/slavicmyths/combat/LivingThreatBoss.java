package org.slavicmyths.combat;
import net.minecraft.world.entity.LivingEntity;
public interface LivingThreatBoss {
 boolean canPursue(LivingEntity e);
 boolean validThreatTarget(LivingEntity e);
 boolean acceptThreatTarget(LivingEntity e);
 boolean threatAttackLocked();
}
