package org.slavicmyths.chud;

import java.util.Arrays;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.Structure;

/** Raw vanilla noise heights only. No ChunkAccess, world reads, tickets, structures or block writes.
 * Shares the exact interpolation cells used by NoiseBasedChunkGenerator.iterateNoiseColumn.
 * Like getBaseHeight, this deliberately excludes later surface rules, carvers and structures. */
public final class ChudNoiseSurvey {
    // Vanilla's zero BeardifierMarker is protected; implement its public, exact zero contract.
    private static final DensityFunctions.BeardifierOrMarker NO_STRUCTURES=new DensityFunctions.BeardifierOrMarker(){
        @Override public double compute(DensityFunction.FunctionContext context){return 0;}
        @Override public void fillArray(double[] values,DensityFunction.ContextProvider context){Arrays.fill(values,0);}
        @Override public double minValue(){return 0;}
        @Override public double maxValue(){return 0;}
    };
    private static final class Sampler extends NoiseChunk {
        Sampler(int cells,Structure.GenerationContext c,int x,int z,NoiseSettings noise,NoiseGeneratorSettings settings,Aquifer.FluidPicker fluids){
            super(cells,c.randomState(),x,z,noise,NO_STRUCTURES,settings,fluids,Blender.empty());
        }
        BlockState sample(){return getInterpolatedState();}
    }
    public static int[] heights(Structure.GenerationContext c,ChunkPos at){
        int[] result=new int[256];Arrays.fill(result,c.heightAccessor().getMinBuildHeight()-1);
        if(!(c.chunkGenerator() instanceof NoiseBasedChunkGenerator generator))return fallback(c,at,result);
        var settings=generator.generatorSettings().value();var noise=settings.noiseSettings().clampToHeightAccessor(c.heightAccessor());
        int width=noise.getCellWidth(),height=noise.getCellHeight(),vertical=noise.height()/height,base=Math.floorDiv(noise.minY(),height);
        if(width>16||16%width!=0)return fallback(c,at,result);
        if(vertical<=0)return result;
        var lava=new Aquifer.FluidStatus(-54,Blocks.LAVA.defaultBlockState());var fluid=new Aquifer.FluidStatus(settings.seaLevel(),settings.defaultFluid());
        Aquifer.FluidPicker picker=(x,y,z)->y<Math.min(-54,settings.seaLevel())?lava:fluid;
        int x0=at.getMinBlockX(),z0=at.getMinBlockZ(),cells=16/width;
        var sampler=new Sampler(cells,c,x0,z0,noise,settings,picker);
        sampler.initializeForFirstCellX();
        try{
            for(int cellX=0;cellX<cells;cellX++){
                ChudSearchBudget.check();sampler.advanceCellX(cellX);
                for(int cellZ=0;cellZ<cells;cellZ++){
                    int remaining=width*width;
                    for(int cellY=vertical-1;cellY>=0&&remaining>0;cellY--){
                        ChudSearchBudget.check();sampler.selectCellYZ(cellY,cellZ);
                        for(int dy=height-1;dy>=0&&remaining>0;dy--){
                            int y=(base+cellY)*height+dy;sampler.updateForY(y,(double)dy/height);
                            for(int dx=0;dx<width;dx++){
                                int lx=cellX*width+dx;sampler.updateForX(x0+lx,(double)dx/width);
                                for(int dz=0;dz<width;dz++){
                                    int lz=cellZ*width+dz,index=(lz<<4)|lx;sampler.updateForZ(z0+lz,(double)dz/width);
                                    if(result[index]!=c.heightAccessor().getMinBuildHeight()-1)continue;
                                    BlockState state=sampler.sample();if(state==null)state=settings.defaultBlock();
                                    if(Heightmap.Types.OCEAN_FLOOR_WG.isOpaque().test(state)){result[index]=y;remaining--;}
                                }
                            }
                        }
                    }
                }
                sampler.swapSlices();
            }
        }finally{sampler.stopInterpolation();}
        return result;
    }
    private static int[] fallback(Structure.GenerationContext c,ChunkPos at,int[] result){
        for(int z=0;z<16;z++)for(int x=0;x<16;x++){ChudSearchBudget.check();result[(z<<4)|x]=c.chunkGenerator().getBaseHeight(at.getMinBlockX()+x,at.getMinBlockZ()+z,Heightmap.Types.OCEAN_FLOOR_WG,c.heightAccessor(),c.randomState())-1;}return result;
    }
    private ChudNoiseSurvey(){}
}
