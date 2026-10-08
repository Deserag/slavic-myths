package org.slavicmyths.military;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
/** Only bandit defense has village-wide reach; native golem targets/attributes stay unchanged. */
public final class VillageBanditTargetGoal extends NearestAttackableTargetGoal<Mob> {
 public VillageBanditTargetGoal(IronGolem golem){super(golem,Mob.class,5,true,false,e->e.getType().is(Military.BANDITS));}
 @Override protected double getFollowDistance(){return Military.PATROL_RADIUS;}
}
