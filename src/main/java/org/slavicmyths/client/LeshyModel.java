package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.entity.LeshyEntity;

public final class LeshyModel extends EntityModel<LeshyEntity> {
 private int texWidth=64,texHeight=32;
 private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final ModelPart head, body, leftArm, rightArm, leftLeg, rightLeg;
    public LeshyModel() {
        texWidth = 64; texHeight = 64;
        head = part(0, 0, 0, -3, 0, -3, -8, -3, 6, 8, 6);
        geometry.box(head,40, 0,-5, -16, -1, 2, 10, 2);
        geometry.box(head,40, 0,3, -17, -1, 2, 11, 2);
        geometry.box(head,40, 26,-8, -13, -1, 4, 2, 2);
        geometry.box(head,40, 26,4, -14, -1, 4, 2, 2);
        body = part(0, 16, 0, -3, 0, -3, 0, -2, 6, 13, 4);
        rightArm = part(24, 16, -4.5F, -2, 0, -1.5F, -1, -1.5F, 3, 19, 3);
        leftArm = part(24, 16, 4.5F, -2, 0, -1.5F, -1, -1.5F, 3, 19, 3);
        rightLeg = part(0, 36, -2, 10, 0, -1.5F, 0, -2, 3, 14, 4);
        leftLeg = part(0, 36, 2, 10, 0, -1.5F, 0, -2, 3, 14, 4);
    }
    private ModelPart part(int u, int v, float x, float y, float z, float bx, float by, float bz, int w, int h, int d) {
        ModelPart part = geometry.part(texWidth,texHeight,u, v);
        part.setPos(x,y,z); geometry.box(part,bx,by,bz,w,h,d); return part;
    }
    @Override public void setupAnim(LeshyEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        head.yRot = yaw * ((float)Math.PI / 180F); head.xRot = pitch * ((float)Math.PI / 180F);
        rightLeg.xRot = Mth.cos(walk * 0.5F) * amount * 0.7F; leftLeg.xRot = -rightLeg.xRot;
        float threat = -Mth.sin(attackTime * (float)Math.PI) * 1.2F;
        rightArm.xRot = threat + leftLeg.xRot * 0.5F; leftArm.xRot = threat + rightLeg.xRot * 0.5F;
        leftArm.zRot = 0.05F + Mth.sin(age * 0.04F) * 0.025F; rightArm.zRot = -leftArm.zRot;
    }
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,int color) {
        head.render(pose,buffer,light,overlay,color); body.render(pose,buffer,light,overlay,color);
        leftArm.render(pose,buffer,light,overlay,color); rightArm.render(pose,buffer,light,overlay,color);
        leftLeg.render(pose,buffer,light,overlay,color); rightLeg.render(pose,buffer,light,overlay,color);
    }
}
