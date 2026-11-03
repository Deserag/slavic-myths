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
import org.slavicmyths.sisters.SisterBoss;

/**
 * Production Sisters V2 geometry, reconstructed one-to-one from the approved
 * Matplotlib cuboids. Coordinates in each addBox use model pixels; the
 * original plot's (x, depth, elevation) becomes (x, -elevation, depth).
 * Torso = 6px, waist = 5px, hips = 8px / 9.5px.
 * No legacy solid pauldron or pillar skirt geometry remains.
 */
public final class SistersModel extends EntityModel<SisterBoss> {
    private final boolean spirit;
    private final ModelPart root,rig,body,head,leftArm,rightArm,mist,weapon;
    private final float headBaseY,bodyBaseY,leftBaseY,rightBaseY;
    public SistersModel(boolean tryaseya) {
        spirit=tryaseya;
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition rigDef=mesh.getRoot().addOrReplaceChild("rig",CubeListBuilder.create(),PartPose.offset(0,24,0));
        buildRig(rigDef,spirit);
        root=LayerDefinition.create(mesh,512,512).bakeRoot();
        rig=root.getChild("rig");
        body=rig.getChild("body");head=rig.getChild("head");
        leftArm=rig.getChild("left_arm");rightArm=rig.getChild("right_arm");
        mist=rig.getChild("mist");weapon=rightArm.getChild("weapon");
        headBaseY=head.y;bodyBaseY=body.y;leftBaseY=leftArm.y;rightBaseY=rightArm.y;
    }
    private static void buildRig(PartDefinition rig,boolean spirit) {
        if(spirit) buildTryaseya(rig); else buildGneteya(rig);
    }
    private static void buildTryaseya(PartDefinition rig) {
        PartDefinition body=rig.addOrReplaceChild("body",CubeListBuilder.create()
                .texOffs(16,0).addBox(-4,4,-3.5F,8,9,7)
                .texOffs(0,0).addBox(-2.5F,-3,-2.5F,5,7,5)
                .texOffs(0,0).addBox(-3,-13,-3,6,10,6),PartPose.offset(0,-33,0));
        PartDefinition head=rig.addOrReplaceChild("head",CubeListBuilder.create()
                .texOffs(256,0).addBox(-3.5F,-10,-3.5F,7,10,7)
                .texOffs(0,0).addBox(-4,-10,-3.3F,8,2,7.3F)
                .texOffs(0,0).addBox(-4,-7,-3.2F,1,6,7.2F)
                .texOffs(0,0).addBox(3,-7,-3.2F,1,6,7.2F)
                .texOffs(0,0).addBox(-4,-8,3.5F,8,8,1)
                .texOffs(480,128).addBox(-2.8F,-6.2F,-3.95F,2,1,0.5F)
                .texOffs(480,128).addBox(0.8F,-6.2F,-3.95F,2,1,0.5F)
                .texOffs(384,0).addBox(-0.85F,-2.6F,-3.98F,1.7F,0.25F,0.45F)
                .texOffs(384,0).addBox(-3F,-9.2F,-4.1F,1,10,0.8F)
                .texOffs(384,0).addBox(-0.8F,-10.2F,-4.2F,1,12,0.8F)
                .texOffs(384,0).addBox(1.5F,-8.4F,-4.25F,1,10,0.8F)
                .texOffs(384,0).addBox(-4.15F,-8,-2.8F,1,16,0.8F)
                .texOffs(384,0).addBox(3.15F,-8,-2.8F,1,15,0.8F)
                .texOffs(384,0).addBox(-3.7F,-9,2.8F,1,17,0.8F)
                .texOffs(384,0).addBox(2.7F,-9,2.8F,1,16,0.8F)
                .texOffs(384,0).addBox(-3.2F,-9,2.9F,1,18,0.8F)
                .texOffs(384,0).addBox(-1,-7,2.9F,1,17,0.8F)
                .texOffs(384,0).addBox(1.2F,-8,2.9F,1,18,0.8F),PartPose.offset(0,-46,0));
        PartDefinition left_arm=rig.addOrReplaceChild("left_arm",CubeListBuilder.create()
                .texOffs(16,0).addBox(-1,-3,-2.5F,2,14,5)
                .texOffs(256,0).addBox(-2,5,-2,1,9,4),PartPose.offset(-4,-46,0));
        PartDefinition right_arm=rig.addOrReplaceChild("right_arm",CubeListBuilder.create()
                .texOffs(16,0).addBox(-1,-3,-2.5F,2,14,5)
                .texOffs(256,0).addBox(1,5,-2,1,9,4),PartPose.offset(4,-46,0));
        PartDefinition mist=rig.addOrReplaceChild("mist",CubeListBuilder.create()
                .texOffs(384,0).addBox(-2,-20,-2,4,4,0.7F)
                .texOffs(384,0).addBox(-2,-20,1.3F,4,4,0.7F)
                .texOffs(384,0).addBox(-2,-20,-2,0.7F,4,4)
                .texOffs(384,0).addBox(1.3F,-20,-2,0.7F,4,4)
                .texOffs(384,0).addBox(-1.9F,-16,-1.9F,3.8F,4,0.7F)
                .texOffs(384,0).addBox(-1.9F,-16,1.2F,3.8F,4,0.7F)
                .texOffs(384,0).addBox(-1.9F,-16,-1.9F,0.7F,4,3.8F)
                .texOffs(384,0).addBox(1.2F,-16,-1.9F,0.7F,4,3.8F)
                .texOffs(384,0).addBox(-1.7F,-12,-1.7F,3.4F,4,0.7F)
                .texOffs(384,0).addBox(-1.7F,-12,1F,3.4F,4,0.7F)
                .texOffs(384,0).addBox(-1.7F,-12,-1.7F,0.7F,4,3.4F)
                .texOffs(384,0).addBox(1F,-12,-1.7F,0.7F,4,3.4F)
                .texOffs(384,0).addBox(-1.4F,-8,-1.4F,2.8F,4,0.7F)
                .texOffs(384,0).addBox(-1.4F,-8,0.7F,2.8F,4,0.7F)
                .texOffs(384,0).addBox(-1.4F,-8,-1.4F,0.7F,4,2.8F)
                .texOffs(384,0).addBox(0.7F,-8,-1.4F,0.7F,4,2.8F)
                .texOffs(384,0).addBox(-1,-4,-1,2,3,0.7F)
                .texOffs(384,0).addBox(-1,-4,0.3F,2,3,0.7F)
                .texOffs(384,0).addBox(-1,-4,-1,0.7F,3,2)
                .texOffs(384,0).addBox(0.3F,-4,-1,0.7F,3,2)
                .texOffs(384,0).addBox(-1,-1,-1,2,2,2),PartPose.offset(0,0,0));
        right_arm.addOrReplaceChild("weapon",CubeListBuilder.create()
                .texOffs(128,0).addBox(1.8F,-16,-1.2F,1,40,1)
                .texOffs(480,128).addBox(0.3F,-21,-2.6F,4,4,4)
                .texOffs(480,128).addBox(-0.3F,-17,-3.2F,5.2F,0.6F,5.2F)
                .texOffs(480,128).addBox(-0.3F,-20.6F,-3.2F,5.2F,0.6F,5.2F),PartPose.ZERO);
    }
    private static void buildGneteya(PartDefinition rig) {
        PartDefinition body=rig.addOrReplaceChild("body",CubeListBuilder.create()
                .texOffs(384,0).addBox(-3.4F,15,-2.2F,2.2F,18,4.4F)
                .texOffs(384,0).addBox(1.2F,15,-2.2F,2.2F,18,4.4F)
                .texOffs(16,0).addBox(-4.75F,7,-4,9.5F,8,8)
                .texOffs(0,0).addBox(-2.5F,0,-2.5F,5,7,5)
                .texOffs(384,0).addBox(-3,-11,-3,6,11,6)
                .texOffs(128,0).addBox(-4.2F,-3,-3.35F,1.2F,1,6.7F)
                .texOffs(128,0).addBox(3,-3,-3.35F,1.2F,1,6.7F)
                .texOffs(128,0).addBox(-3,-3,-3.2F,4.5F,1,0.8F)
                .texOffs(128,0).addBox(-4.2F,-6,-3.35F,1.2F,1,6.7F)
                .texOffs(128,0).addBox(3,-6,-3.35F,1.2F,1,6.7F)
                .texOffs(128,0).addBox(-3,-6,-3.2F,4.5F,1,0.8F)
                .texOffs(128,0).addBox(-4.2F,-9,-3.35F,1.2F,1,6.7F)
                .texOffs(128,0).addBox(3,-9,-3.35F,1.2F,1,6.7F)
                .texOffs(128,0).addBox(-3,-9,-3.2F,4.5F,1,0.8F)
                .texOffs(128,0).addBox(-0.5F,-9,-3.2F,1,8,0.8F)
                .texOffs(128,0).addBox(-0.7F,-3.2F,2.65F,1.4F,2.2F,1.1F)
                .texOffs(128,0).addBox(-0.7F,-6.2F,2.65F,1.4F,2.2F,1.1F)
                .texOffs(128,0).addBox(-0.7F,-9.2F,2.65F,1.4F,2.2F,1.1F)
                .texOffs(128,0).addBox(-0.7F,-12.2F,2.65F,1.4F,2.2F,1.1F)
                .texOffs(128,0).addBox(-5.6F,-7,-4,2.6F,1,8)
                .texOffs(128,0).addBox(-6.5F,-8.4F,-3.8F,1.1F,4,7.6F)
                .texOffs(128,0).addBox(-6,-4.1F,-4.3F,1.1F,1,8.6F)
                .texOffs(128,0).addBox(-4.9F,-2.4F,-4.5F,1.1F,1,9)
                .texOffs(128,0).addBox(3.6F,-7,-4,2.6F,1,8)
                .texOffs(128,0).addBox(4.5F,-8.4F,-3.8F,1.1F,4,7.6F)
                .texOffs(128,0).addBox(4,-4.1F,-4.3F,1.1F,1,8.6F)
                .texOffs(128,0).addBox(2.9F,-2.4F,-4.5F,1.1F,1,9)
                .texOffs(128,0).addBox(-6.2F,-11,2.5F,2,4,2)
                .texOffs(128,0).addBox(-4.8F,-15,3.3F,2,5,2)
                .texOffs(128,0).addBox(-3.6F,-17,3.9F,2,4,2)
                .texOffs(128,0).addBox(1.6F,-11,2.5F,2,4,2)
                .texOffs(128,0).addBox(2.8F,-15,3.3F,2,5,2)
                .texOffs(128,0).addBox(4,-17,3.9F,2,4,2),PartPose.offset(0,-33,0));
        PartDefinition head=rig.addOrReplaceChild("head",CubeListBuilder.create()
                .texOffs(256,0).addBox(-3.5F,-8,-3.5F,7,10,7)
                .texOffs(128,0).addBox(-3.4F,-7,-4.25F,1.1F,5.7F,1.1F)
                .texOffs(128,0).addBox(2.3F,-7,-4.25F,1.1F,5.7F,1.1F)
                .texOffs(128,0).addBox(-2.3F,-7,-4.25F,4.6F,1.3F,1.2F)
                .texOffs(128,0).addBox(-4,-7,-4,1,5,8)
                .texOffs(128,0).addBox(3,-7,-4,1,5,8)
                .texOffs(128,0).addBox(-3.2F,-6.2F,-4.1F,6.4F,1.2F,0.8F)
                .texOffs(128,0).addBox(-2.7F,-2.2F,-4.1F,5.4F,1.2F,0.8F)
                .texOffs(384,0).addBox(-2.6F,-5.05F,-4.8F,2.1F,1.8F,0.5F)
                .texOffs(384,0).addBox(0.5F,-5.05F,-4.8F,2.1F,1.8F,0.5F)
                .texOffs(480,128).addBox(-2.05F,-4.5F,-4.97F,1.1F,0.75F,0.28F)
                .texOffs(480,128).addBox(0.95F,-4.5F,-4.97F,1.1F,0.75F,0.28F),PartPose.offset(0,-46,0));
        PartDefinition left_arm=rig.addOrReplaceChild("left_arm",CubeListBuilder.create()
                .texOffs(384,0).addBox(-2,0,-2.5F,2,16,5)
                .texOffs(128,0).addBox(-2.2F,15,-1.4F,1.2F,4,2.8F),PartPose.offset(-4,-46,0));
        PartDefinition right_arm=rig.addOrReplaceChild("right_arm",CubeListBuilder.create()
                .texOffs(384,0).addBox(0,0,-2.5F,2,16,5)
                .texOffs(128,0).addBox(1,16,-1.2F,1.2F,4,2.8F),PartPose.offset(4,-46,0));
        PartDefinition mist=rig.addOrReplaceChild("mist",CubeListBuilder.create(),PartPose.offset(0,0,0));
        right_arm.addOrReplaceChild("weapon",CubeListBuilder.create()
                .texOffs(384,0).addBox(2.3F,-18,-1,1,48,1)
                .texOffs(128,0).addBox(2.8F,-17,-1,5,3,1)
                .texOffs(128,0).addBox(7.2F,-15,-1,4,3,1)
                .texOffs(128,0).addBox(10.4F,-12,-1,3,3,1)
                .texOffs(128,0).addBox(12.8F,-9,-1,2,3,1)
                .texOffs(128,0).addBox(14.4F,-7,-1,1.5F,4,1)
                .texOffs(128,0).addBox(15.5F,-5,-1,1.2F,4,1)
                .texOffs(128,0).addBox(16.2F,-2,-1,1,4,1),PartPose.ZERO);
    }
    @Override public void setupAnim(SisterBoss boss,float walk,float stride,float age,float yaw,float pitch) {
        float swing=Mth.cos(walk*.6662F)*Mth.clamp(stride,0,1);
        rig.y=24+(spirit?(float)Math.sin(age*.08F)*2.5F-2.5F:0);
        head.y=headBaseY;body.y=bodyBaseY;leftArm.y=leftBaseY;rightArm.y=rightBaseY;
        head.xRot=pitch*Mth.DEG_TO_RAD;head.yRot=yaw*Mth.DEG_TO_RAD;
        body.xRot=.012F*Mth.sin(age*.09F);body.yRot=0;body.zRot=0;
        leftArm.xRot=spirit?.05F:-.25F;rightArm.xRot=spirit?-.08F:-.2F;
        leftArm.yRot=spirit?0:-.18F;rightArm.yRot=spirit?0:.18F;
        leftArm.zRot=spirit?-.04F:-.35F;rightArm.zRot=spirit?.04F:.35F;
        weapon.xRot=0;weapon.yRot=0;weapon.zRot=0;
        mist.visible=spirit;
        if(spirit){mist.yRot=.028F*Mth.sin(age*.07F);rightArm.xRot+=.05F*Mth.sin(age*.11F);}
        else {leftArm.xRot-=.08F+swing*.08F;rightArm.xRot-=.12F-swing*.08F;}
        float f=boss.actionFrame(),wind=Mth.clamp(f/23F,0,1),recovery=Mth.clamp((f-26)/15F,0,1);
        switch(boss.action()){
            case SisterBoss.HEAVY -> {rightArm.xRot=-1.75F*wind+.95F*recovery;body.yRot=.1F*Mth.sin(wind*Mth.PI);}
            case SisterBoss.SLAM -> {rightArm.xRot=-2F*wind+1.1F*recovery;leftArm.xRot=-.6F*wind;}
            case SisterBoss.WALL,SisterBoss.SPIKES,SisterBoss.PULL -> {rightArm.xRot=-1.6F*wind;leftArm.xRot=-.5F*wind;weapon.zRot=.18F*Mth.sin(wind*Mth.PI);}
            case SisterBoss.FEVER,SisterBoss.FLASH,SisterBoss.SPIRITS,SisterBoss.ZONE -> {rightArm.xRot=-.8F*wind;leftArm.xRot=-.3F*wind;}
            case SisterBoss.EVADE -> {body.yRot=.45F*Mth.sin(wind*Mth.PI);}
            default -> {}
        }
        if(boss.enraged())body.zRot=.02F*Mth.sin(age*.27F);
        if(boss.hurtTime>0)body.xRot-=.1F;
        if(boss.deathTime>0)body.zRot+=Mth.clamp(boss.deathTime/20F,0,1)*.45F;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color) {
        root.render(pose,out,light,overlay,color);
    }
}
