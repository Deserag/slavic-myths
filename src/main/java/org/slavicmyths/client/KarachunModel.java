package org.slavicmyths.client;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.karachun.Karachun;
public final class KarachunModel extends EntityModel<Karachun> {
 public final ModelPart root=KarachunMesh.create(),pelvis=root.getChild("pelvis"),torso=pelvis.getChild("torso"),head=torso.getChild("head"),right=torso.getChild("arm_right"),left=torso.getChild("arm_left");
 @Override public void setupAnim(Karachun e,float walk,float speed,float time,float yaw,float pitch){root.getAllParts().forEach(ModelPart::resetPose);float breath=(float)Math.sin(time*.045)*.015f;torso.xRot=.13f+breath;head.xRot=-.08f+pitch*.01745f;head.yRot=yaw*.01745f;right.xRot=-.45f;left.xRot=-.7f;left.yRot=.5f;right.getChild("forearm_right").xRot=-.35f;left.getChild("forearm_left").xRot=-.6f;
  for(String s:new String[]{"left","right"}){float sign=s.equals("left")?1:-1;pelvis.getChild("leg_"+s).xRot=(float)Math.cos(walk*.45)*speed*.4f*sign;for(String panel:new String[]{"front","back","side"})pelvis.getChild("cloak_"+panel+"_"+s).xRot=breath*sign+(float)Math.cos(walk*.45)*speed*.08f*sign;torso.getChild("chains_"+s).zRot=breath*sign*2;torso.getChild("fur_"+s).zRot=breath*sign;head.getChild("hair_"+s).xRot=breath*2;}head.getChild("beard").xRot=breath*2;head.getChild("hair_back").xRot=-breath;
  int a=e.action();float age=e.age();if(a>=1&&a<=3){float t=Math.clamp(age/(a==3?14f:10f),0,1);float swing=(float)Math.sin(t*Math.PI/2);torso.yRot=(a==2?-1:1)*(-.6f+swing*1.2f);right.xRot=a==3?-2.4f+swing*2.2f:-1;left.xRot=right.xRot-.15f;right.yRot=(a==2?-1:1)*(-1.3f+swing*2.6f);left.yRot=.6f;left.getChild("forearm_left").xRot=-1.1f;}
  if(a==4){left.xRot=-1.5f;left.yRot=.15f;right.xRot=-.8f;head.xRot=.2f;}if(a==5){torso.xRot=.3f;head.xRot=.25f;right.xRot=-1.1f;left.xRot=-.8f;}if(a==6){right.xRot=-1.5f+(age>16?.8f:0);left.xRot=-1.2f;torso.yRot=-.4f;}if(a==7){right.xRot=-Math.min(1,age/24)*2.4f;left.xRot=-1.8f;head.xRot=-.3f;}if(a==8){head.xRot=.4f;head.yRot=(float)Math.sin(age*.2)*.12f;left.xRot=-.9f;}if(a==9){right.xRot=-1.3f;left.xRot=-1.1f;torso.yRot=.3f;}if(e.phase()==0){root.yScale=Math.max(.02f,Math.min(1,e.tickCount/40f));right.xRot=-1.8f;}if(e.phase()>=4){torso.xRot=.24f;head.xRot=.35f;left.xRot=-.35f;right.xRot=-.2f;}}
 @Override public void renderToBuffer(PoseStack p,VertexConsumer out,int light,int overlay,int color){root.render(p,out,light,overlay,color);}
}
