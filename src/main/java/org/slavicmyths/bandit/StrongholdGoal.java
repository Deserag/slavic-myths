package org.slavicmyths.bandit;
import java.util.*;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
public final class StrongholdGoal extends Goal {
 private final BanditEntity mob;private int time,nextCheck;
 public StrongholdGoal(BanditEntity b){mob=b;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
 @Override public boolean canUse(){return mob.campTotal==StrongholdRecords.TOTAL&&mob.camp!=null&&mob.getTarget()==null;}
 @Override public void start(){time=0;}
 @Override public void stop(){mob.ambientAction(0);mob.getNavigation().stop();}
 @Override public void tick(){
  if(++time%20!=0)return;ServerWorld w=(ServerWorld)mob.level;StrongholdRecords records=StrongholdRecords.get(w);int state=records.state(w,mob.camp,mob.zone);StrongholdRecords.Record record=records.record(mob.camp);
  LivingEntity suspect=mob.suspect;
  if(suspect!=null){
   if(!suspect.isAlive()||mob.distanceToSqr(suspect)>1600){mob.suspect=null;records.calm(mob.camp,mob.zone);return;}
   mob.getLookControl().setLookAt(suspect,30,30);
   if(state==2){mob.engage(suspect);mob.suspect=null;return;}
   if(record.cleared){mob.suspect=null;return;}
   BlockPos bell=record.bells[mob.zone];
   if(w.getGameTime()-mob.suspectSince<50)return;
   if(records.intact(w,bell)){
    if(mob.blockPosition().distSqr(bell)<9){records.ring(w,mob.camp,mob.zone);mob.engage(suspect);mob.suspect=null;}
    else mob.getNavigation().moveTo(bell.getX()+.5,bell.getY(),bell.getZ()+.5,1.05);
   }else{mob.engage(suspect);mob.suspect=null;}return;
  }
  if(state==2){if(time%100==0&&record.bells[mob.zone]!=null){BlockPos p=record.bells[mob.zone];mob.getNavigation().moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,.85);}return;}
  if(mob.home==null)return;
  if(mob.duty()==2||mob.duty()==3){
   if(time%100==0)mob.getNavigation().moveTo(mob.home.getX()+.5,mob.home.getY(),mob.home.getZ()+.5,.65);
   if(mob.blockPosition().distSqr(mob.home)<9&&time%80==0){mob.ambientAction(1);mob.swing(Hand.MAIN_HAND,true);mob.playSound(mob.duty()==3?SoundEvents.ANVIL_USE:SoundEvents.WOOD_HIT,.35F,1);}
   else if(time%80==20)mob.ambientAction(0);
  }else if(mob.duty()==1){if(time%120==0){BlockPos p=mob.home.offset((time/120%2==0?1:-1)*5,0,(time/240%2==0?1:-1)*3);mob.getNavigation().moveTo(p.getX()+.5,p.getY(),p.getZ()+.5,.65);}}
  else if(time%100==0&&mob.blockPosition().distSqr(mob.home)>9)mob.getNavigation().moveTo(mob.home.getX()+.5,mob.home.getY(),mob.home.getZ()+.5,.65);
 }
}
