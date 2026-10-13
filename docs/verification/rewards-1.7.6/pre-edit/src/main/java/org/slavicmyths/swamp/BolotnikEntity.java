package org.slavicmyths.swamp;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.*;
import org.slavicmyths.water.WaterSpirit;
/** Slow bog ambusher; its limited control power is applied only after a melee hit. */
public final class BolotnikEntity extends WaterSpirit {
 public BolotnikEntity(EntityType<? extends BolotnikEntity> type,Level level){super(type,level);}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,48).add(Attributes.MOVEMENT_SPEED,.20).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.ARMOR,4).add(Attributes.KNOCKBACK_RESISTANCE,.4).add(Attributes.FOLLOW_RANGE,24);}
 @Override protected void think(){if(home==null)home=blockPosition();if(getTarget()!=null){state(3);return;}Player p=nearby(18);if(p!=null&&distanceToSqr(p)<100&&hasLineOfSight(p)){provoke(p);state(3);}else{state(0);wander();}}
 @Override public boolean doHurtTarget(Entity entity){boolean hit=super.doHurtTarget(entity);if(hit&&entity instanceof LivingEntity living&&level().getGameTime()>=nextPower){living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,0));nextPower=level().getGameTime()+120;}return hit;}
 @Override protected SoundEvent getAmbientSound(){return SoundEvents.MUD_BREAK;}
 @Override protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source){return SoundEvents.MUD_HIT;}
 @Override protected SoundEvent getDeathSound(){return SoundEvents.MUD_BREAK;}
}
