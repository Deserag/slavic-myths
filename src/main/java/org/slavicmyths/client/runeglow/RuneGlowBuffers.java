package org.slavicmyths.client.runeglow;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexMultiConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import org.slavicmyths.rpg.Runes;
import org.joml.Matrix4f;
import org.slavicmyths.client.runeglow.mixin.RuneBufferAccess;

/** Local render argument, no global current-stack, inventory polling or persistent flags. */
public final class RuneGlowBuffers implements MultiBufferSource {
    private final MultiBufferSource base;
    private final RuneGlowStyle style;
    private final boolean firstMaterialOnly;
    private final PoseStack poses;
    private final float wave;
    private boolean emitted;
    private RuneGlowBuffers(MultiBufferSource base, RuneGlowStyle style, boolean armor, PoseStack poses, float wave) {
        this.base=base; this.style=style; this.firstMaterialOnly=armor; this.poses=poses; this.wave=wave;
    }
    public static MultiBufferSource item(ItemStack stack, MultiBufferSource source, PoseStack poses) { return wrap(stack,source,false,poses); }
    public static MultiBufferSource armor(ItemStack stack, MultiBufferSource source, PoseStack poses) { return wrap(stack,source,true,poses); }
    // Textile meshes use entityCutoutNoCull, without the vanilla armor depth offset.
    public static MultiBufferSource clothing(ItemStack stack, MultiBufferSource source, PoseStack poses) { return wrap(stack,source,false,poses); }
    public static MultiBufferSource armor(ItemStack stack, MultiBufferSource source, PoseStack poses, LivingEntity entity, float partial) {
        return wrap(stack,source,true,poses,ArmorRuneWave.phase(entity,partial));
    }
    public static MultiBufferSource clothing(ItemStack stack, MultiBufferSource source, PoseStack poses, LivingEntity entity, float partial) {
        return wrap(stack,source,false,poses,ArmorRuneWave.phase(entity,partial));
    }
    private static MultiBufferSource wrap(ItemStack stack, MultiBufferSource source, boolean armor, PoseStack poses) {
        return wrap(stack,source,armor,poses,-1);
    }
    private static MultiBufferSource wrap(ItemStack stack, MultiBufferSource source, boolean armor, PoseStack poses, float wave) {
        // Nested item renderers must use the child stack, not inherit the parent's rune count.
        while (source instanceof RuneGlowBuffers prior) source=prior.base;
        if (stack.isEmpty()) return source;
        RuneGlowStyle style=RuneGlowStyle.of(Runes.list(stack).size());
        if (style.count()==0) return source;
        // Unknown immediate/outline sources may share one live builder: requesting another type
        // would end it mid-quad. Keep them unchanged instead of crashing or leaking native buffers.
        if (!(source instanceof RuneBufferAccess access)
                || !access.slavicmyths$runeFixedBuffers().containsKey(RuneGlowRenderTypes.MAIN)
                || !access.slavicmyths$runeFixedBuffers().containsKey(RuneGlowRenderTypes.TRANSLUCENT_ITEM)
                || !access.slavicmyths$runeFixedBuffers().containsKey(RuneGlowRenderTypes.ARMOR)) return source;
        return new RuneGlowBuffers(source,style,armor,poses,wave);
    }
    @Override public VertexConsumer getBuffer(RenderType type) {
        VertexConsumer normal=base.getBuffer(type);
        // Vanilla glint/trims and our own passes must not receive a second overlay.
        if (type.format()!=DefaultVertexFormat.NEW_ENTITY && type.format()!=DefaultVertexFormat.BLOCK) return normal;
        if (type.mode()!=VertexFormat.Mode.QUADS) return normal;
        if (firstMaterialOnly && emitted) return normal;
        Matrix4f inverse=new Matrix4f(poses.last().pose());
        if (!Float.isFinite(inverse.determinant()) || Math.abs(inverse.determinant())<1.0E-12F) return normal;
        inverse.invert();
        emitted=true;
        RenderType glow=firstMaterialOnly ? RuneGlowRenderTypes.ARMOR : Minecraft.useShaderTransparency() && type==Sheets.translucentItemSheet()
                ? RuneGlowRenderTypes.TRANSLUCENT_ITEM : RuneGlowRenderTypes.MAIN;
        return VertexMultiConsumer.create(new RuneGlowConsumer(base.getBuffer(glow),style,inverse,wave), normal);
    }
}
