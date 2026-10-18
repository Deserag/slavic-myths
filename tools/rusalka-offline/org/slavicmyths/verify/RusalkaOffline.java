package org.slavicmyths.verify;
import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Vector3f;
import org.slavicmyths.client.*;
import org.slavicmyths.water.RusalkaActions;
import org.slavicmyths.entity.AttackTimeline;

/** Calls production model/attack/path code only; no game bootstrap or world is constructed. */
public final class RusalkaOffline {
 private static int checks;
 private static Object field(Object o,String name)throws Exception{var f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 private static void check(boolean ok,String why){checks++;if(!ok)throw new AssertionError(why);}
 private static JsonArray mesh(ModelPart root)throws Exception{var result=new JsonArray();collect(root,new PoseStack(),result);return result;}
 @SuppressWarnings("unchecked") private static void collect(ModelPart part,PoseStack pose,JsonArray out)throws Exception{
  pose.pushPose();part.translateAndRotate(pose);
  for(var cube:(List<?>)field(part,"cubes"))for(var polygon:(Object[])field(cube,"polygons")){
   var face=new JsonArray();for(var vertex:(Object[])field(polygon,"vertices")){
    var pos=(Vector3f)field(vertex,"pos");float u=(float)field(vertex,"u"),v=(float)field(vertex,"v");
    check(Float.isFinite(u)&&Float.isFinite(v)&&u>=0&&u<=1&&v>=0&&v<=1,"UV outside skin");
    var p=pose.last().pose().transformPosition(new Vector3f(pos).div(16)).mul(16);
    check(Float.isFinite(p.x)&&Float.isFinite(p.y)&&Float.isFinite(p.z),"Nonfinite transformed mesh");
    var point=new JsonArray();for(float n:new float[]{p.x,p.y,p.z,u,v})point.add(n);face.add(point);
   }out.add(face);
  }
  for(var child:((Map<String,ModelPart>)field(part,"children")).values())collect(child,pose,out);
  pose.popPose();
 }
 private static String transforms(RusalkaModel m){var s=new StringBuilder();for(var p:m.parts.values())s.append(Arrays.toString(new float[]{p.x,p.y,p.z,p.xRot,p.yRot,p.zRot}));return s.toString();}
 public static void main(String[] args)throws Exception{
  var rig=new RusalkaModel();check(rig.parts.size()==31,"31 expressive, bounded parts");
  for(String name:List.of("hairBack","hairMid","hairTip","foreL","foreR","footL","footR","skirtFrontL","skirtBackR"))check(rig.parts.containsKey(name),"Missing "+name);
  var data=new JsonObject();data.add("before",mesh((ModelPart)field(new WaterSpiritModel(true),"body")));
  String[] names={"idle","observe","song_start","lure","break","windup","grab","recovery","dive"};
  for(int i=0;i<names.length;i++){rig.sample(i,i==5?11:i==7?3:12,0,0,20,0,0,i==8,0,0);data.add(names[i],mesh(rig.parts.get("body")));}
  rig.sample(0,0,2,.8F,20,0,0,false,0,0);data.add("walk",mesh(rig.parts.get("body")));
  rig.sample(0,0,2,.8F,20,0,0,true,0,0);data.add("swim",mesh(rig.parts.get("body")));check(rig.parts.get("body").xRot>1,"Horizontal forward swim");
  rig.sample(0,0,0,0,20,0,0,false,8,0);data.add("hurt",mesh(rig.parts.get("body")));
  rig.sample(0,0,0,0,20,0,0,false,0,18);data.add("death",mesh(rig.parts.get("body")));
  rig.sample(0,0,0,0,20,0,0,false,0,0);String idle=transforms(rig);
  for(int action=0;action<=8;action++){rig.sample(action,9,3,1,92,65,45,true,10,20);rig.sample(0,0,0,0,20,0,0,false,0,0);check(transforms(rig).equals(idle),"Absolute pose reset after "+action);}
  rig.parts.get("head").visible=false;rig.parts.get("head").skipDraw=true;rig.sample(0,0,0,0,20,0,0,false,0,0);check(rig.parts.get("head").visible&&!rig.parts.get("head").skipDraw,"Visibility reset");
  var one=new java.util.UUID(0,1);var two=new java.util.UUID(0,2);
  rig.animate(one,0,0,0,0,20,0,0,false,0,0);String separateIdle=transforms(rig);
  rig.animate(two,6,0,0,0,20,0,0,false,0,0);check(rig.parts.get("armL").xRot<-1.4F,"First actual active pose");
  rig.animate(one,0,0,0,0,20,0,0,false,0,0);check(transforms(rig).equals(separateIdle),"Two singers cannot leak poses");
  rig.animate(one,3,30,0,0,20,0,0,false,0,0);check(transforms(rig).equals(separateIdle),"Transition begins at prior own pose");
  rig.animate(two,6,0,0,0,21,0,0,false,0,0);
  rig.animate(one,3,35,0,0,25,0,0,false,0,0);check(rig.parts.get("armR").xRot<-.9F,"Own transition completes despite interleaved entity");
  rig.animate(one,6,0,0,0,25,0,0,false,0,0);check(rig.parts.get("armL").xRot<-1.4F,"Hit pose never delayed by fade");
  rig.animate(one,4,12,0,0,25,0,0,false,0,0);check(rig.parts.get("armR").xRot==0,"Break drops arm without delayed loop pose");
  for(int n=0;n<100;n++)rig.animate(new java.util.UUID(1,n),0,0,0,0,20,0,0,false,0,0);
  check(((Map<?,?>)field(rig,"blends")).size()<=64,"Bounded per-entity transition cache");
  var timeline=new AttackTimeline();check(timeline.start(12,1,7,1),"Start");int impacts=0,first=-1;for(int i=1;i<=21;i++)if(timeline.tick()){impacts++;first=i;}
  check(impacts==1&&first==12&&timeline.ready(),"Single hit and preserved ~20 tick cadence");
  timeline.start(12,1,7,1);for(int i=0;i<11;i++)timeline.tick();timeline.cancel();check(!timeline.tick()&&timeline.ready(),"Cancellation cannot replay hit");
  check(RusalkaActions.safeDivePath(3.8,d->true),"Clear water path");
  check(!RusalkaActions.safeDivePath(3.8,d->d<1.5||d>2.0),"Dry/solid/unloaded interval rejects dive");
  for(double bad:new double[]{0,.9,3.81,Double.NaN,Double.POSITIVE_INFINITY})check(!RusalkaActions.safeDivePath(bad,d->true),"Invalid length");
  check(!RusalkaActions.safeDivePath(3,d->d!=0),"Start volume checked");check(!RusalkaActions.safeDivePath(3,d->d<3),"End checked");
  check(!RusalkaActions.diveDistance(15.9)&&RusalkaActions.diveDistance(16)&&RusalkaActions.diveDistance(64)&&!RusalkaActions.diveDistance(64.1),"Dive engagement band");
  check(RusalkaActions.remaining(900,100,160)==160&&RusalkaActions.remaining(50,100,160)==0,"NBT deadlines bounded");
  var tag=new net.minecraft.nbt.CompoundTag();tag.putLong("NextPower",470);tag.putInt("VisualVariant",2);
  RusalkaActions.writeMemory(tag,510,540,80);var saved=RusalkaActions.readMemory(tag,450);
  check(saved.equals(new RusalkaActions.Memory(510,540,80)),"Actual CompoundTag roundtrip");
  check(tag.getLong("NextPower")==470&&tag.getInt("VisualVariant")==2,"Inherited keys not overwritten");
  check(RusalkaActions.readMemory(new net.minecraft.nbt.CompoundTag(),450).equals(new RusalkaActions.Memory(450,450,0)),"Old saves missing new keys");
  RusalkaActions.writeMemory(tag,Long.MAX_VALUE,Long.MAX_VALUE,999);check(RusalkaActions.readMemory(tag,450).equals(new RusalkaActions.Memory(570,610,100)),"Corrupt/extreme new keys clamped");
  var dir=Path.of("build/rusalka-49");Files.createDirectories(dir);Files.writeString(dir.resolve("meshes.json"),new Gson().toJson(data));
  System.out.println("RUSALKA_OFFLINE_PASS checks="+checks+" parts="+rig.parts.size()+" poses="+(data.size()-1)+"; no Minecraft/client/server bootstrap");
 }
}
