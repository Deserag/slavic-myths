package org.slavicmyths.auka.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.client.FolkModelGeometry;
import org.slavicmyths.auka.AukaBoss;

/** Three separately authored voxel silhouettes; joint rotations follow the server attack timeline. */
public final class AukaBossModel extends EntityModel<AukaBoss> {
    private final FolkModelGeometry geometry=new FolkModelGeometry();
    private ModelPart part(ModelPart parent,float x,float y,float z){
        ModelPart p=geometry.part(1024,1024);p.setPos(x,y,z);
        if(parent!=null)geometry.attach(parent,p);return p;
    }
    private void cube(ModelPart p,int material,float x,float y,float z,float w,float h,float d){
        geometry.box(p,(material%8)*128+4,(material/8)*128+4,x,y,z,w,h,d);
    }
    private final ModelPart root=part(null,0,0,0);
    private final ModelPart first=part(root,0,0,0),second=part(root,0,0,0);
    private final ModelPart head1=part(first,0,-38,-2), head2=part(second,0,-47,-5);
    private final ModelPart arm1L=part(first,12,-30,0),arm1R=part(first,-12,-30,0);
    private final ModelPart leg1L=part(first,5,-15,0),leg1R=part(first,-5,-15,0);
    private final ModelPart staff1=part(arm1R,0,16,-2);
    private final ModelPart arm2L=part(second,9,-35,-3),arm2R=part(second,-9,-35,-3);
    private final ModelPart elbow2L=part(arm2L,0,15,-2),elbow2R=part(arm2R,0,15,-2);
    private final ModelPart leg2L=part(second,5,-16,0),leg2R=part(second,-5,-16,0);
    private final ModelPart staff2=part(elbow2R,0,16,-3);
    private final AukaAnimalModel<AukaBoss> wolf=new AukaAnimalModel<>(false);
    public AukaBossModel(){
        // First form: rounded belly, patched red hood, white beard, furs and bear-paw pomelo.
        cube(first,2,-10,-33,-6,20,18,13);cube(first,3,-11,-20,-7,22,11,14);
        cube(first,6,-10,-27,-10,20,15,8);cube(first,6,-11,-21,-12,22,9,6);
        cube(first,3,-12,-19,-8,24,3,15);cube(first,15,-3,-20,-13,6,3,2);
        cube(first,4,-13,-37,-7,26,8,15);
        for(int i=0;i<7;i++){
            cube(first,i%2==0?4:5,-12+i*3.8F,-33+(i%3),-10,3,8,4);
            cube(first,i%2==0?8:15,-11+i*3.5F,-34,6,3,5,3);
        }
        cube(head1,0,-7,-9,-6,14,12,12);
        cube(head1,2,-9,-10,-7,18,4,15);cube(head1,3,-9,-6,5,18,9,3);
        cube(head1,1,-8,-3,-9,5,5,5);cube(head1,1,3,-3,-9,5,5,5);
        cube(head1,1,-3,-4,-12,6,7,5);
        cube(head1,4,-6,2,-11,12,9,5);
        for(int i=0;i<5;i++)cube(head1,i%2==0?4:5,-5+i*2.2F,7,-12,1.8F,8+(i%3),3);
        for(int s:new int[]{-1,1}){
            cube(head1,0,s*8-2,-3,-4,4,5,7);
            cube(head1,14,s*3-1,-3,-10,2,2,1);
            cube(head1,13,s*3-.5F,-3,-11,1,1,1);
        }
        for(ModelPart a:new ModelPart[]{arm1L,arm1R}){
            cube(a,2,-4,0,-5,8,15,10);
            cube(a,4,-4,10,-5,8,4,10);
            cube(a,0,-3,14,-4,6,5,8);
        }
        for(ModelPart l:new ModelPart[]{leg1L,leg1R}){
            cube(l,3,-4,0,-5,8,12,10);cube(l,7,-5,11,-7,10,5,13);
        }
        bearStaff(staff1);
        // Second form: tall, lowered shoulders, long independent forearms and legs.
        cube(second,7,-6,-35,-4,12,23,11);
        cube(second,6,-8,-40,-6,16,13,14);
        cube(second,4,-13,-41,-8,20,8,16);
        cube(second,5,-13,-42,-8,12,7,15);
        cube(second,3,-7,-19,-6,14,14,12);
        for(int i=0;i<5;i++){
            cube(second,i%2==0?3:7,-7+i*3,-15-(i%3),-6,3,10,9);
        }
        cube(head2,0,-7,-9,-7,14,13,13);
        cube(head2,1,-8,-1,-10,5,5,5);cube(head2,1,3,-1,-10,5,5,5);
        cube(head2,1,-3,-3,-13,6,7,6);
        cube(head2,4,-6,4,-12,12,9,5);
        for(int i=0;i<5;i++)cube(head2,i%2==0?4:5,-5+i*2.2F,9,-12,2,7+(i%2)*3,3);
        for(int s:new int[]{-1,1}){
            cube(head2,0,s*7-2,-16,-4,4,13,6);
            cube(head2,2,s*7-1,-15,-5,2,10,2);
            cube(head2,14,s*3-1,-3,-12,2,2,1);
            cube(head2,13,s*3-.5F,-3,-13,1,1,1);
        }
        for(ModelPart a:new ModelPart[]{arm2L,arm2R}){
            cube(a,6,-3,0,-4,6,18,9);
        }
        for(ModelPart e:new ModelPart[]{elbow2L,elbow2R}){
            cube(e,0,-3,0,-4,6,16,8);
            cube(e,0,-3,15,-5,6,5,9);
            for(int j=-2;j<=2;j+=2)cube(e,1,j-1,19,-7,2,4,4);
        }
        for(ModelPart l:new ModelPart[]{leg2L,leg2R}){
            cube(l,7,-4,0,-4,8,12,9);
            cube(l,6,-4,10,-5,8,7,10);
            cube(l,7,-5,15,-7,10,3,13);
        }
        bearStaff(staff2);
    }
    private void bearStaff(ModelPart p){
        cube(p,6,-2,-31,-2,4,57,4);
        cube(p,8,-3,-18,-3,6,5,6);
        cube(p,7,-6,-32,-5,12,9,9);
        for(int i=0;i<4;i++){
            cube(p,9,-6+i*3,-41,-5,3,11,4);
            cube(p,9,-6+i*3,-44,-7,3,5,4);
        }
        cube(p,15,-3,12,-3,6,4,6);
    }
    @Override public void setupAnim(AukaBoss e,float walk,float amount,float age,float yaw,float pitch){
        first.visible=e.form()==AukaBoss.Form.HUT_SPIRIT;
        second.visible=e.form()==AukaBoss.Form.STALKER;
        float t=e.animationTicks(age-e.tickCount);
        if(e.wolf()){
            wolf.pose(walk,amount,age,yaw,pitch,e.move(),t,true);
            return;
        }
        ModelPart head=first.visible?head1:head2;
        head.xRot=Mth.clamp(pitch,-30,30)*Mth.DEG_TO_RAD*.5F;
        head.yRot=Mth.clamp(yaw,-35,35)*Mth.DEG_TO_RAD*.5F;
        ModelPart left=first.visible?arm1L:arm2L,right=first.visible?arm1R:arm2R;
        ModelPart l=first.visible?leg1L:leg2L,r=first.visible?leg1R:leg2R;
        left.xRot=Mth.cos(walk*.62F)*amount*.48F;right.xRot=Mth.cos(walk*.62F+Mth.PI)*amount*.48F;
        l.xRot=Mth.cos(walk*.65F)*amount*.56F;r.xRot=Mth.cos(walk*.65F+Mth.PI)*amount*.56F;
        elbow2L.xRot=.12F;elbow2R.xRot=.15F;staff1.xRot=0;staff2.xRot=0;
        first.xRot=second.xRot=0;first.zRot=second.zRot=0;
        AukaBoss.Move attack=e.move();
        if(attack==AukaBoss.Move.STAFF||attack==AukaBoss.Move.SWEEP||attack==AukaBoss.Move.CLAW){
            float progress=Mth.sin(Mth.PI*Mth.clamp(t/25F,0,1));
            right.xRot=-.75F*progress;right.zRot=.6F*progress;
            if(!first.visible)elbow2R.xRot=-.7F*progress;
            (first.visible?staff1:staff2).xRot=-.5F*progress;
            (first.visible?first:second).yRot=.12F*progress;
        } else {
            right.zRot=0;first.yRot=second.yRot=0;
        }
        if(attack==AukaBoss.Move.CHARGE||attack==AukaBoss.Move.SIDE_LUNGE){
            float a=Mth.sin(Mth.PI*Mth.clamp((t-8)/27F,0,1));
            (first.visible?first:second).xRot=.30F*a;left.xRot=-.42F*a;right.xRot=-.6F*a;
        }
        if(attack==AukaBoss.Move.STOMP){
            l.xRot=-.8F*Mth.sin(Mth.PI*Mth.clamp(t/22F,0,1));
        }
        if(attack==AukaBoss.Move.BEAR_CALL||attack==AukaBoss.Move.FROST_BREATH||attack==AukaBoss.Move.PHASE_CHANGE){
            head.xRot-=.23F*Mth.sin(Mth.PI*Mth.clamp(t/45F,0,1));
            left.xRot=-.40F;
        }
    }
    @Override public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){
        if(!first.visible&&!second.visible)wolf.renderToBuffer(pose,out,light,overlay,color);
        else root.render(pose,out,light,overlay,color);
    }
}
