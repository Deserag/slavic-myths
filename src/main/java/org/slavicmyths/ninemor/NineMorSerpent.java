package org.slavicmyths.ninemor;

import java.util.Arrays;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.slavicmyths.gorynych.GorynychContent;

/**
 * Summoned nine-headed plague serpent.
 * Raw damage is scaled against vanilla's 1024 max-health attribute ceiling:
 * the effective unarmored boss health is 3000, shown by the boss bar.
 * Independently scheduled heads never exceed three simultaneous attacks.
 */
public final class NineMorSerpent extends Monster {
    public static final float EFFECTIVE_MAX_HEALTH = 3000F;
    private static final float INTERNAL_MAX_HEALTH = 1024F;
    public static final int GRAB = 1, PROJECTILE = 2, BEAM = 3, AREA = 4;
    public static final int[] COLORS = {
        0xBDA58D, 0xA7CA59, 0x9B71B9, 0x76C9DB, 0xE57D45,
        0xE8CF71, 0x648A60, 0xB89050, 0x7E80C5
    };

    @SuppressWarnings("unchecked")
    private static final EntityDataAccessor<Integer>[] HEADS = new EntityDataAccessor[9];
    static {
        for (int i = 0; i < HEADS.length; i++) HEADS[i] =
            SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.INT);
    }
    private static final EntityDataAccessor<Integer> FLIGHT =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> FLIGHT_AGE =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> RITUAL_AGE =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TAIL_MASK =
        SynchedEntityData.defineId(NineMorSerpent.class, EntityDataSerializers.INT);

    private final ServerBossEvent bar = new ServerBossEvent(
        Component.translatable("entity.slavicmyths.nine_mor_serpent"),
        BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_20);
    private final int[] cooldown = new int[9];
    private final int[] tailCooldown = {60, 95, 130};
    private int nextHead;
    private int nextFlight = 450;
    private boolean ritual = true;

    public NineMorSerpent(EntityType<? extends NineMorSerpent> type, Level level) {
        super(type, level);
        xpReward = 150;
        setPersistenceRequired();
        Arrays.fill(cooldown, 20);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, INTERNAL_MAX_HEALTH)
            .add(Attributes.ARMOR, 10).add(Attributes.ARMOR_TOUGHNESS, 6)
            .add(Attributes.MOVEMENT_SPEED, .25).add(Attributes.FLYING_SPEED, .32)
            .add(Attributes.FOLLOW_RANGE, 64).add(Attributes.ATTACK_DAMAGE, 12)
            .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        for (EntityDataAccessor<Integer> head : HEADS) builder.define(head, 0);
        builder.define(FLIGHT, 0);
        builder.define(FLIGHT_AGE, 0);
        builder.define(RITUAL_AGE, 0);
        builder.define(TAIL_MASK, 0);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, .65));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 40F));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true,
            candidate -> candidate instanceof Player player && !player.isCreative() && !player.isSpectator()));
    }

    public int headMode(int head) { return entityData.get(HEADS[head]) & 15; }
    public int headAge(int head) { return entityData.get(HEADS[head]) >>> 4; }
    public int flightPhase() { return entityData.get(FLIGHT); }
    public int flightAge() { return entityData.get(FLIGHT_AGE); }
    public int ritualAge() { return entityData.get(RITUAL_AGE); }
    public boolean isRitual() { return ritualAge() < 60; }
    public int tailMask() { return entityData.get(TAIL_MASK); }

    public void beginRitual() {
        entityData.set(RITUAL_AGE, 0);
        ritual = true;
        setHealth(getMaxHealth());
        setDeltaMovement(Vec3.ZERO);
    }

    @Override public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bar.addPlayer(player);
    }

    @Override public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bar.removePlayer(player);
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        // Independent attacks are not cancelled when the boss takes a hit.
        if (ritual || isDeadOrDying()) return false;
        return super.hurt(source, amount * (INTERNAL_MAX_HEALTH / EFFECTIVE_MAX_HEALTH));
    }

    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel server)) return;
        bar.setProgress(Mth.clamp(getHealth() / INTERNAL_MAX_HEALTH, 0F, 1F));

        if (isDeadOrDying()) { setNoGravity(false); return; }
        if (ritual) {
            int age = ritualAge() + 1;
            entityData.set(RITUAL_AGE, age);
            getNavigation().stop();
            setDeltaMovement(Vec3.ZERO);
            if ((age & 3) == 0) ritualParticles(server, age);
            if (age == 42) sound("roar_ground", 1.6F);
            if (age >= 60) ritual = false;
            return;
        }

        LivingEntity target = getTarget();
        if (target == null || !target.isAlive()
                || target instanceof Player player && (player.isCreative() || player.isSpectator())
                || target.distanceToSqr(this) > 80 * 80) target = null;
        tickFlight(target);
        tickHeads(target);
        tickTails(target);

        if (flightPhase() == 0 && target != null && tickCount % 20 == 0
                && distanceToSqr(target) > 80D) getNavigation().moveTo(target, .85D);
        if (tickCount % 23 == 0 && getDeltaMovement().horizontalDistanceSqr() > .009D
                && onGround()) sound("step_heavy", .7F);
        if (tickCount % 110 == 0 && random.nextFloat() < .7F) sound("ambient_low", .58F);
        if (tickCount % 70 == 0) ambientMor(server);
    }

    private void tickFlight(LivingEntity target) {
        int phase = flightPhase();
        int age = flightAge() + 1;
        if (phase == 0) {
            setNoGravity(false);
            if (target != null && --nextFlight <= 0 && onGround()) {
                nextFlight = 420 + random.nextInt(230);
                flight(1);
                sound("wings_unfold", 1.3F);
            }
            return;
        }
        entityData.set(FLIGHT_AGE, age);
        getNavigation().stop();
        if (phase == 1) {
            setNoGravity(true);
            setDeltaMovement(getDeltaMovement().multiply(.5D, .2D, .5D).add(0D, .105D, 0D));
            if (age >= 25) { flight(2); sound("wings_flap", 1.15F); }
        } else if (phase == 2) {
            setNoGravity(true);
            Vec3 drift = target != null ? target.position().subtract(position()) : Vec3.ZERO;
            drift = new Vec3(Mth.clamp(drift.x * .014D, -.13D, .13D), 0D,
                             Mth.clamp(drift.z * .014D, -.13D, .13D));
            setDeltaMovement(drift.x, Math.sin(tickCount * .11D) * .025D, drift.z);
            if (age % 23 == 0) sound("wings_flap", .8F);
            if (age >= 92) { flight(3); setNoGravity(false); }
        } else {
            setNoGravity(false);
            if (age >= 55 || onGround()) {
                flight(0);
                setDeltaMovement(getDeltaMovement().multiply(.4D, 0D, .4D));
                sound("land", 1.2F);
            }
        }
    }

    private void flight(int phase) {
        entityData.set(FLIGHT, phase);
        entityData.set(FLIGHT_AGE, 0);
    }

    private void tickHeads(LivingEntity target) {
        int active = 0;
        for (int i = 0; i < 9; i++) {
            if (cooldown[i] > 0) cooldown[i]--;
            int mode = headMode(i), age = headAge(i);
            if (mode == 0) continue;
            active++;
            age++;
            entityData.set(HEADS[i], (age << 4) | mode);
            if (age == 17 && mode == AREA && target != null) circle((ServerLevel)level(), i, target.position(), 5.5D, 36);
            if (age == 17) sound(mode == GRAB ? "head_predator_grab" :
                mode == AREA ? "ground_spikes_warn" :
                mode == BEAM ? "head_fire_breath" : "head_mor_shot", .9F);
            if (age == 25 && target != null && target.distanceToSqr(this) < 62 * 62)
                executeHead(i, mode, target);
            if (age >= 38) {
                entityData.set(HEADS[i], 0);
                cooldown[i] = mode == AREA ? 185 + random.nextInt(75) : 82 + random.nextInt(65);
                active--;
            }
        }
        if (target == null || !hasLineOfSight(target)) return;
        for (int attempt = 0; attempt < 9 && active < 3; attempt++) {
            int index = (nextHead + attempt) % 9;
            if (cooldown[index] > 0 || headMode(index) != 0) continue;
            int mode = (index == 0 || index == 7) ? GRAB :
                (index == 2 || index == 5 || index == 8) && random.nextInt(5) == 0 ? AREA :
                (index == 2 || index == 5 || index == 8) ? BEAM : PROJECTILE;
            if (mode == GRAB && distanceToSqr(target) > 100D) continue;
            entityData.set(HEADS[index], (1 << 4) | mode);
            active++;
            nextHead = (index + 1) % 9;
        }
    }

    private void executeHead(int head, int mode, LivingEntity target) {
        if (!target.isAlive()) return;
        if (mode == GRAB) {
            if (distanceToSqr(target) <= 100D && hasLineOfSight(target)) {
                if (target.hurt(damageSources().mobAttack(this), 12F)) {
                    target.knockback(1.4D, getX() - target.getX(), getZ() - target.getZ());
                    applyElement(target, head);
                }
                sound("head_predator_bite", 1.1F);
            }
            return;
        }
        if (mode == AREA) {
            Vec3 center = target.position();
            circle((ServerLevel) level(), head, center, 6.5D, 42);
            areaDamage(center, 5.5D, head, 10F);
            sound("ground_spikes_impact", 1.1F);
        } else if (mode == BEAM) {
            Vec3 origin = headMouth(head), end = target.getEyePosition();
            Vec3 delta = end.subtract(origin);
            if (delta.lengthSqr() < .1D) return;
            Vec3 direction = delta.normalize();
            Vec3 stop = origin.add(direction.scale(Math.min(42, delta.length())));
            int steps = (int)(origin.distanceTo(stop) * 3D);
            for (int n = 0; n <= steps; n += 2) {
                Vec3 point = origin.add(direction.scale(n / 3D));
                coloredParticle((ServerLevel)level(), head, point, 2);
            }
            for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class,
                    new AABB(origin, stop).inflate(2D), candidate ->
                    candidate != this && candidate.isAlive() && !(candidate instanceof NineMorSerpent))) {
                Vec3 p = victim.getBoundingBox().getCenter();
                double progress = Mth.clamp(p.subtract(origin).dot(direction), 0D, origin.distanceTo(stop));
                if (p.distanceToSqr(origin.add(direction.scale(progress))) < 2.6D) {
                    if (victim.hurt(damageSources().mobAttack(this), 9F)) applyElement(victim, head);
                }
            }
        } else {
            NineMorShot shot = new NineMorShot(level(), this, head);
            Vec3 origin = headMouth(head);
            Vec3 direction = target.getEyePosition().subtract(origin).normalize();
            shot.setPos(origin.x, origin.y, origin.z);
            shot.shoot(direction.x, direction.y, direction.z, 1.32F, 1.0F);
            level().addFreshEntity(shot);
            coloredParticle((ServerLevel)level(), head, origin, 14);
            if (head == 4) sound("head_fireball", 1F);
        }
    }

    private Vec3 headMouth(int head) {
        // Ordered to match the model's nine separate necks, model-scale pixels / 16.
        double[] x = {-12.7D, 0, 12.7D, -14.7D, 0, 14.7D, -8.1D, 0, 8.1D};
        double[] h = {30.5D, 31.6D, 30.5D, 38.6D, 39.3D, 38.6D, 46.8D, 48.8D, 46.8D};
        double[] z = {-24.4D, -26.2D, -24.4D, -20.8D, -21.9D, -20.8D, -16.7D, -17.3D, -16.7D};
        double yaw = -getYRot() * Math.PI / 180D;
        double forward = z[head] / 16D;
        double side = x[head] / 16D;
        return position().add(side * Math.cos(yaw) - forward * Math.sin(yaw),
            h[head] / 16D, side * Math.sin(yaw) + forward * Math.cos(yaw));
    }

    private void tickTails(LivingEntity target) {
        int mask = 0;
        for (int i = 0; i < 3; i++) {
            if (tailCooldown[i] > 0) tailCooldown[i]--;
            if (target != null && tailCooldown[i] <= 0 && distanceToSqr(target) < 75D) {
                tailCooldown[i] = 95 + i * 17 + random.nextInt(30);
                if (target.hurt(damageSources().mobAttack(this), 7.0F))
                    target.knockback(.8D, getX() - target.getX(), getZ() - target.getZ());
                sound("tail_sweep", .8F);
            }
            if (tailCooldown[i] > 85 && tailCooldown[i] < 107) mask |= 1 << i;
        }
        entityData.set(TAIL_MASK, mask);
    }

    private void ambientMor(ServerLevel server) {
        for (LivingEntity living : server.getEntitiesOfClass(LivingEntity.class,
                getBoundingBox().inflate(7D), e -> e != this && e.isAlive() && !(e instanceof NineMorSerpent))) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 90, 0));
        }
        coloredParticle(server, 2, position().add(0, 2D, 0), 26);
    }

    private void areaDamage(Vec3 center, double radius, int head, float damage) {
        AABB area = new AABB(center, center).inflate(radius, 3D, radius);
        for (LivingEntity e : level().getEntitiesOfClass(LivingEntity.class, area,
                candidate -> candidate != this && candidate.isAlive() && !(candidate instanceof NineMorSerpent))) {
            if (e.distanceToSqr(center) <= radius * radius
                    && e.hurt(damageSources().mobAttack(this), damage)) applyElement(e, head);
        }
    }

    public static void applyElement(LivingEntity victim, int head) {
        switch (head) {
            case 1, 6 -> victim.addEffect(new MobEffectInstance(MobEffects.POISON, 90, 0));
            case 2 -> victim.addEffect(new MobEffectInstance(MobEffects.WITHER, 75, 0));
            case 3 -> victim.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 85, 1));
            case 4 -> victim.igniteForSeconds(3);
            case 5 -> victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 85, 1));
            case 7 -> victim.addEffect(new MobEffectInstance(MobEffects.HUNGER, 155, 1));
            case 8 -> victim.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 65, 0));
            default -> victim.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 45, 0));
        }
    }

    private void circle(ServerLevel server, int head, Vec3 center, double radius, int count) {
        for (int j = 0; j < count; j++) {
            double a = j * 2D * Math.PI / count;
            coloredParticle(server, head, center.add(Math.cos(a) * radius, .15D, Math.sin(a) * radius), 2);
        }
    }

    private void ritualParticles(ServerLevel server, int age) {
        for (int i = 0; i < 9; i++) {
            double theta = (i / 9D) * 2D * Math.PI + age * .026D;
            double radius = 4D * (1D - Math.min(1D, age / 65D) * .42D);
            coloredParticle(server, i, position().add(Math.cos(theta) * radius,
                .22D + age * .036D, Math.sin(theta) * radius), 7);
        }
    }

    private void coloredParticle(ServerLevel server, int head, Vec3 at, int count) {
        int color = COLORS[head];
        server.sendParticles(new DustParticleOptions(
            new Vector3f(((color >> 16) & 255) / 255F,
                ((color >> 8) & 255) / 255F, (color & 255) / 255F), 1.2F),
            at.x, at.y, at.z, count, .20, .21, .20, .02);
    }

    private void sound(String sound, float volume) {
        var key = GorynychContent.SOUNDS.get(sound);
        if (key != null) level().playSound(null, blockPosition(),
            key.get(), SoundSource.HOSTILE, volume, .8F + random.nextFloat() * .22F);
    }

    @Override protected net.minecraft.sounds.SoundEvent getHurtSound(DamageSource source) {
        return GorynychContent.SOUNDS.get("head_predator_grab").get();
    }

    @Override protected net.minecraft.sounds.SoundEvent getDeathSound() {
        return GorynychContent.SOUNDS.get("purified_roar").get();
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("RitualAge", ritualAge());
        tag.putBoolean("Ritual", ritual);
        tag.putInt("NextFlight", nextFlight);
        tag.putInt("NextHead", nextHead);
        for (int i = 0; i < 9; i++) {
            tag.putInt("Head"+i, entityData.get(HEADS[i]));
            tag.putInt("HeadCooldown"+i, cooldown[i]);
        }
        for (int i = 0; i < 3; i++) tag.putInt("TailCooldown"+i, tailCooldown[i]);
    }

    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(RITUAL_AGE, tag.getInt("RitualAge"));
        ritual = tag.getBoolean("Ritual");
        nextFlight = Math.max(100, tag.getInt("NextFlight"));
        nextHead = Math.floorMod(tag.getInt("NextHead"), 9);
        for (int i = 0; i < 9; i++) {
            entityData.set(HEADS[i], 0);
            cooldown[i] = Math.max(5, tag.getInt("HeadCooldown"+i));
        }
        for (int i = 0; i < 3; i++) tailCooldown[i] = Math.max(25, tag.getInt("TailCooldown"+i));
        flight(0);
        setNoGravity(false);
    }
}
