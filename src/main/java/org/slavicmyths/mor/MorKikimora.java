package org.slavicmyths.mor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.*;
/** Individual move set; common scheduler owns focus, persistence and non-overlap. */
final class MorKikimora {
 static int choose(MorController c,LivingEntity target,double d,boolean sight){int skill=0;if(d>=1.5&&d<=6&&c.cooldown[5]==0&&sight)skill=5;else if(d<=6&&c.cooldown[6]==0&&sight)skill=6; return skill;}
 static void tick(MorController c,LivingEntity target,int t,int s){if(s==5){if(t==10||t==16||t==22){Vec3 side=new Vec3(-c.aim.z,0,c.aim.x).scale(t==16?-2.3:2.3);if(t==22)side=target.position().subtract(c.mob.position()).multiply(1,0,1);if(side.lengthSqr()>9)side=side.normalize().scale(2.5);c.safeStep(side,true);}if(MorRules.release(s,t))c.swing(target,4,1.75,.643F,false);}
if(s==6&&MorRules.release(s,t))c.projectile(target,true,2);}
 private MorKikimora(){}
}
