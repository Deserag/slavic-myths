package org.slavicmyths.hunt;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.*;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
public final class BossStone extends ThrowableItemProjectile {
 public BossStone(EntityType<? extends BossStone> t,Level w){super(t,w);}
 @Override protected Item getDefaultItem(){return Items.COBBLESTONE;}
 @Override protected double getDefaultGravity(){return .035F;}
 @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount>=80)discard();}
 @Override protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(!level().isClientSide&&getOwner() instanceof LivingEntity&&hit.getEntity() instanceof LivingEntity){LivingEntity victim=(LivingEntity)hit.getEntity();victim.hurt(damageSources().mobProjectile(this,(LivingEntity)getOwner()),7);Vec3 d=victim.position().subtract(position()).normalize();victim.knockback(.5F,-d.x,-d.z);}}
 @Override protected void onHit(HitResult hit){super.onHit(hit);if(!level().isClientSide)discard();}

}
