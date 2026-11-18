package org.slavicmyths.losthalls;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.slavicmyths.registry.ModEntities;
public final class HallsBolt extends Projectile implements ItemSupplier {
 public static final net.neoforged.neoforge.registries.DeferredHolder<EntityType<?>,EntityType<HallsBolt>> TYPE=ModEntities.ENTITIES.register("halls_bolt",()->EntityType.Builder.<HallsBolt>of(HallsBolt::new,MobCategory.MISC).sized(.3F,.3F).clientTrackingRange(8).updateInterval(1).build("slavicmyths:halls_bolt"));
 private static final EntityDataAccessor<Integer> MODE=SynchedEntityData.defineId(HallsBolt.class,EntityDataSerializers.INT);
 public HallsBolt(EntityType<? extends HallsBolt> t,Level w){super(t,w);}public static void init(){}
 public void mode(int mode){entityData.set(MODE,Math.clamp(mode,0,6));}
 @Override protected void defineSynchedData(SynchedEntityData.Builder b){b.define(MODE,0);}
 @Override public ItemStack getItem(){return new ItemStack(entityData.get(MODE)==5?HallsContent.item("lightvein_shard"):Items.FIRE_CHARGE);}
 @Override protected boolean canHitEntity(Entity e){return super.canHitEntity(e)&&!(e instanceof HallsMob)&&!(e instanceof HallsBolt)&&!(e instanceof TamableAnimal a&&a.isTame())&&!e.getType().is(org.slavicmyths.combat.CombatTargeting.PROTECTED)&&!(e instanceof net.minecraft.world.entity.player.Player p&&(p.isCreative()||p.isSpectator()));}
 @Override public void tick(){super.tick();Vec3 next=position().add(getDeltaMovement());if(!level().isClientSide){if(tickCount>80||!level().hasChunkAt(BlockPos.containing(next))){discard();return;}var sweep=new AABB(position(),next).inflate(.2);for(int x=net.minecraft.util.Mth.floor(sweep.minX)>>4;x<=(net.minecraft.util.Mth.floor(sweep.maxX)>>4);x++)for(int z=net.minecraft.util.Mth.floor(sweep.minZ)>>4;z<=(net.minecraft.util.Mth.floor(sweep.maxZ)>>4);z++)if(!level().hasChunk(x,z)){discard();return;}var hit=ProjectileUtil.getHitResultOnMoveVector(this,this::canHitEntity);if(hit.getType()!=HitResult.Type.MISS){if(hit instanceof EntityHitResult eh&&eh.getEntity() instanceof LivingEntity living&&getOwner() instanceof LivingEntity owner&&!owner.isAlliedTo(living)){
   int mode=entityData.get(MODE);boolean hurt=living.hurt(mode==3?damageSources().indirectMagic(this,owner):damageSources().mobProjectile(this,owner),mode==5?7:5);
   if(hurt){if(mode==0||mode==4||mode==6)living.igniteForSeconds(3);if(mode==1)living.addEffect(new MobEffectInstance(MobEffects.POISON,80),owner);if(mode==2)living.addEffect(new MobEffectInstance(MobEffects.WITHER,60),owner);}
  }discard();return;}}
  setPos(next);updateRotation();if(level().isClientSide)level().addParticle(net.minecraft.core.particles.ParticleTypes.ENCHANT,getX(),getY(),getZ(),0,0,0);
 }
 @Override protected void addAdditionalSaveData(CompoundTag t){super.addAdditionalSaveData(t);t.putInt("Mode",entityData.get(MODE));t.putInt("Age",tickCount);}
 @Override protected void readAdditionalSaveData(CompoundTag t){super.readAdditionalSaveData(t);mode(t.getInt("Mode"));tickCount=Math.clamp(t.getInt("Age"),0,81);}
}
