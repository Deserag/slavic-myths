package org.slavicmyths.morozko;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
import net.minecraft.nbt.CompoundTag;
public final class FrostCold {
 public static final String KEY="Frost191Cold";
 public static CompoundTag data(LivingEntity e){var root=e.getPersistentData();if(!root.contains(KEY,10))root.put(KEY,new CompoundTag());return root.getCompound(KEY);}
 public static void add(LivingEntity e,double amount){if(e instanceof Morozko||e instanceof PlayerDouble)return;var n=data(e);n.putDouble("Value",Math.clamp(n.getDouble("Value")+amount,0,100));n.putLong("Touched",e.level().getGameTime());}
 public static void aura(LivingEntity e,double amount,Morozko boss){add(e,amount);var n=data(e);n.remove("Finished");n.putLong("AuraUntil",e.level().getGameTime()+8);n.putUUID("Boss",boss.getUUID());}
 public static void stop(LivingEntity e){if(e.getPersistentData().contains(KEY)){var n=data(e);n.putLong("AuraUntil",0);n.putBoolean("Finished",true);}}
 public static boolean resistant(LivingEntity e){for(var s:e.getArmorSlots())if(org.slavicmyths.rpg.Runes.has(s,"frost_resistance"))return true;return false;}
 public static void tick(LivingEntity e){if(!e.getPersistentData().contains(KEY)||e.tickCount%4!=0||e.level().isClientSide)return;var n=data(e);long now=e.level().getGameTime();double cold=n.getDouble("Value");if(n.getBoolean("Finished")||now-n.getLong("Touched")>40){cold=Math.max(0,cold-(n.getBoolean("Finished")?3:1.2));n.putDouble("Value",cold);}int tier=FrostRules.tier(cold);if(!resistant(e)){if(tier>0)e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,8,tier==4?2:tier>=2?1:0,false,false,true));if(tier>=2)e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,8,tier>=3?1:0,false,false,true));}
 if(tier>=3&&now<n.getLong("AuraUntil")&&now>=n.getLong("DamageAt")&&!n.getBoolean("Finished")){e.hurt(org.slavicmyths.combat.MythDamageSources.unattributed("morozko_aura",e),tier==4?2:1);n.putLong("DamageAt",now+(tier==4?40:60));FrostContent.sound(e,"frost_damage");}
 if(cold<=0)e.getPersistentData().remove(KEY);}
 private FrostCold(){}
}
