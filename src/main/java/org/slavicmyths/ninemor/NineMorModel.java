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
    private static final float[][] HEAD_POS=NineMorRig.HEAD_POS;
    private final ModelPart root;
    private final ModelPart body;
    private final ModelPart dorsalPlates;
    private final ModelPart spineSpurs;
    private final ModelPart lateralScales;
    private final ModelPart bellyScutes;
    private final ModelPart chestGuard;
    private final ModelPart[] necks = new ModelPart[9];
    private final ModelPart[] neckMids = new ModelPart[9];
    private final ModelPart[] neckTips = new ModelPart[9];
    private final ModelPart[] heads = new ModelPart[9];
    public ModelPart trophyHead(int index) { return heads[index]; }
    public ModelPart trophyNeck(int index) { return necks[index]; }
    public ModelPart trophyBody() { return body; }
    public void poseTrophy(float time) {
        root.getAllParts().forEach(ModelPart::resetPose);
        for (int i = 0; i < 9; i++) {
            float phase = time * (.037F + i * .002F) + i * .93F;
            necks[i].yRot = Mth.sin(phase) * .10F;
            necks[i].xRot = Mth.cos(phase * .79F) * .035F;
            neckMids[i].yRot = Mth.cos(phase * .83F) * .13F;
            neckMids[i].xRot = Mth.sin(phase * .71F) * .045F;
            neckTips[i].yRot = Mth.sin(phase * 1.13F) * .11F;
            heads[i].yRot = Mth.sin(phase * .77F) * .15F;
            heads[i].xRot = Mth.cos(phase * .91F) * .055F;
            jaws[i].xRot = .05F + Math.max(0F, Mth.sin(time * .085F + i * 1.37F)) * .38F;
        }
    }
    public void renderTrophy(PoseStack stack, VertexConsumer consumer, int light, int overlay) {
        stack.pushPose();
        body.translateAndRotate(stack);
        body.getChild("shell").render(stack, consumer, light, overlay, 0xFF474853);
        dorsalPlates.render(stack, consumer, light, overlay, 0xFF79727C);
        spineSpurs.render(stack, consumer, light, overlay, 0xFF756968);
        chestGuard.render(stack, consumer, light, overlay, 0xFF76616B);
        for (int i = 0; i < 9; i++) {
            stack.pushPose();
            heads[i].visible = false;
            necks[i].render(stack, consumer, light, overlay, 0xFF575761);
            heads[i].visible = true;
            necks[i].translateAndRotate(stack);
            neckMids[i].translateAndRotate(stack);
            neckTips[i].translateAndRotate(stack);
            heads[i].render(stack, consumer, light, overlay, hideColor(i));
            stack.pushPose();
            heads[i].translateAndRotate(stack);
            accents[i].render(stack, consumer, light, overlay, 0xFF8F8175);
            eyeSockets[i].render(stack, consumer, light, overlay, 0xFF18171F);
            cheekPlates[i].render(stack, consumer, light, overlay, 0xFF665A61);
            stack.popPose();
            stack.popPose();
        }
        stack.popPose();
    }

    private final ModelPart[] jaws = new ModelPart[9];
    private final ModelPart[] accents = new ModelPart[9];
    private final ModelPart[] eyeSockets = new ModelPart[9];
    private final ModelPart[] eyeIrises = new ModelPart[9];
    private final ModelPart[] eyePupils = new ModelPart[9];
    private final ModelPart[] cheekPlates = new ModelPart[9];
    private final ModelPart[] wings = new ModelPart[6];
    private final ModelPart[] wingElbows=new ModelPart[6],wingWrists=new ModelPart[6];
    private final ModelPart[] legs = new ModelPart[4];
    private final ModelPart[] tails = new ModelPart[3];
    private final WingData[] wingData = new WingData[6];

    private record WingData(float[] root, float[] elbow, float[] wrist, float[][] points, int color) {}

    public NineMorModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition base = mesh.getRoot();
        PartDefinition torso = base.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        buildTorso(torso);
        buildArmor(torso);
        buildLegs(torso);
        buildHeads(torso);
        buildTails(torso);
        buildWings(torso);
        root = LayerDefinition.create(mesh, 512, 512).bakeRoot();
        body = root.getChild("body");
        dorsalPlates = body.getChild("dorsal_plates");
        spineSpurs = body.getChild("spine_spurs");
        lateralScales = body.getChild("lateral_scales");
        bellyScutes = body.getChild("belly_scutes");
        chestGuard = body.getChild("chest_guard");
        for (int i = 0; i < 9; i++) {
            necks[i] = body.getChild("neck_" + i);
            neckMids[i] = necks[i].getChild("neck_mid_" + i);
            neckTips[i] = neckMids[i].getChild("neck_tip_" + i);
            heads[i] = neckTips[i].getChild("head_" + i);
            jaws[i] = heads[i].getChild("jaw_" + i);
            accents[i] = heads[i].getChild("accent_" + i);
            eyeSockets[i] = heads[i].getChild("eye_socket_" + i);
            eyeIrises[i] = heads[i].getChild("eye_iris_" + i);
            eyePupils[i] = heads[i].getChild("eye_pupil_" + i);
            cheekPlates[i] = heads[i].getChild("cheek_plates_" + i);
        }
        for (int i = 0; i < 6; i++){wings[i]=body.getChild("wing_"+i);wingElbows[i]=wings[i].getChild("elbow");wingWrists[i]=wingElbows[i].getChild("wrist");}
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

    /**
     * Standalone armor layers use the actual elliptical hull profile.
     * Plates sit just OUTSIDE the shell rather than being buried in the
     * uniform voxel mass; each layer has its own material color.
     */
    private static void buildArmor(PartDefinition body) {
        CubeListBuilder dorsal = CubeListBuilder.create();
        CubeListBuilder spurs = CubeListBuilder.create();
        CubeListBuilder sides = CubeListBuilder.create();
        CubeListBuilder belly = CubeListBuilder.create();
        CubeListBuilder chest = CubeListBuilder.create();

        // Nine staggered transverse carapace sections, with a stone ridge
        // and subsidiary spikes that follow the arch of the animal's back.
        for (int row = 0; row < 11; row++) {
            float longitudinal = -10.8F + row * 2.43F;
            float profile = Mth.sqrt(Math.max(.14F,
                1F - Mth.square((longitudinal - 1.5F) / 17.8F)));
            for (int lane = -1; lane <= 1; lane++) {
                float x = lane * 4.55F * profile;
                float cross = Math.max(.08F, 1F - Mth.square(x / (8F * profile)));
                float surface = 14.1F + 7.15F * profile * Mth.sqrt(cross);
                float segmentWidth = lane == 0 ? 2.55F : 2.20F * profile;
                float rise = lane == 0 ? 1.2F : .67F;
                dorsal.texOffs(STONE_U, STONE_V).addBox(
                    x - segmentWidth / 2F, -surface - rise, longitudinal - 1.1F,
                    segmentWidth, rise + .35F, 2.08F);
                if (lane == 0) {
                    float knob = 1.45F + (row % 3) * .35F;
                    spurs.texOffs(BONES_U, BONES_V).addBox(
                        x - .73F, -surface - rise - knob, longitudinal - .58F,
                        1.46F, knob + .48F, 1.15F);
                    spurs.texOffs(STONE_U, STONE_V).addBox(
                        x - .38F, -surface - rise - knob - 1.25F, longitudinal - .27F,
                        .76F, 1.4F, .57F);
                } else if (row % 2 == 0) {
                    spurs.texOffs(STONE_U, STONE_V).addBox(
                        x - .42F, -surface - 1.75F, longitudinal - .42F,
                        .84F, 1.5F, .84F);
                }
            }
        }

        // A row of clearly overlapping side scutes down each flank.
        for (int segment = 0; segment < 14; segment++) {
            float z = -11.15F + 1.94F * segment;
            float profile = Mth.sqrt(Math.max(.12F,
                1F - Mth.square((z - 1.5F) / 17.8F)));
            float flank = 8F * profile;
            float height = 14.1F + 1.20F * profile;
            for (int side : new int[]{-1, 1}) {
                float x = side * (flank - .45F);
                sides.texOffs(STONE_U, STONE_V).addBox(
                    x - .8F, -height - 2.1F, z - .83F,
                    1.6F, 3.55F, 1.67F);
                sides.texOffs(BONES_U, BONES_V).addBox(
                    x - .55F, -height - 1.92F, z - .56F,
                    1.1F, .45F, 1.14F);
                if (segment % 3 != 1) {
                    float tip = side * (flank + 1.35F);
                    spurs.texOffs(STONE_U, STONE_V).addBox(
                        tip - .45F, -height - .22F, z - .43F,
                        .9F, 1.05F, .86F);
                }
            }
        }

        // Pale scutes on the underside and breast; breaking the silhouette
        // into overlapping scales rather than a single rectangular column.
        for (int n = 0; n < 12; n++) {
            float z = -10.7F + n * 2.15F;
            float profile = Mth.sqrt(Math.max(.16F,
                1F - Mth.square((z - 1.5F) / 17.8F)));
            float w = 4.6F * profile;
            float underside = 14.1F - 7.15F * profile;
            belly.texOffs(BELLY_U, BELLY_V).addBox(
                -w, -underside - .18F, z - .96F,
                2F * w, 1.25F, 1.92F);
            belly.texOffs(BONES_U, BONES_V).addBox(
                -w * .83F, -underside + .81F, z - .42F,
                1.66F * w, .24F, .65F);
        }

        // Massive overlapping sternum / shoulder mantle where the nine
        // neck roots attach. Jagged plates remain separate from the necks.
        for (int side : new int[]{-1, 1}) {
            for (int layer = 0; layer < 5; layer++) {
                float x = side * (1.0F + layer * .72F);
                float y = -18.9F + layer * .38F;
                chest.texOffs(STONE_U, STONE_V).addBox(
                    x - 1.0F, y - 2.05F, -12.3F + layer * .25F,
                    2F, 2.85F - layer * .18F, 2.5F);
            }
            chest.texOffs(BONES_U, BONES_V).addBox(
                side * 5.2F - 1.2F, -21.7F, -11.1F,
                2.4F, 2.1F, 3.7F);
        }
        chest.texOffs(BONES_U, BONES_V).addBox(-.85F, -20.7F, -13.8F, 1.7F, 2.4F, 2.65F);
        chest.texOffs(STONE_U, STONE_V).addBox(-1.4F, -17.9F, -14.15F, 2.8F, 1.45F, 1.35F);

        body.addOrReplaceChild("dorsal_plates", dorsal, PartPose.ZERO);
        body.addOrReplaceChild("spine_spurs", spurs, PartPose.ZERO);
        body.addOrReplaceChild("lateral_scales", sides, PartPose.ZERO);
        body.addOrReplaceChild("belly_scutes", belly, PartPose.ZERO);
        body.addOrReplaceChild("chest_guard", chest, PartPose.ZERO);
    }

    private static void buildLegs(PartDefinition body) {
        int id = 0;
        for (int front = 0; front < 2; front++) for (int side : new int[]{-1, 1}) {
            float x = side * (front == 0 ? 6.8F : 6.3F);
            float y = front == 0 ? -7.9F : 10.2F;
            CubeListBuilder leg = CubeListBuilder.create()
                .texOffs(STONE_U, STONE_V).addBox(-1.75F, 0F, -2.2F, 3.5F, 4.4F, 4.4F)
                .texOffs(SLATE_U, SLATE_V).addBox(-1.45F + side * .3F, 3.7F, -1.5F, 2.7F, 4F, 3.1F)
                .texOffs(SLATE_U, SLATE_V).addBox(-1.6F + side * .4F, 7.7F, -2.6F, 3.15F, 3.9F, 4.4F);
            for (int toe = -1; toe <= 1; toe++) {
                leg.texOffs(STONE_U, STONE_V).addBox(toe * 1.15F - .47F, 10.5F, -4.1F, .94F, 1.35F, 1.75F);
                leg.texOffs(BONES_U, BONES_V).addBox(toe * 1.15F - .30F, 11.1F, -4.65F, .6F, .65F, .7F);
            }
            body.addOrReplaceChild("leg_" + id++, leg, PartPose.offset(x, -12.3F, y));
        }
    }

    private static float[] neckPoint(float x0, float y0, float z0,
                                     float hx, float hy, float hz, int head, float t) {
        return NineMorRig.neckPoint(head,t);
    }

    private static void buildHeads(PartDefinition body) {
        for (int i = 0; i < 9; i++) {
            float hx = HEAD_POS[i][0], hy = HEAD_POS[i][1], hz = HEAD_POS[i][2];
            int row = i / 3, lane = i % 3 - 1;
            float x0 = lane * 2.85F, y0 = -10.7F + row * .65F, z0 = 18F + row * .4F;
            float[] pivotA = neckPoint(x0, y0, z0, hx, hy, hz, i, 0F);
            float[] pivotB = neckPoint(x0, y0, z0, hx, hy, hz, i, 6F / 17F);
            float[] pivotC = neckPoint(x0, y0, z0, hx, hy, hz, i, 12F / 17F);
            CubeListBuilder a = CubeListBuilder.create(), b = CubeListBuilder.create(),
                            c = CubeListBuilder.create();
            for (int j = 0; j < 17; j++) {
                float t = (j + .5F) / 17F;
                float[] p = neckPoint(x0, y0, z0, hx, hy, hz, i, t);
                float[] pivot = j < 6 ? pivotA : j < 12 ? pivotB : pivotC;
                CubeListBuilder part = j < 6 ? a : j < 12 ? b : c;
                float width = 1.90F - .45F * t;
                part.texOffs(j % 4 == 0 ? STONE_U : SLATE_U,
                             j % 4 == 0 ? STONE_V : SLATE_V)
                    .addBox(p[0] - pivot[0] - width, p[1] - pivot[1] - width,
                        p[2] - pivot[2] - 1.1F, width * 2F, width * 2F, 2.2F);
                if (j == 5 || j == 9 || j == 13)
                    part.texOffs(BONES_U, BONES_V).addBox(
                        p[0] - pivot[0] - .38F, p[1] - pivot[1] - 1.7F,
                        p[2] - pivot[2] - .45F, .76F, 1.1F, .9F);
            }
            b.texOffs(SLATE_U,SLATE_V).addBox(-1.65F,-1.65F,-1.65F,3.3F,3.3F,3.3F);
            c.texOffs(SLATE_U,SLATE_V).addBox(-1.5F,-1.5F,-1.5F,3F,3F,3F);
            PartDefinition first = body.addOrReplaceChild("neck_" + i, a,
                PartPose.offset(pivotA[0], pivotA[1], pivotA[2]));
            PartDefinition middle = first.addOrReplaceChild("neck_mid_" + i, b,
                PartPose.offset(pivotB[0] - pivotA[0], pivotB[1] - pivotA[1], pivotB[2] - pivotA[2]));
            PartDefinition tip = middle.addOrReplaceChild("neck_tip_" + i, c,
                PartPose.offset(pivotC[0] - pivotB[0], pivotC[1] - pivotB[1], pivotC[2] - pivotB[2]));

            // Long, low predator skull with recessed eyes and swept-back horns.
            CubeListBuilder skull = CubeListBuilder.create()
                .texOffs(SLATE_U,SLATE_V).addBox(-2.05F,-2.35F,-1.9F,4.10F,2.65F,3.65F)
                .texOffs(STONE_U,STONE_V).addBox(-1.65F,-1.55F,-4.2F,3.3F,1.75F,3.2F)
                .texOffs(SLATE_U,SLATE_V).addBox(-1.3F,-1.0F,-6.5F,2.6F,1.15F,3.1F)
                .texOffs(STONE_U,STONE_V).addBox(-.8F,-1.55F,-5.6F,1.6F,.65F,3.7F)
                .texOffs(SLATE_U,SLATE_V).addBox(-1.6F,-1.6F,-.4F,3.2F,3.2F,2F);
            PartDefinition head=tip.addOrReplaceChild("head_"+i,skull,PartPose.offset(hx-pivotC[0],-hz-pivotC[1],hy-pivotC[2]));
            CubeListBuilder jaw=CubeListBuilder.create()
                .texOffs(SLATE_U,SLATE_V).addBox(-1.24F,-.2F,-6.5F,2.48F,.65F,5.5F);
            for(int tooth=0;tooth<5;tooth++)for(int side:new int[]{-1,1})
                jaw.texOffs(BONES_U,BONES_V).addBox(side<0?-1.16F:.94F,-.9F,-5.9F+tooth*.9F,.22F,.85F,.36F);
            head.addOrReplaceChild("jaw_"+i,jaw,PartPose.offset(0,.45F,0));
            CubeListBuilder accent=CubeListBuilder.create();
            for(int side:new int[]{-1,1}){
                float x=side<0?-2.25F:1.12F;
                accent.texOffs(STONE_U,STONE_V).addBox(x,-2.6F,-2.65F,1.13F,.6F,2.5F);
                // The horn grows out of the rear skull and bends backward, rather than resembling ears.
                for(int segment=0;segment<4;segment++){
                    float width=1F-segment*.18F;
                    accent.texOffs(BONES_U,BONES_V).addBox(side*(1.65F+segment*.35F)-width/2,-2.7F-segment*.55F,.2F+segment*.9F,width,1F,1.5F);
                }
            }
            accent.texOffs(STONE_U,STONE_V).addBox(-.45F,-2.8F,-1.3F,.9F,.6F,3.3F);
            head.addOrReplaceChild("accent_"+i,accent,PartPose.ZERO);
            // Every head has independently renderable black sockets, emissive irises
            // and raised dark pupils. Forward and lateral planes stay readable while
            // nine articulated necks turn around the player.
            CubeListBuilder sockets = CubeListBuilder.create();
            CubeListBuilder irises = CubeListBuilder.create();
            CubeListBuilder pupils = CubeListBuilder.create();
            CubeListBuilder cheeks = CubeListBuilder.create();
            for (int side : new int[]{-1, 1}) {
                float faceX = side < 0 ? -1.55F : .55F;
                float irisX = side < 0 ? -1.30F : .80F;
                float pupilX = side < 0 ? -1.07F : 1.03F;

                sockets.texOffs(SLATE_U, SLATE_V).addBox(faceX, -2.15F, -1.94F, 1.0F, .42F, .28F);
                irises.texOffs(SLATE_U, SLATE_V).addBox(irisX, -1.94F, -2.08F, .52F, .20F, .20F);
                pupils.texOffs(SLATE_U, SLATE_V).addBox(pupilX, -1.93F, -2.19F, .12F, .20F, .11F);

                float sideX = side < 0 ? -2.18F : 1.96F;
                float sideIrisX = side < 0 ? -2.35F : 2.17F;
                float sidePupilX = side < 0 ? -2.43F : 2.35F;
                sockets.texOffs(SLATE_U, SLATE_V).addBox(sideX, -2.1F, -.97F, .22F, .40F, 1.0F);
                irises.texOffs(SLATE_U, SLATE_V).addBox(sideIrisX, -1.90F, -.78F, .18F, .18F, .56F);
                pupils.texOffs(SLATE_U, SLATE_V).addBox(sidePupilX, -1.88F, -.56F, .08F, .18F, .17F);
                cheeks.texOffs(STONE_U, STONE_V).addBox(side < 0 ? -2.11F : 1.64F,
                    -.84F, -2.44F, .47F, .77F, 2.15F);
                cheeks.texOffs(BONES_U, BONES_V).addBox(faceX - .08F,
                    -2.36F, -2.04F, 1.16F, .29F, .56F);
                // Two dark nostril cuts, distinctly forward of the eye sockets.
                pupils.texOffs(SLATE_U, SLATE_V).addBox(
                    side < 0 ? -.88F : .51F, -.55F, -6.55F, .36F, .22F, .18F);
            }
            head.addOrReplaceChild("eye_socket_" + i, sockets, PartPose.ZERO);
            head.addOrReplaceChild("eye_iris_" + i, irises, PartPose.ZERO);
            head.addOrReplaceChild("eye_pupil_" + i, pupils, PartPose.ZERO);
            head.addOrReplaceChild("cheek_plates_" + i, cheeks, PartPose.ZERO);
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
                    .addBox(x - lane*2.7F - radius * .825F, -z +13.6F - radius * .7F, y -13F - .89F,
                            radius * 1.65F, radius * 1.4F, 1.78F);
                if (n % 3 == 1 && t < .88F)
                    tail.texOffs(BONES_U, BONES_V).addBox(x - lane*2.7F - .34F, -z +13.6F - radius, y -13F - .36F,
                        .68F, 1.25F * (1 - t) + .6F, .72F);
            }
            body.addOrReplaceChild("tail_" + i, tail, PartPose.offset(lane*2.7F,-13.6F,13F));
        }
    }

    private void buildWings(PartDefinition body) {
        for(int pair=0;pair<3;pair++)for(int side:new int[]{-1,1}){
            // One dominant flight pair; the two smaller pairs support the rear body.
            float scale=pair==0?1F:pair==1?.68F:.46F;
            float height=-17F+pair*.9F,depth=-5F+pair*7F;
            float[] rootPos={side*6.2F,height,depth};
            float[] elbow=wingPoint(rootPos,side,scale,7F,-3F,3F);
            float[] wrist=wingPoint(rootPos,side,scale,19F,-5F,-2F);
            float[][] fan={
                wingPoint(rootPos,side,scale,43F,-8F,-14F),
                wingPoint(rootPos,side,scale,37F,-4F,8F),
                wingPoint(rootPos,side,scale,25F,1F,23F),
                wingPoint(rootPos,side,scale,12F,3F,30F),
                wingPoint(rootPos,side,scale,.4F,3F,7F)
            };
            int index=pair*2+(side>0?1:0);
            wingData[index]=new WingData(rootPos,elbow,wrist,fan,pair==0?0xFF4D3841:pair==1?0xFF46343C:0xFF403139);
            PartDefinition shoulder=body.addOrReplaceChild("wing_"+index,CubeListBuilder.create().texOffs(SLATE_U,SLATE_V)
                .addBox(-2F*scale,-1.8F*scale,-2F*scale,4F*scale,3.6F*scale,4F*scale),PartPose.offset(rootPos[0],rootPos[1],rootPos[2]));
            beam(shoulder,0,rootPos,elbow,rootPos,2F*scale);
            PartDefinition forearm=shoulder.addOrReplaceChild("elbow",CubeListBuilder.create().texOffs(STONE_U,STONE_V)
                .addBox(-1.4F*scale,-1.4F*scale,-1.4F*scale,2.8F*scale,2.8F*scale,2.8F*scale),PartPose.offset(elbow[0]-rootPos[0],elbow[1]-rootPos[1],elbow[2]-rootPos[2]));
            beam(forearm,0,elbow,wrist,elbow,1.5F*scale);
            PartDefinition hand=forearm.addOrReplaceChild("wrist",CubeListBuilder.create().texOffs(STONE_U,STONE_V)
                .addBox(-1.1F*scale,-1.1F*scale,-1.1F*scale,2.2F*scale,2.2F*scale,2.2F*scale),PartPose.offset(wrist[0]-elbow[0],wrist[1]-elbow[1],wrist[2]-elbow[2]));
            int bone=0;
            for(int finger=0;finger<4;finger++){
                float[] bend=midpoint(wrist,fan[finger]);bend[1]-=1.1F*scale;
                bone=beam(hand,bone,wrist,bend,wrist,(.95F-finger*.09F)*scale);
                bone=beam(hand,bone,bend,fan[finger],wrist,(.58F-finger*.07F)*scale);
            }
        }
    }
    private static float[] wingPoint(float[] root,int side,float scale,float x,float y,float z){return new float[]{root[0]+side*scale*x,root[1]+scale*y,root[2]+scale*z};}
    private static float[] midpoint(float[] a, float[] b) {
        return new float[]{(a[0] + b[0]) / 2, (a[1] + b[1]) / 2, (a[2] + b[2]) / 2};
    }

    private static int beam(PartDefinition wing, int index, float[] a, float[] b, float[] root, float width) {
        float dx = b[0] - a[0], dy = b[1] - a[1], dz = b[2] - a[2];
        float length = Mth.sqrt(dx * dx + dy * dy + dz * dz);
        if (length <= .01F) return index;
        float pitch = (float)Math.atan2(dz, Mth.sqrt(dx*dx+dy*dy));
        float roll = -(float)Math.atan2(dx, dy);
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
        body.y = NineMorRig.bodyY(age,flight);
        body.xRot = NineMorRig.bodyPitch(flight);
        float pace = Mth.clamp(amount, 0F, 1F);
        float airBlend = flight == 1 ? Mth.clamp(entity.flightAge() / 18F, 0F, 1F)
            : flight == 2 ? 1F
            : flight == 3 ? 1F - Mth.clamp(entity.flightAge() / 55F, 0F, 1F) : 0F;
        for (int i = 0; i < 4; i++) {
            float phase = limb * .60F + ((i == 0 || i == 3) ? Mth.PI : 0F);
            legs[i].xRot = Mth.lerp(airBlend, Mth.cos(phase) * pace * .62F, .65F);
            legs[i].zRot = Mth.sin(phase) * pace * .055F * (1F - airBlend);
        }
        for(int i=0;i<9;i++){
            var pose=NineMorRig.pose(entity,i);
            necks[i].yRot=pose.yaw()*.16F;necks[i].xRot=pose.pitch()*.12F;
            neckMids[i].yRot=pose.yaw()*.24F;neckMids[i].xRot=pose.pitch()*.22F;
            neckTips[i].yRot=pose.yaw()*.28F;neckTips[i].xRot=pose.pitch()*.26F;
            heads[i].yRot=pose.yaw()*.32F;heads[i].xRot=pose.pitch()*.40F;
            jaws[i].xRot=pose.jaw();
            // Joint positions remain the baked positions in every attack frame.
            heads[i].visible=false;accents[i].visible=false;eyeSockets[i].visible=false;
            eyeIrises[i].visible=false;eyePupils[i].visible=false;cheekPlates[i].visible=false;
        }
        for (int i = 0; i < 3; i++) {
            tails[i].xRot = Mth.sin(age * (.055F + i * .006F) + i) * .075F;
            tails[i].yRot = Mth.sin(age * .052F + i * 1.25F) * .11F
                + ((entity.tailMask() & (1 << i)) != 0 ? Mth.sin(age * .42F) * .23F : 0F);
        }
        float spread=flight==0?0F:flight==1?Mth.clamp(entity.flightAge()/18F,0,1):flight==2?1F:1F-Mth.clamp(entity.flightAge()/32F,0,1);
        for(int i=0;i<6;i++){
            float side=i%2==0?-1F:1F;
            float pair=i/2;float flap=Mth.sin(age*.18F-pair*.35F);
            wings[i].yRot=-side*Mth.lerp(spread,.40F,.03F);
            wings[i].zRot=side*Mth.lerp(spread,.12F,flap*.40F-.06F);
            wings[i].xRot=Mth.lerp(spread,.03F,-.04F+flap*.06F);
            wingElbows[i].yRot=-side*Mth.lerp(spread,1.35F,.10F);
            wingElbows[i].zRot=side*spread*(.08F+flap*.05F);
            wingWrists[i].yRot=side*Mth.lerp(spread,.25F,0F);
            wingWrists[i].xRot=spread*flap*.06F;
            wings[i].visible=false;
        }
    }
    @Override
    public void renderToBuffer(PoseStack stack, VertexConsumer consumer, int light, int overlay, int color) {
        // Material-specific tints retain the approved graphite / slate silhouette
        // without the previous almost-black texture multiplying away every detail.
        stack.pushPose();
        body.translateAndRotate(stack);
        body.getChild("shell").render(stack, consumer, light, overlay, 0xFF474853);
        for (int i = 0; i < 4; i++) legs[i].render(stack, consumer, light, overlay, 0xFF484953);
        for (int i = 0; i < 9; i++) necks[i].render(stack, consumer, light, overlay, 0xFF575761);
        for (int i = 0; i < 3; i++) tails[i].render(stack, consumer, light, overlay, 0xFF4D4E59);
        stack.popPose();
    }

    public void renderColoredParts(PoseStack stack, VertexConsumer consumer, int light, int overlay) {
        stack.pushPose();
        body.translateAndRotate(stack);
        dorsalPlates.render(stack, consumer, light, overlay, 0xFF79727C);
        spineSpurs.render(stack, consumer, light, overlay, 0xFF756968);
        lateralScales.render(stack, consumer, light, overlay, 0xFF725762);
        bellyScutes.render(stack, consumer, light, overlay, 0xFF817278);
        chestGuard.render(stack, consumer, light, overlay, 0xFF76616B);
        for (int i = 0; i < 9; i++) {
            stack.pushPose();
            necks[i].translateAndRotate(stack);
            neckMids[i].translateAndRotate(stack);
            neckTips[i].translateAndRotate(stack);
            heads[i].visible = true;
            accents[i].visible = false;
            heads[i].render(stack, consumer, light, overlay,
                hideColor(i));
            stack.pushPose();
            heads[i].translateAndRotate(stack);
            accents[i].visible = true;
            accents[i].render(stack, consumer, light, overlay, 0xFF8F8175);
            accents[i].visible = false;
            eyeSockets[i].visible = true;
            eyeSockets[i].render(stack, consumer, light, overlay, 0xFF18171F);
            eyeSockets[i].visible = false;
            eyePupils[i].visible = true;
            eyePupils[i].render(stack, consumer, light, overlay, 0xFF141016);
            eyePupils[i].visible = false;
            cheekPlates[i].visible = true;
            cheekPlates[i].render(stack, consumer, light, overlay, 0xFF665A61);
            cheekPlates[i].visible = false;
            stack.popPose();
            heads[i].visible = false;
            stack.popPose();
        }
        for (int i = 0; i < 6; i++) {
            wings[i].visible = true;
            wings[i].render(stack, consumer, light, overlay, 0xFF60575A);
            wings[i].visible = false;
            stack.pushPose();
            wings[i].translateAndRotate(stack);
            renderMembrane(stack, consumer, light, overlay, articulatedWing(i));
            stack.popPose();
        }
        stack.popPose();
    }

    public void renderEmissiveEyes(PoseStack stack, VertexConsumer consumer) {
        stack.pushPose();
        body.translateAndRotate(stack);
        for (int i = 0; i < 9; i++) {
            stack.pushPose();
            necks[i].translateAndRotate(stack);
            neckMids[i].translateAndRotate(stack);
            neckTips[i].translateAndRotate(stack);
            heads[i].translateAndRotate(stack);
            eyeIrises[i].visible = true;
            eyeIrises[i].render(stack, consumer, 0xF000F0,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                brighten(NineMorSerpent.COLORS[i], .04F));
            eyeIrises[i].visible = false;
            stack.popPose();
        }
        stack.popPose();
    }

    private WingData articulatedWing(int index){
        WingData source=wingData[index];float[] root=source.root(),elbow=source.elbow(),wrist=source.wrist();
        ModelPart a=wingElbows[index],b=wingWrists[index];
        var transform=new org.joml.Matrix4f().translate(elbow[0]-root[0],elbow[1]-root[1],elbow[2]-root[2]).rotateZYX(a.zRot,a.yRot,a.xRot)
            .translate(wrist[0]-elbow[0],wrist[1]-elbow[1],wrist[2]-elbow[2]).rotateZYX(b.zRot,b.yRot,b.xRot);
        float[][] points=new float[5][3];
        for(int j=0;j<4;j++){float[] p=source.points()[j];var v=transform.transformPosition(new org.joml.Vector3f(p[0]-wrist[0],p[1]-wrist[1],p[2]-wrist[2]));points[j]=new float[]{v.x,v.y,v.z};}
        float[] anchor=source.points()[4];points[4]=new float[]{anchor[0]-root[0],anchor[1]-root[1],anchor[2]-root[2]};
        var hand=transform.transformPosition(new org.joml.Vector3f());
        return new WingData(new float[]{0,0,0},new float[]{elbow[0]-root[0],elbow[1]-root[1],elbow[2]-root[2]},new float[]{hand.x,hand.y,hand.z},points,source.color());
    }    private static int hideColor(int head){
        int hue=NineMorSerpent.COLORS[head];float tint=.12F;
        int r=(int)(65*(1-tint)+((hue>>16)&255)*tint);
        int g=(int)(61*(1-tint)+((hue>>8)&255)*tint);
        int b=(int)(67*(1-tint)+(hue&255)*tint);
        return 0xFF000000|r<<16|g<<8|b;
    }    private static int brighten(int rgb, float whiteMix) {
        int r = (int)(((rgb >> 16) & 255) * (1F - whiteMix) + 255F * whiteMix);
        int g = (int)(((rgb >> 8) & 255) * (1F - whiteMix) + 255F * whiteMix);
        int b = (int)((rgb & 255) * (1F - whiteMix) + 255F * whiteMix);
        return 0xFF000000 | r << 16 | g << 8 | b;
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
        triangle(stack,consumer,root,wing.elbow(),wrist,root,wing.color(),light,overlay);
        triangle(stack,consumer,root,wrist,fan[4],root,wing.color(),light,overlay);
        for (int bay = 0; bay < 4; bay++) for (int radial = 0; radial < 4; radial++)
            for (int angle = 0; angle < 5; angle++) {
                float t0 = radial / 4F, t1 = (radial + 1) / 4F;
                float u0 = angle / 5F, u1 = (angle + 1) / 5F;
                float scallop = bay < 3 ? .14F : .08F;
                float[] p00 = point(wrist, fan[bay], fan[bay + 1], t0, u0, scallop);
                float[] p01 = point(wrist, fan[bay], fan[bay + 1], t0, u1, scallop);
                float[] p10 = point(wrist, fan[bay], fan[bay + 1], t1, u0, scallop);
                float[] p11 = point(wrist, fan[bay], fan[bay + 1], t1, u1, scallop);
                int c = darken(wing.color(),.9F+.1F*t0);
                triangle(stack, consumer, p00, p10, p11, root, c, light, overlay);
                triangle(stack, consumer, p00, p11, p01, root, c, light, overlay);
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
