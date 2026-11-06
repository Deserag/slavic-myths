package org.slavicmyths.posad;

import java.util.HashSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "slavicmyths")
public final class PosadNaturalPopulation {
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        var server = event.getServer();
        if (server.getTickCount() % 100 != 0) return;
        var world = server.getLevel(Level.NETHER);
        if (world == null || world.players().isEmpty()) return;

        var structure = world.registryAccess().registryOrThrow(Registries.STRUCTURE)
            .get(ResourceLocation.fromNamespaceAndPath("slavicmyths", "nether_posad"));
        if (structure == null) return;

        var visited = new HashSet<Long>();
        var chunks = world.getChunkSource();
        for (var player : world.players()) {
            var pos = player.chunkPosition();
            var chunk = chunks.getChunkNow(pos.x, pos.z);
            if (chunk == null) continue;
            for (long reference : chunk.getReferencesForStructure(structure)) {
                if (!visited.add(reference)) continue;
                var owner = new ChunkPos(reference);
                var startChunk = chunks.getChunkNow(owner.x, owner.z);
                if (startChunk == null) continue;
                var start = startChunk.getStartForStructure(structure);
                if (start == null || !start.isValid()) continue;
                for (var piece : start.getPieces()) {
                    if (!(piece instanceof PosadPiece posad)) continue;
                    var anchor = posad.anchor();
                    PosadResidentSpawner.populateNatural(world,
                        anchor.getX(), anchor.getY(), anchor.getZ(), posad.turns());
                }
            }
        }
    }

    private PosadNaturalPopulation() {}
}
