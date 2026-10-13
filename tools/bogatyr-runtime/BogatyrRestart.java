package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.bogatyr.*;
import org.slavicmyths.bogatyr.BogatyrRules.State;

/** Actual reopening of one isolated on-disk world; not a connected-client relog. */
@GameTestHolder("slavicmyths_bogatyr_restart") @PrefixGameTestTemplate(false)
public final class BogatyrRestart {
    private static final UUID OWNER=UUID.fromString("17100000-0000-4000-8000-000000000001");
    private static final BlockPos HOME=new BlockPos(8,-58,201712);
    private static final class Connection extends net.minecraft.network.Connection {private final io.netty.channel.embedded.EmbeddedChannel channel=new io.netty.channel.embedded.EmbeddedChannel();Connection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}@Override public io.netty.channel.Channel channel(){return channel;}}
    private static final class Listener extends net.minecraft.server.network.ServerGamePacketListenerImpl {Listener(ServerPlayer p){super(p.getServer(),new Connection(),p,net.minecraft.server.network.CommonListenerCookie.createInitial(p.getGameProfile(),false));}@Override public void send(net.minecraft.network.protocol.Packet<?> p){}@Override public void send(net.minecraft.network.protocol.Packet<?> p,net.minecraft.network.PacketSendListener l){}}
    @GameTest(template="empty",timeoutTicks=1200) public static void diskReopen(GameTestHelper t)throws Exception{
        var w=t.getLevel();w.getChunkAt(HOME);Path checkpoint=Path.of("bogatyr-restart-checkpoint.json");String phase=System.getProperty("slavicmyths.bogatyr.restartPhase","none");
        if(phase.equals("write")){
            for(int x=-8;x<=8;x++)for(int z=-8;z<=8;z++)w.setBlock(HOME.offset(x,-1,z),Blocks.STONE.defaultBlockState(),2);
            var p=new ServerPlayer(w.getServer(),w,new com.mojang.authlib.GameProfile(OWNER,"BogatyrDisk"),ClientInformation.createDefault());p.connection=new Listener(p);p.moveTo(HOME.getX()+1,HOME.getY(),HOME.getZ()+2);
            var b=BogatyrContent.IDOL.get().create(w);b.moveTo(HOME.getX()+8,HOME.getY(),HOME.getZ());w.addFreshEntity(b);b.start(p,false);b.setHealth(300);b.feeds=2;b.begin(State.STUNNED,5000);
            var h=BogatyrContent.ILYA.get().create(w);h.owner=OWNER;h.moveTo(HOME.getX()+.5,HOME.getY(),HOME.getZ()+.5);h.bind(b);h.setHealth(100);h.unyieldingUsed=true;h.returned=true;h.cooldown("mace",400);h.recoveryEnd=w.getGameTime()+1100;h.begin(State.RECOVERY,1100);w.addFreshEntity(h);
            var d=BogatyrLedger.get(w);d.heroes.put(OWNER,h.getUUID());d.victory(p,b,true);d.setDirty();
            var z=EntityType.ZOMBIE.create(w);z.moveTo(HOME.getX()+5,HOME.getY(),HOME.getZ()+5);z.getPersistentData().putUUID("IdolOwner",b.getUUID());z.getPersistentData().putUUID("IdolEncounter",b.encounter);z.getPersistentData().putBoolean("feedEligible",true);z.setPersistenceRequired();z.setNoAi(true);w.addFreshEntity(z);
            var n=new JsonObject();n.addProperty("hero",h.getUUID().toString());n.addProperty("boss",b.getUUID().toString());n.addProperty("minion",z.getUUID().toString());Files.writeString(checkpoint,n.toString());System.out.println("BOGATYR_RESTART_WRITE actualWorldSave=true hero="+h.getUUID()+" HP=100 usedCrisis=true usedReturn=true victoryCount=1");t.succeed();
        }else if(phase.equals("read")){
            w.setChunkForced(HOME.getX()>>4,HOME.getZ()>>4,true);w.setChunkForced((HOME.getX()+8)>>4,HOME.getZ()>>4,true);
            var n=JsonParser.parseString(Files.readString(checkpoint)).getAsJsonObject();UUID hero=UUID.fromString(n.get("hero").getAsString()),boss=UUID.fromString(n.get("boss").getAsString()),minion=UUID.fromString(n.get("minion").getAsString());
            t.startSequence().thenWaitUntil(()->t.assertTrue(w.getEntity(hero) instanceof IlyaEntity&&w.getEntity(boss) instanceof IdolishcheEntity,"Saved entities not accessible")).thenIdle(5).thenExecute(()->{
                var h=(IlyaEntity)w.getEntity(hero);var b=(IdolishcheEntity)w.getEntity(boss);var d=BogatyrLedger.get(w);
                t.assertTrue(d.heroes.get(OWNER).equals(hero),"Canonical UUID lost");t.assertTrue(h.unyieldingUsed&&h.returned&&!h.ready("mace"),"Crisis/return/cooldowns refreshed on restart");t.assertTrue(h.getHealth()>=100&&h.getHealth()<=110,"Reload invented instant healing");
                t.assertTrue(!b.active&&b.getHealth()==900&&!b.rage()&&b.feeds==0,"Interrupted encounter did not abort/reset");t.assertTrue(w.getEntity(minion)==null,"Orphan minion retained");
                var v=d.victories.get(OWNER);t.assertTrue(v!=null&&v.getInt("defeatCount")==1&&v.getBoolean("eligibleForFutureRewards"),"Victory history changed");
                t.assertTrue(w.getEntitiesOfClass(IlyaEntity.class,h.getBoundingBox().inflate(20),a->OWNER.equals(a.owner)).size()==1,"Duplicate after disk reopening");
                w.setChunkForced(HOME.getX()>>4,HOME.getZ()>>4,false);w.setChunkForced((HOME.getX()+8)>>4,HOME.getZ()>>4,false);
                System.out.println("BOGATYR_RESTART_READ PASS actualDiskReopen=true sameHeroUuid=true finiteHp=true budgetsPreserved=true bossAbort=true orphanRemoved=true historyOnce=true NOT_CLIENT_RELOG");
            }).thenSucceed();
        }else throw new IllegalArgumentException("Explicit write/read phase required");
    }
}
