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
import org.slavicmyths.morovoy.MorovoyRaven;

/**
 * Issue #391: four independently articulated raven wings; 16 model units = one block.
 * Forehead z=-44 and tail-root z=+42: 86/16 = 5.375 blocks of body length.
 * The open main wing tips span approximately -128..+128: 256/16 = 16 blocks.
 * Infection is ONLY painted into the UV-aligned emissive PNG, not modeled as cubes.
 */
public final class MorovoyRavenModel extends EntityModel<MorovoyRaven> {
    private final ModelPart root, bird, head, jaw, tail, leftFoot, rightFoot;
    private final ModelPart mainLeft, mainRight, upperLeft, upperRight;
    private final ModelPart mainLeftOuter, mainRightOuter, upperLeftOuter, upperRightOuter;

    public MorovoyRavenModel() {
        root = layer().bakeRoot();
        bird = root.getChild("bird");
        head = bird.getChild("head");
        jaw = head.getChild("jaw");
        tail = bird.getChild("tail");
        leftFoot = bird.getChild("left_foot");
        rightFoot = bird.getChild("right_foot");
        mainLeft = bird.getChild("main_left");
        mainRight = bird.getChild("main_right");
        upperLeft = bird.getChild("upper_left");
        upperRight = bird.getChild("upper_right");
        mainLeftOuter = mainLeft.getChild("outer");
        mainRightOuter = mainRight.getChild("outer");
        upperLeftOuter = upperLeft.getChild("outer");
        upperRightOuter = upperRight.getChild("outer");
    }

    /** Reuses this exact articulated skull in the trophy renderer. */
    public ModelPart trophyHead() { return head; }

    private static void add(PartDefinition parent, String name, int u, int v, float x, float y, float z, float w, float h, float d) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d), PartPose.ZERO);
    }

    private static float fromRoot(int side, float distance, float width) {
        return side < 0 ? -distance - width : distance;
    }

    private static void buildWing(PartDefinition bird, String name, int side, boolean upper) {
        float innerLength = upper ? 40F : 54F;
        float outerLength = upper ? 40F : 60F;
        float y = upper ? -35F : -22F;
        float z = upper ? 5F : -6F;
        float shoulder = upper ? 10F : 13F;
        PartDefinition wing = bird.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.offset(upper ? side * 7.5F : side * shoulder, y, z));

        // A narrow leading-edge spar, a feather fan and one separately rotating wrist.
        add(wing, "leading_edge", upper ? 0 : 0, upper ? 110 : 52, fromRoot(side, 0, innerLength), -4, -10, innerLength, upper ? 4 : 6, upper ? 11 : 15);
        add(wing, "leading_edge_taper", 130, 40, fromRoot(side, innerLength - 14, 14), -3, -12, 14, 4, 11);
        PartDefinition outer = wing.addOrReplaceChild("outer", CubeListBuilder.create(), PartPose.offset(side * innerLength, 0, 0));
        add(outer, "wrist", upper ? 50 : 0, upper ? 125 : 52, fromRoot(side, 0, outerLength), -3, -8, outerLength, upper ? 3 : 4, upper ? 8 : 12);

        int innerCount = upper ? 4 : 5;
        int outerCount = upper ? 5 : 7;
        for (int i = 0; i < innerCount; i++) {
            float distance = 6 + i * (upper ? 9F : 10F);
            float width = upper ? 10 : 12;
            float depth = (upper ? 19 : 24) + i * 2 + (i % 2) * 4;
            float baseZ = -3 + (i % 2) * 2;
            add(wing, "covert_" + i, upper ? 156 : 130, upper ? 0 : 40, fromRoot(side, distance, width), -2, baseZ, width, 3, depth);
            add(wing, "covert_tip_" + i, 216, 170, fromRoot(side, distance + 2, width - 4), 0, baseZ + depth - 3, width - 4, 5 + (i % 3) * 2, 6);
        }
        for (int i = 0; i < outerCount; i++) {
            float distance = 2 + i * (upper ? 7.2F : 8F);
            float width = upper ? 9 : 11;
            float depth = (upper ? 23 : 29) + i + (i % 3) * 3;
            float baseZ = -3 + (i % 3);
            int uvX = upper ? 50 : (i % 2 == 0 ? 0 : 98);
            int uvY = upper ? 125 : 40;
            add(outer, "flight_" + i, uvX, uvY, fromRoot(side, distance, width), -1, baseZ, width, 2.5F, depth);
            // Taper each flight feather in two directions rather than stacking slabs.
            add(outer, "flight_point_" + i, 216, 170, fromRoot(side, distance + 2, width - 4), 1, baseZ + depth - 2, width - 4, 5 + (i % 3) * 2, 6 + (i % 2) * 2);
            if (i == 1 || i == 4 || (!upper && i == 6)) {
                add(outer, "torn_barb_" + i, 130, 40, fromRoot(side, distance + width - 4, 2), 3, baseZ + depth - 7, 2, 4 + i, 4);
            }
        }
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition base = mesh.getRoot();
        PartDefinition bird = base.addOrReplaceChild("bird", CubeListBuilder.create(), PartPose.offset(0, -19, -6.0F));

        // A raven's single tapering breast, abdomen and neck: NO chest cube pile-up.
        add(bird, "breast", 0, 0, -13, -25, -25, 26, 29, 35);
        add(bird, "abdomen", 60, 90, -10, -13, 7, 20, 18, 35);
        add(bird, "neck", 0, 90, -9, -33, -29, 18, 18, 20);
        add(bird, "back_mantle", 130, 40, -11, -28, 7, 22, 5, 26);
        for (int i = 0; i < 4; i++) {
            float width = 14 - i * 2;
            add(bird, "sternum_feather_" + i, 130, 40, -width / 2, -2 + i * 3, -21 + i * 3, width, 3, 11);
        }

        // Skull forehead z=-44; beak consists of progressively narrowing, bone-UV steps.
        PartDefinition skull = bird.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0, -31, -30));
        add(skull, "cranium", 0, 150, -11, -12, -15, 22, 20, 26);
        add(skull, "brow", 94, 151, -8, -12, -12, 16, 4, 12);
        add(skull, "nape", 166, 42, -7, -7, 8, 14, 10, 9);
        add(skull, "upper_beak_root", 64, 194, -7.0F, -1.0F, -25.0F, 14.0F, 7.0F, 14.0F);
        add(skull, "upper_beak_wedge", 64, 194, -5.0F, 0.0F, -36.0F, 10.0F, 6.0F, 11.0F);
        add(skull, "upper_beak_hook", 64, 194, -3.0F, 2.5F, -43.0F, 6.0F, 8.0F, 9.0F);
        add(skull, "hooked_tip", 64, 194, -1.8F, 7.0F, -47.0F, 3.6F, 6.0F, 6.0F);
        add(skull, "nostril_left", 20, 160, -6.9F, 1, -24, 1, 2, 2);
        add(skull, "nostril_right", 20, 160, 5.9F, 1, -24, 1, 2, 2);
        add(skull, "eye_left", 185, 38, -9.6F, -5, -8, 1, 3, 4);
        add(skull, "eye_right", 185, 38, 8.6F, -5, -8, 1, 3, 4);
        for (int i = 0; i < 3; i++) {
            add(skull, "crown_feather_" + i, 0, 150, -6 + i * 4.5F, -15 - (i % 2) * 2, 0, 3, 6, 12);
        }

        // The jaw rotates around the rear hinge, so it truly opens at the beak.
        PartDefinition jaw = skull.addOrReplaceChild("jaw", CubeListBuilder.create(), PartPose.offset(0, 4, -13));
        add(jaw, "lower_mandible", 64, 194, -6, 0, -12, 12, 3, 12);
        add(jaw, "lower_tip", 64, 194, -3, 0, -21, 6, 3, 10);
        add(jaw, "tongue", 156, 194, -2.5F, -0.5F, -14, 5, 1, 10);

        buildWing(bird, "main_left", -1, false);
        buildWing(bird, "main_right", 1, false);
        buildWing(bird, "upper_left", -1, true);
        buildWing(bird, "upper_right", 1, true);

        // Two legs, each with three clear forward claws and one rear claw.
        for (int side : new int[]{-1, 1}) {
            PartDefinition leg = bird.addOrReplaceChild(side < 0 ? "left_foot" : "right_foot", CubeListBuilder.create(), PartPose.offset(side * 8, 2, 23));
            add(leg, "thigh", 112, 84, -4.5F, -2, -4, 9, 14, 10);
            add(leg, "shin", 20, 160, -3, 11, -6, 6, 12, 7);
            add(leg, "ankle", 20, 160, -4, 22, -7, 8, 4, 10);
            for (int toe = 0; toe < 3; toe++) {
                float x = -6 + toe * 4.5F;
                add(leg, "toe_" + toe, 20, 160, x, 24, -15 - (toe == 1 ? 3 : 0), 3.5F, 3, 11);
                add(leg, "talon_" + toe, 64, 194, x + .5F, 26, -20 - (toe == 1 ? 3 : 0), 2.5F, 6, 5);
            }
            add(leg, "rear_toe", 20, 160, -2, 24, 4, 4, 3, 9);
            add(leg, "rear_talon", 64, 194, -1.5F, 26, 10, 3, 5, 4);
        }

        PartDefinition tail = bird.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0, -7, 42));
        add(tail, "tail_root", 0, 229, -8, -2, -6, 16, 5, 13);
        for (int i = 0; i < 7; i++) {
            float x = -16 + i * 4.5F;
            float length = 16 + (3 - Math.abs(3 - i)) * 2;
            add(tail, "tail_feather_" + i, i % 2 == 0 ? 48 : 88, 227, x, 1, 3, 4.5F, 3.5F, length);
            add(tail, "tail_point_" + i, 216, 170, x + .8F, 3, length - 1, 2.9F, 6 + i % 3, 9);
        }

        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(MorovoyRaven entity, float stride, float strideAmount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        int pose = entity.pose();
        float time = entity.animationAge(age - (int) age);
        float speed = pose == MorovoyRaven.GROUND_IDLE ? .06F : pose == MorovoyRaven.DIVE ? .86F : pose == MorovoyRaven.TAKEOFF ? .67F : pose == MorovoyRaven.HOVER || pose == MorovoyRaven.PROJECTILE ? .23F : .47F;
        float strength = pose == MorovoyRaven.DIVE ? .10F : pose == MorovoyRaven.GROUND_IDLE ? .04F : .26F;

        // Four shoulder pivots and four delayed wrists; no shared rectangle rotation.
        mainLeft.zRot = .06F + (float) Math.sin(age * speed) * strength;
        mainRight.zRot = -.06F - (float) Math.sin(age * speed + .14F) * strength;
        upperLeft.zRot = .42F + (float) Math.sin(age * speed * .89F + .56F) * strength * .42F;
        upperRight.zRot = -.42F - (float) Math.sin(age * speed * .92F + .91F) * strength * .42F;
        mainLeftOuter.zRot = .05F + (float) Math.sin(age * speed - .68F) * .15F;
        mainRightOuter.zRot = -.04F - (float) Math.sin(age * speed - .50F) * .14F;
        upperLeftOuter.zRot = .13F + (float) Math.sin(age * speed * .89F + .10F) * .13F;
        upperRightOuter.zRot = -.11F - (float) Math.sin(age * speed * .92F + .31F) * .12F;
        mainLeft.xRot = .05F + (float) Math.sin(age * .13F) * .04F;
        mainRight.xRot = .07F + (float) Math.sin(age * .15F + .35F) * .04F;
        upperLeft.xRot = -.06F + (float) Math.cos(age * .14F) * .05F;
        upperRight.xRot = -.08F + (float) Math.cos(age * .16F + .2F) * .05F;

        bird.zRot = entity.bank();
        bird.xRot = pose == MorovoyRaven.DIVE ? .46F : pose == MorovoyRaven.HOVER ? .07F : .02F;
        head.yRot = (float) Math.toRadians(Math.clamp(yaw, -28, 28));
        head.xRot = (float) Math.toRadians(Math.clamp(pitch, -24, 24)) * .6F;
        jaw.xRot = pose == MorovoyRaven.BEAK && time > 7 && time < 22 ? .54F : .04F;
        leftFoot.xRot = pose == MorovoyRaven.CLAWS || pose == MorovoyRaven.DIVE ? -.65F : .16F;
        rightFoot.xRot = pose == MorovoyRaven.CLAWS || pose == MorovoyRaven.DIVE ? -.70F : .17F;
        tail.xRot = pose == MorovoyRaven.DIVE ? -.20F : .10F + (float) Math.sin(age * .10F) * .035F;

        if (pose == MorovoyRaven.SUMMON || pose == MorovoyRaven.WING) {
            mainLeft.zRot = .53F; mainRight.zRot = -.49F;
            upperLeft.zRot = .66F; upperRight.zRot = -.64F;
            mainLeftOuter.zRot = .19F; mainRightOuter.zRot = -.17F;
            upperLeftOuter.zRot = .24F; upperRightOuter.zRot = -.21F;
        }
        if (pose == MorovoyRaven.WING && time > 14) {
            mainLeft.zRot = -.36F; mainRight.zRot = .34F;
            upperLeft.zRot = .52F; upperRight.zRot = -.48F;
            mainLeftOuter.zRot = -.23F; mainRightOuter.zRot = .21F;
        }
        if (pose == MorovoyRaven.PROJECTILE) head.xRot = -.20F;

        if (pose == MorovoyRaven.GROUND_IDLE || pose == MorovoyRaven.LANDING || pose == MorovoyRaven.TAKEOFF) {
            float fold = pose == MorovoyRaven.GROUND_IDLE ? 1F : pose == MorovoyRaven.LANDING ? Math.clamp(time / 36F, 0F, 1F) : 1F - Math.clamp(time / 34F, 0F, 1F);
            mainLeft.yRot = 1.04F * fold; mainRight.yRot = -1.04F * fold;
            upperLeft.yRot = .83F * fold; upperRight.yRot = -.80F * fold;
            mainLeftOuter.yRot = .21F * fold; mainRightOuter.yRot = -.20F * fold;
            upperLeftOuter.yRot = .17F * fold; upperRightOuter.yRot = -.16F * fold;
            mainLeft.zRot = mainLeft.zRot * (1F - fold) + .20F * fold;
            mainRight.zRot = mainRight.zRot * (1F - fold) - .19F * fold;
            upperLeft.zRot = upperLeft.zRot * (1F - fold) + .48F * fold;
            upperRight.zRot = upperRight.zRot * (1F - fold) - .45F * fold;
        }
        if (entity.hurtTime > 0 && entity.deathTime == 0) {
            head.xRot -= .10F;
            mainLeft.zRot += .11F; mainRight.zRot -= .10F;
        }
        if (entity.deathTime > 0) {
            head.xRot = .43F; jaw.xRot = .25F;
            mainLeft.yRot = 1.13F; mainRight.yRot = -1.13F;
            upperLeft.yRot = .90F; upperRight.yRot = -.91F;
            mainLeft.zRot = .20F; mainRight.zRot = -.20F;
            upperLeft.zRot = .45F; upperRight.zRot = -.44F;
            leftFoot.xRot = .61F; rightFoot.xRot = .64F;
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer out, int light, int overlay, int tint) {
        root.render(matrices, out, light, overlay, tint);
    }
}
