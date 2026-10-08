package org.slavicmyths.combat;

import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.*;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.resources.ResourceLocation;

/** One durable item per throw; the server transfers its exact component-bearing stack. */
public final class ThrowingWeaponItem extends TieredItem {
    public final boolean knife;
    public ThrowingWeaponItem(Tier tier,boolean knife,Properties properties) {
        super(tier,properties.attributes(ItemAttributeModifiers.builder()
            .add(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_ID,(knife?1:2)+tier.getAttackDamageBonus(),AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_ID,knife?-1.7:-2.5,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND)
            .add(Attributes.ENTITY_INTERACTION_RANGE,new AttributeModifier(ResourceLocation.fromNamespaceAndPath("slavicmyths","sulitsa_reach"),knife?0:.5,AttributeModifier.Operation.ADD_VALUE),EquipmentSlotGroup.MAINHAND).build()));
        this.knife=knife;
    }
    public float thrownDamage(){return knife?2+getTier().getAttackDamageBonus()*.35F:5+getTier().getAttackDamageBonus();}
    @Override public int getUseDuration(ItemStack stack,LivingEntity user){return 72000;}
    @Override public UseAnim getUseAnimation(ItemStack stack){return UseAnim.SPEAR;}
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        ItemStack stack=player.getItemInHand(hand);
        if(player.getCooldowns().isOnCooldown(this)||stack.getDamageValue()>=stack.getMaxDamage()-1)return InteractionResultHolder.fail(stack);
        if(knife)throwOne(level,player,hand,stack,1.65F);else player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity user,int remaining) {
        if(knife||!(user instanceof Player player))return;
        int held=getUseDuration(stack,user)-remaining;
        if(held<6||player.getCooldowns().isOnCooldown(this)||stack.getDamageValue()>=stack.getMaxDamage()-1)return;
        throwOne(level,player,player.getUsedItemHand(),stack,1.3F+Math.min(held,20)/20F*1.2F);
    }
    private void throwOne(Level level,Player player,InteractionHand hand,ItemStack stack,float velocity) {
        if(level.isClientSide)return;
        boolean creative=player.hasInfiniteMaterials();
        ItemStack carried=stack.copyWithCount(1);
        if(!creative)carried.hurtAndBreak(1,player,LivingEntity.getSlotForHand(hand));
        if(carried.isEmpty())return;
        WeaponProjectile projectile=new WeaponProjectile(level,player,carried,knife,creative);
        projectile.shootFromRotation(player,player.getXRot(),player.getYRot(),0,velocity,knife?1.5F:1F);
        if(!level.addFreshEntity(projectile))return;
        if(!creative)stack.shrink(1);
        player.getCooldowns().addCooldown(this,knife?8:16);
        player.awardStat(Stats.ITEM_USED.get(this));
        level.playSound(null,player.blockPosition(),SoundEvents.TRIDENT_THROW.value(),SoundSource.PLAYERS,.45F,knife?1.6F:1F);
    }
    @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker){stack.hurtAndBreak(1,attacker,EquipmentSlot.MAINHAND);return true;}
    @Override public boolean supportsEnchantment(ItemStack stack,net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        return !enchantment.is(net.minecraft.world.item.enchantment.Enchantments.SWEEPING_EDGE)&&
            (enchantment.value().isSupportedItem(new ItemStack(Items.IRON_SWORD))||super.supportsEnchantment(stack,enchantment));
    }
    @Override public boolean isPrimaryItemFor(ItemStack stack,net.minecraft.core.Holder<net.minecraft.world.item.enchantment.Enchantment> enchantment) {
        return !enchantment.is(net.minecraft.world.item.enchantment.Enchantments.SWEEPING_EDGE)&&
            (enchantment.value().isPrimaryItem(new ItemStack(Items.IRON_SWORD))||super.isPrimaryItemFor(stack,enchantment));
    }
}
