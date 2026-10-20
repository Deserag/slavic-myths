package org.slavicmyths.marinka;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
/** Persistent-independent local tile warning and finite pulses; renderer sees type and age. */
public final class WitchRune extends Entity {
 private static final EntityDataAccessor<Integer> KIND=SynchedEntityData.defineId(WitchRune.class,EntityDataSerializers.INT),WAIT=SynchedEntityData.defineId(WitchRune.class,EntityDataSerializers.INT);
 public UUID boss,arena;public int waitTicks=35;private final Set<UUID> hit=new HashSet<>();
 public WitchRune(EntityType<? extends WitchRune> t,Level w){super(t,w);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(KIND,0);b.define(WAIT,35);}
 public void kind(int k){entityData.set(KIND,k);}public int kind(){return entityData.get(KIND);}public int warning(){return entityData.get(WAIT);}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;entityData.set(WAIT,waitTicks);var b=boss!=null&&w.getEntity(boss) instanceof MarinkaEntity m&&m.active?m:null;if(b==null||tickCount>waitTicks+(kind()==2?60:10)){discard();return;}boolean pulse=kind()==2?tickCount==20||tickCount==40||tickCount==60:tickCount==waitTicks;if(!pulse)return;if(kind()==1&&!b.burst())return;
  for(var target:w.getEntitiesOfClass(LivingEntity.class,new AABB(getX()-1.25,getY()-.3,getZ()-1.25,getX()+1.25,getY()+2.3,getZ()+1.25),e->e instanceof net.minecraft.world.entity.player.Player||e instanceof DobrynyaEntity)){if(!b.line(position().add(0,.4,0),target.position().add(0,.4,0)))continue;if(kind()!=2&&!hit.add(target.getUUID()))continue;if(kind()==2){var n=target.getPersistentData();if(n.getLong("WitchEmberLast")+20>w.getGameTime())continue;n.putLong("WitchEmberLast",w.getGameTime());target.hurt(target.damageSources().magic(),1);}else{target.hurt(MarinkaEntity.source(b,kind()==1?"rage":"trace"),kind()==1?17.5F:11.2F);if(kind()==0){target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS,80));target.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,40));}}}}
 @Override protected void addAdditionalSaveData(CompoundTag n){}@Override protected void readAdditionalSaveData(CompoundTag n){boss=null;}
}
