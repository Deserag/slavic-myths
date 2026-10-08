package org.slavicmyths.rpg.classes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import org.slavicmyths.rpg.*;
import org.slavicmyths.depth.ThrownNet;

/** Server-only intent execution. No client supplied targets, ranks or damage values. */
public final class ClassRuntime {
    public static LivingEntity target(ServerPlayer p,double reach){
        Vec3 eye=p.getEyePosition(),end=eye.add(p.getLookAngle().scale(reach));
        var block=p.level().clip(new ClipContext(eye,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p));
        double limit=eye.distanceToSqr(block.getLocation());LivingEntity result=null;
        for(var e:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().expandTowards(p.getLookAngle().scale(reach)).inflate(1),e->Abilities.canHit(p,e))){
            var hit=e.getBoundingBox().inflate(.1).clip(eye,end);if(hit.isPresent()&&eye.distanceToSqr(hit.get())<limit){limit=eye.distanceToSqr(hit.get());result=e;}
        }return result;
    }
    public static boolean activate(ServerPlayer p,int slot){
        if(slot<0||slot>2||!p.isAlive()||p.isSpectator()||p.isSleeping())return false;
        String id=ClassState.data(p).getString("Active"+slot);var s=ClassDefinitions.SKILLS.get(id);int rank=ClassState.rank(p,id);
        if(s==null||s.kind()!=ClassDefinitions.Kind.ACTIVE||rank==0||ClassState.remaining(p,id)>0)return false;
        String weapon=Runes.category(p.getMainHandItem());
        if(s.base().equals("druzhinnik")&&!id.equals("shield_ram")&&!java.util.Set.of("sword","dagger","spear","heavy").contains(weapon))return false;
        if(id.equals("shield_ram")&&!(p.getMainHandItem().getItem() instanceof ShieldItem)&&!(p.getOffhandItem().getItem() instanceof ShieldItem))return false;
        var state=ClassState.data(p);double power=ClassBalance.power(state,id,rank),range=ClassBalance.range(state,id,rank);int duration=ClassBalance.duration(state,id,rank);LivingEntity t;
        switch(id){
            case "lunge","dash" -> {
                if(!p.onGround()||p.isPassenger()||p.isSleeping())return false;
                t=id.equals("lunge")?target(p,range+1):null;
                var threats=p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(3),mob->mob.getTarget()==p&&mob.swinging&&Abilities.canHit(p,mob));
                Vec3 direction=new Vec3(p.getLookAngle().x,0,p.getLookAngle().z).normalize();Vec3 before=p.position();
                p.move(MoverType.SELF,direction.scale(range));p.connection.teleport(p.getX(),p.getY(),p.getZ(),p.getYRot(),p.getXRot());
                if(p.position().distanceToSqr(before)<.04)return false;
                if(id.equals("dash")&&threats.stream().anyMatch(mob->p.distanceToSqr(mob)>9||!mob.hasLineOfSight(p)))ClassEvents.opportunity(p);
                if(id.equals("lunge")&&t!=null&&p.distanceToSqr(t)<=9&&Abilities.canHit(p,t)){p.getPersistentData().putDouble("ClassAttackBonus",power);p.attack(t);p.getPersistentData().remove("ClassAttackBonus");}
            }
            case "trip","shield_ram","binding_sign","hex","slumber" -> {
                t=target(p,range);if(t==null)return false;
                boolean resistant=ClassControl.resistant(t);
                int ticks=resistant?Math.max(10,duration/2):duration;
                if(id.equals("hex")){t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,ticks,(int)power));t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,ticks,0));}
                else {if(!ClassControl.apply(t,duration,id.equals("slumber")))return false;if(id.equals("trip"))p.attack(t);if(id.equals("shield_ram")){t.hurt(p.damageSources().playerAttack(p),2);t.knockback(power,p.getX()-t.getX(),p.getZ()-t.getZ());if(ClassBalance.evolution(state,"bulwark"))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,ClassBalance.BULWARK_DEFENSE_TICKS,0));}}
            }
            case "flurry" -> {var speed=p.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED);speed.removeModifier(ClassState.FLURRY);speed.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(ClassState.FLURRY,power,net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));p.getPersistentData().putLong("ClassFlurryUntil",ClassState.now(p)+duration);}
            case "aerial_strike" -> {if(!p.onGround()||p.isPassenger()||p.isSleeping())return false;p.setDeltaMovement(p.getDeltaMovement().add(0,.6+rank*.08,0));p.hurtMarked=true;p.getPersistentData().putLong("ClassAirUntil",ClassState.now(p)+duration);p.getPersistentData().putBoolean("ClassAirborne",false);p.getPersistentData().remove("ClassAirHit");}
            case "ward" -> {float previous=p.getAbsorptionAmount();p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,duration,Math.max(0,(int)Math.ceil(power/4)-1)));p.setAbsorptionAmount(Math.max(previous,(float)power));}
            case "cleanse" -> {
                int removed=0;for(var effect:java.util.List.copyOf(p.getActiveEffects())){if(!effect.getEffect().value().isBeneficial()&&!effect.getEffect().is(ClassControl.PROTECTED)&&removed<(int)power){p.removeEffect(effect.getEffect());removed++;}}if(removed==0)return false;
            }
            case "net" -> {var net=new ThrownNet(p.level(),p);net.getPersistentData().putInt("ClassNetDuration",duration);net.getPersistentData().putDouble("ClassNetRange",s.range(rank));net.getPersistentData().putDouble("ClassNetX",p.getX());net.getPersistentData().putDouble("ClassNetY",p.getEyeY());net.getPersistentData().putDouble("ClassNetZ",p.getZ());net.shootFromRotation(p,p.getXRot(),p.getYRot(),0,1.2F,0);p.level().addFreshEntity(net);}
            case "smoke" -> {p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,duration,0));boolean escaped=false;for(var mob:p.level().getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(s.range(rank)),e->e.getTarget()==p&&p.hasLineOfSight(e)&&!ClassControl.resistant(e))){if(mob.swinging&&p.distanceToSqr(mob)<=9)escaped=true;mob.setTarget(null);ClassControl.apply(mob,Math.min(duration,40),true);}if(escaped)ClassEvents.opportunity(p);p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE,p.getX(),p.getY()+.6,p.getZ(),24,1,.4,1,.01);}
            case "keen_eye","dirty_strike" -> {if(id.equals("keen_eye")&&ClassEvents.markedTarget(p,s.range(rank))==null)return false;p.getPersistentData().putLong("ClassArmed_"+id,ClassState.now(p)+duration);}
            default -> {return false;}
        }
        ClassState.cooldown(p,id,ClassBalance.cooldown(state,id,rank));p.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);p.serverLevel().sendParticles(s.base().equals("vedun")?net.minecraft.core.particles.ParticleTypes.ENCHANT:net.minecraft.core.particles.ParticleTypes.CRIT,p.getX(),p.getY()+.5,p.getZ(),4,.2,.2,.2,.01);p.level().playSound(null,p.blockPosition(),s.base().equals("vedun")?net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE:net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,net.minecraft.sounds.SoundSource.PLAYERS,.35F,1F);ClassState.sync(p);return true;
    }
    private ClassRuntime(){}
}
