package org.slavicmyths.verify;

import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.*;

/** Classification analysis only; no biome registration, worlds, chunks or spawn. */
public final class NavyaClimate {
    public static void main(String[] args) throws Exception {
        net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());
        net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
        var lookup=VanillaRegistries.createLookup();
        var original=MultiNoiseBiomeSourceParameterList.knownPresets().get(MultiNoiseBiomeSourceParameterList.Preset.NETHER);
        var navya=ResourceKey.create(Registries.BIOME,ResourceLocation.fromNamespaceAndPath("slavicmyths","navya_grove"));
        var points=new ArrayList<>(original.values());
        // Candidate chosen for an unoccupied cool/humid climate corner. This is
        // a measured proposal, not an enabled worldgen change or accepted weight.
        points.add(Pair.of(Climate.parameters(-.3F,.4F,0,0,0,0,.175F),navya));
        var candidate=new Climate.ParameterList<>(points);
        var rows=new JsonArray();
        for(long seed:new long[]{0,1,-8675309,20261024,987654321}) {
            var state=RandomState.create(lookup.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(NoiseGeneratorSettings.NETHER).value(),lookup.lookupOrThrow(Registries.NOISE),seed);
            Map<String,Integer> before=new TreeMap<>(),after=new TreeMap<>();
            int[][] grid=new int[128][128];
            for(int x=0;x<128;x++)for(int z=0;z<128;z++) {
                var climate=state.sampler().sample((x*64-4096)>>2,16,(z*64-4096)>>2);
                var old=original.findValue(climate);var next=candidate.findValue(climate);
                before.merge(old.location().toString(),1,Integer::sum);after.merge(next.location().toString(),1,Integer::sum);
                grid[x][z]=next.equals(navya)?1:0;
            }
            var row=new JsonObject();row.addProperty("seed",seed);row.addProperty("sampleCount",16384);
            row.add("vanilla",new Gson().toJsonTree(before));row.add("candidate",new Gson().toJsonTree(after));
            row.addProperty("largestCoarseNavyaComponent",component(grid));rows.add(row);
            System.out.println(seed+" "+after);
        }
        var report=new JsonObject();report.addProperty("scope","Native climate classification, step64, xz=-4096..4032; no worlds/biome injection/features/spawns");
        report.addProperty("candidateOnly",true);report.addProperty("temperature",-.3);report.addProperty("humidity",.4);report.addProperty("offset",.175);report.add("seeds",rows);
        var path=Path.of("docs/implementation/navya-grove/climate-proposal.json");Files.createDirectories(path.getParent());Files.writeString(path,new GsonBuilder().setPrettyPrinting().create().toJson(report));
    }
    private static int component(int[][] grid) {
        boolean[][] seen=new boolean[128][128];int max=0;
        for(int x=0;x<128;x++)for(int z=0;z<128;z++) {
            if(grid[x][z]==0 || seen[x][z])continue;
            ArrayDeque<int[]> q=new ArrayDeque<>();q.add(new int[]{x,z});seen[x][z]=true;int count=0;
            while(!q.isEmpty()) {var at=q.removeFirst();count++;
                for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}) {int nx=at[0]+d[0],nz=at[1]+d[1];
                    if(nx>=0&&nz>=0&&nx<128&&nz<128&&!seen[nx][nz]&&grid[nx][nz]!=0){seen[nx][nz]=true;q.addLast(new int[]{nx,nz});}
                }
            }
            max=Math.max(max,count);
        }
        return max;
    }
}
