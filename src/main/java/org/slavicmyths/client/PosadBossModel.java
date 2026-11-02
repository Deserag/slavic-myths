package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.posad.boss.PosadBoss;

/** Authored voxel bone hierarchy for Issue 389; generated from editable materials-and-cuboids rig sources. */
public final class PosadBossModel extends EntityModel<PosadBoss> {
    private final FolkModelGeometry geometry = new FolkModelGeometry();
    private ModelPart part() { return geometry.part(128, 128); }
    private final ModelPart root = part();
    private final ModelPart head = part();
    private final ModelPart jaw = part();
    private final ModelPart rightArm = part();
    private final ModelPart leftArm = part();
    private final ModelPart leftSickle = part();
    private final ModelPart chain = part();
    private final ModelPart chainSickle = part();
    private final ModelPart cloth = part();
    private final ModelPart mane = part();
    private final ModelPart tail = part();
    private final ModelPart leg0 = part();
    private final ModelPart leg1 = part();
    private final ModelPart leg2 = part();
    private final ModelPart leg3 = part();
    private final ModelPart[] legs = {leg0, leg1, leg2, leg3};
    private final boolean bauk;
    private void cub(ModelPart part, int u, int v, float x, float y, float z, float w, float h, float d) { geometry.box(part, u, v, x, y, z, w, h, d); }
    public PosadBossModel(boolean bauk) {
        this.bauk = bauk;
        geometry.attach(root, head);
        geometry.attach(head, jaw);
        geometry.attach(root, rightArm);
        geometry.attach(root, leftArm);
        geometry.attach(leftArm, leftSickle);
        geometry.attach(rightArm, chain);
        geometry.attach(chain, chainSickle);
        geometry.attach(root, cloth);
        geometry.attach(root, mane);
        geometry.attach(root, tail);
        for (ModelPart leg : legs) geometry.attach(root, leg);
        if (bauk) {
            head.setPos(0, -32, -32); jaw.setPos(0, 5, -13);
            mane.setPos(0, -34, -13); tail.setPos(0, -24, 37);
            leg0.setPos(17, -19, -23); leg1.setPos(-17, -19, -23);
            leg2.setPos(17, -19, 27); leg3.setPos(-17, -19, 27);
            cub(root, 0, 0, -21, -45, -27, 42, 30, 63);
            cub(root, 0, 0, -22, -48, -24, 44, 26, 30);
            cub(head, 32, 32, -14, -13, -16, 28, 21, 25);
            cub(head, 32, 32, -9, -4, -28, 18, 12, 17);
            cub(jaw, 32, 32, -9, 0, -20, 18, 7, 23);
            cub(mane, 0, 0, -22, -13, -12, 44, 18, 29);
            cub(tail, 0, 0, -5, -6, 0, 10, 9, 18);
            for(ModelPart leg : legs) {
                cub(leg, 0, 0, -8, 0, -8, 16, 17, 17);
                cub(leg, 32, 32, -10, 16, -12, 20, 5, 22);
            }
        } else {
            // Player-like anatomy under a bulky tattered hooded robe, floating over mist rather than legs.
            head.setPos(0, -53, 0);
            rightArm.setPos(-13, -53, 0); leftArm.setPos(13, -53, 0);
            leftSickle.setPos(2, 25, -3); chain.setPos(-2, 25, -3); chainSickle.setPos(0, -9, 0);
            cloth.setPos(0, -28, 0); mane.setPos(0, -52, 0);
            cub(root, 0, 0, -11, -51, -7, 22, 34, 14);
            cub(root, 0, 0, -13, -48, -8, 26, 15, 16);
            cub(head, 32, 32, -10, -12, -10, 20, 20, 19);
            cub(head, 0, 0, -12, -14, -12, 24, 7, 23);
            cub(head, 0, 0, -12, -8, -12, 5, 16, 23);
            cub(head, 0, 0, 7, -8, -12, 5, 16, 23);
            cub(cloth, 0, 0, -13, -12, -9, 26, 25, 18);
            cub(cloth, 0, 0, -11, 12, -8, 22, 13, 16);
            cub(mane, 0, 0, -13, -5, -8, 26, 8, 16);
            cub(rightArm, 32, 32, -6, 0, -5, 8, 26, 10);
            cub(leftArm, 32, 32, -2, 0, -5, 8, 26, 10);
            // Two tapering wisps replace feet; animated separately from the upper body.
            leg0.setPos(-5, -10, 0); leg1.setPos(5, -10, 0);
            cub(leg0, 0, 0, -5, 0, -5, 9, 11, 9);
            cub(leg1, 0, 0, -4, 0, -5, 9, 12, 9);
            cub(leftSickle, 32, 32, -1, -13, 0, 2, 14, 2);
            cub(leftSickle, 32, 32, 0, -15, 0, 9, 3, 2);
            cub(chain, 32, 32, -1, -9, 0, 2, 10, 2);
            cub(chainSickle, 32, 32, -1, -12, 0, 2, 13, 2);
            cub(chainSickle, 32, 32, 0, -14, 0, 9, 3, 2);
        }
        root.setPos(0F, 24F, 0F);
        root.getAllParts().forEach(part -> part.setInitialPose(part.storePose()));
    }
    @Override public void setupAnim(PosadBoss entity, float limbSwing, float limbAmount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float stride = Math.min(1F, limbAmount);
        float gait = Mth.cos(limbSwing * .67F) * stride;
        float breath = Mth.sin(age * .13F);
        float remaining = entity.actionRemaining();
        head.yRot = yaw * Mth.DEG_TO_RAD * (bauk ? .32F : .55F);
        head.xRot = pitch * Mth.DEG_TO_RAD * .25F;
        if (bauk) {
            leg0.xRot = gait * .32F;
            leg1.xRot = -gait * .32F;
            leg2.xRot = -gait * .27F;
            leg3.xRot = gait * .27F;
            leg0.y -= Math.max(0F, gait) * .28F;
            leg1.y += Math.min(0F, gait) * .28F;
            leg2.y += Math.min(0F, gait) * .2F;
            leg3.y -= Math.max(0F, gait) * .2F;
            head.xRot += breath * .013F;
            mane.xRot = breath * .018F;
            cloth.xRot = breath * .009F;
            tail.yRot = Mth.sin(age * .06F) * .08F;
            jaw.xRot = entity.move() == PosadBoss.Move.WINDUP_BITE || entity.move() == PosadBoss.Move.WINDUP_GRAB ? .74F : entity.move() == PosadBoss.Move.WINDUP_ROAR || entity.move() == PosadBoss.Move.RAGE_TRANSITION ? .95F : .09F + .04F * breath;
            if (entity.move() == PosadBoss.Move.WINDUP_BITE) jaw.xRot = remaining > 18 ? .12F + .68F * Mth.clamp((32F - remaining) / 14F, 0F, 1F) : .10F + .70F * Mth.clamp(remaining / 18F, 0F, 1F);
            if (entity.move() == PosadBoss.Move.WINDUP_GRAB) jaw.xRot = remaining > 16 ? .18F + .72F * Mth.clamp((38F - remaining) / 22F, 0F, 1F) : .15F;
            if (entity.move() == PosadBoss.Move.WINDUP_CHARGE) { head.xRot -= remaining > 14 ? .25F * Mth.clamp((44F - remaining) / 30F, 0F, 1F) : .32F; tail.xRot = -.18F; if (remaining <= 14F) { leg0.xRot *= 2F; leg1.xRot *= 2F; leg2.xRot *= 2F; leg3.xRot *= 2F; } }
            if (entity.move() == PosadBoss.Move.WINDUP_PAW) { leg0.xRot = -.55F; head.yRot += .1F; }
            if (entity.move() == PosadBoss.Move.WINDUP_SLAM) { leg0.xRot = -.8F; leg1.xRot = -.8F; head.xRot -= .12F; }
            if (entity.enraged()) { head.xRot -= .09F; tail.yRot *= 2F; }
        } else {
            leg0.xRot = gait * .55F;
            leg1.xRot = -gait * .55F;
            cloth.yRot = .025F * Mth.sin(age * .09F);
            mane.xRot = .018F * breath;
            chainSickle.visible = entity.move() != PosadBoss.Move.WINDUP_CHAIN || entity.actionRemaining() > 24;
            rightArm.xRot = -.15F + .055F * breath;
            leftArm.xRot = .1F - .07F * breath;
            if (entity.move() == PosadBoss.Move.WINDUP_SICKLE) { leftArm.xRot = -1.45F * Mth.clamp((38F - remaining) / 24F, 0F, 1F); rightArm.xRot = -.75F; }
            if (entity.move() == PosadBoss.Move.WINDUP_COINS) { rightArm.xRot = -.5F; leftArm.zRot = Mth.sin(age * .35F) * .24F; }
            if (entity.move() == PosadBoss.Move.WINDUP_CHAIN) { float progress = Mth.clamp((50F - remaining) / 24F, 0F, 1F); rightArm.xRot = -1.75F * progress; rightArm.zRot = .2F * Mth.sin(age * .34F); chain.xRot = remaining > 26F ? -.9F * progress + .42F * Mth.sin(age * .55F) : -1.25F; }
            else chain.xRot = .045F * Mth.sin(age * .12F);
            chainSickle.yRot = entity.move() == PosadBoss.Move.WINDUP_CHAIN ? age * .23F : 0F;
            head.zRot = entity.phase() == 2 ? .05F * Mth.sin(age * .14F) : 0F;
            if (entity.hurtTime > 0) { head.xRot -= .14F; root.xRot += .045F; }
            if (entity.deathTime > 0) { root.zRot = Math.min(1.3F, entity.deathTime * .07F); leftArm.xRot = -.15F; }
        }
        if (bauk && entity.hurtTime > 0) head.yRot += .12F;
        if (bauk && entity.deathTime > 0) { root.zRot = Math.min(1.1F, entity.deathTime * .055F); jaw.xRot = .7F; }
    }
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay, int color) { root.render(pose, vertices, light, overlay, color); }
}
