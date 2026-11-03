package org.slavicmyths.morovoy;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class MorovoyFireZmeyMesh {
    private static CubeListBuilder box(int u, int v, float x, float y, float z, float w, float h, float d) {
        return CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d);
    }
    private static PartPose pose(float x, float y, float z, float pitch, float yaw, float roll) {
        return PartPose.offsetAndRotation(x, y, z, pitch, yaw, roll);
    }
    private static float radians(float degrees) {
        return (float)Math.toRadians(degrees);
    }
    public static ModelPart create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7, -6, -10, 7, 12, 20).texOffs(192, 0).addBox(0, -6, -10, 7, 12, 20), PartPose.offset(0, 12, 0));
        body.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 28).addBox(-8.5F, -7, -19, 8.5F, 14, 18).texOffs(192, 28).addBox(0, -7, -19, 8.5F, 14, 18), PartPose.ZERO);
        body.addOrReplaceChild("belly", CubeListBuilder.create().texOffs(80, 28).addBox(-6.5F, -5.5F, -1, 6.5F, 11, 17).texOffs(272, 28).addBox(0, -5.5F, -1, 6.5F, 11, 17), PartPose.ZERO);
        body.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(144, 28).addBox(-5.5F, -5, 13, 5.5F, 10, 12).texOffs(336, 28).addBox(0, -5, 13, 5.5F, 10, 12), PartPose.ZERO);
        for (int i = 0; i < 11; i++) {
            float z = -18 + i * 3.8F;
            float height = 5.0F - 0.22F * i;
            body.addOrReplaceChild("dorsal_" + i, box(208, 28, -1.2F, -7 - height, z, 2.4F, height, 3), PartPose.ZERO);
        }
        for (int s : new int[]{-1, 1}) {
            String side = s < 0 ? "left" : "right";
            int u = s < 0 ? 0 : 192;
            neck(body, side, s, u);
            wing(body, side, s);
            legs(body, side, s);
            tail(body, side, s);
        }
        return LayerDefinition.create(mesh, 512, 512).bakeRoot();
    }
    private static void neck(PartDefinition body, String side, int s, int u) {
        PartDefinition base = body.addOrReplaceChild("neck_" + side + "_base", box(u, 96, -4, -4, -9, 8, 8, 9), pose(s * 5.5F, -5, -10, radians(-32), s * radians(15), 0));
        PartDefinition mid = base.addOrReplaceChild("neck_" + side + "_mid", box(u + 32, 96, -3, -3.5F, -8, 6, 7, 8), pose(s * 2, -0.5F, -8, radians(20), s * radians(10), s * radians(5)));
        PartDefinition tip = mid.addOrReplaceChild("neck_" + side + "_tip", box(u + 64, 96, -2.4F, -3, -7, 4.8F, 6, 7), pose(s * 1.5F, 0, -8, radians(15), -s * radians(7), 0));
        neckRings(base, "base", u, 128, 9, 8, 8, 6);
        neckRings(mid, "mid", u, 160, 8, 6, 7, 6);
        neckRings(tip, "tip", u, 192, 7, 4.8F, 6, 5);
        PartDefinition head = tip.addOrReplaceChild("head_" + side, box(u, 232, -4, -4, -10, 8, 6, 10)
            .texOffs(u + 32, 232).addBox(-3, -1, -15, 6, 3, 7), pose(0, -0.3F, -7, radians(8), s * radians(12), 0));
        PartDefinition jaw = head.addOrReplaceChild("jaw_" + side, box(u + 60, 232, -3.5F, 0, -13, 7, 2.5F, 13), pose(0, 2, -1, radians(15), 0, 0));
        head.addOrReplaceChild("brow_" + side, box(u + 90, 232, -4, -5, -10, 8, 2, 5), PartPose.ZERO);
        for (int j = 0; j < 2; j++) {
            int sign = j == 0 ? -1 : 1;
            head.addOrReplaceChild("eye_" + side + "_" + (j == 0 ? "a" : "b"), box(u + 110, 232, sign < 0 ? -4.5F : 3.2F, -3.0F, -8, 1.3F, 2.2F, 2.4F), PartPose.ZERO);
            head.addOrReplaceChild("horn_" + side + "_" + j, box(u + 120, 232, -1, -6, -1, 2, 7, 2.5F), pose(sign * 3.1F, -4.2F, -2.2F, -0.2F, sign * 0.4F, sign * 0.35F));
        }
        for (int j = 0; j < 4; j++) {
            head.addOrReplaceChild("crest_" + side + "_" + j, box(u + 135, 232, -1, -3.5F - j * 0.4F, -8 + j * 2.5F, 2, 3.5F + j * 0.5F, 2.5F), PartPose.ZERO);
        }
        for (int j = 0; j < 5; j++) {
            jaw.addOrReplaceChild("tooth_" + j, box(u + 150, 232, -3 + j * 1.4F, -2, -11.5F, 0.8F, 2.4F, 1.2F), PartPose.ZERO);
        }
    }
    private static void neckRings(PartDefinition parent, String name, int u, int v, float length, float width, float height, int steps) {
        for (int i = 0; i < steps; i++) {
            float t = i / (float)(steps - 1);
            float scale = 1.0F - 0.32F * t;
            float x = (float)Math.sin(Math.PI * 1.25F * t) * 0.4F;
            float y = (float)Math.sin(Math.PI * 1.5F * t) * 0.45F;
            float depth = length / (steps - 1) * 1.28F;
            PartDefinition ring = parent.addOrReplaceChild(name + "_ring_" + i, box(u, v, -width * scale / 2, -height * scale / 2, -depth / 2, width * scale, height * scale, depth), PartPose.offset(x, y, -length * t));
            for (int s : new int[]{-1, 1}) {
                ring.addOrReplaceChild("side_ridge_" + s, box(u + 32, v, s < 0 ? -width * scale / 2 - 1 : width * scale / 2 - 0.4F, -1.1F, -1, 1.4F, 2.6F, 2), PartPose.ZERO);
            }
        }
    }
    private static void wing(PartDefinition body, String side, int s) {
        // One shoulder, one elbow and one wrist; finger origins are all wrist-local.
        final float upperLength = 29.0F;
        final float forearmLength = 32.0F;
        PartDefinition upper = body.addOrReplaceChild("wing_" + side, box(s < 0 ? 0 : 192, 288, s < 0 ? -upperLength : 0, -2.5F, -2.5F, upperLength, 5, 5), pose(s * 7, -5, -14, radians(6), -s * radians(20), s * radians(12)));
        PartDefinition elbow = upper.addOrReplaceChild("wing_elbow_" + side, box(s < 0 ? 0 : 192, 298, -3, -3, -3, 6, 6, 6), PartPose.offset(s * upperLength, 0, 0));
        PartDefinition forearm = elbow.addOrReplaceChild("wing_forearm_" + side, box(s < 0 ? 0 : 192, 302, s < 0 ? -forearmLength : 0, -1.8F, -1.8F, forearmLength, 3.6F, 3.6F), pose(0, 0, 0, radians(3), -s * radians(21), -s * radians(10)));
        PartDefinition tip = forearm.addOrReplaceChild("wing_tip_" + side, box(s < 0 ? 0 : 192, 314, -2.6F, -2.6F, -2.6F, 5.2F, 5.2F, 5.2F), PartPose.offset(s * forearmLength, 0, 0));

        float[][] ends = new float[4][3];
        for (int finger = 0; finger < 4; finger++) {
            float angle = radians(-70 + finger * 50);
            float length = 52 + (finger == 1 || finger == 2 ? 9 : 0);
            ends[finger][0] = s * length * (float)Math.cos(angle);
            ends[finger][1] = 11.0F;
            ends[finger][2] = length * (float)Math.sin(angle);
            for (int k = 0; k < 23; k++) {
                float t = k / 23.0F;
                float nx = ends[finger][0] * t;
                float nz = ends[finger][2] * t;
                float x2 = ends[finger][0] * (t + 1.0F / 23);
                float z2 = ends[finger][2] * (t + 1.0F / 23);
                float r = 1.35F - .55F * t;
                tip.addOrReplaceChild("digit_" + finger + "_" + k, box(s < 0 ? 0 : 192, 346, Math.min(nx, x2) - r / 2, 11 * t - r / 2, Math.min(nz, z2) - r / 2, Math.abs(x2 - nx) + r, r + 11.0F / 23, Math.abs(z2 - nz) + r), PartPose.ZERO);
            }
        }

        // Webbing connects adjacent digits. The distal edge is inset sinusoidally:
        // at midpoint it is 0.35 of the finger length shorter (inverted scallop).
        for (int gap = 0; gap < 3; gap++) {
            for (int across = 0; across < 32; across++) {
                float u = (across + .5F) / 32.0F;
                float scallop = 1 - .35F * (float)Math.sin(Math.PI * u);
                for (int outward = 0; outward < 23; outward++) {
                    float t = (outward + .5F) / 23.0F * scallop;
                    float x = ((1 - u) * ends[gap][0] + u * ends[gap + 1][0]) * t;
                    float y = ((1 - u) * ends[gap][1] + u * ends[gap + 1][1]) * t;
                    float z = ((1 - u) * ends[gap][2] + u * ends[gap + 1][2]) * t;
                    // 1-pixel membrane: no random radial branches or disconnected feathers.
                    tip.addOrReplaceChild("web_" + gap + "_" + across + "_" + outward, box(s < 0 ? 80 : 272, 326, x - 1.6F, y - .5F, z - 1.6F, 3.2F, 1, 3.2F), PartPose.ZERO);
                }
            }
        }
    }
    private static void legs(PartDefinition body, String side, int s) {
        for (int i = 0; i < 2; i++) {
            PartDefinition leg = body.addOrReplaceChild("leg_" + side + "_" + i, box(0, 384, -3.5F, 0, -3.5F, 7, 12, 7), PartPose.offset(s * 7, 4, i == 0 ? -12 : 14));
            leg.addOrReplaceChild("foot", box(32, 384, -3, 10, -7, 6, 3, 10), PartPose.ZERO);
            for (int j = 0; j < 3; j++) {
                leg.addOrReplaceChild("claw_" + j, box(56, 384, -2.7F + j * 2.0F, 11, -11, 1, 2, 6), PartPose.ZERO);
            }
        }
    }
    private static void tail(PartDefinition body, String side, int s) {
        PartDefinition previous = body;
        float width = 5.8F;
        for (int i = 0; i < 8; i++) {
            width *= 0.78F;
            float height = width * .9F;
            PartDefinition segment = previous.addOrReplaceChild("tail_" + side + "_" + i, box(s < 0 ? 0 : 192, 416, -width / 2, -height / 2, 0, width, height, 8), i == 0 ? pose(s * 3, 1, 22, radians(-8), s * .09F, 0) : pose(s * .25F, .15F, 7.4F, 0, s * .035F, 0));
            if (i >= 6) {
                for (int j = 0; j < 3; j++) {
                    segment.addOrReplaceChild("flame_" + j, box(s < 0 ? 140 : 330, 416, s * (i - 5) * 1.5F + j * .3F, -1 - j * 1.4F, j * 2.4F, Math.max(.7F, width), 1.2F, 4), PartPose.ZERO);
                }
            } else {
                for (int j = 0; j < 2; j++) {
                    segment.addOrReplaceChild("spike_" + j, box(s < 0 ? 40 : 230, 416, -.6F, -height / 2 - 2.3F, j * 3, 1.2F, 2.3F, 2), PartPose.ZERO);
                }
            }
            previous = segment;
        }
    }
    private MorovoyFireZmeyMesh() {}
}
