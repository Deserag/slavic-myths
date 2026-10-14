package org.slavicmyths.poloz;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.network.syncher.*;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.*;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import org.slavicmyths.entity.LandSpiritEntity;
import org.slavicmyths.registry.ModEntities;

/** One health owner with native NeoForge multipart collision, server path and bounded impact queues. */
public final class GreatPoloz extends PathfinderMob implements Enemy {
 private static final EntityDataAccessor<Integer> PHASE=SynchedEntityData.defineId(GreatPoloz.class,EntityDataSerializers.INT),ACTION=SynchedEntityData.defineId(GreatPoloz.class,EntityDataSerializers.INT),CLOCK=SynchedEntityData.defineId(GreatPoloz.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<CompoundTag> TRACK=SynchedEntityData.defineId(GreatPoloz.class,EntityDataSerializers.COMPOUND_TAG);
 private static final EntityDataAccessor<Long> PHASE_START=SynchedEntityData.defineId(GreatPoloz.class,EntityDataSerializers.LONG);
 public final PolozPart[] parts=new PolozPart[27];
 public final PolozWorldImpact impact=new PolozWorldImpact();
 private final ArrayDeque<Vec3> history=new ArrayDeque<>();
 private final Map<UUID,Long> hitTimes=new HashMap<>(),participants=new HashMap<>();
 private final Set<UUID> projectiles=new HashSet<>(),attackHits=new HashSet<>();
 public final Set<UUID> minions=new HashSet<>();
 private final int[] cooldowns=new int[6];private int attackAge,rest,phaseAge,noPlayers,debugTicks;
 private long ordinaryWindow;private float ordinaryDamage;
 private UUID rainVolley=UUID.randomUUID();public UUID rainVolley(){return rainVolley;}
 private UUID deferredTarget;private Vec3 aim=Vec3.ZERO,mark=Vec3.ZERO;private float heading;private boolean finished;private long trackReceived;
 public final ServerBossEvent bar=new ServerBossEvent(Component.translatable("entity.slavicmyths.great_poloz"),BossEvent.BossBarColor.RED,BossEvent.BossBarOverlay.PROGRESS);
 public GreatPoloz(EntityType<? extends GreatPoloz> type,Level level){super(type,level);setPersistenceRequired();noPhysics=true;noCulling=true;
  for(int i=0;i<24;i++)parts[i]=new PolozPart(this,i,false,PolozRules.width(i),i<20?3:1.4f);
  for(int i=0;i<3;i++)parts[24+i]=new PolozPart(this,new int[]{5,12,18}[i],true,i==1?1.2f:1,i==1?.3f:.9f);
  setId(ENTITY_COUNTER.getAndAdd(parts.length+1)+1);setHealth(3000);
 }
 public static AttributeSupplier.Builder attributes(){return Mob.createMobAttributes().add(Attributes.MAX_HEALTH,3000).add(Attributes.MOVEMENT_SPEED,.23).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,80).add(Attributes.ATTACK_DAMAGE,26);}
 @Override public float getMaxHealth(){return 3000;}
 @Override protected void registerGoals(){}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(PHASE,1);b.define(ACTION,0);b.define(CLOCK,0);b.define(TRACK,new CompoundTag());b.define(PHASE_START,0L);}
 @Override public boolean isMultipartEntity(){return true;}
 @Override public PolozPart[] getParts(){return parts;}
 @Override public void setId(int id){super.setId(id);if(parts!=null)for(int i=0;i<parts.length;i++)if(parts[i]!=null)parts[i].setId(id+i+1);}
 @Override public boolean isPickable(){return isAlive();}
 @Override public boolean isPushable(){return false;}
 @Override public boolean canDrownInFluidType(net.neoforged.neoforge.fluids.FluidType type){return false;}
 private boolean surfaceWater(){return isInWater()||level().getFluidState(blockPosition().below()).is(net.minecraft.tags.FluidTags.WATER);}
 private void shoreHeading(ServerLevel w){if(!surfaceWater())return;Vec3 nearest=null;double best=Double.POSITIVE_INFINITY;for(int r:new int[]{16,32})for(int i=0;i<8;i++){double angle=i*Math.PI/4;BlockPos q=blockPosition().offset((int)(Math.cos(angle)*r),0,(int)(Math.sin(angle)*r));if(!w.hasChunkAt(q))continue;q=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,q);if(!w.getFluidState(q.below()).isEmpty())continue;double d=position().distanceToSqr(Vec3.atBottomCenterOf(q));if(d<best){best=d;nearest=Vec3.atBottomCenterOf(q);}}if(nearest!=null)heading=Mth.rotateIfNecessary(heading,(float)(Math.atan2(nearest.z-getZ(),nearest.x-getX())*180/Math.PI)-90,15);}
 @Override public boolean removeWhenFarAway(double distance){return false;}
 public int phase(){return entityData.get(PHASE);}public int action(){return entityData.get(ACTION);}public float actionAge(float partial){return Math.max(0,level().getGameTime()-entityData.get(CLOCK)+partial);}
 public float reveal(float partial){return (float)Math.clamp((level().getGameTime()-entityData.get(PHASE_START)+partial)/60,0,1);}
 public double headLift(float partial){float age=actionAge(partial);if(action()==0&&surfaceWater())return 1.2+.2*Math.sin((tickCount+partial)*.06);if(action()==3)return 4*Math.min(1,age/40)*Math.max(0,Math.min(1,(95-age)/15));if(action()==1)return age<30?3*age/30:Math.max(0,3-(age-30)*.15);if(action()==6)return Math.max(0,2*Math.sin(Math.PI*Math.min(age,50)/50));return 0;}
 private void headBox(){double a=Math.toRadians(getYRot()),sx=Math.abs(Math.cos(a)),sz=Math.abs(Math.sin(a));Vec3 forward=Vec3.directionFromRotation(0,getYRot());Vec3 center=position().add(forward.scale(.5625)).add(0,headLift(0),0);double x=(3.5*sx+5.625*sz)/2,z=(3.5*sz+5.625*sx)/2;setBoundingBox(new AABB(center.x-x,center.y,center.z-z,center.x+x,center.y+3,center.z+z));}
 public double nearestDistance(Vec3 point){double d=getBoundingBox().distanceToSqr(point);for(var part:parts)d=Math.min(d,part.getBoundingBox().distanceToSqr(point));return d;}
 public void debugWeakpoints(){debugTicks=200;}
 public void setPhaseForTest(int phase){setHealth(phase==1?2700:phase==2?1500:600);entityData.set(PHASE,phase);entityData.set(PHASE_START,level().getGameTime());}
 public void initializeBody(){history.clear();Vec3 forward=Vec3.directionFromRotation(0,getYRot());for(int i=0;i<=240;i++)history.add(position().subtract(forward.scale(i*.2)));heading=getYRot();headBox();updateParts();syncTrack();}
 public float trackAlpha(float partial){return (float)Math.clamp((level().getGameTime()-trackReceived+partial)/4,0,1);}
 @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key.equals(TRACK)&&level().isClientSide&&parts!=null){trackReceived=level().getGameTime();var n=entityData.get(TRACK);for(int i=0;i<parts.length;i++)if(n.contains("x"+i)){var p=parts[i];boolean first=p.position().equals(Vec3.ZERO);p.xo=p.getX();p.yo=p.getY();p.zo=p.getZ();p.setPos(n.getDouble("x"+i),n.getDouble("y"+i),n.getDouble("z"+i));if(first){p.xo=p.getX();p.yo=p.getY();p.zo=p.getZ();}}}}
 private void syncTrack(){CompoundTag n=new CompoundTag();for(int i=0;i<parts.length;i++){var p=parts[i];n.putDouble("x"+i,p.getX());n.putDouble("y"+i,p.getY());n.putDouble("z"+i,p.getZ());}entityData.set(TRACK,n);}
 private void updateParts(){if(history.isEmpty())return;var points=new ArrayList<>(history);for(int i=0;i<24;i++){double remaining=PolozRules.offset(i);Vec3 at=points.getLast();for(int j=1;j<points.size();j++){double length=points.get(j-1).distanceTo(points.get(j));if(length>=remaining&&length>0){at=points.get(j-1).lerp(points.get(j),remaining/length);break;}remaining-=length;}var p=parts[i];p.setPos(at);Vec3 tangent=(i==0?position():parts[i-1].position()).subtract(at).multiply(1,0,1).normalize();double width=PolozRules.width(i),length=i<20?1.5:1;double x=(Math.abs(tangent.z)*width+Math.abs(tangent.x)*length)/2,z=(Math.abs(tangent.x)*width+Math.abs(tangent.z)*length)/2;double h=i<20?3:1.4;p.setBoundingBox(new AABB(at.x-x,at.y,at.z-z,at.x+x,at.y+h,at.z+z));}
  for(int i=0;i<3;i++){var scar=parts[24+i];var body=parts[scar.segment];Vec3 tangent=(scar.segment==0?position():parts[scar.segment-1].position()).subtract(body.position()).normalize();Vec3 side=new Vec3(-tangent.z,0,tangent.x).scale(i==0?-1:1);Vec3 at=body.position().add(i==1?Vec3.ZERO:side.scale(PolozRules.width(scar.segment)/2+.15)).add(0,i==1?2.85:1.4,0);scar.setPos(at);}
 }
 private boolean ally(LivingEntity entity){return entity==this||entity.getPersistentData().hasUUID("summonedByPolozUUID")&&getUUID().equals(entity.getPersistentData().getUUID("summonedByPolozUUID"));}
 @Override public boolean isAlliedTo(Entity e){return e instanceof LivingEntity living&&ally(living)||super.isAlliedTo(e);}
 public boolean rewardFissure(ServerPlayer p,float amount){if(!isAlive()||isAlliedTo(p)||nearestDistance(p.position())>36)return false;invulnerableTime=0;boolean hit=super.hurt(p.damageSources().playerAttack(p),amount);if(hit)updatePhase();return hit;}
 public boolean validVictim(LivingEntity e){return e.isAlive()&&!e.isSpectator()&&!e.isInvulnerable()&&!ally(e)&&!org.slavicmyths.combat.CreatureSafety.protectedPlayer(e)&&!(e instanceof net.minecraft.world.entity.decoration.ArmorStand);}
 public boolean hitPart(PolozPart part,DamageSource source,float amount){
  if(!java.util.Arrays.asList(parts).contains(part))return false;
  if(part.scar&&!scarContact(part,source))return false;
  return acceptHit(part.segment,part.scar,source,amount,part.position());
 }
 private boolean scarContact(PolozPart part,DamageSource source){
  if(source.is(net.minecraft.tags.DamageTypeTags.IS_EXPLOSION))return false;
  var direct=source.getDirectEntity();var box=part.getBoundingBox();
  if(direct instanceof net.minecraft.world.entity.projectile.Projectile projectile){Vec3 v=projectile.getDeltaMovement();return box.inflate(.15).contains(projectile.position())||box.inflate(.15).clip(projectile.position().subtract(v),projectile.position().add(v)).isPresent();}
  if(!(direct instanceof LivingEntity actor)||source.getEntity()!=direct)return false;
  Vec3 from=actor.getEyePosition();double reach=actor instanceof Player?actor.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE):3.5;
  Vec3 end=from.add(actor.getLookAngle().scale(reach));var contact=box.clip(from,end);
  if(contact.isEmpty()&&!(actor instanceof Player)&&box.inflate(.25).intersects(actor.getBoundingBox().inflate(.6)))contact=Optional.of(box.getCenter());
  return contact.isPresent()&&level().clip(new ClipContext(from,contact.get(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,actor)).getType()==HitResult.Type.MISS;
 }
 @Override public boolean hurt(DamageSource source,float amount){return acceptHit(-1,false,source,amount,position());}
 private boolean acceptHit(int segment,boolean scar,DamageSource source,float amount,Vec3 at){if(level().isClientSide||!isAlive()||!Float.isFinite(amount)||amount<=0||source.getEntity() instanceof LivingEntity living&&ally(living))return false;
  if(source.is(net.minecraft.tags.DamageTypeTags.IS_FIRE)||source.is(net.minecraft.tags.DamageTypeTags.IS_FALL)||source.getSourcePosition()==null)return false;
  float multiplier=PolozRules.multiplier(phase(),segment,scar);
  var w=(ServerLevel)level();Entity attacker=source.getEntity(),direct=source.getDirectEntity();UUID key=attacker!=null?attacker.getUUID():direct!=null?direct.getUUID():null;long now=w.getGameTime();
  if(key==null||now-hitTimes.getOrDefault(key,-100L)<6||direct instanceof net.minecraft.world.entity.projectile.Projectile&&projectiles.contains(direct.getUUID()))return false;
  hitTimes.put(key,now);if(direct instanceof net.minecraft.world.entity.projectile.Projectile)projectiles.add(direct.getUUID());
  if(multiplier==0){w.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,at.x,at.y+1,at.z,6,.5,.3,.5,0);playSound(PolozContent.sound("poloz_armor_deflect"),.7f,1);if(attacker instanceof ServerPlayer p&&!p.getPersistentData().getBoolean("PolozArmorHint")){p.getPersistentData().putBoolean("PolozArmorHint",true);p.displayClientMessage(Component.translatable("poloz.armor"),true);}return false;}
  boolean ordinary=attacker instanceof Mob mob&&!(mob instanceof org.slavicmyths.wolf.GreyWolf)&&!(mob instanceof org.slavicmyths.quest185.WolfSupport)&&!org.slavicmyths.bogatyr.HeroThreats.availableHero(mob)&&org.slavicmyths.kalin.KalinParty.owner(mob)==null;if(ordinary){if(now-ordinaryWindow>=20){ordinaryWindow=now;ordinaryDamage=0;}amount=Math.min(amount,Math.min(4,Math.max(0,12-ordinaryDamage)));if(amount<=0)return false;}
  invulnerableTime=0;boolean result=super.hurt(source,amount*multiplier);if(result){if(ordinary)ordinaryDamage+=amount;w.sendParticles(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,at.x,at.y+.4,at.z,5,.2,.2,.2,0);UUID credit=attacker instanceof ServerPlayer p?p.getUUID():attacker instanceof LivingEntity l?org.slavicmyths.kalin.KalinParty.owner(l):null;if(credit!=null){participants.put(credit,now);PolozEvent.get(w).participants.add(credit);PolozEvent.get(w).setDirty();}playSound(PolozContent.sound("poloz_weakspot_hit"),.7f,1);updatePhase();}return result;
 }
 private void updatePhase(){if(!isAlive())return;int next=PolozRules.phase(getHealth());if(next<=phase())return;entityData.set(PHASE,next);entityData.set(PHASE_START,level().getGameTime());phaseAge=0;playSound(PolozContent.sound(next==2?"poloz_phase2_cracks":"poloz_phase3_roar"),3,1);if(level() instanceof ServerLevel w)for(var p:w.players())if(nearestDistance(p.position())<256*256){p.displayClientMessage(Component.translatable(next==2?"poloz.phase2":"poloz.phase3"),true);org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:great_poloz",3,false);}}
 @Override public void aiStep(){if(level().isClientSide){super.aiStep();headBox();return;}var w=(ServerLevel)level();if(!isAlive())return;if(getTarget()!=null&&!validVictim(getTarget())){setTarget(null);entityData.set(ACTION,0);attackHits.clear();rest=20;}phaseAge++;
  if(w.players().isEmpty())noPlayers++;else noPlayers=0;if(noPlayers>=6000&&getTarget()==null){impact.clear();}
  for(int i=0;i<cooldowns.length;i++)if(cooldowns[i]>0)cooldowns[i]--;if(rest>0)rest--;
  if(tickCount%20==0){hitTimes.entrySet().removeIf(e->w.getGameTime()-e.getValue()>100);if(projectiles.size()>512)projectiles.clear();updateBar(w);pickTarget(w);minions.removeIf(id->w.getEntity(id) instanceof LivingEntity l&&!l.isAlive());}
  if(action()==0&&rest==0&&getTarget()!=null){int chosen=choose();if(chosen>0)begin(chosen);}
  double speed=(surfaceWater()?.18:.23)*(phase()==1?1:phase()==2?1.1:1.2);Vec3 direction;
  if(action()==1&&attackAge>=30&&attackAge<60||action()==2&&attackAge>=40&&attackAge<80){speed=.55;direction=aim;}else if(action()==5){speed=.55;double a=Math.atan2(aim.z,aim.x)+attackAge*.061;Vec3 ring=mark.add(Math.cos(a)*9,0,Math.sin(a)*9);direction=ring.subtract(position()).multiply(1,0,1).normalize();}else {var target=getTarget();if(target!=null){float wanted=(float)(Mth.atan2(target.getZ()-getZ(),target.getX()-getX())*180/Math.PI)-90;heading=Mth.rotateIfNecessary(heading,wanted,15);if(tickCount%20==0)shoreHeading(w);}else if(tickCount%160==0)heading+=random.nextInt(61)-30;direction=Vec3.directionFromRotation(0,heading+(float)Math.sin(tickCount*.055)*5);}
  if(action()!=0&&action()!=5&&attackAge<windup(action()))speed=0;
  Vec3 dest=position().add(direction.scale(speed));var pos=BlockPos.containing(dest);if(w.hasChunkAt(pos)&&w.getWorldBorder().isWithinBounds(pos)){int y=w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,pos.getX(),pos.getZ());if(action()==2&&!surfaceWater()&&attackAge>=40&&attackAge<100)y-=3;double dy=Math.clamp(y-getY(),-.4,.4);setPos(dest.x,getY()+dy,dest.z);setYRot(heading);}
  if(history.isEmpty())initializeBody();if(history.peekFirst().distanceToSqr(position())>.0025){history.addFirst(position().add(0,surfaceWater()?-1.5:0,0));while(history.size()>600)history.removeLast();}
  headBox();if(tickCount%4==0){updateParts();syncTrack();if(action()==2&&attackAge>=40&&attackAge<100)impact.patch(w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,blockPosition()).below());else impact.sample(this);}impact.tick(this);
  if(action()!=0)attack(w);
  if(tickCount%300==0)playSound(PolozContent.sound("poloz_ambient_rumble"),2,1);
  if(debugTicks>0){debugTicks--;if(tickCount%4==0)for(var part:parts)if(PolozRules.multiplier(phase(),part.segment,part.scar)>0)w.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,part.getX(),part.getY()+.5,part.getZ(),2,.2,.2,.2,0);}
 }
 private void updateBar(ServerLevel w){bar.setProgress(Math.clamp(getHealth()/3000f,0,1));for(var p:new ArrayList<>(bar.getPlayers()))if(!p.isAlive()||p.serverLevel()!=w||nearestDistance(p.position())>256*256)bar.removePlayer(p);for(var p:w.players())if(p.isAlive()&&nearestDistance(p.position())<=256*256)bar.addPlayer(p);}
 private void pickTarget(ServerLevel w){deferredTarget=null;AABB area=getBoundingBox();for(var part:parts)area=area.minmax(part.getBoundingBox());
  setTarget(w.getEntitiesOfClass(LivingEntity.class,area.inflate(48),e->validVictim(e)&&nearestDistance(e.position())<=48*48&&hasLineOfSight(e)).stream().min(Comparator.comparingDouble(e->nearestDistance(e.position()))).orElse(null));
 }
 private int choose(){for(int k=0;k<6;k++){int skill=1+random.nextInt(phase()==1?4:6);if(cooldowns[skill-1]>0)continue;if(skill==1&&getBoundingBox().distanceToSqr(getTarget().position())>36)continue;if(skill==4&&parts[23].getBoundingBox().distanceToSqr(getTarget().position())>81)continue;if(skill==5&&(!ringSpace()||getTarget().distanceTo(this)>20))continue;if(skill==6&&minions.size()+(phase()==3?6:4)>8)continue;return skill;}return 0;}
 private int windup(int skill){return switch(skill){case 1->30;case 2,3,5->40;default->24;};}
 private boolean ringSpace(){if(getTarget()==null||isInWater())return false;Vec3 center=getTarget().position();for(int i=0;i<16;i++){double a=i*Math.PI/8;BlockPos p=BlockPos.containing(center.add(Math.cos(a)*9,0,Math.sin(a)*9));if(!level().hasChunkAt(p)||!level().getFluidState(p).isEmpty()||Math.abs(level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.getX(),p.getZ())-center.y)>2)return false;for(int y=0;y<4;y++)if(!level().getBlockState(p.above(y)).getCollisionShape(level(),p.above(y)).isEmpty())return false;}return true;}
 private void begin(int skill){entityData.set(ACTION,skill);entityData.set(CLOCK,(int)level().getGameTime());attackAge=0;attackHits.clear();if(skill==3)rainVolley=UUID.randomUUID();var target=getTarget();mark=target==null?position():target.position();aim=mark.subtract(position()).multiply(1,0,1).normalize();cooldowns[skill-1]=PolozRules.cooldown(skill,phase());playSound(PolozContent.sound(switch(skill){case 2->"poloz_burrow";case 3->"poloz_mor_rain";case 4->"poloz_tail_slam";case 6->"poloz_minion_call";default->"poloz_surface_roar";}),2,1);}
 private void attack(ServerLevel w){int skill=action();attackAge++;int wind=windup(skill);if(attackAge<wind&&tickCount%4==0)w.sendParticles(org.slavicmyths.mor.MorContent.SPARK.get(),getX(),getY()+1,getZ(),12,2,.4,2,.02);
  if(skill==1&&attackAge>=30&&attackAge<60)strike(getBoundingBox().inflate(1),PolozRules.damage(1),2.5,false);
  if(skill==2&&attackAge>=76&&attackAge<100&&attackAge%4==0){for(int i=0;i<20;i++){double a=i*Math.PI/10;w.sendParticles(org.slavicmyths.mor.MorContent.SPARK.get(),getX()+Math.cos(a)*4,w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(int)getX(),(int)getZ())+.1,getZ()+Math.sin(a)*4,1,0,0,0,0);}}if(skill==2&&attackAge==100){setPos(getX(),w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(int)getX(),(int)getZ()),getZ());playSound(PolozContent.sound("poloz_emerge"),3,1);strike(getBoundingBox().inflate(4),PolozRules.damage(2),2.5,true);}
  if(skill==3&&attackAge>=40&&attackAge<80&&(attackAge-40)%5==0){var glob=PolozContent.GLOB.get().create(w);if(glob!=null){glob.configure(this,aim.yRot((random.nextFloat()-.5f)*.96f).scale(.6+random.nextFloat()*.3).add(0,.45,0));w.addFreshEntity(glob);}}
  if(skill==4&&attackAge==24)tailSweep(w);
  if(skill==5&&attackAge>=80&&attackAge<140&&attackAge%20==0){attackHits.clear();for(var victim:w.getEntitiesOfClass(LivingEntity.class,new AABB(mark.add(-7,-1,-7),mark.add(7,4,7)),v->v.isAlive()&&!ally(v)&&v.position().multiply(1,0,1).distanceToSqr(mark.multiply(1,0,1))<=49))if(!(victim instanceof Player p)||!p.isCreative()&&!p.isSpectator()){victim.hurt(damageSources().mobAttack(this),PolozRules.damage(5)/3);Vec3 push=mark.subtract(victim.position()).multiply(1,0,1).normalize().scale(.15);victim.push(push.x,.1,push.z);}}
  if(skill==6&&attackAge==24)summon(w);
  int end=skill==2?110:skill==5?150:skill==3?95:skill==1?75:50;
  if(attackAge>=end){entityData.set(ACTION,0);rest=phase()==1?100:phase()==2?80:60;attackHits.clear();}
 }
 private void strike(AABB box,float damage,double push,boolean up){for(var e:level().getEntitiesOfClass(LivingEntity.class,box,l->l.isAlive()&&!ally(l)&&(!(l instanceof Player p)||!p.isCreative()&&!p.isSpectator())))if(attackHits.add(e.getUUID())){if(e.hurt(damageSources().mobAttack(this),damage)){Vec3 dir=e.position().subtract(position()).multiply(1,0,1).normalize().scale(push*.2);e.push(dir.x,up?.9:.2,dir.z);}}}
 private void tailSweep(ServerLevel w){Vec3 center=parts[23].position(),forward=parts[22].position().subtract(center).multiply(1,0,1).normalize();for(var victim:w.getEntitiesOfClass(LivingEntity.class,parts[23].getBoundingBox().inflate(9,2,9),v->v.isAlive()&&!ally(v)&&(!(v instanceof Player p)||!p.isCreative()&&!p.isSpectator()))){Vec3 relative=victim.position().subtract(center).multiply(1,0,1);if(relative.lengthSqr()<=81&&(relative.lengthSqr()<.01||relative.normalize().dot(forward)>=Math.cos(Math.toRadians(70)))&&attackHits.add(victim.getUUID())){victim.hurt(damageSources().mobAttack(this),PolozRules.damage(4));Vec3 push=relative.normalize().scale(.8);victim.push(push.x,.2,push.z);}}Vec3 wave=parts[19].position();for(var victim:w.getEntitiesOfClass(LivingEntity.class,parts[19].getBoundingBox().inflate(6,1,6),v->v.isAlive()&&!ally(v)&&(!(v instanceof Player p)||!p.isCreative()&&!p.isSpectator())))if(victim.position().multiply(1,0,1).distanceToSqr(wave.multiply(1,0,1))<=36&&attackHits.add(victim.getUUID()))victim.hurt(damageSources().mobAttack(this),PolozRules.damage(4)/2);}
 private void summon(ServerLevel w){int count=phase()==3?6:4;for(int i=0;i<count&&minions.size()<8;i++){EntityType<? extends LandSpiritEntity> type=i<2?ModEntities.KIKIMORA.get():i==4?ModEntities.LESHY.get():ModEntities.POLUDNITSA.get();var m=type.create(w);if(m==null)continue;double a=random.nextDouble()*Math.PI*2;BlockPos p=blockPosition().offset((int)(Math.cos(a)*9),0,(int)(Math.sin(a)*9));if(!w.hasChunkAt(p))continue;p=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p);m.moveTo(Vec3.atBottomCenterOf(p));m.getPersistentData().putUUID("summonedByPolozUUID",getUUID());if(m.mor.apply(true)&&w.noCollision(m)&&w.addFreshEntity(m))minions.add(m.getUUID());}}
 public boolean rainDamage(LivingEntity target,float amount,UUID volley){if(!validVictim(target)||volley==null)return false;var n=target.getPersistentData();long now=level().getGameTime();if(!n.hasUUID("PolozRainOwner")||!volley.equals(n.getUUID("PolozRainOwner"))||now>n.getLong("PolozRainUntil")){n.putUUID("PolozRainOwner",volley);n.putLong("PolozRainUntil",now+100);n.putFloat("PolozRainDamage",0);}float allowed=Math.max(0,Math.min(amount,PolozRules.damage(3)-n.getFloat("PolozRainDamage")));if(allowed<=0)return false;n.putFloat("PolozRainDamage",n.getFloat("PolozRainDamage")+allowed);return target.hurt(damageSources().mobAttack(this),allowed);}
 public void cancel(){cleanup(false);discard();}
 private void cleanup(boolean defeated){if(finished)return;finished=true;impact.clear();bar.removeAllPlayers();PolozEvent.get((ServerLevel)level()).finish((ServerLevel)level(),defeated);for(UUID id:minions)if(((ServerLevel)level()).getEntity(id) instanceof LivingEntity m)m.getPersistentData().putLong("PolozRemoveAt",level().getGameTime()+100);}
 @Override public void die(DamageSource source){if(!level().isClientSide&&!finished){var w=(ServerLevel)level();for(var e:participants.entrySet()){var p=w.getServer().getPlayerList().getPlayer(e.getKey());if(p!=null&&p.serverLevel()==w&&w.getGameTime()-e.getValue()<=2400&&nearestDistance(p.position())<=256*256){org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:great_poloz",4,true);org.slavicmyths.progression.Knowledge.award(p,"great_poloz_fall");org.slavicmyths.quest185.Quest185Story.data(p).putBoolean("Poloz",true);org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:poloz_rewards",4,true);}}playSound(PolozContent.sound("poloz_death"),3,1);if(w.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)){spawnAtLocation(PolozContent.CORE.get());spawnAtLocation(PolozRewards.WHIP.get());spawnAtLocation(PolozTrophy.ITEM.get());if(random.nextInt(100)<5)spawnAtLocation(PolozRewards.RUNE.get());spawnAtLocation(new net.minecraft.world.item.ItemStack(PolozContent.SCALE.get(),8+random.nextInt(5)));spawnAtLocation(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.GOLD_INGOT,16+random.nextInt(9)));ExperienceOrb.award(w,position(),1500);}cleanup(true);}super.die(source);}
 @Override public void remove(RemovalReason reason){bar.removeAllPlayers();impact.clear();super.remove(reason);}
 @Override protected void tickDeath(){deathTime++;if(deathTime>=100&&!level().isClientSide)remove(RemovalReason.KILLED);}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);UUID focus=getTarget()!=null?getTarget().getUUID():deferredTarget;if(focus!=null)n.putUUID("PolozTarget",focus);n.putInt("PolozPhase",phase());n.putInt("PolozAction",action());n.putInt("PolozAttackAge",attackAge);n.putInt("PolozRest",rest);n.putIntArray("PolozCooldowns",cooldowns);n.putFloat("PolozHeading",heading);n.putLong("PolozPhaseStart",entityData.get(PHASE_START));n.putBoolean("PolozFinished",finished);var list=new ListTag();for(var at:history){var t=new CompoundTag();t.putDouble("X",at.x);t.putDouble("Y",at.y);t.putDouble("Z",at.z);list.add(t);}n.put("PolozHistory",list);var ms=new ListTag();for(UUID id:minions)ms.add(StringTag.valueOf(id.toString()));n.put("PolozMinions",ms);var ps=new CompoundTag();participants.forEach((id,t)->ps.putLong(id.toString(),t));n.put("PolozParticipants",ps);n.putDouble("AimX",aim.x);n.putDouble("AimZ",aim.z);n.putDouble("MarkX",mark.x);n.putDouble("MarkY",mark.y);n.putDouble("MarkZ",mark.z);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);deferredTarget=n.hasUUID("PolozTarget")?n.getUUID("PolozTarget"):null;entityData.set(PHASE,Math.max(Math.clamp(n.getInt("PolozPhase"),1,3),PolozRules.phase(getHealth())));entityData.set(ACTION,Math.clamp(n.getInt("PolozAction"),0,6));attackAge=Math.clamp(n.getInt("PolozAttackAge"),0,150);entityData.set(CLOCK,(int)level().getGameTime()-attackAge);rest=Math.clamp(n.getInt("PolozRest"),0,1100);int[] cds=n.getIntArray("PolozCooldowns");for(int i=0;i<Math.min(6,cds.length);i++)cooldowns[i]=Math.clamp(cds[i],0,1100);heading=n.getFloat("PolozHeading");entityData.set(PHASE_START,n.getLong("PolozPhaseStart"));finished=n.getBoolean("PolozFinished");history.clear();for(var tag:n.getList("PolozHistory",Tag.TAG_COMPOUND)){if(history.size()>=600)break;var t=(CompoundTag)tag;Vec3 v=new Vec3(t.getDouble("X"),t.getDouble("Y"),t.getDouble("Z"));if(Double.isFinite(v.x)&&Double.isFinite(v.y)&&Double.isFinite(v.z))history.add(v);}minions.clear();for(var tag:n.getList("PolozMinions",Tag.TAG_STRING))if(minions.size()<8)try{minions.add(UUID.fromString(tag.getAsString()));}catch(IllegalArgumentException ignored){}participants.clear();var ps=n.getCompound("PolozParticipants");for(String id:ps.getAllKeys())try{participants.put(UUID.fromString(id),ps.getLong(id));}catch(IllegalArgumentException ignored){}aim=new Vec3(n.getDouble("AimX"),0,n.getDouble("AimZ")).normalize();mark=new Vec3(n.getDouble("MarkX"),n.getDouble("MarkY"),n.getDouble("MarkZ"));updateParts();syncTrack();}
}
