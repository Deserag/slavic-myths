package org.slavicmyths.karachun;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
public final class NightScythe extends Item {
 public NightScythe(){super(new Properties().durability(2400).rarity(Rarity.EPIC).attributes(ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_ID,org.slavicmyths.combat.RewardWeaponBalance.SCYTHE-1,AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_ID,-3,AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).build()));}
 @Override public int getEnchantmentValue(){return 18;}
 @Override public boolean isValidRepairItem(ItemStack a,ItemStack b){return b.is(Items.NETHERITE_INGOT)||super.isValidRepairItem(a,b);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<net.minecraft.network.chat.Component> t,TooltipFlag f){t.add(NightContent.text("scythe_hint"));t.add(NightContent.text("scythe_passives"));}
}
