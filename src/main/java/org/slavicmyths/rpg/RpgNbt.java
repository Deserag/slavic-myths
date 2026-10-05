package org.slavicmyths.rpg;
import java.util.List;
import net.minecraft.nbt.*;
/** Small persistence boundary: unrelated player/item tags must survive every operation. */
public final class RpgNbt {
 public static CompoundTag player(CompoundTag persisted){if(!persisted.contains("SlavicPaths",10))persisted.put("SlavicPaths",new CompoundTag());return persisted.getCompound("SlavicPaths");}
 public static void runes(CompoundTag item,int slots,List<String> ids){
  if(slots<0||slots>3||ids.size()>slots||new java.util.HashSet<>(ids).size()!=ids.size())throw new IllegalArgumentException("Invalid sockets");
  CompoundTag rune=item.getCompound("SlavicRunes").copy();rune.putInt("Slots",slots);ListTag list=new ListTag();ids.forEach(id->list.add(StringTag.valueOf(id)));rune.put("Runes",list);item.put("SlavicRunes",rune);
 }
 private RpgNbt(){}
}
