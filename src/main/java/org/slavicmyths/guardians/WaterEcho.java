package org.slavicmyths.guardians;
import java.util.UUID;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
public final class WaterEcho extends PathfinderMob implements Enemy {
 public UUID keeper;private int life=GuardianRules.ECHO_LIFE;
 public WaterEcho(EntityType<? extends WaterEcho> t,Level w){super(t,w);xpReward=0;}
 public static AttributeSupplier.Builder attributes(){return createMobAttributes().add(Attributes.MAX_HEALTH,GuardianRules.ECHO_HP).add(Attributes.MOVEMENT_SPEED,.27).add(Attributes.ATTACK_DAMAGE,GuardianRules.ECHO_DAMAGE).add(Attributes.FOLLOW_RANGE,16);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
 public void configure(SpringKeeper boss){keeper=boss.getUUID();}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;var master=keeper==null?null:w.getEntity(keeper);if(!(master instanceof SpringKeeper b)||!b.fighting()||--life<=0||!b.arena().contains(position())){discard();return;}if(tickCount%10==0){var t=b.getTarget();if(t!=null&&b.candidate(t)&&org.slavicmyths.combat.CombatTargeting.canAttack(this,t,org.slavicmyths.combat.CombatTargeting.Context.AREA)){getLookControl().setLookAt(t);getNavigation().moveTo(t,1);if(distanceToSqr(t)<36&&hasLineOfSight(t)&&tickCount%40==0){w.sendParticles(net.minecraft.core.particles.ParticleTypes.SPLASH,t.getX(),t.getY()+1,t.getZ(),6,.2,.3,.2,.02);t.hurt(damageSources().mobAttack(this),GuardianRules.ECHO_DAMAGE);}}}}
}
