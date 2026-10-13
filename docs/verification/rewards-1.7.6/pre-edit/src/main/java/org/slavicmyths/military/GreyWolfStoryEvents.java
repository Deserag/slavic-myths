package org.slavicmyths.military;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.*;
import net.neoforged.neoforge.event.entity.living.*;
import org.slavicmyths.finist.*;
@net.neoforged.fml.common.EventBusSubscriber(modid="slavicmyths")
public final class GreyWolfStoryEvents {
 public static ServerPlayer owner(net.minecraft.world.damagesource.DamageSource s,ServerLevel w){if(s.getEntity() instanceof ServerPlayer p)return p;if(s.getEntity() instanceof org.slavicmyths.entity.WildlifeEntity wolf)return wolf.ringOwner();return null;}
 @SubscribeEvent public static void damaged(LivingDamageEvent.Post e){if(!(e.getEntity().level() instanceof ServerLevel w)||e.getNewDamage()<=0)return;var victim=e.getEntity();var p=owner(e.getSource(),w);if(p!=null){if(victim instanceof org.slavicmyths.kurgan.KurganCreature k&&k.kurgan!=null){var records=org.slavicmyths.kurgan.BurialRecords.get(w);var site=records.instances.get(k.kurgan);if(site!=null&&GreyWolfStory.mainBoss(site,k)&&site.bossContributors.add(p.getUUID()))records.setDirty();}var d=victim.getPersistentData();d.putUUID("WolfStoryKiller",p.getUUID());d.putLong("WolfStoryDamageAt",w.getGameTime());GreyPackRing.attacked(p,victim);}if(victim instanceof ServerPlayer player&&e.getSource().getEntity() instanceof LivingEntity attacker)GreyPackRing.attacked(player,attacker);}
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void death(LivingDeathEvent e){if(!(e.getEntity().level() instanceof ServerLevel w))return;var victim=e.getEntity();var d=victim.getPersistentData();if(d.getBoolean("WolfStoryDeathCredited"))return;var p=owner(e.getSource(),w);if(p==null&&d.hasUUID("WolfStoryKiller")&&w.getGameTime()-d.getLong("WolfStoryDamageAt")<=200)p=(ServerPlayer)w.getPlayerByUUID(d.getUUID("WolfStoryKiller"));if(p!=null){d.putBoolean("WolfStoryDeathCredited",true);GreyWolfStory.killed(p,victim);}}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.PlayerTickEvent.Post e){if(e.getEntity() instanceof ServerPlayer p){GreyPackRing.tick(p);if(p.tickCount%200==0&&(GreyWolfStory.stage(p,1)==GreyWolfStory.Stage.ACTIVE||GreyWolfStory.stage(p,2)==GreyWolfStory.Stage.ACTIVE)){GreyWolfStory.refreshTarget(p);org.slavicmyths.wolf.WolfStoryNetwork.changed(p);}}}
 @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p){org.slavicmyths.wolf.WolfStoryNetwork.clear(p);GreyPackRing.data(p).remove("SpawnAt");}}
 @SubscribeEvent public static void drops(LivingDropsEvent e){if(e.getEntity().getPersistentData().getBoolean("GreyPackAlly"))e.getDrops().clear();}
 @SubscribeEvent public static void xp(LivingExperienceDropEvent e){if(e.getEntity().getPersistentData().getBoolean("GreyPackAlly"))e.setDroppedExperience(0);}
 @SubscribeEvent public static void removed(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent e){if(!(e.getLevel() instanceof ServerLevel w)||!(e.getEntity() instanceof org.slavicmyths.hunt.WorldBoss b)||b.kind()!=org.slavicmyths.hunt.BossKind.LIKHO||b.getRemovalReason()!=Entity.RemovalReason.DISCARDED||b.encounter.isEmpty())return;var records=org.slavicmyths.hunt.HuntRecords.get(w);records.finishBoss(b.encounter,b.getUUID(),w.getGameTime());}
 private GreyWolfStoryEvents(){}
}
