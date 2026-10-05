package org.slavicmyths.worldgen;

import java.util.HashSet;
import java.util.Set;
import java.util.function.ToIntFunction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

/** Bounded candidate checks using registry placements and biome noise only; never loads chunks. */
public final class StructureCandidates {
    public static boolean clear(Structure.GenerationContext c, ToIntFunction<ResourceLocation> margin) {
        var structures=c.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for(var set:c.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)) {
            if(!(set.placement() instanceof RandomSpreadStructurePlacement placement))continue;
            for(var entry:set.structures()) {
                Structure other=entry.structure().value();
                int radius=margin.applyAsInt(structures.getKey(other));
                if(radius<=0)continue;
                Set<Long> seen=new HashSet<>();
                for(int dx=-radius;dx<=radius;dx+=radius)for(int dz=-radius;dz<=radius;dz+=radius) {
                    ChunkPos q=placement.getPotentialStructureChunk(c.seed(),c.chunkPos().x+dx,c.chunkPos().z+dz);
                    if(!seen.add(q.toLong())||Math.abs(q.x-c.chunkPos().x)>radius||Math.abs(q.z-c.chunkPos().z)>radius)continue;
                    var biome=c.biomeSource().getNoiseBiome((q.x<<2)+2,16,(q.z<<2)+2,c.randomState().sampler());
                    if(other.biomes().contains(biome))return false;
                }
            }
        }
        return true;
    }
    public static boolean vanilla(ResourceLocation id,String... families) {
        if(id==null||!id.getNamespace().equals("minecraft"))return false;
        for(String family:families)if(id.getPath().equals(family)||id.getPath().startsWith(family+"_"))return true;
        return false;
    }
    public static RandomSpreadStructurePlacement placement(net.minecraft.server.level.ServerLevel world,Structure structure){
        if(structure==null)return null;
        var holder=world.registryAccess().registryOrThrow(Registries.STRUCTURE).wrapAsHolder(structure);
        return world.getChunkSource().getGeneratorState().getPlacementsForStructure(holder).stream().filter(RandomSpreadStructurePlacement.class::isInstance).map(RandomSpreadStructurePlacement.class::cast).findFirst().orElse(null);
    }
    public static boolean biome(net.minecraft.server.level.ServerLevel world,Structure structure,ChunkPos chunk){return structure.biomes().contains(world.getChunkSource().getGenerator().getBiomeSource().getNoiseBiome((chunk.x<<2)+2,16,(chunk.z<<2)+2,world.getChunkSource().randomState().sampler()));}
    public static net.minecraft.core.BlockPos locate(net.minecraft.world.level.levelgen.structure.StructureStart start){var box=start.getBoundingBox();return new net.minecraft.core.BlockPos((box.minX()+box.maxX())/2,box.maxY()+1,(box.minZ()+box.maxZ())/2);}
    private StructureCandidates(){}
}
