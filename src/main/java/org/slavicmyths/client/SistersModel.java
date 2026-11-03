package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import org.slavicmyths.sisters.SisterBoss;

/**
 * Actual production voxel geometry for Tryaseya and Gneteya.
 * Coordinates use Minecraft model pixels: height 0 at feet, increasing upwards.
 * All visible parts are real CubeListBuilder cubes; no preview-only geometry.
 */
public final class SistersModel extends EntityModel<SisterBoss> {
    private static final int ATLAS = 1024;
    private static final int SKIN = 0;
    private static final int CLOTH = 256;
    private static final int BONE = 512;
    private static final int HEM = 768;
    private static final int GLOW = 128;
    private static int nextCube;
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final boolean spirit;

    public SistersModel(ModelPart baked, boolean spirit) {
        this.root = baked.getChild("root");
        this.head = root.getChild("head");
        this.rightArm = root.getChild("right_arm");
        this.leftArm = root.getChild("left_arm");
        this.spirit = spirit;
    }

    private static PartDefinition part(PartDefinition parent, String name, float x, float y, float z) {
        return parent.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.offset(x, y, z));
    }

    private static void cube(PartDefinition parent, int u, int v, float x, float height, float z, float width, float tall, float depth) {
        if (width <= 0 || tall <= 0 || depth <= 0) throw new IllegalArgumentException("Invalid sister cube");
        parent.addOrReplaceChild("voxel_" + nextCube++, CubeListBuilder.create().texOffs(u, v).addBox(x, -height - tall, z, width, tall, depth), PartPose.ZERO);
    }

    public static LayerDefinition create(boolean spirit) {
        nextCube = 0;
        MeshDefinition mesh = new MeshDefinition();
        // Root must be at entity feet. offset(0,24,0) displaced the full model underground.
        PartDefinition root = part(mesh.getRoot(), "root", 0, 0, 0);
        PartDefinition head = part(root, "head", 0, -52, 0);
        PartDefinition rightArm = part(root, "right_arm", -5, -43, 0);
        PartDefinition leftArm = part(root, "left_arm", 5, -43, 0);
        if (spirit) buildTryaseya(root, head, rightArm, leftArm);
        else buildGneteya(root, head, rightArm, leftArm);
        return LayerDefinition.create(mesh, ATLAS, ATLAS);
    }

    private static void buildTryaseya(PartDefinition root, PartDefinition head, PartDefinition rightArm, PartDefinition leftArm) {
        // Spectral tapered gown, distinct stacked tier geometry (not one pillar).
        for (int h = 3; h < 29; h += 3) {
            float radius = .8F + (h / 29F) * 4.4F;
            cube(root, HEM, h * 3, -radius, h, -radius / 2, radius * 2, 3, radius);
        }
        cube(root, CLOTH, 0, -3.3F, 32, -2.7F, 6.6F, 15, 5.4F);
        cube(root, HEM, 32, -3.9F, 33.5F, -3, 7.8F, 2, 6);
        cube(root, CLOTH, 48, -3.5F, 47, -2.5F, 7, 3, 5);

        // Face, cyan eyes, mouth, layered bangs and long strands are head-attached.
        cube(head, SKIN, 0, -3.2F, 51 - 52, -2.8F, 6.4F, 7.5F, 5.5F);
        for (float x : new float[]{-2.1F, 1.15F}) cube(head, GLOW, 16, x, 55 - 52, -3.05F, 1.25F, 1, .5F);
        cube(head, SKIN, 32, -.65F, 52.8F - 52, -3.1F, 1.3F, .25F, .35F);
        for (float x : new float[]{-5F, -3.7F, -2.1F, 2.1F, 3.7F, 5F}) {
            for (int i = 0; i < 12; i++) {
                float h = 43 + i * 1.5F;
                cube(head, CLOTH, 128, x + .25F * Mth.sin(i * .9F + x), h - 52, .7F, .85F, 2, 1.05F);
            }
        }
        for (float x : new float[]{-4.3F, 3.35F}) {
            for (int i = 0; i < 12; i++) {
                float h = 40 + i * 1.5F;
                cube(head, CLOTH, 144, x + .25F * Mth.sin(i * .85F), h - 52, -2.6F, 1.05F, 2, 1.2F);
            }
        }

        // Limbs: parent pivots are deliberately at shoulders for animation.
        cube(rightArm, SKIN, 0, -.3F, -12, -1.8F, 1.1F, 14, 3.4F);
        cube(leftArm, SKIN, 0, -.5F, -12, -1.8F, 1.1F, 14, 3.4F);
        cube(rightArm, CLOTH, 128, -.5F, -5, -2, 1.5F, 7, 4);
        cube(leftArm, CLOTH, 128, -.7F, -5, -2, 1.5F, 7, 4);

        // Lantern: full shaft, two metal rims and glowing core. Static in root space,
        // so it cannot disappear when a shoulder rotates.
        cube(root, BONE, 0, -8.7F, 4, -1.5F, .8F, 59, .8F);
        cube(root, GLOW, 32, -10.1F, 60, -3, 3.6F, 4.5F, 3.6F);
        cube(root, BONE, 48, -10.5F, 59.5F, -3.4F, 4.3F, .6F, 4.3F);
        cube(root, BONE, 64, -10.5F, 64.5F, -3.4F, 4.3F, .6F, 4.3F);
    }

    private static void buildGneteya(PartDefinition root, PartDefinition head, PartDefinition rightArm, PartDefinition leftArm) {
        // Layered dark hem and burgundy over-skirt.
        for (int h = 0; h < 28; h += 3) {
            float radius = h < 21 ? 4.2F : 5.5F;
            cube(root, h < 21 ? CLOTH : HEM, h * 3, -radius, h, -2.65F, radius * 2, 3, 5.3F);
        }
        cube(root, CLOTH, 0, -4.4F, 29, -3.1F, 8.8F, 13, 6.2F);
        cube(root, HEM, 32, -3.9F, 42, -3, 7.8F, 10, 6);
        for (int i = 0; i < 5; i++) cube(root, HEM, 96 + i * 12, -4.6F + i * 1.8F, 20 - i * .3F, -3.1F, 1.5F, 10, 6.2F);

        // Bone mask, separate recessed red eye lenses and nose.
        cube(head, BONE, 0, -3.6F, 52 - 52, -2.9F, 7.2F, 8.8F, 5.8F);
        cube(head, BONE, 48, -3.25F, 55 - 52, -3.2F, 6.5F, 4.5F, 1.2F);
        for (float x : new float[]{-2.4F, 1F}) cube(head, GLOW, 16, x, 55.1F - 52, -3.6F, 1.6F, 1.15F, .6F);
        cube(head, CLOTH, 80, -.8F, 53 - 52, -3.55F, 1.6F, 2, .8F);

        // 3D rib cage, sternum, rear spine and shoulder-bone sets.
        for (int i = 0; i < 5; i++) {
            float h = 43.2F - i * 2.2F;
            cube(root, BONE, i * 12, -4.25F, h, -3.75F, 8.5F, .75F, 1.3F);
            cube(root, BONE, i * 12, -4.25F, h, 2.7F, 8.5F, .75F, 1);
            cube(root, BONE, 96, -.65F, h, -4, 1.3F, 2.2F, 1);
        }
        for (int i = 0; i < 10; i++) cube(root, BONE, 128, -.5F, 31 + i * 2, 3.1F, .95F, 1.6F, 1);
        for (int side : new int[]{-1, 1}) {
            for (int i = 0; i < 4; i++) {
                float h = 47.5F + i * 1.4F;
                float x = side * (4.5F + i * .55F);
                cube(root, BONE, 160, x - 1, h, -3, 2, .85F, 5.2F);
            }
        }
        cube(rightArm, CLOTH, 32, -1, -2, -1.2F, 2.5F, 9, 3.2F);
        cube(leftArm, CLOTH, 32, -.5F, -2, -1.2F, 2.5F, 9, 3.2F);
        cube(rightArm, BONE, 80, -1.2F, -8, -2.0F, 1.4F, 3, 4);
        cube(leftArm, BONE, 80, .2F, -8, -2.0F, 1.4F, 3, 4);

        // Tall staff and progressive curved segmented scythe blade.
        cube(root, CLOTH, 192, 6.1F, 13, -1.5F, 1.05F, 55, 1.05F);
        for (int i = 0; i < 22; i++) {
            float t = i / 21F;
            float x = 6.5F + 18F * t;
            float h = 68F - 17F * (float)Math.pow(t, 1.75);
            cube(root, BONE, 192 + (i % 4) * 8, x, h, -1.6F, 1.5F - t * .7F, 1.8F - t * .6F, 2.3F - t);
        }
    }

    @Override
    public void setupAnim(SisterBoss entity, float walk, float stride, float age, float yaw, float pitch) {
        // Do not overwrite the rest position of the baked model with y=24.
        root.y = spirit ? Mth.sin(age * .08F) * 1.5F - 1.0F : 0;
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        rightArm.xRot = spirit ? -.18F + Mth.sin(age * .05F) * .09F : -.55F;
        rightArm.yRot = spirit ? 0 : -.15F;
        leftArm.xRot = spirit ? Mth.sin(age * .05F + 1.4F) * .08F : -.4F;
        leftArm.yRot = spirit ? 0 : .18F;
    }

    @Override
    public void renderToBuffer(PoseStack stack, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(stack, consumer, light, overlay, color);
    }
}
