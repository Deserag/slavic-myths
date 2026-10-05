package org.slavicmyths.kurgan;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;

import org.slavicmyths.registry.ModItems;

/** Short-lived physical projectile; solid/protected masonry stops it. */
public final class KurganBolt extends ThrowableItemProjectile {
    public KurganBolt(EntityType<? extends KurganBolt> type,Level world){super(type,world);}
    @Override protected Item getDefaultItem(){return ModItems.NAV_ESSENCE.get();}
    @Override protected double getDefaultGravity(){return 0;}
    @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount>50)discard();}
    @Override protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(!level().isClientSide){Entity owner=getOwner();if(owner instanceof LivingEntity&&!(hit.getEntity() instanceof KurganCreature))hit.getEntity().hurt(damageSources().mobProjectile(this,(LivingEntity)owner),6);discard();}}
    @Override protected void onHitBlock(BlockHitResult hit){super.onHitBlock(hit);if(!level().isClientSide)discard();}

}
