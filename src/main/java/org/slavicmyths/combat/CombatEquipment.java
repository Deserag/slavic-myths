package org.slavicmyths.combat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.slavicmyths.item.ModGear;
@EventBusSubscriber(modid="slavicmyths")
public final class CombatEquipment {
 public static final class PlateArmor extends ArmorItem {
  private static final ResourceLocation WEIGHT=ResourceLocation.fromNamespaceAndPath("slavicmyths","5d907140-6313-461d-aa06-d4c6623f0800");
  public PlateArmor(Properties properties){super(ModGear.PLATE,Type.CHESTPLATE,properties.durability(352));}
  @Override public ItemAttributeModifiers getDefaultAttributeModifiers() {
   return super.getDefaultAttributeModifiers().withModifierAdded(Attributes.MOVEMENT_SPEED,
    new AttributeModifier(WEIGHT,-.05,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),EquipmentSlotGroup.CHEST);
  }
 }
 @SubscribeEvent public static void hurt(LivingIncomingDamageEvent event) {
  if(event.getEntity().level().isClientSide || event.getSource().is(DamageTypeTags.IS_PROJECTILE)
   || !(event.getSource().getDirectEntity() instanceof LivingEntity attacker))return;
  if(attacker.getMainHandItem().getItem() instanceof MaceItem item) {
   float armor=event.getEntity().getArmorValue();
   event.setAmount(event.getAmount()*(1+Math.min(.45F,armor*item.pressure/20)));
  }
 }
 private CombatEquipment() { }
}
