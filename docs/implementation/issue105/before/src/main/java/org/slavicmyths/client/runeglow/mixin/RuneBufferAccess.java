package org.slavicmyths.client.runeglow.mixin;

import java.util.SequencedMap;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Read-only check: do not attach native buffers to arbitrary third-party sources. */
@Mixin(MultiBufferSource.BufferSource.class)
public interface RuneBufferAccess {
    @Accessor("fixedBuffers")
    SequencedMap<RenderType,ByteBufferBuilder> slavicmyths$runeFixedBuffers();
}
