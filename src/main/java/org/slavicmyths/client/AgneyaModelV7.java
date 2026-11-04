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
 * Agneya V7: an articulated preview rig for NeoForge 1.21.1.
 * Cuboids are exactly AgneyaGeometryV7.addBox, the Java data source used by Python.
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
public final class AgneyaModelV7 extends EntityModel<AgneyaBoss> {
    private static final String[] MATERIALS = {
        "skin", "skin_light", "skin_shadow", "cheek", "lip", "eye_ivory", "eye_dark", "hair_dark", "hair_mid", "hair_light", "dress", "dress_dark", "dress_light", "dress_hem", "leather", "leather_light", "iron", "iron_dark", "iron_light", "iron_shadow", "gold", "gold_light", "bone", "bone_light", "bone_shadow", "socket", "dark_cloth", "eye_amber", "eye_glint"
    };
    private static final String[] BONES = {
        "torso", "head", "left_arm", "left_forearm", "left_hand", "shield",
        "right_arm", "right_forearm", "right_hand", "mace", "left_leg", "right_leg"
    };
    // Absolute pivots in authoring coordinates [X, Y UP, Z].
    private static final float[][] P = {
        {0F,43F,0F}, {0F,63F,0F}, {-7.25F,58.5F,-.2F},
        {-7.25F,47.2F,-.1F}, {-7.20F,39F,-.3F}, {-10.5F,43F,-4.5F},
        {7.25F,58.5F,-.2F}, {7.3F,47.2F,-.1F},
        {7.8F,39F,-.3F}, {7.95F,39F,-1F}, {-2.75F,18F,0F}, {2.75F,18F,0F}
    };
    private final ModelPart root, torso, head, leftArm, leftForearm, leftHand, shield;
    private final ModelPart rightArm, rightForearm, rightHand, mace, leftLeg, rightLeg;
    private boolean manualDefense;
    private float manualAttack = -1F;

    public AgneyaModelV7(ModelPart baked) {
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
        throw new IllegalArgumentException("Unknown Agneya V7 material " + mat);
    }
    private static int bone(String part) {
        return switch(part) {
            case "torso", "dress" -> 0;
            case "head" -> 1;
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
            default -> throw new IllegalArgumentException("Unknown Agneya V7 body group " + part);
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
        for (AgneyaGeometryV7.Box b : AgneyaGeometryV7.build()) {
            int k = bone(b.part()), m = material(b.material());
            float[] pivot = P[k];
            int u = (m % 8) * 128 + 8, v = (m / 8) * 128 + 8;
            cubes[k].texOffs(u, v).addBox(
                (float)(b.x() - pivot[0]), (float)(pivot[1] - (b.y() + b.h())), (float)(b.z() - pivot[2]),
                (float)b.w(), (float)b.h(), (float)b.d());
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
        return LayerDefinition.create(mesh, 1024, 1024);
    }

    /** A boss AI may set synchronized attack/defense state explicitly. */
    public void setDefending(boolean defending) { this.manualDefense = defending; }
    public void setAttackProgress(float progress) { this.manualAttack = progress; }

    private static float ease(float t) {
        float v = Mth.clamp(t, 0F, 1F);
        return v * v * (3F - 2F * v);
    }

    @Override public void setupAnim(AgneyaBoss entity, float walk, float stride, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float step = Mth.sin(walk * .6662F) * Mth.clamp(stride, 0F, 1F);
        float attackTicks = manualAttack >= 0F ? manualAttack * 26F : entity.attackPoseTicks(age - entity.tickCount);
        float defend = manualDefense || (entity.isUsingItem() && entity.getUseItem().is(Items.SHIELD)) ? 1F : 0F;
        torso.yRot = .025F * Mth.sin(age * .055F);
        head.yRot = .018F * Mth.sin(age * .045F) + yaw * Mth.DEG_TO_RAD * .22F;
        head.xRot = pitch * Mth.DEG_TO_RAD * .22F;
        leftArm.xRot = .11F * step - .60F * defend;
        leftArm.zRot = .08F * defend;
        leftArm.yRot = -.22F * defend;
        leftArm.x += 2.0F * defend;
        leftArm.z -= 1.35F * defend;
        leftForearm.xRot = -.48F * defend;
        shield.yRot = -.78F * defend;
        rightArm.xRot = -.12F * step;
        rightForearm.xRot = -.07F;
        rightHand.xRot = 0F;
        // Real 26-tick animation: raise (0..9), land blow (9..15), recover (15..26).
        if (attackTicks >= 0F && attackTicks < 26F) {
            if (attackTicks < 9F) {
                float t = ease(attackTicks / 9F);
                rightArm.xRot = .72F * t;
                rightForearm.xRot = -.07F - .25F * t;
                torso.yRot -= .10F * t;
            } else if (attackTicks < 15F) {
                float t = ease((attackTicks - 9F) / 6F);
                rightArm.xRot = .72F - 2.45F * t;
                rightForearm.xRot = -.32F - .25F * t;
                torso.yRot -= .10F + .14F * t;
            } else {
                float t = ease((attackTicks - 15F) / 11F);
                rightArm.xRot = -1.73F * (1F - t);
                rightForearm.xRot = -.57F * (1F - t) - .07F * t;
                torso.yRot -= .24F * (1F - t);
            }
        }
        mace.xRot = 1.20F; // weapon remains gripped throughout the complete swing
        leftLeg.xRot = .65F * step;
        rightLeg.xRot = -.65F * step;
    }
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer out, int light, int overlay, int color) {
        root.render(pose, out, light, overlay, color);
    }
}