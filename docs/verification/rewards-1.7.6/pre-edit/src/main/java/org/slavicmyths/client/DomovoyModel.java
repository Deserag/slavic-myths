package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.entity.DomovoyEntity;

public final class DomovoyModel extends EntityModel<DomovoyEntity> {
 private int texWidth=64,texHeight=32;
 private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final ModelPart head, body, leftArm, rightArm, leftLeg, rightLeg;
    public DomovoyModel() {
        texWidth = 64; texHeight = 64;
        head = part(0, 0, 0, 11, 0, -4, -8, -3.5F, 8, 8, 7);
        geometry.box(head,32, 0,-3, -1, -5.5F, 6, 7, 2); // large hanging beard
        geometry.box(head,48, 0,-1, -3, -5.5F, 2, 2, 2);
        geometry.box(head,0, 44,-4, -9, -4, 8, 2, 8); // cloth cap
        body = part(0, 16, 0, 11, 0, -4, 0, -2.5F, 8, 8, 5);
        rightArm = part(32, 16, -5.5F, 12, 0, -1.5F, -1, -1.5F, 3, 8, 3);
        leftArm = part(32, 16, 5.5F, 12, 0, -1.5F, -1, -1.5F, 3, 8, 3);
        rightLeg = part(0, 32, -2, 19, 0, -1.5F, 0, -2, 3, 5, 4);
        leftLeg = part(0, 32, 2, 19, 0, -1.5F, 0, -2, 3, 5, 4);
    }
    private ModelPart part(int u, int v, float x, float y, float z, float bx, float by, float bz, int w, int h, int d) {
        ModelPart part = geometry.part(texWidth,texHeight,u,v);
        part.setPos(x, y, z); geometry.box(part,bx, by, bz, w, h, d); return part;
    }
    @Override public void setupAnim(DomovoyEntity entity, float walk, float amount, float age, float yaw, float pitch) {
        head.yRot = yaw * ((float)Math.PI / 180F); head.xRot = pitch * ((float)Math.PI / 180F);
        rightLeg.xRot = Mth.cos(walk * 0.6662F) * amount;
        leftLeg.xRot = -rightLeg.xRot;
        rightArm.xRot = leftLeg.xRot * 0.7F; leftArm.xRot = rightLeg.xRot * 0.7F;
    }
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay,int color) {
        head.render(pose, buffer, light, overlay,color); body.render(pose, buffer, light, overlay,color);
        leftArm.render(pose, buffer, light, overlay,color); rightArm.render(pose, buffer, light, overlay,color);
        leftLeg.render(pose, buffer, light, overlay,color); rightLeg.render(pose, buffer, light, overlay,color);
    }
}
