package org.slavicmyths.hunt;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import org.slavicmyths.registry.*;
public final class EmberClump extends ThrowableItemProjectile {
 public EmberClump(EntityType<? extends EmberClump> t,Level w){super(t,w);}
 @Override protected Item getDefaultItem(){return ModItems.ISKRA_OVINNIKA.get();}
 @Override protected double getDefaultGravity(){return .015F;}
 @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount>60)discard();}
 @Override protected void onHitEntity(EntityHitResult hit){super.onHitEntity(hit);if(!level().isClientSide&&getOwner() instanceof HuntMob&&hit.getEntity() instanceof net.minecraft.world.entity.player.Player)hit.getEntity().hurt(org.slavicmyths.combat.MythDamageSources.ember(this,(HuntMob)getOwner()),damage((HuntMob)getOwner()));}
 @Override protected void onHit(HitResult hit){super.onHit(hit);if(level().isClientSide)return;Entity owner=getOwner();if(owner instanceof HuntMob)for(net.minecraft.world.entity.player.Player p:level().getEntitiesOfClass(net.minecraft.world.entity.player.Player.class,getBoundingBox().inflate(1.5)))if(!(hit instanceof EntityHitResult&&((EntityHitResult)hit).getEntity()==p)&&p.distanceTo(this)<=1.5&&((HuntMob)owner).clearLine(position(),p.position().add(0,1,0)))p.hurt(org.slavicmyths.combat.MythDamageSources.ember(this,(HuntMob)owner),damage((HuntMob)owner));discard();}
 private float damage(HuntMob owner){return owner instanceof ElementHuntMob?ElementRules.fireDamage(4,owner.wetRain()):owner.wetRain()?4.25F:5;}

}
