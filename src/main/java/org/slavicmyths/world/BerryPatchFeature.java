package org.slavicmyths.world;
import java.util.Random;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.*;
import org.slavicmyths.block.FolkBerryBush;
import org.slavicmyths.registry.ModBlocks;
public final class BerryPatchFeature extends Feature<NoFeatureConfig>{
 public BerryPatchFeature(){super(NoFeatureConfig.CODEC);}
 @Override public boolean place(ISeedReader w,ChunkGenerator gen,Random r,BlockPos origin,NoFeatureConfig cfg){boolean placed=false;int x=(origin.getX()&~15)+8,z=(origin.getZ()&~15)+8;
  for(int i=0;i<6;i++){int dx=x+r.nextInt(7)-3,dz=z+r.nextInt(7)-3;BlockPos p=new BlockPos(dx,w.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES,dx,dz),dz);boolean red=w.canSeeSky(p)&&r.nextBoolean();FolkBerryBush b=(FolkBerryBush)(red?ModBlocks.RASPBERRY_BUSH.get():ModBlocks.BLUEBERRY_BUSH.get());
   if(w.isEmptyBlock(p)&&(!red||w.isEmptyBlock(p.above()))&&b.defaultBlockState().canSurvive(w,p)){b.growTo(w,p,2+r.nextInt(2));placed=true;}}
  return placed;
 }
}
