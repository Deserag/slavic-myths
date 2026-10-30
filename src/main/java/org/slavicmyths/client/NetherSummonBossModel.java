package org.slavicmyths.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.netherboss.NetherSummonBoss;

/** Generated from owner-approved Nether boss editable .bbmodel sources.
 * Phase, action and walk are client-rendered only; server handles collision and hits.
 * Original audio, projectile VFX, robe physics and full owner-art fidelity remain WIP.
 */
public final class NetherSummonBossModel extends EntityModel<NetherSummonBoss> {
    private final ModelPart root,body,head,leftArm,rightArm,leftLeg,rightLeg,cape;
    private final boolean morova;
    public float opacity=1;
    public NetherSummonBossModel(boolean morova) {
        this.morova=morova;
        var geometry=new FolkModelGeometry();
        root=geometry.part(128,256);
        if(morova) {
        ModelPart part_body=geometry.part(128,256);part_body.setPos(0F,-3.296F,0F);geometry.attach(root,part_body);
        geometry.box(part_body,0,0,-4.2F,-5.459F,-3.5F,8.4F,10.918F,7F);
        geometry.box(part_body,0,192,-4.4F,-4.549F,-3.9F,8.8F,6.369F,7.8F);
        geometry.box(part_body,0,0,-3.4F,-4.549F,-4.15F,6.8F,7.278F,0.3F);
        geometry.box(part_body,0,0,-3.4F,1.82F,-3.6F,6.8F,4.549F,7.2F);
        geometry.box(part_body,0,96,-3.7F,3.639F,-4.35F,7.4F,0.91F,0.6F);
        geometry.box(part_body,0,96,-1.1F,-3.185F,-4.7F,2.2F,2.275F,0.7F);
        ModelPart part_head=geometry.part(128,256);part_head.setPos(0F,-7.845F,0F);geometry.attach(root,part_head);
        geometry.box(part_head,0,64,-3.7F,-7.279F,-3.5F,7.4F,6.369F,7F);
        geometry.box(part_head,0,96,-2.7F,-4.368F,-3.78F,1.3F,0.728F,0.32F);
        geometry.box(part_head,0,96,1.4F,-4.368F,-3.78F,1.3F,0.728F,0.32F);
        geometry.box(part_head,0,32,-1.3F,-2.457F,-3.76F,2.6F,0.364F,0.3F);
        ModelPart part_left_arm=geometry.part(128,256);part_left_arm.setPos(6F,-6.026F,0F);geometry.attach(root,part_left_arm);
        geometry.box(part_left_arm,0,64,-1.6F,-0.91F,-2.5F,3.2F,10.919F,5F);
        geometry.box(part_left_arm,0,0,-2.1F,0.91F,-3.1F,4.2F,9.099F,6.2F);
        geometry.box(part_left_arm,0,0,-2.6F,9.099F,-3.3F,5.2F,5.459F,6.6F);
        geometry.box(part_left_arm,0,96,-0.9F,2.73F,-3.8F,0.8F,13.648F,0.6F);
        geometry.box(part_left_arm,0,64,-1.1F,12.738F,-2.1F,2.2F,3.64F,4.2F);
        geometry.box(part_left_arm,0,128,-1.3F,15.104F,-2.1F,0.45F,3.731F,0.5F);
        geometry.box(part_left_arm,0,128,-0.6F,15.104F,-2.1F,0.45F,3.731F,0.5F);
        geometry.box(part_left_arm,0,128,0.1F,15.104F,-2.1F,0.45F,3.731F,0.5F);
        ModelPart part_right_arm=geometry.part(128,256);part_right_arm.setPos(-6F,-6.026F,0F);geometry.attach(root,part_right_arm);
        geometry.box(part_right_arm,0,64,-1.6F,-0.91F,-2.5F,3.2F,10.919F,5F);
        geometry.box(part_right_arm,0,0,-2.1F,0.91F,-3.1F,4.2F,9.099F,6.2F);
        geometry.box(part_right_arm,0,0,-2.6F,9.099F,-3.3F,5.2F,5.459F,6.6F);
        geometry.box(part_right_arm,0,96,-0.9F,2.73F,-3.8F,0.8F,13.648F,0.6F);
        geometry.box(part_right_arm,0,64,-1.1F,12.738F,-2.1F,2.2F,3.64F,4.2F);
        geometry.box(part_right_arm,0,128,-1.3F,15.104F,-2.1F,0.45F,3.731F,0.5F);
        geometry.box(part_right_arm,0,128,-0.6F,15.104F,-2.1F,0.45F,3.731F,0.5F);
        geometry.box(part_right_arm,0,128,0.1F,15.104F,-2.1F,0.45F,3.731F,0.5F);
        ModelPart part_left_leg=geometry.part(128,256);part_left_leg.setPos(2F,7.622F,0F);geometry.attach(root,part_left_leg);
        ModelPart part_right_leg=geometry.part(128,256);part_right_leg.setPos(-2F,7.622F,0F);geometry.attach(root,part_right_leg);
        ModelPart part_hair=geometry.part(128,256);part_hair.setPos(0F,-13.305F,0F);geometry.attach(root,part_hair);
        geometry.box(part_hair,0,32,-4.2F,-2.729F,-4F,8.4F,2.729F,8F);
        geometry.box(part_hair,0,32,-4.3F,-1.547F,-4.2F,1.6F,7.007F,1F);
        geometry.box(part_hair,0,32,2.7F,-1.547F,-4.2F,1.6F,7.007F,1F);
        geometry.box(part_hair,0,32,-4.3F,-0.728F,3.2F,8.6F,20.745F,0.9F);
        geometry.box(part_hair,0,32,-5.3F,2.73F,-1.7F,1.8F,16.378F,5.7F);
        geometry.box(part_hair,0,32,3.5F,2.73F,-1.7F,1.8F,20.017F,5.7F);
        geometry.box(part_hair,0,128,-4.5F,-3.275F,-3.6F,9F,1.092F,7.2F);
        geometry.box(part_hair,0,128,-3.85F,-6.005F,-2F,0.9F,3.185F,0.9F);
        geometry.box(part_hair,0,128,-2.15F,-5.004F,-2F,0.9F,2.184F,0.9F);
        geometry.box(part_hair,0,128,-0.45F,-6.005F,-2F,0.9F,3.185F,0.9F);
        geometry.box(part_hair,0,128,1.25F,-5.004F,-2F,0.9F,2.184F,0.9F);
        geometry.box(part_hair,0,128,2.95F,-6.005F,-2F,0.9F,3.185F,0.9F);
        ModelPart part_dress=geometry.part(128,256);part_dress.setPos(0F,2.163F,0F);geometry.attach(root,part_dress);
        geometry.box(part_dress,0,0,-5.5F,-0.91F,-4.2F,11F,18.198F,8.4F);
        geometry.box(part_dress,0,192,-6.8F,6.369F,-5.2F,13.6F,12.738F,10.4F);
        geometry.box(part_dress,0,0,-7.5F,0.91F,-5.5F,1.8F,18.197F,11F);
        geometry.box(part_dress,0,192,-5.5F,1.82F,-5.8F,1.8F,15.923F,11.3F);
        geometry.box(part_dress,0,0,-3.5F,2.73F,-5.5F,1.8F,13.648F,11F);
        geometry.box(part_dress,0,0,-1.5F,0.91F,-5.8F,1.8F,18.197F,11.3F);
        geometry.box(part_dress,0,192,0.5F,1.82F,-5.5F,1.8F,15.923F,11F);
        geometry.box(part_dress,0,0,2.5F,2.73F,-5.8F,1.8F,13.648F,11.3F);
        geometry.box(part_dress,0,0,4.5F,0.91F,-5.5F,1.8F,18.197F,11F);
        geometry.box(part_dress,0,192,6.5F,1.82F,-5.8F,1.8F,15.923F,11.3F);
        geometry.box(part_dress,0,96,-5F,-0.91F,-6F,1F,16.378F,0.8F);
        geometry.box(part_dress,0,96,-1F,-0.91F,-6F,1F,16.378F,0.8F);
        geometry.box(part_dress,0,96,3F,-0.91F,-6F,1F,16.378F,0.8F);
        geometry.box(part_dress,0,160,-5F,14.558F,4.5F,2F,6.369F,0.7F);
        geometry.box(part_dress,0,160,3F,14.558F,4.5F,2F,6.369F,0.7F);
        body=part_body;head=part_head;leftArm=part_left_arm;rightArm=part_right_arm;leftLeg=part_left_leg;rightLeg=part_right_leg;cape=part_dress;
        } else {
        ModelPart part_body=geometry.part(128,256);part_body.setPos(0F,-10F,0F);geometry.attach(root,part_body);
        geometry.box(part_body,0,0,-7.6F,-5F,-4.5F,15.2F,16F,9F);
        geometry.box(part_body,0,32,-6.3F,-3F,-5.3F,12.6F,11F,0.8F);
        geometry.box(part_body,0,224,-1.3F,-2F,-5.8F,2.6F,6F,0.6F);
        geometry.box(part_body,0,96,-5F,1F,-5.7F,0.8F,6F,0.43F);
        geometry.box(part_body,0,96,-1F,0.4F,-5.7F,0.8F,6F,0.43F);
        geometry.box(part_body,0,96,3F,-0.2F,-5.7F,0.8F,6F,0.43F);
        geometry.box(part_body,0,32,-7.8F,8F,-5F,15.6F,3F,10F);
        geometry.box(part_body,0,224,-2.1F,7.8F,-5.5F,4.2F,2.7F,0.6F);
        geometry.box(part_body,0,160,-10F,-5F,3.6F,20F,3F,2.9F);
        geometry.box(part_body,0,160,-8.2F,-4F,-6F,16.4F,2F,1.2F);
        ModelPart part_head=geometry.part(128,256);part_head.setPos(0F,-15F,0F);geometry.attach(root,part_head);
        geometry.box(part_head,0,32,-4.8F,-7F,-4.5F,9.6F,8F,9F);
        geometry.box(part_head,0,192,-3.6F,-4.5F,-5.1F,7.2F,5.7F,0.6F);
        geometry.box(part_head,0,96,-3.1F,-3F,-5.55F,2F,1F,0.5F);
        geometry.box(part_head,0,96,1.1F,-3F,-5.55F,2F,1F,0.5F);
        geometry.box(part_head,0,64,-2.7F,-0.1F,-5.7F,5.4F,0.9F,0.62F);
        geometry.box(part_head,0,0,-5.1F,-6.2F,-5.1F,10.2F,2.2F,0.8F);
        geometry.box(part_head,0,0,-4.9F,-8F,-4.7F,9.8F,2F,9.4F);
        geometry.box(part_head,0,224,-4F,-9F,-2.4F,0.7F,1.4F,1.2F);
        geometry.box(part_head,0,224,-2F,-8F,-2.4F,0.7F,0.4F,1.2F);
        geometry.box(part_head,0,224,0F,-9F,-2.4F,0.7F,1.4F,1.2F);
        geometry.box(part_head,0,224,2F,-8F,-2.4F,0.7F,0.4F,1.2F);
        geometry.box(part_head,0,224,4F,-9F,-2.4F,0.7F,1.4F,1.2F);
        ModelPart part_left_arm=geometry.part(128,256);part_left_arm.setPos(8F,-10F,0F);geometry.attach(root,part_left_arm);
        geometry.box(part_left_arm,0,0,-1.6F,-5F,-5.2F,6.6F,8F,10.4F);
        geometry.box(part_left_arm,0,160,-2.1F,-5.4F,-5.7F,7.6F,3.4F,11.4F);
        geometry.box(part_left_arm,0,32,-0.6F,1F,-4.1F,4.6F,15F,8.2F);
        geometry.box(part_left_arm,0,64,-1F,9F,-4.5F,5.4F,2F,9F);
        geometry.box(part_left_arm,0,0,-1.2F,12F,-4.4F,5.8F,5F,8.8F);
        geometry.box(part_left_arm,0,96,-0.1F,3F,-4.8F,1F,4F,0.6F);
        ModelPart part_right_arm=geometry.part(128,256);part_right_arm.setPos(-8F,-10F,0F);geometry.attach(root,part_right_arm);
        geometry.box(part_right_arm,0,0,-5F,-5F,-5.2F,6.6F,8F,10.4F);
        geometry.box(part_right_arm,0,160,-5.5F,-5.4F,-5.7F,7.6F,3.4F,11.4F);
        geometry.box(part_right_arm,0,32,-4F,1F,-4.1F,4.6F,15F,8.2F);
        geometry.box(part_right_arm,0,64,-4.4F,9F,-4.5F,5.4F,2F,9F);
        geometry.box(part_right_arm,0,0,-4.6F,12F,-4.4F,5.8F,5F,8.8F);
        geometry.box(part_right_arm,0,96,-3.5F,3F,-4.8F,1F,4F,0.6F);
        ModelPart part_left_leg=geometry.part(128,256);part_left_leg.setPos(4F,5F,0F);geometry.attach(root,part_left_leg);
        geometry.box(part_left_leg,0,32,-2.8F,-5F,-3.8F,5F,23F,7.6F);
        geometry.box(part_left_leg,0,0,-3.3F,-1F,-4.4F,6F,13F,8.8F);
        geometry.box(part_left_leg,0,64,-3.3F,4F,-5F,6F,4F,9F);
        geometry.box(part_left_leg,0,0,-3.5F,12F,-5.5F,6.4F,7F,9.8F);
        ModelPart part_right_leg=geometry.part(128,256);part_right_leg.setPos(-4F,5F,0F);geometry.attach(root,part_right_leg);
        geometry.box(part_right_leg,0,32,-2.2F,-5F,-3.8F,5F,23F,7.6F);
        geometry.box(part_right_leg,0,0,-2.7F,-1F,-4.4F,6F,13F,8.8F);
        geometry.box(part_right_leg,0,64,-2.7F,4F,-5F,6F,4F,9F);
        geometry.box(part_right_leg,0,0,-2.9F,12F,-5.5F,6.4F,7F,9.8F);
        ModelPart part_cape=geometry.part(128,256);part_cape.setPos(0F,-12F,5F);geometry.attach(root,part_cape);
        geometry.box(part_cape,0,128,-8.7F,-2F,0.5F,17.4F,4F,1.5F);
        geometry.box(part_cape,0,128,-9.7F,3F,1.3F,2.9F,31F,1.4F);
        geometry.box(part_cape,0,128,-7F,4F,1.3F,2.9F,27F,2F);
        geometry.box(part_cape,0,128,-4.3F,5F,1.3F,2.9F,23F,1.4F);
        geometry.box(part_cape,0,128,-1.6F,3F,1.3F,2.9F,30F,2F);
        geometry.box(part_cape,0,128,1.1F,4F,1.3F,2.9F,26F,1.4F);
        geometry.box(part_cape,0,128,3.8F,5F,1.3F,2.9F,22F,2F);
        geometry.box(part_cape,0,128,6.5F,3F,1.3F,2.9F,29F,1.4F);
        geometry.box(part_cape,0,128,9.2F,4F,1.3F,2.9F,25F,2F);
        geometry.box(part_cape,0,96,-8.5F,29F,2.8F,1.7F,5F,0.4F);
        geometry.box(part_cape,0,96,-1.5F,26F,2.8F,1.7F,5F,0.4F);
        geometry.box(part_cape,0,96,5.5F,23F,2.8F,1.7F,5F,0.4F);
        body=part_body;head=part_head;leftArm=part_left_arm;rightArm=part_right_arm;leftLeg=part_left_leg;rightLeg=part_right_leg;cape=part_cape;
        }
    }
    @Override public void setupAnim(NetherSummonBoss e,float walk,float walkAmount,float age,float yaw,float pitch){
        head.xRot=pitch*((float)Math.PI/180F);head.yRot=yaw*((float)Math.PI/180F);
        float stride=Mth.clamp(walkAmount,0,1);
        leftLeg.xRot=Mth.cos(walk*.6662F)*1.1F*stride;rightLeg.xRot=Mth.cos(walk*.6662F+(float)Math.PI)*1.1F*stride;
        rightArm.xRot=Mth.cos(walk*.6662F)*.65F*stride;leftArm.xRot=Mth.cos(walk*.6662F+(float)Math.PI)*.65F*stride;
        float breathe=Mth.sin(age*.095F);
        body.xRot=morova?.02F*breathe:.012F*breathe;
        cape.xRot=morova?.04F*Mth.sin(age*.15F):.08F+.06F*Mth.sin(age*.13F);
        if(e.action()!=0){
            float chant=.1F*Mth.sin(age*.4F);
            leftArm.xRot=morova?-1.7F+chant:-1.25F+chant;
            rightArm.xRot=morova?-1.7F-chant:-2.2F-chant;
            head.xRot-=morova?.22F:.12F;
        }
        leftArm.zRot=0;rightArm.zRot=0;
        if(e.action()!=0){
            float progress=Mth.clamp(e.actionFrame()/30F,0,1);
            switch(e.action()){
                case 3 -> {leftArm.xRot=-.4F;leftArm.zRot=-1.4F*progress;rightArm.xRot=-1.1F;}
                case 5,6,10 -> {leftArm.xRot=-2.6F*progress;rightArm.xRot=-2.6F*progress;}
                case 8 -> {rightArm.xRot=-2.8F*progress;leftArm.xRot=-1.3F*progress;body.xRot=.18F*progress;}
                case 9 -> {body.xRot=.35F;leftArm.xRot=.6F;rightArm.xRot=-.8F;}
                case 7,12 -> {leftArm.zRot=-1.3F;rightArm.zRot=1.3F;}
                default -> {leftArm.xRot=-1.6F*progress;rightArm.xRot=-1.6F*progress;}
            }
        }
        if(e.hurtTime>0)body.xRot-=.12F;
        if(e.phase()>=3){cape.xRot*=1.35F;body.zRot=.025F*Mth.sin(age*.22F);}else body.zRot=0;
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer buffer,int light,int overlay,int color){
        root.render(pose,buffer,light,overlay,((int)(((color>>>24)&255)*opacity)<<24)|(color&0xffffff));
    }
}
