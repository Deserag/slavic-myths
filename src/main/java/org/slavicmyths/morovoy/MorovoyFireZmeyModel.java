package org.slavicmyths.morovoy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class MorovoyFireZmeyModel extends EntityModel<MorovoyFireZmey> {
    private final ModelPart root = MorovoyFireZmeyMesh.create();
    protected final ModelPart body = root.getChild("body");
    private final ModelPart morBase = body.getChild("neck_left_base");
    private final ModelPart morMid = morBase.getChild("neck_left_mid");
    private final ModelPart morTip = morMid.getChild("neck_left_tip");
    private final ModelPart morHead = morTip.getChild("head_left");
    private final ModelPart fireBase = body.getChild("neck_right_base");
    private final ModelPart fireMid = fireBase.getChild("neck_right_mid");
    private final ModelPart fireTip = fireMid.getChild("neck_right_tip");
    private final ModelPart fireHead = fireTip.getChild("head_right");
    private float animationAge;
    private float flameIntensity;

    @Override
    public void setupAnim(MorovoyFireZmey boss, float limbSwing, float limbSwingAmount, float age, float netHeadYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        animationAge = age;
        boolean sleeping = boss.state() == MorovoyFireZmey.SLEEPING;
        boolean waking = boss.state() == MorovoyFireZmey.WAKING;
        boolean dying = boss.state() == MorovoyFireZmey.DYING;
        float wake = Mth.clamp(boss.stateAge() / 65.0F, 0.0F, 1.0F);
        float awake = sleeping ? 0.0F : waking ? wake : dying ? 0.0F : 1.0F;
        float hover = Mth.sin(age * 0.1F) * 0.2F;
        body.y = 12.0F + (sleeping ? 5.0F : waking ? Mth.lerp(wake, 5.0F, 2.0F) : dying ? Math.min(9.0F, boss.stateAge() * 0.12F) : -8.0F + hover);
        body.xRot = sleeping ? 0.25F : dying ? 0.35F : -0.08F * awake + Mth.sin(age * 0.045F) * 0.025F * awake;
        body.zRot = awake * (Mth.sin(age * 0.065F) * 0.035F + Mth.clamp((float)(boss.getDeltaMovement().x * Mth.cos(boss.getYRot() * Mth.DEG_TO_RAD) + boss.getDeltaMovement().z * Mth.sin(boss.getYRot() * Mth.DEG_TO_RAD)) * 0.7F, -.16F, .16F));

        // The two independent three-bone chains have distinct oscillators, frequencies and targets.
        animateMorNeck(boss, age, netHeadYaw, headPitch, sleeping, dying, awake);
        animateFireNeck(boss, age, netHeadYaw, headPitch, sleeping, dying, awake);

        int action = boss.action();
        int actionAge = boss.actionAge();
        boolean morAttack = action == MorovoyFireZmey.MOR_BOLT || action == MorovoyFireZmey.MOR_BREATH || action == MorovoyFireZmey.DOUBLE_BOLT || action == MorovoyFireZmey.BITE_MOR;
        boolean fireAttack = action == MorovoyFireZmey.FIRE_BOLT || action == MorovoyFireZmey.FIRE_BREATH || action == MorovoyFireZmey.DOUBLE_BOLT || action == MorovoyFireZmey.BITE_FIRE;
        morHead.getChild("jaw_left").xRot = morAttack && actionAge >= 8 && actionAge < 58 ? 0.52F + Mth.sin(actionAge * 0.2F) * 0.12F : 0.045F;
        fireHead.getChild("jaw_right").xRot = fireAttack && actionAge >= 8 && actionAge < 58 ? 0.61F + Mth.sin(actionAge * 0.24F + 1.2F) * 0.12F : 0.045F;
        flameIntensity = sleeping || dying ? 0.0F : waking ? wake * 0.45F : fireAttack ? 1.0F : 0.34F;

        for (String side : new String[]{"left", "right"}) {
            float sign = side.equals("left") ? -1.0F : 1.0F;
            float phase = side.equals("left") ? 0.0F : 1.13F;
            ModelPart wing = body.getChild("wing_" + side);
            ModelPart tip = wing.getChild("wing_elbow_" + side).getChild("wing_forearm_" + side).getChild("wing_wrist_" + side);
            float flap = Mth.sin(age * (boss.state() == MorovoyFireZmey.PHASE_II ? 0.24F : 0.18F) + phase);
            wing.zRot += sign * (sleeping ? 1.35F : waking ? Mth.lerp(wake, 1.35F, 0.15F) : dying ? 1.6F : flap * 0.52F);
            wing.yRot += sign * (sleeping ? 0.7F : dying ? 0.3F : 0.13F + Mth.sin(age * 0.11F + phase) * 0.14F);
            tip.zRot += sign * (sleeping ? 0.55F : dying ? 0.35F : flap * 0.28F);
            tip.xRot += sleeping ? 0.05F : Mth.cos(age * 0.12F + phase) * 0.09F * awake;

            for (int i = 0; i < 2; i++) {
                ModelPart leg = body.getChild("leg_" + side + "_" + i);
                leg.xRot = sleeping ? 0.50F : dying ? 0.15F : 0.80F + Mth.cos(age * 0.14F + phase + i * 1.9F) * 0.09F;
                leg.yRot = awake * sign * (0.16F + Mth.sin(age * 0.1F + i) * 0.05F);
                if (action == MorovoyFireZmey.CLAW && actionAge >= 8 && actionAge <= 26 && i == 0) leg.xRot -= 0.90F;
            }

            ModelPart tail = body.getChild("tail_" + side + "_0");
            for (int i = 0; i < 8; i++) {
                float tailAge = side.equals("left") ? age * 0.071F - i * 0.51F : age * 0.093F + 1.8F - i * 0.43F;
                tail.yRot = sign * (sleeping ? 0.3F : 0.06F) + Mth.sin(tailAge) * 0.12F;
                tail.xRot = awake * Mth.cos(tailAge * 0.9F) * 0.065F;
                tail.zRot = awake * Mth.sin(tailAge * 0.77F) * 0.05F;
                boolean striking = side.equals("left") ? action == MorovoyFireZmey.MOR_TAIL : action == MorovoyFireZmey.FIRE_TAIL;
                if (striking && actionAge >= 12 && actionAge <= 29) tail.yRot += sign * Mth.sin((actionAge - 12) / 17.0F * Mth.PI) * (0.3F + i * 0.08F);
                if (i < 7) tail = tail.getChild("tail_" + side + "_" + (i + 1));
            }
        }
    }

    private void animateMorNeck(MorovoyFireZmey boss, float age, float yaw, float pitch, boolean sleeping, boolean dying, float awake) {
        float leftClock = age * 0.071F + boss.getId() * 0.17F;
        float leftTargetYaw = Mth.clamp(boss.lookYaw() * Mth.DEG_TO_RAD, -0.85F, 0.85F) - 0.17F + Mth.sin(leftClock * 0.69F) * 0.15F;
        float leftTargetPitch = Mth.clamp(boss.lookPitch() * Mth.DEG_TO_RAD, -0.60F, 0.60F) + Mth.sin(leftClock * 0.81F) * 0.10F;
        morBase.xRot = sleeping ? 0.83F : dying ? 0.8F : Mth.lerp(awake, 0.83F, -0.11F + Mth.sin(leftClock) * 0.13F);
        morBase.yRot = sleeping ? -0.45F : Mth.lerp(awake, -0.45F, leftTargetYaw * 0.45F);
        morBase.zRot = awake * (-0.07F + Mth.sin(leftClock * 1.11F) * 0.10F);
        morMid.xRot = sleeping ? 0.55F : dying ? 0.5F : Mth.lerp(awake, 0.55F, -0.08F + leftTargetPitch * 0.45F + Mth.cos(leftClock * 0.73F) * 0.10F);
        morMid.yRot = awake * (leftTargetYaw * 0.35F + Mth.cos(leftClock * 0.92F) * 0.09F);
        morMid.zRot = awake * Mth.sin(leftClock * 0.63F) * 0.09F;
        morTip.xRot = awake * Mth.sin(leftClock * 1.13F) * .06F;
        morHead.xRot = awake * (leftTargetPitch * 0.55F + Mth.cos(leftClock * 1.23F) * 0.065F);
        morHead.yRot = awake * (leftTargetYaw * 0.20F + Mth.sin(leftClock * 1.49F) * 0.07F);
        morHead.zRot = awake * Mth.cos(leftClock * 0.84F) * 0.065F;
    }

    private void animateFireNeck(MorovoyFireZmey boss, float age, float yaw, float pitch, boolean sleeping, boolean dying, float awake) {
        float rightClock = age * 0.109F + boss.getId() * 0.31F + 1.83F;
        float rightTargetYaw = Mth.clamp(boss.lookYaw() * Mth.DEG_TO_RAD, -0.85F, 0.85F) + 0.19F + Mth.cos(rightClock * 0.73F) * 0.16F;
        float rightTargetPitch = Mth.clamp(boss.lookPitch() * Mth.DEG_TO_RAD, -0.60F, 0.60F) + Mth.sin(rightClock * 0.94F) * 0.13F;
        fireBase.xRot = sleeping ? 0.90F : dying ? 0.86F : Mth.lerp(awake, 0.90F, -0.05F + Mth.sin(rightClock) * 0.12F);
        fireBase.yRot = sleeping ? 0.45F : Mth.lerp(awake, 0.45F, rightTargetYaw * 0.43F);
        fireBase.zRot = awake * (0.07F + Mth.cos(rightClock * 0.87F) * 0.11F);
        fireMid.xRot = sleeping ? 0.48F : dying ? 0.52F : Mth.lerp(awake, 0.48F, -0.04F + rightTargetPitch * 0.43F + Mth.sin(rightClock * 0.79F) * 0.12F);
        fireMid.yRot = awake * (rightTargetYaw * 0.35F + Mth.sin(rightClock * 1.14F) * 0.10F);
        fireMid.zRot = awake * Mth.cos(rightClock * 1.06F) * 0.10F;
        fireTip.xRot = awake * Mth.sin(rightClock * 1.14F) * .09F;
        fireHead.xRot = awake * (rightTargetPitch * 0.57F + Mth.sin(rightClock * 1.32F) * 0.07F);
        fireHead.yRot = awake * (rightTargetYaw * 0.22F + Mth.cos(rightClock * 1.31F) * 0.08F);
        fireHead.zRot = awake * Mth.sin(rightClock * 0.91F) * 0.08F;
    }

    /** Only eyes and elemental crest emit light. The old full-neck/wing/tail emissive
     * pass repainted entire body sections neon green and purple. */
    public void renderElementalSide(PoseStack pose, VertexConsumer vertices, int light, int overlay, boolean fire) {
        ModelPart neckBase = fire ? fireBase : morBase;
        ModelPart neckMid = fire ? fireMid : morMid;
        ModelPart tip = fire ? fireTip : morTip;
        ModelPart head = fire ? fireHead : morHead;
        String side = fire ? "right" : "left";
        pose.pushPose();
        body.translateAndRotate(pose);
        neckBase.translateAndRotate(pose);
        neckMid.translateAndRotate(pose);
        tip.translateAndRotate(pose);
        head.translateAndRotate(pose);
        // Both neck fissures and wrist-rooted wing veins share the side's emissive pass.
        // Rendering only named narrow accent children prevents whole-body neon coloring.
        pose.popPose();

        pose.pushPose();
        body.translateAndRotate(pose);
        neckBase.translateAndRotate(pose);
        renderRingVeins(neckBase, "base", 6, pose, vertices, light, overlay);
        neckMid.translateAndRotate(pose);
        renderRingVeins(neckMid, "mid", 6, pose, vertices, light, overlay);
        tip.translateAndRotate(pose);
        renderRingVeins(tip, "tip", 5, pose, vertices, light, overlay);
        head.translateAndRotate(pose);
        head.getChild("eye_" + side + "_a").render(pose, vertices, light, overlay);
        head.getChild("eye_" + side + "_b").render(pose, vertices, light, overlay);
        for (int i = 0; i < 4; i++) head.getChild("crest_" + side + "_" + i).render(pose, vertices, light, overlay);
        pose.popPose();

        pose.pushPose();
        body.translateAndRotate(pose);
        ModelPart wing = body.getChild("wing_" + side);
        wing.translateAndRotate(pose);
        ModelPart elbow = wing.getChild("wing_elbow_" + side);
        elbow.translateAndRotate(pose);
        ModelPart forearm = elbow.getChild("wing_forearm_" + side);
        forearm.translateAndRotate(pose);
        ModelPart wrist = forearm.getChild("wing_wrist_" + side);
        wrist.translateAndRotate(pose);
        for (int finger = 0; finger < 3; finger++) {
            for (int k = 1; k < 12; k++) wrist.getChild("vein_" + finger + "_" + k).render(pose, vertices, light, overlay);
        }
        pose.popPose();
    }

    private void renderRingVeins(ModelPart neck, String segment, int count, PoseStack pose, VertexConsumer vertices, int light, int overlay) {
        for (int i = 0; i < count; i += 2) {
            pose.pushPose();
            ModelPart ring = neck.getChild(segment + "_ring_" + i);
            ring.translateAndRotate(pose);
            ring.getChild("vein").render(pose, vertices, light, overlay);
            pose.popPose();
        }
    }

    public void renderFireFlame(PoseStack pose, VertexConsumer vertices) {
        if (flameIntensity <= 0.0F) return;
        pose.pushPose();
        body.translateAndRotate(pose);
        fireBase.translateAndRotate(pose);
        fireMid.translateAndRotate(pose);
        fireTip.translateAndRotate(pose);
        fireHead.translateAndRotate(pose);
        pose.scale(0.0625F, 0.0625F, 0.0625F);
        float pulse = Mth.sin(animationAge * 0.33F) * 1.7F;
        float reach = (5.0F + 10.0F * flameIntensity) + pulse * .4F;
        drawFlameRibbon(vertices, pose, 3.6F * flameIntensity, -25.0F, -25.0F - reach, 0xCC260A49);
        drawFlameRibbon(vertices, pose, 2.5F * flameIntensity, -25.2F, -25.2F - reach * 0.79F, 0xEEAD21FF);
        drawFlameRibbon(vertices, pose, 1.1F * flameIntensity, -25.4F, -25.4F - reach * 0.61F, 0xFFFFFFFF);
        pose.popPose();
    }

    private void drawFlameRibbon(VertexConsumer vertices, PoseStack pose, float radius, float startZ, float endZ, int color) {
        float wave = Mth.sin(animationAge * 0.38F + radius) * 1.25F;
        for (int i = 0; i < 4; i++) {
            float a = i * Mth.HALF_PI;
            float dx = Mth.cos(a) * radius;
            float dy = Mth.sin(a) * radius;
            PoseStack.Pose transform = pose.last();
            vertices.addVertex(transform, -dx, -dy, startZ).setColor(color);
            vertices.addVertex(transform, dx, dy, startZ).setColor(color);
            vertices.addVertex(transform, wave + dx * 0.12F, -1.0F + dy * 0.12F, endZ).setColor(color);
            vertices.addVertex(transform, wave - dx * 0.12F, -1.0F - dy * 0.12F, endZ).setColor(color);
        }
    }

    @Override
    public void renderToBuffer(PoseStack matrices, VertexConsumer out, int light, int overlay, int color) {
        root.render(matrices, out, light, overlay, color);
    }
}
