package org.slavicmyths.navya.mixin;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.slavicmyths.burningislands.OceanTerrain;
@Mixin(NoiseBasedChunkGenerator.class)
public abstract class OceanGeneratorMixin implements OceanTerrain.GeneratorAccess {
 @Shadow @Final private net.minecraft.core.Holder<NoiseGeneratorSettings> settings;
 @Unique private RandomState slavicmyths$state;@Unique private OceanTerrain slavicmyths$ocean;
 public synchronized OceanTerrain slavicmyths$ocean(RandomState state){if(state!=slavicmyths$state){slavicmyths$state=state;slavicmyths$ocean=new OceanTerrain((NoiseBasedChunkGenerator)(Object)this,state,((OceanTerrain.SeedAccess)(Object)state).slavicmyths$seed(),settings.value());((OceanTerrain.SamplerAccess)(Object)state.sampler()).slavicmyths$ocean(slavicmyths$ocean);}return slavicmyths$ocean;}
 @Redirect(method="doCreateBiomes",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/chunk/ChunkAccess;fillBiomesFromNoise(Lnet/minecraft/world/level/biome/BiomeResolver;Lnet/minecraft/world/level/biome/Climate$Sampler;)V"))
 private void slavicmyths$sample(ChunkAccess chunk,BiomeResolver resolver,Climate.Sampler sampler,Blender blender,RandomState state,StructureManager structures,ChunkAccess original){((OceanTerrain.SamplerAccess)(Object)sampler).slavicmyths$ocean(slavicmyths$ocean(state));chunk.fillBiomesFromNoise(resolver,sampler);}
}
