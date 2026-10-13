package org.slavicmyths.client;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.InteractionHand;
import org.slavicmyths.bandit.BanditEntity;
/** Standard 8x8 head, 8x12 torso, 4x12 limbs; added clothing remains child geometry. */
public final class BanditModel extends HumanoidModel<BanditEntity> {
    private final ModelPart apron;
    public BanditModel(int role){super(root(role));apron=body.getChild("apron");}
    private static ModelPart root(int role){
        var mesh=HumanoidModel.createMesh(net.minecraft.client.model.geom.builders.CubeDeformation.NONE,0);
        var root=mesh.getRoot();int[] partIndex={0};
        root.getChild("body").addOrReplaceChild("apron",net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(0,32).addBox(-4,1,-2.8F,8,14,1),net.minecraft.client.model.geom.PartPose.ZERO);
if(role==BanditEntity.ARCHER){part(root.getChild("head"),partIndex,0,32,-4.5F,-8.5F,-4.5F,9,2,9);part(root.getChild("head"),partIndex,0,32,-4.5F,-6.5F,3.5F,9,7,1);part(root.getChild("head"),partIndex,0,32,-4.5F,-6.5F,-4.5F,1,7,8);part(root.getChild("head"),partIndex,0,32,3.5F,-6.5F,-4.5F,1,7,8);part(root.getChild("body"),partIndex,40,32,1,1,2,3,10,3);part(root.getChild("body"),partIndex,54,32,2,-2,3,1,5,1);}
        if(role>=BanditEntity.HEAVY){part(root.getChild("head"),partIndex,0,32,-4.5F,-9,-4.5F,9,3,9);part(root.getChild("body"),partIndex,32,16,-4.5F,0,-2.5F,9,11,5);part(root.getChild("body"),partIndex,0,48,-5,-1,-3,10,3,6);part(root.getChild("head"),partIndex,48,48,-3,-1,-4.5F,6,3,2);}
        if(role==BanditEntity.FIGHTER||role==BanditEntity.SENIOR)part(root.getChild("body"),partIndex,32,48,-4,10,-2.5F,8,3,5);
        part(root.getChild("body"),partIndex,0,48,-4.5F,8,-2.5F,9,2,5);part(root.getChild("body"),partIndex,48,32,4,7,-2,3,4,3);
        if(role==BanditEntity.ATAMAN){part(root.getChild("body"),partIndex,52,48,-6,7,-1,2,6,2);part(root.getChild("body"),partIndex,28,48,-1,8,-3,2,2,1);}
        return net.minecraft.client.model.geom.builders.LayerDefinition.create(mesh,64,64).bakeRoot();
    }
    private static void part(net.minecraft.client.model.geom.builders.PartDefinition parent,int[] partIndex,int u,int v,float x,float y,float z,float w,float h,float d){
        parent.addOrReplaceChild("clothing_"+partIndex[0]++,net.minecraft.client.model.geom.builders.CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d),net.minecraft.client.model.geom.PartPose.ZERO);
    }
    @Override public void setupAnim(BanditEntity e,float walk,float speed,float age,float yaw,float pitch){
        rightArmPose=e.action()==5?ArmPose.BOW_AND_ARROW:ArmPose.ITEM;leftArmPose=e.isUsingItem()&&e.getUsedItemHand()==InteractionHand.OFF_HAND?ArmPose.BLOCK:ArmPose.EMPTY;
        super.setupAnim(e,walk,speed,age,yaw,pitch);
        hat.visible=false;apron.visible=e.duty()==3;
        if(e.action()==1||e.action()==7){rightArm.xRot=e.action()==7?-2.65F:-1.85F;rightArm.zRot=.18F;body.yRot=-.12F;}
        if(e.action()==6){leftArm.xRot=-1.5F;leftArm.zRot=-.5F;}
    }
}
