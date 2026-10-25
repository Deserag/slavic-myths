package org.slavicmyths.navya;

import net.minecraft.core.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.levelgen.feature.*;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class NavyaTreeFeature extends Feature<NoneFeatureConfiguration> {
    public NavyaTreeFeature() { super(NoneFeatureConfiguration.CODEC); }
    @Override public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        int roll=context.random().nextInt(12);
        return grow(context.level(),context.origin(),context.random(),roll==0?2:roll<5?0:1);
    }
    public static boolean grow(WorldGenLevel world,BlockPos origin,RandomSource random,int size) {
        if(!NavyaBlocks.soil(world.getBlockState(origin.below()))) return false;
        var plan=new NavyaTreePlan(size,random.nextLong());
        if(size==2)for(int x=0;x<2;x++)for(int z=0;z<2;z++)
            if(!NavyaBlocks.soil(world.getBlockState(origin.offset(x,-1,z))))return false;
        var cells=new java.util.HashSet<>(plan.logs.keySet());cells.addAll(plan.foliage);
        for(var cell:cells) {
            var pos=origin.offset(cell.x(),cell.y(),cell.z());
            if(!world.hasChunkAt(pos) || !world.ensureCanWrite(pos) || pos.getY()<world.getMinBuildHeight() || pos.getY()>=world.getMaxBuildHeight()
                    || !world.getLevel().getWorldBorder().isWithinBounds(pos)) return false;
            var state=world.getBlockState(pos);
            if(!state.isAir() && !state.is(NavyaBlocks.get("navya_sapling")) && !state.is(NavyaBlocks.get("navya_shrub"))) return false;
        }
        for(var cell:plan.logs.keySet())for(Direction face:Direction.values()) {
            var neighbor=origin.offset(cell.x(),cell.y(),cell.z()).relative(face);
            if(!world.hasChunkAt(neighbor) || !world.ensureCanWrite(neighbor))return false;
            if(world.getBlockState(neighbor).is(net.minecraft.tags.BlockTags.LOGS))return false;
        }
        // Preflight all cells; never carve structures/terrain or load missing chunks.
        for(var e:plan.logs.entrySet()) {
            var c=e.getKey();var timber=e.getValue();
            world.setBlock(origin.offset(c.x(),c.y(),c.z()),NavyaBlocks.get("navya_log").defaultBlockState()
                    .setValue(RotatedPillarBlock.AXIS,Direction.Axis.values()[timber.axis()])
                    .setValue(NavyaBlocks.WEATHERED,timber.weathered()),2);
        }
        for(var c:plan.foliage) world.setBlock(origin.offset(c.x(),c.y(),c.z()),NavyaBlocks.get("navya_dead_leaves").defaultBlockState()
                .setValue(LeavesBlock.DISTANCE,1).setValue(LeavesBlock.PERSISTENT,false),2);
        return true;
    }
}
