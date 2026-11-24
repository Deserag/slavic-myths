package org.slavicmyths.winter;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.combat.CombatTargeting;
import org.slavicmyths.morozko.FrostVisuals;
import org.slavicmyths.registry.ModEffects;

@EventBusSubscriber(modid="slavicmyths")
public final class SnowArmorEffects {
    private static final net.minecraft.resources.ResourceLocation SPEED=SnowArmor.id("snow_set_speed");
    private static final TagKey<DamageType> FROST=TagKey.create(Registries.DAMAGE_TYPE,SnowArmor.id("snow_armor_frost"));
    private static boolean surface(net.minecraft.world.level.block.state.BlockState b){return b.is(BlockTags.ICE)||b.is(Blocks.SNOW)||b.is(Blocks.SNOW_BLOCK)||b.is(Blocks.POWDER_SNOW);}
    private static boolean freezing(DamageSource s){return s.is(DamageTypeTags.IS_FREEZING)||s.is(FROST);}
    private static void refresh(LivingEntity e){
        if(e.level().isClientSide)return;
        boolean full=SnowArmor.full(e);
        var speed=e.getAttribute(Attributes.MOVEMENT_SPEED);
        var pos=e.blockPosition();
        boolean snowy=full&&e.onGround()&&(surface(e.level().getBlockState(pos))||surface(e.level().getBlockState(pos.below())));
        if(speed!=null){
            if(snowy&&speed.getModifier(SPEED)==null)speed.addTransientModifier(new AttributeModifier(SPEED,.15,AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            else if(!snowy)speed.removeModifier(SPEED);
        }
        if(full){
            e.setTicksFrozen(0);
            e.removeEffect(ModEffects.AUKA_FROST);
            e.removeEffect(ModEffects.AUKA_FROST_BLOOD);
        }
        if(e.getHealth()>e.getMaxHealth())e.setHealth(e.getMaxHealth());
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){refresh(e.getEntity());}
    @SubscribeEvent public static void equip(LivingEquipmentChangeEvent e){refresh(e.getEntity());}
    @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){var a=e.getEntity().getAttribute(Attributes.MOVEMENT_SPEED);if(a!=null)a.removeModifier(SPEED);}
    @SubscribeEvent public static void damage(LivingIncomingDamageEvent e){
        if(e.getEntity().level().isClientSide||!freezing(e.getSource()))return;
        int count=SnowArmor.count(e.getEntity());
        if(count==4){e.setCanceled(true);e.getEntity().setTicksFrozen(0);}
        else if(count>0)e.setAmount(e.getAmount()*(1-.10F*count));
    }
    @SubscribeEvent public static void aura(LivingDamageEvent.Post e){
        if(e.getNewDamage()<=0||!(e.getEntity() instanceof ServerPlayer p)||!SnowArmor.full(p)||p.isSpectator())return;
        if(!(e.getSource().getEntity() instanceof LivingEntity attacker)||!CombatTargeting.canAttack(p,attacker))return;
        long now=p.server.overworld().getGameTime();
        var data=org.slavicmyths.rpg.PathData.data(p);
        if(data.getLong("CD_snow_armor_aura")>now)return;
        boolean applied=false;
        for(var t:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(4),
                t->t.distanceToSqr(p)<=16&&p.hasLineOfSight(t)&&CombatTargeting.canControl(p,t)&&!CombatTargeting.controlResistant(t)
                        &&(t==attacker||CombatTargeting.isEnemy(p,t,CombatTargeting.Context.ACQUIRE)))){
            if(SnowArmor.full(t))continue;
            if(t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100,1),p))applied=true;
        }
        if(applied){data.putLong("CD_snow_armor_aura",now+200);FrostVisuals.ring(p.serverLevel(),p.position(),4,.15,32);}
    }
    private SnowArmorEffects(){}
}
