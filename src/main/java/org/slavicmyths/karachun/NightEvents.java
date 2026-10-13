package org.slavicmyths.karachun;
import java.util.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
@EventBusSubscriber(modid="slavicmyths")
public final class NightEvents {
 @SubscribeEvent(priority=EventPriority.LOWEST) public static void clamp(LivingDamageEvent.Pre e){if(e.getEntity() instanceof Karachun b&&b.fighting())e.setNewDamage(NightRules.clamp(b.getHealth(),b.getMaxHealth(),e.getNewDamage()));}
 @SubscribeEvent public static void friendly(LivingIncomingDamageEvent e){if(e.getSource().getEntity() instanceof NightRaven r&&!r.allowed(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void damage(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||e.getEntity().level().isClientSide)return;var from=e.getSource().getEntity();if(from==null)return;UUID owner=NightOwners.owner(from);if(e.getEntity() instanceof Karachun b&&b.fighting()&&owner!=null){b.credit(owner,e.getNewDamage(),false);if(from instanceof LivingEntity l)NightStatus.reduceFear(l,b.session);}if(from instanceof LivingEntity l&&NightCombat.valid(e.getEntity(),l)&&org.slavicmyths.rpg.Runes.has(l.getMainHandItem(),"waning")){var t=e.getEntity();var n=t.getPersistentData();long now=t.level().getGameTime();if(now>=n.getLong("Night192RuneReady")){n.putLong("Night192RuneReady",now+20);if(l.getRandom().nextDouble()<NightConfig.value("runeChance"))NightStatus.add(t,l.getUUID(),NightConfig.value("runeGain"),0,false);}}}
 @SubscribeEvent public static void attack(AttackEntityEvent e){if(e.getEntity() instanceof ServerPlayer p&&p.getMainHandItem().is(NightContent.SCYTHE.get())){e.setCanceled(true);NightCombat.action(p,0);}}
 @SubscribeEvent public static void offhand(PlayerInteractEvent.RightClickItem e){if(e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightStatus.offhand(e.getEntity())){e.setCanceled(true);notice(e.getEntity());}}
 @SubscribeEvent public static void block(PlayerInteractEvent.RightClickBlock e){if(e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightStatus.offhand(e.getEntity())){e.setCanceled(true);notice(e.getEntity());}}
 @SubscribeEvent public static void entity(PlayerInteractEvent.EntityInteract e){if(e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightStatus.offhand(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void specific(PlayerInteractEvent.EntityInteractSpecific e){if(e.getHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightStatus.offhand(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void use(LivingEntityUseItemEvent.Start e){if(e.getEntity().getUsedItemHand()==net.minecraft.world.InteractionHand.OFF_HAND&&NightStatus.offhand(e.getEntity()))e.setCanceled(true);}
 private static void notice(net.minecraft.world.entity.player.Player p){if(p instanceof ServerPlayer s&&s.level().getGameTime()>=s.getPersistentData().getLong("Night192OffhandNotice")){s.getPersistentData().putLong("Night192OffhandNotice",s.level().getGameTime()+40);s.displayClientMessage(NightContent.text("offhand"),true);NightContent.sound(s,"waning_stage_up");}}
 @SubscribeEvent public static void tick(net.neoforged.neoforge.event.tick.EntityTickEvent.Post e){if(e.getEntity() instanceof LivingEntity l)NightStatus.tick(l);if(e.getEntity() instanceof ServerPlayer p){NightCombat.tick(p);if(p.tickCount%5!=0)return;var d=NightLedger.get(p.serverLevel());if(p.tickCount%20==0&&!d.player(p.getUUID()).getList("Pending",10).isEmpty())d.collect(p);boolean show=p.getPersistentData().contains(NightStatus.KEY)||p.getMainHandItem().is(NightContent.SCYTHE.get())||!org.slavicmyths.item.FolkAccessoryItem.equipped(p,NightContent.WING.get()).isEmpty();if(show||p.getPersistentData().getBoolean("Night192HUD"))NightNetwork.send(p);p.getPersistentData().putBoolean("Night192HUD",show);}}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){if(e.getEntity() instanceof ServerPlayer p){leave(p);var d=NightLedger.get(p.serverLevel());d.collect(p);d.progress(p);NightNetwork.send(p);}}
 public static void leave(ServerPlayer p){for(String key:new ArrayList<>(p.getPersistentData().getCompound("Night192Effects").getAllKeys()))try{NightEffects.clear(p,UUID.fromString(key));}catch(IllegalArgumentException ignored){}NightStatus.clearAll(p);NightCombat.cancel(p);p.getPersistentData().remove("Night192Casts");for(var r:p.serverLevel().getEntitiesOfClass(NightRaven.class,p.getBoundingBox().inflate(64),r->p.getUUID().equals(r.owner)))r.discard();}
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent e){if(e.getEntity() instanceof ServerPlayer p)leave(p);}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){if(e.getEntity() instanceof ServerPlayer p)leave(p);}
 @SubscribeEvent public static void death(LivingDeathEvent e){if(e.getEntity() instanceof ServerPlayer p)leave(p);}
 private NightEvents(){}
}
