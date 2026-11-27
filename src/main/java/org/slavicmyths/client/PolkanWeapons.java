package org.slavicmyths.client;

import net.minecraft.client.model.geom.ModelPart;

/** Shared held and projectile geometry. UV tiles are solid material swatches. */
final class PolkanWeapons {
    static final int COAT=0, DARK=1, SKIN=2, BEARD=3, ARMOR=4, GOLD=5, RED=6, STEEL=7, WOOD=8, RUNE=9, STRING=10, EYE=11, WHITE=12;
    static ModelPart part(FolkModelGeometry g,ModelPart parent,float x,float y,float z){
        ModelPart p=g.part(256,256);p.setPos(x,y,z);if(parent!=null)g.attach(parent,p);return p;
    }
    static void box(FolkModelGeometry g,ModelPart p,int c,float x,float y,float z,float w,float h,float d){
        g.box(p,(c%4)*64+1,(c/4)*64+1,x,y,z,w,h,d);
    }
    static ModelPart spear(FolkModelGeometry g,ModelPart parent,float x,float y,float z){
        ModelPart p=part(g,parent,x,y,z);
        box(g,p,WOOD,-.75F,-21,-.75F,1.5F,42,1.5F);
        for(int i=-18;i<20;i+=12)box(g,p,GOLD,-1,i,-1,2,2,2);
        box(g,p,DARK,-1, -3,-1,2,7,2);
        for(int i=-3;i<4;i+=2)box(g,p,GOLD,-1.1F,i,-1.1F,2.2F,.5F,2.2F);
        box(g,p,STEEL,-1.2F,-24,-1.1F,2.4F,4,2.2F);
        box(g,p,GOLD,-3,-25,-1,6,2,2);
        box(g,p,STEEL,-3,-31,-.85F,6,6,1.7F);
        box(g,p,STEEL,-2,-34,-.7F,4,3,1.4F);
        box(g,p,STEEL,-1,-36,-.55F,2,2,1.1F);
        box(g,p,DARK,-1.1F,-31,-.95F,2.2F,6,1.9F);
        for(int s:new int[]{-1,1}){
            float zc=s<0?-1.05F:.95F;
            box(g,p,RUNE,-.45F,-30,zc,.9F,4,.1F);
            box(g,p,RUNE,-1.5F,-29,zc,3,.6F,.1F);
            box(g,p,RUNE,-1.1F,-27,zc,2.2F,.6F,.1F);
        }
        box(g,p,RED,1,-22,-.5F,5,8,1);
        box(g,p,GOLD,1,-22,-.6F,5,1,1.2F);
        box(g,p,STEEL,-1,20,-1,2,2,2);
        return p;
    }
    static ModelPart arrow(FolkModelGeometry g,ModelPart parent,float x,float y,float z){
        ModelPart p=part(g,parent,x,y,z);
        box(g,p,WOOD,-.3F,-7,-.3F,.6F,15,.6F);
        box(g,p,STEEL,-.8F,-10,-.6F,1.6F,3,1.2F);
        box(g,p,RUNE,-.35F,-11,-.35F,.7F,1,.7F);
        box(g,p,RED,-1,5,-.4F,2,3,.8F);box(g,p,RED,-.4F,5,-1,.8F,3,2);
        return p;
    }
    static final class Bow {
        final ModelPart root, upper,lower,stringTop,stringBottom;
        Bow(FolkModelGeometry g,ModelPart parent,float x,float y,float z){
            root=part(g,parent,x,y,z);
            box(g,root,DARK,-1,-4,-1,2,8,2);
            for(int i=-3;i<=3;i+=2)box(g,root,GOLD,-1.1F,i,-1.1F,2.2F,.7F,2.2F);
            upper=part(g,root,0,-4,0);lower=part(g,root,0,4,0);
            for(int s:new int[]{-1,1}){
                ModelPart limb=s<0?upper:lower;
                for(int i=0;i<5;i++){
                    int zc=i==0?0:i==1?-1:i==2?-3:i==3?-5:-7;
                    float yy=s<0?-(i+1)*3:i*3;
                    box(g,limb,i==4?GOLD:WOOD,-.9F,yy,zc,1.8F,3.2F,2);
                    if(i==1||i==2||i==3)box(g,limb,GOLD,-1.05F,yy,zc-.1F,2.1F,.6F,2.2F);
                    if(i>=1&&i<=3){
                        box(g,limb,RUNE,-1.1F,yy+.7F,zc+.2F,.2F,1.6F,1.2F);
                        box(g,limb,RUNE,.9F,yy+.7F,zc+.2F,.2F,1.6F,1.2F);
                    }
                }
            }
            stringTop=part(g,root,0,-19,-6);stringBottom=part(g,root,0,19,-6);
            box(g,stringTop,STRING,-.13F,0,-.13F,.26F,19,.26F);
            box(g,stringBottom,STRING,-.13F,-19,-.13F,.26F,19,.26F);
        }
        void draw(float t){
            upper.xRot=-.15F*t;lower.xRot=.15F*t;
            float depth=-6F+(float)Math.sin(.15F*t)*15;
            float pull=depth+10*t;float a=(float)Math.atan2(pull-depth,19);
            stringTop.z=depth;stringBottom.z=depth;stringTop.xRot=a;stringBottom.xRot=-a;
            float stretch=(float)Math.sqrt(19*19+(pull-depth)*(pull-depth))/19;
            stringTop.yScale=stretch;stringBottom.yScale=stretch;
        }
    }
}