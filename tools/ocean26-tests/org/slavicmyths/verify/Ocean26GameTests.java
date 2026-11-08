package org.slavicmyths.verify;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
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
        // Prepare a disposable shallow ocean, leaving two water-source layers.
        for (int x = -15; x <= 15; x++)
            for (int z = -15; z <= 15; z++) {
                BlockPos column = anchor.offset(x, 0, z);
                level.setBlock(column.below(), Blocks.WATER.defaultBlockState(), 2);
                level.setBlock(column, Blocks.WATER.defaultBlockState(), 2);
                for (int y = 1; y <= 24; y++)
                    level.setBlock(column.above(y), Blocks.AIR.defaultBlockState(), 2);
            }

        t.assertTrue(FirstShipBlockHull.place(level, anchor, 0, new ListTag(), 0, 0),
                "Empty native small hull could not be placed");
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
}
