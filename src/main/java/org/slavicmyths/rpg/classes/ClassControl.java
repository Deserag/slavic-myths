package org.slavicmyths.rpg.classes;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import org.slavicmyths.registry.ModEffects;

public final class ClassControl {
    public static final TagKey<EntityType<?>> RESISTANT=TagKey.create(Registries.ENTITY_TYPE,ClassState.id("class_control_resistant"));
    public static final TagKey<EntityType<?>> MYTHICAL=TagKey.create(Registries.ENTITY_TYPE,ClassState.id("class_mythical"));
    public static final TagKey<MobEffect> PROTECTED=TagKey.create(Registries.MOB_EFFECT,ClassState.id("class_protected_effects"));
    public static boolean resistant(LivingEntity e){return e instanceof Player||e.getType().is(RESISTANT)||e.getMaxHealth()>=100||!e.canChangeDimensions(e.level(),e.level());}
    public static boolean apply(LivingEntity e,int ticks,boolean sleep){
        long now=e.getServer()==null?e.level().getGameTime():e.getServer().overworld().getGameTime();
        if(e.getPersistentData().getLong("ClassControlUntil")>now)return false;
        boolean soft=resistant(e);int duration=soft?Math.max(10,ticks/2):ticks;
        e.addEffect(new MobEffectInstance(soft?MobEffects.MOVEMENT_SLOWDOWN:ModEffects.CLASS_ROOT,duration,soft?0:1));
        if(sleep){e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,duration,0));if(!soft&&e instanceof Mob m)m.setTarget(null);}
        e.getPersistentData().putLong("ClassControlUntil",now+duration+40);return true;
    }
    public static final class RootEffect extends MobEffect {
        public RootEffect(){super(MobEffectCategory.HARMFUL,0x8EAF87);addAttributeModifier(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED,ClassState.id("class_root_speed"),-.98,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);}
        @Override public boolean shouldApplyEffectTickThisTick(int duration,int amplifier){return true;}
        @Override public boolean applyEffectTick(LivingEntity e,int amplifier){if(!e.level().isClientSide&&!resistant(e)){var v=e.getDeltaMovement();e.setDeltaMovement(0,Math.min(0,v.y),0);if(e instanceof Mob m)m.getNavigation().stop();}return true;}
    }
    private ClassControl(){}
}
