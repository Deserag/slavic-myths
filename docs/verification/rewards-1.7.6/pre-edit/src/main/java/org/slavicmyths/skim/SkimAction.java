package org.slavicmyths.skim;
public enum SkimAction {
 IDLE(0), ARRIVE(24), RECALL(18), TAUNT(26), DODGE(12), SABER(22), FEINT(18), BOW(35), COUNTER(16), RETREAT(25), DRINK(46), REST(0), HURT(8),
 AWAKEN(60), CLAWS(39), LEAP(44), WHISTLE(48), ROAR(58), FLANK(38), GRIP(65), RAGE(64), EXHAUSTED(70), STAGGER(40), RECOVER(20), DEATH(75);
 public final int ticks; SkimAction(int ticks){this.ticks=ticks;}
}
