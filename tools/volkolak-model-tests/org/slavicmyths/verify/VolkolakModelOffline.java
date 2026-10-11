package org.slavicmyths.verify;
import java.lang.reflect.*;
import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
/** Native baked vertices and UVs, exported for independent projection. Does not launch Minecraft. */
public final class VolkolakModelOffline {
 private static Object field(Object o,String name)throws Exception{var f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 public static void main(String[] args)throws Exception{var model=new org.slavicmyths.client.CursedVolkolakModel();var parts=(Map<String,net.minecraft.client.model.geom.ModelPart>)field(model,"parts");int checks=0,cubes=0;var export=new JsonObject();var list=new JsonArray();
  for(var entry:parts.entrySet()){var p=entry.getValue();var item=new JsonObject();item.addProperty("name",entry.getKey());var vertices=new JsonArray();for(var cube:(List<?>)field(p,"cubes")){cubes++;for(var face:(Object[])field(cube,"polygons"))for(var vertex:(Object[])field(face,"vertices")){float u=(float)field(vertex,"u"),v=(float)field(vertex,"v");if(!Float.isFinite(u)||!Float.isFinite(v)||u<0||u>1||v<0||v>1)throw new AssertionError("UV outside atlas");var xyz=(org.joml.Vector3f)field(vertex,"pos");var a=new JsonArray();a.add(xyz.x());a.add(xyz.y());a.add(xyz.z());a.add(u);a.add(v);vertices.add(a);checks++;}}item.add("vertices",vertices);list.add(item);}
  for(String name:List.of("root","pelvis","spine","chest","neck","head","jaw","earL","earR","upperL","upperR","foreL","foreR","handL","handR","thighL","thighR","shinL","shinR","footL","footR","tail1","tail2","tail3","fangUpperL","fangUpperR","eyeL","eyeR")){if(!parts.containsKey(name))throw new AssertionError(name);checks++;}
  export.add("parts",list);export.addProperty("checks",checks);export.addProperty("cubes",cubes);Path out=Path.of("work/volkolak-143/native-geometry.json");Files.createDirectories(out.getParent());Files.writeString(out,new GsonBuilder().setPrettyPrinting().create().toJson(export));System.out.println("VOLKOLAK_MODEL_OFFLINE_CHECKS="+checks+" parts="+parts.size()+" cubes="+cubes+"; no client launch");
 }
}
