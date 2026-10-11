package org.slavicmyths.client;

import java.util.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.joml.*;
import org.slavicmyths.pesigolovtsy.*;
import org.slavicmyths.pesigolovtsy.DogheadRules.*;

@EventBusSubscriber(modid="slavicmyths",bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class DogheadClient {
 @SubscribeEvent public static void renderers(EntityRenderersEvent.RegisterRenderers e){e.registerEntityRenderer(DogheadContent.ENTITY.get(),Renderer::new);e.registerEntityRenderer(DogheadContent.ARROW.get(),ArrowView::new);}
 public static final class ArrowView extends ArrowRenderer<DogheadArrow>{public ArrowView(EntityRendererProvider.Context c){super(c);}public ResourceLocation getTextureLocation(DogheadArrow e){return ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");}}
 public static final class Renderer extends MobRenderer<DogheadEntity,Model>{public Renderer(EntityRendererProvider.Context c){super(c,new Model(),.48F);}public ResourceLocation getTextureLocation(DogheadEntity e){return DogheadContent.id("textures/entity/pesigolovtsy/"+e.role().id()+".png");}protected float getFlipDegrees(DogheadEntity e){return 0;}}
 public static final class Model extends EntityModel<DogheadEntity>{
  public record Rig(ModelPart root,Map<String,ModelPart> nodes){}
  public final EnumMap<Role,Rig> rigs=new EnumMap<>(Role.class);public Role active=Role.HUNTER;
  private static final class Stride{double x,z;float phase,weight,age;boolean initialized;}
  private final Map<DogheadEntity,Stride> strides=new WeakHashMap<>();
  public Model(){for(var r:Role.values()){var root=DogheadMesh.create(r);rigs.put(r,new Rig(root,DogheadMesh.index(root,r)));}}
  public ModelPart n(String name){return rigs.get(active).nodes.get(name);}
  private static float rad(float degrees){return degrees*(float)(java.lang.Math.PI/180);}
  @Override public void setupAnim(DogheadEntity e,float swing,float amount,float age,float yaw,float pitch){float partial=age-e.tickCount;var s=strides.computeIfAbsent(e,k->new Stride());double x=Mth.lerp(partial,e.xo,e.getX()),z=Mth.lerp(partial,e.zo,e.getZ());float dt=s.initialized?java.lang.Math.max(0,age-s.age):0;double distance=s.initialized?java.lang.Math.min(.6,java.lang.Math.hypot(x-s.x,z-s.z)):0;boolean run=e.running();float stride=DogheadPose.stride(e.role(),run);s.phase+=distance/stride;if(dt>0)s.weight+=(java.lang.Math.min(1,(float)(distance/dt*10))-s.weight)*java.lang.Math.min(1,dt/5);s.x=x;s.z=z;s.age=age;s.initialized=true;pose(e.role(),e.clip(),e.clipAge(partial),s.phase,s.weight,age,yaw,pitch,e.bow(),e.onGround(),run);if(e.clip()==Clip.THRUST)n("chest").xRot+=e.thrustAim()*DogheadPose.smooth(e.clipAge(partial),0,7)*(1-DogheadPose.smooth(e.clipAge(partial),14,24));}
  /** The live pose path, shared with offline geometry and attachment checks. */
  public void pose(Role role,Clip clip,float t,float gait,float movement,float age,float yaw,float pitch,boolean bow,boolean grounded,boolean run){
   active=role;var rig=rigs.get(role);rig.root.getAllParts().forEach(p->{p.resetPose();p.visible=true;});float w=grounded&&(clip==Clip.READY||clip==Clip.RETREAT||clip==Clip.DODGE||clip==Clip.SNIFF||clip==Clip.HUNTER_GUARD||clip==Clip.LEADER_GUARD)?Mth.clamp(movement,0,1):0;
   n("chest").xRot=DogheadPose.pitch(role,clip,t);n("chest").yRot=rad(DogheadPose.sample(clip,t,2,0));n("head").xRot=rad(DogheadPose.sample(clip,t,6,0))+rad(Mth.clamp(pitch,-25,25))*(clip.attack()?.15F:.65F);n("head").yRot=rad(Mth.clamp(yaw,-45,45))*(clip.attack()?.15F:1);
   float breath=(float)java.lang.Math.sin(age*2*java.lang.Math.PI/(role==Role.SCOUT?52:role==Role.HUNTER?64:74));n("chest").y-=.12F*breath*(!clip.attack()?1-w:0);n("jaw_lower").xRot=rad(DogheadPose.sample(clip,t,7,2));
   for(String side:List.of("L","R")){float offset=side.equals("L")?0:2.2F;float earWindow=(age+offset*71)%137;float twitch=earWindow<7?(float)java.lang.Math.sin(earWindow*java.lang.Math.PI/7):0;n("ear_"+side).xRot+=.12F*twitch;n("ear_"+side).yRot+=(side.equals("L")?1:-1)*.10F*twitch;}
   float cycle=gait*(float)(java.lang.Math.PI*2),sink=(run?.8F:.45F)*w;n("pelvis").y+=sink;n("chest").yRot+=(float)java.lang.Math.sin(cycle)*.035F*w;n("head").yRot-=(float)java.lang.Math.sin(cycle)*.035F*w;
   for(String side:List.of("L","R")){float phase=cycle+(side.equals("L")?0:(float)java.lang.Math.PI),s=(float)java.lang.Math.sin(phase),z=(float)java.lang.Math.cos(phase)*(run?3.7F:2.6F)*w;float upper=role==Role.SCOUT?7.5F:role==Role.HUNTER?8.2F:8.8F,lower=role==Role.SCOUT?5.9F:role==Role.HUNTER?6.2F:6.6F;var hip=n("leg_"+side+"_upper");var knee=n("leg_"+side+"_lower");var foot=n("foot_"+side);if(grounded){solve(hip,knee,foot,new Vector3f(hip.x,upper+lower-sink-java.lang.Math.max(0,s)*(run?2.2F:1.3F)*w,z),upper,lower,new Vector3f(0,0,-1));foot.xRot=-hip.xRot-knee.xRot;foot.zRot=-hip.zRot-knee.zRot;}else{hip.xRot=-.35F;knee.xRot=.6F;foot.xRot=-.25F;}}
   for(var entry:rig.nodes.entrySet()){String name=entry.getKey();if((name.startsWith("fur_")||name.startsWith("mantle_"))&&!name.contains("root"))entry.getValue().xRot+=(float)java.lang.Math.sin(cycle-.45+name.hashCode()*.1)*.10F*w;if(name.startsWith("cloth_"))entry.getValue().xRot+=(float)java.lang.Math.sin(cycle-.65)*.04F*w;}
   n("tail_1").yRot=(float)java.lang.Math.sin(age*.065)*.10F;n("tail_2").yRot=(float)java.lang.Math.sin(age*.065-.4)*.08F;
   if(clip==Clip.HUNTER_BARK||clip==Clip.SCOUT_SPOT){n("jaw_lower").xRot=.3F*(DogheadPose.smooth(t,0,4)-DogheadPose.smooth(t,9,clip.duration));n("neck").xRot=-.15F;}
   if(clip==Clip.HEAR_BARK||clip==Clip.HEAR_HOWL){n("head").zRot=.16F*(1-DogheadPose.smooth(t,4,clip.duration));n("ear_L").xRot-=.3F;}
   if(clip==Clip.SNIFF){n("neck").xRot=.35F;n("head").xRot=.2F;n("head").yRot=(float)java.lang.Math.sin(t*.22)*.2F;}
   if(clip==Clip.VICTORY){n("jaw_lower").xRot=.22F;n("head").xRot=-.25F;}
   if(clip==Clip.HOWL){n("head").xRot=-rad(DogheadPose.sample(clip,t,6,0));n("neck").xRot=-.10F*(DogheadPose.smooth(t,4,12)-DogheadPose.smooth(t,23,28));}
   if(clip==Clip.KNIFE){float drive=DogheadPose.smooth(t,3,9)*(1-DogheadPose.smooth(t,11,16));n("spine_lower").xRot=n("chest").xRot;n("chest").xRot=0;n("spine_upper").z-=3.3F*drive;}
   if(clip==Clip.HUNTER_HURT||clip==Clip.SCOUT_HURT||clip==Clip.LEADER_HURT){float recoil=1-DogheadPose.smooth(t,0,clip.duration);n("chest").xRot-=.2F*recoil;n("head").zRot=.15F*recoil;}
   if(clip.death()){float fall=DogheadPose.smooth(t,2,clip.duration-3);n("motionRoot").zRot=.65F*fall;n("motionRoot").y+=role.height*8*fall;n("chest").xRot+=.5F*fall;n("head").xRot+=.3F*fall;for(String side:List.of("L","R")){n("leg_"+side+"_upper").xRot=-.5F*fall;n("leg_"+side+"_lower").xRot=.9F*fall;n("foot_"+side).xRot=-.4F*fall;}n("jaw_lower").xRot=.22F*(1-fall);}
   var a=DogheadPose.blendedWeapon(role,clip,t);var weapon=n("weapon_root");weapon.setPos(a.x(),a.y(),a.z());weapon.setRotation(a.pitch(),a.yaw(),a.roll());
   if(role==Role.SCOUT){boolean held=bow;float equip=clip==Clip.BOW_EQUIP?DogheadPose.smooth(t,4,10):held?1:0;if(!held&&clip!=Clip.BOW_EQUIP){weapon.setPos(0,4,3.2F);weapon.setRotation(0,0,.35F);}else{weapon.x=Mth.lerp(equip,0,a.x());weapon.y=Mth.lerp(equip,4,a.y());weapon.z=Mth.lerp(equip,3.2F,a.z());weapon.zRot=Mth.lerp(equip,.35F,0);}
    boolean knife=!held&&(clip!=Clip.KNIFE_SWAP||t>=6);n("knife_root").visible=knife;n("knife_sheath").visible=!knife;
    float draw=clip==Clip.BOW?DogheadPose.key(t,new float[]{0,8,12,16,20,22,32},new float[]{0,2,4,6,6,0,0}):0;n("weapon_secondary_socket").z=draw;
    var top=n("bow_string_top");var bottom=n("bow_string_bottom");top.xRot=(float)java.lang.Math.atan2(draw+1.8,7.6);top.yScale=(float)java.lang.Math.hypot(7.6,draw+1.8)/7.6F;bottom.setPos(0,0,draw);bottom.xRot=-top.xRot;bottom.yScale=top.yScale;n("nocked_arrow").z=draw;n("nocked_arrow").visible=clip==Clip.BOW&&t<22||clip==Clip.BOW_EQUIP&&t>=8;
    if(held||clip==Clip.BOW_EQUIP){grip("L",weapon,0,0,0);grip("R",weapon,0,0,draw);}else{n("arm_L_upper").xRot=.12F*w*(float)java.lang.Math.cos(cycle);float slash=clip==Clip.KNIFE?DogheadPose.key(t,new float[]{0,4,7,9,12,16},new float[]{-.45F,-1.15F,-1.8F,-1.5F,-1.6F,-.45F}):-.45F;n("arm_R_upper").xRot=slash;n("arm_R_upper").yRot=clip==Clip.KNIFE?-.5F*(DogheadPose.smooth(t,3,9)-DogheadPose.smooth(t,11,16)):0;n("arm_R_lower").xRot=clip==Clip.KNIFE?-.05F:-.3F;n("hand_R").xRot=-n("arm_R_upper").xRot-n("arm_R_lower").xRot-n("chest").xRot-n("spine_lower").xRot;}
   }else{grip("R",weapon,0,0,0);grip("L",weapon,0,0,6);}
  }
  private void grip(String side,ModelPart weapon,float x,float y,float z){var goal=new Matrix4f().translate(weapon.x,weapon.y,weapon.z).rotateZYX(weapon.zRot,weapon.yRot,weapon.xRot).transformPosition(new Vector3f(x,y,z));float upper=active==Role.SCOUT?5.5F:active==Role.HUNTER?6.2F:7,lower=active==Role.SCOUT?4.7F:active==Role.HUNTER?5:5.5F;solve(n("arm_"+side+"_upper"),n("arm_"+side+"_lower"),n("hand_"+side),goal,upper,lower,new Vector3f(side.equals("L")?1:-1,.05F,.6F));}
  /** Analytic two-bone IK: the palm origin is the contact, without stretching either limb. */
  public static void solve(ModelPart upper,ModelPart lower,ModelPart end,Vector3f goal,float l1,float l2,Vector3f bend){var origin=new Vector3f(upper.x,upper.y,upper.z);var delta=new Vector3f(goal).sub(origin);float distance=delta.length();var axis=delta.normalize();float d=Mth.clamp(distance,.001F,l1+l2-.001F);float along=(l1*l1-l2*l2+d*d)/(2*d);float radius=(float)java.lang.Math.sqrt(java.lang.Math.max(0,l1*l1-along*along));var perpendicular=new Vector3f(bend).sub(new Vector3f(axis).mul(bend.dot(axis)));if(perpendicular.lengthSquared()<.0001)perpendicular.set(1,0,0);perpendicular.normalize();var elbow=new Vector3f(origin).add(new Vector3f(axis).mul(along)).add(perpendicular.mul(radius));orient(upper,new Vector3f(elbow).sub(origin));var local=new Matrix4f().rotateZYX(upper.zRot,upper.yRot,upper.xRot).invert().transformDirection(new Vector3f(goal).sub(elbow));orient(lower,local);end.xRot=end.yRot=end.zRot=0;}
  private static void orient(ModelPart p,Vector3f v){v.normalize();p.xRot=(float)java.lang.Math.asin(Mth.clamp(v.z,-1,1));p.yRot=0;p.zRot=(float)java.lang.Math.atan2(-v.x,v.y);}
  @Override public void renderToBuffer(PoseStack p,VertexConsumer v,int light,int overlay,int color){rigs.get(active).root.render(p,v,light,overlay,color);}
 }
 private DogheadClient(){}
}
