package org.slavicmyths.underground;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.LootTable;
public final class Podzemnik extends EncounterMob {
 private static final EntityDataAccessor<Integer> VARIANT=SynchedEntityData.defineId(Podzemnik.class,EntityDataSerializers.INT);
 public Podzemnik(EntityType<? extends Monster> t,Level l){super(t,l);}
 protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(VARIANT,0);}
 public int variant(){return entityData.get(VARIANT);}
 public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(key.equals(VARIANT))refreshDimensions();}
 public void variant(int v){entityData.set(VARIANT,v==1?1:0);getAttribute(Attributes.MAX_HEALTH).setBaseValue(v==1?40:28);getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(v==1?.24:.25);getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(v==1?6.5:5);setHealth(getMaxHealth());setItemSlot(EquipmentSlot.MAINHAND,v==1?new ItemStack(Items.IRON_PICKAXE):ItemStack.EMPTY);setDropChance(EquipmentSlot.MAINHAND,0);refreshDimensions();}
 public EntityDimensions getDefaultDimensions(Pose pose){return variant()==1?EntityDimensions.scalable(.78F,1.82F):super.getDefaultDimensions(pose);}
 public SpawnGroupData finalizeSpawn(ServerLevelAccessor l,DifficultyInstance d,MobSpawnType type,SpawnGroupData group){var result=super.finalizeSpawn(l,d,type,group);variant(random.nextFloat()<.3?1:0);return result;}
 public Component getDisplayName(){return hasCustomName()?super.getDisplayName():Component.translatable(variant()==1?"entity.slavicmyths.podzemnik_miner":"entity.slavicmyths.podzemnik");}
 protected net.minecraft.sounds.SoundEvent getAmbientSound(){return UndergroundSounds.sound("podzemnik","idle");}protected net.minecraft.sounds.SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource d){return UndergroundSounds.sound("podzemnik","hurt");}protected net.minecraft.sounds.SoundEvent getDeathSound(){return UndergroundSounds.sound("podzemnik","death");}
 protected void choose(){double d=distanceTo(getTarget());if(variant()==1&&d<2.8&&ready(Action.MINER_HEAVY))begin(Action.MINER_HEAVY,34);else if(d<2.4&&ready(Action.MELEE))begin(Action.MELEE,variant()==1?28:25);else if(variant()==0&&d>=3&&d<=5&&ready(Action.LUNGE)&&safe(blockPosition().relative(getDirection(),2)))begin(Action.LUNGE,22);}
 protected void step(){switch(action()){case MELEE->{if(elapsed==6)melee(variant()==1?6.5F:5,2.4);if(elapsed==duration)finish(Action.MELEE,1,1);}case MINER_HEAVY->{if(elapsed==20){var t=getTarget();melee(10,2.6);if(validTarget(t)&&distanceTo(t)<2.6&&hasLineOfSight(t))t.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,50,0));dust(position(),5);}if(elapsed==duration)finish(Action.MINER_HEAVY,160,12);}case LUNGE->{if(elapsed==7&&validTarget(getTarget())){var v=getTarget().position().subtract(position()).normalize();if(safe(net.minecraft.core.BlockPos.containing(position().add(v.scale(1.5)))))setDeltaMovement(v.scale(.35));}if(elapsed==12)melee(5,2.4);if(elapsed==duration)finish(Action.LUNGE,200,8);}default->{}}}
 public ResourceKey<LootTable> getDefaultLootTable(){return ResourceKey.create(Registries.LOOT_TABLE,UndergroundMobs.id("entities/"+(variant()==1?"podzemnik_miner":"podzemnik")));}
 public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);n.putInt("Variant",variant());}
 public void readAdditionalSaveData(CompoundTag n){float health=n.contains("Health",99)?n.getFloat("Health"):Float.NaN;super.readAdditionalSaveData(n);variant(n.getInt("Variant"));if(!Float.isNaN(health))setHealth(Math.min(health,getMaxHealth()));}
}
