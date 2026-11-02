package org.slavicmyths.sisters;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;

/** Bounded zone with pre-impact warning and periodic line-of-sight-checked damage. */
public final class SistersZone extends Entity {
    private UUID owner;
    private int age, duration=125;
    public SistersZone(EntityType<? extends SistersZone> type,Level level) {super(type,level);noPhysics=true;}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { }
    public void bind(SisterBoss boss) {owner=boss.getUUID();}
    @Override public void tick() {
        super.tick();
        if(level().isClientSide) {
            if(tickCount%3==0) for(int i=0;i<12;i++) {
                double angle=i*Math.PI/6 + tickCount*.025;
                level().addParticle(net.minecraft.core.particles.ParticleTypes.WITCH,getX()+Math.cos(angle)*2.7,getY()+.12,getZ()+Math.sin(angle)*2.7,0,.02,0);
            }
            return;
        }
        if(++age>=duration || owner==null || !(level() instanceof ServerLevel world) || !(world.getEntity(owner) instanceof SisterBoss boss) || !boss.isAlive()) {discard();return;}
        if(age<24 || age%20!=0)return;
        for(Player player:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(3.0,1.8,3.0),p -> p.isAlive() && !p.isCreative() && !p.isSpectator())) {
            if(player.distanceToSqr(getX(),player.getY(),getZ())>3*3 || Math.abs(player.getY()-getY())>1.8 || !boss.getSensing().hasLineOfSight(player))continue;
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,34,0));
            player.hurt(damageSources().mobAttack(boss),2);
        }
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {tag.putInt("Age",age);tag.putInt("Life",duration);if(owner!=null)tag.putUUID("Owner",owner);}
    @Override protected void readAdditionalSaveData(CompoundTag tag) {age=Math.clamp(tag.getInt("Age"),0,150);duration=Math.clamp(tag.getInt("Life"),1,150);owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;}
}
