package org.slavicmyths.wolf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.slavicmyths.navigation.*;
/** Uses the existing persisted player data and native navigation/Xaero publication. */
public final class WolfProgress {
 public static CompoundTag data(ServerPlayer p){var root=org.slavicmyths.rpg.PathData.data(p);if(!root.contains("GreyWolf",10))root.put("GreyWolf",new CompoundTag());return root.getCompound("GreyWolf");}
 public static boolean passed(ServerPlayer p){return data(p).getBoolean("wolf_test_passed");}
 public static void complete(ServerPlayer p,GreyWolf wolf){var d=data(p);if(passed(p))return;d.putBoolean("wolf_met",true);d.putBoolean("wolf_test_passed",true);d.putUUID("wolf_encounter_id",wolf.encounterId());d.putBoolean("wolf_waypoint_unlocked",true);
  NavigationManager.reveal(p,"grey_wolf:"+wolf.encounterId(),MarkerCategory.GREY_WOLF,SlavicMarker.Kind.PERMANENT_DISCOVERY,"entity.slavicmyths.grey_wolf",Level.OVERWORLD.location(),wolf.home(),false,false);
 }
 private WolfProgress(){}
}
