package org.slavicmyths.hunt;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
public final class BossStone extends ProjectileItemEntity {
 public BossStone(EntityType<? extends BossStone> t,World w){super(t,w);}
 @Override protected Item getDefaultItem(){return Items.COBBLESTONE;}
 @Override protected float getGravity(){return .035F;}
 @Override public void tick(){super.tick();if(!level.isClientSide&&tickCount>=80)remove();}
 @Override protected void onHitEntity(EntityRayTraceResult hit){super.onHitEntity(hit);if(!level.isClientSide&&getOwner() instanceof LivingEntity&&hit.getEntity() instanceof LivingEntity){LivingEntity victim=(LivingEntity)hit.getEntity();victim.hurt(DamageSource.indirectMobAttack(this,(LivingEntity)getOwner()).setProjectile(),7);Vector3d d=victim.position().subtract(position()).normalize();victim.knockback(.5F,-d.x,-d.z);}}
 @Override protected void onHit(RayTraceResult hit){super.onHit(hit);if(!level.isClientSide)remove();}
 @Override public net.minecraft.network.IPacket<?> getAddEntityPacket(){return net.minecraftforge.fml.network.NetworkHooks.getEntitySpawningPacket(this);}
}
