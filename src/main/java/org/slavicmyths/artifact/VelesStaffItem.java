package org.slavicmyths.artifact;
import net.minecraft.item.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.particles.ParticleTypes;
import org.slavicmyths.entity.WildlifeEntity;
import org.slavicmyths.registry.ModEntities;
public final class VelesStaffItem extends Item {
 public VelesStaffItem(Properties p){super(p.durability(48));}
 public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand h){ItemStack s=p.getItemInHand(h);if(w.isClientSide)return ActionResult.success(s);long time=ArtifactEvents.now(w);net.minecraft.nbt.CompoundNBT data=p.getPersistentData().getCompound(PlayerEntity.PERSISTED_NBT_TAG);if(time<data.getLong("SlavicGuardianReady")){p.displayClientMessage(new net.minecraft.util.text.TranslationTextComponent("artifact.slavicmyths.resting"),true);return ActionResult.fail(s);}
  WildlifeEntity mob=(w.random.nextInt(5)==0?ModEntities.BROWN_BEAR:ModEntities.FOREST_WOLF).get().create(w);if(mob==null)return ActionResult.fail(s);boolean placed=false;for(int i=0;i<8;i++){double a=i*Math.PI/4;mob.moveTo(p.getX()+Math.cos(a)*2,p.getY()+.1,p.getZ()+Math.sin(a)*2,p.yRot,0);if(w.hasChunkAt(mob.blockPosition())&&w.noCollision(mob)&&w.getBlockState(mob.blockPosition().below()).getMaterial().isSolid()){placed=true;break;}}if(!placed)return ActionResult.fail(s);
  mob.getPersistentData().putUUID("SlavicGuardianOwner",p.getUUID());mob.getPersistentData().putLong("SlavicGuardianUntil",time+1200);mob.setPersistenceRequired();if(!w.addFreshEntity(mob))return ActionResult.fail(s);data.putLong("SlavicGuardianReady",time+2400);data.putUUID("SlavicGuardian",mob.getUUID());p.getPersistentData().put(PlayerEntity.PERSISTED_NBT_TAG,data);p.getCooldowns().addCooldown(this,2400);s.hurtAndBreak(1,p,e->e.broadcastBreakEvent(h));((ServerWorld)w).sendParticles(ParticleTypes.HAPPY_VILLAGER,mob.getX(),mob.getY()+1,mob.getZ(),10,.4,.5,.4,.02);w.playSound(null,mob.blockPosition(),SoundEvents.WOLF_AMBIENT,SoundCategory.NEUTRAL,.5F,.65F);org.slavicmyths.progression.Knowledge.award(p,"forest_guardian");return ActionResult.consume(s);
 }
}
