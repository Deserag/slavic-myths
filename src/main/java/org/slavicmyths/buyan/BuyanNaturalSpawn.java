package org.slavicmyths.buyan;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Reserve one resumable Buyan in ordinary worlds; the existing builders own all terrain work. */
@EventBusSubscriber(modid="slavicmyths")
public final class BuyanNaturalSpawn {
    @SubscribeEvent public static void start(ServerStartedEvent event) {
        var world=event.getServer().overworld();
        if(world.getChunkSource().getGenerator() instanceof FlatLevelSource || org.slavicmyths.winter.WinterWorld.isTest(world))return;
        var state=BuyanState.get(world);
        if(state.active || !state.completedSites().isEmpty())return;
        for(int i=0;i<16;i++) {
            var origin=world.getSharedSpawnPos().offset(3000+i*2048,0,3000);
            var found=world.findClosestBiome3d(b->b.is(BiomeTags.IS_DEEP_OCEAN),origin,8192,64,64);
            if(found==null)continue;
            BlockPos pos=found.getFirst().atY(world.getSeaLevel());
            if(!world.getWorldBorder().isWithinBounds(pos.offset(450,0,450))
                ||!world.getWorldBorder().isWithinBounds(pos.offset(-450,0,-450)))continue;
            if(!BuyanOceanSite.suitable(world,pos.getX(),pos.getZ()))continue;
            world.getChunk(pos);
            if(BuyanGenerator.generate(event.getServer().createCommandSourceStack().withLevel(world)
                .withPosition(Vec3.atCenterOf(pos)))>0) {
                LogUtils.getLogger().info("Natural Buyan reserved at {} in world seed {}",pos,world.getSeed());
                return;
            }
        }
        LogUtils.getLogger().error("No suitable ocean found for Buyan; reservation will retry on world restart");
    }
}