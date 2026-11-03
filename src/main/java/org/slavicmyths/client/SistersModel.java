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
 * Independent Java voxel rigs for the paired bosses. Coordinates are in 1/16
 * blocks: model feet end at y=24; crowns reach y=-61 (Gneteya, 5.3 blocks)
 * and y=-39 (Tryaseya, 3.9 blocks). No flat costume or item quads.
 *
 * Atlas (512px): 0-127 woven/torn cloth; 128-255 bone;
 * 256-383 skin; 384-479 hair/iron/smoke; 480-511 emissive UV.
 * The matching transparent emissive pass affects only eyes/lantern core.
 */
public final class SistersModel extends EntityModel<SisterBoss> {
    private static final int CLOTH=0, HEM=16, BONE=128, SKIN=256, DARK=384, GLOW=480;
    private final boolean spirit;
    private final ModelPart root, body, head, leftArm, rightArm, leftLeg, rightLeg, weapon, lantern;
    private final List<ModelPart> hair=new ArrayList<>(), hems=new ArrayList<>(), smoke=new ArrayList<>();

    public SistersModel(boolean tryaseya) {
        spirit=tryaseya;
        FolkModelGeometry g=new FolkModelGeometry();
        root=g.part(512,512);
        if (!spirit) {
            body=part(g,root,0,-25,0);
            box(g,body,CLOTH,-5,-13,-5,10,27,10);
            box(g,body,HEM,-5,8,-6,10,6,12);
            box(g,body,BONE,-2,-12,-7,4,19,2);
            // Exposed, curved rib cage built from individually raised left/right ribs.
            for (int r=0;r<5;r++) for (int s:new int[]{-1,1}) {
                box(g,body,BONE,s<0?-12:3,-10+r*4,-10,9,2.2F,4);
                box(g,body,BONE,s<0?-13:10,-9+r*4,-8,3,3,10);
            }
            // Six projecting shoulder quills, three on each pauldron.
            for (int s:new int[]{-1,1}) {
                ModelPart shoulder=part(g,body,s*15.75F,-9,0);
                box(g,shoulder,BONE,s<0?-10.5F:0,-5,-9,10.5F,8,18);
                box(g,shoulder,BONE,s<0?-11:7,-10,-8,4,13,6);
                for (int j=0;j<3;j++) {
                    ModelPart quill=part(g,shoulder,s*(5+j*2),-9-j,2+j*2);
                    box(g,quill,BONE,-1,-6,-1,2,4,2);
                    box(g,quill,BONE,-1,-10,-1,2,4,2);
                }
            }
            // Raised dorsal vertebrae are visible from behind, not on the chest.
            for (int i=0;i<7;i++) {
                box(g,body,BONE,-2,-11+i*3.4F,6,4,3,4+i%2);
                box(g,body,BONE,-1,-13+i*3.4F,9,2,5,2);
            }
            head=part(g,root,0,-42,-1);
            box(g,head,DARK,-6,-10,-6,12,14,12);
            // Rigid overlapping skull plates frame black recessed sockets.
            box(g,head,BONE,-7,-11,-9,14,3,4);
            box(g,head,BONE,-7,-8,-9,2,11,3);
            box(g,head,BONE,5,-8,-9,2,11,3);
            box(g,head,BONE,-5,1,-9,10,4,3);
            box(g,head,BONE,-2,-6,-10,4,7,3);
            box(g,head,DARK,-5,-7,-10,4,4,1);
            box(g,head,DARK,1,-7,-10,4,4,1);
            box(g,head,GLOW,-4,-6,-11,2,2,1,128);
            box(g,head,GLOW,2,-6,-11,2,2,1,128);
            // Asymmetric crown of cracked vertebrae; real protruding silhouettes.
            // Jagged horn crown, several independent projecting bone spires.
            for (int i=0;i<7;i++) {
                float hornX=-9+i*3;
                ModelPart horn=part(g,head,hornX,-12,i%2==0?-3:4);
                box(g,horn,BONE,-1.5F,-5,0,3,8,3);
                box(g,horn,BONE,-1,-11,-.5F,2,7,2);
                box(g,horn,BONE,-.5F,-15,-.5F,1,5,1);
            }
            box(g,head,BONE,-9,-13,-7,18,3,14);
            for (int i=0;i<4;i++) {
                ModelPart strand=part(g,head,-6+i*4,-6,7);
                box(g,strand,DARK,-2,0,-1,4,18,2);
                hair.add(strand);
            }
            ModelPart skirt=part(g,root,0,-12,0);
            box(g,skirt,CLOTH,-7.75F,0,-7,15.5F,26,14);
            box(g,skirt,HEM,-9.75F,9,-8,19.5F,15,16);
            for (int i=0;i<12;i++) {
                ModelPart panel=part(g,skirt,-9+i*1.6F,8,i%2==0?-11:9);
                box(g,panel,i%3==0?HEM:CLOTH,0,0,0,3,18+(i*5%6),3);
                hems.add(panel);
            }
            for (int i=0;i<7;i++)box(g,skirt,BONE,-13+i*4,-3,-11,2,6+i%3*3,2);
            leftLeg=part(g,root,8,-4,0);
            rightLeg=part(g,root,-8,-4,0);
            for (ModelPart leg:new ModelPart[]{leftLeg,rightLeg}) {
                box(g,leg,CLOTH,-4,0,-4,8,22,8);
                box(g,leg,BONE,-6,5,-6,12,7,12);
                box(g,leg,BONE,-6,14,-6,12,5,12);
                box(g,leg,DARK,-6,22,-8,12,6,16);
            }
            leftArm=part(g,root,18.5F,-33,0);
            rightArm=part(g,root,-18.5F,-33,0);
            for (ModelPart arm:new ModelPart[]{leftArm,rightArm}) {
                box(g,arm,CLOTH,-5,0,-5,10,26,10);
                box(g,arm,BONE,-8.75F,0,-8,17.5F,8,16);
                box(g,arm,BONE,-7,14,-7,14,9,14);
                box(g,arm,SKIN,-4,24,-4,8,7,8);
                for (int j=0;j<4;j++)box(g,arm,SKIN,-4+j*2,29,-5,1.7F,6,2.5F);
            }
            // Obsidian scythe: 48-pixel continuous shaft and stepped bone hook.
            weapon=part(g,rightArm,-1,27,-7);
            box(g,weapon,DARK,-1.5F,-46,-1.5F,3,48,3);
            box(g,weapon,BONE,-2.5F,-47,-2.5F,5,3,5);
            for (int i=0;i<5;i++) {
                box(g,weapon,BONE,1+i*4,-48+i*3,-2,9-i,3,4);
            }
            box(g,weapon,BONE,17,-34,-2,2,6,3);
            lantern=part(g,root,0,0,0);
        } else {
            body=part(g,root,0,-19,0);
            box(g,body,CLOTH,-3.25F,-11,-3.5F,6.5F,22,7);
            box(g,body,HEM,-5,1,-5,10,13,10);
            for (int i=0;i<9;i++)box(g,body,i%3==0?BONE:HEM,-5+i*1.1F,4,-5,1.7F,14+(i*3%5),2);
            head=part(g,root,0,-27,0);
            // Dark face cavity: only two cold-blue emissive eyes are visible.
            box(g,head,DARK,-5,-9,-5,10,14,10);
            box(g,head,DARK,-5,-5,-6,10,8,1);
            box(g,head,GLOW,-3,-3,-7,2,1,1,128);
            box(g,head,GLOW,1,-3,-7,2,1,1,128);
            // Frayed indigo hood, with deep brow and cheek plates.
            box(g,head,CLOTH,-6,-12,-6,12,3,12);
            box(g,head,CLOTH,-7,-11,-7,2,14,12);
            box(g,head,CLOTH,5,-11,-7,2,14,12);
            box(g,head,HEM,-6,-11,-7,12,2,2);
            for (int i=0;i<7;i++)box(g,head,DARK,-5+i*1.5F,-10-i%3*2,-6,1,5,2);
            for (int i=0;i<12;i++) {
                ModelPart strand=part(g,head,-5+(i%6)*2,-9,i<8?5:-6);
                box(g,strand,DARK,-.5F,0,-.5F,1,20+(i*7%15),1);
                hair.add(strand);
            }
            // Face-crossing locks prevent the bald forehead silhouette.
            for (int i=0;i<4;i++) {
                ModelPart lock=part(g,head,-5+i*2.5F,-11,-6.2F);
                box(g,lock,DARK,0,0,0,1,16+i%2*5,1);
                hair.add(lock);
            }
            leftArm=part(g,root,11,-20,0);
            rightArm=part(g,root,-11,-20,0);
            for (ModelPart arm:new ModelPart[]{leftArm,rightArm}) {
                box(g,arm,HEM,-2,0,-2,4,17,4);
                box(g,arm,BONE,-2.5F,7,-2.5F,5,5,5);
                box(g,arm,SKIN,-2,16,-2,4,6,4);
                for (int j=0;j<4;j++)box(g,arm,SKIN,-2+j*1.1F,20,-3,1,4,2);
            }
            // Floating 4x4x4 plague catalyst, three pixels over the raised right palm.
            lantern=part(g,rightArm,0,17,-5);
            box(g,lantern,GLOW,-2,-7,-2,4,4,4,128);
            for (int s:new int[]{-1,1}) {
                box(g,lantern,DARK,s<0?-3:2,-8,-3,1,6,6);
                box(g,lantern,DARK,-3,-8,s<0?-3:2,6,6,1);
            }
            weapon=part(g,root,0,0,0);
            leftLeg=part(g,root,0,0,0);
            rightLeg=part(g,root,0,0,0);
            // Hollow, shrinking 4-sided vapor shell, never any leg cuboids.
            for (int ring=0;ring<6;ring++) {
                float radius=7-ring*1.2F;
                float side=radius*2;
                float y=-7+ring*5F;
                ModelPart shell=part(g,root,0,y,0);
                box(g,shell,DARK,-radius,-radius/2, -radius,side,1.2F,1);
                box(g,shell,DARK,-radius,-radius/2,radius-1,side,1.2F,1);
                box(g,shell,DARK,-radius,-radius/2,-radius,1,1.2F,side);
                box(g,shell,DARK,radius-1,-radius/2,-radius,1,1.2F,side);
                smoke.add(shell);
            }
            // The final hollow lip terminates at a 2x2-pixel silhouette.
            ModelPart tip=part(g,root,0,23,0);
            box(g,tip,DARK,-1,0,-1,2,2,2);
            smoke.add(tip);
        }
    }

    private static ModelPart part(FolkModelGeometry g,ModelPart parent,float x,float y,float z) {
        ModelPart p=g.part(512,512);
        p.setPos(x,y,z);
        g.attach(parent,p);
        return p;
    }
    private static void box(FolkModelGeometry g,ModelPart p,int u,float x,float y,float z,float w,float h,float d) {g.box(p,u,0,x,y,z,w,h,d);}
    private static void box(FolkModelGeometry g,ModelPart p,int u,float x,float y,float z,float w,float h,float d,int v) {g.box(p,u,v,x,y,z,w,h,d);}

    @Override public void setupAnim(SisterBoss boss,float walk,float stride,float age,float yaw,float pitch) {
        float swing=Mth.cos(walk*.6662F)*Mth.clamp(stride,0,1);
        head.yRot=yaw*Mth.DEG_TO_RAD;
        head.xRot=pitch*Mth.DEG_TO_RAD;
        root.setPos(0,spirit?-2.5F+(float)Math.sin(age*.08F)*2.5F:0,0);
        body.xRot=.018F*Mth.sin(age*.09F);
        body.zRot=0;body.yRot=0;
        leftArm.xRot=swing*.36F;rightArm.xRot=-swing*.36F;
        leftArm.zRot=0;rightArm.zRot=0;
        weapon.xRot=0;weapon.zRot=0;lantern.xRot=0;lantern.zRot=0;
        if (!spirit) {
            leftLeg.xRot=-swing*.43F;rightLeg.xRot=swing*.43F;
            // Defensive two-handed grip converges both gauntlets on the scythe haft.
            leftArm.xRot=-.95F-swing*.12F;
            rightArm.xRot=-1.05F+swing*.12F;
            leftArm.zRot=.48F;
            rightArm.zRot=-.48F;
            weapon.zRot=-.1F;
        } else {
            lantern.xRot=.12F*Mth.sin(age*.14F);
            lantern.zRot=.19F*Mth.sin(age*.095F);
            for (int i=0;i<smoke.size();i++) {
                ModelPart p=smoke.get(i);
                p.yRot=.09F*Mth.sin(age*.07F+i*.51F);
                p.zRot=.06F*Mth.sin(age*.1F+i*.73F);
                p.xRot=.06F*Mth.sin(age*.085F+i*.47F);
            }
        }
        for (int i=0;i<hair.size();i++) {
            hair.get(i).xRot=.07F*Mth.sin(age*.06F+i*.67F);
            hair.get(i).zRot=.05F*Mth.sin(age*.08F+i*.32F);
        }
        for (int i=0;i<hems.size();i++)hems.get(i).xRot=.04F*Mth.sin(age*.12F+i*.39F);
        float f=boss.actionFrame(),wind=Mth.clamp(f/23F,0,1),recovery=Mth.clamp((f-26)/15F,0,1);
        switch (boss.action()) {
            case SisterBoss.HEAVY -> {rightArm.xRot=-2.5F*wind+1.7F*recovery;rightArm.zRot=-.5F;body.yRot=.12F*Mth.sin(wind*Mth.PI);}
            case SisterBoss.SLAM -> {rightArm.xRot=-3F*wind+2.2F*recovery;leftArm.xRot=-1.3F*wind;body.xRot=.3F*recovery;}
            case SisterBoss.WALL,SisterBoss.SPIKES,SisterBoss.PULL -> {rightArm.xRot=-2.3F*wind;leftArm.xRot=-.6F*wind;weapon.zRot=.3F*Mth.sin(wind*Mth.PI);}
            case SisterBoss.FEVER,SisterBoss.FLASH,SisterBoss.SPIRITS,SisterBoss.ZONE -> {rightArm.xRot=-1.8F*wind;leftArm.xRot=-.9F*wind;lantern.xRot=-.8F*wind;lantern.zRot=.28F*Mth.sin(wind*Mth.PI);}
            case SisterBoss.EVADE -> {body.yRot=.8F*Mth.sin(wind*Mth.PI);leftArm.zRot=-.75F;rightArm.zRot=.75F;}
            default -> { }
        }
        if (boss.enraged()) {
            body.zRot=.025F*Mth.sin(age*.27F);
            if (spirit)lantern.zRot+=.13F*Mth.sin(age*.45F);
        }
        if (boss.hurtTime>0)body.xRot-=.12F;
        if (boss.deathTime>0)body.zRot+=Mth.clamp(boss.deathTime/20F,0,1)*.45F;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color) {root.render(pose,out,light,overlay,color);}
}
