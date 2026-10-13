package org.slavicmyths.client;
import org.slavicmyths.poloz.*;
import net.minecraft.client.model.geom.ModelPart;
import java.util.*;
public final class PolozChecks {
 static int checks;
 static void check(boolean ok,String label){checks++;if(!ok)throw new AssertionError(label);}
 static void model(ModelPart p)throws Exception{var cf=ModelPart.class.getDeclaredField("cubes");cf.setAccessible(true);var hf=ModelPart.class.getDeclaredField("children");hf.setAccessible(true);var pf=ModelPart.Cube.class.getDeclaredField("polygons");pf.setAccessible(true);for(var cube:(List<?>)cf.get(p))for(var poly:(Object[])pf.get(cube)){var vf=poly.getClass().getDeclaredField("vertices");vf.setAccessible(true);for(var v:(Object[])vf.get(poly)){var uf=v.getClass().getDeclaredField("u");var vf2=v.getClass().getDeclaredField("v");uf.setAccessible(true);vf2.setAccessible(true);float u=uf.getFloat(v),vv=vf2.getFloat(v);check(Float.isFinite(u)&&Float.isFinite(vv)&&u>=0&&u<=1&&vv>=0&&vv<=1,"native UV bounds u="+u+" v="+vv);}}for(var child:((Map<?,?>)hf.get(p)).values())model((ModelPart)child);}
 public static void main(String[] args)throws Exception{
  check(PolozRules.phase(2100)==2&&PolozRules.phase(2100.1f)==1,"inclusive70%");check(PolozRules.phase(900)==3&&PolozRules.phase(900.1f)==2,"inclusive30%");
  for(int i=-1;i<24;i++)for(int phase=1;phase<=3;phase++){float m=PolozRules.multiplier(phase,i,false);check(phase!=1||m==(i<0?1:i>=22?.8f:0),"phase1 native body policy");check(phase==1||m>0,"entire body phase2+");}
  for(int phase=1;phase<=3;phase++)for(int i:new int[]{5,12,18})check(PolozRules.multiplier(phase,i,true)>0,"scars vulnerable");
  check(PolozRules.offset(23)+.5+3.375>=38&&PolozRules.offset(23)+.5+3.375<=42,"total silhouette length");
  for(int i=0;i<24;i++)check(PolozRules.width(i)>.8&&PolozRules.width(i)<=4,"body dimensions");
  model(PolozRenderer.head());model(PolozRenderer.body());model(PolozRenderer.scar());model(PolozRenderer.glob());
  var create=PolozArmor.class.getDeclaredMethod("create",net.minecraft.world.entity.EquipmentSlot.class);create.setAccessible(true);for(var slot:new net.minecraft.world.entity.EquipmentSlot[]{net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET}){var armor=(net.minecraft.client.model.HumanoidModel<?>)create.invoke(null,slot);for(var part:List.of(armor.head,armor.body,armor.leftArm,armor.rightArm,armor.leftLeg,armor.rightLeg))model(part);}
  System.out.println("POLOZ_OFFLINE_PASS checks="+checks+" nativeModels=8; no game launch");
 }
}
