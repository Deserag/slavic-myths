package org.slavicmyths.verify;
import java.util.*;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.*;
import org.slavicmyths.skim.*;
/** Actual isolated disk world reopened by two processes, not a client relog. */
@GameTestHolder("slavicmyths_skim_restart") @PrefixGameTestTemplate(false)
public final class SkimRestart {
 private static final UUID OWNER=UUID.fromString("17300000-0000-4000-8000-000000000001");private static final BlockPos HOME=new BlockPos(2048,0,204173);
 @GameTest(template="empty",timeoutTicks=1200) public static void diskReopen(GameTestHelper t)throws Exception{
 var w=t.getLevel();for(int x=(HOME.getX()-34)>>4;x<=((HOME.getX()+34)>>4);x++)for(int z=(HOME.getZ()-52)>>4;z<=((HOME.getZ()+34)>>4);z++)w.setChunkForced(x,z,true);
 var d=SkimValley.get(w);var calls=HeroCalls.get(w);String phase=System.getProperty("slavicmyths.skim.restartPhase");var checkpoint=Path.of("skim-restart-checkpoint.properties");
 if("write".equals(phase)){
  var v=new SkimValley.Entry(HOME,0);v.owner=OWNER;v.status=SkimValley.Status.VICTORY;v.victoryAt=w.getGameTime();v.loot=true;d.valleys.put(v.id,v);for(var c:ValleyBlueprint.plan()){var q=c.at();w.setBlock(v.at(q.getX(),q.getY(),q.getZ()),c.state(),2);}var chest=(ChestBlockEntity)w.getBlockEntity(v.at(4,1,-43));chest.setItem(10,new ItemStack(Items.CANDLE,2));chest.setChanged();
  var h=SkimContent.ALYOSHA.get().create(w);h.owner=OWNER;h.moveTo(HOME.getX()+.5,1,HOME.getZ()+8.5);h.setHealth(95);h.intercepted=true;h.cooldown("dodge",400);w.addFreshEntity(h);calls.alyosha.put(OWNER,h.getUUID());v.hero=h.getUUID();calls.setDirty();
  var interrupted=new SkimValley.Entry(HOME.offset(120,0,0),0);interrupted.owner=UUID.randomUUID();interrupted.status=SkimValley.Status.ACTIVE;d.valleys.put(interrupted.id,interrupted);
  var n=new net.minecraft.nbt.CompoundTag();n.putUUID("Player",OWNER);n.putInt("defeat_count",1);n.putBoolean("skim_beast_defeated",true);n.putBoolean("eligibleForFutureRewards",true);d.progress.put(OWNER,n);d.setDirty();
  var props=new Properties();props.setProperty("hero",h.getUUID().toString());props.setProperty("valley",v.id.toString());props.setProperty("interrupted",interrupted.id.toString());try(var out=Files.newOutputStream(checkpoint)){props.store(out,"isolated disk test");}System.out.println("SKIM_RESTART_WRITE actualDiskSave=true hero95HP=true supplies2Candles=true interruptedFight=true");t.succeed();
 }else if("read".equals(phase)){
  var props=new Properties();try(var in=Files.newInputStream(checkpoint)){props.load(in);}UUID hero=UUID.fromString(props.getProperty("hero")),valley=UUID.fromString(props.getProperty("valley")),interrupted=UUID.fromString(props.getProperty("interrupted"));
  t.startSequence().thenWaitUntil(()->t.assertTrue(w.getEntity(hero) instanceof AlyoshaEntity,"Saved hero not loaded")).thenIdle(5).thenExecute(()->{
   var h=(AlyoshaEntity)w.getEntity(hero);t.assertTrue(calls.alyosha.get(OWNER).equals(hero)&&h.getHealth()==95&&h.intercepted&&!h.ready("dodge"),"Hero state changed");t.assertTrue(d.valleys.get(interrupted).status==SkimValley.Status.READY&&d.lock(d.valleys.get(interrupted).owner)==null,"Interrupted lock retained");t.assertTrue(d.progress.get(OWNER).getInt("defeat_count")==1,"History duplicated");
  }).thenWaitUntil(()->t.assertTrue(d.valleys.get(valley).status==SkimValley.Status.COMPLETE,"Pending den not opened")).thenExecute(()->{
   var v=d.valleys.get(valley);t.assertTrue(w.getBlockState(v.at(0,1,-30)).isAir(),"Disk den blocked");var chest=(ChestBlockEntity)w.getBlockEntity(v.at(4,1,-43));t.assertTrue(chest.getItem(10).is(Items.CANDLE)&&chest.getItem(10).getCount()==2,"Supplies overwritten");System.out.println("SKIM_RESTART_READ PASS actualDiskReopen=true canonicalUUID=true HP95=true cooldown=true interceptionBudget=true lockReleased=true denOpen=true lootPreserved=true historyOnce=true NOT_CLIENT_RELOG");
   for(int x=(HOME.getX()-34)>>4;x<=((HOME.getX()+34)>>4);x++)for(int z=(HOME.getZ()-52)>>4;z<=((HOME.getZ()+34)>>4);z++)w.setChunkForced(x,z,false);
  }).thenSucceed();
 }else throw new IllegalArgumentException("Explicit phase required");
 }
}
