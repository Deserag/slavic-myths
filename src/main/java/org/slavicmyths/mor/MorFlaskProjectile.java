package org.slavicmyths.mor;

import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import org.slavicmyths.entity.LandSpiritEntity;
import org.slavicmyths.registry.ModItems;

public final class MorFlaskProjectile extends ThrowableItemProjectile {
    public MorFlaskProjectile(EntityType<? extends MorFlaskProjectile> type,Level level){super(type,level);}
    @Override protected Item getDefaultItem(){return ModItems.MOR_FLASK.get();}
    @Override protected double getDefaultGravity(){return .05;}
    @Override protected void onHit(HitResult hit){super.onHit(hit);if(level() instanceof ServerLevel server){Vec3 p=hit.getLocation();for(var mob:level().getEntitiesOfClass(LandSpiritEntity.class,new AABB(p,p).inflate(3),e->MorRules.flaskRange(e.getBoundingBox().getCenter().distanceToSqr(p))))mob.mor.apply(false);playSound(SoundEvents.SPLASH_POTION_BREAK,1,1);server.sendParticles(MorContent.SPARK.get(),p.x,p.y,p.z,12,.6,.4,.6,0);server.sendParticles(MorContent.ASH.get(),p.x,p.y,p.z,8,.5,.3,.5,.01);discard();}}
    @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount>200)discard();}
}
