package org.slavicmyths.client;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.sisters.SisterBoss;

/**
 * Hand-authored, fully three-dimensional cuboid rigs: bone cuirass, skeletal
 * war-staff, articulated hair/clothing; spectral smoke tail and iron lantern.
 * Floor is y=24 model units, so Gneteya's crown reaches ~5.5 Minecraft blocks.
 * No Steve / flat costume primitives are used.
 */
public final class SistersModel extends EntityModel<SisterBoss> {
    private final boolean spirit;
    private final ModelPart root,body,head,leftArm,rightArm,leftLeg,rightLeg,weapon,lantern;
    private final List<ModelPart> hair=new ArrayList<>(),skirts=new ArrayList<>(),smoke=new ArrayList<>();
    public SistersModel(boolean tryaseya) {
        spirit=tryaseya;
        FolkModelGeometry g=new FolkModelGeometry();
        root=g.part(256,256);
        if (!spirit) {
            body=part(g,root,0,-43,0);
            box(g,body,0,-13,-14,-8,26,29,16);
            box(g,body,0,-11,-17,-9,22,9,18);
            box(g,body,64,-14,-11,-10,28,4,4);
            box(g,body,64,-14,1,-10,28,4,4);
            // Individual ribs and sternum, visibly raised off the gown.
            for(int row=0;row<5;row++) for(int side:new int[]{-1,1}) {
                box(g,body,64,side<0?-13:3,-10+row*4,-11,10,2.2F,3);
                box(g,body,64,side<0?-14:11,-10+row*4,-9,3,4,8);
            }
            box(g,body,64,-2,-13,-13,4,26,4);
            // Independent shoulder arches; primitive, asymmetric bones instead of armor plates.
            for(int side:new int[]{-1,1}) {
                ModelPart shoulder=part(g,body,side*16,-9,0);
                box(g,shoulder,64,side<0?-8:0,-6,-9,8,9,18);
                box(g,shoulder,64,side<0?-10:6,-9,-9,4,15,6);
                box(g,shoulder,64,side<0?-8:2,-12,-7,6,6,7);
                for(int i=0;i<4;i++)box(g,shoulder,64,side<0?-8+i:3+i,-13-i*2,1+i*1.1F,2.3F,7,2.3F);
            }
            head=part(g,root,0,-65,-1);
            box(g,head,128,-10,-14,-9,20,19,17);
            box(g,head,128,-8,-5,-10,16,9,3);
            box(g,head,128,-2,-3,-12,4,5,3);
            for(int side:new int[]{-1,1}) {
                box(g,head,192,side<0?-6:3,-6,-11,3,2,1);
                box(g,head,64,side<0?-9:6,-17,-6,3,7,4);
            }
            // Incomplete vertebra crown, each irregular piece a real cuboid.
            for(int i=0;i<9;i++)box(g,head,64,-10+i*2.3F,-18-(i*7%11),-5+(i%3)*2,2.1F,6+(i%3)*2,2.3F);
            for(int i=0;i<13;i++) {
                ModelPart strand=part(g,head,-11+(i%7)*3,-14,(i<7?7:-8));
                box(g,strand,128,0,0,0,1.5F,21+(i*3%17),2.2F);
                hair.add(strand);
            }
            // Primitive damaged cloth skirt: layered volumetric panels, not texture planes.
            ModelPart skirt=part(g,root,0,-27,0);
            box(g,skirt,0,-14,0,-9,28,27,18);
            for(int i=0;i<12;i++) {
                float x=-19+i*3.35F;
                ModelPart panel=part(g,skirt,x,5,(i%2==0?-10:8));
                box(g,panel,i%3==0?0:48,0,0,0,3.5F,23+(i*5%6),2.5F);
                skirts.add(panel);
            }
            for(int i=0;i<7;i++)box(g,skirt,64,-13+i*4,-4,-12,2,8+(i%3)*2,2);
            // Separately animated long legs and segmented primitive bone guards.
            leftLeg=part(g,root,8,-16,0);rightLeg=part(g,root,-8,-16,0);
            for(ModelPart leg:new ModelPart[]{leftLeg,rightLeg}) {
                box(g,leg,0,-6,0,-5,12,22,10);
                box(g,leg,64,-7,4,-6,14,6,12);
                box(g,leg,64,-5,12,-7,10,8,14);
                box(g,leg,0,-6,19,-8,12,5,17);
                for(int j=0;j<4;j++)box(g,leg,64,-6+j*3,22,-9,2,2,8);
            }
            leftArm=part(g,root,19,-50,0);rightArm=part(g,root,-19,-50,0);
            for(ModelPart arm:new ModelPart[]{leftArm,rightArm}) {
                box(g,arm,0,-6,0,-6,12,25,12);
                box(g,arm,64,-8,0,-8,16,9,16);
                box(g,arm,64,-7,13,-9,14,11,17);
                box(g,arm,128,-5,23,-5,10,7,10);
                for(int j=0;j<4;j++)box(g,arm,128,-5+j*2.5F,28,-6,2,7,3);
                for(int j=0;j<5;j++)box(g,arm,64,-7+j*3,-8-j%3*2,-7,3,9,4);
            }
            // 3D staff is child of the right arm, pivots and actually follows the strike.
            weapon=part(g,rightArm,-2,25,-7);
            box(g,weapon,64,-3,-59,-3,6,79,6);
            for(int i=0;i<12;i++)box(g,weapon,64,-4,-54+i*6,-4,8,3.5F,8);
            box(g,weapon,64,-9,-68,-9,18,19,18);
            box(g,weapon,64,-6,-78,-6,12,12,12);
            for(int i=0;i<8;i++){
                float angle=(float)(i*Math.PI/4);
                box(g,weapon,64,(float)Math.cos(angle)*8-2,-77-i%3*4,(float)Math.sin(angle)*8-2,4,14,4);
            }
            lantern=part(g,root,0,0,0);
        } else {
            body=part(g,root,0,-38,0);
            box(g,192,-8,-9,-5,16,22,10,body);
            box(g,192,-11,4,-7,22,16,14,body);
            for(int i=0;i<9;i++)box(g,192,-11+i*2.6F,2,-8,2,19+(i*3%9),2,body);
            head=part(g,root,0,-53,0);
            box(g,head,128,-7,-12,-6,14,15,12);
            box(g,head,128,-6,-3,-8,12,5,3);
            box(g,head,192,-5,-5,-8,3,2,1);
            box(g,head,192,2,-5,-8,3,2,1);
            box(g,head,64,-7,-14,-5,14,3,10);
            for(int i=0;i<7;i++)box(g,head,64,-7+i*2.1F,-18-i%3*2,-5,1.3F,5,1.6F);
            for(int i=0;i<11;i++) {
                ModelPart strand=part(g,head,-8+(i%6)*3,-11,i%2==0?5:-5);
                box(g,strand,192,0,0,0,1.6F,17+(i*5%13),2);
                hair.add(strand);
            }
            leftArm=part(g,root,11,-39,0);rightArm=part(g,root,-11,-39,0);
            for(ModelPart arm:new ModelPart[]{leftArm,rightArm}) {
                box(g,arm,192,-4,0,-4,8,20,8);
                box(g,arm,192,-5,6,-5,10,9,10);
                box(g,arm,128,-2.5F,19,-3,5,7,6);
                for(int j=0;j<4;j++)box(g,arm,128,-3+j*1.6F,24,-3.5F,1.2F,6,2.4F);
                for(int j=0;j<3;j++)box(g,arm,64,-5+j*3,9,-5.5F,1.3F,8,1.5F);
            }
            // Unmistakably volumetric old iron lantern: metal roof, 4 bars, open center, swinging hook.
            lantern=part(g,rightArm,0,24,-4);
            box(g,lantern,128,-6,0,-6,12,3,12);
            box(g,lantern,128,-5,18,-5,10,3,10);
            box(g,lantern,128,-7,2,-7,14,2,14);
            box(g,lantern,128,-1,-7,-1,2,8,2);
            box(g,lantern,128,-4,-9,-1,8,2,2);
            for(int x:new int[]{-6,4})for(int z:new int[]{-6,4})box(g,lantern,128,x,3,z,2,15,2);
            box(g,lantern,192,-3,7,-3,6,9,6);
            box(g,lantern,192,-2,5,-2,4,13,4);
            leftLeg=part(g,root,0,0,0);rightLeg=part(g,root,0,0,0);
            weapon=part(g,root,0,0,0);
            // Lower body consists of separated animated smoke voxel plates and tapered tendrils: NO LEGS.
            for(int ring=0;ring<6;ring++) {
                float radius=9-ring*1.3F;
                for(int i=0;i<6;i++) {
                    float a=(float)(i*Math.PI/3+ring*.44);
                    ModelPart wisp=part(g,root,(float)Math.cos(a)*radius,-18+ring*7,(float)Math.sin(a)*radius);
                    box(g,wisp,192,-2,0,-2,4.2F,10+(i%3)*4,4);
                    smoke.add(wisp);
                }
            }
            for(int i=0;i<8;i++) {
                ModelPart tendril=part(g,root,(i-4)*1.7F,2,(i%2==0?5:-5));
                box(g,tendril,192,-1,0,-1,2,18+(i%4)*3,2);
                smoke.add(tendril);
            }
            skirts.add(body);
        }
    }
    private static ModelPart part(FolkModelGeometry g,ModelPart parent,float x,float y,float z) {
        ModelPart p=g.part(256,256);
        p.setPos(x,y,z);
        g.attach(parent,p);
        return p;
    }
    private static void box(FolkModelGeometry g,ModelPart p,int u,float x,float y,float z,float w,float h,float d) { g.box(p,u,0,x,y,z,w,h,d); }
    private static void box(FolkModelGeometry g,int u,float x,float y,float z,float w,float h,float d,ModelPart p) {g.box(p,u,0,x,y,z,w,h,d);}
    @Override public void setupAnim(SisterBoss entity,float walk,float stride,float age,float yaw,float pitch) {
        float swing=Mth.cos(walk*.6662F)*Mth.clamp(stride,0,1);
        head.yRot=yaw*((float)Math.PI/180F);
        head.xRot=pitch*((float)Math.PI/180F);
        body.xRot=.015F*Mth.sin(age*.09F);
        body.zRot=0;body.yRot=0;
        leftArm.xRot=swing*.36F;rightArm.xRot=-swing*.36F;
        leftArm.zRot=0;rightArm.zRot=0;
        if (!spirit) {
            leftLeg.xRot=-swing*.42F;rightLeg.xRot=swing*.42F;
            weapon.xRot=0;weapon.zRot=.10F;
        } else {
            lantern.xRot=.1F*Mth.sin(age*.13F);
            lantern.zRot=.16F*Mth.sin(age*.095F);
            for(int i=0;i<smoke.size();i++) {
                ModelPart p=smoke.get(i);
                p.yRot=.25F*Mth.sin(age*.075F+i*.53F);
                p.zRot=.14F*Mth.sin(age*.12F+i*.7F);
                p.xRot=.12F*Mth.sin(age*.09F+i*.4F);
            }
        }
        for(int i=0;i<hair.size();i++) {
            hair.get(i).xRot=.05F*Mth.sin(age*.06F+i*.67F);
            hair.get(i).zRot=.025F*Mth.sin(age*.08F+i*.32F);
        }
        for(int i=0;i<skirts.size();i++)skirts.get(i).xRot=.03F*Mth.sin(age*.12F+i*.39F);
        float f=entity.actionFrame(),wind=Mth.clamp(f/23F,0,1),recovery=Mth.clamp((f-26)/15F,0,1);
        if(entity.action()!=SisterBoss.IDLE) {
            switch(entity.action()) {
                case SisterBoss.HEAVY -> {rightArm.xRot=-2.5F*wind+1.7F*recovery;rightArm.zRot=-.5F;body.yRot=.12F*Mth.sin(wind*Mth.PI);}
                case SisterBoss.SLAM -> {rightArm.xRot=-3.0F*wind+2.2F*recovery;leftArm.xRot=-1.3F*wind;body.xRot=.30F*recovery;}
                case SisterBoss.WALL,SisterBoss.SPIKES,SisterBoss.PULL -> {rightArm.xRot=-2.3F*wind;leftArm.xRot=-.6F*wind;weapon.zRot=.3F*Mth.sin(wind*Mth.PI);}
                case SisterBoss.FEVER,SisterBoss.FLASH,SisterBoss.SPIRITS,SisterBoss.ZONE -> {rightArm.xRot=-1.8F*wind;leftArm.xRot=-.9F*wind;lantern.xRot=-.8F*wind;lantern.zRot=.28F*Mth.sin(wind*Mth.PI);}
                case SisterBoss.EVADE -> {body.yRot=.8F*Mth.sin(wind*Mth.PI);leftArm.zRot=-.75F;rightArm.zRot=.75F;}
                default -> { }
            }
        }
        if(entity.enraged()) {
            body.zRot=.025F*Mth.sin(age*.27F);
            if(spirit)lantern.zRot+=.13F*Mth.sin(age*.45F);
        }
        if(entity.hurtTime>0)body.xRot-=.12F;
        if(entity.deathTime>0)body.zRot+=Mth.clamp(entity.deathTime/20F,0,1)*.45F;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color) {root.render(pose,out,light,overlay,color);}
}
