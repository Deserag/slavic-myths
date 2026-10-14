package org.slavicmyths.gorynych;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.level.saveddata.SavedData;
public final class GorynychLedger extends SavedData {
 private CompoundTag root=new CompoundTag();
 public static GorynychLedger get(ServerLevel w){return w.getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(GorynychLedger::new,GorynychLedger::load),"slavicmyths_gorynych196");}
 public static CompoundTag part(CompoundTag n,String key){if(!n.contains(key,10))n.put(key,new CompoundTag());return n.getCompound(key);}
 public CompoundTag row(UUID id){return part(part(root,"Encounters"),id.toString());}
 public UUID world(){if(!root.hasUUID("World")){root.putUUID("World",UUID.randomUUID());setDirty();}return root.getUUID("World");}
 public void ensure(UUID id,CompoundTag hill){var n=row(id);if(n.contains("Origin"))return;n.putLong("Origin",hill.getLong("Origin"));n.putString("Rotation",hill.getString("Rotation"));n.putLong("Seed",hill.getLong("Seed"));n.putUUID("World",world());n.putInt("State",GorynychRules.PATROL);n.putFloat("HP",GorynychRules.HP);n.putInt(GorynychRules.BALANCE,GorynychRules.BALANCE_VERSION);setDirty();}
 public void participant(UUID id,UUID player){if(player!=null){part(row(id),"Participants").putBoolean(player.toString(),true);setDirty();}}
 public static GorynychLedger load(CompoundTag n,HolderLookup.Provider lookup){var d=new GorynychLedger();d.root=n.copy();for(String id:part(d.root,"Encounters").getAllKeys()){var row=d.row(UUID.fromString(id));if(GorynychRules.migrate(row,"HP","State"))d.setDirty();row.remove("PopulationAt");row.remove("MissingSince");}return d;}
 @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider lookup){return root.copy();}
}
