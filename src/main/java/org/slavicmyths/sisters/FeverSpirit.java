package org.slavicmyths.sisters;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Temporary, owner-bound fever spirit. Does not recruit or damage allied creatures. */
public final class FeverSpirit extends Monster {
    private UUID owner;
    private int life = 20 * 20;
    public FeverSpirit(EntityType<? extends FeverSpirit> type,Level level) { super(type,level); xpReward=0; }
    public static AttributeSupplier.Builder attributes() { return createMonsterAttributes().add(Attributes.MAX_HEALTH,18).add(Attributes.MOVEMENT_SPEED,.34).add(Attributes.ATTACK_DAMAGE,3).add(Attributes.FOLLOW_RANGE,24); }
    public void bind(SisterBoss boss,int slot) { owner=boss.getUUID(); life=20*20 + slot*20; setHealth(getMaxHealth()); }
    public UUID ownerId() { return owner; }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(3,new MeleeAttackGoal(this,1.1,false));
        targetSelector.addGoal(1,new NearestAttackableTargetGoal<>(this,Player.class,true,p -> p instanceof Player player && player.isAlive() && !player.isCreative() && !player.isSpectator()));
    }
    @Override public boolean isAlliedTo(net.minecraft.world.entity.Entity entity) {
        if (entity instanceof SisterBoss || entity instanceof FeverSpirit) return true;
        return super.isAlliedTo(entity);
    }
    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        return target instanceof Player p && !p.isCreative() && !p.isSpectator() && super.doHurtTarget(target);
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (tickCount%4==0) level().addParticle(net.minecraft.core.particles.ParticleTypes.WITCH,getX(),getY()+.6,getZ(),0,.03,0);
            return;
        }
        if (--life<=0 || owner==null || !(level() instanceof ServerLevel server) || !(server.getEntity(owner) instanceof SisterBoss boss) || !boss.isAlive()) discard();
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag);tag.putInt("Remaining",life);if(owner!=null)tag.putUUID("Owner",owner); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag);life=Math.clamp(tag.getInt("Remaining"),0,20*30);owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null; }
}
