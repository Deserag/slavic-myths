package org.slavicmyths.hunt;
import java.util.UUID;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
/** Visual-only projection: one hit, 70 ticks, no loot/XP/encounter ownership. */
public final class SerpentProjection extends CreatureEntity {
 public UUID parent;public long until;public Vector3d drift=Vector3d.ZERO;
 public SerpentProjection(EntityType<? extends SerpentProjection> t,World w){super(t,w);setNoGravity(true);xpReward=0;}
 public static AttributeModifierMap.MutableAttribute attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,1).add(Attributes.MOVEMENT_SPEED,0);}
 @Override protected void registerGoals(){}
 @Override public boolean causeFallDamage(float d,float m){return false;}
 @Override public boolean isAttackable(){return true;}
 @Override public boolean hurt(DamageSource d,float amount){if(level.isClientSide)return false;if(d.getEntity()!=null&&d.getEntity()!=this){burst();remove();return true;}return false;}
 private void burst(){((ServerWorld)level).sendParticles(ParticleTypes.FLAME,getX(),getY()+.7,getZ(),8,.5,.3,.5,.02);}
 @Override public void tick(){super.tick();if(level.isClientSide)return;if(until==0)until=level.getGameTime()+70;Entity source=parent==null?null:((ServerWorld)level).getEntity(parent);if(level.getGameTime()>=until||source!=null&&!source.isAlive()){burst();remove();return;}if(level.hasChunkAt(new net.minecraft.util.math.BlockPos(position().add(drift))))move(MoverType.SELF,drift);if(tickCount%5==0)((ServerWorld)level).sendParticles(ParticleTypes.FLAME,getX(),getY(),getZ(),1,.2,.2,.2,.01);}
 @Override protected net.minecraft.util.ResourceLocation getDefaultLootTable(){return net.minecraft.loot.LootTables.EMPTY;}
 @Override public void addAdditionalSaveData(CompoundNBT n){super.addAdditionalSaveData(n);if(parent!=null)n.putUUID("ProjectionParent",parent);n.putLong("ProjectionUntil",until);n.putDouble("DriftX",drift.x);n.putDouble("DriftY",drift.y);n.putDouble("DriftZ",drift.z);}
 @Override public void readAdditionalSaveData(CompoundNBT n){super.readAdditionalSaveData(n);parent=n.hasUUID("ProjectionParent")?n.getUUID("ProjectionParent"):null;until=n.getLong("ProjectionUntil");drift=new Vector3d(n.getDouble("DriftX"),n.getDouble("DriftY"),n.getDouble("DriftZ"));}
}
