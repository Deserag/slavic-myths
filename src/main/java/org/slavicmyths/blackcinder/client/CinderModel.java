package org.slavicmyths.blackcinder.client;
import java.util.*;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.blackcinder.*;
/** Authored cuboids with disjoint UV islands, pivots and per-role animation. */
public final class CinderModel extends HierarchicalModel<CinderMob> {
 private final ModelPart root;
 private final Map<String,ModelPart> nodes=new LinkedHashMap<>();
 public CinderModel(CinderContent.Kind kind){this(load(kind));}
 private static JsonObject load(CinderContent.Kind kind){
  try(var reader=Minecraft.getInstance().getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath("slavicmyths","cinder_rigs/"+CinderContent.id(kind)+".json"))){return JsonParser.parseReader(reader).getAsJsonObject();}catch(java.io.IOException ex){throw new IllegalStateException("Missing Cinder rig "+kind,ex);}
 }
 public CinderModel(JsonObject data){
  var mesh=new MeshDefinition();var parts=new LinkedHashMap<String,PartDefinition>();parts.put("root",mesh.getRoot());
   for(var element:data.getAsJsonArray("parts")){var p=element.getAsJsonObject();var pivot=p.getAsJsonArray("pivot");var size=p.getAsJsonArray("size");var uv=p.getAsJsonArray("uv");float x=size.get(0).getAsFloat(),y=size.get(1).getAsFloat(),z=size.get(2).getAsFloat();var offset=p.has("offset")?p.getAsJsonArray("offset"):null;var rotation=p.has("rotation")?p.getAsJsonArray("rotation"):null;String name=p.get("name").getAsString();var part=parts.get(p.get("parent").getAsString()).addOrReplaceChild(name,CubeListBuilder.create().texOffs(uv.get(0).getAsInt(),uv.get(1).getAsInt()).addBox(offset==null?-x/2:offset.get(0).getAsFloat(),offset==null?-y/2:offset.get(1).getAsFloat(),offset==null?-z/2:offset.get(2).getAsFloat(),x,y,z),PartPose.offsetAndRotation(pivot.get(0).getAsFloat(),pivot.get(1).getAsFloat(),pivot.get(2).getAsFloat(),rotation==null?0:rotation.get(0).getAsFloat(),rotation==null?0:rotation.get(1).getAsFloat(),rotation==null?0:rotation.get(2).getAsFloat()));parts.put(name,part);}
   root=LayerDefinition.create(mesh,data.get("atlas").getAsInt(),data.get("atlas").getAsInt()).bakeRoot();collect("root",root,data.getAsJsonArray("parts"));
 }
 private void collect(String name,ModelPart part,JsonArray definitions){nodes.put(name,part);for(var element:definitions){var p=element.getAsJsonObject();if(p.get("parent").getAsString().equals(name)){String child=p.get("name").getAsString();collect(child,part.getChild(child),definitions);}}}
 @Override public ModelPart root(){return root;}
 @Override public void setupAnim(CinderMob e,float walk,float amount,float age,float yaw,float pitch){
  pose(e.kind,e.action(),e.actionTime(age-e.tickCount),e.second(),e.flying(),walk,amount,age,yaw,pitch,e.hurtTime,e.deathTime);
 }
 public void pose(CinderContent.Kind kind,CinderMob.Action action,float time,boolean second,boolean flying,float walk,float amount,float age,float yaw,float pitch,int hurt,int death){
  root.getAllParts().forEach(ModelPart::resetPose);float wind=CinderMob.windup(action);float charge=wind==0?0:Math.min(1,time/wind);boolean active=time>=wind;
  for(var entry:nodes.entrySet()){
   String n=entry.getKey();var p=entry.getValue();
   if(n.equals("head")||n.equals("face")){p.yRot=yaw*.0174533F;p.xRot=pitch*.0174533F;}
   if(n.startsWith("leg")&&kind!=CinderContent.Kind.COAL_CRAWLER){int i=n.charAt(3)-'0';p.xRot=(float)Math.cos(walk*.7+(i%2)*Math.PI)*amount*.6F;}
   if(n.startsWith("cloud")||n.startsWith("fleck")){p.y+=(float)Math.sin(age*.06+n.hashCode())*1.5F;p.yRot=(float)Math.sin(age*.025+n.hashCode())*.25F;}
   if(kind==CinderContent.Kind.ASH_SPIRIT&&(n.startsWith("cloud")||n.equals("whorl"))&&(action==CinderMob.Action.SMOKE||action==CinderMob.Action.PULSE)){float spread=active?1+(float)Math.sin(Math.min(Math.PI,(time-wind)*.18))*.5F:1-charge*.35F;p.x*=spread;p.z*=spread;p.yRot+=charge*.8F;if(n.equals("whorl"))p.xRot=charge*.18F;}
   if(n.startsWith("ribbon")||n.startsWith("hair")||n.startsWith("ash_tress")){p.xRot+=(float)Math.sin(age*.045+n.hashCode())*(second?.22F:.1F);p.zRot+=(float)Math.sin(age*.035+n.hashCode())*.08F;}
   if(n.startsWith("gown")||n.startsWith("fold")){p.zRot+=(float)Math.sin(age*.035+n.hashCode())*.025F;p.y+=(float)Math.sin(age*.04+n.hashCode())*.3F;}
   if(n.startsWith("arm")){
    p.zRot+=(n.endsWith("0")?1:-1)*.07F;
    if(action!=CinderMob.Action.IDLE){p.xRot=active?(float)Math.sin((time-wind)*.2)*.45F:-charge*1.4F;if(action==CinderMob.Action.VORTEX)p.zRot=(n.endsWith("0")?1:-1)*1.15F;}
    if(action==CinderMob.Action.COAL&&n.endsWith("0"))p.xRot=-.15F;
   }
   if(n.startsWith("forearm"))p.xRot+=action==CinderMob.Action.IDLE?-.18F:-charge*.7F;
   if(n.equals("jaw")&&action==CinderMob.Action.BITE)p.xRot=active?-.08F:charge*.65F;
  }
  float gesture=org.slavicmyths.client.MobMotion.envelope(time,wind,wind+18);
  if(kind==CinderContent.Kind.COAL_CRAWLER){
   for(int i=0;i<4;i++){int side=i%2==0?-1:1;float phase=walk*.55F+((i==0||i==3)?0:(float)Math.PI);
    org.slavicmyths.client.MobMotion.leg(nodes.get("leg"+i),nodes.get("upper"+i),nodes.get("shin"+i),nodes.get("foot"+i),side,13,10,8,11,phase,amount,.72F,action==CinderMob.Action.BITE&&i<2?gesture:0);
   }nodes.get("body").y+=(float)Math.sin(walk*1.1)*amount*.25F;
  }
  if(kind==CinderContent.Kind.PEKELNIK){
   for(int i=0;i<2;i++){float step=(float)Math.sin(walk*.5+i*Math.PI)*amount;nodes.get("leg"+i).xRot=step*.35F;nodes.get("shin"+i).xRot=Math.max(0,-step)*.65F;nodes.get("arm"+i).xRot=action==CinderMob.Action.IDLE?-step*.18F:-gesture*(i==0?.3F:1.5F);}
   nodes.get("body").zRot=(float)Math.sin(walk*.5)*amount*.055F;
  }
  if(kind==CinderContent.Kind.ASH_SPIRIT){nodes.get("whorl").yRot+=age*.045F;nodes.get("face").zRot=(float)Math.sin(age*.035)*.08F;}
  if(kind==CinderContent.Kind.SPARKLING){float pulse=1+(float)Math.sin(age*.19)*.08F+(float)Math.sin(age*.47)*.035F;nodes.get("gold_core").xScale=nodes.get("gold_core").yScale=nodes.get("gold_core").zScale=pulse;root.x=(float)Math.sin(age*.043)*.9F;root.z=(float)Math.sin(age*.073)*.65F;}
  if(flying)root.y=(float)Math.sin(age*.06)*(kind==CinderContent.Kind.SPARKLING?1.5F:1);
  if(action==CinderMob.Action.BITE)root.xRot=active?-.12F:charge*.12F;
  if(action==CinderMob.Action.PHASE)root.yRot=(float)Math.sin(time*.13)*.1F;
  if(hurt>0)root.zRot=(float)Math.sin(hurt*1.1)*.08F;
  if(death>0){root.y+=death*.45F;root.xRot=Math.min(1.1F,death*.045F);}
 }
}
