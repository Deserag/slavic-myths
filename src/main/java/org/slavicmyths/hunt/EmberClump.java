package org.slavicmyths.hunt;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.slavicmyths.registry.*;
public final class EmberClump extends ProjectileItemEntity {
 public EmberClump(EntityType<? extends EmberClump> t,World w){super(t,w);}
 @Override protected Item getDefaultItem(){return ModItems.ISKRA_OVINNIKA.get();}
 @Override protected float getGravity(){return .015F;}
 @Override public void tick(){super.tick();if(!level.isClientSide&&tickCount>60)remove();}
 @Override protected void onHitEntity(EntityRayTraceResult hit){super.onHitEntity(hit);if(!level.isClientSide&&getOwner() instanceof HuntMob&&hit.getEntity() instanceof net.minecraft.entity.player.PlayerEntity)hit.getEntity().hurt(DamageSource.indirectMobAttack(this,(HuntMob)getOwner()).setProjectile().setIsFire(),damage((HuntMob)getOwner()));}
 @Override protected void onHit(RayTraceResult hit){super.onHit(hit);if(level.isClientSide)return;Entity owner=getOwner();if(owner instanceof HuntMob)for(net.minecraft.entity.player.PlayerEntity p:level.getEntitiesOfClass(net.minecraft.entity.player.PlayerEntity.class,getBoundingBox().inflate(1.5)))if(!(hit instanceof EntityRayTraceResult&&((EntityRayTraceResult)hit).getEntity()==p)&&p.distanceTo(this)<=1.5&&((HuntMob)owner).clearLine(position(),p.position().add(0,1,0)))p.hurt(DamageSource.indirectMobAttack(this,(HuntMob)owner).setProjectile().setIsFire(),damage((HuntMob)owner));remove();}
 private float damage(HuntMob owner){return owner instanceof ElementHuntMob?ElementRules.fireDamage(4,owner.wetRain()):owner.wetRain()?4.25F:5;}
 @Override public net.minecraft.network.IPacket<?> getAddEntityPacket(){return net.minecraftforge.fml.network.NetworkHooks.getEntitySpawningPacket(this);}
}
