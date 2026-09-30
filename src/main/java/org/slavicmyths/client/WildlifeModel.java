package org.slavicmyths.client;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.MathHelper;
import org.slavicmyths.entity.WildlifeEntity;
import java.util.*;
public final class WildlifeModel extends EntityModel<WildlifeEntity> {
 private final Map<String,ModelRenderer> parts=new LinkedHashMap<>();
 private final Map<String,float[]> rest=new LinkedHashMap<>();
 private final WildlifeEntity.Kind kind;
 public WildlifeModel(WildlifeEntity.Kind kind){this.kind=kind;texWidth=256;texHeight=256;WildlifeGeometry.build(this,kind);}
 public void part(String name,String parent,float[] pos,float[] rot){
  ModelRenderer p=new ModelRenderer(this);p.setPos(pos[0],pos[1],pos[2]);
  p.xRot=rot[0];p.yRot=rot[1];p.zRot=rot[2];parts.put(name,p);
  rest.put(name,new float[]{pos[0],pos[1],pos[2],rot[0],rot[1],rot[2]});
  if(parent!=null)parts.get(parent).addChild(p);
 }
 public void box(String name,int u,int v,float[] b){parts.get(name).texOffs(u,v).addBox(b[0],b[1],b[2],b[3],b[4],b[5]);}
 private ModelRenderer p(String n){return parts.get(n);}
 @Override public void setupAnim(WildlifeEntity e,float walk,float amount,float age,float yaw,float pitch){
  parts.forEach((n,p)->{float[] r=rest.get(n);p.setPos(r[0],r[1],r[2]);p.xRot=r[3];p.yRot=r[4];p.zRot=r[5];});
  float partial=age-e.tickCount;
  float moving=MathHelper.lerp(partial,e.visualMoveOld,e.visualMove);
  float run=MathHelper.lerp(partial,e.visualRunOld,e.visualRun);
  float stand=MathHelper.lerp(partial,e.visualStandOld,e.visualStand);
  boolean deer=kind==WildlifeEntity.Kind.STAG||kind==WildlifeEntity.Kind.DOE;
  boolean bear=kind==WildlifeEntity.Kind.BEAR;
  float frequency=bear?.58F:kind==WildlifeEntity.Kind.CUB?1.15F:kind==WildlifeEntity.Kind.BOAR?1.12F:deer?.68F:.88F;
  float phase=walk*frequency, stride=moving*(bear?.45F:deer?.68F:.58F);
  float cycle=phase*(1+run*.35F);
  String[] legs={"fl","fr","bl","br"};
  for(int i=0;i<4;i++){
   boolean front=i<2;
   float walking=MathHelper.cos(phase+((i==0||i==3)?0:3.1415927F))*stride;
   // Gallop pairs gather and extend; not a faster diagonal walk.
   float gallop=MathHelper.sin(cycle+(front?0:2.2F)+(i%2)*.35F)*moving*(deer?1.05F:bear?.64F:.84F);
   float angle=MathHelper.lerp(run,walking,gallop);
   p(legs[i]).xRot+=angle;
   p(legs[i]+"Lower").xRot+=(front?-1:1)*Math.max(0,-angle)*(deer?1.0F:.7F);
   p(legs[i]+"Foot").xRot-=angle*.28F;
  }
  p("spine").zRot=MathHelper.sin(phase)*moving*(bear?.035F:.012F);
  p("spine").y-=Math.abs(MathHelper.sin(cycle))*run*moving*(deer?1.5F:.6F);
  p("belly").xRot=MathHelper.sin(cycle)*run*moving*.06F;
  p("head").yRot=yaw*.0174533F*.7F;p("neck").yRot=yaw*.0174533F*.3F;
  p("head").xRot+=pitch*.0174533F*.6F;
  p("earL").zRot+=MathHelper.sin(age*.065F)*.04F;p("earR").zRot-=MathHelper.sin(age*.053F)*.04F;
  p("head").xRot+=MathHelper.sin(age*.08F)*.025F*(1-moving);
  p("tail").yRot=MathHelper.sin(age*.09F)*.09F;
  if(e.motion()==WildlifeEntity.ALERT){p("head").xRot-=.2F;p("earL").yRot=.3F;p("earR").yRot=-.3F;}
  if(e.isInWaterOrBubble()){
   p("spine").xRot=-.08F;p("head").xRot=-.22F;
   for(int i=0;i<4;i++){p(legs[i]).xRot=MathHelper.sin(age*(bear?.28F:kind==WildlifeEntity.Kind.WOLF?.65F:.42F)+i*3.1415927F)*.45F+.4F;p(legs[i]+"Lower").xRot=-.5F;}
   p("tail").xRot=0;
  }else if(!e.isOnGround()&&Math.abs(e.getDeltaMovement().y)>.025){
   boolean falling=e.getDeltaMovement().y<0;
   for(int i=0;i<4;i++){p(legs[i]).xRot=i<2?(falling?-.55F:-.95F):.65F;p(legs[i]+"Lower").xRot=i<2?.8F:-.8F;}
  }
  if(stand>0){p("spine").xRot-=stand*1.05F;p("spine").y-=stand*4;p("spine").z+=stand*6;p("fl").xRot-=stand*.8F;p("fr").xRot-=stand*.8F;p("head").xRot+=stand*.7F;}
  float attack=e.actionProgress(partial);
  if(e.motion()==WildlifeEntity.ATTACK){
   float strike=MathHelper.sin(attack*3.1415927F);
   if(bear){p("fl").xRot-=strike*1.5F;p("fl").zRot-=strike*.6F;if(e.attackStyle()==1)p("fr").xRot-=strike*1.5F;p("chest").yRot=strike*.18F;}
   else if(kind==WildlifeEntity.Kind.WOLF){p("jaw").xRot=strike*.65F;p("head").xRot+=strike*.32F;p("spine").z-=strike*1.3F;}
   else if(deer)p("neck").xRot+=strike*.7F;
  }
  if(e.motion()==WildlifeEntity.WINDUP||e.motion()==WildlifeEntity.CHARGE){p("head").xRot+=.4F;p("fl").xRot+=MathHelper.sin(age*.8F)*.2F;p("spine").xRot+=.08F;}
  if(e.motion()==WildlifeEntity.STUN){p("head").zRot=MathHelper.sin(age*.7F)*.16F;p("spine").zRot=MathHelper.sin(age*.45F)*.08F;}
  if(e.hurtTime>0)p("head").zRot+=MathHelper.sin(e.hurtTime*.7F)*.15F;
  if(e.deathTime>0){for(String leg:legs)p(leg+"Lower").xRot-=.4F;}
 }
 @Override public void renderToBuffer(MatrixStack m,IVertexBuilder b,int l,int o,float r,float g,float blue,float a){p("root").render(m,b,l,o,r,g,blue,a);}
}

