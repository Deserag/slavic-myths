package org.slavicmyths.desert;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.sounds.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;
import static org.slavicmyths.desert.AntRules.*;
public abstract class AntEntity extends Monster {
 private static final EntityDataAccessor<Integer> ACTION=SynchedEntityData.defineId(AntEntity.class,EntityDataSerializers.INT),START=SynchedEntityData.defineId(AntEntity.class,EntityDataSerializers.INT);
 protected AntEntity(EntityType<? extends AntEntity> type,Level level){super(type,level);}
 @Override protected void registerGoals(){}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(ACTION,IDLE);b.define(START,0);}
 public abstract int kind();
 public int action(){return entityData.get(ACTION);}
 public int age(){return Math.max(0,(int)level().getGameTime()-entityData.get(START));}
 protected void action(int a){entityData.set(ACTION,a);entityData.set(START,(int)level().getGameTime());}
 @Override public Component getName(){return hasCustomName()?super.getName():Component.translatableWithFallback(getType().getDescriptionId(),kind()==QUEEN?"Королева муравьёв":kind()==WORKER?"Пустынный муравей":"Летающий пустынный муравей");}
 public int visualAction(){if(isDeadOrDying())return DEATH;if(action()!=IDLE)return action();if(hurtTime>0)return HURT;if(kind()==FLYING&&!onGround())return FLIGHT;return getDeltaMovement().horizontalDistanceSqr()>.0005?WALK:IDLE;}
 public float visualProgress(float partial){int a=visualAction();if(a==DEATH)return Mth.clamp((deathTime+partial)/duration(DEATH),0,1);if(a==HURT)return Mth.clamp((10-hurtTime+partial)/10,0,1);if(a==IDLE||a==WALK||a==FLIGHT)return ((tickCount+partial)%duration(a))/duration(a);return Mth.clamp((age()+partial)/(kind()!=QUEEN&&a==BITE?12:duration(a)),0,1);}
 public java.util.List<AntGeometry.Box> pose(float partial){return AntGeometry.pose(kind(),visualAction(),visualProgress(partial));}
 protected void turn(Vec3 p){Vec3 d=p.subtract(position());if(d.horizontalDistanceSqr()<.001)return;float yaw=(float)(Mth.atan2(-d.x,d.z)*Mth.RAD_TO_DEG);setYRot(yaw);setYBodyRot(yaw);setYHeadRot(yaw);}
 @Override public boolean isAlliedTo(Entity e){return e instanceof AntEntity||super.isAlliedTo(e);}
 @Override public boolean hurt(DamageSource s,float a){return !(s.getEntity() instanceof AntEntity)&&super.hurt(s,a);}
 @Override protected SoundEvent getAmbientSound(){return AntSounds.AMBIENT.get();}
 @Override protected SoundEvent getHurtSound(DamageSource s){return AntSounds.HURT.get();}
 @Override protected SoundEvent getDeathSound(){return AntSounds.DEATH.get();}
 @Override protected void playStepSound(BlockPos p,BlockState s){playSound(AntSounds.STEP.get(),kind()==QUEEN?.8F:.2F,kind()==QUEEN?.65F:1.5F);}
}
