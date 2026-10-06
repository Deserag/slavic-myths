package org.slavicmyths.entity;

/** Server-local attack clock. Reload cancels an unfinished attack instead of replaying damage. */
public final class AttackTimeline {
 public enum Phase { IDLE, TELEGRAPH, ACTIVE, RECOVERY, COOLDOWN }
 private Phase phase=Phase.IDLE;private int ticks,active,recovery,cooldown,elapsed;
 public boolean start(int telegraph,int active,int recovery,int cooldown){
  if(phase!=Phase.IDLE)return false;
  if(telegraph<1||active<1||recovery<1||cooldown<1)throw new IllegalArgumentException("Attack durations must be positive");
  this.phase=Phase.TELEGRAPH;this.ticks=telegraph;this.active=active;this.recovery=recovery;this.cooldown=cooldown;elapsed=0;return true;
 }
 public Phase phase(){return phase;}
 public int elapsed(){return elapsed;}
 public boolean ready(){return phase==Phase.IDLE;}
 public void cancel(){phase=Phase.IDLE;ticks=elapsed=0;}
 /** Returns true exactly on entering the active window. */
 public boolean tick(){
  if(phase==Phase.IDLE)return false;
  elapsed++;if(--ticks>0)return false;elapsed=0;
  switch(phase){case TELEGRAPH-> {phase=Phase.ACTIVE;ticks=active;return true;}
   case ACTIVE->{phase=Phase.RECOVERY;ticks=recovery;}
   case RECOVERY->{phase=Phase.COOLDOWN;ticks=cooldown;}
   case COOLDOWN->cancel();default->{} }
  return false;
 }
}
