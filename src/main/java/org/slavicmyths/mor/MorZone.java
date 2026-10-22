package org.slavicmyths.mor;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.*;
import org.slavicmyths.entity.LandSpiritEntity;
/** Terrain-safe temporary ground/cloud; loaded lifetime capped, no block writes or chunk tickets. */
public final class MorZone extends Entity {
 private static final EntityDataAccessor<Integer> STYLE=SynchedEntityData.defineId(MorZone.class,EntityDataSerializers.INT);
 private UUID owner;private int age,absent;private final MorExposure exposure=new MorExposure();
 private final Map<UUID,Integer> otherExposure=new HashMap<>();
 public MorZone(EntityType<? extends MorZone> t,Level w){super(t,w);noPhysics=true;}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(STYLE,0);}
 public int style(){return entityData.get(STYLE);}public boolean cloud(){return style()>=2;}public double radius(){return cloud()?5:3;}
 public UUID owner(){return owner;}
 public void configure(LandSpiritEntity m,boolean cloud){owner=m.getUUID();entityData.set(STYLE,(m.mor.kind()==6?1:0)+(cloud?2:0));}
 @Override public void tick(){super.tick();if(level().isClientSide){if(tickCount%4==0)for(int i=0;i<(cloud()?10:3);i++){double x=getX()+(random.nextDouble()*2-1)*radius(),z=getZ()+(random.nextDouble()*2-1)*radius();level().addParticle(style()%2==0?MorContent.ASH.get():net.minecraft.core.particles.ParticleTypes.SMOKE,x,getY()+random.nextDouble()*(cloud()?2:.25),z,0,.02,0);level().addParticle(MorContent.SPARK.get(),x,getY()+.1,z,0,.015,0);}return;}if(++age> HigherRules.life(cloud())){discard();return;}if(tickCount%10!=0)return;
  if(style()%2==1){tickOtherHigherZone();return;}
  Entity source=owner==null?null:((ServerLevel)level()).getEntity(owner);
  if(!(source instanceof LandSpiritEntity s)||!s.isAlive()||s.isRemoved()||distanceToSqr(s)>32*32){exposure.clear();if((absent+=10)>=40)discard();return;}
  absent=0;
  List<LivingEntity> candidates=level().getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(radius(),cloud()?3:1,radius()),e->HigherMor.enemy(s,e));
  Set<UUID> present=new HashSet<>();
  for(LivingEntity p:candidates){
   if(p.distanceToSqr(getX(),p.getY(),getZ())>radius()*radius()||Math.abs(p.getY()-getY())>=(cloud()?3:1.2)||!s.clearLineForMor(position().add(0,.3,0),p.position().add(0,.3,0)))continue;
   present.add(p.getUUID());var sample=exposure.sample(p.getUUID(),tickCount,cloud());int time=sample.time();
   if(sample.effects()){
    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30,0));
    if(style()==3)p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,30,0));
    if(sample.damage()>0){boolean hit=p.hurt(damageSources().mobAttack(s),sample.damage());org.slavicmyths.hunt.LikhoCombatTrace.event(s,"zone.damage cloud="+cloud()+" exposure="+time+" victim="+p.getUUID()+" accepted="+hit);}
   }
  }
  exposure.retain(present);
 }
 // Preserve the other higher boss's existing policy and cadence verbatim.
 private void tickOtherHigherZone(){
  Entity source=owner==null?null:((ServerLevel)level()).getEntity(owner);if(!(source instanceof LandSpiritEntity s)||!s.isAlive()||distanceToSqr(s)>32*32){if((absent+=10)>=40){discard();return;}}else absent=0;
  Set<UUID> present=new HashSet<>();for(Player p:level().getEntitiesOfClass(Player.class,getBoundingBox().inflate(radius(),cloud()?3:1,radius()),p->MorController.valid(p)&&p.distanceToSqr(getX(),p.getY(),getZ())<=radius()*radius()&&Math.abs(p.getY()-getY())<(cloud()?3:1.2)&& (source==null||!source.isAlliedTo(p)))){if(source instanceof LandSpiritEntity s&&!s.clearLineForMor(position().add(0,.3,0),p.position().add(0,.3,0)))continue;present.add(p.getUUID());int time=otherExposure.merge(p.getUUID(),10,Integer::sum);if(HigherRules.harms(cloud(),time)){p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,30,0));if(style()==3)p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,30,0));if(time%20==0)p.hurt(source instanceof LivingEntity l?damageSources().mobAttack(l):damageSources().magic(),cloud()?2:1);}}
  otherExposure.keySet().retainAll(present);
 }
 @Override protected void addAdditionalSaveData(CompoundTag n){n.putInt("Style",style());n.putInt("Age",age);if(owner!=null)n.putUUID("Owner",owner);}
 @Override protected void readAdditionalSaveData(CompoundTag n){entityData.set(STYLE,Math.clamp(n.getInt("Style"),0,3));age=Math.clamp(n.getInt("Age"),0,180);owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;}
}
