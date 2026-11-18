package org.slavicmyths.client;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/** The two distinct native Mor/Fire heads with their joined neck, without re-scaling either axis. */
public final class TrophyWideModelV4 {
    private TrophyWideModelV4() {}

    private static int[] tile(String mat) {
        return switch (mat) {
            case "mor_slate" -> new int[]{4,4};
            case "mor_charcoal", "charcoal" -> new int[]{132,4};
            case "fire_obsidian" -> new int[]{260,4};
            case "fire_charcoal" -> new int[]{132,4};
            case "mor_web" -> new int[]{4,132};
            case "fire_web" -> new int[]{132,132};
            case "bone" -> new int[]{260,132};
            case "scorched_bone" -> new int[]{388,132};
            case "mor_vein" -> new int[]{4,260};
            case "fire_crack" -> new int[]{132,260};
            case "mor_crest", "fire_crest", "crest" -> new int[]{388,260};
            case "near_black" -> new int[]{260,260};
            case "mor_eye" -> new int[]{4,388};
            case "fire_eye" -> new int[]{132,388};
            default -> throw new IllegalStateException("Unmapped trophy material: " + mat);
        };
    }

    public static ModelPart createTwin() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeListBuilder cubes = CubeListBuilder.create();
        for (TrophyNativePreviewV4.Box b : TrophyNativePreviewV4.build(false)) {
            int[] uv = tile(b.material());
            cubes.texOffs(uv[0], uv[1]).addBox(
                (float)(b.x() - 16), (float)(-b.y() - b.h()), (float)(b.z() - 16),
                (float)b.w(), (float)b.h(), (float)b.d());
        }
        root.addOrReplaceChild("twin", cubes, PartPose.ZERO);
        return LayerDefinition.create(mesh, 512, 512).bakeRoot().getChild("twin");
    }
}