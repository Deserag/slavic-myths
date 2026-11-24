package org.slavicmyths.auka.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import org.slavicmyths.auka.AukaMinion;

/** Small bear and white wolf retain independent shoulder, knee, jaw and tail pivots. */
public final class AukaMinionModel extends EntityModel<AukaMinion> {
    private final AukaAnimalModel<AukaMinion> animal;
    private final boolean bear;
    public AukaMinionModel(boolean bear){this.bear=bear;animal=new AukaAnimalModel<>(bear);}
    @Override public void setupAnim(AukaMinion e,float walk,float amount,float age,float yaw,float pitch){
        org.slavicmyths.auka.AukaBoss.Move action=e.motion()==AukaMinion.Motion.BITE
            ?org.slavicmyths.auka.AukaBoss.Move.WOLF_BITE:org.slavicmyths.auka.AukaBoss.Move.IDLE;
        animal.pose(walk,amount,age,yaw,pitch,action,e.actionTicks(age-e.tickCount),true);
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){
        pose.pushPose();
        float scale=bear?.75F:.52F;
        pose.translate(0,1.5F*(1F-scale),0);
        pose.scale(scale,scale,scale);
        animal.renderToBuffer(pose,out,light,overlay,color);
        pose.popPose();
    }
}
