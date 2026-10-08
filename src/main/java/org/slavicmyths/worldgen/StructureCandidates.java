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
        return clear(c,c.chunkPos(),margin);
    }
    public static boolean clear(Structure.GenerationContext c,ChunkPos candidate,ToIntFunction<ResourceLocation> margin) {
        var structures=c.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for(var set:c.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)) {
            if(!(set.placement() instanceof RandomSpreadStructurePlacement placement))continue;
            for(var entry:set.structures()) {
                Structure other=entry.structure().value();
                int radius=margin.applyAsInt(structures.getKey(other));
                if(radius<=0)continue;
                Set<Long> seen=new HashSet<>();
                for(int dx=-radius;dx<=radius;dx+=radius)for(int dz=-radius;dz<=radius;dz+=radius) {
                    ChunkPos q=placement.getPotentialStructureChunk(c.seed(),candidate.x+dx,candidate.z+dz);
                    if(!seen.add(q.toLong())||Math.abs(q.x-candidate.x)>radius||Math.abs(q.z-candidate.z)>radius)continue;
                    var biome=c.biomeSource().getNoiseBiome((q.x<<2)+2,16,(q.z<<2)+2,c.randomState().sampler());
                    if(other.biomes().contains(biome))return false;
                }
            }
        }
        return true;
    }
    /** Resolve accepted neighbor plans in the native eight-chunk reference window.
     * The caller supplies an acyclic priority: kurgan -> gorodishche -> camps -> swamp sites -> vanilla.
     * Discarded cells cannot reserve a footprint. No chunks are loaded by this check.
     */
    public static boolean clearAccepted(Structure.GenerationContext c,ChunkPos candidate,ToIntFunction<ResourceLocation> margin,java.util.List<net.minecraft.world.level.levelgen.structure.StructurePiece> proposed){
        var structures=c.registryAccess().registryOrThrow(Registries.STRUCTURE);
        int minX=Integer.MAX_VALUE,minZ=Integer.MAX_VALUE,maxX=Integer.MIN_VALUE,maxZ=Integer.MIN_VALUE;
        for(var piece:proposed){var box=piece.getBoundingBox();minX=Math.min(minX,box.minX()>>4);minZ=Math.min(minZ,box.minZ()>>4);maxX=Math.max(maxX,box.maxX()>>4);maxZ=Math.max(maxZ,box.maxZ()>>4);}
        for(var set:c.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)){
            if(!(set.placement() instanceof RandomSpreadStructurePlacement placement))continue;
            int spacing=placement.spacing();
            for(var entry:set.structures()){
                Structure other=entry.structure().value();if(margin.applyAsInt(structures.getKey(other))<=0)continue;
                if(c.biomeSource().possibleBiomes().stream().noneMatch(other.biomes()::contains))continue;
                for(int gx=Math.floorDiv(minX-8,spacing);gx<=Math.floorDiv(maxX+8,spacing);gx++)for(int gz=Math.floorDiv(minZ-8,spacing);gz<=Math.floorDiv(maxZ+8,spacing);gz++){
                    ChunkPos neighbor=placement.getPotentialStructureChunk(c.seed(),gx*spacing,gz*spacing);
                    if(neighbor.x<minX-8||neighbor.x>maxX+8||neighbor.z<minZ-8||neighbor.z>maxZ+8)continue;
                    var accepted=StructureCoverageService.neighborFootprints(c,other,neighbor);
                    for(var part:proposed)for(var existing:accepted)if(part.getBoundingBox().intersects(existing))return false;
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
    public static boolean biome(net.minecraft.server.level.ServerLevel world,Structure structure,ChunkPos chunk){
        if(structure==null)return false;
        // Loaded accepted starts must never be hidden by a candidate-biome shortcut.
        if(world.hasChunk(chunk.x,chunk.z))return true;
        var generator=world.getChunkSource().getGenerator();
        var context=new Structure.GenerationContext(world.registryAccess(),generator,generator.getBiomeSource(),world.getChunkSource().randomState(),world.getStructureManager(),world.getSeed(),chunk,world,structure.biomes()::contains);
        try(var samples=StructureCoverageService.sampleTerrain(context)){return StructureCoverageService.localCandidates(context,structure).iterator().hasNext();}
    }
    public static net.minecraft.core.BlockPos locate(net.minecraft.world.level.levelgen.structure.StructureStart start){var terrain=start.getPieces().stream().filter(p->p instanceof LandFoundationPiece).findFirst();var box=start.getBoundingBox();if(terrain.isPresent())return new net.minecraft.core.BlockPos((box.minX()+box.maxX())/2,((LandFoundationPiece)terrain.get()).constructionY()+1,(box.minZ()+box.maxZ())/2);return new net.minecraft.core.BlockPos((box.minX()+box.maxX())/2,box.maxY()+1,(box.minZ()+box.maxZ())/2);}
    private StructureCandidates(){}
}
