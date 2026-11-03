package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Monster;
import org.slavicmyths.sisters.SisterBoss;

public class SistersModel extends EntityModel<SisterBoss> {
    private static final int ATLAS = 1024;
    private static int serial; // Вводим статический счетчик вместо тяжелых UUID

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final boolean isSpirit;

    private static final int SKIN = 0;
    private static final int CLOTH = 256;
    private static final int BONE = 512;
    private static final int HEM = 768;
    private static final int GLOW = 128;

    public SistersModel(ModelPart part, boolean spirit) {
        this.root = part.getChild("root");
        this.head = this.root.getChild("head");
        this.leftArm = this.root.getChild("left_arm");
        this.rightArm = this.root.getChild("right_arm");
        this.isSpirit = spirit;
    }
    // БОЛЬШЕ НИКАКИХ UUID: Используем быстрый инкремент строк
    private static void box(PartDefinition p, int u, int v, float x, float y, float z, float w, float h, float d) {
        p.addOrReplaceChild("box_" + serial++, 
            CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d), PartPose.ZERO);
    }

    private static PartDefinition part(PartDefinition p, String name, float x, float y, float z) {
        return p.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.offset(x, y, z));
    }

    public static LayerDefinition create(boolean spirit) {
        serial = 0; // Сбрасываем счетчик при каждом новом запекании слоя
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, 24, 0));
        
        PartDefinition head = part(root, "head", 0, -32, 0);
        
        if (spirit) {
            buildTryaseya(root, head);
        } else {
            buildGneteya(root, head);
        }

        return LayerDefinition.create(mesh, ATLAS, ATLAS);
    }

    private static void buildTryaseya(PartDefinition root, PartDefinition head) {
        PartDefinition body = part(root, "body", 0, -24, 0);
        box(body, CLOTH, 0, -3.5F, -8, -3.5F, 7, 9, 7);
        box(body, CLOTH, 32, -3.0F, -14, -3.0F, 6, 6, 6);

        box(head, SKIN, 0, -3.5F, -7, -3.5F, 7, 7, 7);
        box(head, CLOTH, 64, -4.0F, -8, -4.0F, 8, 8, 8);
        box(head, GLOW, 0, -2.0F, -4, -3.6F, 1.5F, 1, 0.5F);
        box(head, GLOW, 0, 0.5F, -4, -3.6F, 1.5F, 1, 0.5F);

        for (int i = 0; i < 6; i++) {
            box(head, CLOTH, 128, -4.1F, -7 + i * 2, -3.8F, 0.5F, 11, 1);
            box(head, CLOTH, 128, 3.6F, -7 + i * 2, -3.8F, 0.5F, 11, 1);
        }

        for (int i = 0; i < 6; i++) {
            float size = 8.0F - (i * 1.1F);
            // Прописываем предсказуемые имена лимбов
            PartDefinition ring = part(root, "tail_ring_" + i, 0, -15 + (i * 4), 0);
            box(ring, HEM, i * 16, -size / 2, 0, -size / 2, size, 4, size);
        }

        PartDefinition rArm = part(root, "right_arm", -5, -31, 0);
        box(rArm, SKIN, 0, -1.5F, 0, -1.5F, 3, 14, 3);
        PartDefinition staff = part(rArm, "staff", 0, 10, -2);
        box(staff, BONE, 0, -1.0F, -20, -1.0F, 2, 44, 2);
        box(staff, GLOW, 32, -2.0F, -25, -2.0F, 4, 4, 4);

        PartDefinition lArm = part(root, "left_arm", 5, -31, 0);
        box(lArm, SKIN, 0, -1.5F, 0, -1.5F, 3, 14, 3);
    }

    private static void buildGneteya(PartDefinition root, PartDefinition head) {
        PartDefinition body = part(root, "body", 0, -24, 0);
        box(body, CLOTH, 0, -5.0F, -9, -4.0F, 10, 9, 8);
        box(body, HEM, 0, -3.5F, 0, -3.5F, 7, 6, 7);
        box(body, CLOTH, 64, -4.5F, 6, -4.5F, 9, 5, 9);

        for (int i = 0; i < 3; i++) {
            box(body, BONE, i * 8, -5.2F, -7 + (i * 3), -4.2F, 10.4F, 1, 1);
            box(body, BONE, 40, -1.0F, -9, 4.1F, 2, 9, 1);
        }

        box(head, SKIN, 0, -3.5F, -7, -3.5F, 7, 7, 7);
        box(head, BONE, 0, -4.0F, -8, -4.2F, 8, 8, 1);
        box(head, GLOW, 16, -2.2F, -4, -4.4F, 1.2F, 1, 0.4F);
        box(head, GLOW, 16, 1.0F, -4, -4.4F, 1.2F, 1, 0.4F);

        PartDefinition rArm = part(root, "right_arm", -7, -31, 0);
        box(rArm, CLOTH, 0, -2.0F, 0, -2.0F, 4, 15, 4);
        box(rArm, BONE, 0, -3.0F, -3, -3.0F, 6, 4, 6);
        box(rArm, BONE, 24, -4.5F, -1, -1.0F, 1, 3, 2);
        box(rArm, BONE, 24, -6.0F, -1, 1.0F, 1, 4, 2);

        PartDefinition lArm = part(root, "left_arm", 7, -31, 0);
        box(lArm, CLOTH, 0, -2.0F, 0, -2.0F, 4, 15, 4);
        box(lArm, BONE, 0, -3.0F, -3, -3.0F, 6, 4, 6);
        box(lArm, BONE, 24, 3.5F, -1, -1.0F, 1, 3, 2);
        box(lArm, BONE, 24, 5.0F, -1, 1.0F, 1, 4, 2);

        PartDefinition scythe = part(rArm, "scythe", 0, 11, -3);
        box(scythe, CLOTH, 128, -1.0F, -24, -1.0F, 2, 54, 2);
        for (int i = 0; i < 5; i++) {
            box(scythe, BONE, 160 + (i * 8), 1.0F + (i * 3), -25 + (i * 2), -1.5F, 4, 4, 3);
        }

        PartDefinition rLeg = part(root, "right_leg", -2.5F, -11, 0);
        box(rLeg, CLOTH, 0, -2.0F, 0, -2.0F, 4, 11, 4);
        PartDefinition lLeg = part(root, "left_leg", 2.5F, -11, 0);
        box(lLeg, CLOTH, 0, -2.0F, 0, -2.0F, 4, 11, 4);
    }

    @Override
    public void setupAnim(SisterBoss entity, float walk, float stride, float age, float yaw, float pitch) {
        if (this.isSpirit) {
            this.root.y = -2.0F + Mth.sin(age * 0.08F) * 2.5F;
            this.rightArm.xRot = -0.5F + Mth.sin(age * 0.05F) * 0.1F;
        } else {
            this.root.y = 0.0F;
            this.rightArm.xRot = -0.9F;
            this.rightArm.yRot = -0.3F;
            this.leftArm.xRot = -0.7F;
            this.leftArm.yRot = 0.4F;
            
            float legMove = Mth.cos(walk * 0.6662F) * 1.4F * stride;
            this.root.getChild("right_leg").xRot = legMove;
            this.root.getChild("left_leg").xRot = -legMove;
        }

        this.head.yRot = yaw * ((float)Math.PI / 180F);
        this.head.xRot = pitch * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        this.root.render(pose, buffer, light, overlay, color);
    }
}
