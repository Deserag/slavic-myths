package org.slavicmyths.combat;

import com.google.common.collect.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.util.math.vector.Vector3d;

/** A single-target weapon: no sword sweeping attack. Armor pressure applies to players and NPCs. */
public class MaceItem extends TieredItem {
    private final Multimap<Attribute,AttributeModifier> attributes;
    public final float pressure,push;
    public MaceItem(IItemTier tier,float damage,float speed,float pressure,float push,Properties properties) {
        super(tier,properties);this.pressure=pressure;this.push=push;
        attributes=ImmutableMultimap.of(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,"Mace damage",damage-1,AttributeModifier.Operation.ADDITION),
            Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_UUID,"Mace speed",speed-4,AttributeModifier.Operation.ADDITION));
    }
    @Override public Multimap<Attribute,AttributeModifier> getDefaultAttributeModifiers(EquipmentSlotType slot){return slot==EquipmentSlotType.MAINHAND?attributes:super.getDefaultAttributeModifiers(slot);}
    @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker) {
        if(!attacker.level.isClientSide){Vector3d d=target.position().subtract(attacker.position());target.knockback(push,-d.x,-d.z);stack.hurtAndBreak(1,attacker,e->e.broadcastBreakEvent(EquipmentSlotType.MAINHAND));}return true;
    }
    @Override public boolean canApplyAtEnchantingTable(ItemStack stack,net.minecraft.enchantment.Enchantment enchantment){return enchantment!=net.minecraft.enchantment.Enchantments.SWEEPING_EDGE&&(enchantment.category==net.minecraft.enchantment.EnchantmentType.WEAPON||super.canApplyAtEnchantingTable(stack,enchantment));}
}
