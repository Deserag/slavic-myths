package org.slavicmyths.sisters;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Death is recorded only by die(), never by unloaded/missing partner detection. */
public final class SistersLedger extends SavedData {
    private final CompoundTag deaths = new CompoundTag();
    public static SistersLedger get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(new SavedData.Factory<>(SistersLedger::new,(tag,lookup)->load(tag),null),"slavicmyths_sisters_33");
    }
    private static SistersLedger load(CompoundTag tag) {
        SistersLedger ledger = new SistersLedger();
        ledger.deaths.merge(tag.getCompound("Deaths"));
        return ledger;
    }
    public boolean partnerDied(UUID encounter,boolean survivorIsTryaseya) {
        return encounter != null && deaths.getInt(encounter.toString()) == (survivorIsTryaseya ? 1 : 2);
    }
    public void recordDeath(UUID encounter,boolean deadIsTryaseya) {
        if (encounter == null || deaths.contains(encounter.toString())) return;
        deaths.putInt(encounter.toString(),deadIsTryaseya ? 2 : 1);
        setDirty();
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider provider) {
        tag.put("Deaths",deaths.copy());
        return tag;
    }
}
