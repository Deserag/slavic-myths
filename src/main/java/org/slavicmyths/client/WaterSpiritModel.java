package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.water.WaterSpirit;
public final class WaterSpiritModel extends EntityModel<WaterSpirit>{
 private final ModelRenderer body,head,left,right,legL,legR,hair,mouth;private final boolean rusalka;
 private ModelRenderer part(ModelRenderer parent,int u,int v,float x,float y,float z,float a,float b,float c){ModelRenderer m=new ModelRenderer(this,u,v);m.addBox(x,y,z,a,b,c);if(parent!=null)parent.addChild(m);return m;}
 public WaterSpiritModel(boolean r){rusalka=r;texWidth=256;texHeight=256;
  body=part(null,0,32,r?-3.5F:-6,0,-3,r?7:12,r?11:12,6);body.setPos(0,r?1:3,0);
  head=part(body,0,0,r?-3.5F:-4.5F,-8,-4,r?7:9,8,r?7:8);head.setPos(0,0,r?0:-1.5F);
  part(head,64,0,-2.5F,-5,-4.2F,1.2F,.6F,.3F);part(head,64,0,1.3F,-5,-4.2F,1.2F,.6F,.3F);
  mouth=part(head,64,0,-1,-2,-4.25F,2,.35F,.3F);
  if(r){hair=part(head,64,32,-3.9F,-8.3F,1.3F,7.8F,18,3);part(hair,64,32,-.2F,0,-4,1.5F,13,3);part(hair,64,32,6,0,-3,1.5F,15,2);part(body,0,32,-4,10,-3.5F,8,11,7);part(body,0,32,-4.5F,18,-3.8F,9,3,7.6F);}
  else{part(head,0,0,-1.7F,-4.3F,-6,3.4F,2.8F,2.2F);part(head,64,32,-3.5F,-2,-4.5F,7,9,3);part(head,64,32,-1.5F,5,-4,3,5,2);hair=part(head,64,32,-5,-8.5F,1,10,10,4);part(body,64,32,-6.5F,-.5F,-2,3,5,5);part(body,64,32,4,-.5F,-2,3,4,5);part(body,32,32,-6.2F,9,-3.2F,12.4F,1.3F,6.4F);}
  left=part(body,0,0,0,0,-2,r?2.5F:3.5F,r?13:15,4);left.setPos(r?3.5F:6,0,0);right=part(body,0,0,r?-2.5F:-3.5F,0,-2,r?2.5F:3.5F,r?13:15,4);right.setPos(r?-3.5F:-6,0,0);
  legL=part(body,0,0,-1.8F,0,-2,3.6F,r?12:9,4);legL.setPos(r?1.8F:3,11,0);legR=part(body,0,0,-1.8F,0,-2,3.6F,r?12:9,4);legR.setPos(r?-1.8F:-3,11,0);
 }
 public void setupAnim(WaterSpirit e,float walk,float amount,float age,float yaw,float pitch){head.yRot=yaw*.01745F;head.xRot=pitch*.01745F;body.xRot=e.isInWater()?-.3F:rusalka?0:.2F;float wave=MathHelper.sin(age*.07F);hair.xRot=wave*.04F;legL.xRot=MathHelper.cos(walk*.5F)*amount*.6F;legR.xRot=-legL.xRot;left.xRot=-legL.xRot;right.xRot=legL.xRot;left.zRot=.08F;right.zRot=-.08F;mouth.yRot=0;mouth.xRot=0;mouth.y=0;
  if(rusalka&&(e.state()==2||e.state()==4)){left.xRot=-.35F;right.xRot=-.35F;left.zRot=.15F+wave*.05F;right.zRot=-left.zRot;mouth.y=.2F+wave*.1F;}else if(e.getTarget()!=null){left.xRot=right.xRot=-.8F+wave*.2F;}
 }
 public void renderToBuffer(MatrixStack m,IVertexBuilder v,int l,int o,float r,float g,float b,float a){body.render(m,v,l,o,r,g,b,a);}
}
