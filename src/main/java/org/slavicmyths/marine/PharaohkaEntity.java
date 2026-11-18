package org.slavicmyths.marine;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;

/** Amphibious-surface visual, but water-only survival and combat. Server owns all damage. */
public final class PharaohkaEntity extends Monster {
    public static final int IDLE=0, STRIKE=1, DASH=2, SUMMON=3, CURRENT=4, BLOCK=5, BASH=6;
    private static final EntityDataAccessor<Integer> ACTION =
        SynchedEntityData.defineId(PharaohkaEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ACTION_STARTED =
        SynchedEntityData.defineId(PharaohkaEntity.class, EntityDataSerializers.INT);

    private final ServerBossEvent bossBar = new ServerBossEvent(
        Component.translatable("entity.slavicmyths.pharaohka_leader"),
        BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    private int dryTicks, meleeCooldown, dashCooldown=90, summonCooldown=220, currentCooldown=145, shieldCooldown=125;
    private int dashTicks, currentTicks, aiWander, strikeTicks, shieldTicks;
    private boolean dashHit;

    public PharaohkaEntity(EntityType<? extends PharaohkaEntity> type, Level level) {
        super(type, level);
        moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02F, 0.1F, true);
        setPathfindingMalus(net.minecraft.world.level.pathfinder.PathType.WATER, 0);
        xpReward = 0;
    }

    public boolean leader() { return getType() == PharaohkaContent.LEADER.get(); }
    public static AttributeSupplier.Builder attributes(boolean leader) {
        return Monster.createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, leader ? 550.0 : 32.0)
            .add(Attributes.MOVEMENT_SPEED, leader ? .38 : .30)
            .add(Attributes.ATTACK_DAMAGE, leader ? 11.0 : 5.0)
            .add(Attributes.ARMOR, leader ? 12.0 : 3.0)
            .add(Attributes.FOLLOW_RANGE, leader ? 30.0 : 18.0)
            .add(Attributes.KNOCKBACK_RESISTANCE, leader ? .65 : .15);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ACTION, IDLE);
        builder.define(ACTION_STARTED, 0);
    }
    public int action() { return entityData.get(ACTION); }
    public int actionTicks() { return Math.max(0, (int)level().getGameTime()-entityData.get(ACTION_STARTED)); }
    private void animate(int action) {
        if (entityData.get(ACTION) != action) {
            entityData.set(ACTION, action);
            entityData.set(ACTION_STARTED, (int)level().getGameTime());
        }
    }

    @Override protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override protected void registerGoals() {
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 16));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    public boolean inHabitat() {
        BlockPos pos=blockPosition();
        return isInWaterOrBubble()
            || level().getFluidState(pos).is(FluidTags.WATER)
            || level().getFluidState(pos.below()).is(FluidTags.WATER);
    }
    @Override public boolean removeWhenFarAway(double distance) { return !leader() && super.removeWhenFarAway(distance); }

    @Override public void travel(Vec3 input) {
        if (isEffectiveAi() && isInWater()) {
            moveRelative(.035F, input);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(.89));
        } else {
            super.travel(input);
        }
    }

    @Override public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (leader()) bossBar.setProgress(getHealth()/getMaxHealth());
        boolean wet=inHabitat();
        setNoGravity(wet);
        if (!wet) {
            strikeTicks=0;
            shieldTicks=0;
            if (++dryTicks > 100) hurt(damageSources().dryOut(), getMaxHealth()+1F);
            getNavigation().stop();
            return;
        }
        dryTicks=0;
        if (isInWater() && getTarget()==null) setDeltaMovement(getDeltaMovement().add(0, .003, 0));
        if (meleeCooldown>0) meleeCooldown--;
        if (dashCooldown>0) dashCooldown--;
        if (summonCooldown>0) summonCooldown--;
        if (currentCooldown>0) currentCooldown--;
        if (shieldCooldown>0) shieldCooldown--;
        if (dashTicks>0) { dash(); return; }
        if (currentTicks>0) { currentPulse(); }
        LivingEntity target=getTarget();
        if (target != null && (!target.isAlive() || target instanceof Player p && p.isCreative()
                || target.isSpectator() || !isTargetInWater(target))) {
            setTarget(null);
            target=null;
        }
        if (strikeTicks>0) {
            getNavigation().stop();
            --strikeTicks;
            if (strikeTicks==5 && target!=null && hasLineOfSight(target)
                && distanceToSqr(target)<(leader()?9.0:5.5))
                doHurtTarget(target);
            if (strikeTicks==0) animate(IDLE);
            return;
        }
        if (shieldTicks>0) {
            getNavigation().stop();
            --shieldTicks;
            if (shieldTicks==6 && target!=null && hasLineOfSight(target) && distanceToSqr(target)<9.0) {
                target.hurt(damageSources().mobAttack(this),8F);
                Vec3 away=target.position().subtract(position()).multiply(1,0,1);
                if (away.lengthSqr()>.01) {
                    Vec3 impulse=away.normalize();
                    target.push(impulse.x*1.1, .24, impulse.z*1.1);
                    target.hurtMarked=true;
                }
            }
            if (shieldTicks==0) animate(IDLE);
            return;
        }
        if (target==null) {
            if (--aiWander<=0) {
                aiWander=35+random.nextInt(55);
                BlockPos near=blockPosition().offset(random.nextInt(13)-6, random.nextInt(7)-3, random.nextInt(13)-6);
                if (level().getFluidState(near).is(FluidTags.WATER))
                    getNavigation().moveTo(near.getX()+.5,near.getY()+.5,near.getZ()+.5,.8);
            }
            if (action()!=IDLE && actionTicks()>20) animate(IDLE);
            return;
        }

        getLookControl().setLookAt(target, 30, 30);
        double distance=distanceToSqr(target);
        if (tickCount%10==0 || getNavigation().isDone())
            getNavigation().moveTo(target,leader()?1.3:1.05);
        getMoveControl().setWantedPosition(target.getX(),target.getY()+target.getBbHeight()*.45,
            target.getZ(),leader()?1.3:1.05);

        if (leader() && summonCooldown==0 && distance<400) { summon(); return; }
        if (leader() && dashCooldown==0 && distance>6 && distance<196) { beginDash(target); return; }
        if (leader() && currentCooldown==0 && distance<100) {
            currentTicks=24;currentCooldown=190;animate(CURRENT);
        }
        if (leader() && shieldCooldown==0 && distance<8.0) {
            shieldTicks=15;
            shieldCooldown=115+random.nextInt(65);
            animate(BASH);
            getNavigation().stop();
            return;
        }
        if (distance < (leader()?8.5:5.0) && meleeCooldown==0) {
            animate(STRIKE);
            meleeCooldown=leader()?27:36;
            strikeTicks=12; // Impact occurs midway through the animated spear/arm sweep.
            getNavigation().stop();
        } else if ((action()==SUMMON || action()==BLOCK) && actionTicks()>20) animate(IDLE);
    }

    private boolean isTargetInWater(LivingEntity target) {
        BlockPos pos=target.blockPosition();
        return target.isInWaterOrBubble() || level().getFluidState(pos.below()).is(FluidTags.WATER);
    }

    private void beginDash(LivingEntity target) {
        dashTicks=17;
        dashCooldown=105+random.nextInt(36);
        dashHit=false;
        animate(DASH);
        getNavigation().stop();
    }

    private void dash() {
        LivingEntity target=getTarget();
        if (target==null || !isInWater() || !isTargetInWater(target)) {dashTicks=0;animate(IDLE);return;}
        Vec3 direction=target.getEyePosition().subtract(getEyePosition());
        if (direction.lengthSqr()>.1) {
            Vec3 impulse=direction.normalize().scale(leader()?.57:.4);
            setDeltaMovement(getDeltaMovement().scale(.24).add(impulse));
            hasImpulse=true;
        }
        if (!dashHit && distanceToSqr(target) < 7.5 && hasLineOfSight(target)) {
            target.hurt(damageSources().mobAttack(this), 15F);
            target.push(target.getX()-getX(), .12, target.getZ()-getZ());
            dashHit=true;
        }
        if (--dashTicks<=0) animate(IDLE);
    }

    private void summon() {
        if (!(level() instanceof ServerLevel world)) return;
        List<PharaohkaEntity> allies=world.getEntitiesOfClass(PharaohkaEntity.class,
            getBoundingBox().inflate(14), entity->entity!=this && !entity.leader() && entity.isAlive());
        summonCooldown=260+random.nextInt(100);
        if (allies.size()>=4) return;
        animate(SUMMON);
        int count=Math.min(2,4-allies.size());
        for (int i=0;i<count;i++) {
            PharaohkaEntity minion=PharaohkaContent.PHARAOHKA.get().create(world);
            if (minion==null) continue;
            double ox=(random.nextDouble()-.5)*6, oz=(random.nextDouble()-.5)*6;
            minion.setPos(getX()+ox,getY(),getZ()+oz);
            if (!world.getFluidState(minion.blockPosition()).is(FluidTags.WATER) || !world.noCollision(minion)) continue;
            minion.setTarget(getTarget());
            minion.setPersistenceRequired();
            world.addFreshEntity(minion);
        }
    }

    private void currentPulse() {
        if (currentTicks-- <= 0) return;
        if (tickCount % 4 != 0) return;
        for (Player player:level().getEntitiesOfClass(Player.class, getBoundingBox().inflate(9),
                p->!p.isCreative()&&!p.isSpectator()&&isTargetInWater(p))) {
            Vec3 delta=position().subtract(player.position());
            if (delta.lengthSqr()>.3) {
                player.setDeltaMovement(player.getDeltaMovement().add(delta.normalize().scale(.24)));
                player.hurtMarked=true;
            }
        }
        if (currentTicks<=0) animate(IDLE);
    }

    @Override public boolean hurt(DamageSource source, float damage) {
        if (leader() && isInWater() && source.is(DamageTypeTags.IS_PROJECTILE)) {
            animate(BLOCK);
            damage*=.50F;
        }
        return super.hurt(source, damage);
    }

    @Override public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        if (leader()) bossBar.addPlayer(player);
    }
    @Override public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DryTicks",dryTicks);
        tag.putInt("MeleeCooldown",meleeCooldown);
        tag.putInt("ShieldCooldown",shieldCooldown);
        tag.putInt("DashCooldown",dashCooldown);
        tag.putInt("SummonCooldown",summonCooldown);
        tag.putInt("CurrentCooldown",currentCooldown);
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        dryTicks=tag.getInt("DryTicks");
        meleeCooldown=Math.max(0,tag.getInt("MeleeCooldown"));
        shieldCooldown=Math.max(0,tag.getInt("ShieldCooldown"));
        strikeTicks=0;
        shieldTicks=0;
        dashCooldown=Math.max(0,tag.getInt("DashCooldown"));
        summonCooldown=Math.max(0,tag.getInt("SummonCooldown"));
        currentCooldown=Math.max(0,tag.getInt("CurrentCooldown"));
        bossBar.setProgress(getHealth()/getMaxHealth());
    }
    @Override protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable() {
        return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;
    }
}