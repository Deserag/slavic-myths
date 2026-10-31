package org.slavicmyths.posad;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** Deferred settlement spawn after native structure chunks have been fully generated and loaded. */
@EventBusSubscriber(modid = "slavicmyths")
public final class PosadNaturalPopulation {
    private static int ticks;

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        if (++ticks % 100 != 0) return;
        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().equals(net.minecraft.world.level.Level.NETHER)) continue;
            var structure = level.registryAccess().registryOrThrow(Registries.STRUCTURE).get(ResourceLocation.fromNamespaceAndPath("slavicmyths", "nether_posad"));
            if (structure == null) continue;
            for (var player : level.players()) {
                var start = level.structureManager().getStructureAt(player.blockPosition(), structure);
                if (!start.isValid()) continue;
                for (var piece : start.getPieces()) {
                    if (!(piece instanceof PosadPiece posad)) continue;
                    var anchor = posad.cityAnchor();
                    if (level.getChunkSource().getChunkNow(anchor.getX() >> 4, anchor.getZ() >> 4) == null) continue;
                    boolean loaded = true;
                    for (int cx = anchor.getX() >> 4; cx <= (anchor.getX() + 112) >> 4 && loaded; cx++) {
                        for (int cz = anchor.getZ() >> 4; cz <= (anchor.getZ() + 112) >> 4; cz++) {
                            if (level.getChunkSource().getChunkNow(cx, cz) == null) { loaded = false; break; }
                        }
                    }
                    if (loaded) PosadResidentSpawner.populateNatural(level, anchor.getX(), anchor.getY(), anchor.getZ(), posad.cityTurns());
                }
            }
        }
    }

    private PosadNaturalPopulation() {}
}
