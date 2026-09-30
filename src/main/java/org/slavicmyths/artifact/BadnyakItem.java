package org.slavicmyths.artifact;
import net.minecraft.item.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.potion.*;
import net.minecraft.particles.ParticleTypes;
public final class BadnyakItem extends Item {
 public BadnyakItem(Properties p){super(p.stacksTo(16));}
 public int getUseDuration(ItemStack s){return 60;}public UseAction getUseAnimation(ItemStack s){return UseAction.BOW;}
 public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand h){p.startUsingItem(h);return ActionResult.consume(p.getItemInHand(h));}
 public void onUseTick(World w,LivingEntity e,ItemStack s,int left){if(w.isClientSide||left%10!=0)return;ServerWorld server=(ServerWorld)w;net.minecraft.util.math.vector.Vector3d point=e.position().add(e.getLookAngle().scale(.6)).add(0,1,0);server.sendParticles(left>35?ParticleTypes.FLAME:ParticleTypes.SMOKE,point.x,point.y,point.z,2,.05,.06,.05,.005);if(left==50)w.playSound(null,e.blockPosition(),SoundEvents.FIRE_AMBIENT,SoundCategory.PLAYERS,.4F,1);}
 public ItemStack finishUsingItem(ItemStack s,World w,LivingEntity e){if(!w.isClientSide){s.shrink(1);e.addEffect(new EffectInstance(Effects.REGENERATION,2400,0));e.addEffect(new EffectInstance(Effects.DAMAGE_BOOST,2400,0));e.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED,2400,0));e.addEffect(new EffectInstance(Effects.ABSORPTION,2400,0));e.getPersistentData().putLong("SlavicEmbersUntil",ArtifactEvents.now(w)+100);if(e instanceof PlayerEntity)((PlayerEntity)e).getCooldowns().addCooldown(this,2400);w.playSound(null,e.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundCategory.PLAYERS,.3F,.8F);}return s;}
}
