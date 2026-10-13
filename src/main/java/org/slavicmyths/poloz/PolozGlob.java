package org.slavicmyths.poloz;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
public final class PolozGlob extends Entity {
 private UUID owner,volley;private int age;
 public PolozGlob(EntityType<? extends PolozGlob> t,Level w){super(t,w);}
 public void configure(GreatPoloz p,Vec3 velocity){owner=p.getUUID();volley=p.getUUID();setPos(p.position().add(0,3,0));setDeltaMovement(velocity);}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){}
 @Override public void tick(){super.tick();if(level().isClientSide)return;if(++age>100){discard();return;}var w=(ServerLevel)level();if(!(w.getEntity(owner) instanceof GreatPoloz boss)||!boss.isAlive()){discard();return;}
  Vec3 start=position(),end=start.add(getDeltaMovement());if(!w.hasChunkAt(net.minecraft.core.BlockPos.containing(end))){discard();return;}var hit=w.clip(new net.minecraft.world.level.ClipContext(start,end,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this));var entity=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(w,this,start,end,getBoundingBox().expandTowards(getDeltaMovement()).inflate(.2),e->e instanceof LivingEntity l&&!boss.isAlliedTo(l),0);
  if(entity!=null&&entity.getEntity() instanceof LivingEntity living&&entity.getLocation().distanceToSqr(start)<=hit.getLocation().distanceToSqr(start))boss.rainDamage(living,6,volley);
  if(entity!=null||hit.getType()==HitResult.Type.BLOCK){Vec3 at=entity!=null?entity.getLocation():hit.getLocation();boss.impact.patch(net.minecraft.core.BlockPos.containing(at).below());for(var l:w.getEntitiesOfClass(LivingEntity.class,new AABB(at.add(-2,-2,-2),at.add(2,2,2)),l->!boss.isAlliedTo(l))){boss.rainDamage(l,3,volley);l.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,60,0));}discard();return;}setPos(end);setDeltaMovement(getDeltaMovement().add(0,-.025,0));
 }
 @Override protected void addAdditionalSaveData(CompoundTag n){if(owner!=null)n.putUUID("Owner",owner);if(volley!=null)n.putUUID("Volley",volley);n.putInt("Age",age);}
 @Override protected void readAdditionalSaveData(CompoundTag n){owner=n.hasUUID("Owner")?n.getUUID("Owner"):null;volley=n.hasUUID("Volley")?n.getUUID("Volley"):null;age=Math.clamp(n.getInt("Age"),0,100);}
}
