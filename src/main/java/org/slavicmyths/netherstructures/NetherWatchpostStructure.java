package org.slavicmyths.netherstructures;

import java.util.Optional;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.structure.*;
import org.slavicmyths.worldgen.StructureCandidates;

/**
 * One-chunk native Nether watchpost. 3D noise-column preflight, not an overworld heightmap
 * (which would select the Nether bedrock ceiling). No locate-time chunk loads or mutations.
 */
public final class NetherWatchpostStructure extends Structure {
    public static final MapCodec<NetherWatchpostStructure> CODEC = simpleCodec(NetherWatchpostStructure::new);
    public NetherWatchpostStructure(StructureSettings settings) { super(settings); }
    @Override public StructureType<?> type() { return NetherWatchpostWorldgen.STRUCTURE.get(); }

    /** Candidate materials are ordinary solid Nether terrain, never a constructed or tile block. */
    private static boolean natural(BlockState state) {
        return state.is(Blocks.NETHERRACK) || state.is(Blocks.BLACKSTONE) ||
                state.is(Blocks.BASALT) || state.is(Blocks.SOUL_SAND) ||
                state.is(Blocks.SOUL_SOIL) || state.is(Blocks.GRAVEL) ||
                state.is(Blocks.NETHER_GOLD_ORE) || state.is(Blocks.NETHER_QUARTZ_ORE) ||
                state.is(Blocks.MAGMA_BLOCK);
    }
    static boolean supported(NoiseColumn column, int floorY) {
        for (int y = floorY - 1; y >= floorY - 4; y--) {
            BlockState state = column.getBlock(y);
            if (!state.getFluidState().isEmpty() || state.is(Blocks.BEDROCK)) return false;
            if (natural(state)) return true;
            if (!state.isAir()) return false;
        }
        return false;
    }
    static boolean clearance(NoiseColumn[] columns, int floorY, int deck) {
        for (NoiseColumn column : columns) {
            if (!supported(column, floorY)) return false;
            for (int y = floorY; y <= floorY + deck + 3; y++)
                if (!column.getBlock(y).isAir()) return false;
        }
        return true;
    }

    @Override public Optional<GenerationStub> findGenerationPoint(GenerationContext c) {
        // Vanilla structure registration restricts eligible biomes. Reject Nether lava and
        // unsafe rooms in the actual noise columns before publishing an accepted start.
        ChunkPos chunk = c.chunkPos();
        int cx = chunk.getMiddleBlockX(), cz = chunk.getMiddleBlockZ();
        var biome = c.chunkGenerator().getBiomeSource().getNoiseBiome(cx >> 2, 16, cz >> 2, c.randomState().sampler());
        if (!biomes().contains(biome)) return Optional.empty();
        // Avoid known vanilla/modded candidate footprints without recursive neighbor planning.
        if (!StructureCandidates.clear(c, chunk, id -> {
            if (id == null || id.equals(NetherWatchpostWorldgen.id("nether_watchpost"))) return 0;
            return id.getNamespace().equals("minecraft") ? 4 : 3;
        })) return Optional.empty();

        int[] offsets = {-5, 0, 5};
        NoiseColumn[] columns = new NoiseColumn[9];
        int n = 0;
        for (int dx : offsets) for (int dz : offsets)
            columns[n++] = c.chunkGenerator().getBaseColumn(cx + dx, cz + dz, c.heightAccessor(), c.randomState());

        int min = Math.max(39, c.heightAccessor().getMinBuildHeight() + 6);
        int max = Math.min(108, c.heightAccessor().getMaxBuildHeight() - 23);
        for (int y = max; y >= min; y--) {
            // Structural floors: +5/+9/+13/+17. These are engineering baselines, OWNER_BALANCE_PENDING.
            for (int level = 4; level >= 1; level--) {
                int deck = 1 + level * 4;
                if (y + deck + 3 >= c.heightAccessor().getMaxBuildHeight() || !clearance(columns, y, deck)) continue;
                var center = new BlockPos(cx, y, cz);
                Rotation rotation = Rotation.getRandom(c.random());
                boolean topChest = c.random().nextBoolean();
                int damage = c.random().nextInt(3);
                long appearanceSeed = c.random().nextLong();
                var piece = new NetherWatchpostPiece(center, level, rotation, topChest, damage, appearanceSeed);
                return Optional.of(new GenerationStub(center, builder -> builder.addPiece(piece)));
            }
        }
        return Optional.empty();
    }
}
