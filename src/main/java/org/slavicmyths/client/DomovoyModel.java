package org.slavicmyths.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.entity.DomovoyEntity;

public final class DomovoyModel extends EntityModel<DomovoyEntity> {
    private final ModelRenderer head, body, leftArm, rightArm, leftLeg, rightLeg;
    public DomovoyModel() {
        texWidth = 64; texHeight = 64;
        head = part(0, 0, 0, 11, 0, -4, -8, -3.5F, 8, 8, 7);
        head.texOffs(32, 0).addBox(-3, -1, -5.5F, 6, 7, 2); // large hanging beard
        head.texOffs(48, 0).addBox(-1, -3, -5.5F, 2, 2, 2);
        head.texOffs(0, 44).addBox(-4, -9, -4, 8, 2, 8); // cloth cap
        body = part(0, 16, 0, 11, 0, -4, 0, -2.5F, 8, 8, 5);
        rightArm = part(32, 16, -5.5F, 12, 0, -1.5F, -1, -1.5F, 3, 8, 3);
        leftArm = part(32, 16, 5.5F, 12, 0, -1.5F, -1, -1.5F, 3, 8, 3);
        rightLeg = part(0, 32, -2, 19, 0, -1.5F, 0, -2, 3, 5, 4);
        leftLeg = part(0, 32, 2, 19, 0, -1.5F, 0, -2, 3, 5, 4);
    }
    private ModelRenderer part(int u, int v, float x, float y, float z, float bx, float by, float bz, int w, int h, int d) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.setPos(x, y, z); part.addBox(bx, by, bz, w, h, d); return part;
    }
    @Override public void setupAnim(DomovoyEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        head.yRot = yaw * ((float)Math.PI / 180F); head.xRot = pitch * ((float)Math.PI / 180F);
        rightLeg.xRot = MathHelper.cos(walk * 0.6662F) * amount;
        leftLeg.xRot = -rightLeg.xRot;
        rightArm.xRot = leftLeg.xRot * 0.7F; leftArm.xRot = rightLeg.xRot * 0.7F;
    }
    @Override public void renderToBuffer(MatrixStack pose, IVertexBuilder buffer, int light, int overlay, float r, float g, float b, float a) {
        head.render(pose, buffer, light, overlay, r,g,b,a); body.render(pose, buffer, light, overlay, r,g,b,a);
        leftArm.render(pose, buffer, light, overlay, r,g,b,a); rightArm.render(pose, buffer, light, overlay, r,g,b,a);
        leftLeg.render(pose, buffer, light, overlay, r,g,b,a); rightLeg.render(pose, buffer, light, overlay, r,g,b,a);
    }
}
