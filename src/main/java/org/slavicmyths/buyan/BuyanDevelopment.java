package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.saveddata.SavedData;

/**
 * Phase two: populate the fixed, centered city. Never regenerate the landscape
 * or relocate its 45x45 plaza and paved axes. Safe for an already built island.
 */
public final class BuyanDevelopment {
    private static final String SAVE = "slavicmyths_buyan_development_2622";
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState STONE = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState BEAM = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
    private static final BlockState WHITE = Blocks.CALCITE.defaultBlockState();

    private BuyanDevelopment() {}

    private static long key(BuyanState.Site site) {
        return new BlockPos(site.x(), 0, site.z()).asLong();
    }

    private static final class Record extends SavedData {
        final Set<Long> oaks = new HashSet<>();
        final Set<Long> houses = new HashSet<>();

        static Record get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new, Record::load), SAVE);
        }

        static Record load(CompoundTag tag, HolderLookup.Provider registries) {
            Record r = new Record();
            for (long id : tag.getLongArray("Oaks")) r.oaks.add(id);
            for (long id : tag.getLongArray("Houses")) r.houses.add(id);
            return r;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            tag.putLongArray("Oaks", oaks.stream().mapToLong(Long::longValue).toArray());
            tag.putLongArray("Houses", houses.stream().mapToLong(Long::longValue).toArray());
            return tag;
        }

        void markOak(long site) { oaks.add(site); setDirty(); }
        void markHouses(long site) { houses.add(site); setDirty(); }
    }

    private static final class Plan {
        final ServerLevel world;
        final BuyanState.Site site;
        final int floor;
        final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();

        Plan(ServerLevel world, BuyanState.Site site, int floor) {
            this.world = world;
            this.site = site;
            this.floor = floor;
        }

        void at(int x, int y, int z, BlockState block) {
            blocks.put(new BlockPos(site.x() + x, y, site.z() + z), block);
        }

        void fill(int x0, int x1, int z0, int z1, int y0, int y1, BlockState block) {
            for (int x = x0; x <= x1; x++)
                for (int z = z0; z <= z1; z++)
                    for (int y = y0; y <= y1; y++) at(x, y, z, block);
        }

        void door(int x, int z, Direction facing) {
            BlockState lower = Blocks.SPRUCE_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            at(x, floor + 1, z, lower);
            at(x, floor + 2, z,
                lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }

        void house(int x, int z, int width, int depth, String kind, int variant,
                   Direction entrance) {
            int right = x + width - 1, back = z + depth - 1;
            boolean market = kind.equals("shop") || kind.equals("inn");
            boolean workshop = kind.equals("workshop");
            BlockState wall = market ? WHITE : (variant % 3 == 0
                ? Blocks.SPRUCE_PLANKS.defaultBlockState()
                : Blocks.OAK_PLANKS.defaultBlockState());
            BlockState roof = variant % 3 == 0 ? Blocks.DEEPSLATE_TILES.defaultBlockState()
                : variant % 3 == 1 ? Blocks.DARK_OAK_PLANKS.defaultBlockState()
                    : Blocks.SPRUCE_PLANKS.defaultBlockState();
            BlockState foundation = workshop ? Blocks.COBBLESTONE.defaultBlockState() : STONE;
            fill(x, right, z, back, floor, floor, foundation);
            for (int xx = x; xx <= right; xx++)
                for (int zz = z; zz <= back; zz++)
                    for (int h = 1; h <= 4; h++) {
                        boolean edge = xx == x || xx == right || zz == z || zz == back;
                        at(xx, floor + h, zz,
                            edge ? ((xx == x || xx == right) && (zz == z || zz == back)
                                ? BEAM : wall) : AIR);
                    }
            int doorway = x + width / 2;
            int front = entrance == Direction.NORTH ? z : back;
            door(doorway, front, entrance);

            BlockState window = Blocks.GLASS_PANE.defaultBlockState();
            for (int xx : new int[]{x + 3, right - 3})
                for (int side : new int[]{z, back})
                    if (!(xx == doorway && side == front)) {
                        at(xx, floor + 2, side, window);
                        at(xx, floor + 3, side, window);
                    }
            for (int zz : new int[]{z + 3, back - 3})
                for (int side : new int[]{x, right}) {
                    at(side, floor + 2, zz, window);
                    at(side, floor + 3, zz, window);
                }
            // Stepped gable roof, with filled gables; each house has a
            // real enclosed interior instead of an unroofed facade.
            for (int xx = x - 1; xx <= right + 1; xx++) {
                int rise = Math.min(xx - (x - 1), right + 1 - xx);
                int roofY = floor + 5 + rise;
                for (int zz = z - 1; zz <= back + 1; zz++)
                    at(xx, roofY, zz, roof);
                if (xx >= x && xx <= right)
                    for (int yy = floor + 5; yy < roofY; yy++) {
                        at(xx, floor + 5, z, BEAM);
                        at(xx, yy, z, wall);
                        at(xx, floor + 5, back, BEAM);
                        at(xx, yy, back, wall);
                    }
            }
            for (int yy = floor + 1; yy <= floor + 4; yy++)
                for (int xx : new int[]{x, right})
                    for (int zz : new int[]{z, back})
                        at(xx, yy, zz, BEAM);

            // Distinct interiors for the built-up residential/trading halves.
            at(x + 2, floor + 1, z + 2, Blocks.CRAFTING_TABLE.defaultBlockState());
            at(right - 2, floor + 1, back - 2, Blocks.BARREL.defaultBlockState());
            if (workshop) {
                at(x + 3, floor + 1, back - 3, Blocks.FURNACE.defaultBlockState());
                at(x + 4, floor + 1, back - 3, Blocks.SMITHING_TABLE.defaultBlockState());
            } else if (kind.equals("shop")) {
                at(x + 3, floor + 1, back - 3, Blocks.COMPOSTER.defaultBlockState());
                at(right - 3, floor + 1, back - 3, Blocks.CHEST.defaultBlockState());
                for (int dx = -2; dx <= 2; dx++)
                    at(doorway + dx, floor + 4, front,
                        variant % 2 == 0 ? Blocks.RED_WOOL.defaultBlockState()
                            : Blocks.BLUE_WOOL.defaultBlockState());
                // Keep doorway open below the fabric awning.
            } else if (kind.equals("inn")) {
                at(right - 3, floor + 1, z + 3, Blocks.BOOKSHELF.defaultBlockState());
                at(x + 3, floor + 1, back - 3, Blocks.CHEST.defaultBlockState());
            } else {
                at(right - 3, floor + 1, z + 3, Blocks.BOOKSHELF.defaultBlockState());
                at(x + 3, floor + 1, back - 3, Blocks.FLOWER_POT.defaultBlockState());
            }
            at(x + 2, floor + 3, z + 2, Blocks.LANTERN.defaultBlockState());
            int start = entrance == Direction.NORTH ? front - 1 : front + 1;
            // Stop outside the existing five-block cross street instead
            // of replacing its polished-diorite shoulder.
            int end = entrance == Direction.NORTH
                ? (z == 50 ? z - 2 : z - 1) : back + 3;
            for (int zz = Math.min(start, end); zz <= Math.max(start, end); zz++)
                for (int dx = -1; dx <= 1; dx++)
                    at(doorway + dx, floor, zz,
                        dx == 0 ? Blocks.POLISHED_ANDESITE.defaultBlockState() : STONE);
        }

        void town() {
            // Four building cells in each row of each southern quarter.
            // All footprints fit between the 5-wide cross streets (Z=45,75)
            // and 5-wide north-south feeders (X=-65,+65).
            int[] west = {-101, -82, -58, -39};
            int[] east = {28, 47, 71, 90};
            int[] rows = {27, 50, 79};
            int[] depths = {13, 19, 14};
            for (int row = 0; row < rows.length; row++)
                for (int col = 0; col < 4; col++) {
                    Direction direction = row == 0 ? Direction.SOUTH : Direction.NORTH;
                    house(west[col], rows[row], 12, depths[row],
                        "house", row * 4 + col, direction);
                    String kind = row == 0 ? "shop" : row == 1
                        ? (col == 1 ? "inn" : "shop")
                        : col % 2 == 0 ? "workshop" : "house";
                    house(east[col], rows[row], 12, depths[row],
                        kind, row * 4 + col + 1, direction);
                }
        }

        void oak() {
            // Sacred oak has a compact natural root base in one reserved cell;
            // its wide canopy rises ABOVE the street level without replacing
            // any existing street or creating a 53x53 stone podium.
            final int cx = 85, cz = -70;
            for (int dx = -10; dx <= 10; dx++)
                for (int dz = -10; dz <= 10; dz++) {
                    double radius = Math.hypot(dx, dz);
                    if (radius >= 8.5 && radius <= 10.4)
                        at(cx + dx, floor, cz + dz,
                            Blocks.MOSSY_STONE_BRICKS.defaultBlockState());
                }
            for (int y = 1; y <= 25; y++) {
                int r = y <= 12 ? 3 : y <= 20 ? 2 : 1;
                for (int dx = -r; dx <= r; dx++)
                    for (int dz = -r; dz <= r; dz++)
                        if (dx * dx + dz * dz <= r * r + 2)
                            at(cx + dx, floor + y, cz + dz,
                                (dx == -r || dz == -r)
                                    ? Blocks.DARK_OAK_WOOD.defaultBlockState()
                                    : Blocks.OAK_WOOD.defaultBlockState());
            }
            for (int i = 0; i < 12; i++) {
                double angle = 2.0 * Math.PI * i / 12;
                for (int j = 3; j <= 10; j++) {
                    int dx = (int) Math.round(j * Math.cos(angle));
                    int dz = (int) Math.round(j * Math.sin(angle));
                    at(cx + dx, floor + Math.max(1, 7 - j / 2), cz + dz,
                        Blocks.OAK_WOOD.defaultBlockState());
                }
            }
            for (int i = 0; i < 10; i++) {
                double angle = 2.0 * Math.PI * i / 10;
                for (int j = 2; j <= 18; j++) {
                    int dx = (int) Math.round(j * Math.cos(angle));
                    int dz = (int) Math.round(j * Math.sin(angle));
                    int y = 19 + j / 3;
                    for (int h = -1; h <= 1; h++)
                        at(cx + dx, floor + y + h, cz + dz,
                            Blocks.OAK_WOOD.defaultBlockState());
                }
            }
            for (int[] lobe : new int[][]{
                {0, 0, 27, 12, 12, 9}, {-9, 0, 28, 11, 10, 8},
                {9, 0, 27, 11, 10, 8}, {0, -9, 27, 10, 11, 7},
                {0, 9, 29, 10, 11, 8}, {-9, -9, 28, 9, 9, 7},
                {9, 9, 28, 9, 9, 7}
            }) {
                for (int dx = -lobe[3]; dx <= lobe[3]; dx++)
                    for (int dz = -lobe[4]; dz <= lobe[4]; dz++)
                        for (int dy = -lobe[5]; dy <= lobe[5]; dy++) {
                            double volume = (double) (dx * dx) / (lobe[3] * lobe[3])
                                + (double) (dz * dz) / (lobe[4] * lobe[4])
                                + (double) (dy * dy) / (lobe[5] * lobe[5]);
                            if (volume > 1.0) continue;
                            int xx = cx + lobe[0] + dx, zz = cz + lobe[1] + dz;
                            int yy = floor + lobe[2] + dy;
                            BlockPos position = new BlockPos(site.x() + xx, yy, site.z() + zz);
                            if (blocks.containsKey(position)) continue;
                            double variation = BuyanTerrain.value(world.getSeed()
                                ^ 0xB22A5ACEL, position.getX(),
                                position.getZ(), position.getY());
                            if (variation < 0.085) continue;
                            at(xx, yy, zz, variation < 0.35
                                ? Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState()
                                : Blocks.OAK_LEAVES.defaultBlockState());
                        }
            }
            for (int[] post : new int[][]{{0,-10},{10,0},{0,10},{-10,0}}) {
                at(cx + post[0], floor + 1, cz + post[1], STONE);
                at(cx + post[0], floor + 2, cz + post[1], Blocks.LANTERN.defaultBlockState());
            }
        }

        boolean safe() {
            Set<Long> chunks = new HashSet<>();
            for (Map.Entry<BlockPos, BlockState> entry : blocks.entrySet()) {
                BlockPos p = entry.getKey();
                if (p.getY() <= world.getMinBuildHeight()
                    || p.getY() >= world.getMaxBuildHeight() - 1
                    || !world.getWorldBorder().isWithinBounds(p)) return false;
                long chunk = ChunkPos.asLong(p.getX() >> 4, p.getZ() >> 4);
                if (chunks.add(chunk)) world.getChunk(p.getX() >> 4, p.getZ() >> 4);
                BlockState old = world.getBlockState(p);
                if (!old.equals(entry.getValue()) && !BuyanGenerator.canReplace(old)) {
                    LogUtils.getLogger().warn("Buyan development protected block {}: {}", p, old);
                    return false;
                }
            }
            return true;
        }

        void commit() {
            Map<BlockPos, BlockState> originals = new LinkedHashMap<>();
            try {
                for (var entry : blocks.entrySet()) {
                    BlockPos p = entry.getKey();
                    BlockState old = world.getBlockState(p);
                    if (old.equals(entry.getValue())) continue;
                    originals.put(p, old);
                    if (!world.setBlock(p, entry.getValue(), 2)
                        && !world.getBlockState(p).equals(entry.getValue()))
                        throw new IllegalStateException("Cannot place city block " + p);
                }
            } catch (RuntimeException exception) {
                List<Map.Entry<BlockPos, BlockState>> undo =
                    new ArrayList<>(originals.entrySet());
                for (int i = undo.size() - 1; i >= 0; i--)
                    world.setBlock(undo.get(i).getKey(), undo.get(i).getValue(), 2);
                throw exception;
            }
        }
    }

    static boolean placeHouses(ServerLevel world, BuyanState.Site site) {
        Record record = Record.get(world);
        long id = key(site);
        if (record.houses.contains(id)) return true;
        BuyanCityPlan.Layout layout = BuyanCityPlan.savedLayout(world, site);
        if (layout == null || layout.centerX() != site.x() || layout.centerZ() != site.z())
            return false;
        Plan p = new Plan(world, site, layout.centerY());
        p.town();
        if (!p.safe()) return false;
        p.commit();
        record.markHouses(id);
        LogUtils.getLogger().info("Buyan: 24 new town buildings placed");
        return true;
    }

    static boolean placeOak(ServerLevel world, BuyanState.Site site) {
        Record record = Record.get(world);
        long id = key(site);
        if (record.oaks.contains(id)) return true;
        BuyanCityPlan.Layout layout = BuyanCityPlan.savedLayout(world, site);
        if (layout == null || layout.centerX() != site.x() || layout.centerZ() != site.z())
            return false;
        Plan p = new Plan(world, site, layout.centerY());
        p.oak();
        if (!p.safe()) return false;
        p.commit();
        record.markOak(id);
        LogUtils.getLogger().info("Buyan: sacred oak planted at {},{}",
            site.x() + 85, site.z() - 70);
        return true;
    }

    private static boolean attempt(String name, BooleanSupplier action) {
        try {
            boolean success = action.getAsBoolean();
            if (!success)
                LogUtils.getLogger().warn("Buyan development: {} not placed safely", name);
            return success;
        } catch (RuntimeException failure) {
            LogUtils.getLogger().error("Buyan development: {} failed safely", name, failure);
            return false;
        }
    }

    /** Each area is independent and saves progress. Failed ones can be retried. */
    static String develop(ServerLevel world, BuyanState.Site site) {
        if (world.dimension() != Level.OVERWORLD
            || BuyanState.get(world).active || BuyanNatureState.get(world).active
            || !BuyanNatureState.get(world).finishedAt(site)
            || !BuyanCityPlan.hasBuilt(world, site))
            return "Остров и городская планировка ещё не готовы";

        boolean shrine = attempt("sanctuary", () -> BuyanShrine.placeOnce(world, site));
        boolean oak = attempt("sacred oak", () -> placeOak(world, site));
        boolean palace = attempt("terem", () -> BuyanTerem.placeOnce(world, site));
        boolean housing = attempt("housing and shops", () -> placeHouses(world, site));
        boolean harbor = attempt("harbor", () -> BuyanHarbor.placeOnce(world, site));
        boolean craft = attempt("forge and armory", () -> BuyanCraftQuarter.placeOnce(world, site));
        boolean outpost = attempt("military outpost", () -> BuyanOutpost.placeOnce(world, site));
        return "святилище=" + shrine + ", дуб=" + oak + ", терем=" + palace
            + ", жильё/торговля=" + housing + ", порт=" + harbor
            + ", мастерские=" + craft + ", аванпост=" + outpost;
    }

    public static int generate(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        int x = (int) Math.floor(source.getPosition().x);
        int z = (int) Math.floor(source.getPosition().z);
        BuyanState.Site selected = null;
        long distance = Long.MAX_VALUE;
        for (BuyanState.Site site : BuyanState.get(world).completedSites()) {
            long dx = (long) x - site.x(), dz = (long) z - site.z();
            long r2 = dx * dx + dz * dz;
            if (r2 <= (long) site.radius() * site.radius() && r2 < distance) {
                distance = r2;
                selected = site;
            }
        }
        if (selected == null) {
            source.sendFailure(Component.literal(
                "[Буян] Встаньте на созданный остров и повторите команду."));
            return 0;
        }
        String result = develop(world, selected);
        source.sendSuccess(() -> Component.literal("[Буян] Достройка: " + result), true);
        return result.contains("false") || result.contains("не готовы") ? 0 : 1;
    }
}
