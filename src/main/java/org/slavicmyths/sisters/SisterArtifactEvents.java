package org.slavicmyths.sisters;

import java.util.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid="slavicmyths")
public final class SisterArtifactEvents {
    private static final ResourceLocation HEALTH=ResourceLocation.fromNamespaceAndPath("slavicmyths","sisters_necklace_health");
    private static final ResourceLocation SPEED=ResourceLocation.fromNamespaceAndPath("slavicmyths","sisters_brooch_speed");
    private static final ResourceLocation WIND=ResourceLocation.fromNamespaceAndPath("slavicmyths","sisters_tryaseya_wind");
    private static final ResourceLocation KNOCK=ResourceLocation.fromNamespaceAndPath("slavicmyths","sisters_lomeya_knockback");
    private static boolean power(ServerPlayer p,String id){return p.isAlive()&&!p.isSpectator()&&SisterArtifacts.activePowers(p).contains(id);}
    private static boolean base(ServerPlayer p,String id){return p.isAlive()&&!p.isSpectator()&&SisterArtifacts.hasBase(p,id);}
    private static long now(ServerPlayer p){return p.server.overworld().getGameTime();}
    private static boolean trigger(ServerPlayer p,String key,int ticks){
        long time=now(p);String field="SistersArtifactCd_"+key;
        if(time<p.getPersistentData().getLong(field))return false;
        p.getPersistentData().putLong(field,time+Math.max(1,base(p,"sisters_ring")?(ticks*85/100):ticks));
        return true;
    }
    private static void attribute(ServerPlayer player,net.minecraft.core.Holder<Attribute> type,ResourceLocation id,double amount,boolean enabled){
        AttributeInstance value=player.getAttribute(type);if(value==null)return;
        if(!enabled){if(value.getModifier(id)!=null)value.removeModifier(id);return;}
        if(value.getModifier(id)==null)value.addTransientModifier(new AttributeModifier(id,amount,AttributeModifier.Operation.ADD_VALUE));
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer p))return;
        attribute(p,Attributes.MAX_HEALTH,HEALTH,4,base(p,"sisters_necklace"));
        attribute(p,Attributes.MOVEMENT_SPEED,SPEED,.01,base(p,"sisters_brooch"));
        attribute(p,Attributes.MOVEMENT_SPEED,WIND,.035,power(p,"tryaseya")&&now(p)<p.getPersistentData().getLong("SistersTryaseyaBoostUntil"));
        attribute(p,Attributes.KNOCKBACK_RESISTANCE,KNOCK,.25,power(p,"lomeya"));
        if(p.getHealth()>p.getMaxHealth())p.setHealth(p.getMaxHealth());
        if(p.tickCount%20==0&&power(p,"ledeya"))p.setTicksFrozen(0);
    }
    @SubscribeEvent public static void onDamage(LivingDamageEvent.Pre event){
        if(!(event.getEntity() instanceof ServerPlayer p)||event.getNewDamage()<=0)return;
        var source=event.getSource();
        if(source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))return;
        float damage=event.getNewDamage();
        boolean fire=source.is(DamageTypeTags.IS_FIRE),magic=source.is(DamageTypes.MAGIC)||source.is(DamageTypes.INDIRECT_MAGIC);
        boolean physical=!fire&&!magic&&!source.is(DamageTypeTags.BYPASSES_ARMOR);
        if(base(p,"sisters_belt")&&physical)damage*=.92F;
        if(base(p,"sisters_crown")&&magic)damage*=.90F;
        if(power(p,"lomeya")&&physical)damage*=.88F;
        if(power(p,"agneya")&&fire)damage*=.50F;
        if(power(p,"tryaseya")&&physical&&p.getRandom().nextFloat()<.20F&&trigger(p,"tryaseya",240)){
            damage*=.60F;
            p.getPersistentData().putLong("SistersTryaseyaBoostUntil",now(p)+60);
        }
        if(SisterArtifacts.complete(p)&&damage>=8&&trigger(p,"complete",3600))damage*=.40F;
        event.setNewDamage(Math.max(0,damage));
    }
    @SubscribeEvent public static void afterDamage(LivingDamageEvent.Post event){
        if(event.getNewDamage()<=0)return;
        LivingEntity victim=event.getEntity();
        if(victim instanceof ServerPlayer p&&power(p,"ledeya")&&event.getSource().getEntity() instanceof LivingEntity attacker
            &&!p.isAlliedTo(attacker)&&trigger(p,"ledeya",160)){
            attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1),p);
        }
        if(!(event.getSource().getEntity() instanceof ServerPlayer p)||event.getSource().getDirectEntity()!=p
            ||victim==p||p.isAlliedTo(victim)||!org.slavicmyths.rpg.Abilities.canHit(p,victim))return;
        if(victim instanceof net.minecraft.world.entity.player.Player&&!p.server.isPvpAllowed())return;
        if(power(p,"agneya")&&p.getRandom().nextFloat()<.22F&&trigger(p,"agneya",120))victim.setRemainingFireTicks(Math.max(victim.getRemainingFireTicks(),60));
        if(power(p,"neveya")&&p.getHealth()<p.getMaxHealth()&&p.getRandom().nextFloat()<.30F&&trigger(p,"neveya",160))
            p.heal(Math.min(2F,event.getNewDamage()*.25F));
    }
    @SubscribeEvent public static void shorten(MobEffectEvent.Added event){
        if(!(event.getEntity() instanceof ServerPlayer p)||!power(p,"gneteya")
            ||p.getPersistentData().getBoolean("SistersArtifactEffectGuard"))return;
        MobEffectInstance effect=event.getEffectInstance();
        if(effect.getEffect().value().isBeneficial()||effect.getDuration()<=1||effect.isInfiniteDuration())return;
        int duration=Math.max(1,(int)Math.ceil(effect.getDuration()*.70));
        if(duration>=effect.getDuration())return;
        p.getPersistentData().putBoolean("SistersArtifactEffectGuard",true);
        try{
            p.removeEffect(effect.getEffect());
            p.addEffect(new MobEffectInstance(effect.getEffect(),duration,effect.getAmplifier(),effect.isAmbient(),effect.isVisible(),effect.showIcon()));
        }finally{p.getPersistentData().remove("SistersArtifactEffectGuard");}
    }
    private SisterArtifactEvents(){}
}
