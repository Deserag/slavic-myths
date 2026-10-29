package org.slavicmyths.burningislands;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import org.slavicmyths.rpg.Runes;

/** Post-damage only; native forge components. No terrain writes without durable provenance. */
@EventBusSubscriber(modid="slavicmyths")
public final class MagmaRunes {
 private static final ThreadLocal<Boolean> REFLECTING=ThreadLocal.withInitial(()->false);
 public static boolean rune(String id){return MagmaRuneRules.rune(id);}
 public static boolean supports(ItemStack s){return s.getItem() instanceof ArmorItem a&&a.getType()==ArmorItem.Type.CHESTPLATE;}
 public static boolean conflict(java.util.List<String> ids,String id){return MagmaRuneRules.conflict(ids,id);}
 @SubscribeEvent public static void damage(LivingDamageEvent.Post e){
  var wearer=e.getEntity();if(wearer.level().isClientSide||REFLECTING.get()||e.getSource().is(net.minecraft.world.damagesource.DamageTypes.THORNS)||e.getNewDamage()<=0||!wearer.isAlive())return;
  if(!(e.getSource().getEntity() instanceof LivingEntity attacker)||attacker==wearer||attacker.level()!=wearer.level()||!attacker.isAlive()||wearer.isAlliedTo(attacker)||attacker.getType().is(org.slavicmyths.combat.CombatTargeting.PROTECTED))return;
  if(attacker instanceof Player p&&(p.isCreative()||p.isSpectator()))return;
  if(wearer instanceof Player p&&attacker instanceof Player other&&!p.canHarmPlayer(other))return;
  var chest=wearer.getItemBySlot(EquipmentSlot.CHEST);int tier=MagmaRuneRules.tier(Runes.has(chest,"magma_1"),Runes.has(chest,"magma_2"));
  if(tier==0||!supports(chest))return;
  var data=wearer.getPersistentData();long now=((net.minecraft.server.level.ServerLevel)wearer.level()).getServer().overworld().getGameTime();
  if(!MagmaRuneRules.ready(tier,e.getNewDamage(),true,REFLECTING.get(),now,data.getLong("MagmaRuneReady")))return;
  data.putLong("MagmaRuneReady",now+40);
  REFLECTING.set(true);
  try {if(attacker.hurt(wearer.damageSources().thorns(wearer),MagmaRuneRules.damage(tier))&&!attacker.fireImmune()&&!attacker.hasEffect(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE))attacker.igniteForSeconds(MagmaRuneRules.damage(tier));}finally{REFLECTING.remove();}
  if(tier==2&&wearer instanceof net.minecraft.server.level.ServerPlayer player)TemporaryTerrain.magma(player);
 }
 private MagmaRunes(){}
}
