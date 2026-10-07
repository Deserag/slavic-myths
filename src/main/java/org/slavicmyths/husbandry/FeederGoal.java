package org.slavicmyths.husbandry;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;

/** Bounded, staggered mob-local search. Never loads chunks and never enters love mode. */
public final class FeederGoal extends Goal {
    private final Animal animal;
    private BlockPos target;
    private long nextSearch;
    private int timeout;
    public FeederGoal(Animal a){animal=a;nextSearch=a.level().getGameTime()+a.getRandom().nextInt(40);setFlags(EnumSet.of(Flag.MOVE));}
    private boolean hungry(){return animal.isBaby()||animal.getHealth()<animal.getMaxHealth();}
    private YardStorage feeder(BlockPos p){return animal.level().getBlockEntity(p) instanceof YardStorage s && !s.food().isEmpty() && YardFeeding.accepts(animal,s.food())?s:null;}
    @Override public boolean canUse(){
        long now=animal.level().getGameTime();long cooldown=animal.getPersistentData().getLong("SlavicMythsFeederUntil");
        // Ignore an obsolete absolute time after transferring the entity to another world.
        if(cooldown>now+600){animal.getPersistentData().remove("SlavicMythsFeederUntil");cooldown=0;}
        if(!hungry()||now<cooldown||now<nextSearch)return false;nextSearch=now+40;
        target=YardSearch.find(animal,10,Husbandry.FEEDER.get(),p->feeder(p)!=null);
        return target!=null;
    }
    @Override public void start(){timeout=200;animal.getNavigation().moveTo(target.getX()+.5,target.getY(),target.getZ()+.5,1.05);}
    @Override public boolean canContinueToUse(){return timeout>0&&hungry()&&target!=null&&feeder(target)!=null&&!animal.getNavigation().isDone();}
    @Override public void tick(){
        timeout--;
        if(animal.distanceToSqr(target.getX()+.5,target.getY()+.5,target.getZ()+.5)<=2.25){
            YardStorage s=feeder(target);if(s!=null){var food=s.extract(false).getFirst();animal.heal(2);if(animal.isBaby())animal.ageUp(20,true);animal.getPersistentData().putLong("SlavicMythsFeederUntil",animal.level().getGameTime()+600);YardFeeding.eaten(animal,food);}
            timeout=0;
        }
    }
    @Override public void stop(){animal.getNavigation().stop();target=null;nextSearch=animal.level().getGameTime()+40;}
    @Override public boolean requiresUpdateEveryTick(){return true;}
}
