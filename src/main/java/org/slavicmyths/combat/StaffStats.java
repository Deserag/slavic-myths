package org.slavicmyths.combat;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
/** Melee stats only. Spells retain their separate damage sources and rune scaling. */
public final class StaffStats {
 public static ItemAttributeModifiers melee(double totalDamage){return ItemAttributeModifiers.builder()
  .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,totalDamage-1,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
  .add(Attributes.ATTACK_SPEED,new AttributeModifier(Item.BASE_ATTACK_SPEED_ID,-2.8,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).build();}
 private StaffStats(){}
}
