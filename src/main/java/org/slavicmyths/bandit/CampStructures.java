package org.slavicmyths.bandit;
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
public final class CampStructures {
    public static final RegistryObject<CampStructure> SMALL=SwampStructures.STRUCTURES.register("bandit_camp_small",()->new CampStructure(false));
    public static final RegistryObject<CampStructure> MEDIUM=SwampStructures.STRUCTURES.register("bandit_camp_medium",()->new CampStructure(true));
    public static final RegistryObject<LargeCampStructure> LARGE=SwampStructures.STRUCTURES.register("bandit_camp_large",LargeCampStructure::new);
    public static IStructurePieceType LARGE_PIECE;private static StructureFeature<?,?> large;
    public static IStructurePieceType PIECE;private static StructureFeature<?,?> small,medium;
    public static void register(){}
    public static void setup(){
        LARGE_PIECE=Registry.register(Registry.STRUCTURE_PIECE,new ResourceLocation("slavicmyths","large_camp_piece"),LargeCampPiece::new);
        Structure.STRUCTURES_REGISTRY.put("slavicmyths:bandit_camp_large",LARGE.get());
        large=Registry.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE,new ResourceLocation("slavicmyths","bandit_camp_large"),LARGE.get().configured(NoFeatureConfig.INSTANCE));
        PIECE=Registry.register(Registry.STRUCTURE_PIECE,new ResourceLocation("slavicmyths","bandit_camp_piece"),CampPiece::new);
        Structure.STRUCTURES_REGISTRY.put("slavicmyths:bandit_camp_small",SMALL.get());Structure.STRUCTURES_REGISTRY.put("slavicmyths:bandit_camp_medium",MEDIUM.get());
        small=Registry.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE,new ResourceLocation("slavicmyths","bandit_camp_small"),SMALL.get().configured(NoFeatureConfig.INSTANCE));
        medium=Registry.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE,new ResourceLocation("slavicmyths","bandit_camp_medium"),MEDIUM.get().configured(NoFeatureConfig.INSTANCE));
    }
    @SubscribeEvent public static void biome(BiomeLoadingEvent e){if(e.getName()!=null&&e.getName().getNamespace().equals("minecraft")&&(e.getCategory()==Biome.Category.PLAINS||e.getCategory()==Biome.Category.FOREST||e.getCategory()==Biome.Category.TAIGA)){e.getGeneration().getStructures().add(()->large);e.getGeneration().getStructures().add(()->small);e.getGeneration().getStructures().add(()->medium);}}
    @SubscribeEvent public static void world(WorldEvent.Load e){if(!(e.getWorld() instanceof ServerWorld))return;ServerWorld w=(ServerWorld)e.getWorld();if(!w.dimension().equals(World.OVERWORLD))return;
        DimensionStructuresSettings settings=w.getChunkSource().getGenerator().getSettings();Map<Structure<?>,StructureSeparationSettings> copy=new HashMap<>(settings.structureConfig());
        copy.putIfAbsent(LARGE.get(),new StructureSeparationSettings(192,64,813797));
        copy.putIfAbsent(SMALL.get(),new StructureSeparationSettings(48,16,803701));copy.putIfAbsent(MEDIUM.get(),new StructureSeparationSettings(64,24,803729));
        ObfuscationReflectionHelper.setPrivateValue(DimensionStructuresSettings.class,settings,copy,"field_236193_d_");}
}
