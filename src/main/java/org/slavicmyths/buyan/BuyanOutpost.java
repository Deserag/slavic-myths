package org.slavicmyths.buyan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.loot.LootTable;

/**
 * An eleven-building hill outpost integrated into the existing Buyan.
 * All geometry is staged and collision-checked before the first world write.
 * Nothing runs on chunk load: successful sites are persisted independently.
 */
public final class BuyanOutpost {
    private static final String KEY = "slavicmyths_buyan_outpost_2622";
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();
    private static final BlockState PLANK = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState LOG = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
    private static final BlockState STONE = Blocks.COBBLESTONE.defaultBlockState();
    private static final BlockState ROOF = Blocks.DARK_OAK_PLANKS.defaultBlockState();

    private BuyanOutpost() {}

    private static final class Record extends SavedData {
        final Map<Long, Long> built = new HashMap<>();

        static Record get(ServerLevel world) {
            return world.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(Record::new, Record::load), KEY);
        }

        static Record load(CompoundTag tag, HolderLookup.Provider registries) {
            Record record = new Record();
            long[] keys = tag.getLongArray("Sites");
            long[] positions = tag.getLongArray("Positions");
            for (int i = 0; i < Math.min(keys.length, positions.length); i++)
                record.built.put(keys[i], positions[i]);
            return record;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            long[] keys = new long[built.size()];
            long[] positions = new long[built.size()];
            int i = 0;
            for (var entry : built.entrySet()) {
                keys[i] = entry.getKey();
                positions[i] = entry.getValue();
                i++;
            }
            tag.putLongArray("Sites", keys);
            tag.putLongArray("Positions", positions);
            return tag;
        }

        void mark(long key, BlockPos center) {
            built.put(key, center.asLong());
            setDirty();
        }
    }

    private record Hill(BuyanState.Site site, int cx, int cz, int zx, int zz) {
        Rotation rotation() {
            if (zx == 1) return Rotation.COUNTERCLOCKWISE_90;
            if (zx == -1) return Rotation.CLOCKWISE_90;
            return zz == 1 ? Rotation.NONE : Rotation.CLOCKWISE_180;
        }

        BlockPos at(int x, int y, int z) {
            return new BlockPos(cx + zz * x + zx * z, y, cz - zx * x + zz * z);
        }

        int ground(long seed, int x, int z) {
            BlockPos point = at(x, 0, z);
            return BuyanNature.top(seed, site.x(), site.z(), site.radius(),
                point.getX(), point.getZ(), site.waterLevel());
        }
    }

    private record Plot(int x, int z, int width, int depth, int floors, String kind) {
        int door() { return x + width / 2; }
    }

    private record Candidate(Hill hill, double score) {}

    private static List<Hill> selectHills(ServerLevel world, BuyanState.Site site) {
        long seed = world.getSeed();
        List<Candidate> ranked = new ArrayList<>();
        for (int dx = -216; dx <= 216; dx += 12) {
            for (int dz = -216; dz <= 216; dz += 12) {
                int distance = dx * dx + dz * dz;
                if (distance < 75 * 75 || distance > 225 * 225) continue;
                int x = site.x() + dx, z = site.z() + dz;
                int top = BuyanNature.top(seed, site.x(), site.z(),
                    site.radius(), x, z, site.waterLevel());
                if (top < site.waterLevel() + 36) continue;
                int lo = top, hi = top;
                boolean available = true;
                for (int[] offset : new int[][]{{-7,0},{7,0},{0,-7},{0,7},{-7,-7},{7,7}}) {
                    int sample = BuyanNature.top(seed, site.x(), site.z(),
                        site.radius(), x + offset[0], z + offset[1], site.waterLevel());
                    if (sample == Integer.MIN_VALUE) { available = false; break; }
                    lo = Math.min(lo, sample);
                    hi = Math.max(hi, sample);
                }
                if (!available || hi - lo > 8) continue;
                double score = top - (hi - lo) * 2.0 - Math.sqrt(distance) * 0.018;
                int zx = Math.abs(dx) >= Math.abs(dz) ? Integer.signum(dx) : 0;
                int zz = zx == 0 ? Integer.signum(dz) : 0;
                ranked.add(new Candidate(new Hill(site, x, z, zx, zz), score));
            }
        }
        ranked.sort((a, b) -> Double.compare(b.score(), a.score()));
        List<Hill> candidates = new ArrayList<>();
        for (Candidate candidate : ranked) {
            Hill hill = candidate.hill();
            boolean nearby = false;
            for (Hill chosen : candidates) {
                long dx = hill.cx() - chosen.cx(), dz = hill.cz() - chosen.cz();
                if (dx * dx + dz * dz < 28L * 28L) {
                    nearby = true;
                    break;
                }
            }
            if (!nearby) candidates.add(hill);
            if (candidates.size() >= 12) break;
        }
        return candidates;
    }

    private static final class Plan {
        final ServerLevel world;
        final Hill hill;
        final long seed;
        final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();
        final Map<BlockPos, String> loot = new LinkedHashMap<>();
        final List<BlockPos> weaponRacks = new ArrayList<>();
        final List<Plot> plots = new ArrayList<>();
        final Map<Integer, Integer> mainHeight = new HashMap<>();
        final List<List<BlockPos>> routes = new ArrayList<>();

        Plan(ServerLevel world, Hill hill) {
            this.world = world;
            this.hill = hill;
            this.seed = world.getSeed();
        }

        void at(int x, int y, int z, BlockState block) {
            BlockPos point = hill.at(x, y, z);
            blocks.put(point, block.rotate(hill.rotation()));
        }

        void fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
            for (int x = x1; x <= x2; x++)
                for (int z = z1; z <= z2; z++)
                    for (int y = y1; y <= y2; y++) at(x, y, z, state);
        }

        int ground(int x, int z) { return hill.ground(seed, x, z); }

        int floor(Plot plot) {
            int highest = Integer.MIN_VALUE;
            for (int x = plot.x; x < plot.x + plot.width; x++)
                for (int z = plot.z; z < plot.z + plot.depth; z++) {
                    int y = ground(x, z);
                    if (y == Integer.MIN_VALUE) return Integer.MIN_VALUE;
                    highest = Math.max(highest, y);
                }
            highest = Math.max(highest, ground(plot.door(), plot.z - 1));
            if (highest == Integer.MIN_VALUE) return Integer.MIN_VALUE;
            return highest + 1 + (plot.kind.equals("hanging") ? 4 : 0);
        }

        void posts(int x, int z, int level) {
            int ground = ground(x, z);
            if (ground == Integer.MIN_VALUE) throw new IllegalStateException("Outpost support outside Buyan");
            for (int y = ground + 1; y < level; y++) at(x, y, z, y < level - 4 ? STONE : LOG);
        }

        void chest(int x, int y, int z, String table) {
            at(x, y, z, Blocks.CHEST.defaultBlockState());
            loot.put(hill.at(x, y, z), table);
        }

        void bed(int x, int y, int z) {
            BlockState state = Blocks.RED_BED.defaultBlockState()
                .setValue(BedBlock.FACING, Direction.SOUTH);
            at(x, y, z, state.setValue(BedBlock.PART, BedPart.FOOT));
            at(x, y, z + 1, state.setValue(BedBlock.PART, BedPart.HEAD));
        }

        void lantern(int x, int y, int z) {
            at(x, y + 1, z, LOG);
            at(x, y, z, Blocks.LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true));
        }

        int roofRise(Plot p, int u) {
            int pitch = p.kind.equals("commander") ? 1
                : p.kind.equals("supply") ? 3 : 2;
            return Math.max(0, Math.min(u + 1, p.width - u) / pitch);
        }

        boolean paleRoof(Plot p) {
            return (p.kind.equals("barracks") && p.floors == 1)
                || p.kind.equals("supply")
                || (p.kind.equals("hanging") && p.x < 0);
        }

        void roof(Plot p, int floor, int top) {
            int x = p.x, z = p.z, w = p.width, d = p.depth;
            boolean light = paleRoof(p);
            BlockState tile = light ? Blocks.SPRUCE_STAIRS.defaultBlockState()
                : Blocks.DARK_OAK_STAIRS.defaultBlockState();
            BlockState backing = light ? Blocks.SPRUCE_PLANKS.defaultBlockState() : ROOF;
            int eave = p.kind.equals("barracks") && p.floors == 2 ? 2 : 1;
            for (int u = -eave; u <= w - 1 + eave; u++) {
                int y = top + 2 + roofRise(p, u);
                // Stair fronts climb TOWARD the ridge, not away from it.
                Direction up = u < w / 2 ? Direction.EAST : Direction.WEST;
                BlockState slope = tile
                    .setValue(StairBlock.FACING, up)
                    .setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.BOTTOM)
                    .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
                for (int v = -eave; v <= d - 1 + eave; v++) {
                    at(x + u, y, z + v, slope);
                    if (u >= 0 && u < w && v >= 0 && v < d)
                        at(x + u, y - 1, z + v, backing);
                }
                // Both end gables are closed all the way to the supporting roof.
                if (u >= 0 && u < w)
                    for (int a = top + 1; a < y - 1; a++) {
                        at(x + u, a, z, p.kind.equals("armory")
                            ? Blocks.STONE_BRICKS.defaultBlockState() : backing);
                        at(x + u, a, z + d - 1, p.kind.equals("armory")
                            ? Blocks.STONE_BRICKS.defaultBlockState() : backing);
                    }
            }
            // Check staged continuity for every occupied roof column.
            for (int u = 0; u < w; u++) {
                int y = top + 2 + roofRise(p, u);
                for (int v = 0; v < d; v++) {
                    BlockState beneath = blocks.get(hill.at(x + u, y - 1, z + v));
                    BlockState above = blocks.get(hill.at(x + u, y, z + v));
                    if (beneath == null || !beneath.is(backing.getBlock())
                        || above == null || !above.is(tile.getBlock()))
                        throw new IllegalStateException("Outpost roof surface/backing gap");
                }
            }
        }

        void doorAt(int x, int floor, int z, Direction facing, BlockState material) {
            BlockState lower = material.setValue(DoorBlock.FACING, facing)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER);
            at(x, floor + 1, z, lower);
            at(x, floor + 2, z, lower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        }

        void door(Plot p, int floor) {
            doorAt(p.door(), floor, p.z, Direction.NORTH,
                p.kind.equals("armory") ? Blocks.DARK_OAK_DOOR.defaultBlockState()
                    : Blocks.SPRUCE_DOOR.defaultBlockState());
        }

        void house(Plot p, int y) {
            int x = p.x, z = p.z, w = p.width, d = p.depth;
            int height = p.floors == 2 ? 10 : 5;
            // Only footprint and a narrow, bounded roof/vegetation clearance.
            fill(x - 1, y + 1, z - 1, x + w, y + height + 9, z + d, AIR);
            fill(x, y, z, x + w - 1, y, z + d - 1, PLANK);
            for (int a : new int[]{x, x + w - 1})
                for (int b : new int[]{z, z + d - 1}) posts(a, b, y);
            BlockState panel = p.kind.equals("commander") || p.kind.equals("hanging")
                || (p.kind.equals("barracks") && p.floors == 2)
                ? Blocks.OAK_PLANKS.defaultBlockState() : PLANK;
            BlockState beam = p.kind.equals("commander")
                ? Blocks.DARK_OAK_LOG.defaultBlockState() : LOG;
            for (int k = 1; k <= height; k++) {
                BlockState wall = k <= (p.kind.equals("armory") ? 3 : 1)
                    && !p.kind.equals("hanging")
                    ? (p.kind.equals("armory") ? Blocks.STONE_BRICKS.defaultBlockState() : STONE)
                    : panel;
                for (int u = 0; u < w; u++) {
                    at(x + u, y + k, z, wall);
                    at(x + u, y + k, z + d - 1, wall);
                }
                for (int v = 1; v < d - 1; v++) {
                    at(x, y + k, z + v, wall);
                    at(x + w - 1, y + k, z + v, wall);
                }
            }
            // Strong posts, crossbeams and visible bays; no random recoloring.
            for (int a : new int[]{x, x + w - 1})
                for (int b : new int[]{z, z + d - 1})
                    fill(a, y + 1, b, a, y + height, b, beam);
            for (int a : new int[]{x + 4, x + w - 5})
                for (int b : new int[]{z, z + d - 1})
                    fill(a, y + 2, b, a, y + height, b, beam);
            for (int b : new int[]{z + 3, z + d - 4})
                for (int a : new int[]{x, x + w - 1})
                    fill(a, y + 2, b, a, y + height, b, beam);
            for (int u = 0; u < w; u++)
                for (int b : new int[]{z, z + d - 1})
                    at(x + u, y + height, b, beam);
            for (int v = 1; v < d - 1; v++)
                for (int a : new int[]{x, x + w - 1})
                    at(a, y + height, z + v, beam);
            // Slit windows for armory, wider glazed bays for occupied housing.
            for (int a : new int[]{x + 2, x + w - 3})
                for (int b : new int[]{z, z + d - 1}) {
                    at(a, y + 3, b, Blocks.GLASS_PANE.defaultBlockState());
                    if (!p.kind.equals("armory"))
                        at(a, y + 4, b, Blocks.GLASS_PANE.defaultBlockState());
                    at(a - 1, y + 3, b, beam);
                    at(a + 1, y + 3, b, beam);
                }
            if (p.floors == 2)
                for (int a : new int[]{x + 2, x + w - 3})
                    for (int b : new int[]{z, z + d - 1}) {
                        at(a, y + 8, b, Blocks.GLASS_PANE.defaultBlockState());
                        at(a, y + 7, b, Blocks.GLASS_PANE.defaultBlockState());
                    }
            if (p.floors == 2) {
                fill(x + 1, y + 5, z + 1, x + w - 2, y + 5, z + d - 2, PLANK);
                // An actual opening, continuous backed ladder and upper landing.
                at(x + w - 2, y + 5, z + d - 2, AIR);
                for (int a = 1; a <= 9; a++)
                    at(x + w - 2, y + a, z + d - 2,
                        Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.WEST));
                chest(x + 2, y + 6, z + d - 3, "outpost_supply");
                bed(x + 3, y + 6, z + 2);
                bed(x + 5, y + 6, z + 2);
            }
            roof(p, y, y + height);
            if (p.kind.equals("barracks")) {
                if (p.floors == 2) {
                    for (int i = 0; i < 6; i++) bed(x + 2 + 2 * i, y + 1, z + 2);
                } else {
                    // Preserve a two-block-wide approach from the single front door.
                    bed(x + 2, y + 1, z + 2);
                    bed(x + w - 3, y + 1, z + 2);
                    bed(x + 2, y + 1, z + 4);
                }
                chest(x + (p.floors == 2 ? 2 : 3), y + 1, z + d - 3, "outpost_supply");
                at(x + (p.floors == 2 ? w - 3 : w - 2), y + 1, z + 2,
                    Blocks.CRAFTING_TABLE.defaultBlockState());
                at(x + w - 3, y + 1, z + d - 3, Blocks.BARREL.defaultBlockState());
                weaponRacks.add(hill.at(x + w - 2, y + 1, z + d - 3));
            } else if (p.kind.equals("hanging")) {
                bed(x + 2, y + 1, z + 2);
                bed(x + 4, y + 1, z + 2);
                chest(x + w - 3, y + 1, z + d - 3, "outpost_supply");
            } else if (p.kind.equals("armory")) {
                at(x + 2, y + 1, z + 2, Blocks.SMITHING_TABLE.defaultBlockState());
                at(x + 3, y + 1, z + 2, Blocks.GRINDSTONE.defaultBlockState());
                at(x + w - 3, y + 1, z + 2, Blocks.ANVIL.defaultBlockState());
                chest(x + 2, y + 1, z + d - 3, "outpost_armory");
                weaponRacks.add(hill.at(x + w - 2, y + 1, z + d - 3));
            } else if (p.kind.equals("supply")) {
                for (int t = 2; t <= d - 3; t += 2) {
                    at(x + 2, y + 1, z + t, Blocks.BARREL.defaultBlockState());
                    chest(x + w - 3, y + 1, z + t, "outpost_supply");
                }
            } else if (p.kind.equals("commander")) {
                bed(x + 2, y + 1, z + 2);
                at(x + w - 3, y + 1, z + 2, Blocks.LECTERN.defaultBlockState());
                at(x + w - 3, y + 1, z + 3, Blocks.BOOKSHELF.defaultBlockState());
                chest(x + 2, y + 1, z + d - 3, "outpost_command");
            }
            at(x + w / 2 + 1, y + 1, z + d - 3, Blocks.SPRUCE_STAIRS.defaultBlockState());
            at(x + w / 2 + 1, y + 1, z + d - 4, Blocks.SPRUCE_SLAB.defaultBlockState());
            lantern(x + 1, y + height - 1, z + d - 2);
            if (p.floors == 2) lantern(x + 1, y + 3, z + 3);
            door(p, y);
            // A three-block porch with a real overhead canopy, leaving the
            // central approach and door entirely unobstructed.
            for (int xx = p.door() - 2; xx <= p.door() + 2; xx++)
                for (int zz = z - 2; zz <= z - 1; zz++)
                    at(xx, y + 4, zz, Blocks.DARK_OAK_SLAB.defaultBlockState());
            for (int xx : new int[]{p.door() - 2, p.door() + 2})
                fill(xx, y + 1, z - 2, xx, y + 3, z - 2, LOG);
            if (p.kind.equals("barracks") && p.floors == 2) {
                // Wide, usable side gallery, reached through a second doorway.
                for (int xx = x - 3; xx < x; xx++)
                    for (int zz = z + 1; zz < z + d - 1; zz++)
                        at(xx, y, zz, PLANK);
                for (int zz = z + 1; zz < z + d - 1; zz += 3) {
                    posts(x - 3, zz, y);
                    at(x - 3, y + 1, zz, Blocks.SPRUCE_FENCE.defaultBlockState());
                }
                doorAt(x, y, z + d / 2, Direction.WEST, Blocks.SPRUCE_DOOR.defaultBlockState());
            }
            if (p.kind.equals("barracks") && p.floors == 1) {
                // Compact lean-to, clearly different from the large barracks.
                for (int zz = z + 1; zz < z + d - 1; zz++) {
                    at(x + w, y + 4, zz, Blocks.SPRUCE_SLAB.defaultBlockState());
                    if (zz % 3 == 0) posts(x + w, zz, y + 4);
                }
            }
            if (p.kind.equals("armory") || p.kind.equals("supply")) {
                for (int xx : new int[]{x + 1, x + w - 2})
                    at(xx, y + 1, z - 3, Blocks.BARREL.defaultBlockState());
            }
            if (p.kind.equals("commander")) {
                for (int xx : new int[]{x + 1, x + w - 2}) {
                    at(xx, y + 4, z - 1, Blocks.RED_BANNER.defaultBlockState());
                    at(xx, y + 3, z - 1, LOG);
                }
            }
            if (p.kind.equals("hanging")) {
                for (int xx = x + 2; xx < x + w - 1; xx += 3) {
                    for (int h = 1; h <= 4; h++) {
                        at(xx, y - h, z + 1 + h, LOG);
                        at(xx, y - h, z + d - 2 - h, LOG);
                    }
                }
                for (int xx = x; xx < x + w; xx++)
                    at(xx, y - 1, z + d / 2, LOG);
                // Outward balcony, with a real back exit and diagonal braces.
                for (int xx = x + 1; xx < x + w - 1; xx++)
                    for (int zz = z + d; zz <= z + d + 2; zz++)
                        at(xx, y, zz, PLANK);
                for (int xx = x + 1; xx < x + w - 1; xx++)
                    at(xx, y + 1, z + d + 2, Blocks.SPRUCE_FENCE.defaultBlockState());
                for (int h = 1; h <= 3; h++)
                    for (int xx : new int[]{x + 1, x + w - 2})
                        at(xx, y - h, z + d + 2 - h, LOG);
                doorAt(p.door(), y, z + d - 1, Direction.SOUTH,
                    Blocks.SPRUCE_DOOR.defaultBlockState());
            }
        }

        int towerHeight(Plot p) {
            if (p.z > 0 && p.x > 0) return 15;  // sea-facing signal tower
            if (p.z > 0) return 12;              // compact inland tower
            if (p.z < -80) return 11;            // stout gate tower
            return 13;                          // slope-facing tower
        }

        int galleryOverhang(Plot p) {
            return p.z > 0 && p.x > 0 ? 2 : 1;
        }

        int towerRoofY(Plot p, int u, int v, int height) {
            int edge = galleryOverhang(p);
            int nearest = Math.min(
                Math.min(u + edge + 1, p.width + edge - u),
                Math.min(v + edge + 1, p.depth + edge - v));
            return height + 6 + Math.max(0, nearest / 2);
        }

        void tower(Plot p, int y) {
            int x = p.x, z = p.z, w = p.width, d = p.depth;
            int height = towerHeight(p);
            int middle = height / 2;
            int overhang = galleryOverhang(p);
            boolean sea = p.z > 0 && p.x > 0;
            boolean gate = p.z < -80;
            boolean slope = p.z < 0 && !gate;
            BlockState beam = sea ? Blocks.DARK_OAK_LOG.defaultBlockState() : LOG;
            BlockState panel = sea || gate ? Blocks.OAK_PLANKS.defaultBlockState() : PLANK;
            BlockState masonry = sea ? Blocks.STONE_BRICKS.defaultBlockState() : STONE;
            BlockState tile = (sea || gate) ? Blocks.DARK_OAK_STAIRS.defaultBlockState()
                : Blocks.SPRUCE_STAIRS.defaultBlockState();
            BlockState backing = (sea || gate) ? ROOF : PLANK;

            fill(x - overhang - 1, y + 1, z - overhang - 1,
                x + w + overhang, y + height + 13, z + d + overhang, AIR);
            fill(x, y, z, x + w - 1, y, z + d - 1, masonry);
            for (int a : new int[]{x, x + w - 1})
                for (int b : new int[]{z, z + d - 1}) posts(a, b, y);
            // Real stone plinth, regular wooden structural bays above it.
            for (int v = 1; v <= height; v++) {
                BlockState wall = v <= (gate ? 4 : 3) ? masonry : panel;
                for (int a = 0; a < w; a++) {
                    at(x + a, y + v, z, wall);
                    at(x + a, y + v, z + d - 1, wall);
                }
                for (int b = 1; b < d - 1; b++) {
                    at(x, y + v, z + b, wall);
                    at(x + w - 1, y + v, z + b, wall);
                }
            }
            for (int a : new int[]{x, x + w - 1})
                for (int b : new int[]{z, z + d - 1})
                    fill(a, y + 1, b, a, y + height, b, beam);
            for (int a : new int[]{x + 2, x + w - 3})
                for (int b : new int[]{z, z + d - 1})
                    fill(a, y + 4, b, a, y + height, b, beam);
            for (int v : new int[]{middle, height})
                for (int a = x; a < x + w; a++)
                    for (int b = z; b < z + d; b++)
                        if (a == x || a == x + w - 1 || b == z || b == z + d - 1)
                            at(a, y + v, b, beam);

            // Upper and lower occupied floors, including uninterrupted ladder well.
            fill(x + 1, y + middle, z + 1,
                x + w - 2, y + middle, z + d - 2, PLANK);
            fill(x - overhang, y + height, z - overhang,
                x + w - 1 + overhang, y + height, z + d - 1 + overhang, PLANK);
            for (int deck : new int[]{middle, height})
                at(x + 1, y + deck, z + 1, AIR);
            // Back wall x supports each rung (FACING EAST => WEST support).
            at(x, y + height + 1, z + 1, beam);
            for (int v = 1; v <= height + 1; v++)
                at(x + 1, y + v, z + 1,
                    Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.EAST));
            // The gallery overhang has heavy visible supports and diagonal corbels.
            for (int a : new int[]{x - overhang, x + w - 1 + overhang})
                for (int b : new int[]{z - overhang, z + d - 1 + overhang}) {
                    for (int h = 1; h <= overhang + 1; h++) {
                        int towardX = a < x ? a + h : a - h;
                        int towardZ = b < z ? b + h : b - h;
                        at(towardX, y + height - h, towardZ, beam);
                    }
                    fill(a, y + height + 1, b, a, y + height + 5, b, beam);
                }
            // Rails stay strictly at the outer edge, never across the exit.
            for (int a = x - overhang + 1; a < x + w - 1 + overhang; a++)
                for (int b : new int[]{z - overhang, z + d - 1 + overhang})
                    at(a, y + height + 1, b, Blocks.SPRUCE_FENCE.defaultBlockState());
            for (int b = z - overhang + 1; b < z + d - 1 + overhang; b++)
                for (int a : new int[]{x - overhang, x + w - 1 + overhang})
                    at(a, y + height + 1, b, Blocks.SPRUCE_FENCE.defaultBlockState());
            // Shaded, fully roofed HIP profile, not an undersized cube cap.
            for (int u = -overhang - 1; u <= w + overhang; u++)
                for (int v = -overhang - 1; v <= d + overhang; v++) {
                    int roofY = towerRoofY(p, u, v, height);
                    int left = u + overhang + 1, right = w + overhang - u;
                    int front = v + overhang + 1, back = d + overhang - v;
                    Direction up = Math.min(left, right) <= Math.min(front, back)
                        ? (left <= right ? Direction.EAST : Direction.WEST)
                        : (front <= back ? Direction.SOUTH : Direction.NORTH);
                    BlockState sloping = tile
                        .setValue(StairBlock.FACING, up)
                        .setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.BOTTOM)
                        .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
                    at(x + u, y + roofY, z + v, sloping);
                    if (u >= 0 && u < w && v >= 0 && v < d)
                        at(x + u, y + roofY - 1, z + v, backing);
                }
            // Small slits alternate with wide wall sections; do not obstruct ladder.
            for (int v : new int[]{4, middle + 2})
                for (int b : new int[]{z, z + d - 1})
                    at(x + w / 2, y + v, b, Blocks.GLASS_PANE.defaultBlockState());
            for (int v : new int[]{5, middle + 3})
                for (int a : new int[]{x, x + w - 1})
                    at(a, y + v, z + d - 2, Blocks.GLASS_PANE.defaultBlockState());
            chest(x + w - 3, y + 1, z + d - 3, "outpost_armory");
            at(x + 2, y + 1, z + d - 3, Blocks.BARREL.defaultBlockState());
            at(x + w - 3, y + middle + 1, z + d - 3,
                Blocks.LANTERN.defaultBlockState());
            door(p, y);
            // Entrance canopy and bold gate-house stone flanks.
            for (int a = p.door() - 2; a <= p.door() + 2; a++)
                for (int b = z - 2; b <= z - 1; b++)
                    at(a, y + 4, b, Blocks.DARK_OAK_SLAB.defaultBlockState());
            for (int a : new int[]{p.door() - 2, p.door() + 2})
                fill(a, y + 1, z - 2, a, y + 3, z - 2, beam);
            if (sea) {
                // Signal tower: marker above a fully sealed roof.
                int centerY = towerRoofY(p, w / 2, d / 2, height);
                at(x + w / 2, y + centerY + 1, z + d / 2,
                    Blocks.CAMPFIRE.defaultBlockState());
            } else if (slope) {
                // Extra seaward lookout attached to the slope tower.
                for (int a = x + w; a <= x + w + 2; a++)
                    for (int b = z + 1; b <= z + d - 2; b++)
                        at(a, y + height, b, PLANK);
                for (int b = z + 1; b <= z + d - 2; b++)
                    at(x + w + 2, y + height + 1, b, Blocks.SPRUCE_FENCE.defaultBlockState());
                for (int h = 1; h <= 3; h++)
                    at(x + w + 2 - h, y + height - h, z + d / 2, LOG);
            }
        }

        int platform(int x1, int z1, int x2, int z2) {
            int floor = Integer.MIN_VALUE;
            for (int x = x1; x <= x2; x++)
                for (int z = z1; z <= z2; z++)
                    floor = Math.max(floor, ground(x, z) + 1);
            if (floor == Integer.MIN_VALUE) return Integer.MIN_VALUE;
            for (int x = x1; x <= x2; x++)
                for (int z = z1; z <= z2; z++) {
                    posts(x, z, floor);
                    at(x, floor, z, PLANK);
                    for (int y = floor + 1; y <= floor + 3; y++) at(x, y, z, AIR);
                }
            return floor;
        }

        void yards() {
            // Open upper drill area, actual hay targets and a solid backstop.
            int range = platform(5, 17, 14, 25);
            if (range != Integer.MIN_VALUE) {
                for (int x = 6; x <= 13; x += 3) {
                    at(x, range + 1, 22, Blocks.HAY_BLOCK.defaultBlockState());
                    at(x, range + 2, 22, Blocks.TARGET.defaultBlockState());
                }
                for (int x = 5; x <= 14; x++)
                    for (int y = range + 1; y <= range + 4; y++)
                        at(x, y, 26, LOG);
            }
            int drill = platform(18, 17, 27, 23);
            if (drill != Integer.MIN_VALUE)
                for (int x = 20; x <= 25; x += 2)
                    at(x, drill + 1, 19, Blocks.OAK_FENCE.defaultBlockState());
            int slope = platform(-8, -63, -3, -57);
            if (slope != Integer.MIN_VALUE)
                for (int x = -8; x <= -3; x += 2)
                    at(x, slope + 1, -60, LOG);
            int lower = platform(3, -94, 8, -89);
            if (lower != Integer.MIN_VALUE)
                at(7, lower + 1, -91, Blocks.CRAFTING_TABLE.defaultBlockState());
        }

        int path(int x1, int z1, int x2, int z2, int start, int finish) {
            int length = Math.abs(x2 - x1) + Math.abs(z2 - z1);
            if (length == 0) return start;
            if (x1 != x2 && z1 != z2) throw new IllegalArgumentException("Path must be orthogonal");
            int dx = Integer.compare(x2, x1), dz = Integer.compare(z2, z1);
            int[] h = new int[length + 1];
            for (int i = 0; i <= length; i++) {
                int x = x1 + dx * i, z = z1 + dz * i;
                int interpolation = start + (int) Math.round((finish - start) * i / (double) length);
                h[i] = Math.max(interpolation, ground(x, z) + 1);
            }
            // Raise low cells until no neighboring pair differs by more than one.
            // Two directional sweeps suffice for the shortest 1-Lipschitz majorant.
            for (int i = length - 1; i >= 0; i--) h[i] = Math.max(h[i], h[i + 1] - 1);
            for (int i = 1; i <= length; i++) h[i] = Math.max(h[i], h[i - 1] - 1);
            if (h[0] != start || h[length] != finish)
                throw new IllegalStateException("Outpost route would disconnect an entrance");
            List<BlockPos> route = new ArrayList<>();
            for (int i = 0; i <= length; i++) {
                if (i > 0 && Math.abs(h[i] - h[i - 1]) > 1)
                    throw new IllegalStateException("Outpost route height jump");

                int x = x1 + dx * i, z = z1 + dz * i, floor = h[i];
                BlockState board = PLANK;
                // A stair always faces UP the incline. Its lower half meets the
                // neighboring lower deck; the higher half meets the upper deck.
                if (i > 0 && h[i] > h[i - 1]) {
                    Direction uphill = dx != 0
                        ? (dx > 0 ? Direction.EAST : Direction.WEST)
                        : (dz > 0 ? Direction.SOUTH : Direction.NORTH);
                    board = Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, uphill)
                        .setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.BOTTOM)
                        .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
                } else if (i < length && h[i] > h[i + 1]) {
                    Direction uphill = dx != 0
                        ? (dx > 0 ? Direction.WEST : Direction.EAST)
                        : (dz > 0 ? Direction.NORTH : Direction.SOUTH);
                    board = Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, uphill)
                        .setValue(StairBlock.HALF, net.minecraft.world.level.block.state.properties.Half.BOTTOM)
                        .setValue(StairBlock.SHAPE, StairsShape.STRAIGHT);
                }
                for (int side = -1; side <= 1; side++) {
                    int bx = x + (dx != 0 ? 0 : side);
                    int bz = z + (dx != 0 ? side : 0);
                    posts(bx, bz, floor);
                    at(bx, floor, bz, board);
                    for (int clearance = 1; clearance <= 3; clearance++)
                        at(bx, floor + clearance, bz, AIR);
                }
                for (int side : new int[]{-2, 2}) {
                    int bx = x + (dx != 0 ? 0 : side);
                    int bz = z + (dx != 0 ? side : 0);
                    if (floor - ground(bx, bz) > 2) {
                        posts(bx, bz, floor);
                        at(bx, floor, bz, PLANK);
                        at(bx, floor + 1, bz, Blocks.SPRUCE_FENCE.defaultBlockState());
                    }
                }
                route.add(hill.at(x, floor, z));
                if (x1 == 0 && x2 == 0) mainHeight.put(z, floor);
            }
            routes.add(route);
            return h[length];
        }

        void build() {
            Plot[] buildings = {
                new Plot(-21, -91, 14, 10, 2, "barracks"),
                new Plot(12, -91, 7, 7, 1, "tower"),
                new Plot(-23, -72, 9, 8, 1, "supply"),
                new Plot(-25, -49, 10, 8, 1, "hanging"),
                new Plot(16, -45, 10, 8, 1, "hanging"),
                new Plot(3, -55, 7, 7, 1, "tower"),
                new Plot(-24, -7, 9, 8, 1, "barracks"),
                new Plot(-43, 8, 7, 7, 1, "tower"),
                new Plot(37, 8, 7, 7, 1, "tower"),
                new Plot(16, -7, 8, 7, 1, "armory"),
                new Plot(-8, 19, 10, 9, 1, "commander")
            };
            for (Plot p : buildings) {
                if (floor(p) == Integer.MIN_VALUE)
                    throw new IllegalStateException("Outpost plot extends beyond the island");
                plots.add(p);
            }
            int start = ground(0, -98) + 1;
            int finish = ground(0, 15) + 1;
            path(0, -98, 0, 15, start, finish);
            for (Plot p : plots) {
                int entryZ = p.z - 5;
                int entryY = floor(p);
                int join = mainHeight.getOrDefault(entryZ, ground(0, entryZ) + 1);
                int bend = path(0, entryZ, p.door(), entryZ, join, entryY);
                path(p.door(), entryZ, p.door(), p.z - 1, bend, entryY);
            }
            // Final houses overwrite only their own doorways, never roads.
            for (Plot p : plots) {
                int y = floor(p);
                if (p.kind.equals("tower")) tower(p, y);
                else house(p, y);
            }
            yards();
        }

        boolean validate() {
            Set<Long> chunks = new HashSet<>();
            for (var entry : blocks.entrySet()) {
                BlockPos point = entry.getKey();
                if (point.getY() <= world.getMinBuildHeight()
                    || point.getY() >= world.getMaxBuildHeight() - 1
                    || !world.getWorldBorder().isWithinBounds(point))
                    return false;
                if (chunks.add(ChunkPos.asLong(point.getX() >> 4, point.getZ() >> 4)))
                    world.getChunk(point.getX() >> 4, point.getZ() >> 4);
            }
            for (BlockPos point : blocks.keySet())
                if (!BuyanGenerator.canReplace(world.getBlockState(point))) return false;
            // Assert the FINAL staged geometry, not only its intermediate template.
            // Detect beds, doors and roofs damaged by later furnishing or pathways.
            for (var entry : blocks.entrySet()) {
                BlockPos point = entry.getKey();
                BlockState state = entry.getValue();
                if (state.getBlock() instanceof BedBlock) {
                    Direction face = state.getValue(BedBlock.FACING);
                    boolean foot = state.getValue(BedBlock.PART) == BedPart.FOOT;
                    BlockState pair = blocks.get(point.relative(foot ? face : face.getOpposite()));
                    if (pair == null || pair.getBlock() != state.getBlock()
                        || pair.getValue(BedBlock.PART) != (foot ? BedPart.HEAD : BedPart.FOOT))
                        return false;
                }
                if (state.getBlock() instanceof DoorBlock) {
                    boolean lower = state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
                    BlockState pair = blocks.get(lower ? point.above() : point.below());
                    if (pair == null || pair.getBlock() != state.getBlock()
                        || pair.getValue(DoorBlock.HALF) !=
                            (lower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER))
                        return false;
                }
            }
            for (BlockPos point : loot.keySet()) {
                BlockState state = blocks.get(point);
                if (state == null || !state.is(Blocks.CHEST)) return false;
            }
            // Every ladder must be backed by a full structural block on the
            // opposite side of FACING, including the highest and lowest rung.
            for (var entry : blocks.entrySet()) {
                BlockState ladder = entry.getValue();
                if (!(ladder.getBlock() instanceof LadderBlock)) continue;
                BlockPos point = entry.getKey();
                BlockPos support = point.relative(ladder.getValue(LadderBlock.FACING).getOpposite());
                BlockState backing = blocks.get(support);
                if (backing == null || !(backing.is(Blocks.STRIPPED_SPRUCE_LOG)
                    || backing.is(Blocks.OAK_LOG) || backing.is(Blocks.DARK_OAK_LOG)
                    || backing.is(Blocks.SPRUCE_PLANKS) || backing.is(Blocks.DARK_OAK_PLANKS)
                    || backing.is(Blocks.COBBLESTONE) || backing.is(Blocks.STONE_BRICKS)))
                    return false;
                BlockState headroom = blocks.get(point.above());
                if (headroom != null && !headroom.isAir()
                    && !(headroom.getBlock() instanceof LadderBlock)) return false;
            }
            // Check the FINAL walkable surfaces after houses and yards have been
            // installed. Each landing, intersection and stair must remain intact.
            for (List<BlockPos> route : routes) {
                for (int i = 0; i < route.size(); i++) {
                    BlockPos point = route.get(i);
                    BlockState surface = blocks.get(point);
                    if (surface == null || !(surface.is(Blocks.SPRUCE_PLANKS)
                        || surface.is(Blocks.SPRUCE_STAIRS))) return false;
                    for (int dy = 1; dy <= 2; dy++) {
                        BlockState air = blocks.get(point.above(dy));
                        if (air != null && !air.isAir()) return false;
                    }
                    if (i > 0 && Math.abs(point.getY() - route.get(i - 1).getY()) > 1)
                        return false;
                    if (surface.is(Blocks.SPRUCE_STAIRS)) {
                        if (surface.getValue(StairBlock.HALF)
                            != net.minecraft.world.level.block.state.properties.Half.BOTTOM
                            || surface.getValue(StairBlock.SHAPE) != StairsShape.STRAIGHT)
                            return false;
                        BlockPos lower = null;
                        if (i > 0 && point.getY() > route.get(i - 1).getY())
                            lower = route.get(i - 1);
                        else if (i + 1 < route.size() && point.getY() > route.get(i + 1).getY())
                            lower = route.get(i + 1);
                        if (lower == null) return false;
                        int dx = point.getX() - lower.getX();
                        int dz = point.getZ() - lower.getZ();
                        Direction actualUp = dx > 0 ? Direction.EAST : dx < 0
                            ? Direction.WEST : dz > 0 ? Direction.SOUTH : Direction.NORTH;
                        if (surface.getValue(StairBlock.FACING) != actualUp) return false;
                    }
                }
            }
            for (Plot plot : plots) {
                if (plot.kind.equals("tower")) {
                    int height = towerHeight(plot);
                    int x = plot.x, z = plot.z;
                    BlockState tile = plot.z > 0 && plot.x > 0 || plot.z < -80
                        ? Blocks.DARK_OAK_STAIRS.defaultBlockState()
                        : Blocks.SPRUCE_STAIRS.defaultBlockState();
                    BlockState backingType = plot.z > 0 && plot.x > 0 || plot.z < -80
                        ? ROOF : PLANK;
                    for (int u = 0; u < plot.width; u++)
                        for (int v = 0; v < plot.depth; v++) {
                            int roofY = floor(plot) + towerRoofY(plot, u, v, height);
                            BlockState below = blocks.get(hill.at(x + u, roofY - 1, z + v));
                            BlockState above = blocks.get(hill.at(x + u, roofY, z + v));
                            if (below == null || !below.is(backingType.getBlock())
                                || above == null || !above.is(tile.getBlock()))
                                return false;
                        }
                    for (int deck : new int[]{height / 2, height}) {
                        BlockState ladder = blocks.get(
                            hill.at(plot.x + 1, floor(plot) + deck, plot.z + 1));
                        if (ladder == null || !(ladder.getBlock() instanceof LadderBlock))
                            return false;
                    }
                    continue;
                }
                int top = floor(plot) + (plot.floors == 2 ? 10 : 5);
                BlockState tile = paleRoof(plot) ? Blocks.SPRUCE_STAIRS.defaultBlockState()
                    : Blocks.DARK_OAK_STAIRS.defaultBlockState();
                BlockState backingType = paleRoof(plot)
                    ? Blocks.SPRUCE_PLANKS.defaultBlockState() : ROOF;
                for (int u = 0; u < plot.width; u++) {
                    int y = top + 2 + roofRise(plot, u);
                    for (int v = 0; v < plot.depth; v++) {
                        BlockState backing = blocks.get(
                            hill.at(plot.x + u, y - 1, plot.z + v));
                        BlockState stairs = blocks.get(
                            hill.at(plot.x + u, y, plot.z + v));
                        if (backing == null || !backing.is(backingType.getBlock())
                            || stairs == null || !stairs.is(tile.getBlock())
                            || stairs.getValue(StairBlock.FACING) !=
                                (u < plot.width / 2 ? Direction.EAST : Direction.WEST))
                            return false;
                    }
                }
            }
            return true;
        }

        void commit() {
            for (var entry : blocks.entrySet())
                world.setBlock(entry.getKey(), entry.getValue(), 2);
            for (var entry : loot.entrySet()) {
                BlockEntity entity = world.getBlockEntity(entry.getKey());
                if (!(entity instanceof RandomizableContainerBlockEntity chest))
                    throw new IllegalStateException("Outpost chest block entity missing");
                ResourceKey<LootTable> table = ResourceKey.create(Registries.LOOT_TABLE,
                    ResourceLocation.fromNamespaceAndPath("slavicmyths",
                        "chests/" + entry.getValue()));
                chest.setLootTable(table, seed ^ entry.getKey().asLong());
                chest.setChanged();
            }
            // Actual vanilla armor stands, not fake solid-block weapon racks.
            // Spawning happens only once alongside the structure's saved record.
            for (BlockPos point : weaponRacks) {
                ArmorStand stand = EntityType.ARMOR_STAND.create(world);
                if (stand == null) continue;
                stand.setPos(point.getX() + 0.5, point.getY(), point.getZ() + 0.5);
                stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                if (!world.addFreshEntity(stand))
                    com.mojang.logging.LogUtils.getLogger().warn(
                        "Buyan outpost weapon rack could not spawn at {}", point);
            }
        }
    }

    /** Automatic for newly completed Buyans, operator-only for existing saves. */
    static boolean placeOnce(ServerLevel world, BuyanState.Site site) {
        long key = new BlockPos(site.x(), 0, site.z()).asLong();
        Record record = Record.get(world);
        if (record.built.containsKey(key)) return true;
        if (BuyanState.get(world).active || BuyanNatureState.get(world).active
            || !BuyanNatureState.get(world).finishedAt(site)) return false;
        for (Hill hill : selectHills(world, site)) {
            Plan plan = new Plan(world, hill);
            try {
                plan.build();
                if (!plan.validate()) continue;
            } catch (IllegalStateException exception) {
                com.mojang.logging.LogUtils.getLogger().warn(
                    "Buyan outpost candidate rejected at {},{}: {}",
                    hill.cx, hill.cz, exception.getMessage());
                continue;
            }
            // All planned writes were checked before starting. Never try another
            // candidate after an exception during commit: partial writes remain safe.
            try {
                plan.commit();
            } catch (IllegalStateException exception) {
                com.mojang.logging.LogUtils.getLogger().error(
                    "Buyan outpost commit failed at {},{}", hill.cx, hill.cz, exception);
                return false;
            }
            record.mark(key, hill.at(0, plan.ground(0, 0) + 1, 0));
            return true;
        }
        return false;
    }

    public static int status(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        BuyanState.Site site = BuyanNatureGenerator.selectedFoundation(world,
            (int) Math.floor(source.getPosition().x), (int) Math.floor(source.getPosition().z));
        if (site == null) {
            source.sendFailure(Component.literal("Буян: здесь не найден завершённый остров."));
            return 0;
        }
        Long position = Record.get(world).built.get(new BlockPos(site.x(), 0, site.z()).asLong());
        if (position == null) {
            source.sendSuccess(() -> Component.literal("Буян: военный аванпост ещё не создан."), false);
            return 0;
        }
        BlockPos anchor = BlockPos.of(position);
        source.sendSuccess(() -> Component.literal("Военный аванпост: " + anchor.toShortString()
            + "; 11 зданий, три уровня. Генерация сохранена."), false);
        return 1;
    }

    public static int generate(CommandSourceStack source) {
        ServerLevel world = source.getLevel();
        BuyanState.Site site = BuyanNatureGenerator.selectedFoundation(world,
            (int) Math.floor(source.getPosition().x), (int) Math.floor(source.getPosition().z));
        if (site == null || !BuyanNatureState.get(world).finishedAt(site)) {
            source.sendFailure(Component.literal("Буян: сначала завершите основание и природу."));
            return 0;
        }
        if (!placeOnce(world, site)) {
            source.sendFailure(Component.literal("Аванпост: участок занят или рельеф неподходящий. Мир не перестроен."));
            return 0;
        }
        source.sendSuccess(() -> Component.literal("Военный аванпост размещён или уже существует. /slavicmyths dev buyan outpost status"), true);
        return 1;
    }
}
