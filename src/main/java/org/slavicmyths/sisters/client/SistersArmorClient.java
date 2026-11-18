package org.slavicmyths.sisters.client;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.slavicmyths.sisters.SistersArmor;

/** Per-bone raised 3D armor from the approved four-sisters prototype; no painted vanilla shell. */
public final class SistersArmorClient {
    private static final Map<EquipmentSlot, HumanoidModel<LivingEntity>> MODELS = new EnumMap<>(EquipmentSlot.class);

    public static void register(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extension = new IClientItemExtensions() {
            @Override public HumanoidModel<?> getHumanoidArmorModel(LivingEntity wearer, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
                if (!(stack.getItem() instanceof SistersArmor.Piece)) return original;
                return MODELS.computeIfAbsent(slot, SistersArmorClient::create);
            }
        };
        event.registerItem(extension, SistersArmor.LEDEYA_HELMET.get(), SistersArmor.GNETEYA_CHESTPLATE.get(), SistersArmor.AGNEYA_LEGGINGS.get(), SistersArmor.TRYASEYA_BOOTS.get());
    }

    private static void box(PartDefinition parent, String id, int u, int v, float x, float y, float z, float width, float height, float depth) {
        parent.addOrReplaceChild(id, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, width, height, depth), PartPose.ZERO);
    }

    private static HumanoidModel<LivingEntity> create(EquipmentSlot slot) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5, 2, 0));
        PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5, 2, 0));
        PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12, 0));
        PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12, 0));

        if (slot == EquipmentSlot.HEAD) {
            // Open front: player's face remains exposed under the elevated ice crown.
            box(head, "crown", 0, 0, -4.8F, -9.25F, -4.8F, 9.6F, 3.4F, 9.6F);
            box(head, "forehead", 25, 0, -3.2F, -9.75F, -5.25F, 6.4F, 2.4F, 1.0F);
            box(head, "left_cheek", 24, 9, 3.7F, -6.65F, -4.6F, 1.3F, 5.5F, 2.7F);
            box(head, "right_cheek", 24, 9, -5.0F, -6.65F, -4.6F, 1.3F, 5.5F, 2.7F);
            box(head, "front_crystal", 40, 0, -0.8F, -11.3F, -5.7F, 1.6F, 3.0F, 1.5F);
            box(head, "crown_front_lower_rim", 25, 0, -3.75F, -7.15F, -5.05F, 7.5F, .75F, .8F);
            box(head, "crystal_crown_spire", 40, 0, -.6F, -13.5F, -5.15F, 1.2F, 2.8F, 1.2F);
            box(head, "rear_crown_band", 25, 0, -4.65F, -8.65F, 3.85F, 9.3F, 2.0F, .9F);
            box(head, "raised_brow", 28, 0, -3.55F, -9.1F, -5.6F, 7.1F, 1.1F, .65F);
            box(head, "crest_bridge", 40, 0, -1.3F, -10.35F, -5.4F, 2.6F, .95F, .7F);
            box(head, "crown_rear_spine", 38, 0, -.7F, -12.15F, 3.9F, 1.4F, 3.8F, 1.1F);
            for (int side : new int[]{-1, 1}) {
                float sx = side > 0 ? 4.75F : -6.75F;
                box(head, "wing_base_" + side, 35, 10, sx, -9.4F, -1.7F, 2.0F, 4.3F, 3.1F);
                box(head, "wing_mid_" + side, 39, 0, sx + (side > 0 ? .45F : -.45F), -12.8F, -1.35F, 2.0F, 4.1F, 2.2F);
                box(head, "wing_tip_" + side, 39, 0, sx + (side > 0 ? .95F : -.95F), -15.1F, -.85F, 1.45F, 3.3F, 1.5F);
                box(head, "wing_inner_spike_" + side, 40, 0, side > 0 ? 4.1F : -5.35F, -11.65F, -4.25F, 1.25F, 3.75F, 1.45F);
                box(head, "wing_rear_crest_" + side, 39, 0, side > 0 ? 4.3F : -5.6F, -12.3F, 2.65F, 1.3F, 4.1F, 1.2F);
            }
        }

        if (slot == EquipmentSlot.CHEST) {
            box(body, "inner_chest", 0, 16, -4.45F, -.4F, -2.5F, 8.9F, 11.4F, 5.0F);
            box(body, "sternum", 20, 13, -1.45F, 1.1F, -3.55F, 2.9F, 7.4F, 1.1F);
            for (int side : new int[]{-1, 1}) {
                float x = side < 0 ? -4.7F : 1.45F;
                box(body, "upper_rib_" + side, 34, 8, x, .65F, -3.65F, 3.25F, 2.1F, 1.15F);
                box(body, "middle_rib_" + side, 34, 8, x + (side < 0 ? .1F : -.1F), 3.35F, -3.8F, 3.25F, 2.2F, 1.2F);
                box(body, "lower_rib_" + side, 34, 8, x + (side < 0 ? .4F : -.4F), 6.35F, -3.5F, 2.95F, 1.8F, 1.0F);
                box(body, "back_spine_" + side, 21, 13, side < 0 ? -2.45F : .5F, 2.6F, 2.45F, 1.95F, 6.9F, 1.0F);
            }
            box(body, "heart_rune", 47, 0, -.85F, 3.2F, -4.15F, 1.7F, 2.0F, .75F);
            box(body, "upper_sternum_ridge", 20, 13, -.65F, .5F, -4.15F, 1.3F, 2.8F, .7F);
            box(body, "lower_sternum_point", 20, 13, -.55F, 5.2F, -4.05F, 1.1F, 3.4F, .8F);
            box(body, "backbone_plate", 20, 13, -1.25F, 1.0F, 2.5F, 2.5F, 8.4F, 1.15F);
            box(body, "upper_rune_frame", 34, 8, -1.45F, 2.6F, -4.35F, 2.9F, .55F, .7F);
            box(body, "lower_rune_frame", 34, 8, -1.45F, 5.15F, -4.35F, 2.9F, .55F, .7F);
            box(body, "rear_vertebrae", 34, 8, -.55F, 3.0F, 3.5F, 1.1F, 5.85F, .7F);
            box(body, "belt", 17, 20, -4.8F, 9.25F, -3.15F, 9.6F, 2.05F, 6.3F);
            for (int side : new int[]{-1,1}) {
                PartDefinition arm = side < 0 ? rightArm : leftArm;
                float x = side < 0 ? -4.55F : -.55F;
                box(arm, "shoulder_core_" + side, 0, 16, x, -2.7F, -3.0F, 5.1F, 5.5F, 6.0F);
                box(arm, "pauldron_1_" + side, 33, 8, x + (side < 0 ? -.5F : .5F), -3.8F, -3.45F, 5.2F, 2.7F, 6.9F);
                box(arm, "pauldron_2_" + side, 33, 8, x + (side < 0 ? -.95F : .95F), -4.8F, -2.65F, 4.7F, 1.7F, 5.3F);
                box(arm, "bone_horn_" + side, 48, 0, x + (side < 0 ? -1.3F : 3.9F), -6.75F, -.85F, 1.65F, 2.8F, 2.0F);
                box(arm, "bracer_" + side, 18, 20, x + .55F, 3.0F, -2.7F, 3.9F, 4.1F, 5.4F);
                box(arm, "shoulder_bone_ridge_" + side, 34, 8, x + .35F, -4.2F, -3.9F, 3.8F, 1.15F, 1.15F);
                box(arm, "elbow_guard_" + side, 33, 8, x + .3F, 6.6F, -3.15F, 4.5F, 2.1F, 6.3F);
                box(arm, "forearm_spine_" + side, 20, 13, x + 2.0F, 3.2F, -3.25F, .95F, 4.4F, .7F);
            }
        }

        if (slot == EquipmentSlot.LEGS) {
            box(body, "waist_ring", 0, 16, -4.65F, 8.1F, -2.75F, 9.3F, 3.3F, 5.5F);
            for (int side : new int[]{-1, 1}) {
                float x = side < 0 ? -4.6F : 1.1F;
                box(body, "fauld_top_" + side, 17, 20, x, 9.0F, -3.45F, 3.5F, 3.35F, 1.25F);
                box(body, "fauld_middle_" + side, 28, 12, x + (side < 0 ? -.4F : .4F), 11.3F, -3.85F, 3.9F, 3.0F, 1.35F);
                box(body, "fauld_bottom_" + side, 28, 12, x + (side < 0 ? -.65F : .65F), 13.65F, -4.0F, 4.2F, 2.7F, 1.15F);
            }
            box(body, "front_tabard", 43, 15, -1.65F, 10.7F, -4.25F, 3.3F, 6.65F, 1.05F);
            box(body, "waist_emblem", 47, 0, -.95F, 9.5F, -4.55F, 1.9F, 2.0F, .55F);
            box(body, "front_tabard_center", 47, 0, -.4F, 12.1F, -4.8F, .8F, 4.1F, .65F);
            box(body, "back_waist_plate", 28, 12, -3.3F, 9.4F, 2.7F, 6.6F, 3.4F, 1.0F);
            box(body, "front_apron_tip", 47, 0, -.95F, 15.15F, -4.55F, 1.9F, 2.25F, .7F);
            box(body, "waist_buckle", 43, 15, -1.3F, 8.55F, -3.6F, 2.6F, 1.6F, .55F);
            for (int side : new int[]{-1, 1}) {
                PartDefinition leg = side < 0 ? rightLeg : leftLeg;
                box(leg, "thigh_core_" + side, 0, 16, -2.35F, -.5F, -2.5F, 4.7F, 8.5F, 5.0F);
                box(leg, "thigh_plate_" + side, 32, 8, -2.25F, .8F, -3.05F, 4.5F, 4.2F, .8F);
                box(leg, "knee_" + side, 40, 12, -2.3F, 5.9F, -3.15F, 4.6F, 2.1F, .9F);
                box(leg, "glow_seam_" + side, 47, 0, -.3F, 1.3F, -3.42F, .6F, 5.7F, .45F);
                box(leg, "split_lower_plate_" + side, 28, 12, -2.2F, 7.35F, -3.15F, 4.4F, 3.3F, .9F);
                box(leg, "outer_thigh_flange_" + side, 28, 12, side < 0 ? -3.05F : 2.15F, 1.15F, -2.7F, .9F, 4.6F, 5.45F);
                box(leg, "back_thigh_armor_" + side, 17, 20, -1.95F, 1.6F, 2.55F, 3.9F, 6.3F, .8F);
            }
        }

        if (slot == EquipmentSlot.FEET) {
            for (int side : new int[]{-1, 1}) {
                PartDefinition leg = side < 0 ? rightLeg : leftLeg;
                box(leg, "boot_shell_" + side, 0, 16, -2.45F, 5.2F, -2.85F, 4.9F, 6.9F, 5.7F);
                box(leg, "boot_cuff_" + side, 17, 20, -2.85F, 4.45F, -3.1F, 5.7F, 2.5F, 6.2F);
                box(leg, "toe_" + side, 31, 8, -2.5F, 9.75F, -4.25F, 5.0F, 2.2F, 2.15F);
                box(leg, "rune_" + side, 43, 0, -.8F, 7.15F, -3.4F, 1.6F, 2.7F, .55F);
                box(leg, "rune_upper_" + side, 43, 0, -.4F, 6.55F, -3.5F, .8F, .7F, .65F);
                box(leg, "rune_lower_" + side, 43, 0, -.4F, 9.85F, -3.5F, .8F, .7F, .65F);
                box(leg, "heel_guard_" + side, 17, 20, -2.1F, 8.0F, 2.85F, 4.2F, 3.65F, .95F);
                box(leg, "sole_" + side, 17, 20, -2.65F, 11.0F, -4.15F, 5.3F, 1.25F, 7.25F);
                box(leg, "side_guard_" + side, 34, 8, side < 0 ? -3.3F : 2.35F, 6.8F, -.6F, .95F, 3.8F, 2.7F);
                box(leg, "boot_front_shard_" + side, 43, 0, -1.35F, 4.65F, -3.65F, 2.7F, 1.4F, .7F);
                box(leg, "boot_side_rune_" + side, 43, 0, side < 0 ? -3.6F : 2.6F, 7.1F, .35F, .85F, 2.15F, 1.5F);
                box(leg, "boot_heel_layer_" + side, 17, 20, -2.4F, 10.45F, 2.9F, 4.8F, 1.65F, .75F);
            }
        }

        return new HumanoidModel<>(LayerDefinition.create(mesh, 64, 32).bakeRoot());
    }

    private SistersArmorClient() {}
}
