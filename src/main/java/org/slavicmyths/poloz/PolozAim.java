package org.slavicmyths.poloz;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
/** Hero aim/navigation adapter; damage still passes through the first real intersected part. */
public final class PolozAim {
 public static Entity part(Mob hero,GreatPoloz boss){if(hero instanceof org.slavicmyths.bogatyr.IlyaEntity)return boss;
  Entity best=boss;double distance=Double.POSITIVE_INFINITY;for(var part:boss.parts){if(boss.phase()==1&&!part.scar&&part.segment<22)continue;if(hero instanceof org.slavicmyths.marinka.DobrynyaEntity&&boss.phase()==1&&!part.scar)continue;double d=part.getBoundingBox().distanceToSqr(hero.getEyePosition());if(d<distance){distance=d;best=part;}}return best;
 }
 public static Vec3 point(Mob hero,Entity target){return target instanceof GreatPoloz p?part(hero,p).getBoundingBox().getCenter():target.position();}
 public static double edge(Mob hero,LivingEntity target){Entity part=target instanceof GreatPoloz p?part(hero,p):target;return Math.max(0,Math.sqrt(part.getBoundingBox().distanceToSqr(hero.position()))-hero.getBbWidth()/2);}
 public static boolean sight(Mob hero,GreatPoloz p){Vec3 at=point(hero,p);return hero.level().clip(new net.minecraft.world.level.ClipContext(hero.getEyePosition(),at,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,hero)).getType()==HitResult.Type.MISS;}
 public static void navigate(Mob hero,LivingEntity target,double speed){if(!(target instanceof GreatPoloz p)){hero.getNavigation().moveTo(target,speed);return;}Entity part=part(hero,p);Vec3 at=part.position();Vec3 away=hero.position().subtract(at).multiply(1,0,1).normalize().scale(part.getBbWidth()/2+1);at=at.add(away);hero.getNavigation().moveTo(at.x,part.getY(),at.z,speed);}
 public static boolean strike(Mob hero,GreatPoloz boss,float damage){Vec3 start=hero.getEyePosition(),end=point(hero,boss);Entity first=null;double nearest=Double.POSITIVE_INFINITY;for(var part:boss.parts){var hit=part.getBoundingBox().clip(start,end);if(hit.isPresent()&&hit.get().distanceToSqr(start)<nearest){first=part;nearest=hit.get().distanceToSqr(start);}}var head=boss.getBoundingBox().clip(start,end);if(head.isPresent()&&head.get().distanceToSqr(start)<nearest){first=boss;nearest=head.get().distanceToSqr(start);}if(first==null||nearest>25||!sight(hero,boss))return false;return first.hurt(hero.damageSources().mobAttack(hero),damage);}
 private PolozAim(){}
}
