package org.slavicmyths.client;

import java.util.*;
import java.lang.reflect.*;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.model.geom.ModelPart;

/** Native mesh and actual vertex submission regressions; no game or unsafe entity fixtures. */
public final class VisualChecks {
    static int checks;static void check(boolean b,String why){checks++;if(!b)throw new AssertionError(why);}
    static final List<float[]> faces=new ArrayList<>();static Field cubes,children,polygons;
    static void scan(ModelPart p,float x,float y,float z)throws Exception{
        x+=p.x;y+=p.y;z+=p.z;
        for(var cube:(List<?>)cubes.get(p))for(var face:(Object[])polygons.get(cube))if(face!=null){var vf=face.getClass().getDeclaredField("vertices");vf.setAccessible(true);float[] bounds={Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};for(var vertex:(Object[])vf.get(face)){var pf=vertex.getClass().getDeclaredField("pos");pf.setAccessible(true);var pos=(org.joml.Vector3f)pf.get(vertex);float[] values={pos.x+x,pos.y+y,pos.z+z};for(int i=0;i<3;i++){bounds[i]=Math.min(bounds[i],values[i]);bounds[i+3]=Math.max(bounds[i+3],values[i]);}}faces.add(bounds);}
        for(var child:((Map<String,ModelPart>)children.get(p)).values())scan(child,x,y,z);
    }
    static int coplanar(ModelPart root)throws Exception{faces.clear();scan(root,0,0,0);int count=0;for(int i=0;i<faces.size();i++)for(int j=0;j<i;j++){var a=faces.get(i);var b=faces.get(j);for(int axis=0;axis<3;axis++)if(Math.abs(a[axis]-a[axis+3])<.00001&&Math.abs(b[axis]-b[axis+3])<.00001&&Math.abs(a[axis]-b[axis])<.00001){boolean overlap=true;for(int k=0;k<3;k++)if(k!=axis&&Math.min(a[k+3],b[k+3])-Math.max(a[k],b[k])<.02)overlap=false;if(overlap){if(count<8)System.out.println("pair="+Arrays.toString(a)+" / "+Arrays.toString(b));count++;}}}return count;}
    public static void main(String[] args)throws Exception{
        cubes=ModelPart.class.getDeclaredField("cubes");children=ModelPart.class.getDeclaredField("children");polygons=ModelPart.Cube.class.getDeclaredField("polygons");cubes.setAccessible(true);children.setAccessible(true);polygons.setAccessible(true);
        int sivka=coplanar(SivkaMesh.create());System.out.println("Sivka coplanar overlapping face pairs="+sivka);check(sivka==0,"Sivka no overlapping coplanar faces");
        var boss=new EarthClient.BossModel();int earth=coplanar(boss.root);System.out.println("Earth coplanar overlapping face pairs="+earth);check(earth==0,"Earth no overlapping coplanar faces");
        int expected=0;for(var p:boss.root.getAllParts().toList())expected+=((List<?>)cubes.get(p)).size()*24;final int[] submitted={0};var consumer=(VertexConsumer)Proxy.newProxyInstance(VisualChecks.class.getClassLoader(),new Class[]{VertexConsumer.class},(proxy,method,a)->{if(method.getName().equals("addVertex"))submitted[0]++;return method.getReturnType()==VertexConsumer.class?proxy:null;});boss.renderToBuffer(new PoseStack(),consumer,123,0,-1);check(submitted[0]==expected,"Earth emissive geometry submitted once: "+submitted[0]+" expected "+expected);
        var bird=FirebirdMesh.index(FirebirdMesh.create());check(bird.containsKey("left_talon0")&&bird.containsKey("right_back_toe"),"real articulated avian toes");check(bird.containsKey("neck_plume_4_left")&&bird.containsKey("throat_feather_5_2")&&bird.containsKey("crest6"),"layered long neck and swept crown");
        var model=new FirebirdClient.Model();var feet=FirebirdMesh.index(model.root);model.poseFeet(0,0,true);float rest=feet.get("left_ankle").xRot;model.root.getAllParts().forEach(ModelPart::resetPose);model.poseFeet(1,10,false);float flight=feet.get("left_ankle").xRot;check(Math.abs(rest-flight)>.5,"actual ankle tuck differs from standing");check(feet.get("left_talon0").xRot>.1,"actual talon curl in flight");model.root.getAllParts().forEach(ModelPart::resetPose);model.poseFeet(1,20,false);check(Math.abs(flight-feet.get("left_ankle").xRot)>.001,"feet remain animated between flight frames");
        var horse=new SivkaClient.Model();horse.poseTail(12.99F,0);float tail=horse.nodes.get("tailMid").xRot;horse.poseTail(13.01F,0);check(Math.abs(horse.nodes.get("tailMid").xRot-tail)<.001,"continuous tail across tick/gait boundaries");check(horse.nodes.get("tailTip0").xRot==0&&horse.nodes.get("tailTip2").xRot==0,"tail tuft has one shared articulation");
        boss.poseLegs(.8F);float right=boss.nodes.get("hip_r").xRot;check(right>.5&&boss.nodes.get("hip_l").xRot<-.5,"visible opposite stride on real hip nodes");boss.poseLegs(-.8F);check(boss.nodes.get("hip_r").xRot<-.5&&boss.nodes.get("hip_r").getChild("knee").xRot>.4,"real stride reverses and bends knee");
        check(com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/data/slavicmyths/damage_type/earth_strike.json"))).getAsJsonObject().get("scaling").getAsString().equals("never"),"physical strike resource disables difficulty scaling");float oldEasy=org.slavicmyths.earth.EarthRules.afterArmor(Math.min(org.slavicmyths.earth.EarthRules.MELEE_RAW/2+1,org.slavicmyths.earth.EarthRules.MELEE_RAW),20,8)*.8F;check(oldEasy>2.6&&oldEasy<2.8,"saved EASY + Resistance I explains weak prior hit");check(Math.abs(org.slavicmyths.earth.EarthRules.afterArmor(org.slavicmyths.earth.EarthRules.MELEE_RAW,20,8)*.8F-6.4F)<.001,"Resistance I remains effective without difficulty attenuation");
        System.out.println("VISUAL OFFLINE PASS: "+checks+" assertions. No runtime visual acceptance claimed.");
    }
}
