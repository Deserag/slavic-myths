package org.slavicmyths.combat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** A recent legal attacker takes priority over ambient prey searches. */
public final class BossRetaliation {
    public static boolean select(Mob boss, double range) {
        LivingEntity attacker = boss.getLastHurtByMob();
        if (attacker == null || attacker instanceof Player || !attacker.isAlive()
                || attacker.isRemoved() || attacker.level() != boss.level()
                || boss.tickCount - boss.getLastHurtByMobTimestamp() > 200
                || boss.distanceToSqr(attacker) > range * range
                || !CombatTargeting.canAttack(boss, attacker, CombatTargeting.Context.ACQUIRE)) return false;
        // Keep the victim of a telegraphed move until that move completes.
        if (!BossThreat.attacking(boss) || boss.getTarget() == null || !boss.getTarget().isAlive())
            boss.setTarget(attacker);
        return true;
    }
    public static boolean recent(Mob boss, LivingEntity target) {
        return target != null && target == boss.getLastHurtByMob() && target.isAlive()
            && !target.isRemoved() && target.level() == boss.level()
            && boss.tickCount - boss.getLastHurtByMobTimestamp() <= 200
            && CombatTargeting.canAttack(boss,target,CombatTargeting.Context.ACQUIRE);
    }
    private BossRetaliation() {}
}