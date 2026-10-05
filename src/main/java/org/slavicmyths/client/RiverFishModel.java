package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.slavicmyths.water.RiverFish;
public final class RiverFishModel extends EntityModel<RiverFish> {
 private int texWidth=64,texHeight=32;
 private final FolkModelGeometry geometry=new FolkModelGeometry();
 private final ModelPart root,tail,left,right;private final int kind;
 private ModelPart part(ModelPart parent,int u,int v,float x,float y,float z,float dx,float dy,float dz){ModelPart m=geometry.part(texWidth,texHeight,u,v);geometry.box(m,x,y,z,dx,dy,dz);geometry.attach(parent,m);return m;}
 public RiverFishModel(int k){kind=k;texWidth=256;texHeight=256;root=geometry.part(texWidth,texHeight);root.setPos(0,22,0);
  if(k==0){part(root,0,0,-2,-2,-5,4,4,11);part(root,0,32,-2,-1.5F,-10,4,3,5);part(root,64,0,-2.1F,-1,-8,4.2F,1,1);part(root,0,32,-1.8F,1,-10,3.6F,.5F,4);part(root,32,32,0,-4,2,.3F,3,4);tail=part(root,32,32,0,-3,0,.3F,6,3);tail.setPos(0,0,6);}
  else if(k==1){part(root,0,0,-3,-4,-4,6,8,8);part(root,0,32,-2,-2,-7,4,4,3);part(root,64,0,-2.1F,-1,-6,4.2F,1,1);part(root,32,32,0,-6,-3,.3F,2,7);part(root,0,32,-2.7F,1,-8,.3F,.3F,3);part(root,0,32,2.4F,1,-8,.3F,.3F,3);tail=part(root,32,32,0,-3,0,.3F,6,3);tail.setPos(0,0,4);}
  else{part(root,0,0,-2,-1.5F,-3,4,3,6);part(root,64,0,-1.7F,-2,-3.4F,3.4F,.6F,.6F);for(int i=0;i<3;i++)part(root,0,32,-1.7F+i*.2F,-1,3+i,3.4F-i*.4F,2,1);for(int i=0;i<4;i++){part(root,32,32,-4,.3F,-2+i*1.2F,3,.4F,.4F);part(root,32,32,1,.3F,-2+i*1.2F,3,.4F,.4F);}part(root,32,32,-1.5F,-1,-7,.2F,.2F,4);part(root,32,32,1.3F,-1,-7,.2F,.2F,4);tail=part(root,0,32,-2,-.5F,0,4,1,1.5F);tail.setPos(0,0,6);}
  if(k<2){left=part(root,32,32,0,0,0,3,.3F,3);left.setPos(2,1,-2);right=part(root,32,32,-3,0,0,3,.3F,3);right.setPos(-2,1,-2);}
  else{left=part(root,0,32,0,-1,-4,2,2,4);left.setPos(2,0,-2);right=part(root,0,32,-2,-1,-4,2,2,4);right.setPos(-2,0,-2);part(left,32,32,.1F,-.6F,-5,.5F,1,2);part(right,32,32,-.6F,-.6F,-5,.5F,1,2);}
 }
 public void setupAnim(RiverFish e,float a,float b,float age,float yaw,float pitch){float speed=(float)Math.min(1,e.getDeltaMovement().length()*6);tail.yRot=Mth.sin(age*(kind==0?.12F:.18F))*(.08F+speed*.5F);left.zRot=Mth.sin(age*.13F)*.2F;right.zRot=-left.zRot;root.zRot=e.isInWater()?0:.8F;root.xRot=kind==2?0:Mth.clamp((float)-e.getDeltaMovement().y,-.25F,.25F);}
 public void renderToBuffer(PoseStack m,VertexConsumer v,int l,int o,int color){root.render(m,v,l,o,color);}
}
