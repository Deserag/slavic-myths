package org.slavicmyths.military;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.slavicmyths.hunt.*;
/** Uses the existing saved encounter index and spawning API, never requests terrain chunks. */
public final class StoryLikho {
 public static double distance(BlockPos a,BlockPos b){return Math.hypot(a.getX()-b.getX(),a.getZ()-b.getZ());}
 public static BlockPos clue(BlockPos anchor,Random r){double angle=r.nextDouble()*Math.PI*2,radius=150+r.nextDouble()*100;return new BlockPos((int)Math.round((anchor.getX()+Math.cos(angle)*radius)/100)*100,64,(int)Math.round((anchor.getZ()+Math.sin(angle)*radius)/100)*100);}
 public static void ensure(ServerPlayer p,BlockPos home){var w=p.serverLevel();var q=GreyWolfStory.quest(p,2);var records=HuntRecords.get(w);var b=records.bosses.get(q.getString("BossKey"));
  if(b==null){var candidates=records.bosses.values().stream().limit(4096).filter(v->v.kind==BossKind.LIKHO&&distance(v.anchor,home)>=1000&&distance(v.anchor,home)<=2000&&(v.ready<=w.getGameTime()||v.active)).limit(4096).toList();b=candidates.stream().min(Comparator.comparingDouble(v->distance(v.anchor,home))).orElse(null);
   if(b==null){var r=new Random(w.getSeed()^home.asLong());for(int i=0;i<128;i++){double a=r.nextDouble()*Math.PI*2,d=1024+r.nextDouble()*950;var pos=home.offset((int)(Math.cos(a)*d),0,(int)(Math.sin(a)*d));var biome=w.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome(pos.getX()>>2,16,pos.getZ()>>2,w.getChunkSource().randomState().sampler());if(!BossAnchors.biome(w,biome,BossKind.LIKHO)||!w.getWorldBorder().isWithinBounds(pos)||records.bosses.values().stream().limit(4096).anyMatch(v->distance(v.anchor,pos)<96))continue;String key="wolf_story_likho:"+(pos.getX()>>4)+":"+(pos.getZ()>>4);b=new HuntRecords.Boss(key,BossKind.LIKHO,new BlockPos(pos.getX(),0,pos.getZ()),false);records.bosses.put(key,b);records.setDirty();break;}}
   if(b==null)return;q.putString("BossKey",b.key);q.putLong("Hint",clue(b.anchor,new Random(w.getSeed()^b.anchor.asLong()^p.getUUID().getLeastSignificantBits())).asLong());q.putLong("BoundAt",w.getGameTime());if(q.getBoolean("Tracked"))GreyWolfStory.track(p,2);
  }
  if(b.target!=null)q.putUUID("Target",b.target);
  if(b.active)return; // Unloaded UUID is never treated as absence or a reason to duplicate.
  if(b.target!=null){if(q.getBoolean("Tracked"))GreyWolfStory.untrack(p,2);q.remove("BossKey");q.remove("Target");q.remove("Hint");return;} // Defeated by another source: reselect an unfinished encounter.
  if(Math.hypot(p.getX()-b.anchor.getX(),p.getZ()-b.anchor.getZ())>160||!w.hasChunkAt(b.anchor))return;
  var spot=BossAnchors.find(w,b.anchor,BossKind.LIKHO,0,24,64);if(spot==null||distance(spot,home)<1000||distance(spot,home)>2000)return;
  if(!org.slavicmyths.worldgen.ManualStructureRecords.get(w).overlapping(new net.minecraft.world.level.levelgen.structure.BoundingBox(spot.getX()-96,w.getMinBuildHeight(),spot.getZ()-96,spot.getX()+96,w.getMaxBuildHeight(),spot.getZ()+96)).isEmpty())return;
  if(w.getPoiManager().getInRange(h->h.is(net.minecraft.world.entity.ai.village.poi.PoiTypes.HOME),spot,96,net.minecraft.world.entity.ai.village.poi.PoiManager.Occupancy.ANY).findAny().isPresent())return;
  b.anchor=spot;q.putLong("Hint",clue(spot,new Random(w.getSeed()^spot.asLong()^p.getUUID().getLeastSignificantBits())).asLong());if(q.getBoolean("Tracked"))GreyWolfStory.track(p,2);if(BossAnchors.spawn(w,b,p)){q.putUUID("Target",b.target);records.setDirty();}
 }
 private StoryLikho(){}
}
