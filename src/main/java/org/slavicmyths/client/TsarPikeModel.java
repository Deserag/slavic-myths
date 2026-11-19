package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.marine.TsarPikeEntity;

/** Approved V1 pike anatomy: separate skull/jaw, tapering body rings and volumetric fins.
 * All components are genuine ModelPart cuboids (not a resized vanilla fish).
 */
public final class TsarPikeModel extends EntityModel<TsarPikeEntity> {
    private static final int DARK=0xFF293C30,BACK=0xFF405C3D,OLIVE=0xFF667A47,
        FLANK=0xFF869263,BELLY=0xFFB2AC85,SPOT=0xFFC4BB78,
        SKULL=0xFF3C5540,LIP=0xFF7E8561,JAW=0xFF8D8C6C,
        MOUTH=0xFF653A39,GUM=0xFFB97E6E,TEETH=0xFFF1E5C3,
        IRIS=0xFFD5A554,PUPIL=0xFF101D19,FIN=0xFF684D36,
        FINLIGHT=0xFF99764C,RIB=0xFFB28C56,SCAR=0xFFB9B38B;
    private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final Group root,head,jaw,tailA,tailB,caudal,pectL,pectR,pelvL,pelvR;

    private final class Group {
        final ModelPart pivot=geometry.part(256,256);
        final Map<Integer,ModelPart> materials=new LinkedHashMap<>();
        final List<Group> children=new ArrayList<>();
        final float worldX,worldY,worldZ;
        Group(Group parent,float x,float y,float z){
            worldX=x;worldY=y;worldZ=z;
            if(parent==null)pivot.setPos(x,y,z);
            else {pivot.setPos(x-parent.worldX,y-parent.worldY,z-parent.worldZ);parent.children.add(this);}
        }
        void box(int color,float x,float y,float z,float w,float h,float d){
            ModelPart material=materials.computeIfAbsent(color,c->geometry.part(256,256,0,0));
            geometry.box(material,x-worldX,y-worldY,z-worldZ,w,h,d);
        }
        void draw(PoseStack pose,VertexConsumer buffer,int light,int overlay){
            pose.pushPose();
            pivot.translateAndRotate(pose);
            for(var entry:materials.entrySet())
                entry.getValue().render(pose,buffer,light,overlay,entry.getKey());
            for(Group child:children)child.draw(pose,buffer,light,overlay);
            pose.popPose();
        }
    }

    public TsarPikeModel(){
        root=new Group(null,0,-1,0);
        head=new Group(root,0,-1,-12);
        jaw=new Group(head,0,-1,-14);
        tailA=new Group(root,0,-1,21);
        tailB=new Group(tailA,0,-1,43);
        caudal=new Group(tailB,0,-1,59);
        pectL=new Group(root,-11,-1,-1);
        pectR=new Group(root,11,-1,-1);
        pelvL=new Group(tailA,-9,-1,29);
        pelvR=new Group(tailA,9,-1,29);
        build();
    }
    private static float interpolate(float z,float[] positions,float[] values){
        for(int i=1;i<positions.length;i++)if(z<=positions[i]){
            float factor=(z-positions[i-1])/(positions[i]-positions[i-1]);
            return Mth.lerp(Mth.clamp(factor,0F,1F),values[i-1],values[i]);
        }
        return values[values.length-1];
    }
    private static final float[] BODY_Z={-13,-8,2,12,22,33,44,52,61};
    private static final float[] BODY_W={10.5F,12.6F,16.6F,17.6F,16.1F,13.7F,9.7F,6F,3.5F};
    private static final float[] BODY_H={8.7F,10.5F,12.9F,13.2F,12.3F,10.8F,7.7F,5.1F,3.3F};
    private void build(){
        // Ring-by-ring anatomy: broad shoulders, narrowing waist and tail peduncle.
        for(float z=-12;z<61;z+=2.8F){
            float width=interpolate(z,BODY_Z,BODY_W),height=interpolate(z,BODY_Z,BODY_H);
            Group section=z<21?root:z<43?tailA:tailB;
            for(float x=-width;x<=width;x+=2.8F)for(float y=-height;y<=height;y+=2.8F){
                double radial=(x*x)/(width*width)+(y*y)/(height*height);
                if(radial>1)continue;
                int color=y<-.48F*height?DARK:y<-.15F*height?BACK:
                    y<.55F*height?(Math.floorMod((int)(z*2+x*5),5)==0?FLANK:OLIVE):BELLY;
                section.box(color,x-1.4F,y-1.4F,z,2.8F,2.8F,2.8F);
            }
            // Distinct raised scales and camouflage spots, on both flanks.
            if((int)z%4<2)for(int side:new int[]{-1,1}){
                for(float y=-height*.65F;y<height*.6F;y+=4.8F){
                    float x=side*(width*(float)Math.sqrt(Math.max(0,1-(y*y)/(height*height)))+.1F);
                    section.box((int)(z+y)%3==0?SPOT:FLANK,x-1,y-1.1F,z,2F,2.1F,1.8F);
                }
            }
        }
        // Wide skull narrowing into the flattened, duck-billed snout.
        float[] hz={-62,-58,-51,-44,-36,-29,-21,-14,-9};
        float[] hw={5F,6.7F,8F,8F,9F,11.6F,12.8F,12.4F,11F};
        float[] hh={2.2F,3.2F,3.9F,4.2F,5.1F,7.3F,9.4F,10F,9.2F};
        for(float z=-61;z< -9;z+=2.5F){
            float width=interpolate(z,hz,hw),height=interpolate(z,hz,hh);
            for(float x=-width;x<=width;x+=2.5F)for(float y=-height;y<=height;y+=2.5F){
                if((x*x)/(width*width)+(y*y)/(height*height)>1)continue;
                if(z< -24 && y>1.4F)continue; // hollow mouth, not a painted cavity
                int color=y< -height*.36F?SKULL:y<0?BACK:MOUTH;
                head.box(color,x-1.25F,y-1.25F,z,2.5F,2.5F,2.5F);
            }
            // Separate fleshy lower jaw, articulated from hinge at z=-14.
            float jWidth=interpolate(z,new float[]{-62,-53,-43,-33,-24,-14},
                new float[]{4.9F,6.6F,7.8F,9F,10.2F,10.7F});
            for(float x=-jWidth;x<=jWidth;x+=2.5F)
                for(float y=5.2F;y<=10.5F;y+=2.5F){
                    if(Math.abs(x)>jWidth*(y>8?.9F:1F))continue;
                    jaw.box(y>8?JAW:GUM,x-1.25F,y-1.25F,z,2.5F,2.5F,2.5F);
                }
        }
        // Two distinct tooth rows on the lips and an auxiliary palatal row.
        for(int i=0;i<10;i++){
            float z=-57+i*4.5F;
            float width=interpolate(z,hz,hw)*.82F;
            for(int side:new int[]{-1,1}){
                float x=side*width;
                float length=i%3==0?4.3F:3F;
                head.box(TEETH,x-.95F,1.0F,z,1.9F,2.4F,1.9F);
                head.box(TEETH,x-.44F,3.2F,z+.22F,.88F,length*.7F,.9F);
                jaw.box(TEETH,x*.93F-1F,3.9F,z,2F,2.4F,2F);
                jaw.box(TEETH,x*.93F-.46F,2.0F,z,.92F,2.4F,.92F);
            }
            for(int side:new int[]{-1,1})
                head.box(TEETH,side*width*.33F-.65F,1.5F,z+1.5F,1.3F,2.1F,1.3F);
        }
        for(int side:new int[]{-1,1}){
            float x=side*12.7F;
            head.box(SKULL,x-3.5F,-9.1F,-30,7,5,7);
            head.box(IRIS,x-2.9F,-8.4F,-29,5.8F,4.1F,2F);
            head.box(PUPIL,x-1.6F,-8.2F,-29.2F,3.2F,3.8F,2.2F);
            head.box(TEETH,x-.5F,-8.4F,-29.7F,.9F,1F,1F);
            for(float z=-17;z< -10;z+=3)
                head.box(SCAR,side*12.4F-1,-4,z,2F,8,1.4F);
            head.box(DARK,side*5F-.9F,-3.5F,-54,1.8F,1.3F,2F);
        }
        for(float z=12;z<20;z+=3)
            root.box(SCAR,-16,-4+(z-12)*.18F,z,2,2,3);
        // Extruded dorsal fin and anal fin, visibly thick, from swept volume layers.
        for(float z=32;z<57;z+=2.2F){
            float dorsalHeight=z<43?10+(z-32)*1.6F:Math.max(0,27-(z-43)*1.8F);
            for(float h=10;h<dorsalHeight;h+=2.2F)
                tailA.box(((int)(z+h)%7==0)?RIB:FIN, -2.3F,-h,z,4.6F,2.4F,2.2F);
        }
        for(float z=42;z<60;z+=2.2F){
            float analHeight=z<49?8+(z-42)*2.2F:Math.max(7,23-(z-49)*1.4F);
            for(float h=7;h<analHeight;h+=2.2F)
                tailB.box((int)z%4==0?RIB:FINLIGHT,-2.2F,h,z,4.4F,2.4F,2.2F);
        }
        // Forked caudal fan with separate upper and lower lobes.
        for(float z=59;z<74;z+=2.2F){
            float spread=(z-59)*1.25F+3;
            for(int sign:new int[]{-1,1}){
                float centerY=sign*spread*.58F;
                for(float y=centerY-spread*.40F;y<centerY+spread*.40F;y+=2.2F)
                    caudal.box(((int)(z+y)%6)==0?RIB:FIN,-2.4F,y,z,4.8F,2.4F,2.2F);
            }
        }
        // Paired pectoral and pelvic fins, with depth and ribbing.
        for(int sign:new int[]{-1,1}){
            Group p=sign<0?pectL:pectR, v=sign<0?pelvL:pelvR;
            for(float z=-9;z<14;z+=2.5F)for(float across=0;across<18;across+=2.5F){
                if(across>5+(z+9)*.52F)continue;
                float x=sign*(11+across);
                p.box((int)(z+across)%5==0?RIB:FINLIGHT,x-2.3F,
                    5+across*.23F,z,4.6F,3.2F,2.5F);
            }
            for(float z=21;z<44;z+=2.5F)for(float across=0;across<13;across+=2.5F){
                if(across>4+(z-21)*.45F)continue;
                float x=sign*(9+across);
                v.box((int)(z+across)%5==0?RIB:FIN,x-2.1F,
                    8+across*.18F,z,4.2F,3F,2.5F);
            }
        }
    }

    @Override public void setupAnim(TsarPikeEntity fish,float limbSwing,float limbAmount,
            float age,float yaw,float pitch){
        float time=age;
        float speed=fish.action()==TsarPikeEntity.FAST||fish.action()==TsarPikeEntity.GRAB?2.2F:1F;
        float amp=fish.action()==TsarPikeEntity.FAST||fish.action()==TsarPikeEntity.GRAB?.48F:.19F;
        tailA.pivot.yRot=Mth.sin(time*.13F*speed)*amp*.6F;
        tailB.pivot.yRot=Mth.sin(time*.13F*speed-1.1F)*amp;
        caudal.pivot.yRot=Mth.sin(time*.13F*speed-1.9F)*amp*1.6F;
        // The tail strike is a separate wide lateral sweep, not ordinary swim motion.
        if(fish.action()==TsarPikeEntity.TAIL_SWEEP){
            float progress=Mth.clamp(fish.actionTicks()/19F,0F,1F);
            float sweep=Mth.sin(progress*Mth.PI)*1.18F;
            tailA.pivot.yRot+=sweep*.40F;
            tailB.pivot.yRot+=sweep*.80F;
            caudal.pivot.yRot+=sweep*1.15F;
        }
        pectL.pivot.zRot=Mth.sin(time*.16F)*.16F;
        pectR.pivot.zRot=-pectL.pivot.zRot;
        pelvL.pivot.zRot=Mth.sin(time*.12F+.7F)*.12F;
        pelvR.pivot.zRot=-pelvL.pivot.zRot;
        float open=fish.jawOpen();
        jaw.pivot.xRot=open*.72F;
        head.pivot.yRot=fish.action()==TsarPikeEntity.SHAKE?
            Mth.sin(time*.8F)*.32F:0F;
        root.pivot.xRot=fish.action()==TsarPikeEntity.LEAP?-.18F:0F;
        root.pivot.zRot=0F;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){
        root.draw(pose,out,light,overlay);
    }
    /** Reuse the *actual* articulated boss skull and teeth for the 1x2 trophy. */
    public void renderHead(PoseStack pose,VertexConsumer out,int light,int overlay,float time,boolean active){
        float open=active?.40F+.30F*Math.max(0,Mth.sin(time*.42F)):
            .055F+.05F*Mth.sin(time*.08F);
        jaw.pivot.xRot=open;
        head.pivot.yRot=active?Mth.sin(time*.11F)*.07F:0F;
        pose.pushPose();
        root.pivot.translateAndRotate(pose);
        head.draw(pose,out,light,overlay);
        pose.popPose();
    }
}
