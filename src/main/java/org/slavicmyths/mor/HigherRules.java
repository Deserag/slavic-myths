package org.slavicmyths.mor;
/** Shared arena limits and boundary policy, expressed in loaded game ticks. */
public final class HigherRules {
 public static final int MAX_ZONES=12;
 public static boolean phase2(float hp,float max){return max>0&&hp<=max*.5F;}
 public static int groundPeriod(int kind,boolean phase){return kind==6&&phase?30:60;}
 public static int life(boolean cloud){return cloud?120:180;}
 public static boolean harms(boolean cloud,int exposure){return cloud||exposure>=60;}
 public static boolean ember(int tick){return tick==24||tick==29||tick==34;}
 private HigherRules(){}
}
