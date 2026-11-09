package org.slavicmyths.ships;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Per-dimension write-ahead journal for ship block -> moving contraption
 * transitions. Cargo is copied BEFORE deleting one single world block.
 *
 * This is a recovery checkpoint, NOT permission to blindly spawn duplicate
 * hulls: a ship entity may already carry the authoritative moving payload.
 * A recovery tool must first reconcile ship UUID, entity and chunk occupancy.
 */
public final class ShipMotionJournal extends SavedData {
    private static final String NAME = "slavicmyths_moving_ship_journal";
    private final Map<UUID, CompoundTag> entries = new LinkedHashMap<>();

    public static ShipMotionJournal get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(ShipMotionJournal::new, ShipMotionJournal::load), NAME);
    }

    public static ShipMotionJournal load(CompoundTag root, HolderLookup.Provider registries) {
        ShipMotionJournal data = new ShipMotionJournal();
        ListTag list = root.getList("Entries", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag record = list.getCompound(i);
            if (!record.hasUUID("ShipId")
                    || !record.contains("Motion", Tag.TAG_COMPOUND)
                    || ShipMovingContraption.load(record.getCompound("Motion")) == null)
                continue;
            data.entries.put(record.getUUID("ShipId"), record.copy());
        }
        return data;
    }

    @Override public CompoundTag save(CompoundTag result, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (CompoundTag value : entries.values()) list.add(value.copy());
        result.put("Entries", list);
        return result;
    }

    /** The first save MUST complete before calling detachForMotion. */
    public void begin(ServerLevel level, UUID shipId, BlockPos original,
            ShipMovingContraption prepared) {
        if (entries.containsKey(shipId))
            throw new IllegalStateException("Already journaled moving ship " + shipId);
        CompoundTag record = new CompoundTag();
        record.putUUID("ShipId", shipId);
        record.putLong("Original", original.asLong());
        record.putString("Phase", "PREPARED");
        record.put("Motion", prepared.save());
        entries.put(shipId, record);
        setDirty();
        level.getDataStorage().save();
    }

    /** Repeatable checkpoint of the detached pose and its unchanged cargo. */
    public void checkpoint(UUID shipId, ShipMovingContraption body) {
        CompoundTag record = entries.get(shipId);
        if (record == null) return;
        record.putString("Phase", body.detached() ? "MOVING" : "PARKED");
        record.put("Motion", body.save());
        setDirty();
    }

    public void end(ServerLevel level, UUID shipId) {
        if (entries.remove(shipId) == null) return;
        setDirty();
        level.getDataStorage().save();
    }

    public CompoundTag lookup(UUID shipId) {
        CompoundTag entry = entries.get(shipId);
        return entry == null ? null : entry.copy();
    }
}
