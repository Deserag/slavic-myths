package org.slavicmyths.client;

import java.nio.file.*;
import java.util.*;
import com.mojang.blaze3d.vertex.PoseStack;
import org.slavicmyths.trophy.*;

/** Actual native ModelPart rendering on a plain JVM: no entity/world/client/server. */
public final class Heads140Offline {
 private static int checks;
 private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception {
  var entries=new ArrayList<Map<String,Object>>();
  for(var kind:TrophyHeads.Kind.values()){
   var h=TrophyHeadsClient.create(kind);var raw=new TrophyHeadsClient.Capture();h.part.render(new PoseStack(),raw,0,0);
   var placed=new TrophyHeadsClient.Capture();h.render(new PoseStack(),placed,0,0);
   check(!raw.vertices.isEmpty()&&raw.vertices.size()%4==0,"actual source quads "+kind);
   check(raw.vertices.size()==placed.vertices.size(),"no added/removed head vertices "+kind);
   for(int i=0;i<raw.vertices.size();i++)for(int c=3;c<5;c++)check(raw.vertices.get(i)[c]==placed.vertices.get(i)[c],"native UV unchanged "+kind);
   var b=placed.bounds();check(b[0]>=0&&b[2]>=0&&b[3]<=1&&b[5]<=1&&b[1]>=0&&b[4]<=kind.height,"head fits reserved cells "+kind);
   var server=kind.bounds();for(int i=0;i<6;i++)check(Math.abs(server[i]-b[i])<.00001F,"server collision matches actual rendered head "+kind);
   check(kind.height==1||b[4]>1,"no invisible unused upper cell "+kind);
   if(kind==TrophyHeads.Kind.LIKHO||kind==TrophyHeads.Kind.MOR_LIKHO){
    var baseline=placed.vertices;
    for(int t=0;t<260;t+=5){
     h.pose(t);
     var animated=new TrophyHeadsClient.Capture();h.render(new PoseStack(),animated,0,0);
     check(animated.vertices.size()==baseline.size(),"animation cannot add head geometry "+kind);
     for(int v=0;v<baseline.size();v++)for(int uv=3;uv<=4;uv++)
      check(animated.vertices.get(v)[uv]==baseline.get(v)[uv],"animation must preserve native UV "+kind);
     var ab=animated.bounds();
     check(ab[0]>=0&&ab[2]>=0&&ab[3]<=1&&ab[5]<=1&&ab[1]>=0&&ab[4]<=kind.height,"animated head stays in 1/2 cells "+kind);
    }
    h.resetPose();
    var neutral=new TrophyHeadsClient.Capture();h.render(new PoseStack(),neutral,0,0);
    check(Arrays.equals(neutral.bounds(),b),"head returns to native bounds after animation "+kind);
   }
   entries.add(Map.of("id",kind.id,"height",kind.height,"texture",h.texture.toString(),"native_bounds",h.bounds,"placed_bounds",b,"uniform_scale",h.scale,"before_native_vertices",raw.vertices,"after_placed_vertices",placed.vertices));
  }
  check(TrophyHeadFxRules.lidOffset(0)==0&&TrophyHeadFxRules.lidOffset(66)>.6F&&TrophyHeadFxRules.lidOffset(72)==0,"normal Likho deterministic eyelid");
  check(TrophyHeadFxRules.morEyeScale(0)>1&&TrophyHeadFxRules.morEyeScale(10)>1,"Mor pulse stays visible");
  check(TrophyHeadFxRules.likhoGlow(70)&&!TrophyHeadFxRules.likhoGlow(25),"Likho glow timing");
  check(TrophyHeadFxRules.particleDue(100,Long.MIN_VALUE,52)&&!TrophyHeadFxRules.particleDue(101,100,52)&&TrophyHeadFxRules.particleDue(152,100,52),"bounded cosmetic particles");
  String[] mobs={"tugarin_zmey","skim_beast","cursed_volkolak","likho_one_eyed","large_podzemnik"};
  for(String mob:mobs)check(TrophyHeadDrops.kind(mob,false,true)!=null,"guaranteed mapped drop "+mob);
  check(TrophyHeadDrops.kind("likho_one_eyed",true,false)==TrophyHeads.Kind.MOR_LIKHO,"mor form distinct trophy");
  check(TrophyHeadDrops.kind("tugarin_zmey",false,false)==null,"human phase excluded");
  for(String mob:new String[]{"grey_wolf","gorynych","great_poloz","podzemnik","kladovik","zombie"})check(TrophyHeadDrops.kind(mob,false,true)==null,"excluded mob "+mob);
  Path out=Path.of("docs/implementation/heads140-generation141/native-heads.json");Files.createDirectories(out.getParent());
  Files.writeString(out,new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(Map.of("heads",entries,"checks",checks,"runtime_launches",0))+"\n");
  System.out.println("HEADS140_NATIVE PASS: six real ModelPart subtrees, unchanged UV, reserved bounds, drop form matrix; checks="+checks);
 }
}
