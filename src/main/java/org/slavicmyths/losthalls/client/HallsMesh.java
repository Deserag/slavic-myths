package org.slavicmyths.losthalls.client;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
/** Source-driven native cuboids, separate hinges and non-overlapping atlas islands. */
public final class HallsMesh {
 public final ModelPart root;public final Map<String,ModelPart> parts=new LinkedHashMap<>();
 public HallsMesh(String name){this(read(name));}
 public static JsonObject read(String name){try(var stream=HallsMesh.class.getResourceAsStream("/assets/slavicmyths/lost_halls_rigs/"+name+".json")){if(stream==null)throw new IllegalStateException("Missing Halls rig "+name);return JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();}catch(java.io.IOException e){throw new IllegalStateException(e);}}
 public HallsMesh(JsonObject data){var mesh=new MeshDefinition();Map<String,PartDefinition> defs=new LinkedHashMap<>();defs.put("root",mesh.getRoot());
  for(var raw:data.getAsJsonArray("parts")){var p=raw.getAsJsonObject();var s=p.getAsJsonArray("size");var v=p.getAsJsonArray("pivot");var uv=p.getAsJsonArray("uv");var r=p.getAsJsonArray("rotation");var off=p.getAsJsonArray("offset");String n=p.get("name").getAsString();defs.put(n,defs.get(p.get("parent").getAsString()).addOrReplaceChild(n,CubeListBuilder.create().texOffs(uv.get(0).getAsInt(),uv.get(1).getAsInt()).addBox(off.get(0).getAsFloat(),off.get(1).getAsFloat(),off.get(2).getAsFloat(),s.get(0).getAsFloat(),s.get(1).getAsFloat(),s.get(2).getAsFloat()),PartPose.offsetAndRotation(v.get(0).getAsFloat(),v.get(1).getAsFloat(),v.get(2).getAsFloat(),r.get(0).getAsFloat(),r.get(1).getAsFloat(),r.get(2).getAsFloat())));}
  root=LayerDefinition.create(mesh,data.get("atlas").getAsInt(),data.get("atlas").getAsInt()).bakeRoot();collect("root",root,data);
 }
 private void collect(String n,ModelPart p,JsonObject data){parts.put(n,p);for(var raw:data.getAsJsonArray("parts")){var j=raw.getAsJsonObject();if(j.get("parent").getAsString().equals(n)){String child=j.get("name").getAsString();collect(child,p.getChild(child),data);}}}
 public void reset(){root.getAllParts().forEach(ModelPart::resetPose);}
 public void pose(float walk,float amount,float age,float cast,float mask,int hurt,int death){reset();
  for(var e:parts.entrySet()){String n=e.getKey();var p=e.getValue();
   if(n.startsWith("leg"))p.xRot+=(float)Math.cos(walk*.7+(n.endsWith("0")||n.endsWith("3")?0:Math.PI))*amount*.6F;
   if(n.startsWith("ribbon")||n.startsWith("hair"))p.zRot+=(float)Math.sin(age*.045+n.hashCode())*.08F;
   if(n.startsWith("arm")){float wind=Math.min(1,cast/16);p.xRot-=wind*1.2F;p.zRot+=(float)Math.sin(age*.04+n.hashCode())*.06F;if(parts.containsKey("mask_ring")){int mode=Math.floorMod(Math.round(mask),4);boolean left=n.endsWith("0");p.zRot+=mode==0?(left?-1:1)*wind*.7F:mode==2?(left?-.25F:.25F)*wind:0;p.xRot=mode==1?(left?-.3F:-1.5F)*wind:mode==2?.45F*wind:-wind*1.3F;}}
   if(n.startsWith("wing")){float side=n.contains("left")?1:-1;p.zRot+=side*(float)Math.sin(age*.5)*(n.contains("tip")?.7F:.45F);}
   if(n.equals("mask_ring"))p.yRot=mask*(float)Math.PI/2;
   if(n.equals("body")&&parts.containsKey("hood"))p.y+=(float)Math.sin(age*.07)*.7F;
   if(n.equals("body")&&parts.containsKey("leg3")&&parts.containsKey("crystal0"))p.xRot+=(float)Math.sin(Math.min(Math.PI,cast*Math.PI/30))*.2F;
  }
  if(hurt>0)root.zRot=(float)Math.sin(hurt)*.08F;
  if(death>0){root.xRot=Math.min(1.2F,death*.035F);root.y+=Math.min(12,death*.25F);}
 }
}
