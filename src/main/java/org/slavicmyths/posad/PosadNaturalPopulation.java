package org.slavicmyths.posad;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Retry city roster insertion as its individual generated chunks become available. */
@EventBusSubscriber(modid = "slavicmyths")
public final class PosadNaturalPopulation {
    private static int ticks;
    private static final int SEARCH_RADIUS = 128;
    private static final int SAMPLE_STEP = 32;
    private static final int STRUCTURE_SAMPLE_Y = 32;

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        if (++ticks % 100 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().equals(Level.NETHER)) continue;
            var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths", "nether_posad"));
            if (structure == null) continue;
            Set<BlockPos> visited = new HashSet<>();
            for (var player : level.players()) {
                BlockPos current = player.blockPosition();
                // Scan loaded chunks around the player, independent of their altitude.
                // A 32-block lattice intersects the 113x113 footprint from any approach.
                for (int ox = -SEARCH_RADIUS; ox <= SEARCH_RADIUS; ox += SAMPLE_STEP) for (int oz = -SEARCH_RADIUS; oz <= SEARCH_RADIUS; oz += SAMPLE_STEP) {
                    BlockPos sample = new BlockPos(Math.floorDiv(current.getX(), 32) * 32 + 16 + ox, STRUCTURE_SAMPLE_Y, Math.floorDiv(current.getZ(), 32) * 32 + 16 + oz);
                    if (level.getChunkSource().getChunkNow(sample.getX() >> 4, sample.getZ() >> 4) == null) continue;
                    var start = level.structureManager().getStructureAt(sample, structure);
                    if (!start.isValid()) continue;
                    for (var piece : start.getPieces()) {
                        if (!(piece instanceof PosadPiece posad)) continue;
                        BlockPos anchor = posad.cityAnchor();
                        if (!visited.add(anchor)) continue;
                        // The spawner checks each candidate's generated, loaded local area.
                        // Never demand all 64+ city chunks be simultaneously player-loaded.
                        PosadResidentSpawner.populateNatural(level, posad);
                    }
                }
            }
        }
    }

    private PosadNaturalPopulation() {}
}
