package org.slavicmyths.gorynych;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.*;
public final class GorynychRewards {
 public static boolean claim(ServerPlayer p,UUID id){var w=p.serverLevel();var d=GorynychLedger.get(w);var n=d.row(id);if(!p.isCreative()&&!org.slavicmyths.serpenthill.HillEvents.protectedPlayer(p))return false;if(n.getInt("State")!=GorynychRules.PURIFIED||n.getBoolean("Standalone")||!GorynychLedger.part(n,"Participants").getBoolean(p.getUUID().toString())||!p.isAlive()||p.isSpectator())return false;
  var personal=GorynychLedger.part(org.slavicmyths.rpg.PathData.data(p),"Gorynych196");var receipt=GorynychLedger.part(personal,id.toString());var row=GorynychLedger.part(GorynychLedger.part(n,"Rewards"),p.getUUID().toString());if(row.getBoolean("Claimed")||receipt.getBoolean("Claimed")){row.putBoolean("Claimed",true);d.setDirty();return false;}
  if(p.getInventory().getFreeSlot()<0||free(p)<3){p.displayClientMessage(GorynychContent.text("inventory_full"),true);return false;}
  if(!row.contains("Rune"))row.putInt("Rune",w.random.nextInt(3));int rune=Math.floorMod(row.getInt("Rune"),3);
  p.getInventory().add(new ItemStack(GorynychContent.item("gorynych_scale"),4));p.getInventory().add(new ItemStack(GorynychContent.item("serpent_seal")));p.getInventory().add(new ItemStack(GorynychContent.item("rune_"+GorynychContent.RUNES.get(rune))));receipt.putBoolean("Claimed",true);receipt.putBoolean("CreativeClaim",p.isCreative());receipt.putLong("ShedAt",w.getGameTime()+GorynychRules.SHED_INTERVAL);save(p);row.putBoolean("Claimed",true);d.setDirty();w.getServer().overworld().getDataStorage().save();org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:zmey_gorynych",4,true);p.sendSystemMessage(GorynychContent.text("reward"));return true;
 }
 private static int free(ServerPlayer p){int count=0;for(int i=0;i<36;i++)if(p.getInventory().getItem(i).isEmpty())count++;return count;}
 public static void interact(ServerPlayer p,UUID id){if(claim(p,id))return;var n=GorynychLedger.get(p.serverLevel()).row(id);if(n.getBoolean("Standalone")||n.getInt("State")!=GorynychRules.PURIFIED)return;var receipt=GorynychLedger.part(GorynychLedger.part(org.slavicmyths.rpg.PathData.data(p),"Gorynych196"),id.toString());long now=p.serverLevel().getGameTime();if(!receipt.getBoolean("Claimed"))return;if(now<receipt.getLong("ShedAt")){p.displayClientMessage(GorynychContent.text("shed_wait",(receipt.getLong("ShedAt")-now+19)/20),true);return;}var stack=new ItemStack(GorynychContent.item("gorynych_scale"));if(!p.getInventory().add(stack))return;receipt.putLong("ShedAt",now+GorynychRules.SHED_INTERVAL);save(p);}
 private static void save(ServerPlayer p){p.getInventory().setChanged();((org.slavicmyths.equipment.mixin.FrostPlayerSaveInvoker)p.server.getPlayerList()).slavicmyths$saveFrost(p);}
 private GorynychRewards(){}
}
