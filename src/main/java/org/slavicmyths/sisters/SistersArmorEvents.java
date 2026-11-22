package org.slavicmyths.sisters;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Server-side sister set effects, flight lifecycle, and bounded combat aura. */
@EventBusSubscriber(modid="slavicmyths")
public final class SistersArmorEvents {
    private static final String GRANTED="Sisters25FlightGranted",ACTIVE="Sisters25AuraUntil",COOLDOWN="Sisters25AuraCooldown";
    private static final long AURA_DURATION=400, AURA_COOLDOWN=1200;
    public static boolean active(ServerPlayer player) {return SistersArmor.full(player)&&player.getPersistentData().getLong(ACTIVE)>player.serverLevel().getGameTime();}

    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        boolean full=SistersArmor.full(player),granted=player.getPersistentData().getBoolean(GRANTED);
        if(full&&player.isAlive()&&!player.isSpectator()) {
            if(!player.isCreative()&&(!player.getAbilities().mayfly||runeFlight(player))) {
                player.getAbilities().mayfly=true;player.getPersistentData().putBoolean(GRANTED,true);player.onUpdateAbilities();
            }
            speed(player,true);
            if(active(player)&&player.tickCount%20==0)auraPulse(player);
        } else {
            speed(player,false);
            if(granted) {
                player.getPersistentData().remove(GRANTED);
                if(!player.isCreative()&&!player.isSpectator()) {
                    player.getAbilities().mayfly=runeFlight(player);if(!player.getAbilities().mayfly)player.getAbilities().flying=false;
                    player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,100,0,false,false,true));
                    player.onUpdateAbilities();
                }
            }
            player.getPersistentData().remove(ACTIVE);
        }
        if(player.tickCount%7==0&&player.getItemBySlot(EquipmentSlot.LEGS).is(SistersArmor.AGNEYA_LEGGINGS.get())&&player.isAlive()) {
            // A small supply of satiety is earned continuously while the leggings are worn.
            if(player.getFoodData().getFoodLevel()<20&&player.tickCount%35==0)player.getFoodData().eat(1,.35F);
        }

    }

    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public static void resist(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable event) {
        if(!(event.getEntity() instanceof ServerPlayer player)
                ||event.getResult()==net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result.DO_NOT_APPLY
                ||event.getEffectInstance().getEffect().value().isBeneficial()
                ||!player.getItemBySlot(EquipmentSlot.CHEST).is(SistersArmor.GNETEYA_CHESTPLATE.get()))return;
        float chance=active(player)?.40F:.30F;
        if(player.getRandom().nextFloat()<chance)
            event.setResult(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }

    public static boolean runeFlight(ServerPlayer p){return p.isAlive()&&!p.isSpectator()&&org.slavicmyths.rpg.RareRunes.equipped(p,"flight")&&p.getPersistentData().getLong("RuneFlightUntil")>org.slavicmyths.rpg.RareRuneRuntime.now(p);}
    public static void activateAura(ServerPlayer player){
        if(!player.isAlive()||player.isSpectator()||player.containerMenu!=player.inventoryMenu||!player.isShiftKeyDown()
                ||!player.getMainHandItem().isEmpty()||!SistersArmor.full(player))return;
        var state=player.getPersistentData();long tick=player.serverLevel().getGameTime();
        if(state.getLong("Sisters25RequestUntil")>tick)return;
        state.putLong("Sisters25RequestUntil",tick+4);
        var saved=org.slavicmyths.rpg.PathData.data(player);
        if(!saved.contains(COOLDOWN))saved.putLong(COOLDOWN,state.getLong(COOLDOWN));
        long now=player.serverLevel().getGameTime();
        if(active(player)) {
            player.getPersistentData().remove(ACTIVE);
            player.displayClientMessage(Component.translatable("armor.slavicmyths.sisters.aura_off"),true);
        } else if(now>=saved.getLong(COOLDOWN)) {
            player.getPersistentData().putLong(ACTIVE,now+AURA_DURATION);
            saved.putLong(COOLDOWN,now+AURA_COOLDOWN);
            player.displayClientMessage(Component.translatable("armor.slavicmyths.sisters.aura_on"),true);
            auraPulse(player);
        } else {
            player.displayClientMessage(Component.translatable("armor.slavicmyths.sisters.aura_cooldown",(saved.getLong(COOLDOWN)-now+19)/20),true);
        }
    }

    private static void speed(ServerPlayer player,boolean enabled){
        var attribute=player.getAttribute(Attributes.MOVEMENT_SPEED);if(attribute==null)return;
        var id=net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","sisters_set_speed");
        var potion=player.getEffect(MobEffects.MOVEMENT_SPEED);
        // Supplement weaker potions up to Speed III/IV, never replace or delete an external potion.
        double amount=SistersArmorRules.speedSupplement(potion==null?-1:potion.getAmplifier(),enabled,active(player));
        var previous=attribute.getModifier(id);
        if(previous!=null&&Math.abs(previous.amount()-amount)<1e-8)return;
        attribute.removeModifier(id);
        if(amount>0)attribute.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(id,amount,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
    @SubscribeEvent public static void equipment(net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent event){
        if(event.getEntity() instanceof ServerPlayer p&&!SistersArmor.full(p))speed(p,false);
    }

    private static void auraPulse(ServerPlayer player) {
        var world=player.serverLevel();
        for(int i=0;i<20;i++){
            double a=i*2*Math.PI/20, x=player.getX()+3.8*Math.cos(a),z=player.getZ()+3.8*Math.sin(a);
            world.sendParticles(i%2==0?ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.ENCHANT,x,player.getY()+.8,z,1,.1,.3,.1,.01);
        }
        var targets=world.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(4),
            target->target!=player&&target.isAlive()&&target.distanceToSqr(player)<=16
                &&!player.isAlliedTo(target)&&org.slavicmyths.rpg.Abilities.canHit(player,target));
        targets.sort(java.util.Comparator.comparingDouble(target->target.distanceToSqr(player)));
        for(LivingEntity target:targets.stream().limit(6).toList()) {
            target.hurt(player.damageSources().indirectMagic(player,player),3F);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,45,0),player);
        }
    }

    @SubscribeEvent public static void critical(LivingDamageEvent.Pre event) {
        if(event.getNewDamage()<=0||!(event.getSource().getEntity() instanceof ServerPlayer player)
                ||!player.getItemBySlot(EquipmentSlot.HEAD).is(SistersArmor.LEDEYA_HELMET.get()))return;
        if(!org.slavicmyths.rpg.Abilities.canHit(player,event.getEntity())||player.getRandom().nextFloat()>=.25F)return;
        event.setNewDamage(event.getNewDamage()*1.5F);
        player.serverLevel().sendParticles(ParticleTypes.CRIT,event.getEntity().getX(),event.getEntity().getEyeY(),event.getEntity().getZ(),12,.25,.35,.25,.05);
    }

    private SistersArmorEvents() {}
}
