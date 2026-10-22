package org.slavicmyths.verify;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemTransform;
import org.joml.Vector3f;
import java.nio.file.*;
public final class Weapons87Spears {
 public static void main(String[] args)throws Exception {
  var gson=new GsonBuilder().registerTypeAdapter(ItemTransform.class,new ItemTransform.Deserializer()).setPrettyPrinting().create();
  var rows=JsonParser.parseString(Files.readString(Path.of("docs/implementation/weapons87/spear-native-inputs.json"))).getAsJsonArray();int pivots=0;
  for(var raw:rows){var row=raw.getAsJsonObject();boolean left=row.get("left").getAsBoolean(),after=row.get("after").getAsBoolean();String id=row.get("id").getAsString(),context=row.get("context").getAsString();var pose=new PoseStack();
   var transform=gson.fromJson(row.get("display"),ItemTransform.class);
   if(context.startsWith("firstperson"))pose.translate(left?-.56F:.56F,-.52F,-.72F);
   transform.apply(left,pose);row.add("matrix",gson.toJsonTree(pose.last().pose().get(new float[16])));
   if(after&&context.startsWith("thirdperson")&&java.util.List.of("gold_spear","iron_spear","diamond_spear","perunite_spear","netherite_spear").contains(id)){
    var grip=pose.last().pose().transformPosition(new Vector3f(0,-2/16f,0));if(grip.length()>1e-6F)throw new AssertionError("Spear grip off native palm "+id+left);pivots++;
    var axis=pose.last().pose().transformDirection(new Vector3f(0,1,0));if(Math.abs(axis.length()-.7)>1e-6)throw new AssertionError("Spear actual scale");
   }
  }
  if(rows.size()!=72||pivots!=10)throw new AssertionError("Missing spear coverage");Files.writeString(Path.of("docs/implementation/weapons87/spear-native.json"),gson.toJson(rows));System.out.println("PASS:9 spear IDs,72 archived/current native context transforms;5 FIX grips centered in both palms; no game launch.");
 }
}
