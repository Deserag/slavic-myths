package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.posad.boss.PosadBoss;

/** Independently rigged 2.2 boss silhouettes: Mytar two armed humanoid, Bauk four-legged predator. */
public final class PosadBossModel extends EntityModel<PosadBoss> {
    private final FolkModelGeometry geometry = new FolkModelGeometry();
    private final ModelPart root = geometry.part(128, 128);
    private final ModelPart head = geometry.part(128, 128);
    private final ModelPart jaw = geometry.part(128, 128);
    private final ModelPart rightArm = geometry.part(128, 128);
    private final ModelPart leftSickle = geometry.part(128, 128);
    private final ModelPart chain = geometry.part(128, 128);
    private final ModelPart chainSickle = geometry.part(128, 128);
    private final ModelPart leftArm = geometry.part(128, 128);
    private final ModelPart[] legs = {geometry.part(128, 128), geometry.part(128, 128), geometry.part(128, 128), geometry.part(128, 128)};
    private final boolean bauk;
    private void cub(ModelPart part, int u, int v, float x, float y, float z, float w, float h, float d) { geometry.box(part, u, v, x, y, z, w, h, d); }
    public PosadBossModel(boolean bauk) {
        this.bauk = bauk;
        geometry.attach(root, head);
        geometry.attach(head, jaw);
        if (bauk) {
            cub(root, 0, 0, -9, -24, -16, 18, 17, 34); // Huge elongated body
            cub(root, 0, 36, -10, -27, -12, 20, 13, 12); // Broad shoulders and mane
            cub(root, 68, 28, -9, -27, 7, 18, 9, 9); // Hump
            head.setPos(0, -20, -17);
            cub(head, 0, 63, -8, -8, -11, 16, 13, 15);
            cub(head, 64, 63, -6, -3, -17, 12, 7, 9); // Predatory snout
            cub(head, 102, 63, -7, -10, -3, 3, 7, 4); // Left bone growth
            cub(head, 112, 63, 4, -12, -2, 3, 8, 4); // Right asymmetric horn
            cub(head, 102, 63, -8, -15, -3, 4, 6, 4); // Left horn thick root
            cub(head, 102, 63, -10, -19, -4, 3, 6, 3); // Left horn sweeps outward
            cub(head, 102, 63, -12, -22, -5, 2, 5, 2); // Left horn taper
            cub(head, 112, 63, 5, -15, -3, 4, 6, 4); // Right horn thick root
            cub(head, 112, 63, 8, -19, -4, 3, 6, 3); // Right horn sweeps outward
            cub(head, 112, 63, 10, -21, -5, 2, 5, 2); // Right horn taper
            cub(head, 88, 81, -6, -1, -18, 2, 3, 3); // Fang
            cub(head, 97, 81, 4, -1, -18, 2, 3, 3);
            jaw.setPos(0, 3, -7);
            cub(jaw, 0, 95, -7, 0, -10, 14, 4, 17); // Separately animated jaw
            for (int i = 0; i < legs.length; i++) {
                ModelPart leg = legs[i];
                geometry.attach(root, leg);
                leg.setPos(i % 2 == 0 ? -6 : 6, -12, i < 2 ? -10 : 12);
                cub(leg, 70, 95, -3.5F, 0, -3.5F, 7, 12, 7);
                cub(leg, 102, 99, -4, 10, -5, 8, 3, 9);
                cub(leg, 0, 116, -3, 11, -7, 2, 3, 4); // Heavy claw
            }
            cub(root, 38, 113, -3, -20, 15, 6, 7, 8); // Raised tail
        } else {
            cub(root, 0, 0, -5, -29, -3.5F, 10, 18, 7); // Robe, separate Steve-like torso
            cub(root, 38, 0, -6.5F, -30, -5, 13, 7, 9); // Mantle
            cub(root, 0, 32, -7, -12, -5, 14, 12, 10); // Torn lower robes
            head.setPos(0, -30, 0);
            cub(head, 51, 0, -5, -10, -5, 10, 10, 10); // Gaunt face
            cub(head, 91, 0, -7, -12, -7, 14, 12, 12); // Overhanging hood
            cub(head, 0, 54, -4, -7, -7.5F, 8, 4, 1); // Shaded inset eye region
            for (int i = 0; i < 2; i++) {
                ModelPart arm = i == 0 ? rightArm : leftArm;
                geometry.attach(root, arm);
                arm.setPos(i == 0 ? -6 : 6, -27, 0);
                cub(arm, 30, 54, i == 0 ? -3 : 0, 0, -3, 3, 23, 6); // Skeletal long arms
                cub(arm, 51, 54, i == 0 ? -3 : 0, 19, -3, 3, 4, 6);
                // Weapons use separate articulated rigs instead of identical straight hooks.
            }
            for (int i = 0; i < 2; i++) {
                ModelPart leg = legs[i];
                geometry.attach(root, leg);
                leg.setPos(i == 0 ? -3 : 3, -10, 0);
                cub(leg, 0, 78, -3, 0, -3, 6, 10, 7);
            }
            cub(root, 40, 78, -5, -16, -5, 3, 6, 2); // Three separate coin purses
            cub(root, 50, 78, -1, -17, -5, 3, 6, 2);
            cub(root, 60, 78, 3, -16, -5, 3, 6, 2);
            geometry.attach(leftArm, leftSickle);
            leftSickle.setPos(2, 21, -5);
            cub(leftSickle, 74, 54, -1, 0, 0, 2, 12, 2); // Short left-hand sickle grip
            cub(leftSickle, 74, 54, -2, -2, -1, 4, 3, 4); // Guard
            cub(leftSickle, 74, 54, -2, 10, -1, 6, 2, 2); // Sickle crescent base
            cub(leftSickle, 74, 54, 2, 7, -1, 3, 4, 2);
            cub(leftSickle, 74, 54, 4, 4, -1, 3, 4, 2);
            cub(leftSickle, 74, 54, 5, 1, -1, 2, 4, 2); // Curved blade point
            geometry.attach(rightArm, chain);
            chain.setPos(-2, 22, -5);
            cub(chain, 74, 54, -1, -4, 0, 2, 7, 2); // Right-hand chain grip
            for (int i = 0; i < 9; i++) cub(chain, 74, 54, i % 2 == 0 ? -1.5F : -1F, 3 + i * 2.5F, -1, 3, 2.5F, 2); // Visible articulated chain links
            geometry.attach(chain, chainSickle);
            chainSickle.setPos(0, 25, -1);
            cub(chainSickle, 74, 54, -1, 0, 0, 2, 9, 2); // Heavy hooked sickle shaft
            cub(chainSickle, 74, 54, -3, 8, -1, 8, 2, 3);
            cub(chainSickle, 74, 54, 3, 5, -1, 3, 4, 3);
            cub(chainSickle, 74, 54, 5, 2, -1, 3, 4, 3);
            cub(chainSickle, 74, 54, 6, -1, -1, 2, 4, 3); // Distinct sickle curve and sharpened tip
        }
        // Root is measured from feet at y=0; Minecraft's ModelPart plane is y=24.
        root.setPos(0F, 24F, 0F);\n        root.getAllParts().forEach(part -> part.setInitialPose(part.storePose()));
    }
    @Override public void setupAnim(PosadBoss entity, float swing, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        head.yRot = yaw * Mth.DEG_TO_RAD * .45F;
        head.xRot = pitch * Mth.DEG_TO_RAD * .35F;
        float gait = Mth.cos(swing * .65F) * Math.min(1F, amount);
        if (bauk) {
            legs[0].xRot = gait * .55F;
            legs[1].xRot = -gait * .55F;
            legs[2].xRot = -gait * .55F;
            legs[3].xRot = gait * .55F;
            jaw.xRot = entity.move() == PosadBoss.Move.WINDUP_BITE ? .7F : entity.move() == PosadBoss.Move.WINDUP_ROAR ? .85F : .12F + .08F * Mth.sin(age * .15F);
            if (entity.move() == PosadBoss.Move.WINDUP_CHARGE) root.xRot = -.18F;
            if (entity.move() == PosadBoss.Move.WINDUP_SLAM) { root.xRot = -.32F; legs[0].xRot = -.7F; legs[1].xRot = -.7F; }
            if (entity.enraged()) head.xRot += -.1F;
        } else {
            legs[0].xRot = gait * .6F;
            legs[1].xRot = -gait * .6F;
            boolean throwingChain = entity.move() == PosadBoss.Move.WINDUP_CHAIN;
            rightArm.xRot = throwingChain ? -1.8F : entity.move() == PosadBoss.Move.WINDUP_SICKLE ? -1.1F : -.2F + Mth.sin(age * .08F) * .08F;
            leftArm.xRot = entity.move() == PosadBoss.Move.WINDUP_SICKLE ? -1.4F : .1F;
            chain.xRot = throwingChain ? -.95F + .18F * Mth.sin(age * .22F) : .07F * Mth.sin(age * .12F);
            chain.zRot = throwingChain ? -.12F : 0F;
            chainSickle.xRot = throwingChain ? -.28F : 0F;
            if (entity.phase() == 2) { rightArm.zRot = -.2F; head.zRot = .04F * Mth.sin(age * .16F); }
        }
    }
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay, int color) { root.render(pose, vertices, light, overlay, color); }
}
