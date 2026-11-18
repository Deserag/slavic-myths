package org.slavicmyths.tales;
import net.minecraft.nbt.CompoundTag;
/** Public wire projection only; saved hints and their private links are never mutated. */
public final class TaleHintVisibility {
 public static CompoundTag publicCopy(CompoundTag original,boolean stale){
  var result=new CompoundTag();for(String key:java.util.List.of("X","Z","Radius","Dimension","Source","Direction","Distance","Expires")){var value=original.get(key);if(value!=null)result.put(key,value.copy());}
  result.putBoolean("Stale",stale);return result;
 }
 private TaleHintVisibility(){}
}
