package org.slavicmyths.client;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.Hand;
import org.slavicmyths.bandit.BanditEntity;
/** Standard 8x8 head, 8x12 torso, 4x12 limbs; added clothing remains child geometry. */
public final class BanditModel extends BipedModel<BanditEntity> {
    private final ModelRenderer apron;
    public BanditModel(int role){super(0,0,64,64);apron=new ModelRenderer(this,0,32);apron.addBox(-4,1,-2.8F,8,14,1);body.addChild(apron);
        if(role==BanditEntity.ARCHER){part(head,0,32,-4.5F,-8.5F,-4.5F,9,2,9);part(head,0,32,-4.5F,-6.5F,3.5F,9,7,1);part(head,0,32,-4.5F,-6.5F,-4.5F,1,7,8);part(head,0,32,3.5F,-6.5F,-4.5F,1,7,8);part(body,40,32,1,1,2,3,10,3);part(body,54,32,2,-2,3,1,5,1);}
        if(role>=BanditEntity.HEAVY){part(head,0,32,-4.5F,-9,-4.5F,9,3,9);part(body,32,16,-4.5F,0,-2.5F,9,11,5);part(body,0,48,-5,-1,-3,10,3,6);part(head,48,48,-3,-1,-4.5F,6,3,2);}
        if(role==BanditEntity.FIGHTER||role==BanditEntity.SENIOR)part(body,32,48,-4,10,-2.5F,8,3,5);
        part(body,0,48,-4.5F,8,-2.5F,9,2,5);part(body,48,32,4,7,-2,3,4,3);
        if(role==BanditEntity.ATAMAN){part(body,52,48,-6,7,-1,2,6,2);part(body,28,48,-1,8,-3,2,2,1);}
    }
    private void part(ModelRenderer parent,int u,int v,float x,float y,float z,float w,float h,float d){ModelRenderer p=new ModelRenderer(this,u,v);p.addBox(x,y,z,w,h,d);parent.addChild(p);}
    @Override public void setupAnim(BanditEntity e,float walk,float speed,float age,float yaw,float pitch){
        rightArmPose=e.action()==5?ArmPose.BOW_AND_ARROW:ArmPose.ITEM;leftArmPose=e.isUsingItem()&&e.getUsedItemHand()==Hand.OFF_HAND?ArmPose.BLOCK:ArmPose.EMPTY;
        super.setupAnim(e,walk,speed,age,yaw,pitch);
        hat.visible=false;apron.visible=e.duty()==3;
        if(e.action()==1||e.action()==7){rightArm.xRot=e.action()==7?-2.65F:-1.85F;rightArm.zRot=.18F;body.yRot=-.12F;}
        if(e.action()==6){leftArm.xRot=-1.5F;leftArm.zRot=-.5F;}
    }
}
