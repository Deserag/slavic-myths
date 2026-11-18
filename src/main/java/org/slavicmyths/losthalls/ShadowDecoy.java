package org.slavicmyths.losthalls;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.ModEntities;
/** Visible player-shaped decoy. No inventory, no loot, one valid positive hit. */
public final class ShadowDecoy extends PathfinderMob {
 public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<ShadowDecoy>> TYPE=ModEntities.ENTITIES.register("shadow_face_decoy",()->EntityType.Builder.<ShadowDecoy>of(ShadowDecoy::new,MobCategory.MISC).sized(.6F,1.8F).clientTrackingRange(10).updateInterval(2).build("slavicmyths:shadow_face_decoy"));
 private static final EntityDataAccessor<java.util.Optional<UUID>> OWNER=SynchedEntityData.defineId(ShadowDecoy.class,EntityDataSerializers.OPTIONAL_UUID);
 private int life=120,attackCooldown;
 public ShadowDecoy(EntityType<? extends ShadowDecoy> t,Level w){super(t,w);xpReward=0;}
 public static void init(net.neoforged.bus.api.IEventBus bus){bus.addListener((net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent e)->e.put(TYPE.get(),Mob.createMobAttributes().add(Attributes.MAX_HEALTH,1).add(Attributes.MOVEMENT_SPEED,.3).build()));}
 public UUID owner(){return entityData.get(OWNER).orElse(null);}public void owner(UUID id){entityData.set(OWNER,java.util.Optional.ofNullable(id));}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){super.defineSynchedData(b);b.define(OWNER,java.util.Optional.empty());}
 @Override protected boolean shouldDropLoot(){return false;}
 @Override public boolean hurt(DamageSource s,float a){if(!level().isClientSide&&a>0&&!isInvulnerableTo(s)){discard();return true;}return false;}
 @Override public boolean doHurtTarget(Entity target){return false;}
 @Override public void tick(){super.tick();if(level().isClientSide)return;if(--life<=0||owner()==null){discard();return;}var p=level().getPlayerByUUID(owner());if(p==null||!p.isAlive()||distanceToSqr(p)>24*24){discard();return;}setYRot(p.getYRot());setXRot(p.getXRot());if(attackCooldown>0)attackCooldown--;
  if(tickCount%10==0&&distanceToSqr(p)>9)navigation.moveTo(p,.9);
  var target=p.getLastHurtMob();if(target!=null&&target.isAlive()&&!(target instanceof net.minecraft.world.entity.player.Player)&&!(target instanceof ShadowDecoy)&&!(target instanceof TamableAnimal a&&a.isTame())&&(target instanceof net.minecraft.world.entity.monster.Enemy||target instanceof PostenMob||target instanceof HallsMob h&&(h.kind==HallsContent.Kind.SHEPTUN||h.kind==HallsContent.Kind.OSKOLNIK))&&hasLineOfSight(target)){
   if(distanceToSqr(target)<9&&attackCooldown==0&&p.swinging){swing(net.minecraft.world.InteractionHand.MAIN_HAND);target.hurt(damageSources().mobAttack(this),(float)Math.min(3,Math.max(.25,p.getAttributeValue(Attributes.ATTACK_DAMAGE)*.225)));attackCooldown=20;}else if(tickCount%10==0&&distanceToSqr(p)<100)navigation.moveTo(target,1);
  }
 }
 @Override public void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);if(owner()!=null)t.putUUID("Owner",owner());t.putInt("Life",life);}
 @Override public void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);owner(t.hasUUID("Owner")?t.getUUID("Owner"):null);life=0;}
}
