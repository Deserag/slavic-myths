package org.slavicmyths.village;
import net.minecraft.core.BlockPos;
/** Transient diagnostics/targets only. Real items live exclusively in saved native inventories. */
public final class WorkState {
    public String task="IDLE",failure="NONE",required="none",available="none",pending="none";
    public BlockPos target,storage;public long nextAttempt,nextSearch,nextFishing;public boolean workstationReachable,storageReachable;
    public final java.util.LinkedHashSet<BlockPos> textileStations=new java.util.LinkedHashSet<>();
    public int searches,pathRequests,actions,scanCursor;
}
