package org.slavicmyths.karachun;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.HitResult;
import org.slavicmyths.combat.WeaponInputHandler;
public final class NightScythe extends Item implements WeaponInputHandler {
 public NightScythe(){super(new Properties().durability(2400).rarity(Rarity.EPIC).attributes(ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_ID,15,AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_ID,-3,AttributeModifier.Operation.ADD_VALUE),net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND).build()));}
 @Override public boolean intercepts(Action action, HitResult.Type hit) {
  return switch(action) {
   case LEFT_CLICK -> hit != HitResult.Type.BLOCK;
   case RIGHT_CLICK -> false;
   case SHIFT_LEFT_CLICK, SHIFT_RIGHT_CLICK -> true;
  };
 }
 @Override public void onWeaponAction(ServerPlayer player,ItemStack held,Action action) {
  if(held != player.getMainHandItem() || !player.isAlive() || player.isSpectator()) return;
  switch(action) {
   case LEFT_CLICK -> NightCombat.action(player,0);
   case SHIFT_LEFT_CLICK -> NightCombat.action(player,1);
   case SHIFT_RIGHT_CLICK -> NightCombat.action(player,2);
   case RIGHT_CLICK -> {}
  }
 }
 @Override public int getEnchantmentValue(){return 18;}
 @Override public boolean isValidRepairItem(ItemStack a,ItemStack b){return b.is(Items.NETHERITE_INGOT)||super.isValidRepairItem(a,b);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,java.util.List<net.minecraft.network.chat.Component> t,TooltipFlag f){t.add(NightContent.text("scythe_hint"));t.add(NightContent.text("scythe_passives"));}
}
