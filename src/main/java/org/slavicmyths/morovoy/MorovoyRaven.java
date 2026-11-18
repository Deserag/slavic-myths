package org.slavicmyths.morovoy;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Server-authoritative phase flight controller. No noclip, teleport recovery, chunk
 * tickets, permanent block edits, or dependence on client animation timing.
 */
public final class MorovoyRaven extends PathfinderMob {
    public static final int CIRCLE = 0, HOVER = 1, PROJECTILE = 2, SUMMON = 3, DIVE = 4, RECOVER = 5, BEAK = 6, CLAWS = 7, WING = 8, PLAGUE = 9;
    public static final int LANDING = 10, GROUND_IDLE = 11, TAKEOFF = 12;
    private static final EntityDataAccessor<Integer> POSE = SynchedEntityData.defineId(MorovoyRaven.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> POSE_START = SynchedEntityData.defineId(MorovoyRaven.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> BANK = SynchedEntityData.defineId(MorovoyRaven.class, EntityDataSerializers.FLOAT);
    private final ServerBossEvent bossBar = new ServerBossEvent(Component.translatable("entity.slavicmyths.morovoy_raven"),org.slavicmyths.combat.BossBarPalette.colorFor(getType(),BossEvent.BossBarColor.GREEN), BossEvent.BossBarOverlay.PROGRESS);
    private BlockPos home;
    private UUID parent;
    private UUID staffOwner;
    private UUID staffTarget;
    private int phaseTicks, attackIndex, quietTicks, activeTicks, minionLife;
    private long nextAttack;
    private double orbitAngle;
    private Vec3 waypoint = Vec3.ZERO;
    private boolean strikeDone;

    public MorovoyRaven(EntityType<? extends MorovoyRaven> type, Level level) {
        super(type, level);
        setNoGravity(true);
        moveControl = new FlyingMoveControl(this, 24, true);
        xpReward = 0; // Rewards remain deliberately unbalanced/unimplemented (#392).
    }
    public boolean minion() { return getType() == MorovoyRavenContent.MINION.get(); }
    public static AttributeSupplier.Builder attributes(boolean minion) {
        return createMobAttributes().add(Attributes.MAX_HEALTH, minion ? 28 : 1600).add(Attributes.MOVEMENT_SPEED, .28).add(Attributes.FLYING_SPEED, minion ? .65 : .5).add(Attributes.FOLLOW_RANGE, minion ? 28 : 68).add(Attributes.ATTACK_DAMAGE, minion ? 4 : 13).add(Attributes.KNOCKBACK_RESISTANCE, minion ? .1 : .8);
    }
    @Override protected PathNavigation createNavigation(Level world) {
        FlyingPathNavigation path = new FlyingPathNavigation(this, world);
        path.setCanFloat(true);
        path.setCanOpenDoors(false);
        return path;
    }
    @Override protected void registerGoals() { /* A single bounded flight scheduler owns movement. */ }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(POSE, CIRCLE);
        builder.define(POSE_START, 0);
        builder.define(BANK, 0F);
    }
    public int pose() { return entityData.get(POSE); }
    public float bank() { return entityData.get(BANK); }
    public float animationAge(float partial) { return Math.max(0F, tickCount + partial - entityData.get(POSE_START)); }
    public UUID parent() { return parent; }
    /** Player-aligned minion; boss minions keep their original independent AI. */
    public void attachStaff(ServerPlayer player, LivingEntity preferred, int slot) {
        staffOwner=player.getUUID();
        staffTarget=preferred==null?null:preferred.getUUID();
        parent=null;
        minionLife=20*30;
        nextAttack=player.serverLevel().getGameTime()+12+slot*6;
        setNoGravity(true);
        setHealth(getMaxHealth());
    }
    public boolean summonedBy(ServerPlayer player) {return staffOwner!=null&&staffOwner.equals(player.getUUID());}
    public void attach(MorovoyRaven boss, int slot) {
        parent = boss.getUUID();
        home = boss.home;
        minionLife = 20 * 75;
        nextAttack = level().getGameTime() + 35 + slot * 9;
        setHealth(getMaxHealth());
    }
    public void setArenaAnchor(BlockPos anchor) {
        if (anchor != null && home == null && !minion()) home = anchor.immutable();
    }
    private void pose(int next) {
        if (pose() == next) return;
        entityData.set(POSE, next);
        entityData.set(POSE_START, tickCount);
        phaseTicks = 0;
        strikeDone = false;
    }
    private boolean valid(LivingEntity target) {
        return target instanceof Player p && p.isAlive() && !p.isSpectator() && !p.isCreative() && !isAlliedTo(p);
    }
    @Override public boolean isAlliedTo(Entity other) {
        if(staffOwner!=null&&(staffOwner.equals(other.getUUID())||other instanceof MorovoyRaven bird&&staffOwner.equals(bird.staffOwner)))return true;
        if (other instanceof MorovoyRaven bird) return (minion() && parent != null && parent.equals(bird.getUUID())) || (!minion() && getUUID().equals(bird.parent)) || (parent != null && parent.equals(bird.parent));
        return super.isAlliedTo(other);
    }
    @Override public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (!minion()) bossBar.addPlayer(player);
    }
    @Override public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }
    @Override public void tick() {
        super.tick();
        if (level().isClientSide || !(level() instanceof ServerLevel world) || !isAlive()) return;
        if (isNoAi()) { setDeltaMovement(Vec3.ZERO); return; } // Isolated true-model QA, no fake AI motion.
        if (home == null && !minion()) home = blockPosition().immutable();
        if (minion()) { if(staffOwner!=null)tickStaffMinion(world);else tickMinion(world); return; }
        bossBar.setProgress(Math.max(0, Math.min(1, getHealth() / getMaxHealth())));
        activeTicks++;
        // Stock Minecraft wing foley only; original boss calls/death audio remain unapproved.
        if (pose() != LANDING && pose() != GROUND_IDLE && tickCount % 28 == 0) playSound(SoundEvents.PHANTOM_FLAP, 2.4F, .73F);
        if (tickCount % 20 == 0) {
            LivingEntity current = getTarget();
            if (!valid(current) || distanceToSqr(current) > 72 * 72) {
                ServerPlayer chosen = world.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(64, 35, 64), this::valid).stream().min((a,b) -> Double.compare(distanceToSqr(a), distanceToSqr(b))).orElse(null);
                setTarget(chosen);
            }
        }
        LivingEntity target = getTarget();
        if (!valid(target)) {
            setTarget(null);
            quietTicks++;
            Vec3 landing = landingSpot(world);
            if (pose() == GROUND_IDLE) {
                setNoGravity(false);
                brake();
                return;
            }
            if (landing != null && (pose() == LANDING || quietTicks >= 120)) {
                if (pose() != LANDING) pose(LANDING);
                setNoGravity(false);
                Vec3 approach = new Vec3(landing.x, Math.max(landing.y + .3, getY() - .65), landing.z);
                steer(world, approach, .23);
                double horizontal = (getX() - landing.x) * (getX() - landing.x) + (getZ() - landing.z) * (getZ() - landing.z);
                if (horizontal < 4 && Math.abs(getY() - landing.y) < .65) {
                    setDeltaMovement(Vec3.ZERO);
                    pose(GROUND_IDLE);
                    playSound(SoundEvents.IRON_GOLEM_STEP, 3F, .65F);
                }
                return;
            }
            setNoGravity(true);
            pose(CIRCLE);
            if (home != null && quietTicks % 10 == 0) waypoint = Vec3.atCenterOf(home).add(0, 16, 0);
            steer(world, waypoint, .28);
            return;
        }
        quietTicks = 0;
        if (pose() == LANDING || pose() == GROUND_IDLE) {
            setNoGravity(true);
            pose(TAKEOFF);
            playSound(SoundEvents.PHANTOM_FLAP, 3.5F, .78F);
        }
        phaseTicks++;
        getLookControl().setLookAt(target, 22, 22);
        if (pose() == TAKEOFF) {
            Vec3 lift = Vec3.atCenterOf(home == null ? blockPosition() : home).add(0, 17, 0);
            steer(world, lift, .39);
            if (phaseTicks >= 35 && getY() > lift.y - 7) pose(CIRCLE);
            else if (phaseTicks >= 95) pose(CIRCLE); // bounded recovery if ceiling blocks ascent
            return;
        }
        if (pose() == CIRCLE || pose() == PLAGUE) {
            // Orbit is relative to the persisted arena anchor, not a moving player.
            Vec3 center = home == null ? position() : Vec3.atCenterOf(home);
            // A moving target must not outpace the actual bird: v = radius * angularSpeed.
            // Keep the phase continuous through pose switches and save/reload.
            orbitAngle = (orbitAngle + (pose() == PLAGUE ? .010 : .012)) % (Math.PI * 2);
            double theta = orbitAngle + (getUUID().getLeastSignificantBits() & 7) * .13;
            double radius = pose() == PLAGUE ? 38 : 23;
            waypoint = center.add(Math.cos(theta) * radius, 17 + Math.sin(theta * .75) * 4, Math.sin(theta) * radius);
            steer(world, waypoint, .42);
            if (pose() == PLAGUE && phaseTicks % 9 == 0) placeMiasma(world, position().add(0, -8, 0), 4.8F, 200);
            // 720 ticks at .010 rad/tick is more than a full lap; 165 ticks was only a quarter circle.
            if (pose() == PLAGUE && phaseTicks >= 720) { pose(RECOVER); nextAttack = world.getGameTime() + 55; }
            else if (pose() == CIRCLE && world.getGameTime() >= nextAttack) pose(HOVER);
        } else if (pose() == HOVER) {
            steer(world, target.position().add(0, 14, 0), .25);
            if (phaseTicks >= 26) {
                int attack = attackIndex++ % 5;
                pose(switch (attack) { case 0 -> PROJECTILE; case 1 -> SUMMON; case 2 -> DIVE; case 3 -> PLAGUE; default -> WING; });
                if (pose() == DIVE) { waypoint = target.position().add(0, 1.3, 0); playSound(SoundEvents.PHANTOM_SWOOP, 3F, .72F); }
            }
        } else if (pose() == PROJECTILE) {
            brake();
            if (phaseTicks == 12 || phaseTicks == 24 || phaseTicks == 36) shoot(world, target);
            if (phaseTicks >= 48) finishAttack(world, 65);
        } else if (pose() == SUMMON) {
            brake();
            if (phaseTicks == 24) { playSound(SoundEvents.EVOKER_PREPARE_SUMMON, 2.6F, .8F); summon(world); }
            if (phaseTicks >= 55) finishAttack(world, 80);
        } else if (pose() == DIVE) {
            if (phaseTicks < 15) { brake(); waypoint = target.position().add(0, .9, 0); }
            else {
                steer(world, waypoint, .72);
                if (phaseTicks >= 18 && !strikeDone && getBoundingBox().inflate(.8, .6, .8).intersects(target.getBoundingBox())) hit(target, 17, .6F);
            }
            if (phaseTicks >= 43 || phaseTicks > 15 && (horizontalCollision || verticalCollision)) {
                pose(strikeDone ? RECOVER : BEAK);
            }
        } else if (pose() == BEAK) {
            steer(world, target.position().add(0, 1.5, 0), .23);
            if (phaseTicks == 13) hitCone(target, 3.4, .55, 13, .7F);
            if (phaseTicks >= 24) pose(CLAWS);
        } else if (pose() == CLAWS) {
            brake();
            if (phaseTicks == 14) hitCone(target, 3.7, .35, 15, .7F);
            if (phaseTicks >= 27) finishAttack(world, 70);
        } else if (pose() == WING) {
            brake();
            if (phaseTicks >= 11 && phaseTicks <= 19 && phaseTicks % 2 == 1) wingTelegraph(world);
            if (phaseTicks == 19) { playSound(SoundEvents.PHANTOM_FLAP, 3.4F, .66F); wingSweep(world); }
            if (phaseTicks >= 36) finishAttack(world, 72);
        } else if (pose() == RECOVER) {
            Vec3 center = home == null ? position() : Vec3.atCenterOf(home).add(0, 14, 0);
            steer(world, center, .35);
            if (phaseTicks >= 36) pose(CIRCLE);
        }
    }
    private Vec3 landingSpot(ServerLevel world) {
        if (home == null || !world.hasChunkAt(home)) return null;
        Vec3 center = Vec3.atCenterOf(home);
        Vec3 top = center.add(0, 14, 0), bottom = center.add(0, -29, 0);
        if (!world.hasChunkAt(BlockPos.containing(bottom))) return null;
        net.minecraft.world.phys.BlockHitResult hit = world.clip(new ClipContext(top, bottom, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.BLOCK ? new Vec3(center.x, hit.getLocation().y + .05, center.z) : null;
    }
    private void tickStaffMinion(ServerLevel world) {
        if(staffOwner==null||!(world.getEntity(staffOwner) instanceof ServerPlayer player)||!player.isAlive()
                ||player.isSpectator()||--minionLife<=0||distanceToSqr(player)>48*48){discard();return;}
        LivingEntity target=staffTarget==null?null:world.getEntity(staffTarget) instanceof LivingEntity mob?mob:null;
        if(target==null||!target.isAlive()||target.distanceToSqr(player)>32*32||player.isAlliedTo(target)
                ||!org.slavicmyths.rpg.Abilities.canHit(player,target)) {
            target=player.getLastHurtMob();
            if(target==null||!target.isAlive()||target.distanceToSqr(player)>32*32||player.isAlliedTo(target)
                    ||!org.slavicmyths.rpg.Abilities.canHit(player,target)) {
                target=world.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class,player.getBoundingBox().inflate(25),
                    mob->mob.isAlive()&&!player.isAlliedTo(mob)&&org.slavicmyths.rpg.Abilities.canHit(player,mob))
                    .stream().min(java.util.Comparator.comparingDouble(mob->mob.distanceToSqr(player))).orElse(null);
            }
            staffTarget=target==null?null:target.getUUID();
        }
        LivingEntity chased=target;
        double theta=world.getGameTime()*.1+(getUUID().getLeastSignificantBits()&15);
        if(chased!=null) {
            Vec3 at=chased.position().add(Math.sin(theta)*2.2,1.8+Math.sin(theta*.5)*.4,Math.cos(theta)*2.2);
            steer(world,at,.58);
            if(distanceToSqr(chased)<12.25&&world.getGameTime()>=nextAttack&&hasLineOfSight(chased)){
                if(chased.hurt(player.damageSources().playerAttack(player),3.5F))
                    chased.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,45,0),player);
                nextAttack=world.getGameTime()+35;
            }
        }else steer(world,player.position().add(Math.sin(theta)*2.5,2.4,Math.cos(theta)*2.5),.48);
    }
    private void tickMinion(ServerLevel world) {
        if (parent == null || !(world.getEntity(parent) instanceof MorovoyRaven boss) || !boss.isAlive() || (--minionLife <= 0) || distanceToSqr(boss) > 90 * 90) { discard(); return; }
        if (tickCount % 15 == 0) setTarget(valid(boss.getTarget()) ? boss.getTarget() : null);
        LivingEntity target = getTarget();
        if (valid(target)) {
            double angle = world.getGameTime() * .085 + (getUUID().getLeastSignificantBits() & 15);
            Vec3 at = target.position().add(Math.sin(angle) * 4, 3.2 + Math.sin(angle * .3), Math.cos(angle) * 4);
            steer(world, at, .54);
            if (distanceToSqr(target) < 5 && world.getGameTime() >= nextAttack && hasLineOfSight(target)) {
                if (target.hurt(damageSources().mobAttack(this), 4)) target.addEffect(new MobEffectInstance(MobEffects.POISON, 50, 0));
                nextAttack = world.getGameTime() + 40;
            }
        } else {
            double angle = world.getGameTime() * .045 + (getUUID().getLeastSignificantBits() & 15);
            steer(world, boss.position().add(Math.sin(angle) * 8, 3, Math.cos(angle) * 8), .4);
        }
    }
    private void steer(ServerLevel world, Vec3 goal, double speed) {
        Vec3 difference = goal.subtract(position());
        if (difference.lengthSqr() < .16) { brake(); return; }
        Vec3 forward = difference.normalize().scale(Math.min(speed, difference.length() * .18));
        Vec3 chosen = safe(world, forward) ? forward : null;
        if (chosen == null) {
            Vec3 lateral = new Vec3(-forward.z, 0, forward.x);
            Vec3[] alternatives = { forward.add(0, .5, 0), forward.add(0, -.45, 0), lateral.add(0, .18, 0), lateral.scale(-1).add(0, .18, 0) };
            for (Vec3 alternative : alternatives) if (safe(world, alternative)) { chosen = alternative; break; }
        }
        if (chosen == null) { brake(); return; }
        Vec3 movement = getDeltaMovement().scale(.52).add(chosen.scale(.48));
        if (safe(world, movement)) setDeltaMovement(movement);
        else setDeltaMovement(chosen.scale(.35));
        // Bank from the actual signed yaw change: chosen and difference are
        // parallel when unobstructed, so their cross-product is always zero.
        float desiredYaw = (float)Math.toDegrees(Math.atan2(-chosen.x, chosen.z));
        float turn = Math.clamp(net.minecraft.util.Mth.wrapDegrees(desiredYaw - getYRot()), -9F, 9F);
        float newYaw = getYRot() + turn;
        setYRot(newYaw);
        yBodyRot = yHeadRot = newYaw;
        float oldBank = entityData.get(BANK);
        float nextBank = oldBank * .72F + Math.clamp(turn * .038F, -.40F, .40F) * .28F;
        if (Math.abs(nextBank - oldBank) > .002F) entityData.set(BANK, nextBank);
    }
    private boolean safe(ServerLevel world, Vec3 step) {
        // Predicts the broad four-wing flight envelope, not just the body hitbox.
        if (!world.hasChunkAt(BlockPos.containing(position().add(step.scale(3)))) || !world.getWorldBorder().isWithinBounds(BlockPos.containing(position().add(step.scale(3))))) return false;
        boolean landing = pose() == LANDING;
        // Expanded feather fans reach almost nine blocks horizontally. Takeoff also has opened wings.
        double reach = minion() ? 2.4 : landing ? 5.5 : 9.0;
        // A landing envelope must not dip into the legitimate floor by artificial vertical inflation.
        AABB envelope = getBoundingBox().move(step.scale(3)).inflate(reach, minion() ? .15 : landing ? 0 : .2, reach);
        if (!minion() && !landing) envelope = envelope.expandTowards(0, 2.7, 0);
        if (!world.hasChunkAt(BlockPos.containing(envelope.minX, getY(), envelope.minZ)) || !world.hasChunkAt(BlockPos.containing(envelope.maxX, getY(), envelope.maxZ)) || !world.hasChunkAt(BlockPos.containing(envelope.maxX, getY(), envelope.minZ)) || !world.hasChunkAt(BlockPos.containing(envelope.minX, getY(), envelope.maxZ))) return false;
        return world.noCollision(this, envelope) && world.clip(new ClipContext(position(), position().add(step.scale(3)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.MISS;
    }
    private void brake() {
        setDeltaMovement(getDeltaMovement().scale(.68));
        float oldBank = entityData.get(BANK);
        if (Math.abs(oldBank) > .002F) entityData.set(BANK, oldBank * .78F);
    }
    private void finishAttack(ServerLevel world, int cooldown) { pose(RECOVER); nextAttack = world.getGameTime() + cooldown; }
    private void hit(LivingEntity victim, float damage, float knockback) {
        strikeDone = true;
        if (!valid(victim) || !hasLineOfSight(victim)) return;
        if (victim.hurt(damageSources().mobAttack(this), damage)) {
            victim.addEffect(new MobEffectInstance(MobEffects.POISON, 70, 0));
            victim.knockback(knockback, victim.getX() - getX(), victim.getZ() - getZ());
        }
    }
    private void hitCone(LivingEntity target, double reach, double dotMinimum, float damage, float knockback) {
        Vec3 diff = target.getBoundingBox().getCenter().subtract(position());
        Vec3 facing = getLookAngle();
        if (!strikeDone && diff.lengthSqr() < reach * reach && diff.normalize().dot(facing) > dotMinimum) hit(target, damage, knockback);
    }
    private void wingTelegraph(ServerLevel world) {
        Vec3 facing = getLookAngle();
        Vec3 forward = new Vec3(facing.x, 0, facing.z);
        if (forward.lengthSqr() < .0001) forward = new Vec3(0, 0, 1);
        forward = forward.normalize();
        Vec3 lateral = new Vec3(forward.z, 0, -forward.x);
        // A visible warning follows the feather span; geometry and hit sector use the same lateral axis.
        for (int side : new int[]{-1, 1}) for (int segment = 2; segment <= 8; segment += 2) {
            Vec3 point = getBoundingBox().getCenter().add(lateral.scale(side * segment)).add(forward.scale(.35 + segment * .08));
            world.sendParticles(org.slavicmyths.mor.MorContent.SPARK.get(), point.x, point.y, point.z, 2, .18, .12, .18, .01);
        }
    }
    private void wingSweep(ServerLevel world) {
        strikeDone = true;
        Vec3 look = getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0, look.z);
        if (horizontal.lengthSqr() < .0001) horizontal = new Vec3(0, 0, 1);
        horizontal = horizontal.normalize();
        Vec3 lateral = new Vec3(horizontal.z, 0, -horizontal.x);
        // The strike runs along the two feather fans, not an invisible 360-degree cube.
        for (ServerPlayer player : world.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(8.0, 3.2, 8.0), this::valid)) {
            Vec3 relative = player.getBoundingBox().getCenter().subtract(getBoundingBox().getCenter());
            double across = Math.abs(relative.dot(lateral));
            double forward = relative.dot(horizontal);
            if (across >= 1.0 && across <= 8.2 && Math.abs(forward) <= 3.5 && Math.abs(relative.y) <= 3.2 && hasLineOfSight(player)) hit(player, 11, 1.5F);
        }
    }
    private void shoot(ServerLevel world, LivingEntity target) {
        if (!valid(target) || !hasLineOfSight(target)) return;
        RavenBlightBolt bolt = MorovoyRavenContent.BOLT.get().create(world);
        if (bolt == null) return;
        Vec3 origin = position().add(getLookAngle().scale(2)).add(0, 1, 0);
        bolt.setPos(origin.x, origin.y, origin.z);
        bolt.configure(this, 8);
        Vec3 direction = target.getBoundingBox().getCenter().subtract(origin);
        bolt.shoot(direction.x, direction.y, direction.z, 1.25F, .8F);
        world.addFreshEntity(bolt);
        playSound(SoundEvents.WITHER_SHOOT, 2.4F, .82F);
    }
    private void summon(ServerLevel world) {
        int existing = world.getEntitiesOfClass(MorovoyRaven.class, getBoundingBox().inflate(42), bird -> bird.minion() && getUUID().equals(bird.parent())).size();
        for (int slot = existing; slot < Math.min(4, existing + 2); slot++) {
            MorovoyRaven bird = MorovoyRavenContent.MINION.get().create(world);
            if (bird == null) break;
            Vec3 offset = position().add(Math.cos(slot * 2.1) * 3, -1, Math.sin(slot * 2.1) * 3);
            if (!world.hasChunkAt(BlockPos.containing(offset)) || !world.noCollision(bird.getBoundingBox().move(offset.subtract(bird.position())))) continue;
            bird.setPos(offset);
            bird.attach(this, slot);
            world.addFreshEntity(bird);
        }
    }
    private void placeMiasma(ServerLevel world, Vec3 sample, float radius, int life) {
        BlockPos pos = BlockPos.containing(sample);
        if (!world.hasChunkAt(pos)) return;
        // Nether heightmaps point to the BEDROCK ROOF, not the arena floor.
        // Raycast downward inside the already-loaded arena instead.
        Vec3 lower = sample.add(0, -22, 0);
        if (!world.hasChunkAt(BlockPos.containing(lower))) return;
        HitResult groundHit = world.clip(new ClipContext(sample, lower, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (groundHit.getType() != HitResult.Type.BLOCK) return;
        BlockPos ground = BlockPos.containing(groundHit.getLocation().add(0, .2, 0));
        if (ground.distSqr(blockPosition()) > 45 * 45 || Math.abs(ground.getY() - getY()) > 32) return;
        RavenMiasma zone = MorovoyRavenContent.MIASMA.get().create(world);
        if (zone != null) { zone.setPos(ground.getX() + .5, ground.getY() + .05, ground.getZ() + .5); zone.configure(this, radius, life); world.addFreshEntity(zone); }
    }
    @Override public boolean removeWhenFarAway(double distance) { return minion(); }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override protected void dropAllDeathLoot(ServerLevel world, DamageSource source) { if (!minion() && staffOwner == null && parent == null) { spawnAtLocation(new net.minecraft.world.item.ItemStack(MorovoyRavenContent.FEATHER.get(), 2 + random.nextInt(2))); spawnAtLocation(new net.minecraft.world.item.ItemStack(MorovoyRavenContent.CORE.get())); } }
    @Override public void die(DamageSource cause) { super.die(cause); bossBar.removeAllPlayers(); }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (home != null) { tag.putInt("HomeX", home.getX()); tag.putInt("HomeY", home.getY()); tag.putInt("HomeZ", home.getZ()); tag.putBoolean("HasHome", true); }
        if (parent != null) tag.putUUID("Parent", parent);
        if(staffOwner!=null)tag.putUUID("StaffOwner",staffOwner);
        if(staffTarget!=null)tag.putUUID("StaffTarget",staffTarget);
        tag.putInt("Pose", pose()); tag.putInt("PhaseTicks", phaseTicks); tag.putInt("AttackIndex", attackIndex);
        tag.putInt("MinionLife", minionLife); tag.putLong("NextAttack", nextAttack); tag.putDouble("OrbitAngle", orbitAngle);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        home = tag.getBoolean("HasHome") ? new BlockPos(tag.getInt("HomeX"), tag.getInt("HomeY"), tag.getInt("HomeZ")) : null;
        parent = tag.hasUUID("Parent") ? tag.getUUID("Parent") : null;
        staffOwner=tag.hasUUID("StaffOwner")?tag.getUUID("StaffOwner"):null;
        staffTarget=tag.hasUUID("StaffTarget")?tag.getUUID("StaffTarget"):null;
        entityData.set(POSE, Math.clamp(tag.getInt("Pose"), CIRCLE, TAKEOFF));
        phaseTicks = Math.clamp(tag.getInt("PhaseTicks"), 0, 800);
        // Align visible action timing with the restored server phase after a reload.
        entityData.set(POSE_START, tickCount - phaseTicks);
        attackIndex = tag.getInt("AttackIndex"); minionLife = tag.getInt("MinionLife");
        nextAttack = tag.getLong("NextAttack");
        orbitAngle = tag.contains("OrbitAngle") ? tag.getDouble("OrbitAngle") % (Math.PI * 2) : 0;
        if (!minion() && home != null) waypoint = Vec3.atCenterOf(home).add(0, 15, 0);
        if (!minion()) setNoGravity(pose() != GROUND_IDLE && pose() != LANDING);
    }
}
