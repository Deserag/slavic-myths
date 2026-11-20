package org.slavicmyths.marine;

/**
 * Shared server-safe geometric rule for the tapered 3D neck.
 * Only positive interior ellipse cells may be converted into Minecraft cuboids.
 */
public final class ZhelvaGeometryProfile {
    public static float neckThickness(float x, float radius) {
        if (!Float.isFinite(x) || !Float.isFinite(radius) || radius<=0F)
            throw new IllegalArgumentException("Invalid Zhelva neck coordinates");
        float crossSection=1F-x*x/(radius*radius);
        return crossSection<=0.001F ? 0F : 9F*(float)Math.sqrt(crossSection);
    }
    private ZhelvaGeometryProfile() {}
}
