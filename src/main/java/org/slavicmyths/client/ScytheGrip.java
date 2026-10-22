package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.world.entity.HumanoidArm;

/** Player-only #117 grip, shared by the custom render paths. Boss rigs never use this. */
public final class ScytheGrip {
    public static final float GRIP_Y=12, THIRD_SCALE=.75F, FIRST_SCALE=.65F;

    /** Applied after native PlayerModel.translateToHand, matching the idle item transform. */
    public static void thirdPerson(PoseStack pose,HumanoidArm arm,float horizontal) {
        boolean left=arm==HumanoidArm.LEFT;
        pose.mulPose(Axis.XP.rotationDegrees(-90));
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.translate((left?-1F:1F)/16,.125,-.625);
        // The existing Reaper arm itself pitches to -90 at contact. Compensate the
        // ItemInHandLayer basis so the shaft becomes horizontal, not upright again.
        pose.mulPose(Axis.XP.rotationDegrees(70-160*horizontal));
        pose.mulPose(Axis.YP.rotationDegrees(left?0:180));
        pose.scale(THIRD_SCALE,THIRD_SCALE,THIRD_SCALE);
        pose.translate(0,(8-GRIP_Y)/16,0);
    }

    /** Applied at the existing first-person wrist, before drawing centered held geometry. */
    public static void firstPerson(PoseStack pose,boolean left,float horizontal) {
        float sign=left?-1:1;
        pose.mulPose(Axis.XP.rotationDegrees(-90*horizontal));
        pose.mulPose(Axis.YP.rotationDegrees((left?0:180)-sign*10*(1-horizontal)));
        pose.scale(FIRST_SCALE,FIRST_SCALE,FIRST_SCALE);
        pose.translate(0,(8-GRIP_Y)/16,0);
    }

    private ScytheGrip() {}
}
