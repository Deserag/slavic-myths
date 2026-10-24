package org.slavicmyths.client.chudlight;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.level.chunk.LightChunkGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(LightEngine.class)
public interface LightEngineAccess { @Accessor("chunkSource") LightChunkGetter slavicmyths$source(); }
