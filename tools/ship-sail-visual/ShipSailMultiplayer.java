package org.slavicmyths.shipsailvisual;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.FirstShipNetwork;

/** Two actual clients over loopback TCP, in a GitHub-hosted isolated display. */
@EventBusSubscriber(modid = "slavicmyths_shipsailvisual", value = Dist.CLIENT)
public final class ShipSailMultiplayer {
    private static final String ROLE = System.getProperty("shipsailvisual.multiplayer", "");
    private static final Path OUT = Path.of(System.getProperty("shipsailvisual.output",
            "build/ship-sail-visual/frames")).getParent().resolve("multiplayer");
    private static final int PORT = 25772;
    private static boolean opened, done, started, chestClicked, doorClicked;
    private static int ticks, guestTicks, walkTicks;
    private static long startedAt;
    private static FirstShipEntity ship;
    private static BlockPos door;
    private static boolean guestChest, guestDoor, guestWalk, guestJump;
    private static double guestLocalZ;

    private static boolean marker(String name) { return Files.exists(OUT.resolve(name)); }
    private static void mark(String name) throws Exception {
        Files.writeString(OUT.resolve(name), "true\n");
        System.out.println("SHIP_MP " + ROLE + " " + name);
    }

    private static void finish(Minecraft mc, Throwable error) {
        if (done) return;
        done = true;
        try {
            Files.createDirectories(OUT);
            JsonObject result = new JsonObject();
            result.addProperty("role", ROLE);
            result.addProperty("status", error == null ? "PASS_TWO_REAL_CLIENTS" : "FAIL");
            result.addProperty("error", error == null ? "" : error.toString());
            result.addProperty("guest_chest_gui", guestChest);
            result.addProperty("guest_door_toggle", guestDoor);
            result.addProperty("guest_walk_on_moving_deck", guestWalk);
            result.addProperty("guest_jump_on_moving_deck", guestJump);
            result.addProperty("ship_speed", ship == null ? 0D : ship.serverMovingSpeed());
            Files.writeString(OUT.resolve(ROLE + ".json"), result.toString());
            if (error != null) error.printStackTrace();
        } catch (Exception writing) { writing.printStackTrace(); }
        mc.stop();
    }

    private static FirstShipEntity clientShip(Minecraft mc) {
        if (mc.player == null) return null;
        return mc.level.getEntitiesOfClass(FirstShipEntity.class,
                mc.player.getBoundingBox().inflate(80D, 60D, 80D)).stream()
                .filter(FirstShipEntity::isMoving).findFirst().orElse(null);
    }

    private static void aim(Minecraft mc, FirstShipEntity vessel, BlockPos local) {
        FirstShipHull.Point point = FirstShipHull.toWorld(local.getX(), local.getZ(),
                vessel.getX(), vessel.getZ(), vessel.shipYaw());
        Vec3 delta = new Vec3(point.x(), vessel.getY() + local.getY() + .5D,
                point.z()).subtract(mc.player.getEyePosition());
        float yaw = (float)Math.toDegrees(Math.atan2(-delta.x, delta.z));
        float pitch = (float)-Math.toDegrees(Math.atan2(delta.y,
                Math.hypot(delta.x, delta.z)));
        mc.player.setYRot(yaw);
        mc.player.setYHeadRot(yaw);
        mc.player.setXRot(pitch);
    }

    private static void teleport(ServerPlayer player, FirstShipEntity vessel,
            BlockPos local, double back) {
        FirstShipHull.Point target = FirstShipHull.toWorld(local.getX(), local.getZ(),
                vessel.getX(), vessel.getZ(), vessel.shipYaw());
        FirstShipHull.Point stand = FirstShipHull.toWorld(local.getX(),
                local.getZ() + back, vessel.getX(), vessel.getZ(), vessel.shipYaw());
        double dx = target.x() - stand.x(), dz = target.z() - stand.z();
        double dy = vessel.getY() + local.getY() + .5D
                - (vessel.getY() + local.getY()) - player.getEyeHeight();
        player.teleportTo(player.serverLevel(), stand.x(), vessel.getY() + local.getY(),
                stand.z(), (float)Math.toDegrees(Math.atan2(-dx, dz)),
                (float)-Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))));
    }

    private static void host(Minecraft mc) throws Exception {
        var server = mc.getSingleplayerServer();
        if (server == null || mc.player == null) return;
        if (!started) {
            started = true;
            server.execute(() -> {
                try {
                    ServerPlayer captain = server.getPlayerList().getPlayers().getFirst();
                    ship = captain.serverLevel().getEntitiesOfClass(FirstShipEntity.class,
                            captain.getBoundingBox().inflate(120D)).stream()
                            .filter(FirstShipEntity::isMoving).findFirst().orElseThrow();
                    door = BlockPos.of(JsonParser.parseString(Files.readString(
                            OUT.getParent().resolve("frames/checkpoint.json")))
                            .getAsJsonObject().get("door").getAsLong());
                    captain.closeContainer();
                    if (!captain.startRiding(ship, true))
                        throw new AssertionError("Captain could not mount moving ship");
                    server.setUsesAuthentication(false);
                    server.getConnection().startTcpServerListener(
                            InetAddress.getLoopbackAddress(), PORT);
                    mark("listening.txt");
                } catch (Throwable ex) { mc.execute(() -> finish(mc, ex)); }
            });
        }
        ticks++;
        if (ship == null || ticks < 20) return;
        server.execute(() -> {
            ServerPlayer captain = server.getPlayerList().getPlayers().stream()
                    .filter(p -> p.getGameProfile().getName().equals("Dev"))
                    .findFirst().orElse(null);
            if (captain != null) ship.helmStep(captain,
                    marker("guest-positioned.txt") ? FirstShipNetwork.FORWARD
                            : FirstShipNetwork.HELM_IDLE);
        });
        if (server.getPlayerList().getPlayerCount() < 2) return;
        if (!marker("guest-positioned.txt")) {
            server.execute(() -> {
                try {
                    ServerPlayer guest = server.getPlayerList().getPlayers().stream()
                            .filter(p -> p.getGameProfile().getName().equals("ShipQASecond"))
                            .findFirst().orElseThrow();
                    guest.setGameMode(GameType.CREATIVE);
                    guest.getAbilities().flying = false;
                    guest.onUpdateAbilities();
                    teleport(guest, ship, new BlockPos(2, 7, 0), 2D);
                    mark("guest-positioned.txt");
                } catch (Throwable ex) { mc.execute(() -> finish(mc, ex)); }
            });
            return;
        }
        if (marker("chest-client-opened.txt") && !marker("door-ready.txt")) {
            server.execute(() -> {
                try {
                    ServerPlayer guest = server.getPlayerList().getPlayers().stream()
                            .filter(p -> p.getGameProfile().getName().equals("ShipQASecond"))
                            .findFirst().orElseThrow();
                    if (!(guest.containerMenu instanceof ChestMenu))
                        throw new AssertionError("Guest chest GUI did not reach server");
                    guestChest = true;
                    guest.closeContainer();
                    teleport(guest, ship, door, 2D);
                    mark("door-ready.txt");
                } catch (Throwable ex) { mc.execute(() -> finish(mc, ex)); }
            });
        }
        if (marker("door-client-clicked.txt") && !marker("walk-ready.txt")) {
            var state = ship.serverMovingPalette().get(door);
            if (state != null && state.getBlock() instanceof DoorBlock
                    && state.getValue(DoorBlock.OPEN)) {
                guestDoor = true;
                server.execute(() -> {
                    try {
                        ServerPlayer guest = server.getPlayerList().getPlayers().stream()
                                .filter(p -> p.getGameProfile().getName()
                                        .equals("ShipQASecond")).findFirst().orElseThrow();
                        teleport(guest, ship, new BlockPos(3, 12, 0), 0D);
                        guestLocalZ = FirstShipHull.toLocal(guest.getX(), guest.getZ(),
                                ship.getX(), ship.getZ(), ship.shipYaw()).z();
                        mark("walk-ready.txt");
                    } catch (Throwable ex) { mc.execute(() -> finish(mc, ex)); }
                });
            }
        }
        if (marker("guest-walked.txt") && !marker("host-walk-checked.txt")) {
            server.execute(() -> {
                try {
                    ServerPlayer guest = server.getPlayerList().getPlayers().stream()
                            .filter(p -> p.getGameProfile().getName()
                                    .equals("ShipQASecond")).findFirst().orElseThrow();
                    FirstShipHull.Point local = FirstShipHull.toLocal(guest.getX(),
                            guest.getZ(), ship.getX(), ship.getZ(), ship.shipYaw());
                    guestWalk = ship.serverMovingSpeed() > .03D
                            && Math.abs(local.z() - guestLocalZ) > .25D
                            && Math.abs(local.x()) < 8D && Math.abs(local.z()) < 20D;
                    guestJump = marker("guest-jumped.txt");
                    mark("host-walk-checked.txt");
                } catch (Throwable ex) { mc.execute(() -> finish(mc, ex)); }
            });
        }
        if (marker("host-walk-checked.txt")) {
            if (!guestChest || !guestDoor || !guestWalk || !guestJump
                    || ship.serverMovingSpeed() <= .03D)
                throw new AssertionError("Two-client moving ship failed: chest=" + guestChest
                        + " door=" + guestDoor + " walk=" + guestWalk + " jump="
                        + guestJump + " speed=" + ship.serverMovingSpeed());
            finish(mc, null);
        }
    }

    private static void guest(Minecraft mc) throws Exception {
        if (mc.player == null || mc.level == null) return;
        guestTicks++;
        if (mc.getSingleplayerServer() != null)
            throw new AssertionError("Guest unexpectedly has an integrated server");
        if (!mc.player.getGameProfile().getName().equals("ShipQASecond"))
            throw new AssertionError("Guest has wrong profile");
        FirstShipEntity visible = clientShip(mc);
        if (visible == null || !visible.isMoving() || !marker("guest-positioned.txt"))
            return;
        if (!chestClicked && guestTicks > 50) {
            aim(mc, visible, new BlockPos(2, 7, 0));
            if (guestTicks > 75) {
                chestClicked = true;
                net.minecraft.client.KeyMapping.click(mc.options.keyUse.getKey());
            }
            return;
        }
        if (chestClicked && !marker("chest-client-opened.txt")) {
            if (mc.player.containerMenu instanceof ChestMenu) {
                guestChest = true;
                mark("chest-client-opened.txt");
            }
            return;
        }
        if (marker("door-ready.txt") && !doorClicked) {
            aim(mc, visible, BlockPos.of(JsonParser.parseString(Files.readString(
                    OUT.getParent().resolve("frames/checkpoint.json")))
                    .getAsJsonObject().get("door").getAsLong()));
            if (guestTicks > 130 && mc.screen == null) {
                doorClicked = true;
                net.minecraft.client.KeyMapping.click(mc.options.keyUse.getKey());
                mark("door-client-clicked.txt");
            }
            return;
        }
        if (marker("walk-ready.txt") && !marker("guest-walked.txt")) {
            walkTicks++;
            mc.options.keyUp.setDown(true);
            if (walkTicks >= 20 && walkTicks <= 25)
                mc.options.keyJump.setDown(true);
            else mc.options.keyJump.setDown(false);
            if (walkTicks > 25 && !mc.player.onGround()
                    && !marker("guest-jumped.txt")) mark("guest-jumped.txt");
            if (walkTicks > 85) {
                mc.options.keyUp.setDown(false);
                mc.options.keyJump.setDown(false);
                mark("guest-walked.txt");
            }
        }
        if (marker("host-walk-checked.txt")) {
            guestWalk = true;
            guestJump = true;
            finish(mc, null);
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post ignored) {
        if (ROLE.isEmpty() || done) return;
        Minecraft mc = Minecraft.getInstance();
        try {
            if (!"true".equals(System.getenv("GITHUB_ACTIONS"))
                    || !"github-hosted".equals(System.getenv("RUNNER_ENVIRONMENT")))
                throw new IllegalStateException("Two-client QA requires isolated runner");
            if (startedAt == 0L) startedAt = System.nanoTime();
            if (System.nanoTime() - startedAt > 360_000_000_000L)
                throw new AssertionError("Two-client ship test timed out");
            if (mc.screen != null && mc.screen.getClass().getSimpleName()
                    .equals("AccessibilityOnboardingScreen")) {
                mc.setScreen(new TitleScreen());
                return;
            }
            if (!opened && mc.screen instanceof TitleScreen) {
                opened = true;
                Files.createDirectories(OUT);
                mc.options.pauseOnLostFocus = false;
                mc.options.renderDistance().set(6);
                mc.options.simulationDistance().set(6);
                mc.options.tutorialStep = net.minecraft.client.tutorial.TutorialSteps.NONE;
                if (ROLE.equals("host")) {
                    JsonObject state = JsonParser.parseString(Files.readString(
                            OUT.getParent().resolve("frames/checkpoint.json")))
                            .getAsJsonObject();
                    mc.createWorldOpenFlows().openWorld(state.get("world").getAsString(),
                            () -> { throw new AssertionError("Ship MP world failed to load"); });
                } else {
                    ConnectScreen.startConnecting(new TitleScreen(), mc,
                            ServerAddress.parseString("[::1]:" + PORT),
                            new ServerData("Ship QA loopback", "[::1]:" + PORT,
                                    ServerData.Type.OTHER), false, null);
                }
                return;
            }
            if (ROLE.equals("host")) host(mc);
            else if (ROLE.equals("guest")) guest(mc);
            else throw new IllegalArgumentException("Invalid MP role " + ROLE);
        } catch (Throwable failure) { finish(mc, failure); }
    }
}
