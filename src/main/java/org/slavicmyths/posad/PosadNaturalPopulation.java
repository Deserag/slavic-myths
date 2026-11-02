
package org.slavicmyths.posad;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber(modid = "slavicmyths")
public final class PosadNaturalPopulation {
    private static int ticks;
    private static final int SEARCH_RADIUS = 128;
    private static final int SAMPLE_STEP = 32;
    private static final int[] STRUCTURE_SAMPLE_YS = {16, 64, 112, 160, 208};

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        if (++ticks % 100 != 0) {
            return;
        }

        for (ServerLevel level : event.getServer().getAllLevels()) {
            if (!level.dimension().equals(Level.NETHER)) {
                continue;
            }

            var structure = level.registryAccess()
                    .registryOrThrow(Registries.STRUCTURE)
                    .get(ResourceLocation.fromNamespaceAndPath("slavicmyths", "nether_posad"));

            if (structure == null) {
                continue;
            }

            StructurePieceSerializationContext serializationContext = StructurePieceSerializationContext.fromLevel(level);
            Set<Long> visited = new HashSet<>();

            for (var player : level.players()) {
                BlockPos playerPosition = player.blockPosition();
                int baseX = Math.floorDiv(playerPosition.getX(), SAMPLE_STEP) * SAMPLE_STEP + SAMPLE_STEP / 2;
                int baseZ = Math.floorDiv(playerPosition.getZ(), SAMPLE_STEP) * SAMPLE_STEP + SAMPLE_STEP / 2;

                for (int offsetX = -SEARCH_RADIUS; offsetX <= SEARCH_RADIUS; offsetX += SAMPLE_STEP) {
                    for (int offsetZ = -SEARCH_RADIUS; offsetZ <= SEARCH_RADIUS; offsetZ += SAMPLE_STEP) {
                        int sampleX = baseX + offsetX;
                        int sampleZ = baseZ + offsetZ;

                        if (level.getChunkSource().getChunkNow(sampleX >> 4, sampleZ >> 4) == null) {
                            continue;
                        }

                        for (int sampleY : STRUCTURE_SAMPLE_YS) {
                            BlockPos samplePosition = new BlockPos(sampleX, sampleY, sampleZ);
                            var structureStart = level.structureManager().getStructureAt(samplePosition, structure);

                            if (!structureStart.isValid()) {
                                continue;
                            }

                            for (var piece : structureStart.getPieces()) {
                                if (!(piece instanceof PosadPiece posad)) {
                                    continue;
                                }

                                CompoundTag pieceData = posad.createTag(serializationContext);

                                if (!pieceData.contains("Anchor", 4) || !pieceData.contains("Turns", 3)) {
                                    continue;
                                }

                                BlockPos anchor = BlockPos.of(pieceData.getLong("Anchor"));
                                int turns = Math.floorMod(pieceData.getInt("Turns"), 4);

                                if (!visited.add(anchor.asLong())) {
                                    continue;
                                }

                                if (!posad.getBoundingBox().isInside(anchor)) {
                                    continue;
                                }

                                if (!isCityLoaded(level, anchor)) {
                                    continue;
                                }

                                PosadResidentSpawner.populateNatural(
                                        level,
                                        anchor.getX(),
                                        anchor.getY(),
                                        anchor.getZ(),
                                        turns
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    private static boolean isCityLoaded(ServerLevel level, BlockPos anchor) {
        int minChunkX = anchor.getX() >> 4;
        int maxChunkX = (anchor.getX() + 112) >> 4;
        int minChunkZ = anchor.getZ() >> 4;
        int maxChunkZ = (anchor.getZ() + 112) >> 4;

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) {
                    return false;
                }
            }
        }

        return true;
    }

    private PosadNaturalPopulation() {
    }
}
