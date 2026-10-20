package org.slavicmyths.rpg.classes;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.*;
import org.slavicmyths.combat.CombatTargeting;

/** One finite action per owner. Only the owner's existing tick drives active work. */
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class ClassUltimateRuntime {
    private static final Map<UUID,Action> ACTIVE=new HashMap<>();
    private static final Set<UUID> SECONDARY=new HashSet<>();
    private static final class Action {
        String skill,dimension;long until,start;Vec3 origin,last,direction;UUID target,decoy;int hits,charges;float pool,bleedDealt;boolean airborne,spent;
        final Map<UUID,UUID> previous=new HashMap<>();final Set<UUID> used=new HashSet<>(),members=new HashSet<>();
        Action(ServerPlayer p,ClassUltimates.Definition u){skill=u.skill();dimension=p.level().dimension().location().toString();start=ClassState.now(p);until=start+Math.max(20,u.duration());origin=p.position();last=origin;direction=p.getLookAngle();pool=skill.equals("living_wall")?20:24;}
    }
    public static boolean hostile(ServerPlayer p,LivingEntity t){return !(t instanceof Player)&&ClassXpService.hostile(p,t)&&CombatTargeting.canAttack(p,t)&&p.hasLineOfSight(t);}
    public static boolean friendly(ServerPlayer p,LivingEntity t){return t==p||t.isAlliedTo(p)||t instanceof Villager villager&&!org.slavicmyths.military.GuardBehavior.hostile(villager,p)||t instanceof TamableAnimal animal&&p.getUUID().equals(animal.getOwnerUUID());}
    /** Called only at the guard's existing 40-tick decision cadence. */
    public static LivingEntity priority(Villager guard){for(var p:guard.level().getEntitiesOfClass(ServerPlayer.class,guard.getBoundingBox().inflate(10))){var a=ACTIVE.get(p.getUUID());if(a==null||!a.skill.equals("banner_of_druzhina")||a.until<=ClassState.now(p)||!a.members.contains(guard.getUUID())||a.target==null||guard.position().distanceToSqr(a.origin)>100)continue;var target=p.serverLevel().getEntity(a.target);if(target instanceof LivingEntity living&&hostile(p,living)&&org.slavicmyths.military.GuardBehavior.hostile(guard,living))return living;}return null;}
    private static List<LivingEntity> enemies(ServerPlayer p,Vec3 at,double radius,int limit){return p.level().getEntitiesOfClass(LivingEntity.class,new AABB(at,at).inflate(radius),t->hostile(p,t)&&t.position().distanceToSqr(at)<=radius*radius).stream().sorted(Comparator.comparingDouble(t->t.position().distanceToSqr(at))).limit(limit).toList();}
    private static LivingEntity target(ServerPlayer p,double range){var t=ClassRuntime.target(p,range);return t!=null&&hostile(p,t)?t:null;}
    private static void feedback(ServerPlayer p,String reason){p.displayClientMessage(Component.translatable("classes.slavicmyths.ultimate_"+reason),true);}
    public static boolean activate(ServerPlayer p){
        var d=ClassState.data(p);var u=ClassUltimates.selected(d);
        if(u==null||d.getInt("Level")<40||!ClassTrialService.completed(d,u.parent())){feedback(p,"locked");return false;}
        if(!p.isAlive()||p.isSpectator()||p.isSleeping()||p.isPassenger()||p.isFallFlying()||p.getAbilities().flying){feedback(p,"state");return false;}
        var current=ACTIVE.get(p.getUUID());
        if(current!=null&&current.skill.equals("piercing_breakthrough")&&current.until>ClassState.now(p)&&current.hits<2){if(dash(p,current)){current.hits++;return true;}feedback(p,"position");return false;}
        if(ClassState.remaining(p,u.skill())>0){feedback(p,"cooldown");return false;}
        var a=new Action(p,u);LivingEntity t;
        switch(u.skill()){
            case "earth_thunder"->{if(!p.onGround()||enemies(p,p.position(),8,1).isEmpty()){feedback(p,"position");return false;}p.setDeltaMovement(p.getDeltaMovement().add(0,.8,0));p.hurtMarked=true;}
            case "unyielding_flurry"->{if(!java.util.Set.of("sword","dagger","spear","heavy").contains(org.slavicmyths.rpg.Runes.category(p.getMainHandItem()))){feedback(p,"weapon");return false;}}
            case "living_wall"->{if(!p.isBlocking()||!(p.getUseItem().getItem() instanceof ShieldItem)){feedback(p,"shield");return false;}}
            case "banner_of_druzhina"->{t=target(p,10);a.target=t==null?null:t.getUUID();a.members.add(p.getUUID());for(var ally:p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(10),v->v!=p&&v.isAlive()&&friendly(p,v)&&p.distanceToSqr(v)<=100&&p.hasLineOfSight(v)).stream().sorted(Comparator.comparingDouble(p::distanceToSqr)).limit(5).toList()){a.members.add(ally.getUUID());if(ally instanceof Mob mob&&!(ally instanceof Villager)&&t!=null&&CombatTargeting.canAttack(mob,t)){a.previous.put(mob.getUUID(),mob.getTarget()==null?new UUID(0,0):mob.getTarget().getUUID());mob.setTarget(t);}}}
            case "sacred_circle"->{}
            case "living_water"->{boolean changed=false;var allies=p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(8),v->v.isAlive()&&friendly(p,v)&&p.distanceToSqr(v)<=64&&p.hasLineOfSight(v)).stream().sorted(Comparator.comparingDouble(p::distanceToSqr)).limit(5).toList();for(var ally:allies){float before=ally.getHealth();ally.heal(6);changed|=ally.getHealth()>before;for(var effect:List.copyOf(ally.getActiveEffects()))if(cleanseable(effect)){changed|=ally.removeEffect(effect.getEffect());break;}}if(!changed){feedback(p,"target");return false;}}
            case "plague_seal","blood_mark","vulnerable_point"->{t=target(p,u.range());if(t==null||u.skill().equals("blood_mark")&&(t.getType().is(net.minecraft.tags.EntityTypeTags.UNDEAD)||t.getType().is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,ClassState.id("class_bloodless"))))){feedback(p,"target");return false;}a.target=t.getUUID();if(u.skill().equals("vulnerable_point"))p.serverLevel().sendParticles(ParticleTypes.CRIT,t.getX(),t.getEyeY(),t.getZ(),12,.2,.2,.2,0);}
            case "shadow_pact"->{t=target(p,12);if(t==null){feedback(p,"target");return false;}if(p.getHealth()<8){feedback(p,"health");return false;}a.target=t.getUUID();a.last=p.getEyePosition();a.direction=t.getBoundingBox().getCenter().subtract(a.last).normalize();a.until=a.start+20;
                // Resource expenditure, not a damage attack: exactly 4 HP, no death or armour bypass attack.
                p.setHealth(p.getHealth()-4);}
            case "piercing_breakthrough"->{if(!dash(p,a)){feedback(p,"position");return false;}a.hits=1;}
            case "false_ambush"->{var mobs=enemies(p,p.position(),8,3).stream().filter(v->v instanceof Mob m&&m.getTarget()==p&&!CombatTargeting.controlResistant(v)&&CombatTargeting.canControl(p,v)).toList();if(mobs.isEmpty()){feedback(p,"target");return false;}
                var stand=new ArmorStand(p.level(),p.getX(),p.getY(),p.getZ());stand.setInvisible(true);stand.setInvulnerable(true);stand.setNoGravity(true);stand.setSilent(true);stand.setNoBasePlate(true);stand.getPersistentData().putBoolean("ClassDecoy",true);if(!p.level().addFreshEntity(stand))return false;a.decoy=stand.getUUID();for(var mob:mobs){var m=(Mob)mob;a.previous.put(m.getUUID(),p.getUUID());m.setTarget(stand);}}
            default->{return false;}
        }
        clear(p);ACTIVE.put(p.getUUID(),a);ClassState.cooldown(p,u.skill(),u.cooldown());d.putString("Ultimate",u.skill());ClassState.sync(p);visual(p,a.origin,u.skill());return true;
    }
    private static boolean cleanseable(MobEffectInstance e){return !e.getEffect().is(ClassControl.PROTECTED)&&Set.of(MobEffects.POISON,MobEffects.WEAKNESS,MobEffects.MOVEMENT_SLOWDOWN,MobEffects.BLINDNESS,MobEffects.HUNGER).contains(e.getEffect());}
    private static void visual(ServerPlayer p,Vec3 at,String skill){var particle=switch(skill){case "earth_thunder"->ParticleTypes.CLOUD;case "living_wall","sacred_circle"->ParticleTypes.ENCHANT;case "living_water"->ParticleTypes.HEART;case "plague_seal"->ParticleTypes.SPORE_BLOSSOM_AIR;case "shadow_pact","false_ambush"->ParticleTypes.SMOKE;case "blood_mark"->ParticleTypes.DAMAGE_INDICATOR;default->ParticleTypes.CRIT;};p.serverLevel().sendParticles(particle,at.x,at.y+.5,at.z,16,.7,.3,.7,.03);p.level().playSound(null,net.minecraft.core.BlockPos.containing(at),net.minecraft.sounds.SoundEvents.ENCHANTMENT_TABLE_USE,net.minecraft.sounds.SoundSource.PLAYERS,.5F,1);}
    public static boolean secondary(ServerPlayer p){return SECONDARY.contains(p.getUUID());}
    private static boolean hit(ServerPlayer p,LivingEntity t,float damage,boolean magic){if(!hostile(p,t))return false;SECONDARY.add(p.getUUID());try{return t.hurt(magic?p.damageSources().indirectMagic(p,p):p.damageSources().playerAttack(p),ClassControl.resistant(t)?Math.min(damage,6):damage);}finally{SECONDARY.remove(p.getUUID());}}
    private static boolean dash(ServerPlayer p,Action a){if(!p.onGround()||p.isPassenger())return false;Vec3 start=p.position(),direction=new Vec3(p.getLookAngle().x,0,p.getLookAngle().z).normalize();if(direction.lengthSqr()<.5)return false;Vec3 end=start.add(direction.scale(3));if(!p.level().hasChunkAt(net.minecraft.core.BlockPos.containing(end))||!p.level().getWorldBorder().isWithinBounds(net.minecraft.core.BlockPos.containing(end)))return false;
        p.move(MoverType.SELF,direction.scale(3));if(p.position().distanceToSqr(start)<.25)return false;p.connection.teleport(p.getX(),p.getY(),p.getZ(),p.getYRot(),p.getXRot());int hits=0;for(var t:enemies(p,start.add(p.position()).scale(.5),3,12)){Vec3 delta=t.position().subtract(start);double along=delta.dot(direction);if(along>=0&&along<=p.position().distanceTo(start)+.5&&delta.subtract(direction.scale(along)).lengthSqr()<=2.25&&hits<3){if(hit(p,t,3,false))hits++;}}return true;}
    public static void tick(ServerPlayer p){
        var a=ACTIVE.get(p.getUUID());if(a==null)return;long now=ClassState.now(p);if(!p.isAlive()||p.isSpectator()||p.isPassenger()||p.isFallFlying()||p.getAbilities().flying||!a.dimension.equals(p.level().dimension().location().toString())||a.until<=now){clear(p);return;}
        if(p.position().distanceToSqr(a.last)>64&&!a.skill.equals("shadow_pact")){clear(p);return;}
        if(a.skill.equals("earth_thunder")){if(!p.onGround())a.airborne=true;if(a.airborne&&p.onGround()){for(var t:enemies(p,p.position(),4,6))if(hit(p,t,9,false)&&!ClassControl.resistant(t))t.knockback(.6,p.getX()-t.getX(),p.getZ()-t.getZ());visual(p,p.position(),a.skill);clear(p);return;}}
        if(a.skill.equals("shadow_pact")){Vec3 end=a.last.add(a.direction.scale(1.25));var block=p.level().clip(new net.minecraft.world.level.ClipContext(a.last,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,p));var impact=block.getLocation();LivingEntity collision=null;double distance=a.last.distanceToSqr(impact);for(var t:enemies(p,a.last,3,16)){var clip=t.getBoundingBox().inflate(.2).clip(a.last,end);if(clip.isPresent()&&a.last.distanceToSqr(clip.get())<=distance){impact=clip.get();distance=a.last.distanceToSqr(impact);collision=t;}}p.serverLevel().sendParticles(ParticleTypes.SMOKE,end.x,end.y,end.z,3,.05,.05,.05,0);if(collision!=null||block.getType()!=HitResult.Type.MISS){for(var t:enemies(p,impact,2.5,4))hit(p,t,10,true);clear(p);return;}a.last=end;return;}
        if(now%40==0&&(a.skill.equals("plague_seal")||a.skill.equals("blood_mark"))){var e=p.serverLevel().getEntity(a.target);if(!(e instanceof LivingEntity t)||!hostile(p,t)||p.distanceToSqr(t)>256){clear(p);return;}
            if(a.skill.equals("plague_seal")){hit(p,t,1,true);for(var nearby:enemies(p,t.position(),3,4))nearby.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,45,0));}
            else if(a.charges>0&&a.pool>0){float amount=Math.min(2,a.pool);if(hit(p,t,ClassControl.resistant(t)?Math.min(1,amount):amount,true)){a.pool-=amount;a.bleedDealt+=amount;}}
            visual(p,t.position(),a.skill);
        }
        if(now%20==0&&(a.skill.equals("sacred_circle")||a.skill.equals("living_wall")||a.skill.equals("banner_of_druzhina")||a.skill.equals("false_ambush")))visual(p,a.origin,a.skill);
        a.last=p.position();
    }
    public static void clear(ServerPlayer p){var a=ACTIVE.remove(p.getUUID());if(a==null)return;
        var level=p.getServer().getLevel(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION,net.minecraft.resources.ResourceLocation.parse(a.dimension)));if(level==null)return;
        var decoy=a.decoy==null?null:level.getEntity(a.decoy);for(var entry:a.previous.entrySet()){var e=level.getEntity(entry.getKey());var old=level.getEntity(entry.getValue());if(e instanceof Mob mob&&(mob.getTarget()==decoy||mob.getTarget()==null||mob.getTarget().getUUID().equals(a.target)))mob.setTarget(old instanceof LivingEntity target&&target.isAlive()?target:null);}if(decoy!=null)decoy.discard();
    }
    @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)clear(p);}
    @SubscribeEvent public static void dimension(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)clear(p);}
    @SubscribeEvent public static void stopped(net.neoforged.neoforge.event.server.ServerStoppedEvent e){ACTIVE.clear();SECONDARY.clear();}
    @SubscribeEvent public static void joined(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent e){if(!e.getLevel().isClientSide&&e.loadedFromDisk()&&e.getEntity().getPersistentData().getBoolean("ClassDecoy"))e.getEntity().discard();}
    @SubscribeEvent public static void defense(LivingDamageEvent.Pre e){
        if(e.getNewDamage()<=0||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)||e.getSource().is(net.minecraft.tags.DamageTypeTags.BYPASSES_EFFECTS))return;
        if(e.getSource().getEntity() instanceof ServerPlayer archer&&e.getSource().getDirectEntity() instanceof AbstractArrow&&e.getEntity().getPersistentData().getLong("ClassPierce:"+archer.getUUID())==ClassState.now(archer)&&!ClassControl.resistant(e.getEntity())){float armor=e.getContainer().getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ARMOR);e.setNewDamage(e.getNewDamage()+Math.min(2,armor*.20F));e.getEntity().getPersistentData().remove("ClassPierce:"+archer.getUUID());}
        ServerPlayer best=null;Action selected=null;float reduction=0;
        for(var p:e.getEntity().level().getEntitiesOfClass(ServerPlayer.class,e.getEntity().getBoundingBox().inflate(12))){var a=ACTIVE.get(p.getUUID());if(a==null||a.until<=ClassState.now(p)||!friendly(p,e.getEntity()))continue;float factor=0;
            if(a.skill.equals("sacred_circle")&&e.getEntity().position().distanceToSqr(a.origin)<=25&&a.pool>0)factor=.20F;
            if(a.skill.equals("banner_of_druzhina")&&a.members.contains(e.getEntity().getUUID())&&e.getEntity().position().distanceToSqr(a.origin)<=100)factor=.08F;
            if(a.skill.equals("living_wall")&&a.pool>0&&p.isBlocking()&&p.getUseItem().getItem() instanceof ShieldItem&&p.distanceToSqr(e.getEntity())<=16&&e.getSource().getSourcePosition()!=null){var facing=new Vec3(p.getLookAngle().x,0,p.getLookAngle().z).normalize();var incoming=e.getSource().getSourcePosition().subtract(p.position());if(facing.dot(new Vec3(incoming.x,0,incoming.z).normalize())>=.5&&e.getEntity().position().subtract(p.position()).dot(facing)<=.5)factor=.30F;}
            float value=e.getNewDamage()*factor;if(!a.skill.equals("banner_of_druzhina"))value=Math.min(value,a.pool);if(value>reduction){reduction=value;best=p;selected=a;}}
        if(best!=null&&selected!=null){e.setNewDamage(e.getNewDamage()-reduction);if(!selected.skill.equals("banner_of_druzhina"))selected.pool-=reduction;}
    }
    @SubscribeEvent public static void incoming(LivingIncomingDamageEvent e){
        if(e.getAmount()<=0||!(e.getSource().getEntity() instanceof LivingEntity attacker)||e.getEntity() instanceof Player)return;
        float boost=0;for(var owner:attacker.level().getEntitiesOfClass(ServerPlayer.class,attacker.getBoundingBox().inflate(12))){var zone=ACTIVE.get(owner.getUUID());if(zone==null||zone.until<=ClassState.now(owner)||!hostile(owner,e.getEntity())||!friendly(owner,attacker)||SECONDARY.contains(owner.getUUID()))continue;if(zone.skill.equals("banner_of_druzhina")&&zone.members.contains(attacker.getUUID())&&attacker.position().distanceToSqr(zone.origin)<=100)boost=Math.max(boost,.10F);if(zone.skill.equals("false_ambush")&&zone.previous.containsKey(e.getEntity().getUUID())&&zone.used.add(e.getEntity().getUUID()))boost=Math.max(boost,.10F);}if(boost>0)e.setAmount(e.getAmount()*(1+boost));
        if(!(attacker instanceof ServerPlayer p)||SECONDARY.contains(p.getUUID())||!hostile(p,e.getEntity()))return;var a=ACTIVE.get(p.getUUID());if(a==null||a.until<=ClassState.now(p))return;
        
        if(a.skill.equals("blood_mark")&&e.getEntity().getUUID().equals(a.target)&&e.getSource().getDirectEntity()==p&&"player".equals(e.getSource().getMsgId())&&!a.spent&&e.getEntity().getHealth()<e.getEntity().getMaxHealth()*.25F){a.spent=true;e.setAmount(e.getAmount()+(ClassControl.resistant(e.getEntity())?2:5));}
        if(a.skill.equals("vulnerable_point")&&e.getSource().getDirectEntity() instanceof AbstractArrow&&e.getEntity().getUUID().equals(a.target)&&!a.spent){a.spent=true;e.setAmount(e.getAmount()+(ClassControl.resistant(e.getEntity())?3:6));if(!ClassControl.resistant(e.getEntity()))e.getEntity().getPersistentData().putLong("ClassPierce:"+p.getUUID(),ClassState.now(p));}
        
    }
    @SubscribeEvent public static void damage(LivingDamageEvent.Post e){
        if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||SECONDARY.contains(p.getUUID())||e.getSource().getDirectEntity()!=p||!"player".equals(e.getSource().getMsgId())||!hostile(p,e.getEntity()))return;var a=ACTIVE.get(p.getUUID());if(a==null||a.until<=ClassState.now(p))return;
        if(a.skill.equals("unyielding_flurry")&&a.hits<5){a.hits++;for(var t:enemies(p,e.getEntity().position(),2.5,4))if(t!=e.getEntity())hit(p,t,Math.min(8,e.getNewDamage()*.4F),false);}
        if(a.skill.equals("blood_mark")&&e.getEntity().getUUID().equals(a.target)){a.charges=Math.min(4,a.charges+1);a.pool=Math.max(0,a.charges*2-a.bleedDealt);}
    }
    private ClassUltimateRuntime(){}
}
