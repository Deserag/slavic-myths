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
        MOUTH=0xFF532D30,GUM=0xFF845A55,TEETH=0xFFF1E5C3,
        IRIS=0xFFD5A554,PUPIL=0xFF101D19,FIN=0xFF5C7049,
        FINLIGHT=0xFF879360,RIB=0xFFB4AD79,SCAR=0xFFB9B38B;
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
    private static final float[] BODY_W={8.6F,10.2F,12.8F,14F,12.8F,10.2F,7.3F,4.4F,2.4F};
    private static final float[] BODY_H={7.2F,8.8F,10.7F,11.8F,10.8F,8.5F,5.6F,3.6F,2.3F};
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
            if(Math.floorMod((int)(z*2.0F),7)<2)for(int side:new int[]{-1,1}){
                for(float y=-height*.65F;y<height*.6F;y+=4.8F){
                    float x=side*(width*(float)Math.sqrt(Math.max(0,1-(y*y)/(height*height)))+.1F);
                    section.box(Math.floorMod((int)(z+y),3)==0?SPOT:FLANK,x-.7F,y-.7F,z,1.4F,1.5F,1.3F);
                }
            }
        }
        // Wide skull narrowing into the flattened, duck-billed snout.
        float[] hz={-62,-58,-51,-44,-36,-29,-21,-14,-9};
        float[] hw={4.4F,4.8F,5.4F,6F,6.7F,8.2F,9.6F,10.2F,9.2F};
        float[] hh={2F,2.1F,2.3F,2.7F,3.5F,4.7F,6.3F,7.5F,7.7F};
        for(float z=-61;z< -9;z+=2.5F){
            float width=interpolate(z,hz,hw),height=interpolate(z,hz,hh);
            for(float x=-width;x<=width;x+=2.5F)for(float y=-height;y<=height;y+=2.5F){
                if((x*x)/(width*width)+(y*y)/(height*height)>1)continue;
                if(z< -24 && y>1.4F)continue; // thin upper jaw; visible void above the articulated mandible
                int color=y< -height*.4F?SKULL:y<height*.35F?BACK:BELLY;
                head.box(color,x-1.25F,y-1.25F,z,2.5F,2.5F,2.5F);
            }
            // Separate fleshy lower jaw, articulated from hinge at z=-14.
            float jWidth=interpolate(z,new float[]{-62,-53,-43,-33,-24,-14},
                new float[]{4.2F,4.6F,5.3F,6.1F,7.5F,8.6F});
            for(float x=-jWidth;x<=jWidth;x+=2.5F)
                for(float y=3F;y<=8F;y+=2.5F){
                    if(Math.abs(x)>jWidth*(y>5F?.9F:1F))continue;
                    jaw.box(y>5F?JAW:GUM,x-1.25F,y-1.25F,z,2.5F,2.5F,2.5F);
                }
        }
        // Two distinct tooth rows on the lips and an auxiliary palatal row.
        for(int i=0;i<10;i++){
            float z=-57+i*4.5F;
            float width=interpolate(z,hz,hw)*.82F;
            for(int side:new int[]{-1,1}){
                float x=side*width;
                float length=i%3==0?3F:2.2F;
                head.box(TEETH,x-.95F,1.0F,z,1.9F,2.4F,1.9F);
                head.box(TEETH,x-.44F,3.2F,z+.22F,.88F,length*.7F,.9F);
                jaw.box(TEETH,x*.93F-1F,3.9F,z,2F,2.4F,2F);
                jaw.box(TEETH,x*.93F-.46F,2.0F,z,.92F,2.4F,.92F);
            }
            for(int side:new int[]{-1,1})
                head.box(TEETH,side*width*.33F-.65F,1.5F,z+1.5F,1.3F,2.1F,1.3F);
        }
        for(int side:new int[]{-1,1}){
            float x=side*8.4F;
            // Lateral recessed eyes, rather than tall crocodile-like eye turrets.
            head.box(SKULL,x-2.2F,-5.9F,-27.5F,4.4F,3.1F,4.5F);
            head.box(IRIS,x-1.5F,-5.5F,-27.2F,3F,2.45F,1.1F);
            head.box(PUPIL,x-.8F,-5.35F,-27.5F,1.6F,2.15F,1.15F);
            head.box(TEETH,x-.3F,-5.45F,-27.8F,.6F,.65F,.7F);
            for(float z=-18;z< -11;z+=3)
                head.box(SCAR,side*9.3F-.65F,-3.0F,z,1.3F,5.6F,.9F);
            head.box(DARK,side*5F-.9F,-3.5F,-54,1.8F,1.3F,2F);
        }
        // Actual pike anatomy: long wide duckbill, nostril pairs, exposed
        // tooth beds, and armored opercula extending behind the eye line.
        // Every detail uses the same articulated skull as the living fish.
        for(int side:new int[]{-1,1}){
            for(int ring=0;ring<6;ring++){
                float z=-24F+ring*2.6F;
                float width=interpolate(z,hz,hw);
                head.box(ring%3==0?SPOT:SKULL,
                    side*(width+.25F)-1.2F,-4.0F+ring*.16F,z,
                    2.4F,4.7F-ring*.27F,2.8F);
            }
            // Recessed paired nostrils on the flattened rostrum, not bird nares.
            head.box(LIP,side*3.3F-.9F,-2.1F,-54F,1.8F,.7F,3.8F);
            head.box(DARK,side*3.25F-.48F,-2.35F,-55F,.96F,.42F,1.8F);
            // Gill cover bends backward into the thick base of the skull.
            for(int plate=0;plate<5;plate++){
                float z=-20F+plate*2.45F;
                float x=side*(8.9F+plate*.2F);
                head.box(plate%2==0?FLANK:OLIVE,x-1.5F,-1.8F,
                    z,3F,5.7F-plate*.5F,3.0F);
                head.box(DARK,x-1F,-1.5F,z+.4F,2F,3.4F,1.0F);
            }
            // Thin pale lip stripe runs along the sides of the real jaws.
            for(int j=0;j<16;j++){
                float z=-60F+j*3.0F;
                float x=side*(interpolate(z,hz,hw)*.94F);
                head.box(j%5==0?TEETH:LIP,x-1F,1.0F,z,2F,.8F,3F);
            }
        }
        for(float z=12;z<20;z+=3)
            root.box(SCAR,-13,-4+(z-12)*.18F,z,1.4F,1.4F,2F);
        // Posterior dorsal and anal fins are thin, curved rays: no wooden plates or leg-like boxes.
        for(float z=39F;z<58F;z+=1.75F){
            float phase=(z-39F)/19F;
            float height=2F+11F*Mth.sin(Mth.PI*phase);
            float atBody=interpolate(z,BODY_Z,BODY_H);
            for(float h=0;h<height;h+=1.5F){
                float width=Math.max(1.0F,2.7F*(1F-h/height));
                tailB.box(Math.floorMod((int)(z*3+h),11)==0?RIB:FIN,
                    -width/2,-atBody-h,z,width,1.65F,1.75F);
            }
        }
        for(float z=44F;z<59F;z+=1.75F){
            float phase=(z-44F)/15F;
            float height=1.7F+8.8F*Mth.sin(Mth.PI*phase);
            float atBody=interpolate(z,BODY_Z,BODY_H);
            for(float h=0;h<height;h+=1.5F){
                float width=Math.max(1.0F,2.6F*(1F-h/height));
                tailB.box(Math.floorMod((int)(z*2+h),9)==0?RIB:FINLIGHT,
                    -width/2,atBody+h,z,width,1.65F,1.75F);
            }
        }
        // Forked pike tail: thin upper and lower lobes, a notch between them.
        for(float z=59F;z<75F;z+=1.65F){
            float progress=(z-59F)/16F;
            float spread=2F+progress*9F;
            float half=2F+progress*3.2F;
            for(int lobe:new int[]{-1,1}){
                float center=lobe*spread;
                for(float y=center-half;y<=center+half;y+=1.65F){
                    if(progress>.52F && Math.abs(y)<1F+progress*3.5F)continue;
                    caudal.box(Math.floorMod((int)(z+Math.abs(y)),9)==0?RIB:FINLIGHT,
                        -1.1F,y,z,2.2F,1.75F,1.65F);
                }
            }
        }
        // Small swept-back pectorals: they start at the flank rather than projecting as paws.
        for(int sign:new int[]{-1,1}){
            Group p=sign<0?pectL:pectR,v=sign<0?pelvL:pelvR;
            for(float z=-4F;z<17F;z+=2F){
                float progress=(z+4F)/21F;
                float reach=1F+6.3F*Mth.sin(Mth.PI*progress);
                for(float across=0;across<reach;across+=1.65F){
                    float x=sign*(10.9F+across);
                    p.box(Math.floorMod((int)(z*2+across),7)==0?RIB:FINLIGHT,
                        x-.7F,5.6F+across*.19F,z,1.4F,1.3F,2F);
                }
            }
            // Pelvic fins are shorter and lie beside the rear third of the abdomen.
            for(float z=27F;z<43F;z+=2F){
                float progress=(z-27F)/16F;
                float reach=1F+4.6F*Mth.sin(Mth.PI*progress);
                for(float across=0;across<reach;across+=1.65F){
                    float x=sign*(8.6F+across);
                    v.box(Math.floorMod((int)(z+across*2),8)==0?RIB:FIN,
                        x-.65F,6F+across*.18F,z,1.3F,1.2F,2F);
                }
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
