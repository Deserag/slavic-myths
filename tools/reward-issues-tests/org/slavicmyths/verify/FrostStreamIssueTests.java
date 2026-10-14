package org.slavicmyths.verify;

import java.util.UUID;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ChestMenu;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.morozko.*;

/** Native server lifecycle; injected heartbeat gaps are not a network-lag measurement. */
@GameTestHolder("slavicmyths_rewards") @PrefixGameTestTemplate(false)
public final class FrostStreamIssueTests {
    private static final class Connection extends net.minecraft.network.Connection {
        private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();
        Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public io.netty.channel.Channel channel(){return channel;}
    }
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {
        Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet) {}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener) {}
    }
    private static ServerPlayer player(GameTestHelper t) {
        var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(UUID.randomUUID(),"FrostStreamQA"),ClientInformation.createDefault());
        p.connection=new Listener(p);p.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(FrostContent.STAFF.get()));
        t.assertTrue(FrostSkills.startStream(p),"first start accepted");p.startUsingItem(InteractionHand.MAIN_HAND);return p;
    }
    private static boolean active(ServerPlayer p){return p.getPersistentData().getBoolean("FrostStreaming");}
    @GameTest(template="empty") public static void heartbeatGrace(GameTestHelper t) {
        var p=player(t);long now=FrostSkills.now(p);
        for(int gap:new int[]{8,20}){p.getPersistentData().putLong("FrostStreamHeartbeat",now-gap);FrostSkills.streamTick(p,52);t.assertTrue(active(p)&&p.isUsingItem(),"gap "+gap+" must not prematurely stop");}
        p.getPersistentData().putLong("FrostStreamHeartbeat",now-21);FrostSkills.streamTick(p,39);
        t.assertTrue(!active(p)&&!p.isUsingItem(),"long heartbeat loss stops both state/use");
        t.assertTrue(FrostSkills.data(p).getLong("StreamReady")==now+200,"cooldown recorded once");
        t.assertTrue(!FrostSkills.startStream(p),"retry during cooldown rejected");
        System.out.println("FROST_AFTER gap8=active gap20=active gap21=stopped cooldown=200");t.succeed();
    }
    @GameTest(template="empty") public static void fixedDeadline(GameTestHelper t) {
        var p=player(t);long deadline=p.getPersistentData().getLong("FrostStreamUntil");
        t.assertTrue(FrostSkills.startStream(p),"duplicate accepted use keeps existing stream");
        t.assertTrue(p.getPersistentData().getLong("FrostStreamUntil")==deadline,"duplicate cannot extend deadline");
        p.getPersistentData().putLong("FrostStreamUntil",FrostSkills.now(p));FrostSkills.action(p,4);FrostSkills.streamTick(p,60);
        t.assertTrue(!active(p)&&!p.isUsingItem(),"heartbeat/restart cannot exceed hard deadline");
        t.assertTrue(!p.getPersistentData().contains("FrostStreamUntil"),"timer cleared");t.succeed();
    }
    @GameTest(template="empty") public static void releaseAndMenu(GameTestHelper t) {
        var released=player(t);FrostSkills.action(released,5);t.assertTrue(!active(released)&&!released.isUsingItem(),"release stops stream");
        var menu=player(t);menu.containerMenu=ChestMenu.threeRows(42,menu.getInventory());FrostSkills.streamTick(menu,52);
        t.assertTrue(!active(menu)&&!menu.isUsingItem(),"menu stops stream");t.succeed();
    }
    @GameTest(template="empty",timeoutTicks=100) public static void fullServerInterval(GameTestHelper t) {
        var p=player(t);long start=FrostSkills.now(p);var staff=p.getMainHandItem();
        for(int tick=1;tick<=60;tick++){final int age=tick;t.runAfterDelay(tick,()->{
            if(age%3==0)FrostSkills.action(p,4);
            staff.getItem().onUseTick(t.getLevel(),p,staff,60-age);
            if(age<60)t.assertTrue(active(p),"stream must remain active at age "+age);
            else{t.assertTrue(!active(p)&&!p.isUsingItem(),"ends exactly at 60 server ticks");
                t.assertTrue(FrostSkills.now(p)-start==60,"actual server clock advanced 60 ticks");
                System.out.println("FROST_INTERVAL actual server ticks=60 active through59 stopped60 cooldown200");t.succeed();}
        });}
    }
}
