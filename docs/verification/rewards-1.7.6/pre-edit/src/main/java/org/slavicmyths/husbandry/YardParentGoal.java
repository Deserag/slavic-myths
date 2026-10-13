package org.slavicmyths.husbandry;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;

/** Vanilla follow-parent cadence, filtered by entity type (all three use YardAnimal). */
final class YardParentGoal extends Goal {
    private final YardAnimal child;private YardAnimal parent;private int pathDelay;
    YardParentGoal(YardAnimal c){child=c;setFlags(EnumSet.of(Flag.MOVE));}
    @Override public boolean canUse(){
        if(!child.isBaby())return false;parent=null;double nearest=Double.MAX_VALUE;
        for(YardAnimal a:child.level().getEntitiesOfClass(YardAnimal.class,child.getBoundingBox().inflate(8,4,8),a->a.getType()==child.getType()&&!a.isBaby())){
            double dist=child.distanceToSqr(a);if(dist<nearest){parent=a;nearest=dist;}
        }
        return parent!=null&&nearest>=9;
    }
    @Override public boolean canContinueToUse(){return child.isBaby()&&parent!=null&&parent.isAlive()&&child.distanceToSqr(parent)>=9&&child.distanceToSqr(parent)<=256;}
    @Override public void start(){pathDelay=0;}
    @Override public void tick(){if(--pathDelay<=0){pathDelay=adjustedTickDelay(10);child.getNavigation().moveTo(parent,1.1);}}
    @Override public void stop(){parent=null;child.getNavigation().stop();}
}
