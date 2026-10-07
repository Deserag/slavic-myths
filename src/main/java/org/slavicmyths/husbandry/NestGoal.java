package org.slavicmyths.husbandry;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;

final class NestGoal extends Goal {
    private final YardAnimal bird;private BlockPos target;private int timeout;
    NestGoal(YardAnimal b){bird=b;setFlags(EnumSet.of(Flag.MOVE));}
    private YardStorage nest(){return target!=null&&bird.level().getBlockEntity(target) instanceof YardStorage s&&s.hasRoom()&&bird.level().getBlockState(target).is(Husbandry.NEST.get())?s:null;}
    @Override public boolean canUse(){if(!bird.eggReady())return false;target=YardSearch.find(bird,12,Husbandry.NEST.get(),p->bird.level().getBlockEntity(p) instanceof YardStorage s&&s.hasRoom());return true;}
    @Override public void start(){timeout=200;if(target!=null)bird.getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,1);else finish(false);}
    private void finish(boolean store){var s=nest();if(!store||s==null||s.insert(bird.egg(),1)==0)bird.spawnAtLocation(bird.egg());bird.playSound(net.minecraft.sounds.SoundEvents.CHICKEN_EGG,.8F,1);bird.resetEgg();timeout=0;}
    @Override public boolean canContinueToUse(){return timeout>0&&bird.eggReady();}
    @Override public void tick(){if(timeout<=0)return;timeout--;if(nest()==null){finish(false);return;}if(bird.distanceToSqr(target.getX()+.5,target.getY()+.5,target.getZ()+.5)<=2.25)finish(true);else if(timeout==0||bird.getNavigation().isDone())finish(false);}
    @Override public void stop(){bird.getNavigation().stop();target=null;/* An interruption keeps the ready egg for the next attempt. */}
    @Override public boolean requiresUpdateEveryTick(){return true;}
}
