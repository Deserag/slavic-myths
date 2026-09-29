package org.slavicmyths.entity;

import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import org.slavicmyths.registry.ModSounds;

public final class LeshyEntity extends CreatureEntity {
    private int nextAngrySound;
    private long nextEscape;
    @Override public boolean doHurtTarget(net.minecraft.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && !level.isClientSide && target instanceof LivingEntity && random.nextInt(4) == 0)
            ((LivingEntity) target).addEffect(new net.minecraft.potion.EffectInstance(net.minecraft.potion.Effects.MOVEMENT_SLOWDOWN, 60, 0));
        return hit;
    }
    @Override public boolean hurt(DamageSource damage, float amount) {
        boolean hit = super.hurt(damage, amount);
        if (hit && !level.isClientSide && isAlive() && getHealth() <= 12 && level.getGameTime() >= nextEscape) {
            nextEscape = level.getGameTime() + 600;
            // At most six local candidates; never load a chunk to teleport.
            for (int i = 0; i < 6; i++) {
                double x = getX() + random.nextInt(13) - 6, z = getZ() + random.nextInt(13) - 6;
                net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, getY(), z);
                if (!level.hasChunkAt(pos) || !level.getBlockState(pos.below()).getMaterial().isSolid()
                        || !level.getFluidState(pos).isEmpty()) continue;
                if (randomTeleport(x, getY(), z, true)) {
                    ((net.minecraft.world.server.ServerWorld) level).sendParticles(net.minecraft.particles.ParticleTypes.POOF,
                            getX(), getY() + 1, getZ(), 12, 0.4, 0.5, 0.4, 0.02);
                    getNavigation().stop(); break;
                }
            }
        }
        if (hit && damage.getEntity() instanceof PlayerEntity) org.slavicmyths.progression.Knowledge.award((PlayerEntity) damage.getEntity(), "meet_leshy");
        return hit;
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundNBT tag) {
        super.addAdditionalSaveData(tag); tag.putLong("NextEscape", nextEscape);
    }
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundNBT tag) {
        super.readAdditionalSaveData(tag); nextEscape = tag.getLong("NextEscape");
    }
    public LeshyEntity(EntityType<? extends LeshyEntity> type, World world) { super(type, world); }
    public static AttributeModifierMap.MutableAttribute attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 32).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 16).add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new SwimGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.05, false));
        goalSelector.addGoal(2, new AvoidEntityGoal<PlayerEntity>(this, PlayerEntity.class, 5.0F, 0.8, 1.05,
                candidate -> getTarget() == null));
        goalSelector.addGoal(3, new WaterAvoidingRandomWalkingGoal(this, 0.65));
        goalSelector.addGoal(4, new LookAtGoal(this, PlayerEntity.class, 12.0F, 0.08F));
        goalSelector.addGoal(5, new LookRandomlyGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Vanilla target goal checks periodically, only in a bounded follow range.
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<PlayerEntity>(this, PlayerEntity.class, 20,
                true, false, candidate -> !level.isDay() && distanceToSqr(candidate) < 9.0));
    }
    @Override public void setTarget(LivingEntity target) {
        if (target != null && getTarget() == null && !level.isClientSide && tickCount >= nextAngrySound) {
            playSound(ModSounds.LESHY_ANGRY.get(), 0.7F, 1.0F);
            nextAngrySound = tickCount + 100;
        }
        super.setTarget(target);
    }
    @Override protected ActionResultType mobInteract(PlayerEntity player, Hand hand) {
        org.slavicmyths.progression.Knowledge.award(player, "meet_leshy");
        if (!player.getItemInHand(hand).isEmpty()) return super.mobInteract(player, hand);
        return ActionResultType.sidedSuccess(level.isClientSide);
    }
    @Override public boolean removeWhenFarAway(double distance) { return true; }
    @Override protected boolean shouldDespawnInPeaceful() { return true; }
    @Override public int getMaxSpawnClusterSize() { return 1; }
    @Override public int getAmbientSoundInterval() { return 400; }
    @Override public void playAmbientSound() {
        if (!level.isClientSide && level.getNearestPlayer(this, 24) != null)
            playSound(ModSounds.LESHY_AMBIENT.get(), 1.1F, 0.85F + random.nextFloat() * 0.2F);
    }
    @Override protected float getSoundVolume() { return 0.6F; }
    @Override protected SoundEvent getAmbientSound() { return ModSounds.LESHY_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource damage) { return ModSounds.LESHY_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.LESHY_DEATH.get(); }
}
