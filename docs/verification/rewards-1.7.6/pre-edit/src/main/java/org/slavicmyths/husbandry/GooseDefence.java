package org.slavicmyths.husbandry;
import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;

final class GooseDefence extends Goal {
    private final YardAnimal goose;private Player threat;private int elapsed,bite;private boolean attacked;private long nextWarning;
    GooseDefence(YardAnimal g){goose=g;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
    void warn(Player p,boolean attacked){if(!goose.level().isClientSide&&!p.isCreative()&&!p.isSpectator()&&goose.level().getGameTime()>=nextWarning&&threat==null){threat=p;this.attacked=attacked;}}
    @Override public boolean canUse(){if(threat!=null&&!valid())threat=null;return !goose.isBaby()&&threat!=null;}
    private boolean valid(){return threat!=null&&threat.isAlive()&&!threat.isCreative()&&!threat.isSpectator()&&goose.distanceToSqr(threat)<=64;}
    @Override public void start(){elapsed=0;bite=0;goose.setDefence(1);goose.playSound(Husbandry.GOOSE_HISS.get(),1F,1F);goose.getNavigation().stop();}
    @Override public boolean canContinueToUse(){return elapsed<100&&valid();}
    @Override public void tick(){
        elapsed++;goose.getLookControl().setLookAt(threat,30,30);if(bite>0)bite--;
        if(elapsed<=20)return;
        if(elapsed==21&&!attacked&&goose.level().getEntitiesOfClass(YardAnimal.class,goose.getBoundingBox().inflate(6),a->a.kind==0&&a.isBaby()&&a.distanceToSqr(threat)<=4).isEmpty()){elapsed=100;return;}
        goose.setDefence(bite>12?3:2);
        if(elapsed%10==1)goose.getNavigation().moveTo(threat,1.15);
        if(goose.distanceToSqr(threat)<2.25&&bite==0){threat.hurt(goose.damageSources().mobAttack(goose),1);bite=20;goose.setDefence(3);}
    }
    @Override public void stop(){goose.setDefence(0);goose.getNavigation().stop();threat=null;nextWarning=goose.level().getGameTime()+120;}
    @Override public boolean requiresUpdateEveryTick(){return true;}
}
