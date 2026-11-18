package org.slavicmyths.nethersmithing;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.*;
/** Shared production component transformations, independent of item registration. */
public final class NetherSmithingRules {
 public static ItemAttributeModifiers attributes(ItemAttributeModifiers original){
  double raw=1;for(var e:original.modifiers())if(e.attribute().equals(Attributes.ATTACK_DAMAGE)&&e.modifier().id().equals(Item.BASE_ATTACK_DAMAGE_ID))raw+=e.modifier().amount();
  return original.withModifierAdded(Attributes.ATTACK_DAMAGE,new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID,raw*1.15-1,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND);
 }
 public static int durability(int base){return (int)Math.ceil(base*1.2);}
 public static void normalizeWear(ItemStack base,ItemStack result){if(base.isDamageableItem()&&result.isDamageableItem())result.setDamageValue((int)Math.ceil((double)base.getDamageValue()/base.getMaxDamage()*result.getMaxDamage()));}
 private NetherSmithingRules(){}
}
