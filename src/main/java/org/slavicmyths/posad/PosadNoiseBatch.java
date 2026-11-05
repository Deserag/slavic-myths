package org.slavicmyths.posad;

import java.util.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;

/** Exact vanilla interpolation, with shared noise-cell slices. No chunks or approximate heights. */
public final class PosadNoiseBatch extends NoiseChunk {
 private enum ZeroBeardifier implements DensityFunctions.BeardifierOrMarker {
  INSTANCE;
  @Override public double compute(DensityFunction.FunctionContext context){return 0;}
  @Override public double minValue(){return 0;}
  @Override public double maxValue(){return 0;}
  @Override public void fillArray(double[] values,DensityFunction.ContextProvider context){Arrays.fill(values,0);}
 }
 public record Window(int minY,int maxY,NoiseColumn column){
  public boolean contains(int y){return y>=minY&&y<=maxY;}
  public BlockState state(int y){if(!contains(y))throw new IllegalArgumentException("Outside sampled noise window");return column.getBlock(y);}
 }
 private PosadNoiseBatch(int cells,RandomState random,int x,int z,NoiseSettings noise,NoiseGeneratorSettings settings){
  super(cells,random,x,z,noise,ZeroBeardifier.INSTANCE,settings,fluid(settings),Blender.empty());
 }
 private static Aquifer.FluidPicker fluid(NoiseGeneratorSettings settings){
  var lava=new Aquifer.FluidStatus(-54,Blocks.LAVA.defaultBlockState());var sea=new Aquifer.FluidStatus(settings.seaLevel(),settings.defaultFluid());
  return (x,y,z)->y<Math.min(-54,settings.seaLevel())?lava:sea;
 }
 public static boolean fill(NoiseBasedChunkGenerator generator,RandomState random,LevelHeightAccessor height,int x,int z,Map<Long,NoiseColumn> columns){
  var noise=generator.generatorSettings().value().noiseSettings().clampToHeightAccessor(height);
  var result=sample(generator,random,height,x,z,1,noise.minY(),noise.minY()+noise.height()-1);
  if(result==null)return false;columns.putAll(result);return true;
 }
 /** A 16x16 tile, restricted to the planner's documented vertical query domain. */
 public static boolean fillWindow(NoiseBasedChunkGenerator generator,RandomState random,LevelHeightAccessor height,int x,int z,int minY,int maxY,Map<Long,Window> columns){
  if(minY>maxY)throw new IllegalArgumentException("Empty noise window");
  var result=sample(generator,random,height,x,z,4,minY,maxY);if(result==null)return false;
  result.forEach((key,column)->columns.put(key,new Window(minY,maxY,column)));return true;
 }
 private static Map<Long,NoiseColumn> sample(NoiseBasedChunkGenerator generator,RandomState random,LevelHeightAccessor height,int x,int z,int cells,int minY,int maxY){
  var settings=generator.generatorSettings().value();var noise=settings.noiseSettings().clampToHeightAccessor(height);
  int width=noise.getCellWidth(),cellHeight=noise.getCellHeight(),count=noise.height()/cellHeight;
  if(width>8||count<=0||Math.floorMod(noise.minY(),cellHeight)!=0||noise.height()%cellHeight!=0)return null;
  int side=width*cells,baseX=Math.floorDiv(x,side)*side,baseZ=Math.floorDiv(z,side)*side;
  int low=Math.max(minY,noise.minY()),high=Math.min(maxY,noise.minY()+noise.height()-1);
  var states=new BlockState[side][side][Math.max(0,high-low+1)];
  if(low<=high){
   var sampler=new PosadNoiseBatch(cells,random,baseX,baseZ,noise,settings);
   sampler.initializeForFirstCellX();
   try{
    for(int cx=0;cx<cells;cx++){
     sampler.advanceCellX(cx);
     for(int cz=0;cz<cells;cz++)for(int cy=Math.floorDiv(high-noise.minY(),cellHeight);cy>=Math.floorDiv(low-noise.minY(),cellHeight);cy--){
      sampler.selectCellYZ(cy,cz);
      for(int dy=cellHeight-1;dy>=0;dy--){
       int worldY=noise.minY()+cy*cellHeight+dy;if(worldY<low||worldY>high)continue;
       sampler.updateForY(worldY,(double)dy/cellHeight);
       for(int dx=0;dx<width;dx++){
        int ix=cx*width+dx;sampler.updateForX(baseX+ix,(double)dx/width);
        for(int dz=0;dz<width;dz++){
         int iz=cz*width+dz;sampler.updateForZ(baseZ+iz,(double)dz/width);
         var state=sampler.getInterpolatedState();states[ix][iz][worldY-low]=state==null?settings.defaultBlock():state;
        }
       }
      }
     }
     sampler.swapSlices();
    }
   }finally{sampler.stopInterpolation();}
  }
  var result=new HashMap<Long,NoiseColumn>();
  for(int dx=0;dx<side;dx++)for(int dz=0;dz<side;dz++)result.put(((long)(baseX+dx)<<32)^((baseZ+dz)&0xffffffffL),new NoiseColumn(low,states[dx][dz]));
  return result;
 }
}
