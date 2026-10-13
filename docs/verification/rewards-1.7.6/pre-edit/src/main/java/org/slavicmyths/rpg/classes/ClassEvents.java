package org.slavicmyths.rpg.classes;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.effect.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ClassEvents {
    private record PendingBlock(net.neoforged.neoforge.common.damagesource.DamageContainer damage,long tick,net.minecraft.resources.ResourceLocation dimension){}
    // At most one native damage reference per player, consumed on the next player event.
    // NeoForge 21.1.255 clamps event.getBlockedDamage() to the already reduced newDamage;
    // vanilla's later statistic therefore reports zero on a complete block.
    private static final java.util.Map<java.util.UUID,PendingBlock> BLOCKS=new java.util.HashMap<>();
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST) public static void shield(LivingShieldBlockEvent e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!e.getBlocked()||!e.getOriginalBlock()||e.getBlockedDamage()<=0||!p.isBlocking()||!p.isAlive()||p.invulnerableTime>10||p.isInvulnerableTo(e.getDamageSource())||ClassState.rank(p,"riposte")==0||ClassState.remaining(p,"riposte")>0)return;
        BLOCKS.putIfAbsent(p.getUUID(),new PendingBlock(e.getDamageContainer(),ClassState.now(p),p.level().dimension().location()));
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){BLOCKS.remove(e.getEntity().getUUID());}
    @SubscribeEvent public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent e){BLOCKS.clear();}
    @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p)restore(p);}
    @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){if(e.getEntity() instanceof ServerPlayer p)restore(p);}
    private static void restore(ServerPlayer p){p.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ClassState.FLURRY);for(String key:java.util.Set.copyOf(p.getPersistentData().getAllKeys()))if(key.startsWith("Class"))p.getPersistentData().remove(key);ClassState.restore(p);ClassState.sync(p);}
    @SubscribeEvent public static void tick(PlayerTickEvent.Post e){if(!(e.getEntity() instanceof ServerPlayer p))return;var d=p.getPersistentData();long now=ClassState.now(p);
        var block=BLOCKS.remove(p.getUUID());if(block!=null&&p.isAlive()&&now-block.tick()<=2&&block.dimension().equals(p.level().dimension().location())&&block.damage().getBlockedDamage()>0)confirmedBlock(p);
        if(d.contains("ClassFlurryUntil")&&d.getLong("ClassFlurryUntil")<=now){p.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ClassState.FLURRY);d.remove("ClassFlurryUntil");}
        if(p.getHealth()>=p.getMaxHealth()*.4)d.remove("ClassLowHealthLatched");
        if(d.contains("ClassAirUntil")){if(!p.onGround())d.putBoolean("ClassAirborne",true);if(p.onGround()&&d.getBoolean("ClassAirborne")&&d.getLong("ClassAirUntil")>now)land(p);if(d.getLong("ClassAirUntil")<=now||p.isPassenger()||p.getAbilities().flying){d.remove("ClassAirUntil");d.remove("ClassAirborne");d.remove("ClassAirHit");}}
        int sense=ClassState.passive(p,"spirit_sense");if(sense>0&&p.tickCount%40==0){double range=ClassDefinitions.SKILLS.get("spirit_sense").range(sense);var nearby=p.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,p.getBoundingBox().inflate(range),v->v.getType().is(ClassControl.MYTHICAL)&&p.hasLineOfSight(v)&&p.distanceToSqr(v)<=range*range);if(!nearby.isEmpty()){double distance=nearby.stream().mapToDouble(p::distanceToSqr).min().orElse(range*range);p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ENCHANT,p.getX(),p.getY()+1,p.getZ(),distance<16?5:2,.3,.3,.3,.01);}}
    }
    @SubscribeEvent public static void damage(LivingIncomingDamageEvent e){
        if(!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getAmount()<=0)return;
        boolean melee=e.getSource().getDirectEntity()==p&&"player".equals(e.getSource().getMsgId());boolean arrow=e.getSource().getDirectEntity() instanceof AbstractArrow;if(!melee&&!arrow)return;
        if(!org.slavicmyths.rpg.Abilities.canHit(p,e.getEntity()))return;var d=p.getPersistentData();double bonus=melee?d.getDouble("ClassAttackBonus"):0;
        int cold=ClassState.passive(p,"cold_blood");if(cold>0&&e.getEntity().getHealth()<e.getEntity().getMaxHealth()*.35)bonus+=ClassDefinitions.SKILLS.get("cold_blood").power(cold)*ClassState.branchMultiplier(p,"cold_blood");
        if(melee&&d.getBoolean("ClassAirborne")&&d.getLong("ClassAirUntil")>ClassState.now(p)&&!p.onGround()&&p.getDeltaMovement().y<0&&!d.getBoolean("ClassAirHit")){bonus+=ClassBalance.power(ClassState.data(p),"aerial_strike",ClassState.rank(p,"aerial_strike"));d.putBoolean("ClassAirHit",true);}
        if(melee&&d.getLong("ClassRiposteUntil")>ClassState.now(p)){e.setAmount(e.getAmount()+(float)ClassDefinitions.SKILLS.get("riposte").power(ClassState.rank(p,"riposte")));d.remove("ClassRiposteUntil");}
        if(melee&&d.getLong("ClassArmed_dirty_strike")>ClassState.now(p)&&e.getEntity().getLookAngle().dot(p.position().subtract(e.getEntity().position()).normalize())<.3){bonus+=ClassBalance.power(ClassState.data(p),"dirty_strike",ClassState.rank(p,"dirty_strike"));d.remove("ClassArmed_dirty_strike");}
        if(d.getLong("ClassOpportunityUntil")>ClassState.now(p)){bonus+=ClassDefinitions.SKILLS.get("opportunity").power(ClassState.rank(p,"opportunity"));d.remove("ClassOpportunityUntil");}
        e.setAmount((float)(e.getAmount()*(1+bonus)));
    }
    public static net.minecraft.world.entity.LivingEntity markedTarget(ServerPlayer p,double range){var d=p.getPersistentData();if(!d.hasUUID("ClassLastTarget")||ClassState.now(p)-d.getLong("ClassLastTargetTick")>200||!p.level().dimension().location().toString().equals(d.getString("ClassLastTargetDimension")))return null;var e=p.serverLevel().getEntity(d.getUUID("ClassLastTarget"));return e instanceof net.minecraft.world.entity.LivingEntity living&&p.distanceToSqr(living)<=range*range&&org.slavicmyths.rpg.Abilities.canHit(p,living)?living:null;}
    @SubscribeEvent public static void projectile(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){if(e.getLevel().isClientSide||!(e.getEntity() instanceof AbstractArrow a)||!(a.getOwner() instanceof ServerPlayer p)||a.getPersistentData().getBoolean("ClassChecked"))return;a.getPersistentData().putBoolean("ClassChecked",true);
        int passive=ClassState.passive(p,"steady_aim");if(passive>0&&a.isCritArrow())a.setBaseDamage(a.getBaseDamage()*(1+ClassDefinitions.SKILLS.get("steady_aim").power(passive)));
        var d=p.getPersistentData();int rank=ClassState.rank(p,"keen_eye");if(rank==0||d.getLong("ClassArmed_keen_eye")<=ClassState.now(p))return;var skill=ClassDefinitions.SKILLS.get("keen_eye");var target=markedTarget(p,skill.range(rank));if(target==null)return;
        var velocity=a.getDeltaMovement();if(velocity.lengthSqr()<.001)return;var direction=velocity.normalize();var aim=target.getEyePosition().subtract(a.position()).normalize();double angle=Math.acos(net.minecraft.util.Mth.clamp(direction.dot(aim),-1,1));double max=Math.toRadians(ClassBalance.power(ClassState.data(p),"keen_eye",rank));if(angle>Math.toRadians(60))return;var corrected=angle<=max?aim:direction.scale(Math.cos(max)).add(aim.subtract(direction.scale(Math.cos(angle))).normalize().scale(Math.sin(max))).normalize();a.setDeltaMovement(corrected.scale(velocity.length()));d.remove("ClassArmed_keen_eye");
    }
    @SubscribeEvent public static void jump(LivingEvent.LivingJumpEvent e){if(e.getEntity().hasEffect(org.slavicmyths.registry.ModEffects.CLASS_ROOT)&&!ClassControl.resistant(e.getEntity())){var v=e.getEntity().getDeltaMovement();e.getEntity().setDeltaMovement(v.x,0,v.z);}}
    @SubscribeEvent public static void food(LivingEntityUseItemEvent.Finish e){if(e.getEntity() instanceof ServerPlayer p&&(e.getItem().is(org.slavicmyths.registry.ModItems.BERRY_MORS.get())||e.getItem().is(org.slavicmyths.registry.ModItems.FOREST_MIX.get())))healFromHerb(p,1);}
    public static void healFromHerb(ServerPlayer p,double amount){int rank=ClassState.passive(p,"herbalism");if(rank>0&&amount>0)p.heal((float)(Math.min(amount,4)*ClassDefinitions.SKILLS.get("herbalism").power(rank)*ClassState.branchMultiplier(p,"herbalism")));}
    @SubscribeEvent public static void fall(LivingFallEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.getPersistentData().getBoolean("ClassAirborne")&&p.getPersistentData().getLong("ClassAirUntil")>ClassState.now(p)){e.setDamageMultiplier(0);land(p);}}
    private static void land(ServerPlayer p){var d=p.getPersistentData();int rank=ClassState.rank(p,"aerial_strike");var s=ClassDefinitions.SKILLS.get("aerial_strike");if(rank>0&&!d.getBoolean("ClassAirHit")){int hits=0;for(var target:p.level().getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,p.getBoundingBox().inflate(s.range(rank)),v->org.slavicmyths.rpg.Abilities.canHit(p,v)&&p.distanceToSqr(v)<=s.range(rank)*s.range(rank))){if(hits++>=6)break;target.hurt(p.damageSources().playerAttack(p),(float)(2*(1+ClassBalance.power(ClassState.data(p),"aerial_strike",rank))));}}p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,p.getX(),p.getY()+.1,p.getZ(),12,1,.1,1,.02);d.remove("ClassAirUntil");d.remove("ClassAirborne");d.remove("ClassAirHit");}
    public static void opportunity(ServerPlayer p){int rank=ClassState.rank(p,"opportunity");if(rank==0||ClassState.remaining(p,"opportunity")>0)return;var s=ClassDefinitions.SKILLS.get("opportunity");p.getPersistentData().putLong("ClassOpportunityUntil",ClassState.now(p)+s.duration(rank));ClassState.cooldown(p,"opportunity",s.cooldown(rank));}
    @SubscribeEvent public static void afterDamage(LivingDamageEvent.Post e){if(e.getNewDamage()<=0)return;
        if(e.getEntity() instanceof ServerPlayer p&&!p.getPersistentData().getBoolean("ClassLowHealthLatched")&&p.isAlive()&&p.getHealth()<p.getMaxHealth()*.3&&p.getHealth()+e.getNewDamage()>=p.getMaxHealth()*.3){p.getPersistentData().putBoolean("ClassLowHealthLatched",true);for(String id:java.util.List.of("second_wind","last_rite")){int rank=ClassState.rank(p,id);if(rank>0&&ClassState.remaining(p,id)==0){var s=ClassDefinitions.SKILLS.get(id);float before=p.getAbsorptionAmount();double power=ClassBalance.power(ClassState.data(p),id,rank);p.addEffect(new MobEffectInstance(id.equals("second_wind")?MobEffects.DAMAGE_RESISTANCE:MobEffects.ABSORPTION,ClassBalance.duration(ClassState.data(p),id,rank),id.equals("last_rite")?Math.max(0,(int)Math.ceil(power/4)-1):0));if(id.equals("last_rite"))p.setAbsorptionAmount(Math.max(before,(float)power));ClassState.cooldown(p,id,s.cooldown(rank));ClassState.sync(p);}}}
        if(e.getSource().getEntity() instanceof ServerPlayer p&&e.getSource().getDirectEntity() instanceof AbstractArrow&&org.slavicmyths.rpg.Abilities.canAffect(p,e.getEntity())){p.getPersistentData().putUUID("ClassLastTarget",e.getEntity().getUUID());p.getPersistentData().putString("ClassLastTargetDimension",p.level().dimension().location().toString());p.getPersistentData().putLong("ClassLastTargetTick",ClassState.now(p));int rank=ClassState.rank(p,"hunters_fervor");if(rank>0&&ClassState.remaining(p,"hunters_fervor")==0){var s=ClassDefinitions.SKILLS.get("hunters_fervor");p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,s.duration(rank),0));ClassState.cooldown(p,"hunters_fervor",s.cooldown(rank));ClassState.sync(p);}}
    }
    public static void confirmedBlock(ServerPlayer p){int rank=ClassState.rank(p,"riposte");if(rank==0||ClassState.remaining(p,"riposte")>0)return;var s=ClassDefinitions.SKILLS.get("riposte");p.getPersistentData().putLong("ClassRiposteUntil",ClassState.now(p)+s.duration(rank));ClassState.cooldown(p,"riposte",s.cooldown(rank));ClassState.sync(p);}
    private ClassEvents(){}
}
