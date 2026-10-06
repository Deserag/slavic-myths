package org.slavicmyths.hunt;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attribute;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.core.particles.*;
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
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.server.level.ServerLevel;
import org.slavicmyths.registry.ModEntities;
import static org.slavicmyths.hunt.ElementRules.State;
/** Two additional move sets on the existing HuntMob owner, anchor, boss bar and encounter lifecycle. */
public final class ElementHuntMob extends HuntMob {
 private static final EntityDataAccessor<Integer> MOVE=SynchedEntityData.defineId(ElementHuntMob.class,EntityDataSerializers.INT),SINCE=SynchedEntityData.defineId(ElementHuntMob.class,EntityDataSerializers.INT);
 private final EnumMap<State,Long> ready=new EnumMap<>(State.class);
 private final Map<UUID,Long> hits=new HashMap<>();private final Set<UUID> deflected=new HashSet<>(),dashHits=new HashSet<>();private final List<Trace> traces=new ArrayList<>();
 private static final class Trace{Vec3 p;long until;Trace(Vec3 p,long t){this.p=p;until=t;}}
 private int ticks,memory,blocked,strafeAt,waterUntil,alignWait;private Vec3 aim=Vec3.ZERO,destination,seen,dashAlign;private boolean hit,aligned;private double travelled;private long copiesUntil;
 public ElementHuntMob(EntityType<? extends ElementHuntMob> t,Level w){super(t,w);xpReward=60;setNoGravity(serpent());}
 public boolean serpent(){return getType()==ModEntities.FIRE_SERPENT.get();}
 @Override public HuntTarget huntTarget(){return serpent()?HuntTarget.FIRE_SERPENT:HuntTarget.PODVEY;}
 @Override public boolean oven(){return false;}
 public static AttributeSupplier.Builder attributes(boolean fire){AttributeSupplier.Builder a=createMobAttributes().add(Attributes.MAX_HEALTH,fire?155:160).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.MOVEMENT_SPEED,fire?.24:.29).add(Attributes.ARMOR,fire?5:4).add(Attributes.KNOCKBACK_RESISTANCE,fire?.35:.75).add(Attributes.FOLLOW_RANGE,fire?40:38);return fire?a.add(Attributes.FLYING_SPEED,.34):a;}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(MOVE,0);builder.define(SINCE,0);}
 public State move(){return State.values()[entityData.get(MOVE)];}public float moveElapsed(float partial){return Math.max(0,(int)level().getGameTime()-entityData.get(SINCE)+partial);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));goalSelector.addGoal(1,new Fight());goalSelector.addGoal(5,new LookAtPlayerGoal(this,Player.class,16));goalSelector.addGoal(6,new RandomLookAroundGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<Player>(this,Player.class,true).setUnseenMemoryTicks(220));}
 @Override public boolean causeFallDamage(float distance,float multiplier,net.minecraft.world.damagesource.DamageSource source){return false;}
 @Override public void travel(Vec3 input){if(!serpent()){super.travel(input);return;}if(isEffectiveAi()){move(MoverType.SELF,getDeltaMovement());setDeltaMovement(getDeltaMovement().scale(.88));}else super.travel(input);calculateEntityAnimation(false);}
 @Override protected void playStepSound(BlockPos p,net.minecraft.world.level.block.state.BlockState b){}
 @Override protected SoundEvent getAmbientSound(){return serpent()?SoundEvents.BLAZE_AMBIENT:SoundEvents.PHANTOM_FLAP;}
 @Override protected SoundEvent getHurtSound(DamageSource d){return serpent()?SoundEvents.BLAZE_HURT:SoundEvents.PHANTOM_HURT;}
 @Override protected SoundEvent getDeathSound(){return serpent()?SoundEvents.BLAZE_DEATH:SoundEvents.PHANTOM_DEATH;}
 @Override public float getVoicePitch(){return serpent()?.7F:.6F;}
 @Override public boolean wetRain(){return level().isRainingAt(blockPosition());}
 private void cue(SoundEvent s){playSound(s,.8F,serpent()?.75F:.6F);}
 private void state(State s){entityData.set(MOVE,s.ordinal());entityData.set(SINCE,(int)level().getGameTime());ticks=0;navigation.stop();}
 private boolean available(State s){return level().getGameTime()>=ready.getOrDefault(s,0L);}
 private boolean valid(LivingEntity p){return p.isAlive()&&!(p instanceof Player&&(((Player)p).isCreative()||p.isSpectator()));}
 private List<Player> players(double radius){return level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius),p->valid(p)&&distanceTo(p)<=radius&&getSensing().hasLineOfSight(p));}
 private void ring(Vec3 p,double radius,boolean fire,int count){ServerLevel w=(ServerLevel)level();for(int i=0;i<count;i++){double a=i*Math.PI*2/count;w.sendParticles(fire?ParticleTypes.FLAME:ParticleTypes.CLOUD,p.x+Math.cos(a)*radius,p.y+.15,p.z+Math.sin(a)*radius,1,0,0,0,.01);}}
 private float fireDamage(float d){return ElementRules.fireDamage(d,wetRain());}
 private void fireHit(Player p,float d,int burn,float push){if(p.hurt(org.slavicmyths.combat.MythDamageSources.caused("fire_serpent",this),fireDamage(d))){int duration=dampFire(p,ElementRules.ignite(burn,wetRain()));p.setRemainingFireTicks(Math.max(p.getRemainingFireTicks(),duration));}Vec3 v=p.position().subtract(position()).multiply(1,0,1).normalize();p.knockback(push,-v.x,-v.z);}
 private void begin(State s,LivingEntity t){state(s);ready.put(s,level().getGameTime()+(s==State.MELEE_SNAP&&!serpent()?20:ElementRules.cooldown(s,wetRain())));aim=t.position().add(t.getDeltaMovement().scale(8)).subtract(position()).normalize();destination=t.position();hit=false;dashHits.clear();travelled=0;alignWait=0;aligned=false;
  if(s==State.ALIGN_DIVE)destination=t.position();if(s==State.ALIGN_DASH){destination=t.position().add(t.getDeltaMovement().scale(8)).add(0,.6,0);aim=destination.subtract(position()).normalize();dashAlign=position().subtract(aim.scale(2.5)).add(0,.3,0);}if(s==State.BROKEN_PATH_CHARGE)aim=t.position().subtract(position()).multiply(1,0,1).normalize();
  cue(s==State.ALIGN_DIVE?SoundEvents.FIREWORK_ROCKET_LAUNCH:s==State.ALIGN_DASH?SoundEvents.FIRE_AMBIENT:s==State.DECOY_CAST?SoundEvents.BLAZE_SHOOT:s==State.MELEE_SNAP?SoundEvents.PHANTOM_BITE:s==State.VORTEX_CHARGE?SoundEvents.ELYTRA_FLYING:s==State.GUST_CHARGE?SoundEvents.PLAYER_ATTACK_SWEEP:SoundEvents.PHANTOM_FLAP);
 }
 private void recover(int duration){state(State.RECOVERY);waterUntil=duration;setDeltaMovement(getDeltaMovement().scale(.2));cue(serpent()?SoundEvents.FIRE_AMBIENT:SoundEvents.PHANTOM_FLAP);}
 private double ground(Vec3 p){BlockPos q=BlockPos.containing(p);return level().hasChunkAt(q)?level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,q).getY():getY();}
 private void fly(Vec3 point,double speed){Vec3 delta=point.subtract(position());if(delta.lengthSqr()<.16){setDeltaMovement(getDeltaMovement().scale(.5));return;}Vec3 step=delta.normalize().scale(speed);BlockPos next=BlockPos.containing(position().add(step));if(!level().hasChunkAt(next)||!level().noCollision(this,getBoundingBox().move(step))){blocked++;setDeltaMovement(Vec3.ZERO);if(blocked%10==0){double turn=(blocked%20==0?1:-1);Vec3 side=new Vec3(-step.z*turn,.12,step.x*turn);if(level().hasChunkAt(BlockPos.containing(position().add(side)))&&level().noCollision(this,getBoundingBox().move(side)))setDeltaMovement(side);}return;}blocked=Math.max(0,blocked-1);setDeltaMovement(step);hasImpulse=true;}
 @Override public void tick(){super.tick();if(level().isClientSide)return;setNoGravity(serpent());if(!isAlive()){if(serpent())setDeltaMovement(getDeltaMovement().add(0,-.04,0));return;}
  if(serpent()&&isInWater()){if(tickCount%20==0)hurt(damageSources().drown(),4);if(move()!=State.WATER_STUN){state(State.WATER_STUN);waterUntil=50;ready.put(State.ALIGN_DASH,level().getGameTime()+130);cue(SoundEvents.FIRE_EXTINGUISH);}}
  if(tickCount%5==0){long now=level().getGameTime();traces.removeIf(t->t.until<=now);hits.entrySet().removeIf(e->e.getValue()+40<now);for(Trace tr:traces){((ServerLevel)level()).sendParticles(ParticleTypes.FLAME,tr.p.x,tr.p.y,tr.p.z,1,.2,.1,.2,.01);for(Player p:level().getEntitiesOfClass(Player.class,new AABB(tr.p.x-.7,tr.p.y-.6,tr.p.z-.7,tr.p.x+.7,tr.p.y+.7,tr.p.z+.7),this::valid))if(now>=hits.getOrDefault(p.getUUID(),0L)&&clearLine(tr.p,p.getEyePosition(1))){p.hurt(org.slavicmyths.combat.MythDamageSources.caused("serpent_trail",this),fireDamage(2));hits.put(p.getUUID(),now+20);}}}
  if(!serpent()&&tickCount%(level().isThundering()?5:6)==0){ServerLevel w=(ServerLevel)level();w.sendParticles(ParticleTypes.CLOUD,getX(),getY()+.6,getZ(),2,.65,.5,.65,.03);ParticleOptions debris=level().getBiome(blockPosition()).value().getPrecipitationAt(blockPosition())==net.minecraft.world.level.biome.Biome.Precipitation.SNOW?ParticleTypes.ITEM_SNOWBALL:new BlockParticleOption(ParticleTypes.BLOCK,level().getBlockState(blockPosition().below()));w.sendParticles(debris,getX(),getY()+1,getZ(),1,.5,.6,.5,.02);}
 }
 private void vortex(){if(ticks%4==0)ring(position(),7,false,18);for(Player p:players(7)){double d=distanceTo(p);Vec3 pull=position().subtract(p.position()).multiply(1,0,1).normalize().scale(ElementRules.pull(d));Vec3 v=p.getDeltaMovement().add(pull);double h=Math.sqrt(v.x*v.x+v.z*v.z);if(h>.45)v=new Vec3(v.x*.45/h,v.y,v.z*.45/h);p.setDeltaMovement(v);p.hurtMarked=true;
   if(d<1.5&&level().getGameTime()>=hits.getOrDefault(p.getUUID(),0L)){p.hurt(damageSources().mobAttack(this),6);p.setDeltaMovement(p.position().subtract(position()).multiply(1,0,1).normalize().scale(.65).add(0,.5,0));p.hurtMarked=true;hits.put(p.getUUID(),level().getGameTime()+30);}}
  if(ticks%2==0)for(Projectile e:level().getEntitiesOfClass(Projectile.class,getBoundingBox().inflate(5),e->e.getOwner()!=this&&(e instanceof AbstractArrow||e instanceof Snowball||e instanceof ThrownEgg)&&distanceTo(e)<5))if(!deflected.contains(e.getUUID())&&clearLine(position().add(0,1,0),e.position())){Vec3 radial=e.position().subtract(position()).multiply(1,0,1).normalize(),v=e.getDeltaMovement();if(v.dot(radial)>=0)continue;Vec3 side=new Vec3(-radial.z,v.y*.25,radial.x).normalize().scale(Math.max(.35,v.length()*.85));e.setDeltaMovement(side);e.hasImpulse=true;deflected.add(e.getUUID());}
 }
 private void projections(){if(copiesUntil>level().getGameTime())return;copiesUntil=level().getGameTime()+70;for(int i=0;i<2;i++){SerpentProjection e=ModEntities.SERPENT_PROJECTION.get().create(level());Vec3 offset=new Vec3(i==0?-2:2,0,1);if(e!=null&&level().hasChunkAt(BlockPos.containing(position().add(offset)))&&level().noCollision(e,getBoundingBox().move(offset))){e.parent=getUUID();e.until=copiesUntil;e.drift=new Vec3(i==0?-.08:.08,.025,.04);e.moveTo(getX()+offset.x,getY(),getZ()+1,getYRot(),0);level().addFreshEntity(e);}}particles(ParticleTypes.FLAME,10);}
 private final class Fight extends Goal {
  Fight(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean requiresUpdateEveryTick(){return true;}
  @Override public boolean canUse(){return isAlive();}
  @Override public void tick(){ticks++;LivingEntity t=getTarget();if(t==null&&owner!=null&&tickCount%20==0){Player ownerPlayer=level().getPlayerByUUID(owner);if(ownerPlayer!=null&&valid(ownerPlayer)&&distanceTo(ownerPlayer)<80){setTarget(ownerPlayer);t=ownerPlayer;}}if(t!=null&&!valid(t)){setTarget(null);t=null;}if(t!=null&&getSensing().hasLineOfSight(t)){memory=220;seen=t.position();}else if(t!=null&&!serpent()&&--memory<=0){setTarget(null);t=null;}
   Vec3 anchor=home==null?position():new Vec3(home.getX()+.5,home.getY(),home.getZ()+.5);if(home!=null&&(position().distanceToSqr(anchor)>72*72||t!=null&&t.position().distanceToSqr(anchor)>80*80||blocked>80)){state(State.RETURN_TO_ANCHOR);blocked=0;}
   if(move()==State.WATER_STUN){navigation.stop();if(serpent())fly(position().add(0,.7,0),.08);if(!isInWater()&&ticks>=waterUntil)recover(20);return;}
   if(move()==State.RETURN_TO_ANCHOR){if(serpent())fly(anchor,.24);else if(tickCount%10==0)navigation.moveTo(anchor.x,anchor.y,anchor.z,1);if(position().distanceToSqr(anchor)<16)state(serpent()?State.ORBIT:State.HUNT);return;}
   if(move()==State.RECOVERY){if(serpent())fly(position().add(0,(ground(position())+1.3-getY())*.5,0),.34*.75);if(ticks>=waterUntil)state(serpent()?State.ORBIT:State.HUNT);return;}
   if(t==null){if(serpent())fly(anchor.add(0,home==null?0:3,0),.2);else if(tickCount%30==0&&position().distanceToSqr(anchor)>16)navigation.moveTo(anchor.x,anchor.y,anchor.z,.7);return;}
   lookControl.setLookAt(t,30,30);double d=distanceTo(t);boolean sight=getSensing().hasLineOfSight(t);State s=move();
   if(s==State.ALIGN_DASH){if(!aligned){fly(dashAlign,.34);if(position().distanceToSqr(dashAlign)<.5||ticks>=30){aligned=true;aim=destination.subtract(position()).normalize();entityData.set(SINCE,(int)level().getGameTime());ticks=0;}return;}setDeltaMovement(Vec3.ZERO);aim=t.position().add(t.getDeltaMovement().scale(8)).add(0,.6,0).subtract(position()).normalize();boolean facing=DashFacing.turn(ElementHuntMob.this,aim,18);if(ticks%4==0)particles(ParticleTypes.FLAME,2);if(ticks>=16){if(!facing){if(++alignWait<=20)return;recover(10);return;}DashFacing.lock(ElementHuntMob.this,aim);state(State.FIRE_DASH);cue(SoundEvents.FIREWORK_ROCKET_LAUNCH);}return;}
   if(s==State.FIRE_DASH){fly(position().add(aim.scale(2)),.34*1.8);if(ticks%3==0&&traces.size()<14)traces.add(new Trace(position(),level().getGameTime()+40));for(Player p:players(2.2))if(!dashHits.contains(p.getUUID())&&getBoundingBox().inflate(.4).intersects(p.getBoundingBox())){fireHit(p,8,60,.7F);dashHits.add(p.getUUID());}if(ticks>=18||horizontalCollision)recover(18);return;}
   if(s==State.ALIGN_DIVE){Vec3 high=destination.add(0,8,0);if(!aligned){fly(high,.45);if(position().distanceToSqr(high)<2||ticks>=30){aligned=true;entityData.set(SINCE,(int)level().getGameTime());ticks=0;}return;}setDeltaMovement(Vec3.ZERO);if(ticks%4==0)ring(destination,3,true,18);if(ticks>=24){aim=destination.subtract(position()).normalize();if(!DashFacing.turn(ElementHuntMob.this,aim,25)){if(++alignWait<=20)return;recover(10);return;}DashFacing.lock(ElementHuntMob.this,aim);state(State.STAR_DIVE);cue(SoundEvents.FIREWORK_ROCKET_LAUNCH);}return;}
   if(s==State.STAR_DIVE){fly(destination,.85);if(position().distanceToSqr(destination)<2||verticalCollision||horizontalCollision||ticks>=24){if(position().distanceToSqr(destination)<12&&clearLine(position().add(0,.3,0),destination.add(0,.4,0))){ring(destination,3.25,true,22);cue(SoundEvents.GENERIC_EXPLODE.value());for(Player p:players(5))if(p.position().distanceToSqr(destination)<=3.25*3.25){fireHit(p,10,40,.8F);p.setDeltaMovement(p.getDeltaMovement().add(0,.2,0));p.hurtMarked=true;}}recover(30);}return;}
   if(s==State.DECOY_CAST){setDeltaMovement(getDeltaMovement().scale(.6));if(ticks==18)projections();if(ticks>=24)recover(12);return;}
   if(s==State.MELEE_SNAP){navigation.stop();if(ticks==(serpent()?7:8)&&d<=(serpent()?2.4:2.5)&&sight)t.hurt(damageSources().mobAttack(ElementHuntMob.this),7);if(ticks>=12)recover(6);return;}
   if(s==State.VORTEX_CHARGE){navigation.stop();if(ticks%4==0)ring(position(),2+ticks*.2,false,16);if(ticks>=20){state(State.VORTEX_ACTIVE);deflected.clear();cue(SoundEvents.ELYTRA_FLYING);}return;}
   if(s==State.VORTEX_ACTIVE){navigation.stop();if(isInWater()){recover(24);return;}vortex();if(ticks>=60){deflected.clear();recover(24);}return;}
   if(s==State.GUST_CHARGE){navigation.stop();if(ticks%4==0)particles(ParticleTypes.CLOUD,3);if(ticks>=14){state(State.GUST_RELEASE);cue(SoundEvents.PLAYER_ATTACK_SWEEP);for(Player p:players(8)){Vec3 v=p.position().subtract(position()).multiply(1,0,1).normalize();if(aim.multiply(1,0,1).normalize().dot(v)<.843)continue;boolean shield=p.isBlocking()&&p.getLookAngle().dot(position().subtract(p.position()).normalize())>.4;p.hurt(damageSources().mobAttack(ElementHuntMob.this),4);p.setDeltaMovement(p.getDeltaMovement().add(v.scale(shield?1.25*.55:1.25)).add(0,.15,0));p.hurtMarked=true;}for(int i=1;i<=8;i++)ring(position().add(aim.scale(i)),.35,false,3);}return;}
   if(s==State.GUST_RELEASE){if(ticks>=8)recover(12);return;}
   if(s==State.BROKEN_PATH_CHARGE){navigation.stop();aim=t.position().subtract(position()).multiply(1,0,1).normalize();boolean facing=DashFacing.turn(ElementHuntMob.this,aim,18);if(ticks>=10){if(!facing){if(++alignWait<=20)return;recover(10);return;}DashFacing.lock(ElementHuntMob.this,aim);state(State.BROKEN_PATH_DASH);cue(SoundEvents.PHANTOM_FLAP);}return;}
   if(s==State.BROKEN_PATH_DASH){navigation.stop();Vec3 step=aim.scale(.55);if(level().hasChunkAt(BlockPos.containing(position().add(step)))&&level().noCollision(ElementHuntMob.this,getBoundingBox().move(step))){setDeltaMovement(new Vec3(step.x,getDeltaMovement().y,step.z));hasImpulse=true;travelled+=.55;}else travelled=7;
    if(!hit&&d<2.1&&sight){t.hurt(damageSources().mobAttack(ElementHuntMob.this),6);t.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,60));t.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40));t.knockback(.3F,-aim.z,aim.x);hit=true;}if(travelled>=7||ticks>=14)recover(16);return;}
   if(isInWater()&&!serpent()){if(tickCount%10==0)navigation.moveTo(getX()+random.nextInt(7)-3,getY()+1,getZ()+random.nextInt(7)-3,1.2);return;}
   if(sight){if(d<=(serpent()?2.4:2.5)&&available(State.MELEE_SNAP)){begin(State.MELEE_SNAP,t);return;}
    if(serpent()){if(available(State.DECOY_CAST)&&copiesUntil<=level().getGameTime()&&tickCount%3==0){begin(State.DECOY_CAST,t);return;}if(d>=7&&d<=16&&available(State.ALIGN_DASH)){begin(State.ALIGN_DASH,t);return;}if(d>4&&available(State.ALIGN_DIVE)){begin(State.ALIGN_DIVE,t);return;}}
    else {if(d>=4&&d<=10&&available(State.BROKEN_PATH_CHARGE)){begin(State.BROKEN_PATH_CHARGE,t);return;}if(d<=7&&available(State.VORTEX_CHARGE)){begin(State.VORTEX_CHARGE,t);return;}if(d<=8&&available(State.GUST_CHARGE)){begin(State.GUST_CHARGE,t);return;}}
   }
   if(serpent()){if(!sight&&ticks>80&&available(State.GUST_CHARGE)){ready.put(State.GUST_CHARGE,level().getGameTime()+100);EmberClump e=ModEntities.EMBER_CLUMP.get().create(level());if(e!=null){e.setOwner(ElementHuntMob.this);e.setPos(getX(),getEyeY(),getZ());Vec3 v=(seen==null?t.position():seen).add(0,1,0).subtract(e.position());e.shoot(v.x,v.y,v.z,.8F,1);level().addFreshEntity(e);cue(SoundEvents.BLAZE_SHOOT);}}
    double angle=tickCount*.07+getId();double y=Math.min(ground(t.position())+12,Math.max(ground(t.position())+3,t.getY()+4+Math.sin(angle*.7)*2));Vec3 point=sight?t.position().add(Math.cos(angle)*7,y-t.getY(),Math.sin(angle)*7):(seen==null?anchor:seen).add(Math.cos(angle)*6,4,Math.sin(angle)*6);fly(point,.34);if(move()!=State.ORBIT)state(State.ORBIT);
   }else if(tickCount%10==0){if(tickCount>=strafeAt&&sight&&d<10){Vec3 toward=t.position().subtract(position()).multiply(1,0,1).normalize();double side=random.nextBoolean()?5:-5;destination=t.position().add(-toward.z*side,0,toward.x*side);strafeAt=tickCount+30+random.nextInt(31);state(State.STRAFE);}Vec3 point=move()==State.STRAFE&&destination!=null?destination:seen==null?t.position():seen;boolean ok=navigation.moveTo(point.x,point.y,point.z,1);if(!ok){blocked+=10;navigation.moveTo(getX()+random.nextInt(9)-4,getY(),getZ()+random.nextInt(9)-4,1);}else blocked=0;if(move()==State.STRAFE&&ticks>=30)state(State.HUNT);}
  }
 }
 @Override public void die(DamageSource d){boolean before=dead;super.die(d);if(!before&&dead&&!level().isClientSide){cue(serpent()?SoundEvents.FIRE_EXTINGUISH:SoundEvents.ELYTRA_FLYING);if(d.getEntity() instanceof Player)org.slavicmyths.item.FolkAccessoryItem.award((LivingEntity)d.getEntity(),serpent()?"fire_in_the_sky":"catch_the_wind");}}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);CompoundTag cds=new CompoundTag();for(Map.Entry<State,Long> e:ready.entrySet())cds.putLong(e.getKey().name(),e.getValue());n.put("ElementReady",cds);n.putLong("CopiesUntil",copiesUntil);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);CompoundTag cds=n.getCompound("ElementReady");for(State s:State.values())ready.put(s,cds.getLong(s.name()));copiesUntil=n.getLong("CopiesUntil");state(serpent()?State.ORBIT:State.HUNT);getAttribute(Attributes.MAX_HEALTH).setBaseValue(serpent()?155:160);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(serpent()?.24:.29);getAttribute(Attributes.ARMOR).setBaseValue(serpent()?5:4);getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(serpent()?.35:.75);getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(serpent()?40:38);}
 private static int dampFire(LivingEntity e,int ticks){var holder=e.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getOrThrow(net.minecraft.world.item.enchantment.Enchantments.FIRE_PROTECTION);int level=net.minecraft.world.item.enchantment.EnchantmentHelper.getEnchantmentLevel(holder,e);return level>0?ticks-Mth.floor(ticks*level*.15F):ticks;}
}
