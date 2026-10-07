package org.slavicmyths.husbandry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.block.Block;

final class YardSearch {
    /** Vertical band covers an enclosure and its ramps; Euclidean radius caps candidates. */
    static BlockPos find(PathfinderMob mob,int radius,Block block,java.util.function.Predicate<BlockPos> valid){
        BlockPos base=mob.blockPosition();
        var candidates=new java.util.ArrayList<BlockPos>();
        for(BlockPos mutable:BlockPos.betweenClosed(base.offset(-radius,-3,-radius),base.offset(radius,3,radius))){
            double d=mutable.distSqr(base);if(d>radius*radius||!mob.level().hasChunkAt(mutable)||!mob.level().getBlockState(mutable).is(block)||!valid.test(mutable))continue;
            candidates.add(mutable.immutable());candidates.sort(java.util.Comparator.comparingDouble(p->p.distSqr(base)));
            if(candidates.size()>4)candidates.removeLast();
        }
        // A blocked nearest trough/nest must not hide the next accessible one.
        // At most four path attempts per cadence, regardless of enclosure size.
        for(BlockPos candidate:candidates){var path=mob.getNavigation().createPath(candidate,1);if(path!=null&&path.canReach())return candidate;}
        return null;
    }
    private YardSearch(){}
}
