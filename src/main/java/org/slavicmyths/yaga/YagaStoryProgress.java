package org.slavicmyths.yaga;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.GameType;
import org.slavicmyths.tales.TaleData;
/** Durable book projection only. This class never grants combat criteria or trust rewards. */
public final class YagaStoryProgress {
 public static final String ENTRY="slavicmyths:yaga_search";
 public enum Status { AVAILABLE,SEARCHING,FOUND,UNAVAILABLE }
 public static boolean reconcile(TaleData tales,YagaData yaga,UUID player,GameType mode,Predicate<String> completed,long now){
  var relation=yaga.players.get(player);
  boolean known=relation!=null&&(relation.intro>0||relation.stage>0);
  if(!known&&!YagaAccessRules.allowed(mode==GameType.CREATIVE,tales.stage(player,ENTRY),completed.test("name_your_misfortune"),completed.test("steppe_champion"),completed.test("defeat_nightingale")))return false;
  tales.promote(player,ENTRY,1,now,false);
  var search=tales.player(player).getCompound("YagaSearch");
  if(search.getString("Status").isEmpty())state(tales,player,Status.AVAILABLE,"");
  return true;
 }
 public static void state(TaleData tales,UUID player,Status status,String reason){
  if(!tales.compatible())return;
  var n=new CompoundTag();n.putString("Status",status.name());n.putString("Reason",reason);tales.player(player).put("YagaSearch",n);tales.setDirty();
 }
 public static CompoundTag delta(TaleData tales,UUID player){
  var n=new CompoundTag();var entries=new CompoundTag();entries.put(ENTRY,tales.player(player).getCompound("Entries").getCompound(ENTRY).copy());n.put("Entries",entries);n.put("YagaSearch",tales.player(player).getCompound("YagaSearch").copy());return n;
 }
 private YagaStoryProgress(){}
}
