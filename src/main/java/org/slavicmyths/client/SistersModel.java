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

/**
 * Высокодетализированная модель Сестёр-Лихорадок (Трясея и Гнетея) v3.
 * Исправлена проблема дистрофичных "палочных" пропорций, добавлены лица, 
 * объёмный каскадный хвост мага и массивная двухручная коса танка.
 */
public class SistersModel extends EntityModel<SisterBoss> {
    private static final int ATLAS = 1024;
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final boolean isSpirit;

    // UV-слоты текстурного атласа
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

    private static void box(PartDefinition p, int u, int v, float x, float y, float z, float w, float h, float d) {
        p.addOrReplaceChild("box_" + java.util.UUID.randomUUID().toString().substring(0, 8), 
            CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, w, h, d), PartPose.ZERO);
    }

    private static PartDefinition part(PartDefinition p, String name, float x, float y, float z) {
        return p.addOrReplaceChild(name, CubeListBuilder.create(), PartPose.offset(x, y, z));
    }

    public static LayerDefinition create(boolean spirit) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0, 24, 0));
        
        // Общая база головы для обеих сестёр
        PartDefinition head = part(root, "head", 0, -32, 0);
        
        if (spirit) {
            buildTryaseya(root, head);
        } else {
            buildGneteya(root, head);
        }

        return LayerDefinition.create(mesh, ATLAS, ATLAS);
    }

    private static void buildTryaseya(PartDefinition root, PartDefinition head) {
        // ТРЯСЕЯ (Маг дальнего боя — изящная, женственная геометрия)
        PartDefinition body = part(root, "body", 0, -24, 0);
        box(body, CLOTH, 0, -3.5F, -8, -3.5F, 7, 9, 7); // Извилистый тонкий торс
        box(body, CLOTH, 32, -3.0F, -14, -3.0F, 6, 6, 6); // Верх груди

        // Лицо и Капюшон
        box(head, SKIN, 0, -3.5F, -7, -3.5F, 7, 7, 7); // Бледное ведьминское лицо
        box(head, CLOTH, 64, -4.0F, -8, -4.0F, 8, 8, 8); // Капюшон поверх головы
        box(head, GLOW, 0, -2.0F, -4, -3.6F, 1.5F, 1, 0.5F); // Левый бирюзовый глаз
        box(head, GLOW, 0, 0.5F, -4, -3.6F, 1.5F, 1, 0.5F);  // Правый бирюзовый глаз

        // Волосы тонкими пластинами (1px)
        for (int i = 0; i < 6; i++) {
            box(head, CLOTH, 128, -4.1F, -7 + i * 2, -3.8F, 0.5F, 11, 1); // Левые пряди
            box(head, CLOTH, 128, 3.6F, -7 + i * 2, -3.8F, 0.5F, 11, 1);  // Правые пряди
        }

        // Исправление "Эффекта горшка" — Каскадный полый спектральный шлейф пара
        for (int i = 0; i < 6; i++) {
            float size = 8.0F - (i * 1.1F); // Плавное сужение по экспоненте
            PartDefinition ring = part(root, "tail_ring_" + i, 0, -15 + (i * 4), 0);
            box(ring, HEM, i * 16, -size / 2, 0, -size / 2, size, 4, size);
        }

        // Посох Чумы и Парящий Катализатор
        PartDefinition rArm = part(root, "right_arm", -5, -31, 0);
        box(rArm, SKIN, 0, -1.5F, 0, -1.5F, 3, 14, 3);
        PartDefinition staff = part(rArm, "staff", 0, 10, -2);
        box(staff, BONE, 0, -1.0F, -20, -1.0F, 2, 44, 2); // Плотный посох
        box(staff, GLOW, 32, -2.0F, -25, -2.0F, 4, 4, 4); // Парящий магический фонарь

        PartDefinition lArm = part(root, "left_arm", 5, -31, 0);
        box(lArm, SKIN, 0, -1.5F, 0, -1.5F, 3, 14, 3);
    }

    private static void buildGneteya(PartDefinition root, PartDefinition head) {
        // ГНЕТЕЯ (Тяжелый бронированный танк — широкие плечи, выраженная женская талия)
        PartDefinition body = part(root, "body", 0, -24, 0);
        box(body, CLOTH, 0, -5.0F, -9, -4.0F, 10, 9, 8); // Мощная грудь танка
        box(body, HEM, 0, -3.5F, 0, -3.5F, 7, 6, 7);   // Узкая талия (Переход)
        box(body, CLOTH, 64, -4.5F, 6, -4.5F, 9, 5, 9);  // Выраженные бедра

        // Костяной Корсет и Ребра на груди
        for (int i = 0; i < 3; i++) {
            box(body, BONE, i * 8, -5.2F, -7 + (i * 3), -4.2F, 10.4F, 1, 1); // Рёбра-обручи
            box(body, BONE, 40, -1.0F, -9, 4.1F, 2, 9, 1); // Позвоночный хребет на спине
        }

        // Тяжелая костяная Маска-Череп поверх лица
        box(head, SKIN, 0, -3.5F, -7, -3.5F, 7, 7, 7); 
        box(head, BONE, 0, -4.0F, -8, -4.2F, 8, 8, 1); // Фронтальное забрало
        box(head, GLOW, 16, -2.2F, -4, -4.4F, 1.2F, 1, 0.4F); // Левый красный глаз
        box(head, GLOW, 16, 1.0F, -4, -4.4F, 1.2F, 1, 0.4F);  // Правый красный глаз

        // Массивные костяные наплечники со спалдерами (Шипы)
        PartDefinition rArm = part(root, "right_arm", -7, -31, 0);
        box(rArm, CLOTH, 0, -2.0F, 0, -2.0F, 4, 15, 4);
        box(rArm, BONE, 0, -3.0F, -3, -3.0F, 6, 4, 6); // Наплечник
        box(rArm, BONE, 24, -4.5F, -1, -1.0F, 1, 3, 2); // Острый шип 1
        box(rArm, BONE, 24, -6.0F, -1, 1.0F, 1, 4, 2);  // Острый шип 2

        PartDefinition lArm = part(root, "left_arm", 7, -31, 0);
        box(lArm, CLOTH, 0, -2.0F, 0, -2.0F, 4, 15, 4);
        box(lArm, BONE, 0, -3.0F, -3, -3.0F, 6, 4, 6);
        box(lArm, BONE, 24, 3.5F, -1, -1.0F, 1, 3, 2);
        box(lArm, BONE, 24, 5.0F, -1, 1.0F, 1, 4, 2);

        // Двуручная Коса Жнеца
        PartDefinition scythe = part(rArm, "scythe", 0, 11, -3);
        box(scythe, CLOTH, 128, -1.0F, -24, -1.0F, 2, 54, 2); // Обсидиановое длинное древко
        for (int i = 0; i < 5; i++) {
            box(scythe, BONE, 160 + (i * 8), 1.0F + (i * 3), -25 + (i * 2), -1.5F, 4, 4, 3); // Зазубренное лезвие
        }

        // Тяжелые ноги Танка
        PartDefinition rLeg = part(root, "right_leg", -2.5F, -11, 0);
        box(rLeg, CLOTH, 0, -2.0F, 0, -2.0F, 4, 11, 4);
        PartDefinition lLeg = part(root, "left_leg", 2.5F, -11, 0);
        box(lLeg, CLOTH, 0, -2.0F, 0, -2.0F, 4, 11, 4);
    }

    @Override
    public void setupAnim(SisterBoss entity, float walk, float stride, float age, float yaw, float pitch) {
        // Логика парения Трясеи над землей
        if (this.isSpirit) {
            this.root.y = -2.0F + Mth.sin(age * 0.08F) * 2.5F;
            this.rightArm.xRot = -0.5F + Mth.sin(age * 0.05F) * 0.1F;
        } else {
            // Боевая защитная стойка Гнетеи с зажатой косой
            this.root.y = 0.0F;
            this.rightArm.xRot = -0.9F;
            this.rightArm.yRot = -0.3F;
            this.leftArm.xRot = -0.7F;
            this.leftArm.yRot = 0.4F;
            
            float legMove = Mth.cos(walk * 0.6662F) * 1.4F * stride;
            this.root.getChild("right_leg").xRot = legMove;
            this.root.getChild("left_leg").xRot = -legMove;
        }

        // Повороты головы вслед за игроком
        this.head.yRot = yaw * ((float)Math.PI / 180F);
        this.head.xRot = pitch * ((float)Math.PI / 180F);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        this.root.render(pose, buffer, light, overlay, color);
    }
}
