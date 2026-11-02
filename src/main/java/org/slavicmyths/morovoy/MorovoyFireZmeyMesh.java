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
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", box(0, 0, -7, -6, -10, 14, 12, 20), PartPose.offset(0, 12, 0));
        body.addOrReplaceChild("chest", box(0, 28, -8.5F, -7, -19, 17, 14, 18), PartPose.ZERO);
        body.addOrReplaceChild("belly", box(80, 28, -6.5F, -5.5F, -1, 13, 11, 17), PartPose.ZERO);
        body.addOrReplaceChild("pelvis", box(144, 28, -5.5F, -5, 13, 11, 10, 12), PartPose.ZERO);
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
        PartDefinition base = body.addOrReplaceChild("neck_" + side + "_base", box(u, 96, -4, -4, -12, 8, 8, 12), pose(s * 5.5F, -5, -16, radians(-45), s * radians(15), 0));
        PartDefinition mid = base.addOrReplaceChild("neck_" + side + "_mid", box(u + 32, 96, -3, -3.5F, -12, 6, 7, 12), pose(s * 2, -0.5F, -12, radians(25), s * radians(10), s * radians(5)));
        PartDefinition tip = mid.addOrReplaceChild("neck_" + side + "_tip", box(u + 64, 96, -2.4F, -3, -10, 4.8F, 6, 10), pose(s * 1.5F, 0, -12, radians(20), -s * radians(7), 0));
        neckRings(base, "base", u, 128, 12, 8, 8, 7);
        neckRings(mid, "mid", u, 160, 12, 6, 7, 7);
        neckRings(tip, "tip", u, 192, 10, 4.8F, 6, 6);
        PartDefinition head = tip.addOrReplaceChild("head_" + side, box(u, 232, -4, -4, -10, 8, 6, 10)
            .texOffs(u + 32, 232).addBox(-3, -1, -15, 6, 3, 7), pose(0, -0.3F, -10, radians(8), s * radians(12), 0));
        PartDefinition jaw = head.addOrReplaceChild("jaw_" + side, box(u + 60, 232, -3.5F, 0, -13, 7, 2.5F, 13), pose(0, 2, -1, radians(15), 0, 0));
        head.addOrReplaceChild("brow_" + side, box(u + 90, 232, -4, -5, -10, 8, 2, 5), PartPose.ZERO);
        for (int j = 0; j < 2; j++) {
            int sign = j == 0 ? -1 : 1;
            head.addOrReplaceChild("eye_" + side + "_" + (j == 0 ? "a" : "b"), box(u + 110, 232, sign < 0 ? -4.5F : 3.2F, -3.0F, -8, 1.3F, 2.2F, 2.4F), PartPose.ZERO);
            head.addOrReplaceChild("horn_" + side + "_" + j, box(u + 120, 232, -1, -7, -1, 2, 7, 2.5F), pose(sign * 3, -4, -2, -0.2F, sign * 0.4F, sign * 0.35F));
        }
        for (int j = 0; j < 4; j++) {
            head.addOrReplaceChild("crest_" + side + "_" + j, box(u + 135, 232, -1, -7 - j, -7 + j * 3.2F, 2, 4 + j, 2.5F), PartPose.ZERO);
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
        PartDefinition upper = body.addOrReplaceChild("wing_" + side, box(0, 288, s < 0 ? -16 : 0, -2, -2, 16, 4, 4), pose(s * 7, -5, -14, radians(10), -s * radians(40), s * radians(30)));
        PartDefinition forearm = upper.addOrReplaceChild("wing_forearm_" + side, box(0, 302, s < 0 ? -18 : 0, -1.5F, -1.5F, 18, 3, 3), pose(s * 16, 0, 0, 0, -s * radians(50), -s * radians(20)));
        float yaw = -s * radians(50);
        float roll = -s * radians(20);
        float tipX = s * 16 + s * 18 * (float)(Math.cos(yaw) * Math.cos(roll));
        float tipY = s * 18 * (float)Math.sin(roll);
        float tipZ = -s * 18 * (float)Math.sin(yaw);
        PartDefinition tip = upper.addOrReplaceChild("wing_tip_" + side, box(0, 314, s < 0 ? -9 : 0, -1, -1, 9, 2, 2), pose(tipX, tipY, tipZ, radians(2), -s * radians(50), -s * radians(20)));
        for (int i = 0; i < 14; i++) {
            float progress = i / 13.0F;
            float length = 16 + 26 * (float)Math.sin(Math.PI * progress);
            float rotY = s < 0 ? radians(-10 - i * 9) : radians(10 + i * 9);
            forearm.addOrReplaceChild("membrane_" + i, box(80, 326, s < 0 ? -length : 0, 0, 0, length, 0.25F, 5), pose(s * (1 + i * 1.2F), 0, 0, radians(2), rotY, 0));
        }
        for (int i = 0; i < 5; i++) {
            tip.addOrReplaceChild("tip_rib_" + i, box(80, 346, s < 0 ? -6 : 0, 0, i * 3, 6, 0.5F, 4), pose(0, 0, 0, 0, s * i * 0.11F, 0));
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
        for (int i = 0; i < 6; i++) {
            float width = 5.8F - i * 0.65F;
            PartDefinition segment = previous.addOrReplaceChild("tail_" + side + "_" + i, box(0, 416, -width / 2, -width * 0.45F, 0, width, width * 0.9F, 10), i == 0 ? pose(s * 3, 1, 22, radians(-10), s * 0.13F, 0) : pose(s * 0.3F, 0.2F, 9, 0, s * 0.04F, 0));
            for (int j = 0; j < 3; j++) {
                segment.addOrReplaceChild("spike_" + j, box(40, 416, -0.6F, -width * 0.45F - 3, j * 3, 1.2F, 3, 2), PartPose.ZERO);
            }
            previous = segment;
        }
    }
    private MorovoyFireZmeyMesh() {}
}
