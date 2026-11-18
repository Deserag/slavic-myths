package org.slavicmyths.client;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import org.slavicmyths.marine.PharaohkaEntity;

/** True colored ModelPart cuboids, based on the accepted V3 Python silhouette. */
public final class PharaohkaModel extends EntityModel<PharaohkaEntity> {
    private static final int SKIN=0xFF91AFAD, HAIR=0xFF15323D, HAIR2=0xFF315D68;
    private static final int TAIL=0xFF23686C, SCALE=0xFF488E8E, FIN=0xFF7ABCB1;
    private static final int ARMOR=0xFF89745B, BRONZE=0xFFAA8A56, GOLD=0xFFD2B879;
    private static final int CLOTH=0xFF344B53, DARK=0xFF25383F, SHELL=0xFFD7D1AE;
    private static final int EYE=0xFFF3DCA2, CORAL=0xFFC16A69;
    private record Colored(ModelPart part,int color) {}
    private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final List<Colored> colored=new ArrayList<>();
    private final boolean leader;
    private final ModelPart skin,head,hair,hair2,armor,trim,cloth,eye,pupil;
    private final ModelPart armL,guardL,armR,guardR,spear,shield,shieldTrim,crown;
    private final ModelPart tailA,tailB,tailC,fin;

    private ModelPart create(int color,float x,float y,float z) {
        // Large parts (shield and trident) must not sample outside the white source texture.
        ModelPart p=geometry.part(256,256);
        p.setPos(x,y,z);
        p.setInitialPose(PartPose.offset(x,y,z));
        colored.add(new Colored(p,color));
        return p;
    }
    private void box(ModelPart p,float x,float y,float z,float w,float h,float d) {
        geometry.box(p,x,y,z,w,h,d);
    }
    public PharaohkaModel(boolean boss) {
        leader=boss;
        skin=create(SKIN,0,24,0);
        head=create(SKIN,0,-9,0);
        hair=create(HAIR,0,-9,0);
        hair2=create(HAIR2,0,-9,0);
        armor=create(boss?ARMOR:CLOTH,0,24,0);
        trim=create(boss?GOLD:BRONZE,0,24,0);
        cloth=create(DARK,0,24,0);
        eye=create(EYE,0,-9,0);
        pupil=create(DARK,0,-9,0);
        armL=create(SKIN,-6,-8,0);
        guardL=create(boss?ARMOR:CLOTH,-6,-8,0);
        armR=create(SKIN,6,-8,0);
        guardR=create(boss?ARMOR:CLOTH,6,-8,0);
        spear=create(GOLD,-6,-8,0);
        shield=create(ARMOR,6,-8,0);
        shieldTrim=create(GOLD,6,-8,0);
        crown=create(GOLD,0,-9,0);
        tailA=create(TAIL,0,8,0);
        tailB=create(SCALE,0,15,0);
        tailC=create(TAIL,0,22,0);
        fin=create(FIN,0,26,0);
        build();
    }
    private void build() {
        // Neck, compact head, full bust and pronounced waist.
        box(skin,-3.2F,-27,-2.3F,6.4F,12,4.6F);
        box(skin,-4.7F,-36,-3.1F,9.4F,10.2F,6.2F);
        box(skin,-3.4F,-19,-2.6F,6.8F,5.2F,5.2F);
        box(skin,-1.25F,-39,-1.3F,2.5F,5,2.6F);
        box(head,-3.7F,-12,-3.35F,7.4F,9.3F,6.7F);
        for (int side:new int[]{-1,1}) {
            float x=side*3.6F;
            box(head,x-.65F,-10,-1.5F,1.3F,3.5F,3.0F);
            for(int j=0;j<3;j++)box(head,x+side*j*.35F-.3F,-8+j,0,.7F,.65F,1.5F);
            box(eye,side*1.85F-.77F,-8.1F,-3.8F,1.55F,1.2F,.45F);
            box(pupil,side*1.85F-.32F,-8,-4.05F,.65F,.85F,.42F);
        }
        // Closed corslet: chest, sides and rear share six overlapping layers.
        for (int i=0;i<6;i++) {
            float y=-37+i*2.45F,w=i<2?9.7F:i<4?8:6.2F,d=i<2?6.8F:i<4?5.7F:5;
            box(armor,-w/2,y,-d/2,w,2.8F,d);
            box(trim,-w/2-.1F,y,-d/2-.25F,w+.2F,.6F,.6F);
            box(trim,-w/2-.12F,y,-d/2,.6F,2.4F,d);
            box(trim,w/2-.48F,y,-d/2,.6F,2.4F,d);
            box(trim,-w/2+.5F,y,d/2-.1F,w-1,.45F,.55F);
        }
        // Raised enlarged united breastplate and high neckline.
        box(armor,-4.8F,-37.2F,-5.1F,9.6F,4.6F,2.5F);
        box(armor,-5.3F,-34,-5.7F,10.6F,3.1F,3.0F);
        box(armor,-4.4F,-31.2F,-4.8F,8.8F,2.2F,1.5F);
        box(trim,-5.25F,-31.3F,-5.95F,10.5F,.7F,.75F);
        box(trim,-2.3F,-38.2F,-4.3F,4.6F,1.1F,.9F);
        // Backplates and side ribs, even when the hair is removed.
        box(armor,-3.9F,-31,2.7F,7.8F,11.1F,1.0F);
        box(trim,-1.2F,-30.3F,3.1F,2.4F,9.4F,.7F);
        box(trim,-4.9F,-20.3F,-3.4F,9.8F,1.4F,6.8F);
        box(cloth,-4.3F,-18.4F,-3.6F,8.6F,7.0F,.7F);
        for (int s:new int[]{-1,1}) {
            box(trim,s*3.6F-.45F,-28,-3.7F,.9F,8,1);
            box(cloth,s*3.9F-1.05F,-17.3F,-3.4F,2.1F,7.8F,1.0F);
        }
        // No scalp/side voids: close-set front, intermediate and rear strands.
        box(hair,-4.8F,-15.7F,-3.9F,9.6F,4.7F,7.8F);
        box(hair,-4.8F,-12.5F,2.1F,9.6F,13,2.3F);
        for(int i=0;i<10;i++) {
            float x=-4.7F+i*1.05F;
            box(hair,x,-3,2.75F,1.2F,19+(i%3)*2.7F,1.5F);
            box(hair2,x,-13.2F+(i%3)*.4F,-4.05F,1.1F,2.9F,1.3F);
        }
        for (int s:new int[]{-1,1}) {
            for (int lane=0;lane<5;lane++) {
                float x=s*(3.65F+lane*.37F),z=-3.5F+lane*1.75F;
                for (int j=0;j<8+lane%2;j++) {
                    float wave=Mth.sin(j*.65F+lane)*.4F;
                    box(hair,x+wave,-9+j*2,z,1.4F,2.55F,1.7F);
                    if (j%2==0) box(hair2,x+wave+s*.3F,-8+j*2,z-.22F,.65F,1,1.0F);
                }
            }
        }
        // Shoulder pivot, long forearms and wrist guards.
        for(boolean l:new boolean[]{true,false}) {
            ModelPart a=l?armL:armR,g=l?guardL:guardR;
            box(a,-1.6F,-.4F,-2,3.2F,13.5F,4);
            box(a,-1.15F,11,-1.6F,2.3F,3.2F,3.2F);
            box(g,-2.1F,-1.5F,-2.5F,4.2F,3.8F,5);
            box(g,-1.7F,6,-2.35F,3.4F,3.6F,4.7F);
        }
        // Tail divided into real moving bones with alternating stepped scales.
        for(int i=0;i<5;i++) {
            float w=9.2F-i*1.05F;
            box(tailA,-w/2,-4+i*2.25F,-2.9F+i*.22F,w,2.7F,5.8F-i*.44F);
        }
        for(int i=0;i<5;i++) {
            float w=5.3F-i*.5F;
            box(tailB,-w/2,-3+i*2,-2.1F,w,2.4F,4.2F-i*.31F);
        }
        for(int i=0;i<4;i++) {
            float w=3.5F-i*.3F;
            box(tailC,-w/2,-3+i*2.1F,-1.5F,w,2.5F,3);
        }
        for(int s:new int[]{-1,1})for(int i=0;i<8;i++) {
            float x=s*(1+i*1.05F);
            box(fin,x-1.0F,1+i*.2F,-1,2,7.2F-i*.47F,2);
        }
        if (leader) {
            box(crown,-4.8F,-17,-4.1F,9.6F,1.8F,8.2F);
            box(crown,-2.2F,-20,-4.5F,4.4F,4,1.3F);
            ModelPart coral=create(CORAL,0,-9,0);
            for(int s:new int[]{-1,1})for(int i=0;i<3;i++)
                box(coral,s*(2.2F+i*1.35F)-.55F,-20-i*.8F,-2.2F,1.1F,3.8F+i*.8F,1.2F);
            // The large shield is articulated with the right forearm.
            box(shield,1.0F,-5.4F,-6.4F,9.8F,19.6F,1.5F);
            box(shieldTrim,.9F,-5.6F,-6.9F,10,1,1.1F);
            box(shieldTrim,.9F,13.2F,-6.9F,10,1,1.1F);
            box(shieldTrim,1,-4.7F,-7,.9F,18.5F,1.0F);
            box(shieldTrim,10,-4.7F,-7,.9F,18.5F,1.0F);
            box(shieldTrim,5,2,-7.4F,2.8F,3.5F,.8F);
            // Distinct trident with three prongs attached to the left arm.
            box(spear,-3,-23,-4.1F,1,44,1);
            for (int s:new int[]{-1,0,1})
                box(spear,-3+s*2.1F,-29+(s==0?-2:0),-4.3F,1,9,1);
            ModelPart shell=create(SHELL,-6,-8,0);
            box(shell,-4.5F,-23,-4.6F,4.7F,1.1F,1.5F);
        } else {
            spear.visible=false;shield.visible=false;shieldTrim.visible=false;crown.visible=false;
        }
    }
    @Override public void setupAnim(PharaohkaEntity entity,float walk,float amount,float age,float yaw,float pitch) {
        for(Colored part:colored)part.part().resetPose();
        float w=Mth.sin(age*.16F),speed=Mth.clamp((float)entity.getDeltaMovement().length()*5,0,1);
        head.yRot=yaw*.012F;
        head.xRot=pitch*.009F;
        hair.yRot=head.yRot*.5F;
        hair2.yRot=head.yRot*.5F;
        tailA.yRot=w*.12F;
        tailB.yRot=Mth.sin(age*.16F-.7F)*(.14F+speed*.13F);
        tailC.yRot=Mth.sin(age*.16F-1.4F)*(.19F+speed*.21F);
        fin.yRot=Mth.sin(age*.16F-2.1F)*(.24F+speed*.28F);
        armL.xRot=guardL.xRot=spear.xRot=-.12F+w*.1F;
        armR.xRot=guardR.xRot=shield.xRot=shieldTrim.xRot=.1F-w*.1F;
        switch(entity.action()) {
            case PharaohkaEntity.STRIKE -> {
                float t=Math.min(1,entity.actionTicks()/7F);
                armL.xRot=guardL.xRot=spear.xRot=-1.2F*t;
            }
            case PharaohkaEntity.DASH -> {
                armor.xRot=-.22F;
                armL.xRot=guardL.xRot=spear.xRot=-1.5F;
                armR.xRot=guardR.xRot=shield.xRot=shieldTrim.xRot=-.35F;
                tailB.yRot*=1.6F;tailC.yRot*=1.7F;fin.yRot*=1.8F;
            }
            case PharaohkaEntity.SUMMON -> {
                armL.xRot=guardL.xRot=spear.xRot=-1.9F;
                armR.xRot=guardR.xRot=shield.xRot=shieldTrim.xRot=-1.7F;
            }
            case PharaohkaEntity.CURRENT -> {
                armL.zRot=.55F;armR.zRot=-.55F;
                fin.yRot*=1.6F;
            }
            case PharaohkaEntity.BLOCK -> {
                armR.xRot=guardR.xRot=shield.xRot=shieldTrim.xRot=-1.2F;
            }
            case PharaohkaEntity.BASH -> {
                float t=Math.min(1.0F,entity.actionTicks()/9.0F);
                float reach=Mth.sin(t*3.1415927F*.8F);
                armR.xRot=guardR.xRot=shield.xRot=shieldTrim.xRot=-.4F-.9F*reach;
                armR.zRot=guardR.zRot=shield.zRot=shieldTrim.zRot=-.25F*reach;
                tailB.yRot*=1.35F;fin.yRot*=1.2F;
            }
            default -> { }
        }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer consumer,int light,int overlay,int rgba) {
        int alpha=(rgba>>>24)&255;
        for (Colored p:colored) p.part().render(pose,consumer,light,overlay,(alpha<<24)|(p.color()&0xFFFFFF));
    }
}