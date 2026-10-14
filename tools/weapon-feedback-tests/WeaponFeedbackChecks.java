package org.slavicmyths.gorynych;
import org.slavicmyths.client.NightSwingPose;
import org.slavicmyths.poloz.PolozRules;
import org.slavicmyths.gorynych.SerpentCrossbow;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.CompoundTag;
public final class WeaponFeedbackChecks {
 private static int checks;
 private static void require(boolean v,String label){checks++;if(!v)throw new AssertionError(label);}
 public static void main(String[] args){
  net.neoforged.fml.loading.LoadingModList.of(java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.List.of(),java.util.Map.of());net.minecraft.SharedConstants.tryDetectVersion();net.minecraft.server.Bootstrap.bootStrap();
  for(int phase=1;phase<=3;phase++)for(int part=-1;part<24;part++)for(boolean scar:new boolean[]{false,true})require(PolozRules.multiplier(phase,part,scar)==1,"all parts equally vulnerable");
  for(int mode=0;mode<3;mode++)for(int combo=0;combo<3;combo++){
   int duration=mode==0?(combo==2?20:combo==1?15:14):26;int hit=mode==0?(combo==2?7:5):10;
   require(NightSwingPose.motion(0,mode,combo,duration)==0,"rest starts at zero");
   require(NightSwingPose.motion(hit,mode,combo,duration)==0,"server hit coincides with visual arc midpoint");
   require(NightSwingPose.motion(hit-2,mode,combo,duration)==-1,"windup reaches one edge");
   require(NightSwingPose.motion(hit+2,mode,combo,duration)==1,"blade reaches opposite edge");
   require(NightSwingPose.motion(duration,mode,combo,duration)==0,"restored pose");
   require(NightSwingPose.horizontal(hit,mode,combo,duration)==1&&NightSwingPose.horizontal(hit+2,mode,combo,duration)==1,"shaft remains horizontal through full sweep");
   require(NightSwingPose.horizontal(0,mode,combo,duration)==0&&NightSwingPose.horizontal(duration,mode,combo,duration)==0,"horizontal transition recovers idle");
   float last=0;for(int i=0;i<=duration*100;i++){float x=NightSwingPose.motion(i/100f,mode,combo,duration);require(Float.isFinite(x)&&Math.abs(x)<=1,"bounded finite motion");require(Math.abs(x-last)<.006,"no pose discontinuity");last=x;}
  }
  var stack=new ItemStack(Items.CROSSBOW);var root=new CompoundTag();root.putString("Unrelated","keep");var state=new CompoundTag();state.putInt("Mode",2);state.putBoolean("Burst",true);state.putInt("Ammo0",3);root.put("Gorynych196",state);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(root));
  var copy=SerpentCrossbow.state(stack);copy.putInt("Mode",0);require(SerpentCrossbow.state(stack).getInt("Mode")==2,"state reading is a deep copy");
  SerpentCrossbow.write(stack,copy);require(SerpentCrossbow.state(stack).getInt("Mode")==0,"actual mode write");require(stack.get(DataComponents.CUSTOM_DATA).copyTag().getString("Unrelated").equals("keep"),"mode write preserves unrelated data");
  var cloned=stack.copy();require(SerpentCrossbow.state(cloned).getBoolean("Burst"),"volley survives stack copy");require(cloned.get(DataComponents.CUSTOM_DATA).copyTag().getString("Unrelated").equals("keep"),"unrelated data retained");

  for(String name:java.util.List.of("restored_carolingian_sword_held","ancient_carolingian_sword_held","likho_staff","chekan_held","ancient_chekan_held","victor_spear","restored_spear_held","ancient_spear_held","nightingale_dagger","ataman_flail","trophy_kalin_sabre","karachun_scythe_held")){
   try{
    var json=com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(java.nio.file.Path.of("src/main/resources/assets/slavicmyths/models/item/"+name+".json"))).getAsJsonObject();
    for(String side:java.util.List.of("righthand","lefthand")){
     var transform=json.getAsJsonObject("display").getAsJsonObject("thirdperson_"+side);var rotation=transform.getAsJsonArray("rotation");var matrix=new org.joml.Matrix4f().scale(-1,-1,1).rotateX(-(float)Math.PI/2).rotateY((float)Math.PI).rotateXYZ(rotation.get(0).getAsFloat()*(float)Math.PI/180,rotation.get(1).getAsFloat()*(float)Math.PI/180,rotation.get(2).getAsFloat()*(float)Math.PI/180);
     var direction=matrix.transformDirection(new org.joml.Vector3f(0,1,0));require(direction.y>.99&&Math.abs(direction.x)<.001&&Math.abs(direction.z)<.001,"blade points upward in native player coordinates: "+name+side);
    }
   }catch(java.io.IOException ex){throw new RuntimeException(ex);}
  }
  System.out.println("WEAPON_FEEDBACK_OFFLINE_PASS checks="+checks+"; no Minecraft launches; gameplay and visual QA not run");
 }
}
