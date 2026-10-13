package org.slavicmyths.poloz;
import java.util.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.server.level.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.*;
import net.neoforged.neoforge.event.entity.*;
import net.neoforged.neoforge.event.entity.living.*;
import org.slavicmyths.entity.LandSpiritEntity;
@EventBusSubscriber(modid="slavicmyths")
public final class PolozEvents {
 @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST) public static void single(EntityJoinLevelEvent e){if(!(e.getLevel() instanceof ServerLevel w)||!(e.getEntity() instanceof GreatPoloz boss)||!boss.isAlive())return;var d=PolozEvent.get(w);if(w.dimension()!=net.minecraft.world.level.Level.OVERWORLD||d.running()&&!boss.getUUID().equals(d.boss)){e.setCanceled(true);return;}if(!d.running()){d.boss=boss.getUUID();d.event=UUID.randomUUID();d.spawn=boss.blockPosition();d.state=PolozEvent.State.ACTIVE;d.elapsed=180;d.setDirty();}}
 private static final Set<PathfinderMob> installed=Collections.newSetFromMap(new WeakHashMap<>());
 @SubscribeEvent public static void tick(ServerTickEvent.Post e){PolozEvent.get(e.getServer().overworld()).tick(e.getServer().overworld());}
 @SubscribeEvent public static void mob(EntityTickEvent.Post e){if(!(e.getEntity().level() instanceof ServerLevel w))return;var n=e.getEntity().getPersistentData();if(n.hasUUID("summonedByPolozUUID")&&e.getEntity().tickCount%20==0){var data=PolozEvent.get(w);if(!data.running()||data.boss==null||!data.boss.equals(n.getUUID("summonedByPolozUUID"))){if(!n.contains("PolozRemoveAt"))n.putLong("PolozRemoveAt",w.getGameTime()+100);}if(n.contains("PolozRemoveAt")&&w.getGameTime()>=n.getLong("PolozRemoveAt"))e.getEntity().discard();}}
 @SubscribeEvent public static void loot(LivingDropsEvent e){var m=e.getEntity();if(m.getPersistentData().hasUUID("summonedByPolozUUID")){e.setCanceled(true);return;}if(!(m instanceof LandSpiritEntity s)||s.morState()!=2||s.morClone()||!m.level().getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_DOMOBLOOT))return;
  net.minecraft.world.item.Item item=null;int count=1;int kind=s.mor.kind();if(kind==5)item=PolozContent.EYE.get();else if(kind==6)item=PolozContent.HEART.get();else if(kind>=1&&kind<=4){int chance=kind==3?60:70;if(kind==4||m.getRandom().nextInt(100)<chance){item=PolozContent.DUST.get();count=kind==4?2+m.getRandom().nextInt(3):1+m.getRandom().nextInt(2);}}
  if(item==PolozContent.DUST.get()&&e.getSource().getEntity() instanceof LivingEntity killer){var looting=m.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT).getHolderOrThrow(net.minecraft.world.item.enchantment.Enchantments.LOOTING);if(net.minecraft.world.item.enchantment.EnchantmentHelper.getItemEnchantmentLevel(looting,killer.getMainHandItem())>0)count+=m.getRandom().nextInt(2);}
  if(item!=null)e.getDrops().add(new net.minecraft.world.entity.item.ItemEntity(m.level(),m.getX(),m.getY(),m.getZ(),new net.minecraft.world.item.ItemStack(item,count)));
 }
 @SubscribeEvent public static void xp(LivingExperienceDropEvent e){if(e.getEntity().getPersistentData().hasUUID("summonedByPolozUUID"))e.setCanceled(true);}
 @SubscribeEvent public static void craft(net.neoforged.neoforge.event.entity.player.PlayerEvent.ItemCraftedEvent e){if(e.getEntity() instanceof ServerPlayer p&&e.getCrafting().is(PolozContent.HORN.get()))org.slavicmyths.tales.TaleService.promote(p,"slavicmyths:mor_horn",3,false);}
 @SubscribeEvent public static void damaged(LivingDamageEvent.Post e){if(e.getNewDamage()>0&&e.getEntity() instanceof ServerPlayer p&&p.isUsingItem()&&p.getUseItem().is(PolozContent.HORN.get()))p.stopUsingItem();}
 @SubscribeEvent public static void join(EntityJoinLevelEvent e){if(e.getLevel().isClientSide||!(e.getEntity() instanceof PathfinderMob mob)||mob instanceof GreatPoloz||!installed.add(mob))return;
  boolean fighter=mob instanceof Enemy||mob instanceof net.minecraft.world.entity.animal.IronGolem||org.slavicmyths.kalin.KalinParty.owner(mob)!=null||mob instanceof TamableAnimal pet&&pet.isTame()&&mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)!=null;
  if(fighter){mob.targetSelector.addGoal(0,new PolozTarget(mob));if(org.slavicmyths.kalin.KalinParty.owner(mob)==null&&!(mob instanceof net.minecraft.world.entity.monster.RangedAttackMob))mob.goalSelector.addGoal(0,new BodyMelee(mob));}
  else if(mob instanceof Animal||mob instanceof AbstractVillager)mob.goalSelector.addGoal(0,new BodyFlee(mob));
 }
 private static final class BodyMelee extends Goal {
  private final PathfinderMob actor;private int swingAt;
  BodyMelee(PathfinderMob m){actor=m;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
  @Override public boolean canUse(){return actor.getTarget() instanceof GreatPoloz p&&p.isAlive()&&(!(actor instanceof LandSpiritEntity s)||s.morState()==0);}
  @Override public boolean canContinueToUse(){return canUse();}
  @Override public void tick(){var boss=(GreatPoloz)actor.getTarget();Entity nearest=boss;double best=boss.getBoundingBox().distanceToSqr(actor.getEyePosition());for(var part:boss.parts)if(!part.scar){double d=part.getBoundingBox().distanceToSqr(actor.getEyePosition());if(d<best){best=d;nearest=part;}}actor.getLookControl().setLookAt(nearest.getX(),nearest.getY()+1,nearest.getZ());if(best<9&&actor.tickCount>=swingAt){actor.swing(net.minecraft.world.InteractionHand.MAIN_HAND);actor.doHurtTarget(boss);swingAt=actor.tickCount+20;}else if(actor.tickCount%20==0){Vec3 at=nearest.position();Vec3 away=actor.position().subtract(at).multiply(1,0,1).normalize().scale(nearest.getBbWidth()/2+.8);at=at.add(away);actor.getNavigation().moveTo(at.x,at.y,at.z,1);}}
 }
 private static final class BodyFlee extends Goal {
  private final PathfinderMob actor;private GreatPoloz boss;private int next;
  BodyFlee(PathfinderMob m){actor=m;setFlags(EnumSet.of(Flag.MOVE));}
  @Override public boolean canUse(){if(actor.tickCount<next||!(actor.level() instanceof ServerLevel w))return false;next=actor.tickCount+20;boss=PolozEvent.get(w).loaded(w);return boss!=null&&boss.isAlive()&&boss.nearestDistance(actor.position())<28*28;}
  @Override public boolean canContinueToUse(){return boss!=null&&boss.isAlive()&&boss.nearestDistance(actor.position())<28*28;}
  @Override public void start(){run();}
  @Override public void tick(){if(actor.tickCount%20==0)run();}
  private void run(){Entity nearest=boss;double best=actor.distanceToSqr(boss);for(var part:boss.parts)if(actor.distanceToSqr(part)<best){best=actor.distanceToSqr(part);nearest=part;}Vec3 away=actor.position().subtract(nearest.position()).multiply(1,0,1).normalize().scale(16);var at=actor.position().add(away);actor.getNavigation().moveTo(at.x,at.y,at.z,1.4);}
 }
 private static final class PolozTarget extends Goal {
  private final PathfinderMob actor;private GreatPoloz boss;private LivingEntity old;private int next;
  PolozTarget(PathfinderMob m){actor=m;setFlags(EnumSet.of(Flag.TARGET));}
  @Override public boolean canUse(){if(actor.tickCount<next)return false;next=actor.tickCount+20;if(!(actor.level() instanceof ServerLevel w)||actor instanceof LandSpiritEntity s&&s.morState()!=0||actor.getPersistentData().hasUUID("summonedByPolozUUID"))return false;boss=PolozEvent.get(w).loaded(w);return boss!=null&&boss.isAlive()&&boss.nearestDistance(actor.position())<48*48;}
  @Override public boolean canContinueToUse(){return boss!=null&&boss.isAlive()&&boss.nearestDistance(actor.position())<48*48&&(!(actor instanceof LandSpiritEntity s)||s.morState()==0);}
  @Override public void start(){old=actor.getTarget();actor.setTarget(boss);}
  @Override public void tick(){actor.setTarget(boss);}
  @Override public void stop(){if(actor.getTarget()==boss)actor.setTarget(old!=null&&old.isAlive()?old:null);boss=null;}
 }
 private PolozEvents(){}
}
