package org.slavicmyths.combat;

import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.particles.SimpleParticleType;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.slavicmyths.registry.ModEffects;
import org.slavicmyths.rpg.*;

@EventBusSubscriber(modid="slavicmyths")
public final class RareCombat {
    public static final int SWING_WINDOW=80, SECOND_WINDOW=25, SECOND_COOLDOWN=160;
    public static long now(Entity e){return e.getServer()==null?e.level().getGameTime():e.getServer().overworld().getGameTime();}
    public static boolean paired(Player p){return p.getMainHandItem().is(RareWeapons.TUGARIN.get())&&p.getOffhandItem().is(RareWeapons.TUGARIN.get());}
    private static boolean melee(net.minecraft.world.damagesource.DamageSource s){return s.is(DamageTypes.PLAYER_ATTACK)&&s.getDirectEntity() instanceof ServerPlayer;}
    public static void feedback(LivingEntity e,SimpleParticleType particle){if(e.level() instanceof ServerLevel w)w.sendParticles(particle,e.getX(),e.getEyeY(),e.getZ(),4,.15,.15,.15,.01);}
    @SubscribeEvent(priority=EventPriority.LOWEST) public static void attack(AttackEntityEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;
        var d=p.getPersistentData();d.putFloat("RareAttackCharge",p.getAttackStrengthScale(.5F));
        if(!p.getMainHandItem().is(RareWeapons.VICTOR.get())||!(e.getTarget() instanceof LivingEntity target))return;
        if(d.getLong("RareSecondUntil")>now(p)&&d.hasUUID("RareSecondTarget")&&d.getUUID("RareSecondTarget").equals(target.getUUID())
            &&d.getString("RareSecondDimension").equals(p.level().dimension().location().toString())
            &&p.canInteractWithEntity(target,0)&&p.hasLineOfSight(target)&&Abilities.canHit(p,target)){
            d.remove("RareSecondUntil");d.putBoolean("RareSecondAttack",true);
            PathData.data(p).putLong("CD_rare_second",now(p)+SECOND_COOLDOWN);
            // Native Player.attack still owns damage, enchantments, exhaustion and durability.
            p.attackStrengthTicker=1000;target.invulnerableTime=0;
        }else d.remove("RareSecondAttack");
    }
    @SubscribeEvent(priority=EventPriority.HIGH) public static void incoming(LivingIncomingDamageEvent e){
        if(e.getEntity().level().isClientSide)return;
        if(e.getSource().getEntity() instanceof Mob mob&&mob.hasEffect(ModEffects.DISORIENTATION)
            &&(DisorientationEffect.stunned(mob)||!DisorientationEffect.allowed(mob,e.getEntity()))){e.setCanceled(true);return;}
        if(!melee(e.getSource()))return;var p=(ServerPlayer)e.getSource().getDirectEntity();var d=p.getPersistentData();
        if(paired(p)&&d.getFloat("RareAttackCharge")>=.9F)e.setAmount(e.getAmount()*1.4F);
        if(p.getMainHandItem().is(RareWeapons.ATAMAN.get())&&d.getFloat("RareAttackCharge")>=.9F
            &&d.getLong("RareSwingUntil")>now(p)&&d.getInt("RareSwingStacks")==3){
            e.setAmount(e.getAmount()*1.35F);d.putBoolean("RareSwingBoost",true);
        }else d.remove("RareSwingBoost");
    }
    @SubscribeEvent public static void shield(LivingShieldBlockEvent e){
        if(e.getEntity().getPersistentData().getLong("RareShieldUntil")>now(e.getEntity())){e.setBlocked(false);return;}
        if(!e.getOriginalBlock()||!melee(e.getDamageSource()))return;
        var p=(ServerPlayer)e.getDamageSource().getDirectEntity();if(!p.getPersistentData().getBoolean("RareSwingBoost"))return;
        if(e.getEntity() instanceof Player defender){defender.getCooldowns().addCooldown(defender.getUseItem().getItem(),60);defender.stopUsingItem();}
        else e.getEntity().stopUsingItem();
        e.getEntity().getPersistentData().putLong("RareShieldUntil",now(e.getEntity())+60);
        e.setBlockedDamage(e.getOriginalBlockedDamage()*.5F);
    }
    @SubscribeEvent public static void hit(LivingDamageEvent.Post e){
        if(e.getNewDamage()<=0||!melee(e.getSource()))return;
        var p=(ServerPlayer)e.getSource().getDirectEntity();var d=p.getPersistentData();var target=e.getEntity();
        if(paired(p)&&d.getFloat("RareAttackCharge")>=.9F){RareBleedEffect.apply(p,target);p.swing(InteractionHand.OFF_HAND,true);p.getOffhandItem().hurtAndBreak(1,p,EquipmentSlot.OFFHAND);}
        if(p.getMainHandItem().is(RareWeapons.ATAMAN.get())&&d.getFloat("RareAttackCharge")>=.9F){
            boolean boost=d.getBoolean("RareSwingBoost");d.putInt("RareSwingStacks",boost?0:d.getLong("RareSwingUntil")>now(p)?Math.min(3,d.getInt("RareSwingStacks")+1):1);
            d.putLong("RareSwingUntil",now(p)+SWING_WINDOW);d.remove("RareSwingBoost");
            if(boost)target.knockback(1,p.getX()-target.getX(),p.getZ()-target.getZ());
            p.playSound(net.minecraft.sounds.SoundEvents.CHAIN_HIT,.35F,boost?.7F:1.2F);
            p.displayClientMessage(net.minecraft.network.chat.Component.translatable("rare.slavicmyths.swing",d.getInt("RareSwingStacks")),true);
        }
        if(p.getMainHandItem().is(RareWeapons.VICTOR.get())){
            if(d.getBoolean("RareSecondAttack")){d.remove("RareSecondAttack");return;}
            if(d.getFloat("RareAttackCharge")>=.9F&&target.getBoundingBox().distanceToSqr(p.getEyePosition())>9&&p.canInteractWithEntity(target,0)&&p.hasLineOfSight(target)
                &&PathData.data(p).getLong("CD_rare_second")<=now(p)){
                d.putUUID("RareSecondTarget",target.getUUID());d.putString("RareSecondDimension",p.level().dimension().location().toString());d.putLong("RareSecondUntil",now(p)+SECOND_WINDOW);
                p.displayClientMessage(net.minecraft.network.chat.Component.translatable("rare.slavicmyths.second"),true);p.playSound(net.minecraft.sounds.SoundEvents.IRON_GOLEM_REPAIR,.35F,1.5F);
            }
        }
    }
    @SubscribeEvent public static void target(LivingChangeTargetEvent e){
        if(e.getEntity() instanceof Mob mob&&mob.hasEffect(ModEffects.DISORIENTATION)
            &&(DisorientationEffect.stunned(mob)||!DisorientationEffect.allowed(mob,e.getNewAboutToBeSetTarget())))e.setNewAboutToBeSetTarget(null);
    }
    @SubscribeEvent public static void join(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent e){clear(e.getEntity());}
    @SubscribeEvent public static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){clear(e.getEntity());}
    @SubscribeEvent public static void respawn(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerRespawnEvent e){clear(e.getEntity());}
    private static void clear(Player p){var d=p.getPersistentData();for(String key:new String[]{"RareSecondUntil","RareSecondAttack","RareSwingStacks","RareSwingUntil","RareSwingBoost","RareAttackCharge"})d.remove(key);}
    private RareCombat() { }
}
