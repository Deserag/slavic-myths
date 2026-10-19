package org.slavicmyths.morozko;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Player weapon targeting, separate from the mirror's autonomous hostile-only AI. */
public final class FrostStaffTargeting {
    public static boolean canHit(LivingEntity target,LivingEntity caster) {
        if(target==caster||!target.isAlive()||target.isSpectator())return false;
        // Keep the normal team/PvP contract. Actual hurt still enforces server PvP,
        // invulnerability and the target's own damage rules.
        return !(target instanceof Player other)||caster instanceof Player player&&player.canHarmPlayer(other);
    }

    public static boolean inStream(LivingEntity caster,LivingEntity target) {
        return caster.hasLineOfSight(target)&&intersectsStream(caster.getEyePosition(),caster.getLookAngle(),target.getBoundingBox(),10,12.5);
    }

    public static boolean intersectsStream(Vec3 eye,Vec3 look,AABB box,double range,double halfAngle) {
        if(FrostWorld.bboxDistance(eye,box)>range*range)return false;
        // Aiming at a head/edge at close range must not miss because its centre
        // lies below the narrow cone. Preserve the existing cone spread too.
        return box.contains(eye)||box.clip(eye,eye.add(look.scale(range))).isPresent()
            ||look.dot(box.getCenter().subtract(eye).normalize())>=Math.cos(Math.toRadians(halfAngle));
    }
    private FrostStaffTargeting(){}
}
