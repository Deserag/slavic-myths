package org.slavicmyths.auka.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.slavicmyths.client.FolkModelGeometry;
import org.slavicmyths.auka.AukaBoss;

/** Jointed four-legged cubic rig shared by the giant Auka wolf and his smaller companions. */
public final class AukaAnimalModel<T extends LivingEntity> extends EntityModel<T> {
    private final FolkModelGeometry geometry=new FolkModelGeometry();
    private ModelPart part(ModelPart parent,float x,float y,float z){
        ModelPart p=geometry.part(1024,1024);p.setPos(x,y,z);
        if(parent!=null)geometry.attach(parent,p);
        return p;
    }
    private void cube(ModelPart p,int material,float x,float y,float z,float w,float h,float d){
        geometry.box(p,(material%8)*128+4,(material/8)*128+4,x,y,z,w,h,d);
    }
    private final ModelPart root=part(null,0,0,0);
    private final ModelPart body=part(root,0,0,0);
    private final ModelPart head=part(root,0,-29,-34);
    private final ModelPart jaw=part(head,0,5,-12);
    private final ModelPart neck=part(root,0,-26,-27);
    private final ModelPart tail=part(root,0,-25,28);
    private final ModelPart tailTip=part(tail,0,0,14);
    private final ModelPart[] upper=new ModelPart[4],lower=new ModelPart[4];
    private final boolean bear;
    public AukaAnimalModel(boolean bear){
        this.bear=bear;
        // Atlas: skin=0 red=2 fur=4 bark=6 moss=8 bone=9 wolf=10 silver=11 shadow=12 eyes=13.
        int hide=bear?6:10,shade=bear?7:11,light=bear?4:5;
        cube(body,hide,-11,-32,-17,22,17,28);
        cube(body,hide,-10,-31,9,20,17,27);
        cube(body,shade,-10,-18,-15,20,5,45);
        cube(body,light,-13,-34,-26,26,15,22);
        cube(body,shade,-10,-35,-14,20,5,18);
        cube(neck,light,-11,-12,-8,22,18,19);
        for(int side:new int[]{-1,1}){
            cube(neck,bear?6:11,side*8-3,-13,-4,6,12,15);
            cube(neck,light,side*9-2,-7,-8,5,9,10);
        }
        cube(head,hide,-8,-9,-12,16,16,20);
        cube(head,light,-7,-5,-18,14,8,13);
        cube(head,shade,-5,2,-23,10,6,11);
        cube(jaw,shade,-6,0,-11,12,5,12);
        cube(head,14,-3,-2,-24,6,3,3);
        for(int side:new int[]{-1,1}){
            cube(head,hide,side*7-2,-16,-5,5,11,6);
            cube(head,shade,side*7-1,-15,-6,3,8,2);
            cube(head,13,side*4-1,-3,-19,2,2,1);
            cube(head,14,side*4-1,-3,-20,1,1,1);
        }
        for(int i=0;i<4;i++){
            int side=i%2==0?-1:1;
            boolean front=i<2;
            upper[i]=part(root,side*9,-21,front?-18:19);
            lower[i]=part(upper[i],0,12,0);
            cube(upper[i],hide,-4,0,-5,8,14,10);
            cube(lower[i],shade,-3,0,-4,6,11,9);
            cube(lower[i],hide,-4,9,-8,8,3,13);
            for(int j=-2;j<=2;j+=2)cube(lower[i],9,j-1,10,-9,2,2,3);
        }
        cube(tail,hide,-5,-5,0,10,10,19);
        cube(tailTip,light,-5,-5,0,10,10,12);
        for(int i=0;i<5;i++){
            float x=(i-2)*4;
            cube(neck,i%2==0?11:5,x-2,-12,-3+(i%3)*2,4,10,9);
        }
    }
    private static float rise(float x){return Mth.clamp(x,0,1);}
    public void pose(float walk,float amount,float age,float yaw,float pitch,AukaBoss.Move action,float clock,boolean moving){
        root.xRot=root.yRot=root.zRot=0;
        body.xRot=0;body.yRot=0;body.zRot=Mth.sin(walk*.55F)*amount*.035F;
        neck.xRot=0;neck.zRot=0;head.xRot=Mth.clamp(pitch,-25,25)*Mth.DEG_TO_RAD*.55F;
        head.yRot=Mth.clamp(yaw,-30,30)*Mth.DEG_TO_RAD*.55F;
        jaw.xRot=0;
        tail.xRot=-.12F;tail.yRot=Mth.sin(age*.08F)*.17F;
        tailTip.yRot=Mth.sin(age*.08F-1)*.21F;
        for(int i=0;i<4;i++){
            float phase=walk*.72F+(i==0||i==3?0:Mth.PI);
            upper[i].xRot=Mth.cos(phase)*Math.min(1,amount)*.65F;
            lower[i].xRot=Math.max(0,Mth.sin(phase))*.7F*Math.min(1,amount);
        }
        if(action==AukaBoss.Move.PACK_CALL){
            float a=rise(clock/15)*(1-rise((clock-34)/15));
            head.xRot-=.4F*a;neck.xRot=-.45F*a;jaw.xRot=.55F*a;
            for(int i=0;i<4;i++){upper[i].xRot=0;lower[i].xRot=0;}
        }
        if(action==AukaBoss.Move.WOLF_LEAP){
            float crouch=rise(clock/10)*(1-rise((clock-13)/4));
            float flight=rise((clock-13)/5)*(1-rise((clock-29)/9));
            body.xRot=.15F*crouch-.20F*flight;head.xRot=.24F*crouch-.12F*flight;
            for(int i=0;i<4;i++){
                upper[i].xRot=(i<2?-.8F:.48F)*flight+(i<2?.2F:-.3F)*crouch;
                lower[i].xRot=(i<2?.65F:.8F)*flight;
            }
            jaw.xRot=.52F*flight;
        } else if(action==AukaBoss.Move.WOLF_BITE){
            float a=rise((clock-6)/7)*(1-rise((clock-15)/8));
            neck.xRot=.3F*a;head.xRot+=.3F*a;jaw.xRot=.65F*a;
        } else if(action==AukaBoss.Move.WOLF_RAM){
            float a=rise((clock-8)/7)*(1-rise((clock-26)/9));
            head.xRot+=.4F*a;neck.xRot=.13F*a;body.xRot=.08F*a;
        }
    }
    @Override public void setupAnim(T entity,float walk,float amount,float age,float yaw,float pitch){
        pose(walk,amount,age,yaw,pitch,AukaBoss.Move.IDLE,0,true);
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){
        root.render(pose,out,light,overlay,color);
    }
}
