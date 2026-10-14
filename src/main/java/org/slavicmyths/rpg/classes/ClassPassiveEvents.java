package org.slavicmyths.rpg.classes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;

@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ClassPassiveEvents {
    public static final net.minecraft.resources.ResourceLocation INSTINCT=ClassState.id("class_survival_instinct");
    public static void block(ServerPlayer p){int rank=ClassState.passive(p,"iron_will");if(rank<=0||!ClassXpService.eligible(p)||ClassState.remaining(p,"iron_will")>0)return;p.getPersistentData().putLong("ClassIronUntil",ClassState.now(p)+40);ClassState.cooldown(p,"iron_will",400);ClassState.sync(p);}
    @SubscribeEvent public static void effect(MobEffectEvent.Added e){
        if(!(e.getEntity() instanceof ServerPlayer p))return;int rank=ClassState.passive(p,"spiritual_fortitude");var effect=e.getEffectInstance();
        // Whitelist prevents shortening mod/story debuffs even when a pack forgot its protected tag.
        if(rank<=0||effect==null||effect.isInfiniteDuration()||effect.getEffect().is(ClassControl.PROTECTED))return;
        if(java.util.Set.of(MobEffects.POISON,MobEffects.WEAKNESS,MobEffects.MOVEMENT_SLOWDOWN,MobEffects.BLINDNESS,MobEffects.DIG_SLOWDOWN,MobEffects.HUNGER,MobEffects.CONFUSION).contains(effect.getEffect()))effect.duration=Math.max(1,(int)Math.ceil(effect.getDuration()*(1-ClassDefinitions.SKILLS.get("spiritual_fortitude").power(rank))));
    }
    @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){
        if(e.getAmount()<=0)return;
        if(e.getEntity() instanceof ServerPlayer p&&p.getPersistentData().getLong("ClassIronUntil")>ClassState.now(p)&&ClassState.passive(p,"iron_will")>0&&!e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)&&!e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_EFFECTS)){
            e.setAmount((float)(e.getAmount()*(1-ClassDefinitions.SKILLS.get("iron_will").power(ClassState.passive(p,"iron_will")))));p.getPersistentData().remove("ClassIronUntil");
        }
    }
    public static double ambush(ServerPlayer p,net.minecraft.world.entity.LivingEntity target){
        if(!ClassXpService.hostile(p,target))return 0;
        String key="ClassAmbush:"+p.getUUID();int rank=ClassState.passive(p,"ambush_master");
        if(rank<=0||target.getPersistentData().getBoolean(key)||target instanceof Mob mob&&mob.getTarget()==p||target.getLastHurtByMob()==p)return 0;
        target.getPersistentData().putBoolean(key,true);return ClassDefinitions.SKILLS.get("ambush_master").power(rank);
    }
    @SubscribeEvent public static void damaged(LivingDamageEvent.Post e){
        if(!(e.getEntity() instanceof ServerPlayer p)||e.getNewDamage()<6||!p.isAlive())return;int rank=ClassState.passive(p,"survival_instinct");if(rank<=0||ClassState.remaining(p,"survival_instinct")>0)return;
        var speed=p.getAttribute(Attributes.MOVEMENT_SPEED);speed.removeModifier(INSTINCT);speed.addTransientModifier(new AttributeModifier(INSTINCT,ClassDefinitions.SKILLS.get("survival_instinct").power(rank),AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));p.getPersistentData().putLong("ClassInstinctUntil",ClassState.now(p)+60);ClassState.cooldown(p,"survival_instinct",1200);ClassState.sync(p);
    }
    public static void tick(ServerPlayer p){if(p.getPersistentData().getLong("ClassInstinctUntil")<=ClassState.now(p)||ClassState.passive(p,"survival_instinct")==0)p.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(INSTINCT);}
    private ClassPassiveEvents(){}
}
