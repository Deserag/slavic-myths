package org.slavicmyths.entity;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.*;
public final class HotStoneEntity extends ThrowableItemProjectile {
    public HotStoneEntity(EntityType<? extends HotStoneEntity> t,Level w){super(t,w);}
    public HotStoneEntity(Level w,LivingEntity owner){super(ModEntities.HOT_STONE.get(),owner,w);}
    @Override protected Item getDefaultItem(){return ModItems.BATH_STONE.get();}

    @Override public void tick(){super.tick();if(!level().isClientSide && tickCount>100)discard();}
    @Override protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(!level().isClientSide && hit.getEntity().hurt(damageSources().thrown(this,getOwner()),4))hit.getEntity().igniteForSeconds(2);}
    @Override protected void onHit(HitResult hit){super.onHit(hit);if(!level().isClientSide)discard();}
}
