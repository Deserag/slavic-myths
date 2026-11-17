package org.slavicmyths.ships.client;

import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.FirstShipNetwork;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipModularBlockHull;
import org.slavicmyths.ships.ShipMovingCargoTarget;
import org.slavicmyths.ships.RaftContent;

/**
 * Helm and hull consist of normal Minecraft blocks (not entity hitboxes).
 * This input adapter chooses the nearest known ship with server-side validation.
 */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class FirstShipInteractionClient {
    @SubscribeEvent
    public static void onUse(InputEvent.InteractionKeyMappingTriggered event) {
        if (event.isAttack()) {
            breakMovingBlock(event);
            return;
        }
        if (!event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null || mc.player.isPassenger()) return;
        if (useMovingDoor(mc, event)) return;
        boolean key = mc.player.getMainHandItem().is(RaftContent.SHIPYARD_KEY.get())
                || mc.player.getOffhandItem().is(RaftContent.SHIPYARD_KEY.get());
        if (mc.player.isShiftKeyDown() && !key) {
            placeOnMovingShip(mc, event);
            return;
        }

        Vec3 eye = mc.player.getEyePosition(), direction = mc.player.getLookAngle();
        FirstShipEntity selected = null;
        int action = 0;
        double nearest = Double.POSITIVE_INFINITY;
        // The old 12-block scan never reached the helm of a 25x45 hull.
        // Candidate ships are still independently validated by helm reach and
        // the authoritative server before a command can mount the player.
        for (FirstShipEntity ship : mc.level.getEntitiesOfClass(
                FirstShipEntity.class, mc.player.getBoundingBox().inflate(42D, 26D, 42D))) {
            if (ship.isRemoved() || (!ship.isNativeHull() && !(mc.player.isShiftKeyDown() && key))) continue;
            FirstShipHull.Point at = FirstShipHull.toLocal(mc.player.getX(), mc.player.getZ(),
                    ship.getX(), ship.getZ(), ship.shipYaw());
            if (mc.player.isShiftKeyDown() && key) {
                if (mc.player.distanceToSqr(ship) > 160D) continue;
                for (double step = .2D; step <= 5D; step += .2D) {
                    Vec3 probe = eye.add(direction.scale(step));
                    FirstShipHull.Point hit = FirstShipHull.toLocal(
                            probe.x, probe.z, ship.getX(), ship.getZ(), ship.shipYaw());
                    if (FirstShipHull.withinHull(hit.x(), hit.z(), .05D)
                            && probe.y >= ship.holdY() - .3D && probe.y <= ship.deckY() + 2D
                            && step * step < nearest) {
                        selected = ship; action = FirstShipNetwork.PACK; nearest = step * step;
                        break;
                    }
                }
                continue;
            }
            if (ship.isMoving() && !ship.movingCargoView().isBlank()) {
                FirstShipHull.Point start = FirstShipHull.toLocal(eye.x, eye.z,
                        ship.getX(), ship.getZ(), ship.shipYaw());
                Vec3 endWorld = eye.add(direction.scale(5D));
                FirstShipHull.Point end = FirstShipHull.toLocal(endWorld.x, endWorld.z,
                        ship.getX(), ship.getZ(), ship.shipYaw());
                Vec3 localEye = new Vec3(start.x(), eye.y - ship.getY(), start.z());
                Vec3 localEnd = new Vec3(end.x(), endWorld.y - ship.getY(), end.z());
                String[] cargo = ship.movingCargoView().split(";");
                for (int i = 0; i < cargo.length; i++) {
                    String[] fields = cargo[i].split(",");
                    if (fields.length < 4) continue;
                    try {
                        int x = Integer.parseInt(fields[0]);
                        int y = Integer.parseInt(fields[1]);
                        int z = Integer.parseInt(fields[2]);
                        int cargoKind = Integer.parseInt(fields[3]);
                        // Only the four built-in portable menus are implemented.
                        // Generic saved storage still travels with the hull.
                        if (cargoKind < 1 || cargoKind > 4) continue;
                        var hit = new net.minecraft.world.phys.AABB(x - .5D, y, z - .5D,
                                x + .5D, y + 1D, z + .5D).clip(localEye, localEnd);
                        if (hit.isPresent()) {
                            double distance = hit.get().distanceToSqr(localEye);
                            if (distance < nearest) {
                                nearest = distance;
                                selected = ship;
                                action = FirstShipNetwork.CARGO_BASE + i;
                            }
                        }
                    } catch (NumberFormatException invalid) {
                        // Malformed cosmetic sync data cannot create an action.
                    }
                }
            }
            // Use the same live helm geometry as the server: never infer a
            // modular captain station from the legacy 7x11 hard-coded wheel.
            double helmZ = ship.helmSeatZ(), helmY = ship.getY() + ship.helmSeatHeight();
            if (Math.abs(mc.player.getY() - helmY) < 1.3D
                    && Math.abs(at.x()) < 1.8D
                    && Math.abs(at.z() - helmZ) < 1.8D) {
                FirstShipHull.Point wheel = FirstShipHull.toWorld(0D, ship.helmWheelZ(),
                        ship.getX(), ship.getZ(), ship.shipYaw());
                Vec3 ring = new Vec3(wheel.x(), helmY + 1.78D, wheel.z()).subtract(eye);
                Vec3 post = new Vec3(wheel.x(), helmY + .58D, wheel.z()).subtract(eye);
                boolean aimsRing = ring.lengthSqr() > .09D && ring.lengthSqr() < 25D
                        && ring.normalize().dot(direction) > .30D;
                boolean aimsPost = post.lengthSqr() > .09D && post.lengthSqr() < 25D
                        && post.normalize().dot(direction) > .30D;
                double distance = Math.min(ring.lengthSqr(), post.lengthSqr());
                if ((aimsRing || aimsPost) && distance < nearest) {
                    selected = ship; action = FirstShipNetwork.HELM; nearest = distance;
                }
            }
            if (action == FirstShipNetwork.HELM) continue;
            if (mc.player.getY() >= ship.deckY() - 1.1D) continue;
            double z = FirstShipHull.clamp(at.z(), -4.7D, 4.7D);
            double half = FirstShipHull.halfBeam(z);
            if (Math.abs(at.z()) > 6.25D || Math.abs(at.x()) > half + 1.7D
                    || (Math.abs(at.x()) < half - 1.1D && Math.abs(at.z()) < 4.8D)) continue;
            for (double step = .4D; step <= 5D; step += .25D) {
                Vec3 probe = eye.add(direction.scale(step));
                if (probe.y < ship.getY() || probe.y > ship.deckY() + .2D) continue;
                FirstShipHull.Point hit = FirstShipHull.toLocal(
                        probe.x, probe.z, ship.getX(), ship.getZ(), ship.shipYaw());
                if (FirstShipHull.withinHull(hit.x(), hit.z(), .1D) && step * step < nearest) {
                    selected = ship; action = FirstShipNetwork.BOARD; nearest = step * step;
                    break;
                }
            }
        }
        if (selected == null) {
            if (!key) placeOnMovingShip(mc, event);
            return;
        }
        PacketDistributor.sendToServer(new FirstShipNetwork.Command(selected.getId(), action));
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onMineTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && !mc.player.getAbilities().instabuild
                && mc.options.keyAttack.isDown()) breakMovingBlock(null);
    }

    private static void breakMovingBlock(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.screen != null
                || mc.player.isPassenger()) return;
        Vec3 eye = mc.player.getEyePosition();
        Vec3 end = eye.add(mc.player.getLookAngle().scale(5D));
        for (FirstShipEntity ship : mc.level.getEntitiesOfClass(
                FirstShipEntity.class, mc.player.getBoundingBox().inflate(46D, 50D, 46D))) {
            ShipBuildConfiguration config = ship.modularConfiguration();
            if (!ship.isMoving() || config == null) continue;
            Map<BlockPos, BlockState> palette = ShipModularBlockHull.localVisualPalette(
                    config, ShipModularBlockHull.decodeVisibleCargo(ship.movingCargoView()));
            var hit = ShipMovingCargoTarget.hit(ship, eye, end, palette);
            if (hit == null) continue;
            PacketDistributor.sendToServer(new FirstShipNetwork.BreakMovingBlock(
                    ship.getId(), hit.cell().getX(), hit.cell().getY(), hit.cell().getZ()));
            if (event != null) {
                event.setSwingHand(false);
                event.setCanceled(true);
            }
            return;
        }
    }

    private static boolean useMovingDoor(Minecraft mc,
            InputEvent.InteractionKeyMappingTriggered event) {
        Vec3 eye = mc.player.getEyePosition();
        Vec3 end = eye.add(mc.player.getLookAngle().scale(5D));
        for (FirstShipEntity ship : mc.level.getEntitiesOfClass(
                FirstShipEntity.class, mc.player.getBoundingBox().inflate(46D, 50D, 46D))) {
            ShipBuildConfiguration config = ship.modularConfiguration();
            if (!ship.isMoving() || config == null) continue;
            Map<BlockPos, BlockState> palette = ShipModularBlockHull.localVisualPalette(
                    config, ShipModularBlockHull.decodeVisibleCargo(ship.movingCargoView()));
            var hit = ShipMovingCargoTarget.hit(ship, eye, end, palette);
            if (hit == null || !(palette.get(hit.cell()).getBlock()
                    instanceof net.minecraft.world.level.block.DoorBlock)) continue;
            PacketDistributor.sendToServer(new FirstShipNetwork.UseMovingDoor(
                    ship.getId(), hit.cell().getX(), hit.cell().getY(), hit.cell().getZ()));
            event.setSwingHand(false);
            event.setCanceled(true);
            return true;
        }
        return false;
    }

    /**
     * A detached hull no longer has vanilla world-block hitboxes.
     * Select a local palette face and let the server authorize item placement.
     */
    private static boolean placeOnMovingShip(Minecraft mc,
            InputEvent.InteractionKeyMappingTriggered event) {
        InteractionHand hand = event.getHand();
        ItemStack stack = mc.player.getItemInHand(hand);
        if (!(stack.getItem() instanceof BlockItem)) {
            if (hand != InteractionHand.MAIN_HAND
                    || !(mc.player.getOffhandItem().getItem() instanceof BlockItem))
                return false;
            hand = InteractionHand.OFF_HAND;
        }
        Vec3 eye = mc.player.getEyePosition();
        Vec3 end = eye.add(mc.player.getLookAngle().scale(5D));
        FirstShipEntity nearestShip = null;
        BlockPos selectedCell = null;
        double nearestDistance = Double.POSITIVE_INFINITY;
        for (FirstShipEntity ship : mc.level.getEntitiesOfClass(
                FirstShipEntity.class, mc.player.getBoundingBox().inflate(46D, 50D, 46D))) {
            ShipBuildConfiguration config = ship.modularConfiguration();
            if (ship.isRemoved() || !ship.isMoving() || config == null) continue;
            ListTag cargo = ShipModularBlockHull.decodeVisibleCargo(
                    ship.movingCargoView());
            Map<BlockPos, BlockState> palette =
                    ShipModularBlockHull.localVisualPalette(config, cargo);
            BlockPos cell = ShipMovingCargoTarget.target(ship, eye, end, palette);
            if (cell == null) continue;
            FirstShipHull.Point world = FirstShipHull.toWorld(
                    cell.getX(), cell.getZ(), ship.getX(), ship.getZ(), ship.shipYaw());
            double distance = eye.distanceToSqr(new Vec3(
                    world.x(), ship.getY() + cell.getY() + .5D, world.z()));
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestShip = ship;
                selectedCell = cell;
            }
        }
        if (nearestShip == null) return false;
        if (selectedCell != null
                && ShipModularBlockHull.movingCargoCell(
                        nearestShip.modularConfiguration(), selectedCell)) {
            PacketDistributor.sendToServer(new FirstShipNetwork.PlaceCargo(
                    nearestShip.getId(), selectedCell.getX(), selectedCell.getY(),
                    selectedCell.getZ(), hand == InteractionHand.MAIN_HAND ? 0 : 1));
        }
        event.setSwingHand(false);
        event.setCanceled(true);
        return true;
    }

    private FirstShipInteractionClient() {}
}
