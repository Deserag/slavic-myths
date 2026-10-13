package org.slavicmyths.bandit;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/** Explicit wind control with a bounded minimum impulse after native resistance. */
public final class WindImpulse {
    public static final TagKey<EntityType<?>> IMMUNE = TagKey.create(Registries.ENTITY_TYPE,
        ResourceLocation.fromNamespaceAndPath("slavicmyths", "wind_immune"));
    public static void push(LivingEntity victim, Vec3 direction, double strength) {
        if (victim.getType().is(IMMUNE) || strength <= 0) return;
        Vec3 away = direction.multiply(1, 0, 1).normalize();
        if (away.lengthSqr() < .001) return;
        victim.knockback(strength, -away.x, -away.z);
        Vec3 velocity = victim.getDeltaMovement();
        double minimum = Math.min(.5, strength);
        double outward = velocity.dot(away);
        if (outward < minimum) velocity = velocity.add(away.scale(minimum - outward));
        victim.setDeltaMovement(velocity.x, Math.max(velocity.y, .12), velocity.z);
        victim.hasImpulse = true;
        victim.hurtMarked = true;
    }
    private WindImpulse() {}
}
