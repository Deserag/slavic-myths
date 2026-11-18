package org.slavicmyths.guardians;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.sanctuary.*;
/** Instance combat state; player claims are global per fragment type, not per shrine. */
public final class GuardianLedger extends SavedData {
 private CompoundTag root=new CompoundTag();
 private final Map<Long,Set<UUID>> rooms=new HashMap<>();
 public static GuardianLedger get(ServerLevel w){return w.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(GuardianLedger::new,GuardianLedger::load),"slavicmyths_guardians194");}
 private static CompoundTag part(CompoundTag n,String key){if(!n.contains(key,10))n.put(key,new CompoundTag());return n.getCompound(key);}
 public CompoundTag row(UUID id){return part(part(root,"Encounters"),id.toString());}
 public Set<UUID> near(BlockPos pos){return rooms.getOrDefault(net.minecraft.world.level.ChunkPos.asLong(pos.getX()>>4,pos.getZ()>>4),Set.of());}
 public void ensure(UUID id,CompoundTag sanctuary){if(!sanctuary.contains("Origin"))return;var n=row(id);if(n.contains("Origin"))return;for(String key:List.of("Origin","Rotation","Spring","GuardianBounds","ArenaBounds","Pedestal","SpawnAnchor","Door"))if(sanctuary.contains(key))n.put(key,sanctuary.get(key).copy());n.putString("Stage","SEALED");org.slavicmyths.combat.CreatureHealthBalance.mark(n);index(id,n);setDirty();}
 private void index(UUID id,CompoundTag n){var a=n.getIntArray("GuardianBounds");if(a.length!=6)return;for(int x=(a[0]-5)>>4;x<=(a[3]+5)>>4;x++)for(int z=(a[2]-5)>>4;z<=(a[5]+5)>>4;z++)rooms.computeIfAbsent(net.minecraft.world.level.ChunkPos.asLong(x,z),k->new HashSet<>()).add(id);}
 public CompoundTag standalone(UUID id,BlockPos p,boolean water){var n=new CompoundTag();n.putLong("Origin",p.asLong());n.putString("Rotation","NONE");n.putBoolean("Spring",water);int[] bounds={p.getX()-10,p.getY(),p.getZ()-10,p.getX()+10,p.getY()+8,p.getZ()+10};n.putIntArray("GuardianBounds",bounds);n.putIntArray("ArenaBounds",bounds);n.putLong("SpawnAnchor",p.asLong());n.putLong("Pedestal",p.asLong());ensure(id,n);var result=row(id);result.putBoolean("Standalone",true);result.putUUID("Boss",id);stage(id,"READY");return result;}
 public String stage(UUID id){return row(id).getString("Stage");}
 /** The existing sanctuary flags also unlock legacy rooms; never reset combat or claims. */
 public boolean unlock(UUID id,SanctuaryRecords sanctuary){var n=row(id);if(!n.contains("Origin")||n.getBoolean("Standalone")||!stage(id).equals("SEALED"))return false;boolean ready=n.getBoolean("Spring")?sanctuary.cleansed(id):sanctuary.restored(id);if(!ready)return false;stage(id,"READY");n.putString("RelicDoorState","OPEN");sanctuary.row(id).putString("RelicDoorState","OPEN_194");sanctuary.setDirty();return true;}
 public void stage(UUID id,String stage){row(id).putString("Stage",stage);setDirty();}
 public void participate(UUID instance,UUID player){if(player==null)return;part(row(instance),"Participants").putBoolean(player.toString(),true);setDirty();}
 public boolean claimed(UUID player,boolean spring){return part(part(root,"Claims"),player.toString()).getBoolean(spring?"Water":"Fire");}
 public boolean rewardAvailable(UUID instance){var n=row(instance);return n.contains("Origin")&&!n.getBoolean("Standalone")&&n.getString("Stage").equals("CLEARED");}
 public synchronized boolean claim(ServerPlayer p,UUID instance){
  var n=row(instance);if(!rewardAvailable(instance)||p.isSpectator()||!p.isAlive())return false;
  boolean spring=n.getBoolean("Spring");String key=spring?"Water":"Fire";var receipt=part(org.slavicmyths.rpg.PathData.data(p),"Guardian194Claims");
  if(claimed(p.getUUID(),spring)||receipt.getBoolean(key)){part(part(root,"Claims"),p.getUUID().toString()).putBoolean(key,true);setDirty();p.displayClientMessage(GuardianContent.text("already_claimed"),true);return false;}
  if(p.getInventory().getFreeSlot()<0){p.displayClientMessage(GuardianContent.text("inventory_full"),true);return false;}
  var stack=new ItemStack(spring?GuardianContent.WATER_FRAGMENT.get():GuardianContent.FIRE_FRAGMENT.get());p.getInventory().add(stack);if(!stack.isEmpty())return false;
  // Inventory and receipt are persisted together BEFORE the world claim is consumed.
  receipt.putBoolean(key,true);p.getInventory().setChanged();((org.slavicmyths.equipment.mixin.FrostPlayerSaveInvoker)p.server.getPlayerList()).slavicmyths$saveFrost(p);
  part(part(root,"Claims"),p.getUUID().toString()).putBoolean(key,true);setDirty();p.server.getLevel(net.minecraft.world.level.Level.OVERWORLD).getDataStorage().save();
  GuardianContent.sound(p.level(),p.blockPosition(),"amulet_fragment_claim");org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:"+(spring?"cleansed_spring_keeper":"cleansed_flame_guardian"),4,true);org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:mor_guardian_fragments",claimed(p.getUUID(),!spring)?4:3,claimed(p.getUUID(),!spring));if(org.slavicmyths.progression.AdvancementMilestones.eligible(p)&&n.getCompound("Participants").getBoolean(p.getUUID().toString()))org.slavicmyths.progression.Knowledge.award(p,spring?"claimed_water_fragment":"claimed_fire_fragment");return true;
 }
 public static GuardianLedger load(CompoundTag tag,HolderLookup.Provider registry){var d=new GuardianLedger();d.root=tag.copy();for(String key:part(d.root,"Encounters").getAllKeys()){var n=d.row(UUID.fromString(key));GuardianRules.migrateRecord(n);if(n.getString("Stage").equals("ACTIVE"))n.putString("Stage","READY");n.putInt("Valves",0);n.remove("ValveBlockedUntil");n.remove("MissingSince");n.remove("PopulationCheckAt");n.remove("PendingValve0");n.remove("PendingValve1");d.index(UUID.fromString(key),n);}d.setDirty();return d;}
 @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider registry){return root.copy();}
}
