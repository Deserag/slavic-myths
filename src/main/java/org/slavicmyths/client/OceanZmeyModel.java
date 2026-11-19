package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.marine.OceanZmeyEntity;

/**
 * Independent procedurally voxelized STORM ZMEY, in real ModelPart cuboids.
 * 16 linked body pivots, jaw hinge, volumetric streamlined skull, 3D fin rays.
 * The same geometry drives the entity and the collectible animated head.
 */
public final class OceanZmeyModel extends EntityModel<OceanZmeyEntity> {
    private static final int ABYSS=0xFF102D3B,DARK=0xFF1D4755,TEAL=0xFF316A6D,
        SCALE=0xFF468F89,LIGHT=0xFF73B7A8,BELLY=0xFF89A49D,
        FIN=0xFF2D7684,FINRAY=0xFF72B7BC,SKULL=0xFF234955,
        LIP=0xFF477F78,GUM=0xFF643A45,MAW=0xFF311C31,
        TOOTH=0xFFF3E7CB,GLOW=0xFF7CE5C8,IRIS=0xFFFFCE72,EYE=0xFF0B1522;
    private static final int SEGMENTS=16,SECTION_LENGTH=26;
    private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final Group root,head,jaw,crest;
    private final Group wingLeft,wingRight;
    private final Group[] body=new Group[SEGMENTS];

    private final class Group {
        final ModelPart pivot=geometry.part(256,256);
        final Map<Integer,ModelPart> colors=new LinkedHashMap<>();
        final List<Group> children=new ArrayList<>();
        final float x,y,z;
        Group(Group parent,float x,float y,float z){
            this.x=x;this.y=y;this.z=z;
            if(parent==null)pivot.setPos(x,y,z);
            else {
                pivot.setPos(x-parent.x,y-parent.y,z-parent.z);
                parent.children.add(this);
            }
        }
        void cube(int tint,float x,float y,float z,float w,float h,float d){
            ModelPart mesh=colors.computeIfAbsent(tint,k->geometry.part(256,256,0,0));
            geometry.box(mesh,x-this.x,y-this.y,z-this.z,w,h,d);
        }
        void draw(PoseStack pose,VertexConsumer out,int light,int overlay){
            pose.pushPose();
            pivot.translateAndRotate(pose);
            for(var colored:colors.entrySet())
                colored.getValue().render(pose,out,light,overlay,colored.getKey());
            for(Group child:children)child.draw(pose,out,light,overlay);
            pose.popPose();
        }
    }
    public OceanZmeyModel(){
        root=new Group(null,0,0,0);
        head=new Group(root,0,-24,-10);
        jaw=new Group(head,0,-16,-12);
        for(int i=0;i<SEGMENTS;i++)
            body[i]=new Group(i==0?root:body[i-1],0,-24,i*SECTION_LENGTH);
        crest=new Group(null,0,0,0);
        wingLeft=new Group(root,-23,-34,35);
        wingRight=new Group(root,23,-34,35);
        makeHead();
        makeBody();
        makeCrest();
        makeWings();
    }
    private void makeHead(){
        // Ocean Zmey elongated dragon-serpent anatomy: high narrow snout and powerful rear skull.
        for(float z=-87;z< -7;z+=3.25F){
            float t=(z+87)/80F;
            float halfWidth=9.8F+13F*(float)Math.pow(t,.82);
            float halfHeight=8.8F+5.5F*t;
            for(float x=-halfWidth;x<=halfWidth;x+=3.2F)
                for(float y=-30-halfHeight;y<=-22+halfHeight*.23F;y+=3.2F){
                    float dy=(y+30)/halfHeight;
                    if(x*x/(halfWidth*halfWidth)+dy*dy>1.03)continue;
                    int tint=y< -31?ABYSS:y< -26?SKULL:
                        Math.floorMod((int)(z+x),7)==0?SCALE:DARK;
                    head.cube(tint,x-1.7F,y-1.7F,z,3.4F,3.4F,3.4F);
                }
            // Separate upper lips and gum line, not a flat solid crocodile muzzle.
            for(int sign:new int[]{-1,1}){
                float x=sign*(halfWidth-2.7F);
                head.cube(LIP,x-2F,-19.5F,z,4F,2.3F,3.25F);
                head.cube(GUM,x-1.3F,-16.8F,z,2.6F,1.6F,3.15F);
            }
            // A true articulated mandible; teeth rotate together with this hinge.
            float lowerWidth=halfWidth*.92F;
            for(float x=-lowerWidth;x<=lowerWidth;x+=3.3F){
                float relative=x/lowerWidth;
                float depth=(float)Math.sqrt(Math.max(0,1-relative*relative));
                for(float y=-10;y<=-5+depth*6.5F;y+=3.1F)
                    jaw.cube(y>-4?BELLY:LIP,x-1.75F,y-1.55F,z,3.5F,3.2F,3.25F);
            }
            jaw.cube(MAW,-lowerWidth*.66F,-11.8F,z,lowerWidth*1.32F,1.6F,3.2F);
        }
        // Real volumetric fangs, staggered and alternating between upper and lower jaw.
        for(int i=0;i<14;i++){
            float z=-84+i*5.7F;
            float t=(z+87)/80F;
            float width=9.8F+13*(float)Math.pow(t,.82);
            for(int sign:new int[]{-1,1}){
                float x=sign*(width*.76F);
                head.cube(TOOTH,x-1.3F,-17,z,3.3F,9.0F,3.2F);
                head.cube(TOOTH,x-.8F,-11,z+.6F,1.6F,2.6F,1.8F);
                if(i%2==0){
                    jaw.cube(TOOTH,x-1.2F,-18.0F,z+2.0F,3.1F,11.0F,3F);
                    jaw.cube(TOOTH,x-.75F,-20.5F,z+2.5F,1.5F,3F,1.6F);
                }
            }
        }
        // Tall swept-back branching horns, not stubby forehead knobs.
        for(int sign:new int[]{-1,1}){
            for(int j=0;j<18;j++){
                float y=-43-j*2.9F;
                float x=sign*(12F+j*.34F);
                float z=-10F+j*.85F;
                float radius=Math.max(1.25F,5.7F-j*.24F);
                head.cube(j>13?FINRAY:SKULL,x-radius*.5F,y,z,
                    radius,3.4F,radius);
                if(j==9 || j==13){
                    for(int k=0;k<6;k++){
                        float branch=2.7F-k*.34F;
                        head.cube(FINRAY,x+sign*(k*1.7F)-branch*.5F,y-k*2F,z-k*2F,
                            branch,2.4F,branch);
                    }
                }
            }
        }
        // Inset side eyes, no protruding lizard eye stalks.
        for(int sign:new int[]{-1,1}){
            float x=sign*18.5F;
            head.cube(ABYSS,x-2.6F,-34,-26,5.2F,6.2F,6.3F);
            head.cube(IRIS,x-2.1F,-32.8F,-25.3F,4.2F,4.4F,4.5F);
            head.cube(EYE,x-1.3F,-31.7F,-24.6F,2.6F,2.5F,3F);
            head.cube(GLOW,x-1.1F,-35F,-24.5F,2.2F,1.2F,1.8F);
            head.cube(ABYSS,sign*5.4F-1.3F,-34,-64F,2.6F,2.5F,3F);
            // Recessed gill lines identify it as a marine predator, not a dragon.
            for(int i=0;i<3;i++)
                head.cube(ABYSS,sign*(20.2F-i*.55F)-1F,-25.5F,-15F-i*4F,
                    2F,7.0F-i*.6F,1.6F);
        }
    }
    private static float radius(float t){
        // Constant large trunk; only the rear quarter narrows to a pointed tail.
        return OceanZmeyEntity.radius(t)*16F;
    }
    private void makeBody(){
        for(int i=0;i<SEGMENTS;i++){
            Group part=body[i];
            for(float localZ=0;localZ<SECTION_LENGTH;localZ+=4.0F){
                float t=Mth.clamp((i*SECTION_LENGTH+localZ)/(SEGMENTS*SECTION_LENGTH),0,1);
                float rx=radius(t),ry=rx*.77F;
                // Three-dimensional ellipse, not a flat rectangular slab.
                for(float x=-rx;x<=rx;x+=4.1F)
                    for(float dy=-ry;dy<=ry;dy+=4.1F){
                        float normalized=(x*x)/(rx*rx)+(dy*dy)/(ry*ry);
                        if(normalized>1.06F || normalized<.70F)continue;
                        int color=dy<-.4F*ry?ABYSS:dy<.15F*ry?DARK:
                            dy>.57F*ry?BELLY:TEAL;
                        if(Math.floorMod((int)(i*41+x*2+dy*3+localZ*5),13)==0)
                            color=SCALE;
                        part.cube(color,x-2.1F,-24+dy-2.1F,i*SECTION_LENGTH+localZ,
                            4.25F,4.25F,4.1F);
                    }
                // Curved, tapered dorsal web and narrow rib rays; volumetric on both sides.
                if(i<15){
                    float h=(6F+7F*Mth.sin((i*SECTION_LENGTH+localZ)*.028F)
                        +3F*Mth.sin((i*SECTION_LENGTH+localZ)*.079F))
                        *Mth.clamp(rx/17F,0F,1F);
                    h=Math.max(2,h);
                    for(float climb=0;climb<h;climb+=2.5F){
                        float fraction=climb/h;
                        float width=2.6F*(1-fraction)+.6F;
                        float sweep=2.0F*Mth.sin((i*SECTION_LENGTH+localZ)*.075F+fraction);
                        part.cube(climb>h-3?FINRAY:FIN,sweep-width*.5F,
                            -24-ry-climb,i*SECTION_LENGTH+localZ,
                            width,2.7F,4.0F);
                    }
                    if(localZ<1F)part.cube(FINRAY,-1.5F,-24-ry-h*.7F,
                        i*SECTION_LENGTH+localZ,3F,4.8F,2.3F);
                }
                // Short posterior webbed side fins only, no rigid legs.
                if(i==4 || i==9)
                    for(int sign:new int[]{-1,1})
                        for(int r=0;r<5;r++){
                            float reach=3.0F+r*2.0F;
                            float taper=Math.max(.8F,3.0F-r*.46F);
                            float x=sign*(rx+reach);
                            part.cube(r==4?FINRAY:FIN,x-taper*.5F,
                                -23+reach*.24F,i*SECTION_LENGTH+localZ+r*2,
                                taper,2.4F,4F);
                        }
            }
        }
        // A narrow fin-tip follows the tapered tail instead of a comically wide paddle.
        Group tail=body[SEGMENTS-1];
        float start=SEGMENTS*SECTION_LENGTH;
        for(float z=0;z<16;z+=2.5F){
            float t=z/16F;
            float width=Math.max(.65F,3.1F*(1-t));
            float height=Math.max(.75F,5.0F*(1-t));
            tail.cube(z>11?FINRAY:FIN,-width*.5F,-24-height*.5F,
                start+z,width,height,3.0F);
        }
    }
    private void makeCrest(){
        // A fragment of the living dorsal fin. Native 1 x 2 block footprint after scaling.
        for(float z=-19;z<=18;z+=3F){
            float h=14F+12F*(.5F+.5F*Mth.sin(z*.12F+1.1F));
            for(float y=0;y<h;y+=2.5F){
                float narrowing=1F-y/h;
                float across=1.2F+2.0F*narrowing;
                crest.cube(y>h-3?GLOW:FIN,-across,-8-y,z,
                    across*2,2.7F,3.5F);
            }
            crest.cube(FINRAY,-1.15F,-7-h*.73F,z,2.3F,6F,2.4F);
        }
        crest.cube(GLOW,-5.5F,-11F,-6F,11F,5F,11F);
        crest.cube(ABYSS,-4.4F,-10.7F,-5F,8.8F,3.5F,8.8F);
    }
    /** Two genuinely articulated membrane wings folded close to the trunk while swimming. */
    private void makeWings(){
        for(int side:new int[]{-1,1}){
            Group wing=side<0?wingLeft:wingRight;
            float baseX=side*23F;
            for(int rib=0;rib<5;rib++){
                float sweepZ=35F+rib*16F;
                float extent=105F-rib*11F;
                for(float x=0;x<extent;x+=4.0F){
                    float taper=1F-x/extent;
                    float arcY=-34F-16F*Mth.sin(x/extent*Mth.PI*.8F)-rib*1.7F;
                    float globalX=baseX+side*x;
                    int color=(int)(x/4)%7==0?FINRAY:FIN;
                    wing.cube(color,globalX-2.2F,arcY,
                        sweepZ+x*.12F,4.4F,
                        1.8F+2.6F*taper,5F);
                    if(rib<4 && x<extent-9){
                        // Diagonal web between structural ribs.
                        for(int membrane=1;membrane<4;membrane++){
                            float acrossZ=sweepZ+membrane*4F+x*.12F;
                            float acrossY=arcY+membrane*.5F;
                            wing.cube(membrane==3?TEAL:FIN,globalX-2F,acrossY,
                                acrossZ,4F,1.65F,4.4F);
                        }
                    }
                }
            }
        }
    }
    @Override public void setupAnim(OceanZmeyEntity boss,float walk,float walkAmount,
        float age,float yaw,float pitch){
        float t=age*.085F;
        int action=boss.action();
        float attackBoost=action==OceanZmeyEntity.FLIGHT?2.1F:action==OceanZmeyEntity.TAIL?2.7F:1F;
        for(int i=0;i<SEGMENTS;i++){
            float wave=t*attackBoost-i*.34F;
            float arc=.075F*Mth.sin(wave) *
                (.3F+1.0F*i/(float)(SEGMENTS-1));
            if(action==OceanZmeyEntity.GRAB) {
                float progress=Mth.clamp(boss.actionTicks()/26F,0F,1F);
                // Exact real hinge rotations generate the grab loop.
                arc+= (i>2?.11F:0F)*progress;
            }
            if(action==OceanZmeyEntity.STORM){
                float phase=Mth.clamp(boss.actionTicks()/22F,0F,1F);
                if(i>1)arc+=.14F*phase;
            }
            if(action==OceanZmeyEntity.TAIL) arc+=.22F*Mth.sin(Mth.PI*Mth.clamp(boss.actionTicks()/42F,0F,1F))*(i/(float)SEGMENTS);
            body[i].pivot.yRot=arc;
            body[i].pivot.xRot=.012F*Mth.sin(t-i*.3F);
            body[i].pivot.zRot=0;
        }
        jaw.pivot.xRot=Mth.clamp(boss.jawOpen(),0F,1F)*.72F;
        head.pivot.yRot=boss.hurtTime>0?Mth.sin(age*.95F)*.20F:0F;
        head.pivot.xRot=action==OceanZmeyEntity.BITE?-.22F*Mth.sin(Mth.PI*Mth.clamp(boss.actionTicks()/32F,0F,1F)):0F;
        root.pivot.zRot=action==OceanZmeyEntity.FLIGHT?.10F*Mth.sin(age*.15F):0F;
        root.pivot.xRot=action==OceanZmeyEntity.TAKEOFF?-.12F:action==OceanZmeyEntity.LAND?.08F:0F;
        wingLeft.pivot.zRot=action==OceanZmeyEntity.FLIGHT || action==OceanZmeyEntity.TAKEOFF
            ? -.32F-.22F*Mth.sin(age*.28F):.88F;
        wingRight.pivot.zRot=-wingLeft.pivot.zRot;
        if(boss.deathTime>0) {
            float fall=Mth.clamp(boss.deathTime/20F,0F,1F);
            jaw.pivot.xRot=fall*.48F;
            head.pivot.yRot=fall*.13F;
            root.pivot.xRot=fall*.15F;
            for(int i=0;i<SEGMENTS;i++){
                body[i].pivot.yRot=.12F*Mth.sin(i*.43F)*fall;
                body[i].pivot.xRot=(-.045F-.025F*Mth.sin(i*.33F))*fall;
                body[i].pivot.zRot=.016F*fall*Mth.sin(i*.66F);
            }
        }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){
        root.draw(pose,out,light,overlay);
    }
    public void renderHead(PoseStack pose,VertexConsumer out,int light,int overlay,
        float age,boolean awake){
        jaw.pivot.xRot=(awake?.48F+.22F*Math.max(0,Mth.sin(age*.29F)):.08F)*.72F;
        head.pivot.yRot=awake?Mth.sin(age*.08F)*.05F:0F;
        pose.pushPose();
        root.pivot.translateAndRotate(pose);
        head.draw(pose,out,light,overlay);
        pose.popPose();
    }
    public void renderCrest(PoseStack pose,VertexConsumer out,int light,int overlay,
        float age,boolean awake){
        crest.pivot.zRot=(awake?.11F:.04F)*Mth.sin(age*.11F);
        crest.draw(pose,out,light,overlay);
    }
}
