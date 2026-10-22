package org.slavicmyths.client;
import java.nio.file.*;
import java.util.*;
import java.lang.reflect.*;
import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;
import org.slavicmyths.accessories.AccessoryRules;
import org.joml.Matrix4f;

/** Native mesh/UV and shared production policies only. No entity, world or game creation. */
public final class Accessories148Offline {
 static int checks;
 static void check(boolean b,String label){checks++;if(!b)throw new AssertionError(label);}
 static Object field(Class<?> type,Object owner,String name)throws Exception{var f=type.getDeclaredField(name);f.setAccessible(true);return f.get(owner);}
 static void rules() throws Exception {
  check(AccessoryRules.bathDuration(100,true,false)==80,"bath -20%");
  var savedNbt=new net.minecraft.nbt.CompoundTag();savedNbt.putLong("spark",1160);var restored=net.minecraft.nbt.TagParser.parseTag(savedNbt.toString());check(!AccessoryRules.ready(1159,restored.getLong("spark")),"real NBT cooldown serialization; not a relog test");
  for(int duration:new int[]{-1,0,1})check(AccessoryRules.bathDuration(duration,true,false)==duration,"unbounded/nonfinite protected "+duration);
  check(AccessoryRules.bathDuration(72001,true,false)==57600,"long but finite effect still reduced");
  check(AccessoryRules.bathDuration(100,true,true)==100,"story/boss excluded");check(AccessoryRules.bathDuration(100,false,false)==100,"other effects excluded");
  check(AccessoryRules.fall(10,12,true)==3,"threshold 12, -70%");check(AccessoryRules.fall(10,11.99F,true)==10,"below 12");check(AccessoryRules.fall(0,20,true)==0,"no damage no proc");check(AccessoryRules.fall(10,20,false)==10,"cooldown");
  check(Math.abs(AccessoryRules.fall(10*.75F,12,true)-2.25F)<.0001,"stone path composes multiplicatively");
  check(AccessoryRules.forest(6,true,true),"6 HP forest hostile");check(!AccessoryRules.forest(5.99F,true,true),"below 6");check(!AccessoryRules.forest(6,false,true),"outside forest");check(!AccessoryRules.forest(6,true,false),"friendly/self/environment");
  for(boolean manual:new boolean[]{false,true})for(boolean mature:new boolean[]{false,true})for(boolean edible:new boolean[]{false,true})for(boolean rune:new boolean[]{false,true})
   check(AccessoryRules.harvest(manual,mature,edible,rune,0)==(manual&&mature&&edible&&!rune),"harvest eligibility truth table");
  check(AccessoryRules.harvest(true,true,true,false,.09999F),"10% below boundary");check(!AccessoryRules.harvest(true,true,true,false,.1F),"strict 10% chance");
  for(boolean melee:new boolean[]{false,true})for(boolean hostile:new boolean[]{false,true})for(boolean immune:new boolean[]{false,true})check(AccessoryRules.spark(melee,hostile,immune,0)==(melee&&hostile&&!immune),"spark truth table");
  check(AccessoryRules.spark(true,true,false,.24999F),"25% boundary below");check(!AccessoryRules.spark(true,true,false,.25F),"strict 25%");
  for(int cd:new int[]{AccessoryRules.BATH_CD,AccessoryRules.HARVEST_CD,AccessoryRules.PODVEY_CD,AccessoryRules.LESHY_CD,AccessoryRules.SPARK_CD}){
   long saved=1000+cd;check(!AccessoryRules.ready(saved-1,saved),"persisted cooldown before expiry");check(AccessoryRules.ready(saved,saved),"cooldown exact expiry");
  }
 }
 static HumanoidModel<?> player(boolean slim,String pose){
  var p=new PlayerModel<>(LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,slim),64,64).bakeRoot(),slim);p.young=false;
  if(pose.equals("walk_t10")){float phase=10*.6662F,amount=.7F;p.rightArm.xRot=(float)Math.cos(phase+Math.PI)*amount;p.leftArm.xRot=(float)Math.cos(phase)*amount;p.rightLeg.xRot=(float)Math.cos(phase)*1.4F*amount;p.leftLeg.xRot=(float)Math.cos(phase+Math.PI)*1.4F*amount;}
  if(pose.equals("held_item"))p.rightArm.xRot=-1.2F;
  if(pose.equals("sneak")){p.body.xRot=.5F;p.rightArm.xRot=.4F;p.leftArm.xRot=.4F;p.rightLeg.z=4;p.leftLeg.z=4;p.rightLeg.y=12.2F;p.leftLeg.y=12.2F;p.head.y=4.2F;p.body.y=3.2F;p.leftArm.y=5.2F;p.rightArm.y=5.2F;}
  p.hat.copyFrom(p.head);p.leftSleeve.copyFrom(p.leftArm);p.rightSleeve.copyFrom(p.rightArm);p.leftPants.copyFrom(p.leftLeg);p.rightPants.copyFrom(p.rightLeg);p.jacket.copyFrom(p.body);return p;
 }
 static JsonObject capture(String id,String tex,ModelPart part,boolean fit){
  var pose=new PoseStack();if(id.equals("miners_goggles"))AccessoryBodyFit.applyHead(pose,fit);else AccessoryBodyFit.apply(pose,fit);var out=new TrophyHeadsClient.Capture();part.render(pose,out,0,0);
  var faces=new JsonArray();for(int i=0;i<out.vertices.size();i+=4){var face=new JsonArray();for(int j=0;j<4;j++){var a=new JsonArray();var v=out.vertices.get(i+j);for(int k=0;k<5;k++){float value=v[k]*(k<3?16:1);check(Float.isFinite(value),"finite native vertex");if(k>=3)check(value>=0&&value<=1,"native UV range "+id);a.add(value);}face.add(a);}faces.add(face);}
  var result=new JsonObject();result.addProperty("id",id);result.addProperty("texture",tex);result.add("faces",faces);return result;
 }
 @SuppressWarnings({"rawtypes","unchecked"}) public static void main(String[] args)throws Exception{
  rules();var samples=new JsonArray();
  for(boolean slim:new boolean[]{false,true})for(String pose:List.of("idle","walk_t10","held_item","sneak"))for(boolean armor:new boolean[]{false,true})for(boolean after:new boolean[]{false,true}){
   var p=(PlayerModel)player(slim,pose);var layers=new JsonArray();
   layers.add(Armor88FullMeshes.entry("skin","minecraft:textures/entity/player/"+(slim?"slim/alex":"wide/steve")+".png",List.of(p.head,p.hat,p.body,p.leftArm,p.rightArm,p.leftLeg,p.rightLeg,p.jacket,p.leftSleeve,p.rightSleeve,p.leftPants,p.rightPants)));
   if(armor){var shell=new HumanoidModel(LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(1),0),64,32).bakeRoot());p.copyPropertiesTo(shell);layers.add(Armor88FullMeshes.entry("chest_armor","minecraft:textures/models/armor/iron_layer_1.png",List.of(shell.head,shell.hat,shell.body,shell.leftArm,shell.rightArm)));}
   var artifacts=new JsonObject();
   for(String name:List.of("poyas_tugarina","odnoglazyy_obereg","volchiy_poyas","zolny_obereg","obereg_padayuschey_zvezdy","finist_quiver","miners_goggles","podvey_belt","leshy_necklace")){
    ModelPart part;String texture;boolean body=true,fit=false;
    if(name.equals("miners_goggles")){part=(ModelPart)field(MiningGogglesRenderer.class,new MiningGogglesRenderer(),"model");texture="textures/models/miners_goggles.png";body=false;}
    else{
     HumanoidModel m;
     if(name.equals("poyas_tugarina")||name.equals("odnoglazyy_obereg")){m=(HumanoidModel)field(BossAccessoryRenderer.class,null,name.equals("poyas_tugarina")?"BELT":"EYE");texture="textures/entity/"+(name.equals("poyas_tugarina")?"tugarin_zmey":"likho_one_eyed")+".png";fit=after&&armor;}
     else if(name.equals("finist_quiver")){m=FinistQuiverRenderer.model();texture="textures/armor/finist_quiver_worn.png";}
     else if(name.equals("podvey_belt")||name.equals("leshy_necklace")){m=NewAccessoryRenderer.model(name.equals("podvey_belt"));texture="textures/entity/accessories148.png";fit=armor;}
     else{m=(HumanoidModel)field(HuntAccessoryRenderer.class,null,name.equals("volchiy_poyas")?"BELT":name.equals("zolny_obereg")?"CHARM":"STAR");texture="textures/entity/"+(name.equals("obereg_padayuschey_zvezdy")?"hunt_star_accessory":"hunt_accessories")+".png";fit=after&&armor;}
     p.copyPropertiesTo(m);part=m.body;
    }
    var stack=new PoseStack();if(!body)p.head.translateAndRotate(stack);
    JsonObject item;
    if(!body){var raw=new TrophyHeadsClient.Capture();part.render(stack,raw,0,0);var copy=part.storePose();part.loadPose(p.head.storePose());item=capture(name,texture,part,after&&armor);part.loadPose(copy);}
    else item=capture(name,texture,part,fit);
    artifacts.add(name,item);
   }
   var sample=new JsonObject();sample.addProperty("body",slim?"Alex":"Steve");sample.addProperty("pose",pose);sample.addProperty("armor",armor);sample.addProperty("after",after);sample.add("base_layers",layers);sample.add("artifacts",artifacts);
   var anchors=new JsonObject();for(String bone:List.of("body","head")){var part=bone.equals("body")?p.body:p.head;var a=new JsonArray();for(float f:new float[]{part.x,part.y,part.z,part.xRot,part.yRot,part.zRot})a.add(f);anchors.add(bone,a);}sample.add("anchors",anchors);samples.add(sample);
  }
  var result=new JsonObject();result.addProperty("checks",checks);result.addProperty("game_launches",0);result.addProperty("limitations","Fixed source poses, no animation engine/world/player entity; held_item is arm pose only; runtime owner pending");result.add("samples",samples);
  var output=Path.of("docs/implementation/accessories-148/native-worn.json");Files.writeString(output,new GsonBuilder().setPrettyPrinting().create().toJson(result));
  System.out.println("ACCESSORIES148_OFFLINE PASS: shared production rules, 9 native ModelPart accessories, Steve/Alex x4 source poses x2 armor states x A/B; checks="+checks+"; game launches0");
 }
}
