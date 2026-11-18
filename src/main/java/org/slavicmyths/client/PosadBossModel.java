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
    private ModelPart part() { return geometry.part(256, 256); }
    private final ModelPart root = part();
    private final ModelPart head = part();
    private final ModelPart jaw = part();
    private final ModelPart rightArm = part();
    private final ModelPart leftArm = part();
    private final ModelPart leftSickle = part();
    private final ModelPart chain = part();
    private final ModelPart chainSickle = part();
    private final ModelPart[] links = new ModelPart[70];
    private final ModelPart[] animatedParts;
    private int visibleLinks;
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
            head.setPos(0, -32, -32); jaw.setPos(0, 5, -10);
            mane.setPos(0, -34, -13); tail.setPos(0, -24, 37);
            leg0.setPos(17, -19, -23); leg1.setPos(-17, -19, -23);
            leg2.setPos(17, -19, 27); leg3.setPos(-17, -19, 27);
            cub(root, 0, 0, -21, -43, -31, 42, 28, 70);
            cub(root, 0, 0, -20, -47, -30, 40, 4, 66);
            cub(root, 128, 176, -19, -48, -13, 38, 1, 20);
            cub(root, 128, 176, -21.6F, -41, -13, .6F, 9, 20);
            cub(root, 128, 176, 21, -41, -13, .6F, 9, 20);
            cub(head, 32, 32, -14, -13, -16, 28, 21, 25);
            cub(head, 0, 0, -16, -22, 1, 6, 9, 5);
            cub(head, 0, 0, 10, -22, 1, 6, 9, 5);
            cub(head, 32, 32, -10, -4, -22, 20, 10, 9);
            cub(head, 224, 246, -5, -5, -23, 10, 4, 2);
            cub(jaw, 32, 32, -9, 0, -11, 18, 5, 12);
            cub(mane, 0, 0, -22, -12, -17, 44, 7, 23);
            for (int side : new int[]{-1, 1}) {
                for (int step = 0; step < 5; step++) {
                    int size = 6 - step / 2;
                    cub(head, 224, 224, side * (10 + step * 3) - size / 2F,
                        -15 - step * 4 - (side > 0 ? step / 2 : 0), -8 + step, size, 5, size);
                }
            }
            cub(tail, 0, 0, -4, -4, 0, 8, 7, 8);
            for (int side : new int[]{-1, 1}) {
                cub(head, 246, 240, side * 8 - 1, -6, -16.2F, 2, 2, 1);
            }
            for(ModelPart leg : legs) {
                cub(leg, 0, 0, -8, 0, -8, 16, 17, 17);
                cub(leg, 32, 32, -10, 16, -12, 20, 5, 22);
                for (int claw = -6; claw <= 6; claw += 6) {
                    cub(leg, 224, 224, claw - 1, 19, -14, 2, 2, 2);
                }
            }
        } else {
            head.setPos(0, -46, 0);
            rightArm.setPos(-6.5F, -43, 0);
            leftArm.setPos(6.5F, -43, 0);
            leftSickle.setPos(0, 17, -1.2F);
            leftSickle.zRot = .24F;
            chain.setPos(0, 13.4F, -1.2F);

            cub(root, 0, 0, -4.5F, -44, -2.6F, 9, 20, 5.4F);
            cub(root, 64, 0, -5.5F, -43, -3, 11, 4, 6.2F);
            cub(cloth, 64, 0, -5.7F, -43, 2.8F, 11.4F, 23, 2);
            cub(cloth, 64, 0, -5.5F, -20, 3.4F, 11, 11, 2);
            cub(cloth, 0, 0, -5.2F, -25, -2.7F, 10.4F, 14, 6.1F);
            cub(cloth, 64, 0, -5.2F, -11, -2.7F, 2.7F, 3.4F, 6.1F);
            cub(cloth, 0, 0, -2.5F, -11, -2.7F, 3.8F, 6.5F, 7.4F);
            cub(cloth, 64, 0, 1.3F, -11, -2.7F, 3.9F, 4.8F, 7.9F);
            cub(cloth, 64, 0, -5.5F, -9, 4.1F, 3, 2, 1.3F);
            cub(cloth, 64, 0, 2.1F, -9, 4.1F, 3.4F, 3.8F, 1.3F);

            cub(head, 64, 0, -5, -9, -4.6F, 10, 3, 9);
            cub(head, 0, 0, -4, -10, -3.5F, 8, 1, 7);
            cub(head, 0, 0, -5, -6, 2.2F, 10, 9, 2.2F);
            cub(head, 64, 0, -5, -6, -4.6F, 2.3F, 8.4F, 6.8F);
            cub(head, 64, 0, 2.7F, -6, -4.6F, 2.3F, 7.5F, 6.8F);
            cub(head, 96, 0, -2.7F, -6, -2.8F, 5.4F, 4.4F, 4);
            cub(head, 96, 0, -1.85F, -1.6F, -2.5F, 3.7F, 2.8F, 3);
            for (int side : new int[]{-1, 1}) {
                cub(head, 120, 0, side * 1.35F - .7F, -4.2F, -2.95F, 1.4F, 1.2F, .16F);
            }
            cub(head, 120, 0, -.38F, -2.7F, -2.96F, .76F, .9F, .17F);
            for (int tooth = -1; tooth <= 1; tooth++) {
                cub(head, 120, 0, tooth - .1F, -.85F, -2.66F, .2F, 1.1F, .17F);
            }

            for (ModelPart arm : new ModelPart[]{leftArm, rightArm}) {
                cub(arm, 0, 0, -1.55F, 0, -1.95F, 3.1F, 11, 3.9F);
                cub(arm, 64, 0, -2.2F, 11, -2.55F, .85F, 4.1F, 5.1F);
                cub(arm, 64, 0, 1.35F, 11, -2.55F, .85F, 4.1F, 5.1F);
                cub(arm, 64, 0, -1.35F, 11, -2.55F, 2.7F, 3.6F, .85F);
                cub(arm, 64, 0, -1.35F, 11, 1.7F, 2.7F, 4.2F, .85F);
                cub(arm, 96, 0, -.92F, 15.1F, -1.85F, 1.84F, 3.2F, 1.4F);
                cub(arm, 96, 0, -.62F, 16.1F, -.45F, 1.24F, 1.6F, .6F);
            }
            chainSickle.setPos(0, 3.6F, 0);
            chainSickle.zRot = -.24F;
            sickle(leftSickle, 1);
            sickle(chainSickle, -1);
        }
        root.setPos(0F, 24F, 0F);
        animatedParts = root.getAllParts().toArray(ModelPart[]::new);
        for (ModelPart part : animatedParts) part.setInitialPose(part.storePose());
        if (!bauk) {
            for (int i = 0; i < links.length; i++) {
                ModelPart link = links[i] = part();
                geometry.attach(chain, link);
                link.y = .6F * i;
                link.visible = false;
                float width = i % 2 == 0 ? .7F : .35F;
                cub(link, 96, 96, -width / 2, .1F, -.3F, width, .5F, .6F);
            }
        }
    }
    private void sickle(ModelPart part, int side) {
        // Wrist rests on the ferrule. Two different outward-facing hooked blades,
        // with actual curved steel volume rather than a five-pixel flat rake.
        cub(part, 64, 64, -.55F, -3.5F, -.57F, 1.1F, 9F, 1.14F);
        cub(part, 96, 64, -.8F, -3.85F, -.82F, 1.6F, 1.55F, 1.64F);
        cub(part, 64, 64, -.72F, 4.4F, -.72F, 1.44F, 1.65F, 1.44F);
        for(int step=0;step<9;step++){
            float t=step/8F;
            float x=side*(.25F+6.7F*t);
            float y=-3.5F-3.4F*(float)Math.sin(t*Math.PI*.88F)+1.7F*t;
            float depth=.95F-.23F*t,thickness=1.23F-.33F*t;
            cub(part, 96, 96, x-thickness*.5F,y,-depth*.5F,thickness,1.4F,depth);
        }
        float tip=side*7.3F;
        cub(part, 120, 112, tip-.38F,-2.25F,-.31F,.76F,2.7F,.62F);
        cub(part, 120, 112, tip-side*.60F-.24F,-.40F,-.26F,.48F,1.35F,.52F);
    }

    private static float attackPose(float remaining, float duration, float impact, float windup, float strike) {
        float elapsed = duration - remaining;
        float impactTime = duration - impact;
        float windupEnd = impactTime * .7F;
        if (elapsed < windupEnd) return Mth.lerp(Mth.clamp(elapsed / windupEnd, 0F, 1F), 0F, windup);
        if (elapsed < impactTime) return Mth.lerp((elapsed - windupEnd) / (impactTime - windupEnd), windup, strike);
        return strike * Mth.clamp(remaining / impact, 0F, 1F);
    }

    @Override public void setupAnim(PosadBoss entity, float limbSwing, float limbAmount, float age, float yaw, float pitch) {
        for (ModelPart part : animatedParts) part.resetPose();
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
            if (entity.move() == PosadBoss.Move.WINDUP_PAW) { leg0.xRot += attackPose(remaining, 38F, 20F, .45F, -1.05F); }
            if (entity.move() == PosadBoss.Move.WINDUP_SLAM) {
                float slam = attackPose(remaining, 55F, 25F, -.75F, 0F);
                leg0.xRot += slam;
                leg1.xRot += slam;
            }
            if (entity.enraged()) { head.xRot -= .09F; tail.yRot *= 2F; }
        } else {
            root.y += .65F * breath;
            cloth.xRot = .04F * breath + .08F * stride;
            float extension = 0;
            rightArm.xRot = -.24F;
            leftArm.xRot = -.24F;
            rightArm.zRot = .38F;
            leftArm.zRot = -.38F;
            if (entity.move() == PosadBoss.Move.WINDUP_SICKLE) {
                leftArm.xRot += attackPose(remaining, 38F, 14F, .85F, -1.55F);
            }
            if (entity.move() == PosadBoss.Move.WINDUP_COINS) leftArm.zRot += Mth.sin(age * .35F) * .24F;
            if (entity.move() == PosadBoss.Move.WINDUP_CHAIN) {
                rightArm.xRot += attackPose(remaining, 40F, 14F, .65F, -1.37F);
                extension = 38F * Math.min(Mth.clamp((24F - remaining) / 10F, 0F, 1F),
                    Mth.clamp(remaining / 14F, 0F, 1F));
                rightArm.zRot *= 1F - Mth.clamp(extension / 8F, 0F, 1F);
                chainSickle.zRot = -.24F * (1F - Mth.clamp(extension / 5F, 0F, 1F));
                chain.xRot = (-rightArm.xRot - 1.57F) * Mth.clamp(extension / 4F, 0F, 1F);
                chainSickle.xRot = -chain.xRot;
            }
            chainSickle.y = 3.6F + extension;
            int nextVisible = Math.min(links.length, (int) Math.ceil(chainSickle.y / .6F));
            for (int i = Math.min(visibleLinks, nextVisible); i < Math.max(visibleLinks, nextVisible); i++) {
                links[i].visible = i < nextVisible;
            }
            visibleLinks = nextVisible;
            head.zRot = entity.phase() == 2 ? .05F * Mth.sin(age * .14F) : 0F;
            if (entity.hurtTime > 0) { head.xRot -= .14F; root.xRot += .045F; }
            if (entity.deathTime > 0) { root.zRot = Math.min(1.3F, entity.deathTime * .07F); leftArm.xRot = -.15F; }
        }
        if (bauk && entity.hurtTime > 0) head.yRot += .12F;
        if (bauk && entity.deathTime > 0) { root.zRot = Math.min(1.1F, entity.deathTime * .055F); jaw.xRot = .7F; }
    }
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay, int color) { root.render(pose, vertices, light, overlay, color); }
}
