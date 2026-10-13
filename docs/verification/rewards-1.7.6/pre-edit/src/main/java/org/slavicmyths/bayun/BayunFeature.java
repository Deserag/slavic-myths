package org.slavicmyths.bayun;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class BayunFeature extends Feature<NoneFeatureConfiguration> {
    public BayunFeature(){super(NoneFeatureConfiguration.CODEC);}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){return place(c.level(),c.origin());}
    public static boolean place(WorldGenLevel w,BlockPos source){
        BlockPos center=w.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,source);int y=center.getY();
        // Validate the entire small site BEFORE writing; no terrain search or chunk tickets.
        for(int x=-6;x<=6;x++)for(int z=-6;z<=6;z++){BlockPos p=center.offset(x,0,z);if(!w.hasChunkAt(p))return false;int surface=w.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,p.getX(),p.getZ());if(Math.abs(surface-y)>2)return false;var ground=w.getBlockState(new BlockPos(p.getX(),surface-1,p.getZ()));if(!ground.is(BlockTags.DIRT)&&!ground.is(Blocks.STONE)&&!ground.is(Blocks.MOSS_BLOCK))return false;for(int dy=0;dy<=3;dy++){var state=w.getBlockState(new BlockPos(p.getX(),y+dy,p.getZ()));if(!state.isAir()&&!state.canBeReplaced()&&!state.is(BlockTags.LEAVES))return false;}if(!w.getFluidState(p).isEmpty())return false;}
        for(int x=-2;x<=2;x++)for(int z=-2;z<=2;z++){BlockPos floor=center.offset(x,-1,z);for(int depth=0;depth<3;depth++){BlockPos p=floor.below(depth);if(w.getBlockState(p).isSolid())break;w.setBlock(p,Blocks.COBBLESTONE.defaultBlockState(),2);}w.setBlock(floor,(Math.floorMod(x+z,3)==0?Blocks.MOSSY_COBBLESTONE:Blocks.STONE_BRICKS).defaultBlockState(),2);for(int dy=0;dy<=2;dy++)w.setBlock(center.offset(x,dy,z),Blocks.AIR.defaultBlockState(),2);}
        for(int x:new int[]{-4,4})for(int z:new int[]{-4,4}){BlockPos p=center.offset(x,-1,z);for(int depth=1;depth<=3;depth++){BlockPos support=p.below(depth);if(w.getBlockState(support).isSolid())break;w.setBlock(support,Blocks.DARK_OAK_LOG.defaultBlockState(),2);}w.setBlock(p,Blocks.DARK_OAK_LOG.defaultBlockState(),2);w.setBlock(p.above(),Blocks.MOSS_BLOCK.defaultBlockState(),2);}
        BlockPos post=center.offset(3,0,3);for(int dy=0;dy<3;dy++)w.setBlock(post.above(dy),Blocks.IRON_BARS.defaultBlockState(),2);
        var b=BayunContent.ENTITY.get().create(w.getLevel());if(b==null)return false;b.home=center;b.moveTo(center.getX()+.5,y,center.getZ()+.5,0,0);return w.addFreshEntity(b);
    }
}
