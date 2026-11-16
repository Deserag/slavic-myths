package org.slavicmyths.client;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.FirstShipNetwork;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipCargoPlacementGuard;
import org.slavicmyths.ships.ShipHullVoxelLayout;
import org.slavicmyths.ships.ShipModularBlockHull;

/**
 * Right-click on the rendered, detached ship while holding an allowed
 * block. Vanilla has no physical block hit at that position, so sample the
 * actual local voxel geometry rather than placing a block in empty water.
 * Only a candidate CELL is sent; all authority remains on the server.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class ShipMovingCargoInput {
    private record Target(FirstShipEntity ship, BlockPos cell, double distance) {}

    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isUseItem()) return;
        Minecraft mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || mc.level == null || mc.screen != null
                || player.isPassenger() || player.isSpectator()
                || !(player.getItemInHand(event.getHand()).getItem() instanceof BlockItem item)
                || ShipCargoPlacementGuard.forbidden(item.getBlock().defaultBlockState()))
            return;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double maxDistance = 5D;
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK)
            maxDistance = Math.min(maxDistance,
                    mc.hitResult.getLocation().distanceTo(eye) - .04D);
        if (maxDistance <= .05D) return;
        Target nearest = null;
        for (FirstShipEntity ship : mc.level.getEntitiesOfClass(
                FirstShipEntity.class, player.getBoundingBox().inflate(43D, 50D, 43D))) {
            if (ship.isRemoved() || !ship.isMoving()) continue;
            ShipBuildConfiguration config = ship.modularConfiguration();
            if (config == null) continue;
            Target candidate = raycast(ship, config, eye, look, maxDistance);
            if (candidate != null && (nearest == null
                    || candidate.distance() < nearest.distance()))
                nearest = candidate;
        }
        if (nearest == null) return;
        BlockPos cell = nearest.cell();
        PacketDistributor.sendToServer(new FirstShipNetwork.PlaceCargo(
                nearest.ship().getId(), cell.getX(), cell.getY(), cell.getZ(),
                event.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? 0 : 1));
        event.setCanceled(true);
        event.setSwingHand(true);
    }

    private static Target raycast(FirstShipEntity ship, ShipBuildConfiguration config,
            Vec3 eye, Vec3 look, double reach) {
        var geometry = ShipHullVoxelLayout.generate(config.structure());
        Set<BlockPos> cargo = new HashSet<>();
        String view = ship.movingCargoView();
        if (!view.isBlank()) for (String token : view.split(";")) {
            String[] parts = token.split(",");
            if (parts.length < 4) continue;
            try {
                cargo.add(new BlockPos(Integer.parseInt(parts[0]),
                        Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
            } catch (NumberFormatException invalid) {
                // Ignore invalid cosmetic cargo entries.
            }
        }
        BlockPos previous = null;
        for (double distance = .04D; distance <= reach; distance += .06D) {
            Vec3 sample = eye.add(look.scale(distance));
            FirstShipHull.Point local = FirstShipHull.toLocal(sample.x, sample.z,
                    ship.getX(), ship.getZ(), ship.getYRot());
            if (!Double.isFinite(local.x()) || !Double.isFinite(local.z()))
                return null;
            BlockPos cell = new BlockPos((int)Math.round(local.x()),
                    (int)Math.floor(sample.y - ship.getY()),
                    (int)Math.round(local.z()));
            var part = geometry.get(new ShipHullVoxelLayout.Cell(
                    cell.getX(), cell.getY(), cell.getZ()));
            boolean solid = part != null && part != ShipHullVoxelLayout.Part.SAIL
                    && part != ShipHullVoxelLayout.Part.GUNPORT_SHUTTER
                    || cargo.contains(cell);
            if (solid) {
                if (previous == null
                        || Math.abs(previous.getX() - cell.getX())
                            + Math.abs(previous.getY() - cell.getY())
                            + Math.abs(previous.getZ() - cell.getZ()) != 1)
                    return null;
                if (!ShipModularBlockHull.movingCargoCell(config, previous))
                    return null;
                return new Target(ship, previous, distance);
            }
            previous = cell;
        }
        return null;
    }

    private ShipMovingCargoInput() {}
}
