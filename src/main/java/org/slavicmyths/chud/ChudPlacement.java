package org.slavicmyths.chud;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.structure.*;
import net.minecraft.world.level.levelgen.structure.placement.*;
import org.slavicmyths.worldgen.StructureCoverageService;

/** Seed-only bounded protection of accepted neighboring structure geometry. */
public final class ChudPlacement {
    private static java.util.List<BoundingBox> neighbors(Structure.GenerationContext c,Structure other,net.minecraft.world.level.ChunkPos at){
        long began=System.nanoTime();var result=StructureCoverageService.neighborFootprints(c,other,at);
        if(Boolean.getBoolean("slavicmyths.chudPlacementDiagnostics")&&System.nanoTime()-began>250_000_000L)com.mojang.logging.LogUtils.getLogger().info("Chud protection plan {} {} took {}ms",c.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(other),at,(System.nanoTime()-began)/1_000_000);
        return result;
    }
    private record RingKey(Object generator,Object random,Object registries,long seed,ConcentricRingsStructurePlacement placement){}
    private static final java.util.Map<RingKey,java.util.Map<Integer,net.minecraft.world.level.ChunkPos>> RINGS=new java.util.LinkedHashMap<>(16,.75f,true);
    public static void clearCache(){synchronized(RINGS){RINGS.clear();}}
    /** Exact vanilla RNG/fork order, but biome-adjust only rings that can touch this assembly.
     * Vanilla's global ring future biome-adjusts all 128 sites and must never be joined in worldgen. */
    private static java.util.List<net.minecraft.world.level.ChunkPos> ringsNear(Structure.GenerationContext c,ConcentricRingsStructurePlacement p,BoundingBox box){
        var key=new RingKey(c.chunkGenerator(),c.randomState(),c.registryAccess(),c.seed(),p);
        java.util.Map<Integer,net.minecraft.world.level.ChunkPos> cached;
        synchronized(RINGS){cached=RINGS.computeIfAbsent(key,k->new java.util.concurrent.ConcurrentHashMap<>());while(RINGS.size()>16)RINGS.remove(RINGS.keySet().iterator().next());}
        var out=new java.util.ArrayList<net.minecraft.world.level.ChunkPos>();var random=net.minecraft.util.RandomSource.create(c.seed());
        double angle=random.nextDouble()*Math.PI*2;int spread=p.spread(),inRing=0,ring=0;
        for(int i=0;i<p.count();i++){
            ChudSearchBudget.check();double distance=4.*p.distance()+p.distance()*ring*6.+(random.nextDouble()-.5)*p.distance()*2.5;
            int x=(int)Math.round(Math.cos(angle)*distance),z=(int)Math.round(Math.sin(angle)*distance);var fork=random.fork();
            // Up to seven chunks biome displacement plus eight chunks of native geometry.
            if(x>=(box.minX()>>4)-15&&x<=(box.maxX()>>4)+15&&z>=(box.minZ()>>4)-15&&z<=(box.maxZ()>>4)+15){
                var at=cached.get(i);if(at==null){var found=c.biomeSource().findBiomeHorizontal(x*16+8,0,z*16+8,112,p.preferredBiomes()::contains,fork,c.randomState().sampler());at=found==null?new net.minecraft.world.level.ChunkPos(x,z):new net.minecraft.world.level.ChunkPos(found.getFirst());cached.put(i,at);}out.add(at);
            }
            angle+=Math.PI*2/spread;if(++inRing==spread){ring++;inRing=0;spread+=2*spread/(ring+1);spread=Math.min(spread,p.count()-i);angle+=random.nextDouble()*Math.PI*2;}
        }
        return out;
    }
    /** Check the complete new-start assembly against accepted, acyclic neighbor plans. */
    public static boolean clearFull(Structure.GenerationContext c,StructurePiece root,ChudSurfaceAccess access){
        var proposed=new java.util.ArrayList<StructurePiece>();proposed.add(root);if(access!=null)proposed.add(new ChudAccessPiece(access));
        if(!StructureCoverageService.referencesFit(c.chunkPos(),proposed))return false;
        var fills=new java.util.ArrayList<ChudGeometry.Fill>();
        if(root instanceof ChudPiece city)fills.addAll(city.blueprint());else fills.addAll(((AbandonedPiece)root).blueprint());
        if(access!=null)fills.addAll(access.blueprint());
        // Empty corners of the old 255x255 accounting box are not excavation.
        // Check every touched column, including AIR destinations and the surface annex.
        var columns=new java.util.HashMap<Long,BoundingBox>();
        for(var fill:fills){var b=fill.box();for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++){
            var clipped=new BoundingBox(Math.max(x*16,b.minX()),b.minY(),Math.max(z*16,b.minZ()),Math.min(x*16+15,b.maxX()),b.maxY(),Math.min(z*16+15,b.maxZ()));
            columns.merge(net.minecraft.world.level.ChunkPos.asLong(x,z),clipped,(a,d)->BoundingBox.encapsulatingBoxes(java.util.List.of(a,d)).orElseThrow());
        }}
        if(root instanceof ChudPiece city&&city.repairsRoof())city.roofCover().protect(columns);else if(root instanceof AbandonedPiece abandoned&&abandoned.repairsRoof())abandoned.roofCover().protect(columns);
        if(ChudRegionArchive.protectedRecords(c,columns.values()))return false;
        var bounds=BoundingBox.encapsulatingBoxes(columns.values()).orElseThrow();var structures=c.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for(var set:c.registryAccess().registryOrThrow(Registries.STRUCTURE_SET))if(set.placement() instanceof RandomSpreadStructurePlacement p){
                if(set.structures().stream().allMatch(e->{var id=structures.getKey(e.structure().value());return id!=null&&id.getNamespace().equals("slavicmyths")&&(id.getPath().equals("chud_city")||id.getPath().equals("chud_abandoned"));}))continue;
                if(set.structures().stream().noneMatch(e->c.biomeSource().possibleBiomes().stream().anyMatch(e.structure().value().biomes()::contains)))continue;
                int minX=(bounds.minX()>>4)-8,maxX=(bounds.maxX()>>4)+8,minZ=(bounds.minZ()>>4)-8,maxZ=(bounds.maxZ()>>4)+8;
                for(int gx=Math.floorDiv(minX,p.spacing());gx<=Math.floorDiv(maxX,p.spacing());gx++)for(int gz=Math.floorDiv(minZ,p.spacing());gz<=Math.floorDiv(maxZ,p.spacing());gz++){
                    ChudSearchBudget.check();var at=p.getPotentialStructureChunk(c.seed(),gx*p.spacing(),gz*p.spacing());
                    if(at.x<minX||at.x>maxX||at.z<minZ||at.z>maxZ||!p.applyAdditionalChunkRestrictions(at.x,at.z,c.seed()))continue;
                    var chosen=selectedNeighbor(c,set,at);
                    for(var existing:chosen.boxes())for(var column:columns.values())if(column.intersects(existing)){
                        StructureCoverageService.note(StructureCoverageService.Rejection.PROTECTED_STRUCTURE,"chudConflict:"+chosen.id(),1);
                        com.mojang.logging.LogUtils.getLogger().info("Chud candidate {} rejected: protected {} at {}",c.chunkPos(),chosen.id(),at);return false;
                    }
                }
        }
        return clear(c,BoundingBox.encapsulatingBoxes(proposed.stream().map(StructurePiece::getBoundingBox).toList()).orElseThrow());
    }
    public record Neighbor(net.minecraft.resources.ResourceLocation id,java.util.List<BoundingBox> boxes){}
    /** Same weighted-without-replacement retry order as the pinned ChunkGenerator.createStructures. */
    public static Neighbor selectedNeighbor(Structure.GenerationContext c,StructureSet set,net.minecraft.world.level.ChunkPos at){
        var entries=new java.util.ArrayList<>(set.structures());boolean singleton=entries.size()==1;
        var random=new net.minecraft.world.level.levelgen.WorldgenRandom(new net.minecraft.world.level.levelgen.LegacyRandomSource(0));random.setLargeFeatureSeed(c.seed(),at.x,at.z);
        int total=entries.stream().mapToInt(StructureSet.StructureSelectionEntry::weight).sum();
        while(!entries.isEmpty()){
            ChudSearchBudget.check();int draw=singleton?0:random.nextInt(total),index=0;
            for(var entry:entries){draw-=entry.weight();if(draw<0)break;index++;}
            var entry=entries.get(index);var other=entry.structure().value();var id=c.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(other);
            if(id!=null&&id.getNamespace().equals("slavicmyths")&&(id.getPath().equals("chud_city")||id.getPath().equals("chud_abandoned")))throw new IllegalStateException("Recursive mixed Chud structure set");
            var boxes=neighbors(c,other,at);if(!boxes.isEmpty())return new Neighbor(id,boxes);
            entries.remove(index);total-=entry.weight();
        }
        return new Neighbor(null,java.util.List.of());
    }
    public static boolean clear(Structure.GenerationContext c,ChudPlan plan){
        return clear(c,plan.bounds());
    }
    public static boolean clear(Structure.GenerationContext c,BoundingBox bounds){
        var structures=c.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var protectedBox=new BoundingBox(bounds.minX()-16,bounds.minY()-16,bounds.minZ()-16,bounds.maxX()+16,bounds.maxY()+16,bounds.maxZ()+16);
        for(var set:c.registryAccess().registryOrThrow(Registries.STRUCTURE_SET)){
            for(var entry:set.structures()){
                var other=entry.structure().value();var id=structures.getKey(other);if(id==null||!(id.getNamespace().equals("minecraft")&&id.getPath().equals("stronghold")||id.getNamespace().equals("slavicmyths")&&id.getPath().equals("serpent_hill")))continue;
                if(c.biomeSource().possibleBiomes().stream().noneMatch(other.biomes()::contains))continue;
                if(set.placement() instanceof RandomSpreadStructurePlacement p){
                    int minX=(bounds.minX()>>4)-8,maxX=(bounds.maxX()>>4)+8,minZ=(bounds.minZ()>>4)-8,maxZ=(bounds.maxZ()>>4)+8;
                    for(int gx=Math.floorDiv(minX,p.spacing());gx<=Math.floorDiv(maxX,p.spacing());gx++)for(int gz=Math.floorDiv(minZ,p.spacing());gz<=Math.floorDiv(maxZ,p.spacing());gz++){
                        var q=p.getPotentialStructureChunk(c.seed(),gx*p.spacing(),gz*p.spacing());
                        if(q.x<minX||q.x>maxX||q.z<minZ||q.z>maxZ||!p.applyAdditionalChunkRestrictions(q.x,q.z,c.seed()))continue;
                        // Only strongholds reserve a protected footprint.
                        // Resolve their accepted geometry; rejected cells reserve no fictitious footprint.
                        for(var b:neighbors(c,other,q))if(protectedBox.intersects(b)){StructureCoverageService.note(StructureCoverageService.Rejection.PROTECTED_STRUCTURE,"chudConflict:"+id,1);return false;}
                    }
                }else if(set.placement() instanceof ConcentricRingsStructurePlacement p){
                    var positions=ringsNear(c,p,protectedBox);
                    for(var q:positions)if(Math.abs(q.x-c.chunkPos().x)<=24&&Math.abs(q.z-c.chunkPos().z)<=24)for(var b:neighbors(c,other,q))if(protectedBox.intersects(b)){StructureCoverageService.note(StructureCoverageService.Rejection.PROTECTED_STRUCTURE,"chudConflict:"+id,1);return false;}
                }else {StructureCoverageService.note(StructureCoverageService.Rejection.PROTECTED_STRUCTURE,"chudUnknownPlacement:"+id,1);return false;} // Unknown third-party placement cannot be checked safely.
            }
        }
        return true;
    }
    private ChudPlacement(){}
}
