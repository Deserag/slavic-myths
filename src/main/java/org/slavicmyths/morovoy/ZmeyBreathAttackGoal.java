package org.slavicmyths.morovoy;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Server-authoritative breath windows, independent from navigation and melee attacks. */
public final class ZmeyBreathAttackGoal extends Goal {
    private final MorovoyFireZmey boss;
    public ZmeyBreathAttackGoal(MorovoyFireZmey boss) { this.boss=boss; }
    @Override public boolean canUse() { return active() && boss.getTarget()!=null && boss.canDamage(boss.getTarget()); }
    @Override public boolean canContinueToUse() { return canUse(); }
    private boolean active() { return boss.action()==MorovoyFireZmey.MOR_BREATH || boss.action()==MorovoyFireZmey.FIRE_BREATH; }
    @Override public void tick() {
        int age=boss.actionAge();
        if(age<18 || age>55 || age%4!=0 || !(boss.level() instanceof ServerLevel world))return;
        LivingEntity target=boss.getTarget();
        if(target==null || !boss.canDamage(target))return;
        int element=boss.action()==MorovoyFireZmey.MOR_BREATH?0:1;
        Vec3 from=boss.mouth(element);
        Vec3 aim=target.getEyePosition().subtract(from).normalize();
        Set<UUID> damaged=new HashSet<>();
        for(int i=1;i<=8;i++){
            Vec3 point=from.add(aim.scale(i*1.5));
            BlockPos at=BlockPos.containing(point);
            if(!world.hasChunkAt(at) || !world.getBlockState(at).isAir())break;
            if(i%2==0)boss.particles(element,point,5);
            for(LivingEntity victim:world.getEntitiesOfClass(LivingEntity.class,new AABB(point,point).inflate(1.2),boss::canDamage)){
                if(damaged.add(victim.getUUID()) && boss.hasLineOfSight(victim)){
                    victim.hurt(boss.damageSources().mobAttack(boss),element==0?5.0f:6.25f);
                    if(element==0)victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,45));
                }
            }
        }
        if(age%12==0 && target.distanceToSqr(boss)<225){
            MorovoyZmeyField.spawn(world,boss,element,target.position(),65);
            if(element==1)VioletFireBlock.place(world,target.blockPosition());
        }
    }
}
