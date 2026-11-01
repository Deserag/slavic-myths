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
 * Voxel proportions are based on the ORIGINAL approved appearance PNG:
 * MOROVOY_VORON_APPROVED_APPEARANCE.png (sha256 6b3b39da1c47e513...).
 * The long LOWER main pair is wider than the raised UPPER plague-damaged pair.
 * Runtime comparison with the approved PNG remains required; source edits are not approval.
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
        PartDefinition bird = base.addOrReplaceChild("bird", CubeListBuilder.create().texOffs(0,0).addBox(-13,-28,-25,26,36,47).texOffs(0,90).addBox(-15,-22,-24,30,23,27).texOffs(60,90).addBox(-11,-15,10,22,23,23).texOffs(108,0).addBox(-12,-30,-18,24,7,19), PartPose.offset(0,-19,0));
        // Overlapping body volumes, shoulder armor of ragged feathers, and exposed Mor lesions.
        for (int side : new int[]{-1,1}) {
            float x = side < 0 ? -19 : 13;
            add(bird,"shoulder_"+side,135,0,x,-27,-20,6,13,25);
            add(bird,"feather_ridge_"+side,156,0,side < 0 ? -17 : 11,-30,4,6,10,15);
            add(bird,"breast_vein_"+side,180,0,side < 0 ? -15.4F : 14.2F,-14,-21,1.2F,2,23);
            add(bird,"hip_vein_"+side,180,8,side < 0 ? -11.8F : 10.6F,-9,17,1.2F,2,15);
        }
        add(bird,"throat_lesion",180,0,-2,-31,-23,4,1.7F,16);
        add(bird,"spine_feathers",130,40,-10,-32,6,20,4,24);
        // Pale, uneven shoulder/back plague growths seen clearly on rear and 3/4 PNG.
        for (int i = 0; i < 7; i++) {
            float x = -10 + i * 3.3F;
            float z = (i % 2 == 0) ? 1 + (i / 2) * 4 : -3 + (i / 2) * 4;
            float h = 5 + ((i * 5) % 6);
            add(bird,"bone_growth_"+i,64,194,x,-33-h,z,3.2F,h,3.6F);
            add(bird,"bone_growth_tip_"+i,100,194,x+0.8F,-35-h,z+0.5F,1.5F,3,2);
        }
        // Layered transition from throat to tapered breast; avoid one blank cuboid.
        for (int i = 0; i < 5; i++) {
            float width = 18 - i * 2.4F;
            add(bird,"breast_plume_"+i,(i%2==0)?130:156,40,-width/2,-8+i*3,-24+i*1.6F,width,5,7+i);
        }
        // Distinct stepped fragments around the back of the neck.
        for (int i = 0; i < 4; i++) {
            add(bird,"neck_plume_left_"+i,156,0,-13-i*.6F,-29+i*3,-25-i,3,8,9+i);
            add(bird,"neck_plume_right_"+i,156,0,10+i*.6F,-29+i*3,-25-i,3,8,9+i);
        }
        PartDefinition head = bird.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,150).addBox(-11,-11,-12,22,21,23).texOffs(94,151).addBox(-10,-13,-6,20,4,18).texOffs(166,42).addBox(-9,-14,2,18,6,11),PartPose.offset(0,-19,-17));
        // Wedge-like hooked upper beak and articulated lower mandible.
        add(head,"upper_beak_base",0,199,-8,0,-25,16,9,16);
        add(head,"upper_beak_bridge",64,194,-6.5F,2,-31,13,7,11);
        add(head,"upper_beak_hook",64,194,-4.8F,5,-34,9.6F,6.5F,8);
        add(head,"beak_tip",100,194,-2.6F,8,-35,5.2F,7.5F,5);
        PartDefinition jaw = head.addOrReplaceChild("jaw",box(130,193,-6,10,-25,12,4,17),PartPose.offset(0,0,0));
        add(jaw,"lower_mandible_tip",100,194,-3.7F,10,-32,7.4F,3.2F,8);
        add(jaw,"tongue",156,194,-3,11,-25,6,1.5F,13);
        // Visible long plague saliva strings dangling from the open beak.
        add(jaw,"mor_drool_left",180,0,-6.5F,13,-19,1.4F,15,1.4F);
        add(jaw,"mor_drool_right",180,0,4.4F,14,-25,1.5F,9,1.3F);
        add(jaw,"mor_drool_center",180,0,-1.2F,14,-28,1.6F,18,1.4F);
        add(head,"eye_left",185,38,9.2F,-4,-8,1.5F,4,5);
        add(head,"eye_right",185,38,-10.7F,-4,-8,1.5F,4,5);
        add(head,"eye_glow_left",185,38,10.5F,-2,-6,1,2,2);
        add(head,"eye_glow_right",185,38,-11.5F,-2,-6,1,2,2);
        for(int i=0;i<5;i++) {
            add(head,"crown_"+i,176,62,-8+i*3.7F,-15-(i%3),-2,2.8F,6,12);
            add(head,"crown_rear_"+i,176,62,-7.5F+i*3.6F,-11-(i%2)*2,8,2.5F,5.5F,9+(i%2)*4);
        }
        for (int side : new int[]{-1,1}) for (int i=0;i<3;i++) {
            float x = side < 0 ? -12.4F : 10.1F;
            add(head,"cheek_plume_"+side+"_"+i,176,62,x,-3+i*4,4+i*2,2.3F,5.2F,12-i*2);
        }
        // "primary" = main LOWER long pair. "secondary" = smaller mutated UPPER V.
        // Keep four distinct wing bones; do not mirror plume damage identically.
        for(int side : new int[]{-1,1}) {
            String suffix = side < 0 ? "left" : "right";
            PartDefinition primary = bird.addOrReplaceChild("primary_"+suffix, box(0,52,side < 0 ? -34 : 0,-4,-12,34,7,24),PartPose.offset(side*15,-17,-8));
            for(int i=0;i<9;i++) {
                float start = 20+i*9.5F;
                float outer = side < 0 ? -start-12 : start;
                float length = 29+i*2.2F+(i%3)*2+(side<0 ? 0 : (i%2)*3);
                add(primary,"main_flight_"+i,(i%2==0)?0:98,40,outer,-2,-11-i*.23F,12,3.3F,length);
                add(primary,"main_cover_"+i,130,40,outer+1,-3,-15-i*.13F,10,3,19+i*.8F);
                if(i>1 && i<8 && (i+side)%3!=0) {
                    float damage = 4 + (i%3)*4 + (side<0 ? 1 : 0);
                    add(primary,"torn_plume_"+i,197,115,outer+3,1,-10+length-4,4,damage,3.6F);
                }
                if(i==1 || i==4 || (i==6 && side<0)) {
                    add(primary,"mor_branch_core_"+i,174,111,outer+3,-3.8F,-3,2,1.6F,14);
                    add(primary,"mor_branch_side_"+i,180,0,outer+8,-3.6F,2,2,1.2F,7);
                }
            }
            PartDefinition secondary = bird.addOrReplaceChild("secondary_"+suffix, box(0,110,side < 0 ? -23 : 0,-4,-9,23,7,18),PartPose.offset(side*12,-32,3));
            for(int i=0;i<6;i++) {
                float start = 15+i*7.1F;
                float outer = side < 0 ? -start-10 : start;
                float length = 24+i*1.7F+(i%3)*2+(side>0 ? 2:0);
                add(secondary,"upper_flight_"+i,50,125,outer,-2,-8-i*.37F,10,3.2F,length);
                add(secondary,"upper_cover_"+i,156,0,outer+1,-3,-12,8,3,16+i*.5F);
                if(i==2 || i==4 || (side<0 && i==5)) add(secondary,"upper_torn_"+i,197,115,outer+3,1,-8+length-3,3,5+i*2,3);
                if(i==1 || i==4) {
                    add(secondary,"upper_mor_branch_"+i,180,0,outer+3,-3.7F,-4,2,1.2F,12);
                    add(secondary,"upper_mor_branch_spur_"+i,180,8,outer+7,-3.5F,2,2,1.3F,5);
                }
            }
            // Chunky articulated-looking thighs, knee, shins, forward grip and hooked ivory tips.
            PartDefinition foot = bird.addOrReplaceChild(side<0?"left_foot":"right_foot",box(112,84,-5,-3,-5,10,14,11),PartPose.offset(side*8,7,11));
            add(foot,"knee",112,84,-5,9,-5,11,6,12);
            add(foot,"shin",146,85,-3.5F,12,-8,7,12,8);
            add(foot,"ankle",146,85,-5,22,-8,10,4,12);
            for(int toe=0;toe<3;toe++) {
                float x=-7+toe*5;
                float z=-16-(toe==1 ? 3:0);
                add(foot,"toe_"+toe,168,88,x,24,z,4.4F,3.8F,12);
                add(foot,"talon_base_"+toe,192,89,x+.4F,25,z-6,3.8F,4.6F,7);
                add(foot,"talon_hook_"+toe,64,194,x+1,29,z-8,2.8F,6,4.5F);
                add(foot,"talon_point_"+toe,100,194,x+1.5F,33,z-7,1.8F,2.5F,3.2F);
            }
            add(foot,"rear_toe",168,88,-1,24,2,4,4,11);
            add(foot,"rear_talon_hook",100,194,-.2F,28,9,2.5F,6,4);
        }
        PartDefinition tail = bird.addOrReplaceChild("tail",box(0,229,-9,-4,-1,18,6,19),PartPose.offset(0,-3,13));
        for(int i=0;i<7;i++) {
            float z=6+(i%2)*2;
            float length=11+(3-Math.abs(i-3))*2.5F+(i%3);
            add(tail,"tail_plume_"+i,(i%2==0)?48:88,227,-19+i*5,-3,z,6,2.5F,length);
            if(i==1 || i==5) add(tail,"tail_torn_"+i,197,115,-18+i*5,-1,z+length-1,3,6+i%2*2,3);
        }
        add(tail,"tail_mor_vein",180,8,-2,-5,12,4,1.4F,10);
        return LayerDefinition.create(mesh,256,256);
    }
    @Override public void setupAnim(MorovoyRaven entity,float stride,float strideAmount,float age,float yaw,float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float t = entity.animationAge(age - (int)age);
        int pose = entity.pose();
        float speed = pose == MorovoyRaven.GROUND_IDLE ? .045F : pose == MorovoyRaven.LANDING ? .16F : pose == MorovoyRaven.TAKEOFF ? .65F : pose == MorovoyRaven.DIVE ? .88F : pose == MorovoyRaven.HOVER || pose == MorovoyRaven.PROJECTILE ? .18F : .45F;
        float flap = (float)Math.sin(age * speed) * (pose == MorovoyRaven.DIVE ? .16F : .40F);
        float second = (float)Math.sin(age * speed + .7F) * (pose == MorovoyRaven.DIVE ? .10F : .18F);
        // Negative X wing must rotate with positive Z to rise (voxel model Y points down).
        leftPrimary.zRot = -.04F + flap; rightPrimary.zRot = .04F - flap;
        leftSecondary.zRot = .90F + second; rightSecondary.zRot = -.90F - second;
        leftPrimary.xRot = .06F + .08F*(float)Math.sin(age*.17F);
        rightPrimary.xRot = leftPrimary.xRot;
        leftSecondary.xRot = -.08F + .08F*(float)Math.cos(age*.2F);
        rightSecondary.xRot = leftSecondary.xRot;
        bird.zRot = entity.bank();
        bird.xRot = pose == MorovoyRaven.DIVE ? .46F : pose == MorovoyRaven.HOVER ? .09F : .02F;
        if (pose == MorovoyRaven.GROUND_IDLE) bird.xRot = .025F + (float)Math.sin(age*.07F)*.015F;
        head.yRot = (float)Math.toRadians(Math.clamp(yaw,-28,28));
        head.xRot = (float)Math.toRadians(Math.clamp(pitch,-24,24))*.6F;
        jaw.xRot = pose == MorovoyRaven.BEAK && t > 7 && t < 22 ? .47F : .12F;
        leftFoot.xRot = rightFoot.xRot = pose == MorovoyRaven.CLAWS || pose == MorovoyRaven.DIVE ? -.72F : .24F;
        tail.xRot = pose == MorovoyRaven.DIVE ? -.22F : .16F;
        if (pose == MorovoyRaven.SUMMON || pose == MorovoyRaven.WING) {
            leftPrimary.zRot = .65F; rightPrimary.zRot = -.65F;
            leftSecondary.zRot = 1.02F; rightSecondary.zRot = -1.02F;
        }
        if (pose == MorovoyRaven.WING && t > 14) {
            leftPrimary.zRot = -.40F; rightPrimary.zRot = .40F;
            leftSecondary.zRot = .62F; rightSecondary.zRot = -.62F;
        }
        if (pose == MorovoyRaven.PROJECTILE) {
            head.xRot = -.21F; leftPrimary.zRot = .27F; rightPrimary.zRot = -.27F;
            leftSecondary.zRot = .98F; rightSecondary.zRot = -.98F;
        }
        if (pose == MorovoyRaven.GROUND_IDLE || pose == MorovoyRaven.LANDING || pose == MorovoyRaven.TAKEOFF) {
            float fold = pose == MorovoyRaven.GROUND_IDLE ? 1F : pose == MorovoyRaven.LANDING ? Math.clamp(t / 36F, 0F, 1F) : 1F - Math.clamp(t / 34F, 0F, 1F);
            leftPrimary.yRot = 1.10F * fold; rightPrimary.yRot = -1.10F * fold;
            leftSecondary.yRot = .88F * fold; rightSecondary.yRot = -.88F * fold;
            leftPrimary.zRot = leftPrimary.zRot * (1F-fold) + .25F*fold;
            rightPrimary.zRot = -leftPrimary.zRot;
            leftSecondary.zRot = leftSecondary.zRot * (1F-fold) + .55F*fold;
            rightSecondary.zRot = -leftSecondary.zRot;
            leftFoot.xRot = rightFoot.xRot = pose == MorovoyRaven.TAKEOFF ? -.24F : .16F;
            head.xRot += pose == MorovoyRaven.GROUND_IDLE ? .04F*(float)Math.sin(age*.07F) : 0;
        }
        // Vanilla hurt/death lifecycle remains authoritative; these are visible recoil overlays,
        // not a substitute for an approved death animation or in-game frame-by-frame testing.
        if (entity.hurtTime > 0 && entity.deathTime == 0) {
            head.xRot -= .12F;
            leftPrimary.zRot += .13F; rightPrimary.zRot -= .13F;
            leftSecondary.zRot += .08F; rightSecondary.zRot -= .08F;
        }
        if (entity.deathTime > 0) {
            head.xRot = .48F;
            jaw.xRot = .25F;
            leftPrimary.yRot = 1.12F; rightPrimary.yRot = -1.12F;
            leftSecondary.yRot = .94F; rightSecondary.yRot = -.94F;
            leftPrimary.zRot = .2F; rightPrimary.zRot = -.2F;
            leftSecondary.zRot = .45F; rightSecondary.zRot = -.45F;
            leftFoot.xRot = rightFoot.xRot = .64F;
        }
    }
    @Override public void renderToBuffer(PoseStack matrices, VertexConsumer out, int light, int overlay, int tint) {
        root.render(matrices,out,light,overlay,tint);
    }
}
