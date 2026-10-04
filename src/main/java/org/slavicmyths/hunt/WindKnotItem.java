package org.slavicmyths.hunt;
import java.util.List;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraft.world.*;
import net.minecraft.world.server.ServerWorld;
/** An active non-damaging wind pulse, local and server authoritative. */
public final class WindKnotItem extends Item {
 public WindKnotItem(Properties p){super(p);}
 @Override public void appendHoverText(ItemStack s,World w,List<ITextComponent> lines,net.minecraft.client.util.ITooltipFlag f){lines.add(new TranslationTextComponent(getDescriptionId()+".effect").withStyle(TextFormatting.GRAY));}
 @Override public ActionResult<ItemStack> use(World w,PlayerEntity p,Hand h){ItemStack stack=p.getItemInHand(h);if(p.getCooldowns().isOnCooldown(this))return ActionResult.fail(stack);if(w.isClientSide)return ActionResult.success(stack);p.getCooldowns().addCooldown(this,500);
  for(MobEntity e:w.getEntitiesOfClass(MobEntity.class,p.getBoundingBox().inflate(4),e->e.isAlive()&&!e.isAlliedTo(p)&&(e instanceof IMob||e.getType().is(HuntTags.TARGETS)||e.getTarget()==p)&&p.distanceToSqr(e)<=16)){
   if(w.clip(new RayTraceContext(p.getEyePosition(1),e.position().add(0,.8,0),RayTraceContext.BlockMode.COLLIDER,RayTraceContext.FluidMode.NONE,p)).getType()!=RayTraceResult.Type.MISS)continue;Vector3d d=e.position().subtract(p.position()).multiply(1,0,1).normalize();double push=1.1*ElementRules.pushScale(e.getMaxHealth(),e.getType().is(HuntTags.TARGETS));e.setDeltaMovement(e.getDeltaMovement().add(d.scale(push)));e.hasImpulse=true;
  }
  ServerWorld server=(ServerWorld)w;for(int i=0;i<28;i++){double a=i*Math.PI*2/28;server.sendParticles(ParticleTypes.CLOUD,p.getX()+Math.cos(a)*4,p.getY()+.6,p.getZ()+Math.sin(a)*4,1,0,0,0,.025);}w.playSound(null,p.blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundCategory.PLAYERS,.9F,.55F);return ActionResult.consume(stack);
 }
}
