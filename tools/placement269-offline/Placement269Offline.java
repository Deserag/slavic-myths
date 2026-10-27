package org.slavicmyths.verify;
import java.util.*;import com.mojang.serialization.Lifecycle;
import net.minecraft.core.*;import net.minecraft.core.registries.Registries;import net.minecraft.resources.*;import net.minecraft.world.level.*;import net.minecraft.world.level.biome.*;import net.minecraft.world.level.chunk.*;import net.minecraft.world.level.levelgen.*;import net.minecraft.world.level.levelgen.structure.*;import net.minecraft.world.level.levelgen.structure.pieces.*;import net.minecraft.world.level.levelgen.structure.placement.*;import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slavicmyths.worldgen.*;
/** Native placement predicates and production collision code with metadata probes. Zero game/world/server. */
public final class Placement269Offline {
 static int checks;
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);checks++;}
 static final class Box extends StructurePiece {
  Box(){super(StructurePieceType.STRONGHOLD_FILLER_CORRIDOR,0,new BoundingBox(0,0,0,15,10,15));}
  protected void addAdditionalSaveData(StructurePieceSerializationContext c,net.minecraft.nbt.CompoundTag n){}
  public void postProcess(WorldGenLevel w,StructureManager m,ChunkGenerator g,net.minecraft.util.RandomSource r,BoundingBox b,ChunkPos c,BlockPos p){throw new AssertionError("No world placement allowed in offline check");}
 }
 static final class Neighbor extends Structure {
  int calls;Neighbor(Holder<Biome> biome){super(new StructureSettings(HolderSet.direct(biome)));}
  public StructureType<?> type(){return StructureType.MINESHAFT;}
  public Optional<GenerationStub> findGenerationPoint(GenerationContext c){throw new AssertionError("Metadata generate override only");}
  public StructureStart generate(RegistryAccess a,ChunkGenerator g,BiomeSource b,RandomState r,StructureTemplateManager t,long seed,ChunkPos pos,int refs,LevelHeightAccessor h,java.util.function.Predicate<Holder<Biome>> biomes){calls++;return new StructureStart(this,pos,0,new PiecesContainer(List.of(new Box())));}
 }
 static void scenario(float frequency,long seed){
  var biome=Holder.direct(new Biome.BiomeBuilder().hasPrecipitation(true).temperature(.8F).downfall(.4F).specialEffects(new BiomeSpecialEffects.Builder().fogColor(0).waterColor(0).waterFogColor(0).skyColor(0).build()).mobSpawnSettings(MobSpawnSettings.EMPTY).generationSettings(BiomeGenerationSettings.EMPTY).build());
  var other=new Neighbor(biome);var structures=new MappedRegistry<Structure>(Registries.STRUCTURE,Lifecycle.stable());var holder=structures.register(ResourceKey.create(Registries.STRUCTURE,ResourceLocation.fromNamespaceAndPath("slavicmyths","collision_probe")),other,RegistrationInfo.BUILT_IN);structures.freeze();
  var placement=new RandomSpreadStructurePlacement(Vec3i.ZERO,StructurePlacement.FrequencyReductionMethod.DEFAULT,frequency,269,Optional.empty(),16,15,RandomSpreadType.LINEAR);
  var sets=new MappedRegistry<StructureSet>(Registries.STRUCTURE_SET,Lifecycle.stable());sets.register(ResourceKey.create(Registries.STRUCTURE_SET,ResourceLocation.fromNamespaceAndPath("slavicmyths","probe_set")),new StructureSet(holder,placement),RegistrationInfo.BUILT_IN);sets.freeze();
  var registries=new RegistryAccess.ImmutableRegistryAccess(List.of(structures,sets));var context=new Structure.GenerationContext(registries,null,new FixedBiomeSource(biome),null,null,seed,new ChunkPos(0,0),LevelHeightAccessor.create(-64,384),biome::equals);
  StructureCoverageService.clearFootprints();boolean active=placement.applyAdditionalChunkRestrictions(0,0,seed);boolean clear=StructureCandidates.clearAccepted(context,new ChunkPos(0,0),id->1,List.of(new Box()));
  check(clear==!active,"frequency="+frequency+" seed="+seed+" native_active="+active+" collision_clear="+clear+" raw_neighbor_calls="+other.calls);
  check(other.calls==(active?1:0),"disabled native candidate must not be planned; frequency="+frequency+" calls="+other.calls);
 }
 public static void main(String[] args){net.neoforged.fml.loading.LoadingModList.of(List.of(),List.of(),List.of(),List.of(),Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();scenario(0,269);scenario(1,269);for(int i=0;i<512;i++)scenario(.2F,i);System.out.println("PLACEMENT269_OFFLINE PASS checks="+checks+"; production collision/predicate integration, metadata probes only; game/world/server/client launches=0; natural generation NOT_RUN");}
}
