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
import org.slavicmyths.morovoy.MorovoyRaven;

/**
 * Independent boss geometry, not a scaled NightRaven. 16 voxel units per block.
 * The beak-to-tail silhouette measures approximately 96 model units (6 blocks).
 * Primary wings span roughly 230 model units (14.4 blocks), pose-dependent.
 */
public final class MorovoyRavenModel extends EntityModel<MorovoyRaven> {
    private final ModelPart root, bird, head, jaw, leftPrimary, rightPrimary, leftSecondary, rightSecondary, leftFoot, rightFoot, tail;
    public MorovoyRavenModel() {
        root = layer().bakeRoot();
        bird = root.getChild("bird");
        head = bird.getChild("head");
        jaw = head.getChild("jaw");
        leftPrimary = bird.getChild("primary_left");
        rightPrimary = bird.getChild("primary_right");
        leftSecondary = bird.getChild("secondary_left");
        rightSecondary = bird.getChild("secondary_right");
        leftFoot = bird.getChild("left_foot");
        rightFoot = bird.getChild("right_foot");
        tail = bird.getChild("tail");
    }
    private static CubeListBuilder box(int u, int v, float x, float y, float z, float w, float h, float d) {
        return CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d);
    }
    private static void add(PartDefinition parent,String name,int u,int v,float x,float y,float z,float w,float h,float d) {
        parent.addOrReplaceChild(name, box(u,v,x,y,z,w,h,d),PartPose.ZERO);
    }
    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition base = mesh.getRoot();
        PartDefinition bird = base.addOrReplaceChild("bird", CubeListBuilder.create().texOffs(0,0).addBox(-13,-28,-25,26,36,47).texOffs(0,90).addBox(-15,-22,-24,30,23,27).texOffs(60,90).addBox(-11,-15,10,22,23,23).texOffs(108,0).addBox(-12,-30,-18,24,7,19), PartPose.offset(0,-11,0));
        // Overlapping body volumes, shoulder armor of ragged feathers, and exposed Mor lesions.
        for (int side : new int[]{-1,1}) {
            float x = side < 0 ? -19 : 13;
            add(bird,"shoulder_"+side,135,0,x,-27,-20,6,13,25);
            add(bird,"feather_ridge_"+side,156,0,side < 0 ? -17 : 11,-30,4,6,10,15);
            add(bird,"breast_vein_"+side,180,0,side < 0 ? -15.4F : 14.2F,-14,-21,1.2F,2,23);
            add(bird,"hip_vein_"+side,180,8,side < 0 ? -11.8F : 10.6F,-9,17,1.2F,2,15);
        }
        add(bird,"throat_lesion",180,0,-2,-31,-23,4,1.7F,16);
        add(bird,"spine_feathers",130,40,-10,-32,10,20,4,24);
        PartDefinition head = bird.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,150).addBox(-11,-11,-12,22,21,23).texOffs(94,151).addBox(-10,-13,-6,20,4,18).texOffs(166,42).addBox(-9,-14,2,18,6,11),PartPose.offset(0,-19,-20));
        // Wedge-like hooked upper beak and articulated lower mandible.
        add(head,"upper_beak_base",0,199,-8,0,-25,16,9,16);
        add(head,"upper_beak_hook",64,194,-6,3,-33,12,8,12);
        add(head,"beak_tip",100,194,-3,8,-35,6,8,7);
        PartDefinition jaw = head.addOrReplaceChild("jaw",box(130,193,-6,8,-25,12,4,16),PartPose.offset(0,0,0));
        add(jaw,"tongue",156,194,-3,9,-25,6,1.5F,13);
        add(head,"eye_left",185,38,9.2F,-4,-8,1.5F,4,5);
        add(head,"eye_right",185,38,-10.7F,-4,-8,1.5F,4,5);
        add(head,"eye_glow_left",185,38,10.5F,-2,-6,1,2,2);
        add(head,"eye_glow_right",185,38,-11.5F,-2,-6,1,2,2);
        for(int i=0;i<5;i++) add(head,"crown_"+i,176,62,-8+i*3.7F,-15-(i%3),-2,2.8F,6,12);
        // Four articulated wing roots: the broad primary pair is LOWER and the smaller V-shaped secondary pair is UPPER.
        for(int side : new int[]{-1,1}) {
            String suffix = side < 0 ? "left" : "right";
            PartDefinition primary = bird.addOrReplaceChild("primary_"+suffix, box(0,52,side < 0 ? -32 : 0,-4,-10,32,8,20),PartPose.offset(side*14,-13,15));
            for(int i=0;i<7;i++) {
                float start = 19+i*10;
                float outer = side < 0 ? -start-13 : start-3;
                float length = 28+i*3+(i%2)*5;
                add(primary,"flight_feather_"+i,(i%2==0)?0:98,40,outer,-2,-10-i*.3F,13,3.2F,length);
                if(i%2==0) add(primary,"lesion_"+i,174,111,outer+3,-3,-4,4,1.7F,12);
                if(i%3==1) add(primary,"broken_tip_"+i,197,115,outer+side*2,-1,22,7,2,7);
            }
            PartDefinition secondary = bird.addOrReplaceChild("secondary_"+suffix, box(0,110,side < 0 ? -22 : 0,-3,-9,22,6,20),PartPose.offset(side*12,-24,-7));
            for(int i=0;i<6;i++) {
                float start = 14+i*9;
                float outer = side < 0 ? -start-10 : start;
                add(secondary,"secondary_feather_"+i,50,125,outer,-1,-7-i*.6F,10,3,27+(i%3)*4);
                if(i%2==1) add(secondary,"secondary_vein_"+i,180,0,outer+3,-2,6,3,1,12);
            }
            PartDefinition foot = bird.addOrReplaceChild(side<0?"left_foot":"right_foot",box(112,84,-4,-2,-3,8,21,8),PartPose.offset(side*8,7,11));
            add(foot,"ankle",146,85,-5,14,-5,10,5,11);
            for(int toe=0;toe<3;toe++) {
                add(foot,"toe_"+toe,168,88,-6+toe*4,19,-14,3.5F,3.8F,14);
                add(foot,"talon_"+toe,192,89,-5.8F+toe*4,20,-19,3,4.5F,7);
            }
        }
        PartDefinition tail = bird.addOrReplaceChild("tail",box(0,229,-9,-4,-1,18,7,22),PartPose.offset(0,-3,17));
        for(int i=0;i<7;i++) add(tail,"tail_plume_"+i,(i%2==0)?48:88,227,-19+i*5,-3,8+(i%2)*2,6,2.5F,14-(Math.abs(i-3)*2));
        add(tail,"tail_mor_vein",180,8,-2,-5,15,4,2,12);
        return LayerDefinition.create(mesh,256,256);
    }
    @Override public void setupAnim(MorovoyRaven entity,float stride,float strideAmount,float age,float yaw,float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float t = entity.animationAge(age - (int)age);
        int pose = entity.pose();
        float speed = pose == MorovoyRaven.DIVE ? .88F : pose == MorovoyRaven.HOVER || pose == MorovoyRaven.PROJECTILE ? .18F : .45F;
        float flap = (float)Math.sin(age * speed) * (pose == MorovoyRaven.DIVE ? .2F : .6F);
        float second = (float)Math.sin(age * speed + 1.2F) * .5F;
        leftPrimary.zRot = -.22F - flap;
        rightPrimary.zRot = .22F + flap;
        leftSecondary.zRot = -.45F - second;
        rightSecondary.zRot = .45F + second;
        leftPrimary.xRot = .12F + .1F*(float)Math.sin(age*.17);
        rightPrimary.xRot = leftPrimary.xRot;
        leftSecondary.xRot = -.17F + .13F*(float)Math.cos(age*.2);
        rightSecondary.xRot = leftSecondary.xRot;
        bird.zRot = entity.bank();
        bird.xRot = pose == MorovoyRaven.DIVE ? -.42F : pose == MorovoyRaven.HOVER ? .09F : .02F;
        head.yRot = (float)Math.toRadians(Math.clamp(yaw,-28,28));
        head.xRot = (float)Math.toRadians(Math.clamp(pitch,-24,24))*.6F;
        jaw.xRot = pose == MorovoyRaven.BEAK && t > 7 && t < 22 ? .55F : 0;
        leftFoot.xRot = rightFoot.xRot = pose == MorovoyRaven.CLAWS || pose == MorovoyRaven.DIVE ? -.72F : .24F;
        tail.xRot = pose == MorovoyRaven.DIVE ? -.22F : .16F;
        if (pose == MorovoyRaven.SUMMON || pose == MorovoyRaven.WING) {
            leftPrimary.zRot = -1.12F; rightPrimary.zRot = 1.12F;
            leftSecondary.zRot = -.85F; rightSecondary.zRot = .85F;
        }
        if (pose == MorovoyRaven.WING && t > 14) {
            leftPrimary.zRot = -.1F; rightPrimary.zRot = .1F;
            leftSecondary.zRot = -.08F; rightSecondary.zRot = .08F;
        }
        if (pose == MorovoyRaven.PROJECTILE) {
            head.xRot = -.21F; leftPrimary.zRot = -.8F; rightPrimary.zRot = .8F;
        }
    }
    @Override public void renderToBuffer(PoseStack matrices, VertexConsumer out, int light, int overlay, int tint) {
        root.render(matrices,out,light,overlay,tint);
    }
}
