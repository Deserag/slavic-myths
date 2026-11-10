package org.slavicmyths.verify;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.ships.FirstShipBlockHull;
import org.slavicmyths.ships.FirstShipEntity;
import org.slavicmyths.ships.FirstShipHull;
import org.slavicmyths.ships.FirstShipNetwork;
import org.slavicmyths.ships.RaftContent;

/**
 * Opt-in dedicated server fixture: exercises real vanilla blocks, real BEs
 * and two server players. Does not test network rendering or user key input.
 * Does NOT claim to be a real-world save/restart or manual multiplayer test.
 */
@GameTestHolder("slavicmyths_ocean26")
@PrefixGameTestTemplate(false)
public final class Ocean26GameTests {
    private static final TicketType<Long> VOYAGE_TICKET = TicketType.create(
            "slavicmyths_ocean26_voyage", Long::compare);
    private static final class LocalConnection extends Connection {
        private final EmbeddedChannel wire = new EmbeddedChannel();
        LocalConnection() { super(PacketFlow.SERVERBOUND); }
        @Override public Channel channel() { return wire; }
    }
    private static final class SilentListener extends ServerGamePacketListenerImpl {
        SilentListener(ServerPlayer p) {
            super(p.getServer(), new LocalConnection(), p,
                    CommonListenerCookie.createInitial(p.getGameProfile(), false));
        }
        @Override public void send(Packet<?> packet) {}
        @Override public void send(Packet<?> packet, PacketSendListener listener) {}
    }

    private static ServerPlayer player(ServerLevel level, String name, Vec3 position) {
        ServerPlayer p = new ServerPlayer(level.getServer(), level,
                new GameProfile(UUID.randomUUID(), name), ClientInformation.createDefault());
        p.connection = new SilentListener(p);
        p.setPos(position);
        level.addNewPlayer(p);
        return p;
    }

    private static ChestBlockEntity chest(ServerLevel level, BlockPos position, GameTestHelper t) {
        t.assertTrue(level.getBlockEntity(position) instanceof ChestBlockEntity,
                "Actual vanilla chest BlockEntity missing at " + position);
        return (ChestBlockEntity) level.getBlockEntity(position);
    }

    /**
     * V8 is opt-in for new ships. Verify distinct actual mast/yard blocks for
     * each cloth tier, cargo-safe packing and complete teardown. Legacy V7
     * layout remains validated separately by the existing fixtures.
     */
    @GameTest(template = "empty", timeoutTicks = 200, batch = "ocean_native")
    public static void v8SailTiersBuildAndPackWithoutLeavingBlocks(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        BlockPos anchor = t.absolutePos(new BlockPos(12, 4, 12));
        for (int x = -10; x <= 10; x++)
            for (int z = -15; z <= 15; z++) {
                BlockPos water = anchor.offset(x, 0, z);
                level.setBlock(water.below(), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(water, Blocks.WATER.defaultBlockState(), 2);
                for (int y = 1; y <= 24; y++)
                    level.setBlock(water.above(y), Blocks.AIR.defaultBlockState(), 2);
            }
        for (int tier = 1; tier <= 3; tier++) {
            t.assertTrue(FirstShipBlockHull.place(level, anchor, 0,
                    new ListTag(), 1, tier, 8), "V8 tier " + tier + " build failed");
            int mastTop = tier == 1 ? 13 : tier == 2 ? 14 : 15;
            BlockPos tip = FirstShipBlockHull.world(anchor, 0, 0, mastTop, -1);
            t.assertTrue(!level.getBlockState(tip).isAir(),
                    "V8 tier " + tier + " mast/yard tip absent");
            if (tier == 3)
                t.assertTrue(level.getBlockState(FirstShipBlockHull.world(
                        anchor, 0, 4, 15, -1)).is(Blocks.DARK_OAK_LOG),
                        "Tier III broad wooden upper yard missing");
            t.assertTrue(FirstShipBlockHull.pack(level, anchor, 0, 1, tier, 8) != null,
                    "V8 tier " + tier + " source geometry changed unexpectedly");
            t.assertTrue(FirstShipBlockHull.remove(level, anchor, 0, 1, tier, 8),
                    "V8 tier " + tier + " cannot be packed");
            t.assertTrue(level.getBlockState(tip).isAir(),
                    "V8 tier " + tier + " left orphaned mast blocks after packing");
        }
        // Historical V7 is NOT reinterpreted as V8 when reloaded.
        t.assertTrue(FirstShipBlockHull.place(level, anchor, 0,
                new ListTag(), 1, 1, 7), "Legacy V7 geometry refused placement");
        t.assertTrue(FirstShipBlockHull.pack(level, anchor, 0, 1, 1, 7) != null,
                "Legacy V7 hull was not preserved");
        t.assertTrue(FirstShipBlockHull.remove(level, anchor, 0, 1, 1, 7),
                "Legacy V7 packing failed");
        t.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 230, batch = "ocean_native")
    public static void captainCrewReboardRotateAndPackCargo(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        // Resource validity must be checked IN the loader, not via JSON alone.
        // Previously the GameTest itself passed while all ship recipes failed
        // to load due to 1.21.1 requiring objects for shaped ingredients.
        for (String recipe : new String[]{
                "ship_deck_section", "ship_helm_module", "ship_oar_module",
                "ship_rail_module", "ship_sail_module", "ship_sail_linen_module",
                "ship_sail_reinforced_module", "ship_rigging_upgrade",
                "shipyard_key", "shipyard_workbench", "reinforced_sailcloth",
                "linen_cloth"}) {
            t.assertTrue(level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(
                    "slavicmyths", recipe)).isPresent(), "Ship crafting recipe rejected: " + recipe);
        }
        BlockPos anchor = t.absolutePos(new BlockPos(12, 4, 12));
        // Narrow X fixture: NeoForge schedules GameTests 17 blocks apart; a ±15\n        // area would overwrite the next live ship. Keep X within ±10.\n        // Prepare a disposable shallow ocean, leaving two water-source layers.
        for (int x = -10; x <= 10; x++)
            for (int z = -15; z <= 15; z++) {
                BlockPos column = anchor.offset(x, 0, z);
                level.setBlock(column.below(), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(column, Blocks.WATER.defaultBlockState(), 2);
                for (int y = 1; y <= 24; y++)
                    level.setBlock(column.above(y), Blocks.AIR.defaultBlockState(), 2);
            }

        t.assertTrue(FirstShipBlockHull.place(level, anchor, 0, new ListTag(), 0, 0),
                "Empty native small hull could not be placed");
        // Geometry V5 uses the existing real 3D helm item model, NOT
        // fake slabs and fences; the stern rudder remains real world blocks.
        t.assertTrue(level.getBlockState(FirstShipBlockHull.world(anchor, 0, 0, 8, -4)).isAir()
                && level.getBlockState(FirstShipBlockHull.world(anchor, 0, 2, 8, -4)).isAir()
                && level.getBlockState(FirstShipBlockHull.world(anchor, 0, 0, 1, -6))
                    .is(Blocks.DARK_OAK_PLANKS),
                "V5 fake wheel blocks remained or stern rudder disappeared");
        t.assertTrue(level.getBlockState(FirstShipBlockHull.world(anchor, 0, 0, 1, 0)).isAir()
                && level.getBlockState(FirstShipBlockHull.world(anchor, 0, 0, 2, 0)).isAir()
                && level.getBlockState(FirstShipBlockHull.world(anchor, 0, 0, 3, 0))
                        .is(Blocks.SPRUCE_PLANKS),
                "Dry cargo hold does not have two-block headroom and solid ceiling");
        t.assertTrue(level.getBlockState(FirstShipBlockHull.world(anchor, 0, 0, 4, 2)).isAir()
                && level.getBlockState(FirstShipBlockHull.world(anchor, 0, -1, 3, 2))
                        .is(Blocks.LADDER), "Actual open hatch and ladder are obstructed");
        FirstShipEntity ship = RaftContent.FIRST_SHIP.get().create(level);
        t.assertTrue(ship != null, "Registered native ship entity missing");
        ship.initializeNativeHull(anchor, 0);
        t.assertTrue(level.addFreshEntity(ship), "Server refused ship entity");

        BlockPos cargoLocal = FirstShipBlockHull.world(anchor, 0, 1, 5, 0);
        t.assertTrue(level.setBlock(cargoLocal, Blocks.CHEST.defaultBlockState(), 2),
                "Vanilla chest could not be placed on deck");
        chest(level, cargoLocal, t).setItem(0, new ItemStack(Items.DIAMOND, 7));

        ServerPlayer captain = player(level, "OceanCaptain",
                new Vec3(anchor.getX() + .5D, anchor.getY() + 5.01D,
                        anchor.getZ() - 2.5D));
        ServerPlayer walker = player(level, "OceanWalker",
                new Vec3(anchor.getX() + 2.0D, anchor.getY() + 5.01D,
                        anchor.getZ() + 1.5D));
        t.assertTrue(captain.startRiding(ship), "Captain cannot mount ship");
        t.runAfterDelay(6, () -> {
            t.assertTrue(captain.getVehicle()==ship && ship.getFirstPassenger()==captain,
                    "Captain lost mounted helm before the first move");
            t.assertTrue(FirstShipBlockHull.pack(level,ship.blockAnchor(),ship.quarterTurns(),0,0,5)!=null,
                    "V5 hull invalid before mounted helm action");
            t.assertTrue(ship.helmStep(captain, FirstShipNetwork.FORWARD), "Native voxel sail step rejected");
            BlockPos movedAnchor = anchor.offset(0, 0, 1);
            t.assertTrue(ship.blockAnchor().equals(movedAnchor), "Hull failed to advance one block");
            t.assertTrue(captain.getVehicle() == ship, "Captain lost mount after motion");
            FirstShipHull.Point captainLocal = FirstShipHull.toLocal(captain.getX(), captain.getZ(),
                    ship.getX(), ship.getZ(), ship.getYRot());
            t.assertTrue(Math.abs(captainLocal.z() - FirstShipHull.HELM_Z) < .15D,
                    "Captain remained in world while ship moved");
            FirstShipHull.Point walkerLocal = FirstShipHull.toLocal(walker.getX(), walker.getZ(),
                    ship.getX(), ship.getZ(), ship.getYRot());
            t.assertTrue(FirstShipHull.mainDeckSupports(walkerLocal.x(), walkerLocal.z()),
                    "Second player left the deck on first step");
            BlockPos cargoMoved = FirstShipBlockHull.world(movedAnchor, 0, 1, 5, 0);
            t.assertTrue(chest(level, cargoMoved, t).getItem(0).getCount() == 7,
                    "Chest cargo lost/duplicated on translation");

            captain.stopRiding();
            t.assertTrue(captain.getVehicle() == null, "Dismount failed");
            t.assertTrue(captain.getY() >= ship.deckY() - .1D,
                    "Captain dismounted INSIDE the solid hull");
            FirstShipHull.Point landed = FirstShipHull.toLocal(captain.getX(), captain.getZ(),
                    ship.getX(), ship.getZ(), ship.getYRot());
            t.assertTrue(FirstShipHull.mainDeckSupports(landed.x(), landed.z()),
                    "Dismount landed outside the solid upper deck");
            FirstShipHull.Point helm = FirstShipHull.toWorld(0D, -4D,
                    ship.getX(), ship.getZ(), ship.getYRot());
            captain.lookAt(EntityAnchorArgument.Anchor.EYES,
                    new Vec3(helm.x(), ship.deckY() + 2.5D, helm.z()));
            t.assertTrue(ship.tryHelm(captain), "Second right-click failed after dismount");
            t.assertTrue(ship.helmStep(captain, FirstShipNetwork.RIGHT),
                    "Native 90 degree turn rejected after reboarding");
            t.assertTrue(ship.quarterTurns() == 1, "Ship did not rotate");
            var fence = level.getBlockState(FirstShipBlockHull.world(movedAnchor, 1, 3, 5, 0));
            t.assertTrue(fence.is(Blocks.SPRUCE_FENCE)
                    && fence.getValue(FenceBlock.EAST) && fence.getValue(FenceBlock.WEST),
                    "Physical vanilla fence edge connections did not rotate with hull");
            FirstShipHull.Point walkerTurned = FirstShipHull.toLocal(walker.getX(), walker.getZ(),
                    ship.getX(), ship.getZ(), ship.getYRot());
            t.assertTrue(FirstShipHull.mainDeckSupports(walkerTurned.x(), walkerTurned.z()),
                    "Second player fell outside deck after rotation");
            BlockPos cargoRotated = FirstShipBlockHull.world(movedAnchor, 1, 1, 5, 0);
            t.assertTrue(chest(level, cargoRotated, t).getItem(0).getCount() == 7,
                    "Chest cargo lost/duplicated on quarter turn");

            captain.stopRiding();
            captain.setPos(ship.getX() + 12D, ship.deckY(), ship.getZ());
            walker.setPos(ship.getX() + 12D, ship.deckY(), ship.getZ() + 2D);
            t.assertTrue(ship.packWithKey(captain) == InteractionResult.CONSUME,
                    "Stationary unoccupied ship cannot be safely packed");
            ItemStack packed = ItemStack.EMPTY;
            for (int i = 0; i < captain.getInventory().getContainerSize(); i++) {
                ItemStack value = captain.getInventory().getItem(i);
                if (value.is(RaftContent.FIRST_SHIP_ITEM.get())) { packed = value.copy(); break; }
            }
            t.assertTrue(!packed.isEmpty() && packed.has(DataComponents.CUSTOM_DATA),
                    "Packed ship item lost cargo metadata");
            FirstShipEntity restored = RaftContent.FIRST_SHIP.get().create(level);
            t.assertTrue(restored != null, "Could not recreate packed ship entity");
            restored.initializeNativeHull(movedAnchor, 1);
            t.assertTrue(restored.loadPacked(packed), "Saved cargo/rig metadata was rejected");
            t.assertTrue(FirstShipBlockHull.place(level, movedAnchor, 1,
                    restored.pendingBlockCargo(), restored.rigLevel(), restored.fabricQuality()),
                    "Unpacked ship placement rejected");
            t.assertTrue(chest(level, cargoRotated, t).getItem(0).getCount() == 7,
                    "Vanilla chest inventory did not survive pack/unpack");
            t.assertTrue(level.addFreshEntity(restored), "Could not spawn restored ship");

            // Four tiers + three cloth qualities. This checks the REAL server
            // inventory transaction and real rebuilt chest BlockEntity at each
            // stage; at end dismantling returns exactly all consumed materials.
            FirstShipHull.Point mastWalk = FirstShipHull.toWorld(
                    1D, FirstShipHull.MAST_Z - 1D, restored.getX(),
                    restored.getZ(), restored.getYRot());
            FirstShipHull.Point mastAim = FirstShipHull.toWorld(
                    0D, FirstShipHull.MAST_Z, restored.getX(),
                    restored.getZ(), restored.getYRot());
            captain.setPos(mastWalk.x(), restored.deckY() + .01D, mastWalk.z());
            captain.lookAt(EntityAnchorArgument.Anchor.EYES,
                    new Vec3(mastAim.x(), restored.deckY() + 2.5D, mastAim.z()));
            captain.getInventory().setItem(0, new ItemStack(RaftContent.SAIL_MODULE.get(), 5));
            captain.getInventory().setItem(1, new ItemStack(RaftContent.SAIL_RIGGING_KIT.get(), 3));
            captain.getInventory().setItem(2, new ItemStack(RaftContent.SAIL_LINEN_MODULE.get(), 4));
            captain.getInventory().setItem(3, new ItemStack(RaftContent.SAIL_REINFORCED_MODULE.get(), 4));
            captain.getInventory().selected = 0;
            t.assertTrue(restored.configureRigging(captain, FirstShipNetwork.RIG_CONFIGURE)
                    && restored.rigLevel() == 1 && restored.fabricQuality() == 1,
                    "First standalone wool sail could not be installed");
            captain.getInventory().selected = 1;
            for (int tier = 2; tier <= 4; tier++) {
                t.assertTrue(restored.configureRigging(captain, FirstShipNetwork.RIG_CONFIGURE)
                        && restored.rigLevel() == tier, "Failed to craft tier " + tier);
                t.assertTrue(chest(level, cargoRotated, t).getItem(0).getCount() == 7,
                        "Chest contents changed during mast tier expansion");
            }
            t.assertTrue(captain.getInventory().countItem(RaftContent.SAIL_MODULE.get()) == 1,
                    "Tier upgrades failed to charge proportional wool canvas");
            captain.getInventory().selected = 2;
            t.assertTrue(restored.configureRigging(captain, FirstShipNetwork.RIG_CONFIGURE)
                    && restored.fabricQuality() == 2, "Linen sail replacement failed");
            captain.getInventory().selected = 3;
            t.assertTrue(restored.configureRigging(captain, FirstShipNetwork.RIG_CONFIGURE)
                    && restored.fabricQuality() == 3, "Reinforced sail replacement failed");
            t.assertTrue(chest(level, cargoRotated, t).getItem(0).getCount() == 7,
                    "Real chest changed during four-tier fabric replacement");
            captain.getInventory().selected = 8;
            captain.setShiftKeyDown(true);
            for (int tier = 3; tier >= 0; tier--) {
                t.assertTrue(captain.getMainHandItem().isEmpty(), "Removal requires empty hand");
                t.assertTrue(restored.configureRigging(captain, FirstShipNetwork.RIG_REMOVE)
                        && restored.rigLevel() == tier, "Safe sail dismantling failed at tier " + tier);
            }
            captain.setShiftKeyDown(false);
            t.assertTrue(captain.getInventory().countItem(RaftContent.SAIL_MODULE.get()) == 5
                    && captain.getInventory().countItem(RaftContent.SAIL_RIGGING_KIT.get()) == 3
                    && captain.getInventory().countItem(RaftContent.SAIL_LINEN_MODULE.get()) == 4
                    && captain.getInventory().countItem(RaftContent.SAIL_REINFORCED_MODULE.get()) == 4,
                    "Sail/mast craft-refund conservation violated: item duplication or loss");
            t.assertTrue(chest(level, cargoRotated, t).getItem(0).getCount() == 7,
                    "Native container contents changed after all sail operations");
            captain.setPos(restored.getX() + 12D, restored.deckY(), restored.getZ());
            t.assertTrue(FirstShipBlockHull.remove(level, movedAnchor, 1,
                    restored.rigLevel(), restored.fabricQuality()), "Cleanup hull failed");
            restored.discard();
            captain.discard();
            walker.discard();
            t.succeed();
        });
    }

    /**
     * Regression for the real-world lily-pad stop: assert that blocked moves
     * leave BOTH the hard obstacle and another soft obstacle untouched.
     * These are real ServerLevel blocks, not a mocked canWrite boolean.
     */
    @GameTest(template = "empty", timeoutTicks = 260, batch = "ocean_native")
    public static void clearSoftWaterObstaclesButPreserveHardBlocksAndCargo(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        BlockPos origin = t.absolutePos(new BlockPos(12, 4, 12));
        for (int x = -10; x <= 10; x++)
            for (int z = -15; z <= 15; z++) {
                BlockPos column = origin.offset(x, 0, z);
                level.setBlock(column.below(), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(column, Blocks.WATER.defaultBlockState(), 2);
                for (int y = 1; y <= 24; y++)
                    level.setBlock(column.above(y), Blocks.AIR.defaultBlockState(), 2);
            }
        t.assertTrue(FirstShipBlockHull.place(level, origin, 0, new ListTag(), 0, 0),
                "V5 bare hull failed to place on two water source layers");
        t.assertTrue(level.getBlockState(FirstShipBlockHull.world(origin, 0, 0, 11, -1))
                .is(Blocks.STRIPPED_SPRUCE_LOG),
                "V5 naked ship has no full-height mast");
        BlockPos cargo = FirstShipBlockHull.world(origin, 0, 1, 5, 0);
        level.setBlock(cargo, Blocks.CHEST.defaultBlockState(), 2);
        chest(level, cargo, t).setItem(0, new ItemStack(Items.DIAMOND, 13));

        // Only positions swept by the NEXT footprint, not its current blocks.
        BlockPos lily = origin.offset(0, 1, 6);
        BlockPos grass = origin.offset(0, 0, 6);
        BlockPos kelp = origin.offset(-1, 0, 5);
        BlockPos coral = origin.offset(1, 0, 5);
        level.setBlock(grass, Blocks.SEAGRASS.defaultBlockState(), 2);
        level.setBlock(lily, Blocks.LILY_PAD.defaultBlockState(), 2);
        level.setBlock(kelp, Blocks.KELP.defaultBlockState(), 2);
        level.setBlock(coral, Blocks.TUBE_CORAL_BLOCK.defaultBlockState(), 2);
        BlockPos forward = origin.offset(0, 0, 1);
        t.assertTrue(FirstShipBlockHull.move(level, origin, 0, forward, 0, 0, 0, 0, 0, 5),
                "Lily pad, seagrass, kelp or coral incorrectly blocked the first move");
        for (BlockPos pos : new BlockPos[]{lily, grass, kelp, coral})
            t.assertTrue(!level.getBlockState(pos).is(Blocks.LILY_PAD)
                    && !level.getBlockState(pos).is(Blocks.SEAGRASS)
                    && !level.getBlockState(pos).is(Blocks.KELP)
                    && !level.getBlockState(pos).is(Blocks.TUBE_CORAL_BLOCK),
                    "Soft obstacle remained after successful passage: " + pos);
        t.assertTrue(chest(level, FirstShipBlockHull.world(forward, 0, 1, 5, 0), t)
                .getItem(0).getCount() == 13, "Cargo lost on forward clearance");

        // The turn would cross these cells outside the current narrow hull.
        BlockPos stone = FirstShipBlockHull.world(forward, 1, 0, 1, 5);
        BlockPos survivingLily = FirstShipBlockHull.world(forward, 1, 0, 1, 4);
        level.setBlock(stone, Blocks.STONE.defaultBlockState(), 2);
        level.setBlock(survivingLily, Blocks.LILY_PAD.defaultBlockState(), 2);
        t.assertTrue(!FirstShipBlockHull.move(level, forward, 0, forward, 1, 0, 0, 0, 0, 5),
                "Stone wall did not block a 90 degree turn");
        t.assertTrue(level.getBlockState(stone).is(Blocks.STONE)
                && level.getBlockState(survivingLily).is(Blocks.LILY_PAD),
                "Rejected turn partially destroyed the world");
        t.assertTrue(chest(level, FirstShipBlockHull.world(forward, 0, 1, 5, 0), t)
                .getItem(0).getCount() == 13, "Rejected turn damaged cargo");
        // Stone sits one block ABOVE the waterline; restore air, not water.
        level.setBlock(stone, Blocks.AIR.defaultBlockState(), 2);

        // Four consecutive turns must return to the exact initial heading,
        // retaining the same real chest BE and its 13 diamonds at each step.
        for (int turn = 1; turn <= 4; turn++) {
            int from = (turn - 1) % 4, to = turn % 4;
            t.assertTrue(FirstShipBlockHull.move(level, forward, from, forward, to, 0, 0, 0, 0, 5),
                    "Turn " + turn + " rejected with otherwise open water");
            t.assertTrue(chest(level, FirstShipBlockHull.world(forward, to, 1, 5, 0), t)
                    .getItem(0).getCount() == 13,
                    "Cargo damaged by turn " + turn);
        }
        t.assertTrue(FirstShipBlockHull.move(level, forward, 0, origin, 0, 0, 0, 0, 0, 5),
                "Backward movement after four turns was rejected");
        t.assertTrue(chest(level, cargo, t).getItem(0).getCount() == 13,
                "Backward movement damaged or duplicated chest cargo");
        t.assertTrue(FirstShipBlockHull.remove(level, origin, 0, 0, 0, 5),
                "Cleanup failed after obstacle and rotation checks");
        t.succeed();
    }

    /**
     * Regression for the user-reported "ship moves, people stay behind" bug.
     * Repeated server-authoritative voxel steps must preserve each crew member's
     * own local coordinates; this still cannot replace a real CLIENT visual test.
     */
    @GameTest(template = "empty", timeoutTicks = 270, batch = "ocean_native")
    public static void walkingCrewAndCaptainFollowRepeatedHelmCommands(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        BlockPos origin = t.absolutePos(new BlockPos(12, 4, 12));
        for (int x = -10; x <= 10; x++)
            for (int z = -15; z <= 15; z++) {
                BlockPos pos = origin.offset(x, 0, z);
                level.setBlock(pos.below(), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(pos, Blocks.WATER.defaultBlockState(), 2);
                for (int y = 1; y <= 24; y++)
                    level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 2);
            }
        t.assertTrue(FirstShipBlockHull.place(level, origin, 0, new ListTag(), 0, 0, 5),
                "V5 hull could not be placed in crew regression world");
        FirstShipEntity ship = RaftContent.FIRST_SHIP.get().create(level);
        t.assertTrue(ship != null,"Ship entity missing");
        ship.initializeNativeHull(origin,0);
        t.assertTrue(level.addFreshEntity(ship),"Ship entity spawn rejected");

        ServerPlayer captain = player(level,"LongVoyageCaptain",
                new Vec3(ship.getX(),ship.deckY()+.08D,ship.getZ()-3D));
        ServerPlayer deckWalker = player(level,"LongVoyageDeck",
                new Vec3(ship.getX()+2D,ship.deckY()+.02D,ship.getZ()+1D));
        ServerPlayer holdWalker = player(level,"LongVoyageHold",
                new Vec3(ship.getX(),ship.holdY()+.02D,ship.getZ()));
        ServerPlayer outsider = player(level,"LongVoyageOutsider",
                new Vec3(ship.getX()+8D,ship.deckY()+.02D,ship.getZ()));
        t.assertTrue(captain.startRiding(ship),"Captain failed to mount wheel");
        final Vec3 outsiderStart = outsider.position();
        int[] steps = {FirstShipNetwork.FORWARD,FirstShipNetwork.FORWARD,
                FirstShipNetwork.RIGHT,FirstShipNetwork.FORWARD,
                FirstShipNetwork.LEFT,FirstShipNetwork.BACKWARD};
        // All events are registered up-front at absolute GameTest tick times.
        // Recursive delayed callbacks were not reliably scheduled by the runner.
        for(int i=0;i<steps.length;i++){
            final int step=i;
            t.runAtTickTime(5L+24L*step,()->{
                System.out.println("OCEAN26_MULTISTEP_CREW_BEGIN step="+step);
                FirstShipHull.Point beforeDeck = FirstShipHull.toLocal(
                        deckWalker.getX(),deckWalker.getZ(),ship.getX(),ship.getZ(),ship.shipYaw());
                FirstShipHull.Point beforeHold = FirstShipHull.toLocal(
                        holdWalker.getX(),holdWalker.getZ(),ship.getX(),ship.getZ(),ship.shipYaw());
                t.assertTrue(captain.getVehicle()==ship && ship.getFirstPassenger()==captain,
                        "Captain not riding immediately before helm step "+step);
                t.assertTrue(FirstShipBlockHull.pack(level,ship.blockAnchor(),ship.quarterTurns(),0,0,5)!=null,
                        "Physical hull failed source inspection before helm step "+step);
                t.assertTrue(ship.helmStep(captain,steps[step]),
                        "Helm step "+step+" rejected");
                FirstShipHull.Point afterDeck = FirstShipHull.toLocal(
                        deckWalker.getX(),deckWalker.getZ(),ship.getX(),ship.getZ(),ship.shipYaw());
                FirstShipHull.Point afterHold = FirstShipHull.toLocal(
                        holdWalker.getX(),holdWalker.getZ(),ship.getX(),ship.getZ(),ship.shipYaw());
                FirstShipHull.Point helm = FirstShipHull.toLocal(
                        captain.getX(),captain.getZ(),ship.getX(),ship.getZ(),ship.shipYaw());
                t.assertTrue(Math.hypot(afterDeck.x()-beforeDeck.x(),afterDeck.z()-beforeDeck.z())<.03D,
                        "Deck walker LEFT BEHIND on step "+step);
                t.assertTrue(Math.hypot(afterHold.x()-beforeHold.x(),afterHold.z()-beforeHold.z())<.03D,
                        "Hold walker LEFT BEHIND on step "+step);
                t.assertTrue(captain.getVehicle()==ship
                        && Math.abs(helm.z()-FirstShipHull.HELM_Z)<.12D,
                        "Mounted captain LEFT BEHIND on step "+step);
                t.assertTrue(Math.hypot(outsider.getX()-outsiderStart.x,outsider.getZ()-outsiderStart.z)<.02D,
                        "Unrelated player was wrongly carried on step "+step);
                if(step==steps.length-1) {
                    t.assertTrue(FirstShipBlockHull.remove(level,ship.blockAnchor(),
                            ship.quarterTurns(),0,0,5),"V5 hull teardown failed");
                    ship.discard();
                    captain.discard();
                    deckWalker.discard();
                    holdWalker.discard();
                    outsider.discard();
                    System.out.println("OCEAN26_MULTISTEP_CREW_PASS steps=6 captain=true deckWalker=true holdWalker=true outsiderStable=true");
                    t.succeed();
                }
            });
        }
    }

    /** Workshop transactions are real item/NTB operations, not UI simulation. */
    @GameTest(template = "empty", timeoutTicks = 120, batch = "ocean_workshop")
    public static void shipyardSailsAndRaftBlueprints(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        ServerPlayer p = player(level, "OceanShipwright",
                Vec3.atCenterOf(t.absolutePos(new BlockPos(2, 2, 2))));
        p.getInventory().setItem(0, new ItemStack(RaftContent.SAIL_MODULE.get(), 10));
        p.getInventory().setItem(1, new ItemStack(RaftContent.SAIL_LINEN_MODULE.get(), 10));
        p.getInventory().setItem(2, new ItemStack(RaftContent.SAIL_RIGGING_KIT.get(), 3));
        p.getInventory().setItem(3, new ItemStack(RaftContent.DECK_SECTION.get(), 40));
        p.getInventory().setItem(4, new ItemStack(RaftContent.OAR_MODULE.get(), 2));
        ItemStack ship = new ItemStack(RaftContent.FIRST_SHIP_ITEM.get());
        ship = org.slavicmyths.ships.ShipyardUpgrades.outfitFirstShip(p, ship, 20);
        t.assertTrue(!ship.isEmpty()
                && org.slavicmyths.ships.ShipyardUpgrades.shipRigLevel(ship) == 1
                && org.slavicmyths.ships.ShipyardUpgrades.shipFabric(ship) == 1,
                "Workshop failed to install real wool sail module");
        ship = org.slavicmyths.ships.ShipyardUpgrades.outfitFirstShip(p, ship, 23);
        t.assertTrue(!ship.isEmpty()
                && org.slavicmyths.ships.ShipyardUpgrades.shipRigLevel(ship) == 2,
                "Workshop mast upgrade failed");
        ship = org.slavicmyths.ships.ShipyardUpgrades.outfitFirstShip(p, ship, 21);
        t.assertTrue(!ship.isEmpty()
                && org.slavicmyths.ships.ShipyardUpgrades.shipFabric(ship) == 2,
                "Workshop fabric replacement failed");
        FirstShipEntity testShip = RaftContent.FIRST_SHIP.get().create(level);
        t.assertTrue(testShip != null && testShip.loadPacked(ship)
                && testShip.rigLevel() == 2 && testShip.fabricQuality() == 2
                && testShip.geometryVersion() == 8,
                "Workshop packed sails were rejected by the actual ship entity");
        ItemStack removed = org.slavicmyths.ships.ShipyardUpgrades.outfitFirstShip(p, ship, 25);
        t.assertTrue(!removed.isEmpty()
                && org.slavicmyths.ships.ShipyardUpgrades.shipRigLevel(removed) == 0
                && org.slavicmyths.ships.ShipyardUpgrades.shipFabric(removed) == 0
                && p.getInventory().countItem(RaftContent.SAIL_MODULE.get()) == 10
                && p.getInventory().countItem(RaftContent.SAIL_LINEN_MODULE.get()) == 10
                && p.getInventory().countItem(RaftContent.SAIL_RIGGING_KIT.get()) == 3,
                "Sail/rig conservation failed through complete workshop cycle");

        ItemStack raft = org.slavicmyths.ships.ShipyardUpgrades.buildRaft(p,
                org.slavicmyths.ships.RaftSize.CARGO);
        t.assertTrue(raft.is(RaftContent.RAFT_CARGO.get())
                && p.getInventory().countItem(RaftContent.DECK_SECTION.get()) == 5
                && p.getInventory().countItem(RaftContent.OAR_MODULE.get()) == 1,
                "Large raft blueprint failed to consume exactly 35 sections and 1 oars");
        org.slavicmyths.ships.RaftBoatEntity raftEntity = RaftContent.RAFT.get().create(level);
        t.assertTrue(raftEntity != null, "Existing raft entity registration broke");
        raftEntity.configure(org.slavicmyths.ships.RaftSize.CARGO);
        BlockPos raftWater = t.absolutePos(new BlockPos(8, 2, 8));
        for (int dx = -3; dx <= 3; dx++)
            for (int dz = -3; dz <= 3; dz++)
                level.setBlock(raftWater.offset(dx, 0, dz), Blocks.WATER.defaultBlockState(), 2);
        raftEntity.setPos(raftWater.getX() + .5D, raftWater.getY() + .86D,
                raftWater.getZ() + .5D);
        raftEntity.setWaterSurface(raftWater.getY() + 1D);
        t.assertTrue(level.addFreshEntity(raftEntity), "Existing raft placement broke");
        double raftStartX = raftEntity.getX();
        raftEntity.setDeltaMovement(.12D, 0D, 0D);
        for (int i = 0; i < 5; i++) raftEntity.tick();
        t.assertTrue(raftEntity.getX() > raftStartX + .02D,
                "Existing raft no longer moves across water");
        CompoundTag raftData = new CompoundTag();
        raftEntity.saveWithoutId(raftData);
        org.slavicmyths.ships.RaftBoatEntity loadedRaft = RaftContent.RAFT.get().create(level);
        t.assertTrue(loadedRaft != null, "Existing raft entity reload failed to create");
        loadedRaft.load(raftData);
        t.assertTrue(loadedRaft.deckWidth() == raftEntity.deckWidth()
                && loadedRaft.deckLength() == raftEntity.deckLength(),
                "Existing raft dimensions changed through save/load");
        raftEntity.discard();
        loadedRaft.discard();
        testShip.discard();
        p.discard();
        System.out.println("OCEAN26_SHIPYARD_PASS sails=true mast=true linenSwap=true refunds=true blueprint=true");
        t.succeed();
    }

    /**
     * Transactional rehearsal for Create-style assemble/detach/park. This uses
     * genuine chest BlockEntities and actual vanilla world blocks; it does NOT
     * start the moving-renderer mode or rely on a fake hull.
     */
    @GameTest(template = "empty", timeoutTicks = 160, batch = "ocean_motion")
    public static void reversibleContraptionHandoffPreservesCargo(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        BlockPos from = t.absolutePos(new BlockPos(12, 4, 12));
        for (int x = -10; x <= 10; x++)
            for (int z = -15; z <= 15; z++) {
                BlockPos column = from.offset(x, 0, z);
                level.setBlock(column.below(), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(column, Blocks.WATER.defaultBlockState(), 2);
                for (int y = 1; y <= 24; y++)
                    level.setBlock(column.above(y), Blocks.AIR.defaultBlockState(), 2);
            }
        t.assertTrue(FirstShipBlockHull.place(level, from, 0, new ListTag(), 0, 0, 5),
                "Moving-snapshot test source ship could not be placed");
        BlockPos cargo = FirstShipBlockHull.world(from, 0, 1, 5, 0);
        level.setBlock(cargo, Blocks.CHEST.defaultBlockState(), 2);
        chest(level, cargo, t).setItem(0, new ItemStack(Items.DIAMOND, 17));

        FirstShipBlockHull.MotionSnapshot snapshot =
                FirstShipBlockHull.captureForMotion(level, from, 0, 0, 0, 5);
        t.assertTrue(snapshot != null && snapshot.cargo().size() == 1
                && !FirstShipBlockHull.localMotionPalette(snapshot).isEmpty(),
                "Failed to capture local palette or chest NBT");
        chest(level, cargo, t).setItem(0, new ItemStack(Items.DIAMOND, 18));
        t.assertTrue(!FirstShipBlockHull.detachForMotion(level, from, snapshot)
                && chest(level, cargo, t).getItem(0).getCount() == 18,
                "Stale snapshot was able to remove changed cargo");
        chest(level, cargo, t).setItem(0, new ItemStack(Items.DIAMOND, 17));
        t.assertTrue(FirstShipBlockHull.detachForMotion(level, from, snapshot),
                "Validated intact source ship failed to detach");
        t.assertTrue(level.getBlockState(cargo).isAir()
                && level.getBlockState(FirstShipBlockHull.world(from, 0, 0, 8, -1)).isAir(),
                "Detached vessel left world-collision blocks behind");

        var palette = FirstShipBlockHull.localMotionPalette(snapshot);
        var sailingPose = new org.slavicmyths.ships.ShipRigidMotion.Pose(
                from.getX() + .5D, from.getZ() + .5D, 37.125D);
        t.assertTrue(org.slavicmyths.ships.ShipMovingClearance.clearPose(
                level, sailingPose, from.getY(), palette),
                "Continuous rotated hull reports false collision on clear water");
        FirstShipHull.Point obstruction = FirstShipHull.toWorld(
                0D, 4D, sailingPose.x(), sailingPose.z(), sailingPose.yaw());
        BlockPos bowStone = BlockPos.containing(obstruction.x(), from.getY() + 2D,
                obstruction.z());
        level.setBlock(bowStone, Blocks.STONE.defaultBlockState(), 2);
        t.assertTrue(!org.slavicmyths.ships.ShipMovingClearance.clearPose(
                level, sailingPose, from.getY(), palette),
                "Swept angled hull incorrectly passed through a solid obstruction");
        level.setBlock(bowStone, Blocks.AIR.defaultBlockState(), 2);

        BlockPos dest = from.offset(0, 0, 2);
        BlockPos obstacle = FirstShipBlockHull.world(dest, 0, 0, 2, 5);
        level.setBlock(obstacle, Blocks.STONE.defaultBlockState(), 2);
        t.assertTrue(!FirstShipBlockHull.restoreFromMotion(level, dest, 0, snapshot)
                && level.getBlockState(obstacle).is(Blocks.STONE),
                "Blocked parking incorrectly destroyed a solid player block");
        level.setBlock(obstacle, Blocks.AIR.defaultBlockState(), 2);
        t.assertTrue(FirstShipBlockHull.restoreFromMotion(level, dest, 0, snapshot),
                "Clear destination rejected a moving-ship parked snapshot");
        t.assertTrue(chest(level, FirstShipBlockHull.world(dest, 0, 1, 5, 0), t)
                .getItem(0).getCount() == 17,
                "Moving-contraption park lost or duplicated chest diamonds");
        t.assertTrue(FirstShipBlockHull.remove(level, dest, 0, 0, 0, 5),
                "Failed to clean up parked ship");
        System.out.println("OCEAN26_CONTRAPTION_HANDOFF_PASS snapshot=true staleCargoGuard=true detach=true angle37=true rotatedCollision=true blockedParkGuard=true restore=true inventory=17");
        t.succeed();
    }

    /**
     * Headless proof of the NEW continuous rigid-contraption lifecycle. Save
     * and reload with a real chest BlockEntity detached from normal chunks.
     * Rendering and local player physics still require runClient acceptance.
     */
    @GameTest(template = "empty", timeoutTicks = 160, batch = "ocean_motion_state")
    public static void continuousMotionNbtAndParking(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        BlockPos anchor = t.absolutePos(new BlockPos(12, 4, 12));
        for (int x = -9; x <= 9; x++)
            for (int z = -13; z <= 13; z++) {
                BlockPos col = anchor.offset(x, 0, z);
                level.setBlock(col.below(), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(col, Blocks.WATER.defaultBlockState(), 2);
                for (int y = 1; y <= 24; y++)
                    level.setBlock(col.above(y), Blocks.AIR.defaultBlockState(), 2);
            }
        t.assertTrue(FirstShipBlockHull.place(level, anchor, 0,
                new ListTag(), 0, 0, 5), "Cannot place continuous-mode source");
        BlockPos storage = FirstShipBlockHull.world(anchor, 0, 1, 5, 0);
        level.setBlock(storage, Blocks.CHEST.defaultBlockState(), 2);
        chest(level, storage, t).setItem(0, new ItemStack(Items.EMERALD, 23));
        var body = org.slavicmyths.ships.ShipMovingContraption.prepare(
                level, anchor, 0, 0, 0, 5);
        t.assertTrue(body != null && body.snapshot().cargo().size() == 1,
                "Contraption preparation dropped physical cargo");
        t.assertTrue(body.detach(level, anchor),
                "Contraption failed to detach after complete NBT checkpoint");
        t.assertTrue(level.getBlockState(storage).isAir(),
                "Moving chest incorrectly remained in world");
        BlockPos waterloggedWall = anchor.offset(0, 0, 4);
        level.setBlock(waterloggedWall, Blocks.OAK_SLAB.defaultBlockState()
                .setValue(SlabBlock.WATERLOGGED, true), 2);
        t.assertTrue(!org.slavicmyths.ships.ShipMovingClearance.clearPose(level,
                body.pose(), body.waterlineY(), body.palette()),
                "Waterlogged solid construction was treated as open water");
        level.setBlock(waterloggedWall, Blocks.TUBE_CORAL_BLOCK.defaultBlockState(), 2);
        t.assertTrue(!org.slavicmyths.ships.ShipMovingClearance.clearPose(level,
                body.pose(), body.waterlineY(), body.palette()),
                "Solid coral block was treated as a harmless plant");
        level.setBlock(waterloggedWall, Blocks.WATER.defaultBlockState(), 2);
        BlockPos shoal = anchor.below();
        level.setBlock(shoal, Blocks.STONE.defaultBlockState(), 2);
        t.assertTrue(!org.slavicmyths.ships.ShipMovingClearance.clearPose(level,
                body.pose(), body.waterlineY(), body.palette()),
                "Moving hull accepted a stone shoal below the waterline");
        level.setBlock(shoal, Blocks.WATER.defaultBlockState(), 2);
        BlockPos lily = anchor.offset(0, 0, 3);
        level.setBlock(lily, Blocks.LILY_PAD.defaultBlockState(), 2);
        var throttle = new org.slavicmyths.ships.ShipRigidMotion.Control(.7D,.4D);
        for (int tick = 0; tick < 25; tick++) {
            var frame = body.tick(level, throttle);
            t.assertTrue(!frame.blocked(),"Real world collision denied open-water fractional motion");
        }
        t.assertTrue(!level.getBlockState(lily).is(Blocks.LILY_PAD),
                "Moving lower hull failed to clear a crossed lily pad");
        t.assertTrue(body.pose().z() > anchor.getZ() + .8D
                && Math.abs(body.pose().yaw()) > 1D
                && Math.abs(body.pose().yaw()) < 75D,
                "Continuous ship still snaps to integer blocks or 90-degree turns");
        CompoundTag saved = body.save();
        var reloaded = org.slavicmyths.ships.ShipMovingContraption.load(saved);
        t.assertTrue(reloaded != null && reloaded.detached()
                && Math.abs(reloaded.pose().x()-body.pose().x()) < 1E-9D
                && Math.abs(reloaded.pose().z()-body.pose().z()) < 1E-9D
                && Math.abs(reloaded.pose().yaw()-body.pose().yaw()) < 1E-9D
                && reloaded.snapshot().cargo().equals(body.snapshot().cargo()),
                "Fractional pose or chest contents corrupted by NBT save/reload");
        t.assertTrue(!reloaded.park(level),
                "Ship parked mid-turn without approaching a safe cardinal bearing");
        reloaded.motion().restore(new org.slavicmyths.ships.ShipRigidMotion.Pose(
                anchor.getX()+.5D,anchor.getZ()+.5D,0D),0D,0D);
        t.assertTrue(reloaded.park(level),
                "Aligned stationary ship failed to restore physical world blocks");
        t.assertTrue(chest(level,storage,t).getItem(0).getCount()==23,
                "Moving ship's 23 emeralds lost or duplicated on reload and park");
        t.assertTrue(FirstShipBlockHull.remove(level,anchor,0,0,0,5),
                "Moving ship post-park cleanup failed");
        System.out.println("OCEAN26_CONTINUOUS_BODY_PASS fractional=true steering=true collision=true nbtReload=true cargo=23 park=true");
        t.succeed();
    }

    /**
     * First LIVE-ENTITY regression for the new Create-style moving palette.
     * A new ship opts in; a previously loaded V5 ship keeps the old mode.
     * This is a real integrated server-world voyage, not a GPU/client proof.
     */
    @GameTest(template = "empty", timeoutTicks = 180, batch = "ocean_live_motion")
    public static void liveFirstShipDetachesAndSailsFractionally(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        BlockPos anchor = t.absolutePos(new BlockPos(12, 4, 12));
        level.getChunkSource().addRegionTicket(VOYAGE_TICKET,
                new ChunkPos(anchor), 3, anchor.asLong());
        for(int x=-25;x<=25;x++)
            for(int z=-25;z<=25;z++) {
                BlockPos p = anchor.offset(x,0,z);
                level.setBlock(p.below(),Blocks.WATER.defaultBlockState(),2);
                level.setBlock(p,Blocks.WATER.defaultBlockState(),2);
                for(int y=1;y<=24;y++)level.setBlock(p.above(y),Blocks.AIR.defaultBlockState(),2);
            }
        t.assertTrue(FirstShipBlockHull.place(level,anchor,0,new ListTag(),0,0,5),
                "Live moving ship source hull failed to place");
        BlockPos cargo=FirstShipBlockHull.world(anchor,0,1,5,0);
        level.setBlock(cargo,Blocks.CHEST.defaultBlockState(),2);
        chest(level,cargo,t).setItem(0,new ItemStack(Items.DIAMOND,11));
        FirstShipEntity ship=RaftContent.FIRST_SHIP.get().create(level);
        t.assertTrue(ship!=null,"Live moving ship entity failed to create");
        ship.initializeNativeHull(anchor,0);
        ship.enableSmoothMotion();
        t.assertTrue(level.addFreshEntity(ship),"Live moving ship entity could not spawn");
        ServerPlayer captain=player(level,"MotionCaptain",new Vec3(ship.getX(),ship.deckY(),ship.getZ()-3D));
        ServerPlayer deckCrew=player(level,"SmoothDeckCrew",
                new Vec3(ship.getX()+1D,ship.deckY()+.02D,ship.getZ()));
        ServerPlayer holdCrew=player(level,"SmoothHoldCrew",
                new Vec3(ship.getX(),ship.holdY()+.02D,ship.getZ()));
        ServerPlayer climber=player(level,"SmoothLadderCrew",
                new Vec3(ship.getX()-1D,ship.holdY()+1D,ship.getZ()+2D));
        t.assertTrue(captain.startRiding(ship),"Motion captain could not board");
        final double startingZ=ship.getZ();
        t.runAtTickTime(5L,()->{
            t.assertTrue(ship.helmStep(captain,FirstShipNetwork.FORWARD),
                    "New ship failed to assemble into moving palette");
            t.assertTrue(ship.isMoving() && level.getBlockState(cargo).isAir(),
                    "Moving ship left real chest blocks behind");
            t.assertTrue(!deckCrew.startRiding(ship) && ship.getFirstPassenger() == captain,
                    "A second player took the occupied moving helm");
            java.util.List<net.minecraft.world.phys.shapes.VoxelShape> initialDeckShapes =
                    new java.util.ArrayList<>();
            t.assertTrue(org.slavicmyths.ships.ShipDynamicCollision.hasNearbyMovingShip(
                    deckCrew, level, deckCrew.getBoundingBox())
                    && !org.slavicmyths.ships.ShipDynamicCollision.hasNearbyMovingShip(
                            ship, level, ship.getBoundingBox()),
                    "Moving collision fast path skipped a walker or included the ship itself");
            org.slavicmyths.ships.ShipDynamicCollision.collect(deckCrew, level,
                    deckCrew.getBoundingBox().expandTowards(0D, -.35D, 0D), initialDeckShapes);
            t.assertTrue(!initialDeckShapes.isEmpty(),
                    "Moving deck collision query returned no local shapes: ship="
                            + ship.position() + " crew=" + deckCrew.position()
                            + " nearby=" + level.getEntitiesOfClass(FirstShipEntity.class,
                                    deckCrew.getBoundingBox().inflate(12D, 15D, 12D)).size());
            t.assertTrue(climber.onClimbable(),
                    "Detached moving ladder no longer supports vanilla climbing");
            float originalYaw = ship.getYRot();
            ship.setYRot(107F);
            FirstShipHull.Point ladder = FirstShipHull.toWorld(-1D, 2D,
                    ship.getX(), ship.getZ(), ship.getYRot());
            climber.setPos(ladder.x(), ship.holdY() + .02D, ladder.z());
            double ladderStartY = climber.getY();
            climber.move(net.minecraft.world.entity.MoverType.SELF,
                    new Vec3(0D, 1D, 0D));
            t.assertTrue(climber.getY() > ladderStartY + .8D,
                    "Rotated moving hatch blocks climbing from the ladder centre");
            FirstShipHull.Point intoBacking = FirstShipHull.toWorld(-1D, 0D,
                    0D, 0D, ship.getYRot());
            climber.move(net.minecraft.world.entity.MoverType.SELF,
                    new Vec3(intoBacking.x(), 0D, intoBacking.z()));
            FirstShipHull.Point againstBacking = FirstShipHull.toLocal(
                    climber.getX(), climber.getZ(), ship.getX(), ship.getZ(), ship.getYRot());
            t.assertTrue(againstBacking.x() > -1.7D,
                    "Ladder shaft clearance removed the supporting wall collision");
            ship.setYRot(originalYaw);
            climber.discard();
            t.assertTrue(ship.openMovingCargo(deckCrew, 0),
                    "Detached chest cannot be opened by deck crew");
            deckCrew.containerMenu.getSlot(0).set(new ItemStack(Items.DIAMOND, 13));
            deckCrew.closeContainer();
            ServerPlayer outsider = player(level, "CargoOutsider",
                    new Vec3(ship.getX() + 8D, ship.deckY(), ship.getZ()));
            t.assertTrue(!ship.openMovingCargo(outsider, 0),
                    "Outsider opened detached cargo from beyond the hull");
            BlockPos barrier = BlockPos.containing(outsider.getX() + 1D,
                    outsider.getY(), outsider.getZ());
            level.setBlock(barrier, Blocks.STONE.defaultBlockState(), 2);
            org.slavicmyths.ships.ShipDynamicCollision.carryWithWorldCollision(
                    outsider, ship, new Vec3(2D, 0D, 0D));
            t.assertTrue(outsider.getX() < barrier.getX() - .1D,
                    "Hull carry ignored a real world wall");
            level.setBlock(barrier, Blocks.AIR.defaultBlockState(), 2);
            outsider.discard();
            // Prove actual vanilla collision resolution, not a Post-tick
            // teleport: a descending crew member must hit the moving deck.
            deckCrew.move(net.minecraft.world.entity.MoverType.SELF,
                    new Vec3(0D, -.35D, 0D));
            t.assertTrue(deckCrew.getY() >= ship.deckY() - .06D,
                    "Moving deck has no physical player collision after detachment: y="
                            + deckCrew.getY() + " deck=" + ship.deckY()
                            + " ship=" + ship.position());
        });
        for(int tick=6;tick<=45;tick++)
            t.runAtTickTime(tick,()->{
                if(!ship.isMoving()||ship.isRemoved())return;
                t.assertTrue(ship.helmStep(captain,FirstShipNetwork.FORWARD_RIGHT),
                        "Moving helm rejected combined WASD");
            });
        final double[] releasedPose = new double[2];
        t.runAtTickTime(50L,()->{
            t.assertTrue(ship.isMoving()&&ship.getZ()>startingZ+.12D
                    && Math.abs(ship.getYRot())>2D && Math.abs(ship.getYRot())<90D,
                    "Live ship still moves in cardinal integer jumps");
            t.assertTrue(captain.getVehicle()==ship,"Captain fell off continuously moving ship");
            FirstShipHull.Point helm=FirstShipHull.toLocal(captain.getX(),captain.getZ(),
                    ship.getX(),ship.getZ(),ship.getYRot());
            t.assertTrue(Math.abs(helm.z()-FirstShipHull.HELM_Z)<.25D,
                    "Captain seat failed to follow noncardinal ship heading");
            FirstShipHull.Point deckLocal=FirstShipHull.toLocal(
                    deckCrew.getX(),deckCrew.getZ(),ship.getX(),ship.getZ(),ship.getYRot());
            FirstShipHull.Point holdLocal=FirstShipHull.toLocal(
                    holdCrew.getX(),holdCrew.getZ(),ship.getX(),ship.getZ(),ship.getYRot());
            t.assertTrue(Math.hypot(deckLocal.x()-1D,deckLocal.z())<.42D
                    && Math.abs(deckCrew.getY()-ship.deckY())<.30D,
                    "Free-walking deck crew stayed behind: local=" + deckLocal
                            + " y=" + deckCrew.getY() + " deck=" + ship.deckY()
                            + " ship=" + ship.position());
            t.assertTrue(Math.hypot(holdLocal.x(),holdLocal.z())<.42D
                    && Math.abs(holdCrew.getY()-ship.holdY())<.30D,
                    "Free-walking hold crew stayed behind the smooth ship");
            java.util.List<net.minecraft.world.phys.shapes.VoxelShape> deckShapes =
                    new java.util.ArrayList<>();
            org.slavicmyths.ships.ShipDynamicCollision.collect(deckCrew, level,
                    deckCrew.getBoundingBox().inflate(.2D), deckShapes);
            t.assertTrue(!deckShapes.isEmpty() && deckShapes.size() < 800,
                    "Rotated moving deck collision missing or unbounded: " + deckShapes.size());
            FirstShipHull.Point wallStep = FirstShipHull.toWorld(3D, 0D,
                    0D, 0D, ship.getYRot());
            holdCrew.move(net.minecraft.world.entity.MoverType.SELF,
                    new Vec3(wallStep.x(), 0D, wallStep.z()));
            FirstShipHull.Point stoppedAtWall = FirstShipHull.toLocal(
                    holdCrew.getX(), holdCrew.getZ(), ship.getX(), ship.getZ(), ship.getYRot());
            t.assertTrue(Math.abs(stoppedAtWall.x()) <=
                    FirstShipHull.holdBeam(stoppedAtWall.z()) + .35D,
                    "Hold crew crossed rotated moving hull wall: " + stoppedAtWall);
            double deckBeforeJump = deckCrew.getY();
            deckCrew.move(net.minecraft.world.entity.MoverType.SELF,
                    new Vec3(0D, .42D, 0D));
            t.assertTrue(deckCrew.getY() > deckBeforeJump + .35D,
                    "Moving deck prevented a normal upward jump");
            deckCrew.move(net.minecraft.world.entity.MoverType.SELF,
                    new Vec3(0D, -.65D, 0D));
            t.assertTrue(deckCrew.getY() >= ship.deckY() - .06D,
                    "Jumping player fell through moving deck on landing");
            CompoundTag saved=new CompoundTag();
            ship.saveWithoutId(saved);
            t.assertTrue(saved.contains("FirstShipMovingBody",net.minecraft.nbt.Tag.TAG_COMPOUND)
                    && org.slavicmyths.ships.ShipMovingContraption.load(
                        saved.getCompound("FirstShipMovingBody")).snapshot().cargo().size()==1,
                    "Moving ship save lacks the chest payload");
            var savedBody = org.slavicmyths.ships.ShipMovingContraption.load(
                    saved.getCompound("FirstShipMovingBody"));
            var savedChest = net.minecraft.core.NonNullList.withSize(27, ItemStack.EMPTY);
            net.minecraft.world.ContainerHelper.loadAllItems(
                    savedBody.cargoEntry(0).getCompound("BlockEntity"),
                    savedChest, level.registryAccess());
            t.assertTrue(savedChest.get(0).is(Items.DIAMOND)
                    && savedChest.get(0).getCount() == 13,
                    "Edited moving chest contents were not persisted in ship NBT");
            t.assertTrue(ship.serverMovingSpeed() > .03D,
                    "Ship reached the dismount check without forward velocity: "
                            + ship.serverMovingSpeed());
            captain.stopRiding();
            t.assertTrue(captain.getVehicle() == null && captain.getY() >= ship.deckY() - .1D,
                    "Captain failed to dismount onto the moving deck");
            releasedPose[0] = ship.getX();
            releasedPose[1] = ship.getZ();
        });
        t.runAtTickTime(52L, () -> {
            t.assertTrue(ship.isMoving() && Math.hypot(ship.getX() - releasedPose[0],
                    ship.getZ() - releasedPose[1]) > .005D,
                    "Ship lost inertia after dismount: moving=" + ship.isMoving()
                            + " dx=" + (ship.getX() - releasedPose[0])
                            + " dz=" + (ship.getZ() - releasedPose[1])
                            + " speed=" + ship.serverMovingSpeed()
                            + " originZ=" + startingZ + " currentZ=" + ship.getZ());
            FirstShipHull.Point onDeck = FirstShipHull.toLocal(captain.getX(), captain.getZ(),
                    ship.getX(), ship.getZ(), ship.getYRot());
            t.assertTrue(FirstShipHull.mainDeckSupports(onDeck.x(), onDeck.z())
                    && captain.getY() >= ship.deckY() - .1D,
                    "Dismounted captain fell through or detached from moving deck");
            t.assertTrue(!ship.helmStep(captain, FirstShipNetwork.FORWARD)
                    && captain.getVehicle() == null,
                    "Exited captain retained helm authority");
            captain.setPos(ship.getX() + 12D, ship.deckY() + 1D, ship.getZ());
            releasedPose[0] = captain.getX();
            releasedPose[1] = captain.getZ();
        });
        t.runAtTickTime(60L, () -> {
            t.assertTrue(Math.hypot(captain.getX() - releasedPose[0],
                    captain.getZ() - releasedPose[1]) < .25D,
                    "Ship continued to carry a captain who left the hull");
            t.assertTrue(deckCrew.startRiding(ship),
                    "Deck crew could not take over the released moving helm");
            t.assertTrue(ship.helmStep(deckCrew, FirstShipNetwork.FORWARD_RIGHT),
                    "New captain could not resume smooth steering");
            deckCrew.stopRiding();
            captain.discard();
            deckCrew.discard();
            holdCrew.discard();
            ship.discard();
            level.getChunkSource().removeRegionTicket(VOYAGE_TICKET,
                    new ChunkPos(anchor), 3, anchor.asLong());
            System.out.println("OCEAN26_LIVE_SMOOTH_SHIP_PASS fractional=true dismount=true inertia=true deck=true releasedHelm=true leaveHull=true helmHandoff=true cargoNBT=true");
            t.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 140, batch = "ocean_v6_sail")
    public static void fullSizeStarterSailAndCargoRemainPhysical(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        BlockPos anchor = t.absolutePos(new BlockPos(12, 4, 12));
        for (int x = -10; x <= 10; x++)
            for (int z = -15; z <= 15; z++) {
                BlockPos p = anchor.offset(x, 0, z);
                level.setBlock(p.below(), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                for (int y = 1; y <= 25; y++)
                    level.setBlock(p.above(y), Blocks.AIR.defaultBlockState(), 2);
            }
        t.assertTrue(FirstShipBlockHull.place(level, anchor, 0, new ListTag(), 1, 1, 6),
                "New V6 ship failed to place its full cloth sail");
        int clothCount = 0;
        for (int x = -3; x <= 3; x++)
            for (int y = 7; y <= 12; y++)
                if (level.getBlockState(FirstShipBlockHull.world(anchor, 0, x, y, 0))
                        .is(Blocks.WHITE_WOOL)) clothCount++;
        t.assertTrue(clothCount >= 12,
                "Starter sail still contains too little real fabric: " + clothCount);
        BlockPos cargo = FirstShipBlockHull.world(anchor, 0, 1, 5, 0);
        level.setBlock(cargo, Blocks.CHEST.defaultBlockState(), 2);
        chest(level, cargo, t).setItem(0, new ItemStack(Items.EMERALD, 17));
        ListTag beforeMove = FirstShipBlockHull.pack(level, anchor, 0, 1, 1, 6);
        t.assertTrue(beforeMove != null,
                "V6 source layout failed physical block/cargo inspection");
        t.assertTrue(beforeMove.size() == 1,
                "V6 source layout captured " + beforeMove.size() + " cargo entries, expected 1");
        var moving = org.slavicmyths.ships.ShipMovingContraption.prepare(
                level, anchor, 0, 1, 1, 6);
        t.assertTrue(moving != null,
                "V6 moving body preparation rejected intact layout");
        t.assertTrue(moving.snapshot().cargo().size() == 1,
                "V6 moving body did not capture physical chest cargo");
        t.assertTrue(moving.detach(level, anchor),
                "V6 sail could not detach without cargo loss");
        t.assertTrue(moving.park(level),
                "V6 sail could not restore its full physical hull");
        t.assertTrue(chest(level, cargo, t).getItem(0).getCount() == 17,
                "V6 sail transition modified chest contents");
        t.assertTrue(FirstShipBlockHull.migrateVisualSailV7(level, anchor, 0, 1, 1),
                "V6 to visual-sail V7 migration failed");
        t.assertTrue(FirstShipBlockHull.pack(level, anchor, 0, 1, 1, 7) != null
                && chest(level, cargo, t).getItem(0).getCount() == 17,
                "V7 migration changed the hull or chest contents");
        System.out.println("OCEAN26_V6_SAIL_PASS fullCloth=true cargo=17 restored=true migratedV7=true");
        t.succeed();
    }
    @GameTest(template = "empty", timeoutTicks = 270, batch = "ocean_v7_sail")
    public static void visualSailKeepsCargoAndMoves(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        BlockPos anchor = t.absolutePos(new BlockPos(12, 4, 12));
        level.getChunkSource().addRegionTicket(VOYAGE_TICKET,
                new ChunkPos(anchor), 3, anchor.asLong());
        for (int x = -25; x <= 25; x++) for (int z = -25; z <= 25; z++) {
            BlockPos p = anchor.offset(x, 0, z);
            level.setBlock(p.below(), Blocks.WATER.defaultBlockState(), 2);
            level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
            for (int y = 1; y <= 24; y++) level.setBlock(p.above(y), Blocks.AIR.defaultBlockState(), 2);
        }
        t.assertTrue(FirstShipBlockHull.place(level, anchor, 0, new ListTag(), 1, 1, 7),
                "V7 ship could not be placed");
        t.assertTrue(level.getBlockState(FirstShipBlockHull.world(anchor, 0, 1, 9, 0)).isAir()
                && level.getBlockState(FirstShipBlockHull.world(anchor, 0, 1, 13, -1))
                        .is(Blocks.DARK_OAK_LOG),
                "V7 must keep yards but no solid wool sail wall");
        BlockPos cargo = FirstShipBlockHull.world(anchor, 0, 1, 5, 0);
        level.setBlock(cargo, Blocks.CHEST.defaultBlockState(), 2);
        chest(level, cargo, t).setItem(0, new ItemStack(Items.EMERALD, 19));
        BlockPos furnace = FirstShipBlockHull.world(anchor, 0, -1, 5, 0);
        level.setBlock(furnace, Blocks.FURNACE.defaultBlockState(), 2);
        t.assertTrue(level.getBlockEntity(furnace) instanceof
                net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity,
                "V7 moving furnace BlockEntity missing");
        var furnaceBe = (net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity)
                level.getBlockEntity(furnace);
        furnaceBe.setItem(0, new ItemStack(Items.IRON_ORE));
        furnaceBe.setItem(1, new ItemStack(Items.COAL));
        FirstShipEntity ship = RaftContent.FIRST_SHIP.get().create(level);
        t.assertTrue(ship != null, "V7 ship entity missing");
        ship.initializeNativeHull(anchor, 0);
        ship.equipStarterSail();
        ship.enableSmoothMotion();
        t.assertTrue(level.addFreshEntity(ship), "V7 ship entity refused");
        ServerPlayer captain = player(level, "V7Captain",
                new Vec3(ship.getX(), ship.deckY(), ship.getZ() - 3D));
        t.assertTrue(captain.startRiding(ship), "V7 captain cannot board");
        t.runAtTickTime(5L, () -> {
            t.assertTrue(ship.helmStep(captain, FirstShipNetwork.FORWARD_LEFT),
                    "V7 combined first command rejected");
            t.assertTrue(ship.isMoving() && level.getBlockState(cargo).isAir()
                    && level.getBlockState(furnace).isAir(),
                    "V7 did not detach physical hull and cargo");
        });
        for (int tick = 6; tick < 20; tick++)
            t.runAtTickTime(tick, () -> t.assertTrue(
                    ship.helmStep(captain, FirstShipNetwork.FORWARD_LEFT),
                    "V7 held W+A command rejected"));
        t.runAtTickTime(20L, () -> {
            t.assertTrue(ship.isMoving() && ship.getZ() > anchor.getZ() + .5D,
                    "V7 vessel did not sail fractionally");
            t.assertTrue(ship.serverMovingPalette() != null
                    && ship.serverMovingPalette().values().stream().noneMatch(s -> s.is(Blocks.WHITE_WOOL)),
                    "Moving V7 palette recreated the solid wool sail");
            t.assertTrue(ship.helmStep(captain, FirstShipNetwork.HELM_IDLE),
                    "V7 helm did not accept release of held keys");
        });
        t.runAtTickTime(235L, () -> {
            t.assertTrue(ship.isMoving(), "V7 ship parked before furnace test completed");
            CompoundTag saved = new CompoundTag();
            ship.saveWithoutId(saved);
            var body = org.slavicmyths.ships.ShipMovingContraption.load(
                    saved.getCompound("FirstShipMovingBody"));
            t.assertTrue(body != null, "Moving furnace body failed NBT reload");
            boolean cooked = false, chestSafe = false;
            String furnaceState = "missing";
            for (int i = 0; i < body.cargoCount(); i++) {
                CompoundTag entry = body.cargoEntry(i);
                int kind = entry.getInt("Kind");
                var items = net.minecraft.core.NonNullList.withSize(
                        kind == 4 ? 3 : 27, ItemStack.EMPTY);
                net.minecraft.world.ContainerHelper.loadAllItems(
                        entry.getCompound("BlockEntity"), items, level.registryAccess());
                if (kind == 4) {
                    cooked = items.get(2).is(Items.IRON_INGOT);
                    furnaceState = items.get(0) + "/" + items.get(1) + "/"
                            + items.get(2) + " heat=" + entry.getCompound("BlockEntity")
                                    .getIntArray("FirstShipHeat").length;
                }
                if (kind == 1) chestSafe = items.get(0).is(Items.EMERALD)
                        && items.get(0).getCount() == 19;
            }
            t.assertTrue(cooked && chestSafe,
                    "Moving furnace failed: cooked=" + cooked + " furnace=" + furnaceState
                            + " chestSafe=" + chestSafe
                            + " speed=" + ship.serverMovingSpeed());
            ship.discard();
            captain.discard();
            body.motion().restore(new org.slavicmyths.ships.ShipRigidMotion.Pose(
                    anchor.getX() + .5D, anchor.getZ() + .5D, 0D), 0D, 0D);
            t.assertTrue(body.park(level), "Saved V7 cargo failed physical restoration");
            t.assertTrue(level.getBlockEntity(furnace) instanceof
                    net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity restoredFurnace
                    && restoredFurnace.getItem(2).is(Items.IRON_INGOT),
                    "Restored furnace lost cooked output");
            t.assertTrue(chest(level, cargo, t).getItem(0).getCount() == 19,
                    "Restored chest lost preserved emeralds");
            t.assertTrue(FirstShipBlockHull.remove(level, anchor, 0, 1, 1, 7),
                    "V7 post-restoration hull cleanup failed");
            level.getChunkSource().removeRegionTicket(VOYAGE_TICKET,
                    new ChunkPos(anchor), 3, anchor.asLong());
            t.succeed();
        });
    }
}
