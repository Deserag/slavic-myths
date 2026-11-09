package org.slavicmyths.ships.mixin;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.slavicmyths.ships.ShipDynamicCollision;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Integrates moving ship blocks into vanilla swept entity collision queries. */
@Mixin(Entity.class)
public abstract class MovingShipCollisionMixin {
    @Inject(method = "collectColliders", at = @At("RETURN"), cancellable = true)
    private static void slavicmyths$movingHullCollision(Entity entity, Level level,
            List<VoxelShape> collisions, AABB query,
            CallbackInfoReturnable<List<VoxelShape>> callback) {
        if (entity == null || !ShipDynamicCollision.hasNearbyMovingShip(entity, level, query))
            return;
        List<VoxelShape> combined = new ArrayList<>(callback.getReturnValue());
        int previous = combined.size();
        ShipDynamicCollision.collect(entity, level, query, combined);
        if (combined.size() != previous) callback.setReturnValue(combined);
    }
}
