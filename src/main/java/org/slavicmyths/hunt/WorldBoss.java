package org.slavicmyths.hunt;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.server.*;
import org.slavicmyths.registry.ModEntities;
import static org.slavicmyths.hunt.BossRules.Move;
/** Exclusive combat scheduler: bounded navigation, fixed launch headings, no terrain mutation. */
public final class WorldBoss extends HuntMob {
 private static final DataParameter<Integer> MOVE=EntityDataManager.defineId(WorldBoss.class,DataSerializers.INT),START=EntityDataManager.defineId(WorldBoss.class,DataSerializers.INT),PHASE=EntityDataManager.defineId(WorldBoss.class,DataSerializers.INT);
 public String encounter="";public UUID bossOwner;
 private final EnumMap<Move,Integer> cooldowns=new EnumMap<>(Move.class);
 private final Set<UUID> hits=new HashSet<>();private final List<Vector3d> zones=new ArrayList<>();
 private final ServerBossInfo bossBar=new ServerBossInfo(new net.minecraft.util.text.StringTextComponent(""),BossInfo.Color.RED,BossInfo.Overlay.PROGRESS);
 private int time,wait,memory,unreachable,align;private Vector3d heading=Vector3d.ZERO,pivot;private UUID held;private int holdTicks;
 public WorldBoss(EntityType<? extends WorldBoss> type,World world){super(type,world);xpReward=kind().xp;setPersistenceRequired();}
 @Override public boolean isPushable(){return move()!=Move.SPIN&&super.isPushable();}
 @Override public void knockback(float power,double x,double z){if(move()!=Move.SPIN)super.knockback(power,x,z);}
 @Override protected float getStandingEyeHeight(net.minecraft.entity.Pose pose,net.minecraft.entity.EntitySize size){return kind()==BossKind.LIKHO?3.02F:2.68F;}
 public BossKind kind(){return getType()==ModEntities.LIKHO_ONE_EYED.get()?BossKind.LIKHO:BossKind.TUGARIN;}
 @Override public boolean oven(){return false;}@Override protected boolean miniBoss(){return false;}@Override protected boolean showsHuntBar(){return false;}@Override public HuntTarget huntTarget(){return null;}
 public static AttributeModifierMap.MutableAttribute attributes(BossKind k){return createMobAttributes().add(Attributes.MAX_HEALTH,k.hp).add(Attributes.ATTACK_DAMAGE,k==BossKind.LIKHO?11:12).add(Attributes.MOVEMENT_SPEED,k.speed).add(Attributes.ARMOR,k.armor).add(Attributes.ARMOR_TOUGHNESS,k.toughness).add(Attributes.KNOCKBACK_RESISTANCE,k.resistance).add(Attributes.FOLLOW_RANGE,k.range);}
 @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(MOVE,0);entityData.define(START,0);entityData.define(PHASE,1);}
 public Move move(){return Move.values()[entityData.get(MOVE)];}public int phase(){return entityData.get(PHASE);}public float animationTime(float p){return Math.max(0,(int)level.getGameTime()-entityData.get(START)+p);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new Fight());goalSelector.addGoal(5,new LookRandomlyGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<PlayerEntity>(this,PlayerEntity.class,true).setUnseenMemoryTicks(260));}
 @Override public boolean canChangeDimensions(){return false;}
 @Override public void tick(){super.tick();if(level.isClientSide)return;if(!isAlive()){releaseHeld(false);return;}if(!encounter.isEmpty()){HuntRecords.Boss b=HuntRecords.get((ServerWorld)level).bosses.get(encounter);if(b==null||!b.active||!getUUID().equals(b.target)){remove();return;}}
  if(tickCount%20==0){bossBar.setName(getDisplayName());bossBar.setColor(kind()==BossKind.LIKHO?BossInfo.Color.PURPLE:BossInfo.Color.RED);List<ServerPlayerEntity> nearby=level.getEntitiesOfClass(ServerPlayerEntity.class,getBoundingBox().inflate(48),p->p.isAlive()&&!p.isSpectator()&&distanceToSqr(p)<=48*48);for(ServerPlayerEntity p:new ArrayList<>(bossBar.getPlayers()))if(!nearby.contains(p))bossBar.removePlayer(p);for(ServerPlayerEntity p:nearby)bossBar.addPlayer(p);}
  bossBar.setPercent(Math.max(0,getHealth()/getMaxHealth()));bossBar.setVisible(true);
 }
 @Override public void onRemovedFromWorld(){releaseHeld(false);bossBar.removeAllPlayers();super.onRemovedFromWorld();}
 @Override public void checkDespawn(){if(!level.isClientSide&&level.getDifficulty()==Difficulty.PEACEFUL){if(!encounter.isEmpty()){HuntRecords d=HuntRecords.get((ServerWorld)level);HuntRecords.Boss b=d.bosses.get(encounter);if(b!=null&&getUUID().equals(b.target)){b.active=false;b.ready=level.getGameTime()+1200;d.setDirty();}}remove();}}
 private boolean ready(Move m){return cooldowns.getOrDefault(m,0)==0;}
 private void begin(Move m,LivingEntity target){entityData.set(MOVE,m.ordinal());entityData.set(START,(int)level.getGameTime());time=0;align=0;hits.clear();zones.clear();navigation.stop();heading=target.position().subtract(position()).multiply(1,0,1).normalize();pivot=position();cooldowns.put(m,BossRules.cooldown(kind(),m,phase()));
  if(m==Move.GROUND)for(int i=0;i<3;i++)zones.add(target.position().add(target.getDeltaMovement().scale(8+i*4)).add((i-1)*1.8,0,(i%2)*1.4));
  playSound(m==Move.BREATH?SoundEvents.FIRECHARGE_USE:m==Move.SPIN||m==Move.SWEEP?SoundEvents.PLAYER_ATTACK_SWEEP:m==Move.GAZE?SoundEvents.EVOKER_PREPARE_ATTACK:m==Move.GRAB?SoundEvents.EVOKER_PREPARE_SUMMON:m==Move.GROUND?SoundEvents.EVOKER_CAST_SPELL:m==Move.LEAP?SoundEvents.PHANTOM_FLAP:m==Move.SLAM?SoundEvents.IRON_GOLEM_ATTACK:m==Move.STONE?SoundEvents.SNOWBALL_THROW:m==Move.PUNCH?SoundEvents.PLAYER_ATTACK_STRONG:m==Move.CHASE?SoundEvents.WITCH_AMBIENT:SoundEvents.RAVAGER_ROAR,.8F,.55F);
 }
 private void recover(int duration){releaseHeld(false);entityData.set(MOVE,Move.RECOVERY.ordinal());entityData.set(START,(int)level.getGameTime());time=0;wait=duration;navigation.stop();}
 private boolean strike(LivingEntity p,float damage,double radius,double cone,float kb){if(!p.isAlive()||p instanceof PlayerEntity&&(((PlayerEntity)p).isCreative()||p.isSpectator())||distanceTo(p)>radius||!getSensing().canSee(p)||heading.dot(p.position().subtract(position()).multiply(1,0,1).normalize())<cone)return false;boolean shield=p.isBlocking()&&p.getLookAngle().dot(position().subtract(p.position()).normalize())>.35;boolean hit=p.hurt(DamageSource.mobAttack(this),damage);Vector3d d=p.position().subtract(position()).multiply(1,0,1).normalize();if(kb>0)p.knockback(shield?kb*.35F:kb,-d.x,-d.z);return hit;}
 private List<PlayerEntity> players(double radius){return level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(radius),p->p.isAlive()&&!p.isCreative()&&!p.isSpectator());}
 private void area(float damage,double r,double cone,float kb){for(PlayerEntity p:players(r))strike(p,damage,r,cone,kb);}
 private void fate(LivingEntity p){p.addEffect(new EffectInstance(BossEffects.ILL_FATE.get(),160));}
 private LivingEntity heldEntity(){if(held==null||level.isClientSide)return null;Entity e=((ServerWorld)level).getEntity(held);return e instanceof LivingEntity?(LivingEntity)e:null;}
 private void releaseHeld(boolean toss){LivingEntity p=heldEntity();held=null;holdTicks=0;if(p==null)return;p.fallDistance=0;p.addEffect(new EffectInstance(Effects.SLOW_FALLING,60,0,false,false));if(toss&&p.isAlive()){p.hurt(DamageSource.mobAttack(this),6);p.setDeltaMovement(heading.scale(.72).add(0,.35,0));p.hurtMarked=true;}}
 private boolean spinSafe(){return onGround&&!isInWater()&&level.noCollision(getBoundingBox().inflate(.7,0,.7).expandTowards(0,.4,0));}
 private Move choose(LivingEntity t,boolean visible){if(!visible)return kind()==BossKind.TUGARIN&&phase()>=2&&unreachable>=50&&ready(Move.STONE)?Move.STONE:Move.IDLE;double d=distanceTo(t);if(kind()==BossKind.LIKHO){if(phase()==3&&d>4&&ready(Move.CHASE))return Move.CHASE;if(phase()==3&&d>=8&&d<=15&&ready(Move.LEAP))return Move.LEAP;if(phase()>=2&&d<=10&&ready(Move.GAZE))return Move.GAZE;if(phase()>=2&&d<=12&&ready(Move.GROUND))return Move.GROUND;if(d<=3.5&&ready(Move.GRAB))return Move.GRAB;if(d<=4&&ready(Move.SWEEP))return Move.SWEEP;}else{if(phase()==3&&d<=7&&ready(Move.BREATH))return Move.BREATH;if(d<=7&&ready(Move.SPIN)&&spinSafe())return Move.SPIN;if(d>=7&&d<=16&&onGround&&!isInWater()&&ready(Move.CHARGE))return Move.CHARGE;if(phase()>=2&&d<=4.5&&ready(Move.SLAM))return Move.SLAM;if(phase()>=2&&(d>10||unreachable>=50)&&ready(Move.STONE))return Move.STONE;if(d<=2.8&&ready(Move.PUNCH))return Move.PUNCH;}return Move.IDLE;}
 private void release(LivingEntity t){switch(move()){
  case SWEEP:area(10,4,Math.cos(Math.toRadians(55)),.55F);break;
  case PUNCH:strike(t,12,2.8,.5,.8F);break;
  case GRAB:if(strike(t,4,3.5,Math.cos(Math.toRadians(30)),0)){held=t.getUUID();holdTicks=0;fate(t);entityData.set(MOVE,Move.HOLD.ordinal());entityData.set(START,(int)level.getGameTime());time=0;}break;
  case GAZE:for(PlayerEntity p:players(10))if(distanceTo(p)<=10&&getSensing().canSee(p)&&heading.dot(p.position().subtract(position()).multiply(1,0,1).normalize())>=Math.cos(Math.toRadians(25))&&p.getLookAngle().dot(getEyePosition(1).subtract(p.getEyePosition(1)).normalize())>=.75){p.hurt(DamageSource.mobAttack(this),2);fate(p);p.addEffect(new EffectInstance(Effects.WEAKNESS,80));}break;
  case GROUND:for(Vector3d z:zones)if(clearLine(getEyePosition(1),z.add(0,.3,0)))for(PlayerEntity p:players(14))if(p.position().distanceToSqr(z)<=1.75*1.75&&clearLine(z.add(0,.3,0),p.position().add(0,.3,0))&&hits.add(p.getUUID())){p.hurt(DamageSource.mobAttack(this),6);Vector3d d=p.position().subtract(z).normalize();p.knockback(.6F,-d.x,-d.z);p.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,50));}break;
  case LEAP:setDeltaMovement(heading.scale(Math.min(.95,distanceTo(t)/14)).add(0,.55,0));hasImpulse=true;break;
  case SLAM:area(11,4.5,Math.cos(Math.toRadians(50)),1);dust();break;
  case STONE:BossStone stone=new BossStone(ModEntities.TUGARIN_STONE.get(),level);stone.setOwner(this);stone.setPos(getX(),getEyeY()-.2,getZ());Vector3d d=t.getEyePosition(1).subtract(stone.position());stone.shoot(d.x,d.y+d.length()*.10,d.z,1.1F,.5F);level.addFreshEntity(stone);break;
  default:break;
 }}
 private void dust(){((ServerWorld)level).sendParticles(ParticleTypes.CLOUD,getX(),getY()+.15,getZ(),18,1,.1,1,.035);playSound(SoundEvents.IRON_GOLEM_STEP,.8F,.5F);}
 private final class Fight extends Goal{
  Fight(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
  public boolean canUse(){return isAlive();}public void stop(){releaseHeld(false);navigation.stop();entityData.set(MOVE,Move.IDLE.ordinal());}
  public void tick(){for(Move m:Move.values())cooldowns.computeIfPresent(m,(k,v)->Math.max(0,v-1));int next=BossRules.phase(kind(),getHealth()/getMaxHealth());if(next>phase()){entityData.set(PHASE,next);recover(18);entityData.set(MOVE,Move.TRANSITION.ordinal());playSound(SoundEvents.RAVAGER_ROAR,1,.55F);}
   getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(kind().speed*(kind()==BossKind.LIKHO?(phase()==3?1.12:phase()==2?1.07:1):(phase()>=2?1.06:1))*(move()==Move.CHASE?1.25:move()==Move.RECOVERY&&wait>=30?.7:1));
   LivingEntity t=getTarget();if(t!=null&&(!t.isAlive()||t instanceof PlayerEntity&&(((PlayerEntity)t).isCreative()||t.isSpectator()))){setTarget(null);t=null;}boolean sight=t!=null&&getSensing().canSee(t);if(sight)memory=260;else if(t!=null&&--memory<=0){setTarget(null);t=null;}
   if(move()==Move.RECOVERY||move()==Move.TRANSITION){navigation.stop();if(++time>=wait)entityData.set(MOVE,Move.IDLE.ordinal());return;}
   if(t==null){if(move()!=Move.IDLE)recover(10);if(home!=null&&tickCount%10==0)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);return;}
   if(home!=null&&distanceToSqr(home.getX()+.5,home.getY(),home.getZ()+.5)>96*96){setTarget(null);recover(10);return;}
   if(move()==Move.IDLE){Move chosen=choose(t,sight);if(chosen!=Move.IDLE){begin(chosen,t);return;}lookControl.setLookAt(t,25,25);if(tickCount%10==0){boolean path=navigation.moveTo(t,1);unreachable=path?0:unreachable+10;}return;}
   time++;Move m=move();if(m==Move.HOLD){LivingEntity victim=heldEntity();if(victim==null||!victim.isAlive()||distanceTo(victim)>5){recover(26);return;}navigation.stop();Vector3d desired=position().add(heading.scale(1.4)).add(0,1.2,0);victim.setDeltaMovement(desired.subtract(victim.position()).scale(.25));victim.fallDistance=0;victim.hurtMarked=true;if(++holdTicks>=20){releaseHeld(true);recover(26);}return;}
   if(m==Move.CHASE){if(tickCount%8==0)navigation.moveTo(t,1.25);heading=t.position().subtract(position()).multiply(1,0,1).normalize();if(time%25==0)area(10,4,Math.cos(Math.toRadians(55)),.55F);if(time>=80)recover(40);return;}
   navigation.stop();if(m==Move.CHARGE||m==Move.LEAP){if(time<=m.windup){heading=t.position().add(t.getDeltaMovement().scale(3)).subtract(position()).multiply(1,0,1).normalize();boolean aligned=DashFacing.turn(WorldBoss.this,heading,m==Move.CHARGE?18:25);if(time==m.windup&&!aligned){if(++align<=20){time--;entityData.set(START,entityData.get(START)+1);return;}recover(10);return;}if(time==m.windup){if(m==Move.CHARGE&&!level.noCollision(getBoundingBox().expandTowards(heading.scale(3)))){recover(30);return;}DashFacing.lock(WorldBoss.this,heading);}}else DashFacing.lock(WorldBoss.this,heading);}
   else if(m!=Move.SPIN&&time<=m.windup)DashFacing.turn(WorldBoss.this,heading,25);
   if(m==Move.GROUND&&time<=24&&time%4==0)for(Vector3d z:zones)((ServerWorld)level).sendParticles(ParticleTypes.SMOKE,z.x,z.y+.1,z.z,6,.65,.04,.65,.01);
   if(time==m.windup)release(t);
   if(m==Move.CHARGE&&time>12){setDeltaMovement(heading.scale(1.7).add(0,getDeltaMovement().y,0));hasImpulse=true;for(PlayerEntity p:players(2.4))if(!hits.contains(p.getUUID())&&strike(p,10,2.4,.2,1.2F))hits.add(p.getUUID());if(horizontalCollision){dust();recover(30);return;}if(time>=34){recover(20);return;}}
   if(m==Move.LEAP&&time>18){if(onGround&&time>20){area(8,3.5,-1,1);dust();recover(26);return;}if(time>=48){recover(26);return;}}
   if(m==Move.SPIN){setDeltaMovement(0,getDeltaMovement().y,0);if(time>=22&&time<28)for(PlayerEntity p:players(phase()==3?4.5:4))if(!hits.contains(p.getUUID())&&strike(p,phase()==3?10:9,phase()==3?4.5:4,-1,phase()==3?1.9F:1.7F)){hits.add(p.getUUID());if(!p.isBlocking())p.setDeltaMovement(p.getDeltaMovement().add(0,.25,0));}if(time>=36){recover(phase()==3?24:28);return;}return;}
   if(m==Move.BREATH&&time>22&&time<=46){if(time%2==0)((ServerWorld)level).sendParticles(ParticleTypes.FLAME,getX()+heading.x*2,getEyeY()-.3,getZ()+heading.z*2,7,1,.35,1,.04);if(time==24||time==34||time==44)for(PlayerEntity p:players(7))if(strike(p,4,7,Math.cos(Math.toRadians(30)),0))p.setSecondsOnFire(3);if(time==46)recover(18);return;}
   if(m!=Move.CHARGE&&m!=Move.LEAP&&time>=m.windup+m.recovery)recover(1);
  }
 }
 @Override public void die(DamageSource source){boolean was=dead;super.die(source);if(!was&&dead&&!level.isClientSide){releaseHeld(false);bossBar.removeAllPlayers();if(!encounter.isEmpty())HuntRecords.get((ServerWorld)level).finishBoss(encounter,getUUID(),level.getGameTime());if(source.getEntity() instanceof PlayerEntity)org.slavicmyths.item.FolkAccessoryItem.award((PlayerEntity)source.getEntity(),kind()==BossKind.LIKHO?"name_your_misfortune":"steppe_champion");}}
 @Override protected SoundEvent getAmbientSound(){return kind()==BossKind.LIKHO?SoundEvents.WITCH_AMBIENT:SoundEvents.RAVAGER_AMBIENT;}@Override protected SoundEvent getHurtSound(DamageSource s){return kind()==BossKind.LIKHO?SoundEvents.WITCH_HURT:SoundEvents.RAVAGER_HURT;}@Override protected SoundEvent getDeathSound(){return kind()==BossKind.LIKHO?SoundEvents.WITCH_DEATH:SoundEvents.RAVAGER_DEATH;}@Override protected void playStepSound(BlockPos p,net.minecraft.block.BlockState s){playSound(kind()==BossKind.LIKHO?SoundEvents.BONE_BLOCK_STEP:SoundEvents.IRON_GOLEM_STEP,.5F,.6F);}
 @Override public void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);n.putString("BossEncounter",encounter);if(bossOwner!=null)n.putUUID("BossOwner",bossOwner);n.putInt("BossPhase",phase());CompoundNBT cd=new CompoundNBT();cooldowns.forEach((m,v)->cd.putInt(m.name(),v));n.put("BossCooldowns",cd);}
 @Override public void readAdditionalSaveData(CompoundNBT n){super.readAdditionalSaveData(n);owner=null;encounter=n.getString("BossEncounter");bossOwner=n.hasUUID("BossOwner")?n.getUUID("BossOwner"):null;entityData.set(PHASE,Math.max(1,Math.min(3,n.getInt("BossPhase"))));entityData.set(MOVE,Move.RECOVERY.ordinal());wait=20;CompoundNBT cd=n.getCompound("BossCooldowns");for(Move m:Move.values())cooldowns.put(m,Math.max(0,Math.min(m.cooldown,cd.getInt(m.name()))));BossKind k=kind();getAttribute(Attributes.MAX_HEALTH).setBaseValue(k.hp);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(k==BossKind.LIKHO?11:12);getAttribute(Attributes.ARMOR).setBaseValue(k.armor);getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(k.toughness);getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(k.resistance);getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(k.range);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(k.speed);}
}
