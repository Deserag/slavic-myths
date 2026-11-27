package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.savanna.SavannaAnimal;
/** V2: a continuous flat-backed torso, articulated limbs, surface-only coat markings. */
public final class SavannaAnimalModel extends EntityModel<SavannaAnimal>{
    private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final Map<String,ModelPart> parts=new LinkedHashMap<>();
    private final Map<String,float[]> rest=new LinkedHashMap<>();
    private final SavannaAnimal.Kind kind;
    private int coat=255;
    public SavannaAnimalModel(SavannaAnimal.Kind kind){
        this.kind=kind;p("root",null,0,0,0,0,0,0);
        switch(kind){
            case AUROCHS -> quadruped(25,13,14,12,9,8,9,1,7,4);
            case SAIGA -> quadruped(18,7,9,12,6,5,6,3,3,2);
            case TARPAN -> quadruped(24,10,12,15,9,6,8,8,12,3);
            case JAYRAN -> quadruped(16,7,8,14,5,4,5,6,3,2);
            case JACKAL -> quadruped(16,6,7,9,7,4,5,1,7,2);
            case STRIPED_HYENA -> quadruped(20,8,9,10,8,5,6,1,7,2);
            case PERSIAN_LEOPARD -> quadruped(23,8,8,10,7,6,6,0,15,3);
            case PHEASANT -> bird(10,5,6,3,4,3,4,7,13);
            case STEPPE_EAGLE -> bird(13,6,7,5,5,4,4,12,7);
        }
    }
    private void p(String name,String parent,float x,float y,float z,float rx,float ry,float rz){
        ModelPart part=geometry.part(128,128);part.setPos(x,y,z);part.xRot=rx;part.yRot=ry;part.zRot=rz;
        parts.put(name,part);rest.put(name,new float[]{x,y,z,rx,ry,rz});
        if(parent!=null)geometry.attach(parts.get(parent),part);
    }
    private void b(String name,int u,int v,float x,float y,float z,float w,float h,float d){geometry.box(parts.get(name),u,v,x,y,z,w,h,d);}
    private ModelPart p(String name){return parts.get(name);}
    private void quadruped(int l,int w,int h,int g,int hl,int hw,int hh,int rise,int tailLength,int legWidth){
        p("body","root",0,24-g-h/2F,0,0,0,0);
        b("body",0,0,-w/2F,-h/2F,-l/2F,w,h,l);
        p("neck","body",0,-h*.13F,-l*.38F,kind==SavannaAnimal.Kind.TARPAN?-.25F:0,0,0);
        int nh=Math.max(4,rise+Math.round(h*.45F));
        b("neck",80,0,-hw/2F,-nh,-hw*.45F,hw,nh+3,Math.max(hw,4));
        p("head","neck",0,-rise-Math.round(h*.22F),-hw*.35F,0,0,0);
        b("head",0,44,-hw/2F,-hh*.55F,-hl*.55F,hw,hh,hl);
        int ml=kind==SavannaAnimal.Kind.PERSIAN_LEOPARD?3:Math.max(3,Math.round(hl*.65F));
        int mh=kind==SavannaAnimal.Kind.SAIGA?6:Math.max(2,Math.round(hh*.52F));
        b("head",40,44,-hw*.38F,-hh*.12F,-hl*.55F-ml+1,Math.max(2,Math.round(hw*.76F)),mh,ml);
        b("head",70,44,-hw*.25F,kind==SavannaAnimal.Kind.SAIGA?3:0,-hl*.55F-ml,Math.max(2,Math.round(hw*.5F)),2,1);
        p("jaw","head",0,hh*.33F,-hl*.31F,0,0,0);
        b("jaw",40,64,-hw*.31F,0,-ml,Math.max(2,Math.round(hw*.62F)),1,ml);
        for(int sign:new int[]{-1,1}){
            String side=sign<0?"L":"R";int eh=kind==SavannaAnimal.Kind.JACKAL||kind==SavannaAnimal.Kind.STRIPED_HYENA||kind==SavannaAnimal.Kind.TARPAN?3:2;
            p("ear"+side,"head",sign*hw*.43F,-hh*.53F,hl*.20F,0,0,sign*.18F);
            b("ear"+side,88,44,-1,-eh,0,2,eh,2);
            if(kind==SavannaAnimal.Kind.AUROCHS||kind==SavannaAnimal.Kind.SAIGA||kind==SavannaAnimal.Kind.JAYRAN){
                boolean bull=kind==SavannaAnimal.Kind.AUROCHS;
                p("horn"+side,"head",sign*hw*.35F,-hh*.48F,0,-.15F,0,sign*(bull?1.05F:.12F));
                b("horn"+side,100,44,-1,-5,-1,2,5,2);
                p("hornTip"+side,"horn"+side,0,-5,0,.24F,0,-sign*(bull?.7F:.12F));
                b("hornTip"+side,112,44,-.5F,bull?-4:-3,-.5F,1,bull?4:3,1);
            }
        }
        if(kind==SavannaAnimal.Kind.TARPAN)b("neck",104,24,-1,-nh,hw*.38F,2,nh,2);
        for(int i=0;i<4;i++){
            int sign=i%2==0?-1:1,upper=Math.round(g*.55F),lower=g-upper;
            p("leg"+i,"body",sign*w*.33F,h/2F-1,i<2?-l*.34F:l*.34F,0,0,0);
            b("leg"+i,0,80,-legWidth/2F,0,-legWidth/2F,legWidth,upper+1,legWidth);
            p("shin"+i,"leg"+i,0,upper,0,0,0,0);
            b("shin"+i,20,80,-legWidth/2F,0,-legWidth/2F,legWidth,lower,legWidth);
            b("shin"+i,40,80,-legWidth/2F,lower-1,-legWidth/2F-.1F,legWidth,1,legWidth);
        }
        p("tail","body",0,-h*.24F,l*.48F,kind==SavannaAnimal.Kind.PERSIAN_LEOPARD?-.25F:-.9F,0,0);
        int tw=kind==SavannaAnimal.Kind.TARPAN?3:2,seg=Math.round(tailLength/2F);
        b("tail",60,80,-tw/2F,0,0,tw,tw,seg);
        p("tailTip","tail",0,0,seg,-.18F,0,0);
        b("tailTip",90,80,-1,0,0,2,2,Math.max(1,tailLength-seg));
    }
    private void bird(int l,int w,int h,int g,int hl,int hw,int hh,int span,int tailLength){
        p("body","root",0,24-g-h/2F,0,0,0,0);
        b("body",0,0,-w/2F,-h/2F,-l/2F,w,h,l);
        p("neck","body",0,-h*.34F,-l*.34F,0,0,0);
        b("neck",80,0,-hw/2F,-4,-1,hw,5,3);
        p("head","neck",0,-4,-1,0,0,0);
        b("head",0,44,-hw/2F,-hh/2F,-hl/2F,hw,hh,hl);
        b("head",40,44,-1,-.5F,-hl/2F-2,2,2,3);
        if(kind==SavannaAnimal.Kind.STEPPE_EAGLE)b("head",70,44,-.5F,1,-hl/2F-2,1,2,1);
        else{b("head",88,44,-hw/2F-.1F,-1.5F,-1.5F,1,2,2);b("head",88,44,hw/2F-.9F,-1.5F,-1.5F,1,2,2);}
        for(int i=0;i<2;i++){
            int sign=i==0?-1:1;String side=i==0?"L":"R";
            int inner=Math.round(span*.55F),outer=Math.round(span*.48F);
            p("wing"+side,"body",sign*w*.47F,-h*.3F,0,0,0,sign*1.35F);
            b("wing"+side",0,80,sign<0?-inner:0,0,-l*.36F,inner,2,Math.round(l*.72F));
            p("wingTip"+side,"wing"+side,sign*inner,0,1,0,0,sign*.18F);
            b("wingTip"+side",40,80,sign<0?-outer:0,0,-2,outer,1,Math.round(l*.55F));
            p("leg"+i,"body",sign*w*.25F,h/2F-1,0,0,0,0);
            b("leg"+i,88,44,-.5F,0,-.5F,1,g,1);
            b("leg"+i,100,44,-1,g-1,-2,2,1,3);
        }
        p("tail","body",0,h*.14F,l*.43F,-.15F,0,0);
        b("tail",60,80,-1.5F,0,0,3,1,tailLength);
    }
    @Override public void setupAnim(SavannaAnimal e,float walk,float amount,float age,float yaw,float pitch){
        coat=e.hyenaRole()==SavannaAnimal.HyenaRole.LEADER?58:e.hyenaRole()==SavannaAnimal.HyenaRole.GUARD?165:255;
        parts.forEach((name,part)->{float[] r=rest.get(name);part.setPos(r[0],r[1],r[2]);part.xRot=r[3];part.yRot=r[4];part.zRot=r[5];});
        float phase=walk*.75F,move=Math.min(1,amount),run=Mth.clamp((move-.45F)*1.8F,0,1);
        p("head").yRot+=yaw*.012F;p("head").xRot+=pitch*.01F+Mth.sin(age*.08F)*.025F*(1-move);
        float strike=e.strikeTicks()>0?Mth.sin(3.1415927F*(e.strikeLength()-e.strikeTicks()+age-e.tickCount)/e.strikeLength()):0;
        if(!kind.bird()){
            for(int i=0;i<4;i++){
                float walkAngle=Mth.cos(phase+((i==0||i==3)?0:3.1415927F))*.65F*move;
                float gallop=Mth.sin(phase*1.25F+(i<2?0:2.2F)+(i%2)*.3F)*move;
                float angle=Mth.lerp(run,walkAngle,gallop);
                p("leg"+i).xRot+=angle;p("shin"+i).xRot+=(i<2?-1:1)*Math.max(0,-angle)*.65F;
            }
            p("body").y-=Math.abs(Mth.sin(phase))*run*.8F;
            p("tail").yRot+=Mth.sin(age*.1F)*.12F;p("tailTip").yRot+=Mth.sin(age*.1F-.5F)*.13F;
            p("earL").zRot+=Mth.sin(age*.09F)*.05F;p("earR").zRot-=Mth.sin(age*.08F)*.05F;
            if(kind==SavannaAnimal.Kind.AUROCHS){p("neck").xRot+=strike*.45F;p("head").xRot+=strike*.25F;}
            else if(kind.predator()){p("jaw").xRot+=strike*.52F;p("head").xRot+=strike*.18F;p("body").z-=strike*.8F;if(e.roarTicks()>0){float roar=Mth.sin((20-e.roarTicks())*3.1415927F/20F);p("jaw").xRot+=roar*.8F;p("head").xRot-=roar*.35F;}}
        }else{
            boolean flying=!e.onGround()&&!e.isInWater();
            for(int i=0;i<2;i++){
                int sign=i==0?-1:1;String side=i==0?"L":"R";
                p("wing"+side).zRot=flying?sign*(Mth.sin(age*.65F)*.7F):sign*1.35F;
                p("wingTip"+side).zRot=flying?sign*(Mth.sin(age*.65F-.6F)*.4F):sign*.18F;
                p("leg"+i).xRot=flying?.8F:Mth.cos(phase+i*3.1415927F)*move*.65F;
            }
            if(flying)p("body").xRot=-.12F+strike*.3F;
            p("head").xRot+=strike*.3F;
        }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){int tinted=(color&0xff000000)|((((color>>>16)&255)*coat/255)<<16)|((((color>>>8)&255)*coat/255)<<8)|((color&255)*coat/255);p("root").render(pose,out,light,overlay,tinted);}
}
