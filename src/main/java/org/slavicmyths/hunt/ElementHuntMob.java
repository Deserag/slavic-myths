package org.slavicmyths.hunt;
import java.util.*;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.projectile.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.*;
import net.minecraft.particles.*;
import net.minecraft.potion.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.*;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import org.slavicmyths.registry.ModEntities;
import static org.slavicmyths.hunt.ElementRules.State;
/** Two additional move sets on the existing HuntMob owner, anchor, boss bar and encounter lifecycle. */
public final class ElementHuntMob extends HuntMob {
 private static final DataParameter<Integer> MOVE=EntityDataManager.defineId(ElementHuntMob.class,DataSerializers.INT),SINCE=EntityDataManager.defineId(ElementHuntMob.class,DataSerializers.INT);
 private final EnumMap<State,Long> ready=new EnumMap<>(State.class);
 private final Map<UUID,Long> hits=new HashMap<>();private final Set<UUID> deflected=new HashSet<>(),dashHits=new HashSet<>();private final List<Trace> traces=new ArrayList<>();
 private static final class Trace{Vector3d p;long until;Trace(Vector3d p,long t){this.p=p;until=t;}}
 private int ticks,memory,blocked,strafeAt,waterUntil;private Vector3d aim=Vector3d.ZERO,destination,seen,dashAlign;private boolean hit,aligned;private double travelled;private long copiesUntil;
 public ElementHuntMob(EntityType<? extends ElementHuntMob> t,World w){super(t,w);xpReward=60;setNoGravity(serpent());}
 public boolean serpent(){return getType()==ModEntities.FIRE_SERPENT.get();}
 @Override public HuntTarget huntTarget(){return serpent()?HuntTarget.FIRE_SERPENT:HuntTarget.PODVEY;}
 @Override public boolean oven(){return false;}
 public static AttributeModifierMap.MutableAttribute attributes(boolean fire){AttributeModifierMap.MutableAttribute a=createMobAttributes().add(Attributes.MAX_HEALTH,fire?155:160).add(Attributes.ATTACK_DAMAGE,7).add(Attributes.MOVEMENT_SPEED,fire?.24:.29).add(Attributes.ARMOR,fire?5:4).add(Attributes.KNOCKBACK_RESISTANCE,fire?.35:.75).add(Attributes.FOLLOW_RANGE,fire?40:38);return fire?a.add(Attributes.FLYING_SPEED,.34):a;}
 @Override protected void defineSynchedData(){super.defineSynchedData();entityData.define(MOVE,0);entityData.define(SINCE,0);}
 public State move(){return State.values()[entityData.get(MOVE)];}public float moveElapsed(float partial){return Math.max(0,(int)level.getGameTime()-entityData.get(SINCE)+partial);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new SwimGoal(this));goalSelector.addGoal(1,new Fight());goalSelector.addGoal(5,new LookAtGoal(this,PlayerEntity.class,16));goalSelector.addGoal(6,new LookRandomlyGoal(this));targetSelector.addGoal(1,new HurtByTargetGoal(this));targetSelector.addGoal(2,new NearestAttackableTargetGoal<PlayerEntity>(this,PlayerEntity.class,true).setUnseenMemoryTicks(220));}
 @Override public boolean causeFallDamage(float distance,float multiplier){return false;}
 @Override public void travel(Vector3d input){if(!serpent()){super.travel(input);return;}if(isEffectiveAi()){move(MoverType.SELF,getDeltaMovement());setDeltaMovement(getDeltaMovement().scale(.88));}else super.travel(input);calculateEntityAnimation(this,false);}
 @Override protected void playStepSound(BlockPos p,net.minecraft.block.BlockState b){}
 @Override protected SoundEvent getAmbientSound(){return serpent()?SoundEvents.BLAZE_AMBIENT:SoundEvents.PHANTOM_FLAP;}
 @Override protected SoundEvent getHurtSound(DamageSource d){return serpent()?SoundEvents.BLAZE_HURT:SoundEvents.PHANTOM_HURT;}
 @Override protected SoundEvent getDeathSound(){return serpent()?SoundEvents.BLAZE_DEATH:SoundEvents.PHANTOM_DEATH;}
 @Override protected float getVoicePitch(){return serpent()?.7F:.6F;}
 @Override public boolean wetRain(){return level.isRainingAt(blockPosition());}
 private void cue(SoundEvent s){playSound(s,.8F,serpent()?.75F:.6F);}
 private void state(State s){entityData.set(MOVE,s.ordinal());entityData.set(SINCE,(int)level.getGameTime());ticks=0;navigation.stop();}
 private boolean available(State s){return level.getGameTime()>=ready.getOrDefault(s,0L);}
 private boolean valid(LivingEntity p){return p.isAlive()&&!(p instanceof PlayerEntity&&(((PlayerEntity)p).isCreative()||p.isSpectator()));}
 private List<PlayerEntity> players(double radius){return level.getEntitiesOfClass(PlayerEntity.class,getBoundingBox().inflate(radius),p->valid(p)&&distanceTo(p)<=radius&&getSensing().canSee(p));}
 private void ring(Vector3d p,double radius,boolean fire,int count){ServerWorld w=(ServerWorld)level;for(int i=0;i<count;i++){double a=i*Math.PI*2/count;w.sendParticles(fire?ParticleTypes.FLAME:ParticleTypes.CLOUD,p.x+Math.cos(a)*radius,p.y+.15,p.z+Math.sin(a)*radius,1,0,0,0,.01);}}
 private float fireDamage(float d){return ElementRules.fireDamage(d,wetRain());}
 private void fireHit(PlayerEntity p,float d,int burn,float push){if(p.hurt(new EntityDamageSource("fire_serpent",this).setIsFire(),fireDamage(d))){int duration=net.minecraft.enchantment.ProtectionEnchantment.getFireAfterDampener(p,ElementRules.ignite(burn,wetRain()));p.setRemainingFireTicks(Math.max(p.getRemainingFireTicks(),duration));}Vector3d v=p.position().subtract(position()).multiply(1,0,1).normalize();p.knockback(push,-v.x,-v.z);}
 private void begin(State s,LivingEntity t){state(s);ready.put(s,level.getGameTime()+(s==State.MELEE_SNAP&&!serpent()?20:ElementRules.cooldown(s,wetRain())));aim=t.position().add(t.getDeltaMovement().scale(8)).subtract(position()).normalize();destination=t.position();hit=false;dashHits.clear();travelled=0;aligned=false;
  if(s==State.ALIGN_DIVE)destination=t.position();if(s==State.ALIGN_DASH){destination=t.position().add(t.getDeltaMovement().scale(8)).add(0,.6,0);aim=destination.subtract(position()).normalize();dashAlign=position().subtract(aim.scale(2.5)).add(0,.3,0);}if(s==State.BROKEN_PATH_CHARGE)aim=t.position().subtract(position()).multiply(1,0,1).normalize();
  cue(s==State.ALIGN_DIVE?SoundEvents.FIREWORK_ROCKET_LAUNCH:s==State.ALIGN_DASH?SoundEvents.FIRE_AMBIENT:s==State.DECOY_CAST?SoundEvents.BLAZE_SHOOT:s==State.MELEE_SNAP?SoundEvents.PHANTOM_BITE:s==State.VORTEX_CHARGE?SoundEvents.ELYTRA_FLYING:s==State.GUST_CHARGE?SoundEvents.PLAYER_ATTACK_SWEEP:SoundEvents.PHANTOM_FLAP);
 }
 private void recover(int duration){state(State.RECOVERY);waterUntil=duration;setDeltaMovement(getDeltaMovement().scale(.2));cue(serpent()?SoundEvents.FIRE_AMBIENT:SoundEvents.PHANTOM_FLAP);}
 private double ground(Vector3d p){BlockPos q=new BlockPos(p);return level.hasChunkAt(q)?level.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,q).getY():getY();}
 private void fly(Vector3d point,double speed){Vector3d delta=point.subtract(position());if(delta.lengthSqr()<.16){setDeltaMovement(getDeltaMovement().scale(.5));return;}Vector3d step=delta.normalize().scale(speed);BlockPos next=new BlockPos(position().add(step));if(!level.hasChunkAt(next)||!level.noCollision(this,getBoundingBox().move(step))){blocked++;setDeltaMovement(Vector3d.ZERO);if(blocked%10==0){double turn=(blocked%20==0?1:-1);Vector3d side=new Vector3d(-step.z*turn,.12,step.x*turn);if(level.hasChunkAt(new BlockPos(position().add(side)))&&level.noCollision(this,getBoundingBox().move(side)))setDeltaMovement(side);}return;}blocked=Math.max(0,blocked-1);setDeltaMovement(step);hasImpulse=true;}
 @Override public void tick(){super.tick();if(level.isClientSide)return;setNoGravity(serpent());if(!isAlive()){if(serpent())setDeltaMovement(getDeltaMovement().add(0,-.04,0));return;}
  if(serpent()&&isInWater()){if(tickCount%20==0)hurt(DamageSource.DROWN,4);if(move()!=State.WATER_STUN){state(State.WATER_STUN);waterUntil=50;ready.put(State.ALIGN_DASH,level.getGameTime()+130);cue(SoundEvents.FIRE_EXTINGUISH);}}
  if(tickCount%5==0){long now=level.getGameTime();traces.removeIf(t->t.until<=now);hits.entrySet().removeIf(e->e.getValue()+40<now);for(Trace tr:traces){((ServerWorld)level).sendParticles(ParticleTypes.FLAME,tr.p.x,tr.p.y,tr.p.z,1,.2,.1,.2,.01);for(PlayerEntity p:level.getEntitiesOfClass(PlayerEntity.class,new AxisAlignedBB(tr.p.x-.7,tr.p.y-.6,tr.p.z-.7,tr.p.x+.7,tr.p.y+.7,tr.p.z+.7),this::valid))if(now>=hits.getOrDefault(p.getUUID(),0L)&&clearLine(tr.p,p.getEyePosition(1))){p.hurt(new EntityDamageSource("serpent_trail",this).setIsFire(),fireDamage(2));hits.put(p.getUUID(),now+20);}}}
  if(!serpent()&&tickCount%(level.isThundering()?5:6)==0){ServerWorld w=(ServerWorld)level;w.sendParticles(ParticleTypes.CLOUD,getX(),getY()+.6,getZ(),2,.65,.5,.65,.03);IParticleData debris=level.getBiome(blockPosition()).getPrecipitation()==net.minecraft.world.biome.Biome.RainType.SNOW?ParticleTypes.ITEM_SNOWBALL:new BlockParticleData(ParticleTypes.BLOCK,level.getBlockState(blockPosition().below()));w.sendParticles(debris,getX(),getY()+1,getZ(),1,.5,.6,.5,.02);}
 }
 private void vortex(){if(ticks%4==0)ring(position(),7,false,18);for(PlayerEntity p:players(7)){double d=distanceTo(p);Vector3d pull=position().subtract(p.position()).multiply(1,0,1).normalize().scale(ElementRules.pull(d));Vector3d v=p.getDeltaMovement().add(pull);double h=Math.sqrt(v.x*v.x+v.z*v.z);if(h>.45)v=new Vector3d(v.x*.45/h,v.y,v.z*.45/h);p.setDeltaMovement(v);p.hurtMarked=true;
   if(d<1.5&&level.getGameTime()>=hits.getOrDefault(p.getUUID(),0L)){p.hurt(DamageSource.mobAttack(this),6);p.setDeltaMovement(p.position().subtract(position()).multiply(1,0,1).normalize().scale(.65).add(0,.5,0));p.hurtMarked=true;hits.put(p.getUUID(),level.getGameTime()+30);}}
  if(ticks%2==0)for(ProjectileEntity e:level.getEntitiesOfClass(ProjectileEntity.class,getBoundingBox().inflate(5),e->e.getOwner()!=this&&(e instanceof AbstractArrowEntity||e instanceof SnowballEntity||e instanceof EggEntity)&&distanceTo(e)<5))if(!deflected.contains(e.getUUID())&&clearLine(position().add(0,1,0),e.position())){Vector3d radial=e.position().subtract(position()).multiply(1,0,1).normalize(),v=e.getDeltaMovement();if(v.dot(radial)>=0)continue;Vector3d side=new Vector3d(-radial.z,v.y*.25,radial.x).normalize().scale(Math.max(.35,v.length()*.85));e.setDeltaMovement(side);e.hasImpulse=true;deflected.add(e.getUUID());}
 }
 private void projections(){if(copiesUntil>level.getGameTime())return;copiesUntil=level.getGameTime()+70;for(int i=0;i<2;i++){SerpentProjection e=ModEntities.SERPENT_PROJECTION.get().create(level);Vector3d offset=new Vector3d(i==0?-2:2,0,1);if(e!=null&&level.hasChunkAt(new BlockPos(position().add(offset)))&&level.noCollision(e,getBoundingBox().move(offset))){e.parent=getUUID();e.until=copiesUntil;e.drift=new Vector3d(i==0?-.08:.08,.025,.04);e.moveTo(getX()+offset.x,getY(),getZ()+1,yRot,0);level.addFreshEntity(e);}}particles(ParticleTypes.FLAME,10);}
 private final class Fight extends Goal {
  Fight(){setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
  @Override public boolean canUse(){return isAlive();}
  @Override public void tick(){ticks++;LivingEntity t=getTarget();if(t==null&&owner!=null&&tickCount%20==0){PlayerEntity ownerPlayer=level.getPlayerByUUID(owner);if(ownerPlayer!=null&&valid(ownerPlayer)&&distanceTo(ownerPlayer)<80){setTarget(ownerPlayer);t=ownerPlayer;}}if(t!=null&&!valid(t)){setTarget(null);t=null;}if(t!=null&&getSensing().canSee(t)){memory=220;seen=t.position();}else if(t!=null&&!serpent()&&--memory<=0){setTarget(null);t=null;}
   Vector3d anchor=home==null?position():new Vector3d(home.getX()+.5,home.getY(),home.getZ()+.5);if(home!=null&&(position().distanceToSqr(anchor)>72*72||t!=null&&t.position().distanceToSqr(anchor)>80*80||blocked>80)){state(State.RETURN_TO_ANCHOR);blocked=0;}
   if(move()==State.WATER_STUN){navigation.stop();if(serpent())fly(position().add(0,.7,0),.08);if(!isInWater()&&ticks>=waterUntil)recover(20);return;}
   if(move()==State.RETURN_TO_ANCHOR){if(serpent())fly(anchor,.24);else if(tickCount%10==0)navigation.moveTo(anchor.x,anchor.y,anchor.z,1);if(position().distanceToSqr(anchor)<16)state(serpent()?State.ORBIT:State.HUNT);return;}
   if(move()==State.RECOVERY){if(serpent())fly(position().add(0,(ground(position())+1.3-getY())*.5,0),.34*.75);if(ticks>=waterUntil)state(serpent()?State.ORBIT:State.HUNT);return;}
   if(t==null){if(serpent())fly(anchor.add(0,home==null?0:3,0),.2);else if(tickCount%30==0&&position().distanceToSqr(anchor)>16)navigation.moveTo(anchor.x,anchor.y,anchor.z,.7);return;}
   lookControl.setLookAt(t,30,30);double d=distanceTo(t);boolean sight=getSensing().canSee(t);State s=move();
   if(s==State.ALIGN_DASH){if(!aligned){fly(dashAlign,.34);if(position().distanceToSqr(dashAlign)<.5||ticks>=30){aligned=true;aim=destination.subtract(position()).normalize();entityData.set(SINCE,(int)level.getGameTime());ticks=0;}return;}setDeltaMovement(Vector3d.ZERO);if(ticks%4==0)particles(ParticleTypes.FLAME,2);if(ticks>=16){state(State.FIRE_DASH);cue(SoundEvents.FIREWORK_ROCKET_LAUNCH);}return;}
   if(s==State.FIRE_DASH){fly(position().add(aim.scale(2)),.34*1.8);if(ticks%3==0&&traces.size()<14)traces.add(new Trace(position(),level.getGameTime()+40));for(PlayerEntity p:players(2.2))if(!dashHits.contains(p.getUUID())&&getBoundingBox().inflate(.4).intersects(p.getBoundingBox())){fireHit(p,8,60,.7F);dashHits.add(p.getUUID());}if(ticks>=18||horizontalCollision)recover(18);return;}
   if(s==State.ALIGN_DIVE){Vector3d high=destination.add(0,8,0);if(!aligned){fly(high,.45);if(position().distanceToSqr(high)<2||ticks>=30){aligned=true;entityData.set(SINCE,(int)level.getGameTime());ticks=0;}return;}setDeltaMovement(Vector3d.ZERO);if(ticks%4==0)ring(destination,3,true,18);if(ticks>=24){state(State.STAR_DIVE);aim=destination.subtract(position()).normalize();cue(SoundEvents.FIREWORK_ROCKET_LAUNCH);}return;}
   if(s==State.STAR_DIVE){fly(destination,.85);if(position().distanceToSqr(destination)<2||verticalCollision||horizontalCollision||ticks>=24){if(position().distanceToSqr(destination)<12&&clearLine(position().add(0,.3,0),destination.add(0,.4,0))){ring(destination,3.25,true,22);cue(SoundEvents.GENERIC_EXPLODE);for(PlayerEntity p:players(5))if(p.position().distanceToSqr(destination)<=3.25*3.25){fireHit(p,10,40,.8F);p.setDeltaMovement(p.getDeltaMovement().add(0,.2,0));p.hurtMarked=true;}}recover(30);}return;}
   if(s==State.DECOY_CAST){setDeltaMovement(getDeltaMovement().scale(.6));if(ticks==18)projections();if(ticks>=24)recover(12);return;}
   if(s==State.MELEE_SNAP){navigation.stop();if(ticks==(serpent()?7:8)&&d<=(serpent()?2.4:2.5)&&sight)t.hurt(DamageSource.mobAttack(ElementHuntMob.this),7);if(ticks>=12)recover(6);return;}
   if(s==State.VORTEX_CHARGE){navigation.stop();if(ticks%4==0)ring(position(),2+ticks*.2,false,16);if(ticks>=20){state(State.VORTEX_ACTIVE);deflected.clear();cue(SoundEvents.ELYTRA_FLYING);}return;}
   if(s==State.VORTEX_ACTIVE){navigation.stop();if(isInWater()){recover(24);return;}vortex();if(ticks>=60){deflected.clear();recover(24);}return;}
   if(s==State.GUST_CHARGE){navigation.stop();if(ticks%4==0)particles(ParticleTypes.CLOUD,3);if(ticks>=14){state(State.GUST_RELEASE);cue(SoundEvents.PLAYER_ATTACK_SWEEP);for(PlayerEntity p:players(8)){Vector3d v=p.position().subtract(position()).multiply(1,0,1).normalize();if(aim.multiply(1,0,1).normalize().dot(v)<.843)continue;boolean shield=p.isBlocking()&&p.getLookAngle().dot(position().subtract(p.position()).normalize())>.4;p.hurt(DamageSource.mobAttack(ElementHuntMob.this),4);p.setDeltaMovement(p.getDeltaMovement().add(v.scale(shield?1.25*.55:1.25)).add(0,.15,0));p.hurtMarked=true;}for(int i=1;i<=8;i++)ring(position().add(aim.scale(i)),.35,false,3);}return;}
   if(s==State.GUST_RELEASE){if(ticks>=8)recover(12);return;}
   if(s==State.BROKEN_PATH_CHARGE){navigation.stop();if(ticks>=10){state(State.BROKEN_PATH_DASH);cue(SoundEvents.PHANTOM_FLAP);}return;}
   if(s==State.BROKEN_PATH_DASH){navigation.stop();Vector3d step=aim.scale(.55);if(level.hasChunkAt(new BlockPos(position().add(step)))&&level.noCollision(ElementHuntMob.this,getBoundingBox().move(step))){setDeltaMovement(new Vector3d(step.x,getDeltaMovement().y,step.z));hasImpulse=true;travelled+=.55;}else travelled=7;
    if(!hit&&d<2.1&&sight){t.hurt(DamageSource.mobAttack(ElementHuntMob.this),6);t.addEffect(new EffectInstance(Effects.WEAKNESS,60));t.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,40));t.knockback(.3F,-aim.z,aim.x);hit=true;}if(travelled>=7||ticks>=14)recover(16);return;}
   if(isInWater()&&!serpent()){if(tickCount%10==0)navigation.moveTo(getX()+random.nextInt(7)-3,getY()+1,getZ()+random.nextInt(7)-3,1.2);return;}
   if(sight){if(d<=(serpent()?2.4:2.5)&&available(State.MELEE_SNAP)){begin(State.MELEE_SNAP,t);return;}
    if(serpent()){if(available(State.DECOY_CAST)&&copiesUntil<=level.getGameTime()&&tickCount%3==0){begin(State.DECOY_CAST,t);return;}if(d>=7&&d<=16&&available(State.ALIGN_DASH)){begin(State.ALIGN_DASH,t);return;}if(d>4&&available(State.ALIGN_DIVE)){begin(State.ALIGN_DIVE,t);return;}}
    else {if(d>=4&&d<=10&&available(State.BROKEN_PATH_CHARGE)){begin(State.BROKEN_PATH_CHARGE,t);return;}if(d<=7&&available(State.VORTEX_CHARGE)){begin(State.VORTEX_CHARGE,t);return;}if(d<=8&&available(State.GUST_CHARGE)){begin(State.GUST_CHARGE,t);return;}}
   }
   if(serpent()){if(!sight&&ticks>80&&available(State.GUST_CHARGE)){ready.put(State.GUST_CHARGE,level.getGameTime()+100);EmberClump e=ModEntities.EMBER_CLUMP.get().create(level);if(e!=null){e.setOwner(ElementHuntMob.this);e.setPos(getX(),getEyeY(),getZ());Vector3d v=(seen==null?t.position():seen).add(0,1,0).subtract(e.position());e.shoot(v.x,v.y,v.z,.8F,1);level.addFreshEntity(e);cue(SoundEvents.BLAZE_SHOOT);}}
    double angle=tickCount*.07+getId();double y=Math.min(ground(t.position())+12,Math.max(ground(t.position())+3,t.getY()+4+Math.sin(angle*.7)*2));Vector3d point=sight?t.position().add(Math.cos(angle)*7,y-t.getY(),Math.sin(angle)*7):(seen==null?anchor:seen).add(Math.cos(angle)*6,4,Math.sin(angle)*6);fly(point,.34);if(move()!=State.ORBIT)state(State.ORBIT);
   }else if(tickCount%10==0){if(tickCount>=strafeAt&&sight&&d<10){Vector3d toward=t.position().subtract(position()).multiply(1,0,1).normalize();double side=random.nextBoolean()?5:-5;destination=t.position().add(-toward.z*side,0,toward.x*side);strafeAt=tickCount+30+random.nextInt(31);state(State.STRAFE);}Vector3d point=move()==State.STRAFE&&destination!=null?destination:seen==null?t.position():seen;boolean ok=navigation.moveTo(point.x,point.y,point.z,1);if(!ok){blocked+=10;navigation.moveTo(getX()+random.nextInt(9)-4,getY(),getZ()+random.nextInt(9)-4,1);}else blocked=0;if(move()==State.STRAFE&&ticks>=30)state(State.HUNT);}
  }
 }
 @Override public void die(DamageSource d){boolean before=dead;super.die(d);if(!before&&dead&&!level.isClientSide){cue(serpent()?SoundEvents.FIRE_EXTINGUISH:SoundEvents.ELYTRA_FLYING);if(d.getEntity() instanceof PlayerEntity)org.slavicmyths.item.FolkAccessoryItem.award((LivingEntity)d.getEntity(),serpent()?"fire_in_the_sky":"catch_the_wind");}}
 @Override public void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);CompoundNBT cds=new CompoundNBT();for(Map.Entry<State,Long> e:ready.entrySet())cds.putLong(e.getKey().name(),e.getValue());n.put("ElementReady",cds);n.putLong("CopiesUntil",copiesUntil);}
 @Override public void readAdditionalSaveData(CompoundNBT n){super.readAdditionalSaveData(n);CompoundNBT cds=n.getCompound("ElementReady");for(State s:State.values())ready.put(s,cds.getLong(s.name()));copiesUntil=n.getLong("CopiesUntil");state(serpent()?State.ORBIT:State.HUNT);getAttribute(Attributes.MAX_HEALTH).setBaseValue(serpent()?155:160);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(7);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(serpent()?.24:.29);getAttribute(Attributes.ARMOR).setBaseValue(serpent()?5:4);getAttribute(Attributes.ARMOR_TOUGHNESS).setBaseValue(0);getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(serpent()?.35:.75);getAttribute(Attributes.FOLLOW_RANGE).setBaseValue(serpent()?40:38);}
}
