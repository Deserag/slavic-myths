package org.slavicmyths.kurgan;

import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import org.slavicmyths.registry.ModItems;

/** Short-lived physical projectile; solid/protected masonry stops it. */
public final class KurganBolt extends ProjectileItemEntity {
    public KurganBolt(EntityType<? extends KurganBolt> type,World world){super(type,world);}
    @Override protected Item getDefaultItem(){return ModItems.NAV_ESSENCE.get();}
    @Override protected float getGravity(){return 0;}
    @Override public void tick(){super.tick();if(!level.isClientSide&&tickCount>50)remove();}
    @Override protected void onHitEntity(EntityRayTraceResult hit){super.onHitEntity(hit);if(!level.isClientSide){Entity owner=getOwner();if(owner instanceof LivingEntity&&!(hit.getEntity() instanceof KurganCreature))hit.getEntity().hurt(DamageSource.indirectMobAttack(this,(LivingEntity)owner).setProjectile(),6);remove();}}
    @Override protected void onHitBlock(BlockRayTraceResult hit){super.onHitBlock(hit);if(!level.isClientSide)remove();}
    @Override public IPacket<?> getAddEntityPacket(){return NetworkHooks.getEntitySpawningPacket(this);}
}
