package org.slavicmyths.underground;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.industry.ChudPerson;
/** Local, server authoritative attack phases. No terrain edits, world scans or client hits. */
public abstract class EncounterMob extends Monster {
 public enum Action {IDLE,ALERT,MELEE,MINER_HEAVY,LUNGE,BURROW,THROW,SPIKES,COIN_WAVE,TREMOR,HEAVY,CHARGE,RAGE,RECOVER}
 private static final EntityDataAccessor<Integer> STATE=SynchedEntityData.defineId(EncounterMob.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Long> START=SynchedEntityData.defineId(EncounterMob.class,EntityDataSerializers.LONG);
 private static final EntityDataAccessor<Boolean> RAGED=SynchedEntityData.defineId(EncounterMob.class,EntityDataSerializers.BOOLEAN);
 protected int elapsed,duration;protected Vec3 aim=Vec3.ZERO;protected final Map<Action,Integer> cooldowns=new EnumMap<>(Action.class);protected BlockPos home;protected String encounterKey="";
 public EncounterMob(EntityType<? extends Monster> t,Level l){super(t,l);setPersistenceRequired();getAttribute(Attributes.STEP_HEIGHT).setBaseValue(1);}
 public static AttributeSupplier.Builder attributes(double hp,double speed,double damage,double range,double kb){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,hp).add(Attributes.MOVEMENT_SPEED,speed).add(Attributes.ATTACK_DAMAGE,damage).add(Attributes.FOLLOW_RANGE,range).add(Attributes.KNOCKBACK_RESISTANCE,kb);}
 protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(STATE,0);b.define(START,0L);b.define(RAGED,false);}
 public Action action(){return Action.values()[entityData.get(STATE)];}public float age(float partial){return (float)(level().getGameTime()-entityData.get(START))+partial;}public boolean raged(){return entityData.get(RAGED);}protected void rage(){entityData.set(RAGED,true);}
 public void bind(BlockPos p,String key){home=p.immutable();encounterKey=key;restrictTo(home,leash());}
 protected int leash(){return 22;}
 protected boolean neutral(){return false;}
 public boolean isAlliedTo(Entity e){return e instanceof EncounterMob||super.isAlliedTo(e);}
 protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new Goal(){ {setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}public boolean canUse(){return action()!=Action.IDLE||getTarget()!=null;}public void tick(){if(action()!=Action.IDLE){getNavigation().stop();return;}if(validTarget(getTarget())){getLookControl().setLookAt(getTarget(),25,25);getNavigation().moveTo(getTarget(),1);} }});goalSelector.addGoal(3,new MoveTowardsRestrictionGoal(this,.85));goalSelector.addGoal(5,new WaterAvoidingRandomStrollGoal(this,.6));goalSelector.addGoal(6,new LookAtPlayerGoal(this,Player.class,8));goalSelector.addGoal(7,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));if(!neutral()){targetSelector.addGoal(2,new NearestAttackableTargetGoal<>(this,Player.class,true));targetSelector.addGoal(3,new NearestAttackableTargetGoal<>(this,ChudPerson.class,true));}}
 protected boolean validTarget(LivingEntity t){return t!=null&&t.isAlive()&&t.level()==level()&&!(t instanceof Player p&&(p.isCreative()||p.isSpectator()))&&(home==null||t.distanceToSqr(home.getCenter())<=leash()*leash());}
 protected boolean ready(Action a){return cooldowns.getOrDefault(a,0)<=0;}
 protected void begin(Action a,int ticks){entityData.set(STATE,a.ordinal());entityData.set(START,level().getGameTime());elapsed=0;duration=ticks;var t=getTarget();aim=t==null?position().add(getLookAngle().scale(3)):t.position();getNavigation().stop();}
 protected void finish(Action a,int cd,int recovery){cooldowns.put(a,cd);begin(Action.RECOVER,recovery);}
 public void aiStep(){super.aiStep();if(level().isClientSide||!isAlive())return;if(home==null)bind(blockPosition(),encounterKey);cooldowns.replaceAll((a,n)->Math.max(0,n-1));if(!validTarget(getTarget())){setTarget(null);if(action()!=Action.IDLE&&action()!=Action.RECOVER)cancel();}
  if(action()==Action.IDLE){if(validTarget(getTarget())&&hasLineOfSight(getTarget()))choose();}else{elapsed++;step();if(elapsed>=duration&&action()!=Action.IDLE){if(action()==Action.RECOVER||action()==Action.ALERT||action()==Action.RAGE)begin(Action.IDLE,0);else finish(action(),40,20);}}
 }
 protected abstract void choose();protected abstract void step();
 protected void cancel(){noPhysics=false;setInvisible(false);finish(action(),200,20);}
 protected void melee(float damage,double reach){var t=getTarget();if(validTarget(t)&&distanceToSqr(t)<=reach*reach&&hasLineOfSight(t)&&Math.abs(t.getY()-getY())<1.8)t.hurt(damageSources().mobAttack(this),damage);}
 protected List<LivingEntity> victims(Vec3 center,double radius){return level().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(radius,1.4,radius),t->validTarget(t)&&(t instanceof Player||t instanceof ChudPerson)&&t.distanceToSqr(center)<=radius*radius&&Math.abs(t.getY()-center.y)<1.4&&hasLineOfSight(t)&&!isAlliedTo(t));}
 protected void ring(double radius,int count){if(level() instanceof ServerLevel w)for(int i=0;i<count;i++){double a=i*Math.PI*2/count;w.sendParticles(ParticleTypes.POOF,getX()+Math.cos(a)*radius,getY()+.12,getZ()+Math.sin(a)*radius,1,0,0,0,.01);}}
 protected void dust(Vec3 p,int n){if(level() instanceof ServerLevel w)w.sendParticles(ParticleTypes.POOF,p.x,p.y+.12,p.z,n,.5,.05,.5,.02);}
 protected boolean safe(BlockPos p){if(!level().hasChunkAt(p)||home!=null&&p.distSqr(home)>leash()*leash())return false;for(int y=0;y<Math.ceil(getBbHeight());y++)if(!level().getBlockState(p.above(y)).isAir())return false;return level().getFluidState(p).isEmpty()&&level().getBlockState(p.below()).isCollisionShapeFullBlock(level(),p.below())&&level().noCollision(this,getBoundingBox().move(p.getX()+.5-getX(),p.getY()-getY(),p.getZ()+.5-getZ()));}
 public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(home!=null)n.putLong("EncounterHome",home.asLong());n.putString("EncounterKey",encounterKey);n.putBoolean("EncounterRage",raged());var c=new CompoundTag();cooldowns.forEach((a,v)->c.putInt(a.name(),v));n.put("EncounterCooldowns",c);n.putString("EncounterAction",action().name());}
 public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);if(n.contains("EncounterHome"))bind(BlockPos.of(n.getLong("EncounterHome")),n.getString("EncounterKey"));entityData.set(RAGED,n.getBoolean("EncounterRage"));for(Action a:Action.values())cooldowns.put(a,Math.max(0,n.getCompound("EncounterCooldowns").getInt(a.name())));if(!n.getString("EncounterAction").equals("IDLE")){for(Action a:Action.values())cooldowns.merge(a,200,Math::max);}noPhysics=false;setInvisible(false);begin(Action.RECOVER,20);}
 public void die(net.minecraft.world.damagesource.DamageSource d){cancel();if(level() instanceof ServerLevel w&&!encounterKey.isEmpty())EncounterPopulation.data(w).defeat(encounterKey);super.die(d);}
}
