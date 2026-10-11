import java.util.*;
import net.minecraft.nbt.CompoundTag;
import org.slavicmyths.depths.*;

/** Native NBT only; no Minecraft bootstrap, client, server or world. */
public final class DepthsPersistenceOfflineChecks {
    public static void main(String[] args){
        int checks=0;
        for(int direction=0;direction<4;direction++)for(String status:new String[]{"PENDING","COMPLETE","FAILED_NEEDS_OPERATOR"}){
            var plan=new DepthsPlan(new UUID(3,11),new DepthsPlan.Point(-800,70,-22),new DepthsPlan.Point(1511,69,724),direction,37,true,4);
            var record=new DepthsSavedData.Entry(plan,new UUID(5,9),"MAP");record.status=status;record.phase=status.equals("PENDING")?"MOBS":status;record.cursor=65712;record.chests=3;record.mobs=4095;record.mapId=123;record.rewardDelivered=true;
            var restored=DepthsSavedData.Entry.load(record.save().copy());
            if(!record.plan.equals(restored.plan)||!restored.status.equals(status)||!restored.phase.equals(record.phase)||restored.cursor!=65712||restored.mobs!=4095||restored.chests!=3||restored.mapId!=123||!restored.rewardDelivered||!restored.player.equals(record.player)||!restored.source.equals("MAP"))throw new AssertionError("record roundtrip");
            if(!java.util.Arrays.equals(record.save().getIntArray("BossAnchor"),restored.save().getIntArray("BossAnchor")))throw new AssertionError("boss anchor");
            checks+=12;
        }
        System.out.println("DEPTHS NBT PASS: "+checks+" field assertions /12 roundtrips; not a real relog; Minecraft launches=0");
    }
}
