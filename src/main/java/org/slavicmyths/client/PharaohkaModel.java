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
    private final ModelPart armL,guardL,armR,guardR,spear,shield,shieldTrim,handGripL,handGripR,crown;
    private final ModelPart tailA,tailB,tailC,fin;
    private ModelPart coralCrown;
    private ModelPart tridentBand;

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
        // Weapons have independent wrist anchors, never shoulder-origin shafts.
        spear=create(GOLD,-6,4,0);
        shield=create(ARMOR,6,3,0);
        shieldTrim=create(GOLD,6,3,0);
        handGripL=create(SKIN,-6,4,0);
        handGripR=create(SKIN,6,3,0);
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
        // Low scalp (2.5px rather than a second head), sealed to the forehead,
        // temples and back. Strands start inside this continuous root layer.
        box(hair,-4.6F,-12.65F,-3.85F,9.2F,2.55F,7.85F);
        box(hair,-4.75F,-10.65F,2.15F,9.5F,10.8F,2.25F);
        for(int i=0;i<10;i++) {
            float x=-4.55F+i*1.01F;
            box(hair,x,-9.7F,2.6F,1.15F,19.5F+(i%3)*2.6F,1.75F);
            // Short, staggered forehead locks; eyes remain open.
            if (i>0 && i<9)
                box(hair2,x,-11.45F+(i%2)*.25F,-4.12F,.95F,2.15F+(i%3)*.22F,.75F);
        }
        for (int side:new int[]{-1,1}) {
            float rootX=side*4.0F;
            // Fill the common scalp/temple gaps even while the head turns.
            box(hair,rootX-1.05F,-11.8F,-3.5F,2.1F,5.0F,3.0F);
            box(hair,rootX-1.05F,-11.5F,1.65F,2.1F,5.5F,2.7F);
            for (int lane=0;lane<5;lane++) {
                float x=side*(3.65F+lane*.37F),z=-3.5F+lane*1.75F;
                for (int j=0;j<8+lane%2;j++) {
                    float wave=Mth.sin(j*.65F+lane)*.4F;
                    box(hair,x+wave,-9.9F+j*1.85F,z,1.3F,2.3F,1.65F);
                    if (j%2==0)
                        box(hair2,x+wave+side*.22F,-9.15F+j*1.85F,z-.25F,.62F,.9F,.9F);
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
            coralCrown=coral;
            for(int s:new int[]{-1,1})for(int i=0;i<3;i++)
                box(coral,s*(2.2F+i*1.35F)-.55F,-20-i*.8F,-2.2F,1.1F,3.8F+i*.8F,1.2F);
            // Forearm shield: plate faces forward, centered on a moving hand grip.
            // Its supporting strap is BETWEEN the hand and the shield, not at the shoulder.
            box(shield,-4.65F,-12.2F,-4.2F,9.3F,19.1F,1.45F);
            box(shieldTrim,-4.8F,-12.35F,-4.65F,9.6F,.85F,.8F);
            box(shieldTrim,-4.8F,5.95F,-4.65F,9.6F,.85F,.8F);
            box(shieldTrim,-4.8F,-11.65F,-4.7F,.85F,17.6F,.8F);
            box(shieldTrim,3.95F,-11.65F,-4.7F,.85F,17.6F,.8F);
            box(shieldTrim,-1.35F,-3.15F,-4.95F,2.7F,3.2F,.72F);
            box(shieldTrim,-.72F,-1.5F,-2.95F,1.44F,3.9F,.65F);
            // Trident shaft runs through the closed left palm. All 3 prongs
            // extend from the shaft, not from the upper arm.
            box(spear,-.52F,-31.0F,-2.05F,1.05F,40.5F,1.05F);
            box(spear,-3.0F,-32.2F,-2.25F,6.0F,1.05F,1.4F);
            for (int prong:new int[]{-1,0,1})
                box(spear,prong*2.4F-.5F,-40.0F+(prong==0?-1.2F:0F),
                    -2.15F,1.0F,8.2F+(prong==0?1.2F:0F),1.15F);
            ModelPart shell=create(SHELL,-6,4,0);
            box(shell,-3.05F,-32.45F,-2.38F,6.1F,.65F,1.65F);
            // A visible hand and curled fingers conceal the weapon's grip gap.
            box(handGripL,-1.15F,-1.2F,-1.8F,2.3F,2.35F,3.1F);
            box(handGripL,-.9F,-.75F,-2.46F,1.8F,1.25F,.58F);
            // Right hand holds the shield handle; rear strap is visible in profile.
            box(handGripR,-1.15F,-1.4F,-2.9F,2.3F,2.75F,1.8F);
            box(handGripR,-.9F,-.85F,-3.08F,1.8F,1.3F,.55F);
            tridentBand=shell;
        } else {
            spear.visible=false;shield.visible=false;shieldTrim.visible=false;
            handGripL.visible=false;handGripR.visible=false;crown.visible=false;
        }
    }
    @Override public void setupAnim(PharaohkaEntity entity,float walk,float amount,float age,float yaw,float pitch) {
        for(Colored part:colored)part.part().resetPose();
        float w=Mth.sin(age*.16F),speed=Mth.clamp((float)entity.getDeltaMovement().length()*5,0,1);
        head.yRot=yaw*.012F;
        head.xRot=pitch*.009F;
        // Facial details and tiara share the same pivot as the head.
        // Back braids trail slightly behind the head during steering.
        eye.yRot=pupil.yRot=crown.yRot=head.yRot;
        eye.xRot=pupil.xRot=crown.xRot=head.xRot;
        if (coralCrown!=null) {
            coralCrown.yRot=head.yRot;
            coralCrown.xRot=head.xRot;
        }
        // Scalp and fringe must follow exactly the head's pivot. Lagging the
        // full hair shell behind head rotation exposes scalp-shaped bald gaps.
        hair.yRot=hair2.yRot=head.yRot;
        hair.xRot=hair2.xRot=head.xRot;

        // The four unparented colored tail sections follow a single curved centerline.
        // Overlap at every hinge prevents visible openings when swimming fast.
        float phase=age*.16F;
        tailA.yRot=Mth.sin(phase)*(.09F+speed*.05F);
        tailB.yRot=tailA.yRot+Mth.sin(phase-.7F)*(.12F+speed*.13F);
        tailC.yRot=tailB.yRot+Mth.sin(phase-1.4F)*(.13F+speed*.16F);
        fin.yRot=tailC.yRot+Mth.sin(phase-2.1F)*(.12F+speed*.15F);
        tailB.x=7F*Mth.sin(tailA.yRot)*.42F;
        tailC.x=tailB.x+7F*Mth.sin(tailB.yRot)*.34F;
        fin.x=tailC.x+4F*Mth.sin(tailC.yRot)*.28F;
        armL.xRot=guardL.xRot=spear.xRot=-.12F+w*.1F;
        armR.xRot=guardR.xRot=shield.xRot=shieldTrim.xRot=.1F-w*.1F;
        switch(entity.action()) {
            case PharaohkaEntity.STRIKE -> {
                float t=Math.min(1,entity.actionTicks()/7F);
                armL.xRot=guardL.xRot=spear.xRot=-1.2F*t;
            }
            case PharaohkaEntity.DASH -> {
                // The whole torso inclines together so the armor never peels from the skin.
                armor.xRot=skin.xRot=trim.xRot=cloth.xRot=-.22F;
                armL.xRot=guardL.xRot=spear.xRot=-1.5F;
                armR.xRot=guardR.xRot=shield.xRot=shieldTrim.xRot=-.35F;
                tailB.yRot*=1.18F;tailC.yRot*=1.22F;fin.yRot*=1.28F;
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
        if (leader) {
            followWrist(spear,armL,12F);
            followWrist(handGripL,armL,12F);
            if (tridentBand!=null) followWrist(tridentBand,armL,12F);
            followWrist(shield,armR,11F);
            followWrist(shieldTrim,armR,11F);
            followWrist(handGripR,armR,11F);
        }
    }

    private static void followWrist(ModelPart item,ModelPart arm,float wristLength) {
        // Explicit shoulder -> hand transform. Unlike the old independent
        // shoulder-root weapons, the item pivot now follows a moving wrist.
        float bend=arm.xRot,spread=arm.zRot;
        float rollCos=Mth.cos(spread);
        item.setPos(arm.x-wristLength*Mth.sin(spread),
            arm.y+wristLength*Mth.cos(bend)*rollCos,
            arm.z+wristLength*Mth.sin(bend)*rollCos);
        item.xRot=bend;
        item.yRot=arm.yRot;
        item.zRot=spread;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer consumer,int light,int overlay,int rgba) {
        int alpha=(rgba>>>24)&255;
        for (Colored p:colored) p.part().render(pose,consumer,light,overlay,(alpha<<24)|(p.color()&0xFFFFFF));
    }
}