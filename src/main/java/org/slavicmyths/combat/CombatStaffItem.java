package org.slavicmyths.combat;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
/** Staff melee and enchanting admission without inheriting sword sweep/looting tags. */
public class CombatStaffItem extends Item {
 private final int enchantability;
 public CombatStaffItem(Properties p,double damage,int enchantability){super(p.attributes(StaffStats.melee(damage)));this.enchantability=enchantability;}
 @Override public int getEnchantmentValue(){return enchantability;}
 @Override public boolean isEnchantable(ItemStack stack){return true;}
 @Override public boolean isPrimaryItemFor(ItemStack stack,Holder<Enchantment> enchantment){return supportsEnchantment(stack,enchantment);}
 @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){if(!attacker.level().isClientSide&&stack.isDamageableItem())stack.hurtAndBreak(1,attacker,EquipmentSlot.MAINHAND);return true;}
 @Override public void appendHoverText(ItemStack stack,TooltipContext c,java.util.List<net.minecraft.network.chat.Component> lines,TooltipFlag flag){super.appendHoverText(stack,c,lines,flag);lines.add(net.minecraft.network.chat.Component.translatable("combat.slavicmyths.staff_damage_scope"));}
}
