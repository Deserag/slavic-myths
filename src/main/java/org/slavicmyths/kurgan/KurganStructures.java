package org.slavicmyths.kurgan;
import java.util.*;
import net.minecraft.util.*;
import net.minecraft.util.registry.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.settings.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.*;
import org.slavicmyths.swamp.SwampStructures;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class KurganStructures {
 public static final RegistryObject<KurganStructure> SMALL=SwampStructures.STRUCTURES.register("kurgan_small",()->new KurganStructure(0));
 public static final RegistryObject<KurganStructure> WARRIOR=SwampStructures.STRUCTURES.register("kurgan_warrior",()->new KurganStructure(1));
 public static final RegistryObject<KurganStructure> GREAT=SwampStructures.STRUCTURES.register("kurgan_great",()->new KurganStructure(2));
 public static IStructurePieceType PIECE,DUNGEON;private static final List<StructureFeature<?,?>> configured=new ArrayList<>();
 public static void register(){}
 public static KurganStructure type(int i){return i==0?SMALL.get():i==1?WARRIOR.get():GREAT.get();}
 public static void setup(){DUNGEON=Registry.register(Registry.STRUCTURE_PIECE,new ResourceLocation("slavicmyths","kurgan_dungeon"),KurganDungeonPiece::new);PIECE=Registry.register(Registry.STRUCTURE_PIECE,new ResourceLocation("slavicmyths","kurgan_shell"),KurganPiece::new);for(int i=0;i<3;i++){KurganStructure s=type(i);Structure.STRUCTURES_REGISTRY.put(s.getRegistryName().toString(),s);configured.add(Registry.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE,s.getRegistryName(),s.configured(NoFeatureConfig.INSTANCE)));}}
 @SubscribeEvent public static void biome(BiomeLoadingEvent e){if(e.getName()!=null&&e.getName().getNamespace().equals("minecraft")&&(e.getCategory()==Biome.Category.PLAINS||e.getName().getPath().equals("birch_forest")))for(StructureFeature<?,?> s:configured)e.getGeneration().getStructures().add(()->s);}
 @SubscribeEvent public static void world(WorldEvent.Load e){if(!(e.getWorld()instanceof ServerWorld))return;ServerWorld w=(ServerWorld)e.getWorld();if(!w.dimension().equals(World.OVERWORLD))return;DimensionStructuresSettings settings=w.getChunkSource().getGenerator().getSettings();Map<Structure<?>,StructureSeparationSettings> map=new HashMap<>(settings.structureConfig());for(int i=0;i<3;i++)map.putIfAbsent(type(i),new StructureSeparationSettings(64,24,841973));ObfuscationReflectionHelper.setPrivateValue(DimensionStructuresSettings.class,settings,map,"field_236193_d_");}
}
