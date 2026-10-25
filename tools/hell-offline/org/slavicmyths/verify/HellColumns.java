package org.slavicmyths.verify;

import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.*;

/** Native interpolated base columns only: no MinecraftServer, Level, saves or launcher. */
public final class HellColumns {
    public static void main(String[] args) throws Exception {
        net.neoforged.fml.loading.LoadingModList.of(List.of(), List.of(), List.of(), List.of(), Map.of());
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        var lookup = VanillaRegistries.createLookup();
        var dimensionPath = Path.of("src/main/resources/data/minecraft/dimension_type/the_nether.json");
        if (Files.exists(dimensionPath)) {
            var dimension = net.minecraft.world.level.dimension.DimensionType.DIRECT_CODEC
                    .parse(JsonOps.INSTANCE, JsonParser.parseString(Files.readString(dimensionPath))).getOrThrow();
            if (dimension.minY()!=0 || dimension.height()!=256 || dimension.logicalHeight()!=224)
                throw new AssertionError("Dimension profile");
        }
        var settings = lookup.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.NETHER).value();
        String name = "vanilla";
        if (!args[0].equals("vanilla")) {
            settings = NoiseGeneratorSettings.DIRECT_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, lookup),
                    JsonParser.parseString(Files.readString(Path.of(args[0])))).getOrThrow();
            name = "candidate";
        }
        var biome = lookup.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.NETHER_WASTES);
        var generator = new NoiseBasedChunkGenerator(new FixedBiomeSource(biome), Holder.direct(settings));
        var height = LevelHeightAccessor.create(0, 256);
        var rows = new JsonArray();
        // Identical coordinates for both runs, five fixed independent seeds, four regions.
        long[] seeds = {0, 1, -8675309, 20261024, 987654321};
        int[][] origins = {{0,0},{1024,-1024},{-2048,1536},{4096,3072}};
        for (long seed : seeds) {
            var state = RandomState.create(settings, lookup.lookupOrThrow(Registries.NOISE), seed);
            for (int[] origin : origins) {
                long started = System.nanoTime();
                int air = 0, lava = 0, openColumns = 0, maxGap = 0, floors = 0;
                int seaColumns = 0, solidBottom = 0, solidRoof = 0;
                int[][] ground = new int[64][64];
                for (var line : ground) Arrays.fill(line, -1);
                var slice = new JsonArray();
                for (int dx = 0; dx < 64; dx++) {
                    StringBuilder cells = new StringBuilder();
                    for (int dz = 0; dz < 64; dz++) {
                        var column = generator.getBaseColumn(origin[0]+dx, origin[1]+dz, height, state);
                        if (!column.getBlock(31).getFluidState().isEmpty()) seaColumns++;
                        if (!column.getBlock(0).isAir() && column.getBlock(0).getFluidState().isEmpty()) solidBottom++;
                        if (!column.getBlock(settings.noiseSettings().height()-1).isAir()
                                && column.getBlock(settings.noiseSettings().height()-1).getFluidState().isEmpty()) solidRoof++;
                        int gap = 0, longest = 0;
                        for (int y = 0; y < settings.noiseSettings().height(); y++) {
                            var block = column.getBlock(y);
                            if (block.isAir()) { air++; longest = Math.max(longest, ++gap); }
                            else { gap = 0; if (!block.getFluidState().isEmpty()) lava++; }
                            if (y > 32 && y < 110 && !block.isAir() && block.getFluidState().isEmpty()
                                    && column.getBlock(y+1).isAir() && column.getBlock(y+2).isAir()) {
                                floors++;
                                // Lower ground, excluding roof. Base terrain only, no decorations.
                                ground[dx][dz] = y;
                            }
                            if (dz == 32) cells.append(block.isAir() ? '.' : !block.getFluidState().isEmpty() ? '~' : '#');
                        }
                        if (longest >= 48) openColumns++;
                        maxGap = Math.max(maxGap, longest);
                    }
                    slice.add(cells.toString());
                }
                var row = new JsonObject();
                row.addProperty("seed", seed); row.addProperty("x", origin[0]); row.addProperty("z", origin[1]);
                row.addProperty("height", settings.noiseSettings().height()); row.addProperty("columns",4096);
                row.addProperty("airBlocks",air); row.addProperty("lavaBlocks",lava);
                row.addProperty("columnsWith48Air",openColumns); row.addProperty("maximumAirGap",maxGap);
                row.addProperty("twoBlockFloorsY33to109",floors);
                row.addProperty("seaSurfaceColumnsY31",seaColumns);
                row.addProperty("solidBottomColumns",solidBottom); row.addProperty("solidRoofColumns",solidRoof);
                row.addProperty("largestBaseGroundComponentStep1", component(ground));
                row.addProperty("baseColumnMillis", (System.nanoTime()-started)/1e6);
                row.add("sliceX1Y1_atZ32", slice); rows.add(row);
                System.out.println(name+" seed="+seed+" x="+origin[0]+" z="+origin[1]+" open48="+openColumns+" maxGap="+maxGap);
            }
        }
        Path out = Path.of("docs/implementation/hell-patch1/"+name+"-columns.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out,new GsonBuilder().setPrettyPrinting().create().toJson(rows));
        System.out.println("Native base-column sample complete: "+out+". Surface/features/structures/portals/MSPT NOT_RUN; game/server launches 0.");
    }

    private static int component(int[][] ground) {
        boolean[][] seen = new boolean[64][64];
        int largest = 0;
        for (int x=0; x<64; x++) for (int z=0; z<64; z++) {
            if (seen[x][z] || ground[x][z]<0) continue;
            var queue = new ArrayDeque<int[]>(); queue.add(new int[]{x,z}); seen[x][z]=true;
            int size=0;
            while (!queue.isEmpty()) {
                int[] at=queue.removeFirst(); size++;
                for (int[] dir : new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {
                    int nx=at[0]+dir[0], nz=at[1]+dir[1];
                    if (nx<0 || nz<0 || nx>=64 || nz>=64 || seen[nx][nz] || ground[nx][nz]<0) continue;
                    if (Math.abs(ground[nx][nz]-ground[at[0]][at[1]])>1) continue;
                    seen[nx][nz]=true; queue.addLast(new int[]{nx,nz});
                }
            }
            largest=Math.max(largest,size);
        }
        return largest;
    }
}
