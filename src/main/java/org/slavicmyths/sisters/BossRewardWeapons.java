package org.slavicmyths.sisters;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class BossRewardWeapons {
    public static final class Weapon extends SwordItem {
        private final boolean scythe;
        public Weapon(boolean scythe) {
            super(Tiers.NETHERITE,new Item.Properties().stacksTo(1).durability(scythe?1900:1650).rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE,scythe?7:6,scythe?-2.8F:-2.6F)));
            this.scythe=scythe;
        }
        private boolean valid(Player owner,LivingEntity target) {
            if(target==owner||!target.isAlive()||owner.isAlliedTo(target)||target.isAlliedTo(owner))return false;
            if(target instanceof Player p&&(p.isCreative()||p.isSpectator()||!owner.canHarmPlayer(p)))return false;
            return true;
        }
        private void effect(LivingEntity target) {
            if(scythe) {
                target.addEffect(new MobEffectInstance(MobEffects.WITHER,100,0));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,100,0));
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,70,1));
                if(target instanceof Player p)FractureHooks.apply(p);
            }
        }
        @Override public boolean hurtEnemy(ItemStack stack,LivingEntity target,LivingEntity attacker) {
            if(!attacker.level().isClientSide&&!(attacker instanceof Player p&& !valid(p,target)))effect(target);
            return super.hurtEnemy(stack,target,attacker);
        }
        @Override public InteractionResultHolder<ItemStack> use(Level level,Player user,InteractionHand hand) {
            ItemStack stack=user.getItemInHand(hand);
            if(level.isClientSide)return InteractionResultHolder.sidedSuccess(stack,true);
            if(user.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(stack);
            if(hand!=InteractionHand.MAIN_HAND)return InteractionResultHolder.fail(stack);
            ServerLevel world=(ServerLevel)level;
            double radius=scythe?4.25:3.5;
            Vec3 center=user.position().add(0,.65,0);
            int hits=0;
            for(LivingEntity target:world.getEntitiesOfClass(LivingEntity.class,user.getBoundingBox().inflate(radius,2.1,radius),
                target->valid(user,target))) {
                if(target.position().distanceToSqr(user.position())>radius*radius || !user.hasLineOfSight(target))continue;
                if(target.hurt(world.damageSources().playerAttack(user),scythe?10F:9F)) {
                    effect(target);
                    hits++;
                }
            }
            for(int i=0;i<32;i++){
                double a=i*Math.PI/16;
                world.sendParticles(scythe?ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.CRIT,
                    center.x+Math.cos(a)*radius,center.y,center.z+Math.sin(a)*radius,1,0,0,0,0);
            }
            user.getCooldowns().addCooldown(this,scythe?180:150);
            stack.hurtAndBreak(2,user,net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            return InteractionResultHolder.success(stack);
        }
        @Override public void appendHoverText(ItemStack stack,Item.TooltipContext context,List<Component> lines,TooltipFlag flag) {
            super.appendHoverText(stack,context,lines,flag);
            lines.add(Component.translatable(scythe?"item.slavicmyths.neveya_scythe.effect":"item.slavicmyths.lomeya_hammer.effect"));
        }
    }
    private BossRewardWeapons(){}
}
