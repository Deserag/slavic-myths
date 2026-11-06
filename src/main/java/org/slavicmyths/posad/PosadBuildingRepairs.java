package org.slavicmyths.posad;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;

/** Repairs to the authored communal house and council hall for newly planned cities. */
final class PosadBuildingRepairs {
    private record ChestMove(int fromX, int y, int fromZ, int toX, int toZ, Direction facing) {}
    private static final ChestMove[] CHESTS = {
        new ChestMove(16, 1, 18, 15, 18, Direction.EAST),
        new ChestMove(32, 1, 18, 33, 18, Direction.WEST),
        new ChestMove(32, 7, 25, 33, 25, Direction.WEST)
    };
    private static final Map<Long, BlockState> INTERIOR = buildInterior();

    private static Map<Long, BlockState> buildInterior() {
        Map<Long, BlockState> cells = new HashMap<>();
        // Clear the old table and chair locations. Place the new pairs in rows
        // directly alongside the exterior side walls, clear of the stairwell.
        int[][] oldPairs = {
            {19,1,18,20}, {29,1,18,30}, {17,1,23,16}, {30,1,23,29},
            {17,7,19,18}, {29,7,18,30}, {17,7,23,18}, {30,7,23,29}
        };
        for (int[] pair : oldPairs) {
            for (int x : new int[]{pair[0], pair[3]}) {
                var old = PosadHouseRenovation.voxel(x, pair[1], pair[2]);
                if (old != null && old.getBlock() instanceof org.slavicmyths.furniture.FurnitureBlock) {
                    var base = PosadBuildings.voxel(x, pair[1], pair[2]);
                    cells.put(BlockPos.asLong(x, pair[1], pair[2]), base == null ? Blocks.AIR.defaultBlockState() : base);
                }
            }
        }
        // The R1 house contains full planks and slabs used as improvised
        // furniture in otherwise open room cells. Keep the enclosing walls.
        for (int y : new int[]{1, 7}) {
            for (int z = 17; z <= 27; z++) {
                for (int x = 16; x <= 32; x++) {
                    if (x > 21 && x < 27 || z == 22) continue;
                    var base = PosadBuildings.voxel(x,y,z);
                    if (base != null && (base.is(Blocks.CRIMSON_PLANKS) || base.is(Blocks.CRIMSON_SLAB))) {
                        cells.put(BlockPos.asLong(x,y,z), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        for (int y : new int[]{1, 7}) {
            for (int x : new int[]{15, 33}) {
                var facing = x == 15 ? Direction.EAST : Direction.WEST;
                for (int z : new int[]{19, 27}) {
                    cells.put(BlockPos.asLong(x,y,z), Blocks.CRIMSON_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP));
                    cells.put(BlockPos.asLong(x,y,z+1), Blocks.CRIMSON_STAIRS.defaultBlockState().setValue(StairBlock.FACING, facing));
                }
            }
        }
        for (var move : CHESTS) {
            cells.put(BlockPos.asLong(move.fromX(),move.y(),move.fromZ()), Blocks.AIR.defaultBlockState());
            cells.put(BlockPos.asLong(move.toX(),move.y(),move.toZ()), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, move.facing()));
        }
        return Map.copyOf(cells);
    }

    private static final class WallFurniture {
        static final Map<Long, BlockState> LEGACY = build(false);
        static final Map<Long, BlockState> NETHER_WOOD = build(true);

        private static BlockState authored(int x, int y, int z) {
            var state = PosadArchitectureR3.voxel(x,y,z);
            return state == null ? PosadArchitectureR3.old(x,y,z) : state;
        }

        private static boolean empty(BlockState state) {
            return state == null || state.isAir();
        }

        private static boolean wall(BlockState state) {
            return state != null && (state.is(Blocks.NETHER_BRICKS)
                || state.is(Blocks.POLISHED_BLACKSTONE_BRICKS) || state.is(Blocks.POLISHED_BASALT)
                || state.is(Blocks.CRIMSON_PLANKS));
        }

        private static Map<Long, BlockState> build(boolean netherWood) {
            Map<Long, BlockState> cells = new HashMap<>();
            for (var slot : PosadGeometry.Slot.values()) {
                if (slot == PosadGeometry.Slot.COMMUNAL_HOUSE || slot == PosadGeometry.Slot.VECHE_SQUARE) continue;
                var area = slot.local();
                for (int y : new int[]{1, 7}) {
                    for (int z = area.minZ(); z <= area.maxZ(); z++) {
                        for (int x = area.minX(); x <= area.maxX(); x++) {
                            var original = authored(x,y,z);
                            if (original == null || !(original.getBlock() instanceof org.slavicmyths.furniture.FurnitureBlock furniture)) continue;
                            if (!java.util.Set.of("table", "chair", "bench", "stool").contains(furniture.kind)) continue;
                            String wood = slot == PosadGeometry.Slot.ZELEYNAYA_IZBA ? "warped" : "crimson";
                            move(cells,area,x,y,z,furniture.kind,netherWood ? wood : null);
                        }
                    }
                }
            }
            return Map.copyOf(cells);
        }

        private static void move(Map<Long,BlockState> cells, PosadGeometry.Rect area, int x, int y, int z, String kind, String wood) {
            for (int distance = 0; distance <= 4; distance++) {
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    int tx = x + direction.getStepX()*distance;
                    int tz = z + direction.getStepZ()*distance;
                    int wx = tx + direction.getStepX(), wz = tz + direction.getStepZ();
                    if (!area.contains(tx,tz) || !area.contains(wx,wz)) continue;
                    long destination = BlockPos.asLong(tx,y,tz);
                    if (cells.containsKey(destination) || (distance != 0 && !empty(authored(tx,y,tz)))) continue;
                    if (!empty(authored(tx,y+1,tz)) || !wall(authored(wx,y,wz)) || !wall(authored(wx,y+1,wz))) continue;
                    var floor = authored(tx,y-1,tz);
                    if (empty(floor) || floor.getBlock() instanceof StairBlock) continue;
                    if (distance != 0) cells.put(BlockPos.asLong(x,y,z), Blocks.AIR.defaultBlockState());
                    var replacement = wood != null ? furniture(wood,kind,direction.getOpposite()) : switch (kind) {
                        case "chair", "bench" -> Blocks.CRIMSON_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING,direction.getOpposite());
                        case "stool" -> Blocks.CRIMSON_SLAB.defaultBlockState();
                        default -> Blocks.CRIMSON_SLAB.defaultBlockState().setValue(SlabBlock.TYPE,SlabType.TOP);
                    };
                    cells.put(destination,replacement);
                    return;
                }
            }
        }
    }

    private static BlockState furniture(String wood, String kind, Direction facing) {
        return org.slavicmyths.furniture.Furniture.get(wood + "_" + kind).defaultBlockState()
            .setValue(org.slavicmyths.furniture.FurnitureBlock.FACING, facing);
    }

    static BlockState voxel(int x, int y, int z, boolean netherWood) {
        if (netherWood && (y == 1 || y == 7) && (x == 15 || x == 33) && (z == 19 || z == 20 || z == 27 || z == 28)) {
            return furniture("crimson", z == 19 || z == 27 ? "table" : "chair",
                x == 15 ? Direction.EAST : Direction.WEST);
        }
        var interior = INTERIOR.get(BlockPos.asLong(x,y,z));
        if (interior != null) return interior;

        // The end gables meet the underside of both roof slopes. Fill only
        // the end faces, leaving the central attic and its ventilation open.
        if (x == 13 || x == 35) {
            if ((y == 12 && z >= 16 && z <= 28)
                || (y == 13 && z >= 17 && z <= 27)
                || (y == 14 && z >= 19 && z <= 25)
                || (y == 15 && z >= 21 && z <= 23)) {
                return Blocks.NETHER_BRICKS.defaultBlockState();
            }
        }

        if (z == 34 && x >= 43 && x <= 69) {
            // The stepped face rises from a full-width cornice. The black
            // corner piers continue down to the original stone platform.
            int offset = Math.abs(x - 56);
            if ((offset == 12 && y >= 1 && y <= 10)
                || (offset >= 10 && offset <= 13 && y == 11)) {
                return (y == 1 || y == 10 || y == 11
                    ? Blocks.CHISELED_POLISHED_BLACKSTONE : Blocks.POLISHED_BASALT).defaultBlockState();
            }
            if (y == 12 && offset >= 6) {
                return Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH);
            }
            if (y >= 13 && y <= 17 && offset <= 17 - y) {
                if (offset == 0 && y >= 15) return Blocks.CRYING_OBSIDIAN.defaultBlockState();
                if (offset == 17 - y) return Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
                return Blocks.NETHER_BRICKS.defaultBlockState();
            }
            // Keep the existing door and the clear two-block-high approach.
            if ((offset == 4 && y >= 1 && y <= 6)
                || (offset == 3 && y == 7) || (offset == 2 && y == 8)
                || (offset <= 1 && y == 9)) {
                return y == 9 && offset == 0 ? Blocks.CRYING_OBSIDIAN.defaultBlockState()
                    : Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
            }
        }
        return y == 1 || y == 7 ? (netherWood ? WallFurniture.NETHER_WOOD : WallFurniture.LEGACY).get(BlockPos.asLong(x,y,z)) : null;
    }

    static String chestLoot(int x, int y, int z) {
        for (var move : CHESTS) {
            if (x == move.toX() && y == move.y() && z == move.toZ()) {
                return PosadBuildings.chestLoot(move.fromX(), y, move.fromZ());
            }
        }
        return null;
    }

    /** Only called inside fresh, fully preflighted standalone placement, never on an existing city. */
    static void moveFreshHouseChests(net.minecraft.server.level.ServerLevel world, BlockPos origin) {
        for (var move : CHESTS) {
            var from=origin.offset(move.fromX()-13,move.y(),move.fromZ()-15);
            var to=origin.offset(move.toX()-13,move.y(),move.toZ()-15);
            if (!(world.getBlockEntity(from) instanceof net.minecraft.world.level.block.entity.ChestBlockEntity source))
                throw new IllegalStateException("Missing authored house chest at "+from);
            if (world.getBlockEntity(to)!=null) throw new IllegalStateException("Occupied house chest destination "+to);
            var saved=source.saveWithoutMetadata(world.registryAccess());
            // Detach the source first so ChestBlock.onRemove cannot scatter its inventory.
            world.removeBlockEntity(from);
            world.setBlock(from,Blocks.AIR.defaultBlockState(),2);
            world.setBlock(to,Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING,move.facing()),2);
            var target=world.getBlockEntity(to);
            if (!(target instanceof net.minecraft.world.level.block.entity.ChestBlockEntity))
                throw new IllegalStateException("House chest destination not created "+to);
            target.loadWithComponents(saved,world.registryAccess());
            target.setChanged();
        }
    }

    private PosadBuildingRepairs() {}
}
