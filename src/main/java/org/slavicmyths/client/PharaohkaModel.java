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
    private static final int SKIN=0xFF8EBBBB, HAIR=0xFF123A49, HAIR2=0xFF347482;
    private static final int TAIL=0xFF185D70, SCALE=0xFF3E9B9E, FIN=0xFF7BCFC6;
    private static final int ARMOR=0xFF326873, BRONZE=0xFFB48B63, GOLD=0xFFE6C987;
    private static final int CLOTH=0xFF224E61, DARK=0xFF142F3E, SHELL=0xFFDDE4CC;
    private static final int EYE=0xFFF4D998, CORAL=0xFFD77E82;
    private record Colored(ModelPart part,int color) {}
    private final FolkModelGeometry geometry=new FolkModelGeometry();
    private final List<Colored> colored=new ArrayList<>();
    private final boolean leader;
    private final ModelPart skin,head,hair,hair2,armor,trim,cloth,eye,pupil;
    private final ModelPart armL,guardL,armR,guardR,spear,shield,shieldTrim,handGripL,handGripR,crown;
    private final ModelPart tailA,tailB,tailC,fin;
    private ModelPart coralCrown;
    private ModelPart tridentBand;
    private final ModelPart faceFins,faceMarks,bodyFins,bodyPearls,shieldShell;

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
        faceFins=create(FIN,0,-9,0);
        faceMarks=create(CORAL,0,-9,0);
        bodyFins=create(SCALE,0,24,0);
        bodyPearls=create(SHELL,0,24,0);
        shieldShell=create(SHELL,6,3,0);
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
        // Gill-frilled ears, brow crests and fine cheek markings read as an
        // enchanted freshwater rusalka, not an ordinary armored villager.
        for(int side:new int[]{-1,1}){
            for(int row=0;row<4;row++){
                float x=side*(3.7F+row*.73F);
                float y=-8.0F+row*.9F;
                box(faceFins,x-.62F,y,-.9F+row*.62F,
                    1.3F,3.5F-row*.37F,2.2F);
                if(row<3)box(faceMarks,side*(3.8F+row*.62F)-.4F,
                    -6.35F+row*.67F,-3.45F+row*.35F,.8F,.46F,1.05F);
            }
            box(faceMarks,side*1.8F-1F,-8.9F,-4.11F,2F,.45F,.55F);
            box(faceFins,side*4.3F-1.1F,-1.8F,.2F,2.2F,2F,3.3F);
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
            // Shoulder fins join the arms, below the wavy shoulder-length hair.
            for(int j=0;j<5;j++){
                float x=s*(4.0F+j*1.16F);
                float y=-32.2F+j*1.15F;
                box(bodyFins,x-1.2F,y,-3.0F+j*.28F,2.45F,2.3F,4.0F-j*.38F);
            }
            // Pelvic finlets flare from the corslet into the actual fish tail.
            for(int j=0;j<6;j++){
                float x=s*(3.6F+j*.9F);
                box(bodyFins,x-1.1F,-17F+j*1.45F,-2.5F+j*.2F,
                    2.2F,3.0F-j*.22F,2.8F);
            }
            for(int j=0;j<3;j++)box(bodyPearls,s*(2.2F+j*1.1F)-.45F,
                -31.5F+j*3F,-6.17F,.9F,.9F,.9F);
        }
        // A mother-of-pearl throat collar and flowing scale patterns.
        for(int j=0;j<7;j++){
            float x=-3.3F+j*1.1F;
            box(bodyPearls,x-.4F,-34.8F+1.5F*Mth.abs(x)/3.3F,-6.25F,
                .9F,.85F,.85F);
        }
        for(int j=0;j<4;j++)
            box(bodyFins,-2.1F+j*.9F,-26.5F+j*1.75F,-5.52F,
                1.4F,2.1F,1.3F);
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
        // Side fins at the hips and overlapping scale bands reinforce the fish
        // silhouette even in shallow water, where the bottom fluke is hidden.
        for(int side:new int[]{-1,1}){
            for(int i=0;i<7;i++){
                float x=side*(3.2F+i*1.05F);
                box(tailA,x-1.0F,2.1F+i*.52F,-2F,2F,4.6F-i*.4F,2.8F);
            }
            for(int i=0;i<6;i++){
                float x=side*(2.0F+i*1.55F);
                box(fin,x-1.2F,1.3F+i*.25F,-1.7F,2.4F,7.9F-i*.68F,2.8F);
            }
        }
        for(int row=0;row<6;row++)
            for(int x=-3;x<=3;x+=2)
                box(tailA,x-.45F,-2F+row*2.1F,-3F+row*.15F,
                    .95F,.85F,.75F);
        if (leader) {
            // Crown ring sits directly on the hair's scalp at local Y=-12.65.
            // Coral prongs rise from the ring; none has an independent floating pivot.
            box(crown,-4.9F,-12.7F,-4.15F,9.8F,1.55F,8.3F);
            box(crown,-4.2F,-13.35F,-4.45F,8.4F,1.0F,1.3F);
            box(crown,-1.85F,-15.75F,-4.65F,3.7F,3.15F,1.45F);
            box(crown,-4.2F,-12.65F,3F,8.4F,1.1F,1.1F);
            ModelPart coral=create(CORAL,0,-9,0);
            coralCrown=coral;
            for(int side:new int[]{-1,1})for(int i=0;i<3;i++){
                float x=side*(2.1F+i*1.1F);
                box(coral,x-.56F,-14.65F-i*.52F,-2.85F,
                    1.12F,2.8F+i*.72F,1.25F);
                box(coral,x-.5F,-12.65F,1.7F,1.0F,1.8F+i*.4F,1.3F);
            }
            // An eight-ribbed scallop shield follows a wrist displaced to
            // the side of the torso. Curved edges replace the giant rectangular plate.
            for(int rib=0;rib<8;rib++){
                float t=rib/7F;
                float half=2.2F+2.25F*Mth.sin(t*Mth.PI);
                float y=-10.0F+rib*2.0F;
                float z=-3.8F-.7F*Mth.sin(t*Mth.PI);
                box(shield,-half,y,z,2*half,2.15F,1.35F);
                box(shieldTrim,-half-.25F,y-.15F,z-.36F,.65F,2.4F,.68F);
                box(shieldTrim,half-.38F,y-.15F,z-.36F,.65F,2.4F,.68F);
                if(rib%2==0)
                    box(shieldShell,-half+.55F,y+.2F,z-.47F,2*half-1.1F,.42F,.45F);
            }
            box(shieldTrim,-2.25F,-10.2F,-4.5F,4.5F,.75F,.8F);
            box(shieldTrim,-2.25F,5.6F,-4.5F,4.5F,.75F,.8F);
            box(shieldShell,-1.15F,-3.8F,-5.15F,2.3F,4.5F,.62F);
            box(shieldShell,-.48F,-6.2F,-5.2F,.96F,9.1F,.5F);
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
            box(shield,-5.35F,-1.2F,-2.3F,3.2F,1.6F,1.5F);
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
        eye.yRot=pupil.yRot=crown.yRot=faceFins.yRot=faceMarks.yRot=head.yRot;
        eye.xRot=pupil.xRot=crown.xRot=faceFins.xRot=faceMarks.xRot=head.xRot;
        if (coralCrown!=null) {
            coralCrown.yRot=head.yRot;
            coralCrown.xRot=head.xRot;
        }
        // Scalp and fringe must follow exactly the head's pivot. Lagging the
        // full hair shell behind head rotation exposes scalp-shaped bald gaps.
        hair.yRot=hair2.yRot=head.yRot;
        hair.xRot=hair2.xRot=head.xRot;
        bodyFins.xRot=bodyPearls.xRot=skin.xRot;

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
                armor.xRot=skin.xRot=trim.xRot=cloth.xRot=bodyFins.xRot=
                    bodyPearls.xRot=-.22F;
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
            followWrist(shield,armR,11F,5.2F,-.9F);
            followWrist(shieldTrim,armR,11F,5.2F,-.9F);
            followWrist(shieldShell,armR,11F,5.2F,-.9F);
            followWrist(handGripR,armR,11F,2.7F,-.8F);
        }
    }

    private static void followWrist(ModelPart item,ModelPart arm,float wristLength) {
        followWrist(item,arm,wristLength,0,0);
    }
    private static void followWrist(ModelPart item,ModelPart arm,float wristLength,
                                    float sideways,float forward) {
        // Wrist position plus a stable hand-local lateral offset. The shield
        // remains outside the chest while pitching and bashing.
        float bend=arm.xRot,spread=arm.zRot;
        float rollCos=Mth.cos(spread),rollSin=Mth.sin(spread);
        item.setPos(arm.x-wristLength*rollSin+sideways*rollCos,
            arm.y+wristLength*Mth.cos(bend)*rollCos+sideways*rollSin,
            arm.z+wristLength*Mth.sin(bend)*rollCos+forward);
        item.xRot=bend;
        item.yRot=arm.yRot;
        item.zRot=spread;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer consumer,int light,int overlay,int rgba) {
        int alpha=(rgba>>>24)&255;
        for (Colored p:colored) p.part().render(pose,consumer,light,overlay,(alpha<<24)|(p.color()&0xFFFFFF));
    }
}