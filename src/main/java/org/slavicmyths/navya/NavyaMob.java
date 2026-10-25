package org.slavicmyths.navya;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.ai.navigation.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slavicmyths.navya.NavyaContent.Kind;

/** Shared protocol, distinct attack selection and movement for each inhabitant. */
public final class NavyaMob extends PathfinderMob {
    public enum Action { IDLE,MELEE,SHOT,ROOTS,DASH,THROW,SUMMON,PHASE }
    private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(NavyaMob.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(NavyaMob.class,EntityDataSerializers.LONG);
    private static final EntityDataAccessor<Boolean> SECOND=SynchedEntityData.defineId(NavyaMob.class,EntityDataSerializers.BOOLEAN);
    public final Kind kind;
    private int recovery, rootsCooldown,shotCooldown,dashCooldown,throwCooldown,summonCooldown;
    private int actionAge;
    private Vec3 aim=Vec3.ZERO;
    private UUID summoner;
    private BlockPos home;
    private final Set<UUID> helpers=new HashSet<>();
    private final ServerBossEvent bossbar=new ServerBossEvent(net.minecraft.network.chat.Component.translatable("entity.slavicmyths.grove_guardian"),BossEvent.BossBarColor.PURPLE,BossEvent.BossBarOverlay.PROGRESS);
    public NavyaMob(EntityType<? extends NavyaMob> type,Level world,Kind kind) {
        super(type,world);this.kind=kind;xpReward=kind==Kind.GUARDIAN?50:kind==Kind.WISP?0:12;
        if(flying()) { setNoGravity(true);moveControl=new FlyingMoveControl(this,20,true);navigation=new FlyingPathNavigation(this,world); }
    }
    public static AttributeSupplier.Builder attributes(Kind kind) {
        double hp=switch(kind){case NAVKA->80;case ROOTLING->120;case CRAWLER->150;case WISP->20;case GUARDIAN->700;};
        double damage=switch(kind){case NAVKA->6;case ROOTLING->9;case CRAWLER->10;case WISP->0;case GUARDIAN->14;};
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,hp).add(Attributes.ATTACK_DAMAGE,damage)
                .add(Attributes.MOVEMENT_SPEED,kind==Kind.GUARDIAN?.20:kind==Kind.CRAWLER?.29:.25)
                .add(Attributes.FLYING_SPEED,.3).add(Attributes.FOLLOW_RANGE,32)
                .add(Attributes.KNOCKBACK_RESISTANCE,kind==Kind.GUARDIAN?.9:kind==Kind.ROOTLING?.4:0);
    }
    public boolean flying() { return kind==Kind.NAVKA || kind==Kind.WISP; }
    @Override protected void registerGoals() {
        goalSelector.addGoal(0,new FloatGoal(this));
        goalSelector.addGoal(2,new CombatGoal());
        goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.65));
        goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,10));
        goalSelector.addGoal(7,new RandomLookAroundGoal(this));
        targetSelector.addGoal(1,new HurtByTargetGoal(this));
        targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));
    }
    // registerGoals is called by Mob's constructor, before our kind is assigned.
    @Override public void setTarget(LivingEntity target) { super.setTarget(kind==Kind.WISP?null:target); }
    @Override public boolean canAttack(LivingEntity target) { return kind!=Kind.WISP && !(target instanceof NavyaMob) && super.canAttack(target); }
    @Override public boolean isAlliedTo(Entity entity) { return entity instanceof NavyaMob || super.isAlliedTo(entity); }
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) { super.defineSynchedData(builder);builder.define(ACTION,0);builder.define(START,0L);builder.define(SECOND,false); }
    public Action action() { return Action.values()[Math.min(Action.values().length-1,Math.max(0,entityData.get(ACTION)))]; }
    public float actionTime(float partial) { return Math.max(0,level().getGameTime()-entityData.get(START)+partial); }
    public boolean secondPhase() { return entityData.get(SECOND); }
    public boolean summoned() { return summoner!=null; }
    public int windup(Action action) { return NavyaTiming.windup(kind,action); }
    public int duration(Action action) { return NavyaTiming.duration(kind,action); }
    private void begin(Action action) {
        entityData.set(ACTION,action.ordinal());entityData.set(START,level().getGameTime());actionAge=0;
        if(getTarget()!=null)aim=getTarget().position();
        getNavigation().stop();
        playSound(NavyaSounds.get(action==Action.PHASE?"phase":action==Action.THROW?"trunk_lift":action==Action.SUMMON?"summon":NavyaContent.NAMES.get(kind)+".warn"),.6F,1);
    }
    @Override public void tick() {
        super.tick();
        if(level().isClientSide) return;
        if(flying()) { setNoGravity(true);if(action()==Action.IDLE && getTarget()==null && tickCount%60==0) {
            var pos=position().add(random.nextDouble()*8-4,random.nextDouble()*4-2,random.nextDouble()*8-4);
            if(level().hasChunkAt(BlockPos.containing(pos)) && level().getBlockState(BlockPos.containing(pos)).isAir())getMoveControl().setWantedPosition(pos.x,pos.y,pos.z,.55);
        }}
        if(recovery>0) recovery--;if(rootsCooldown>0)rootsCooldown--;if(shotCooldown>0)shotCooldown--;if(dashCooldown>0)dashCooldown--;if(throwCooldown>0)throwCooldown--;if(summonCooldown>0)summonCooldown--;
        if(kind==Kind.GUARDIAN) {
            bossbar.setProgress(getHealth()/getMaxHealth());
            if(!secondPhase() && getHealth()<=350 && isAlive()) { entityData.set(SECOND,true);begin(Action.PHASE); }
            if(tickCount%40==0 && isAlive()) for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(6),p->!p.isCreative()&&!p.isSpectator()&&distanceToSqr(p)<=36))
                p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60,0));
        }
        if(summoner!=null && tickCount%100==0 && level() instanceof ServerLevel server) {
            Entity owner=server.getEntity(summoner);
            // No owner chunk loads; an unloaded/dead summoner cannot sustain a loot farm.
            if(!(owner instanceof NavyaMob mob) || !mob.isAlive())discard();
        }
        if(action()!=Action.IDLE) {
            Action action=action();actionAge++;
            if(actionAge==windup(action))impact(action);
            if(actionAge>=duration(action)) {entityData.set(ACTION,0);entityData.set(START,level().getGameTime());recovery=secondPhase()?18:26;}
        }
    }
    private void impact(Action action) {
        if(!(level() instanceof ServerLevel server) || !isAlive())return;
        switch(action) {
            case MELEE -> {
                double range=kind==Kind.GUARDIAN?6:kind==Kind.CRAWLER?3.2:2.8;
                for(LivingEntity target:server.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(range,1,range),e->canAttack(e) && !isAlliedTo(e))) {
                    Vec3 delta=target.position().subtract(position());
                    if(distanceTo(target)>range || !hasLineOfSight(target) || delta.horizontalDistanceSqr()>0 && getLookAngle().multiply(1,0,1).normalize().dot(delta.multiply(1,0,1).normalize())<.25)continue;
                    if(target.hurt(damageSources().mobAttack(this),(float)getAttributeValue(Attributes.ATTACK_DAMAGE)))target.knockback(kind==Kind.GUARDIAN?1:.3,-delta.x,-delta.z);
                }
                playSound(NavyaSounds.get(NavyaContent.NAMES.get(kind)+".strike"),.7F,1);
            }
            case SHOT,THROW -> {
                if(getTarget()!=null && getTarget().isAlive() && hasLineOfSight(getTarget())) {
                    var shot=new NavyaShot(NavyaContent.SHOT.get(),server);shot.setOwner(this);
                    shot.mode(action==Action.THROW?2:kind==Kind.NAVKA?0:1);
                    shot.setPos(getX(),getY()+(action==Action.THROW?3:getEyeHeight()*.8),getZ());
                    Vec3 direction=getTarget().getEyePosition().subtract(shot.position());
                    shot.shoot(direction.x,direction.y,direction.z,action==Action.THROW?.8F:1.1F,1F);
                    server.addFreshEntity(shot);playSound(NavyaSounds.get(action==Action.THROW?"trunk_throw":NavyaContent.NAMES.get(kind)+".strike"),.7F,1);
                }
            }
            case ROOTS -> {
                int count=kind==Kind.GUARDIAN?3:1;
                for(int i=0;i<count;i++) {
                    var p=BlockPos.containing(aim).offset(count==1?0:(i-1)*2,0,0);
                    for(int y=0;y<8;y++) {
                        var q=p.below(y);
                        if(!server.hasChunkAt(q))break;
                        if(server.getBlockState(q.below()).isFaceSturdy(server,q.below(),Direction.UP) && server.isEmptyBlock(q) && server.isEmptyBlock(q.above())) {
                            var roots=new NavyaRoots(NavyaContent.ROOTS.get(),server);roots.setPos(q.getX()+.5,q.getY(),q.getZ()+.5);roots.owner(getUUID());server.addFreshEntity(roots);break;
                        }
                    }
                }
            }
            case DASH -> {
                Vec3 delta=aim.subtract(position()).multiply(1,0,1).normalize().scale(.9);
                setDeltaMovement(delta.x,.15,delta.z);hurtMarked=true;
            }
            case SUMMON -> summon(server);
            default -> { }
        }
    }
    private void summon(ServerLevel server) {
        // Count only living marked helpers in loaded local space. Unloaded helpers
        // remain in the persisted UUID set and keep their reservation.
        helpers.removeIf(id->{Entity e=server.getEntity(id);return e!=null && !e.isAlive();});
        if(helpers.size()>=2)return;
        for(int i=helpers.size();i<2;i++) {
            var pos=blockPosition().offset(i==0?-4:4,0,0);
            if(!server.hasChunkAt(pos) || !server.getWorldBorder().isWithinBounds(pos))continue;
            var helper=NavyaContent.MOBS.get(Kind.ROOTLING).get().create(server);
            if(helper==null)continue;helper.moveTo(pos.getX()+.5,pos.getY(),pos.getZ()+.5,0,0);
            if(!server.noCollision(helper) || !server.getBlockState(pos.below()).isFaceSturdy(server,pos.below(),Direction.UP))continue;
            helper.summoner=getUUID();helper.setTarget(getTarget());helper.setPersistenceRequired();
            if(server.addFreshEntity(helper))helpers.add(helper.getUUID());
        }
    }
    @Override protected boolean shouldDropLoot() { return summoner==null && kind!=Kind.WISP && super.shouldDropLoot(); }
    @Override protected int getBaseExperienceReward() { return summoner!=null?0:super.getBaseExperienceReward(); }
    @Override public void die(DamageSource source) {
        super.die(source);
        if(summoner!=null && level() instanceof ServerLevel server && server.getEntity(summoner) instanceof NavyaMob owner)owner.helpers.remove(getUUID());
    }
    @Override public boolean hurt(DamageSource source,float amount) { return super.hurt(source,kind==Kind.GUARDIAN && secondPhase()?amount*.8F:amount); }
    @Override protected SoundEvent getAmbientSound() { return NavyaSounds.get(NavyaContent.NAMES.get(kind)+".idle"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return NavyaSounds.get(NavyaContent.NAMES.get(kind)+".hurt"); }
    @Override protected SoundEvent getDeathSound() { return NavyaSounds.get(NavyaContent.NAMES.get(kind)+".death"); }
    @Override public int getAmbientSoundInterval() { return kind==Kind.WISP?300:220; }
    @Override protected float getSoundVolume() { return kind==Kind.WISP?.13F:.6F; }
    @Override public net.minecraft.sounds.SoundSource getSoundSource() { return kind==Kind.WISP?net.minecraft.sounds.SoundSource.AMBIENT:net.minecraft.sounds.SoundSource.HOSTILE; }
    @Override protected void playStepSound(BlockPos pos,net.minecraft.world.level.block.state.BlockState state) {
        if(!flying())playSound(NavyaSounds.get(NavyaContent.NAMES.get(kind)+".step"),kind==Kind.GUARDIAN?.55F:.25F,1);
    }
    @Override public boolean removeWhenFarAway(double distance) { return kind!=Kind.GUARDIAN && summoner==null; }
    @Override protected boolean shouldDespawnInPeaceful() { return kind!=Kind.WISP; }
    @Override public boolean causeFallDamage(float distance,float multiplier,DamageSource source) { return !flying() && super.causeFallDamage(distance,multiplier,source); }
    @Override public void startSeenByPlayer(ServerPlayer player) { super.startSeenByPlayer(player);if(kind==Kind.GUARDIAN)bossbar.addPlayer(player); }
    @Override public void stopSeenByPlayer(ServerPlayer player) { super.stopSeenByPlayer(player);bossbar.removePlayer(player); }
    @Override public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);tag.putBoolean("NavyaPhase2",secondPhase());
        if(summoner!=null)tag.putUUID("NavyaSummoner",summoner);
        var saved=new net.minecraft.nbt.ListTag();for(UUID helper:helpers)saved.add(net.minecraft.nbt.NbtUtils.createUUID(helper));tag.put("NavyaHelpers",saved);
        tag.putIntArray("NavyaCooldowns",new int[]{recovery,rootsCooldown,shotCooldown,dashCooldown,throwCooldown,summonCooldown});
        if(home!=null)tag.putLong("NavyaHome",home.asLong());
    }
    @Override public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);entityData.set(SECOND,tag.getBoolean("NavyaPhase2"));
        summoner=tag.hasUUID("NavyaSummoner")?tag.getUUID("NavyaSummoner"):null;
        helpers.clear();for(var n:tag.getList("NavyaHelpers",11))if(helpers.size()<2)helpers.add(net.minecraft.nbt.NbtUtils.loadUUID(n));
        int[] cooldowns=tag.getIntArray("NavyaCooldowns");if(cooldowns.length==6){recovery=clamp(cooldowns[0]);rootsCooldown=clamp(cooldowns[1]);shotCooldown=clamp(cooldowns[2]);dashCooldown=clamp(cooldowns[3]);throwCooldown=clamp(cooldowns[4]);summonCooldown=clamp(cooldowns[5]);}
        home=tag.contains("NavyaHome")?BlockPos.of(tag.getLong("NavyaHome")):null;
        if(home!=null)restrictTo(home,24);
        // Cancel an interrupted telegraph on reload; never replay its damage.
        entityData.set(ACTION,0);entityData.set(START,level().getGameTime());recovery=Math.max(recovery,30);
    }
    private static int clamp(int value) { return Math.max(0,Math.min(1200,value)); }
    public void home(BlockPos pos) { home=pos.immutable();restrictTo(home,24);setPersistenceRequired(); }
    private final class CombatGoal extends Goal {
        CombatGoal() { setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK)); }
        @Override public boolean canUse() { return kind!=Kind.WISP && getTarget()!=null && getTarget().isAlive(); }
        @Override public void tick() {
            var target=getTarget();if(target==null)return;
            getLookControl().setLookAt(target,15,15);
            if(action()!=Action.IDLE)return;
            double distance=distanceTo(target);
            if(home!=null && distanceToSqr(Vec3.atCenterOf(home))>32*32){getNavigation().moveTo(home.getX(),home.getY(),home.getZ(),1);return;}
            if(recovery>0 || !hasLineOfSight(target)) {getNavigation().moveTo(target,.9);return;}
            if(kind==Kind.GUARDIAN && summonCooldown==0) {begin(Action.SUMMON);summonCooldown=500;return;}
            if(kind==Kind.GUARDIAN && throwCooldown==0 && distance>7 && distance<28) {begin(Action.THROW);throwCooldown=220;return;}
            if((kind==Kind.GUARDIAN || kind==Kind.ROOTLING) && rootsCooldown==0 && distance<16) {begin(Action.ROOTS);rootsCooldown=180;return;}
            if((kind==Kind.NAVKA || kind==Kind.CRAWLER) && shotCooldown==0 && distance>4 && distance<26) {begin(Action.SHOT);shotCooldown=kind==Kind.NAVKA?70:110;return;}
            if(kind==Kind.CRAWLER && dashCooldown==0 && distance>4 && distance<10) {begin(Action.DASH);dashCooldown=160;return;}
            if(distance<(kind==Kind.GUARDIAN?6:kind==Kind.CRAWLER?3.2:2.8)) {begin(Action.MELEE);return;}
            if(flying())getMoveControl().setWantedPosition(target.getX(),target.getY()+2,target.getZ(),.8);
            else getNavigation().moveTo(target,1);
        }
    }
}
