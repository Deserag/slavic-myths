package org.slavicmyths.kurgan;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Shared 4-block Voronoi soil patches. Material choice never changes construction heights. */
public final class KurganSurface {
    private static long hash(long value){value=(value^(value>>>30))*0xbf58476d1ce4e5b9L;value=(value^(value>>>27))*0x94d049bb133111ebL;return value^(value>>>31);}
    public static int patch(long seed,int x,int z){
        int gx=Math.floorDiv(x,4),gz=Math.floorDiv(z,4);double best=Double.MAX_VALUE;long chosen=0;
        for(int a=gx-1;a<=gx+1;a++)for(int b=gz-1;b<=gz+1;b++){
            long h=hash(seed^a*341873128712L^b*132897987541L);
            double dx=x-(a*4+1+(h&255)/128.0),dz=z-(b*4+1+((h>>>8)&255)/128.0),d=dx*dx+dz*dz;
            if(d<best){best=d;chosen=h;}
        }return (int)Math.floorMod(chosen,100);
    }
    public static Block material(long seed,int dx,int dz,int radius,int tier,int slope,boolean top,boolean forest,boolean dry){
        double radial=Math.hypot(dx,dz)/radius;
        int v=patch(seed,dx,dz);boolean entrance=Math.hypot(dx,dz-radius)<8;
        Block block;
        if(entrance&&v>=55-tier*3)block=v<68?Blocks.COARSE_DIRT:v<78?Blocks.GRAVEL:v<88?Blocks.STONE:v<95?Blocks.COBBLESTONE:Blocks.MOSSY_COBBLESTONE;
        else if(radial<.42&&slope<=1)block=v<60?Blocks.GRASS_BLOCK:v<72?Blocks.COARSE_DIRT:v<80?Blocks.ROOTED_DIRT:v<87?Blocks.DIRT:v<92?Blocks.GRAVEL:v<96?Blocks.MOSS_BLOCK:Blocks.STONE;
        else if(radial>.82)block=v<55?Blocks.GRASS_BLOCK:v<67?Blocks.COARSE_DIRT:v<75?Blocks.DIRT:v<81?Blocks.ROOTED_DIRT:v<89?Blocks.GRAVEL:v<94?Blocks.STONE:v<97?Blocks.COBBLESTONE:Blocks.MOSSY_COBBLESTONE;
        else block=v<48?Blocks.GRASS_BLOCK:v<66?Blocks.COARSE_DIRT:v<76?Blocks.DIRT:v<84?Blocks.ROOTED_DIRT:v<91?Blocks.GRAVEL:v<96?Blocks.STONE:v<98?Blocks.MOSS_BLOCK:Blocks.MOSSY_COBBLESTONE;
        if(slope>=2&&v>=84&&v<91)block=Blocks.STONE;
        if(tier==0&&(block==Blocks.COBBLESTONE||block==Blocks.MOSSY_COBBLESTONE)&&!entrance)block=Blocks.COARSE_DIRT;
        if(dry&&block==Blocks.MOSS_BLOCK)block=Blocks.COARSE_DIRT;
        if(forest&&block==Blocks.GRASS_BLOCK&&patch(seed^91371,dx,dz)<8)block=Blocks.PODZOL;
        if(!top&&block==Blocks.GRASS_BLOCK)block=Blocks.ROOTED_DIRT;
        return block;
    }
    public static Block block(WorldGenLevel world,BlockPos origin,BlockPos pos,long seed,int radius,int tier,int slope,boolean top){
        var biome=world.getBiome(pos);return material(seed^origin.asLong(),pos.getX()-origin.getX(),pos.getZ()-origin.getZ(),radius,tier,slope,top,biome.is(BiomeTags.IS_TAIGA)||biome.is(BiomeTags.IS_FOREST),!biome.value().hasPrecipitation());
    }
    public static void decorate(WorldGenLevel world,BlockPos origin,BlockPos surface,long seed,int radius,net.minecraft.world.level.levelgen.structure.BoundingBox clip){
        int dx=surface.getX()-origin.getX(),dz=surface.getZ()-origin.getZ();
        if(Math.abs(dx)<=3&&dz>=radius-10)return;
        BlockPos above=surface.above();if(!clip.isInside(above)||!world.getBlockState(above).isAir())return;
        var biome=world.getBiome(above);var soil=world.getBlockState(surface);
        if(biome.value().coldEnoughToSnow(above)&&Math.floorMod(hash(seed^surface.asLong()),100)>=12&&Blocks.SNOW.defaultBlockState().canSurvive(world,above))world.setBlock(above,Blocks.SNOW.defaultBlockState(),2);
        else if((soil.is(Blocks.GRASS_BLOCK)||soil.is(Blocks.PODZOL))&&Math.floorMod(hash(seed^surface.asLong()),100)<11){
            var plant=(biome.is(BiomeTags.IS_TAIGA)||biome.is(BiomeTags.IS_FOREST)?Blocks.FERN:Blocks.SHORT_GRASS).defaultBlockState();
            if(plant.canSurvive(world,above))world.setBlock(above,plant,2);
        }
    }
    private KurganSurface(){}
}
