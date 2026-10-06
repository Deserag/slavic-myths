package org.slavicmyths.worldgen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
@EventBusSubscriber(modid="slavicmyths")
public final class FootprintCacheLifecycle {
 @SubscribeEvent public static void unload(LevelEvent.Unload event){if(event.getLevel() instanceof net.minecraft.server.level.ServerLevel)StructureCoverageService.clearFootprints();}
 @SubscribeEvent public static void reload(OnDatapackSyncEvent event){if(event.getPlayer()==null)StructureCoverageService.clearFootprints();}
 private FootprintCacheLifecycle(){}
}
