package org.slavicmyths.buyan;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

/** Persistent, resumable progress for explicit large-island construction. */
public final class BuyanState extends SavedData {
    public record Site(int x, int z, int radius) {}

    private static final String ID = "slavicmyths_buyan_2621";
    private final List<Site> completed = new ArrayList<>();
    public boolean active;
    public boolean paused;
    public int centerX;
    public int centerZ;
    public int radius;
    public int chunkIndex;
    public int columnIndex;
    public String lastError = "";

    public static BuyanState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
            new SavedData.Factory<>(BuyanState::new, BuyanState::load), ID);
    }

    public boolean overlaps(int x, int z, int newRadius) {
        for (Site site : completed) {
            long dx = (long) x - site.x();
            long dz = (long) z - site.z();
            long exclusion = (long) newRadius + site.radius()
                + BuyanTerrain.margin(newRadius) + BuyanTerrain.margin(site.radius()) + 128;
            if (dx * dx + dz * dz < exclusion * exclusion) return true;
        }
        return false;
    }

    public void begin(int x, int z, int r) {
        if (active) throw new IllegalStateException("Buyan construction is already reserved");
        active = true;
        paused = false;
        centerX = x;
        centerZ = z;
        radius = r;
        chunkIndex = 0;
        columnIndex = 0;
        lastError = "";
        setDirty();
    }

    public void progress(int chunk, int column) {
        chunkIndex = chunk;
        columnIndex = column;
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
        completed.add(new Site(centerX, centerZ, radius));
        active = false;
        paused = false;
        lastError = "";
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("Schema", 1);
        tag.putBoolean("Active", active);
        tag.putBoolean("Paused", paused);
        tag.putInt("CenterX", centerX);
        tag.putInt("CenterZ", centerZ);
        tag.putInt("Radius", radius);
        tag.putInt("Chunk", chunkIndex);
        tag.putInt("Column", columnIndex);
        tag.putString("Error", lastError);
        ListTag sites = new ListTag();
        for (Site site : completed) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("X", site.x());
            entry.putInt("Z", site.z());
            entry.putInt("Radius", site.radius());
            sites.add(entry);
        }
        tag.put("Completed", sites);
        return tag;
    }

    public static BuyanState load(CompoundTag tag, HolderLookup.Provider registries) {
        BuyanState data = new BuyanState();
        data.active = tag.getBoolean("Active");
        data.paused = tag.getBoolean("Paused");
        data.centerX = tag.getInt("CenterX");
        data.centerZ = tag.getInt("CenterZ");
        data.radius = tag.getInt("Radius");
        data.chunkIndex = Math.max(0, tag.getInt("Chunk"));
        data.columnIndex = Math.clamp(tag.getInt("Column"), 0, 255);
        data.lastError = tag.getString("Error");
        for (Tag value : tag.getList("Completed", 10)) {
            CompoundTag site = (CompoundTag) value;
            int radius = site.getInt("Radius");
            if (radius >= BuyanTerrain.MIN_RADIUS && radius <= BuyanTerrain.MAX_RADIUS) {
                data.completed.add(new Site(site.getInt("X"), site.getInt("Z"), radius));
            }
        }
        if (data.active && (data.radius < BuyanTerrain.MIN_RADIUS
            || data.radius > BuyanTerrain.MAX_RADIUS)) {
            data.paused = true;
            data.lastError = "Invalid saved radius";
        }
        return data;
    }
}
