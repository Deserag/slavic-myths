package org.slavicmyths.earth;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
public final class EarthServant extends Monster {
    private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(EarthServant.class,EntityDataSerializers.INT),VARIANT=SynchedEntityData.defineId(EarthServant.class,EntityDataSerializers.INT);
    private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(EarthServant.class,EntityDataSerializers.LONG);
    public UUID combatOwner(){return owner;}
    private UUID owner,arena;private BlockPos home;private int attackIn=32,collapseIn=-1,idleIn=260;
    public EarthServant(EntityType<? extends Monster> t,Level w){super(t,w);setPersistenceRequired();xpReward=0;getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(EarthRules.SERVANT_HEALTH);setHealth((float)EarthRules.SERVANT_HEALTH);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STATE,0);b.define(VARIANT,0);b.define(START,0L);}
    public int state(){return entityData.get(STATE);}public int variant(){return entityData.get(VARIANT);}public float age(float p){return level().getGameTime()-entityData.get(START)+p;}
    public void bind(UUID owner,UUID arena,BlockPos home){this.owner=owner;this.arena=arena;this.home=home.immutable();entityData.set(VARIANT,random.nextInt(2));entityData.set(START,level().getGameTime());restrictTo(home,19);}
    public void collapse(){collapseIn=16;entityData.set(STATE,3);entityData.set(START,level().getGameTime());setTarget(null);getNavigation().stop();if(level() instanceof ServerLevel w)EarthArena.cue(w,blockPosition(),"servant_death",.4F);}
    @Override protected void registerGoals(){goalSelector.addGoal(1,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}public boolean canUse(){return state()==1&&getTarget()!=null;}public void tick(){getLookControl().setLookAt(getTarget(),25,20);getNavigation().moveTo(getTarget(),1);}});}
    @Override public boolean isAlliedTo(Entity e){return e instanceof EarthSpirit||e instanceof EarthServant||super.isAlliedTo(e);}
    private boolean valid(Player p){return p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&(home==null||p.position().distanceToSqr(home.getCenter())<19*19);}
    @Override public void aiStep(){super.aiStep();if(level().isClientSide||!isAlive())return;var w=(ServerLevel)level();if(home==null){home=blockPosition();entityData.set(VARIANT,random.nextInt(2));entityData.set(START,w.getGameTime());}
        if(collapseIn>=0){if(--collapseIn<=0)discard();return;}if(owner!=null){var b=w.getEntity(owner);if(b==null){var a=arena==null?null:EarthArena.get(w).entries.get(arena);if(a!=null&&a.state==EarthArena.State.DEFEATED)collapse();getNavigation().stop();setTarget(null);return;}if(!b.isAlive()){collapse();return;}if(b instanceof EarthSpirit spirit&&spirit.action()==EarthRules.Action.DORMANT){setTarget(null);getNavigation().stop();return;}}
        if(state()==0){if(age(0)<12)return;entityData.set(STATE,1);}if(tickCount%10==0)setTarget(w.getEntitiesOfClass(Player.class,new AABB(home).inflate(19,5,19),this::valid).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null));
        attackIn=Math.max(0,attackIn-1);if(state()==2){getNavigation().stop();if(age(0)>=9&&age(0)<10&&getTarget() instanceof Player p&&valid(p)&&distanceToSqr(p)<2.6*2.6&&hasLineOfSight(p)){p.hurt(damageSources().mobAttack(this),EarthRules.SERVANT_RAW);EarthArena.cue(w,blockPosition(),"servant_hit",.4F);}if(age(0)>=21)entityData.set(STATE,1);}else if(attackIn==0&&getTarget()!=null&&distanceToSqr(getTarget())<2.6*2.6&&hasLineOfSight(getTarget())){entityData.set(STATE,2);entityData.set(START,w.getGameTime());attackIn=32;}
        if(--idleIn<=0){EarthArena.cue(w,blockPosition(),"servant_idle",.35F);idleIn=260+random.nextInt(140);}
    }
    @Override public void die(net.minecraft.world.damagesource.DamageSource s){if(owner!=null&&level() instanceof ServerLevel w&&w.getEntity(owner) instanceof EarthSpirit b)b.servantDied(getUUID());super.die(s);}
    @Override public void remove(RemovalReason reason){
        if(reason.shouldDestroy()&&owner!=null&&level() instanceof ServerLevel w){
            if(w.getEntity(owner) instanceof EarthSpirit b)b.servantDied(getUUID());
            else if(arena!=null){var a=EarthArena.get(w).entries.get(arena);if(a!=null){a.retired.add(getUUID());EarthArena.get(w).setDirty();}}
        }
        super.remove(reason);
    }
    @Override protected net.minecraft.sounds.SoundEvent getDeathSound(){return EarthContent.sound("servant_death");}@Override protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource s){return EarthContent.sound("servant_hit");}
    @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(owner!=null)n.putUUID("EarthOwner",owner);if(arena!=null)n.putUUID("EarthArena",arena);if(home!=null)n.putLong("EarthHome",home.asLong());n.putInt("EarthVariant",variant());n.putInt("EarthCollapse",collapseIn);}
    @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);owner=n.hasUUID("EarthOwner")?n.getUUID("EarthOwner"):null;arena=n.hasUUID("EarthArena")?n.getUUID("EarthArena"):null;if(n.contains("EarthHome"))home=BlockPos.of(n.getLong("EarthHome"));entityData.set(VARIANT,n.getInt("EarthVariant"));entityData.set(STATE,1);collapseIn=n.contains("EarthCollapse")?n.getInt("EarthCollapse"):-1;attackIn=32;}
}
