package org.slavicmyths.hunt;

import java.util.function.Predicate;
import static org.slavicmyths.hunt.BossRules.Move;

/** Likho-only choice policy; damage, telegraphs and cooldowns remain in BossRules/WorldBoss. */
public final class LikhoCombatRules {
 public static Move choose(int phase,double distance,boolean visible,boolean player,int closeEnemies,Predicate<Move> ready){
  if(!visible)return Move.IDLE;
  if(closeEnemies>=2){
   if(distance<=4&&ready.test(Move.SWEEP))return Move.SWEEP;
   if(phase>=2&&distance<=12&&ready.test(Move.GROUND))return Move.GROUND;
  }
  if(phase==3&&distance>=8&&distance<=15&&ready.test(Move.LEAP))return Move.LEAP;
  if(phase==3&&distance>4&&ready.test(Move.CHASE))return Move.CHASE;
  if(phase>=2&&distance<=10&&player&&ready.test(Move.GAZE))return Move.GAZE;
  if(phase>=2&&distance<=12&&ready.test(Move.GROUND))return Move.GROUND;
  if(distance<=3.5&&ready.test(Move.GRAB))return Move.GRAB;
  if(distance<=4&&ready.test(Move.SWEEP))return Move.SWEEP;
  return Move.IDLE;
 }
 public static boolean phaseBoundary(Move move){return move==Move.IDLE||move==Move.RECOVERY;}
 public static boolean committed(Move move){return move!=Move.IDLE&&move!=Move.RECOVERY&&move!=Move.TRANSITION&&move!=Move.CHASE;}
 private LikhoCombatRules(){}
}
