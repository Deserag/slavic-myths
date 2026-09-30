package org.slavicmyths.rpg;
import java.util.List;
import net.minecraft.nbt.*;
/** Small persistence boundary: unrelated player/item tags must survive every operation. */
public final class RpgNbt {
 public static CompoundNBT player(CompoundNBT persisted){if(!persisted.contains("SlavicPaths",10))persisted.put("SlavicPaths",new CompoundNBT());return persisted.getCompound("SlavicPaths");}
 public static void runes(CompoundNBT item,int slots,List<String> ids){
  if(slots<0||slots>3||ids.size()>slots||new java.util.HashSet<>(ids).size()!=ids.size())throw new IllegalArgumentException("Invalid sockets");
  CompoundNBT rune=item.getCompound("SlavicRunes").copy();rune.putInt("Slots",slots);ListNBT list=new ListNBT();ids.forEach(id->list.add(StringNBT.valueOf(id)));rune.put("Runes",list);item.put("SlavicRunes",rune);
 }
 private RpgNbt(){}
}
