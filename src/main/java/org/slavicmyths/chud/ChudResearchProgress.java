package org.slavicmyths.chud;
import net.minecraft.nbt.CompoundTag;

/** Small state transitions for the existing tale journal's per-player research records. */
public final class ChudResearchProgress {
    public static boolean visit(CompoundTag quest,String dimension,long owner){
        if(!quest.contains("Target")||quest.getBoolean("Visited")||quest.getLong("Owner")!=owner||!quest.getString("Dimension").equals(dimension))return false;
        quest.putBoolean("Visited",true);return true;
    }
    public static boolean claim(CompoundTag quest){
        if(!quest.contains("Target")||!quest.getBoolean("Visited")||quest.getBoolean("Claimed"))return false;
        quest.putBoolean("Claimed",true);return true;
    }
    private ChudResearchProgress(){}
}
