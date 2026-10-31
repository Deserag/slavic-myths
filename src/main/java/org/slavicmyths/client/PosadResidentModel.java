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
    private final ModelPart farmerHat, mushroomHood, herbalistHood, cookCap, collar;
    private final ModelPart smithGauntletLeft, smithGauntletRight, bandolier, toolPouch, waistcoat, cape;

    public PosadResidentModel() {
        super(createRoot());
        horns = new ModelPart[] {head.getChild("horn_0"), head.getChild("horn_1"), head.getChild("horn_2"), head.getChild("horn_3")};
        wings = new ModelPart[] {body.getChild("wing_left"), body.getChild("wing_right")};
        apron = body.getChild("apron");
        armor = body.getChild("armor");
        satchel = body.getChild("satchel");
        ceremonialSash = body.getChild("ceremonial_sash");
        farmerHat = head.getChild("farmer_hat");
        mushroomHood = head.getChild("mushroom_hood");
        herbalistHood = head.getChild("herbalist_hood");
        cookCap = head.getChild("cook_cap");
        collar = body.getChild("collar");
        smithGauntletLeft = leftArm.getChild("smith_gauntlet");
        smithGauntletRight = rightArm.getChild("smith_gauntlet");
        bandolier = body.getChild("bandolier");
        toolPouch = body.getChild("tool_pouch");
        waistcoat = body.getChild("waistcoat");
        cape = body.getChild("cape");
    }
    private static ModelPart createRoot() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition root = mesh.getRoot(), head = root.getChild("head"), body = root.getChild("body");
        PartDefinition leftArm = root.getChild("left_arm"), rightArm = root.getChild("right_arm");
        PartDefinition hornLeft = head.addOrReplaceChild("horn_0", CubeListBuilder.create().texOffs(0, 48).addBox(-3.15F, -10.0F, -.55F, 1.2F, 2.7F, 1.2F), PartPose.rotation(0, 0, -.18F));
        hornLeft.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(8, 48).addBox(-3.0F, -11.5F, -.4F, .8F, 1.8F, .8F), PartPose.rotation(0, 0, -.25F));
        PartDefinition hornRight = head.addOrReplaceChild("horn_1", CubeListBuilder.create().texOffs(0, 48).addBox(1.95F, -10.0F, -.55F, 1.2F, 2.7F, 1.2F), PartPose.rotation(0, 0, .18F));
        hornRight.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(8, 48).addBox(2.2F, -11.5F, -.4F, .8F, 1.8F, .8F), PartPose.rotation(0, 0, .25F));
        head.addOrReplaceChild("horn_2", CubeListBuilder.create().texOffs(8, 48).addBox(-1.85F, -9.55F, .25F, 1.0F, 1.9F, 1.0F), PartPose.rotation(.12F, 0, -.08F));
        head.addOrReplaceChild("horn_3", CubeListBuilder.create().texOffs(8, 48).addBox(.85F, -9.1F, .25F, 1.0F, 1.35F, 1.0F), PartPose.rotation(.12F, 0, .08F));
        // Reference #2: dark ribbed bat wings with broad red membranes, not
        // horizontal rectangular bars. Each wing has a shoulder, two tapered
        // membrane panels and visible spars; all use the existing 64x64 atlas.
        for (int side = 0; side < 2; side++) {
            boolean left = side == 0;
            float direction = left ? 1.0F : -1.0F;
            PartDefinition wing = body.addOrReplaceChild(left ? "wing_left" : "wing_right",
                CubeListBuilder.create().texOffs(32, 32).addBox(left ? 0.0F : -3.2F, -0.4F, -0.35F, 3.2F, 5.8F, 0.7F),
                PartPose.offset(left ? 3.1F : -3.1F, 1.0F, 2.5F));
            wing.addOrReplaceChild("outer", CubeListBuilder.create().texOffs(32, 32)
                .addBox(left ? 0.8F : -5.6F, 0.0F, -0.2F, 4.8F, 6.5F, 0.4F),
                PartPose.rotation(0, 0, direction * -0.22F));
            wing.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(12, 32)
                .addBox(left ? 5.0F : -8.2F, 1.8F, -0.18F, 3.2F, 4.9F, 0.36F),
                PartPose.rotation(0, 0, direction * 0.18F));
            wing.addOrReplaceChild("vein", CubeListBuilder.create().texOffs(24, 40)
                .addBox(left ? 0.6F : -7.1F, -0.7F, -0.55F, 6.5F, 0.9F, 1.1F),
                PartPose.rotation(0, 0, direction * -0.24F));
        }
        // Workers wear recognizable apron/belt details rather than full-body armor.
        body.addOrReplaceChild("apron", CubeListBuilder.create().texOffs(40, 32)
            .addBox(-3.45F, 3.2F, -2.38F, 6.9F, 9.2F, .38F), PartPose.ZERO);
        body.addOrReplaceChild("satchel", CubeListBuilder.create().texOffs(0, 48)
            .addBox(2.8F, 7.3F, -2.6F, 1.8F, 3.5F, 1.5F), PartPose.ZERO);
        // Reduced guard armor: narrow chest guard and shoulder pieces, not the
        // former giant dark full-torso cube that obscured humanoid clothing.
        body.addOrReplaceChild("armor", CubeListBuilder.create().texOffs(20, 20)
            .addBox(-3.6F, .25F, -2.75F, 7.2F, 7.8F, .65F)
            .texOffs(0, 40).addBox(-4.6F, -.5F, -2.4F, 1.7F, 3.0F, 4.8F)
            .texOffs(0, 40).addBox(2.9F, -.5F, -2.4F, 1.7F, 3.0F, 4.8F), PartPose.ZERO);
        body.addOrReplaceChild("ceremonial_sash", CubeListBuilder.create().texOffs(40, 32)
            .addBox(-3.1F, .4F, -2.86F, 1.7F, 10.5F, .4F), PartPose.ZERO);
        // Profession identifiers use recognisable silhouettes, not the same
        // anonymous military armour on every citizen. Parts are child geometry
        // in the existing 64x64 Steve UV atlas and follow vanilla animations.
        head.addOrReplaceChild("farmer_hat", CubeListBuilder.create().texOffs(0, 16)
            .addBox(-4.55F, -8.65F, -4.55F, 9.1F, .7F, 9.1F)
            .texOffs(0, 0).addBox(-3.55F, -10.1F, -3.55F, 7.1F, 1.5F, 7.1F), PartPose.ZERO);
        head.addOrReplaceChild("mushroom_hood", CubeListBuilder.create().texOffs(0, 16)
            .addBox(-4.15F, -8.75F, -4.15F, 8.3F, 1.1F, 8.3F)
            .texOffs(0, 32).addBox(-4.15F, -7.75F, -4.25F, .75F, 4.2F, .9F)
            .texOffs(0, 32).addBox(3.4F, -7.75F, -4.25F, .75F, 4.2F, .9F), PartPose.ZERO);
        head.addOrReplaceChild("herbalist_hood", CubeListBuilder.create().texOffs(0, 32)
            .addBox(-4.1F, -8.85F, -4.1F, 8.2F, 1.2F, 8.2F)
            .texOffs(0, 40).addBox(-4.05F, -7.75F, -4.3F, .7F, 4.6F, .85F)
            .texOffs(0, 40).addBox(3.35F, -7.75F, -4.3F, .7F, 4.6F, .85F), PartPose.ZERO);
        head.addOrReplaceChild("cook_cap", CubeListBuilder.create().texOffs(0, 0)
            .addBox(-4.05F, -8.8F, -4.05F, 8.1F, 1.0F, 8.1F)
            .texOffs(0, 16).addBox(-3.3F, -10.65F, -3.3F, 6.6F, 1.9F, 6.6F), PartPose.ZERO);
        body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(24, 40)
            .addBox(-4.35F, -.65F, -3.25F, 8.7F, 2.3F, 1.2F), PartPose.ZERO);
        body.addOrReplaceChild("bandolier", CubeListBuilder.create().texOffs(40, 32)
            .addBox(-.9F, -4.2F, -2.9F, 1.8F, 12.0F, .7F), PartPose.rotation(0,0,.48F));
        body.addOrReplaceChild("tool_pouch", CubeListBuilder.create().texOffs(0, 48)
            .addBox(-4.8F, 7.5F, -.25F, 2.1F, 3.6F, 3.1F), PartPose.ZERO);
        body.addOrReplaceChild("waistcoat", CubeListBuilder.create().texOffs(20, 20)
            .addBox(-3.95F, 1.1F, -2.53F, 7.9F, 9.4F, .45F)
            .texOffs(40, 32).addBox(-3.85F, 7.5F, -2.7F, 7.7F, 1.2F, .55F), PartPose.ZERO);
        body.addOrReplaceChild("cape", CubeListBuilder.create().texOffs(20, 32)
            .addBox(-3.75F, 1.5F, 2.65F, 7.5F, 13.0F, .65F), PartPose.ZERO);
        leftArm.addOrReplaceChild("smith_gauntlet", CubeListBuilder.create().texOffs(0, 40)
            .addBox(-2.05F, 7.2F, -2.08F, 4.1F, 3.4F, 4.15F), PartPose.ZERO);
        rightArm.addOrReplaceChild("smith_gauntlet", CubeListBuilder.create().texOffs(0, 40)
            .addBox(-2.05F, 7.2F, -2.08F, 4.1F, 3.4F, 4.15F), PartPose.ZERO);
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
            wings[i].getChild("tip").visible = traits.wings() == 2;
            wings[i].getChild("vein").visible = traits.wings() > 0;
            // Fold slightly while walking to keep the silhouette readable in
            // one-block doorways; no flying or changes to the entity hitbox.
            float spread = stride > .18F ? .18F : .38F;
            wings[i].yRot = (i == 0 ? -1 : 1) * (spread + Mth.sin(age * .055F + i) * .035F);
            wings[i].xRot = Mth.sin(age * .075F + i * .45F) * .055F;
            wings[i].zRot = (i == 0 ? 1 : -1) * Mth.sin(age * .045F) * .035F;
        }
        var role = entity.role();
        body.zRot = 0;
        apron.visible = switch (role) {
            case BLACKSMITH, WEAPONSMITH, FARMER, MUSHROOM_GROWER, COOK, CRAFTER -> true;
            default -> false;
        };
        armor.visible = role.guard();
        satchel.visible = switch (role) {
            case FARMER, MUSHROOM_GROWER, HERBALIST, STOREKEEPER -> true;
            default -> false;
        };
        ceremonialSash.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.POSADNIK
            || role == org.slavicmyths.posad.PosadPopulationPlan.Role.SOTNIK;
        farmerHat.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.FARMER;
        mushroomHood.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.MUSHROOM_GROWER;
        herbalistHood.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.HERBALIST;
        cookCap.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.COOK;
        collar.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.POSADNIK
            || role == org.slavicmyths.posad.PosadPopulationPlan.Role.SOTNIK;
        smithGauntletLeft.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.BLACKSMITH
            || role == org.slavicmyths.posad.PosadPopulationPlan.Role.WEAPONSMITH;
        smithGauntletRight.visible = smithGauntletLeft.visible;
        bandolier.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.WEAPONSMITH;
        toolPouch.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.CRAFTER;
        waistcoat.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.STOREKEEPER
            || role == org.slavicmyths.posad.PosadPopulationPlan.Role.CIVILIAN
            || role == org.slavicmyths.posad.PosadPopulationPlan.Role.CRAFTER;
        cape.visible = role == org.slavicmyths.posad.PosadPopulationPlan.Role.POSADNIK;
        if (role == org.slavicmyths.posad.PosadPopulationPlan.Role.POSADNIK) body.zRot = Mth.sin(age * .04F) * .014F;
    }
}
