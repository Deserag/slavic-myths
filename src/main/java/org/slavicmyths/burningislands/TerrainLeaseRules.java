package org.slavicmyths.burningislands;
import java.util.UUID;import net.minecraft.nbt.*;import net.minecraft.world.level.block.state.BlockState;
/** Pure serialized lease contract used by production and native fault-cut tests. */
public final class TerrainLeaseRules {
 public static CompoundTag create(BlockState original,BlockState placed,UUID owner,long until){if(owner==null||until<0)throw new IllegalArgumentException("Invalid terrain lease");var e=new CompoundTag();e.put("Original",NbtUtils.writeBlockState(original));e.put("Placed",NbtUtils.writeBlockState(placed));e.putUUID("Owner",owner);e.putLong("Until",until);return e;}
 public static boolean mayRestore(CompoundTag e,BlockState current){return e.hasUUID("Owner")&&e.contains("Until",4)&&e.contains("Original",10)&&e.contains("Placed",10)&&e.getCompound("Placed").equals(NbtUtils.writeBlockState(current));}
 public static boolean expired(CompoundTag e,long now){return e.contains("Until",4)&&now>=e.getLong("Until");}
 private TerrainLeaseRules(){}
}
