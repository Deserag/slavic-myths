package org.slavicmyths.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.entity.LeshyEntity;

public final class LeshyModel extends EntityModel<LeshyEntity> {
    private final ModelRenderer head, body, leftArm, rightArm, leftLeg, rightLeg;
    public LeshyModel() {
        texWidth = 64; texHeight = 64;
        head = part(0, 0, 0, -3, 0, -3, -8, -3, 6, 8, 6);
        head.texOffs(40, 0).addBox(-5, -16, -1, 2, 10, 2);
        head.texOffs(40, 0).addBox(3, -17, -1, 2, 11, 2);
        head.texOffs(40, 26).addBox(-8, -13, -1, 4, 2, 2);
        head.texOffs(40, 26).addBox(4, -14, -1, 4, 2, 2);
        body = part(0, 16, 0, -3, 0, -3, 0, -2, 6, 13, 4);
        rightArm = part(24, 16, -4.5F, -2, 0, -1.5F, -1, -1.5F, 3, 19, 3);
        leftArm = part(24, 16, 4.5F, -2, 0, -1.5F, -1, -1.5F, 3, 19, 3);
        rightLeg = part(0, 36, -2, 10, 0, -1.5F, 0, -2, 3, 14, 4);
        leftLeg = part(0, 36, 2, 10, 0, -1.5F, 0, -2, 3, 14, 4);
    }
    private ModelRenderer part(int u, int v, float x, float y, float z, float bx, float by, float bz, int w, int h, int d) {
        ModelRenderer part = new ModelRenderer(this, u, v);
        part.setPos(x,y,z); part.addBox(bx,by,bz,w,h,d); return part;
    }
    @Override public void setupAnim(LeshyEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        head.yRot = yaw * ((float)Math.PI / 180F); head.xRot = pitch * ((float)Math.PI / 180F);
        rightLeg.xRot = MathHelper.cos(walk * 0.5F) * amount * 0.7F; leftLeg.xRot = -rightLeg.xRot;
        float threat = -MathHelper.sin(attackTime * (float)Math.PI) * 1.2F;
        rightArm.xRot = threat + leftLeg.xRot * 0.5F; leftArm.xRot = threat + rightLeg.xRot * 0.5F;
        leftArm.zRot = 0.05F + MathHelper.sin(age * 0.04F) * 0.025F; rightArm.zRot = -leftArm.zRot;
    }
    @Override public void renderToBuffer(MatrixStack pose, IVertexBuilder buffer, int light, int overlay, float r, float g, float b, float a) {
        head.render(pose,buffer,light,overlay,r,g,b,a); body.render(pose,buffer,light,overlay,r,g,b,a);
        leftArm.render(pose,buffer,light,overlay,r,g,b,a); rightArm.render(pose,buffer,light,overlay,r,g,b,a);
        leftLeg.render(pose,buffer,light,overlay,r,g,b,a); rightLeg.render(pose,buffer,light,overlay,r,g,b,a);
    }
}
