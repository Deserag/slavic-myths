package org.slavicmyths.netherores;

import net.minecraft.core.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.slavicmyths.registry.ModFeatures;

/** Chunk-clipped new-chunk mineral pass, after geology and decoration; no retro-gen/ticks. */
public final class NetherOreFeature extends Feature<NoneFeatureConfiguration> {
 public static final TagKey<Block> HOST=TagKey.create(Registries.BLOCK,ResourceLocation.fromNamespaceAndPath("slavicmyths","nether_ore_hosts"));
 static {ModFeatures.FEATURES.register("nether_minerals",NetherOreFeature::new);}
 public static void init(){}
 public NetherOreFeature(){super(NoneFeatureConfiguration.CODEC);}
 static int extra(net.minecraft.util.RandomSource r,float count){int whole=(int)count;return whole+(r.nextFloat()<count-whole?1:0);}
 public static int low(int minY,int suggested){return Math.max(minY+5,suggested);}
 public static int high(int maxY,int suggested){return Math.min(maxY-6,suggested);}
 @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){
  var w=c.level();if(!(w instanceof net.minecraft.server.level.WorldGenRegion)||!w.getLevel().dimension().equals(Level.NETHER))return false;
  int bx=c.origin().getX()&~15,bz=c.origin().getZ()&~15;var chunk=w.getChunk(new BlockPos(bx,w.getMinBuildHeight()+10,bz));
  if(chunk.getAllReferences().values().stream().anyMatch(ref->!ref.isEmpty()))return false;
  var r=c.random();boolean changed=false;
  int[] attempts={12,1,1,5,2},surface={2,1,1,1,3},min={24,12,20,24,24},max={176,132,144,160,148},size={6,3,2,4,4};
  String[] ids={"livingstone_ore","alatyrite_ore","runnik_ore","infernal_ore","sulfur_deposit"};
  for(int ore=0;ore<5;ore++){
   int low=low(w.getMinBuildHeight(),min[ore]),high=high(w.getMaxBuildHeight(),max[ore]);if(high<low)continue;
   var biome=w.getBiome(new BlockPos(bx+8,(low+high)/2,bz+8)).unwrapKey().orElseThrow().location().getPath();
   float multiplier=(ore==1&&(biome.equals("soul_sand_valley")||biome.equals("navya_grove"))||ore==2&&(biome.equals("warped_forest")||biome.equals("lost_halls"))||ore==3&&(biome.equals("basalt_deltas")||biome.equals("black_cinder")||biome.equals("burning_islands")))?1.25F:1;
   int tries=extra(r,attempts[ore]*multiplier);
   for(int i=0;i<tries;i++){
    int x=bx+1+r.nextInt(14),z=bz+1+r.nextInt(14),y=low+r.nextInt(high-low+1);BlockPos at=new BlockPos(x,y,z);
    int count=ore==0?3+r.nextInt(4):ore==3?2+r.nextInt(3):1+r.nextInt(size[ore]);
    for(int j=0;j<count;j++){
     if(inside(at,bx,bz)&&host(w,at)&&enclosed(w,at)){w.setBlock(at,NetherOres.BLOCKS.get(ids[ore]).get().defaultBlockState(),2);changed=true;}
     at=at.relative(Direction.getRandom(r));if(at.getY()<low||at.getY()>high)break;
    }
   }
   float chance=ore==1||ore==2?.25F:ore==3?.8F:1;
   for(int i=0;i<surface[ore];i++){if(r.nextFloat()>=chance*multiplier)continue;int x=bx+1+r.nextInt(14),z=bz+1+r.nextInt(14);
    // An exposed embedded mineral replaces existing rock, never fills lava/air.
    for(int y=high;y>=low;y--){var at=new BlockPos(x,y,z);if(!host(w,at)||!exposed(w,at))continue;
     int count=ore==0?1+r.nextInt(3):ore==3?1+r.nextInt(2):ore==4?1+r.nextInt(5):1;
     for(int j=0;j<count;j++){var q=at.offset(j%2,0,j/2);if(inside(q,bx,bz)&&host(w,q)&&exposed(w,q)){w.setBlock(q,NetherOres.BLOCKS.get(ids[ore]).get().defaultBlockState(),2);changed=true;}}
     break;
    }
   }
  }return changed;
 }
 private static boolean inside(BlockPos p,int x,int z){return p.getX()>x&&p.getX()<x+15&&p.getZ()>z&&p.getZ()<z+15;}
 private static boolean host(WorldGenLevel w,BlockPos p){return w.ensureCanWrite(p)&&w.getBlockState(p).is(HOST)&&w.getBlockEntity(p)==null;}
 private static boolean enclosed(WorldGenLevel w,BlockPos p){for(Direction d:Direction.values()){var s=w.getBlockState(p.relative(d));if(s.isAir()||!s.getFluidState().isEmpty())return false;}return true;}
 private static boolean exposed(WorldGenLevel w,BlockPos p){int rock=0;boolean open=false;for(Direction d:Direction.values()){var s=w.getBlockState(p.relative(d));if(!s.getFluidState().isEmpty())return false;open|=s.isAir();if(s.is(HOST))rock++;}return open&&rock>=2;}
}
