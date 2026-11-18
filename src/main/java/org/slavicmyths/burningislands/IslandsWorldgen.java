package org.slavicmyths.burningislands;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slavicmyths.registry.ModFeatures;
/** Chunk-clipped natural geology, no retro-generation. Existing structure references win. */
@EventBusSubscriber(modid="slavicmyths")
public final class IslandsWorldgen {
 public static final net.neoforged.neoforge.registries.DeferredHolder<Feature<?>,Terrain> TERRAIN=ModFeatures.FEATURES.register("burning_islands_terrain",Terrain::new);
 public static void init(net.neoforged.bus.api.IEventBus bus){TemporaryTerrain.init(bus);}
 @SubscribeEvent public static void preset(net.neoforged.neoforge.event.server.ServerAboutToStartEvent e){var r=e.getServer().registryAccess();var b=r.registryOrThrow(Registries.BIOME).getHolder(IslandsContent.BIOME);var p=r.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).get(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.NETHER);if(b.isPresent()&&p!=null)((org.slavicmyths.navya.NavyaPresetAccess)(Object)p).slavicmyths$append(b.get());}
 @SubscribeEvent public static void ready(net.neoforged.neoforge.event.server.ServerStartedEvent e){for(var s:e.getServer().getAllLevels())if(s.dimension().equals(Level.NETHER))context(s);}
 public static OceanTerrain context(ServerLevel s){return s.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator g?((OceanTerrain.GeneratorAccess)(Object)g).slavicmyths$ocean(s.getChunkSource().randomState()):null;}
 public static final class Terrain extends Feature<NoneFeatureConfiguration>{public Terrain(){super(NoneFeatureConfiguration.CODEC);}
  @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){var w=c.level();int bx=c.origin().getX()&~15,bz=c.origin().getZ()&~15;var chunk=w.getChunk(new BlockPos(bx,32,bz));if(chunk.getAllReferences().values().stream().anyMatch(v->!v.isEmpty()))return false;var context=context(w.getLevel());if(context==null)return false;var p=context.plan(bx,bz);boolean changed=false;
   for(int x=bx;x<bx+16;x++)for(int z=bz;z<bz+16;z++){if(!context.contains(Math.floorDiv(x,4),p.sea()/4,Math.floorDiv(z,4)))continue;int floor=OceanTerrain.floor(p,x,z);if(floor==Integer.MIN_VALUE)continue;
    for(int y=p.sea()-6;y<=p.sea()+36;y++){BlockPos at=new BlockPos(x,y,z);if(!w.ensureCanWrite(at)||w.getBlockEntity(at)!=null)continue;var old=w.getBlockState(at);if(!old.isAir()&&!old.is(Blocks.LAVA)&&!old.is(Blocks.NETHERRACK)&&!old.is(Blocks.BLACKSTONE)&&!old.is(Blocks.BASALT)&&!old.is(Blocks.GRAVEL))continue;
     boolean moat=OceanTerrain.craterLava(p,x,z)&&y>floor&&y<p.home().getY()-1;var block=moat?Blocks.LAVA:y<=floor?(y==floor&&Math.floorMod(x*17+z*31,43)==0?Blocks.OBSIDIAN:Math.floorMod(x+z+y,7)==0?Blocks.BASALT:Blocks.BLACKSTONE):Blocks.AIR;
     // Small exposed lava stream off the crater, never across both bridges.
     if(z==p.z()+36&&x>p.x()+38&&x<p.x()+72&&y==floor+1)block=Blocks.LAVA;
     w.setBlock(at,block.defaultBlockState(),2);changed=true;
    }
    var ground=new BlockPos(x,floor,z);var actual=w.getBlockState(ground);if(actual.is(Blocks.BLACKSTONE)||actual.is(Blocks.BASALT)||actual.is(Blocks.OBSIDIAN))TemporaryTerrain.natural(chunk,ground,actual);
   }return changed;
  }
 }
 private record Pending(OceanTerrain.Plan plan,long until,String last){}
 // Chunk Load precedes entity readiness. Bounded deferred retries, never chunk tickets/loads.
 private static final Map<ServerLevel,LinkedHashMap<Long,Pending>> PENDING=new WeakHashMap<>();
 private static void queue(ServerLevel s,OceanTerrain.Plan p){var q=PENDING.computeIfAbsent(s,k->new LinkedHashMap<>());long key=p.home().asLong();q.put(key,new Pending(p,s.getGameTime()+1200,"QUEUED"));while(q.size()>64)q.remove(q.keySet().iterator().next());}
 @SubscribeEvent public static void retry(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event){for(var entry:new ArrayList<>(PENDING.entrySet())){var s=entry.getKey();if(s.getGameTime()%20!=0)continue;var q=entry.getValue();int budget=Math.min(4,q.size());for(int i=0;i<budget;i++){long key=q.keySet().iterator().next();var pending=q.remove(key);if(s.getGameTime()>pending.until())continue;String result=boss(s,pending.plan());UUID id=UUID.nameUUIDFromBytes(("islands:"+s.getSeed()+":"+pending.plan().home().asLong()).getBytes(java.nio.charset.StandardCharsets.UTF_8));var saved=MagmaEncounters.get(s).snapshot(key);if(s.getEntity(id)==null&&saved.filter(v->v.defeated()).isEmpty())q.put(key,new Pending(pending.plan(),pending.until(),result));}if(q.isEmpty())PENDING.remove(s);}}
 @SubscribeEvent public static void stop(net.neoforged.neoforge.event.server.ServerStoppingEvent e){PENDING.clear();}
 @SubscribeEvent public static void load(net.neoforged.neoforge.event.level.ChunkEvent.Load e){if(!(e.getLevel() instanceof ServerLevel s)||!s.dimension().equals(Level.NETHER))return;var c=context(s);if(c==null)return;var pos=e.getChunk().getPos();var plan=c.plan(pos.getMiddleBlockX(),pos.getMiddleBlockZ());s.getServer().execute(()->queue(s,plan));}
 private static String boss(ServerLevel s,OceanTerrain.Plan p){BlockPos home=p.home();if(s.getDifficulty()==net.minecraft.world.Difficulty.PEACEFUL)return "PEACEFUL";int cx=home.getX()>>4,cz=home.getZ()>>4;for(int x=cx-3;x<=cx+3;x++)for(int z=cz-3;z<=cz+3;z++){if(!s.hasChunk(x,z))return "WAITING_CHUNKS";if(!s.areEntitiesLoaded(ChunkPos.asLong(x,z)))return "WAITING_ENTITIES";}
  UUID id=UUID.nameUUIDFromBytes(("islands:"+s.getSeed()+":"+home.asLong()).getBytes(java.nio.charset.StandardCharsets.UTF_8));var old=MagmaEncounters.get(s).snapshot(home.asLong());if(old.isPresent()&&(old.get().defeated()||!old.get().boss().equals(id))||s.getEntity(id)!=null)return "ALREADY";
  for(int x=-24;x<=24;x++)for(int z=-24;z<=24;z++)if(x*x+z*z<=24*24){var at=home.offset(x,0,z);if(!s.getBiome(at).is(IslandsContent.BIOME))return "BAD_BIOME";if(!s.getBlockState(at.below()).is(Blocks.BLACKSTONE)&&!s.getBlockState(at.below()).is(Blocks.BASALT)&&!s.getBlockState(at.below()).is(Blocks.OBSIDIAN))return "BAD_FLOOR";for(int y=0;y<6;y++)if(!s.getBlockState(at.above(y)).getCollisionShape(s,at.above(y)).isEmpty()||!s.getFluidState(at.above(y)).isEmpty())return "OBSTRUCTED";}
  for(int side:new int[]{-1,1})for(int x=25;x<=39;x++){var at=home.offset(side*x,0,0);if(!s.hasChunkAt(at)||!s.getBlockState(at.below()).isFaceSturdy(s,at.below(),Direction.UP)||!s.isEmptyBlock(at))return "BAD_BRIDGE";}
  var mob=IslandsContent.TYPES.get(IslandsContent.Kind.MAGMA_LORD).get().create(s);if(mob==null)return "FACTORY_NULL";mob.setUUID(id);mob.bindNatural(home);mob.moveTo(home.getX()+.5,home.getY(),home.getZ()+.5,0,0);return s.addFreshEntity(mob)?"SPAWNED":"JOIN_REJECTED";
 }
 private IslandsWorldgen(){}
}
