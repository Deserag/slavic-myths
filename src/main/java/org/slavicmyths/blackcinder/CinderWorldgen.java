package org.slavicmyths.blackcinder;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.registry.*;
import org.slavicmyths.swamp.SwampStructures;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class CinderWorldgen {
 public static ResourceLocation id(String path){return ResourceLocation.fromNamespaceAndPath("slavicmyths",path);}
 public static final ResourceKey<Biome> BIOME=ResourceKey.create(Registries.BIOME,id("black_cinder"));
 public static final DeferredHolder<Feature<?>,CinderTerrain> TERRAIN=ModFeatures.FEATURES.register("black_cinder_terrain",()->new CinderTerrain(false));
 public static final DeferredHolder<Feature<?>,CinderTerrain> NATURE=ModFeatures.FEATURES.register("black_cinder_nature",()->new CinderTerrain(true));
 public static final DeferredHolder<StructureType<?>,StructureType<CinderArenaStructure>> ARENA=SwampStructures.STRUCTURES.register("zharenitsa_arena",()->()->CinderArenaStructure.CODEC);
 public static final DeferredHolder<net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType,net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType> PIECE=SwampStructures.PIECES.register("zharenitsa_arena",()->(c,t)->new CinderArenaPiece(t));
 public static final DeferredHolder<EntityType<?>,EntityType<CinderCoal>> COAL=ModEntities.ENTITIES.register("cinder_coal",()->EntityType.Builder.<CinderCoal>of(CinderCoal::new,MobCategory.MISC).sized(.35F,.35F).fireImmune().clientTrackingRange(10).updateInterval(1).build("slavicmyths:cinder_coal"));
 public static final DeferredHolder<EntityType<?>,EntityType<CinderSpike>> SPIKE=ModEntities.ENTITIES.register("cinder_ash_spike",()->EntityType.Builder.<CinderSpike>of(CinderSpike::new,MobCategory.MISC).sized(.8F,2F).fireImmune().clientTrackingRange(10).updateInterval(3).build("slavicmyths:cinder_ash_spike"));
 @net.neoforged.bus.api.SubscribeEvent public static void loaded(net.neoforged.neoforge.event.server.ServerAboutToStartEvent e){var registry=e.getServer().registryAccess();var holder=registry.registryOrThrow(Registries.BIOME).getHolder(BIOME);var preset=registry.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).get(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.NETHER);if(holder.isPresent()&&preset!=null)((org.slavicmyths.navya.NavyaPresetAccess)(Object)preset).slavicmyths$append(holder.get());}
 public static void init(net.neoforged.bus.api.IEventBus bus){CinderSounds.init();bus.addListener((net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent e)->{
  for(var k:new CinderContent.Kind[]{CinderContent.Kind.COAL_CRAWLER,CinderContent.Kind.ASH_SPIRIT,CinderContent.Kind.PEKELNIK,CinderContent.Kind.SPARKLING})e.register(CinderContent.TYPES.get(k).get(),k==CinderContent.Kind.ASH_SPIRIT||k==CinderContent.Kind.SPARKLING?SpawnPlacementTypes.NO_RESTRICTIONS:SpawnPlacementTypes.ON_GROUND,net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,l,r,p,random)->l.getBiome(p).is(BIOME)&&l.isEmptyBlock(p)&&l.isEmptyBlock(p.above())&&(k==CinderContent.Kind.SPARKLING||l.getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL)&&p.getY()>=33&&p.getY()<=100&&l.getFluidState(p).isEmpty()&&(k==CinderContent.Kind.SPARKLING||Mob.checkMobSpawnRules(t,l,r,p,random)),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
 });}
 private CinderWorldgen(){}
}
