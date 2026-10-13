package org.slavicmyths.karachun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
import net.minecraft.core.Holder;
import net.minecraft.nbt.*;
import java.util.*;
/** Preserve an earlier effect; cleanup only restores effects still owned by this source. */
public final class NightEffects {
 public static void add(LivingEntity e,UUID source,Holder<MobEffect> effect,int duration,int amplifier){var current=e.getEffect(effect);if(current!=null&&(current.getAmplifier()>amplifier||current.getAmplifier()==amplifier&&current.getDuration()>=duration))return;String key=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getKey(effect.value()).toString();var all=e.getPersistentData().getCompound("Night192Effects");var n=all.getCompound(source.toString());var row=n.getCompound(key);long now=e.level().getGameTime();if(!row.contains("Until")&&current!=null){row.putInt("PreviousAmp",current.getAmplifier());row.putLong("PreviousUntil",now+current.getDuration());row.putBoolean("HadPrevious",true);}row.putLong("Until",now+duration);row.putInt("Amp",amplifier);n.put(key,row);all.put(source.toString(),n);e.getPersistentData().put("Night192Effects",all);e.addEffect(new MobEffectInstance(effect,duration,amplifier));}
 public static void clear(LivingEntity e,UUID source){var all=e.getPersistentData().getCompound("Night192Effects");var n=all.getCompound(source.toString());long now=e.level().getGameTime();for(String key:n.getAllKeys()){var type=net.minecraft.core.registries.BuiltInRegistries.MOB_EFFECT.getHolder(net.minecraft.resources.ResourceLocation.parse(key)).orElse(null);if(type==null)continue;var row=n.getCompound(key);var current=e.getEffect(type);if(current!=null&&current.getAmplifier()==row.getInt("Amp")&&Math.abs(current.getDuration()-(row.getLong("Until")-now))<=2){e.removeEffect(type);int remaining=(int)(row.getLong("PreviousUntil")-now);if(row.getBoolean("HadPrevious")&&remaining>0)e.addEffect(new MobEffectInstance(type,remaining,row.getInt("PreviousAmp")));}}all.remove(source.toString());if(all.isEmpty())e.getPersistentData().remove("Night192Effects");}
 private NightEffects(){}
}
