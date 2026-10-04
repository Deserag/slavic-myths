package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.hunt.*;
/** 42-cube charred/straw anatomy versus 44-cube digitigrade wolf anatomy. */
public class HuntModel<T extends HuntMob> extends EntityModel<T>{
 private final boolean oven;private final ModelRenderer root,body,head,jaw,left,right,leftFore,rightFore,leftLeg,rightLeg,leftShin,rightShin,eyes,chestGlow,leftGlow,rightGlow;
 private ModelRenderer part(ModelRenderer parent,int material,float x,float y,float z){ModelRenderer p=new ModelRenderer(this,material%4*64,material/4*64);p.setPos(x,y,z);if(parent!=null)parent.addChild(p);return p;}
 private ModelRenderer box(ModelRenderer parent,int mat,float x,float y,float z,float w,float h,float d){ModelRenderer p=part(parent,mat,0,0,0);p.addBox(x,y,z,w,h,d);return p;}
 public HuntModel(boolean oven){this.oven=oven;texWidth=256;texHeight=256;root=part(null,0,0,24,0);body=part(root,0,0,oven?-12:-13,0);body.addBox(oven?-7:-6,oven?-19:-13,-4,oven?14:12,oven?19:13,8);
  head=part(body,0,0,oven?-19:-14,oven?-1:-3);head.addBox(oven?-3.5F:-4,oven?-8:-7,-4,oven?7:8,oven?8:7,7);
  jaw=part(head,oven?2:4,0,0,0);jaw.addBox(-3,oven?-1.5F:-1,oven?-4.2F:-10,6,2,oven?4:9);
  box(head,4,-3,-5,-4.2F,2,2,.4F);box(head,4,1,-5,-4.2F,2,2,.4F);
  eyes=part(head,6,0,0,0);eyes.addBox(-2.8F,-4.6F,-4.6F,1.5F,1.3F,.5F);eyes.addBox(1.3F,-4.6F,-4.6F,1.5F,1.3F,.5F);
  left=part(body,0,oven?8:7,oven?-16:-11,0);right=part(body,0,oven?-8:-7,oven?-16:-11,0);left.addBox(-.5F,0,-2.5F,oven?5:4,oven?9:7,5);right.addBox(oven?-4.5F:-3.5F,0,-2.5F,oven?5:4,oven?9:7,5);
  leftFore=part(left,0,1.5F,oven?8:6,0);rightFore=part(right,0,-1.5F,oven?8:6,0);for(ModelRenderer fore:new ModelRenderer[]{leftFore,rightFore}){fore.addBox(oven?-3:-2.5F,0,-3,oven?6:5,oven?10:8,6);box(fore,oven?2:1,-3,oven?9:7,-3,6,3,6);for(int f=0;f<3;f++){ModelRenderer claw=box(fore,2,-2.7F+f*2,oven?11:9,-4,1.2F,4,1.5F);claw.xRot=-.25F;}}
  leftLeg=part(root,0,oven?3.8F:3,oven?-12:-13,0);rightLeg=part(root,0,oven?-3.8F:-3,oven?-12:-13,0);for(ModelRenderer leg:new ModelRenderer[]{leftLeg,rightLeg})leg.addBox(oven?-2.8F:-2.5F,0,-3,oven?5.6F:5,oven?6:6,6);
  leftShin=part(leftLeg,0,0,5,1);rightShin=part(rightLeg,0,0,5,1);for(ModelRenderer shin:new ModelRenderer[]{leftShin,rightShin}){shin.addBox(-2.3F,0,-2.5F,4.6F,oven?7:8,5);box(shin,oven?2:1,-3,oven?4:5,-5,6,3,8);}
  chestGlow=part(body,3,0,0,0);leftGlow=part(leftFore,3,0,0,0);rightGlow=part(rightFore,3,0,0,0);
  if(oven){for(int side:new int[]{-1,1})for(int bundle=0;bundle<3;bundle++){ModelRenderer straw=box(body,1,side<0?-11-bundle*.5F:5+bundle*.5F,-19+bundle,-4+bundle*2,6,9+bundle*2,2);straw.zRot=side*.2F;}
   for(int b=0;b<3;b++){ModelRenderer straw=box(body,1,-5+b*3.5F,-17,4.3F,3,18+(b%2)*2,2);straw.xRot=.08F;}
   for(int p=0;p<2;p++)box(head,2,-3+p*4,-10-p,-1,3,3,3);
   chestGlow.addBox(-4,-13,-4.3F,1.3F,9,.5F);chestGlow.addBox(1,-9,-4.4F,3,1.3F,.5F);leftGlow.addBox(-1,2,-3.2F,1,6,.4F);rightGlow.addBox(-2,4,-3.2F,3,1.2F,.4F);
  }else{box(head,1,-3,-4,-10,6,3,7);for(int side:new int[]{-1,1}){box(head,2,side<0?-4:2,-10,-1,2,4,2);for(int tooth=0;tooth<2;tooth++)box(jaw,3,side<0?-2.6F:1.9F,-1.8F,-9+tooth*3,.7F,1.8F,1);for(int clump=0;clump<2;clump++){ModelRenderer fur=box(body,2,side<0?-8:4,-13+clump*2,-1+clump*3,4,6,4);fur.zRot=side*.18F;}}
   for(int ridge=0;ridge<3;ridge++)box(body,2,-2,-15+ridge*5,3,4,5,4);
   box(body,5,-6.5F,-2,-4.5F,13,2,9);for(int side:new int[]{-1,1}){box(body,5,side<0?-5:3,-12,-4.3F,2,11,.6F);ModelRenderer rag=box(body,7,side<0?-5:0,-1,-4.6F,5,5+(side>0?2:0),1);rag.zRot=side*.1F;}
   box(body,8,-1.6F,-2.5F,-5,3.2F,3,1);
  }
 }
 @Override public void setupAnim(T e,float stride,float amount,float age,float yaw,float pitch){float walk=MathHelper.cos(stride*.65F)*amount;root.y=24;root.xRot=0;body.xRot=oven?.16F:.18F;body.yRot=0;head.yRot=yaw*.017453F;head.xRot=pitch*.017453F;jaw.xRot=!oven&&e.rage()?.2F:0;left.xRot=walk*.3F;right.xRot=-walk*.3F;left.zRot=-.09F;right.zRot=.09F;leftFore.xRot=rightFore.xRot=oven?-.05F:-.23F;leftLeg.xRot=-walk*.65F;rightLeg.xRot=walk*.65F;leftShin.xRot=rightShin.xRot=oven?0:-.22F;
  HuntRules.Move move=e.action();float t=e.elapsed(age-e.tickCount),prep=Math.min(1,t/Math.max(1,move.windup));if(move==HuntRules.Move.IDLE){body.xRot+=MathHelper.sin(age*.075F)*.015F;return;}
  float swing=t<move.windup?0:Math.min(1,(t-move.windup)/5);
  switch(move){case SLAM:left.xRot=right.xRot=-prep*2.7F+swing*3.7F;body.xRot+=swing*.3F;break;
   case MELEE:body.yRot=-prep*.35F+swing*.6F;right.xRot=-prep*1.8F+swing*2.7F;break;
   case ASH:head.xRot=-prep*.4F;body.xRot-=prep*.12F;left.xRot=right.xRot=-prep*.5F;break;
   case EMBER:right.xRot=-prep*2.1F+swing*2.5F;break;
   case POUNCE:root.y+=t<10?prep*2:0;body.xRot=.4F;left.xRot=right.xRot=-prep*1.2F;leftLeg.xRot=rightLeg.xRot=.5F;break;
   case HOWL:head.xRot=-prep*.75F;jaw.xRot=prep*.55F;left.xRot=right.xRot=.1F;break;
   case COMBO:float hit=Math.max(0,t-move.windup);float wave=hit%6/5;if(hit<6){left.xRot=-prep*1.8F+wave*2.7F;}else if(hit<12){right.xRot=-1.8F+wave*2.7F;}else{left.xRot=right.xRot=-1.6F+Math.min(1,(hit-12)/5)*2.5F;}jaw.xRot=.3F;break;
   case CLAW:right.xRot=-prep*1.5F+swing*2.4F;body.yRot=prep*.25F-swing*.45F;break;
   case TRAIL:body.xRot=.35F;left.xRot=walk*.6F;right.xRot=-walk*.6F;break;
   case DODGE:body.yRot=.35F;break;default:break;}
 }
 public void glow(MatrixStack p,IVertexBuilder out,T e){float intensity=oven?e.rage()||e.action()==HuntRules.Move.ASH?1:.65F:1;p.pushPose();root.translateAndRotate(p);body.translateAndRotate(p);chestGlow.render(p,out,15728640,0,intensity,intensity,intensity,1);p.pushPose();head.translateAndRotate(p);eyes.render(p,out,15728640,0);p.popPose();for(int side=0;side<2;side++){p.pushPose();(side==0?left:right).translateAndRotate(p);(side==0?leftFore:rightFore).translateAndRotate(p);(side==0?leftGlow:rightGlow).render(p,out,15728640,0,intensity,intensity,intensity,1);p.popPose();}p.popPose();}
 @Override public void renderToBuffer(MatrixStack p,IVertexBuilder out,int light,int overlay,float r,float g,float b,float alpha){root.render(p,out,light,overlay,r,g,b,alpha);}
}
