package org.slavicmyths.artifact;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.attributes.Attributes;
/** Common summon quality entry point. Future class revision should modify enhancedChance only. */
public final class SummonQuality {
 public static double enhancedChance(Player player){return .10D;}
 public static void enhance(Mob m){
  var hp=m.getAttribute(Attributes.MAX_HEALTH);if(hp!=null)hp.setBaseValue(hp.getBaseValue()*1.5D);
  var damage=m.getAttribute(Attributes.ATTACK_DAMAGE);if(damage!=null)damage.setBaseValue(damage.getBaseValue()*1.35D);
  var size=m.getAttribute(Attributes.SCALE);if(size!=null)size.setBaseValue(size.getBaseValue()*1.2D);
  m.setHealth(m.getMaxHealth());m.getPersistentData().putBoolean("SlavicGuardianEnhanced",true);
 }
 private SummonQuality(){}
}
