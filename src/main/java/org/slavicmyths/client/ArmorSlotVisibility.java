package org.slavicmyths.client;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.entity.EquipmentSlot;
/** Cached custom armor models must not retain visible bones from other equipment slots. */
public final class ArmorSlotVisibility {
 public static <T extends HumanoidModel<?>> T apply(T model,EquipmentSlot slot){
  model.setAllVisible(false);
  switch(slot){
   case HEAD -> {model.head.visible=true;model.hat.visible=true;}
   case CHEST -> {model.body.visible=true;model.leftArm.visible=true;model.rightArm.visible=true;}
   case LEGS -> {model.body.visible=true;model.leftLeg.visible=true;model.rightLeg.visible=true;}
   case FEET -> {model.leftLeg.visible=true;model.rightLeg.visible=true;}
   default -> {}
  }
  return model;
 }
 private ArmorSlotVisibility(){}
}