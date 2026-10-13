package org.slavicmyths.husbandry;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.goal.Goal;

final class DuckWaterGoal extends Goal {
    private final YardAnimal duck;private BlockPos water;private long nextSearch;private int timeout;
    DuckWaterGoal(YardAnimal d){duck=d;setFlags(EnumSet.of(Flag.MOVE));}
    @Override public boolean canUse(){
        long now=duck.level().getGameTime();if(now<nextSearch||duck.isInWater()||duck.isBaby())return false;nextSearch=now+200+duck.getRandom().nextInt(200);
        BlockPos base=duck.blockPosition();water=null;double nearest=64;
        for(BlockPos p:BlockPos.betweenClosed(base.offset(-8,-2,-8),base.offset(8,2,8))){double dist=p.distSqr(base);if(dist<nearest&&duck.level().hasChunkAt(p)&&duck.level().getFluidState(p).is(FluidTags.WATER)&&duck.level().getFluidState(p.above()).isEmpty()&&duck.level().getBlockState(p.above()).getCollisionShape(duck.level(),p.above()).isEmpty()){water=p.immutable();nearest=dist;}}
        if(water==null)return false;var path=duck.getNavigation().createPath(water,1);return path!=null&&path.canReach();
    }
    @Override public void start(){timeout=160;duck.getNavigation().moveTo(water.getX()+.5,water.getY(),water.getZ()+.5,1);}
    @Override public boolean canContinueToUse(){return timeout-->0&&!duck.isInWater()&&!duck.getNavigation().isDone();}
    @Override public void stop(){duck.getNavigation().stop();}
}
