package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.bandit.NightingaleEntity;
/** Independent forest-bandit and thin cartoon rigs with two curved blades. */
public final class NightingaleModel extends EntityModel<NightingaleEntity> implements BossGeometrySink {
 private final FolkModelGeometry geometry=new FolkModelGeometry();
 private final Map<String,ModelPart> parts=new LinkedHashMap<>();
 private final Map<String,float[]> neutral=new HashMap<>();
 public NightingaleModel(){this(false);}
 public NightingaleModel(boolean second){BossReworkGeometry.build(this,second?3:2);}
 public void add(String name,String parent,float x,float y,float z,float[] box,int material,float rx,float ry,float rz){
  ModelPart p=geometry.part(1024,1024,material%4*256,material/4*256);p.setPos(x,y,z);p.xRot=rx;p.yRot=ry;p.zRot=rz;
  if(box!=null)geometry.box(p,box[0],box[1],box[2],box[3],box[4],box[5]);
  parts.put(name,p);neutral.put(name,new float[]{x,y,z,rx,ry,rz});if(!parent.isEmpty())geometry.attach(parts.get(parent),p);
 }
 private void rx(String n,float a){ModelPart p=parts.get(n);if(p!=null)p.xRot+=a;}
 public void setupAnim(NightingaleEntity e,float stride,float amount,float age,float yaw,float pitch){
  parts.forEach((n,p)->{float[] v=neutral.get(n);p.setPos(v[0],v[1],v[2]);p.xRot=v[3];p.yRot=v[4];p.zRot=v[5];});
  parts.get("head").yRot=yaw*.017453F;parts.get("head").xRot=pitch*.017453F;
  float walk=(float)Math.cos(stride*.65)*amount,t=e.elapsed(age-e.tickCount);
  rx("legL",walk*.65F);rx("legR",-walk*.65F);rx("armL",-walk*.35F);rx("armR",walk*.35F);
  int a=e.action();
  if(e.phase()==NightingaleEntity.Phase.PHASE_TWO&&!e.onGround()){
   rx("legL",.35F);rx("legR",.35F);rx("foreL",-.35F);rx("foreR",-.35F);
   parts.get("body").y+=(float)Math.sin(age*.12)*.35F;
  }
  if(a==NightingaleEntity.BREAK_AWAY){rx("body",-.2F);rx("jaw",.3F);rx("armL",-.8F);rx("armR",-.8F);}
  if(a==NightingaleEntity.AIR_DIVE){
   boolean falling=t>=10&&t<36&&e.getDeltaMovement().y<-.1;
   rx("body",falling?.7F:-.1F);rx("armL",falling?-2F:-.5F);rx("armR",falling?-2F:-.5F);
  }
  if(a==NightingaleEntity.QUICK||a==NightingaleEntity.COMBO){float swing=(float)Math.sin(Math.min(1,(t%12)/12)*Math.PI);rx(((int)(t/12)&1)==0?"armR":"armL",-2*swing);parts.get("body").yRot=.35F*swing;}
  if(a==NightingaleEntity.INHALE||a==NightingaleEntity.WAVE||a==NightingaleEntity.WHIRLWIND||a==NightingaleEntity.TRANSFORM){rx("body",-.12F);rx("jaw",Math.min(.35F,t/40));rx("armL",-.6F);rx("armR",-.6F);}
  if(a==NightingaleEntity.DASH||a==NightingaleEntity.ONSLAUGHT||a==NightingaleEntity.LEAP_ATTACK){rx("body",.35F);rx("armL",-.6F);rx("armR",-.6F);}
  if(a==NightingaleEntity.ONSLAUGHT&&t>=20&&t<40){int stage=Math.min(1,(int)((t-20)/12));float swing=(float)Math.sin(Math.min(1,(t-20-stage*12)/20)*Math.PI);rx(stage==0?"armR":"armL",-1.8F*swing);parts.get("body").yRot=(stage==0?-.3F:.3F)*swing;}
  if(a==NightingaleEntity.ONSLAUGHT&&t>=40){rx("body",-.2F);rx("jaw",.3F);}
  if(a==NightingaleEntity.LEAP_ATTACK&&t>=12&&e.onGround()){rx("body",.2F);rx("legL",-.25F);rx("legR",-.25F);}
  if(a==NightingaleEntity.WHIRLWIND&&t>=16)parts.get("root").yRot=(t-16)*.45F;
  for(int i=0;i<7;i++)rx("cloakStrip"+i,(float)Math.sin(age*.08+i)*.05F+amount*.12F);
  rx("topknot",(float)Math.sin(age*.08)*.06F);
  rx("topknot",amount*.09F);
  if(e.hurtTime>0){rx("body",-.12F*e.hurtTime/10);rx("head",.15F*e.hurtTime/10);}
  if(e.deathTime>0){parts.get("root").zRot=Math.min(1,e.deathTime/45F)*1.5F;parts.get("root").y+=Math.min(10,e.deathTime*.2F);}
 }
 public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){parts.get("root").render(pose,out,light,overlay,color);}
}
