package org.slavicmyths.guardians;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.ParticleTypes;
public final class FireGuardian extends GuardianEntity {
 private long pulseReady,slamReady,lockReady;
 private boolean finalLock;private float slamYaw;
 public FireGuardian(EntityType<? extends FireGuardian> t,Level w){super(t,w,false);}
 public boolean lastEmbers(){return getHealth()<=getMaxHealth()*.25;}
 @Override protected boolean hit(LivingEntity target,float damage){return super.hit(target,damage,state()==GuardianRules.MELEE?damageSources().mobAttack(this):GuardianContent.fireDamage(this));}
 public boolean dangerous(Vec3 at,int wave){var b=arena();return GuardianRules.strip(at.x,b.minX,b.getXsize())!=GuardianRules.safeStrip(pattern(),wave);}
 @Override protected void combat(){long now=level().getGameTime();int s=state(),a=age();
  if(s==GuardianRules.COMBAT){if(getTarget()==null)return;if(getHealth()<=getMaxHealth()*.75&&now>=lockReady){finalLock=lastEmbers();pattern(getRandom().nextInt(3));state(GuardianRules.LOCKDOWN);sound("fire_lockdown_warning");return;}if(now>=pulseReady){state(GuardianRules.PULSE);sound("fire_pulse_charge");return;}if(now>=slamReady){slamYaw=getYRot();state(GuardianRules.SLAM);sound("fire_guardian_swing");return;}melee();return;}
  if(s==GuardianRules.MELEE){meleeTick();return;}
  if(s==GuardianRules.PULSE){if(a<GuardianRules.PULSE_WINDUP&&a%4==0){ring(position(),3,ParticleTypes.SMOKE);ring(position(),6,ParticleTypes.FLAME);ring(position(),10,ParticleTypes.SMOKE);}if(a==GuardianRules.PULSE_WINDUP){sound("fire_pulse_burst");for(var t:targets())hit(t,GuardianRules.pulse(distanceTo(t)));ring(position(),6,ParticleTypes.FLAME);}if(a>=GuardianRules.PULSE_WINDUP+18){pulseReady=now+(lastEmbers()?224:GuardianRules.PULSE_COOLDOWN);state(GuardianRules.COMBAT);}return;}
  if(s==GuardianRules.SLAM){if(a%4==0)cracks(a>=GuardianRules.SLAM_WINDUP);if(a==GuardianRules.SLAM_WINDUP){sound("fire_guardian_ground_impact");for(var t:targets())if(inCrack(t.position())&&t.getY()<getY()+.65&&hit(t,GuardianRules.SLAM_DAMAGE))t.knockback(.25,getX()-t.getX(),getZ()-t.getZ());}if(a>=GuardianRules.SLAM_WINDUP+18){slamReady=now+GuardianRules.SLAM_COOLDOWN;state(GuardianRules.COMBAT);}return;}
  if(s==GuardianRules.LOCKDOWN){int warning=GuardianRules.lockWarning(finalLock),length=warning+GuardianRules.LOCK_ACTIVE,wave=Math.min(2,(a-1)/length),local=(a-1)%length;boolean active=local>=warning;if(local==0&&wave>0)sound("fire_lockdown_warning");if(local==warning)sound("fire_lockdown_ignite");
   if(a%4==0){var b=arena();for(double x=b.minX+.5;x<b.maxX;x+=2)for(double z=b.minZ+.5;z<b.maxZ;z+=2)if(dangerous(new Vec3(x,b.minY,z),wave))particle(active?ParticleTypes.FLAME:ParticleTypes.SMOKE,new Vec3(x,b.minY+(active?.6:.1),z),1);else if(a%8==0)particle(ParticleTypes.HAPPY_VILLAGER,new Vec3(x,b.minY+.08,z),1);ring(position().add(0,1,0),1.4,ParticleTypes.FLAME);}
   if(active&&local%15==warning%15)for(var t:targets())if(dangerous(t.position(),wave))hit(t,GuardianRules.ZONE_DAMAGE,GuardianContent.fireDamage(this));
   if(a>=length*GuardianRules.LOCK_WAVES){lockReady=now+GuardianRules.LOCK_COOLDOWN;sound("fire_shield_break");state(GuardianRules.EXHAUSTED);}return;}
  if(s==GuardianRules.EXHAUSTED&&a>=GuardianRules.EXHAUST)state(GuardianRules.COMBAT);
 }
 private boolean inCrack(Vec3 p){var delta=p.subtract(position());for(int i=-1;i<=1;i++){double angle=Math.toRadians(slamYaw+i*25),x=-Math.sin(angle),z=Math.cos(angle),along=delta.x*x+delta.z*z,side=Math.abs(delta.x*z-delta.z*x);if(along>=0&&along<=6&&side<.5)return true;}return false;}
 private void cracks(boolean active){for(int i=-1;i<=1;i++){double angle=Math.toRadians(slamYaw+i*25);for(int j=1;j<=6;j++){var p=position().add(-Math.sin(angle)*j,.12,Math.cos(angle)*j);if(arena().contains(p))particle(active?ParticleTypes.FLAME:ParticleTypes.SMOKE,p,1);}}}
}
