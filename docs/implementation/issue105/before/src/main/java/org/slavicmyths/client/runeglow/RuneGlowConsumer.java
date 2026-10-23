package org.slavicmyths.client.runeglow;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;

/** Copies geometry into the shared overlay; per-vertex color keeps batched stacks independent. */
public final class RuneGlowConsumer implements VertexConsumer {
    private final VertexConsumer target;
    private final RuneGlowStyle style;
    private final Matrix4f inversePose;
    private float u, v;
    public RuneGlowConsumer(VertexConsumer target, RuneGlowStyle style, Matrix4f inversePose) {
        this.target = target; this.style = style; this.inversePose = inversePose;
    }
    @Override public VertexConsumer addVertex(float x, float y, float z) {
        target.addVertex(x,y,z);
        // Restore model units: GUI pixels, world coordinates and armor atlas sizes must not change density.
        float lx=inversePose.m00()*x+inversePose.m10()*y+inversePose.m20()*z+inversePose.m30();
        float ly=inversePose.m01()*x+inversePose.m11()*y+inversePose.m21()*z+inversePose.m31();
        float lz=inversePose.m02()*x+inversePose.m12()*y+inversePose.m22()*z+inversePose.m32();
        u=lx+lz*.6F; v=ly-lz*.25F;
        return this;
    }
    @Override public VertexConsumer setColor(int r, int g, int b, int a) {
        target.setColor(0x55,0xC9,0x78,style.alpha()); return this;
    }
    @Override public VertexConsumer setUv(float ignoredU, float ignoredV) { target.setUv(u,v); return this; }
    // The overlay format has no lighting, overlay or normal attributes.
    @Override public VertexConsumer setUv1(int u, int v) { return this; }
    @Override public VertexConsumer setUv2(int u, int v) { return this; }
    @Override public VertexConsumer setNormal(float x, float y, float z) { return this; }
}
