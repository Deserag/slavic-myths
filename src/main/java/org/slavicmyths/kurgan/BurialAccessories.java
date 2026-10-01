package org.slavicmyths.kurgan;
import net.minecraft.item.*;
import net.minecraft.entity.*;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.util.text.*;
import org.slavicmyths.registry.ModItems;
import org.slavicmyths.item.FolkEquipmentEffects;
@Mod.EventBusSubscriber(modid="slavicmyths")
public final class BurialAccessories {
 @SubscribeEvent public static void hurt(LivingHurtEvent e){LivingEntity victim=e.getEntityLiving();if(victim.level.isClientSide||e.getSource().isBypassInvul())return;
  if(FolkEquipmentEffects.wears(victim,ModItems.LUNULA.get())&&victim.level.isNight()&&e.getSource().getEntity() instanceof LivingEntity)e.setAmount(e.getAmount()*.95F);
  if(FolkEquipmentEffects.wears(victim,ModItems.GRIVNA.get())&&!e.getSource().isBypassArmor())e.setAmount(e.getAmount()*.96F);
  Entity attacker=e.getSource().getEntity();if(attacker instanceof LivingEntity&&((LivingEntity)attacker).getMobType()==CreatureAttribute.UNDEAD&&FolkEquipmentEffects.wears(victim,ModItems.GRAVE_WARD.get()))e.setAmount(e.getAmount()*.91F);
 }
 @SubscribeEvent public static void tooltip(ItemTooltipEvent e){Item i=e.getItemStack().getItem();if(i==ModItems.ANCIENT_FIBULA.get()||i==ModItems.ANCIENT_COMB.get()||i==ModItems.ANCIENT_BEADS.get()||i==ModItems.OLD_BUCKLE.get()||i==ModItems.POTTERY_FRAGMENT.get()||i==ModItems.OLD_ARROWHEAD.get()||i==ModItems.LUNULA.get()||i==ModItems.GRIVNA.get()||i==ModItems.GRAVE_WARD.get()||i==ModItems.ANCIENT_CHEKAN.get())e.getToolTip().add(new TranslationTextComponent("tooltip.slavicmyths.burial_find").withStyle(TextFormatting.GRAY));}
}
