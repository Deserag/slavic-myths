package org.slavicmyths.mor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.*;
/** Individual move set; common scheduler owns focus, persistence and non-overlap. */
final class MorLeshy {
 static int choose(MorController c,LivingEntity target,double d,boolean sight){int skill=0;if(d<=4&&c.cooldown[2]==0&&sight)skill=2;else if(d<=9&&c.cooldown[1]==0&&sight&&c.ground(target.position()))skill=1; return skill;}
 static void tick(MorController c,LivingEntity target,int t,int s){if(s==1){if(t==1||t==9||t==17)c.rootCue(false);if(MorRules.release(s,t)){c.rootCue(true);boolean hit=false;for(int i=-1;i<=1;i++){Vec3 p=c.mark.add(i,0,i);if(!hit&&c.ground(p)&&target.getBoundingBox().inflate(.15).intersects(new AABB(p.x-.25,p.y,p.z-.25,p.x+.25,p.y+1.3,p.z+.25))&&c.sight(target)){hit=c.damage(target,4);if(hit)c.hold(target,30);}}}}
if(s==2&&MorRules.release(s,t)){c.burst(c.mob.position(),16,5);for(Player p:c.players(5))if(c.damage(p,3))p.knockback(1.1,c.mob.getX()-p.getX(),c.mob.getZ()-p.getZ());}}
 private MorLeshy(){}
}
