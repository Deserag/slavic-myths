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
        var parts = new java.util.EnumMap<Joint, net.minecraft.client.model.geom.builders.PartDefinition>(Joint.class);
        for (Joint joint : Joint.values()) {
            var cubes = CubeListBuilder.create();
            for (var p : panels) {
                if (p.joint() != joint) continue;
                cubes.texOffs(p.material().u(), p.material().v()).addBox(
                    (float)(p.x() - joint.x), (float)(joint.y - p.y() - p.h()), (float)(p.z() - joint.z),
                    (float)p.w(), (float)p.h(), (float)p.d());
            }
            Joint parent = joint.parent();
            var owner = parent == null ? root : parts.get(parent);
            float x = (float)(joint.x - (parent == null ? 0 : parent.x));
            float y = (float)((parent == null ? 0 : parent.y) - joint.y);
            float z = (float)(joint.z - (parent == null ? 0 : parent.z));
            parts.put(joint, owner.addOrReplaceChild(joint.name(), cubes, PartPose.offset(x, y, z)));
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
                if (joint.ordinal() < Joint.HEM_0.ordinal() || joint.ordinal() > Joint.HEM_6.ordinal()) continue;
                ModelPart tier = root.getChild(joint.name());
                tier.y -= (float)LedeyaGeometry.hemLift(joint, step);
                tier.zScale = 1F + .34F * Math.abs(step);
            }

            float ticks = entity.attackTicks(Mth.clamp(age - entity.tickCount, 0, 1));
            LedeyaBoss.Attack attack = entity.attack();
            float draw = attack.draw(ticks);
            float ready = ticks < 0 || ticks >= attack.duration ? 0
                : Math.min(1, Math.min(ticks / 6F, (attack.duration - ticks) / 8F));
            boolean rain = attack == LedeyaBoss.Attack.RAIN;
            ModelPart left = root.getChild(Joint.LEFT_ARM.name());
            ModelPart right = root.getChild(Joint.RIGHT_ARM.name());
            left.xRot = -.18F * step - (rain ? 2.32F : 1.44F) * ready;
            right.xRot = .18F * step - (rain ? 2.18F : 1.43F) * ready + .10F * draw;
            right.yRot = .68F * ready;
            ModelPart bow = left.getChild(Joint.BOW.name());
            bow.xRot = -left.xRot - (rain ? 1.17F * ready : 0);
            ModelPart upper = bow.getChild(Joint.STRING_UP.name());
            ModelPart lower = bow.getChild(Joint.STRING_DOWN.name());
            upper.z += 2.2F * draw;
            lower.z += 2.2F * draw;
            upper.xRot = (.24F / (float)LedeyaGeometry.BOW_LENGTH_SCALE) * draw;
            lower.xRot = -(.24F / (float)LedeyaGeometry.BOW_LENGTH_SCALE) * draw;
            boolean nocked = ticks >= 0 && switch (attack) {
                case SHOT -> ticks < 16;
                case RAIN -> ticks < 18;
                case RAPID -> ticks < 34;
            };
            for (Joint joint : new Joint[]{Joint.ARROW_CENTER, Joint.ARROW_LEFT, Joint.ARROW_RIGHT}) {
                ModelPart arrow = bow.getChild(joint.name());
                arrow.visible = nocked && (rain || joint == Joint.ARROW_CENTER);
                arrow.z += 2.2F * draw;
            }
        }

        @Override public void renderToBuffer(PoseStack pose, VertexConsumer out, int light, int overlay, int color) {
            root.render(pose, out, light, overlay, color);
        }
    }
    @Override protected boolean shouldShowName(LedeyaBoss boss) { return false; }

}
