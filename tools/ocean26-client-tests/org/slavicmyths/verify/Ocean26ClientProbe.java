package org.slavicmyths.verify;

import java.util.UUID;
import java.nio.file.Files;
import java.io.IOException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.CameraType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipBlockHull;
import org.slavicmyths.ships.FirstShipItem;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.ShipRigidMotion;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipDeckContact;
import org.slavicmyths.ships.ShipModularBlockHull;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipyardModular;
import org.slavicmyths.ships.RaftContent;
import org.slavicmyths.ships.client.FirstShipRenderer;

/** Disposable real-client input/network probe; excluded from production JAR. */
@EventBusSubscriber(modid = "slavicmyths", value = Dist.CLIENT)
public final class Ocean26ClientProbe {
    private static final boolean REVERSE = Boolean.getBoolean("ocean26.reverse");
    private static final boolean RELOAD = Boolean.getBoolean("ocean26.reload");
    private static final boolean SAVE_MOVING = Boolean.getBoolean("ocean26.saveMoving");
    private static final boolean RAIL = Boolean.getBoolean("ocean26.rail");
    private static final boolean HOLD = Boolean.getBoolean("ocean26.hold");
    private static final boolean VISUAL = Boolean.getBoolean("ocean26.visual");
    private static final boolean MODULAR_VISUAL = Boolean.getBoolean("ocean26.modularVisual");
    private static final boolean LONG_FORWARD = Boolean.getBoolean("ocean26.longForward");
    private static final boolean TURN_CAMERAS = Boolean.getBoolean("ocean26.turnCamera");
    private static final boolean RENDER_BENCH = LONG_FORWARD && MODULAR_VISUAL;
    private static volatile BlockPos benchAnchor;
    private static volatile UUID benchShipId;
    private static volatile String benchError;
    private static boolean benchPreparing;
    private static UUID benchPrevious;
    private static int benchCase = -1, benchWait, benchVariant;
    private static volatile boolean benchBaselineReady;
    private static Integer benchOriginalFpsLimit;
    private static Boolean benchOriginalVsync;
    private static boolean benchSampling, benchSceneDone;
    private static int benchRepeat, benchWarmup, benchSamples;
    private static long benchLastFrameNanos;
    private static final int FRAME_WARMUP = 60, FRAME_SAMPLES = 180, FRAME_REPEATS = 3;
    private static final long[][] frameTimes = new long[9][FRAME_SAMPLES * FRAME_REPEATS];
    private static final String[] FRAME_SCENES = {
            "SEA", "SMALL_LEGACY", "SMALL_BATCH",
            "LARGE_UNLIT_LEGACY", "LARGE_UNLIT_BATCH",
            "LARGE_LIT_LEGACY", "LARGE_LIT_BATCH",
            "LARGEST_LIT_LEGACY", "LARGEST_LIT_BATCH"
    };

    private static int frameScenario() {
        return benchCase < 0 ? 0 : 1 + 2 * benchCase + benchVariant;
    }
    private static volatile boolean modularSetupRequested, modularReady;
    private static boolean turnMotionStarted;
    private static int phase;
    private static int noWorldTicks;
    private static volatile int elapsed;
    private static UUID shipId;
    private static double startX, startZ;
    private static double walkStartX, walkStartZ, jumpPeak;
    private static double reverseStartX, reverseStartZ;
    private static float reverseStartYaw;
    private static double holdStartY, holdPeakY;
    private static double previousMotionX, previousMotionZ, largestMotionStep;
    private static float previousMotionYaw;
    private static double largestYawStep;
    private static int zeroMotionFrames, motionSamples;
    private static int reverseMotionFrames;
    private static int cameraFrames, reverseCameraFrames;
    private static double previousCameraX, previousCameraZ, maximumCameraSeatError;
    private static double maximumRiderOldSeatError, maximumRiderCurrentSeatError;
    private static int dismountCameraFrames;
    private static double dismountLocalX, dismountLocalZ, maximumDismountCameraStep;
    private static int turnFrames;
    private static double maximumTurnSeatError;
    private static double mostNegativeForwardStep;
    private static boolean holdReachedDeck, holdExited;
    private static long tickStartNanos, shipTickNanos, emptyTickNanos;
    private static int shipTicks, emptyTicks;

    /** Intervals between rendered world frames; includes CPU scheduling and GPU stalls, not GPU timings. */
    @SubscribeEvent
    public static void measureFrameInterval(RenderLevelStageEvent event) {
        if (!RENDER_BENCH || phase != 6 || !benchSampling
                || event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;
        long now = System.nanoTime();
        long previous = benchLastFrameNanos;
        benchLastFrameNanos = now;
        if (previous == 0L || now <= previous) return;
        if (benchWarmup < FRAME_WARMUP) {
            benchWarmup++;
            return;
        }
        int scenario = frameScenario();
        frameTimes[scenario][benchRepeat * FRAME_SAMPLES + benchSamples++] = now - previous;
        if (benchSamples != FRAME_SAMPLES) return;
        double meanMs = frameMeanMs(frameTimes[scenario], benchRepeat * FRAME_SAMPLES, FRAME_SAMPLES);
        System.out.println(String.format(java.util.Locale.ROOT,
                "OCEAN26_FRAME_REPEAT scenario=%s repeat=%d frames=%d avgMs=%.4f fps=%.3f",
                FRAME_SCENES[scenario], benchRepeat + 1, FRAME_SAMPLES, meanMs, 1000D / meanMs));
        benchSamples = 0;
        benchWarmup = 0;
        benchLastFrameNanos = 0L;
        if (++benchRepeat == FRAME_REPEATS) {
            benchSampling = false;
            benchSceneDone = true;
        }
    }

    private static double frameMeanMs(long[] samples, int from, int length) {
        double total = 0D;
        for (int i = from; i < from + length; i++) total += samples[i] / 1_000_000D;
        return total / length;
    }

    private static void printFrameResult(int scenario) {
        long[] sorted = frameTimes[scenario].clone();
        java.util.Arrays.sort(sorted);
        int count = sorted.length;
        double mean = frameMeanMs(frameTimes[scenario], 0, count);
        double sea = frameMeanMs(frameTimes[0], 0, frameTimes[0].length);
        double fps = 1000D / mean, seaFps = 1000D / sea;
        double minimumRepeatMs = Double.POSITIVE_INFINITY, maximumRepeatMs = 0D;
        for (int repeat = 0; repeat < FRAME_REPEATS; repeat++) {
            double repeatMs = frameMeanMs(frameTimes[scenario], repeat * FRAME_SAMPLES, FRAME_SAMPLES);
            minimumRepeatMs = Math.min(minimumRepeatMs, repeatMs);
            maximumRepeatMs = Math.max(maximumRepeatMs, repeatMs);
        }
        System.out.println(String.format(java.util.Locale.ROOT,
                "OCEAN26_FRAME_RESULT scenario=%s repeats=%d warmupPerRepeat=%d frames=%d "
                + "fps=%.3f avgMs=%.4f p95Ms=%.4f p99Ms=%.4f "
                + "deltaFpsPct=%+.2f deltaFrameMsPct=%+.2f "
                + "repeatAvgMsMin=%.4f repeatAvgMsMax=%.4f GPU=NOT_MEASURED",
                FRAME_SCENES[scenario], FRAME_REPEATS, FRAME_WARMUP, count,
                fps, mean, sorted[(int)Math.ceil(.95D * count) - 1] / 1_000_000D,
                sorted[(int)Math.ceil(.99D * count) - 1] / 1_000_000D,
                100D * (fps / seaFps - 1D), 100D * (mean / sea - 1D),
                minimumRepeatMs, maximumRepeatMs));
    }

    private static void beginFrameCapture() {
        if (benchCase >= 1) {
            String label = FRAME_SCENES[frameScenario()];
            Screenshot.grab(Minecraft.getInstance().gameDirectory,
                    Minecraft.getInstance().getMainRenderTarget(),
                    message -> System.out.println("OCEAN26_AB_VISUAL " + label
                            + " screenshot=" + message.getString()));
        }
        benchRepeat = benchSamples = benchWarmup = 0;
        benchLastFrameNanos = 0L;
        benchSampling = true;
    }

    private static void printAbFrameResult(int index) {
        int legacy = 1 + 2 * index, batch = legacy + 1;
        double oldMs = frameMeanMs(frameTimes[legacy], 0, frameTimes[legacy].length);
        double newMs = frameMeanMs(frameTimes[batch], 0, frameTimes[batch].length);
        System.out.println(String.format(java.util.Locale.ROOT,
                "OCEAN26_AB_FRAME ship=%s oldFrameMs=%.4f newFrameMs=%.4f oldFps=%.3f newFps=%.3f deltaFpsPct=%+.2f",
                FRAME_SCENES[legacy].replace("_LEGACY", ""),
                oldMs, newMs, 1000D / oldMs, 1000D / newMs,
                100D * (oldMs / newMs - 1D)));
    }

    @SubscribeEvent
    public static void measureCamera(RenderLevelStageEvent event) {
        if (phase != 2 || event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES)
            return;
        boolean forwardWindow = LONG_FORWARD && elapsed >= 32 && elapsed <= 158;
        boolean turnWindow = !LONG_FORWARD && (!MODULAR_VISUAL || TURN_CAMERAS)
                && elapsed >= 20 && elapsed <= 40;
        boolean dismountWindow = !LONG_FORWARD && (!MODULAR_VISUAL || TURN_CAMERAS)
                && elapsed >= 54 && elapsed <= 88;
        if (!forwardWindow && !dismountWindow && !turnWindow) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        FirstShipEntity ship;
        if (forwardWindow || turnWindow) {
            if (!(mc.player.getVehicle() instanceof FirstShipEntity mounted)) return;
            ship = mounted;
        } else {
            ship = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(48D, 20D, 48D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            if (ship == null || mc.player.isPassenger()) return;
        }
        if (!ship.isMoving()) return;
        double partial = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        double x = net.minecraft.util.Mth.lerp(partial, ship.xo, ship.getX());
        double z = net.minecraft.util.Mth.lerp(partial, ship.zo, ship.getZ());
        double yaw = net.minecraft.util.Mth.rotLerp((float)partial,
                ship.yRotO, ship.getYRot());
        var camera = event.getCamera().getPosition();
        if (forwardWindow || turnWindow) {
            FirstShipHull.Point previousSeat = FirstShipHull.toWorld(0D,
                    ship.helmSeatZ(), ship.xo, ship.zo, ship.yRotO);
            FirstShipHull.Point currentSeat = FirstShipHull.toWorld(0D,
                    ship.helmSeatZ(), ship.getX(), ship.getZ(), ship.getYRot());
            maximumRiderOldSeatError = Math.max(maximumRiderOldSeatError,
                    Math.hypot(mc.player.xo - previousSeat.x(), mc.player.zo - previousSeat.z()));
            maximumRiderCurrentSeatError = Math.max(maximumRiderCurrentSeatError,
                    Math.hypot(mc.player.getX() - currentSeat.x(), mc.player.getZ() - currentSeat.z()));
        }
        if (turnWindow) {
            double playerX = net.minecraft.util.Mth.lerp(partial, mc.player.xo, mc.player.getX());
            double playerZ = net.minecraft.util.Mth.lerp(partial, mc.player.zo, mc.player.getZ());
            FirstShipHull.Point seat = FirstShipHull.toLocal(playerX, playerZ, x, z, yaw);
            maximumTurnSeatError = Math.max(maximumTurnSeatError,
                    Math.hypot(seat.x(), seat.z() - ship.helmSeatZ()));
            turnFrames++;
            return;
        }
        FirstShipHull.Point local = FirstShipHull.toLocal(camera.x, camera.z,
                x, z, yaw);
        if (dismountWindow) {
            if (dismountCameraFrames > 0) {
                double step = Math.hypot(local.x() - dismountLocalX,
                        local.z() - dismountLocalZ);
                maximumDismountCameraStep = Math.max(maximumDismountCameraStep, step);
                if (step > .10D)
                    System.out.println("OCEAN26_DISMOUNT_FRAME_DELTA tick=" + elapsed
                            + " step=" + step + " cameraLocal=(" + local.x() + ","
                            + local.z() + ") previousLocal=(" + dismountLocalX
                            + "," + dismountLocalZ + ") shipPoseFrame="
                            + ship.clientPoseFrame() + " playerCurrent="
                            + mc.player.position() + " playerPrevious=("
                            + mc.player.xo + "," + mc.player.zo + ")");
            }
            dismountLocalX = local.x();
            dismountLocalZ = local.z();
            dismountCameraFrames++;
            return;
        }
        maximumCameraSeatError = Math.max(maximumCameraSeatError,
                Math.hypot(local.x(), local.z() - ship.helmSeatZ()));
        if (cameraFrames > 0) {
            double heading = Math.toRadians(ship.getYRot());
            double forward = -(camera.x - previousCameraX) * Math.sin(heading)
                    + (camera.z - previousCameraZ) * Math.cos(heading);
            if (forward < -.003D) reverseCameraFrames++;
        }
        previousCameraX = camera.x;
        previousCameraZ = camera.z;
        cameraFrames++;
    }

    @SubscribeEvent
    public static void serverTickStart(ServerTickEvent.Pre event) {
        tickStartNanos = System.nanoTime();
    }

    @SubscribeEvent
    public static void serverTickEnd(ServerTickEvent.Post event) {
        long duration = System.nanoTime() - tickStartNanos;
        if (elapsed >= 150 && elapsed < 195) {
            shipTickNanos += duration;
            shipTicks++;
        } else if (elapsed >= 205 && elapsed < 255) {
            emptyTickNanos += duration;
            emptyTicks++;
        }
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (phase < 0 || Boolean.getBoolean("ocean26.shipyardSwitch")) return;
        Minecraft mc = Minecraft.getInstance();
        MinecraftServer server = mc.getSingleplayerServer();
        if (mc.player == null || mc.level == null || server == null) {
            if (++noWorldTicks % 100 == 0)
                System.out.println("OCEAN26_CAPTAIN_STARTUP_WAIT ticks=" + noWorldTicks
                        + " screen=" + (mc.screen == null ? "null"
                            : mc.screen.getClass().getName()));
            if (noWorldTicks > 1200) fail(mc, "world did not open for captain probe");
            return;
        }
        if (phase == 0) {
            if (MODULAR_VISUAL && !modularSetupRequested) {
                modularSetupRequested = true;
                UUID playerId = mc.player.getUUID();
                server.execute(() -> {
                    ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                    var level = server.getLevel(Level.OVERWORLD);
                    if (player == null || level == null) return;
                    BlockPos anchor = player.blockPosition().offset(64,
                            Math.max(70, player.getBlockY() + 30) - player.getBlockY(), 0);
                    for (int x = -24; x <= 24; x++)
                        for (int z = -30; z <= 30; z++) {
                            BlockPos pos = anchor.offset(x, 0, z);
                            level.setBlock(pos.below(), Blocks.WATER.defaultBlockState(), 2);
                            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                            for (int y = 1; y <= 25; y++)
                                level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 2);
                        }
                    var shipClass = ShipSectionPlan.ShipClass.valueOf(
                            System.getProperty("ocean26.shipClass", "MEDIUM"));
                    var config = ShipBuildConfiguration.initial(shipClass);
                    if (shipClass != ShipSectionPlan.ShipClass.SMALL)
                        config = config.resize(1, 1).withSail(1, 2).withDecks(2)
                                .withBridge(true).withCabin(true);
                    boolean oldCreative = player.getAbilities().instabuild;
                    player.getAbilities().instabuild = true;
                    var item = ShipyardModular.build(player, config);
                    player.getAbilities().instabuild = oldCreative;
                    if (item.isEmpty() || !(item.getItem() instanceof FirstShipItem shipItem)) return;
                    player.setItemInHand(InteractionHand.MAIN_HAND, item.copy());
                    player.teleportTo(anchor.getX() + .5D, anchor.getY() + 2D,
                            anchor.getZ() - 2.5D);
                    player.setYRot(0F);
                    player.setXRot(55F);
                    shipItem.use(level, player, InteractionHand.MAIN_HAND);
                    FirstShipEntity ship = level.getEntitiesOfClass(FirstShipEntity.class,
                            new AABB(anchor).inflate(8D, 20D, 8D)).stream()
                            .filter(s -> s.isNativeHull() && s.modularConfiguration() != null
                                    && s.modularConfiguration().shipClass() == shipClass)
                            .findFirst().orElse(null);
                    if (ship == null) return;
                    int joinedRails = 0;
                    for (BlockPos local : ShipModularBlockHull.localPalette(config).keySet()) {
                        BlockPos world = FirstShipBlockHull.world(ship.blockAnchor(),
                                ship.quarterTurns(), local.getX(), local.getY(), local.getZ());
                        var rail = level.getBlockState(world);
                        if (rail.is(Blocks.SPRUCE_FENCE)
                                && (rail.getValue(net.minecraft.world.level.block.FenceBlock.NORTH)
                                    || rail.getValue(net.minecraft.world.level.block.FenceBlock.SOUTH)
                                    || rail.getValue(net.minecraft.world.level.block.FenceBlock.EAST)
                                    || rail.getValue(net.minecraft.world.level.block.FenceBlock.WEST)))
                            joinedRails++;
                    }
                    System.out.println("OCEAN26_SPAWN_RAILS class=" + shipClass
                            + " joined=" + joinedRails);
                    if (joinedRails == 0) return;
                    if (SAVE_MOVING) {
                        BlockPos chestPos = org.slavicmyths.ships.FirstShipBlockHull.world(
                                ship.blockAnchor(), ship.quarterTurns(), 2, 5, 0);
                        if (!level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 2)
                                || !(level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest)) return;
                        chest.setItem(0, new ItemStack(Items.DIAMOND, 7));
                    }
                    shipId = ship.getUUID();
                    if (RENDER_BENCH) benchAnchor = anchor;
                    player.teleportTo(ship.getX(), ship.deckY() + 2D, ship.getZ() - 6D);
                    modularReady = true;
                });
                return;
            }
            if (MODULAR_VISUAL && !modularReady) {
                if (++elapsed > 400) fail(mc, "modular visual fixture could not be created");
                return;
            }
            for (FirstShipEntity ship : mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))) {
                if (!ship.isNativeHull()) continue;
                if (MODULAR_VISUAL && !ship.getUUID().equals(shipId)) continue;
                if (RELOAD && !ship.isMoving()) continue;
                if (MODULAR_VISUAL && ship.modularConfiguration() == null) {
                    fail(mc, "modular blueprint did not synchronize to client");
                    return;
                }
                if (RELOAD) {
                    phase = 5;
                    UUID reloadedId = ship.getUUID();
                    server.execute(() -> {
                        FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                                .getEntity(reloadedId);
                        net.minecraft.nbt.CompoundTag saved = new net.minecraft.nbt.CompoundTag();
                        if (body != null) body.saveWithoutId(saved);
                        var motion = org.slavicmyths.ships.ShipMovingContraption.load(
                                saved.getCompound("FirstShipMovingBody"));
                        boolean cargoIntact = false;
                        if (motion != null && motion.cargo().size() == 1) {
                            var stored = motion.cargo().getCompound(0)
                                    .getCompound("BlockEntity")
                                    .getList("Items", net.minecraft.nbt.Tag.TAG_COMPOUND);
                            if (stored.size() == 1) {
                                ItemStack diamonds = ItemStack.parseOptional(
                                        server.registryAccess(), stored.getCompound(0));
                                cargoIntact = diamonds.is(Items.DIAMOND)
                                        && diamonds.getCount() == 7;
                            }
                        }
                        boolean passed = body != null && body.isMoving()
                                && (body.modularConfiguration() != null || body.rigLevel() > 0)
                                && cargoIntact;
                        System.out.println("OCEAN26_RELOAD_RESULT ship=" + reloadedId
                                + " moving=" + (body != null && body.isMoving())
                                + " cargoIntact=" + cargoIntact
                                + " pass=" + passed);
                        mc.execute(() -> {
                            if (passed) finish(mc, true, "reload");
                            else fail(mc, "moving ship or cargo did not survive world reload");
                        });
                    });
                    return;
                }
                shipId = ship.getUUID();
                startX = ship.getX();
                startZ = ship.getZ();
                if (SAVE_MOVING) {
                    phase = 4;
                    elapsed = 0;
                    UUID playerId = mc.player.getUUID();
                    server.execute(() -> {
                        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                        FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                                .getEntity(shipId);
                        if (player != null && body != null) {
                            player.teleportTo(body.getX(), body.deckY(),
                                    body.getZ() + body.helmSeatZ());
                            player.startRiding(body);
                        }
                    });
                    return;
                }
                if (VISUAL) {
                    phase = 3;
                    elapsed = 0;
                    mc.options.setCameraType(CameraType.FIRST_PERSON);
                    return;
                }
                UUID playerId = mc.player.getUUID();
                server.execute(() -> {
                    ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                    FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                            .getEntity(shipId);
                    if (player != null && body != null) {
                        boolean mounted;
                        if (body.modularConfiguration() != null) {
                            var seat = FirstShipHull.toWorld(0D, body.helmSeatZ(),
                                    body.getX(), body.getZ(), body.getYRot());
                            player.teleportTo(seat.x(), body.getY() + body.helmSeatHeight(), seat.z());
                            BlockPos helm = FirstShipBlockHull.world(body.blockAnchor(),
                                    body.quarterTurns(), 0, (int)body.helmSeatHeight(),
                                    (int)body.helmWheelZ());
                            mounted = body.tryHelmFromBlock(player, helm);
                        } else {
                            player.teleportTo(body.getX(), body.deckY() + .02D,
                                    body.getZ() - 3D);
                            mounted = player.startRiding(body);
                        }
                        System.out.println("OCEAN26_CLIENT_MOUNT mounted=" + mounted
                                + " moving=" + body.isMoving() + " x=" + body.getX());
                    }
                });
                phase = 1;
                break;
            }
            if (++elapsed > 400) fail(mc, "ship not found in copied world");
            return;
        }
        if (phase == 1) {
            if (!(mc.player.getVehicle() instanceof FirstShipEntity)) {
                if (++elapsed > 500) fail(mc, "mount not synchronized");
                return;
            }
            elapsed = 0;
            phase = 2;
        }
        if (phase == 3) {
            visualTick(mc, server);
            return;
        }
        if (phase == 6) {
            profileShips(mc, server);
            return;
        }
        if (phase == 4) {
            elapsed++;
            UUID playerId = mc.player.getUUID();
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                        .getEntity(shipId);
                if (player != null && body != null && player.getVehicle() == body)
                    body.helmStep(player, org.slavicmyths.ships.FirstShipNetwork.FORWARD);
            });
            if (elapsed == 60) {
                FirstShipEntity body = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                        new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                        .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
                boolean passed = body != null && body.isMoving()
                        && Math.hypot(body.getX() - startX, body.getZ() - startZ) > .1D;
                System.out.println("OCEAN26_SAVE_MOVING_RESULT pass=" + passed);
                if (passed) finish(mc, true, "saveMoving");
                else fail(mc, "moving fixture did not sail before world save");
            }
            return;
        }
        if (phase != 2) return;
        elapsed++;
        // First confirm the integrated client has received the moving hull.
        // In a fresh singleplayer world the authoritative server may detach
        // only around tick 40; the old 20..40 camera window then measured zero
        // frames and incorrectly declared a steering-camera regression.
        if (TURN_CAMERAS && !turnMotionStarted) {
            mc.options.keyUp.setDown(true);
            if (mc.player.getVehicle() instanceof FirstShipEntity ship && ship.isMoving()) {
                turnMotionStarted = true;
                elapsed = 0;
                System.out.println("OCEAN26_TURN_MOTION_READY moving=true ship=" + ship.getId()
                        + " fpsOption=" + mc.options.framerateLimit().get()
                        + " vsync=" + mc.options.enableVsync().get()
                        + " observedFps=" + mc.getFps());
            } else if (elapsed > 300) {
                fail(mc, "turn camera scenario never entered a moving hull");
            }
            return;
        }
        if (elapsed >= 18 && elapsed <= (LONG_FORWARD ? 172 : 42)) {
            FirstShipEntity measured = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            if (measured != null) {
                if (motionSamples > 0) {
                    double step = Math.hypot(measured.getX() - previousMotionX,
                            measured.getZ() - previousMotionZ);
                    double heading = Math.toRadians(measured.getYRot());
                    double forwardStep = -(measured.getX() - previousMotionX) * Math.sin(heading)
                            + (measured.getZ() - previousMotionZ) * Math.cos(heading);
                    mostNegativeForwardStep = Math.min(mostNegativeForwardStep, forwardStep);
                    if (forwardStep < -.005D) reverseMotionFrames++;
                    System.out.println("OCEAN26_MOTION_STEP tick=" + elapsed
                            + " step=" + step + " x=" + measured.getX()
                            + " z=" + measured.getZ());
                    largestMotionStep = Math.max(largestMotionStep, step);
                    largestYawStep = Math.max(largestYawStep, Math.abs(
                            ShipRigidMotion.wrapDegrees(measured.getYRot() - previousMotionYaw)));
                    if (step < .001D) zeroMotionFrames++;
                }
                previousMotionX = measured.getX();
                previousMotionZ = measured.getZ();
                previousMotionYaw = measured.getYRot();
                motionSamples++;
            }
            if (elapsed == (LONG_FORWARD ? 172 : 42)) {
                System.out.println("OCEAN26_MOTION_CADENCE samples=" + motionSamples
                        + " zeroFrames=" + zeroMotionFrames
                        + " reverseFrames=" + reverseMotionFrames
                        + " mostNegativeForwardStep=" + mostNegativeForwardStep
                        + " maxStep=" + largestMotionStep
                        + " maxYawStep=" + largestYawStep);
                if (!LONG_FORWARD && (!MODULAR_VISUAL || TURN_CAMERAS)) {
                    System.out.println("OCEAN26_TURN_SEAT frames=" + turnFrames
                            + " maxError=" + maximumTurnSeatError);
                    if (turnFrames < 10 || maximumTurnSeatError > .04D) {
                        fail(mc, "captain and wheel diverged during rendered turn");
                        return;
                    }
                }
                if (reverseMotionFrames > 0
                        || largestMotionStep > ShipRigidMotion.MAX_FORWARD + .011D
                        || largestYawStep > ShipRigidMotion.MAX_ANGULAR + .11D) {
                    fail(mc, "client motion jumped beyond the physical speed limit");
                    return;
                }
            }
        }
        if (elapsed == 18 && !LONG_FORWARD)
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        if (elapsed == 24 && mc.player.getVehicle() instanceof FirstShipEntity body)
            mc.player.setYRot(body.getYRot() + 90F);
        if (elapsed == 31 && !LONG_FORWARD) {
            mc.options.setCameraType(TURN_CAMERAS ? CameraType.FIRST_PERSON : CameraType.THIRD_PERSON_BACK);
            if (mc.player.getVehicle() instanceof FirstShipEntity body)
                mc.player.setYRot(body.getYRot());
        }
        if (elapsed == 50 && !LONG_FORWARD && (!MODULAR_VISUAL || TURN_CAMERAS))
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        if (elapsed == 20 || elapsed == 65)
            Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(),
                    message -> System.out.println("OCEAN26_SCREENSHOT tick=" + elapsed
                            + " result=" + message.getString()));
        if (elapsed == 29)
            Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(),
                    message -> System.out.println("OCEAN26_CAPTAIN_POSE_SCREENSHOT "
                            + message.getString()));
        boolean throttle = elapsed <= (LONG_FORWARD ? 180 : 42);
        mc.options.keyUp.setDown(throttle);
        mc.options.keyShift.setDown(!LONG_FORWARD && elapsed >= 42 && elapsed <= 44
                || REVERSE && elapsed >= 173 && elapsed <= 175);
        mc.options.keyRight.setDown(!LONG_FORWARD && elapsed >= 20 && elapsed <= (TURN_CAMERAS ? 27 : 35));
        mc.options.keyDown.setDown(MODULAR_VISUAL && elapsed >= 112 && elapsed <= 120
                || REVERSE && elapsed >= 145 && elapsed <= 170);
        // From the starboard handoff cell, walking right heads into the mast.
        // Walk outward along the clear rail when checking free deck movement.
        mc.options.keyLeft.setDown(TURN_CAMERAS && elapsed >= 29 && elapsed <= 35
                || !MODULAR_VISUAL && elapsed >= 112 && elapsed <= (RAIL ? 155 : 120)
                || REVERSE && elapsed >= 150 && elapsed <= 165);
        mc.options.keyJump.setDown(!LONG_FORWARD && elapsed >= 102 && elapsed <= 104);
        // The sustained forward camera scenario must remain mounted. The
        // general-purpose suite enters spectator mode at tick 145; running
        // that unrelated sequence made the "captain" camera probe compare a
        // free-flying spectator against the helm seat until tick 175.
        if (LONG_FORWARD && elapsed < 175) {
            if (!(mc.player.getVehicle() instanceof FirstShipEntity)) {
                fail(mc, "captain detached during sustained forward steering");
            }
            return;
        }
        if (HOLD && elapsed >= 160 && elapsed <= 250) {
            FirstShipEntity body = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            if (body != null) {
                FirstShipHull.Point local = FirstShipHull.toLocal(mc.player.getX(),
                        mc.player.getZ(), body.getX(), body.getZ(), body.getYRot());
                if (mc.player.getY() >= body.deckY() - .05D) holdReachedDeck = true;
                if (holdReachedDeck && local.z() >= 2.8D
                        && local.x() >= -.9D
                        && FirstShipHull.mainDeckSupports(local.x(), local.z()))
                    holdExited = true;
                if (!holdExited) {
                    float angle = holdReachedDeck ? -60F : (float)Math.max(45D,
                            Math.min(135D, 90D + (local.z() - 2D) * 100D));
                    mc.player.setYRot(body.getYRot() + angle);
                    mc.options.keyUp.setDown(true);
                    mc.options.keyJump.setDown(true);
                }
            }
        }
        if (HOLD && elapsed >= 155 && elapsed <= 260)
            holdPeakY = Math.max(holdPeakY, mc.player.getY());
        if (elapsed == 108) {
            FirstShipEntity body = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            if (body != null) mc.player.setYRot(body.getYRot());
        }
        if (elapsed >= 100 && elapsed <= 140)
            jumpPeak = Math.max(jumpPeak, mc.player.getY());
        if (elapsed <= 195 && (elapsed % 10 == 0 || elapsed == 1
                || elapsed >= 40 && elapsed <= 55)) {
            FirstShipEntity clientShip = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            if (clientShip == null) { fail(mc, "client ship disappeared"); return; }
            UUID playerId = mc.player.getUUID();
            int tick = elapsed;
            System.out.println("OCEAN26_CLIENT_TICK tick=" + tick
                    + " geometry=" + clientShip.geometryVersion()
                    + " x=" + clientShip.getX() + " z=" + clientShip.getZ()
                    + " yaw=" + clientShip.getYRot() + " moving=" + clientShip.isMoving()
                    + " rider=" + (mc.player.getVehicle() == clientShip)
                    + " player=" + mc.player.position()
                    + " velocity=" + mc.player.getDeltaMovement()
                    + " local=" + FirstShipHull.toLocal(mc.player.getX(), mc.player.getZ(),
                            clientShip.getX(), clientShip.getZ(), clientShip.getYRot()));
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                        .getEntity(shipId);
                if (player != null && body != null)
                    System.out.println("OCEAN26_SERVER_TICK tick=" + tick
                            + " x=" + body.getX() + " z=" + body.getZ()
                            + " yaw=" + body.getYRot() + " moving=" + body.isMoving()
                            + " speed=" + body.serverMovingSpeed()
                            + " rider=" + (body.getFirstPassenger() == player)
                            + " player=" + player.position()
                            + " velocity=" + player.getDeltaMovement()
                            + " local=" + FirstShipHull.toLocal(player.getX(), player.getZ(),
                                    body.getX(), body.getZ(), body.getYRot()));
            });
        }
        if (elapsed == (LONG_FORWARD ? 175 : 100)) {
            if (!LONG_FORWARD && (!MODULAR_VISUAL || TURN_CAMERAS)) {
                System.out.println("OCEAN26_DISMOUNT_CAMERA frames=" + dismountCameraFrames
                        + " maxLocalStep=" + maximumDismountCameraStep);
                if (dismountCameraFrames < 20 || maximumDismountCameraStep > .12D) {
                    fail(mc, "dismounted camera jerked relative to moving deck");
                    return;
                }
            }
            FirstShipEntity clientShip = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId))
                    .findFirst().orElse(null);
            double traveled = clientShip == null ? 0D : Math.hypot(
                    clientShip.getX() - startX, clientShip.getZ() - startZ);
            FirstShipHull.Point local = clientShip == null ? null : FirstShipHull.toLocal(
                    mc.player.getX(), mc.player.getZ(), clientShip.getX(),
                    clientShip.getZ(), clientShip.getYRot());
            if (TURN_CAMERAS) {
                int clientContact = clientShip == null || local == null ? -1
                        : ShipDeckContact.contactFloor(clientShip, local.x(), local.z(),
                                mc.player.getY());
                double authorityGap = clientShip == null
                        ? -1D : clientShip.clientAuthoritativePoseGap();
                int expectedContact = clientShip == null
                        || clientShip.modularConfiguration() == null ? 1
                        : clientShip.modularConfiguration().bridge() ? 4 : 1;
                System.out.println("OCEAN26_TURN_CAMERA_RESULT turnFrames=" + turnFrames
                        + " seatError=" + maximumTurnSeatError
                        + " dismountFrames=" + dismountCameraFrames
                        + " dismountLocalStep=" + maximumDismountCameraStep
                        + " reverseFrames=" + reverseMotionFrames
                        + " yawStep=" + largestYawStep
                        + " traveled=" + traveled
                        + " clientDeckContact=" + clientContact
                        + " expectedDeckContact=" + expectedContact
                        + " authorityGap=" + authorityGap
                        + " mounted=" + mc.player.isPassenger());
                // Camera motion and an actual Shift dismount are independently
                // checked here. Deck walking and landing support have their
                // own server and real-client regressions; do not accidentally
                // demand that unrelated walk/jump itinerary in a turn probe.
                if (clientShip != null && traveled > 1D
                        && !mc.player.isPassenger()
                        && turnFrames >= 10 && maximumTurnSeatError <= .04D
                        && dismountCameraFrames >= 20
                        && maximumDismountCameraStep <= .12D
                        && authorityGap >= 0D && authorityGap <= 1.25D
                        && clientContact == expectedContact
                        && reverseMotionFrames == 0
                        && largestYawStep > .05D
                        && largestYawStep <= ShipRigidMotion.MAX_ANGULAR + .11D) {
                    finish(mc, true, "turnCamera");
                } else {
                    fail(mc, "turning/Shift dismount camera regression");
                }
                return;
            }
            if (LONG_FORWARD) {
                System.out.println("OCEAN26_LONG_FORWARD traveled=" + traveled
                        + " reverseFrames=" + reverseMotionFrames
                        + " cameraFrames=" + cameraFrames
                        + " reverseCameraFrames=" + reverseCameraFrames
                        + " maximumCameraSeatError=" + maximumCameraSeatError
                         + " oldRiderSeatError=" + maximumRiderOldSeatError
                         + " currentRiderSeatError=" + maximumRiderCurrentSeatError
                        + " seatZ=" + (clientShip == null ? 0D : clientShip.helmSeatZ()));
                if (clientShip != null && traveled > 5D && reverseMotionFrames == 0
                        && cameraFrames > 20 && reverseCameraFrames == 0
                        && maximumCameraSeatError < .12D
                         && maximumRiderOldSeatError < .04D
                         && maximumRiderCurrentSeatError < .04D
                        && (clientShip.modularConfiguration() != null
                            || Math.abs(clientShip.helmSeatZ() + 4D) < .001D))
                    {
                        if (RENDER_BENCH) {
                            phase = 6;
                            benchPrevious = shipId;
                            benchVariant = 0;
                            FirstShipRenderer.setExperimentalBatchEnabled(false);
                            benchOriginalVsync = mc.options.enableVsync().get();
                            benchOriginalFpsLimit = mc.options.framerateLimit().get();
                            mc.options.enableVsync().set(false);
                            mc.options.framerateLimit().set(260);
                            System.out.println("OCEAN26_FRAME_ENV resolution="
                                    + mc.getWindow().getWidth() + "x" + mc.getWindow().getHeight()
                                    + " renderDistance=" + mc.options.renderDistance().get()
                                    + " vsync=" + mc.options.enableVsync().get()
                                    + " fpsOption=" + mc.options.framerateLimit().get()
                                    + " stage=AFTER_LEVEL timing=FRAME_INTERVAL GPU=NOT_MEASURED");
                        } else finish(mc, true, "longForward");
                    }
                else fail(mc, "sustained forward motion reversed or stalled");
                return;
            }
            boolean supported = clientShip != null && local != null
                    && (MODULAR_VISUAL
                        ? ShipDeckContact.contactFloor(clientShip, local.x(), local.z(),
                                mc.player.getY()) >= 0
                        : FirstShipHull.mainDeckSupports(local.x(), local.z())
                            && Math.abs(local.x() - 1D) < .6D
                            && Math.abs(local.z() + 2D) < 1D
                            && mc.player.getY() >= clientShip.deckY() - .1D);
            boolean passed = clientShip != null && traveled > 1D
                    && !mc.player.isPassenger() && supported;
            System.out.println("OCEAN26_CLIENT_RESULT traveled=" + traveled
                    + " player=" + mc.player.position() + " local=" + local
                    + " fps=" + mc.getFps() + " pass=" + passed);
            if (!passed) {
                fail(mc, "motion or deck handoff assertion failed");
                return;
            }
            walkStartX = mc.player.getX();
            walkStartZ = mc.player.getZ();
        }
        if (elapsed == 140) {
            double walked = Math.hypot(mc.player.getX() - walkStartX,
                    mc.player.getZ() - walkStartZ);
            FirstShipEntity jumpingShip = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            double deckSurface = jumpingShip == null ? 67D
                    : jumpingShip.getY() + (MODULAR_VISUAL ? jumpingShip.helmSeatHeight() : 5D);
            boolean passed = (MODULAR_VISUAL || jumpPeak > deckSurface + .4D)
                    && (HOLD || walked > .25D)
                    && mc.player.getY() >= deckSurface - .1D;
            System.out.println("OCEAN26_WALK_JUMP walked=" + walked
                    + " peakY=" + jumpPeak + " finalY=" + mc.player.getY()
                    + " pass=" + passed);
            if (!passed) { fail(mc, "deck walk or jump assertion failed"); return; }
            if (REVERSE) {
                UUID playerId = mc.player.getUUID();
                server.execute(() -> {
                    ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                    FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                            .getEntity(shipId);
                    if (player != null && body != null) {
                        reverseStartX = body.getX();
                        reverseStartZ = body.getZ();
                        reverseStartYaw = body.getYRot();
                        System.out.println("OCEAN26_REBOARD mounted=" + player.startRiding(body));
                    }
                });
            }
        }
        if (RAIL && elapsed == 165) {
            FirstShipEntity body = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            FirstShipHull.Point local = body == null ? null : FirstShipHull.toLocal(
                    mc.player.getX(), mc.player.getZ(), body.getX(), body.getZ(), body.getYRot());
            boolean passed = body != null && local != null
                    && FirstShipHull.mainDeckSupports(local.x(), local.z())
                    && mc.player.getY() >= body.deckY() - .1D;
            System.out.println("OCEAN26_RAIL_RESULT local=" + local
                    + " y=" + mc.player.getY() + " pass=" + passed);
            if (!passed) { fail(mc, "rail collision did not retain deck walker"); return; }
            finish(mc, true, "rail");
        }
        if (HOLD && elapsed == 145) {
            UUID playerId = mc.player.getUUID();
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                        .getEntity(shipId);
                if (player != null && body != null) {
                    FirstShipHull.Point point = FirstShipHull.toWorld(-.65D, 2D,
                            body.getX(), body.getZ(), body.getYRot());
                    holdStartY = body.holdY();
                    player.connection.teleport(point.x(), holdStartY + .02D, point.z(),
                            body.getYRot(), 0F);
                }
            });
        }
        if (HOLD && elapsed == 155) {
            FirstShipEntity body = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            if (body != null) mc.player.setYRot(body.getYRot() + 90F);
            System.out.println("OCEAN26_HOLD_ENTRY y=" + mc.player.getY()
                    + " expected=" + holdStartY + " ladder=" + mc.player.onClimbable());
        }
        if (HOLD && elapsed == 260) {
            FirstShipEntity body = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            FirstShipHull.Point local = body == null ? null : FirstShipHull.toLocal(
                    mc.player.getX(), mc.player.getZ(), body.getX(), body.getZ(), body.getYRot());
            boolean passed = body != null && holdReachedDeck && holdExited
                    && local != null && FirstShipHull.mainDeckSupports(local.x(), local.z())
                    && mc.player.getY() >= body.deckY() - .1D;
            System.out.println("OCEAN26_HOLD_LADDER peakY=" + holdPeakY
                    + " floorY=" + holdStartY + " finalY=" + mc.player.getY()
                    + " local=" + local + " reachedDeck=" + holdReachedDeck
                    + " pass=" + passed);
            if (!passed) { fail(mc, "hold or ladder movement failed"); return; }
            finish(mc, true, "hold");
        }
        if (REVERSE && elapsed == 190) {
            FirstShipEntity body = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            double traveled = body == null ? 0D : Math.hypot(body.getX() - reverseStartX,
                    body.getZ() - reverseStartZ);
            double turned = body == null ? 0D : Math.abs(body.getYRot() - reverseStartYaw);
            boolean passed = body != null && traveled > .3D && turned > 2D
                    && !mc.player.isPassenger();
            System.out.println("OCEAN26_REVERSE_RESULT traveled=" + traveled
                    + " turned=" + turned + " pass=" + passed);
            if (!passed) { fail(mc, "reverse or reboard assertion failed"); return; }
            finish(mc, true, "reverse");
        }
        if (!REVERSE && !RAIL && !HOLD && elapsed == 145) {
            UUID playerId = mc.player.getUUID();
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                        .getEntity(shipId);
                if (player != null && body != null) {
                    player.setGameMode(GameType.SPECTATOR);
                    player.teleportTo(body.getX() + 12D, body.deckY() + 2D,
                            body.getZ() + 5D);
                }
            });
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
        if (!REVERSE && !RAIL && !HOLD && elapsed == 155) {
            mc.player.setYRot(112F);
            mc.player.setXRot(5F);
        }
        if (!REVERSE && !RAIL && !HOLD && elapsed == 165)
            Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(),
                    message -> System.out.println("OCEAN26_EXTERNAL_SCREENSHOT "
                            + message.getString()));
        if (!REVERSE && !RAIL && !HOLD && elapsed == 170) {
            UUID playerId = mc.player.getUUID();
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                        .getEntity(shipId);
                if (player != null && body != null) {
                    FirstShipHull.Point side = FirstShipHull.toWorld(10D, -1D,
                            body.getX(), body.getZ(), body.getYRot());
                    player.teleportTo(side.x(), body.deckY() + 2D, side.z());
                }
            });
        }
        if (!REVERSE && !RAIL && !HOLD && elapsed == 180) {
            FirstShipEntity body = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            if (body != null) {
                mc.player.setYRot((float)Math.toDegrees(Math.atan2(
                        mc.player.getX() - body.getX(), body.getZ() - mc.player.getZ())));
                mc.player.setXRot(5F);
            }
        }
        if (!REVERSE && !RAIL && !HOLD && elapsed == 185)
            Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(),
                    message -> System.out.println("OCEAN26_SIDE_SCREENSHOT "
                            + message.getString()));
        if (!REVERSE && !RAIL && !HOLD && elapsed == 195) {
            System.out.println("OCEAN26_PERF_WITH_SHIP fps=" + mc.getFps()
                    + " mspt=" + (shipTicks == 0 ? -1D
                    : shipTickNanos / 1_000_000D / shipTicks));
            server.execute(() -> {
                FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                        .getEntity(shipId);
                if (body != null) body.discard();
            });
        }
        if (!REVERSE && !RAIL && !HOLD && elapsed == 260) {
            System.out.println("OCEAN26_PERF_WITHOUT_SHIP fps=" + mc.getFps()
                    + " mspt=" + (emptyTicks == 0 ? -1D
                    : emptyTickNanos / 1_000_000D / emptyTicks));
            System.out.println("OCEAN26_OFFBOARD player=" + mc.player.position());
            finish(mc, true, "main");
        }
    }

    private static void visualTick(Minecraft mc, MinecraftServer server) {
        int tick = elapsed++;
        int view = tick / 25;
        int frame = tick % 25;
        double[][] stations = MODULAR_VISUAL
                ? new double[][]{{0D, 21D, 6D}, {16D, 0D, 6D},
                        {0D, -21D, 6D}, {-16D, 0D, 6D}, {0D, 30D, 10D},
                        {0D, -7D, 0D}, {0D, -11D, 5D}}
                : new double[][]{{0D, 11D, 3D}, {11D, 0D, 3D},
                        {0D, -11D, 3D}, {-11D, 0D, 3D}, {0D, 20D, 6D}};
        String[] names = {"bow", "starboard", "stern", "port", "distance",
                "cabin", "bridge"};
        if (view >= stations.length) {
            finish(mc, true, "visual");
            return;
        }
        if (frame == 0) {
            UUID playerId = mc.player.getUUID();
            double[] station = stations[view];
            server.execute(() -> {
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                FirstShipEntity body = (FirstShipEntity)server.getLevel(Level.OVERWORLD)
                        .getEntity(shipId);
                if (player == null || body == null) return;
                player.setGameMode(GameType.SPECTATOR);
                FirstShipHull.Point point = FirstShipHull.toWorld(station[0], station[1],
                        body.getX(), body.getZ(), body.getYRot());
                player.teleportTo(point.x(), body.deckY() + station[2], point.z());
            });
        }
        if (frame == 12 || frame == 17) {
            FirstShipEntity body = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().filter(s -> s.getUUID().equals(shipId)).findFirst().orElse(null);
            if (body == null) { fail(mc, "visual ship missing"); return; }
            double dx = body.getX() - mc.player.getX();
            double dz = body.getZ() - mc.player.getZ();
            mc.player.setYRot((float)Math.toDegrees(Math.atan2(-dx, dz)));
            mc.player.setXRot((float)-Math.toDegrees(Math.atan2(
                    body.deckY() + 2D - mc.player.getEyeY(), Math.hypot(dx, dz))));
            if (frame == 17) {
                System.out.println("OCEAN26_VISUAL_VIEW " + names[view]
                        + " fps=" + mc.getFps() + " ship=" + body.position());
                Screenshot.grab(mc.gameDirectory, mc.getMainRenderTarget(),
                        message -> System.out.println("OCEAN26_VISUAL_SCREENSHOT "
                                + names[view] + " " + message.getString()));
            }
        }
    }

    /** Uses the existing CaptainCi arena; every case is a real detached moving hull. */
    private static ShipBuildConfiguration benchConfig(int index) {
        if (index == 0) return ShipBuildConfiguration.initial(ShipSectionPlan.ShipClass.SMALL);
        ShipSectionPlan.ShipClass type = index == 3
                ? ShipSectionPlan.ShipClass.LARGEST : ShipSectionPlan.ShipClass.LARGE;
        int growth = index == 3 ? 2 : 1;
        ShipBuildConfiguration config = ShipBuildConfiguration.initial(type)
                .resize(growth, growth).withSail(1, 2)
                .withDecks(2).withBridge(true).withCabin(true);
        return index >= 2 ? config.withLighting() : config;
    }

    private static void profileShips(Minecraft mc, MinecraftServer server) {
        mc.options.keyUp.setDown(false);
        mc.options.keyLeft.setDown(false);
        mc.options.keyRight.setDown(false);
        mc.options.keyDown.setDown(false);
        mc.options.keyShift.setDown(false);
        if (benchError != null) {
            fail(mc, "render/frame profile: " + benchError);
            return;
        }
        if (benchSceneDone) {
            if (benchCase >= 0) {
                FirstShipEntity ship = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                        new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D)).stream()
                        .filter(candidate -> candidate.getUUID().equals(benchShipId))
                        .findFirst().orElse(null);
                if (ship == null || !ship.isMoving()
                        || !(mc.getEntityRenderDispatcher().getRenderer(ship) instanceof FirstShipRenderer renderer)
                        || !renderer.profileComplete(benchShipId)) {
                    fail(mc, "ship was not visibly moving throughout frame case " + benchCase
                            + " variant=" + benchVariant);
                    return;
                }
            }
            printFrameResult(frameScenario());
            benchSceneDone = false;
            if (benchCase >= 0 && benchVariant == 0) {
                benchVariant = 1;
                FirstShipRenderer.setExperimentalBatchEnabled(true);
                System.out.println("OCEAN26_AB_SWITCH ship=" + benchCase + " mode=BATCH");
                return;
            }
            if (benchCase >= 0) printAbFrameResult(benchCase);
            benchVariant = 0;
            FirstShipRenderer.setExperimentalBatchEnabled(false);
            benchPrevious = benchShipId;
            benchShipId = null;
            benchPreparing = false;
            benchWait = 0;
            benchCase++;
            if (benchCase == 4) {
                System.out.println("OCEAN26_RENDER_BENCH_DONE cases=8 frameScenarios=9");
                finish(mc, true, "longForward");
            }
            return;
        }
        if (benchCase == -1) {
            if (!benchBaselineReady && !benchPreparing) {
                benchPreparing = true;
                UUID previous = benchPrevious, playerId = mc.player.getUUID();
                server.execute(() -> {
                    try {
                        ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                        var level = server.getLevel(Level.OVERWORLD);
                        if (player == null || level == null || benchAnchor == null)
                            throw new IllegalStateException("Missing baseline player, level or anchor");
                        player.stopRiding();
                        if (previous != null && level.getEntity(previous) instanceof FirstShipEntity old)
                            old.discard();
                        player.setGameMode(GameType.SPECTATOR);
                        player.teleportTo(benchAnchor.getX() + .5D, benchAnchor.getY() + 7D,
                                benchAnchor.getZ() - 3D);
                        player.setYRot(0F);
                        player.setXRot(16F);
                        benchBaselineReady = true;
                    } catch (Throwable error) {
                        benchError = error.toString();
                    }
                });
                return;
            }
            if (!benchBaselineReady) {
                if (++benchWait > 450) fail(mc, "baseline setup timed out");
                return;
            }
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            mc.player.setYRot(0F);
            mc.player.setXRot(16F);
            boolean nearbyShip = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                    new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D))
                    .stream().anyMatch(FirstShipEntity::isMoving);
            if (nearbyShip) {
                if (++benchWait > 450) fail(mc, "baseline has a moving ship");
                return;
            }
            if (!benchSampling) beginFrameCapture();
            return;
        }
        if (benchShipId == null && !benchPreparing) {
            benchPreparing = true;
            int index = benchCase;
            UUID previous = benchPrevious, playerId = mc.player.getUUID();
            server.execute(() -> {
                try {
                    ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                    var level = server.getLevel(Level.OVERWORLD);
                    if (player == null || level == null || benchAnchor == null)
                        throw new IllegalStateException("Missing bench player, level or anchor");
                    player.stopRiding();
                    if (previous != null && level.getEntity(previous) instanceof FirstShipEntity old)
                        old.discard();
                    player.setGameMode(GameType.CREATIVE);
                    ShipBuildConfiguration config = benchConfig(index);
                    boolean originalCreative = player.getAbilities().instabuild;
                    player.getAbilities().instabuild = true;
                    ItemStack item;
                    try { item = ShipyardModular.build(player, config); }
                    finally { player.getAbilities().instabuild = originalCreative; }
                    if (item.isEmpty()) throw new IllegalStateException("Could not build " + config.shipClass());
                    FirstShipEntity ship = RaftContent.FIRST_SHIP.get().create(level);
                    if (ship == null) throw new IllegalStateException("Could not create ship entity");
                    ship.initializeNativeHull(benchAnchor, 0);
                    ship.enableSmoothMotion();
                    if (!ship.loadPacked(item)
                            || !ShipModularBlockHull.place(level, benchAnchor, 0,
                                    config, ship.pendingBlockCargo())
                            || !level.addFreshEntity(ship))
                        throw new IllegalStateException("Could not place " + config.shipClass());
                    FirstShipHull.Point seat = FirstShipHull.toWorld(0D, ship.helmSeatZ(),
                            ship.getX(), ship.getZ(), ship.getYRot());
                    player.teleportTo(seat.x(), ship.getY() + ship.helmSeatHeight(), seat.z());
                    if (!player.startRiding(ship)
                            || !ship.helmStep(player, org.slavicmyths.ships.FirstShipNetwork.FORWARD))
                        throw new IllegalStateException("Could not sail " + config.shipClass());
                    ship.helmStep(player, org.slavicmyths.ships.FirstShipNetwork.HELM_IDLE);
                    benchShipId = ship.getUUID();
                    System.out.println("OCEAN26_RENDER_SCENE class=" + config.shipClass()
                            + " geometry=" + config.hullGeometry()
                            + " size=" + config.width() + "x" + config.length());
                } catch (Throwable error) {
                    benchError = error.toString();
                }
            });
            return;
        }
        if (benchShipId == null) {
            if (++benchWait > 450) fail(mc, "render profile setup timed out");
            return;
        }
        FirstShipEntity ship = mc.level.getEntitiesOfClass(FirstShipEntity.class,
                new AABB(mc.player.blockPosition()).inflate(128D, 32D, 128D)).stream()
                .filter(candidate -> candidate.getUUID().equals(benchShipId))
                .findFirst().orElse(null);
        if (ship != null && ship.isMoving() && mc.player.getVehicle() == ship) {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            mc.player.setYRot(ship.getYRot());
            mc.player.setXRot(16F);
            if (!benchSampling) beginFrameCapture();
            return;
        }
        if (++benchWait > 450)
            fail(mc, "moving/rendered ship not ready for frame case " + benchCase);
    }

    private static void fail(Minecraft mc, String reason) {
        phase = -1;
        System.out.println("OCEAN26_CLIENT_FAIL " + reason);
        finish(mc, false, reason);
    }

    private static void finish(Minecraft mc, boolean passed, String detail) {
        try {
            Files.writeString(mc.gameDirectory.toPath().resolve("ocean26-probe-result.txt"),
                    (passed ? "PASS " : "FAIL ") + detail);
        } catch (IOException error) {
            throw new IllegalStateException("Cannot record client probe result", error);
        } finally {
            try {
                FirstShipRenderer.setExperimentalBatchEnabled(false);
                if (benchOriginalFpsLimit != null && benchOriginalVsync != null) {
                    mc.options.enableVsync().set(benchOriginalVsync);
                    mc.options.framerateLimit().set(benchOriginalFpsLimit);
                    mc.options.save();
                    System.out.println("OCEAN26_FRAME_RESTORE fpsOption="
                            + mc.options.framerateLimit().get()
                            + " vsync=" + mc.options.enableVsync().get());
                }
            } finally {
                benchOriginalFpsLimit = null;
                benchOriginalVsync = null;
                mc.stop();
            }
        }
    }
}
