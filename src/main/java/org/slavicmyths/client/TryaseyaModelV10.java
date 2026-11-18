package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.slavicmyths.sisters.SisterBoss;

/** Minecraft 1.21.1 / NeoForge model wrapper. Geometry comes exclusively from TryaseyaGeometryV10.java. */
public final class TryaseyaModelV10 extends EntityModel<SisterBoss> {
    public static final String[] MATERIALS = {"skin","skin_shadow","eye","eye_glint","mouth","purple_dark","purple_mid","purple_light","hair_dark","hair_mid","hair_light","hair_bright","mist","mist_hi","silver","staff","staff_dark","lantern","frame"};
    private final ModelPart root;

    public TryaseyaModelV10(ModelPart baked) { this.root = baked.getChild("root"); }

    public static LayerDefinition create() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);
        Map<String, CubeListBuilder> groups = new LinkedHashMap<>();
        for (TryaseyaGeometryV10.Box box : TryaseyaGeometryV10.build()) {
            int swatch = Arrays.asList(MATERIALS).indexOf(box.material());
            if (swatch < 0) throw new IllegalStateException("Unknown material: " + box.material());
            CubeListBuilder cubes = groups.computeIfAbsent(box.part(), k -> CubeListBuilder.create());
            int u = (swatch % 8) * 128;
            int v = (swatch / 8) * 128;
            cubes.texOffs(u, v).addBox((float)box.x(), (float)(-box.y()-box.h()), (float)box.z(), (float)box.w(), (float)box.h(), (float)box.d());
        }
        for (Map.Entry<String, CubeListBuilder> entry : groups.entrySet()) root.addOrReplaceChild(entry.getKey(), entry.getValue(), PartPose.ZERO);
        return LayerDefinition.create(mesh, 1024, 1024);
    }

    @Override public void setupAnim(SisterBoss boss, float walk, float stride, float age, float yaw, float pitch) { root.y = (float)Math.sin(age * .08F) * .35F; }
    @Override public void renderToBuffer(PoseStack pose, VertexConsumer out, int light, int overlay, int color) { root.render(pose, out, light, overlay, color); }
}