package org.slavicmyths.underground;
import java.util.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.rpg.Runes;
@EventBusSubscriber(modid="slavicmyths")
public final class OminousAura {
 public static final TagKey<EntityType<?>> IMMUNE=TagKey.create(Registries.ENTITY_TYPE,UndergroundMobs.id("ominous_aura_immune"));
 public static int active(Player p){int n=0;for(ItemStack s:p.getArmorSlots())if(Runes.has(s,"ominous_aura"))n++;if(Runes.has(p.getMainHandItem(),"ominous_aura"))n++;if(Runes.has(p.getOffhandItem(),"ominous_aura"))n++;return n;}
 public static boolean ignores(Mob mob,Player p){if(!(mob instanceof net.minecraft.world.entity.monster.Enemy)||mob instanceof RoomBoss||mob instanceof org.slavicmyths.industry.ChudPerson||mob.getType().is(IMMUNE)||mob.getMaxHealth()>50||active(p)<2)return false;var prov=mob.getPersistentData().getCompound("OminousProvocation");String id=p.getUUID().toString();boolean provoked=prov.contains(id)&&(mob.level().getGameTime()-prov.getLong(id)<=1800||mob.getTarget()==p);return EncounterRules.protectedByRunes(mob.getMaxHealth(),active(p),false,provoked);}
 @SubscribeEvent public static void target(LivingChangeTargetEvent e){if(e.getEntity() instanceof Mob m&&e.getNewAboutToBeSetTarget() instanceof Player p&&ignores(m,p))e.setNewAboutToBeSetTarget(null);}
 @SubscribeEvent public static void provoke(LivingDamageEvent.Post e){if(e.getNewDamage()<=0||!(e.getEntity() instanceof Mob m)||m.level().isClientSide)return;Entity source=e.getSource().getEntity();Player p=source instanceof Player a?a:source instanceof TamableAnimal t&&t.getOwner() instanceof Player a?a:null;if(p==null)return;var root=m.getPersistentData();var c=root.getCompound("OminousProvocation");long now=m.level().getGameTime();for(String id:new ArrayList<>(c.getAllKeys()))if(now-c.getLong(id)>1800)c.remove(id);c.putLong(p.getUUID().toString(),now);root.put("OminousProvocation",c);}
 public static float exhaustion(Player p,float amount){if(amount<=0||p.level().isClientSide||p.isCreative()||p.isSpectator()||!p.isAlive())return amount;return p.level().getEntitiesOfClass(LargePodzemnik.class,p.getBoundingBox().inflate(12),b->b.auraActive()&&b.distanceToSqr(p)<=144).isEmpty()?amount:EncounterRules.exhaustion(amount,true);}
 public static boolean knocked(LivingEntity p){return p.hasEffect(UndergroundMobs.KNOCKDOWN);}
 public static void knock(Player p,LargePodzemnik owner){long now=p.level().getGameTime();var n=p.getPersistentData();if(p.isPassenger()||p.isCreative()||p.isSpectator()||n.getLong("UndergroundControlImmune")>now)return;n.putLong("UndergroundControlImmune",now+52);n.putUUID("UndergroundControlOwner",owner.getUUID());n.putString("UndergroundControlDimension",p.level().dimension().location().toString());p.addEffect(new MobEffectInstance(UndergroundMobs.KNOCKDOWN,20,0,false,false,true));if(p.getForcedPose()==null){p.setForcedPose(Pose.SWIMMING);n.putBoolean("UndergroundForcedPose",true);}}
 @SubscribeEvent public static void tick(PlayerTickEvent.Post e){Player p=e.getEntity();if(p.level().isClientSide)return;var n=p.getPersistentData();if(knocked(p)){boolean valid=n.hasUUID("UndergroundControlOwner")&&p.level() instanceof ServerLevel w&&w.getEntity(n.getUUID("UndergroundControlOwner")) instanceof LargePodzemnik b&&b.isAlive()&&n.getString("UndergroundControlDimension").equals(w.dimension().location().toString());if(!valid||!p.isAlive()||p.isPassenger())clear(p);else p.setDeltaMovement(p.getDeltaMovement().multiply(.05,1,.05));}else if(n.contains("UndergroundControlOwner"))clear(p);}
 @SubscribeEvent public static void attack(AttackEntityEvent e){if(knocked(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void mine(PlayerInteractEvent.LeftClickBlock e){if(knocked(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void item(PlayerInteractEvent.RightClickItem e){if(knocked(e.getEntity()))e.setCanceled(true);}
 @SubscribeEvent public static void directAttack(LivingIncomingDamageEvent e){if(e.getSource().getEntity() instanceof Player p&&e.getSource().getDirectEntity()==p&&knocked(p))e.setCanceled(true);}
 @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent e){clear(e.getEntity());}
 @SubscribeEvent public static void login(PlayerEvent.PlayerLoggedInEvent e){clear(e.getEntity());}
 @SubscribeEvent public static void respawn(PlayerEvent.PlayerRespawnEvent e){clear(e.getEntity());}
 private static void clear(Player p){var n=p.getPersistentData();p.removeEffect(UndergroundMobs.KNOCKDOWN);if(n.getBoolean("UndergroundForcedPose")&&p.getForcedPose()==Pose.SWIMMING)p.setForcedPose(null);n.remove("UndergroundForcedPose");n.remove("UndergroundControlOwner");n.remove("UndergroundControlDimension");}
 private OminousAura(){}
}
