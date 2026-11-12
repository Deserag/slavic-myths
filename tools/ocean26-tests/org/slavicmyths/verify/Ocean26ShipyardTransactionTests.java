package org.slavicmyths.verify;

import com.mojang.authlib.GameProfile;
import io.netty.channel.Channel;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.slavicmyths.ships.RaftContent;
import org.slavicmyths.ships.ShipBuildConfiguration;
import org.slavicmyths.ships.ShipSectionPlan;
import org.slavicmyths.ships.ShipyardMenu;
import org.slavicmyths.ships.ShipyardModular;

/** Server-side stale selection rejection with real ItemStack NBT. */
@GameTestHolder("slavicmyths_ocean26")
@PrefixGameTestTemplate(false)
public final class Ocean26ShipyardTransactionTests {
    private static final class LocalConnection extends Connection {
        private final EmbeddedChannel wire = new EmbeddedChannel();
        LocalConnection() { super(PacketFlow.SERVERBOUND); }
        @Override public Channel channel() { return wire; }
    }
    private static final class SilentListener extends ServerGamePacketListenerImpl {
        SilentListener(ServerPlayer player) {
            super(player.getServer(), new LocalConnection(), player,
                    CommonListenerCookie.createInitial(player.getGameProfile(), false));
        }
        @Override public void send(Packet<?> packet) {}
        @Override public void send(Packet<?> packet, PacketSendListener listener) {}
    }
    private static ServerPlayer newPlayer(ServerLevel level, BlockPos pos) {
        ServerPlayer player = new ServerPlayer(level.getServer(), level,
                new GameProfile(UUID.randomUUID(), "ShipyardSwapRegression"),
                ClientInformation.createDefault());
        player.connection = new SilentListener(player);
        player.setPos(Vec3.atCenterOf(pos).add(0D, 1D, 0D));
        level.addNewPlayer(player);
        player.getAbilities().instabuild = true;
        return player;
    }

    @GameTest(template = "empty", timeoutTicks = 120, batch = "ocean_shipyard_transactions")
    public static void swappingInspectedShipsRejectsStaleUpgrades(GameTestHelper t) {
        ServerLevel level = t.getLevel();
        BlockPos bench = t.absolutePos(new BlockPos(3, 2, 3));
        level.setBlock(bench, RaftContent.SHIPYARD_BENCH.get().defaultBlockState(), 2);
        ServerPlayer owner = newPlayer(level, bench);
        ShipyardMenu menu = new ShipyardMenu(47, owner.getInventory(), bench);
        owner.containerMenu = menu;

        ShipBuildConfiguration initial = ShipBuildConfiguration.initial(ShipSectionPlan.ShipClass.MEDIUM);
        ItemStack shipA = ShipyardModular.build(owner, initial);
        ItemStack shipB = ShipyardModular.build(owner, initial.resize(1, 0));
        t.assertTrue(!shipA.isEmpty() && !shipB.isEmpty(), "Cannot build ship fixture");
        menu.getSlot(0).set(shipA.copy());
        int oldHash = ItemStack.hashItemAndComponents(menu.inspected());
        menu.getSlot(0).set(shipB.copy());
        int newHash = ItemStack.hashItemAndComponents(menu.inspected());

        t.assertTrue(oldHash != newHash, "Different ship blueprints must have distinct checksums");
        t.assertTrue(!menu.clickVerifiedButton(owner, 200, oldHash, -1),
                "A stale upgrade unexpectedly modified the replacement ship");
        t.assertTrue(ItemStack.isSameItemSameComponents(menu.inspected(), shipB),
                "Rejected action changed replacement ship NBT");
        t.assertTrue(!menu.clickVerifiedButton(owner, 200, newHash, 12345),
                "Nearby-ship mismatch was accepted");
        t.assertTrue(ItemStack.isSameItemSameComponents(menu.inspected(), shipB),
                "Nearby-ship mismatch changed the inspected item");

        t.assertTrue(menu.clickVerifiedButton(owner, 200, newHash, -1),
                "Correctly bound action was rejected");
        ShipBuildConfiguration upgraded = ShipyardModular.configuration(menu.inspected());
        t.assertTrue(upgraded != null && upgraded.extraLength() == 2,
                "Verified upgrade did not apply to the selected ship");
        menu.getSlot(0).set(new ItemStack(RaftContent.RAFT_ITEM.get()));
        t.assertTrue(!menu.clickVerifiedButton(owner, 200, newHash, -1),
                "Modular upgrade unexpectedly applied after swapping to a raft");
        menu.getSlot(0).set(ItemStack.EMPTY);
        t.assertTrue(!menu.clickVerifiedButton(owner, 200, newHash, -1),
                "Modular upgrade unexpectedly applied after removing the ship");
        menu.removed(owner);
        owner.discard();
        t.succeed();
    }
}
