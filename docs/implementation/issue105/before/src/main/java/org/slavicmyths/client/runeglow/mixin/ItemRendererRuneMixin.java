package org.slavicmyths.client.runeglow.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.slavicmyths.client.runeglow.RuneGlowBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererRuneMixin {
    @ModifyVariable(method="render", at=@At("HEAD"), argsOnly=true, ordinal=0)
    private MultiBufferSource slavicmyths$runeItem(MultiBufferSource source, ItemStack stack,
            ItemDisplayContext context, boolean left, PoseStack pose, MultiBufferSource original,
            int light, int overlay, BakedModel model) {
        return RuneGlowBuffers.item(stack,source,pose);
    }
}
