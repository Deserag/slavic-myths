package org.slavicmyths.posad;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import org.slavicmyths.registry.ModEntities;
import org.slavicmyths.worldgen.ManualStructureRecords;

/** Bounded, restart-safe insertion into an already completed manual city. No chunk tickets. */
public final class PosadResidentSpawner {
    public record Result(int added, int existing, int blocked) {}
    public static Result populate(ServerLevel world, int anchorX, int deckY, int anchorZ, int turns) {
        return populateVerified(world, anchorX, deckY, anchorZ, turns, false);
    }
    public static Result populateNatural(ServerLevel world, int anchorX, int deckY, int anchorZ, int turns) {
        return populateVerified(world, anchorX, deckY, anchorZ, turns, true);
    }
    private static Result populateVerified(ServerLevel world, int anchorX, int deckY, int anchorZ, int turns, boolean nativeStructure) {
        if (turns < 0 || turns > 3) throw new IllegalArgumentException("Invalid city rotation");
        BlockPos anchor = new BlockPos(anchorX, deckY, anchorZ);
        boolean completed = ManualStructureRecords.get(world).entries().stream().anyMatch(e -> e.family().equals("nether_posad") && e.arrival().equals(anchor));
        if (!nativeStructure && !completed) throw new IllegalStateException("Only verified completed manual cities can be populated");
        UUID settlement = PosadPopulationPlan.settlementId(world.dimension().location().toString(), anchorX, deckY, anchorZ);
        PosadGrievances records = PosadGrievances.get(world);
        int added = 0, existing = 0, blocked = 0;
        for (var member : PosadPopulationPlan.at(world.dimension().location().toString(), anchorX, deckY, anchorZ, turns)) {
            if (records.spawned(settlement, member.member().key())) {
                existing++;
                continue;
            }
            if (world.getEntity(member.identity()) != null) {
                records.markSpawned(settlement, member.member().key());
                existing++;
                continue;
            }
            PosadResidentEntity npc = ModEntities.POSAD_RESIDENT.get().create(world);
            if (npc == null) { blocked++; continue; }
            BlockPos spot = safe(world, member.worldX(), deckY, member.worldZ(), npc);
            if (spot == null) { blocked++; continue; }
            npc.moveTo(spot.getX() + .5, spot.getY(), spot.getZ() + .5, turns * 90F, 0);
            npc.configure(member, settlement, spot);
            if (world.addFreshEntity(npc)) {
                records.markSpawned(settlement, member.member().key());
                added++;
            } else blocked++;
        }
        return new Result(added, existing, blocked);
    }
    /** Spawn residents of an independently placed authored building using the same persistent roster. */
    public static Result populateBuilding(ServerLevel world, String template, BlockPos origin, int width, int depth) {
        int offsetX, offsetZ;
        switch (template) {
            case "veche_hall_2_2_2" -> { offsetX = 42; offsetZ = 12; }
            case "communal_house_2_2_3" -> { offsetX = 13; offsetZ = 15; }
            case "druzhinny_dvor_2_2_4" -> { offsetX = 12; offsetZ = 75; }
            case "kuznechny_dvor_2_2_5" -> { offsetX = 12; offsetZ = 44; }
            case "menovye_ryady_2_2_6" -> { offsetX = 75; offsetZ = 75; }
            case "zeleynaya_2_2_7" -> { offsetX = 75; offsetZ = 12; }
            case "granary_2_2_8" -> { offsetX = 75; offsetZ = 12; }
            default -> { return new Result(0, 0, 0); }
        }
        UUID settlement = PosadPopulationPlan.settlementId(world.dimension().location().toString(), origin.getX() - offsetX, origin.getY(), origin.getZ() - offsetZ);
        PosadGrievances records = PosadGrievances.get(world);
        int added = 0, existing = 0, blocked = 0;
        for (var member : PosadPopulationPlan.roster()) {
            int dx = member.localX() - offsetX, dz = member.localZ() - offsetZ;
            if (dx < 0 || dz < 0 || dx >= width || dz >= depth) continue;
            String slot = member.key();
            UUID identity = PosadPopulationPlan.memberId(settlement, slot);
            if (records.spawned(settlement, slot)) { existing++; continue; }
            if (world.getEntity(identity) != null) { records.markSpawned(settlement, slot); existing++; continue; }
            PosadResidentEntity npc = ModEntities.POSAD_RESIDENT.get().create(world);
            if (npc == null) { blocked++; continue; }
            BlockPos spot = safe(world, origin.getX() + dx, origin.getY(), origin.getZ() + dz, npc);
            if (spot == null) { blocked++; continue; }
            var positioned = new PosadPopulationPlan.WorldMember(identity, member, origin.getX() + dx, origin.getZ() + dz);
            npc.moveTo(spot.getX() + .5, spot.getY(), spot.getZ() + .5, 0, 0);
            npc.configure(positioned, settlement, spot);
            if (world.addFreshEntity(npc)) { records.markSpawned(settlement, slot); added++; } else blocked++;
        }
        return new Result(added, existing, blocked);
    }
    private static BlockPos safe(ServerLevel world, int x, int deckY, int z, PosadResidentEntity npc) {
        // Look only inside a fixed 5x5 area and four deck levels. Failed candidates stay retryable.
        for (int radius = 0; radius <= 2; radius++) for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
            if (radius > 0 && Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
            for (int dy = 1; dy <= 4; dy++) {
                BlockPos p = new BlockPos(x + dx, deckY + dy, z + dz), below = p.below();
                if (!world.hasChunkAt(p) || !world.hasChunkAt(p.above()) || !world.hasChunkAt(below)) continue;
                if (world.getBlockState(below).is(Blocks.MAGMA_BLOCK) || world.getBlockState(below).is(Blocks.FIRE) || world.getBlockState(below).is(Blocks.SOUL_FIRE)) continue;
                if (!world.getBlockState(below).isFaceSturdy(world, below, Direction.UP)) continue;
                if (!world.getFluidState(p).isEmpty() || !world.getFluidState(p.above()).isEmpty()) continue;
                if (!world.getBlockState(p).getCollisionShape(world, p).isEmpty() || !world.getBlockState(p.above()).getCollisionShape(world, p.above()).isEmpty()) continue;
                if (!world.getEntitiesOfClass(PosadResidentEntity.class, new AABB(p).inflate(.4)).isEmpty()) continue;
                npc.moveTo(p.getX() + .5, p.getY(), p.getZ() + .5, 0, 0);
                if (world.noCollision(npc)) return p;
            }
        }
        return null;
    }
    private PosadResidentSpawner() {}
}
