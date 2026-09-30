package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.depth.ElderVodyanoy;
public final class ElderVodyanoyModel extends EntityModel<ElderVodyanoy>{
 private final ModelRenderer root,body,head,left,right,legL,legR;private final ModelRenderer[] beard=new ModelRenderer[5];
 private ModelRenderer part(ModelRenderer p,int u,int v,float x,float y,float z,float a,float b,float c){ModelRenderer m=new ModelRenderer(this,u,v);m.addBox(x,y,z,a,b,c);p.addChild(m);return m;}
 public ElderVodyanoyModel(){texWidth=256;texHeight=256;root=new ModelRenderer(this);root.setPos(0,24,0);body=part(root,0,32,-9,-24,-6,18,18,12);part(body,0,0,-8,-17,-9,16,12,5);part(body,0,32,-11,-26,-4,22,8,10);part(body,32,32,-9,-9,-6.5F,18,2,13);
  head=part(body,0,0,-6,-6,-5,12,10,10);head.setPos(0,-25,-4);part(head,0,0,-2,-1,-8,4,4,3);part(head,0,32,-5,-3,-5.8F,4,1.5F,2);part(head,0,32,1,-3,-5.8F,4,1.5F,2);part(head,96,0,-4,-1.6F,-5.5F,1.2F,.8F,.4F);part(head,96,0,2.8F,-1.6F,-5.5F,1.2F,.8F,.4F);part(head,64,0,-3,3,-5.2F,6,.6F,.3F);
  for(int i=0;i<5;i++){beard[i]=part(head,64,32,-1.3F,0,-1,2.6F,11+(i%3)*2,2.6F);beard[i].setPos(-4.4F+i*2.2F,3,-5);if(i%2==0)part(beard[i],96,32,-.7F,6,-1.4F,1.4F,1,1);}
  for(int i=0;i<4;i++)part(head,64,32,-6+i*3,-7,3,3,13+i%2*3,4);part(body,64,32,-11,-26,0,5,9,5);part(body,64,32,7,-25,0,5,11,5);
  left=part(body,0,0,-2,0,-3,6,17,6);left.setPos(10,-23,0);part(left,0,0,-2.5F,15,-4,7,5,8);right=part(body,0,0,-4,0,-3,6,17,6);right.setPos(-10,-23,0);part(right,0,0,-4.5F,15,-4,7,5,8);
  legL=part(root,0,0,-3,0,-3,6,9,7);legL.setPos(4,-9,0);legR=part(root,0,0,-3,0,-3,6,9,7);legR.setPos(-4,-9,0);
 }
 public void setupAnim(ElderVodyanoy e,float walk,float amount,float age,float yaw,float pitch){float breath=MathHelper.sin(age*.07F),prep=e.preparation(0);body.xRot=e.isInWater()?-.32F:.18F+breath*.015F;head.yRot=yaw*.01745F*.6F;head.xRot=pitch*.01745F*.5F;left.xRot=right.xRot=0;left.zRot=.08F;right.zRot=-.08F;legL.xRot=MathHelper.cos(walk*.45F)*amount*.5F;legR.xRot=-legL.xRot;
  if(e.isInWater()){left.xRot=-.5F+MathHelper.sin(age*.13F)*.5F;right.xRot=-.5F-MathHelper.sin(age*.13F)*.5F;legL.xRot=legR.xRot=.35F;}
  if(e.state()==1)right.xRot=-2.2F*prep;else if(e.state()==2){right.yRot=-1.4F*prep;right.xRot=-1;}else right.yRot=0;
  if(e.state()==6){left.xRot=right.xRot=-2.5F*prep;}if(e.state()==5){left.xRot=right.xRot=-1.4F*prep;left.zRot=.5F;right.zRot=-.5F;}if(e.state()==3){left.xRot=right.xRot=1.2F*prep;}
  for(int i=0;i<5;i++){beard[i].xRot=(e.isInWater()?.25F:0)+MathHelper.sin(age*.07F+i)*.055F;beard[i].zRot=MathHelper.sin(age*.05F+i)*.035F;}
 }
 public void renderToBuffer(MatrixStack m,IVertexBuilder v,int l,int o,float r,float g,float b,float a){root.render(m,v,l,o,r,g,b,a);}
}
