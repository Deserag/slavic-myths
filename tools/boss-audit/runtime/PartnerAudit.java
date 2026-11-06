package org.slavicmyths.bossaudit;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import org.slavicmyths.sisters.*;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
@GameTestHolder("slavicmyths_bossaudit") @PrefixGameTestTemplate(false)
public final class PartnerAudit {
 @GameTest(template="empty",timeoutTicks=100,batch="partner")
 public static void partnerDeathAndUnloadControl(GameTestHelper t)throws Exception{
  var w=t.getLevel();var survivors=new ArrayList<SisterBoss>();
  for(int i=0;i<3;i++){var pos=t.absolutePos(new BlockPos(64+i*48,140,64));var cp=new ChunkPos(pos);for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++){w.setChunkForced(cp.x+dx,cp.z+dz,true);w.getChunk(cp.x+dx,cp.z+dz);}var g=SistersContent.GNETEYA.get().create(w);var r=SistersContent.TRYASEYA.get().create(w);var encounter=UUID.randomUUID();g.link(encounter,r.getUUID());r.link(encounter,g.getUUID());g.setNoGravity(true);r.setNoGravity(true);g.moveTo(pos.getX(),pos.getY(),pos.getZ());r.moveTo(pos.getX()+4,pos.getY(),pos.getZ());w.addFreshEntity(g);w.addFreshEntity(r);
   if(i==0){g.hurt(g.damageSources().genericKill(),Float.MAX_VALUE);survivors.add(r);}else if(i==1){r.hurt(r.damageSources().genericKill(),Float.MAX_VALUE);survivors.add(g);}else{g.discard();survivors.add(r);}
  }
  t.runAfterDelay(10,()->{try{var rows=new JsonArray();for(int i=0;i<3;i++){var m=survivors.get(i);boolean expected=i<2;var row=new JsonObject();row.addProperty("scenario",i==0?"gneteya_killed":i==1?"tryaseya_killed":"gneteya_discarded_control");row.addProperty("survivor_enraged",m.enraged());row.addProperty("expected",expected);row.addProperty("entity_ticks",m.tickCount);rows.add(row);}var out=Path.of(System.getProperty("bossaudit.output"));Files.createDirectories(out);Files.writeString(out.resolve("partner-events.json"),new GsonBuilder().setPrettyPrinting().create().toJson(rows));for(int i=0;i<3;i++){t.assertTrue(survivors.get(i).enraged()==(i<2),"Partner event mismatch scenario"+i);survivors.get(i).discard();}t.succeed();}catch(Exception ex){throw new RuntimeException(ex);}});
 }
}
