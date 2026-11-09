package org.slavicmyths.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import org.slavicmyths.posad.PosadResidentEntity;

/** Classic player-proportioned figure; clothing and regalia live on the skin UV. */
public final class PosadResidentModel extends HumanoidModel<PosadResidentEntity> {
    public PosadResidentModel() {
        super(LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 64).bakeRoot());
        hat.visible = false;
    }

    @Override
    public void setupAnim(PosadResidentEntity entity, float walk, float stride, float age, float yaw, float pitch) {
        rightArmPose = entity.getMainHandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        leftArmPose = entity.isUsingItem() ? ArmPose.BLOCK : ArmPose.EMPTY;
        super.setupAnim(entity, walk, stride, age, yaw, pitch);
        hat.visible = false;
    }
}
