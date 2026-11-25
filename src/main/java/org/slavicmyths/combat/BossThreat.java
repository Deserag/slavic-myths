package org.slavicmyths.combat;

import java.util.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.entity.living.*;

/** Per-encounter, server-only threat. Weak keys release unloaded bosses; entries expire locally. */
@EventBusSubscriber(modid="slavicmyths")
public final class BossThreat {
 private static final Map<Mob,Encounter> ENCOUNTERS=new WeakHashMap<>();
 private static final class Threat {double score; long active,last; Threat(long now){active=last=now;} }
 private static final class Encounter {final Map<UUID,Threat> players=new HashMap<>();UUID focus;long switched,diverted;}
 private static final net.minecraft.tags.TagKey<EntityType<?>> TYPES=net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","boss_threat"));
 private static boolean boss(Mob b){return b.isAlive()&&!b.level().isClientSide&&(b.getType().is(TYPES)||b.getType().is(CombatTargeting.BOSS)||org.slavicmyths.mor.HigherMor.higher(b))&&CombatTargeting.bossMode(b);}
 private static boolean valid(Mob b,Player p){return (!(b instanceof org.slavicmyths.icequeen.IceQueen q)||q.participant(p))&&p!=null&&p.isAlive()&&!p.isCreative()&&!p.isSpectator()&&p.level()==b.level()&&b.distanceToSqr(p)<=48*48&&CombatTargeting.canAttack(b,p,CombatTargeting.Context.ACQUIRE);}
 private static Threat entry(Encounter e,Player p,long now){var t=e.players.computeIfAbsent(p.getUUID(),id->new Threat(now));decay(t,now);return t;}
 private static void decay(Threat t,long now){t.score*=Math.pow(.5,Math.max(0,now-t.last)/160D);t.last=now;}
 public static boolean attacking(Mob b){
  if(b instanceof org.slavicmyths.desert.SandKingEntity s)return s.action()!=org.slavicmyths.desert.SandKingRules.IDLE;
  if(b.swinging)return true;
  if(b instanceof org.slavicmyths.icequeen.IceQueen q)return q.action()!=org.slavicmyths.icequeen.IceQueen.IDLE;
  if(b instanceof org.slavicmyths.entity.LandSpiritEntity s&&s.morState()!=0)return s.morSkill()!=0;
  if(b instanceof org.slavicmyths.sisters.SisterBoss s)return s.action()!=0;
  if(b instanceof org.slavicmyths.sisters.LomeyaBoss s)return s.action()!=0;
  if(b instanceof org.slavicmyths.sisters.NeveyaBoss s)return s.action()!=0;
  if(b instanceof org.slavicmyths.sisters.ViyBoss s)return s.action()!=0;
  if(b instanceof org.slavicmyths.sisters.AgneyaBoss s)return s.attackPoseTicks(0)>=0;
  if(b instanceof org.slavicmyths.sisters.LedeyaBoss s)return s.attackTicks(0)>=0;
  if(b instanceof org.slavicmyths.posad.boss.PosadBoss s)return s.move()!=org.slavicmyths.posad.boss.PosadBoss.Move.IDLE;
  if(b instanceof org.slavicmyths.netherboss.NetherSummonBoss s)return s.action()!=0;
  if(b instanceof org.slavicmyths.marine.StratimBirdEntity s)return s.action()!=0;
  if(b instanceof org.slavicmyths.marine.OceanZmeyEntity s)return s.action()!=0;
  if(b instanceof org.slavicmyths.marine.DeepSeaSerpentEntity s)return s.action()!=0;
  if(b instanceof org.slavicmyths.marine.TsarPikeEntity s)return s.action()!=0;
  if(b instanceof org.slavicmyths.marine.PharaohkaEntity s)return s.action()!=0;
  if(b instanceof org.slavicmyths.marine.ZhelvaEntity s)return s.action()!=0;
  if(b instanceof org.slavicmyths.morozko.Morozko s)return s.action()!=0;
  if(b instanceof org.slavicmyths.karachun.Karachun s)return s.action()!=0;
  if(b instanceof org.slavicmyths.morovoy.MorovoyFireZmey s)return s.action()!=0;
  if(b instanceof org.slavicmyths.bandit.NightingaleEntity s)return s.action()!=0;
  if(b instanceof org.slavicmyths.skim.SkimBeastEntity s)return s.action()!=org.slavicmyths.skim.SkimAction.IDLE;
  if(b instanceof org.slavicmyths.marinka.MarinkaEntity s)return s.action()!=org.slavicmyths.marinka.WitchAction.IDLE;
  if(b instanceof org.slavicmyths.finist.KartausEntity s)return s.action()!=org.slavicmyths.finist.FalconAction.IDLE;
  if(b instanceof org.slavicmyths.earth.EarthSpirit s)return s.action()!=org.slavicmyths.earth.EarthRules.Action.IDLE;
  return false;
 }
 @SubscribeEvent public static void damage(LivingDamageEvent.Post event){
  if(!(event.getEntity() instanceof Mob b)||!boss(b)||event.getNewDamage()<=0)return;
  Entity source=event.getSource().getEntity();Player p=source instanceof Player player?player:null;
  if(p==null&&source instanceof LivingEntity living){UUID owner=CombatTargeting.affiliation(living);if(owner!=null&&b.level() instanceof net.minecraft.server.level.ServerLevel w)p=w.getServer().getPlayerList().getPlayer(owner);}
  if(!valid(b,p))return;
  long now=b.level().getGameTime();var e=ENCOUNTERS.computeIfAbsent(b,x->new Encounter());var t=entry(e,p,now);
  t.score=Math.min(100000,t.score+event.getNewDamage());t.active=now;
 }
 @SubscribeEvent public static void tick(EntityTickEvent.Post event){
  if(!(event.getEntity() instanceof Mob b)||b.level().isClientSide)return;
  if(!boss(b)){ENCOUNTERS.remove(b);return;}
  if(b.tickCount%10!=0||!(b.getTarget() instanceof Player current))return;
  long now=b.level().getGameTime();var e=ENCOUNTERS.computeIfAbsent(b,x->new Encounter());
  var currentThreat=entry(e,current,now);
  if(e.focus==null){e.focus=current.getUUID();e.switched=e.diverted=now;currentThreat.score=Math.max(12,currentThreat.score);}
  if(valid(b,current)&&b.distanceToSqr(current)<=36&&b.hasLineOfSight(current)){
   currentThreat.score=Math.min(100000,currentThreat.score+(current.isBlocking()?5:2));currentThreat.active=now;
  }
  e.players.entrySet().removeIf(row->{var p=b.level().getPlayerByUUID(row.getKey());return !valid(b,p)||now-row.getValue().active>400;});
  if(attacking(b)||now-e.switched<60)return;
  Player best=null,alternate=null;double highest=-1,other=-1;
  for(var row:e.players.entrySet()){
   var p=b.level().getPlayerByUUID(row.getKey());if(!valid(b,p)||!b.hasLineOfSight(p))continue;
   var t=row.getValue();decay(t,now);
   if(t.score>highest){highest=t.score;best=p;}
   if(p!=current&&now-t.active<=120&&b.distanceToSqr(p)>36&&t.score>other){other=t.score;alternate=p;}
  }
  boolean diversion=now-e.diverted>=240&&alternate!=null&&other>=12;
  if(diversion){best=alternate;e.diverted=now;}
  if(best!=null&&best!=current&&(diversion||!valid(b,current)||highest>Math.max(12,currentThreat.score*1.3+5))){
   e.focus=best.getUUID();e.switched=now;b.setTarget(best);
  }
 }
 @SubscribeEvent(priority=EventPriority.HIGH) public static void changing(LivingChangeTargetEvent event){
  if(!(event.getEntity() instanceof Mob b)||!boss(b))return;
  var e=ENCOUNTERS.get(b);if(e==null||e.focus==null||!(b.getTarget() instanceof Player current)||!valid(b,current))return;
  var proposed=event.getNewAboutToBeSetTarget();
  if(proposed==null||!(proposed instanceof Player))return; // Preserve disengagement and retaliation against mobs.
  if(attacking(b)){event.setNewAboutToBeSetTarget(current);return;}
  var focus=b.level().getPlayerByUUID(e.focus);
  if(valid(b,focus)&&b.hasLineOfSight(focus))event.setNewAboutToBeSetTarget(focus);
 }
 private BossThreat(){}
}