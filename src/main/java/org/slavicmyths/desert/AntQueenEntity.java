package org.slavicmyths.desert;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.entity.PartEntity;
import org.slavicmyths.combat.*;
import static org.slavicmyths.desert.AntRules.*;
public final class AntQueenEntity extends AntEntity implements LivingThreatBoss {
 private final ServerBossEvent bar=new CreatureBossBar(this,Component.translatableWithFallback("entity.slavicmyths.ant_queen","Королева муравьёв"),BossEvent.BossBarColor.BLUE,BossEvent.BossBarOverlay.PROGRESS);
 private final Part[] parts=new Part[AntGeometry.GROUPS.length];private final int[] cd=new int[15];
 private float lockedYaw;private UUID lockedTarget;private boolean fired;private int recovery;private Vec3 home;
 public AntQueenEntity(EntityType<? extends AntQueenEntity> t,Level w){super(t,w);xpReward=150;setPersistenceRequired();for(int i=0;i<parts.length;i++)parts[i]=new Part(this,i);}
 public static AttributeSupplier.Builder attributes(){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,QUEEN_HP).add(Attributes.ATTACK_DAMAGE,20).add(Attributes.ARMOR,12).add(Attributes.ARMOR_TOUGHNESS,2).add(Attributes.KNOCKBACK_RESISTANCE,1).add(Attributes.FOLLOW_RANGE,48).add(Attributes.MOVEMENT_SPEED,.20);}
 @Override public int kind(){return QUEEN;}
 public int battlePhase(){return phase(getHealth()/getMaxHealth());}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public boolean isMultipartEntity(){return true;}
 @Override public PartEntity<?>[] getParts(){return parts;}
 @Override public boolean isPickable(){return false;}
 @Override public void setId(int id){super.setId(id);if(parts!=null)for(int i=0;i<parts.length;i++)if(parts[i]!=null)parts[i].setId(id+i+1);}
 @Override public void startSeenByPlayer(ServerPlayer p){super.startSeenByPlayer(p);bar.addPlayer(p);}
 @Override public void stopSeenByPlayer(ServerPlayer p){super.stopSeenByPlayer(p);bar.removePlayer(p);}
 public static final class Part extends PartEntity<AntQueenEntity>{
  final int index;Part(AntQueenEntity q,int i){super(q);index=i;noPhysics=true;refreshDimensions();}
  @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder b){}
  @Override protected void addAdditionalSaveData(CompoundTag n){}
  @Override protected void readAdditionalSaveData(CompoundTag n){}
  @Override public EntityDimensions getDimensions(Pose p){return EntityDimensions.scalable(1,1);}
  @Override public boolean shouldBeSaved(){return false;}
  @Override public boolean isPickable(){return getParent().isAlive();}
  @Override public boolean is(Entity e){return e==this||e==getParent();}
  @Override public boolean hurt(DamageSource s,float a){return getParent().hurt(s,a);}
 }
 private Vec3 world(double x,double y,double z){double a=Math.toRadians(getYRot());return position().add(x*Math.cos(a)-z*Math.sin(a),y,x*Math.sin(a)+z*Math.cos(a));}
 private void updateParts(){if(parts==null)return;double[][] lo=new double[parts.length][3],hi=new double[parts.length][3];for(int i=0;i<parts.length;i++){Arrays.fill(lo[i],Double.POSITIVE_INFINITY);Arrays.fill(hi[i],Double.NEGATIVE_INFINITY);}
  for(var b:pose(0)){int i=b.group();float[] v=b.vertices();for(int k=0;k<v.length;k+=3){Vec3 p=world(v[k],v[k+1],v[k+2]);double[] xyz={p.x,p.y,p.z};for(int j=0;j<3;j++){lo[i][j]=Math.min(lo[i][j],xyz[j]);hi[i][j]=Math.max(hi[i][j],xyz[j]);}}}
  for(int i=0;i<parts.length;i++){double[] l=lo[i],h=hi[i];if(!Double.isFinite(l[0]))continue;parts[i].setPos((l[0]+h[0])/2,l[1],(l[2]+h[2])/2);parts[i].setBoundingBox(new AABB(l[0],l[1],l[2],h[0],h[1],h[2]).inflate(.025));}
 }
 public boolean canPursue(LivingEntity e){return e!=null&&!(e instanceof AntEntity)&&distanceToSqr(e)<=96*96&&(home==null||home.distanceToSqr(e.position())<=80*80)&&CombatTargeting.canAttack(this,e,CombatTargeting.Context.DIRECT);}
 public boolean validThreatTarget(LivingEntity e){return canPursue(e)&&(CombatTargeting.canAttack(this,e,CombatTargeting.Context.ACQUIRE)||BossThreat.recordedAttacker(this,e));}
 public boolean acceptThreatTarget(LivingEntity e){if(action()!=IDLE||!validThreatTarget(e))return false;setTarget(e);return getTarget()==e;}
 public boolean threatAttackLocked(){return action()!=IDLE;}
 @Override public void tick(){super.tick();if(!isAlive()){updateParts();return;}if(level() instanceof ServerLevel w){
  if(home==null)home=position();bar.setProgress(getHealth()/getMaxHealth());bar.setColor(BossEvent.BossBarColor.BLUE);
  for(int i=0;i<cd.length;i++)if(cd[i]>0)cd[i]--;if(recovery>0)recovery--;
  if(action()==IDLE)think(w);else execute(w);
 }updateParts();}
 private List<AntMinionEntity> colony(ServerLevel w){return w.getEntitiesOfClass(AntMinionEntity.class,getBoundingBox().inflate(64),a->a.isAlive()&&getUUID().equals(a.ownerId())&&!a.dispersing());}
 private void think(ServerLevel w){BossThreat.selectLivingBoss(this);LivingEntity t=getTarget();if(!validThreatTarget(t)){setTarget(null);if(tickCount%10==0)t=w.getEntitiesOfClass(Player.class,getBoundingBox().inflate(24),p->validThreatTarget(p)&&hasLineOfSight(p)).stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null);else t=null;if(t!=null)setTarget(t);}
  if(!validThreatTarget(t)){if(home.distanceToSqr(position())>4&&tickCount%10==0)getNavigation().moveTo(home.x,home.y,home.z,1);return;}
  if(home.distanceToSqr(position())>80*80){setTarget(null);getNavigation().moveTo(home.x,home.y,home.z,1);return;}
  double dist=distanceTo(t);boolean sight=hasLineOfSight(t);if(recovery==0&&sight){int p=battlePhase();var ants=colony(w);long ground=ants.stream().filter(a->a.kind()==WORKER).count(),air=ants.size()-ground;
   if(dist<=24&&cd[SUMMON]==0&&ground<cap(false,p)){begin(SUMMON,t,false);return;}
   if(dist<=24&&p>=2&&cd[AIR_SUMMON]==0&&air<cap(true,p)){begin(AIR_SUMMON,t,false);return;}
   if(dist<=24&&p>=2&&cd[PHEROMONE]==0&&ants.size()>=5){begin(PHEROMONE,t,false);return;}
   Vec3 d=t.position().subtract(position());Vec3 f=Vec3.directionFromRotation(0,getYRot());
   if(dist<=4.5&&d.dot(f)<-1&&cd[ABDOMEN]==0){begin(ABDOMEN,t,true);return;}
   if(dist<=4&&cd[BITE]==0){begin(BITE,t,false);return;}
   if(dist<=4&&cd[LEGS]==0){begin(LEGS,t,false);return;}
  }
  turn(t.position());if(dist>5&&tickCount%8==0)getNavigation().moveTo(t,1);else if(dist<=5)getNavigation().stop();
 }
 private void begin(int a,LivingEntity t,boolean rear){if(!rear)turn(t.position());lockedYaw=getYRot();lockedTarget=t.getUUID();fired=false;cd[a]=cooldown(a,battlePhase());action(a);getNavigation().stop();playSound(AntSounds.ALERT.get(),1,.8F);}
 private void execute(ServerLevel w){int a=action();getNavigation().stop();setDeltaMovement(0,getDeltaMovement().y,0);setYRot(lockedYaw);setYBodyRot(lockedYaw);setYHeadRot(lockedYaw);
  if(!fired&&age()>=impact(a)){fired=true;switch(a){
   case SUMMON,AIR_SUMMON->summon(w,a==AIR_SUMMON);
   case PHEROMONE->{for(var ant:colony(w))ant.enrage();w.sendParticles(ParticleTypes.HAPPY_VILLAGER,getX(),getY()+1,getZ(),50,3,.8,3,.04);playSound(AntSounds.CALL.get(),1.5F,1.2F);}
   case BITE,LEGS,ABDOMEN->strike(w,a);
   default->{}
  }}if(age()>=duration(a)){action(IDLE);lockedTarget=null;recovery=12;}
 }
 private void strike(ServerLevel w,int a){updateParts();int[] ids=a==BITE?new int[]{3}:a==LEGS?new int[]{4,7}:new int[]{2};Set<UUID> hit=new HashSet<>();playSound(AntSounds.BITE.get(),1.4F,a==ABDOMEN?.65F:1F);
  for(int id:ids){AABB box=parts[id].getBoundingBox().inflate(a==ABDOMEN?.65:.4);for(LivingEntity t:w.getEntitiesOfClass(LivingEntity.class,box,e->canPursue(e)&&hasLineOfSight(e)))if(hit.add(t.getUUID())&&t.hurt(damageSources().mobAttack(this),damage(a))){Vec3 d=t.position().subtract(position());t.knockback(a==ABDOMEN?1.7:a==LEGS?.8:.4,-d.x,-d.z);}}
 }
 private void summon(ServerLevel w,boolean flying){int p=battlePhase();long existing=colony(w).stream().filter(a->a.kind()==(flying?FLYING:WORKER)).count();int wanted=Math.min(count(flying,p),Math.max(0,cap(flying,p)-(int)existing));int spawned=0;
  for(int i=0;i<wanted*6&&spawned<wanted;i++){double angle=i*2.399963229728653;double r=3.1+(i%4)*.65;Vec3 at=position().add(Math.cos(angle)*r,0,Math.sin(angle)*r);BlockPos pos=BlockPos.containing(at);if(!w.isLoaded(pos))continue;for(int j=0;j<3&&!w.getBlockState(pos.below()).isFaceSturdy(w,pos.below(),net.minecraft.core.Direction.UP);j++)pos=pos.below();
   if(!w.getBlockState(pos.below()).isFaceSturdy(w,pos.below(),net.minecraft.core.Direction.UP))continue;
   AntMinionEntity ant=(flying?AntContent.FLYING.get():AntContent.WORKER.get()).create(w);if(ant==null)continue;ant.setPos(pos.getX()+.5,pos.getY(),pos.getZ()+.5);if(!w.noCollision(ant,ant.getBoundingBox()))continue;ant.bind(getUUID(),spawned,lockedTarget);if(w.addFreshEntity(ant)){spawned++;w.sendParticles(ParticleTypes.POOF,ant.getX(),ant.getY()+.2,ant.getZ(),10,.25,.1,.25,.02);}
  }playSound(AntSounds.CALL.get(),1.5F,flying?1.2F:.8F);
 }
 @Override public void die(DamageSource source){super.die(source);bar.removeAllPlayers();if(level() instanceof ServerLevel w)for(var a:colony(w))a.disperse();}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putIntArray("AntCooldowns",cd);if(home!=null){n.putDouble("AntHomeX",home.x);n.putDouble("AntHomeY",home.y);n.putDouble("AntHomeZ",home.z);}}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);int[] c=n.getIntArray("AntCooldowns");for(int i=0;i<Math.min(c.length,cd.length);i++)cd[i]=Math.max(0,c[i]);if(n.contains("AntHomeX"))home=new Vec3(n.getDouble("AntHomeX"),n.getDouble("AntHomeY"),n.getDouble("AntHomeZ"));action(IDLE);}
}
