package org.slavicmyths.quest185;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.world.effect.*;
import org.slavicmyths.wolf.*;
import org.slavicmyths.poloz.*;
/** Temporary combat representative. Original GreyWolf actors and quests remain independent. */
public final class WolfSupport extends GreyWolf {
 private UUID owner;private long expiry,sniffAt,leapAt,howlAt;private int stuck,fade;private Vec3 last=Vec3.ZERO;
 public WolfSupport(EntityType<? extends GreyWolf> t,Level w){super(t,w);getPersistentData().putBoolean("Quest185Support",true);xpReward=0;}
 @Override public boolean narrativeActor(){return false;}
 public UUID owner(){return owner;}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new MeleeAttackGoal(this,1.2,true));}
 public static boolean summon(ServerPlayer p){var n=Quest185Story.data(p);long now=Quest185Story.now(p);if(!n.getBoolean("WolfUnlocked")){p.displayClientMessage(Quest185Story.text("wolf_locked"),true);return false;}if(n.hasUUID("Support")){var e=p.serverLevel().getEntity(n.getUUID("Support"));if(e instanceof WolfSupport wolf&&!wolf.isRemoved()){p.displayClientMessage(Quest185Story.text("wolf_present"),true);return false;}if(now<n.getLong("SupportExpires")){p.displayClientMessage(Quest185Story.text("wolf_present"),true);return false;}}
  if(now<n.getLong("WolfReady")){p.displayClientMessage(Quest185Story.text("cooldown",(n.getLong("WolfReady")-now+19)/20),true);return false;}var at=Quest185Sessions.safe(p,p.blockPosition(),3,6,2.2f,3.5f);if(at==null){p.displayClientMessage(Quest185Story.text("no_safe_ground"),true);return false;}var wolf=Quest185Content.WOLF.get().create(p.serverLevel());if(wolf==null)return false;wolf.owner=p.getUUID();wolf.expiry=now+2400;wolf.setHealth(50);wolf.moveTo(at);wolf.setPersistenceRequired();if(!p.serverLevel().addFreshEntity(wolf))return false;n.putUUID("Support",wolf.getUUID());n.putLong("SupportExpires",wolf.expiry);n.putLong("WolfReady",now+6000);Quest185Content.sound(p,"wolf_call");Quest185Content.sound(p,"wolf_arrive");p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,at.x,at.y+1,at.z,16,.6,.6,.6,.01);return true;
 }
 public boolean friendly(LivingEntity l){if(l==this||l.getUUID().equals(owner))return true;if(level() instanceof ServerLevel w){var p=w.getServer().getPlayerList().getPlayer(owner);if(p!=null&&p.isAlliedTo(l))return true;}if(l instanceof WolfSupport wolf)return Objects.equals(owner,wolf.owner);if(l instanceof TamableAnimal pet&&Objects.equals(owner,pet.getOwnerUUID()))return true;return Objects.equals(owner,org.slavicmyths.kalin.KalinParty.owner(l));}
 @Override public boolean isAlliedTo(Entity e){return e instanceof LivingEntity l&&friendly(l)||super.isAlliedTo(e);}
 public void retreat(){if(fade==0){fade=20;setTarget(null);getNavigation().stop();playSound(WolfSounds.get("trial_end"),.6f,1);}}
 @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float amount){if(s.getEntity() instanceof LivingEntity l&&friendly(l))return false;if(getHealth()-amount<=5){retreat();return false;}return super.hurt(s,amount);}
 @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);if(hit)supportMove(WolfTimeline.Move.BITE);return hit;}
 @Override public void die(net.minecraft.world.damagesource.DamageSource s){retreat();setHealth(5);}
 @Override protected net.minecraft.world.InteractionResult mobInteract(net.minecraft.world.entity.player.Player p,net.minecraft.world.InteractionHand h){return net.minecraft.world.InteractionResult.PASS;}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;long now=w.getServer().overworld().getGameTime();var p=w.getServer().getPlayerList().getPlayer(owner);if(fade>0){w.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,getX(),getY()+1,getZ(),2,.5,.4,.5,.01);if(--fade==0){if(p!=null){Quest185Story.data(p).remove("Support");Quest185Story.data(p).remove("SupportExpires");}discard();}return;}if(p==null||p.serverLevel()!=w||!p.isAlive()){retreat();return;}var boss=PolozEvent.get(w).loaded(w);boolean combat=boss!=null&&boss.isAlive()&&boss.nearestDistance(position())<48*48;
  if(combat){expiry=now+2400;Quest185Story.data(p).putLong("SupportExpires",expiry);}if(now>=expiry||getHealth()<=5){retreat();return;}if(tickCount%20!=0)return;
  if(combat){if(now>=sniffAt){sniffAt=now+600;var n=new CompoundTag();n.putString("Kind","weakpoints");n.putInt("Boss",boss.getId());n.putLong("Until",now+120);Quest185Network.send(p,n);playSound(WolfSounds.get("ambient"),.5f,1);supportMove(WolfTimeline.Move.BITE);}
   if(now>=leapAt&&boss.nearestDistance(position())<=64){leapAt=now+800;Entity target=boss;double d=boss.getBoundingBox().distanceToSqr(getEyePosition());for(var part:boss.parts){if(PolozRules.multiplier(boss.phase(),part.segment,part.scar)<=0)continue;double distance=part.getBoundingBox().distanceToSqr(getEyePosition());if(distance<d){d=distance;target=part;}}if(d<=64&&hasLineOfSight(target)){var delta=target.position().subtract(position()).multiply(1,0,1).normalize();setDeltaMovement(delta.scale(.55).add(0,.3,0));supportMove(WolfTimeline.Move.POUNCE);if(d<=16){target.hurt(damageSources().mobAttack(this),8);boss.setTarget(this);getPersistentData().putLong("ThreatUntil",now+80);}}}
   if(now>=howlAt){howlAt=now+1200;supportMove(WolfTimeline.Move.HOWL_SUMMON);playSound(Quest185Content.SOUNDS.get("wolf_howl").get(),.8f,1);for(var ally:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(12),l->friendly(l)&&l.isAlive()&&distanceToSqr(l)<=144))ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,300,0));}
   setTarget(boss);
  }else{var enemies=w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(8),l->l.isAlive()&&!friendly(l)&&(l instanceof Enemy||Quest185Events.mor(l)));setTarget(enemies.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null));}
  if(getTarget()==null&&distanceToSqr(p)>16)getNavigation().moveTo(p,1);stuck=position().distanceToSqr(last)<.04&&distanceToSqr(p)>24*24?stuck+20:0;last=position();if(stuck>=100){var at=Quest185Sessions.safe(p,p.blockPosition(),4,6,2.2f,3.5f);if(at!=null){setPos(at);getNavigation().stop();}stuck=0;}if(animationTicks(0)>140)supportMove(WolfTimeline.Move.NONE);
 }
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("SupportOwner",owner);n.putLong("Expiry",expiry);n.putLong("Sniff",sniffAt);n.putLong("Leap",leapAt);n.putLong("Howl",howlAt);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("SupportOwner")?n.getUUID("SupportOwner"):null;expiry=n.getLong("Expiry");sniffAt=n.getLong("Sniff");leapAt=n.getLong("Leap");howlAt=n.getLong("Howl");setHealth(Math.clamp(n.getFloat("Health"),5,50));}
}
