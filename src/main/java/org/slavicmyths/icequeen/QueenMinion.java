package org.slavicmyths.icequeen;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.slavicmyths.morozko.*;
public final class QueenMinion extends net.minecraft.world.entity.monster.Monster {
 private final QueenContent.Kind kind; private UUID owner; private int orphans,attack;
 public QueenMinion(EntityType<? extends QueenMinion> t,Level w,QueenContent.Kind k){super(t,w);kind=k;xpReward=0;setPersistenceRequired();if(k==QueenContent.Kind.OWL)setNoGravity(true);}
 public static AttributeSupplier.Builder attributes(QueenContent.Kind k){return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH,k.hp).add(Attributes.ATTACK_DAMAGE,k.damage).add(Attributes.ARMOR,k==QueenContent.Kind.MELEE_COPY?8:k==QueenContent.Kind.RANGED_COPY?4:0).add(Attributes.MOVEMENT_SPEED,k==QueenContent.Kind.LARGE_GOLEM?.22:.31).add(Attributes.FOLLOW_RANGE,52);}
 public QueenContent.Kind kind(){return kind;}
 public void bind(UUID id){owner=id;setHealth(getMaxHealth());}
 public boolean ownedBy(IceQueen b){return b.getUUID().equals(owner);}
 @Override protected void registerGoals(){goalSelector.addGoal(0,new FloatGoal(this));}
 @Override public boolean isAlliedTo(Entity e){return e instanceof IceQueen b&&ownedBy(b)||e instanceof QueenMinion m&&owner!=null&&owner.equals(m.owner)||super.isAlliedTo(e);}
 @Override public boolean hurt(net.minecraft.world.damagesource.DamageSource s,float a){return !(s.getEntity()!=null&&isAlliedTo(s.getEntity()))&&!s.is(net.minecraft.tags.DamageTypeTags.IS_FREEZING)&&super.hurt(s,a);}
 @Override public boolean removeWhenFarAway(double d){return false;}
 @Override public void tick(){super.tick();if(!(level() instanceof ServerLevel w))return;
  if(owner==null||!(w.getEntity(owner) instanceof IceQueen boss)||!boss.active()){if(++orphans>=20)discard();return;}orphans=0;
  Player p=boss.victim();if(p==null){getNavigation().stop();return;}if(distanceToSqr(boss)>3600){discard();return;}getLookControl().setLookAt(p,45,45);
  double d=distanceToSqr(p);
  if(kind==QueenContent.Kind.OWL){var target=p.getEyePosition().add(0,1,0);var v=target.subtract(position());setDeltaMovement(v.normalize().scale(.30));if(v.lengthSqr()<4&&tickCount%25==0&&hasLineOfSight(p)){if(p.hurt(damageSources().mobAttack(this),kind.damage))FrostCold.add(p,5);playSound(SoundEvents.PHANTOM_BITE,1,1.3F);}return;}
  if(kind==QueenContent.Kind.RANGED_COPY){if(d>144)getNavigation().moveTo(p,1);else getNavigation().stop();if(tickCount%45==0&&d<400&&hasLineOfSight(p)){FrostShard.launch(this,p.getEyePosition(),kind.damage,8,null);playSound(SoundEvents.SNOWBALL_THROW,1,.7F);}return;}
  double reach=kind==QueenContent.Kind.LARGE_GOLEM||kind.copy()?3.7:2.1;
  if(d>reach*reach){getNavigation().moveTo(p,1.12);attack=0;return;}getNavigation().stop();
  if(++attack==10){swing(net.minecraft.world.InteractionHand.MAIN_HAND);playSound(kind==QueenContent.Kind.WOLF?SoundEvents.WOLF_GROWL:kind==QueenContent.Kind.BEAR?SoundEvents.POLAR_BEAR_WARNING:SoundEvents.IRON_GOLEM_ATTACK,1,.9F);}
  if(attack==18&&hasLineOfSight(p)&&distanceToSqr(p)<=reach*reach&&p.hurt(damageSources().mobAttack(this),kind.damage))FrostCold.add(p,6);
  if(attack>=36)attack=0;
 }
}
