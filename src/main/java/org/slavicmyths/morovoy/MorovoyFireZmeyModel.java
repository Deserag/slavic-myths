package org.slavicmyths.morovoy;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public final class MorovoyFireZmeyModel extends EntityModel<MorovoyFireZmey> {
    private final ModelPart root = MorovoyFireZmeyMesh.create();
    private final ModelPart body = root.getChild("body");
    private final ModelPart morBase = body.getChild("neck_left_base");
    private final ModelPart morMid = morBase.getChild("neck_left_mid");
    private final ModelPart morHead = morMid.getChild("head_left");
    private final ModelPart fireBase = body.getChild("neck_right_base");
    private final ModelPart fireMid = fireBase.getChild("neck_right_mid");
    private final ModelPart fireHead = fireMid.getChild("head_right");
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
        body.y = 12.0F + (sleeping ? 5.0F : waking ? Mth.lerp(wake, 5.0F, -8.0F) : dying ? Math.min(9.0F, boss.stateAge() * 0.12F) : -8.0F + hover);
        body.xRot = sleeping ? 0.25F : dying ? 0.35F : -0.08F * awake + Mth.sin(age * 0.045F) * 0.025F * awake;
        body.zRot = awake * Mth.sin(age * 0.065F) * 0.035F;

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
            ModelPart tip = wing.getChild("wing_tip_" + side);
            float flap = Mth.sin(age * (boss.state() == MorovoyFireZmey.PHASE_II ? 0.24F : 0.18F) + phase);
            wing.zRot = sign * (sleeping ? 1.35F : waking ? Mth.lerp(wake, 1.35F, 0.15F) : dying ? 1.6F : flap * 0.52F);
            wing.yRot = sign * (sleeping ? 0.7F : dying ? 0.3F : 0.13F + Mth.sin(age * 0.11F + phase) * 0.14F);
            tip.zRot = sign * (sleeping ? 0.55F : dying ? 0.35F : flap * 0.28F);
            tip.xRot = sleeping ? 0.05F : Mth.cos(age * 0.12F + phase) * 0.09F * awake;

            for (int i = 0; i < 2; i++) {
                ModelPart leg = body.getChild("leg_" + side + "_" + i);
                leg.xRot = sleeping ? 0.50F : dying ? 0.15F : 0.80F + Mth.cos(age * 0.14F + phase + i * 1.9F) * 0.09F;
                leg.yRot = awake * sign * (0.16F + Mth.sin(age * 0.1F + i) * 0.05F);
                if (action == MorovoyFireZmey.CLAW && actionAge >= 8 && actionAge <= 26 && i == 0) leg.xRot -= 0.90F;
            }

            ModelPart tail = body.getChild("tail_" + side + "_0");
            for (int i = 0; i < 6; i++) {
                float tailAge = side.equals("left") ? age * 0.071F - i * 0.51F : age * 0.093F + 1.8F - i * 0.43F;
                tail.yRot = sign * (sleeping ? 0.3F : 0.06F) + Mth.sin(tailAge) * 0.12F;
                tail.xRot = awake * Mth.cos(tailAge * 0.9F) * 0.065F;
                tail.zRot = awake * Mth.sin(tailAge * 0.77F) * 0.05F;
                boolean striking = side.equals("left") ? action == MorovoyFireZmey.MOR_TAIL : action == MorovoyFireZmey.FIRE_TAIL;
                if (striking && actionAge >= 12 && actionAge <= 29) tail.yRot += sign * Mth.sin((actionAge - 12) / 17.0F * Mth.PI) * (0.3F + i * 0.08F);
                if (i < 5) tail = tail.getChild("tail_" + side + "_" + (i + 1));
            }
        }
    }

    private void animateMorNeck(MorovoyFireZmey boss, float age, float yaw, float pitch, boolean sleeping, boolean dying, float awake) {
        float leftClock = age * 0.071F + boss.getId() * 0.17F;
        float leftTargetYaw = Mth.clamp(yaw * Mth.DEG_TO_RAD, -0.55F, 0.55F) - 0.17F + Mth.sin(leftClock * 0.69F) * 0.15F;
        float leftTargetPitch = Mth.clamp(pitch * Mth.DEG_TO_RAD, -0.38F, 0.38F) + Mth.sin(leftClock * 0.81F) * 0.10F;
        morBase.xRot = sleeping ? 0.83F : dying ? 0.8F : Mth.lerp(awake, 0.83F, -0.11F + Mth.sin(leftClock) * 0.13F);
        morBase.yRot = sleeping ? -0.45F : Mth.lerp(awake, -0.45F, leftTargetYaw * 0.45F);
        morBase.zRot = awake * (-0.07F + Mth.sin(leftClock * 1.11F) * 0.10F);
        morMid.xRot = sleeping ? 0.55F : dying ? 0.5F : Mth.lerp(awake, 0.55F, -0.08F + leftTargetPitch * 0.45F + Mth.cos(leftClock * 0.73F) * 0.10F);
        morMid.yRot = awake * (leftTargetYaw * 0.35F + Mth.cos(leftClock * 0.92F) * 0.09F);
        morMid.zRot = awake * Mth.sin(leftClock * 0.63F) * 0.09F;
        morHead.xRot = awake * (leftTargetPitch * 0.55F + Mth.cos(leftClock * 1.23F) * 0.065F);
        morHead.yRot = awake * (leftTargetYaw * 0.20F + Mth.sin(leftClock * 1.49F) * 0.07F);
        morHead.zRot = awake * Mth.cos(leftClock * 0.84F) * 0.065F;
    }

    private void animateFireNeck(MorovoyFireZmey boss, float age, float yaw, float pitch, boolean sleeping, boolean dying, float awake) {
        float rightClock = age * 0.109F + boss.getId() * 0.31F + 1.83F;
        float rightTargetYaw = Mth.clamp(yaw * Mth.DEG_TO_RAD, -0.55F, 0.55F) + 0.19F + Mth.cos(rightClock * 0.73F) * 0.16F;
        float rightTargetPitch = Mth.clamp(pitch * Mth.DEG_TO_RAD, -0.38F, 0.38F) + Mth.sin(rightClock * 0.94F) * 0.13F;
        fireBase.xRot = sleeping ? 0.90F : dying ? 0.86F : Mth.lerp(awake, 0.90F, -0.05F + Mth.sin(rightClock) * 0.12F);
        fireBase.yRot = sleeping ? 0.45F : Mth.lerp(awake, 0.45F, rightTargetYaw * 0.43F);
        fireBase.zRot = awake * (0.07F + Mth.cos(rightClock * 0.87F) * 0.11F);
        fireMid.xRot = sleeping ? 0.48F : dying ? 0.52F : Mth.lerp(awake, 0.48F, -0.04F + rightTargetPitch * 0.43F + Mth.sin(rightClock * 0.79F) * 0.12F);
        fireMid.yRot = awake * (rightTargetYaw * 0.35F + Mth.sin(rightClock * 1.14F) * 0.10F);
        fireMid.zRot = awake * Mth.cos(rightClock * 1.06F) * 0.10F;
        fireHead.xRot = awake * (rightTargetPitch * 0.57F + Mth.sin(rightClock * 1.32F) * 0.07F);
        fireHead.yRot = awake * (rightTargetYaw * 0.22F + Mth.cos(rightClock * 1.31F) * 0.08F);
        fireHead.zRot = awake * Mth.sin(rightClock * 0.91F) * 0.08F;
    }

    public void renderElementalSide(PoseStack pose, VertexConsumer vertices, int light, int overlay, boolean fire) {
        ModelPart base = fire ? fireBase : morBase;
        String side = fire ? "right" : "left";
        ModelPart wing = body.getChild("wing_" + side);
        ModelPart tail = body.getChild("tail_" + side + "_0");
        pose.pushPose();
        body.translateAndRotate(pose);
        base.render(pose, vertices, light, overlay);
        wing.render(pose, vertices, light, overlay);
        tail.render(pose, vertices, light, overlay);
        pose.popPose();
    }

    public void renderFireFlame(PoseStack pose, VertexConsumer vertices) {
        if (flameIntensity <= 0.0F) return;
        pose.pushPose();
        body.translateAndRotate(pose);
        fireBase.translateAndRotate(pose);
        fireMid.translateAndRotate(pose);
        fireHead.translateAndRotate(pose);
        pose.scale(0.0625F, 0.0625F, 0.0625F);
        float pulse = Mth.sin(animationAge * 0.33F) * 1.7F;
        float reach = (9.0F + 18.0F * flameIntensity) + pulse;
        drawFlameRibbon(vertices, pose, 7.3F * flameIntensity, -25.0F, -25.0F - reach, 0xCC260A49);
        drawFlameRibbon(vertices, pose, 4.8F * flameIntensity, -25.2F, -25.2F - reach * 0.79F, 0xEEAD21FF);
        drawFlameRibbon(vertices, pose, 2.0F * flameIntensity, -25.4F, -25.4F - reach * 0.61F, 0xFFFFFFFF);
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
