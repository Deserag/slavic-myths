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
import org.slavicmyths.marinka.*;

/** Two actual server processes reopening one isolated disk world. No connected-client relog. */
@GameTestHolder("slavicmyths_marinka_restart") @PrefixGameTestTemplate(false)
public final class MarinkaRestart {
 private static final UUID OWNER=UUID.fromString("17200000-0000-4000-8000-000000000001");
 private static final BlockPos HOME=new BlockPos(1024,0,204172);
 @GameTest(template="empty",timeoutTicks=1200) public static void diskReopen(GameTestHelper t)throws Exception{
  var w=t.getLevel();for(int x=(HOME.getX()-31)>>4;x<=((HOME.getX()+31)>>4);x++)for(int z=(HOME.getZ()-31)>>4;z<=((HOME.getZ()+31)>>4);z++)w.setChunkForced(x,z,true);
  var d=WitchYard.get(w);String phase=System.getProperty("slavicmyths.marinka.restartPhase");var checkpoint=Path.of("marinka-restart-checkpoint.properties");
  if("write".equals(phase)){
   var e=new WitchYard.Entry(UUID.randomUUID(),HOME,0);e.owner=OWNER;e.status=WitchYard.Status.COMBAT;d.yards.put(e.id,e);
   for(var cell:YardBlueprint.plan()){var p=cell.local();w.setBlock(e.at(p.getX(),p.getY(),p.getZ()),cell.state(),2);}
   var chest=(ChestBlockEntity)w.getBlockEntity(e.at(3,-9,-9));chest.setItem(10,new ItemStack(Items.CANDLE,2));chest.setChanged();e.lootInstalled=true;
   var h=MarinkaContent.DOBRYNYA.get().create(w);h.owner=OWNER;h.moveTo(HOME.getX()+.5,1,HOME.getZ()+8.5);h.setHealth(95);h.retreated=true;h.cooldown("thrust",400);h.begin(WitchAction.RECOVER,0);h.recoverySince=w.getGameTime();w.addFreshEntity(h);d.heroes.put(OWNER,h.getUUID());e.hero=h.getUUID();
   var b=MarinkaContent.MARINKA.get().create(w);b.arena=e.id;b.owner=OWNER;d.victory(w,e,b);
   var interrupted=new WitchYard.Entry(UUID.randomUUID(),HOME.offset(120,0,0),0);interrupted.owner=UUID.randomUUID();interrupted.status=WitchYard.Status.COMBAT;d.yards.put(interrupted.id,interrupted);
   var n=new net.minecraft.nbt.CompoundTag();n.putUUID("Player",OWNER);n.putInt("DefeatCount",1);n.putBoolean("marinka_defeated",true);n.putBoolean("eligibleForFutureRewards",true);d.progress.put(OWNER,n);d.setDirty();
   var props=new Properties();props.setProperty("hero",h.getUUID().toString());props.setProperty("yard",e.id.toString());props.setProperty("interrupted",interrupted.id.toString());try(var out=Files.newOutputStream(checkpoint)){props.store(out,"isolated disk test");}System.out.println("MARINKA_RESTART_WRITE actualDiskSave=true sameHero=95HP cellarLoot=2Candles midHatch=true interruptedFight=true");t.succeed();
  }else if("read".equals(phase)){
   var props=new Properties();try(var in=Files.newInputStream(checkpoint)){props.load(in);}UUID hero=UUID.fromString(props.getProperty("hero")),yard=UUID.fromString(props.getProperty("yard")),interrupted=UUID.fromString(props.getProperty("interrupted"));
   t.startSequence().thenWaitUntil(()->t.assertTrue(w.getEntity(hero) instanceof DobrynyaEntity,"Saved hero not loaded")).thenIdle(5).thenExecute(()->{
    var h=(DobrynyaEntity)w.getEntity(hero);t.assertTrue(d.heroes.get(OWNER).equals(hero)&&h.getHealth()==95&&h.retreated&&!h.ready("thrust"),"Hero identity/HP/budget/cooldown changed");t.assertTrue(d.yards.get(interrupted).status==WitchYard.Status.READY&&d.lock(d.yards.get(interrupted).owner)==null,"Interrupted fight kept lock");t.assertTrue(d.progress.get(OWNER).getInt("DefeatCount")==1,"History duplicated");
   }).thenWaitUntil(()->t.assertTrue(d.yards.get(yard).status==WitchYard.Status.OPEN,"Pending hatch not opened")).thenExecute(()->{
    var e=d.yards.get(yard);for(int x=0;x<2;x++)for(int z=0;z<3;z++)t.assertTrue(w.getBlockState(e.at(x,0,z)).isAir(),"Disk hatch incoherent");var chest=(ChestBlockEntity)w.getBlockEntity(e.at(3,-9,-9));t.assertTrue(chest.getItem(10).is(Items.CANDLE)&&chest.getItem(10).getCount()==2,"Loot overwritten on disk restart");System.out.println("MARINKA_RESTART_READ PASS actualDiskReopen=true canonicalUUID=true HP95=true cooldown=true retreatBudget=true lockReleased=true hatchOpen=true lootPreserved=true historyOnce=true NOT_CLIENT_RELOG");
    for(int x=(HOME.getX()-31)>>4;x<=((HOME.getX()+31)>>4);x++)for(int z=(HOME.getZ()-31)>>4;z<=((HOME.getZ()+31)>>4);z++)w.setChunkForced(x,z,false);
   }).thenSucceed();
  }else throw new IllegalArgumentException("Explicit write/read phase required");
 }
}
