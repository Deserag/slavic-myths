package org.slavicmyths.verify;
import net.minecraft.gametest.framework.*;
import net.neoforged.neoforge.gametest.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.*;
import org.slavicmyths.posad.PosadNoiseBatch;
@GameTestHolder("slavicmyths_posad") @PrefixGameTestTemplate(false)
public final class PosadNoiseGameTests {
 @GameTest(template="empty",timeoutTicks=200)
 public static void batchedNoiseColumnsExactlyMatchVanilla(GameTestHelper t){
  var r=t.getLevel().registryAccess();var settings=r.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(NoiseGeneratorSettings.NETHER);
  var preset=r.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.NETHER);
  var generator=new NoiseBasedChunkGenerator(net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromPreset(preset),settings);
  long nativeNanos=0,batchNanos=0;int compared=0;
  for(long seed:new long[]{11,29,101,2026,22022026}){
   var random=RandomState.create(settings.value(),r.lookupOrThrow(Registries.NOISE),seed);
   for(int cell:new int[]{-65,-1,0,51,151,593}){
    int x=cell*4,z=(cell-17)*4;var columns=new java.util.HashMap<Long,net.minecraft.world.level.NoiseColumn>();
    long before=System.nanoTime();t.assertTrue(PosadNoiseBatch.fill(generator,random,t.getLevel(),x,z,columns),"Noise batch unavailable");batchNanos+=System.nanoTime()-before;
    for(int dx=0;dx<4;dx++)for(int dz=0;dz<4;dz++){
     before=System.nanoTime();var expected=generator.getBaseColumn(x+dx,z+dz,t.getLevel(),random);nativeNanos+=System.nanoTime()-before;
     var actual=columns.get(((long)(x+dx)<<32)^((z+dz)&0xffffffffL));t.assertTrue(actual!=null,"Batched column missing");
     for(int y=settings.value().noiseSettings().minY()-1;y<=settings.value().noiseSettings().minY()+settings.value().noiseSettings().height();y++){compared++;t.assertTrue(actual.getBlock(y).equals(expected.getBlock(y)),"Noise differs seed="+seed+" xyz="+(x+dx)+","+y+","+(z+dz));}
    }
   }
  }
  System.out.println("POSAD_NOISE_BATCH_PARITY_PASS states="+compared+" batchNanos="+batchNanos+" nativeNanos="+nativeNanos+" (column benchmark, not server MSPT)");t.succeed();
 }
 @GameTest(template="empty",timeoutTicks=200)
 public static void boundedSixteenTileWindowExactlyMatchesVanilla(GameTestHelper t){
  var r=t.getLevel().registryAccess();var settings=r.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(NoiseGeneratorSettings.NETHER);
  var preset=r.registryOrThrow(Registries.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST).getHolderOrThrow(net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterLists.NETHER);
  var generator=new NoiseBasedChunkGenerator(net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromPreset(preset),settings);
  long nativeNanos=0,batchNanos=0;int states=0;
  for(long seed:new long[]{11,29,101,2026,22022026}){
   var random=RandomState.create(settings.value(),r.lookupOrThrow(Registries.NOISE),seed);
   for(int cell:new int[]{-4,0,14}){
    int x=cell*16,z=(cell-17)*16;var windows=new java.util.HashMap<Long,PosadNoiseBatch.Window>();long before=System.nanoTime();
    t.assertTrue(PosadNoiseBatch.fillWindow(generator,random,t.getLevel(),x,z,-16,81,windows),"Window sampler unavailable");batchNanos+=System.nanoTime()-before;
    for(int dx=0;dx<16;dx++)for(int dz=0;dz<16;dz++){
     before=System.nanoTime();var expected=generator.getBaseColumn(x+dx,z+dz,t.getLevel(),random);nativeNanos+=System.nanoTime()-before;
     var actual=windows.get(((long)(x+dx)<<32)^((z+dz)&0xffffffffL));
     for(int y=-16;y<=81;y++){states++;t.assertTrue(actual.state(y).equals(expected.getBlock(y)),"Window differs seed="+seed+" xyz="+(x+dx)+","+y+","+(z+dz));}
     boolean rejected=false;try{actual.state(82);}catch(IllegalArgumentException outside){rejected=true;}t.assertTrue(rejected,"Out-of-domain sample silently fabricated air");
    }
   }
  }
  System.out.println("POSAD_NOISE_WINDOW_PARITY_PASS states="+states+" batchNanos="+batchNanos+" nativeNanos="+nativeNanos+" (column benchmark only)");t.succeed();
 }

}
