package org.slavicmyths.shipsailvisual;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.RaftContent;
import org.slavicmyths.ships.FirstShipNetwork;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipModularBlockHull;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.client.FirstShipRenderer;

/**
 * Captures the ACTUAL packaged-mod client framebuffer in a throwaway
 * GitHub-hosted Xvfb single-player world. This is a renderer geometry fixture,
 * not proof of real wind/acceleration, player survival or owner acceptance.
 */
@EventBusSubscriber(modid = "slavicmyths_shipsailvisual", value = Dist.CLIENT)
public final class ShipSailCapture {
    private static final Path OUT = Path.of(
            System.getProperty("shipsailvisual.output", "build/ship-sail-visual/frames"));
    private static final boolean RELOG_RUN = Boolean.getBoolean("shipsailvisual.relog");
    private static final BlockPos ANCHOR = new BlockPos(0, 81, 0);
    private static final int BASIN_MIN_X = -80, BASIN_MAX_X = 80;
    private static final int BASIN_MIN_Z = -75, BASIN_MAX_Z = 115;
    private static final TicketType<Long> MOTION_TICKET = TicketType.create(
            "slavicmyths_ship_507_capture", Long::compare);
    private static final int FRAMES_PER_CONFIG = 12; // 4 views x 3 fill states.
    private static final int EXPECTED_FRAMES = 6 * FRAMES_PER_CONFIG;
    private static final JsonArray FRAMES = new JsonArray();
    private static final JsonArray MOTION_FRAMES = new JsonArray();
    private static Map<BlockPos, BlockState> lastPalette = Map.of();
    private static final Set<BlockPos> historicalCells = new HashSet<>();
    private static volatile int serverResidualBlocks;
    private static FirstShipEntity serverShip;
    private static boolean opened;
    private static boolean finished;
    private static volatile boolean positioned;
    private static volatile int expectedShipId = -1;
    private static int currentConfig = -1;
    private static int scene = -1;
    private static int age = 0;
    private static String loadedSha = "";
    private static boolean motionStarted;
    private static volatile boolean motionReady;
    private static int motionAge;
    private static volatile long motionStartServerTick;
    private static double motionStartZ;
    private static double motionRestartZ;
    private static double motionRestartX;
    private static volatile double serverMotionSpeed;
    private static volatile int clientControlFrames;
    private static volatile int cameraStandId = -1;
    private static ArmorStand cameraStand;
    private static int interactionPhase;
    private static int interactionAge;
    private static BlockPos interactionDoor;
    private static boolean chestOpened;
    private static boolean doorOpened;
    private static boolean plankBroken;
    private static BlockPos interactionPlank;
    private static BlockPos placementBase;
    private static boolean chestPlacedByClient;
    private static String isolatedWorldName;
    private static boolean reloadingWorld;
    private static int relogAge;
    private static boolean relogChestOpened;
    private static boolean relogDoorWorked;
    private static String placementDiagnostic = "not attempted";
    private static String chestClientDiagnostic = "not observed";
    private static volatile String chestServerDiagnostic = "not observed";
    private static String doorClientDiagnostic = "not attempted";
    private static volatile String doorServerDiagnostic = "not observed";

    private static void checkpoint(Minecraft mc) throws Exception {
        JsonObject state = new JsonObject();
        state.addProperty("world", isolatedWorldName);
        state.addProperty("jar_sha256", loadedSha);
        state.addProperty("door", interactionDoor.asLong());
        state.addProperty("chest_opened", chestOpened);
        state.addProperty("door_opened", doorOpened);
        state.addProperty("plank_broken", plankBroken);
        state.addProperty("chest_placed", chestPlacedByClient);
        state.add("frames", FRAMES);
        state.add("motion_frames", MOTION_FRAMES);
        Files.writeString(OUT.resolve("checkpoint.json"),
                new GsonBuilder().setPrettyPrinting().create().toJson(state));
        System.out.println("SHIP_RELOG_CHECKPOINT world=" + isolatedWorldName);
        finished = true;
        mc.stop();
    }

    private static void restoreCheckpoint() throws Exception {
        JsonObject state = JsonParser.parseString(
                Files.readString(OUT.resolve("checkpoint.json"))).getAsJsonObject();
        isolatedWorldName = state.get("world").getAsString();
        loadedSha = state.get("jar_sha256").getAsString();
        interactionDoor = BlockPos.of(state.get("door").getAsLong());
        chestOpened = state.get("chest_opened").getAsBoolean();
        doorOpened = state.get("door_opened").getAsBoolean();
        plankBroken = state.get("plank_broken").getAsBoolean();
        chestPlacedByClient = state.get("chest_placed").getAsBoolean();
        state.getAsJsonArray("frames").forEach(FRAMES::add);
        state.getAsJsonArray("motion_frames").forEach(MOTION_FRAMES::add);
        reloadingWorld = true;
    }

    private record Case(int shipLevel, int tier) {}

    private static Case configuration(int scene) {
        int config = scene / FRAMES_PER_CONFIG;
        return new Case(config < 3 ? 3 : 4, config % 3 + 1);
    }

    private static String name(Case c, int angle, int fill) {
        return "ship-" + c.shipLevel() + "-tier-" + c.tier()
                + "-" + new String[]{"bow", "starboard", "stern", "deck"}[angle]
                + "-" + new String[]{"weak", "medium", "strong"}[fill];
    }

    private static ShipBuildConfiguration blueprint(Case c) {
        boolean iv = c.shipLevel() == 4;
        List<Integer> tiers = iv
                ? List.of(c.tier(), c.tier(), c.tier(), c.tier())
                : List.of(c.tier(), c.tier(), c.tier());
        ShipBuildConfiguration plan = new ShipBuildConfiguration(iv
                ? ShipSectionPlan.ShipClass.LARGEST
                : ShipSectionPlan.ShipClass.LARGE,
                0, 0, tiers, 1, false, false, iv ? 4 : 3);
        return iv && c.tier() == 3 ? plan.withCabin(true) : plan;
    }

    private static ItemStack packed(ShipBuildConfiguration config) {
        CompoundTag pack = new CompoundTag();
        pack.putInt("Version", 2);
        pack.putInt("RigLevel", 1);
        pack.putInt("FabricQuality", 3);
        pack.putInt("GeometryVersion", 9);
        pack.put(ShipBuildConfiguration.NBT_KEY, config.save());
        pack.put("Modules", new ListTag());
        CompoundTag root = new CompoundTag();
        root.put("FirstShipPack", pack);
        ItemStack result = new ItemStack(RaftContent.FIRST_SHIP_ITEM.get());
        result.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return result;
    }

    private static void install(ServerLevel world, Case requested) {
        if (serverShip != null) {
            serverShip.discard();
            serverShip = null;
        }
        var config = blueprint(requested);
        var palette = ShipModularBlockHull.localPalette(config);
        if (palette.isEmpty()) throw new AssertionError("No physical III/IV hull palette");
        for (var cell : lastPalette.entrySet())
            if (!palette.containsKey(cell.getKey()))
                world.setBlock(ANCHOR.offset(cell.getKey()),
                        Blocks.AIR.defaultBlockState(), 2);
        for (var cell : palette.entrySet())
            if (!cell.getValue().equals(lastPalette.get(cell.getKey())))
                world.setBlock(ANCHOR.offset(cell.getKey()), cell.getValue(), 2);
        lastPalette = new LinkedHashMap<>(palette);
        historicalCells.addAll(palette.keySet());
        if (requested.shipLevel() == 4 && requested.tier() == 3) {
            BlockPos chest = ANCHOR.offset(2, 7, 0);
            if (!ShipModularBlockHull.movingCargoCell(config, new BlockPos(2, 7, 0))
                    || !world.setBlock(chest, Blocks.CHEST.defaultBlockState(), 2)
                    || !(world.getBlockEntity(chest) instanceof net.minecraft.world.Container box))
                throw new AssertionError("IV interaction chest cannot be installed");
            box.setItem(0, new ItemStack(Items.DIAMOND, 7));
        }
        FirstShipEntity ship = RaftContent.FIRST_SHIP.get().create(world);
        if (ship == null) throw new AssertionError("Cannot create FirstShipEntity");
        ship.initializeNativeHull(ANCHOR, 0);
        if (!ship.loadPacked(packed(config)))
            throw new AssertionError("Cannot load modular blueprint " + requested);
        ship.enableSmoothMotion();
        if (!world.addFreshEntity(ship))
            throw new AssertionError("Cannot add modular ship " + requested);
        serverShip = ship;
        expectedShipId = ship.getId();
    }

    private static void setCamera(ServerPlayer player, Case c, int angle) {
        double distance = c.shipLevel() == 4 ? 69D : 51D;
        double targetY = ANCHOR.getY() + (c.shipLevel() == 4 ? 28D : 23D);
        double eyeY = ANCHOR.getY() + (c.shipLevel() == 4 ? 35D : 31D);
        double x = angle == 1 ? distance : angle == 3
                ? (c.shipLevel() == 4 ? 4D : 2D) : 0D;
        double z = angle == 0 ? distance : angle == 2 ? -distance
                : angle == 3 ? (c.shipLevel() == 4 ? 0D : -15D) : 0D;
        if (angle == 3) {
            eyeY = ANCHOR.getY() + 14D;
            targetY = ANCHOR.getY() + 22D;
        }
        double dx = -x, dy = targetY - eyeY;
        double dz = angle == 3 && c.shipLevel() == 4 ? 12D - z : -z;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(
                Math.atan2(dy, Math.hypot(dx, dz)));
        player.teleportTo(player.serverLevel(),
                ANCHOR.getX() + x, eyeY - player.getEyeHeight(),
                ANCHOR.getZ() + z, yaw, pitch);
    }

    @SuppressWarnings("unchecked")
    private static void forcePreviewPressure(Minecraft mc, double pressure)
            throws ReflectiveOperationException {
        var entity = mc.level.getEntity(expectedShipId);
        if (!(entity instanceof FirstShipEntity ship))
            return; // Let entity sync arrive before attempting a screenshot.
        var renderer = mc.getEntityRenderDispatcher().getRenderer(ship);
        if (!(renderer instanceof FirstShipRenderer)) return;
        var field = FirstShipRenderer.class.getDeclaredField("sailPressure");
        field.setAccessible(true);
        var map = (Map<FirstShipEntity, Double>) field.get(renderer);
        // The production renderer blends 18% toward zero for stationary
        // vessels. Counter that single draw so the target shape is observable.
        map.put(ship, pressure / .82D);
    }

    private static void capture(Minecraft mc) throws Exception {
        Case c = configuration(scene);
        int local = scene % FRAMES_PER_CONFIG;
        int angle = local / 3;
        int fill = local % 3;
        String file = String.format("%02d-%s.png",
                scene, name(c, angle, fill));
        try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            image.writeToFile(OUT.resolve(file));
        }
        var row = new JsonObject();
        row.addProperty("file", file);
        row.addProperty("ship_level", c.shipLevel());
        row.addProperty("sail_tier", c.tier());
        row.addProperty("view", new String[]{"bow", "starboard", "stern", "deck"}[angle]);
        row.addProperty("pressure_preview",
                new double[]{.13D, .52D, 1D}[fill]);
        row.addProperty("pressure_source", "SIMULATED_PRESSURE_VISUAL_ONLY");
        row.addProperty("actual_packaged_mod", true);
        row.addProperty("server_tick", mc.level.getGameTime());
        FRAMES.add(row);
    }

    private static void finish(Minecraft mc) throws Exception {
        mc.options.keyUp.setDown(false);
        mc.options.keyRight.setDown(false);
        finished = true;
        if (FRAMES.size() != EXPECTED_FRAMES)
            throw new AssertionError("Missing captures " + FRAMES.size());
        var receipt = new JsonObject();
        receipt.addProperty("status", "CAPTURE_COMPLETE_NOT_OWNER_ACCEPTANCE");
        receipt.addProperty("environment",
                "GitHub-hosted isolated Xvfb integrated Minecraft singleplayer world");
        receipt.addProperty("scope",
                "Actual production JAR: stationary tier matrix with test-only pressure "
                + "preview, followed by an actual moving IV ship turn/idle/restart; "
                + "automated real-client chest/door/block actions and world relog; "
                + "NOT manual owner acceptance or two-client multiplayer");
        receipt.addProperty("seed", 5072026L);
        receipt.addProperty("jar_sha256", loadedSha);
        receipt.addProperty("captured", FRAMES.size());
        receipt.addProperty("chest_opened_by_client", chestOpened);
        receipt.addProperty("door_opened_by_client", doorOpened);
        receipt.addProperty("plank_broken_by_client", plankBroken);
        receipt.addProperty("chest_placed_by_client", chestPlacedByClient);
        receipt.addProperty("relog_chest_opened_by_client", relogChestOpened);
        receipt.addProperty("relog_door_toggled_by_client", relogDoorWorked);
        receipt.add("frames", FRAMES);
        receipt.add("motion_frames", MOTION_FRAMES);
        Files.writeString(OUT.resolve("finished.json"),
                new GsonBuilder().setPrettyPrinting().create().toJson(receipt));
        System.out.println("SHIP_SAIL_VISUAL " + receipt);
        mc.stop();
    }

    private static void beginMotion(Minecraft mc) {
        motionStarted = true;
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        mc.getSingleplayerServer().execute(() -> {
            ServerPlayer player = mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
            ServerLevel world = player.serverLevel();
            // A temporary water basin surrounds the already-installed IV hull.
            // Do not replace any cell of the physical ship palette.
            // Leave enough water for the entire turn and restart sweep.
            for (int x = BASIN_MIN_X; x <= BASIN_MAX_X; x++)
                for (int z = BASIN_MIN_Z; z <= BASIN_MAX_Z; z++) {
                    world.setBlock(ANCHOR.offset(x, -1, z),
                            Blocks.WATER.defaultBlockState(), 2);
                    if (!lastPalette.containsKey(new BlockPos(x, 0, z)))
                        world.setBlock(ANCHOR.offset(x, 0, z),
                                Blocks.WATER.defaultBlockState(), 2);
                }
            player.setGameMode(GameType.CREATIVE);
            if (!player.startRiding(serverShip))
                throw new AssertionError("IV test captain could not board");
            motionStartZ = serverShip.getZ();
            if (!serverShip.helmStep(player, FirstShipNetwork.FORWARD_RIGHT))
                throw new AssertionError("IV motion assembly rejected");
            serverResidualBlocks = 0;
            for (BlockPos cell : historicalCells) {
                BlockState leftover = world.getBlockState(ANCHOR.offset(cell));
                if (!leftover.isAir() && !leftover.is(Blocks.WATER))
                    serverResidualBlocks++;
            }
            world.getChunkSource().addRegionTicket(MOTION_TICKET,
                    new ChunkPos(ANCHOR), 4, ANCHOR.asLong());
            motionStartServerTick = world.getGameTime();
            // After successful detachment the old world palette is no longer
            // physical. Restore the entire temporary water surface, including
            // stale solid cells left by earlier stationary capture scenes.
            for (int x = BASIN_MIN_X; x <= BASIN_MAX_X; x++)
                for (int z = BASIN_MIN_Z; z <= BASIN_MAX_Z; z++) {
                    BlockPos surface = ANCHOR.offset(x, 0, z);
                    world.setBlock(surface, Blocks.WATER.defaultBlockState(), 2);
                }
            cameraStand = EntityType.ARMOR_STAND.create(world);
            if (cameraStand == null) throw new AssertionError("Cannot create camera stand");
            cameraStand.setNoGravity(true);
            cameraStand.setInvisible(true);
            cameraStand.setPos(serverShip.getX() + 55D,
                    serverShip.getY() + 27D, serverShip.getZ());
            cameraStand.setYRot(90F);
            cameraStand.setYHeadRot(90F);
            cameraStand.setXRot(8F);
            if (!world.addFreshEntity(cameraStand))
                throw new AssertionError("Cannot spawn camera stand");
            cameraStandId = cameraStand.getId();
            motionReady = true;
        });
    }

    private static void motionTick(Minecraft mc) throws Exception {
        if (!motionReady) return;
        motionAge++;
        long serverAge = mc.getSingleplayerServer().overworld().getGameTime()
                - motionStartServerTick;
        if (serverAge >= 350) {
            if (MOTION_FRAMES.size() != 3)
                throw new AssertionError("Missing moving IV captures");
            interactionTick(mc);
            return;
        }
        if (mc.level.getEntity(cameraStandId) instanceof ArmorStand stand
                && mc.getCameraEntity() != stand)
            mc.setCameraEntity(stand);
        // Allow the remote camera's section meshes to finish loading before
        // the first underway screenshot; otherwise the turning frame is sky.
        int action = serverAge < 100 ? FirstShipNetwork.HELM_IDLE
                : serverAge < 150 ? FirstShipNetwork.FORWARD_RIGHT
                : serverAge < 260 ? FirstShipNetwork.HELM_IDLE
                : FirstShipNetwork.FORWARD;
        mc.options.keyUp.setDown(action != FirstShipNetwork.HELM_IDLE);
        mc.options.keyRight.setDown(action == FirstShipNetwork.FORWARD_RIGHT);
        mc.getSingleplayerServer().execute(() -> {
            ServerPlayer player = mc.getSingleplayerServer().getPlayerList().getPlayers().getFirst();
            if (player.getVehicle() != serverShip)
                throw new AssertionError("IV captain left helm at motion tick " + motionAge);
            clientControlFrames++;
            serverMotionSpeed = serverShip.serverMovingSpeed();
            cameraStand.setPos(serverShip.getX() + 55D,
                    serverShip.getY() + 27D, serverShip.getZ());
        });
        int frameIndex = MOTION_FRAMES.size();
        int frameTick = frameIndex == 0 ? 145 : frameIndex == 1 ? 255 : 340;
        if (frameIndex < 3 && serverAge >= frameTick) {
            FirstShipEntity visible = mc.level.getEntity(expectedShipId) instanceof FirstShipEntity ship
                    ? ship : null;
            if (visible == null || !visible.isMoving()
                    || Math.hypot(visible.getX() - ANCHOR.getX() - .5D,
                            visible.getZ() - motionStartZ) < .1D)
                throw new AssertionError("Moving IV hull not synchronized at tick " + motionAge);
            String state = frameIndex == 0 ? "turn" : frameIndex == 1 ? "idle" : "restart";
            String file = "motion-iv-tier-3-" + state + ".png";
            try (var image = Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
                image.writeToFile(OUT.resolve(file));
            }
            JsonObject row = new JsonObject();
            row.addProperty("file", file);
            row.addProperty("state", state);
            row.addProperty("x", visible.getX());
            row.addProperty("z", visible.getZ());
            row.addProperty("yaw", visible.getYRot());
            row.addProperty("speed", visible.visualMotionSpeed());
            row.addProperty("server_speed", serverMotionSpeed);
            row.addProperty("client_control_frames", clientControlFrames);
            int clientResidual = 0;
            for (BlockPos cell : historicalCells) {
                BlockState leftover = mc.level.getBlockState(ANCHOR.offset(cell));
                if (!leftover.isAir() && !leftover.is(Blocks.WATER))
                    clientResidual++;
            }
            row.addProperty("server_residual_blocks", serverResidualBlocks);
            row.addProperty("client_residual_blocks", clientResidual);
            System.out.println("SHIP_SAIL_RESIDUAL server=" + serverResidualBlocks
                    + " client=" + clientResidual + " frame=" + state);
            MOTION_FRAMES.add(row);
            if (frameIndex == 0 && serverMotionSpeed <= .05D)
                throw new AssertionError("IV did not turn under client helm input");
            if (frameIndex == 1) {
                if (serverMotionSpeed > .01D)
                    throw new AssertionError("IV remained underway after idle input");
                motionRestartX = visible.getX();
                motionRestartZ = visible.getZ();
            }
            if (frameIndex == 2 && (serverMotionSpeed <= .05D
                    || Math.hypot(visible.getX() - motionRestartX,
                            visible.getZ() - motionRestartZ) <= .5D))
                throw new AssertionError("IV failed to restart after idle: "
                        + motionRestartX + "," + motionRestartZ + " -> "
                        + visible.getX() + "," + visible.getZ()
                        + " serverSpeed=" + serverMotionSpeed);
        }
    }

    private static void aimAt(ServerPlayer player, BlockPos local, double offsetZ) {
        FirstShipHull.Point target = FirstShipHull.toWorld(local.getX(), local.getZ(),
                serverShip.getX(), serverShip.getZ(), serverShip.getYRot());
        FirstShipHull.Point standing = FirstShipHull.toWorld(local.getX(),
                local.getZ() + offsetZ, serverShip.getX(), serverShip.getZ(),
                serverShip.getYRot());
        double playerY = serverShip.getY() + local.getY();
        double dx = target.x() - standing.x(), dz = target.z() - standing.z();
        double dy = serverShip.getY() + local.getY() + .5D
                - playerY - player.getEyeHeight();
        float yaw = (float)Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float)-Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        player.teleportTo(player.serverLevel(), standing.x(), playerY,
                standing.z(), yaw, pitch);
    }

    private static void aimClientAt(Minecraft mc, BlockPos local) {
        FirstShipEntity visible = mc.level.getEntity(expectedShipId)
                instanceof FirstShipEntity ship ? ship : null;
        if (visible == null) throw new AssertionError("Missing client IV for aiming");
        FirstShipHull.Point point = FirstShipHull.toWorld(local.getX(), local.getZ(),
                visible.getX(), visible.getZ(), visible.shipYaw());
        Vec3 delta = new Vec3(point.x(), visible.getY() + local.getY() + .5D,
                point.z()).subtract(mc.player.getEyePosition());
        float yaw = (float)Math.toDegrees(Math.atan2(-delta.x, delta.z));
        float pitch = (float)-Math.toDegrees(Math.atan2(delta.y,
                Math.hypot(delta.x, delta.z)));
        mc.player.setYRot(yaw);
        mc.player.setYHeadRot(yaw);
        mc.player.setXRot(pitch);
    }

    private static void interactionTick(Minecraft mc) throws Exception {
        interactionAge++;
        mc.options.keyUp.setDown(false);
        mc.options.keyRight.setDown(false);
        var server = mc.getSingleplayerServer();
        if (interactionPhase == 0) {
            if (interactionAge < 100) return;
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                player.stopRiding();
                aimAt(player, new BlockPos(2, 7, 0), 2D);
            });
            mc.setCameraEntity(mc.player);
            mc.options.hideGui = false;
            interactionPhase = 1; interactionAge = 0;
            return;
        }
        if (interactionPhase == 1) {
            if (interactionAge < 25) return;
            aimClientAt(mc, new BlockPos(2, 7, 0));
            FirstShipEntity visible = mc.level.getEntity(expectedShipId)
                    instanceof FirstShipEntity ship ? ship : null;
            var eye = mc.player.getEyePosition();
            var end = eye.add(mc.player.getLookAngle().scale(5D));
            var palette = visible == null ? Map.<BlockPos, BlockState>of()
                    : ShipModularBlockHull.localVisualPalette(blueprint(new Case(4, 3)),
                            ShipModularBlockHull.decodeVisibleCargo(
                                    visible.movingCargoView()));
            var hit = visible == null ? null
                    : org.slavicmyths.ships.ShipMovingCargoTarget.hit(
                            visible, eye, end, palette);
            chestClientDiagnostic = "player=" + mc.player.position()
                    + " passenger=" + mc.player.isPassenger()
                    + " screen=" + mc.screen
                    + " look=" + mc.player.getLookAngle()
                    + " clientShip=" + (visible == null ? null : visible.position())
                    + " hit=" + hit + " cargoView="
                    + (visible == null ? "missing ship" : visible.movingCargoView());
            System.out.println("SHIP_CHEST_CLIENT_DIAGNOSTIC " + chestClientDiagnostic);
            net.minecraft.client.KeyMapping.click(mc.options.keyUse.getKey());
            interactionPhase = 2; interactionAge = 0;
            return;
        }
        if (interactionPhase == 2) {
            if (interactionAge < 12) return;
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                chestOpened = player.containerMenu != player.inventoryMenu;
                if (!chestOpened) {
                    boolean direct = serverShip.openMovingCargo(player, 0);
                    chestServerDiagnostic = "direct=" + direct + " player="
                            + player.position() + " ship=" + serverShip.position()
                            + " view=" + serverShip.movingCargoView();
                    System.out.println("SHIP_CHEST_SERVER_DIAGNOSTIC "
                            + chestServerDiagnostic);
                }
                player.closeContainer();
                interactionDoor = serverShip.serverMovingPalette().entrySet().stream()
                        .filter(entry -> entry.getValue().getBlock()
                                instanceof net.minecraft.world.level.block.DoorBlock
                                && entry.getValue().getValue(
                                        net.minecraft.world.level.block.DoorBlock.HALF)
                                        == net.minecraft.world.level.block.state.properties
                                                .DoubleBlockHalf.LOWER)
                        .map(Map.Entry::getKey).findFirst().orElse(null);
                if (interactionDoor != null) aimAt(player, interactionDoor, 2D);
            });
            interactionPhase = 3; interactionAge = 0;
            return;
        }
        if (interactionPhase == 3) {
            if (interactionAge < 25) return;
            if (!chestOpened || interactionDoor == null)
                throw new AssertionError("Client could not open detached IV chest: "
                        + chestClientDiagnostic + " server=" + chestServerDiagnostic);
            aimClientAt(mc, interactionDoor);
            FirstShipEntity visibleDoorShip = mc.level.getEntity(expectedShipId)
                    instanceof FirstShipEntity ship ? ship : null;
            Vec3 doorEye = mc.player.getEyePosition();
            var doorHit = visibleDoorShip == null ? null
                    : org.slavicmyths.ships.ShipMovingCargoTarget.hit(
                            visibleDoorShip, doorEye,
                            doorEye.add(mc.player.getLookAngle().scale(5D)),
                            ShipModularBlockHull.localVisualPalette(
                                    blueprint(new Case(4, 3)),
                                    ShipModularBlockHull.decodeVisibleCargo(
                                            visibleDoorShip.movingCargoView())));
            doorClientDiagnostic = "target=" + interactionDoor + " hit=" + doorHit
                    + " screen=" + mc.screen + " player=" + mc.player.position()
                    + " ship=" + (visibleDoorShip == null ? null : visibleDoorShip.position());
            System.out.println("SHIP_DOOR_CLIENT_DIAGNOSTIC " + doorClientDiagnostic);
            net.minecraft.client.KeyMapping.click(mc.options.keyUse.getKey());
            interactionPhase = 4; interactionAge = 0;
            return;
        }
        if (interactionPhase == 4) {
            if (interactionAge < 12) return;
            server.execute(() -> {
                BlockState door = serverShip.serverMovingPalette().get(interactionDoor);
                doorOpened = door != null && door.getValue(
                        net.minecraft.world.level.block.DoorBlock.OPEN);
                if (!doorOpened) {
                    ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                    Vec3 eye = player.getEyePosition();
                    var hit = org.slavicmyths.ships.ShipMovingCargoTarget.hit(
                            serverShip, eye, eye.add(player.getLookAngle().scale(5D)),
                            serverShip.serverMovingPalette());
                    doorServerDiagnostic = "target=" + interactionDoor + " hit=" + hit
                            + " player=" + player.position() + " ship="
                            + serverShip.position() + " state=" + door;
                    System.out.println("SHIP_DOOR_SERVER_DIAGNOSTIC "
                            + doorServerDiagnostic);
                }
                interactionPlank = serverShip.serverMovingPalette().entrySet().stream()
                        .filter(entry -> entry.getValue().is(Blocks.SPRUCE_PLANKS)
                                && entry.getKey().getY() >= 6
                                && Math.abs(entry.getKey().getX()) <= 5
                                && Math.abs(entry.getKey().getZ()) <= 8
                                && !serverShip.serverMovingPalette().containsKey(
                                        entry.getKey().above())
                                && !serverShip.serverMovingPalette().containsKey(
                                        entry.getKey().above(2)))
                        .map(Map.Entry::getKey).findFirst().orElse(null);
                placementBase = serverShip.serverMovingPalette().entrySet().stream()
                        .filter(entry -> entry.getValue().is(Blocks.SPRUCE_PLANKS)
                                && entry.getKey().getY() >= 6
                                && !entry.getKey().equals(interactionPlank)
                                && !serverShip.serverMovingPalette().containsKey(
                                        entry.getKey().above())
                                && org.slavicmyths.ships.ShipModularBlockHull.movingCargoCell(
                                        blueprint(new Case(4, 3)), entry.getKey().above()))
                        .map(Map.Entry::getKey).findFirst().orElse(null);
                if (interactionPlank != null) {
                    ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                    FirstShipHull.Point point = FirstShipHull.toWorld(
                            interactionPlank.getX(), interactionPlank.getZ(),
                            serverShip.getX(), serverShip.getZ(), serverShip.getYRot());
                    player.teleportTo(player.serverLevel(), point.x(),
                            serverShip.getY() + interactionPlank.getY() + 2D,
                            point.z(), serverShip.getYRot(), 90F);
                }
            });
            interactionPhase = 5; interactionAge = 0;
            return;
        }
        if (interactionPhase == 5) {
            if (interactionAge < 25) return;
            if (!doorOpened || interactionPlank == null)
                throw new AssertionError("Client could not open detached IV door: "
                        + doorClientDiagnostic + " server=" + doorServerDiagnostic);
            aimClientAt(mc, interactionPlank);
            net.minecraft.client.KeyMapping.click(mc.options.keyAttack.getKey());
            interactionPhase = 6; interactionAge = 0;
            return;
        }
        if (interactionPhase == 6) {
            if (interactionAge < 12) return;
            server.execute(() -> plankBroken = interactionPlank != null
                    && !serverShip.serverMovingPalette().containsKey(interactionPlank));
            interactionPhase = 7; interactionAge = 0;
            return;
        }
        if (interactionPhase == 7) {
            if (interactionAge < 5) return;
            if (!plankBroken)
                throw new AssertionError("Client could not break detached IV plank");
            if (placementBase == null)
                throw new AssertionError("No IV deck cell for client placement");
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,
                        new ItemStack(Items.CHEST));
                FirstShipHull.Point point = FirstShipHull.toWorld(
                        placementBase.getX(), placementBase.getZ(),
                        serverShip.getX(), serverShip.getZ(), serverShip.getYRot());
                player.teleportTo(player.serverLevel(), point.x(),
                        serverShip.getY() + placementBase.getY() + 2D,
                        point.z(), serverShip.getYRot(), 90F);
            });
            interactionPhase = 8; interactionAge = 0;
            return;
        }
        if (interactionPhase == 8) {
            if (interactionAge < 25) return;
            aimClientAt(mc, placementBase);
            FirstShipEntity visible = mc.level.getEntity(expectedShipId)
                    instanceof FirstShipEntity ship ? ship : null;
            Vec3 eye = mc.player.getEyePosition();
            BlockPos target = visible == null ? null
                    : org.slavicmyths.ships.ShipMovingCargoTarget.target(visible,
                            eye, eye.add(mc.player.getLookAngle().scale(5D)),
                            org.slavicmyths.ships.ShipModularBlockHull.localVisualPalette(
                                    blueprint(new Case(4, 3)),
                                    org.slavicmyths.ships.ShipModularBlockHull.decodeVisibleCargo(
                                            visible.movingCargoView())));
            placementDiagnostic = "client target=" + target + " expected="
                    + placementBase.above() + " item=" + mc.player.getMainHandItem()
                    + " player=" + mc.player.position();
            System.out.println("SHIP_PLACE_CLIENT " + placementDiagnostic);
            mc.options.keyShift.setDown(true);
            net.minecraft.client.KeyMapping.click(mc.options.keyUse.getKey());
            interactionPhase = 9; interactionAge = 0;
            return;
        }
        if (interactionPhase == 9) {
            if (interactionAge < 12) return;
            mc.options.keyShift.setDown(false);
            server.execute(() -> chestPlacedByClient = serverShip.serverMovingPalette()
                    .getOrDefault(placementBase.above(), Blocks.AIR.defaultBlockState())
                    .is(Blocks.CHEST));
            interactionPhase = 10; interactionAge = 0;
            return;
        }
        if (interactionPhase == 10) {
            if (interactionAge < 5) return;
            if (!chestPlacedByClient) {
                server.execute(() -> {
                    ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                    Vec3 eye = player.getEyePosition();
                    BlockPos target = org.slavicmyths.ships.ShipMovingCargoTarget.target(
                            serverShip, eye,
                            eye.add(player.getLookAngle().scale(5.5D)),
                            serverShip.serverMovingPalette());
                    System.out.println("SHIP_PLACE_SERVER target=" + target
                            + " item=" + player.getMainHandItem()
                            + " player=" + player.position() + " expected="
                            + placementBase.above() + " direct="
                            + serverShip.placeMovingCargo(player, placementBase.above(),
                                    net.minecraft.world.InteractionHand.MAIN_HAND));
                });
                interactionPhase = 11; interactionAge = 0;
                return;
            }
            checkpoint(mc);
            return;
        }
        if (interactionPhase == 11 && interactionAge >= 5)
            throw new AssertionError("Client could not place detached IV chest: "
                    + placementDiagnostic);
    }

    private static void relogTick(Minecraft mc) throws Exception {
        relogAge++;
        if (relogAge == 25) {
            mc.getSingleplayerServer().execute(() -> {
                ServerPlayer player = mc.getSingleplayerServer().getPlayerList()
                        .getPlayers().getFirst();
                serverShip = player.serverLevel().getEntitiesOfClass(
                        FirstShipEntity.class, player.getBoundingBox().inflate(120D))
                        .stream().filter(FirstShipEntity::isMoving).findFirst()
                        .orElseThrow(() -> new AssertionError("IV ship missing after relog"));
                expectedShipId = serverShip.getId();
                aimAt(player, new BlockPos(2, 7, 0), 2D);
            });
            mc.setCameraEntity(mc.player);
        }
        if (relogAge == 55) {
            aimClientAt(mc, new BlockPos(2, 7, 0));
            net.minecraft.client.KeyMapping.click(mc.options.keyUse.getKey());
        }
        if (relogAge == 75) {
            mc.getSingleplayerServer().execute(() -> {
                ServerPlayer player = mc.getSingleplayerServer().getPlayerList()
                        .getPlayers().getFirst();
                relogChestOpened = player.containerMenu != player.inventoryMenu;
                player.closeContainer();
                aimAt(player, interactionDoor, 2D);
            });
        }
        if (relogAge == 100) {
            if (!relogChestOpened)
                throw new AssertionError("Client chest failed after actual world relog");
            aimClientAt(mc, interactionDoor);
            net.minecraft.client.KeyMapping.click(mc.options.keyUse.getKey());
        }
        if (relogAge == 120) {
            mc.getSingleplayerServer().execute(() -> {
                BlockState door = serverShip.serverMovingPalette().get(interactionDoor);
                relogDoorWorked = door != null && !door.getValue(
                        net.minecraft.world.level.block.DoorBlock.OPEN);
            });
        }
        if (relogAge == 125) {
            if (!relogDoorWorked)
                throw new AssertionError("Client door failed after actual world relog");
            finish(mc);
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post ignored) throws Exception {
        if (System.getProperty("shipsailvisual.multiplayer") != null) return;
        if (finished) return;
        if (!"true".equals(System.getenv("GITHUB_ACTIONS"))
                || !"github-hosted".equals(System.getenv("RUNNER_ENVIRONMENT")))
            throw new IllegalStateException(
                    "Only an isolated GitHub-hosted runner may run the visual test");
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null && mc.screen.getClass().getSimpleName()
                .equals("AccessibilityOnboardingScreen")) {
            mc.setScreen(new TitleScreen());
            return;
        }
        if (!opened && mc.screen instanceof TitleScreen) {
            opened = true;
            Files.createDirectories(OUT);
            var jar = ModList.get().getModFileById("slavicmyths")
                    .getFile().getFilePath();
            if (!jar.toString().endsWith(".jar"))
                throw new AssertionError("Visual test requires packaged production JAR");
            loadedSha = HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256")
                    .digest(Files.readAllBytes(jar)));
            if (RELOG_RUN) {
                restoreCheckpoint();
                if (!loadedSha.equals(HexFormat.of().formatHex(
                        MessageDigest.getInstance("SHA-256")
                        .digest(Files.readAllBytes(jar)))))
                    throw new AssertionError("Production JAR changed between relog runs");
                mc.createWorldOpenFlows().openWorld(isolatedWorldName,
                        () -> { throw new AssertionError("Isolated IV world could not reload"); });
                return;
            }
            mc.options.pauseOnLostFocus = false;
            mc.options.renderDistance().set(10);
            mc.options.simulationDistance().set(8);
            mc.options.enableVsync().set(false);
            mc.options.framerateLimit().set(60);
            mc.options.guiScale().set(2);
            mc.options.fov().set(67);
            mc.options.tutorialStep = net.minecraft.client.tutorial.TutorialSteps.NONE;
            isolatedWorldName = "ShipSail-QA-" + System.currentTimeMillis();
            mc.createWorldOpenFlows().createFreshLevel(
                    isolatedWorldName,
                    new LevelSettings("Temporary ship sail framebuffer proof",
                            GameType.CREATIVE, false, Difficulty.PEACEFUL,
                            true, new GameRules(), WorldDataConfiguration.DEFAULT),
                    new WorldOptions(5072026L, true, false),
                    registry -> registry.registryOrThrow(Registries.WORLD_PRESET)
                            .getHolderOrThrow(WorldPresets.FLAT)
                            .value().createWorldDimensions(),
                    mc.screen);
            return;
        }
        if (mc.level == null || mc.player == null
                || mc.getSingleplayerServer() == null) return;
        if (reloadingWorld) {
            relogTick(mc);
            return;
        }
        if (motionStarted) {
            motionTick(mc);
            return;
        }
        if (scene < 0 || age >= 52) {
            scene++;
            age = 0;
            positioned = false;
            mc.options.hideGui = true;
            mc.options.setCameraType(CameraType.FIRST_PERSON);
            if (scene == EXPECTED_FRAMES) {
                beginMotion(mc);
                return;
            }
            Case c = configuration(scene);
            int idx = scene / FRAMES_PER_CONFIG;
            int angle = scene % FRAMES_PER_CONFIG / 3;
            var server = mc.getSingleplayerServer();
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
                player.setGameMode(GameType.SPECTATOR);
                if (currentConfig != idx) {
                    install(player.serverLevel(), c);
                    currentConfig = idx;
                }
                setCamera(player, c, angle);
                positioned = true;
            });
            return;
        }
        if (!positioned) return;
        age++;
        if (age >= 42 && age <= 48) {
            double pressure = new double[]{.13D, .52D, 1D}[scene % 3];
            forcePreviewPressure(mc, pressure);
        }
        if (age == 49) {
            if (!(mc.level.getEntity(expectedShipId) instanceof FirstShipEntity))
                throw new AssertionError("Ship not synchronized to client");
            capture(mc);
        }
    }

    private ShipSailCapture() {}
}
