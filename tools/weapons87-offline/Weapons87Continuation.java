package org.slavicmyths.verify;

import com.google.gson.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.block.model.ItemTransform;
import org.slavicmyths.client.*;
import org.joml.Vector3f;
import java.nio.file.*;
import java.util.*;

/** Offline real ModelPart/UV and production deformation; no Minecraft client/world/server. */
public final class Weapons87Continuation {
 static final Path OUT=Path.of(System.getProperty("slavicmyths.continuationOutput","docs/implementation/weapons87/continuation53"));
 static void check(boolean ok,String why){if(!ok)throw new AssertionError(why);}
 public static void main(String[] args)throws Exception{
  var gson=new GsonBuilder().registerTypeAdapter(ItemTransform.class,new ItemTransform.Deserializer()).create();
  var rows=new JsonArray();
  var representatives=JsonParser.parseString(Files.readString(OUT.resolve("representative-input.json"))).getAsJsonObject();
  for(boolean slim:new boolean[]{false,true})for(boolean left:new boolean[]{false,true})
   for(var shape:PlayerWeaponPose.Shape.values())for(float phase:new float[]{0,.20F,.45F,.72F,1})for(boolean after:new boolean[]{false,true}){
    var model=new PlayerModel<>(LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,slim),64,64).bakeRoot(),slim);
    model.young=false;model.rightArm.xRot=model.leftArm.xRot=-(float)Math.PI/10;
    var arm=left?model.leftArm:model.rightArm;
    if(after)PlayerWeaponPose.pose(arm,shape,left,phase,0,0,0);
    check(Float.isFinite(arm.xRot)&&Float.isFinite(arm.yRot)&&Float.isFinite(arm.zRot),"finite arm");
    if(phase==0||phase==1)check(Math.abs(arm.xRot+Math.PI/10)<1e-6&&arm.yRot==0&&arm.zRot==0,"idle/recovery reset");
    check((left?model.rightArm:model.leftArm).yRot==0,"unused arm");
    var capture=new TrophyHeadsClient.Capture();var pose=new PoseStack();
    for(var part:List.of(model.head,model.body,model.leftArm,model.rightArm,model.leftLeg,model.rightLeg))part.render(pose,capture,0,0);
    var faces=new JsonArray();for(int i=0;i<capture.vertices.size();i+=4){var face=new JsonArray();for(int j=0;j<4;j++){var vertex=new JsonArray();for(int k=0;k<5;k++)vertex.add(capture.vertices.get(i+j)[k]*(k<3?16:1));face.add(vertex);}faces.add(face);}
    var hand=new PoseStack();model.translateToHand(left?net.minecraft.world.entity.HumanoidArm.LEFT:net.minecraft.world.entity.HumanoidArm.RIGHT,hand);
    hand.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));hand.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));hand.translate((left?-1F:1F)/16,.125,-.625);
    var row=new JsonObject();row.addProperty("body",slim?"Alex":"Steve");row.addProperty("left",left);row.addProperty("shape",shape.name());row.addProperty("phase",phase);row.addProperty("after",after);row.addProperty("texture","minecraft:textures/entity/player/"+(slim?"slim/alex":"wide/steve")+".png");row.add("faces",faces);row.add("hand",gson.toJsonTree(hand.last().pose().get(new float[16])));
    var representative=representatives.getAsJsonObject(shape.name());
    var display=representative.getAsJsonObject(after&&representative.has("after_model")?"after_model":"model").getAsJsonObject("display");
    String context="thirdperson_"+(left?"left":"right")+"hand";
    var transform=gson.fromJson(display.has(context)?display.get(context):display.get("thirdperson_righthand"),ItemTransform.class);
    if(shape==PlayerWeaponPose.Shape.WHIP&&after){WhipClient.cancelDisplayOffset(hand,transform,left);transform.apply(left,hand);WhipClient.centerHandle(hand);}else transform.apply(left,hand);
    if(!after&&shape!=PlayerWeaponPose.Shape.WHIP){
     Class oldShape=Class.forName("org.slavicmyths.client.ThirdPersonWeaponMotion$Shape");
     var animate=ThirdPersonWeaponMotion.class.getDeclaredMethod("animate",PoseStack.class,oldShape,boolean.class,float.class,float.class,float.class,float.class);animate.setAccessible(true);
     animate.invoke(null,hand,Enum.valueOf(oldShape,shape.name()),left,phase,0F,0F,0F);
    }
    hand.translate(-.5,-.5,-.5);row.add("item_matrix",gson.toJsonTree(hand.last().pose().get(new float[16])));rows.add(row);
   }
  Files.writeString(OUT.resolve("player-source.json"),gson.toJson(rows));
  var whipModel=JsonParser.parseString(Files.readString(Path.of("src/main/resources/assets/slavicmyths/models/item/poloz_whip_pose_0.json"))).getAsJsonObject();
  for(boolean left:new boolean[]{false,true})for(String ctx:new String[]{"firstperson_righthand","thirdperson_righthand"}){
   var transform=gson.fromJson(whipModel.getAsJsonObject("display").get(ctx),ItemTransform.class);var pose=new PoseStack();
   WhipClient.cancelDisplayOffset(pose,transform,left);transform.apply(left,pose);WhipClient.centerHandle(pose);pose.translate(-.5,-.5,-.5);
   var grip=pose.last().pose().transformPosition(new Vector3f(.5F,.5F/16,.5F));check(grip.length()<1e-6,"actual whip palm "+ctx+left+grip);
  }
  var input=JsonParser.parseString(Files.readString(OUT.resolve("whip-input.json"))).getAsJsonArray();var output=new JsonArray();
  for(float phase:new float[]{0,.2F,.45F,.72F,1}){
   var faces=new JsonArray();for(var raw:input){var face=raw.getAsJsonObject().deepCopy();var vertices=face.getAsJsonArray("vertices");
    for(int i=0;i<vertices.size();i++){var v=vertices.get(i).getAsJsonArray();double x=v.get(0).getAsDouble(),y=v.get(1).getAsDouble(),z=v.get(2).getAsDouble();var p=WhipMotion.point(x,y,z,phase,0);
     check(Double.isFinite(p.x())&&Double.isFinite(p.y())&&Double.isFinite(p.z()),"finite lash");
     if(y<=5)check(x==p.x()&&y==p.y()&&z==p.z(),"fixed grip/collar");
     if(phase==1)check(p.equals(WhipMotion.point(x,y,z,0,0)),"lash recovery identity");
     var changed=new JsonArray();changed.add(p.x());changed.add(p.y());changed.add(p.z());vertices.set(i,changed);
    }faces.add(face);
   }var row=new JsonObject();row.addProperty("phase",phase);row.add("faces",faces);output.add(row);
  }
  check(input.size()==216,"36 actual whip cuboids");
  Files.writeString(OUT.resolve("whip-source.json"),gson.toJson(output));
  Files.writeString(OUT.resolve("pose-coverage.json"),gson.toJson(PlayerWeaponPose.coverage()));
  int grips=0;
  for(String material:List.of("stone","gold","iron","silver","diamond","perunite","netherite"))for(String family:List.of("throwing_knife","sulitsa")){
   var path=Path.of("src/main/resources/assets/slavicmyths/models/item/"+material+"_"+family+"_held.json");
   var display=JsonParser.parseString(Files.readString(path)).getAsJsonObject().getAsJsonObject("display");
   for(var entry:display.entrySet()){
    boolean left=entry.getKey().endsWith("lefthand"),fp=entry.getKey().startsWith("firstperson");
    var pose=new PoseStack();gson.fromJson(entry.getValue(),ItemTransform.class).apply(left,pose);pose.translate(-.5,-.5,-.5);
    var grip=pose.last().pose().transformPosition(new Vector3f(.5F,(family.equals("throwing_knife")?4.5F:3.5F)/16,.5F));
    var expected=fp?new Vector3f((left?-1F:1F)/16,-1F/16,-2F/16):new Vector3f();
    check(grip.distance(expected)<1e-6,"actual thrown palm "+path+entry.getKey()+grip);grips++;
   }
  }
  check(grips==56,"all thrown material/hand/view grips");
  System.out.println("PASS 56 actual native thrown ItemTransform grip matrices (14 items x 4 hand/view contexts)");
  System.out.println("PASS 360 actual player ModelPart/UV samples, both skins/hands, 9 archetypes/5 phases; 4 native grip matrices; actual 216-quad whip source/5 phases; fixed handle/finite/recovery. Runtime NOT_RUN.");
 }
}
