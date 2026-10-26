package org.slavicmyths.verify;
import java.nio.file.*;
import com.google.gson.*;
import org.slavicmyths.equipment.UniqueEquipmentRules;
public final class Armor287Offline {
 private static float formula(float raw,float armor,float toughness){float effective=Math.min(20,Math.max(armor*.2F,armor-raw/(2+toughness/4)));return raw*(1-effective/25);}
 public static void main(String[] args)throws Exception{
  int checks=0;var curves=new JsonArray();
  for(var entry:JsonParser.parseString(Files.readString(Path.of("docs/implementation/armor287-20261026/comparison.json"))).getAsJsonArray()){
   var row=entry.getAsJsonObject();float armor=row.get("generic.armor").getAsFloat(),tough=row.get("generic.armor_toughness").getAsFloat();float multiplier=row.get("family").getAsString().equals("poloz")?.78F:1;
   float damage=formula(10,armor,tough)*multiplier;
   if(Math.abs(damage-row.get("raw10_damage").getAsFloat())>.0001)throw new AssertionError("native server/offline mismatch "+row);checks++;
   var values=new JsonObject();values.addProperty("family",row.get("family").getAsString());for(int raw:new int[]{1,4,10,20,40}){float result=formula(raw,armor,tough)*multiplier;if(result<0||result>raw)throw new AssertionError("damage bounds");checks++;values.addProperty("raw"+raw,result);}curves.add(values);
  }
  if(!UniqueEquipmentRules.stance(7,20)||UniqueEquipmentRules.stance(7.01F,20)||UniqueEquipmentRules.controlDuration(100)!=65||UniqueEquipmentRules.controlDuration(-1)!=-1)throw new AssertionError("stance/control boundaries");checks++;
  for(int worn:new int[]{1,17,100,200}){int result=UniqueEquipmentRules.upgradedDamage(worn,250,640);if((double)result/640<(double)worn/250)throw new AssertionError("free repair");checks++;}
  var out=new JsonObject();out.addProperty("checks",checks);out.addProperty("game_launches",0);out.addProperty("scope","offline armor formula calibrated against separate real server measurements; existing UniqueEquipmentRules");out.add("curves",curves);Files.writeString(Path.of("docs/implementation/armor287-20261026/offline.json"),new GsonBuilder().setPrettyPrinting().create().toJson(out));System.out.println("ARMOR287_OFFLINE PASS checks="+checks+"; no server/world/player");
 }
}
