package org.slavicmyths.hellqa;

import java.util.*;
import java.nio.file.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.losthalls.*;
import org.slavicmyths.burningislands.*;
import org.slavicmyths.trophy.*;

/** Three separate real NeoForge JVMs, actual PlayerList save/load and two deliberate process halts.
 * No natural boss kill, client connection or filesystem power-loss guarantee is claimed. */
@GameTestHolder("slavicmyths_hellqa") @PrefixGameTestTemplate(false)
public final class RewardCrashQaTests {
    static final UUID A=UUID.fromString("a1770000-0000-0000-0000-000000000001"),B=UUID.fromString("a1770000-0000-0000-0000-000000000002");
    static final String KEY="hellqa:actual-player-storage-crash:fixed-recipient";
    static ServerPlayer player(GameTestHelper t,UUID id,String name){
        var p=new ServerPlayer(t.getLevel().getServer(),t.getLevel(),new com.mojang.authlib.GameProfile(id,name),ClientInformation.createDefault());
        p.connection=new HellQaTests.Listener(p);p.setGameMode(GameType.SURVIVAL);return p;
    }
    @SuppressWarnings("unchecked") static Map<UUID,ServerPlayer> online(PlayerList list)throws Exception{
        var f=PlayerList.class.getDeclaredField("playersByUUID");f.setAccessible(true);return (Map<UUID,ServerPlayer>)f.get(list);
    }
    static void save(ServerPlayer p){((org.slavicmyths.navya.mixin.PlayerListSaveInvoker)p.getServer().getPlayerList()).slavicmyths$save(p);}
    static void halt(int code,String stage)throws Exception{
        System.out.println("HELLQA_ACTUAL_PROCESS_HALT stage="+stage+" exit="+code);System.out.flush();Runtime.getRuntime().halt(code);
    }
    @GameTest(template="empty") public static void pinnedRewardActualPlayerFiles(GameTestHelper t)throws Exception{
        String phase=System.getProperty("hellqa.rewardCrashPhase");
        HellQaTests.check(Set.of("pin","deliver","verify").contains(phase),"Crash fixture requires an explicit phase");
        var server=t.getLevel().getServer();var players=server.getPlayerList();var a=player(t,A,"CrashRecipientA");var b=player(t,B,"CrashRecipientB");
        var online=online(players);online.put(A,a);online.put(B,b);
        Path authority=server.getWorldPath(LevelResource.ROOT).resolve("data/slavicmyths_boss_reward_authority.json");
        Path playerFile=server.getWorldPath(LevelResource.PLAYER_DATA_DIR).resolve(A+".dat");
        try {
            if(phase.equals("pin")){
                HellQaTests.check(!Files.exists(authority),"Refuse to overwrite an existing crash fixture authority");
                a.getInventory().setItem(0,new ItemStack(Items.COBBLESTONE,3));b.getInventory().setItem(0,new ItemStack(Items.GOLD_INGOT,7));save(a);save(b);
                HellQaTests.check(Files.isRegularFile(playerFile),"Actual PlayerDataStorage did not create A.dat");
                var entry=BossRewardInbox.prepare(server,KEY,"magma",A,4,0);
                HellQaTests.check(entry!=null&&entry.recipient().equals(A)&&Files.isRegularFile(authority),"Pin not durable");
                // File pin is durable; player inventory/receipt still pre-delivery. No clean shutdown.
                halt(61,"after_authority_pin_before_player_delivery");
            }
            HellQaTests.check(players.load(a).isPresent()&&players.load(b).isPresent(),"Actual saved player files missing after JVM restart");
            HellQaTests.check(a.getInventory().countItem(Items.COBBLESTONE)==3&&b.getInventory().countItem(Items.GOLD_INGOT)==7,"Vanilla player load lost baseline inventory");
            var repeated=BossRewardInbox.prepare(server,KEY,"magma",B,6,0);
            HellQaTests.check(repeated!=null&&repeated.recipient().equals(A)&&repeated.cores()==4,"Cross-player retry reassigned or rerolled reward");
            var head=TrophyHeads.ITEMS.get(TrophyHeads.Kind.MAGMA_LORD).get();
            if(phase.equals("deliver")){
                HellQaTests.check(!BossRewardInbox.hasReceipt(a.getPersistentData(),KEY)&&a.getInventory().countItem(head)==0,"Unexpected delivery survived the pre-delivery cut");
                BossRewardInbox.reconcile(a);
                HellQaTests.check(BossRewardInbox.hasReceipt(a.getPersistentData(),KEY)&&a.getInventory().countItem(head)==1,"Real delivery did not save inventory+receipt");
                // Production reconcile invokes the actual PlayerList save before returning.
                halt(62,"after_actual_player_save_before_world_save");
            }
            HellQaTests.check(BossRewardInbox.hasReceipt(a.getPersistentData(),KEY),"Actual player receipt did not survive second hard process halt");
            BossRewardInbox.reconcile(a);BossRewardInbox.reconcile(a);BossRewardInbox.reconcile(b);
            HellQaTests.check(a.getInventory().countItem(head)==1&&a.getInventory().countItem(IslandsContent.item("magma_core"))==4&&a.getInventory().countItem(IslandsContent.item("magma_lord_heart"))==1,"Actual restart duplicated or lost A reward");
            HellQaTests.check(b.getInventory().countItem(head)==0&&b.getInventory().countItem(IslandsContent.item("magma_core"))==0&&!BossRewardInbox.hasReceipt(b.getPersistentData(),KEY),"Recipient B stole a reward after restart");
            System.out.println("HELLQA_PASS ACTUAL_NEOFORGE_PROCESS_CRASH two halts + three JVMs; actual PlayerList/PlayerDataStorage A.dat; inventory+receipt survive; A one head/heart/4cores; B zero; no reroll 4->6; fixture authority not natural boss kill or real network MP/power loss");
            t.succeed();
        } finally {online.remove(A);online.remove(B);}
    }
}
