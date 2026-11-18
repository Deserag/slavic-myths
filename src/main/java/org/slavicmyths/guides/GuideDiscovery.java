package org.slavicmyths.guides;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.slavicmyths.tales.*;
/** Read-only reconciliation of earned victories and known cat homes. Never spawns cats or scans chunks. */
public final class GuideDiscovery {
 public static void refresh(ServerPlayer p){reconcile(p);var delta=new net.minecraft.nbt.CompoundTag();delta.put("Hints",TaleService.hints(p));TaleNetwork.send(p,delta,false,false);}
 public static void reconcile(ServerPlayer p){
  if(!TaleData.get(p.serverLevel()).compatible())return;
  int count=GuideQuests.victories(p);if(count<2)return;
  TaleService.promote(p,"slavicmyths:bayun_search",1,false);
  var state=TaleData.get(p.serverLevel()).player(p.getUUID());var search=TaleData.compound(state,"BayunSearch");
  var home=org.slavicmyths.bayun.BayunHomes.get(p.server.overworld()).nearest(p.blockPosition());if(home==null)return;
  var loaded=p.server.overworld().getEntity(home.entity());if(p.server.overworld().hasChunkAt(home.position())&&(loaded==null||!loaded.isAlive()))return;
  var oldHint=state.getCompound("Hints").getCompound("slavicmyths:bayun_search");
  if(search.hasUUID("Target")&&search.getUUID("Target").equals(home.entity())&&search.getInt("Victories")==count&&oldHint.getLong("Expires")>=p.level().getGameTime())return;
  search.putUUID("Target",home.entity());search.putInt("Victories",count);
  TaleService.hint(p,"slavicmyths:bayun_search",Level.OVERWORLD.location().toString(),home.entity(),home.position());
  var hint=state.getCompound("Hints").getCompound("slavicmyths:bayun_search");hint.putInt("Radius",count>=3?180:320);hint.putString("Source","tales.entry.bayun_search.title");TaleData.get(p.serverLevel()).setDirty();
 }
 private GuideDiscovery(){}
}
