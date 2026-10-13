package org.slavicmyths.chud;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.slavicmyths.worldgen.*;

/** Immutable seed plans only. Bounded cache; no chunks, ticks, or mutable placed pieces. */
public final class ChudRegionPlanner {
    private static final ThreadLocal<Boolean> RAW=new ThreadLocal<>();
    private static final ThreadLocal<Map<Long,net.minecraft.world.level.NoiseColumn>> COLUMNS=new ThreadLocal<>();
    static net.minecraft.world.level.NoiseColumn column(Structure.GenerationContext c,int x,int z) {
        var columns=COLUMNS.get();long key=BlockPos.asLong(x,0,z);
        if(columns!=null){var saved=columns.get(key);if(saved!=null)return saved;}
        var result=c.chunkGenerator().getBaseColumn(x,z,c.heightAccessor(),c.randomState());
        if(columns!=null&&columns.size()<8192)columns.put(key,result);
        return result;
    }
    private record Key(Object generator,Object random,Object registries,Object templates,long seed,int x,int z,int min,int height,double attached) {}
    public record Selected(ChunkPos owner,BlockPos surface,ChudPlan city,AbandonedPlan abandoned) {
        Optional<Structure.GenerationStub> stub(ChunkPos requested,boolean wantCity) {
            if(!owner.equals(requested)||(city!=null)!=wantCity)return Optional.empty();
            return Optional.of(new Structure.GenerationStub(surface,b->{
                if(city!=null)b.addPiece(new ChudPiece(city,abandoned));else b.addPiece(new AbandonedPiece(abandoned));
            }));
        }
    }
    private static final Map<Key,Optional<Selected>> CACHE=new LinkedHashMap<>(128,.75f,true);
    private record Slot(Key region,long chunk) {}
    private static final Map<Slot,Optional<Selected>> SLOTS=new LinkedHashMap<>(1024,.75f,true);
    private static long epoch;
    public static void clear() { synchronized(CACHE){epoch++;CACHE.clear();SLOTS.clear();} }
    public static boolean raw() { return Boolean.TRUE.equals(RAW.get()); }
    public static ChudRegionPlacement placement(Structure.GenerationContext c) {
        var set=c.registryAccess().registryOrThrow(Registries.STRUCTURE_SET).get(ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));
        return set!=null&&set.placement() instanceof ChudRegionPlacement p?p:null;
    }
    public static Optional<Structure.GenerationStub> find(Structure.GenerationContext c,boolean city) {
        return select(c).flatMap(p->p.stub(c.chunkPos(),city));
    }
    public static Optional<Selected> select(Structure.GenerationContext c) {
        var placement=placement(c);if(placement==null)return Optional.empty();
        var key=new Key(c.chunkGenerator(),c.randomState(),c.registryAccess(),c.structureTemplateManager(),c.seed(),
            Math.floorDiv(c.chunkPos().x,placement.spacing()),Math.floorDiv(c.chunkPos().z,placement.spacing()),
            c.heightAccessor().getMinBuildHeight(),c.heightAccessor().getHeight(),AbandonedConfig.ATTACHED_CHANCE.get());
        long currentEpoch;
        synchronized(CACHE){var saved=CACHE.get(key);if(saved!=null)return saved;currentEpoch=epoch;}
        var structures=c.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var city=structures.get(ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_city"));
        var abandoned=structures.get(ResourceLocation.fromNamespaceAndPath("slavicmyths","chud_abandoned"));
        var candidates=new ArrayList<>(placement.candidates(c.seed(),c.chunkPos().x,c.chunkPos().z));
        var terrain=new ChudStructure.Terrain(c);
        var heights=new HashMap<ChunkPos,Integer>();
        for(var p:candidates){ChudSearchBudget.check();heights.put(p,terrain.surface(p.getMiddleBlockX(),p.getMiddleBlockZ()));}
        // Keep old owning slot first. Rank retry slots by mountain preference, then actual surface height.
        candidates.subList(1,candidates.size()).sort(Comparator.<ChunkPos>comparingInt(p->{
            var biome=c.biomeSource().getNoiseBiome((p.x<<2)+2,heights.get(p)>>2,(p.z<<2)+2,c.randomState().sampler());
            return biome.is(BiomeTags.IS_MOUNTAIN)?1:0;
        }).reversed().thenComparing(Comparator.comparingInt((ChunkPos p)->heights.get(p)).reversed()));
        Optional<Selected> result=Optional.empty();
        RAW.set(true);
        COLUMNS.set(new HashMap<>());
        try(var samples=StructureCoverageService.sampleTerrain(c)) {
            outer:for(var owner:candidates) {
                ChudSearchBudget.check();
                var slot=new Slot(key,owner.toLong());
                Optional<Selected> saved;
                synchronized(CACHE){saved=SLOTS.get(slot);}
                if(saved!=null){if(saved.isPresent()){result=saved;break;}continue;}
                var local=new Structure.GenerationContext(c.registryAccess(),c.chunkGenerator(),c.biomeSource(),c.randomState(),
                    c.structureTemplateManager(),c.seed(),owner,c.heightAccessor(),c.validBiome());
                for(var type:Arrays.asList(city,abandoned)) {
                    if(type==null)continue;
                    var start=StructureCoverageService.neighborPlan(local,type,owner);
                    if(!start.isValid())continue;
                    for(var piece:start.getPieces()) {
                        if(piece instanceof ChudPiece a)result=Optional.of(new Selected(owner,new BlockPos(owner.getMiddleBlockX(),heights.get(owner),owner.getMiddleBlockZ()),a.plan,a.abandoned));
                        else if(piece instanceof AbandonedPiece b)result=Optional.of(new Selected(owner,new BlockPos(owner.getMiddleBlockX(),heights.get(owner),owner.getMiddleBlockZ()),null,b.plan));
                    }
                    if(result.isPresent())break;
                }
                synchronized(CACHE){if(epoch==currentEpoch){SLOTS.put(slot,result);while(SLOTS.size()>1024)SLOTS.remove(SLOTS.keySet().iterator().next());}}
                if(result.isPresent())break outer;
            }
        } finally { RAW.remove();COLUMNS.remove(); }
        synchronized(CACHE){if(epoch==currentEpoch){CACHE.put(key,result);while(CACHE.size()>128)CACHE.remove(CACHE.keySet().iterator().next());}}
        return result;
    }
    private ChudRegionPlanner() {}
}
