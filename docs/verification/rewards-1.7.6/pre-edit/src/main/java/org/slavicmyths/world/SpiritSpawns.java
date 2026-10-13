package org.slavicmyths.world;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.Heightmap;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModEntities;

public final class SpiritSpawns {
    public static void registerPlacements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event) {
        event.register(ModEntities.DOMOVOY.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, world, reason, pos, random) ->
                        random.nextInt(8) == 0 && world.getMaxLocalRawBrightness(pos) > 8
                        && Mob.checkMobSpawnRules(type, world, reason, pos, random)
                        && world.getLevel().isVillage(pos),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.LESHY.get(), net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, world, reason, pos, random) ->
                        random.nextInt(8) == 0 && world.canSeeSky(pos)
                        && world.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                        && Monster.isDarkEnoughToSpawn(world, pos, random)
                        && Mob.checkMobSpawnRules(type, world, reason, pos, random),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private SpiritSpawns() { }
}
