package org.slavicmyths.garden;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
public final class GardenPatchFeature extends Feature<NoneFeatureConfiguration>{
    private final int kind;
    public GardenPatchFeature(int kind){super(NoneFeatureConfiguration.CODEC);this.kind=kind;}
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> c){
        int min=new int[]{2,3,2,2,3}[kind],max=new int[]{5,6,4,5,7}[kind],target=min+c.random().nextInt(max-min+1),placed=0;
        var bush=(PerennialBush)Gardens.block(Gardens.BERRIES[kind]+"_bush");
        for(int attempt=0;attempt<target*4&&placed<target;attempt++){
            var candidate=c.origin().offset(c.random().nextInt(9)-4,0,c.random().nextInt(9)-4);
            var pos=c.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,candidate);
            if(!c.level().hasChunkAt(pos)||!c.level().isEmptyBlock(pos)||!bush.defaultBlockState().canSurvive(c.level(),pos)||(kind==0&&!c.level().isEmptyBlock(pos.above())))continue;
            int phase=c.random().nextInt(5)==0?4:1+c.random().nextInt(3);
            if(bush.growTo(c.level(),pos,phase))placed++;
        }
        return placed>0;
    }
}
