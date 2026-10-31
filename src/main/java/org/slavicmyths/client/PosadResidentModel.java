package org.slavicmyths.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import org.slavicmyths.posad.PosadResidentEntity;

/** Steve-sized original humanoid with identity-persistent horns, folding wings and profession layers. */
public final class PosadResidentModel extends HumanoidModel<PosadResidentEntity> {
    private final ModelPart[] horns;
    private final ModelPart[] wings;
    private final ModelPart apron, armor;

    public PosadResidentModel() {
        super(createRoot());
        horns = new ModelPart[] {head.getChild("horn_0"), head.getChild("horn_1"), head.getChild("horn_2"), head.getChild("horn_3")};
        wings = new ModelPart[] {body.getChild("wing_left"), body.getChild("wing_right")};
        apron = body.getChild("apron");
        armor = body.getChild("armor");
    }
    private static ModelPart createRoot() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition root = mesh.getRoot(), head = root.getChild("head"), body = root.getChild("body");
        for (int index = 0; index < 4; index++) {
            float x = index == 0 ? -2.8F : index == 1 ? 2.0F : index == 2 ? -1.0F : .8F;
            float height = index < 2 ? 3.0F : 2.2F;
            head.addOrReplaceChild("horn_" + index, CubeListBuilder.create().texOffs(0, 48).addBox(x, -10.5F - (index < 2 ? 0 : -1.0F), -.7F, 1.4F, height, 1.4F), PartPose.ZERO);
        }
        body.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(32, 32).addBox(3.8F, 1.0F, 2.3F, 2.5F, 10, 1.3F), PartPose.ZERO);
        body.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(32, 32).addBox(-6.3F, 1.0F, 2.3F, 2.5F, 10, 1.3F), PartPose.ZERO);
        body.addOrReplaceChild("apron", CubeListBuilder.create().texOffs(40, 20).addBox(-4.1F, 3, -2.55F, 8.2F, 10, .65F), PartPose.ZERO);
        body.addOrReplaceChild("armor", CubeListBuilder.create().texOffs(20, 20).addBox(-4.4F, -1, -2.5F, 8.8F, 10, 5), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }
    @Override public void setupAnim(PosadResidentEntity entity, float walk, float stride, float age, float yaw, float pitch) {
        rightArmPose = entity.getMainHandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        leftArmPose = entity.isUsingItem() ? ArmPose.BLOCK : ArmPose.EMPTY;
        super.setupAnim(entity, walk, stride, age, yaw, pitch);
        hat.visible = false;
        var traits = entity.traits();
        for (int i = 0; i < horns.length; i++) horns[i].visible = i < traits.horns();
        for (int i = 0; i < wings.length; i++) {
            wings[i].visible = traits.wings() > 0;
            wings[i].xRot = Mth.sin(age * .07F + i * .8F) * .05F;
            wings[i].yRot = (i == 0 ? 1 : -1) * (.1F + Mth.sin(age * .06F) * .08F);
        }
        apron.visible = switch (entity.role()) {
            case BLACKSMITH, WEAPONSMITH, FARMER, MUSHROOM_GROWER, COOK, CRAFTER -> true;
            default -> false;
        };
        armor.visible = entity.role().guard();
        if (entity.role() == org.slavicmyths.posad.PosadPopulationPlan.Role.POSADNIK) body.zRot = Mth.sin(age * .04F) * .014F;
    }
}
