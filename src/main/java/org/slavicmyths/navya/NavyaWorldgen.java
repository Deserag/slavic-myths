package org.slavicmyths.navya;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class NavyaWorldgen {
    public static final ResourceKey<Biome> BIOME=ResourceKey.create(Registries.BIOME,id("navya_grove"));
    public static final DeferredHolder<StructureType<?>,StructureType<NavyaHeartStructure>> HEART=SwampStructures.STRUCTURES.register("navya_heart",()->()->NavyaHeartStructure.CODEC);
    public static final DeferredHolder<net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType,net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType> PIECE=SwampStructures.PIECES.register("navya_heart",()->(context,tag)->new NavyaHeartPiece(tag));
    public static ResourceLocation id(String name){return ResourceLocation.fromNamespaceAndPath("slavicmyths",name);}
    public static boolean protectedChunk(WorldGenLevel world,BlockPos pos){
        for(var entry:world.getChunk(pos).getAllReferences().entrySet()) {
            var key=world.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(entry.getKey());
            if(!entry.getValue().isEmpty() && (key==null || !key.equals(id("navya_heart"))))return true;
        }
        return false;
    }
    public static boolean heartChunk(WorldGenLevel world,BlockPos pos){
        for(var entry:world.getChunk(pos).getAllReferences().entrySet())
            if(!entry.getValue().isEmpty() && id("navya_heart").equals(world.registryAccess().registryOrThrow(Registries.STRUCTURE).getKey(entry.getKey())))return true;
        return false;
    }
    @net.neoforged.bus.api.SubscribeEvent public static void loaded(net.neoforged.neoforge.event.server.ServerAboutToStartEvent event){
        var registries=event.getServer().registryAccess();
        var biome=registries.registryOrThrow(Registries.BIOME).getHolder(BIOME);
        var preset=registries.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).get(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.NETHER);
        if(biome.isPresent()&&preset!=null)((NavyaPresetAccess)(Object)preset).slavicmyths$append(biome.get());
    }
    public static void init(){ }
    private NavyaWorldgen(){ }
}
