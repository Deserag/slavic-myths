package org.slavicmyths.savanna;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import org.slavicmyths.registry.ModEntities;
public final class SavannaSpawns {
    private SavannaSpawns(){}
    public static void placements(RegisterSpawnPlacementsEvent event){
        event.register(ModEntities.AUROCHS.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,pos,r)->canSpawn(type,w,reason,pos,r,SavannaAnimal.Kind.AUROCHS),RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.SAIGA.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,pos,r)->canSpawn(type,w,reason,pos,r,SavannaAnimal.Kind.SAIGA),RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.TARPAN.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,pos,r)->canSpawn(type,w,reason,pos,r,SavannaAnimal.Kind.TARPAN),RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.JAYRAN.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,pos,r)->canSpawn(type,w,reason,pos,r,SavannaAnimal.Kind.JAYRAN),RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.JACKAL.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,pos,r)->canSpawn(type,w,reason,pos,r,SavannaAnimal.Kind.JACKAL),RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.STRIPED_HYENA.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,pos,r)->canSpawn(type,w,reason,pos,r,SavannaAnimal.Kind.STRIPED_HYENA),RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.PERSIAN_LEOPARD.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,pos,r)->canSpawn(type,w,reason,pos,r,SavannaAnimal.Kind.PERSIAN_LEOPARD),RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.PHEASANT.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,pos,r)->canSpawn(type,w,reason,pos,r,SavannaAnimal.Kind.PHEASANT),RegisterSpawnPlacementsEvent.Operation.REPLACE);
        event.register(ModEntities.STEPPE_EAGLE.get(),SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(type,w,reason,pos,r)->canSpawn(type,w,reason,pos,r,SavannaAnimal.Kind.STEPPE_EAGLE),RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }
    private static boolean canSpawn(EntityType<SavannaAnimal> type,ServerLevelAccessor w,MobSpawnType reason,BlockPos pos,RandomSource random,SavannaAnimal.Kind kind){
        var soil=w.getBlockState(pos.below());
        boolean dry=kind==SavannaAnimal.Kind.JACKAL||kind==SavannaAnimal.Kind.STRIPED_HYENA||kind==SavannaAnimal.Kind.JAYRAN||kind==SavannaAnimal.Kind.STEPPE_EAGLE||kind==SavannaAnimal.Kind.PERSIAN_LEOPARD;
        if(w.getLevel().dimension()!=Level.OVERWORLD||!w.getFluidState(pos).isEmpty())return false;
        if(!(soil.is(BlockTags.DIRT)||soil.is(Blocks.MOSS_BLOCK)||dry&&(soil.is(BlockTags.SAND)||soil.is(Blocks.TERRACOTTA)||soil.is(Blocks.RED_TERRACOTTA)||soil.is(Blocks.ORANGE_TERRACOTTA)||soil.is(Blocks.YELLOW_TERRACOTTA)||soil.is(Blocks.WHITE_TERRACOTTA)||soil.is(Blocks.BROWN_TERRACOTTA))))return false;
        if(kind.herd()&&!w.canSeeSky(pos))return false;
        int height=kind==SavannaAnimal.Kind.STEPPE_EAGLE?3:2;
        for(int i=0;i<height;i++)if(!w.getFluidState(pos.above(i)).isEmpty()||!w.getBlockState(pos.above(i)).getCollisionShape(w,pos.above(i)).isEmpty())return false;
        return Mob.checkMobSpawnRules(type,w,reason,pos,random);
    }
}
