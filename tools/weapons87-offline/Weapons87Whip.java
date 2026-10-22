package org.slavicmyths.verify;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransform;
import org.joml.Vector3f;
import java.nio.file.*;
public final class Weapons87Whip {
 public static void main(String[] args)throws Exception {
  var gson=new GsonBuilder().registerTypeAdapter(ItemTransform.class,new ItemTransform.Deserializer()).setPrettyPrinting().create();var rows=JsonParser.parseString(Files.readString(Path.of("docs/implementation/weapons87/whip-native-inputs.json"))).getAsJsonArray();int pivots=0;
  for(var raw:rows){var row=raw.getAsJsonObject();boolean left=row.get("left").getAsBoolean(),after=row.get("after").getAsBoolean();boolean first=row.get("context").getAsString().startsWith("firstperson");var pose=new PoseStack();if(first)pose.translate(left?-.56F:.56F,-.52F,-.72F);gson.fromJson(row.get("display"),ItemTransform.class).apply(left,pose);row.add("matrix",gson.toJsonTree(pose.last().pose().get(new float[16])));
   if(after){var grip=pose.last().pose().transformPosition(new Vector3f(0,-.5F,0));var expected=first?new Vector3f(left?-.56F:.56F,-.52F+4/16F,-.72F-2/16F):new Vector3f();if(grip.distance(expected)>1e-6F)throw new AssertionError("Whip grip off actual native palm "+row);pivots++;}
  }
  if(rows.size()!=40||pivots!=20)throw new AssertionError("Missing whip hand/state scope");Files.writeString(Path.of("docs/implementation/weapons87/whip-native.json"),gson.toJson(rows));System.out.println("PASS:5 static whip poses,40 old/current native FP/TP transforms,20 actual calibrated grip positions; no world/entity/game.");
 }
}
