package org.slavicmyths.artifact;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.*;
import net.minecraft.sounds.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import org.slavicmyths.entity.WildlifeEntity;
import org.slavicmyths.registry.ModEntities;
public final class VelesStaffItem extends Item {
 public VelesStaffItem(Properties p){super(p.durability(48));}
 public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){ItemStack s=p.getItemInHand(h);if(w.isClientSide)return InteractionResultHolder.success(s);long time=ArtifactEvents.now(w);net.minecraft.nbt.CompoundTag data=p.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);if(time<data.getLong("SlavicGuardianReady")){p.displayClientMessage(Component.translatable("artifact.slavicmyths.resting"),true);return InteractionResultHolder.fail(s);}
  WildlifeEntity mob=(w.random.nextInt(5)==0?ModEntities.BROWN_BEAR:ModEntities.FOREST_WOLF).get().create(w);if(mob==null)return InteractionResultHolder.fail(s);boolean placed=false;for(int i=0;i<8;i++){double a=i*Math.PI/4;mob.moveTo(p.getX()+Math.cos(a)*2,p.getY()+.1,p.getZ()+Math.sin(a)*2,p.getYRot(),0);if(w.hasChunkAt(mob.blockPosition())&&w.noCollision(mob)&&w.getBlockState(mob.blockPosition().below()).isSolid()){placed=true;break;}}if(!placed)return InteractionResultHolder.fail(s);
  mob.getPersistentData().putUUID("SlavicGuardianOwner",p.getUUID());mob.getPersistentData().putLong("SlavicGuardianUntil",time+1200);mob.setPersistenceRequired();if(!w.addFreshEntity(mob))return InteractionResultHolder.fail(s);data.putLong("SlavicGuardianReady",time+2400);data.putUUID("SlavicGuardian",mob.getUUID());p.getPersistentData().put(Player.PERSISTED_NBT_TAG,data);p.getCooldowns().addCooldown(this,2400);s.hurtAndBreak(1,p,net.minecraft.world.entity.LivingEntity.getSlotForHand(h));((ServerLevel)w).sendParticles(ParticleTypes.HAPPY_VILLAGER,mob.getX(),mob.getY()+1,mob.getZ(),10,.4,.5,.4,.02);w.playSound(null,mob.blockPosition(),SoundEvents.WOLF_AMBIENT,SoundSource.NEUTRAL,.5F,.65F);org.slavicmyths.progression.Knowledge.award(p,"forest_guardian");return InteractionResultHolder.consume(s);
 }
}
