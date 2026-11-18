package org.slavicmyths.chud;

import java.util.HashSet;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/** Natural features yield to occupied Chud volumes, including their reserved air.
 * Reads only the existing worldgen dependency region; never requests a world chunk.
 */
public final class ChudFeatureProtection {
    private ChudFeatureProtection() {}

    public static boolean intersects(WorldGenLevel level, BoundingBox affected) {
        if (!(level instanceof WorldGenRegion region)) return false;
        var seen = new HashSet<StructureStart>();
        for (int x = affected.minX() >> 4; x <= affected.maxX() >> 4; x++) {
            for (int z = affected.minZ() >> 4; z <= affected.maxZ() >> 4; z++) {
                if (!region.hasChunk(x, z)) continue;
                // Neighbor dependency chunks may have STARTS but not REFERENCES yet.
                // Never ask StructureManager to promote their generation status.
                var chunk = region.getChunk(x, z, net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS, false);
                if (chunk == null) continue;
                var starts = new java.util.ArrayList<StructureStart>();
                for (var entry : chunk.getAllStarts().entrySet()) if (entry.getKey() instanceof ChudStructure || entry.getKey() instanceof AbandonedStructure) starts.add(entry.getValue());
                for (var entry : chunk.getAllReferences().entrySet()) {
                    if (!(entry.getKey() instanceof ChudStructure || entry.getKey() instanceof AbandonedStructure)) continue;
                    for (long owner : entry.getValue()) {
                        var at = new ChunkPos(owner);
                        if (!region.hasChunk(at.x, at.z)) continue;
                        var root = region.getChunk(at.x, at.z, net.minecraft.world.level.chunk.status.ChunkStatus.STRUCTURE_STARTS, false);
                        if (root != null) {var start = root.getStartForStructure(entry.getKey()); if (start != null) starts.add(start);}
                    }
                }
                for (var start : starts) {
                    if (!seen.add(start) || !start.getBoundingBox().intersects(affected)) continue;
                    for (var piece : start.getPieces()) {
                        if (!piece.getBoundingBox().intersects(affected)) continue;
                        if(piece instanceof ChudPiece city&&city.repairsRoof()&&city.roofCover().intersects(affected))return true;
                        if(piece instanceof AbandonedPiece abandoned&&abandoned.repairsRoof()&&abandoned.roofCover().intersects(affected))return true;
                        var fills = piece instanceof ChudPiece city ? city.blueprint()
                                : piece instanceof AbandonedPiece abandoned ? abandoned.blueprint()
                                : piece instanceof ChudAccessPiece access ? access.access.blueprint() : null;
                        if (fills != null) for (var fill : fills) {
                            if (fill.box().intersects(affected)) return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
