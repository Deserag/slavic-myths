package org.slavicmyths.navya;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.*;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.slavicmyths.swamp.SwampStructures;
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
    public static void init(){ }
    private NavyaWorldgen(){ }
}
