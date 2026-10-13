package org.slavicmyths.morozko;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
/** A spike, fixed falling icicle, or expanding wave; no block mutation. */
public final class FrostHazard extends Entity {
 private static final EntityDataAccessor<Integer> KIND=SynchedEntityData.defineId(FrostHazard.class,EntityDataSerializers.INT),DELAY=SynchedEntityData.defineId(FrostHazard.class,EntityDataSerializers.INT),AGE=SynchedEntityData.defineId(FrostHazard.class,EntityDataSerializers.INT);
 private static final EntityDataAccessor<Float> RADIUS=SynchedEntityData.defineId(FrostHazard.class,EntityDataSerializers.FLOAT);
 private UUID owner;public UUID session;private float damage;private int cold;private final Set<UUID> hit=new HashSet<>();private double groundY;
 public FrostHazard(EntityType<? extends FrostHazard> t,net.minecraft.world.level.Level w){super(t,w);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(KIND,0);b.define(DELAY,18);b.define(AGE,0);b.define(RADIUS,7f);}
 public int kind(){return entityData.get(KIND);}public int delay(){return entityData.get(DELAY);}public int age(){return entityData.get(AGE);}public float radius(){return entityData.get(RADIUS);}
 private static FrostHazard create(LivingEntity from,Vec3 p,int kind,int delay,float radius,float damage,int cold,UUID session){var h=FrostContent.HAZARD.get().create(from.level());if(h==null)return null;h.owner=from.getUUID();h.session=session;h.damage=damage;h.cold=cold;h.entityData.set(KIND,kind);h.entityData.set(DELAY,delay);h.entityData.set(RADIUS,radius);h.setPos(p);h.groundY=p.y;from.level().addFreshEntity(h);return h;}
 public static void spike(LivingEntity from,Vec3 p,int delay,float damage,int cold,UUID session){create(from,p,0,delay,1,damage,cold,session);}
 public static void wave(LivingEntity from,float radius,float damage,int cold,UUID session){create(from,from.position(),2,0,radius,damage,cold,session);}
 public static void playerWave(LivingEntity from){create(from,from.position(),2,7,5,4,8,null);}
 public static void falling(LivingEntity from,Vec3 p,int delay,double groundY){var h=create(from,p,1,delay,1,5,10,null);if(h!=null)h.groundY=groundY;}
 @Override public void tick(){super.tick();if(level().isClientSide)return;var w=(ServerLevel)level();entityData.set(AGE,age()+1);var from=owner==null?null:w.getEntity(owner);if(!(from instanceof LivingEntity caster)||!caster.isAlive()||session!=null&&(!(caster instanceof Morozko b)||!b.fighting())){discard();return;}int a=age()-delay();if(a<0){if(tickCount%4==0)FrostVisuals.ring(w,new Vec3(getX(),kind()==1?groundY:getY(),getZ()),.55,.05,8);return;}
 if(a==0)FrostContent.sound(this,kind()==1?"staff_icicles":kind()==2?"wave":"spike_break");AABB box=getBoundingBox();if(kind()==1){var old=position();var next=old.add(0,-.8,0);var clip=w.clip(new net.minecraft.world.level.ClipContext(old,next,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));if(clip.getType()!=HitResult.Type.MISS){FrostVisuals.burst(w,clip.getLocation(),12);discard();return;}setPos(next);box=getBoundingBox().minmax(new AABB(old,next).inflate(.3));}else if(kind()==0){if(!FrostRules.spikeActive(a)){if(a>40)discard();return;}box=new AABB(getX()-.5,getY(),getZ()-.5,getX()+.5,getY()+2.5,getZ()+.5);}else{double r=2+(radius()-2)*Math.min(1,a/18.0);if(a>18){discard();return;}if(tickCount%3==0)FrostVisuals.ring(w,position(),r,.15,24);box=getBoundingBox().inflate(r,1,r);}
 for(var t:w.getEntitiesOfClass(LivingEntity.class,box,e->(caster instanceof Morozko b?b.candidate(e):FrostWorld.hostile(e,caster))&&!hit.contains(e.getUUID())&&caster.hasLineOfSight(e))){if(kind()==2){double r=2+(radius()-2)*Math.min(1,a/18.0);double dist=Math.sqrt(FrostWorld.bboxDistance(position(),t.getBoundingBox()));if(!t.onGround()||dist<r-.8||dist>r+.8)continue;}hit.add(t.getUUID());if(t.hurt(FrostDamage.projectile(kind()==1?"morozko_icicle_damage":"morozko_frost",this,caster),damage)){FrostCold.add(t,cold);if(caster instanceof Morozko b)b.attacked(t);if(kind()==1){t.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,60));t.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,60));}if(kind()==2)t.knockback(.7,getX()-t.getX(),getZ()-t.getZ());}if(kind()==1){FrostVisuals.burst(w,position(),12);discard();break;}}
 if(tickCount>100)discard();}
 @Override protected void readAdditionalSaveData(CompoundTag n){discard();}
 @Override protected void addAdditionalSaveData(CompoundTag n){}
}
