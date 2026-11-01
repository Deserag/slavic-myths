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
    private final ModelPart root, bird, head, jaw, leftPrimary, rightPrimary, leftSecondary, rightSecondary, leftPrimaryTip, rightPrimaryTip, leftSecondaryTip, rightSecondaryTip, leftFoot, rightFoot, tail;
    public MorovoyRavenModel() {
        root = layer().bakeRoot();
        bird = root.getChild("bird");
        head = bird.getChild("head");
        jaw = head.getChild("jaw");
        leftPrimary = bird.getChild("primary_left");
        rightPrimary = bird.getChild("primary_right");
        leftSecondary = bird.getChild("secondary_left");
        rightSecondary = bird.getChild("secondary_right");
        leftPrimaryTip = leftPrimary.getChild("outer");
        rightPrimaryTip = rightPrimary.getChild("outer");
        leftSecondaryTip = leftSecondary.getChild("outer");
        rightSecondaryTip = rightSecondary.getChild("outer");
        leftFoot = bird.getChild("left_foot");
        rightFoot = bird.getChild("right_foot");
        tail = bird.getChild("tail");
    }
    /** Reuses every original beak, eye, feather and jaw cube in the trophy renderer. */
    public ModelPart trophyHead() { return head; }
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
        // Unequal branching Y-shaped infection, raised from the actual chest surface.
        add(bird,"sternum_crack_trunk",180,0,-1.4F,-19,-25.9F,2.8F,13,1.1F);
        add(bird,"sternum_crack_split_left",180,8,-4.4F,-20,-25.9F,3.4F,3.2F,1.1F);
        add(bird,"sternum_crack_arm_left",180,8,-7.2F,-23,-25.8F,2.9F,4,1.1F);
        add(bird,"sternum_crack_split_right",180,8,1.1F,-17.4F,-25.9F,3.7F,2.4F,1.1F);
        add(bird,"sternum_crack_arm_right",180,8,4.2F,-22,-25.8F,2.6F,6,1.1F);
        add(bird,"sternum_crack_uneven_tip",180,0,-2.2F,-7,-25.6F,2.3F,5,1.1F);
        add(bird,"spine_feathers",130,40,-10,-32,6,20,4,24);
        // Asymmetrical exposed bone spurs on either shoulder, distinct from the spine ridge.
        add(bird,"left_shoulder_bone",64,194,-19,-40,-6,5,12,6);
        add(bird,"right_shoulder_bone",64,194,13,-36,1,4.5F,9,5);
        add(bird,"left_shoulder_bone_tip",64,194,-18,-46,-5,2.4F,7,3);
        add(bird,"right_shoulder_bone_tip",64,194,14,-42,2,2.2F,7,3);
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
        PartDefinition head = bird.addOrReplaceChild("head",CubeListBuilder.create().texOffs(0,150).addBox(-10,-10,-13,20,18,24).texOffs(94,151).addBox(-9,-12,-7,18,4,18).texOffs(166,42).addBox(-8,-13,2,16,5,12),PartPose.offset(0,-19,-17));
        // Wedge-like hooked upper beak and articulated lower mandible.
        add(head,"upper_beak_base",64,194,-8,0,-25,16,8,16);
        add(head,"upper_beak_bridge",64,194,-6.1F,1.8F,-31,12.2F,6.4F,11);
        add(head,"upper_beak_hook",64,194,-4.1F,4.5F,-36,8.2F,6.8F,7);
        add(head,"beak_tip",64,194,-2.3F,7.5F,-39,4.6F,9.6F,5);
        // The upper bill must curl below the dark lower mandible, not end as a square gray plank.
        add(head,"hook_underbite",64,194,-2.45F,11.5F,-40.8F,4.9F,4.8F,4.0F);
        add(head,"downturned_tip",64,194,-1.45F,15.5F,-41.0F,2.9F,4.5F,2.5F);
        add(head,"left_brow",0,150,-11.1F,-7.8F,-12.5F,3.3F,4,10);
        add(head,"right_brow",0,150,7.8F,-7.8F,-12.5F,3.3F,4,10);
        add(head,"nostril_left",20,160,-6.3F,3.2F,-24.6F,1.2F,2.0F,3.1F);
        add(head,"nostril_right",20,160,5.1F,3.2F,-24.6F,1.2F,2.0F,3.1F);
        PartDefinition jaw = head.addOrReplaceChild("jaw",box(130,193,-6,10,-25,12,4,17),PartPose.offset(0,0,0));
        add(jaw,"lower_mandible_tip",64,194,-3.7F,10,-32,7.4F,3.2F,8);
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
            add(head,"crown_"+i,0,150,-8+i*3.7F,-15-(i%3),-2,2.8F,6,12);
            add(head,"crown_rear_"+i,0,150,-7.5F+i*3.6F,-11-(i%2)*2,8,2.5F,5.5F,9+(i%2)*4);
        }
        for (int side : new int[]{-1,1}) for (int i=0;i<3;i++) {
            float x = side < 0 ? -12.4F : 10.1F;
            add(head,"cheek_plume_"+side+"_"+i,0,150,x,-3+i*4,4+i*2,2.3F,5.2F,12-i*2);
        }
        // "primary" = main LOWER long pair. "secondary" = smaller mutated UPPER V.
        // Keep four distinct wing bones; do not mirror plume damage identically.
        for(int side : new int[]{-1,1}) {
            String suffix = side < 0 ? "left" : "right";
            PartDefinition primary = bird.addOrReplaceChild("primary_"+suffix, box(0,52,side < 0 ? -34 : 0,-4,-12,34,7,24),PartPose.offset(side*15,-17,-8));
            // Outer feather fan is articulated and lags behind the shoulder instead of rotating as a solid slab.
            PartDefinition primaryTip = primary.addOrReplaceChild("outer", CubeListBuilder.create(), PartPose.offset(side*56,0,0));
            for(int i=0;i<9;i++) {
                PartDefinition featherBone = i >= 4 ? primaryTip : primary;
                float featherOffset = i >= 4 ? side*56 : 0;
                float start = 20+i*9.5F;
                float outer = side < 0 ? -start-12 : start;
                float length = 29+i*2.2F+(i%3)*2+(side<0 ? 0 : (i%2)*3);
                // A feather must show its broad downward face from the FRONT, not a 3px-thin horizontal strip.
                float fanDrop = 17 + i * 1.5F + (i % 3) * 3F + (side > 0 && i % 2 == 0 ? 2F : 0F);
                float tipWidth = i >= 6 ? 12 - (i - 5) * 1.6F : 12;
                add(featherBone,"main_flight_"+i,(i%2==0)?0:98,40,outer-featherOffset,-2,-11+i*.65F,tipWidth,fanDrop,7.4F);
                // Split plum-coloured lower edges into overlapping ragged fingers.
                add(featherBone,"main_plum_end_"+i,216,170,outer+1-featherOffset,fanDrop-7,-11+i*.65F,10,8+(i%3)*2,5.1F);
                if(i%2==0 || i==7) add(featherBone,"main_hanging_fray_"+i,216,170,outer+((i%3)*2)-featherOffset,fanDrop+1,-11+i*.65F,2.3F,7+(i*7)%13,2.5F);
                if(i%3==1) add(featherBone,"main_sheared_feather_"+i,130,40,outer+5-featherOffset,fanDrop-11,-11.6F+i*.65F,3,5.5F,4.1F);
                add(featherBone,"main_cover_"+i,130,40,outer+1-featherOffset,-4,-15+i*.65F,Math.min(10,tipWidth),8+(i%3)*2,7.8F);
                add(featherBone,"main_layered_covert_"+i,130,40,outer+3-featherOffset,3+(i%3),-15.4F+i*.65F,Math.min(7,tipWidth),7+(i%3)*1.7F,2.2F);
                if(i>1 && i<8 && (i+side)%3!=0) {
                    float damage = 4 + (i%3)*4 + (side<0 ? 1 : 0);
                    add(featherBone,"torn_plume_"+i,216,170,outer+3-featherOffset,fanDrop+1,-10+i*.65F,4,damage+3,3.6F);
                }
                if(i==1 || i==4 || (i==6 && side<0)) {
                    add(featherBone,"mor_branch_core_"+i,180,0,outer+3-featherOffset,4,-15.9F+i*.65F,2,fanDrop*.53F,1.6F);
                    add(featherBone,"mor_branch_side_"+i,180,8,outer+8-featherOffset,8,-15.8F+i*.65F,2,fanDrop*.27F,1.4F);
                    add(featherBone,"mor_branch_elbow_"+i,180,0,outer+4-featherOffset,10,-15.95F+i*.65F,5.3F,1.9F,1.5F);
                    if(i==4 || (i==6 && side<0)) add(featherBone,"mor_branch_broken_fork_"+i,180,8,outer+9-featherOffset,13,-15.9F+i*.65F,1.3F,6,1.3F);
                }
            }
            PartDefinition secondary = bird.addOrReplaceChild("secondary_"+suffix, box(0,110,side < 0 ? -23 : 0,-4,-9,23,7,18),PartPose.offset(side*12,-32,3));
            PartDefinition secondaryTip = secondary.addOrReplaceChild("outer", CubeListBuilder.create(), PartPose.offset(side*36,0,0));
            for(int i=0;i<6;i++) {
                PartDefinition featherBone = i >= 3 ? secondaryTip : secondary;
                float featherOffset = i >= 3 ? side*36 : 0;
                float start = 15+i*7.1F;
                float outer = side < 0 ? -start-10 : start;
                float length = 24+i*1.7F+(i%3)*2+(side>0 ? 2:0);
                float sickDrop = 14 + i * 1.8F + (i % 3) * 3.5F;
                float sickWidth = i >= 4 ? 10 - (i - 3) * 1.4F : 10;
                add(featherBone,"upper_flight_"+i,50,125,outer-featherOffset,-2,-8+i*.75F,sickWidth,sickDrop,6.5F);
                add(featherBone,"upper_plum_end_"+i,216,170,outer+1-featherOffset,sickDrop-7,-8+i*.75F,8,9+(i%3)*2,5.2F);
                if(i%2==0 || i==5) add(featherBone,"upper_frayed_strand_"+i,216,170,outer+((i%3)*2)-featherOffset,sickDrop+1,-8+i*.75F,2.1F,8+(i*5)%13,2.4F);
                add(featherBone,"upper_cover_"+i,156,0,outer+1-featherOffset,-4,-12+i*.75F,Math.min(8,sickWidth),7+(i%2)*2,7.7F);
                add(featherBone,"upper_layered_covert_"+i,130,40,outer+2-featherOffset,2+(i%3),-12.3F+i*.75F,Math.min(6,sickWidth),5+(i%3)*1.3F,2.1F);
                if(i==2 || i==4 || (side<0 && i==5)) add(featherBone,"upper_torn_"+i,216,170,outer+3-featherOffset,sickDrop, -8+i*.75F,3,5+i*2,3);
                if(i==1 || i==4) {
                    add(featherBone,"upper_mor_branch_"+i,180,0,outer+3-featherOffset,2,-12.8F+i*.75F,2,sickDrop*.52F,1.2F);
                    add(featherBone,"upper_mor_branch_spur_"+i,180,8,outer+7-featherOffset,5,-12.7F+i*.75F,2,sickDrop*.24F,1.3F);
                    add(featherBone,"upper_mor_angled_joint_"+i,180,0,outer+4-featherOffset,8,-12.9F+i*.75F,4,1.7F,1.5F);
                }
            }
            // Chunky articulated-looking thighs, knee, shins, forward grip and hooked ivory tips.
            PartDefinition foot = bird.addOrReplaceChild(side<0?"left_foot":"right_foot",box(112,84,-5,-3,-5,10,14,11),PartPose.offset(side*8,7,11));
            add(foot,"knee",112,84,-5,9,-5,11,6,12);
            add(foot,"shin",20,160,-3.5F,12,-8,7,12,8);
            add(foot,"ankle",20,160,-5,22,-8,10,4,12);
            for(int toe=0;toe<3;toe++) {
                float x=-7+toe*5;
                float z=-16-(toe==1 ? 3:0);
                add(foot,"toe_"+toe,20,160,x,24,z,4.4F,3.8F,12);
                add(foot,"talon_base_"+toe,64,194,x+.4F,25,z-6,3.8F,4.6F,7);
                add(foot,"talon_hook_"+toe,64,194,x+1,29,z-8,2.8F,6,4.5F);
                add(foot,"talon_point_"+toe,64,194,x+1.5F,33,z-7,1.8F,2.5F,3.2F);
            }
            add(foot,"rear_toe",168,88,-1,24,2,4,4,11);
            add(foot,"rear_talon_hook",64,194,-.2F,28,9,2.5F,6,4);
        }
        PartDefinition tail = bird.addOrReplaceChild("tail",box(0,229,-9,-4,-1,18,6,19),PartPose.offset(0,-3,13));
        for(int i=0;i<7;i++) {
            float z=6+(i%2)*2;
            float length=11+(3-Math.abs(i-3))*2.5F+(i%3);
            add(tail,"tail_plume_"+i,(i%2==0)?48:88,227,-19+i*5,-3,z,6,5+(i%3)*1.2F,length);
            // Trailing two-stage plumes create a fanned, hanging tail from both back and side.
            add(tail,"tail_plum_tip_"+i,216,170,-18+i*5,1+(i%3),z+length*.53F,4,8+(i%3)*2,length*.55F);
            if(i==1 || i==5) add(tail,"tail_torn_"+i,216,170,-18+i*5,8,z+length-1,3,8+i%2*2,3);
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
        // Independent delayed distal flexion on all four wings.
        leftPrimaryTip.zRot = .055F + (float)Math.sin(age*speed-.60F)*.145F;
        rightPrimaryTip.zRot = -leftPrimaryTip.zRot;
        leftSecondaryTip.zRot = .085F + (float)Math.sin(age*speed+.10F)*.115F;
        rightSecondaryTip.zRot = -leftSecondaryTip.zRot;
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
            leftPrimaryTip.zRot = .20F; rightPrimaryTip.zRot = -.20F;
            leftSecondaryTip.zRot = .12F; rightSecondaryTip.zRot = -.12F;
        }
        if (pose == MorovoyRaven.WING && t > 14) {
            leftPrimary.zRot = -.40F; rightPrimary.zRot = .40F;
            leftSecondary.zRot = .62F; rightSecondary.zRot = -.62F;
            leftPrimaryTip.zRot = -.37F; rightPrimaryTip.zRot = .37F;
            leftSecondaryTip.zRot = -.21F; rightSecondaryTip.zRot = .21F;
        }
        if (pose == MorovoyRaven.PROJECTILE) {
            head.xRot = -.21F; leftPrimary.zRot = .27F; rightPrimary.zRot = -.27F;
            leftSecondary.zRot = .98F; rightSecondary.zRot = -.98F;
        }
        if (pose == MorovoyRaven.GROUND_IDLE || pose == MorovoyRaven.LANDING || pose == MorovoyRaven.TAKEOFF) {
            float fold = pose == MorovoyRaven.GROUND_IDLE ? 1F : pose == MorovoyRaven.LANDING ? Math.clamp(t / 36F, 0F, 1F) : 1F - Math.clamp(t / 34F, 0F, 1F);
            leftPrimary.yRot = 1.10F * fold; rightPrimary.yRot = -1.10F * fold;
            leftSecondary.yRot = .88F * fold; rightSecondary.yRot = -.88F * fold;
            leftPrimaryTip.yRot = .24F * fold; rightPrimaryTip.yRot = -.24F * fold;
            leftSecondaryTip.yRot = .18F * fold; rightSecondaryTip.yRot = -.18F * fold;
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
            leftPrimaryTip.zRot = .26F; rightPrimaryTip.zRot = -.26F;
            leftSecondaryTip.zRot = .18F; rightSecondaryTip.zRot = -.18F;
            leftFoot.xRot = rightFoot.xRot = .64F;
        }
    }
    @Override public void renderToBuffer(PoseStack matrices, VertexConsumer out, int light, int overlay, int tint) {
        root.render(matrices,out,light,overlay,tint);
    }
}
