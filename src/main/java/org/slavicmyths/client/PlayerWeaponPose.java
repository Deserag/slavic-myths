package org.slavicmyths.client;

import java.util.Map;
import java.util.HashMap;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;

/** Finite vanilla-player shoulder poses, after setupAnim; no entity AI or server timing. */
public final class PlayerWeaponPose {
 public enum Shape { BLADE,HEAVY,SPEAR,POLEAXE,SICKLE,STAFF,KNIFE,SULITSA,WHIP }
 private static final Map<String,Shape> IDS=new HashMap<>();
 static {
  add(Shape.BLADE,"bone_knife","silver_dagger","ancient_carolingian_sword","restored_carolingian_sword","silver_sword","perunite_sword","tugarin_sword","trophy_kalin_sabre","nightingale_dagger");
  add(Shape.HEAVY,"club","wooden_mace","stone_mace","mace","silver_mace","perunite_mace","battle_axe","chekan","ancient_chekan","thunder_axe");
  add(Shape.SPEAR,"ancient_spear","restored_spear","spear","gold_spear","iron_spear","silver_spear","diamond_spear","perunite_spear","netherite_spear","pool_spear","thunder_spear","victor_spear");
  add(Shape.POLEAXE,"berdysh");add(Shape.SICKLE,"noon_sickle");add(Shape.STAFF,"carved_staff","storm_staff","veles_staff","likho_staff");
  for(String metal:new String[]{"stone","gold","iron","silver","diamond","perunite","netherite"}){add(Shape.KNIFE,metal+"_throwing_knife");add(Shape.SULITSA,metal+"_sulitsa");}
  add(Shape.WHIP,"poloz_whip");
 }
 private static void add(Shape shape,String... names){for(String name:names)if(IDS.put(name,shape)!=null)throw new IllegalStateException(name);}
 public static Map<String,Shape> coverage(){return java.util.Collections.unmodifiableMap(IDS);}
 public static void apply(PlayerModel<?> model,LivingEntity entity,float age){
  if(!(entity instanceof net.minecraft.client.player.AbstractClientPlayer)||entity.isFallFlying()||entity.isVisuallySwimming()||entity.isSleeping())return;
  poseHand(model,entity,HumanoidArm.RIGHT,age);poseHand(model,entity,HumanoidArm.LEFT,age);
  model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);
 }
 private static void poseHand(PlayerModel<?> model,LivingEntity owner,HumanoidArm arm,float age){
  InteractionHand hand=arm==owner.getMainArm()?InteractionHand.MAIN_HAND:InteractionHand.OFF_HAND;
  var stack=owner.getItemInHand(hand);var id=BuiltInRegistries.ITEM.getKey(stack.getItem());
  if(!id.getNamespace().equals("slavicmyths"))return;
  Shape shape=IDS.get(id.getPath());if(shape==null)return;
  if(owner.isUsingItem()&&owner.getUsedItemHand()!=hand)return; // Preserve shield/food/two-handed use.
  float partial=Mth.clamp(age-owner.tickCount,0,1);
  float swing=owner.swinging&&owner.swingingArm==hand?owner.getAttackAnim(partial):0;
  float charge=owner.isUsingItem()&&owner.getUsedItemHand()==hand?Mth.clamp((owner.getTicksUsingItem()+partial)/20F,0,1):0;
  pose(arm==HumanoidArm.LEFT?model.leftArm:model.rightArm,shape,arm==HumanoidArm.LEFT,swing,charge,owner.walkAnimation.speed(partial),age);
 }
 private static float ease(float t){t=Mth.clamp(t,0,1);return t*t*(3-2*t);}
 /** Mutates native ModelPart angles only. Vanilla resets these at the next setupAnim. */
 public static void pose(ModelPart arm,Shape shape,boolean left,float swing,float charge,float walk,float age){
  int sign=left?-1:1;
  float t=Mth.clamp(swing,0,1),prep=ease(t/.27F),hit=ease((t-.27F)/.27F),recovery=1-ease((t-.54F)/.46F);
  float windup=(prep-hit)*recovery,follow=hit*recovery;
  float sway=Mth.sin(age*.65F)*Mth.clamp(walk,0,1),use=ease(charge);
  switch(shape){
   case BLADE->{arm.xRot+=-.24F*windup+.18F*follow;arm.yRot+=sign*(-.22F*windup+.35F*follow);arm.zRot+=sign*.10F*follow;}
   case HEAVY->{arm.xRot+=-.48F*windup+.32F*follow;arm.zRot+=sign*(.18F*windup-.25F*follow);}
   case SPEAR->{arm.xRot+=-.18F*windup-.40F*follow;arm.yRot+=sign*(.12F*windup-.10F*follow);}
   case POLEAXE->{arm.xRot+=-.30F*windup+.20F*follow;arm.yRot+=sign*(-.32F*windup+.40F*follow);}
   case SICKLE->{arm.zRot+=sign*(.25F*windup-.40F*follow);arm.xRot+=-.14F*windup+.17F*follow;}
   case STAFF->{arm.xRot+=-.24F*follow;arm.yRot+=sign*.12F*follow;}
   case KNIFE->{arm.xRot+=-.48F*follow;arm.yRot+=sign*.20F*follow;}
   case SULITSA->{arm.xRot+=-.35F*use+.32F*follow;arm.zRot+=sign*.12F*use;}
   case WHIP->{arm.xRot+=-.32F*windup+.35F*follow;arm.yRot+=sign*(-.22F*windup+.28F*follow);}
  }
  arm.zRot+=sign*.025F*sway;
 }
 private PlayerWeaponPose(){}
}
