package org.slavicmyths.kurgan;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.*;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.item.FolkEquipmentEffects;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class BurialAccessories {
 @SubscribeEvent public static void hurt(LivingIncomingDamageEvent e){LivingEntity victim=e.getEntity();if(victim.level().isClientSide||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY))return;
  if(FolkEquipmentEffects.wears(victim,ModItems.LUNULA.get())&&victim.level().isNight()&&e.getSource().getEntity() instanceof LivingEntity)e.setAmount(e.getAmount()*.95F);
  if(FolkEquipmentEffects.wears(victim,ModItems.GRIVNA.get())&&!e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_ARMOR))e.setAmount(e.getAmount()*.96F);
  Entity attacker=e.getSource().getEntity();if(attacker instanceof LivingEntity&&attacker.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)&&FolkEquipmentEffects.wears(victim,ModItems.GRAVE_WARD.get()))e.setAmount(e.getAmount()*.91F);
 }
 @SubscribeEvent public static void tooltip(ItemTooltipEvent e){Item i=e.getItemStack().getItem();if(i==ModItems.ANCIENT_FIBULA.get()||i==ModItems.ANCIENT_COMB.get()||i==ModItems.ANCIENT_BEADS.get()||i==ModItems.OLD_BUCKLE.get()||i==ModItems.POTTERY_FRAGMENT.get()||i==ModItems.OLD_ARROWHEAD.get()||i==ModItems.LUNULA.get()||i==ModItems.GRIVNA.get()||i==ModItems.GRAVE_WARD.get()||i==ModItems.ANCIENT_CHEKAN.get())e.getToolTip().add(Component.translatable("tooltip.slavicmyths.burial_find").withStyle(ChatFormatting.GRAY));}
}
