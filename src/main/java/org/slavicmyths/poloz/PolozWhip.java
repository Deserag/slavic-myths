package org.slavicmyths.poloz;

import java.util.List;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;

/** Short spiked grip, long articulated dark/gold lash; never a recolored sword. */
public final class PolozWhip extends Item {
 public PolozWhip(){super(new Properties().durability(2031).fireResistant().rarity(Rarity.EPIC).attributes(ItemAttributeModifiers.builder()
  .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_ID,12,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
  .add(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_ID,-3,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
  .add(Attributes.ENTITY_INTERACTION_RANGE,new AttributeModifier(PolozContent.id("whip_reach"),3,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).build()));}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand hand){var s=p.getItemInHand(hand);if(p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(s);if(p instanceof ServerPlayer server){PolozRewardEvents.spikes(server);p.getCooldowns().addCooldown(this,360);s.hurtAndBreak(1,p,net.minecraft.world.entity.LivingEntity.getSlotForHand(hand));p.swing(hand,true);}return InteractionResultHolder.sidedSuccess(s,w.isClientSide);}
 @Override public void appendHoverText(ItemStack s,TooltipContext c,List<Component> l,TooltipFlag f){l.add(Component.translatable("item.slavicmyths.poloz_whip.desc"));}
}
