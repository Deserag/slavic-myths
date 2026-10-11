package org.slavicmyths.verify;
import java.util.*;
import com.google.gson.*;
public final class FinistModelOffline {
 private static Object field(Object o,String name)throws Exception{var f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 public static void main(String[] args)throws Exception{
  var model=org.slavicmyths.client.FinistQuiverRenderer.model();
  var parts=(Map<String,net.minecraft.client.model.geom.ModelPart>)field(model.body,"children");
  int checks=0,cubes=0;var geometry=new JsonArray();
  for(var part:parts.entrySet()){
   var cubeList=(List<?>)field(part.getValue(),"cubes");
   for(var cube:cubeList){cubes++;var box=new JsonObject();box.addProperty("part",part.getKey());var bounds=new JsonArray();
    for(String key:List.of("minX","minY","minZ","maxX","maxY","maxZ")){float v=(float)field(cube,key);if(!Float.isFinite(v))throw new AssertionError("Bounds");bounds.add(v);checks++;}box.add("bounds",bounds);geometry.add(box);
    for(var face:(Object[])field(cube,"polygons"))for(var vertex:(Object[])field(face,"vertices")){float u=(float)field(vertex,"u"),v=(float)field(vertex,"v");if(!Float.isFinite(u)||!Float.isFinite(v)||u<0||u>1||v<0||v>1)throw new AssertionError("UV "+part.getKey()+" "+u+","+v);checks+=4;}
   }
  }
  if(cubes!=17)throw new AssertionError("Quiver pieces="+cubes);checks++;
  var definitions=org.slavicmyths.military.WolfStoryDefinition.ALL;
  if(definitions.size()!=4||!definitions.get(3).rewardPolicy().equals("FUTURE_LOCKED"))throw new AssertionError("Story definitions");checks+=2;
  java.nio.file.Path out=java.nio.file.Path.of("docs/verification/grey_wolf_1.4.2/previews/quiver-native-geometry.json");java.nio.file.Files.createDirectories(out.getParent());java.nio.file.Files.writeString(out,new GsonBuilder().setPrettyPrinting().create().toJson(geometry));
  System.out.println("FINIST_NATIVE_MODEL_OFFLINE PASS checks="+checks+" cubes="+cubes+"; no client/server launched");
 }
}
