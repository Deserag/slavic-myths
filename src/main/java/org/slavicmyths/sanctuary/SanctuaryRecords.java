package org.slavicmyths.sanctuary;
import java.util.*;
import net.minecraft.nbt.*;
import net.minecraft.core.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.saveddata.SavedData;
public final class SanctuaryRecords extends SavedData {
 private CompoundTag root=new CompoundTag();
 public static SanctuaryRecords get(ServerLevel w){return w.getServer().overworld().getDataStorage().computeIfAbsent(new SavedData.Factory<>(SanctuaryRecords::new,SanctuaryRecords::load),"slavicmyths_sanctuaries193");}
 private CompoundTag part(String name){if(!root.contains(name,10))root.put(name,new CompoundTag());return root.getCompound(name);}
 public CompoundTag row(UUID id){var all=part("Structures");if(!all.contains(id.toString(),10))all.put(id.toString(),new CompoundTag());return all.getCompound(id.toString());}
 public CompoundTag player(UUID id){var all=part("Players");if(!all.contains(id.toString(),10))all.put(id.toString(),new CompoundTag());return all.getCompound(id.toString());}
 public void ensure(UUID id,BlockPos origin,Rotation r,boolean spring){var n=row(id);if(n.contains("Origin"))return;n.putLong("Origin",origin.asLong());n.putString("Rotation",r.name());n.putBoolean("Spring",spring);var box=SanctuaryGeometry.guardian(origin,spring,r);n.putIntArray("GuardianBounds",new int[]{box.minX(),box.minY(),box.minZ(),box.maxX(),box.maxY(),box.maxZ()});var arena=SanctuaryGeometry.arena(origin,spring,r);n.putIntArray("ArenaBounds",new int[]{arena.minX(),arena.minY(),arena.minZ(),arena.maxX(),arena.maxY(),arena.maxZ()});n.putLong("Pedestal",SanctuaryGeometry.pedestal(origin,spring,r).asLong());n.putLong("SpawnAnchor",SanctuaryGeometry.spawnAnchor(origin,spring,r).asLong());n.putLong("RewardAnchor",SanctuaryGeometry.pedestal(origin,spring,r).above().asLong());n.putLong("Door",SanctuaryGeometry.door(origin,spring,r).asLong());n.putString("RelicDoorState","SEALED_193");setDirty();}
 public boolean restored(UUID id){return row(id).getBoolean("FireRestored");}public boolean cleansed(UUID id){return row(id).getInt("Pools")==3;}
 public boolean restore(UUID id){var n=row(id);if(n.getBoolean("FireRestored"))return false;n.putBoolean("FireRestored",true);setDirty();return true;}
 public boolean pool(UUID id,int index){var n=row(id);int mask=1<<index;if((n.getInt("Pools")&mask)!=0)return false;n.putInt("Pools",n.getInt("Pools")|mask);n.putInt("PoolsPending",n.getInt("PoolsPending")|mask);setDirty();return true;}
 public boolean claim(UUID id,UUID player){var n=row(id);var claims=n.getCompound("FireClaims");if(claims.getBoolean(player.toString()))return false;claims.putBoolean(player.toString(),true);n.put("FireClaims",claims);setDirty();return true;}
 public void discover(ServerPlayer p,UUID id){var s=row(id);if(!s.contains("Origin"))return;boolean spring=s.getBoolean("Spring");var n=player(p.getUUID());String key=spring?"SpringFound":"FireFound";n.putBoolean(key,true);n.putLong(key+"Location",s.getLong("Origin"));n.putUUID(key+"Instance",id);var known=n.getCompound("KnownLocations");known.put(id.toString(),s.copy());n.put("KnownLocations",known);setDirty();String tale=spring?"abode_pure_spring":"sanctuary_unfading_fire";org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:"+tale,3,false);org.slavicmyths.tales.TaleService.hint(p,"slavicmyths:"+tale,p.level().dimension().location().toString(),id,BlockPos.of(s.getLong("Origin")));org.slavicmyths.item.FolkAccessoryItem.award(p,"found_ancient_sanctuary");if(n.getBoolean("SpringFound")&&n.getBoolean("FireFound")){n.putBoolean("BothSanctuariesFound",true);org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:two_sanctuaries",4,true);org.slavicmyths.item.FolkAccessoryItem.award(p,"found_two_sanctuaries");}}
 public static SanctuaryRecords load(CompoundTag n,HolderLookup.Provider registry){var d=new SanctuaryRecords();d.root=n.copy();return d;}
 @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider registry){return root.copy();}
}
