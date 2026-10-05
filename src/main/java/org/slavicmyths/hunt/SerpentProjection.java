package org.slavicmyths.hunt;
import net.minecraft.world.entity.ai.attributes.Attribute;
import java.util.UUID;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
/** Visual-only projection: one hit, 70 ticks, no loot/XP/encounter ownership. */
public final class SerpentProjection extends PathfinderMob {
 public UUID parent;public long until;public Vec3 drift=Vec3.ZERO;
 public SerpentProjection(EntityType<? extends SerpentProjection> t,Level w){super(t,w);setNoGravity(true);xpReward=0;}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,1).add(Attributes.MOVEMENT_SPEED,0);}
 @Override protected void registerGoals(){}
 @Override public boolean causeFallDamage(float d,float m,net.minecraft.world.damagesource.DamageSource source){return false;}
 @Override public boolean isAttackable(){return true;}
 @Override public boolean hurt(DamageSource d,float amount){if(level().isClientSide)return false;if(d.getEntity()!=null&&d.getEntity()!=this){burst();discard();return true;}return false;}
 private void burst(){((ServerLevel)level()).sendParticles(ParticleTypes.FLAME,getX(),getY()+.7,getZ(),8,.5,.3,.5,.02);}
 @Override public void tick(){super.tick();if(level().isClientSide)return;if(until==0)until=level().getGameTime()+70;Entity source=parent==null?null:((ServerLevel)level()).getEntity(parent);if(level().getGameTime()>=until||source!=null&&!source.isAlive()){burst();discard();return;}if(level().hasChunkAt(net.minecraft.core.BlockPos.containing(position().add(drift))))move(MoverType.SELF,drift);if(tickCount%5==0)((ServerLevel)level()).sendParticles(ParticleTypes.FLAME,getX(),getY(),getZ(),1,.2,.2,.2,.01);}
 @Override protected net.minecraft.resources.ResourceKey<net.minecraft.world.level.storage.loot.LootTable> getDefaultLootTable(){return net.minecraft.world.level.storage.loot.BuiltInLootTables.EMPTY;}
 @Override public void addAdditionalSaveData(CompoundTag n){super.addAdditionalSaveData(n);if(parent!=null)n.putUUID("ProjectionParent",parent);n.putLong("ProjectionUntil",until);n.putDouble("DriftX",drift.x);n.putDouble("DriftY",drift.y);n.putDouble("DriftZ",drift.z);}
 @Override public void readAdditionalSaveData(CompoundTag n){super.readAdditionalSaveData(n);parent=n.hasUUID("ProjectionParent")?n.getUUID("ProjectionParent"):null;until=n.getLong("ProjectionUntil");drift=new Vec3(n.getDouble("DriftX"),n.getDouble("DriftY"),n.getDouble("DriftZ"));}
}
