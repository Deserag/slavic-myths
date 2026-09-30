package org.slavicmyths.rpg;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.*;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.*;
import net.minecraft.util.DamageSource;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
public final class RuneEffects {
 public static boolean forest(PlayerEntity p){Biome.Category c=p.level.getBiome(p.blockPosition()).getBiomeCategory();return c==Biome.Category.FOREST||c==Biome.Category.TAIGA||c==Biome.Category.JUNGLE;}
 public static boolean day(PlayerEntity p){return p.level.isDay()&&p.level.canSeeSky(p.blockPosition());}
 public static boolean dark(PlayerEntity p){return !p.level.isDay()||p.level.getMaxLocalRawBrightness(p.blockPosition())<7;}
 private static boolean proc(PlayerEntity p,String id,int ticks){return PathData.ready(p,"rune_"+id,PathData.has(p,"resonance")?(int)(ticks*.9):ticks);}
 public static void hit(PlayerEntity p,LivingEntity target,ItemStack item,LivingHurtEvent e,boolean arrow,boolean first){
  String type=Runes.category(item);if(type.isEmpty())return;
  if(Runes.has(item,"thunder")&&(!type.equals("dagger")||first)&&proc(p,"thunder",140)){
   net.minecraft.nbt.CompoundNBT shock=new net.minecraft.nbt.CompoundNBT();shock.putInt("Target",target.getId());shock.putUUID("UUID",target.getUUID());shock.putString("Dimension",p.level.dimension().location().toString());shock.putFloat("Damage",type.equals("heavy")?2:type.equals("dagger")?1:1.5F);p.getPersistentData().put("SlavicShock",shock);Abilities.particles(p,target);
   if(type.equals("sword"))for(LivingEntity next:p.level.getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(2),x->x!=target&&Abilities.canHit(p,x)&&x instanceof IMob)){next.hurt(DamageSource.indirectMagic(p,p),1);Abilities.particles(p,next);break;}
  }
  if(Runes.has(item,"heat")&&proc(p,"heat",180))target.setSecondsOnFire(2);
  if(!arrow&&Runes.has(item,"forest")&&forest(p)&&target instanceof IMob)e.setAmount(e.getAmount()+.75F);
  if(!arrow&&Runes.has(item,"midday")&&day(p))e.setAmount(e.getAmount()+.6F);
  if(!arrow&&Runes.has(item,"shadow")&&dark(p)){
   if(type.equals("dagger")&&first)e.setAmount(e.getAmount()+1);
   else if(type.equals("sword")&&proc(p,"shadow",200))p.addEffect(new EffectInstance(Effects.MOVEMENT_SPEED,40,0));
  }
  if(Runes.has(item,"protection")&&proc(p,"protection",300))p.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,30,0));
  if(Runes.has(item,"wind")&&proc(p,"wind",140))target.knockback(type.equals("spear")?.45F:.25F,p.getX()-target.getX(),p.getZ()-target.getZ());
 }
 public static void finishShock(ServerPlayerEntity p){
  if(!p.getPersistentData().contains("SlavicShock",10))return;
  net.minecraft.nbt.CompoundNBT n=p.getPersistentData().getCompound("SlavicShock");p.getPersistentData().remove("SlavicShock");
  if(!n.getString("Dimension").equals(p.level.dimension().location().toString()))return;
  Entity entity=p.level.getEntity(n.getInt("Target"));
  if(!(entity instanceof LivingEntity)||!n.hasUUID("UUID")||!entity.getUUID().equals(n.getUUID("UUID"))||!Abilities.canAffect(p,(LivingEntity)entity))return;
  LivingEntity target=(LivingEntity)entity;int previous=target.invulnerableTime;
  // Add one separately attributed magic pulse after the weapon hit completes.
  // Keep the original hurt window; never recursively treat magic as a melee hit.
  target.invulnerableTime=0;
  try{target.hurt(DamageSource.indirectMagic(p,p),Math.min(2,Math.max(0,n.getFloat("Damage"))));}
  finally{target.invulnerableTime=Math.max(previous,target.invulnerableTime);}
 }
 public static void block(ServerPlayerEntity p,LivingEntity attacker,ItemStack shield){
  if(PathData.has(p,"riposte"))PathData.data(p).putLong("Riposte",p.level.getGameTime()+35);
  if(Runes.has(shield,"protection")&&proc(p,"protection",240))p.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,45,0));
  if(Runes.has(shield,"forest")&&forest(p)&&proc(p,"forest",240))p.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,30,0));
  if(Runes.has(shield,"midday")&&day(p)&&proc(p,"midday",240))p.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,25,0));
  if(Runes.has(shield,"life")&&p.getHealth()<p.getMaxHealth()*.4&&proc(p,"life",900))p.addEffect(new EffectInstance(Effects.REGENERATION,60,0));
  if(attacker==null||!Abilities.canHit(p,attacker)||p.distanceToSqr(attacker)>16)return;
  if(Runes.has(shield,"thunder")&&proc(p,"thunder",180)){attacker.hurt(DamageSource.indirectMagic(p,p),1.5F);Abilities.particles(p,attacker);}
  if(Runes.has(shield,"heat")&&proc(p,"heat",200))attacker.setSecondsOnFire(1);
  if(Runes.has(shield,"wind")&&proc(p,"wind",160))attacker.knockback(.4F,p.getX()-attacker.getX(),p.getZ()-attacker.getZ());
 }
 public static void staff(PlayerEntity p,ItemStack staff,LivingEntity target){
  if(Runes.has(staff,"heat")&&proc(p,"heat",200))target.setSecondsOnFire(2);
  if(Runes.has(staff,"wind")&&proc(p,"wind",160))target.knockback(.5F,p.getX()-target.getX(),p.getZ()-target.getZ());
  if(Runes.has(staff,"forest")&&proc(p,"forest",900))p.addEffect(new EffectInstance(Effects.REGENERATION,50,0));
  if(Runes.has(staff,"life")&&proc(p,"life",1200))p.heal(1);
  if(Runes.has(staff,"protection")&&proc(p,"protection",300))p.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE,30,0));
 }
 private RuneEffects(){}
}
