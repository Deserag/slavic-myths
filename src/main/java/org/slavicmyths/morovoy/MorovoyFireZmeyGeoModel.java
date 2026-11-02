package org.slavicmyths.morovoy;

import net.minecraft.client.model.geom.ModelPart;

/** Runtime anatomy correction layered on the animated native model. */
public final class MorovoyFireZmeyGeoModel extends MorovoyFireZmeyModel {
    private static final float TORSO_SCALE_X = ZmeyConfig.TARGET_TORSO_WIDTH_VOXELS / ZmeyConfig.TORSO_WIDTH_VOXELS;
    private static final float WING_SCALE_Z = ZmeyConfig.WING_DEPTH_SCALE;
    @Override public void setupAnim(MorovoyFireZmey boss,float limbSwing,float limbSwingAmount,float age,float yaw,float pitch) {
        super.setupAnim(boss,limbSwing,limbSwingAmount,age,yaw,pitch);
        body.xScale = TORSO_SCALE_X;
        for(String side:new String[]{"left","right"}) {
            ModelPart wing=body.getChild("wing_"+side);
            wing.zScale=WING_SCALE_Z;
            wing.getChild("wing_tip_"+side).zScale=WING_SCALE_Z;
        }
    }
}
