package org.slavicmyths.world;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.util.RandomSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.*;
import org.slavicmyths.block.FolkBerryBush;
import org.slavicmyths.registry.ModBlocks;
public final class BerryPatchFeature extends Feature<NoneFeatureConfiguration>{
 public BerryPatchFeature(){super(NoneFeatureConfiguration.CODEC);}
 @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context){WorldGenLevel w=context.level();ChunkGenerator gen=context.chunkGenerator();RandomSource r=context.random();BlockPos origin=context.origin();NoneFeatureConfiguration cfg=context.config();boolean placed=false;int x=(origin.getX()&~15)+8,z=(origin.getZ()&~15)+8;
  for(int i=0;i<6;i++){int dx=x+r.nextInt(7)-3,dz=z+r.nextInt(7)-3;BlockPos p=new BlockPos(dx,w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,dx,dz),dz);boolean red=w.canSeeSky(p)&&r.nextBoolean();FolkBerryBush b=(FolkBerryBush)(red?ModBlocks.RASPBERRY_BUSH.get():ModBlocks.BLUEBERRY_BUSH.get());
   if(w.isEmptyBlock(p)&&(!red||w.isEmptyBlock(p.above()))&&b.defaultBlockState().canSurvive(w,p)){b.growTo(w,p,2+r.nextInt(2));placed=true;}}
  return placed;
 }
}
