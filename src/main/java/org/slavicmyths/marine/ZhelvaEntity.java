package org.slavicmyths.marine;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

/**
 * Zhelva of the Stony Seafloor: fictional guardian inspired by Slavic turtle folklore.
 * Every attack is a finite, synced telegraph with a single server-authoritative impact.
 * The model and multipart collision geometry share a forward-facing head and four flippers.
 */
public final class ZhelvaEntity extends Monster {
    // Aquatic encounters may occupy water; retain block and entity obstruction checks.
    @Override public boolean checkSpawnObstruction(net.minecraft.world.level.LevelReader world) {
        return world.isUnobstructed(this) && world.noCollision(this);
    }

    public static final int IDLE = 0, SWIM = 1, BITE = 2, SWEEP_LEFT = 3, SWEEP_RIGHT = 4,
        PRESSURE_RING = 5, GUARD = 6, WHIRLPOOL = 7, SILT = 8, CHARGE = 9,
        SHELL_SLAM = 10, PRESSURE_LANCE = 11;
    public static final float HEALTH = 540F;
    public static final float BASE_DAMAGE = 14F;
    private static final EntityDataAccessor<Integer> ACTION =
        SynchedEntityData.defineId(ZhelvaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> START =
        SynchedEntityData.defineId(ZhelvaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> PHASE =
        SynchedEntityData.defineId(ZhelvaEntity.class, EntityDataSerializers.INT);
    private final ServerBossEvent bar = new org.slavicmyths.combat.CreatureBossBar(this,
        Component.translatableWithFallback("entity.slavicmyths.zhelva", "Желва Каменного Дна"),
        BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private final Set<UUID> damagedThisAction = new HashSet<>();
    private final Part[] parts = new Part[6];
    private int remainingAction, recovery = 28, selection, dryTicks;
    private Vec3 home, lockedPoint;
    private int lastPhase = 1;

    public static final class Part extends PartEntity<ZhelvaEntity> {
        private final float width;
        private final float height;
        private final boolean head;
        private Part(ZhelvaEntity parent, float width, float height, boolean head) {
            super(parent);
            this.width = width;
            this.height = height;
            this.head = head;
            noPhysics = true;
            refreshDimensions();
        }
        @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {}
        @Override protected void addAdditionalSaveData(CompoundTag tag) {}
        @Override protected void readAdditionalSaveData(CompoundTag tag) {}
        @Override public EntityDimensions getDimensions(Pose pose) {
            return EntityDimensions.scalable(width, height);
        }
        @Override public boolean shouldBeSaved() { return false; }
        @Override public boolean isPickable() { return true; }
        @Override public boolean is(Entity entity) { return entity == this || entity == getParent(); }
        @Override public boolean hurt(DamageSource source, float amount) {
            return getParent().hurtPart(source, amount, head);
        }
    }

    public ZhelvaEntity(EntityType<? extends ZhelvaEntity> type, Level level) {
        super(type, level);
        xpReward = 105;
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(HEALTH);
            setHealth(HEALTH);
        }
        parts[0] = new Part(this, 2.55F, 2.15F, true);
        for (int i = 1; i <= 4; i++) parts[i] = new Part(this, 1.65F, 1.25F, false);
        parts[5] = new Part(this, 1.2F, .85F, false);
    }
    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, HEALTH)
            .add(Attributes.ATTACK_DAMAGE, BASE_DAMAGE)
            .add(Attributes.MOVEMENT_SPEED, .23)
            .add(Attributes.ARMOR, 14)
            .add(Attributes.ARMOR_TOUGHNESS, 2)
            .add(Attributes.FOLLOW_RANGE, 48)
            .add(Attributes.KNOCKBACK_RESISTANCE, .90);
    }
    @Override protected void registerGoals() {}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION, IDLE);
        builder.define(START, 0);
        builder.define(PHASE, 1);
    }
    @Override public boolean isMultipartEntity() { return true; }
    @Override public PartEntity<?>[] getParts() { return parts; }
    @Override public void setId(int id) {
        super.setId(id);
        if (parts != null)
            for (int i = 0; i < parts.length; i++)
                if (parts[i] != null) parts[i].setId(id + i + 1);
    }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public Component getName() {
        return hasCustomName() ? super.getName() :
            Component.translatableWithFallback("entity.slavicmyths.zhelva", "Желва Каменного Дна");
    }
    public int action() { return entityData.get(ACTION); }
    public int actionTicks() {
        return Math.max(0, (int) level().getGameTime() - entityData.get(START));
    }
    public int battlePhase() { return entityData.get(PHASE); }
    public static int phaseAt(float ratio) { return ratio <= .32F ? 3 : ratio <= .66F ? 2 : 1; }
    public boolean exposedHead() {
        int a = action(), ticks = actionTicks();
        return (a == BITE && ticks >= 9 && ticks <= 27)
            || (a == PRESSURE_RING && ticks >= 19 && ticks <= 34)
            || (a == CHARGE && ticks >= 30 && ticks <= 48)
            || (a == SHELL_SLAM && ticks >= 30 && ticks <= 48);
    }
    private Vec3 forward() {
        Vec3 horizontal = getLookAngle().multiply(1, 0, 1);
        return horizontal.lengthSqr() < .0001
            ? Vec3.directionFromRotation(0, getYRot()).multiply(1, 0, 1).normalize()
            : horizontal.normalize();
    }
    private Vec3 side() {
        Vec3 f = forward();
        return new Vec3(f.z, 0, -f.x);
    }
    private void movePart(Part part, Vec3 center) {
        part.setPos(center.x, center.y - part.getBbHeight() * .5, center.z);
    }
    private void updateParts() {
        Vec3 f = forward(), s = side(), center = position();
        movePart(parts[0], center.add(f.scale(3.0)).add(0, 1.48, 0));
        for (int i = 0; i < 4; i++) {
            int sign = i % 2 == 0 ? -1 : 1;
            double longitudinal = i < 2 ? 1.35 : -1.40;
            double stroke = Math.sin(tickCount * .11 + i * 1.7) * .16;
            movePart(parts[i + 1], center.add(f.scale(longitudinal))
                .add(s.scale(sign * (2.36 + stroke))).add(0, .88, 0));
        }
        movePart(parts[5], center.subtract(f.scale(3.15)).add(0, .67, 0));
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
        return super.hurt(source, effectiveDamage(source, amount, false));
    }
    private boolean hurtPart(DamageSource source, float amount, boolean struckHead) {
        return super.hurt(source, effectiveDamage(source, amount, struckHead));
    }
    private float effectiveDamage(DamageSource source, float amount, boolean struckHead) {
        if (amount <= 0) return amount;
        Vec3 origin = source.getSourcePosition();
        boolean frontal = struckHead;
        if (origin != null) {
            Vec3 offset = origin.subtract(position()).multiply(1, 0, 1);
            frontal |= offset.lengthSqr() > .01 && offset.normalize().dot(forward()) > .35;
        }
        // During telegraphed attacks the neck is vulnerable, not the shell.
        if (exposedHead() && frontal) return amount * (struckHead ? 1.65F : 1.38F);
        // Hiding inside the shell is strong, finite defense: no absolute immunity.
        if (action() == GUARD) return amount * .57F;
        // Rear armor is harder, but its protection is far below a serpent's total HP.
        if (!frontal && origin != null) return amount * .84F;
        return amount;
    }
    private boolean valid(LivingEntity entity) {
        return entity != this && entity.isAlive() && !entity.isSpectator()
            && !entity.isInvulnerable() && !(entity instanceof Player player && player.isCreative())
            && !(entity instanceof ZhelvaEntity);
    }
    private void findTarget() {
        if (tickCount % 12 != 0) return;
        LivingEntity old = getTarget();
        if (old != null && (!valid(old) || distanceToSqr(old) > 60 * 60)) setTarget(null);
        Player player = null;
        double best = 48 * 48;
        // Aerial players remain legitimate targets even if no water path exists.
        for (Player candidate : level().getEntitiesOfClass(Player.class,
            getBoundingBox().inflate(48, 32, 48), this::valid)) {
            double distance = distanceToSqr(candidate);
            if (distance < best && (hasLineOfSight(candidate) || distance < 14 * 14)) {
                player = candidate;
                best = distance;
            }
        }
        if (player != null) { setTarget(player); return; }
        if (getTarget() != null) return;
        LivingEntity aquatic = null;
        best = 25 * 25;
        for (LivingEntity candidate : level().getEntitiesOfClass(LivingEntity.class,
            getBoundingBox().inflate(25, 9, 25), this::valid)) {
            if (candidate instanceof Player || !candidate.isInWaterOrBubble()) continue;
            double distance = distanceToSqr(candidate);
            if (distance < best && hasLineOfSight(candidate)) {
                best = distance; aquatic = candidate;
            }
        }
        if (aquatic != null) setTarget(aquatic);
    }
    private void turnTo(Vec3 point) {
        Vec3 delta = point.subtract(position());
        if (delta.horizontalDistanceSqr() < .0001) return;
        float goal = (float) (Mth.atan2(-delta.x, delta.z) * Mth.RAD_TO_DEG);
        setYRot(getYRot() + Mth.clamp(Mth.wrapDegrees(goal - getYRot()), -7F, 7F));
        setYHeadRot(getYRot());
        setYBodyRot(getYRot());
    }
    private Vec3 patrolPoint;
    void swimToward(Vec3 target, double speed) {
        if (!isInWaterOrBubble()) return;
        Vec3 delta = target.subtract(position()).multiply(1, 0, 1);
        if (delta.lengthSqr() < .2) {
            setDeltaMovement(getDeltaMovement().scale(.72));
            return;
        }
        turnTo(target);
        Vec3 step = delta.normalize().scale(speed);
        // Never follow a flying player vertically. Patrol near the seafloor.
        double vertical = home == null ? 0 : Mth.clamp((home.y - getY()) * .025, -.045, .045);
        Vec3 motion = getDeltaMovement().scale(.72).add(step.scale(.28)).add(0, vertical, 0);
        if (!clearSwimMotion(motion)) {
            Vec3 direction=delta.normalize();
            boolean found=false;
            for(double angle:new double[]{Math.PI/4,-Math.PI/4,Math.PI/2,-Math.PI/2,3*Math.PI/4,-3*Math.PI/4}){
                double x=direction.x*Math.cos(angle)-direction.z*Math.sin(angle);
                double z=direction.x*Math.sin(angle)+direction.z*Math.cos(angle);
                Vec3 side=new Vec3(x*Math.min(speed,.18),vertical,z*Math.min(speed,.18));
                if(clearSwimMotion(side)){motion=side;found=true;break;}
            }
            if(!found){setDeltaMovement(Vec3.ZERO);return;}
        }
        setDeltaMovement(motion);
        hasImpulse = true;
    }
    private boolean clearSwimMotion(Vec3 motion){
        var swept=getBoundingBox().expandTowards(motion.scale(4)).deflate(.001);
        // Check loaded bounds before collision queries; steering never generates terrain.
        for(double x:new double[]{swept.minX,swept.maxX})for(double z:new double[]{swept.minZ,swept.maxZ})
            if(!level().hasChunkAt(BlockPos.containing(x,getY(),z)))return false;
        BlockPos destination=BlockPos.containing(position().add(motion.scale(4)));
        return level().getFluidState(destination).is(net.minecraft.tags.FluidTags.WATER)
            &&level().noCollision(this,swept);
    }
    void begin(int next, int duration, Vec3 point) {
        entityData.set(ACTION, next);
        entityData.set(START, (int) level().getGameTime());
        remainingAction = duration;
        damagedThisAction.clear();
        lockedPoint = point;
        if (next != IDLE && next != SWIM) {
            playSound(SoundEvents.ELDER_GUARDIAN_CURSE, .75F, .62F + random.nextFloat() * .12F);
        }
    }
    private void impact(Vec3 center, double horizontal, double height, float damage,
                        double directionalDot, int direction) {
        Vec3 facing = forward();
        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class,
            new AABB(center, center).inflate(horizontal, height, horizontal), this::valid)) {
            Vec3 delta = victim.position().add(0, victim.getBbHeight() * .45, 0).subtract(center);
            Vec3 flat = delta.multiply(1, 0, 1);
            if (Math.abs(delta.y) > height || flat.lengthSqr() > horizontal * horizontal) continue;
            if (flat.lengthSqr() > .1) {
                double facingValue = direction == 1 ? facing.dot(flat.normalize())
                    : direction == -1 ? -facing.dot(flat.normalize())
                    : side().dot(flat.normalize());
                if (direction == 2) facingValue = -facingValue;
                if (facingValue < directionalDot) continue;
            }
            if (!damagedThisAction.add(victim.getUUID())) continue;
            if (victim.hurt(damageSources().mobAttack(this), damage)) {
                Vec3 knockback = flat.lengthSqr() < .01 ? facing : flat.normalize();
                victim.push(knockback.x * .75, .24, knockback.z * .75);
            }
        }
    }
    public static boolean insideWaveBand(double horizontal, double vertical) {
        return horizontal >= 7.0 && horizontal <= 9.4 && Math.abs(vertical) <= 3.1;
    }
    private void waveImpact() {
        Vec3 center = position().add(0, 1, 0);
        for (LivingEntity victim : level().getEntitiesOfClass(LivingEntity.class,
            new AABB(center, center).inflate(9.4, 3.1, 9.4), this::valid)) {
            Vec3 delta = victim.position().add(0, victim.getBbHeight() * .45, 0).subtract(center);
            if (!insideWaveBand(delta.horizontalDistance(), delta.y)) continue;
            if (!damagedThisAction.add(victim.getUUID())) continue;
            if (victim.hurt(damageSources().mobAttack(this), 11F)) {
                Vec3 outward = delta.multiply(1, 0, 1).normalize();
                victim.push(outward.x * 1.0, .3, outward.z * 1.0);
            }
        }
    }
    static Vec3 vortexCenter(Vec3 center, int index) {
        double angle = Math.PI * 2 * index / 3;
        return center.add(Math.cos(angle) * 6.4, 0, Math.sin(angle) * 6.4);
    }
    private void particleRing(ServerLevel server, Vec3 center, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = i * Math.PI * 2 / count;
            server.sendParticles(ParticleTypes.END_ROD,
                center.x + Math.cos(angle) * radius, center.y + .4,
                center.z + Math.sin(angle) * radius, 1, .08, .1, .08, .005);
        }
    }
    private void warning(int ticks, int state) {
        if (!(level() instanceof ServerLevel server) || ticks % 5 != 0) return;
        if (state == PRESSURE_RING || state == SHELL_SLAM)
            particleRing(server, position(), state == PRESSURE_RING ? 8.5 : 5.1, 16);
        else if (state == WHIRLPOOL)
            for (int i = 0; i < 3; i++)
                particleRing(server, vortexCenter(position(), i), 2.3, 8);
        else if (lockedPoint != null && (state == PRESSURE_LANCE || state == SILT))
            particleRing(server, lockedPoint, state == SILT ? 3.8 : 2.6, 12);
        else if (state == BITE || state == SWEEP_LEFT || state == SWEEP_RIGHT || state == CHARGE)
            server.sendParticles(ParticleTypes.SPLASH,
                getX() + forward().x * 3, getY() + 1.5, getZ() + forward().z * 3,
                8, .9, .8, .9, .03);
    }
    private void execute(int state, int ticks) {
        if (state == IDLE || state == SWIM) return;
        warning(ticks, state);
        if (state == BITE && ticks == 21)
            impact(position().add(forward().scale(3.6)).add(0, 1.4, 0), 3.4, 2.9, 16F, -.2, 1);
        if ((state == SWEEP_LEFT || state == SWEEP_RIGHT) && ticks == 25)
            impact(position().add(0, 1, 0), 5.4, 2.6, 13F, .0, state == SWEEP_LEFT ? 0 : 2);
        if (state == PRESSURE_RING && ticks == 32) waveImpact();
        if (state == WHIRLPOOL && ticks == 36) {
            Vec3 at = position();
            for (int i = 0; i < 3; i++)
                impact(vortexCenter(at, i), 2.6, 5.5, 11F, -1, 1);
        }
        if ((state == SILT && ticks == 36) || (state == PRESSURE_LANCE && ticks == 30)) {
            if (lockedPoint != null)
                impact(lockedPoint, state == SILT ? 3.8 : 2.8,
                    state == SILT ? 5.0 : 17, state == SILT ? 12F : 10F, -1, 1);
        }
        if (state == CHARGE) {
            if (ticks >= 19 && ticks <= 38 && lockedPoint != null)
                swimToward(lockedPoint, .31);
            if (ticks == 39)
                impact(position().add(forward().scale(2.8)), 4.2, 3.1, 17F, -.1, 1);
        }
        if (state == SHELL_SLAM && ticks == 41)
            impact(position().add(0, 1.4, 0), 5.7, 3.6, 19F, -1, 1);
    }
    @Override public void aiStep() {
        super.aiStep();
        updateParts();
        if (level().isClientSide) return;
        if (level().getDifficulty() == Difficulty.PEACEFUL) { discard(); return; }
        if (home == null) home = position();
        setNoGravity(isInWaterOrBubble());
        if (!isInWaterOrBubble() && ++dryTicks > 80)
            setDeltaMovement(getDeltaMovement().multiply(.6, 1, .6));
        if (isInWaterOrBubble()) dryTicks = 0;
        int phase = phaseAt(getHealth() / Math.max(1, getMaxHealth()));
        if (phase != lastPhase) {
            lastPhase = phase;
            entityData.set(PHASE, phase);
            bar.setColor(phase == 3 ? BossEvent.BossBarColor.YELLOW : BossEvent.BossBarColor.BLUE);
        }
        bar.setProgress(getHealth() / Math.max(1, getMaxHealth()));
        findTarget();
        if (recovery > 0) recovery--;
        int state = action();
        if (state != IDLE && state != SWIM) {
            execute(state, actionTicks());
            if (--remainingAction <= 0) {
                begin(IDLE, 0, null);
                recovery = phase == 3 ? 15 : phase == 2 ? 23 : 33;
            }
            return;
        }
        LivingEntity target = getTarget();
        if (target == null) {
            if (home != null && distanceToSqr(home) > 12 * 12)
                swimToward(home, .12);
            else if(isInWaterOrBubble()){
                if(patrolPoint==null||distanceToSqr(patrolPoint)<4||tickCount%120==0){
                    patrolPoint=null;
                    for(int attempt=0;attempt<8;attempt++){
                        double angle=random.nextDouble()*Math.PI*2;
                        Vec3 point=home.add(Math.cos(angle)*8,0,Math.sin(angle)*8);
                        BlockPos at=BlockPos.containing(point);
                        if(level().hasChunkAt(at)&&level().getFluidState(at).is(net.minecraft.tags.FluidTags.WATER)&&level().noCollision(this,getBoundingBox().move(point.subtract(position())))){patrolPoint=point;break;}
                    }
                }
                if(patrolPoint!=null)swimToward(patrolPoint,.10);else setDeltaMovement(getDeltaMovement().scale(.84));
            }else setDeltaMovement(getDeltaMovement().scale(.84));
            if (state != IDLE) begin(IDLE, 0, null);
            return;
        }
        turnTo(target.position());
        double horizontal = position().multiply(1, 0, 1)
            .distanceToSqr(target.position().multiply(1, 0, 1));
        double heightDifference = target.getY() - getY();
        if (horizontal > 35 * 35 || Math.abs(heightDifference) >= 26) {
            setTarget(null);
            return;
        }
        if (recovery == 0) {
            // A player using flight cannot force an endless impossible water pursuit.
            if (heightDifference > 5.5 || horizontal > 15 * 15) {
                begin(PRESSURE_LANCE, 44, target.position().add(0, target.getBbHeight() * .45, 0));
                return;
            } else if (horizontal < 6.6 * 6.6) {
                if (phase == 3 && selection % 5 == 0)
                    begin(SHELL_SLAM, 56, null);
                else if (phase >= 2 && selection % 5 == 0)
                    begin(GUARD, 42, null);
                else if (selection % 3 == 0)
                    begin(BITE, 34, null);
                else if (selection % 3 == 1)
                    begin(SWEEP_LEFT, 38, null);
                else
                    begin(SWEEP_RIGHT, 38, null);
                selection++;
                return;
            } else if (horizontal < 13 * 13) {
                if (phase >= 2 && selection % 3 == 0)
                    begin(WHIRLPOOL, 52, null);
                else if (phase == 3 && selection % 3 == 1)
                    begin(CHARGE, 54, target.position());
                else if (phase >= 2 && selection % 3 == 2)
                    begin(SILT, 53, target.position());
                else begin(PRESSURE_RING, 48, null);
                selection++;
                return;
            }
        }
        if (horizontal > 5 * 5 && heightDifference <= 6 && horizontal < 34 * 34)
            swimToward(target.position(), phase == 3 ? .20 : .16);
        else setDeltaMovement(getDeltaMovement().scale(.80));
        if (state != SWIM) begin(SWIM, 0, null);
    }
    @Override public void die(DamageSource source) {
        setNoGravity(false);
        super.die(source);
    }
    @Override public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type) {
        return type != net.neoforged.neoforge.common.NeoForgeMod.WATER_TYPE.value();
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Recovery", recovery);
        tag.putInt("AttackSelection", selection);
        if (home != null) {
            tag.putDouble("HomeX", home.x);
            tag.putDouble("HomeY", home.y);
            tag.putDouble("HomeZ", home.z);
        }
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        recovery = Mth.clamp(tag.getInt("Recovery"), 0, 180);
        selection = Math.max(0, tag.getInt("AttackSelection"));
        if (tag.contains("HomeX"))
            home = new Vec3(tag.getDouble("HomeX"), tag.getDouble("HomeY"), tag.getDouble("HomeZ"));
        entityData.set(PHASE, phaseAt(getHealth() / Math.max(1, getMaxHealth())));
        lastPhase = battlePhase();
        // Never restore a half-resolved strike that could duplicate server damage.
        begin(IDLE, 0, null);
        setNoGravity(false);
    }
}
