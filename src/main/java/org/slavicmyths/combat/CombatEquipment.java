package org.slavicmyths.combat;

import com.google.common.collect.*;
import java.util.UUID;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.*;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="slavicmyths")
public final class CombatEquipment {
    public static final IArmorMaterial GAMBESON=material("gambeson",4,12,0);
    public static final IArmorMaterial PLATE=material("plate",7,22,1);
    private static IArmorMaterial material(String name,int defense,int durability,float toughness) {
        return new IArmorMaterial(){
            public int getDurabilityForSlot(EquipmentSlotType s){return 16*durability;}
            public int getDefenseForSlot(EquipmentSlotType s){return defense;}
            public int getEnchantmentValue(){return 9;}
            public SoundEvent getEquipSound(){return name.equals("plate")?SoundEvents.ARMOR_EQUIP_IRON:SoundEvents.ARMOR_EQUIP_LEATHER;}
            public Ingredient getRepairIngredient(){return Ingredient.of(name.equals("plate")?Items.IRON_INGOT:Items.LEATHER);}
            public String getName(){return "slavicmyths:"+name;}
            public float getToughness(){return toughness;}
            public float getKnockbackResistance(){return name.equals("plate")?.05F:0;}
        };
    }
    public static final class PlateArmor extends ArmorItem {
        public PlateArmor(Properties p){super(PLATE,EquipmentSlotType.CHEST,p);}
        @Override public Multimap<Attribute,AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType slot) {
            if(slot!=EquipmentSlotType.CHEST)return super.getDefaultAttributeModifiers(slot);
            return ImmutableMultimap.<Attribute,AttributeModifier>builder().putAll(super.getDefaultAttributeModifiers(slot))
                .put(Attributes.MOVEMENT_SPEED,new AttributeModifier(UUID.fromString("5d907140-6313-461d-aa06-d4c6623f0800"),"Plate weight",-.05,AttributeModifier.Operation.MULTIPLY_TOTAL)).build();
        }
    }
    @SubscribeEvent public static void hurt(LivingHurtEvent event) {
        if(event.getEntityLiving().level.isClientSide||event.getSource().isProjectile()||!(event.getSource().getDirectEntity() instanceof LivingEntity))return;
        LivingEntity attacker=(LivingEntity)event.getSource().getDirectEntity();Item item=attacker.getMainHandItem().getItem();
        if(item instanceof MaceItem) {
            // Scales the accepted hit, not a second invulnerability-bypassing damage event.
            float armor=event.getEntityLiving().getArmorValue();
            event.setAmount(event.getAmount()*(1+Math.min(.45F,armor*((MaceItem)item).pressure/20)));
        }
    }
    private CombatEquipment(){}
}
