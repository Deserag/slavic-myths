package org.slavicmyths.world;

import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntitySpawnPlacementRegistry;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slavicmyths.SlavicMyths;
import org.slavicmyths.registry.ModEntities;

@Mod.EventBusSubscriber(modid = SlavicMyths.MOD_ID)
public final class SpiritSpawns {
    public static void registerPlacements() {
        EntitySpawnPlacementRegistry.register(ModEntities.DOMOVOY.get(), EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (type, world, reason, pos, random) ->
                        random.nextInt(8) == 0 && world.getMaxLocalRawBrightness(pos) > 8
                        && MobEntity.checkMobSpawnRules(type, world, reason, pos, random)
                        && world.getLevel().isVillage(pos));
        EntitySpawnPlacementRegistry.register(ModEntities.LESHY.get(), EntitySpawnPlacementRegistry.PlacementType.ON_GROUND,
                Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, (type, world, reason, pos, random) ->
                        random.nextInt(8) == 0 && world.canSeeSky(pos)
                        && world.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                        && MonsterEntity.isDarkEnoughToSpawn(world, pos, random)
                        && MobEntity.checkMobSpawnRules(type, world, reason, pos, random));
    }
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void biomes(BiomeLoadingEvent event) {
        if (event.getName() == null || !event.getName().getNamespace().equals("minecraft")) return;
        Biome.Category category = event.getCategory();
        if (category == Biome.Category.PLAINS || category == Biome.Category.TAIGA || category == Biome.Category.SAVANNA) {
            event.getSpawns().addSpawn(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(ModEntities.DOMOVOY.get(), 2, 1, 1));
        }
        if (category == Biome.Category.FOREST || category == Biome.Category.TAIGA) {
            event.getSpawns().addSpawn(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(ModEntities.LESHY.get(), 3, 1, 1));
        }
    }
    private SpiritSpawns() { }
}
