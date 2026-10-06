package org.slavicmyths.bandit;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
/** Level load runs before initial spawn generation. Workers read prepared records, not DimensionDataStorage. */
@EventBusSubscriber(modid="slavicmyths")
public final class CampRecordLifecycle {
 @SubscribeEvent public static void load(LevelEvent.Load event){if(event.getLevel() instanceof ServerLevel world){CampRecords.get(world);StrongholdRecords.get(world);}}
 @SubscribeEvent public static void unload(LevelEvent.Unload event){if(event.getLevel() instanceof ServerLevel world){CampRecords.unload(world);StrongholdRecords.unload(world);}}
 private CampRecordLifecycle(){}
}
