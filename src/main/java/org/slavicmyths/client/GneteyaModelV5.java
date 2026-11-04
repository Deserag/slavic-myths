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

/** Gneteya V5: articulated Minecraft model using the same cuboids as the Java/Python preview. */
public final class GneteyaModelV5 extends EntityModel<SisterBoss> {
    private static final String[] MATERIALS={"black","black_hi","red","red_hi","red_dark","red_shadow","bone","bone_shadow","bone_dark","skin","hair","hair_hi","hair_dark","socket","red_eye","eye_glint","shaft","shaft_hi"};
    private static final List<GneteyaGeometryV5.Box> GEOMETRY=GneteyaGeometryV5.build();
    private final ModelPart root;
    private final ModelPart upper;
    private final ModelPart redSkirt;
    private final ModelPart blackDress;
    private final ModelPart head;
    private final ModelPart braid;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart scythe;
    private final ModelPart panelLeft;
    private final ModelPart panelRight;
    private final ModelPart panelCenter;
    private final ModelPart skirtLeft;
    private final ModelPart skirtRight;

    public GneteyaModelV5(ModelPart baked){
        this.root=baked.getChild("root");
        this.redSkirt=root.getChild("red_skirt");
        this.blackDress=root.getChild("black_dress");
        this.upper=root.getChild("upper");
        this.head=upper.getChild("head");
        this.braid=head.getChild("braid");
        this.leftArm=upper.getChild("left_arm");
        this.rightArm=upper.getChild("right_arm");
        this.scythe=leftArm.getChild("scythe");
        this.panelLeft=root.getChild("red_panel_left");
        this.panelRight=root.getChild("red_panel_right");
        this.panelCenter=root.getChild("red_panel_center");
        this.skirtLeft=root.getChild("skirt_left");
        this.skirtRight=root.getChild("skirt_right");
    }

    private static int material(String name){for(int i=0;i<MATERIALS.length;i++)if(MATERIALS[i].equals(name))return i;throw new IllegalArgumentException("Gneteya material not in atlas: "+name);}
    private static CubeListBuilder cubes(Set<String> parts,double px,double py,double pz){
        CubeListBuilder builder=CubeListBuilder.create();
        for(GneteyaGeometryV5.Box box:GEOMETRY){
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
        root.addOrReplaceChild("legs",cubes(Set.of("legs"),0,0,0),PartPose.ZERO);
        root.addOrReplaceChild("black_dress",cubes(Set.of("dress_black"),0,20.1,0),offset(0,20.1,0,0,0,0));
        root.addOrReplaceChild("red_skirt",cubes(Set.of("red_skirt"),0,34.2,0),offset(0,34.2,0,0,0,0));
        root.addOrReplaceChild("skirt_left",cubes(Set.of("skirt_left"),-4.1,17.8,0),offset(-4.1,17.8,0,0,0,0));
        root.addOrReplaceChild("skirt_right",cubes(Set.of("skirt_right"),4.1,17.8,0),offset(4.1,17.8,0,0,0,0));
        root.addOrReplaceChild("red_panel_left",cubes(Set.of("red_panel_left"),-4.7,33.9,-2.0),offset(-4.7,33.9,-2,0,0,0));
        root.addOrReplaceChild("red_panel_right",cubes(Set.of("red_panel_right"),4.7,33.9,-2.0),offset(4.7,33.9,-2,0,0,0));
        root.addOrReplaceChild("red_panel_center",cubes(Set.of("red_panel_center"),0,33.9,-2.0),offset(0,33.9,-2,0,0,0));
        PartDefinition upper=root.addOrReplaceChild("upper",cubes(Set.of("torso","ribcage","neck"),0,34.4,0),offset(0,34.4,0,0,0,0));
        PartDefinition head=upper.addOrReplaceChild("head",cubes(Set.of("head","jaw","eyes","hair_crown","hair_side","hair_back"),0,52.0,0),offset(0,52,0,0,34.4,0));
        head.addOrReplaceChild("braid",cubes(Set.of("braid"),4.5,57.0,2.2),offset(4.5,57,2.2,0,52,0));
        PartDefinition left=upper.addOrReplaceChild("left_arm",cubes(Set.of("left_arm","armor_left"),-5.10,47.9,0),offset(-5.10,47.9,0,0,34.4,0));
        upper.addOrReplaceChild("right_arm",cubes(Set.of("right_arm","armor_right"),5.10,47.9,0),offset(5.10,47.9,0,0,34.4,0));
        left.addOrReplaceChild("scythe",cubes(Set.of("scythe"),-7.5,39.10,-3.50),offset(-7.5,39.10,-3.50,-5.10,47.9,0));
        return LayerDefinition.create(mesh,1024,512);
    }
    private static float sin(float f){return (float)Math.sin(f);}
    private static float clamp(float v,float lo,float hi){return Math.max(lo,Math.min(hi,v));}
    @Override public void setupAnim(SisterBoss boss,float walk,float stride,float age,float yaw,float pitch){
        float moving=clamp(stride,0,1);
        float step=sin(walk*.73F);
        float idle=sin(age*.07F);
        root.y=idle*.20F;
        redSkirt.xRot=step*moving*.028F;
        redSkirt.zRot=step*moving*.019F;
        blackDress.xRot=-step*moving*.018F;
        blackDress.zRot=-step*moving*.011F;
        upper.xRot=0;upper.yRot=0;upper.zRot=sin(age*.055F)*.014F;
        head.xRot=clamp(pitch*.0174532925F,-.32F,.32F)*.32F;
        head.yRot=clamp(yaw*.0174532925F,-.42F,.42F)*.39F;
        head.zRot=sin(age*.06F)*.013F;
        braid.xRot=sin(age*.085F)*.055F+step*moving*.06F;
        braid.zRot=sin(age*.09F)*.045F;
        panelLeft.xRot=-step*.060F*moving;
        panelRight.xRot=step*.060F*moving;
        panelCenter.xRot=sin(walk*.73F+.4F)*.045F*moving;
        panelLeft.zRot=sin(age*.06F)*.024F;
        panelRight.zRot=-sin(age*.06F)*.024F;
        panelCenter.zRot=0;
        skirtLeft.xRot=-step*.052F*moving;
        skirtRight.xRot=step*.052F*moving;
        leftArm.xRot=sin(age*.10F)*.018F;leftArm.yRot=0;leftArm.zRot=0;
        rightArm.xRot=sin(age*.10F+.3F)*.018F;rightArm.yRot=0;rightArm.zRot=0;
        scythe.xRot=0;scythe.yRot=0;scythe.zRot=0;
        if(!boss.isTryaseya() && boss.action()!=SisterBoss.IDLE){
            float frame=boss.actionFrame();
            float windup=clamp(frame/15F,0,1);
            float impact=clamp((frame-24F)/6F,0,1);
            float recovery=clamp((frame-33F)/11F,0,1);
            float power=windup*(1-recovery);
            int action=boss.action();
            if(action==SisterBoss.HEAVY){
                upper.yRot=-.34F*power+.85F*impact*(1-recovery);
                leftArm.zRot=-.40F*power+.72F*impact*(1-recovery);
                leftArm.yRot=.08F*power;
                scythe.zRot=-.16F*power+.16F*impact;
            } else if(action==SisterBoss.SLAM){
                leftArm.xRot=-.72F*power+1.10F*impact*(1-recovery);
                rightArm.xRot=-.65F*power+.92F*impact*(1-recovery);
                upper.xRot=-.12F*power+.25F*impact*(1-recovery);
                scythe.xRot=-.12F*power;
            } else if(action==SisterBoss.WALL||action==SisterBoss.PULL||action==SisterBoss.SPIKES){
                leftArm.xRot=-.44F*power;
                rightArm.xRot=-.25F*power;
                scythe.xRot=-.12F*power;
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
