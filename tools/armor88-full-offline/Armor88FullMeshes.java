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
import net.minecraft.world.entity.EquipmentSlot;

/** Actual native PlayerModel + private garment factory. No client/entity/world construction. */
public final class Armor88FullMeshes {
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
 static HumanoidModel create(String id,EquipmentSlot slot,boolean slim)throws Exception {
  Class<?> type=null;
  if(List.of("linen_headscarf","linen_shirt","linen_ports","bast_shoes","sarafan","linen_apron","folk_vest").contains(id)){
   Method m=TextileClothingLayer.class.getDeclaredMethod("mesh",String.class,boolean.class);m.setAccessible(true);return (HumanoidModel)m.invoke(null,id,slim);
  }
  if(id.startsWith("perunite_"))type=PeruniteArmor.class;
  if(id.startsWith("dragon_"))type=DragonArmor.class;
  if(id.startsWith("poloz_"))type=PolozArmor.class;
  if(List.of("bogatyr_chestplate","clear_sight_helmet","indomitability_leggings","wind_boots").contains(id))type=UniqueArmorModels.class;
  if(type!=null){Method m=type.getDeclaredMethod("create",EquipmentSlot.class);m.setAccessible(true);return (HumanoidModel)m.invoke(null,slot);}
  if(id.startsWith("trophy_")){int kind=List.of("trophy_idol_chest","trophy_marinka_cloak","trophy_kartaus_mantle").indexOf(id);return (HumanoidModel)field(new RewardArmor(kind),"model");}
  try {Class<?> c=Class.forName("org.slavicmyths.client.BasicArmorModels");Method m=c.getDeclaredMethod("create",String.class,boolean.class);m.setAccessible(true);return (HumanoidModel)m.invoke(null,id,slim);}
  catch(ClassNotFoundException e){return new HumanoidModel(LayerDefinition.create(HumanoidModel.createMesh(new CubeDeformation(1),0),64,32).bakeRoot());}
 }
 static String texture(String id){
  if(List.of("linen_headscarf","linen_shirt","linen_ports","bast_shoes","sarafan","linen_apron","folk_vest").contains(id))return "textures/entity/clothing/"+id+".png";
  if(id.startsWith("trophy_"))return "textures/models/armor/"+id+".png";
  if(id.startsWith("dragon_")||id.startsWith("poloz_"))return "textures/models/armor/"+id.split("_")[0]+".png";
  if(id.startsWith("perunite_"))return "textures/models/armor/perunite_layer_"+(id.endsWith("leggings")?2:1)+".png";
  return switch(id){case "gambeson"->"textures/models/armor/gambeson_layer_1.png";case "plate_cuirass"->"textures/models/armor/plate_layer_1.png";case "seven_league_boots"->"textures/armor/seven_league.png";default->"textures/armor/unique_"+switch(id){case "bogatyr_chestplate"->"bogatyr";case "clear_sight_helmet"->"clear_sight";case "indomitability_leggings"->"indomitability";default->"wind";}+"_layer_"+(id.endsWith("leggings")?2:1)+".png";};
 }
 public static void main(String[] args)throws Exception{
  String[] ids={"linen_headscarf","linen_shirt","linen_ports","bast_shoes","sarafan","linen_apron","folk_vest","gambeson","plate_cuirass","bogatyr_chestplate","clear_sight_helmet","indomitability_leggings","wind_boots","seven_league_boots","perunite_helmet","perunite_chestplate","perunite_leggings","perunite_boots","dragon_helmet","dragon_chestplate","dragon_leggings","dragon_boots","poloz_helmet","poloz_chestplate","poloz_leggings","poloz_boots","trophy_idol_chest","trophy_marinka_cloak","trophy_kartaus_mantle"};
  JsonArray output=new JsonArray();
  for(String id:ids)for(boolean slim:new boolean[]{false,true})for(String pose:List.of("idle","walk_t10","weapon_hold","sneak")){
   EquipmentSlot slot=id.equals("linen_headscarf")||id.endsWith("helmet")?EquipmentSlot.HEAD:id.equals("linen_ports")||id.endsWith("leggings")?EquipmentSlot.LEGS:id.equals("bast_shoes")||id.endsWith("boots")?EquipmentSlot.FEET:EquipmentSlot.CHEST;
   ModelPart root=LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE,slim),64,64).bakeRoot();PlayerModel player=new PlayerModel(root,slim);player.young=false;
   if(pose.equals("walk_t10")){float phase=10*.6662F,amount=.7F;player.rightArm.xRot=(float)Math.cos(phase+Math.PI)*amount;player.leftArm.xRot=(float)Math.cos(phase)*amount;player.rightLeg.xRot=(float)Math.cos(phase)*1.4F*amount;player.leftLeg.xRot=(float)Math.cos(phase+Math.PI)*1.4F*amount;}
   if(pose.equals("weapon_hold"))player.rightArm.xRot=-1.2F;
   if(pose.equals("sneak")){player.body.xRot=.5F;player.rightArm.xRot=.4F;player.leftArm.xRot=.4F;player.rightLeg.z=4;player.leftLeg.z=4;player.rightLeg.y=12.2F;player.leftLeg.y=12.2F;player.head.y=4.2F;player.body.y=3.2F;player.leftArm.y=5.2F;player.rightArm.y=5.2F;}
   player.hat.copyFrom(player.head);player.leftSleeve.copyFrom(player.leftArm);player.rightSleeve.copyFrom(player.rightArm);player.leftPants.copyFrom(player.leftLeg);player.rightPants.copyFrom(player.rightLeg);player.jacket.copyFrom(player.body);
   HumanoidModel m=create(id,slot,slim);player.copyPropertiesTo(m);
   if(id.startsWith("trophy_")&&!id.equals("trophy_idol_chest"))for(String side:List.of("tail_left","tail_right")){float t=pose.equals("walk_t10")?10:0,amount=pose.equals("walk_t10")?.7F:0;boolean before=args[0].contains("before-native");m.body.getChild(side).xRot=(float)((before?.06:.18)+Math.sin(t*.65+(!before&&id.equals("trophy_kartaus_mantle")||side.endsWith("left")?0:1.2))*(before?.13:.08)*amount);}
   // Match the native armor layer visibility; clothing meshes include only their authored bones.
   List<ModelPart> parts=switch(slot){case HEAD->List.of(m.head,m.hat);case CHEST->List.of(m.body,m.leftArm,m.rightArm);case LEGS->List.of(m.body,m.leftLeg,m.rightLeg);default->List.of(m.leftLeg,m.rightLeg);};
   if(List.of("sarafan","linen_apron").contains(id))parts=List.of(m.body,m.leftArm,m.rightArm,m.leftLeg,m.rightLeg);
   JsonObject sample=new JsonObject();sample.addProperty("id",id);sample.addProperty("slot",slot.name());sample.addProperty("body",slim?"Alex":"Steve");sample.addProperty("pose",pose);JsonArray layers=new JsonArray();
   layers.add(entry("player_skin","minecraft:textures/entity/player/"+(slim?"slim/alex":"wide/steve")+".png",List.of(player.head,player.hat,player.body,player.leftArm,player.rightArm,player.leftLeg,player.rightLeg,player.jacket,player.leftSleeve,player.rightSleeve,player.leftPants,player.rightPants)));
   layers.add(entry(id,texture(id),parts));sample.add("layers",layers);output.add(sample);
  }
  Path p=Path.of(args[0]);Files.createDirectories(p.getParent());Files.writeString(p,new GsonBuilder().setPrettyPrinting().create().toJson(output));System.out.println("ARMOR88_FULL_NATIVE_PASS 29 IDs x 2 bodies x 4 source poses, UV bounded; game launches0");
 }
}
