package org.slavicmyths.marinka;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
/** Three legitimate player strikes or 80HP. Cannot be broken by the boss or herd. */
public final class HexAnchor extends net.minecraft.world.entity.PathfinderMob {
 private static final EntityDataAccessor<Integer> HITS=SynchedEntityData.defineId(HexAnchor.class,EntityDataSerializers.INT);
 public UUID boss,arena;
 public HexAnchor(EntityType<? extends HexAnchor> t,Level w){super(t,w);setNoGravity(true);setPersistenceRequired();}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(HITS,0);}
 public int hits(){return entityData.get(HITS);}
 @Override protected void registerGoals(){}
 @Override public void tick(){super.tick();if(level() instanceof ServerLevel w&&(boss==null||!(w.getEntity(boss) instanceof MarinkaEntity m)||!m.active))discard();}
 @Override public boolean hurt(DamageSource s,float n){if(!(s.getEntity() instanceof net.minecraft.server.level.ServerPlayer p)||p.isSpectator()||n<=0)return false;if(invulnerableTime>0)return false;boolean result=super.hurt(s,n);if(result){entityData.set(HITS,hits()+1);if(level() instanceof ServerLevel w&&w.getEntity(boss) instanceof MarinkaEntity m)m.contribution(p);if(hits()>=3&&isAlive())release();}return result;}
 private void release(){if(level() instanceof ServerLevel w&&boss!=null&&w.getEntity(boss) instanceof MarinkaEntity m)m.anchorBroken();discard();}
 @Override public void die(DamageSource s){release();}
 @Override protected void dropAllDeathLoot(ServerLevel w,DamageSource s){}@Override protected void dropExperience(Entity cause){}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(boss!=null)n.putUUID("Boss",boss);if(arena!=null)n.putUUID("Arena",arena);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);boss=n.hasUUID("Boss")?n.getUUID("Boss"):null;arena=n.hasUUID("Arena")?n.getUUID("Arena"):null;}
}
