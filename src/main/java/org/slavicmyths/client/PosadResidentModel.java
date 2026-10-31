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
    private final ModelPart apron, armor, satchel, ceremonialSash;

    public PosadResidentModel() {
        super(createRoot());
        horns = new ModelPart[] {head.getChild("horn_0"), head.getChild("horn_1"), head.getChild("horn_2"), head.getChild("horn_3")};
        wings = new ModelPart[] {body.getChild("wing_left"), body.getChild("wing_right")};
        apron = body.getChild("apron");
        armor = body.getChild("armor");
        satchel = body.getChild("satchel");
        ceremonialSash = body.getChild("ceremonial_sash");
    }
    private static ModelPart createRoot() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition root = mesh.getRoot(), head = root.getChild("head"), body = root.getChild("body");
        for (int index = 0; index < 4; index++) {
            float x = index == 0 ? -2.8F : index == 1 ? 2.0F : index == 2 ? -1.0F : .8F;
            float height = index < 2 ? 3.0F : 2.2F;
            head.addOrReplaceChild("horn_" + index, CubeListBuilder.create().texOffs(0, 48).addBox(x, -10.5F - (index < 2 ? 0 : -1.0F), -.7F, 1.4F, height, 1.4F), PartPose.ZERO);
        }
        // Three-section bat wings, mounted behind the shoulders instead of two tiny
        // bars hidden behind the torso. The exposed outer sections remain visible
        // from the front, sides and back; the body retains vanilla Steve proportions.
        for (int side = 0; side < 2; side++) {
            boolean left = side == 0;
            PartDefinition wing = body.addOrReplaceChild(left ? "wing_left" : "wing_right",
                CubeListBuilder.create().texOffs(32, 32).addBox(left ? 0 : -3.8F, -1.2F, -.45F, 3.8F, 7.4F, .9F),
                PartPose.offset(left ? 3.65F : -3.65F, 1.0F, 2.45F));
            wing.addOrReplaceChild("outer", CubeListBuilder.create().texOffs(0, 32)
                .addBox(left ? 3.45F : -7.0F, -.25F, -.38F, 3.55F, 9.0F, .76F), PartPose.ZERO);
            wing.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(12, 32)
                .addBox(left ? 6.7F : -9.5F, 1.2F, -.32F, 2.8F, 5.7F, .64F), PartPose.ZERO);
            wing.addOrReplaceChild("vein", CubeListBuilder.create().texOffs(24, 40)
                .addBox(left ? 1.2F : -6.7F, -1.3F, -.7F, 5.5F, 1.2F, 1.4F), PartPose.ZERO);
        }
        // Workers wear recognizable apron/belt details rather than full-body armor.
        body.addOrReplaceChild("apron", CubeListBuilder.create().texOffs(40, 20)
            .addBox(-3.45F, 3.2F, -2.38F, 6.9F, 9.2F, .38F), PartPose.ZERO);
        body.addOrReplaceChild("satchel", CubeListBuilder.create().texOffs(0, 48)
            .addBox(2.8F, 7.3F, -2.6F, 1.8F, 3.5F, 1.5F), PartPose.ZERO);
        // Reduced guard armor: narrow chest guard and shoulder pieces, not the
        // former giant dark full-torso cube that obscured humanoid clothing.
        body.addOrReplaceChild("armor", CubeListBuilder.create().texOffs(20, 20)
            .addBox(-3.6F, .25F, -2.75F, 7.2F, 7.8F, .65F)
            .texOffs(0, 40).addBox(-4.6F, -.5F, -2.4F, 1.7F, 3.0F, 4.8F)
            .texOffs(0, 40).addBox(2.9F, -.5F, -2.4F, 1.7F, 3.0F, 4.8F), PartPose.ZERO);
        body.addOrReplaceChild("ceremonial_sash", CubeListBuilder.create().texOffs(40, 48)
            .addBox(-3.1F, .4F, -2.86F, 1.7F, 10.5F, .4F), PartPose.ZERO);
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
            wings[i].getChild("outer").visible = traits.wings() >= 2;
            wings[i].getChild("tip").visible = traits.wings() >= 2;
            wings[i].getChild("vein").visible = true;
            // Fold slightly while walking to keep the silhouette readable in
            // one-block doorways; no flying or changes to the entity hitbox.
            float spread = stride > .18F ? .18F : .38F;
            wings[i].yRot = (i == 0 ? -1 : 1) * (spread + Mth.sin(age * .055F + i) * .035F);
            wings[i].xRot = Mth.sin(age * .075F + i * .45F) * .055F;
            wings[i].zRot = (i == 0 ? 1 : -1) * Mth.sin(age * .045F) * .035F;
        }
        apron.visible = switch (entity.role()) {
            case BLACKSMITH, WEAPONSMITH, FARMER, MUSHROOM_GROWER, COOK, CRAFTER -> true;
            default -> false;
        };
        armor.visible = entity.role().guard();
        satchel.visible = switch (entity.role()) {
            case FARMER, MUSHROOM_GROWER, HERBALIST, STOREKEEPER -> true;
            default -> false;
        };
        ceremonialSash.visible = entity.role() == org.slavicmyths.posad.PosadPopulationPlan.Role.POSADNIK
            || entity.role() == org.slavicmyths.posad.PosadPopulationPlan.Role.SOTNIK;
        if (entity.role() == org.slavicmyths.posad.PosadPopulationPlan.Role.POSADNIK) body.zRot = Mth.sin(age * .04F) * .014F;
    }
}
