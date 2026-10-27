package org.slavicmyths.blackcinder;
import java.util.UUID;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.nbt.CompoundTag;
/** Temporary solid-model attack visual, never a terrain edit; no damage on reload. */
public final class CinderSpike extends Entity {
 public UUID owner;
 public CinderSpike(EntityType<? extends CinderSpike> t,Level l){super(t,l);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
 @Override public void tick(){super.tick();if(level().isClientSide)return;if(tickCount==5&&owner!=null&&level() instanceof net.minecraft.server.level.ServerLevel s&&s.getEntity(owner) instanceof CinderMob caster&&caster.isAlive())for(var e:s.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(.4),e->caster.canAttack(e)&&caster.hasLineOfSight(e)))e.hurt(damageSources().mobAttack(caster),10);if(tickCount>22)discard();}
 @Override protected void addAdditionalSaveData(CompoundTag t){t.putInt("Age",tickCount);if(owner!=null)t.putUUID("Owner",owner);}
 @Override protected void readAdditionalSaveData(CompoundTag t){tickCount=Math.max(6,t.getInt("Age"));if(t.hasUUID("Owner"))owner=t.getUUID("Owner");}
}
