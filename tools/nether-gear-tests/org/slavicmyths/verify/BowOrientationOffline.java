package org.slavicmyths.verify;
import java.nio.file.*;import com.google.gson.*;import org.joml.Vector3f;
import com.mojang.blaze3d.vertex.PoseStack;import net.minecraft.client.renderer.block.model.ItemTransform;
import org.slavicmyths.client.BowDrawClient;

/** Native hand matrices and actual model JSON. Not an in-game screenshot or arrow-hit test. */
public final class BowOrientationOffline {
 static Vector3f vec(JsonArray a){return new Vector3f(a.get(0).getAsFloat(),a.get(1).getAsFloat(),a.get(2).getAsFloat());}
 static Vector3f axis(JsonObject display,boolean left,boolean first,Vector3f local){
  var pose=new PoseStack();if(first)BowDrawClient.drawPose(pose,left,0,30,24);
  new ItemTransform(vec(display.getAsJsonArray("rotation")),new Vector3f(),vec(display.getAsJsonArray("scale"))).apply(left,pose);
  return pose.last().pose().transformDirection(local,new Vector3f()).normalize();
 }
 public static void main(String[] args)throws Exception{
  int checks=0;
  for(String name:new String[]{"short_bow","heavy_bow","infernal_bow"})for(String suffix:new String[]{"","_pulling_0","_pulling_1","_pulling_2"}){
   var model=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/slavicmyths/models/item/"+name+suffix+".json"))).getAsJsonObject();
   for(boolean left:new boolean[]{false,true})for(boolean first:new boolean[]{false,true}){
    String context=(first?"firstperson_":"thirdperson_")+(left?"lefthand":"righthand");
    // Actual vanilla asset transforms. Its pulling sprite arrow points upper-left in pixel space.
    float[] r=first?new float[]{0,left?90:-90,left?-25:25}:new float[]{-80,left?-280:260,left?40:-40};
    var vanilla=new JsonObject();var rotation=new JsonArray();for(float v:r)rotation.add(v);vanilla.add("rotation",rotation);
    var scale=new JsonArray();for(int i=0;i<3;i++)scale.add(1);vanilla.add("scale",scale);
    var expected=axis(vanilla,left,first,new Vector3f(-1,1,0));
    var actual=axis(model.getAsJsonObject("display").getAsJsonObject(context),left,first,new Vector3f(1,0,0));
    if(expected.dot(actual)<.99999F)throw new AssertionError("Wrong bow arrow axis "+name+suffix+" "+context+": "+actual+" vs "+expected);
    if(expected.dot(axis(vanilla,left,first,new Vector3f(1,0,0)))>0)throw new AssertionError("Negative old-axis control did not fail");
    checks+=2;
   }
  }
  System.out.println("Native bow orientation PASS "+checks+";48 actual model/hand matrices match vanilla shooting axis;48 reversed-axis negative controls; actual client before/after still required");
 }
}
