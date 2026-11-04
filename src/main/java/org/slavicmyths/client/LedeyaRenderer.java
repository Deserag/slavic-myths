package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.slavicmyths.client.LedeyaGeometry.Joint;
import org.slavicmyths.sisters.LedeyaBoss;

public final class LedeyaRenderer extends MobRenderer<LedeyaBoss, LedeyaRenderer.Model> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath("slavicmyths", "ledeya"), "main");
    private static final ResourceLocation TEXTURE =
        ResourceLocation.fromNamespaceAndPath("slavicmyths", "textures/entity/ledeya.png");

    public LedeyaRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)), .9F);
    }

    @Override public ResourceLocation getTextureLocation(LedeyaBoss entity) { return TEXTURE; }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0, 24F, 0));
        var panels = LedeyaGeometry.build();
        for (Joint joint : Joint.values()) {
            var cubes = CubeListBuilder.create();
            for (var p : panels) {
                if (p.joint() != joint) continue;
                cubes.texOffs(p.material().u(), p.material().v()).addBox(
                    (float)(p.x() - joint.x), (float)(joint.y - p.y() - p.h()), (float)(p.z() - joint.z),
                    (float)p.w(), (float)p.h(), (float)p.d());
            }
            root.addOrReplaceChild(joint.name(), cubes,
                PartPose.offset((float)joint.x, -(float)joint.y, (float)joint.z));
        }
        return LayerDefinition.create(mesh, 512, 512);
    }

    public static final class Model extends EntityModel<LedeyaBoss> {
        private final ModelPart root;

        private Model(ModelPart baked) { root = baked.getChild("body"); }

        @Override public void setupAnim(LedeyaBoss entity, float walk, float stride, float age, float yaw, float pitch) {
            root.getAllParts().forEach(ModelPart::resetPose);
            float step = Mth.sin(walk * .6662F) * Mth.clamp(stride, 0, 1);
            root.getChild(Joint.HEAD.name()).yRot = yaw * Mth.DEG_TO_RAD * .16F;
            root.getChild(Joint.HEAD.name()).xRot = pitch * Mth.DEG_TO_RAD * .13F;
            root.getChild(Joint.LEFT_LEG.name()).xRot = .25F * step;
            root.getChild(Joint.RIGHT_LEG.name()).xRot = -.25F * step;
            for (Joint joint : Joint.values()) {
                if (joint.ordinal() < Joint.HEM_0.ordinal()) continue;
                ModelPart tier = root.getChild(joint.name());
                tier.y -= (float)LedeyaGeometry.hemLift(joint, step);
                tier.zScale = 1F + .34F * Math.abs(step);
            }

            float ticks = entity.castTicks(Mth.clamp(age - entity.tickCount, 0, 1));
            float cast = 0;
            if (ticks >= 0 && ticks < LedeyaBoss.CAST_TICKS) {
                cast = ticks <= LedeyaBoss.RELEASE_TICK ? ticks / LedeyaBoss.RELEASE_TICK
                    : (LedeyaBoss.CAST_TICKS - ticks) / (LedeyaBoss.CAST_TICKS - LedeyaBoss.RELEASE_TICK);
                cast = cast * cast * (3 - 2 * cast);
            }
            root.getChild(Joint.LEFT_ARM.name()).xRot = -.18F * step - 1.24F * cast;
            root.getChild(Joint.RIGHT_ARM.name()).xRot = .18F * step - 1.42F * cast;
            root.getChild(Joint.LEFT_ARM.name()).yRot = .32F * cast;
            root.getChild(Joint.RIGHT_ARM.name()).yRot = -.32F * cast;
        }

        @Override public void renderToBuffer(PoseStack pose, VertexConsumer out, int light, int overlay, int color) {
            root.render(pose, out, light, overlay, color);
        }
    }
}
