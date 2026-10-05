package org.slavicmyths.depth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.effect.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.*;
public final class ThrownNet extends ThrowableItemProjectile {
 public ThrownNet(EntityType<? extends ThrownNet> t,Level w){super(t,w);}public ThrownNet(Level w,LivingEntity e){super(ModEntities.THROWN_NET.get(),e,w);}
 protected Item getDefaultItem(){return ModItems.VODYANOY_NET.get();}

 public void tick(){super.tick();if(!level().isClientSide&&tickCount>80)discard();}
 protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(level().isClientSide||!(hit.getEntity() instanceof LivingEntity)||hit.getEntity()==getOwner())return;LivingEntity e=(LivingEntity)hit.getEntity();if(getOwner()!=null&&getOwner().isAlliedTo(e))return;if(!e.canChangeDimensions(e.level(),e.level())||e.getMaxHealth()>=100||e.getTags().contains("slavic_music_immune"))return;e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,e.isInWater()?100:60,e.isInWater()?4:3));}
 protected void onHit(HitResult hit){super.onHit(hit);if(!level().isClientSide)discard();}
}
