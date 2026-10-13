package org.slavicmyths.mor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.*;
/** Individual move set; common scheduler owns focus, persistence and non-overlap. */
final class MorPoludnitsa {
 static int choose(MorController c,LivingEntity target,double d,boolean sight){int skill=0;if(d>=2&&d<=5&&Math.abs(target.getY()-c.mob.getY())<=1.5&&c.cooldown[4]==0&&sight)skill=4;else if(d<=6&&c.cooldown[3]==0&&sight)skill=3; return skill;}
 static void tick(MorController c,LivingEntity target,int t,int s){if(s==3&&MorRules.release(s,t)){c.burst(c.mob.position(),12,6);for(Player p:c.players(6))if(c.damage(p,MorRules.noon(c.mob.level().dimension()==Level.OVERWORLD,c.mob.level().getDayTime())?3:2))p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS,30));}
if(s==4){if(t==14)c.aim=target.position().subtract(c.mob.position()).multiply(1,0,1).normalize();if(t>=14&&t<19){if(c.ground(c.mob.position().add(c.aim.scale(.8))))c.safeVelocity(c.aim.scale(.8));else c.mob.setDeltaMovement(Vec3.ZERO);}if(MorRules.release(s,t)){c.mob.setDeltaMovement(Vec3.ZERO);c.swing(target,3,2.5,.574F,t==25);if(t==25)c.cooldown[4]=220;}}}
 private MorPoludnitsa(){}
}
