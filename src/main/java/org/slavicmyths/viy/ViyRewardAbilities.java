package org.slavicmyths.viy;

import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import org.slavicmyths.item.PassiveCharm;
import org.slavicmyths.nochnitsa.NochnitsaContent;

@EventBusSubscriber(modid="slavicmyths")
public final class ViyRewardAbilities {
    public static final String OWNER="SlavicViySummonOwner",UNTIL="SlavicViySummonUntil";
    private static final String READY="SlavicViyRingReady",EYE_READY="SlavicViyEyeReady",LAST_TARGET="SlavicViyLastTarget";
    public static final int COOLDOWN=2400;
    private static boolean valid(ServerPlayer owner,LivingEntity target) {
        if(target==owner||!target.isAlive()||target.level()!=owner.level()||owner.isAlliedTo(target)||target.isAlliedTo(owner))return false;
        if(target.getPersistentData().hasUUID(OWNER))return false;
        if(target instanceof Player p && (p.isCreative()||p.isSpectator()||!owner.canHarmPlayer(p)))return false;
        return true;
    }
    @SubscribeEvent public static void attacked(AttackEntityEvent event) {
        if(event.getEntity() instanceof ServerPlayer p && event.getTarget() instanceof LivingEntity t && valid(p,t))
            p.getPersistentData().putUUID(LAST_TARGET,t.getUUID());
    }
    private static LivingEntity lastTarget(ServerPlayer owner) {
        CompoundTag n=owner.getPersistentData();
        if(!n.hasUUID(LAST_TARGET))return null;
        var e=owner.serverLevel().getEntity(n.getUUID(LAST_TARGET));
        return e instanceof LivingEntity living && valid(owner,living)?living:null;
    }
    private static LivingEntity choice(ServerPlayer p) {
        LivingEntity last=lastTarget(p);
        if(last!=null && last.distanceToSqr(p)<32*32)return last;
        return p.serverLevel().getEntitiesOfClass(Monster.class,p.getBoundingBox().inflate(18,8,18),
            m->valid(p,m)&&m.getPersistentData().getBoolean("SlavicViySummoned")==false).stream()
            .min((a,b)->Double.compare(a.distanceToSqr(p),b.distanceToSqr(p))).orElse(null);
    }
    public static void summon(ServerPlayer p) {
        if(!p.isAlive()||p.isSpectator()||PassiveCharm.equipped(p,ViyContent.VIY_RING.get(),"ring").isEmpty())return;
        ServerLevel level=p.serverLevel();
        long now=level.getGameTime();
        if(p.getPersistentData().getLong(READY)>now)return;
        LivingEntity target=choice(p);
        int spawned=0;
        for(int i=0;i<5;i++) {
            Mob mob=i<2?net.minecraft.world.entity.EntityType.ZOMBIE.create(level):i<4?net.minecraft.world.entity.EntityType.SKELETON.create(level):NochnitsaContent.ENTITY.get().create(level);
            if(mob==null)continue;
            double angle=(i*Math.PI*2)/5;
            mob.moveTo(p.getX()+Math.cos(angle)*2.2,p.getY(),p.getZ()+Math.sin(angle)*2.2,p.getYRot(),0);
            if(!level.hasChunkAt(mob.blockPosition())||!level.noCollision(mob))continue;
            mob.getPersistentData().putBoolean("SlavicViySummoned",true);
            mob.getPersistentData().putUUID(OWNER,p.getUUID());
            mob.getPersistentData().putLong(UNTIL,now+600);
            mob.setPersistenceRequired();
            if(level.addFreshEntity(mob))spawned++;
        }
        if(spawned>0){
            p.getPersistentData().putLong(READY,now+COOLDOWN);
            level.sendParticles(ParticleTypes.SOUL,p.getX(),p.getY()+1,p.getZ(),12,1,.7,1,.02);
        }
    }
    private static LivingEntity gaze(ServerPlayer p) {
        Vec3 eye=p.getEyePosition(),look=p.getLookAngle();
        LivingEntity selected=null;
        double nearest=16;
        for(LivingEntity e:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(16),t->valid(p,t))) {
            Vec3 to=e.getBoundingBox().getCenter().subtract(eye);
            double distance=to.length();
            if(distance<.01||distance>nearest||to.normalize().dot(look)<.985||!p.hasLineOfSight(e))continue;
            selected=e;nearest=distance;
        }
        return selected;
    }
    public static void eyeTick(ServerPlayer p) {
        if(p.tickCount%10!=0)return;
        LivingEntity target=gaze(p);
        if(target==null)target=lastTarget(p);
        if(target==null||target.distanceToSqr(p)>256)return;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,35,1,true,true));
        if(p.tickCount%120==0)target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,30,0,true,true));
        long now=p.serverLevel().getGameTime();
        if(p.getPersistentData().getLong(EYE_READY)>now)return;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,254,true,true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,40,254,true,true));
        target.getPersistentData().putLong("SlavicViyPetrifiedUntil",now+40);
        p.getPersistentData().putLong(EYE_READY,now+COOLDOWN);
        p.serverLevel().sendParticles(ParticleTypes.ASH,target.getX(),target.getY()+target.getBbHeight()*.6,target.getZ(),24,.35,.6,.35,.01);
    }
    @SubscribeEvent public static void summonedTick(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof Mob mob)||!(mob.level() instanceof ServerLevel world)
            ||!mob.getPersistentData().getBoolean("SlavicViySummoned"))return;
        CompoundTag data=mob.getPersistentData();
        if(!data.hasUUID(OWNER)||world.getGameTime()>=data.getLong(UNTIL)){mob.discard();return;}
        ServerPlayer owner=world.getServer().getPlayerList().getPlayer(data.getUUID(OWNER));
        if(owner==null||owner.level()!=world||!owner.isAlive()){mob.discard();return;}
        if(mob.tickCount%5!=0)return;
        LivingEntity target=choice(owner);
        if(target!=null&&mob.distanceToSqr(target)<=32*32){
            if(mob.getTarget()!=target)mob.setTarget(target);
            if(mob instanceof org.slavicmyths.nochnitsa.NochnitsaEntity)mob.getNavigation().moveTo(target,1.2);
        }else{
            mob.setTarget(null);
            if(mob.distanceToSqr(owner)>16)mob.getNavigation().moveTo(owner,1.15);
            else mob.getNavigation().stop();
        }
    }
    @SubscribeEvent public static void protectSummons(LivingIncomingDamageEvent event) {
        Entity attacker=event.getSource().getEntity();
        if(!(attacker instanceof Mob mob)||!mob.getPersistentData().getBoolean("SlavicViySummoned")
            ||!mob.getPersistentData().hasUUID(OWNER)||!(mob.level() instanceof ServerLevel world))return;
        ServerPlayer owner=world.getServer().getPlayerList().getPlayer(mob.getPersistentData().getUUID(OWNER));
        LivingEntity victim=event.getEntity();
        if(owner==null||owner.level()!=world||victim==owner||victim.getPersistentData().getBoolean("SlavicViySummoned")
            ||owner.isAlliedTo(victim)||victim.isAlliedTo(owner)
            ||victim instanceof Player p&&(p.isCreative()||p.isSpectator()||!owner.canHarmPlayer(p)))
            event.setCanceled(true);
    }
    @SubscribeEvent public static void noSummonedLoot(LivingDropsEvent e) {
        if(e.getEntity().getPersistentData().getBoolean("SlavicViySummoned"))e.getDrops().clear();
    }
    @SubscribeEvent public static void noSummonedExperience(LivingExperienceDropEvent e) {
        if(e.getEntity().getPersistentData().getBoolean("SlavicViySummoned"))e.setDroppedExperience(0);
    }
    private ViyRewardAbilities(){}
}
