package org.slavicmyths.netherboss;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/** Bounded, persisted owner-bound summons. Never loads an owner chunk or grants rewards. */
public final class NetherRetainer extends Zombie {
 private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> SHADOW=net.minecraft.network.syncher.SynchedEntityData.defineId(NetherRetainer.class,net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
 private UUID owner;
 private int remaining=1200;
 private long expires;
 private boolean playerOwned;
 private int missingOwnerTicks;
 public NetherRetainer(EntityType<? extends Zombie> type,Level level){super(type,level);xpReward=0;setPersistenceRequired();}
 @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(SHADOW,false);}
 public boolean shadow(){return entityData.get(SHADOW);}
 public void configure(LivingEntity master,float hp,float damage,int lifetime) {
  entityData.set(SHADOW,master instanceof NetherSummonBoss boss&&boss.isMorova());
  owner=master.getUUID();playerOwned=master instanceof Player;remaining=Math.clamp(lifetime,1,1200);expires=level().getServer().overworld().getGameTime()+remaining;
  getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);setHealth(hp);
  for(var slot:EquipmentSlot.values())setDropChance(slot,0);
 }
 public boolean playerOwned(){return playerOwned;}
 public UUID owner(){return owner;}
 public static java.util.List<NetherRetainer> prepare(LivingEntity master,int count,float hp,float damage){
  if(count<1||count>5||!(master.level() instanceof ServerLevel world))return java.util.List.of();
  var result=new java.util.ArrayList<NetherRetainer>();NetherRetainer probe=null;
  for(int radius:new int[]{2,3,4})for(int step=0;step<12&&result.size()<count;step++){
   if(probe==null)probe=org.slavicmyths.registry.ModEntities.NETHER_RETAINER.get().create(world);
   double angle=step*Math.PI/6+master.getYRot()*Math.PI/180;double x=master.getX()+Math.cos(angle)*radius,z=master.getZ()+Math.sin(angle)*radius;
   probe.moveTo(x,master.getY(),z,master.getYRot(),0);var box=probe.getBoundingBox();
   if(!world.hasChunkAt(net.minecraft.core.BlockPos.containing(box.minX,box.minY,box.minZ))||!world.hasChunkAt(net.minecraft.core.BlockPos.containing(box.maxX,box.maxY,box.maxZ)))continue;
   var below=net.minecraft.core.BlockPos.containing(x,master.getY()-.05,z);
   if(world.getBlockState(below).getCollisionShape(world,below).isEmpty()||!world.noCollision(probe))continue;
   boolean overlap=false;for(var previous:result)if(previous.getBoundingBox().intersects(box)){overlap=true;break;}
   if(overlap)continue;probe.configure(master,hp,damage,1200);result.add(probe);probe=null;
  }return java.util.List.copyOf(result);
 }
 @Override public boolean killedEntity(ServerLevel world,LivingEntity victim){return true;} // Never convert a villager into an unowned vanilla zombie.
 @Override protected boolean convertsInWater(){return false;}
 @Override public boolean canChangeDimensions(Level from,Level to){return false;}
 @Override protected boolean isSunSensitive(){return false;}
 @Override protected void dropAllDeathLoot(ServerLevel world,net.minecraft.world.damagesource.DamageSource source){}
 @Override public boolean canPickUpLoot(){return false;}
 @Override public boolean canAttack(LivingEntity target){
  if(owner==null||!(level() instanceof ServerLevel world))return false;
  var master=world.getEntity(owner);
  if(!(master instanceof LivingEntity living)||!living.isAlive()||target==master||target instanceof NetherRetainer other&&java.util.Objects.equals(owner,other.owner()))return false;
  if(living instanceof net.minecraft.server.level.ServerPlayer player)return NetherTrophies.hostile(player,target);
  return master instanceof NetherSummonBoss boss&&target==boss.getTarget()&&super.canAttack(target);
 }
 @Override public void setTarget(LivingEntity target){super.setTarget(target==null||canAttack(target)?target:null);}
 @Override public void remove(RemovalReason reason){
  if((reason==RemovalReason.KILLED||reason==RemovalReason.DISCARDED)&&level() instanceof ServerLevel world&&owner!=null&&world.getEntity(owner) instanceof NetherSummonBoss boss)boss.retainerDied(getUUID());
  super.remove(reason);
 }
 @Override public void die(net.minecraft.world.damagesource.DamageSource source){
  if(level() instanceof ServerLevel world&&owner!=null&&world.getEntity(owner) instanceof NetherSummonBoss boss)boss.retainerDied(getUUID());
  super.die(source);
 }
 @Override public void tick(){
  super.tick();if(level().isClientSide)return;
  if(--remaining<=0||owner==null||level().getServer().overworld().getGameTime()>=expires){discard();return;}
  var master=((ServerLevel)level()).getEntity(owner);
  if(!(master instanceof LivingEntity living)||!living.isAlive()){
   setTarget(null);setNoAi(true);if(++missingOwnerTicks>=100)discard();return;
  }
  if(missingOwnerTicks>0){missingOwnerTicks=0;if(getPersistentData().contains("MorovaSilenceUntil"))getPersistentData().putBoolean("MorovaSilencePriorNoAI",false);else setNoAi(false);}
  if(tickCount%10==0){
   if(master instanceof NetherSummonBoss boss)setTarget(boss.getTarget());
   else if(master instanceof Player player){
    var target=player.getLastHurtByMob();
    if(target==null||!target.isAlive())target=player.getLastHurtMob();
    setTarget(target);
   }
   if(getTarget()==null&&distanceToSqr(master)>16)getNavigation().moveTo(master,1.1);
  }
 }
 @Override public void addAdditionalSaveData(CompoundTag tag){super.addAdditionalSaveData(tag);if(owner!=null)tag.putUUID("RiteOwner",owner);tag.putInt("RiteRemaining",remaining);tag.putLong("RiteExpires",expires);tag.putBoolean("RitePlayerOwned",playerOwned);tag.putBoolean("RiteShadow",shadow());}
 @Override public void readAdditionalSaveData(CompoundTag tag){super.readAdditionalSaveData(tag);owner=tag.hasUUID("RiteOwner")?tag.getUUID("RiteOwner"):null;remaining=Math.clamp(tag.getInt("RiteRemaining"),0,1200);expires=tag.getLong("RiteExpires");playerOwned=tag.getBoolean("RitePlayerOwned");entityData.set(SHADOW,tag.getBoolean("RiteShadow"));}
}
