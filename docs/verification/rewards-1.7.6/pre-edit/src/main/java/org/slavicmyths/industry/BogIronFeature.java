package org.slavicmyths.industry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
/** Finite surface-relative ore patch; no player or world polling. */
public final class BogIronFeature extends Feature<NoneFeatureConfiguration> {
    public BogIronFeature(){super(NoneFeatureConfiguration.CODEC);}
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){
        var world=c.level();if(!world.getLevel().dimension().equals(net.minecraft.world.level.Level.OVERWORLD))return false;var r=c.random();if(r.nextFloat()>=.45F)return false;int veins=r.nextFloat()<.05F?2:1,placed=0;
        for(int attempt=0;attempt<veins;attempt++){
            int x=c.origin().getX()+3+r.nextInt(10),z=c.origin().getZ()+3+r.nextInt(10);int surface=world.getHeight(Heightmap.Types.OCEAN_FLOOR_WG,x,z)-1;
            if(surface<49)continue;int y=surface-1-r.nextInt(5),size=2+r.nextInt(5);
            for(int i=0;i<size;i++){
                var p=new BlockPos(x+(i%3)-1,y-(i/3),z+((i/3)&1));var old=world.getBlockState(p);
                if(!world.isOutsideBuildHeight(p)&&!old.hasBlockEntity()&&(old.is(BlockTags.DIRT)||old.is(BlockTags.BASE_STONE_OVERWORLD))&&old.getFluidState().isEmpty()&&world.getBlockState(p.below()).isSolid()){
                    world.setBlock(p,Industry.ORE.get().defaultBlockState(),2);placed++;
                }
            }
        }return placed>0;
    }
}
