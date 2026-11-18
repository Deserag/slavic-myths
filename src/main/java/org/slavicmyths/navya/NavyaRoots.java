package org.slavicmyths.navya;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;

/** One fixed transient attack: warn -> rise -> hold -> recede. No block mutations. */
public final class NavyaRoots extends Entity {
    private static final EntityDataAccessor<Long> BORN=SynchedEntityData.defineId(NavyaRoots.class,EntityDataSerializers.LONG);
    private UUID owner;
    public NavyaRoots(EntityType<? extends NavyaRoots> type,Level world) { super(type,world); }
    public void owner(UUID owner) { this.owner=owner;entityData.set(BORN,level().getGameTime()); }
    public float age(float partial) { return Math.max(0,level().getGameTime()-entityData.get(BORN)+partial); }
    @Override public net.minecraft.sounds.SoundSource getSoundSource() { return net.minecraft.sounds.SoundSource.HOSTILE; }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { builder.define(BORN,0L); }
    @Override public void tick() {
        super.tick();
        if(!(level() instanceof ServerLevel server))return;
        Entity source=owner==null?null:server.getEntity(owner);
        if(!(source instanceof NavyaMob mob) || !mob.isAlive() || age(0)>60) {discard();return;}
        int age=(int)age(0);
        if(age==1)playSound(NavyaSounds.get("roots_warn"),.4F,1);
        if(age==20) {
            playSound(NavyaSounds.get("roots_rise"),.6F,1);
            for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,getBoundingBox(),entity->mob.canAttack(entity) && !mob.isAlliedTo(entity))) {
                if(mob.hasLineOfSight(target) && target.hurt(damageSources().mobAttack(mob),mob.kind==NavyaContent.Kind.GUARDIAN?10:6))
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,45,0),mob);
            }
        }
        if(age==45)playSound(NavyaSounds.get("roots_recede"),.35F,1);
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { if(owner!=null)tag.putUUID("Owner",owner);tag.putLong("Born",entityData.get(BORN)); }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;entityData.set(BORN,tag.getLong("Born")); }
}
