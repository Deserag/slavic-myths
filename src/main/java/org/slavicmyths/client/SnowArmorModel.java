package org.slavicmyths.client;

import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.slavicmyths.winter.SnowArmor;

/** Stepped ice crown and individually articulated winter armor plates. */
public final class SnowArmorModel implements IClientItemExtensions {
    private final Map<EquipmentSlot, HumanoidModel<?>> models = new EnumMap<>(EquipmentSlot.class);

    private SnowArmorModel() {
        for (var slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            models.put(slot, create(slot));
        }
    }

    public static void register(RegisterClientExtensionsEvent event) {
        var extension = new SnowArmorModel();
        for (var holder : SnowArmor.ITEMS.values()) event.registerItem(extension, holder.get());
    }

    public static HumanoidModel<?> create(EquipmentSlot slot) {
        try (var stream = SnowArmorModel.class.getResourceAsStream("/assets/slavicmyths/snow_armor/rig.json");
             var reader = new InputStreamReader(java.util.Objects.requireNonNull(stream), StandardCharsets.UTF_8)) {
            var data = JsonParser.parseReader(reader).getAsJsonObject();
            var atlas = data.getAsJsonArray("atlas");
            var materials = data.getAsJsonArray("materials");
            var mesh = new MeshDefinition();
            var root = mesh.getRoot();
            for (String name : new String[]{"head", "hat", "body", "left_arm", "right_arm", "left_leg", "right_leg"}) {
                float x = name.equals("left_arm") ? 5 : name.equals("right_arm") ? -5 :
                    name.equals("left_leg") ? 1.9F : name.equals("right_leg") ? -1.9F : 0;
                float y = name.endsWith("arm") ? 2 : name.endsWith("leg") ? 12 : 0;
                root.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.offset(x, y, 0));
            }
            int index = 0;
            for (var raw : data.getAsJsonArray("parts")) {
                var p = raw.getAsJsonArray();
                if (!p.get(0).getAsString().equals(slot.getName())) continue;
                String bone = p.get(1).getAsString(), material = p.get(2).getAsString();
                int tile = 0;
                while (tile < materials.size() && !materials.get(tile).getAsString().equals(material)) tile++;
                if (tile == materials.size()) throw new IllegalArgumentException("Unknown snow armor material " + material);
                root.getChild(bone).addOrReplaceChild("ice_plate_" + index++,
                    CubeListBuilder.create().texOffs(tile % 4 * 64, tile / 4 * 64)
                        .addBox(p.get(3).getAsFloat(), p.get(4).getAsFloat(), p.get(5).getAsFloat(),
                            p.get(6).getAsFloat(), p.get(7).getAsFloat(), p.get(8).getAsFloat()), PartPose.ZERO);
            }
            return new HumanoidModel<>(LayerDefinition.create(mesh, atlas.get(0).getAsInt(), atlas.get(1).getAsInt()).bakeRoot());
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("Cannot read snow armor geometry", ex);
        }
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
        var model = models.get(slot);
        return model == null ? original : ArmorSlotVisibility.apply(model, slot);
    }
}
