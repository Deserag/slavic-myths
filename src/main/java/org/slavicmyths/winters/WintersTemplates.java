package org.slavicmyths.winters;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
/** Server-issued receipts prevent a mere fabricated template=true flag from copying. */
public final class WintersTemplates extends SavedData {
 private CompoundTag issued=new CompoundTag();
 public static WintersTemplates get(ServerLevel w){return w.getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(WintersTemplates::new,WintersTemplates::load),"slavicmyths_winters197_templates");}
 public void issue(UUID id){issued.putBoolean(id.toString(),true);setDirty();}
 public boolean contains(UUID id){return issued.getBoolean(id.toString());}
 public static WintersTemplates load(CompoundTag n,HolderLookup.Provider p){var s=new WintersTemplates();s.issued=n.copy();return s;}
 @Override public CompoundTag save(CompoundTag n,HolderLookup.Provider p){return issued.copy();}
}
