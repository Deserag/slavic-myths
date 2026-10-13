package org.slavicmyths.artifact;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.*;
import net.minecraft.core.particles.ParticleTypes;
public final class BadnyakItem extends Item {
 public BadnyakItem(Properties p){super(p.stacksTo(16));}
 public int getUseDuration(ItemStack s,net.minecraft.world.entity.LivingEntity user){return 60;}public UseAnim getUseAnimation(ItemStack s){return UseAnim.BOW;}
 public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){p.startUsingItem(h);return InteractionResultHolder.consume(p.getItemInHand(h));}
 public void onUseTick(Level w,LivingEntity e,ItemStack s,int left){if(w.isClientSide||left%10!=0)return;ServerLevel server=(ServerLevel)w;net.minecraft.world.phys.Vec3 point=e.position().add(e.getLookAngle().scale(.6)).add(0,1,0);server.sendParticles(left>35?ParticleTypes.FLAME:ParticleTypes.SMOKE,point.x,point.y,point.z,2,.05,.06,.05,.005);if(left==50)w.playSound(null,e.blockPosition(),SoundEvents.FIRE_AMBIENT,SoundSource.PLAYERS,.4F,1);}
 public ItemStack finishUsingItem(ItemStack s,Level w,LivingEntity e){if(!w.isClientSide){s.shrink(1);e.addEffect(new MobEffectInstance(MobEffects.REGENERATION,2400,0));e.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST,2400,0));e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,2400,0));e.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,2400,0));e.getPersistentData().putLong("SlavicEmbersUntil",ArtifactEvents.now(w)+100);if(e instanceof Player)((Player)e).getCooldowns().addCooldown(this,2400);w.playSound(null,e.blockPosition(),SoundEvents.FIRE_EXTINGUISH,SoundSource.PLAYERS,.3F,.8F);}return s;}
}
