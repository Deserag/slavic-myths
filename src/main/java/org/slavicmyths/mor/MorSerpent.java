package org.slavicmyths.mor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.*;
/** Individual move set; common scheduler owns focus, persistence and non-overlap. */
final class MorSerpent {
 static int choose(MorController c,LivingEntity target,double d,boolean sight){int skill=0;if(MorRules.clonesNeeded(c.mob.morClone(),c.clonesSummoned,c.mob.getHealth(),c.mob.getMaxHealth()))skill=9;else if(d>=5&&d<=14&&c.cooldown[7]==0&&sight)skill=7;else if(d<=12&&c.cooldown[8]==0&&sight)skill=8; return skill;}
 static void tick(MorController c,LivingEntity target,int t,int s){if(s==7&&MorRules.release(s,t))c.projectile(target,false,c.mob.morClone()?2:3);
if(s==7&&t==33)c.cooldown[7]=180;
if(s==8){if(MorRules.release(s,t)){c.safeVelocity(c.aim.scale(.7));if(!c.diveHit&&c.mob.getBoundingBox().inflate(.3).intersects(target.getBoundingBox())&&c.sight(target)){c.diveHit=c.damage(target,c.mob.morClone()?3:6);}}if(t>=29&&t<=40)c.safeVelocity(new Vec3(0,Math.min(.2,Math.max(0,c.origin.y-c.mob.getY())),0));}
if(s==9){if(t==11||t==21)c.burst(c.mob.position().add(2,1,0),8,.8);if(MorRules.release(s,t)){c.spawnClones();for(var e:c.localClones())e.mor.shuffle();c.shuffle();}}}
 private MorSerpent(){}
}
