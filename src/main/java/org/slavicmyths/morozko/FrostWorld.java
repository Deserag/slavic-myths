package org.slavicmyths.morozko;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
public final class FrostWorld {
 public static Vec3 safe(ServerLevel w,Vec3 origin,float width,float height,int radius){var base=BlockPos.containing(origin);for(int r=0;r<=radius;r++)for(int dx=-r;dx<=r;dx++)for(int dz=-r;dz<=r;dz++){if(Math.max(Math.abs(dx),Math.abs(dz))!=r)continue;for(int dy:new int[]{0,1,-1,2,-2}){var p=base.offset(dx,dy,dz);if(!w.hasChunkAt(p)||!w.hasChunkAt(p.above((int)Math.ceil(height)))||!w.getWorldBorder().isWithinBounds(p)||!w.getBlockState(p.below()).isFaceSturdy(w,p.below(),net.minecraft.core.Direction.UP)||w.getBlockEntity(p.below())!=null||!w.getFluidState(p).isEmpty())continue;var box=new AABB(p.getX()+.5-width/2,p.getY(),p.getZ()+.5-width/2,p.getX()+.5+width/2,p.getY()+height,p.getZ()+.5+width/2);if(w.noCollision(box)&&!w.containsAnyLiquid(box))return new Vec3(p.getX()+.5,p.getY(),p.getZ()+.5);}}return null;}
 public static java.util.UUID owner(Entity e){return e instanceof net.minecraft.server.level.ServerPlayer p?p.getUUID():e instanceof PlayerDouble p?p.owner():org.slavicmyths.kalin.KalinParty.owner(e);}
 public static boolean hostile(LivingEntity t,LivingEntity caster){
  if(t==null||caster==null)return false;
  LivingEntity observer=caster instanceof PlayerDouble d&&d.player()!=null?d.player():caster;
  if(t instanceof net.minecraft.world.entity.player.Player)return org.slavicmyths.combat.CombatTargeting.canAttack(observer,t,org.slavicmyths.combat.CombatTargeting.Context.NATIVE_DAMAGE);
  return org.slavicmyths.combat.CombatTargeting.isEnemy(observer,t,org.slavicmyths.combat.CombatTargeting.Context.ACQUIRE);
 }

 public static double bboxDistance(Vec3 p,AABB b){double x=Math.clamp(p.x,b.minX,b.maxX),y=Math.clamp(p.y,b.minY,b.maxY),z=Math.clamp(p.z,b.minZ,b.maxZ);return p.distanceToSqr(new Vec3(x,y,z));}
 public static boolean cone(LivingEntity from,LivingEntity to,double radius,double halfAngle){var v=to.getBoundingBox().getCenter().subtract(from.getEyePosition());return bboxDistance(from.getEyePosition(),to.getBoundingBox())<=radius*radius&&from.hasLineOfSight(to)&&from.getLookAngle().dot(v.normalize())>=Math.cos(Math.toRadians(halfAngle));}
 private FrostWorld(){}
}
