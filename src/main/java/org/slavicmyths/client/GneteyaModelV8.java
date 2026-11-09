package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.List;
import java.util.Set;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import org.slavicmyths.sisters.SisterBoss;

/** Gneteya V8 preview NeoForge 1.21.1 articulated model.
 * ALL cubes come from the compilable pure Java GneteyaGeometryV8 class, also used for GLB/Python.
 * This version is a preview source; not installed into the production renderer until approved. */
public final class GneteyaModelV8 extends EntityModel<SisterBoss> {
    public static final String[] MATERIALS={"black","black_hi","red","red_hi","red_dark","red_shadow","bone","bone_shadow","bone_dark","skin","hair","hair_hi","hair_dark","socket","red_eye","eye_glint","shaft","shaft_hi"};
    private static final List<GneteyaGeometryV8.Box> GEOMETRY=GneteyaGeometryV8.build();
    private final ModelPart root;
    private final ModelPart upper;
    private final ModelPart redSkirt;
    private final ModelPart blackDress;
    private final ModelPart head;
    private final ModelPart braid;
    private final ModelPart leftArm;
    private final ModelPart leftForearm;
    private final ModelPart rightForearm;
    private final ModelPart legLeft;
    private final ModelPart legRight;
    private final ModelPart rightArm;
    private final ModelPart scythe;
    private final ModelPart panelLeft;
    private final ModelPart panelRight;
    private final ModelPart panelCenter;
    private final ModelPart skirtLeft;
    private final ModelPart skirtRight;

    public GneteyaModelV8(ModelPart baked){
        this.root=baked.getChild("root");
        this.redSkirt=root.getChild("red_skirt");
        this.blackDress=root.getChild("black_dress");
        this.upper=root.getChild("upper");
        this.head=upper.getChild("head");
        this.braid=head.getChild("braid");
        this.leftArm=upper.getChild("left_arm");
        this.leftForearm=leftArm.getChild("left_forearm");
        this.rightArm=upper.getChild("right_arm");
        this.rightForearm=rightArm.getChild("right_forearm");
        this.legLeft=root.getChild("leg_left");
        this.legRight=root.getChild("leg_right");
        this.scythe=leftForearm.getChild("scythe");
        this.panelLeft=root.getChild("red_panel_left");
        this.panelRight=root.getChild("red_panel_right");
        this.panelCenter=root.getChild("red_panel_center");
        this.skirtLeft=root.getChild("skirt_left");
        this.skirtRight=root.getChild("skirt_right");
    }

    private static int material(String name){for(int i=0;i<MATERIALS.length;i++)if(MATERIALS[i].equals(name))return i;throw new IllegalArgumentException("Gneteya material not in atlas: "+name);}
    private static CubeListBuilder cubes(Set<String> parts,double px,double py,double pz){
        CubeListBuilder builder=CubeListBuilder.create();
        for(GneteyaGeometryV8.Box box:GEOMETRY){
            if(!parts.contains(box.part()))continue;
            int id=material(box.material());
            int u=id%8*128;
            int v=id/8*128;
            builder.texOffs(u,v).addBox((float)(box.x()-px),(float)(-box.y()-box.h()+py),(float)(box.z()-pz),(float)box.w(),(float)box.h(),(float)box.d());
        }
        return builder;
    }
    private static PartPose offset(double childX,double childY,double childZ,double parentX,double parentY,double parentZ){return PartPose.offset((float)(childX-parentX),(float)(parentY-childY),(float)(childZ-parentZ));}
    public static LayerDefinition create(){
        MeshDefinition mesh=new MeshDefinition();
        PartDefinition root=mesh.getRoot().addOrReplaceChild("root",CubeListBuilder.create(),PartPose.ZERO);
        // Main gown lower half and legs are separate from upper body and head.
        root.addOrReplaceChild("leg_left",cubes(Set.of("leg_left"),-2.05,16,0),offset(-2.05,16,0,0,0,0));
        root.addOrReplaceChild("leg_right",cubes(Set.of("leg_right"),2.05,16,0),offset(2.05,16,0,0,0,0));
        root.addOrReplaceChild("black_dress",cubes(Set.of("dress_black"),0,20.1,0),offset(0,20.1,0,0,0,0));
        root.addOrReplaceChild("red_skirt",cubes(Set.of("red_skirt"),0,34.2,0),offset(0,34.2,0,0,0,0));
        root.addOrReplaceChild("skirt_left",cubes(Set.of("skirt_left"),-4.1,29.0,0),offset(-4.1,29.0,0,0,0,0));
        root.addOrReplaceChild("skirt_right",cubes(Set.of("skirt_right"),4.1,29.0,0),offset(4.1,29.0,0,0,0,0));
        root.addOrReplaceChild("red_panel_left",cubes(Set.of("red_panel_left"),-4.7,33.9,-2.0),offset(-4.7,33.9,-2,0,0,0));
        root.addOrReplaceChild("red_panel_right",cubes(Set.of("red_panel_right"),4.7,33.9,-2.0),offset(4.7,33.9,-2,0,0,0));
        root.addOrReplaceChild("red_panel_center",cubes(Set.of("red_panel_center"),0,33.9,-2.0),offset(0,33.9,-2,0,0,0));
        PartDefinition upper=root.addOrReplaceChild("upper",cubes(Set.of("torso","ribcage","neck"),0,34.4,0),offset(0,34.4,0,0,0,0));
        // Head is a separate joint; braid swings with head and against movement.
        PartDefinition head=upper.addOrReplaceChild("head",cubes(Set.of("head","jaw","eyes","hair_crown","hair_side","hair_back"),0,52.0,0),offset(0,52,0,0,34.4,0));
        head.addOrReplaceChild("braid",cubes(Set.of("braid"),4.5,57.0,2.2),offset(4.5,57,2.2,0,52,0));
        PartDefinition left=upper.addOrReplaceChild("left_arm",cubes(Set.of("left_arm","armor_left"),-5.10,47.9,0),offset(-5.10,47.9,0,0,34.4,0));
        PartDefinition right=upper.addOrReplaceChild("right_arm",cubes(Set.of("right_arm","armor_right"),5.10,47.9,0),offset(5.10,47.9,0,0,34.4,0));
        // Weapon lives under weapon-arm: when that arm swings, grip and scythe move together.
        PartDefinition leftForearm=left.addOrReplaceChild("left_forearm",cubes(Set.of("left_forearm"),-6.55,41.1,-.20),offset(-6.55,41.1,-.20,-5.10,47.9,0));
        right.addOrReplaceChild("right_forearm",cubes(Set.of("right_forearm"),5.55,39.5,-.20),offset(5.55,39.5,-.20,5.10,47.9,0));
        leftForearm.addOrReplaceChild("scythe",cubes(Set.of("scythe"),-7.5,36.0,-3.50),offset(-7.5,36.0,-3.50,-6.55,41.1,-.20));
        return LayerDefinition.create(mesh,1024,512);
    }
    private static float sin(float f){return (float)Math.sin(f);}
    private static float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
    @Override public void setupAnim(SisterBoss boss,float walk,float stride,float age,float yaw,float pitch){
        // Restore the baked pose, including hidden rotation/scale axes after combat.
        root.getAllParts().forEach(ModelPart::resetPose);
        // Absolute poses every frame: no cumulative drifting bones.
        float moving=clamp(stride,0,1);
        float step=sin(walk*.73F);
        float idle=sin(age*.07F);
        // Gneteya WALKS. Feet remain grounded: no hovering offset.
        root.y=0;
        legLeft.xRot=step*moving*.41F;
        legRight.xRot=-step*moving*.41F;
        leftForearm.xRot=-.12F+sin(walk*.73F+.25F)*moving*.11F;
        rightForearm.xRot=.08F+sin(walk*.73F+.90F)*moving*.12F;
        redSkirt.xRot=step*moving*.24F;
        redSkirt.yScale=1.0F-.10F*moving*Math.abs(step);
        redSkirt.zRot=step*moving*.019F;
        blackDress.xRot=-step*moving*.08F;
        blackDress.yScale=1.0F-.035F*moving*Math.abs(step);
        blackDress.zRot=-step*moving*.011F;
        upper.xRot=0;
        upper.yRot=0;
        upper.zRot=sin(age*.055F)*.014F;
        head.xRot=clamp(pitch*.0174532925F,-.32F,.32F)*.32F;
        head.yRot=clamp(yaw*.0174532925F,-.42F,.42F)*.39F;
        head.zRot=sin(age*.06F)*.013F;
        braid.xRot=sin(age*.085F)*.055F+step*moving*.06F;
        braid.zRot=sin(age*.09F)*.045F;
        panelLeft.xRot=-step*.32F*moving;
        panelLeft.yScale=1.0F+.13F*moving*Math.max(0F,step);
        panelRight.xRot=step*.32F*moving;
        panelRight.yScale=1.0F+.13F*moving*Math.max(0F,-step);
        panelCenter.xRot=sin(walk*.73F+.4F)*.045F*moving;
        panelLeft.zRot=sin(age*.06F)*.024F;
        panelRight.zRot=-sin(age*.06F)*.024F;
        panelCenter.zRot=0;
        skirtLeft.xRot=-step*.34F*moving;
        skirtRight.xRot=step*.34F*moving;
        skirtLeft.yScale=1.0F+.12F*Math.max(0F,step)*moving;
        skirtRight.yScale=1.0F+.12F*Math.max(0F,-step)*moving;
        skirtLeft.zRot=-step*.034F*moving;
        skirtRight.zRot=step*.034F*moving;
        leftArm.xRot=sin(age*.10F)*.018F;
        leftArm.yRot=0;
        leftArm.zRot=0;
        rightArm.xRot=sin(age*.10F+.3F)*.018F;
        rightArm.yRot=0;
        rightArm.zRot=0;
        leftForearm.yRot=0; leftForearm.zRot=0;
        rightForearm.yRot=0; rightForearm.zRot=0;
        scythe.xRot=0;
        scythe.yRot=0;
        scythe.zRot=0;
        // Gneteya combat state is already available via the synchronized SisterBoss action/frame.
        if(!boss.isTryaseya() && boss.action()!=SisterBoss.IDLE){
            float frame=boss.actionFrame();
            float windup=clamp(frame/15F,0,1);
            float impact=clamp((frame-24F)/6F,0,1);
            float recovery=clamp((frame-33F)/11F,0,1);
            float power=windup*(1-recovery);
            int action=boss.action();
            if(action==SisterBoss.HEAVY){
                // SINGLE-HANDED SWEEP. Shaft and blade remain attached to the LEFT gripping hand.
                // The forearm and weapon form a continuous linkage, with no local scythe flip.
                upper.yRot=-.25F*power-.55F*impact*(1-recovery);
                leftArm.zRot=.12F*power+.50F*impact*(1-recovery);
                leftArm.yRot=-.12F*power-.20F*impact*(1-recovery);
                leftForearm.zRot=-.18F*power+.35F*impact*(1-recovery);
                leftArm.xRot=-.14F*power+.23F*impact*(1-recovery);
                rightArm.xRot=-.11F*power;
                head.yRot+=.10F*power;
            }else if(action==SisterBoss.SLAM){
                // TWO-HANDED OVERHEAD SLAM. The left arm draws the staff toward body center,
                // the right shoulder/elbow crosses the chest to grasp the SAME shaft.
                // No independent 3D rotation of the scythe bone: blade stays aligned to pole.
                upper.yRot=-.09F*power+.11F*impact*(1-recovery);
                leftArm.zRot=.37F*power+.06F*impact*(1-recovery);
                leftArm.xRot=-.24F*power+1.16F*impact*(1-recovery);
                leftForearm.xRot=-.07F*power+.19F*impact*(1-recovery);
                leftForearm.zRot=-.08F*power;
                rightArm.zRot=-.7825F*power-.3095F*impact*(1-recovery);
                rightArm.xRot=-.4317F*power+1.004F*impact*(1-recovery);
                rightForearm.zRot=.2403F*power-.5843F*impact*(1-recovery);
                rightForearm.xRot=.7701F*power-.7121F*impact*(1-recovery);
                upper.xRot=-.10F*power+.30F*impact*(1-recovery);
            }else if(action==SisterBoss.WALL||action==SisterBoss.PULL||action==SisterBoss.SPIKES){
                leftArm.zRot=.22F*power;
                leftArm.xRot=-.52F*power;
                leftForearm.xRot=-.25F*power;
                rightArm.xRot=-.21F*power;
                head.xRot-=.10F*power;
                upper.zRot+=.10F*power;
            }
            panelLeft.xRot+=sin(frame*.18F)*.05F*power;
            panelRight.xRot-=sin(frame*.18F)*.05F*power;
            braid.zRot+=.06F*power;
        }
        if(boss.hurtTime>0){head.zRot+=sin(age*.7F)*.09F;upper.zRot+=.06F;}
        if(boss.enraged()){braid.xRot*=1.5F;panelLeft.zRot*=1.8F;panelRight.zRot*=1.8F;}
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){root.render(pose,out,light,overlay,color);}
}