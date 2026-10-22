package org.slavicmyths.hunt;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.network.chat.Component;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.*;
import org.slavicmyths.registry.ModEntities;
import static org.slavicmyths.hunt.BossRules.Move;
/** Exclusive combat scheduler: bounded navigation, fixed launch headings, no terrain mutation. */
public final class WorldBoss extends HuntMob {
 private static final EntityDataAccessor<Integer> MOVE=SynchedEntityData.defineId(WorldBoss.class,EntityDataSerializers.INT),START=SynchedEntityData.defineId(WorldBoss.class,EntityDataSerializers.INT),PHASE=SynchedEntityData.defineId(WorldBoss.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> TUGARIN_PREVIOUS=SynchedEntityData.defineId(WorldBoss.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Integer> TUGARIN_STATE=SynchedEntityData.defineId(WorldBoss.class,EntityDataSerializers.INT);
 public enum TugarinState {PHASE_ONE,TRANSFORMING,PHASE_TWO}
 public TugarinState tugarinState(){return TugarinState.values()[entityData.get(TUGARIN_STATE)];}
 void tugarinState(TugarinState state){entityData.set(TUGARIN_STATE,state.ordinal());entityData.set(PHASE,state==TugarinState.PHASE_TWO?2:1);if(!level().isClientSide){if(state==TugarinState.PHASE_TWO)addEffect(new MobEffectInstance(org.slavicmyths.registry.ModEffects.SERPENT_GUARD,-1,0,false,true,true));else removeEffect(org.slavicmyths.registry.ModEffects.SERPENT_GUARD);}}
 public Move previousTugarinMove(){return Move.values()[entityData.get(TUGARIN_PREVIOUS)];}
 void tugarinMove(Move move){if(move==Move.RECOVERY)entityData.set(TUGARIN_PREVIOUS,move().ordinal());entityData.set(MOVE,move.ordinal());entityData.set(START,(int)level().getGameTime());}
 public String encounter="";public UUID bossOwner;
 private final EnumMap<Move,Integer> cooldowns=new EnumMap<>(Move.class);
 private final Set<UUID> hits=new HashSet<>();private final List<Vec3> zones=new ArrayList<>();
 private final ServerBossEvent bossBar=new ServerBossEvent(Component.literal(""),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
 private TugarinCombat tugarin;
 private TugarinCombat tugarin(){if(tugarin==null)tugarin=new TugarinCombat(this);return tugarin;}
 private UUID attackTarget;
 private int time,wait,memory,unreachable,align;private Vec3 heading=Vec3.ZERO,pivot;private UUID held;private int holdTicks;
 public WorldBoss(EntityType<? extends WorldBoss> type,Level world){super(type,world);xpReward=kind().xp;setPersistenceRequired();}
 @Override public boolean isPushable(){return move()!=Move.SPIN&&super.isPushable();}
 @Override public void knockback(double power,double x,double z){if(kind()==BossKind.LIKHO)LikhoCombatTrace.event(this,"knockback power="+power+" before="+getDeltaMovement());if(move()!=Move.SPIN)super.knockback(power,x,z);}
 @Override protected net.minecraft.world.entity.EntityDimensions getDefaultDimensions(net.minecraft.world.entity.Pose pose){return kind()==BossKind.LIKHO?(morState()!=0?EntityDimensions.scalable(1.4F,4.4F).withEyeHeight(4.05F):super.getDefaultDimensions(pose).withEyeHeight(3.02F)):EntityDimensions.scalable(3.5F,4.85F).withEyeHeight(4.15F);}
 public BossKind kind(){return getType()==ModEntities.LIKHO_ONE_EYED.get()?BossKind.LIKHO:BossKind.TUGARIN;}
 @Override public boolean oven(){return false;}@Override protected boolean miniBoss(){return false;}@Override protected boolean showsHuntBar(){return false;}@Override public HuntTarget huntTarget(){return null;}
 public static AttributeSupplier.Builder attributes(BossKind k){return createMobAttributes().add(Attributes.MAX_HEALTH,k.hp).add(Attributes.ATTACK_DAMAGE,k==BossKind.LIKHO?11:12).add(Attributes.MOVEMENT_SPEED,k.speed).add(Attributes.ARMOR,k.armor).add(Attributes.ARMOR_TOUGHNESS,k.toughness).add(Attributes.KNOCKBACK_RESISTANCE,k.resistance).add(Attributes.FOLLOW_RANGE,k.range);}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(MOVE,0);builder.define(START,0);builder.define(PHASE,1);builder.define(TUGARIN_STATE,0);builder.define(TUGARIN_PREVIOUS,0);}
 public Move move(){return Move.values()[entityData.get(MOVE)];}public int phase(){return entityData.get(PHASE);}public float animationTime(float p){return Math.max(0,(int)level().getGameTime()-entityData.get(START)+p);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new org.slavicmyths.mor.MorGoal(this));goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new Fight());goalSelector.addGoal(5,new RandomLookAroundGoal(this));if(kind()==BossKind.LIKHO){targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<Player>(this,Player.class,true).setUnseenMemoryTicks(260));}}
 @Override public boolean canChangeDimensions(net.minecraft.world.level.Level from,net.minecraft.world.level.Level to){return false;}
 @Override public void tick(){super.tick();if(level().isClientSide)return;LikhoDpsProbe.sample(this);if(!isAlive()){releaseHeld(false);return;}if(!encounter.isEmpty()){HuntRecords.Boss b=HuntRecords.get((ServerLevel)level()).bosses.get(encounter);if(b==null||!b.active||!getUUID().equals(b.target)){discard();return;}}
  if(tickCount%20==0){bossBar.setName(kind()==BossKind.TUGARIN&&tugarinState()!=TugarinState.PHASE_ONE?Component.translatable(tugarinState()==TugarinState.TRANSFORMING?"boss.slavicmyths.tugarin.transforming":"boss.slavicmyths.tugarin.true_form"):getDisplayName());bossBar.setColor(kind()==BossKind.LIKHO?BossEvent.BossBarColor.PURPLE:BossEvent.BossBarColor.RED);List<ServerPlayer> nearby=level().getEntitiesOfClass(ServerPlayer.class,getBoundingBox().inflate(48),p->p.isAlive()&&!p.isSpectator()&&distanceToSqr(p)<=48*48);for(ServerPlayer p:new ArrayList<>(bossBar.getPlayers()))if(!nearby.contains(p))bossBar.removePlayer(p);for(ServerPlayer p:nearby)bossBar.addPlayer(p);}
  bossBar.setProgress(Math.max(0,getHealth()/getMaxHealth()));bossBar.setVisible(true);
 }
 @Override public void onRemovedFromLevel(){releaseHeld(false);bossBar.removeAllPlayers();super.onRemovedFromLevel();}
 @Override public void checkDespawn(){if(!level().isClientSide&&level().getDifficulty()==Difficulty.PEACEFUL){if(!encounter.isEmpty()){HuntRecords d=HuntRecords.get((ServerLevel)level());HuntRecords.Boss b=d.bosses.get(encounter);if(b!=null&&getUUID().equals(b.target)){b.active=false;b.ready=level().getGameTime()+1200;d.setDirty();}}discard();}}
 private boolean ready(Move m){return cooldowns.getOrDefault(m,0)==0;}
 private void begin(Move m,LivingEntity target){attackTarget=target.getUUID();LikhoCombatTrace.event(this,"begin "+m+" focus="+attackTarget);LikhoDpsProbe.begin(this,m.name());entityData.set(MOVE,m.ordinal());entityData.set(START,(int)level().getGameTime());time=0;align=0;hits.clear();zones.clear();navigation.stop();heading=target.position().subtract(position()).multiply(1,0,1).normalize();pivot=position();cooldowns.put(m,BossRules.cooldown(kind(),m,phase()));
  if(m==Move.GROUND)for(int i=0;i<3;i++)zones.add(target.position().add(target.getDeltaMovement().scale(8+i*4)).add((i-1)*1.8,0,(i%2)*1.4));
  playSound(m==Move.BREATH?SoundEvents.FIRECHARGE_USE:m==Move.SPIN||m==Move.SWEEP?SoundEvents.PLAYER_ATTACK_SWEEP:m==Move.GAZE?SoundEvents.EVOKER_PREPARE_ATTACK:m==Move.GRAB?SoundEvents.EVOKER_PREPARE_SUMMON:m==Move.GROUND?SoundEvents.EVOKER_CAST_SPELL:m==Move.LEAP?SoundEvents.PHANTOM_FLAP:m==Move.SLAM?SoundEvents.IRON_GOLEM_ATTACK:m==Move.STONE?SoundEvents.SNOWBALL_THROW:m==Move.PUNCH?SoundEvents.PLAYER_ATTACK_STRONG:m==Move.CHASE?SoundEvents.WITCH_AMBIENT:SoundEvents.RAVAGER_ROAR,.8F,.55F);
 }
 private void recover(int duration){recover(duration,"action_window_or_invalid_focus");}
 private void recover(int duration,String reason){LikhoCombatTrace.event(this,"recover reason="+reason+" duration="+duration);attackTarget=null;releaseHeld(false);entityData.set(MOVE,Move.RECOVERY.ordinal());entityData.set(START,(int)level().getGameTime());time=0;wait=duration;navigation.stop();}
 private boolean strike(LivingEntity p,float damage,double radius,double cone,float kb){
  String rejected=kind()==BossKind.LIKHO&&!LikhoTargets.allowed(this,p)?"protected_or_not_enemy":!p.isAlive()?"dead":p instanceof Player&&(((Player)p).isCreative()||p.isSpectator())?"protected_player":distanceTo(p)>radius?"range":!getSensing().hasLineOfSight(p)?"LOS":heading.dot(p.position().subtract(position()).multiply(1,0,1).normalize())<cone?"cone":null;
  if(rejected!=null){LikhoCombatTrace.event(this,"strike.reject reason="+rejected+" victim="+p.getUUID());return false;}
  boolean shield=p.isBlocking()&&p.getLookAngle().dot(position().subtract(p.position()).normalize())>.35;
  boolean hit=p.hurt(damageSources().mobAttack(this),damage);LikhoCombatTrace.event(this,"damage victim="+p.getUUID()+" amount="+damage+" accepted="+hit);
  Vec3 d=p.position().subtract(position()).multiply(1,0,1).normalize();if(kb>0)p.knockback(shield?kb*.35F:kb,-d.x,-d.z);return hit;
 }

 private List<Player> players(double radius){return level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius),p->p.isAlive()&&!p.isCreative()&&!p.isSpectator());}
 private List<? extends LivingEntity> victims(double radius){return kind()==BossKind.LIKHO?level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(radius),e->LikhoTargets.allowed(this,e)):players(radius);}
 private LivingEntity attackFocus(){if(attackTarget==null)return null;var e=((ServerLevel)level()).getEntity(attackTarget);return e instanceof LivingEntity living&&LikhoTargets.allowed(this,living)?living:null;}
 private List<? extends LivingEntity> closeEnemies(){return victims(4).stream().filter(e->distanceToSqr(e)<=16&&getSensing().hasLineOfSight(e)).toList();}
 private void area(float damage,double r,double cone,float kb){for(LivingEntity p:victims(r))strike(p,damage,r,cone,kb);}
 private void fate(LivingEntity p){p.addEffect(new MobEffectInstance(BossEffects.ILL_FATE,160));}
 private void gaze(Player p){
  if(!p.hurt(damageSources().mobAttack(this),2))return;
  fate(p);
  p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,LikhoGazeRules.WEAKNESS_TICKS,0));
  CompoundTag tags=p.getPersistentData();
  long now=level().getGameTime();
  if(LikhoGazeRules.visualReady(now,tags.getLong(LikhoGazeRules.VISUAL_READY_TAG))){
   tags.putLong(LikhoGazeRules.VISUAL_READY_TAG,now+LikhoGazeRules.VISUAL_COOLDOWN_TICKS);
   p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,LikhoGazeRules.BLINDNESS_TICKS,0));
   p.addEffect(new MobEffectInstance(MobEffects.CONFUSION,LikhoGazeRules.NAUSEA_TICKS,0));
  }
 }
 public void cancelTarget(Player player){if(heldEntity()==player)releaseHeld(false);if(getTarget()==player){setTarget(null);recover(20);}}
 private LivingEntity heldEntity(){if(held==null||level().isClientSide)return null;Entity e=((ServerLevel)level()).getEntity(held);return e instanceof LivingEntity?(LivingEntity)e:null;}
 private void releaseHeld(boolean toss){LivingEntity p=heldEntity();held=null;holdTicks=0;if(p==null)return;p.fallDistance=0;p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING,60,0,false,false));if(toss&&p.isAlive()&&(kind()!=BossKind.LIKHO||LikhoTargets.allowed(this,p))){p.hurt(damageSources().mobAttack(this),6);p.setDeltaMovement(heading.scale(.72).add(0,.35,0));p.hurtMarked=true;}}
 private boolean spinSafe(){return onGround()&&!isInWater()&&level().noCollision(getBoundingBox().inflate(.7,0,.7).expandTowards(0,.4,0));}
 private Move choose(LivingEntity t,boolean visible,int close){if(!visible)return kind()==BossKind.TUGARIN&&phase()>=2&&unreachable>=50&&ready(Move.STONE)?Move.STONE:Move.IDLE;double d=distanceTo(t);if(kind()==BossKind.LIKHO){return LikhoCombatRules.choose(phase(),d,visible,t instanceof Player,close,this::ready);}else{if(phase()==3&&d<=7&&ready(Move.BREATH))return Move.BREATH;if(d<=7&&ready(Move.SPIN)&&spinSafe())return Move.SPIN;if(d>=7&&d<=16&&onGround()&&!isInWater()&&ready(Move.CHARGE))return Move.CHARGE;if(phase()>=2&&d<=4.5&&ready(Move.SLAM))return Move.SLAM;if(phase()>=2&&(d>10||unreachable>=50)&&ready(Move.STONE))return Move.STONE;if(d<=2.8&&ready(Move.PUNCH))return Move.PUNCH;}return Move.IDLE;}
 private void release(LivingEntity t){LikhoCombatTrace.event(this,"release focus="+(t==null?"none":t.getUUID()));switch(move()){
  case SWEEP:area(10,4,Math.cos(Math.toRadians(55)),.55F);break;
  case PUNCH:strike(t,12,2.8,.5,.8F);break;
  case GRAB:if(strike(t,4,3.5,Math.cos(Math.toRadians(30)),0)){held=t.getUUID();holdTicks=0;fate(t);entityData.set(MOVE,Move.HOLD.ordinal());entityData.set(START,(int)level().getGameTime());time=0;}break;
  case GAZE:for(Player p:players(10))if(LikhoTargets.allowed(this,p)&&distanceTo(p)<=10&&getSensing().hasLineOfSight(p)&&heading.dot(p.position().subtract(position()).multiply(1,0,1).normalize())>=Math.cos(Math.toRadians(25))&&p.getLookAngle().dot(getEyePosition(1).subtract(p.getEyePosition(1)).normalize())>=.75){gaze(p);}break;
  case GROUND:for(Vec3 z:zones)if(clearLine(getEyePosition(1),z.add(0,.3,0)))for(LivingEntity p:victims(14))if(p.position().distanceToSqr(z)<=1.75*1.75&&clearLine(z.add(0,.3,0),p.position().add(0,.3,0))&&hits.add(p.getUUID())){boolean hit=p.hurt(damageSources().mobAttack(this),6);LikhoCombatTrace.event(this,"ground victim="+p.getUUID()+" accepted="+hit);Vec3 d=p.position().subtract(z).normalize();p.knockback(.6F,-d.x,-d.z);p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,50));}break;
  case LEAP:setDeltaMovement(heading.scale(Math.min(.95,distanceTo(t)/14)).add(0,.55,0));hasImpulse=true;break;
  case SLAM:area(11,4.5,Math.cos(Math.toRadians(50)),1);dust();break;
  case STONE:BossStone stone=new BossStone(ModEntities.TUGARIN_STONE.get(),level());stone.setOwner(this);stone.setPos(getX(),getEyeY()-.2,getZ());Vec3 d=t.getEyePosition(1).subtract(stone.position());stone.shoot(d.x,d.y+d.length()*.10,d.z,1.1F,.5F);level().addFreshEntity(stone);break;
  default:break;
 }}
 private void dust(){((ServerLevel)level()).sendParticles(ParticleTypes.CLOUD,getX(),getY()+.15,getZ(),18,1,.1,1,.035);playSound(SoundEvents.IRON_GOLEM_STEP,.8F,.5F);}
 private final class Fight extends Goal{
  Fight(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean requiresUpdateEveryTick(){return true;}
  public boolean canUse(){return isAlive()&&morState()==0;}public void stop(){LikhoCombatTrace.event(WorldBoss.this,"goal.stop");attackTarget=null;releaseHeld(false);navigation.stop();entityData.set(MOVE,Move.IDLE.ordinal());}
  public void tick(){if(kind()==BossKind.TUGARIN){tugarin().tick();return;}for(Move m:Move.values())cooldowns.computeIfPresent(m,(k,v)->Math.max(0,v-1));int next=BossRules.phase(kind(),getHealth()/getMaxHealth());if(next>phase()&&LikhoCombatRules.phaseBoundary(move())){LikhoCombatTrace.event(WorldBoss.this,"phase "+phase()+" -> "+next);entityData.set(PHASE,next);recover(18,"phase_boundary");entityData.set(MOVE,Move.TRANSITION.ordinal());playSound(SoundEvents.RAVAGER_ROAR,1,.55F);}
   getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(kind().speed*(kind()==BossKind.LIKHO?(phase()==3?1.12:phase()==2?1.07:1):(phase()>=2?1.06:1))*(move()==Move.CHASE?1.25:move()==Move.RECOVERY&&wait>=30?.7:1));
   LivingEntity t=getTarget();if(t!=null&&(!LikhoTargets.allowed(WorldBoss.this,t)||!t.isAlive()||t instanceof Player&&(((Player)t).isCreative()||t.isSpectator()))){setTarget(null);t=null;}boolean sight=t!=null&&getSensing().hasLineOfSight(t);if(sight)memory=260;else if(t!=null&&--memory<=0){setTarget(null);t=null;}
   if(LikhoCombatRules.committed(move())){LivingEntity focus=attackFocus();if(focus!=null)t=focus;}
   if(move()==Move.RECOVERY||move()==Move.TRANSITION){navigation.stop();if(++time>=wait)entityData.set(MOVE,Move.IDLE.ordinal());return;}
   if(t==null){if(move()!=Move.IDLE)recover(10,"no_valid_focus");if(home!=null&&tickCount%10==0)navigation.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,1);return;}
   if(home!=null&&distanceToSqr(home.getX()+.5,home.getY(),home.getZ()+.5)>96*96){setTarget(null);recover(10,"home_leash");return;}
   if(move()==Move.IDLE){var nearby=closeEnemies();if(nearby.size()>=2){t=nearby.stream().min(Comparator.comparingDouble(WorldBoss.this::distanceToSqr)).orElseThrow();sight=true;}Move chosen=choose(t,sight,nearby.size());if(chosen!=Move.IDLE){begin(chosen,t);return;}lookControl.setLookAt(t,25,25);if(tickCount%10==0){boolean path=navigation.moveTo(t,1);unreachable=path?0:unreachable+10;}return;}
   time++;Move m=move();if(m==Move.HOLD){LivingEntity victim=heldEntity();if(victim==null||!LikhoTargets.allowed(WorldBoss.this,victim)||!victim.isAlive()||org.slavicmyths.combat.CreatureSafety.protectedPlayer(victim)||distanceTo(victim)>5){recover(26);return;}navigation.stop();Vec3 desired=position().add(heading.scale(1.4)).add(0,1.2,0);victim.setDeltaMovement(desired.subtract(victim.position()).scale(.25));victim.fallDistance=0;victim.hurtMarked=true;if(++holdTicks>=20){releaseHeld(true);recover(26);}return;}
   if(m==Move.CHASE){if(tickCount%8==0)navigation.moveTo(t,1.25);heading=t.position().subtract(position()).multiply(1,0,1).normalize();if(time%25==0)area(10,4,Math.cos(Math.toRadians(55)),.55F);if(time>=80)recover(40);return;}
   navigation.stop();if(m==Move.CHARGE||m==Move.LEAP){if(time<=m.windup){heading=t.position().add(t.getDeltaMovement().scale(3)).subtract(position()).multiply(1,0,1).normalize();boolean aligned=DashFacing.turn(WorldBoss.this,heading,m==Move.CHARGE?18:25);if(time==m.windup&&!aligned){if(++align<=20){time--;entityData.set(START,entityData.get(START)+1);return;}recover(10);return;}if(time==m.windup){if(m==Move.CHARGE&&!level().noCollision(getBoundingBox().expandTowards(heading.scale(3)))){recover(30);return;}DashFacing.lock(WorldBoss.this,heading);}}else DashFacing.lock(WorldBoss.this,heading);}
   else if(m!=Move.SPIN&&time<=m.windup)DashFacing.turn(WorldBoss.this,heading,25);
   if(m==Move.GROUND&&time<=24&&time%4==0)for(Vec3 z:zones)((ServerLevel)level()).sendParticles(ParticleTypes.SMOKE,z.x,z.y+.1,z.z,6,.65,.04,.65,.01);
   if(time==m.windup)release(t);
   if(m==Move.CHARGE&&time>12){setDeltaMovement(heading.scale(1.7).add(0,getDeltaMovement().y,0));hasImpulse=true;for(Player p:players(2.4))if(!hits.contains(p.getUUID())&&strike(p,10,2.4,.2,1.2F))hits.add(p.getUUID());if(horizontalCollision){dust();recover(30);return;}if(time>=34){recover(20);return;}}
   if(m==Move.LEAP&&time>18){if(onGround()&&time>20){area(8,3.5,-1,1);dust();recover(26);return;}if(time>=48){recover(26);return;}}
   if(m==Move.SPIN){setDeltaMovement(0,getDeltaMovement().y,0);if(time>=22&&time<28)for(Player p:players(phase()==3?4.5:4))if(!hits.contains(p.getUUID())&&strike(p,phase()==3?10:9,phase()==3?4.5:4,-1,phase()==3?1.9F:1.7F)){hits.add(p.getUUID());if(!p.isBlocking())p.setDeltaMovement(p.getDeltaMovement().add(0,.25,0));}if(time>=36){recover(phase()==3?24:28);return;}return;}
   if(m==Move.BREATH&&time>22&&time<=46){if(time%2==0)((ServerLevel)level()).sendParticles(ParticleTypes.FLAME,getX()+heading.x*2,getEyeY()-.3,getZ()+heading.z*2,7,1,.35,1,.04);if(time==24||time==34||time==44)for(Player p:players(7))if(strike(p,4,7,Math.cos(Math.toRadians(30)),0))p.igniteForSeconds(3);if(time==46)recover(18);return;}
   if(m!=Move.CHARGE&&m!=Move.LEAP&&time>=m.windup+m.recovery)recover(1);
  }
 }
 @Override public void die(DamageSource source){boolean was=dead;super.die(source);if(!was&&dead&&!level().isClientSide){releaseHeld(false);bossBar.removeAllPlayers();if(!encounter.isEmpty())HuntRecords.get((ServerLevel)level()).finishBoss(encounter,getUUID(),level().getGameTime());if(source.getEntity() instanceof Player)org.slavicmyths.item.FolkAccessoryItem.award((Player)source.getEntity(),kind()==BossKind.LIKHO?"name_your_misfortune":"steppe_champion");}}
 @Override public void setTarget(LivingEntity target){LivingEntity before=getTarget();super.setTarget(target);if(before!=getTarget())LikhoCombatTrace.event(this,"target.change from="+(before==null?"none":before.getUUID()));}
 @Override public boolean hurt(DamageSource source,float amount){if(kind()!=BossKind.TUGARIN){Move before=move();boolean hit=super.hurt(source,amount);LikhoCombatTrace.event(this,"hurt accepted="+hit+" amount="+amount+" before="+before+" after="+move());return hit;}if(tugarinState()==TugarinState.TRANSFORMING)amount*=.15F;else if(tugarinState()==TugarinState.PHASE_TWO)amount*=.5F;boolean hit=super.hurt(source,amount);if(hit&&source.getEntity() instanceof LivingEntity attacker)tugarin().threat(attacker,amount);if(hit&&isAlive())tugarin().startTransformationIfNeeded();return hit;}
 @Override public int getAmbientSoundInterval(){return kind()==BossKind.TUGARIN?400:super.getAmbientSoundInterval();}
 @Override protected SoundEvent getAmbientSound(){return kind()==BossKind.LIKHO?(morState()!=0?null:SoundEvents.WITCH_AMBIENT):tugarinState()==TugarinState.PHASE_ONE?org.slavicmyths.registry.ModSounds.boss("tugarin_phase1_idle"):org.slavicmyths.registry.ModSounds.boss("tugarin_phase2_idle");}@Override protected SoundEvent getHurtSound(DamageSource s){return kind()==BossKind.LIKHO?(morState()!=0?org.slavicmyths.mor.MorAudio.hurt(5):SoundEvents.WITCH_HURT):tugarinState()==TugarinState.PHASE_ONE?org.slavicmyths.registry.ModSounds.boss("tugarin_phase1_hurt"):org.slavicmyths.registry.ModSounds.boss("tugarin_phase2_hurt");}@Override protected SoundEvent getDeathSound(){return kind()==BossKind.LIKHO?(morState()!=0?org.slavicmyths.mor.MorAudio.death(5):SoundEvents.WITCH_DEATH):tugarinState()==TugarinState.PHASE_ONE?org.slavicmyths.registry.ModSounds.boss("tugarin_phase1_death"):org.slavicmyths.registry.ModSounds.boss("tugarin_phase2_death");}@Override protected void playStepSound(BlockPos p,net.minecraft.world.level.block.state.BlockState s){playSound(kind()==BossKind.LIKHO?SoundEvents.BONE_BLOCK_STEP:org.slavicmyths.registry.ModSounds.boss("tugarin.phase1.step"),.5F,.6F);}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);org.slavicmyths.combat.CreatureHealthBalance.mark(n);if(kind()==BossKind.TUGARIN)tugarin().save(n);n.putString("BossEncounter",encounter);if(bossOwner!=null)n.putUUID("BossOwner",bossOwner);n.putInt("BossPhase",phase());CompoundTag cd=new CompoundTag();cooldowns.forEach((m,v)->cd.putInt(m.name(),v));n.put("BossCooldowns",cd);}
 @Override public void readAdditionalSaveData(CompoundTag n){n=org.slavicmyths.combat.CreatureHealthBalance.prepare(n,kind().hp,kind()==BossKind.LIKHO?320:350,false);super.readAdditionalSaveData(n);owner=null;encounter=n.getString("BossEncounter");bossOwner=n.hasUUID("BossOwner")?n.getUUID("BossOwner"):null;entityData.set(PHASE,Math.max(1,Math.min(3,n.getInt("BossPhase"))));entityData.set(MOVE,Move.RECOVERY.ordinal());wait=20;CompoundTag cd=n.getCompound("BossCooldowns");for(Move m:Move.values())cooldowns.put(m,Math.max(0,Math.min(m.cooldown,cd.getInt(m.name()))));BossKind k=kind();getAttribute(Attributes.MAX_HEALTH).setBaseValue(k.hp);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(k==BossKind.LIKHO?11:12);getAttribute(Attributes.ARMOR).setBaseValue(k.armor);getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(k.toughness);getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(k.resistance);getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(k.range);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(k.speed);if(k==BossKind.TUGARIN)tugarin().load(n);}
}
