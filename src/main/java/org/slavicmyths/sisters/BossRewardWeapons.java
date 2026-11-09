package org.slavicmyths.sisters;

import java.util.List;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
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

@EventBusSubscriber(modid="slavicmyths")
public final class BossRewardWeapons {
    private static final String AOE="SlavicLomeyaAreaStrike",DUAL="SlavicLomeyaDualStrike",READY="SlavicLomeyaDualReady",READY_TICK="SlavicLomeyaDualReadyTick";
    public static final class Weapon extends SwordItem {
        private final boolean scythe;
        public Weapon(boolean scythe) {
            super(Tiers.NETHERITE,new Item.Properties().stacksTo(1).durability(scythe?1900:1650).rarity(Rarity.EPIC)
                .attributes(SwordItem.createAttributes(Tiers.NETHERITE,scythe?7:6,scythe?-2.8F:-2.6F)));
            this.scythe=scythe;
        }
        private boolean valid(Player owner,LivingEntity target) {
            if(target==owner||!target.isAlive()||owner.isAlliedTo(target)||target.isAlliedTo(owner)
                ||target.getPersistentData().getBoolean("SlavicViySummoned"))return false;
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
            if(!scythe)user.getPersistentData().putBoolean(AOE,true);
            try {
                for(LivingEntity target:world.getEntitiesOfClass(LivingEntity.class,user.getBoundingBox().inflate(radius,2.1,radius),
                    target->valid(user,target))) {
                    if(target.position().distanceToSqr(user.position())>radius*radius || !user.hasLineOfSight(target))continue;
                    if(target.hurt(world.damageSources().playerAttack(user),scythe?10F:9F)) {
                        effect(target);
                        hits++;
                    }
                }
            }finally {
                if(!scythe)user.getPersistentData().remove(AOE);
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
    @SubscribeEvent public static void prepareDualHammer(AttackEntityEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer owner))return;
        owner.getPersistentData().remove(READY);
        owner.getPersistentData().remove(READY_TICK);
        if(!(event.getTarget() instanceof LivingEntity target)
            ||!owner.getMainHandItem().is(SistersContent.LOMEYA_HAMMER.get())
            ||!owner.getOffhandItem().is(SistersContent.LOMEYA_HAMMER_OFFHAND.get())
            ||owner.getAttackStrengthScale(.5F)<.9F
            ||!target.isAlive()||target==owner||owner.isAlliedTo(target)||target.isAlliedTo(owner)
            ||target.getPersistentData().getBoolean("SlavicViySummoned")
            ||target instanceof Player p&&(p.isCreative()||p.isSpectator()||!owner.canHarmPlayer(p)))return;
        owner.getPersistentData().putUUID(READY,target.getUUID());
        owner.getPersistentData().putLong(READY_TICK,owner.serverLevel().getGameTime());
    }
    @SubscribeEvent public static void dualHammer(LivingDamageEvent.Post event) {
        LivingEntity target=event.getEntity();
        if(!(event.getSource().getEntity() instanceof ServerPlayer owner)
            ||!event.getSource().is(DamageTypes.PLAYER_ATTACK)
            ||!owner.getMainHandItem().is(SistersContent.LOMEYA_HAMMER.get())
            ||!owner.getOffhandItem().is(SistersContent.LOMEYA_HAMMER_OFFHAND.get())
            ||owner.getPersistentData().getBoolean(AOE)
            ||owner.getPersistentData().getBoolean(DUAL)
            ||!owner.getPersistentData().hasUUID(READY)
            ||!owner.getPersistentData().getUUID(READY).equals(target.getUUID())
            ||owner.getPersistentData().getLong(READY_TICK)!=owner.serverLevel().getGameTime()
            ||!target.isAlive()||target==owner||owner.isAlliedTo(target)||target.isAlliedTo(owner)
            ||target.getPersistentData().getBoolean("SlavicViySummoned")
            ||target instanceof Player p&&(p.isCreative()||p.isSpectator()||!owner.canHarmPlayer(p)))return;
        owner.getPersistentData().putBoolean(DUAL,true);
        try {
            owner.swing(InteractionHand.OFF_HAND,true);
            if(target.hurt(owner.damageSources().playerAttack(owner),3.5F)){
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,70,1));
                if(target instanceof Player player)FractureHooks.apply(player);
                owner.getOffhandItem().hurtAndBreak(1,owner,net.minecraft.world.entity.EquipmentSlot.OFFHAND);
            }
        }finally {
            owner.getPersistentData().remove(DUAL);
            owner.getPersistentData().remove(READY);
            owner.getPersistentData().remove(READY_TICK);
        }
    }
    private BossRewardWeapons(){}
}
