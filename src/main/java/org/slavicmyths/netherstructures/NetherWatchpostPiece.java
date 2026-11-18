package org.slavicmyths.netherstructures;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

/**
 * Chunk-local authored stone ruin (11x11). No new registry IDs, entities or custom containers.
 * Centered in the start chunk (local X/Z 3..13); all writes stay inside that ONE chunk.
 * Interior ladder is on the SOUTH face of a continuous inner stone support at z=-1.
 */
public final class NetherWatchpostPiece extends StructurePiece {
    private final BlockPos center;
    private final int sections, degradation;
    private final Rotation rotation;
    private final boolean topChest;
    private final long designSeed;
    public NetherWatchpostPiece(BlockPos center, int sections, Rotation rotation,
                               boolean topChest, int degradation, long seed) {
        super(NetherWatchpostWorldgen.PIECE.get(), 0,
                new BoundingBox(center.getX()-5, center.getY()-4, center.getZ()-5,
                        center.getX()+5, center.getY()+sections*4+5, center.getZ()+5));
        this.center = center;
        this.sections = sections;
        this.rotation = rotation;
        this.topChest = topChest;
        this.degradation = degradation;
        this.designSeed = seed;
    }
    public NetherWatchpostPiece(CompoundTag tag) {
        super(NetherWatchpostWorldgen.PIECE.get(), tag);
        center = BlockPos.of(tag.getLong("Center"));
        sections = tag.getInt("Sections");
        rotation = Rotation.valueOf(tag.getString("Rotation"));
        topChest = tag.getBoolean("TopChest");
        degradation = tag.getInt("Degradation");
        designSeed = tag.getLong("DesignSeed");
    }
    @Override protected void addAdditionalSaveData(StructurePieceSerializationContext ctx, CompoundTag tag) {
        tag.putLong("Center", center.asLong());
        tag.putInt("Sections", sections);
        tag.putString("Rotation", rotation.name());
        tag.putBoolean("TopChest", topChest);
        tag.putInt("Degradation", degradation);
        tag.putLong("DesignSeed", designSeed);
    }
    public int deck() { return 1 + sections * 4; }

    private boolean broken(int x, int y, int z) {
        long h = designSeed + x*73856093L + z*19349663L + y*83492791L;
        h = (h ^ (h >>> 30)) * -4658895280553007687L;
        return Math.floorMod(h ^ (h >>> 27), 10) < degradation * 2;
    }
    /** Null = untouched world; AIR = explicit open aperture, never a missing structure part. */
    public BlockState state(int x, int y, int z) {
        int ax = Math.abs(x), az = Math.abs(z), deck = deck();
        if (y < 0) {
            if (y == -1 && ax <= 4 && az <= 4) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            // Four legs and a central bearing column can be extended down to local natural rock.
            if (y >= -4 && ((ax == 4 && az == 4) || (x == 0 && z == -1)))
                return Blocks.BASALT.defaultBlockState();
            return null;
        }
        if (y == 0 && ax <= 4 && az <= 4) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        if (y == deck && ax <= 5 && az <= 5) {
            if (x == 0 && z == 0) return Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH); // vertical access through the deck
            return ax == 5 || az == 5 ? Blocks.POLISHED_BLACKSTONE_BRICK_SLAB.defaultBlockState()
                    : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        }
        if (y >= 1 && y <= deck + 1) {
            if (x == 0 && z == -1) return Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            if (x == 0 && z == 0) return Blocks.LADDER.defaultBlockState()
                    .setValue(LadderBlock.FACING, Direction.SOUTH);
        }
        if (y > 0 && y < deck && ax == 4 && az == 4)
            return Blocks.POLISHED_BASALT.defaultBlockState();
        if (y > 0 && y < deck && (ax == 4 || az == 4)) {
            if (z == 4 && ax <= 1 && y <= 3) return Blocks.AIR.defaultBlockState(); // ground entrance
            if (y%4 == 2 && (x == -2 || x == 2 || z == -2 || z == 2))
                return Blocks.IRON_BARS.defaultBlockState();
            if (broken(x,y,z) && y%4 != 1) return Blocks.AIR.defaultBlockState();
            return broken(x,y+1,z) ? Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS.defaultBlockState()
                    : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
        }
        if (y == deck+1 && (ax == 5 || az == 5)) {
            if (x == 0 && z == 5) return Blocks.AIR.defaultBlockState();
            return broken(x,y,z) ? null : Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState();
        }
        if (y == deck+1 && x == -3 && z == -3) return Blocks.CAMPFIRE.defaultBlockState();
        if (y == deck+1 && x == -3 && z == 2) return Blocks.LANTERN.defaultBlockState();
        int chestY = topChest ? deck+1 : 1;
        if (y == chestY && x == 2 && z == 2)
            return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH);
        if (y == chestY + 1 && x == 2 && z == 2) return Blocks.AIR.defaultBlockState();
        if (y > 0 && y < deck && ax < 4 && az < 4) return Blocks.AIR.defaultBlockState();
        return null;
    }

    @Override public void postProcess(WorldGenLevel world, StructureManager manager,
            ChunkGenerator generator, RandomSource random, BoundingBox clip, ChunkPos chunk, BlockPos pivot) {
        int deck = deck();
        for (int y = -4; y <= deck+4; y++) for (int x = -5; x <= 5; x++) for (int z = -5; z <= 5; z++) {
            BlockState state = state(x,y,z);
            if (state == null) continue;
            BlockPos pos = center.offset(new BlockPos(x,y,z).rotate(rotation));
            if (!clip.isInside(pos) || !world.ensureCanWrite(pos)) continue;
            if (world.getBlockState(pos).is(Blocks.BEDROCK) || world.getBlockEntity(pos) != null) continue;
            world.setBlock(pos, state.rotate(rotation), 2);
        }
        // Only one chest at one deterministic coordinate, with native lazy loot and a fixed seed.
        int lootY = topChest ? deck+1 : 1;
        BlockPos chestPos = center.offset(new BlockPos(2,lootY,2).rotate(rotation));
        if (clip.isInside(chestPos) && world.getBlockEntity(chestPos) instanceof RandomizableContainerBlockEntity chest
                && chest.getLootTable() == null && chest.isEmpty()) {
            chest.setLootTable(ResourceKey.create(Registries.LOOT_TABLE,
                    NetherWatchpostWorldgen.id("chests/nether_watchpost")), designSeed ^ chestPos.asLong());
        }
    }
}
