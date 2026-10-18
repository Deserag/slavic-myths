package org.slavicmyths.client;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.slavicmyths.bandit.NightingaleEntity;
/** Independent stocky forest-bandit and lean whistle rigs with two curved blades. */
public final class NightingaleModel extends EntityModel<NightingaleEntity> implements BossGeometrySink {
 private final boolean second;
 private final FolkModelGeometry geometry=new FolkModelGeometry();
 private final Map<String,ModelPart> parts=new LinkedHashMap<>();
 private final Map<String,float[]> neutral=new HashMap<>();
 public NightingaleModel(){this(false);}
 public NightingaleModel(boolean second){this.second=second;BossReworkGeometry.build(this,second?3:2);}
 public void add(String name,String parent,float x,float y,float z,float[] box,int material,float rx,float ry,float rz){
  ModelPart p=geometry.part(1024,1024,material%4*256,material/4*256);p.setPos(x,y,z);p.xRot=rx;p.yRot=ry;p.zRot=rz;
  if(box!=null)geometry.box(p,box[0],box[1],box[2],box[3],box[4],box[5]);
  parts.put(name,p);neutral.put(name,new float[]{x,y,z,rx,ry,rz});if(!parent.isEmpty())geometry.attach(parts.get(parent),p);
 }
 private void rx(String n,float a){ModelPart p=parts.get(n);if(p!=null)p.xRot+=a;}
 @Override public void setupAnim(NightingaleEntity e,float stride,float amount,float age,float yaw,float pitch){
  sample(e.action(),e.elapsed(age-e.tickCount),e.phase()==NightingaleEntity.Phase.PHASE_TWO,e.onGround(),(float)e.getDeltaMovement().y,e.hurtTime,e.deathTime,stride,amount,age,yaw,pitch);
 }
 public void sample(int a,float t,boolean phaseTwo,boolean grounded,float verticalSpeed,int hurtTicks,int deathTicks,float stride,float amount,float age,float yaw,float pitch){
  parts.forEach((n,p)->{float[] v=neutral.get(n);p.setPos(v[0],v[1],v[2]);p.xRot=v[3];p.yRot=v[4];p.zRot=v[5];p.xScale=p.yScale=p.zScale=1;p.visible=true;p.skipDraw=false;});
  parts.get("head").yRot=net.minecraft.util.Mth.clamp(yaw,-35,35)*net.minecraft.util.Mth.DEG_TO_RAD;
  parts.get("head").xRot=pitch*net.minecraft.util.Mth.DEG_TO_RAD;
  NightingalePoses.sample(parts,second,a,t,phaseTwo,grounded,verticalSpeed,hurtTicks,deathTicks,stride,amount,age);
 }
 public void renderToBuffer(PoseStack pose,VertexConsumer out,int light,int overlay,int color){parts.get("root").render(pose,out,light,overlay,color);}
}
