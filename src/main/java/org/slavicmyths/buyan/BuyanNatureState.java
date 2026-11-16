package org.slavicmyths.buyan;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Independent progress: never changes or replays the accepted foundation job. */
public final class BuyanNatureState extends SavedData {
    private static final String ID = "slavicmyths_buyan_2622_nature";
    private static final int SCHEMA = 1;

    private final List<BuyanState.Site> completed = new ArrayList<>();
    public boolean active;
    public boolean compatible = true;
    public boolean paused;
    public int centerX;
    public int centerZ;
    public int radius;
    public int phase;             // 0 terrain and soils, 1 trees and understory
    public int chunkIndex;
    public int columnIndex;
    public int skippedChunks;
    public int skippedColumns;
    public int plantedTrees;
    public double forestCutoff;
    public String lastError = "";

    public static BuyanNatureState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(BuyanNatureState::new, BuyanNatureState::load), ID);
    }

    public boolean finishedAt(BuyanState.Site site) {
        return completed.stream().anyMatch(s -> s.x() == site.x()
            && s.z() == site.z() && s.radius() == site.radius());
    }

    public void begin(BuyanState.Site site, double cutoff) {
        if (active || finishedAt(site)) {
            throw new IllegalStateException("Buyan nature generation already reserved or completed");
        }
        active = true;
        paused = false;
        centerX = site.x();
        centerZ = site.z();
        radius = site.radius();
        forestCutoff = cutoff;
        phase = 0;
        chunkIndex = 0;
        columnIndex = 0;
        skippedChunks = 0;
        skippedColumns = 0;
        plantedTrees = 0;
        lastError = "";
        setDirty();
    }

    public void progress(int chunk, int column) {
        chunkIndex = chunk;
        columnIndex = column;
        setDirty();
    }

    public void nextPhase() {
        if (phase != 0) throw new IllegalStateException("Unexpected Buyan nature pass");
        phase = 1;
        chunkIndex = 0;
        columnIndex = 0;
        setDirty();
    }

    public void skippedChunk() {
        skippedChunks++;
        setDirty();
    }

    public void skippedColumn() {
        skippedColumns++;
        setDirty();
    }

    public void tree() {
        plantedTrees++;
        setDirty();
    }

    public void pause(String reason) {
        paused = true;
        lastError = reason;
        setDirty();
    }

    public void resume() {
        paused = false;
        lastError = "";
        setDirty();
    }

    public void finish() {
        completed.add(new BuyanState.Site(centerX, centerZ, radius));
        active = false;
        paused = false;
        lastError = "";
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Schema", SCHEMA);
        tag.putBoolean("Active", active);
        tag.putBoolean("Paused", paused);
        tag.putInt("CenterX", centerX);
        tag.putInt("CenterZ", centerZ);
        tag.putInt("Radius", radius);
        tag.putInt("Phase", phase);
        tag.putInt("Chunk", chunkIndex);
        tag.putInt("Column", columnIndex);
        tag.putInt("SkippedChunks", skippedChunks);
        tag.putInt("SkippedColumns", skippedColumns);
        tag.putInt("Trees", plantedTrees);
        tag.putDouble("ForestCutoff", forestCutoff);
        tag.putString("Error", lastError);
        ListTag sites = new ListTag();
        for (BuyanState.Site site : completed) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("X", site.x());
            entry.putInt("Z", site.z());
            entry.putInt("Radius", site.radius());
            sites.add(entry);
        }
        tag.put("Completed", sites);
        return tag;
    }

    public static BuyanNatureState load(CompoundTag tag, HolderLookup.Provider registries) {
        BuyanNatureState state = new BuyanNatureState();
        state.active = tag.getBoolean("Active");
        state.paused = tag.getBoolean("Paused");
        state.centerX = tag.getInt("CenterX");
        state.centerZ = tag.getInt("CenterZ");
        state.radius = tag.getInt("Radius");
        state.phase = tag.getInt("Phase");
        state.chunkIndex = Math.max(0, tag.getInt("Chunk"));
        state.columnIndex = Math.clamp(tag.getInt("Column"), 0, 255);
        state.skippedChunks = Math.max(0, tag.getInt("SkippedChunks"));
        state.skippedColumns = Math.max(0, tag.getInt("SkippedColumns"));
        state.plantedTrees = Math.max(0, tag.getInt("Trees"));
        state.forestCutoff = tag.getDouble("ForestCutoff");
        state.compatible = tag.getInt("Schema") == SCHEMA;
        state.lastError = tag.getString("Error");
        for (Tag entry : tag.getList("Completed", 10)) {
            CompoundTag site = (CompoundTag) entry;
            state.completed.add(new BuyanState.Site(site.getInt("X"),
                site.getInt("Z"), site.getInt("Radius")));
        }
        if (state.active && (!state.compatible
            || state.phase < 0 || state.phase > 1
            || !Double.isFinite(state.forestCutoff))) {
            state.pause("Unsupported nature generation state; cannot safely resume");
        }
        return state;
    }
}
