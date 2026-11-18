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
 /** Individual ordinary-mob timelines; Posten retains the separate mask pose above. */
 public void poseMob(String kind,float walk,float amount,float age,float cast,int hurt,int death,boolean sit,boolean glide,int variant){
  reset();float pulse=org.slavicmyths.client.MobMotion.envelope(cast,16,30);float recoil=cast>=16&&cast<24?(float)Math.sin((cast-16)*Math.PI/8):0;
  for(var e:parts.entrySet()){String n=e.getKey();var p=e.getValue();
   if(n.startsWith("variant_hood"))p.visible=n.equals("variant_hood"+Math.floorMod(variant,4));
   if(n.startsWith("ribbon")){p.xRot+=(float)Math.sin(age*.06-n.hashCode()%5)*.08F+pulse*.18F;p.zRot+=(float)Math.sin(age*.035+n.hashCode())*.05F;}
   if(n.startsWith("leg")||n.startsWith("knee")){int i=n.charAt(n.length()-1)-'0';float speed=kind.equals("griboshmyg")?1.2F:kind.equals("oskolnik")?.45F:.65F;float step=(float)Math.sin(walk*speed+(i==0||i==3?0:Math.PI))*amount;p.xRot+=n.startsWith("knee")?Math.max(0,-step)*.65F:step*.45F;}
   if(n.startsWith("arm")){int side=n.endsWith("0")?-1:1;p.xRot-=pulse*(kind.equals("sheptun")?(side<0?.55F:1.45F):.85F);p.xRot+=recoil*.45F;p.zRot+=side*((float)Math.sin(age*.045)*.035F+pulse*.12F);}
   if(n.startsWith("forearm"))p.xRot-=pulse*.55F;
   if(n.startsWith("crystal")){int i=n.charAt(n.length()-1)-'0';float resonance=pulse*(float)Math.sin(age*.4-i*.9);p.y-=resonance*.6F;p.zRot+=resonance*.07F;}
   if(n.startsWith("wing")){float side=n.contains("left")?-1:1;boolean tip=n.contains("tip");p.zRot=side*(sit?(tip?1.15F:1.35F):glide?(tip?.08F:.12F):(float)Math.sin(age*.23-(tip?.8:0))*(tip?.6F:.4F));p.yRot+=side*pulse*.2F;}
   if(n.startsWith("ear"))p.xRot+=(float)Math.sin(age*.08+n.hashCode())*.06F;
   if(n.startsWith("sidefungus"))p.y+=(float)Math.sin(walk*2.4-1)*amount*.3F;
  }
  var body=parts.get("body");
  switch(kind){
   case "sheptun"->{body.y+=(float)Math.sin(age*.055)*.7F;body.xRot=pulse*.12F-recoil*.2F;parts.get("head").zRot=(float)Math.sin(age*.032)*.04F;}
   case "oskolnik"->{body.zRot=(float)Math.sin(walk*.45)*amount*.055F;body.y+=Math.abs((float)Math.sin(walk*.45))*amount*.3F;body.xRot=pulse*.1F;}
   case "gulokryl"->{body.y+=sit?2:glide?.2F:(float)Math.sin(age*.23)*.3F;parts.get("head").xRot-=pulse*.3F;}
   case "ekhovik"->{body.zRot=(float)Math.sin(walk*.65)*amount*.045F;body.xRot=pulse*.16F-recoil*.12F;}
   case "griboshmyg"->{body.y-=Math.abs((float)Math.sin(walk*1.2))*amount*.5F;var cap=parts.get("cap");cap.y+=(float)Math.sin(walk*2.4-.7)*amount*.55F+(float)Math.sin(age*.06)*.12F;cap.zRot=(float)Math.sin(walk*1.2-.5)*amount*.06F;if(cast<0){float feed=(float)Math.sin(-cast*Math.PI/12);parts.get("head").xRot=feed*.25F;cap.y-=feed*.6F;}}
  }
  if(hurt>0){root.zRot=(float)Math.sin(hurt)*(kind.equals("oskolnik")?.025F:.07F);body.xRot-=.08F;}
  if(death>0){float d=Math.min(1,death/20F);root.xRot=d*(kind.equals("gulokryl")?1.1F:.55F);root.y+=d*(kind.equals("sheptun")?12:4);if(kind.equals("griboshmyg"))parts.get("cap").zRot+=d*.25F;}
 }

}
