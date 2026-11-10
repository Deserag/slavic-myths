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
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slavicmyths.item.PassiveCharm;
import org.slavicmyths.nochnitsa.NochnitsaContent;

@EventBusSubscriber(modid="slavicmyths")
public final class ViyRewardAbilities {
    public static final String OWNER="SlavicViySummonOwner",UNTIL="SlavicViySummonUntil";
    private static final String READY="SlavicViyRingReady",EYE_READY="SlavicViyEyeReady",LAST_TARGET="SlavicViyLastTarget";
    private static final String PETRIFIED="SlavicViyPetrifiedUntil",PREVIOUS_AI="SlavicViyPetrifiedPreviousNoAi",APPLIED_AI="SlavicViyPetrifiedAppliedAi";
    public static final int COOLDOWN=2400;
    private static boolean valid(ServerPlayer owner,LivingEntity target) {
        if(target==owner||!target.isAlive()||target.level()!=owner.level()||owner.isAlliedTo(target)||target.isAlliedTo(owner))return false;
        if(target.getPersistentData().hasUUID(OWNER))return false;
        if(target instanceof Player p && (p.isCreative()||p.isSpectator()||!owner.canHarmPlayer(p)))return false;
        return true;
    }
    @SubscribeEvent public static void attacked(AttackEntityEvent event) {
        if(frozen(event.getEntity())){event.setCanceled(true);return;}
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
        if(target==null||target.distanceToSqr(p)>256||!p.hasLineOfSight(target))return;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,35,1,true,true));
        if(p.tickCount%120==0)target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,30,0,true,true));
        long now=p.serverLevel().getGameTime();
        if(p.getPersistentData().getLong(EYE_READY)>now||!petrifiable(target)||target.getPersistentData().getLong(PETRIFIED)>now)return;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,254,true,true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,40,254,true,true));
        CompoundTag state=target.getPersistentData();
        state.putLong(PETRIFIED,now+40);
        state.putDouble("SlavicViyPetrifiedX",target.getX());
        state.putDouble("SlavicViyPetrifiedY",target.getY());
        state.putDouble("SlavicViyPetrifiedZ",target.getZ());
        if(target instanceof Mob mob){
            state.putBoolean(PREVIOUS_AI,mob.isNoAi());
            state.putBoolean(APPLIED_AI,true);
            mob.setNoAi(true);
            mob.getNavigation().stop();
        }
        p.getPersistentData().putLong(EYE_READY,now+COOLDOWN);
        p.serverLevel().sendParticles(ParticleTypes.ASH,target.getX(),target.getY()+target.getBbHeight()*.6,target.getZ(),24,.35,.6,.35,.01);
    }
    private static boolean frozen(LivingEntity target){
        return target.level() instanceof ServerLevel world&&target.getPersistentData().getLong(PETRIFIED)>world.getGameTime();
    }
    private static boolean petrifiable(LivingEntity target){
        if(target instanceof net.minecraft.world.entity.boss.enderdragon.EnderDragon
            ||target instanceof net.minecraft.world.entity.boss.wither.WitherBoss)return false;
        var type=target.getType();
        return type!=ViyContent.VIY.get()
            &&type!=org.slavicmyths.sisters.SistersContent.TRYASEYA.get()
            &&type!=org.slavicmyths.sisters.SistersContent.GNETEYA.get()
            &&type!=org.slavicmyths.sisters.SistersContent.LEDEYA.get()
            &&type!=org.slavicmyths.sisters.SistersContent.AGNEYA.get()
            &&type!=org.slavicmyths.sisters.SistersContent.NEVEYA.get()
            &&type!=org.slavicmyths.sisters.SistersContent.LOMEYA.get()
            &&type!=org.slavicmyths.morovoy.MorovoyRavenContent.BOSS.get()
            &&type!=org.slavicmyths.morovoy.MorovoyFireZmeyContent.BOSS.get()
            &&type!=org.slavicmyths.ninemor.NineMorContent.BOSS.get()
            &&target.getMaxHealth()<250;
    }
    @SubscribeEvent public static void petrifiedActions(PlayerInteractEvent.RightClickItem event){
        if(frozen(event.getEntity()))event.setCanceled(true);
    }
    @SubscribeEvent public static void petrifiedBlock(PlayerInteractEvent.RightClickBlock event){
        if(frozen(event.getEntity()))event.setCanceled(true);
    }
    @SubscribeEvent public static void petrifiedEntity(PlayerInteractEvent.EntityInteract event){
        if(frozen(event.getEntity()))event.setCanceled(true);
    }
    @SubscribeEvent public static void petrifiedMining(PlayerInteractEvent.LeftClickBlock event){
        if(frozen(event.getEntity()))event.setCanceled(true);
    }
    @SubscribeEvent public static void petrifiedTick(EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof LivingEntity target)||!(target.level() instanceof ServerLevel world))return;
        CompoundTag state=target.getPersistentData();
        if(!state.contains(PETRIFIED))return;
        if(world.getGameTime()>=state.getLong(PETRIFIED)){
            if(target instanceof Mob mob&&state.getBoolean(APPLIED_AI))mob.setNoAi(state.getBoolean(PREVIOUS_AI));
            state.remove(PETRIFIED);
            state.remove(PREVIOUS_AI);
            state.remove(APPLIED_AI);
            state.remove("SlavicViyPetrifiedX");
            state.remove("SlavicViyPetrifiedY");
            state.remove("SlavicViyPetrifiedZ");
            return;
        }
        target.setDeltaMovement(Vec3.ZERO);
        if(target instanceof Mob mob){mob.setNoAi(true);mob.getNavigation().stop();}
        if(target instanceof ServerPlayer player){
            player.stopUsingItem();
            double x=state.getDouble("SlavicViyPetrifiedX"),y=state.getDouble("SlavicViyPetrifiedY"),z=state.getDouble("SlavicViyPetrifiedZ");
            if(player.distanceToSqr(x,y,z)>.0025)player.teleportTo(x,y,z);
        }
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
        if(attacker instanceof LivingEntity living&&frozen(living)){event.setCanceled(true);return;}
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
