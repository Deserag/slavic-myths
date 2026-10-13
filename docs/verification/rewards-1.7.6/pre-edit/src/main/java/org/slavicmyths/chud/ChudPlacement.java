package org.slavicmyths.chud;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.*;
import org.slavicmyths.worldgen.StructureCoverageService;

/** Seed-only bounded stronghold protection; other structures yield to excavation. */
public final class ChudPlacement {
    public static boolean clear(Structure.GenerationContext c,ChudPlan plan){
        return clear(c,plan.bounds());
    }
    public static boolean clear(Structure.GenerationContext c,BoundingBox bounds){
        var structures=c.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var protectedBox=new BoundingBox(bounds.minX()-16,bounds.minY()-16,bounds.minZ()-16,bounds.maxX()+16,bounds.maxY()+16,bounds.maxZ()+16);
        ChunkGeneratorStructureState rings=null;
        for(var set:c.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)){
            for(var entry:set.structures()){
                var other=entry.structure().value();var id=structures.getKey(other);if(id==null||!id.getNamespace().equals("minecraft")||!id.getPath().equals("stronghold"))continue;
                if(c.biomeSource().possibleBiomes().stream().noneMatch(other.biomes()::contains))continue;
                if(set.placement() instanceof RandomSpreadStructurePlacement p){
                    int minX=(bounds.minX()>>4)-8,maxX=(bounds.maxX()>>4)+8,minZ=(bounds.minZ()>>4)-8,maxZ=(bounds.maxZ()>>4)+8;
                    for(int gx=Math.floorDiv(minX,p.spacing());gx<=Math.floorDiv(maxX,p.spacing());gx++)for(int gz=Math.floorDiv(minZ,p.spacing());gz<=Math.floorDiv(maxZ,p.spacing());gz++){
                        var q=p.getPotentialStructureChunk(c.seed(),gx*p.spacing(),gz*p.spacing());
                        if(q.x<minX||q.x>maxX||q.z<minZ||q.z>maxZ||!p.applyAdditionalChunkRestrictions(q.x,q.z,c.seed()))continue;
                        var biome=c.biomeSource().getNoiseBiome((q.x<<2)+2,16,(q.z<<2)+2,c.randomState().sampler());if(!other.biomes().contains(biome))continue;
                        // Only strongholds reserve a protected footprint.
                        // Resolve their accepted geometry; rejected cells reserve no fictitious footprint.
                        for(var b:StructureCoverageService.neighborFootprints(c,other,q))if(protectedBox.intersects(b)){StructureCoverageService.note(StructureCoverageService.Rejection.PROTECTED_STRUCTURE,"chudConflict:"+id,1);return false;}
                    }
                }else if(set.placement() instanceof ConcentricRingsStructurePlacement p){
                    if(rings==null)rings=ChunkGeneratorStructureState.createForNormal(c.randomState(),c.seed(),c.biomeSource(),c.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET));
                    var positions=rings.getRingPositionsFor(p);if(positions==null)continue;
                    for(var q:positions)if(Math.abs(q.x-c.chunkPos().x)<=24&&Math.abs(q.z-c.chunkPos().z)<=24)for(var b:StructureCoverageService.neighborFootprints(c,other,q))if(protectedBox.intersects(b)){StructureCoverageService.note(StructureCoverageService.Rejection.PROTECTED_STRUCTURE,"chudConflict:"+id,1);return false;}
                }else {StructureCoverageService.note(StructureCoverageService.Rejection.PROTECTED_STRUCTURE,"chudUnknownPlacement:"+id,1);return false;} // Unknown third-party placement cannot be checked safely.
            }
        }
        return true;
    }
    private ChudPlacement(){}
}
