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
        var site=BayunPlacement.prepare(w,source);if(site.failure!=null)return false;
        var b=BayunContent.ENTITY.get().create(w.getLevel());if(b==null)return false;
        b.home=site.anchor;b.moveTo(site.anchor.getX()+.5,site.anchor.getY(),site.anchor.getZ()+.5,0,0);
        return org.slavicmyths.yaga.YagaPlacement.commit(w,site,()->w.addFreshEntity(b),()->{
            b.discard();BayunHomes.get(w.getLevel()).forget(site.anchor,b.getUUID());
        }).ok;
    }
}
