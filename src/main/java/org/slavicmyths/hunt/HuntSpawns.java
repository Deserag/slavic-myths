package org.slavicmyths.hunt;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.*;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.*;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.Heightmap;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.slavicmyths.registry.ModEntities;
public final class HuntSpawns {
 public static boolean forest(LevelAccessor w,BlockPos p){var biome=w.getBiome(p);return biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_FOREST)||biome.is(net.neoforged.neoforge.common.Tags.Biomes.IS_TAIGA);}
 public static boolean environment(LevelAccessor w,BlockPos p,int radius){BlockPos.MutableBlockPos q=new BlockPos.MutableBlockPos();for(int y=-radius;y<=radius;y++)for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){if(x*x+y*y+z*z>radius*radius)continue;q.set(p.getX()+x,p.getY()+y,p.getZ()+z);if(!w.hasChunkAt(q))continue;net.minecraft.world.level.block.state.BlockState b=w.getBlockState(q);if(b.is(Blocks.HAY_BLOCK)||b.is(Blocks.CAMPFIRE)||b.is(Blocks.SMOKER))return true;}return false;}
 public static boolean clearSpace(LevelAccessor w,BlockPos p,float width,float height){if(!w.hasChunkAt(p.offset(-1,0,-1))||!w.hasChunkAt(p.offset(1,0,1))||!w.hasChunkAt(p.offset(-1,0,1))||!w.hasChunkAt(p.offset(1,0,-1)))return false;
  if(!w.getBlockState(p.below()).isFaceSturdy(w,p.below(),Direction.UP)||!w.getFluidState(p.below()).isEmpty())return false;for(int y=0;y<3;y++)if(!w.getFluidState(p.above(y)).isEmpty()||!w.isEmptyBlock(p.above(y)))return false;
  return w.noCollision(new AABB(p.getX()+.5-width/2,p.getY(),p.getZ()+.5-width/2,p.getX()+.5+width/2,p.getY()+height,p.getZ()+.5+width/2));
 }
 public static boolean alone(net.minecraft.server.level.ServerLevel w,BlockPos p,boolean oven){return w.getEntitiesOfClass(HuntMob.class,new AABB(p).inflate(oven?64:72),m->m.isAlive()&&m.huntTarget()==(oven?HuntTarget.OVINNIK:HuntTarget.VOLKOLAK)).isEmpty();}
 public static boolean openBiome(LevelAccessor w,BlockPos p){return w.getBiome(p).is(net.minecraft.tags.TagKey.create(net.minecraft.core.registries.Registries.BIOME,net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("slavicmyths","spawn_open")));}
 public static boolean airVolume(LevelAccessor w,BlockPos p,int radius,int height){for(int x=-radius;x<=radius;x++)for(int z=-radius;z<=radius;z++){BlockPos q=p.offset(x,0,z);if(!w.hasChunkAt(q))return false;for(int y=0;y<height;y++)if(!w.isEmptyBlock(q.above(y))||!w.getFluidState(q.above(y)).isEmpty())return false;}return true;}
 public static boolean safe(LevelAccessor w,BlockPos p,BlockPos ground,HuntTarget k){if(k==HuntTarget.FIRE_SERPENT)return w.getBlockState(ground.below()).isFaceSturdy(w,ground.below(),Direction.UP)&&w.getFluidState(ground.below()).isEmpty()&&w.canSeeSky(p)&&airVolume(w,p.getY()-ground.getY()>=4?p.below(2):p,2,5);if(k==HuntTarget.PODVEY)return clearSpace(w,p,1.1F,2.55F)&&airVolume(w,p,1,4)&&w.canSeeSky(p);return clearSpace(w,p,k==HuntTarget.OVINNIK?1.15F:1.05F,k==HuntTarget.OVINNIK?2.45F:2.25F);}
 public static HuntMob create(Level w,HuntTarget k){switch(k){case OVINNIK:return ModEntities.OVINNIK.get().create(w);case VOLKOLAK:return ModEntities.VOLKOLAK.get().create(w);case FIRE_SERPENT:return ModEntities.FIRE_SERPENT.get().create(w);default:return ModEntities.PODVEY.get().create(w);}}
 public static boolean alone(net.minecraft.server.level.ServerLevel w,BlockPos p,HuntTarget k){if(k==HuntTarget.OVINNIK||k==HuntTarget.VOLKOLAK)return alone(w,p,k==HuntTarget.OVINNIK);return w.getEntitiesOfClass(HuntMob.class,new AABB(p).inflate(96),m->m.isAlive()&&m.huntTarget()==k&&m.distanceToSqr(p.getX()+.5,p.getY(),p.getZ()+.5)<=96*96).isEmpty();}
 private static boolean elemental(ServerLevelAccessor w,BlockPos p,net.minecraft.util.RandomSource n,HuntTarget k){net.minecraft.server.level.ServerLevel s=w.getLevel();boolean fire=k==HuntTarget.FIRE_SERPENT;return n.nextInt(!fire&&(s.isRaining()||s.isThundering())?64:192)==0&&s.dimension()==Level.OVERWORLD&&s.getDifficulty()!=Difficulty.PEACEFUL&&(!s.isDay()||!fire&&s.isRaining())&&w.canSeeSky(p)&&(openBiome(w,p)||fire&&forest(w,p))&&safe(w,p,p,k)&&alone(s,p,k);}
 public static void placements(net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent event){event.register(ModEntities.FIRE_SERPENT.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,r,p,n)->elemental(w,p,n,HuntTarget.FIRE_SERPENT),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);event.register(ModEntities.PODVEY.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,r,p,n)->elemental(w,p,n,HuntTarget.PODVEY),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);event.register(ModEntities.OVINNIK.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,r,p,n)->natural(w,p,n,true),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);event.register(ModEntities.VOLKOLAK.get(),net.minecraft.world.entity.SpawnPlacementTypes.ON_GROUND,Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(t,w,r,p,n)->natural(w,p,n,false),net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent.Operation.REPLACE);}
 private static boolean natural(ServerLevelAccessor w,BlockPos p,net.minecraft.util.RandomSource n,boolean oven){return n.nextInt(96)==0&&w.getLevel().dimension()==Level.OVERWORLD&&!w.getLevel().isDay()&&w.getDifficulty()!=Difficulty.PEACEFUL&&clearSpace(w,p,oven?1.15F:1.05F,oven?2.45F:2.25F)&&alone(w.getLevel(),p,oven)&&(oven?environment(w,p,16):forest(w,p));}
}
