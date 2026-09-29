package org.slavicmyths.entity;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.slavicmyths.registry.*;
public final class HotStoneEntity extends ProjectileItemEntity {
    public HotStoneEntity(EntityType<? extends HotStoneEntity> t,World w){super(t,w);}
    public HotStoneEntity(World w,LivingEntity owner){super(ModEntities.HOT_STONE.get(),owner,w);}
    @Override protected Item getDefaultItem(){return ModItems.BATH_STONE.get();}
    @Override public net.minecraft.network.IPacket<?> getAddEntityPacket(){return net.minecraftforge.fml.network.NetworkHooks.getEntitySpawningPacket(this);}
    @Override public void tick(){super.tick();if(!level.isClientSide && tickCount>100)remove();}
    @Override protected void onHitEntity(EntityRayTraceResult hit){super.onHitEntity(hit);if(!level.isClientSide && hit.getEntity().hurt(DamageSource.thrown(this,getOwner()),4))hit.getEntity().setSecondsOnFire(2);}
    @Override protected void onHit(RayTraceResult hit){super.onHit(hit);if(!level.isClientSide)remove();}
}
