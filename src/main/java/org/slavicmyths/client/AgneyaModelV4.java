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
import org.slavicmyths.sisters.AgneyaBoss;
import net.minecraft.world.item.Items;

/**
 * Agneya V4: an articulated preview rig for NeoForge 1.21.1.
 * Cuboids are exactly AgneyaGeometryV4.addBox, the Java data source used by Python.
 * Local Y in the mesh is downward, whereas authored geometry uses upward Y.
 * No fire aura here: fire belongs to a particle renderer, not body geometry.
 *
 * Bone tree:
 * root
 *   torso [body, dress, cape]
 *       head [face, hair, skull-mask]
 *       left_arm -> left_forearm -> left_hand; shield anchored to left_forearm
 *       right_arm -> right_forearm -> right_hand -> mace
 *   left_leg
 *   right_leg
 */
public final class AgneyaModelV4 extends EntityModel<AgneyaBoss> {
    private static final String[] MATERIALS = {
        "skin", "skin_light", "skin_shadow", "cheek", "lip", "eye_white", "eye_black", "eye_fire",
        "eye_glint", "hair_black", "hair_dark", "hair_mid", "hair_light", "hair_glow", "dress_dark",
        "dress", "dress_light", "dress_hem", "cape_dark", "cape", "cape_light", "leather",
        "leather_light", "iron", "iron_light", "iron_shadow", "gold", "gold_light", "gold_shadow",
        "bone", "bone_light", "bone_shadow", "socket", "ember"
    };
    private static final String[] BONES = {
        "torso", "head", "left_arm", "left_forearm", "left_hand", "shield",
        "right_arm", "right_forearm", "right_hand", "mace", "left_leg", "right_leg"
    };
    // Absolute pivots in authoring coordinates [X, Y UP, Z].
    private static final float[][] P = {
        {0F, 43F, 0F}, {0F, 63F, 0F}, {-7.7F, 55.5F, -.2F},
        {-9.15F, 45.3F, -.5F}, {-9.9F, 38F, -1.3F}, {-11.2F, 39.4F, -4F},
        {7.7F, 55.5F, -.2F}, {9.15F, 45.3F, -.5F},
        {9.9F, 38F, -1.3F}, {10F, 37F, -1.8F}, {-3.1F, 18F, 0F}, {3.1F, 18F, 0F}
    };
    private final ModelPart root, torso, head, leftArm, leftForearm, leftHand, shield;
    private final ModelPart rightArm, rightForearm, rightHand, mace, leftLeg, rightLeg;
    private boolean manualDefense;
    private float manualAttack = -1F;

    public AgneyaModelV4(ModelPart baked) {
        root = baked.getChild("root");
        torso = root.getChild("torso");
        head = torso.getChild("head");
        leftArm = torso.getChild("left_arm");
        leftForearm = leftArm.getChild("left_forearm");
        leftHand = leftForearm.getChild("left_hand");
        shield = leftForearm.getChild("shield");
        rightArm = torso.getChild("right_arm");
        rightForearm = rightArm.getChild("right_forearm");
        rightHand = rightForearm.getChild("right_hand");
        mace = rightHand.getChild("mace");
        leftLeg = root.getChild("left_leg");
        rightLeg = root.getChild("right_leg");
    }

    private static int material(String mat) {
        for (int i = 0; i < MATERIALS.length; i++) if (MATERIALS[i].equals(mat)) return i;
        throw new IllegalArgumentException("Unknown Agneya V4 material " + mat);
    }
    private static int bone(String part) {
        return switch(part) {
            case "torso", "dress", "cape" -> 0;
            case "head", "skull", "hair" -> 1;
            case "left_arm" -> 2;
            case "left_forearm" -> 3;
            case "left_hand" -> 4;
            case "shield" -> 5;
            case "right_arm" -> 6;
            case "right_forearm" -> 7;
            case "right_hand" -> 8;
            case "mace" -> 9;
            case "left_leg" -> 10;
            case "right_leg" -> 11;
            default -> throw new IllegalArgumentException("Unknown Agneya V4 body group " + part);
        };
    }
    private static PartDefinition child(PartDefinition parent, int bone, CubeListBuilder cubes, int parentBone) {
        float[] p = P[bone];
        float[] q = parentBone < 0 ? new float[]{0F, 0F, 0F} : P[parentBone];
        return parent.addOrReplaceChild(BONES[bone], cubes,
            PartPose.offset(p[0] - q[0], q[1] - p[1], p[2] - q[2]));
    }
    public static LayerDefinition create() {
        CubeListBuilder[] cubes = new CubeListBuilder[BONES.length];
        for (int i=0; i < cubes.length; i++) cubes[i] = CubeListBuilder.create();
        for (AgneyaGeometryV4.Box b : AgneyaGeometryV4.build()) {
            int k = bone(b.part()), m = material(b.material());
            float[] pivot = P[k];
            int u = (m % 8) * 64, v = (m / 8) * 64;
            cubes[k].texOffs(u, v).addBox(
                b.x() - pivot[0], pivot[1] - (b.y() + b.h()), b.z() - pivot[2],
                b.w(), b.h(), b.d());
        }
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition torso = child(root, 0, cubes[0], -1);
        child(torso, 1, cubes[1], 0);
        PartDefinition leftArm = child(torso, 2, cubes[2], 0);
        PartDefinition leftForearm = child(leftArm, 3, cubes[3], 2);
        child(leftForearm, 4, cubes[4], 3);
        child(leftForearm, 5, cubes[5], 3);
        PartDefinition rightArm = child(torso, 6, cubes[6], 0);
        PartDefinition rightForearm = child(rightArm, 7, cubes[7], 6);
        PartDefinition rightHand = child(rightForearm, 8, cubes[8], 7);
        child(rightHand, 9, cubes[9], 8);
        child(root, 10, cubes[10], -1);
        child(root, 11, cubes[11], -1);
        return LayerDefinition.create(mesh, 512, 512);
    }

    /** A boss AI may set synchronized attack/defense state explicitly. */
    public void setDefending(boolean defending) { this.manualDefense = defending; }
    public void setAttackProgress(float progress) { this.manualAttack = progress; }

    @Override public void setupAnim(AgneyaBoss entity, float walk, float stride, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float step = Mth.sin(walk * .6662F) * Math.min(stride, 1F);
        float attack = manualAttack >= 0F ? Mth.clamp(manualAttack, 0F, 1F) : entity.getAttackAnim(age);
        float defend = manualDefense || (entity.isUsingItem() && entity.getUseItem().is(Items.SHIELD)) ? 1F : 0F;
        float swing = Mth.sin(attack * Mth.PI);
        torso.yRot = .023F * Mth.sin(age * .055F);
        head.yRot = .015F * Mth.sin(age * .041F) + yaw * Mth.DEG_TO_RAD * .23F;
        head.xRot = pitch * Mth.DEG_TO_RAD * .22F;
        leftArm.xRot = .08F * step - .60F * defend;
        leftArm.zRot = .08F * defend;
        leftArm.yRot = -.22F * defend;
        leftArm.x += 2.0F * defend;    // forearm and shield cover central chest
        leftArm.z -= 1.35F * defend;
        leftForearm.xRot = -.48F * defend;
        shield.yRot = -.78F * defend; // turn convex face toward target
        rightArm.xRot = -.09F * step - 1.38F * swing;
        rightArm.zRot = -.14F * swing;
        rightForearm.xRot = -.20F * swing - .16F * attack;
        rightHand.xRot = -.10F * swing;
        // Mace inherits EVERY rotation of right upper arm -> elbow -> hand.
        leftLeg.xRot = .34F * step;
        rightLeg.xRot = -.34F * step;
    }
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer out, int light, int overlay, int color) {
        root.render(pose, out, light, overlay, color);
    }
}