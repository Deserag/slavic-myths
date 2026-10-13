package org.slavicmyths.sanctuary;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.server.level.ServerLevel;
public final class FlameVial extends ThrowableItemProjectile {
 public FlameVial(EntityType<? extends FlameVial> t,Level w){super(t,w);}
 @Override protected Item getDefaultItem(){return SanctuaryContent.item("flame_vial");}
 @Override public void tick(){super.tick();if(!level().isClientSide&&tickCount>60)discard();}
 @Override protected void onHit(HitResult hit){super.onHit(hit);if(!(level() instanceof ServerLevel w))return;setPos(hit.getLocation().add(hit instanceof BlockHitResult block?net.minecraft.world.phys.Vec3.atLowerCornerOf(block.getDirection().getNormal()).scale(.1):Vec3.ZERO));if(getOwner() instanceof LivingEntity owner&&owner.isAlive())for(var t:w.getEntitiesOfClass(LivingEntity.class,getBoundingBox().inflate(2.5),t->t instanceof Mob&&(org.slavicmyths.morozko.FrostWorld.hostile(t,owner)||org.slavicmyths.bogatyr.HeroThreats.blighted(t))&&!(t instanceof org.slavicmyths.karachun.NightRaven r&&r.allied())&&org.slavicmyths.morozko.FrostWorld.bboxDistance(position(),t.getBoundingBox())<=6.25&&w.clip(new net.minecraft.world.level.ClipContext(position(),t.getBoundingBox().getCenter(),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,this)).getType()==HitResult.Type.MISS)){boolean mor=t instanceof org.slavicmyths.entity.LandSpiritEntity m&&m.morState()>0;float damage=(float)(SanctuaryConfig.FLAME_DAMAGE.get()*(mor?1.25:1));if(t.hurt(damageSources().thrown(this,owner),damage))t.igniteForSeconds(3);}w.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,getX(),getY(),getZ(),24,.8,.25,.8,.04);SanctuaryContent.sound(w,blockPosition(),"vial_hit");discard();}
}
