package org.slavicmyths.wood;
import java.util.Random;
import net.minecraft.block.*;
import net.minecraft.block.trees.Tree;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.*;
public final class WoodlandGrower extends Tree {
 private final String species;
 public WoodlandGrower(String s){species=s;}
 @Override protected ConfiguredFeature<BaseTreeFeatureConfig,?> getConfiguredFeature(Random r,boolean flowers){return null;}
 // The four custom geometries use NoFeatureConfig rather than vanilla trunk/foliage placers.
 @Override public boolean growTree(ServerWorld w,ChunkGenerator g,BlockPos p,BlockState sapling,Random r){return WoodlandWorldgen.TREES.get(species).get().grow(w,r,p);}
}
