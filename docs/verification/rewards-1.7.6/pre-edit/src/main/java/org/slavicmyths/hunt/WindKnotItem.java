package org.slavicmyths.hunt;
import java.util.List;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.network.chat.*;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.server.level.ServerLevel;
/** An active non-damaging wind pulse, local and server authoritative. */
public final class WindKnotItem extends Item {
 public WindKnotItem(Properties p){super(p);}
 @Override public void appendHoverText(ItemStack s,net.minecraft.world.item.Item.TooltipContext w,List<Component> lines,net.minecraft.world.item.TooltipFlag f){lines.add(Component.translatable(getDescriptionId()+".effect").withStyle(ChatFormatting.GRAY));}
 @Override public InteractionResultHolder<ItemStack> use(Level w,Player p,InteractionHand h){ItemStack stack=p.getItemInHand(h);if(p.getCooldowns().isOnCooldown(this))return InteractionResultHolder.fail(stack);if(w.isClientSide)return InteractionResultHolder.success(stack);p.getCooldowns().addCooldown(this,500);
  for(Mob e:w.getEntitiesOfClass(Mob.class,p.getBoundingBox().inflate(4),e->e.isAlive()&&!e.isAlliedTo(p)&&(e instanceof Enemy||e.getType().is(HuntTags.TARGETS)||e.getTarget()==p)&&p.distanceToSqr(e)<=16)){
   if(w.clip(new ClipContext(p.getEyePosition(1),e.position().add(0,.8,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,p)).getType()!=HitResult.Type.MISS)continue;Vec3 d=e.position().subtract(p.position()).multiply(1,0,1).normalize();double push=1.1*ElementRules.pushScale(e.getMaxHealth(),e.getType().is(HuntTags.TARGETS));e.setDeltaMovement(e.getDeltaMovement().add(d.scale(push)));e.hasImpulse=true;
  }
  ServerLevel server=(ServerLevel)w;for(int i=0;i<28;i++){double a=i*Math.PI*2/28;server.sendParticles(ParticleTypes.CLOUD,p.getX()+Math.cos(a)*4,p.getY()+.6,p.getZ()+Math.sin(a)*4,1,0,0,0,.025);}w.playSound(null,p.blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,.9F,.55F);return InteractionResultHolder.consume(stack);
 }
}
