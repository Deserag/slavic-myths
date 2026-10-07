package org.slavicmyths.rpg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
public final class RuneEffects {
 public static boolean forest(Player p){var biome=p.level().getBiome(p.blockPosition());return biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_FOREST)||biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_TAIGA)||biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_JUNGLE);}
 public static boolean day(Player p){return p.level().isDay()&&p.level().canSeeSky(p.blockPosition());}
 public static boolean dark(Player p){return !p.level().isDay()||p.level().getMaxLocalRawBrightness(p.blockPosition())<RuneDefinition.i("shadow","light_threshold");}
 private static boolean proc(Player p,String id,int ticks){return PathData.ready(p,"rune_"+id,PathData.has(p,"resonance")?(int)(ticks*.9):ticks);}
 public static void hit(Player p,LivingEntity target,ItemStack item,LivingIncomingDamageEvent e,boolean arrow,boolean first){
  String type=Runes.category(item);if(type.isEmpty())return;
  if(Runes.has(item,"thunder")&&(!type.equals("dagger")||first)&&proc(p,"thunder",RuneDefinition.i("thunder","hit_cooldown"))){
   net.minecraft.nbt.CompoundTag shock=new net.minecraft.nbt.CompoundTag();shock.putInt("Target",target.getId());shock.putUUID("UUID",target.getUUID());shock.putString("Dimension",p.level().dimension().location().toString());shock.putFloat("Damage",type.equals("heavy")?RuneDefinition.i("thunder","heavy_damage"):type.equals("dagger")?RuneDefinition.i("thunder","dagger_damage"):RuneDefinition.f("thunder","hit_damage"));p.getPersistentData().put("SlavicShock",shock);Abilities.particles(p,target);
   if(type.equals("sword"))for(LivingEntity next:p.level().getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(RuneDefinition.i("thunder","chain_radius")),x->x!=target&&Abilities.canHit(p,x)&&x instanceof Enemy)){next.hurt(p.damageSources().indirectMagic(p,p),RuneDefinition.i("thunder","chain_damage"));Abilities.particles(p,next);break;}
  }
  if(Runes.has(item,"heat")&&proc(p,"heat",RuneDefinition.i("heat","hit_cooldown")))target.igniteForSeconds(RuneDefinition.i("heat","fire_seconds"));
  if(!arrow&&Runes.has(item,"forest")&&forest(p)&&target instanceof Enemy)e.setAmount(e.getAmount()+RuneDefinition.f("forest","melee_damage"));
  if(!arrow&&Runes.has(item,"midday")&&day(p))e.setAmount(e.getAmount()+RuneDefinition.f("midday","melee_damage"));
  if(!arrow&&Runes.has(item,"shadow")&&dark(p)){
   if(type.equals("dagger")&&first)e.setAmount(e.getAmount()+RuneDefinition.i("shadow","dagger_damage"));
   else if(type.equals("sword")&&proc(p,"shadow",RuneDefinition.i("shadow","speed_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,RuneDefinition.i("shadow","speed_duration"),RuneDefinition.i("shadow","speed_level")));
  }
  if(Runes.has(item,"protection")&&proc(p,"protection",RuneDefinition.i("protection","hit_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,RuneDefinition.i("protection","hit_duration"),RuneDefinition.i("protection","hit_level")));
  if(Runes.has(item,"wind")&&proc(p,"wind",RuneDefinition.i("wind","hit_cooldown")))target.knockback(type.equals("spear")?RuneDefinition.f("wind","spear_force"):RuneDefinition.f("wind","weapon_force"),p.getX()-target.getX(),p.getZ()-target.getZ());
 }
 public static void finishShock(ServerPlayer p){
  if(!p.getPersistentData().contains("SlavicShock",10))return;
  net.minecraft.nbt.CompoundTag n=p.getPersistentData().getCompound("SlavicShock");p.getPersistentData().remove("SlavicShock");
  if(!n.getString("Dimension").equals(p.level().dimension().location().toString()))return;
  Entity entity=p.level().getEntity(n.getInt("Target"));
  if(!(entity instanceof LivingEntity)||!n.hasUUID("UUID")||!entity.getUUID().equals(n.getUUID("UUID"))||!Abilities.canAffect(p,(LivingEntity)entity))return;
  LivingEntity target=(LivingEntity)entity;int previous=target.invulnerableTime;
  // Add one separately attributed magic pulse after the weapon hit completes.
  // Keep the original hurt window; never recursively treat magic as a melee hit.
  target.invulnerableTime=0;
  try{target.hurt(p.damageSources().indirectMagic(p,p),Math.min(2,Math.max(0,n.getFloat("Damage"))));}
  finally{target.invulnerableTime=Math.max(previous,target.invulnerableTime);}
 }
 public static void block(ServerPlayer p,LivingEntity attacker,ItemStack shield){
  if(PathData.has(p,"riposte"))PathData.data(p).putLong("Riposte",p.level().getGameTime()+35);
  if(Runes.has(shield,"protection")&&proc(p,"protection",RuneDefinition.i("protection","shield_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,RuneDefinition.i("protection","shield_duration"),RuneDefinition.i("protection","shield_level")));
  if(Runes.has(shield,"forest")&&forest(p)&&proc(p,"forest",RuneDefinition.i("forest","shield_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,RuneDefinition.i("forest","shield_duration"),RuneDefinition.i("forest","shield_level")));
  if(Runes.has(shield,"midday")&&day(p)&&proc(p,"midday",RuneDefinition.i("midday","shield_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,RuneDefinition.i("midday","shield_duration"),RuneDefinition.i("midday","shield_level")));
  if(Runes.has(shield,"life")&&p.getHealth()<p.getMaxHealth()*RuneDefinition.n("life","shield_health_threshold")&&proc(p,"life",RuneDefinition.i("life","shield_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,RuneDefinition.i("life","shield_regeneration"),RuneDefinition.i("life","shield_level")));
  if(attacker==null||!Abilities.canHit(p,attacker)||p.distanceToSqr(attacker)>16)return;
  if(Runes.has(shield,"thunder")&&proc(p,"thunder",RuneDefinition.i("thunder","shield_cooldown"))){attacker.hurt(p.damageSources().indirectMagic(p,p),RuneDefinition.f("thunder","shield_damage"));Abilities.particles(p,attacker);}
  if(Runes.has(shield,"heat")&&proc(p,"heat",RuneDefinition.i("heat","shield_cooldown")))attacker.igniteForSeconds(RuneDefinition.i("heat","shield_fire_seconds"));
  if(Runes.has(shield,"wind")&&proc(p,"wind",RuneDefinition.i("wind","shield_cooldown")))attacker.knockback(RuneDefinition.f("wind","shield_force"),p.getX()-attacker.getX(),p.getZ()-attacker.getZ());
 }
 public static void staff(Player p,ItemStack staff,LivingEntity target){
  if(Runes.has(staff,"heat")&&proc(p,"heat",RuneDefinition.i("heat","staff_cooldown")))target.igniteForSeconds(RuneDefinition.i("heat","staff_fire_seconds"));
  if(Runes.has(staff,"wind")&&proc(p,"wind",RuneDefinition.i("wind","staff_cooldown")))target.knockback(RuneDefinition.f("wind","staff_force"),p.getX()-target.getX(),p.getZ()-target.getZ());
  if(Runes.has(staff,"forest")&&proc(p,"forest",RuneDefinition.i("forest","staff_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.REGENERATION,RuneDefinition.i("forest","staff_regeneration"),RuneDefinition.i("forest","staff_level")));
  if(Runes.has(staff,"life")&&proc(p,"life",RuneDefinition.i("life","staff_cooldown")))p.heal(RuneDefinition.i("life","staff_healing"));
  if(Runes.has(staff,"protection")&&proc(p,"protection",RuneDefinition.i("protection","staff_cooldown")))p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,RuneDefinition.i("protection","staff_duration"),RuneDefinition.i("protection","staff_level")));
 }
 private RuneEffects(){}
}
