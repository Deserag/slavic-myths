package org.slavicmyths.desert;

import java.util.Comparator;
import java.util.List;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.slavicmyths.combat.CombatTargeting;

/** Server-side aim. Visual origins are deliberately separate from hit selection. */
public final class DesertWeaponTargeting {
    public record Ray(Vec3 start,Vec3 end,LivingEntity target){}
    public static boolean legal(ServerPlayer p,LivingEntity t){
        return t!=null&&CombatTargeting.canAttack(p,t,CombatTargeting.Context.DIRECT)&&p.hasLineOfSight(t);
    }
    public static Ray trace(ServerPlayer p,double range){
        Vec3 start=p.getEyePosition(),far=start.add(p.getLookAngle().scale(range));
        Vec3 wall=p.serverLevel().clip(new ClipContext(start,far,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getLocation();
        LivingEntity target=p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(start,wall).inflate(1),
            t->legal(p,t)&&t.getBoundingBox().inflate(.2).clip(start,wall).isPresent()).stream()
            .min(Comparator.comparingDouble(t->t.getBoundingBox().inflate(.2).clip(start,wall).orElse(wall).distanceToSqr(start))).orElse(null);
        Vec3 end=target==null?wall:target.getBoundingBox().inflate(.2).clip(start,wall).orElse(wall);
        return new Ray(start,end,target);
    }
    public static List<LivingEntity> area(ServerPlayer p,Vec3 center,double radius){
        return p.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius),
            t->legal(p,t)&&t.getBoundingBox().getCenter().distanceToSqr(center)<=radius*radius
            &&(CombatTargeting.isEnemy(p,t,CombatTargeting.Context.ACQUIRE)||t==p.getLastHurtMob())
            &&p.serverLevel().clip(new ClipContext(center,t.getBoundingBox().getCenter(),
              ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()==HitResult.Type.MISS);
    }
    private DesertWeaponTargeting(){}
}
