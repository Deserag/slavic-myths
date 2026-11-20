package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slavicmyths.village.VillageRoles;

/** Population exists only at completed Buyan islands, never in general village generation. */
@EventBusSubscriber(modid = "slavicmyths")
public final class BuyanPopulation {
    private static final String DATA_NAME = "slavicmyths_buyan_population_v1";
    private static final int CITIZENS = 24;
    private static final int PRINCE = CITIZENS;
    private static final int TOTAL = CITIZENS + 1;
    private static final String[] ROLES = {
        "zemledelec", "zemledelec", "zemledelec", "zemledelec",
        "rybak", "rybak", "rybak", "kuznec", "kuznec",
        "tkach", "tkach", "travnik", "travnik",
        "kupec", "kupec", "kupec",
        "obychny", "obychny", "obychny", "obychny",
        "obychny", "obychny", "obychny", "obychny"
    };

    private BuyanPopulation() {}

    private static final class PopulationRecord extends SavedData {
        private final Map<Long, Long> occupied = new HashMap<>();

        private static PopulationRecord get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(PopulationRecord::new, PopulationRecord::load), DATA_NAME);
        }

        private static PopulationRecord load(CompoundTag tag, HolderLookup.Provider registries) {
            PopulationRecord result = new PopulationRecord();
            ListTag sites = tag.getList("Sites", Tag.TAG_COMPOUND);
            for (int i = 0; i < sites.size(); ++i) {
                CompoundTag site = sites.getCompound(i);
                result.occupied.put(site.getLong("Key"), site.getLong("Mask"));
            }
            return result;
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag sites = new ListTag();
            for (var entry : occupied.entrySet()) {
                CompoundTag site = new CompoundTag();
                site.putLong("Key", entry.getKey());
                site.putLong("Mask", entry.getValue());
                sites.add(site);
            }
            tag.put("Sites", sites);
            return tag;
        }

        private boolean contains(long site, int slot) {
            return (occupied.getOrDefault(site, 0L) & (1L << slot)) != 0;
        }

        private void mark(long site, int slot) {
            occupied.merge(site, 1L << slot, (a, b) -> a | b);
            setDirty();
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % 100 != 0) return;
        for (ServerLevel world : event.getServer().getAllLevels()) {
            if (world.players().isEmpty()) continue;
            PopulationRecord record = PopulationRecord.get(world);
            for (BuyanState.Site site : BuyanState.get(world).completedSites()) {
                if (!nearPlayer(world, site) || !ready(world, site)) continue;
                BuyanCityPlan.Layout city = BuyanCityPlan.savedLayout(world, site);
                long key = new BlockPos(site.x(), 0, site.z()).asLong();
                int budget = 4;
                for (int slot = 0; slot < TOTAL && budget > 0; ++slot) {
                    if (record.contains(key, slot)) continue;
                    if (trySpawn(world, site, city, slot)) {
                        record.mark(key, slot);
                        budget--;
                    }
                }
            }
        }
    }

    private static boolean ready(ServerLevel world, BuyanState.Site site) {
        return !BuyanState.get(world).active
            && BuyanNatureState.get(world).finishedAt(site)
            && BuyanCityPlan.hasBuilt(world, site)
            && BuyanCityHousing.hasBuilt(world, site)
            && BuyanTerem.hasBuilt(world, site);
    }

    private static boolean nearPlayer(ServerLevel world, BuyanState.Site site) {
        for (var player : world.players()) {
            double dx = player.getX() - site.x();
            double dz = player.getZ() - site.z();
            if (dx * dx + dz * dz <= 180.0D * 180.0D) return true;
        }
        return false;
    }

    private static UUID identity(ServerLevel world, BuyanState.Site site, int slot) {
        String name = "slavicmyths:buyan:" + world.dimension().location() + ":"
            + site.x() + ":" + site.z() + ":" + slot;
        return UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8));
    }

    private static boolean trySpawn(ServerLevel world, BuyanState.Site site,
                                    BuyanCityPlan.Layout city, int slot) {
        UUID id = identity(world, site, slot);
        if (world.getEntity(id) != null) return true;
        String role = slot == PRINCE ? "knyaz" : ROLES[slot];
        BlockPos anchor;
        if (slot == PRINCE) {
            anchor = BuyanTerem.princeHome(world, site);
        } else {
            BuyanCityHousing.Building house = BuyanCityHousing.buildings().get(slot % 8);
            int outsideZ = house.facesSouth() ? house.z() + house.depth() + 1 : house.z() - 2;
            anchor = new BlockPos(site.x() + house.x() + house.width() / 2 + slot / 8 - 1,
                city.centerY() + 1, site.z() + outsideZ);
        }
        BlockPos spawn = findClearSpace(world, site, anchor);
        if (spawn == null) return false;

        Villager villager = EntityType.VILLAGER.create(world);
        if (villager == null) return false;
        villager.setUUID(id);
        villager.setPos(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D);
        villager.setVillagerData(new VillagerData(VillagerType.PLAINS, profession(role), 1));
        villager.setData(VillageRoles.BUYAN_ROLE, role);
        villager.setPersistenceRequired();
        villager.restrictTo(anchor, slot == PRINCE ? 24 : 40);
        villager.setCustomName(Component.literal(display(role)));
        villager.setCustomNameVisible(slot == PRINCE);
        if (!world.addFreshEntity(villager)) return false;

        if (slot < CITIZENS) {
            BuyanCityHousing.Building building = BuyanCityHousing.buildings().get(slot % 8);
            addWorkstation(world, site, city, building, slot, role);
        } else {
            addPrinceWorkstation(world, spawn);
        }
        LogUtils.getLogger().info("Buyan island resident {} spawned at {}", role, spawn);
        return true;
    }

    private static BlockPos findClearSpace(ServerLevel world, BuyanState.Site site, BlockPos anchor) {
        for (int distance = 0; distance <= 4; distance++) {
            for (int dx = -distance; dx <= distance; ++dx) {
                for (int dz = -distance; dz <= distance; ++dz) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != distance) continue;
                    for (int dy = -1; dy <= 2; ++dy) {
                        BlockPos pos = anchor.offset(dx, dy, dz);
                        long x = (long) pos.getX() - site.x();
                        long z = (long) pos.getZ() - site.z();
                        if (x * x + z * z >= (long) site.radius() * site.radius()
                                || !world.getWorldBorder().isWithinBounds(pos)
                                || !loaded(world, pos) || !loaded(world, pos.below())) continue;
                        if (world.getBlockState(pos).isAir()
                                && world.getBlockState(pos.above()).isAir()
                                && world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP))
                            return pos;
                    }
                }
            }
        }
        return null;
    }

    private static boolean loaded(ServerLevel world, BlockPos pos) {
        return world.getChunkSource().hasChunk(pos.getX() >> 4, pos.getZ() >> 4);
    }

    private static void addWorkstation(ServerLevel world, BuyanState.Site site,
                                       BuyanCityPlan.Layout city, BuyanCityHousing.Building b,
                                       int slot, String role) {
        Block workplace = workstation(role);
        if (workplace == null) return;
        int x = slot / 8 == 0 ? 6 : 7;
        int z = slot / 8 == 0 ? 3 : 11;
        int wx = site.x() + b.x() + (b.mirror() ? b.width() - 1 - x : x);
        int wz = site.z() + b.z() + (b.facesSouth() ? b.depth() - 1 - z : z);
        placeStationIfEmpty(world, new BlockPos(wx, city.centerY() + 1, wz), workplace);
    }

    private static void addPrinceWorkstation(ServerLevel world, BlockPos at) {
        // Only an empty, supported space is considered; never replace palace blocks.
        for (int x = -2; x <= 2; x++) {
            BlockPos pos = at.offset(x, 0, 3);
            if (placeStationIfEmpty(world, pos, Blocks.LECTERN)) return;
        }
    }

    private static boolean placeStationIfEmpty(ServerLevel world, BlockPos pos, Block station) {
        if (!loaded(world, pos) || !world.getBlockState(pos).isAir()
            || !world.getBlockState(pos.below()).isFaceSturdy(world, pos.below(), Direction.UP)) return false;
        return world.setBlock(pos, station.defaultBlockState(), 3);
    }

    private static Block workstation(String role) {
        return switch (role) {
            case "zemledelec" -> Blocks.COMPOSTER;
            case "rybak" -> Blocks.BARREL;
            case "kuznec" -> Blocks.BLAST_FURNACE;
            case "tkach" -> Blocks.LOOM;
            case "travnik" -> Blocks.BREWING_STAND;
            case "kupec", "knyaz" -> Blocks.LECTERN;
            default -> null;
        };
    }

    private static VillagerProfession profession(String role) {
        return switch (role) {
            case "zemledelec" -> VillagerProfession.FARMER;
            case "rybak" -> VillagerProfession.FISHERMAN;
            case "kuznec" -> VillagerProfession.ARMORER;
            case "tkach" -> VillagerProfession.SHEPHERD;
            case "travnik" -> VillagerProfession.CLERIC;
            case "kupec", "knyaz" -> VillagerProfession.LIBRARIAN;
            default -> VillagerProfession.NONE;
        };
    }

    private static String display(String role) {
        return switch (role) {
            case "zemledelec" -> "Земледелец Буяна";
            case "rybak" -> "Рыбак Буяна";
            case "kuznec" -> "Кузнец Буяна";
            case "tkach" -> "Ткач Буяна";
            case "travnik" -> "Травник Буяна";
            case "kupec" -> "Купец Буяна";
            case "knyaz" -> "Князь Буяна";
            default -> "Житель Буяна";
        };
    }
}
