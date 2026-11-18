package org.slavicmyths.netherboss;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.effect.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slavicmyths.rpg.Runes;

@EventBusSubscriber(modid="slavicmyths")
public final class NetherBossRunes {
 public static boolean rune(String id){return id.equals("morova_lingering")||id.equals("trizna_ember_ward");}
 public static boolean supports(ItemStack stack,String id){return id.equals("trizna_ember_ward")?stack.getItem() instanceof ArmorItem a&&a.getType()==ArmorItem.Type.CHESTPLATE:
  id.equals("morova_lingering")&&java.util.Set.of("sword","dagger","spear","heavy","staff","whip").contains(Runes.category(stack));}
 @SubscribeEvent public static void hit(LivingDamageEvent.Post e){
  if(e.getNewDamage()<=0||!(e.getSource().getEntity() instanceof ServerPlayer p)||e.getSource().getDirectEntity()!=p)return;
  var target=e.getEntity();var weapon=p.getMainHandItem();
  if(!supports(weapon,"morova_lingering")||!Runes.has(weapon,"morova_lingering")||!NetherTrophies.hostile(p,target))return;
  long now=p.server.overworld().getGameTime();var root=target.getPersistentData();var times=root.getCompound("MorovaLingeringCD");
  for(var effect:java.util.List.copyOf(target.getActiveEffects())){
   if(effect.isInfiniteDuration()||effect.getDuration()<=0||effect.getEffect().value().getCategory()!=MobEffectCategory.HARMFUL)continue;
   String key=BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value()).toString();
   if(times.getLong(key)>now)continue;
   int extra=Math.min(Math.min(100,effect.getDuration()/4),Integer.MAX_VALUE-effect.getDuration());if(extra<=0)continue;
   var updated=new MobEffectInstance(effect);updated.duration=effect.getDuration()+extra;
   if(target.addEffect(updated,p))times.putLong(key,now+200);
  }root.put("MorovaLingeringCD",times);
 }
 @SubscribeEvent public static void aura(PlayerTickEvent.Post e){
  if(!(e.getEntity() instanceof ServerPlayer p)||!p.isAlive()||p.isSpectator())return;
  var chest=p.getItemBySlot(EquipmentSlot.CHEST);if(!supports(chest,"trizna_ember_ward")||!Runes.has(chest,"trizna_ember_ward"))return;
  var root=org.slavicmyths.rpg.PathData.data(p);long now=p.server.overworld().getGameTime();
  if(now<root.getLong("TriznaAuraNext"))return;
  boolean combat=p.tickCount-p.getLastHurtByMobTimestamp()<=100&&p.getLastHurtByMob()!=null
   ||p.tickCount-p.getLastHurtMobTimestamp()<=100&&p.getLastHurtMob()!=null;
  if(!combat)return;root.putLong("TriznaAuraNext",now+40);boolean damaged=false;
  for(var target:p.serverLevel().getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(4),t->NetherTrophies.hostile(p,t)&&p.distanceToSqr(t)<=16&&p.hasLineOfSight(t)&&!t.fireImmune())){
   float before=target.getHealth();target.hurt(p.damageSources().onFire(),2);damaged|=target.getHealth()<before;
  }
  if(!damaged||now<root.getLong("TriznaHealNext"))return;
  var old=root.getLongArray("TriznaHealing");var recent=new java.util.ArrayList<Long>();for(long stamp:old)if(stamp>now-600&&stamp<=now)recent.add(stamp);
  if(recent.size()<6&&p.getHealth()<p.getMaxHealth()){p.heal(1);root.putLong("TriznaHealNext",now+100);recent.add(now);}
  root.putLongArray("TriznaHealing",recent);
 }
 private NetherBossRunes(){}
}
