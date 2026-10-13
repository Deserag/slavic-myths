package org.slavicmyths.sanctuary;
public final class SanctuaryRules {
 public static int shortened(int duration){return Math.max(1,(int)Math.ceil(duration*.65));}
 public static boolean healTick(int remaining){return remaining>0&&remaining%20==1;}
 public static int guardDuration(int duration,double reduction){return Math.max(1,(int)Math.ceil(duration*(1-reduction)));}
 private SanctuaryRules(){}
}
