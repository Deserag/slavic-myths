package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.husbandry.YardAnimal;

/** Six separately authored meshes: adult/baby goose, duck and domestic goat. */
public final class HusbandryModel extends EntityModel<YardAnimal> {
    private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final ModelPart root=geometry.part(128,128);
    private final Map<String,ModelPart> parts=new LinkedHashMap<>();
    private final Map<String,float[]> rest=new LinkedHashMap<>();
    private final int kind;
    public HusbandryModel(int kind,boolean baby){this.kind=kind;
        if(kind==0 && baby==false){
            add("body",0,0,new float[]{0F,14F,1F},new float[]{-4.5F,-4F,-6F,9F,8F,12F});
            add("breast",43,0,new float[]{0F,13F,-4F},new float[]{-3.5F,-3F,-2F,7F,6F,3F});
            add("neck",64,0,new float[]{0F,11F,-4F},new float[]{-1.5F,-7F,-1.5F,3F,8F,3F});
            add("head",77,0,new float[]{0F,4F,-4.5F},new float[]{-2F,-2F,-2F,4F,4F,4F});
            add("beak",94,0,new float[]{0F,5F,-6.5F},new float[]{-1.5F,-0.5F,-3F,3F,2F,3F});
            add("left_wing",0,21,new float[]{4.5F,11F,0F},new float[]{0F,0F,-5F,1.5F,7F,10F});
            add("right_wing",24,21,new float[]{-4.5F,11F,0F},new float[]{-1.5F,0F,-5F,1.5F,7F,10F});
            add("left_leg",48,21,new float[]{2.5F,18F,0F},new float[]{-0.5F,0F,-0.5F,1F,5F,1F});
            add("left_foot",53,21,new float[]{2.5F,23F,0F},new float[]{-1F,0F,-2F,2F,1F,3F});
            add("right_leg",64,21,new float[]{-2.5F,18F,0F},new float[]{-0.5F,0F,-0.5F,1F,5F,1F});
            add("right_foot",69,21,new float[]{-2.5F,23F,0F},new float[]{-1F,0F,-2F,2F,1F,3F});
            add("tail",80,21,new float[]{0F,13F,5F},new float[]{-2F,0F,0F,4F,2F,3F});
            add("left_eye",95,21,new float[]{2F,3.5F,-5.6F},new float[]{0F,0F,0F,0.2F,0.7F,0.7F});
            add("right_eye",98,21,new float[]{-2.2F,3.5F,-5.6F},new float[]{0F,0F,0F,0.2F,0.7F,0.7F});
        }
        if(kind==0 && baby==true){
            add("body",0,0,new float[]{0F,18F,0F},new float[]{-3.5F,-3F,-4F,7F,6F,8F});
            add("neck",31,0,new float[]{0F,15F,-3F},new float[]{-1.5F,-2F,-1.5F,3F,4F,3F});
            add("head",44,0,new float[]{0F,12.5F,-4F},new float[]{-2.5F,-2F,-2F,5F,4F,4F});
            add("beak",63,0,new float[]{0F,13F,-6F},new float[]{-1.5F,0F,-2.5F,3F,1.5F,2.5F});
            add("left_wing",75,0,new float[]{3.5F,17F,0F},new float[]{0F,0F,-2.5F,1F,3F,5F});
            add("right_wing",88,0,new float[]{-3.5F,17F,0F},new float[]{-1F,0F,-2.5F,1F,3F,5F});
            add("left_leg",101,0,new float[]{2F,21F,0F},new float[]{-0.5F,0F,-0.5F,1F,2F,1F});
            add("left_foot",106,0,new float[]{2F,23F,0F},new float[]{-1F,0F,-2F,2F,1F,3F});
            add("right_leg",117,0,new float[]{-2F,21F,0F},new float[]{-0.5F,0F,-0.5F,1F,2F,1F});
            add("right_foot",0,15,new float[]{-2F,23F,0F},new float[]{-1F,0F,-2F,2F,1F,3F});
            add("tail",11,15,new float[]{0F,18F,3.5F},new float[]{-2F,0F,0F,4F,2F,3F});
            add("left_eye",26,15,new float[]{2.5F,12F,-5.2F},new float[]{0F,0F,0F,0.2F,0.7F,0.7F});
            add("right_eye",29,15,new float[]{-2.7F,12F,-5.2F},new float[]{0F,0F,0F,0.2F,0.7F,0.7F});
        }
        if(kind==1 && baby==false){
            add("body",0,0,new float[]{0F,17F,0F},new float[]{-4F,-3F,-5.5F,8F,6F,11F});
            add("breast",39,0,new float[]{0F,16F,-4.5F},new float[]{-3F,-2F,-1.5F,6F,4F,2F});
            add("neck",56,0,new float[]{0F,14F,-3.5F},new float[]{-1.5F,-2F,-1.5F,3F,3F,3F});
            add("head",69,0,new float[]{0F,11.5F,-4F},new float[]{-2F,-2F,-2F,4F,4F,4F});
            add("beak",86,0,new float[]{0F,12.5F,-6F},new float[]{-2F,-0.5F,-3.5F,4F,1.5F,3.5F});
            add("left_wing",102,0,new float[]{4F,15F,0F},new float[]{0F,0F,-4F,1F,4F,8F});
            add("right_wing",0,18,new float[]{-4F,15F,0F},new float[]{-1F,0F,-4F,1F,4F,8F});
            add("left_leg",19,18,new float[]{2.5F,20F,0F},new float[]{-0.5F,0F,-0.5F,1F,3F,1F});
            add("left_foot",24,18,new float[]{2.5F,23F,0F},new float[]{-1F,0F,-2F,2F,1F,3F});
            add("right_leg",35,18,new float[]{-2.5F,20F,0F},new float[]{-0.5F,0F,-0.5F,1F,3F,1F});
            add("right_foot",40,18,new float[]{-2.5F,23F,0F},new float[]{-1F,0F,-2F,2F,1F,3F});
            add("tail",51,18,new float[]{0F,16F,5F},new float[]{-2F,0F,0F,4F,2F,3F});
            add("left_eye",66,18,new float[]{2F,11F,-5.1F},new float[]{0F,0F,0F,0.2F,0.7F,0.7F});
            add("right_eye",69,18,new float[]{-2.2F,11F,-5.1F},new float[]{0F,0F,0F,0.2F,0.7F,0.7F});
        }
        if(kind==1 && baby==true){
            add("body",0,0,new float[]{0F,19F,0F},new float[]{-3.5F,-2.5F,-4.5F,7F,5F,9F});
            add("neck",33,0,new float[]{0F,17F,-3F},new float[]{-1.5F,-2F,-1.5F,3F,2F,3F});
            add("head",46,0,new float[]{0F,14F,-4F},new float[]{-2.5F,-2F,-2F,5F,4F,4F});
            add("beak",65,0,new float[]{0F,14.5F,-6F},new float[]{-2F,0F,-3F,4F,1F,3F});
            add("left_wing",80,0,new float[]{3.5F,18F,0F},new float[]{0F,0F,-3F,1F,3F,6F});
            add("right_wing",95,0,new float[]{-3.5F,18F,0F},new float[]{-1F,0F,-3F,1F,3F,6F});
            add("left_leg",110,0,new float[]{2F,21F,0F},new float[]{-0.5F,0F,-0.5F,1F,2F,1F});
            add("left_foot",115,0,new float[]{2F,23F,0F},new float[]{-1F,0F,-2F,2F,1F,3F});
            add("right_leg",0,15,new float[]{-2F,21F,0F},new float[]{-0.5F,0F,-0.5F,1F,2F,1F});
            add("right_foot",5,15,new float[]{-2F,23F,0F},new float[]{-1F,0F,-2F,2F,1F,3F});
            add("tail",16,15,new float[]{0F,19F,3.5F},new float[]{-2F,0F,0F,4F,2F,3F});
            add("left_eye",31,15,new float[]{2.5F,13.5F,-5.2F},new float[]{0F,0F,0F,0.2F,0.7F,0.7F});
            add("right_eye",34,15,new float[]{-2.7F,13.5F,-5.2F},new float[]{0F,0F,0F,0.2F,0.7F,0.7F});
        }
        if(kind==2 && baby==false){
            add("body",0,0,new float[]{0F,12F,0F},new float[]{-4.5F,-4F,-6F,9F,9F,13F});
            add("neck",45,0,new float[]{0F,11F,-5F},new float[]{-2F,-3F,-1.5F,4F,5F,4F});
            add("head",62,0,new float[]{0F,8.5F,-7F},new float[]{-2.5F,-2.5F,-2F,5F,5F,4F});
            add("muzzle",81,0,new float[]{0F,9.5F,-9F},new float[]{-1.5F,-0.5F,-2F,3F,2F,2F});
            add("left_ear",92,0,new float[]{2.5F,7.5F,-7F},new float[]{0F,-0.5F,-0.5F,2F,1F,2F});
            add("left_eye",101,0,new float[]{2.5F,7.7F,-8.5F},new float[]{0F,0F,0F,0.2F,0.8F,0.8F});
            add("right_ear",104,0,new float[]{-2.5F,7.5F,-7F},new float[]{-2F,-0.5F,-0.5F,2F,1F,2F});
            add("right_eye",113,0,new float[]{-2.7F,7.7F,-8.5F},new float[]{0F,0F,0F,0.2F,0.8F,0.8F});
            add("left_horn",116,0,new float[]{1.5F,6F,-7F},new float[]{-0.6F,-3F,0F,1.2F,3F,1.2F});
            add("left_horn_tip",122,0,new float[]{1.5F,3.5F,-6.2F},new float[]{-0.5F,-0.7F,0F,1F,1F,2F});
            add("right_horn",0,23,new float[]{-1.5F,6F,-7F},new float[]{-0.6F,-3F,0F,1.2F,3F,1.2F});
            add("right_horn_tip",6,23,new float[]{-1.5F,3.5F,-6.2F},new float[]{-0.5F,-0.7F,0F,1F,1F,2F});
            add("beard",13,23,new float[]{0F,10.5F,-7F},new float[]{-0.8F,0F,-1F,1.6F,2F,2F});
            add("collar",22,23,new float[]{0F,11F,-5F},new float[]{-2.4F,-2F,-1.7F,4.8F,1F,4.4F});
            add("bell",42,23,new float[]{0F,11F,-7F},new float[]{-0.7F,0F,-0.5F,1.4F,1.5F,1F});
            add("front_left_leg",48,23,new float[]{3F,16F,-4F},new float[]{-0.75F,0F,-0.75F,1.5F,7F,1.5F});
            add("front_left_hoof",55,23,new float[]{3F,23F,-4F},new float[]{-1F,0F,-1F,2F,1F,2F});
            add("front_right_leg",64,23,new float[]{-3F,16F,-4F},new float[]{-0.75F,0F,-0.75F,1.5F,7F,1.5F});
            add("front_right_hoof",71,23,new float[]{-3F,23F,-4F},new float[]{-1F,0F,-1F,2F,1F,2F});
            add("back_left_leg",80,23,new float[]{3F,16F,5F},new float[]{-0.75F,0F,-0.75F,1.5F,7F,1.5F});
            add("back_left_hoof",87,23,new float[]{3F,23F,5F},new float[]{-1F,0F,-1F,2F,1F,2F});
            add("back_right_leg",96,23,new float[]{-3F,16F,5F},new float[]{-0.75F,0F,-0.75F,1.5F,7F,1.5F});
            add("back_right_hoof",103,23,new float[]{-3F,23F,5F},new float[]{-1F,0F,-1F,2F,1F,2F});
            add("tail",112,23,new float[]{0F,10F,7F},new float[]{-0.7F,-1F,0F,1.4F,2F,3F});
        }
        if(kind==2 && baby==true){
            add("body",0,0,new float[]{0F,18F,0F},new float[]{-3F,-2.5F,-4F,6F,5F,8F});
            add("neck",29,0,new float[]{0F,17F,-3F},new float[]{-2F,-3F,-1.5F,4F,5F,4F});
            add("head",46,0,new float[]{0F,15F,-5F},new float[]{-2.5F,-2.5F,-2F,5F,5F,4F});
            add("muzzle",65,0,new float[]{0F,16F,-7F},new float[]{-1.5F,-0.5F,-2F,3F,2F,2F});
            add("left_ear",76,0,new float[]{2.5F,14F,-5F},new float[]{0F,-0.5F,-0.5F,2F,1F,2F});
            add("left_eye",85,0,new float[]{2.5F,14.2F,-6.5F},new float[]{0F,0F,0F,0.2F,0.8F,0.8F});
            add("right_ear",88,0,new float[]{-2.5F,14F,-5F},new float[]{-2F,-0.5F,-0.5F,2F,1F,2F});
            add("right_eye",97,0,new float[]{-2.7F,14.2F,-6.5F},new float[]{0F,0F,0F,0.2F,0.8F,0.8F});
            add("front_left_leg",100,0,new float[]{2F,20F,-2.5F},new float[]{-0.75F,0F,-0.75F,1.5F,3F,1.5F});
            add("front_left_hoof",107,0,new float[]{2F,23F,-2.5F},new float[]{-1F,0F,-1F,2F,1F,2F});
            add("front_right_leg",116,0,new float[]{-2F,20F,-2.5F},new float[]{-0.75F,0F,-0.75F,1.5F,3F,1.5F});
            add("front_right_hoof",0,14,new float[]{-2F,23F,-2.5F},new float[]{-1F,0F,-1F,2F,1F,2F});
            add("back_left_leg",9,14,new float[]{2F,20F,2.5F},new float[]{-0.75F,0F,-0.75F,1.5F,3F,1.5F});
            add("back_left_hoof",16,14,new float[]{2F,23F,2.5F},new float[]{-1F,0F,-1F,2F,1F,2F});
            add("back_right_leg",25,14,new float[]{-2F,20F,2.5F},new float[]{-0.75F,0F,-0.75F,1.5F,3F,1.5F});
            add("back_right_hoof",32,14,new float[]{-2F,23F,2.5F},new float[]{-1F,0F,-1F,2F,1F,2F});
            add("tail",41,14,new float[]{0F,16F,4F},new float[]{-0.7F,-1F,0F,1.4F,2F,3F});
        }
    }
    private void add(String name,int u,int v,float[] p,float[] box){
        var part=geometry.part(128,128);
        boolean headChild=Set.of("beak","muzzle","left_eye","right_eye","left_ear","right_ear","left_horn","right_horn","left_horn_tip","right_horn_tip","beard").contains(name);
        if(headChild){var h=rest.get("head");p=new float[]{p[0]-h[0],p[1]-h[1],p[2]-h[2]};}
        part.setPos(p[0],p[1],p[2]);geometry.box(part,u,v,box[0],box[1],box[2],box[3],box[4],box[5]);geometry.attach(headChild?parts.get("head"):root,part);parts.put(name,part);rest.put(name,p);
    }
    @Override public void setupAnim(YardAnimal e,float walk,float amount,float age,float yaw,float pitch){
        parts.forEach((name,p)->{var r=rest.get(name);p.setPos(r[0],r[1],r[2]);p.xRot=p.yRot=p.zRot=0;});
        float turn=yaw*Mth.DEG_TO_RAD,tilt=pitch*Mth.DEG_TO_RAD;
        var head=parts.get("head");head.yRot=turn;head.xRot=tilt;
        if(kind==2&&e.eatingTicks>0){head.y+=3;head.xRot+=.65F;}
        parts.get("neck").yRot=turn*.35F;
        boolean swim=e.isInWater();float phase=swim?age*.35F:walk*(e.isBaby()?1.5F:.8F);float stride=swim?.25F:amount*.8F;
        int index=0;for(var entry:parts.entrySet())if(entry.getKey().endsWith("_leg")){
            var p=entry.getValue();p.xRot=Mth.cos(phase+((index++%2==0)?0:Mth.PI))*stride;
            String end=entry.getKey().replace("_leg",kind==2?"_hoof":"_foot");var foot=parts.get(end);if(foot!=null){foot.z+=Mth.sin(p.xRot)*(foot.y-p.y);foot.y=p.y+Mth.cos(p.xRot)*(foot.y-p.y);foot.xRot=p.xRot;}
        }
        parts.get("tail").yRot=Mth.sin(age*.10F)*.1F;
        if(kind<2){
            float flap=(!e.onGround()&&!swim)?Mth.sin(age*1.2F)*.7F:(age%100<8?Mth.sin(age*.8F)*.18F:0);
            parts.get("left_wing").zRot=-flap;parts.get("right_wing").zRot=flap;
            if(swim){parts.get("body").xRot=-.04F;parts.get("body").y+=Mth.sin(age*.08F)*.15F;}
            if(e.defenceState()>0){parts.get("neck").xRot=-.18F;parts.get("head").y-=1.5F;parts.get("left_wing").zRot=-.25F;parts.get("right_wing").zRot=.25F;}
            if(e.defenceState()==3){parts.get("head").z-=1.5F;}
        }
        parts.get("body").y+=Mth.sin(age*.06F)*.08F;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer buffer,int light,int overlay,int color){root.render(pose,buffer,light,overlay,color);}
}
