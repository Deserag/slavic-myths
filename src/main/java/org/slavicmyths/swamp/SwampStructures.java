package org.slavicmyths.swamp;

import java.util.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.structure.*;
import net.minecraft.world.gen.settings.*;
import net.minecraftforge.event.world.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.*;

/** Standard structure starts/references, not a runtime POI scan or retrospective feature. */
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class SwampStructures {
    public static final DeferredRegister<Structure<?>> STRUCTURES = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES,"slavicmyths");
    public static final String[] IDS={"swamp_hut","abandoned_settlement","bog_causeway","flooded_shrine","fishing_camp","underwater_ruins","swamp_remnants"};
    private static final int[] SPACING={24,52,18,48,30,22,10};
    public static final Map<String,RegistryObject<SwampStructure>> TYPES=new LinkedHashMap<>();
    private static final Map<String,StructureFeature<?,?>> CONFIGURED=new HashMap<>();
    public static IStructurePieceType PIECE;
    static { for(String id:IDS) TYPES.put(id,STRUCTURES.register(id,()->new SwampStructure(id))); }
    public static void setup() {
        PIECE=Registry.register(Registry.STRUCTURE_PIECE,new ResourceLocation("slavicmyths","swamp_piece"),SwampPiece::new);
        for(String id:IDS) {
            SwampStructure structure=TYPES.get(id).get();
            Structure.STRUCTURES_REGISTRY.put("slavicmyths:"+id,structure);
            CONFIGURED.put(id,Registry.register(WorldGenRegistries.CONFIGURED_STRUCTURE_FEATURE,new ResourceLocation("slavicmyths",id),structure.configured(NoFeatureConfig.INSTANCE)));
        }
    }
    @SubscribeEvent public static void biome(BiomeLoadingEvent event) {
        if(event.getName()==null || !event.getName().getNamespace().equals("minecraft")) return;
        for(String id:IDS) if(event.getCategory()==Biome.Category.SWAMP ||
                (id.equals("underwater_ruins") && event.getCategory()==Biome.Category.RIVER))
            event.getGeneration().getStructures().add(()->CONFIGURED.get(id));
    }
    @SubscribeEvent public static void world(WorldEvent.Load event) {
        if(!(event.getWorld() instanceof ServerWorld))return;
        ServerWorld world=(ServerWorld)event.getWorld();
        if(!world.dimension().equals(World.OVERWORLD))return;
        DimensionStructuresSettings settings=world.getChunkSource().getGenerator().getSettings();
        Map<Structure<?>,StructureSeparationSettings> map=new HashMap<>(settings.structureConfig());
        for(int i=0;i<IDS.length;i++)map.putIfAbsent(TYPES.get(IDS[i]).get(),new StructureSeparationSettings(SPACING[i],SPACING[i]/2,730031+i*619));
        // Codec-loaded maps can be immutable. Replace a copy once, without changing vanilla entries.
        ObfuscationReflectionHelper.setPrivateValue(DimensionStructuresSettings.class,settings,map,"field_236193_d_");
    }
    /** Called only during feature placement, using the current region's existing references. */
    public static boolean occupied(net.minecraft.world.ISeedReader world,net.minecraft.util.math.BlockPos pos) {
        if(!(world instanceof net.minecraft.world.gen.WorldGenRegion))return false;
        StructureManager manager=world.getLevel().structureFeatureManager().forWorldGenRegion((net.minecraft.world.gen.WorldGenRegion)world);
        for(String id:IDS)if(manager.startsForFeature(net.minecraft.util.math.SectionPos.of(pos),TYPES.get(id).get()).anyMatch(StructureStart::isValid))return true;
        return false;
    }
    private SwampStructures(){}
}
