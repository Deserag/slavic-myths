package org.slavicmyths.gorynych;

import java.util.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.*;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.effect.*;
import net.minecraft.world.phys.*;

/** Crossbow-only storm balance; rune/boss burst rules remain separate. */
public final class StormCrossbow {
    public static final float DAMAGE=3;
    public static final int CONTROL_TICKS=40,TARGET_COOLDOWN=30,MAX_TARGETS=8;
    public static final double RADIUS=3;
    private static final Map<LivingEntity,Map<UUID,Long>> READY=new WeakHashMap<>();
    public static void afterHit(AbstractArrow arrow,Vec3 at){
        if(arrow.level().isClientSide||arrow.getPersistentData().getInt("GorynychSpecial")!=3)return;
        arrow.getPersistentData().remove("GorynychSpecial");
        if(arrow.getOwner() instanceof LivingEntity owner&&arrow.getWeaponItem()!=null
            &&arrow.getWeaponItem().getItem() instanceof SerpentCrossbow crossbow&&crossbow.special)burst(owner,at);
    }
    private static boolean admit(LivingEntity target,UUID owner,long now){
        var map=READY.computeIfAbsent(target,unused->new HashMap<>());
        map.values().removeIf(until->until<=now);
        if(now<map.getOrDefault(owner,0L))return false;
        if(map.size()>=8)map.remove(map.entrySet().stream().min(Map.Entry.comparingByValue()).orElseThrow().getKey());
        map.put(owner,now+TARGET_COOLDOWN);return true;
    }
    public static void burst(LivingEntity owner,Vec3 at){
        if(!(owner.level() instanceof ServerLevel level))return;
        var targets=level.getEntitiesOfClass(LivingEntity.class,new AABB(at.subtract(RADIUS,RADIUS,RADIUS),at.add(RADIUS,RADIUS,RADIUS)),t->
            t.isAlive()&&BreathCloud.allowed(owner,t)&&t.position().distanceToSqr(at)<=RADIUS*RADIUS
            &&level.clip(new net.minecraft.world.level.ClipContext(at.add(0,.5,0),t.getEyePosition(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,owner)).getType()==HitResult.Type.MISS);
        targets.sort(Comparator.comparingDouble(t->t.distanceToSqr(at)));
        var ends=new ArrayList<Vec3>();
        for(var target:targets){if(ends.size()>=MAX_TARGETS)break;if(!admit(target,owner.getUUID(),level.getGameTime()))continue;
            var original=org.slavicmyths.combat.MythDamageSources.periodic("gorynych_impact",owner);
            var damage=new DamageSource(original.typeHolder(),null,owner){@Override public boolean is(TagKey<DamageType> tag){return tag==DamageTypeTags.BYPASSES_COOLDOWN||super.is(tag);}};
            if(!target.hurt(damage,DAMAGE))continue;
            double dx=at.x-target.getX(),dz=at.z-target.getZ();if(dx*dx+dz*dz<1e-6){dx=-owner.getLookAngle().x;dz=-owner.getLookAngle().z;}
            target.knockback(.65,dx,dz);
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,CONTROL_TICKS,1));
            ends.add(target.getBoundingBox().getCenter());
        }
        StormVfx.play(level,at,ends);
        level.playSound(null,net.minecraft.core.BlockPos.containing(at),net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT,net.minecraft.sounds.SoundSource.PLAYERS,.4f,1.4f);
    }
    private StormCrossbow(){}
}
