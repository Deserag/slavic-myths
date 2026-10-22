package org.slavicmyths.verify;
import org.slavicmyths.client.FlailMotion;
import com.google.gson.GsonBuilder;
import java.nio.file.*;
import java.util.*;
public final class Weapons87FlailMotion {
 public static void main(String[] args)throws Exception {
  List<Map<String,Object>> samples=new ArrayList<>();
  for(boolean heavy:new boolean[]{false,true})for(int age=0;age<120;age++)for(float walk:new float[]{0,.5f,1})for(float charge:new float[]{0,.5f,1})for(float swing:new float[]{-1,0,.05f,.25f,.5f,.75f,1}){
   var p=FlailMotion.sample(age,walk,charge,Math.max(swing,0),swing>=0,heavy);
   var anchor=new FlailMotion.Point(8,17,8);
   for(int i=0;i<7;i++){
    var dir=p.directions()[i];double length=Math.sqrt(dir.x()*dir.x()+dir.y()*dir.y()+dir.z()*dir.z());
    if(Math.abs(length-1)>1e-10||dir.y()>=0)throw new AssertionError("Non-unit/inverted link");
    var expected=anchor.add(dir,1.2);if(distance(expected,p.links()[i])>1e-9)throw new AssertionError("Link attachment");anchor=anchor.add(dir,2.4);
   }
   if(distance(anchor.add(p.directions()[6],2),p.ball())>1e-9)throw new AssertionError("Detached ball");
   if(swing<0&&p.ball().y()>=8)throw new AssertionError("Idle/charge head above grip");
   if(swing==1){var idle=FlailMotion.sample(age,walk,charge,0,false,heavy);if(distance(p.ball(),idle.ball())>1e-8)throw new AssertionError("Discontinuous return");}
  }
  for(boolean heavy:new boolean[]{false,true})for(String phase:List.of("idle","walk","charge9","charge18","contact","trailing","return","rest")){
   float walk=phase.equals("walk")?1:0,charge=phase.equals("charge9")?.5f:phase.equals("charge18")?1:0;
   float swing=switch(phase){case "contact"->.5f;case "trailing"->.7f;case "return"->.95f;case "rest"->1;default->-1;};
   if(heavy&&charge>0)continue; // Ataman is a MaceItem: never invent a charge.
   var item=new LinkedHashMap<String,Object>();item.put("heavy",heavy);item.put("phase",phase);item.put("age",20);item.put("walk",walk);item.put("charge",charge);item.put("swing",swing);var matrices=new LinkedHashMap<String,float[]>();
   for(boolean left:new boolean[]{false,true}){
    var nativePose=new com.mojang.blaze3d.vertex.PoseStack();org.slavicmyths.client.FlailClient.firstPersonWrist(nativePose,left,0,Math.max(swing,0));
    var model=com.google.gson.JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/slavicmyths/models/item/flail.json"))).getAsJsonObject();
    var gson=new GsonBuilder().registerTypeAdapter(net.minecraft.client.renderer.block.model.ItemTransform.class,new net.minecraft.client.renderer.block.model.ItemTransform.Deserializer()).create();
    gson.fromJson(model.getAsJsonObject("display").getAsJsonObject("firstperson_"+(left?"lefthand":"righthand")),net.minecraft.client.renderer.block.model.ItemTransform.class).apply(left,nativePose);
    matrices.put(left?"LEFT":"RIGHT",nativePose.last().pose().get(new float[16]));
   }
   item.put("fp_matrices",matrices);item.put("pose",FlailMotion.sample(20,walk,charge,Math.max(0,swing),swing>=0,heavy));samples.add(item);
  }
  Path out=Path.of("docs/implementation/weapons87/flail-motion-native.json");Files.writeString(out,new GsonBuilder().setPrettyPrinting().create().toJson(samples));
  System.out.println("PASS: bounded non-inverted connected chain, idle/charge below grip, exact return; 15120 actual numeric poses; no game/world/entity. Exported "+samples.size()+" phase poses.");
 }
 private static double distance(FlailMotion.Point a,FlailMotion.Point b){return Math.sqrt(Math.pow(a.x()-b.x(),2)+Math.pow(a.y()-b.y(),2)+Math.pow(a.z()-b.z(),2));}
}
