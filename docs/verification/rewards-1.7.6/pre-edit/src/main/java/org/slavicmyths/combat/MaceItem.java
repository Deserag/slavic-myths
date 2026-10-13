package org.slavicmyths.combat;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
/** One target, original total damage/speed, armor pressure and push; no sword sweep. */
public class MaceItem extends TieredItem {
 public final float pressure,push;
 public MaceItem(Tier tier,float damage,float speed,float pressure,float push,Properties properties) {
  super(tier,properties.attributes(ItemAttributeModifiers.builder()
   .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_ID,damage-1,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
   .add(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_ID,speed-4,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).build()));
  this.pressure=pressure;this.push=push;
 }
 @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker) {
  if(!attacker.level().isClientSide){var d=target.position().subtract(attacker.position());target.knockback(push,-d.x,-d.z);stack.hurtAndBreak(1,attacker,EquipmentSlot.MAINHAND);}return true;
 }
 // Legacy WEAPON + BREAKABLE eligibility, expressed through the target sword definitions.
 // The item remains a TieredItem, so this does not grant SWORD_SWEEP or sword mining behavior.
 @Override public boolean supportsEnchantment(ItemStack stack,Holder<Enchantment> enchantment) {
  return !enchantment.is(Enchantments.SWEEPING_EDGE) &&
   (enchantment.value().isSupportedItem(new ItemStack(Items.IRON_SWORD)) || super.supportsEnchantment(stack,enchantment));
 }
 @Override public boolean isPrimaryItemFor(ItemStack stack,Holder<Enchantment> enchantment) {
  return !enchantment.is(Enchantments.SWEEPING_EDGE) &&
   (enchantment.value().isPrimaryItem(new ItemStack(Items.IRON_SWORD)) || super.isPrimaryItemFor(stack,enchantment));
 }
}
