package org.slavicmyths.depth;
import net.minecraft.entity.*;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.item.Item;
import net.minecraft.potion.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.slavicmyths.registry.*;
public final class ThrownNet extends ProjectileItemEntity {
 public ThrownNet(EntityType<? extends ThrownNet> t,World w){super(t,w);}public ThrownNet(World w,LivingEntity e){super(ModEntities.THROWN_NET.get(),e,w);}
 protected Item getDefaultItem(){return ModItems.VODYANOY_NET.get();}
 public net.minecraft.network.IPacket<?> getAddEntityPacket(){return net.minecraftforge.fml.network.NetworkHooks.getEntitySpawningPacket(this);}
 public void tick(){super.tick();if(!level.isClientSide&&tickCount>80)remove();}
 protected void onHitEntity(EntityRayTraceResult hit){super.onHitEntity(hit);if(level.isClientSide||!(hit.getEntity() instanceof LivingEntity)||hit.getEntity()==getOwner())return;LivingEntity e=(LivingEntity)hit.getEntity();if(getOwner()!=null&&getOwner().isAlliedTo(e))return;if(!e.canChangeDimensions()||e.getMaxHealth()>=100||e.getTags().contains("slavic_music_immune"))return;e.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,e.isInWater()?100:60,e.isInWater()?4:3));}
 protected void onHit(RayTraceResult hit){super.onHit(hit);if(!level.isClientSide)remove();}
}
