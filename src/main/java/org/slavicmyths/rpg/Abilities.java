package org.slavicmyths.rpg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;
public final class Abilities {
 public static boolean active(int i){return i==3||i==4||i==5||i==15||i==20||i==23;}
 public static boolean canAffect(Player p,LivingEntity e){return e!=p&&e.isAlive()&&!e.isAlliedTo(p)&&!e.isInvulnerable()&&(!(e instanceof Player)||p.canHarmPlayer((Player)e));}
 public static boolean canHit(Player p,LivingEntity e){return canAffect(p,e)&&p.hasLineOfSight(e);}
 public static LivingEntity target(Player p,double reach){Vec3 eye=p.getEyePosition(1),look=p.getLookAngle();return p.level().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().expandTowards(look.scale(reach)).inflate(.7),e->canHit(p,e)&&e.distanceToSqr(p)<=reach*reach&&e.getEyePosition(1).subtract(eye).normalize().dot(look)>.92).stream().min(Comparator.comparingDouble(p::distanceToSqr)).orElse(null);}
 public static void particles(Player p,LivingEntity e){if(p.level() instanceof ServerLevel)((ServerLevel)p.level()).sendParticles(ParticleTypes.ENCHANT,e.getX(),e.getY()+.9,e.getZ(),12,.3,.5,.3,.03);}
 public static void activate(ServerPlayer p){
  int i=PathData.data(p).getInt("Active")-1;
  if(i<0||i>=24||!active(i)||!PathData.has(p,PathData.SKILLS[i])){PathData.message(p,"select_ability");return;}
  String skill=PathData.SKILLS[i];ItemStack main=p.getMainHandItem();String type=Runes.category(main);
  if(i==3&&!(p.getOffhandItem().getItem() instanceof ShieldItem)&&!(main.getItem() instanceof ShieldItem)||i==5&&!Runes.heavy(main)||(i==20||i==23)&&!type.equals("staff")){PathData.message(p,"ability_equipment");return;}
  long now=p.level().getGameTime(),until=PathData.data(p).getLong("CD_ability_"+skill);
  if(until>now){PathData.message(p,"cooldown",Component.translatable("skill.slavicmyths."+skill),(until-now+19)/20);return;}
  LivingEntity target=(i==3||i==20||i==23)?target(p,i==3?3:8):null;
  if((i==3||i==20||i==23)&&target==null){PathData.message(p,"no_target");return;}
  int cooldown=i==3||i==15?200:i==20?100:i==23?500:700;
  if((i==20||i==23)&&((Runes.has(main,"midday")&&RuneEffects.day(p))||(Runes.has(main,"shadow")&&RuneEffects.dark(p))))cooldown=(int)(cooldown*.9);
  PathData.ready(p,"ability_"+skill,cooldown);
  if(i==3){target.hurt(org.slavicmyths.combat.MythDamageSources.caused("slavic_path",p),2);target.knockback(.8F,p.getX()-target.getX(),p.getZ()-target.getZ());}
  if(i==4){p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,100,0));p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,100,0));PathData.data(p).putLong("Stance",now+100);}
  if(i==5)PathData.data(p).putLong("Sweep",now+100);
  if(i==15){Vec3 look=p.getLookAngle();Vec3 motion=new Vec3(look.x,0,look.z).normalize().scale(.75);p.setDeltaMovement(motion.x,Math.max(.08,p.getDeltaMovement().y),motion.z);p.hurtMarked=true;}
  if(i==20||i==23){float damage=(i==23?5:3)*(PathData.has(p,"staff_power")?1.12F:1)*(Runes.has(main,"thunder")?1.15F:1);target.hurt(p.damageSources().indirectMagic(p,p),damage);particles(p,target);RuneEffects.staff(p,main,target);}
  PathData.message(p,"activated",Component.translatable("skill.slavicmyths."+skill));
 }
 private Abilities(){}
}
