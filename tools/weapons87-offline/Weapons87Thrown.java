package org.slavicmyths.verify;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransform;
import org.slavicmyths.client.WeaponProjectileRenderer;
import org.joml.Vector3f;
import java.nio.file.*;
import java.util.*;
public final class Weapons87Thrown {
 public static void main(String[] args)throws Exception {
  var gson=new GsonBuilder().registerTypeAdapter(ItemTransform.class,new ItemTransform.Deserializer()).setPrettyPrinting().create();var rows=JsonParser.parseString(Files.readString(Path.of("docs/implementation/weapons87/thrown-native-inputs.json"))).getAsJsonArray();var output=new JsonArray();int checks=0;
  for(var raw:rows){var row=raw.getAsJsonObject();boolean knife=row.get("family").getAsString().equals("throwing_knife");
   for(float yaw:new float[]{0,45,90,180,-135})for(float pitch:new float[]{-45,0,35})for(boolean after:new boolean[]{false,true}){
    var pose=new PoseStack();if(after)WeaponProjectileRenderer.flightPose(pose,yaw,pitch,knife);else{pose.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yaw-90));pose.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(pitch-45));}
    var context=after?WeaponProjectileRenderer.FLIGHT_CONTEXT:net.minecraft.world.item.ItemDisplayContext.FIXED;
    var transform=gson.fromJson(context==net.minecraft.world.item.ItemDisplayContext.FIXED?row.get("display_fixed"):row.get("display_none"),ItemTransform.class);transform.apply(false,pose);
    var axis=pose.last().pose().transformDirection(new Vector3f(after?0:1,1,0)).normalize();double y=Math.toRadians(yaw),p=Math.toRadians(pitch);var velocity=new Vector3f((float)(Math.sin(y)*Math.cos(p)),(float)Math.sin(p),(float)(Math.cos(y)*Math.cos(p)));float dot=axis.dot(velocity);
    if(after?dot<.999999F:Math.abs(dot)>1e-5)throw new AssertionError("Actual blade/velocity mismatch "+row.get("id")+" after="+after+" dot="+dot);
    var sample=new JsonObject();sample.add("id",row.get("id"));sample.addProperty("yaw",yaw);sample.addProperty("pitch",pitch);sample.addProperty("after",after);sample.addProperty("direction_dot",dot);sample.add("matrix",gson.toJsonTree(pose.last().pose().get(new float[16])));output.add(sample);checks++;
   }
  }
  if(rows.size()!=14||checks!=420)throw new AssertionError("Missing thrown coverage");Files.writeString(Path.of("docs/implementation/weapons87/continuation53/thrown-native.json"),gson.toJson(output));System.out.println("PASS:14 IDs x15 actual flight poses x2 contexts; BEFORE axis perpendicular, AFTER tip follows velocity; no world/entity/game created.");
 }
}
