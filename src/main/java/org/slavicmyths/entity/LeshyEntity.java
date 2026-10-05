package org.slavicmyths.entity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.ModSounds;

public final class LeshyEntity extends PathfinderMob {
    private int nextAngrySound;
    private long nextEscape;
    @Override public boolean doHurtTarget(net.minecraft.world.entity.Entity target) {
        boolean hit = super.doHurtTarget(target);
        if (hit && !level().isClientSide && target instanceof LivingEntity && random.nextInt(4) == 0)
            ((LivingEntity) target).addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        return hit;
    }
    @Override public boolean hurt(DamageSource damage, float amount) {
        boolean hit = super.hurt(damage, amount);
        if (hit && !level().isClientSide && isAlive() && getHealth() <= 12 && level().getGameTime() >= nextEscape) {
            nextEscape = level().getGameTime() + 600;
            // At most six local candidates; never load a chunk to teleport.
            for (int i = 0; i < 6; i++) {
                double x = getX() + random.nextInt(13) - 6, z = getZ() + random.nextInt(13) - 6;
                net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(x, getY(), z);
                if (!level().hasChunkAt(pos) || !level().getBlockState(pos.below()).isSolid()
                        || !level().getFluidState(pos).isEmpty()) continue;
                if (randomTeleport(x, getY(), z, true)) {
                    ((net.minecraft.server.level.ServerLevel) level()).sendParticles(net.minecraft.core.particles.ParticleTypes.POOF,
                            getX(), getY() + 1, getZ(), 12, 0.4, 0.5, 0.4, 0.02);
                    getNavigation().stop(); break;
                }
            }
        }
        if (hit && damage.getEntity() instanceof Player) org.slavicmyths.progression.Knowledge.award((Player) damage.getEntity(), "meet_leshy");
        return hit;
    }
    @Override public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag); tag.putLong("NextEscape", nextEscape);
    }
    @Override public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag); nextEscape = tag.getLong("NextEscape");
    }
    public LeshyEntity(EntityType<? extends LeshyEntity> type, Level world) { super(type, world); }
    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 32).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.FOLLOW_RANGE, 16).add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.25);
    }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.05, false));
        goalSelector.addGoal(2, new AvoidEntityGoal<Player>(this, Player.class, 5.0F, 0.8, 1.05,
                candidate -> getTarget() == null));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.65));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12.0F, 0.08F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Vanilla target goal checks periodically, only in a bounded follow range.
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>(this, Player.class, 20,
                true, false, candidate -> !level().isDay() && distanceToSqr(candidate) < 9.0));
    }
    @Override public void setTarget(LivingEntity target) {
        if (target != null && getTarget() == null && !level().isClientSide && tickCount >= nextAngrySound) {
            playSound(ModSounds.LESHY_ANGRY.get(), 0.7F, 1.0F);
            nextAngrySound = tickCount + 100;
        }
        super.setTarget(target);
    }
    @Override protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        org.slavicmyths.progression.Knowledge.award(player, "meet_leshy");
        if (!player.getItemInHand(hand).isEmpty()) return super.mobInteract(player, hand);
        return InteractionResult.sidedSuccess(level().isClientSide);
    }
    @Override public boolean removeWhenFarAway(double distance) { return true; }
    @Override protected boolean shouldDespawnInPeaceful() { return true; }
    @Override public int getMaxSpawnClusterSize() { return 1; }
    @Override public int getAmbientSoundInterval() { return 400; }
    @Override public void playAmbientSound() {
        if (!level().isClientSide && level().getNearestPlayer(this, 24) != null)
            playSound(ModSounds.LESHY_AMBIENT.get(), 1.1F, 0.85F + random.nextFloat() * 0.2F);
    }
    @Override protected float getSoundVolume() { return 0.6F; }
    @Override protected SoundEvent getAmbientSound() { return ModSounds.LESHY_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource damage) { return ModSounds.LESHY_HURT.get(); }
    @Override protected SoundEvent getDeathSound() { return ModSounds.LESHY_DEATH.get(); }
}
