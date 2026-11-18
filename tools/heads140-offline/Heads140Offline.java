package org.slavicmyths.client;

import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import org.slavicmyths.trophy.*;

/** Actual native ModelPart rendering on a plain JVM: no entity/world/client/server. */
public final class Heads140Offline {
 private static int checks;
 private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 private static net.minecraft.client.model.geom.ModelPart bone(TrophyHeadsClient.Head h,String name)throws Exception{var f=h.getClass().getDeclaredField(name);f.setAccessible(true);return (net.minecraft.client.model.geom.ModelPart)f.get(h);}
 private static List<String> canonical(List<float[]> vertices){var result=new ArrayList<String>();for(var v:vertices){var s=new StringBuilder();for(float f:v)s.append(Math.round(f*10000)).append(',');result.add(s.toString());}Collections.sort(result);return result;}
 public static void main(String[] args)throws Exception {
  var entries=new ArrayList<Map<String,Object>>();
  var boundsIssues=new ArrayList<String>();
  // Historical six-head regression; Magma Lord uses burning-islands-tests native poses.
  for(var kind:java.util.EnumSet.range(TrophyHeads.Kind.TUGARIN,TrophyHeads.Kind.PODZEMNIK)){
   var h=TrophyHeadsClient.create(kind);var raw=new TrophyHeadsClient.Capture();h.part.render(new PoseStack(),raw,0,0);
   var placed=new TrophyHeadsClient.Capture();h.render(new PoseStack(),placed,0,0);
   if(kind==TrophyHeads.Kind.SKIM){
    var original=new TrophyHeadsClient.Head(kind,SkimMesh.create().getChild("body").getChild("neck"),"textures/entity/skim_beast.png");
    var old=new TrophyHeadsClient.Capture();original.render(new PoseStack(),old,0,0);
    check(canonical(old.vertices).equals(canonical(placed.vertices)),"Skim split eye/ear bones preserve every neutral vertex and UV");
   }
   check(!raw.vertices.isEmpty()&&raw.vertices.size()%4==0,"actual source quads "+kind);
   check(raw.vertices.size()==placed.vertices.size(),"no added/removed head vertices "+kind);
   for(int i=0;i<raw.vertices.size();i++)for(int c=3;c<5;c++)check(raw.vertices.get(i)[c]==placed.vertices.get(i)[c],"native UV unchanged "+kind);
   var b=placed.bounds();check(b[0]>=0&&b[2]>=0&&b[3]<=1&&b[5]<=1&&b[1]>=0&&b[4]<=kind.height,"head fits reserved cells "+kind);
   var server=kind.bounds();for(int i=0;i<6;i++)check(Math.abs(server[i]-b[i])<.00001F,"server collision matches actual rendered head "+kind);
   check(kind.height==1||b[4]>1,"no invisible unused upper cell "+kind);
   {
    var baseline=placed.vertices;
    boolean actualMotion=false,boundsViolation=false;
    for(int t=0;t<600;t++){
     h.pose(t);
     var animated=new TrophyHeadsClient.Capture();h.render(new PoseStack(),animated,0,0);
     check(animated.vertices.size()==baseline.size(),"animation cannot add head geometry "+kind);
     for(int v=0;v<baseline.size();v++){
      if(Math.abs(animated.vertices.get(v)[0]-baseline.get(v)[0])>.00001F||
         Math.abs(animated.vertices.get(v)[1]-baseline.get(v)[1])>.00001F||
         Math.abs(animated.vertices.get(v)[2]-baseline.get(v)[2])>.00001F)actualMotion=true;
     }
     for(int v=0;v<baseline.size();v++)for(int uv=3;uv<=4;uv++)
      check(animated.vertices.get(v)[uv]==baseline.get(v)[uv],"animation must preserve native UV "+kind);
     var ab=animated.bounds();
     if(!(ab[0]>=0&&ab[2]>=0&&ab[3]<=1&&ab[5]<=1&&ab[1]>=0&&ab[4]<=kind.height)&&!boundsViolation){
      boundsViolation=true;boundsIssues.add(kind+" t="+t+" bounds="+Arrays.toString(ab)+" base="+Arrays.toString(b));
     }
    }
    check(actualMotion,"each head must animate its native part "+kind);
    h.resetPose();
    var neutral=new TrophyHeadsClient.Capture();h.render(new PoseStack(),neutral,0,0);
    check(Arrays.equals(neutral.bounds(),b),"head returns to native bounds after animation "+kind);
    check(canonical(neutral.vertices).equals(canonical(baseline)),"all animated vertices restored "+kind);
    if(kind==TrophyHeads.Kind.LIKHO||kind==TrophyHeads.Kind.MOR_LIKHO){
     var lid=bone(h,"lid");var eye=bone(h,"glow");
     h.pose(29,0,0);
     check(lid.yScale>=6&&lid.z<eye.z,"blink covers the eye from in front, not behind it "+kind);
     h.resetPose();
    }
    if(kind==TrophyHeads.Kind.LIKHO||kind==TrophyHeads.Kind.MOR_LIKHO||kind==TrophyHeads.Kind.SKIM||kind==TrophyHeads.Kind.TUGARIN){
     h.pose(0,-1.1F,-.75F);var left=new TrophyHeadsClient.Capture();h.render(new PoseStack(),left,0,0);
     h.pose(0,1.1F,.75F);var right=new TrophyHeadsClient.Capture();h.render(new PoseStack(),right,0,0);
     check(!canonical(left.vertices).equals(canonical(right.vertices)),"actual eye vertices follow both gaze directions "+kind);
     h.resetPose();
    }
    if(kind==TrophyHeads.Kind.SKIM||kind==TrophyHeads.Kind.VOLKOLAK){
     var left=bone(h,"earLeft");var right=bone(h,"earRight");float l=left.zRot,r=right.zRot;
     h.pose(27);check(Math.abs(left.zRot-l)>.1&&Math.abs(right.zRot-r)>.1,"both ears really move "+kind);h.resetPose();
     check(left.zRot==l&&right.zRot==r,"both ears reset "+kind);
    }
   }
   entries.add(Map.of("id",kind.id,"height",kind.height,"texture",h.texture.toString(),"native_bounds",h.bounds,"placed_bounds",b,"uniform_scale",h.scale,"before_native_vertices",raw.vertices,"after_placed_vertices",placed.vertices));
  }
  check(boundsIssues.isEmpty(),"animated bounds regressions: "+String.join(" | ",boundsIssues));
  for(var facing:List.of(net.minecraft.core.Direction.NORTH,net.minecraft.core.Direction.EAST,net.minecraft.core.Direction.SOUTH,net.minecraft.core.Direction.WEST)){
   double dx=-facing.getStepZ(),dz=facing.getStepX(); // player's position on facing-relative +side
   double side=-dx*facing.getStepZ()+dz*facing.getStepX();
   check(TrophyHeadsClient.eyeLook(side,4)<0,"gaze sign matches placed model rotation "+facing);
   check(TrophyHeadsClient.eyeLook(-side,4)>0,"gaze reverses correctly "+facing);
  }
  check(TrophyHeadFxRules.lidOffset(0)==0&&TrophyHeadFxRules.lidOffset(29)>1.7F&&TrophyHeadFxRules.lidOffset(34)==0,"normal Likho deterministic eyelid");
  check(TrophyHeadFxRules.morEyeScale(0)>1&&TrophyHeadFxRules.morEyeScale(10)>1,"Mor pulse stays visible");
  check(TrophyHeadFxRules.likhoGlow(70)&&!TrophyHeadFxRules.likhoGlow(25),"Likho glow timing");
  check(TrophyHeadFxRules.particleDue(100,Long.MIN_VALUE,52)&&!TrophyHeadFxRules.particleDue(101,100,52)&&TrophyHeadFxRules.particleDue(152,100,52),"bounded cosmetic particles");
  check(TrophyHeadFxRules.tugarinJaw(0)>0&&Math.abs(TrophyHeadFxRules.tugarinWhisker(20))<=.24,"Tugarin jaw and whisker limits");
  check(TrophyHeadFxRules.skimJaw(0)>0&&Math.abs(TrophyHeadFxRules.skimMane(20))<=.20,"Skim jaw and mane limits");
  check(TrophyHeadFxRules.volkolakJaw(55)>.03&&TrophyHeadFxRules.volkolakJaw(0)==0&&TrophyHeadFxRules.volkolakEar(27)>.30,"Volkolak snarl and ear twitch");
  check(TrophyHeadFxRules.podzemnikJaw(0)>0&&Math.abs(TrophyHeadFxRules.podzemnikBrow(20))<=.14,"Podzemnik jaw and brow limits");
  String[] mobs={"tugarin_zmey","skim_beast","cursed_volkolak","likho_one_eyed","large_podzemnik"};
  for(String mob:mobs)check(TrophyHeadDrops.kind(mob,false,true)!=null,"guaranteed mapped drop "+mob);
  check(TrophyHeadDrops.kind("likho_one_eyed",true,false)==TrophyHeads.Kind.MOR_LIKHO,"mor form distinct trophy");
  check(TrophyHeadDrops.kind("tugarin_zmey",false,false)==null,"human phase excluded");
  for(String mob:new String[]{"grey_wolf","gorynych","great_poloz","podzemnik","kladovik","zombie"})check(TrophyHeadDrops.kind(mob,false,true)==null,"excluded mob "+mob);
  Path out=Path.of("work/trophy140/native-heads.json");Files.createDirectories(out.getParent());
  Files.writeString(out,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("heads",entries,"checks",checks,"runtime_launches",0))+"\n");
  System.out.println("HEADS140_NATIVE PASS: six real ModelPart subtrees, unchanged UV, reserved bounds, drop form matrix; checks="+checks);
 }
}
