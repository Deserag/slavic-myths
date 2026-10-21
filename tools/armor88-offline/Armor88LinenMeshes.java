package org.slavicmyths.client;

import com.google.gson.*;
import java.nio.file.*;
import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/** Actual native PlayerModel + private garment factory. No client/entity/world construction. */
public final class Armor88LinenMeshes {
 static Object field(Object o,String name)throws Exception {Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
 static void visit(ModelPart part,Matrix4f parent,JsonArray faces)throws Exception {
  if(!part.visible)return;
  Matrix4f matrix=new Matrix4f(parent).translate(part.x,part.y,part.z).rotateZYX(part.zRot,part.yRot,part.xRot).scale(part.xScale,part.yScale,part.zScale);
  for(Object cube:(List<?>)field(part,"cubes"))for(Object polygon:(Object[])field(cube,"polygons")){
   JsonArray face=new JsonArray();
   for(Object vertex:(Object[])field(polygon,"vertices")){
    Vector3f pos=matrix.transformPosition(new Vector3f((Vector3f)field(vertex,"pos")));float u=(float)field(vertex,"u"),v=(float)field(vertex,"v");
    if(!pos.isFinite()||u<0||u>1||v<0||v>1)throw new AssertionError("Invalid native UV/vertex");
    JsonArray out=new JsonArray();for(float x:new float[]{pos.x,pos.y,pos.z,u,v})out.add(x);face.add(out);
   }faces.add(face);
  }
  for(Object child:((Map<?,?>)field(part,"children")).values())visit((ModelPart)child,matrix,faces);
 }
 static JsonObject entry(String id,String texture,List<ModelPart> parts)throws Exception {
  JsonArray faces=new JsonArray();for(ModelPart p:parts)visit(p,new Matrix4f(),faces);
  JsonObject out=new JsonObject();out.addProperty("id",id);out.addProperty("texture",texture);out.add("faces",faces);return out;
 }
 @SuppressWarnings({"rawtypes","unchecked"})
 public static void main(String[] args)throws Exception {
  Method factory=TextileClothingLayer.class.getDeclaredMethod("mesh",String.class,boolean.class);factory.setAccessible(true);
  JsonArray entries=new JsonArray();JsonArray bounds=new JsonArray();
  for(boolean slim:new boolean[]{false,true})for(String pose:List.of("idle","walk_t10","weapon_hold")){
   ModelPart root=LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,slim),64,64).bakeRoot();PlayerModel player=new PlayerModel(root,slim);player.young=false;
   if(pose.equals("walk_t10")){
    float phase=10*.6662F,amount=.7F;player.rightArm.xRot=(float)Math.cos(phase+Math.PI)*amount;player.leftArm.xRot=(float)Math.cos(phase)*amount;player.rightLeg.xRot=(float)Math.cos(phase)*1.4F*amount;player.leftLeg.xRot=(float)Math.cos(phase+Math.PI)*1.4F*amount;
   }
   if(pose.equals("weapon_hold"))player.rightArm.xRot=-(float)Math.PI/10;
   player.leftSleeve.copyFrom(player.leftArm);player.rightSleeve.copyFrom(player.rightArm);player.leftPants.copyFrom(player.leftLeg);player.rightPants.copyFrom(player.rightLeg);player.jacket.copyFrom(player.body);
   JsonObject sample=new JsonObject();sample.addProperty("body",slim?"Alex":"Steve");sample.addProperty("pose",pose);JsonArray layers=new JsonArray();
   List<ModelPart> skin=List.of(player.head,player.hat,player.body,player.leftArm,player.rightArm,player.leftLeg,player.rightLeg,player.jacket,player.leftSleeve,player.rightSleeve,player.leftPants,player.rightPants);
   layers.add(entry("player_skin","minecraft:textures/entity/player/"+(slim?"slim/alex":"wide/steve")+".png",skin));
   for(String id:List.of("linen_shirt","linen_ports")){
    HumanoidModel garment=(HumanoidModel)factory.invoke(null,id,slim);player.copyPropertiesTo(garment);
    // Same copyPropertiesTo as TextileClothingLayer.show, including Alex's native arm pivots.
    if(garment.leftArm.y!=player.leftArm.y||garment.rightArm.y!=player.rightArm.y)throw new AssertionError("Arm anchor changed");
    layers.add(entry(id,"textures/entity/clothing/"+id+".png",List.of(garment.head,garment.hat,garment.body,garment.leftArm,garment.rightArm,garment.leftLeg,garment.rightLeg)));
    if(pose.equals("idle")){
     for(var pair:List.of(Map.entry("body",garment.body),Map.entry("left_arm",garment.leftArm),Map.entry("right_arm",garment.rightArm),Map.entry("left_leg",garment.leftLeg),Map.entry("right_leg",garment.rightLeg))){
      int segment=0;for(Object cube:(List<?>)field(pair.getValue(),"cubes")){
       float[] limits=new float[]{Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.POSITIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY,Float.NEGATIVE_INFINITY};
       for(Object poly:(Object[])field(cube,"polygons"))for(Object vertex:(Object[])field(poly,"vertices")){
        Vector3f v=(Vector3f)field(vertex,"pos");float[] xyz={v.x,v.y,v.z};for(int a=0;a<3;a++){limits[a]=Math.min(limits[a],xyz[a]);limits[a+3]=Math.max(limits[a+3],xyz[a]);}
       }
       JsonObject row=new JsonObject();row.addProperty("body",slim?"Alex":"Steve");row.addProperty("id",id);row.addProperty("part",pair.getKey());row.addProperty("segment",segment++);row.add("bounds",new Gson().toJsonTree(limits));bounds.add(row);
      }
     }
    }
   }
   com.mojang.blaze3d.vertex.PoseStack hand=new com.mojang.blaze3d.vertex.PoseStack();
   player.translateToHand(net.minecraft.world.entity.HumanoidArm.RIGHT,hand);
   hand.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-90));hand.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180));hand.translate(1F/16,.125F,-.625F);
   // Unchanged vanilla handheld thirdperson_righthand display, then ItemRenderer's centering.
   hand.translate(0,4F/16,.5F/16);hand.mulPose(new org.joml.Quaternionf().rotationXYZ(0,-(float)Math.PI/2,(float)Math.toRadians(55)));hand.scale(.85F,.85F,.85F);hand.translate(-.5F,-.5F,-.5F);
   sample.add("right_hand_matrix",new Gson().toJsonTree(hand.last().pose().get(new float[16])));
   sample.add("layers",layers);entries.add(sample);
  }
  JsonArray controls=new JsonArray();
  for(boolean slim:new boolean[]{false,true})for(String id:List.of("sarafan","linen_headscarf","linen_apron","folk_vest","bast_shoes","woven_belt")){
   HumanoidModel m=(HumanoidModel)factory.invoke(null,id,slim);
   JsonObject control=entry(id,"unchanged source mesh",List.of(m.head,m.hat,m.body,m.leftArm,m.rightArm,m.leftLeg,m.rightLeg));control.addProperty("slim",slim);controls.add(control);
  }
  JsonObject out=new JsonObject();out.addProperty("scope","native source poses, NOT Minecraft gameplay");out.add("samples",entries);out.add("bounds",bounds);out.add("controls",controls);
  Path path=Path.of(args[0]);Files.createDirectories(path.getParent());Files.writeString(path,new GsonBuilder().setPrettyPrinting().create().toJson(out));
  System.out.println("ARMOR88_NATIVE_PASS six source poses, actual PlayerModel/garment bake, native UV, copied pivots; game launches0");
 }
}
