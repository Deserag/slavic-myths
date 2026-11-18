package org.slavicmyths.burningislands.client;

import java.util.*;
import com.google.gson.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import org.slavicmyths.burningislands.*;
import org.slavicmyths.burningislands.MagmaCombat.Action;

/** Independently authored rigs; every cuboid/pivot/UV is exported by the native offline verifier. */
public final class IslandsModel extends HierarchicalModel<IslandsMob> {
 private final ModelPart root;
 private final Map<String,ModelPart> nodes=new LinkedHashMap<>();
 public IslandsModel(IslandsContent.Kind kind){this(load(kind));}
 private static JsonObject load(IslandsContent.Kind kind){try(var reader=Minecraft.getInstance().getResourceManager().openAsReader(ResourceLocation.fromNamespaceAndPath("slavicmyths","burning_islands_rigs/"+IslandsContent.id(kind)+".json"))){return JsonParser.parseReader(reader).getAsJsonObject();}catch(java.io.IOException ex){throw new IllegalStateException("Missing islands rig "+kind,ex);}}
 public static IslandsModel bundled(IslandsContent.Kind kind){
  try(var stream=IslandsModel.class.getResourceAsStream("/assets/slavicmyths/burning_islands_rigs/"+IslandsContent.id(kind)+".json")){
   if(stream==null)throw new IllegalStateException("Missing bundled islands rig "+kind);
   return new IslandsModel(JsonParser.parseReader(new java.io.InputStreamReader(stream,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject());
  }catch(java.io.IOException ex){throw new IllegalStateException(ex);}
 }
 public IslandsModel(JsonObject data){
  var mesh=new MeshDefinition();var definitions=new LinkedHashMap<String,PartDefinition>();definitions.put("root",mesh.getRoot());
  for(var element:data.getAsJsonArray("parts")){
   var p=element.getAsJsonObject();var pivot=p.getAsJsonArray("pivot");var size=p.getAsJsonArray("size");var uv=p.getAsJsonArray("uv");var offset=p.getAsJsonArray("offset");var rotation=p.has("rotation")?p.getAsJsonArray("rotation"):null;
   var def=definitions.get(p.get("parent").getAsString()).addOrReplaceChild(p.get("name").getAsString(),CubeListBuilder.create().texOffs(uv.get(0).getAsInt(),uv.get(1).getAsInt()).addBox(offset.get(0).getAsFloat(),offset.get(1).getAsFloat(),offset.get(2).getAsFloat(),size.get(0).getAsFloat(),size.get(1).getAsFloat(),size.get(2).getAsFloat()),PartPose.offsetAndRotation(pivot.get(0).getAsFloat(),pivot.get(1).getAsFloat(),pivot.get(2).getAsFloat(),rotation==null?0:rotation.get(0).getAsFloat(),rotation==null?0:rotation.get(1).getAsFloat(),rotation==null?0:rotation.get(2).getAsFloat()));definitions.put(p.get("name").getAsString(),def);
  }
  root=LayerDefinition.create(mesh,data.get("atlas").getAsInt(),data.get("atlas").getAsInt()).bakeRoot();collect(root,"root",data.getAsJsonArray("parts"));
 }
 private void collect(ModelPart part,String name,JsonArray defs){nodes.put(name,part);for(var element:defs){var p=element.getAsJsonObject();if(p.get("parent").getAsString().equals(name)){String n=p.get("name").getAsString();collect(part.getChild(n),n,defs);}}}
 @Override public ModelPart root(){return root;}
 public ModelPart part(String name){return nodes.get(name);}
 @Override public void setupAnim(IslandsMob e,float walk,float amount,float age,float yaw,float pitch){pose(e.kind,e.action(),e.actionTime(age-e.tickCount),e.second(),e.exposed(),walk,amount,age,yaw,pitch,e.hurtTime,e.deathTime);}
 public void pose(IslandsContent.Kind kind,Action action,float time,boolean second,boolean exposed,float walk,float amount,float age,float yaw,float pitch,int hurt,int death){
  root.getAllParts().forEach(ModelPart::resetPose);
  boolean boss=kind==IslandsContent.Kind.MAGMA_LORD;
  if(!boss){ordinaryPose(kind,action,time,walk,amount,age,yaw,pitch,hurt,death);return;}
  float charge=action.release==0?0:Math.min(1,time/action.release);
  float release=time<action.release?0:Math.max(0,1-(time-action.release)/Math.max(1,action.duration-action.release));
  for(var entry:nodes.entrySet()){
   String n=entry.getKey();var p=entry.getValue();
   if(n.equals("head")){p.yRot=yaw*.0174533F;p.xRot=pitch*.0174533F;}
   if(n.startsWith("leg")){int i=n.charAt(3)-'0';if(kind==IslandsContent.Kind.SLAG_CRAB)p.zRot=(float)Math.cos(walk*.8+(i%2)*Math.PI)*amount*.4F;else p.xRot=(float)Math.cos(walk*.7+(i%2)*Math.PI)*amount*.55F;}
   if(n.equals("tail"))p.yRot=(float)Math.sin(age*.08)*.15F+(float)Math.sin(walk*.6)*amount*.3F;
   if(boss&&(n.startsWith("rib")||n.startsWith("back_crest"))){float spread=second?1.8F:0;float side=n.contains("0")?-1:1;p.x+=side*spread;p.yRot+=side*(second?.13F:0);}
   if(boss&&n.startsWith("shin")){p.y+=(float)Math.sin(age*.035+n.hashCode())*1.2F;}
   if(n.startsWith("shard")){p.y+=(float)Math.sin(age*.045+n.hashCode())*.65F;p.yRot=(float)Math.sin(age*.03+n.hashCode())*.13F;}
   if(n.startsWith("shell")){float spread=exposed?1:second?.25F:0;if(action==Action.PHASE)spread=charge;p.x+=(n.endsWith("0")?-1:1)*spread*3.5F;p.yRot+=(n.endsWith("0")?-1:1)*spread*.35F;}
   if(n.equals("jaw"))p.xRot=action==Action.WARN?charge*.35F:action==Action.CLAWS&&!boss?charge*.4F:0;
   if(n.startsWith("arm")||n.startsWith("claw")||n.startsWith("backarm")){
    float side=n.endsWith("0")?-1:1;
    switch(action){
     case CLAWS->{p.yRot=side*(time<action.release?charge*.9F:-release*.9F);p.xRot=-charge*.6F;}
     case FRACTURE->{p.xRot=time<action.release?-charge*1.2F:release*.8F;}
     case PULL->{p.xRot=-charge*1.3F;p.zRot=side*(time<27?.55F:release*.15F);}
     case HANDS->{if(n.startsWith("backarm")){p.xRot=time<25?-charge*1.6F:-release*.9F;p.yRot=side*charge*.2F;}}
     case ERUPTION->{p.zRot=side*charge*.9F;p.xRot=-charge*.6F;if(time>=31)p.y+=(float)Math.sin((time-31)*Math.PI/8)*2;}
     case RING->{p.zRot=side*charge*1.25F;p.xRot=-charge*.4F;}
     case PHASE,WAKE->{p.zRot=side*charge*.8F;}
     case WARN->{p.zRot=side*charge*.25F;}
     default->{if(!boss)p.xRot=(float)Math.cos(walk*.7)*amount*.25F;}
    }
   }
  }
  if(boss&&nodes.containsKey("core")){var core=nodes.get("core");float pulse=1+(float)Math.sin(age*.1)*(exposed?.12F:.04F);core.xScale=core.yScale=core.zScale=pulse;}
  if(boss){root.y+=(float)Math.sin(age*.05)*.7F;if(action==Action.PHASE)root.zRot=(float)Math.sin(time*.7)*.025F;}
  if(hurt>0)root.zRot+=(float)Math.sin(hurt)*.045F;
  if(death>0){float fade=Math.min(1,death/(boss?80F:20F));for(var entry:nodes.entrySet())if(entry.getKey().startsWith("shard"))entry.getValue().y+=fade*6;root.y+=fade*5;root.xRot=fade*.4F;}
 }
 private void ordinaryPose(IslandsContent.Kind kind,Action action,float time,float walk,float amount,float age,float yaw,float pitch,int hurt,int death){
  float gesture=action==Action.IDLE?0:org.slavicmyths.client.MobMotion.envelope(time,action.release,action.duration);
  var body=nodes.get("body");var head=nodes.get("head");head.yRot=yaw*.0174533F;head.xRot=pitch*.0174533F;
  if(kind==IslandsContent.Kind.HEAT_SALAMANDER){
   for(int i=0;i<4;i++){float phase=walk*.8F+(i==0||i==3?0:(float)Math.PI);var step=org.slavicmyths.client.MobMotion.step(phase,.65F,5,2);var leg=nodes.get("leg"+i);leg.xRot=step.forward()*amount*.12F;leg.y-=step.lift()*amount;nodes.get("foot"+i).xRot=-leg.xRot;}
   body.yRot=(float)Math.sin(walk*.8)*amount*.045F;nodes.get("tail").yRot=(float)Math.sin(walk*.8-.65)*amount*.24F+(float)Math.sin(age*.045)*.08F;nodes.get("tail_tip").yRot=(float)Math.sin(walk*.8-1.4)*amount*.3F+(float)Math.sin(age*.045-.7)*.12F;nodes.get("jaw").xRot=gesture*.45F;head.xRot-=gesture*.18F;
  }else if(kind==IslandsContent.Kind.SLAG_CRAB){
   for(int i=0;i<6;i++){int side=i%2==0?-1:1;var step=org.slavicmyths.client.MobMotion.step(walk*.65F+(i%2+i/2%2)*(float)Math.PI,.7F,4,2);var leg=nodes.get("leg"+i);leg.zRot=side*step.forward()*amount*.09F;leg.y-=step.lift()*amount;nodes.get("toe"+i).zRot=-leg.zRot;}
   body.zRot=(float)Math.sin(walk*1.3)*amount*.025F;
   for(int i=0;i<2;i++){float side=i==0?-1:1;nodes.get("arm"+i).yRot=side*gesture*.5F;nodes.get("claw"+i).xRot=-gesture*.7F;nodes.get("pincer"+i).yRot=side*gesture*.65F;nodes.get("eyestalk"+i).xRot=(float)Math.sin(age*.04+i)*.06F;}
  }else{
   float shift=(float)Math.sin(walk*.4)*amount;body.zRot=shift*.065F;body.y+=Math.abs(shift)*.5F;body.xRot=gesture*.1F;
   for(int i=0;i<2;i++){float step=(i==0?1:-1)*shift;nodes.get("leg"+i).xRot=step*.28F;nodes.get("knee"+i).xRot=Math.max(0,-step)*.5F;nodes.get("foot"+i).xRot=-step*.15F;nodes.get("arm"+i).xRot=-step*.12F-gesture*(i==0?1.15F:.75F);nodes.get("fist"+i).xRot=-gesture*.3F;}
   var core=nodes.get("core");core.xScale=core.yScale=1+(float)Math.sin(age*.085)*.025F+gesture*.08F;
  }
  if(hurt>0){root.zRot=(float)Math.sin(hurt)*(kind==IslandsContent.Kind.VENT_GOLEM?.025F:.07F);head.xRot-=.08F;}
  if(death>0){float d=Math.min(1,death/20F);root.zRot=d*(kind==IslandsContent.Kind.SLAG_CRAB?1.3F:.45F);root.y+=d*4;if(kind==IslandsContent.Kind.HEAT_SALAMANDER)nodes.get("tail_tip").yRot+=d*.35F;}
 }

}
