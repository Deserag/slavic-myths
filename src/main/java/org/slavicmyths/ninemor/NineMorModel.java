package org.slavicmyths.ninemor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * Procedural version of the approved graphite/burgundy nine-head prototype.
 * Body cuboids are baked once. Six wings use faceted triangle membranes,
 * not hundreds of voxel cubes per web. Head parts, jaw parts, legs and
 * individual tails are parented separately for independent animation.
 */
public final class NineMorModel extends EntityModel<NineMorSerpent> {
    private static final int SLATE_U = 4, SLATE_V = 4;
    private static final int STONE_U = 132, STONE_V = 4;
    private static final int BELLY_U = 388, BELLY_V = 132;
    private static final int BONES_U = 260, BONES_V = 132;
    private static final float[][] HEAD_POS = {
        {-12.7F, -24.4F, 30.5F}, {0, -26.2F, 31.6F}, {12.7F, -24.4F, 30.5F},
        {-14.7F, -20.8F, 38.6F}, {0, -21.9F, 39.3F}, {14.7F, -20.8F, 38.6F},
        {-8.1F, -16.7F, 46.8F}, {0, -17.3F, 48.8F}, {8.1F, -16.7F, 46.8F}
    };
    private static final float[][] WING_SHAPES = {
        {56, -8, -17.5F, -34, -22, -10, 3, 36.5F, 32, 26, 18.8F, -2.9F, 23.5F},
        {51, 2.8F, 4, -13, 0, 14, 27, 32, 29.5F, 23, 17.7F, 0, 23},
        {45, 11.5F, 22, 13, 26, 39, 51, 28, 25.6F, 20, 15, 2.9F, 22.4F}
    };

    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart[] necks = new ModelPart[9];
    private final ModelPart[] heads = new ModelPart[9];
    private final ModelPart[] jaws = new ModelPart[9];
    private final ModelPart[] accents = new ModelPart[9];
    private final ModelPart[] wings = new ModelPart[6];
    private final ModelPart[] legs = new ModelPart[4];
    private final ModelPart[] tails = new ModelPart[3];
    private final WingData[] wingData = new WingData[6];

    private record WingData(float[] root, float[] wrist, float[][] points, int color) {}

    public NineMorModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition base = mesh.getRoot();
        PartDefinition torso = base.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0, 24, 0));
        buildTorso(torso);
        buildLegs(torso);
        buildHeads(torso);
        buildTails(torso);
        buildWings(torso);
        root = LayerDefinition.create(mesh, 512, 512).bakeRoot();
        body = root.getChild("body");
        for (int i = 0; i < 9; i++) {
            necks[i] = body.getChild("neck_" + i);
            heads[i] = necks[i].getChild("head_" + i);
            jaws[i] = heads[i].getChild("jaw_" + i);
            accents[i] = heads[i].getChild("accent_" + i);
        }
        for (int i = 0; i < 6; i++) wings[i] = body.getChild("wing_" + i);
        for (int i = 0; i < 4; i++) legs[i] = body.getChild("leg_" + i);
        for (int i = 0; i < 3; i++) tails[i] = body.getChild("tail_" + i);
    }

    private static float smooth(float t) { return t * t * (3F - 2F * t); }
    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    private static void buildTorso(PartDefinition body) {
        CubeListBuilder torso = CubeListBuilder.create();
        for (float y = -11.5F; y < 16; y += 1.65F) {
            float yshape = Math.max(.23F, 1F - (float)Math.pow((y - 1.5F) / 17.8F, 2));
            float rx = 8F * Mth.sqrt(yshape);
            for (float x = -8.5F; x < 9F; x += 1.52F) {
                float xshape = 1F - (x / rx) * (x / rx);
                if (xshape <= 0) continue;
                float rz = 7.15F * Mth.sqrt(xshape * yshape);
                for (float z = 7F; z < 22.7F; z += 1.48F) {
                    if (z < 14.1F - rz || z > 14.1F + rz) continue;
                    boolean underside = z < 12.6F;
                    torso.texOffs(underside ? BELLY_U : (x + y + z) % 3 > 0 ? STONE_U : SLATE_U,
                        underside ? BELLY_V : (x + y + z) % 3 > 0 ? STONE_V : SLATE_V)
                        .addBox(x - .825F, -z - .8F, y - .86F, 1.65F, 1.6F, 1.72F);
                }
            }
        }
        for (float x = -6.8F; x < 7F; x += 1.7F) {
            for (float y = -12.2F; y < -7.8F; y += 1.55F) {
                float z = 15.4F + 1.2F * (1 - Math.abs(x) / 8F);
                torso.texOffs(STONE_U, STONE_V).addBox(x - .93F, -z - 2.3F, y - .875F, 1.86F, 4.6F, 1.75F);
            }
        }
        for (float y = -8F; y < 15F; y += 2.55F) {
            for (float x : new float[]{-4.7F, -2.4F, 0, 2.4F, 4.7F}) {
                float sy = Math.max(.1F, 1F - (float)Math.pow((y - 1.5F) / 17.8F, 2));
                float sx = 1F - (float)Math.pow(x / (8 * Mth.sqrt(sy)), 2);
                if (sx <= 0) continue;
                float z = 14.1F + 7.15F * Mth.sqrt(sx * sy);
                float height = 1.2F + ((int)Math.abs(x * 7 + y * 3) % 4) * .55F;
                torso.texOffs(BONES_U, BONES_V).addBox(x - .6F, -z - height, y - .65F, 1.2F, height, 1.3F);
                torso.texOffs(STONE_U, STONE_V).addBox(x - .85F, -z - .5F, y - .9F, 1.7F, 1F, 1.8F);
            }
        }
        body.addOrReplaceChild("shell", torso, PartPose.ZERO);
    }

    private static void buildLegs(PartDefinition body) {
        int id = 0;
        for (int front = 0; front < 2; front++) for (int side : new int[]{-1, 1}) {
            float x = side * (front == 0 ? 6.8F : 6.3F);
            float y = front == 0 ? -7.9F : 10.2F;
            CubeListBuilder leg = CubeListBuilder.create()
                .texOffs(STONE_U, STONE_V).addBox(-1.75F, -12.3F, -2.2F, 3.5F, 4.4F, 4.4F)
                .texOffs(SLATE_U, SLATE_V).addBox(-1.45F + side * .3F, -8.6F, -1.5F, 2.7F, 4F, 3.1F)
                .texOffs(SLATE_U, SLATE_V).addBox(-1.6F + side * .4F, -4.6F, -2.6F, 3.15F, 3.9F, 4.4F);
            for (int toe = -1; toe <= 1; toe++) {
                leg.texOffs(STONE_U, STONE_V).addBox(toe * 1.15F - .47F, -1.8F, -4.1F, .94F, 1.35F, 1.75F);
                leg.texOffs(BONES_U, BONES_V).addBox(toe * 1.15F - .30F, -1.2F, -4.65F, .6F, .65F, .7F);
            }
            body.addOrReplaceChild("leg_" + id++, leg, PartPose.offset(x, 0, y));
        }
    }

    private static void buildHeads(PartDefinition body) {
        for (int i = 0; i < 9; i++) {
            float hx = HEAD_POS[i][0], hy = HEAD_POS[i][1], hz = HEAD_POS[i][2];
            int row = i / 3, lane = i % 3 - 1;
            float x0 = lane * 2.85F, y0 = -10.7F + row * .65F, z0 = 21.5F + row * .4F;
            CubeListBuilder neck = CubeListBuilder.create();
            for (int j = 0; j < 17; j++) {
                float t = (j + .5F) / 17F, e = smooth(t);
                float nx = lerp(x0, hx, e)
                    + .6F * Mth.sin(t * Mth.TWO_PI + i * .67F) * Mth.sin(t * Mth.PI);
                float ny = lerp(y0, hy + 2.4F, t) - .9F * Mth.sin(t * Mth.PI);
                float nz = lerp(z0, hz - .8F, t) + .5F * Mth.sin(t * Mth.PI);
                float w = 1.48F - .22F * t;
                neck.texOffs(j % 4 == 0 ? STONE_U : SLATE_U, j % 4 == 0 ? STONE_V : SLATE_V)
                    .addBox(nx - w, -nz - w, ny - 1.1F, w * 2F, w * 2F, 2.2F);
                if (j == 5 || j == 9 || j == 13)
                    neck.texOffs(BONES_U, BONES_V).addBox(nx - .4F, -nz - 1.8F, ny - .45F, .8F, 1.2F, .9F);
            }
            PartDefinition neckPart = body.addOrReplaceChild("neck_" + i, neck, PartPose.ZERO);
            CubeListBuilder skull = CubeListBuilder.create()
                .texOffs(SLATE_U, SLATE_V).addBox(-1.78F, -2.2F, -1.55F, 3.55F, 3.15F, 3.1F)
                .texOffs(STONE_U, STONE_V).addBox(-1.34F, -.60F, -4.0F, 2.68F, 1.60F, 3.0F)
                .texOffs(BONES_U, BONES_V).addBox(-1.4F, -2.85F, -.5F, .65F, 1.35F, 1.3F)
                .texOffs(BONES_U, BONES_V).addBox(.75F, -2.85F, -.5F, .65F, 1.35F, 1.3F);
            PartDefinition head = neckPart.addOrReplaceChild("head_" + i, skull,
                PartPose.offset(hx, -hz, hy));
            CubeListBuilder jaw = CubeListBuilder.create()
                .texOffs(STONE_U, STONE_V).addBox(-1.22F, -.22F, -4.05F, 2.44F, .7F, 3.3F)
                .texOffs(BONES_U, BONES_V).addBox(-.95F, -.48F, -4.45F, .25F, .70F, .45F)
                .texOffs(BONES_U, BONES_V).addBox(.7F, -.48F, -4.45F, .25F, .70F, .45F);
            head.addOrReplaceChild("jaw_" + i, jaw, PartPose.offset(0, .82F, 0));
            CubeListBuilder accent = CubeListBuilder.create()
                .texOffs(4, 132).addBox(-1.50F, -2.3F, -2.7F, 3F, .6F, 2.2F)
                .texOffs(4, 132).addBox(-.55F, -3.45F, -.3F, 1.1F, 1.3F, .95F)
                .texOffs(4, 132).addBox(-1.78F, -1.95F, -2.1F, .24F, .55F, .75F)
                .texOffs(4, 132).addBox(1.54F, -1.95F, -2.1F, .24F, .55F, .75F);
            head.addOrReplaceChild("accent_" + i, accent, PartPose.ZERO);
        }
    }

    private static void buildTails(PartDefinition body) {
        for (int i = 0; i < 3; i++) {
            int lane = i - 1;
            CubeListBuilder tail = CubeListBuilder.create();
            for (int n = 0; n < 31; n++) {
                float t = n / 30F;
                float x = lane * (2.7F + 17.2F * (float)Math.pow(t, 1.45F))
                    + .8F * Mth.sin(t * 6.8F + i * 1.2F) * t;
                float y = 13F + 37F * t;
                float z = 13.6F - 8.7F * t
                    + 1.35F * Mth.sin(t * 8.2F + i * .7F) * Mth.sin(Mth.PI * t);
                float radius = 2.4F * (float)Math.pow(1 - t, 1.1F) + .35F;
                tail.texOffs(n % 4 == 0 ? STONE_U : SLATE_U, n % 4 == 0 ? STONE_V : SLATE_V)
                    .addBox(x - radius * .825F, -z - radius * .7F, y - .89F,
                            radius * 1.65F, radius * 1.4F, 1.78F);
                if (n % 3 == 1 && t < .88F)
                    tail.texOffs(BONES_U, BONES_V).addBox(x - .34F, -z - radius, y - .36F,
                        .68F, 1.25F * (1 - t) + .6F, .72F);
            }
            body.addOrReplaceChild("tail_" + i, tail, PartPose.ZERO);
        }
    }

    private void buildWings(PartDefinition body) {
        for (int pair = 0; pair < 3; pair++) for (int side : new int[]{-1, 1}) {
            float[] w = WING_SHAPES[pair];
            float[] rootPos = {side * 6.8F, -w[12], w[11]};
            float[] elbow = {side * (13.5F + pair * .55F), -(31F - pair * 2F), w[1]};
            float[] wrist = {side * (26F - pair), -(33.5F - pair * 3F), w[2]};
            float[][] fan = new float[5][3];
            for (int j = 0; j < 4; j++) {
                fan[j] = new float[]{side * (w[0] - new float[]{0, 3.6F, 9.5F, 17F}[j]),
                    -w[7 + j], w[3 + j]};
            }
            fan[4] = new float[]{side * 8.5F, -(18.5F - pair * .35F), 10.5F + pair * 6.6F};
            int index = pair * 2 + (side > 0 ? 1 : 0);
            wingData[index] = new WingData(rootPos, wrist, fan,
                pair == 0 ? 0xFF764D5B : pair == 1 ? 0xFF78535C : 0xFF704D5A);
            PartDefinition wing = body.addOrReplaceChild("wing_" + index,
                CubeListBuilder.create().texOffs(BONES_U, BONES_V)
                    .addBox(-1.5F, -1.2F, -1.4F, 3F, 2.4F, 2.8F),
                PartPose.offset(rootPos[0], rootPos[1], rootPos[2]));
            int count = 0;
            count = beam(wing, count, rootPos, elbow, rootPos, 1.8F);
            count = beam(wing, count, elbow, wrist, rootPos, 1.45F);
            for (int j = 0; j < 5; j++) {
                float[] tip = fan[j];
                float[] mid = midpoint(wrist, tip);
                count = beam(wing, count, wrist, mid, rootPos, j == 4 ? .70F : 1.05F - j * .08F);
                count = beam(wing, count, mid, tip, rootPos, j == 4 ? .38F : .7F - j * .05F);
            }
            wing.addOrReplaceChild("elbow", CubeListBuilder.create()
                .texOffs(BONES_U, BONES_V).addBox(-1.1F, -1.1F, -1.1F, 2.2F, 2.2F, 2.2F),
                PartPose.offset(elbow[0] - rootPos[0], elbow[1] - rootPos[1], elbow[2] - rootPos[2]));
            wing.addOrReplaceChild("wrist", CubeListBuilder.create()
                .texOffs(BONES_U, BONES_V).addBox(-1.1F, -1.1F, -1.1F, 2.2F, 2.2F, 2.2F),
                PartPose.offset(wrist[0] - rootPos[0], wrist[1] - rootPos[1], wrist[2] - rootPos[2]));
        }
    }

    private static float[] midpoint(float[] a, float[] b) {
        return new float[]{(a[0] + b[0]) / 2, (a[1] + b[1]) / 2, (a[2] + b[2]) / 2};
    }

    private static int beam(PartDefinition wing, int index, float[] a, float[] b, float[] root, float width) {
        float dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
        float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= .01F) return index;
        float pitch = (float)Math.atan2(dz, dy);
        float roll = -(float)Math.atan2(dx, Mth.sqrt(dy * dy + dz * dz));
        wing.addOrReplaceChild("bone_" + index, CubeListBuilder.create().texOffs(BONES_U, BONES_V)
                .addBox(-width / 2F, 0, -width / 2F, width, length, width),
            PartPose.offsetAndRotation(a[0] - root[0], a[1] - root[1], a[2] - root[2],
                pitch, 0F, roll));
        return index + 1;
    }

    @Override
    public void setupAnim(NineMorSerpent entity, float limb, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        int flight = entity.flightPhase();
        body.y = 24F + Mth.sin(age * .055F) * (flight == 2 ? .75F : .16F);
        body.xRot = flight == 2 ? -.10F : flight == 1 ? -.055F : 0F;
        for (int i = 0; i < 4; i++) {
            legs[i].xRot = flight == 0
                ? Mth.cos(limb * .65F + (i == 0 || i == 3 ? Mth.PI : 0)) * amount * .55F
                : .52F;
        }
        for (int i = 0; i < 9; i++) {
            int mode = entity.headMode(i), actionAge = entity.headAge(i);
            float motion = Mth.sin(age * (.042F + i * .002F) + i * .8F);
            necks[i].xRot = motion * .018F;
            necks[i].yRot = Mth.cos(age * .037F + i * 1.09F) * .050F;
            if (mode != 0) {
                float anticipate = Math.min(1F, actionAge / 16F);
                necks[i].xRot -= anticipate * (mode == NineMorSerpent.GRAB ? .22F : .09F);
                necks[i].yRot += Mth.sin(i * 2.1F) * anticipate * .11F;
            }
            heads[i].xRot = mode == 0 ? Mth.sin(age * .05F + i) * .025F
                : -Math.min(.2F, actionAge * .012F);
            jaws[i].xRot = mode == 0 ? .05F
                : (mode == NineMorSerpent.GRAB ? .72F : .46F)
                    * Mth.sin(Mth.PI * Mth.clamp(actionAge / 38F, 0, 1));
            accents[i].visible = false;
        }
        for (int i = 0; i < 3; i++) {
            tails[i].xRot = Mth.sin(age * (.055F + i * .005F) + i) * .05F;
            tails[i].yRot = Mth.sin(age * .038F + i * 1.25F) * .065F
                + ((entity.tailMask() & (1 << i)) != 0 ? Mth.sin(age * .4F) * .17F : 0F);
        }
        for (int i = 0; i < 6; i++) {
            float side = i % 2 == 0 ? -1F : 1F;
            float shift = i / 2 * .38F;
            wings[i].zRot = side * (flight == 0 ? .10F :
                flight == 1 ? .25F - entity.flightAge() / 25F * .30F :
                flight == 2 ? Mth.sin(age * .17F + shift) * .53F :
                -.20F + Mth.sin(age * .12F + shift) * .20F);
            wings[i].xRot = (flight == 2 ? Mth.sin(age * .14F + shift) * .11F : .045F);
            wings[i].yRot = side * (flight == 0 ? .02F : .05F);
        }
    }

    @Override
    public void renderToBuffer(PoseStack stack, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(stack, consumer, light, overlay, color);
        // These are actual two-sided colored triangles, not thousands of boxes.
        stack.pushPose();
        body.translateAndRotate(stack);
        for (int i = 0; i < 6; i++) {
            stack.pushPose();
            wings[i].translateAndRotate(stack);
            renderMembrane(stack, consumer, light, overlay, wingData[i]);
            stack.popPose();
        }
        stack.popPose();
    }

    public void renderAccents(PoseStack stack, VertexConsumer consumer, int light, int overlay) {
        stack.pushPose();
        body.translateAndRotate(stack);
        for (int i = 0; i < 9; i++) {
            stack.pushPose();
            necks[i].translateAndRotate(stack);
            heads[i].translateAndRotate(stack);
            accents[i].visible = true;
            accents[i].render(stack, consumer, light, overlay, 0xFF000000 | NineMorSerpent.COLORS[i]);
            accents[i].visible = false;
            stack.popPose();
        }
        stack.popPose();
    }

    private static float[] point(float[] wrist, float[] a, float[] b, float t, float u, float scallop) {
        float edge = 1F - scallop * (float)Math.pow(Mth.sin(Mth.PI * u), 1.2F);
        float radial = t * edge;
        float[] p = new float[3];
        for (int i = 0; i < 3; i++) {
            float boundary = lerp(a[i], b[i], u);
            p[i] = lerp(wrist[i], boundary, radial);
        }
        p[1] -= 1.05F * Mth.sin(Mth.PI * radial) * Mth.sin(Mth.PI * u);
        return p;
    }

    private static void renderMembrane(PoseStack stack, VertexConsumer consumer, int light,
                                        int overlay, WingData wing) {
        float[] root = wing.root();
        float[] wrist = wing.wrist();
        float[][] fan = wing.points();
        for (int bay = 0; bay < 4; bay++) for (int radial = 0; radial < 4; radial++)
            for (int angle = 0; angle < 5; angle++) {
                float t0 = radial / 4F, t1 = (radial + 1) / 4F;
                float u0 = angle / 5F, u1 = (angle + 1) / 5F;
                float scallop = bay < 3 ? .25F : .13F;
                float[] p00 = point(wrist, fan[bay], fan[bay + 1], t0, u0, scallop);
                float[] p01 = point(wrist, fan[bay], fan[bay + 1], t0, u1, scallop);
                float[] p10 = point(wrist, fan[bay], fan[bay + 1], t1, u0, scallop);
                float[] p11 = point(wrist, fan[bay], fan[bay + 1], t1, u1, scallop);
                int c = (bay + radial + angle) % 4 == 0 ? darken(wing.color(), .77F) : wing.color();
                triangle(stack, consumer, p00, p10, p11, root, c, light, overlay);
                triangle(stack, consumer, p00, p11, p01, root, darken(c, .9F), light, overlay);
            }
    }

    private static int darken(int argb, float amount) {
        return 0xFF000000 | (((int)(((argb >> 16) & 255) * amount)) << 16)
            | (((int)(((argb >> 8) & 255) * amount)) << 8)
            | (int)((argb & 255) * amount);
    }

    private static void triangle(PoseStack stack, VertexConsumer consumer, float[] a, float[] b,
                                 float[] c, float[] root, int color, int light, int overlay) {
        float[] u = {b[0] - a[0], b[1] - a[1], b[2] - a[2]};
        float[] v = {c[0] - a[0], c[1] - a[1], c[2] - a[2]};
        float nx = u[1] * v[2] - u[2] * v[1];
        float ny = u[2] * v[0] - u[0] * v[2];
        float nz = u[0] * v[1] - u[1] * v[0];
        float length = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (length < .0001F) return;
        nx /= length; ny /= length; nz /= length;
        vertex(stack, consumer, a, root, color, light, overlay, nx, ny, nz, 0, 0);
        vertex(stack, consumer, b, root, color, light, overlay, nx, ny, nz, 0, 1);
        vertex(stack, consumer, c, root, color, light, overlay, nx, ny, nz, 1, 1);
        vertex(stack, consumer, c, root, color, light, overlay, nx, ny, nz, 1, 1);
        // Facing both sides keeps the membranes visible while circling the boss.
        vertex(stack, consumer, c, root, color, light, overlay, -nx, -ny, -nz, 1, 1);
        vertex(stack, consumer, b, root, color, light, overlay, -nx, -ny, -nz, 0, 1);
        vertex(stack, consumer, a, root, color, light, overlay, -nx, -ny, -nz, 0, 0);
        vertex(stack, consumer, a, root, color, light, overlay, -nx, -ny, -nz, 0, 0);
    }

    private static void vertex(PoseStack stack, VertexConsumer out, float[] p, float[] root,
                               int color, int light, int overlay, float nx, float ny, float nz,
                               float u, float v) {
        out.addVertex(stack.last(), (p[0] - root[0]) / 16F, (p[1] - root[1]) / 16F,
                (p[2] - root[2]) / 16F)
            .setColor(color).setUv((4F + u * 12F) / 512F, (132F + v * 12F) / 512F)
            .setOverlay(overlay).setLight(light).setNormal(stack.last(), nx, ny, nz);
    }
}
