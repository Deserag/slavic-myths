package org.slavicmyths.hunt;
/** Fixed tuning, also used by headless boundary checks. */
public final class HuntRules {
 public static double nightSpeed(boolean oven,boolean rage,boolean night,boolean fullMoon,boolean prey){return (oven?.245:.335)*(rage?(oven?1.08:1.07):1)*( !oven&&night&&fullMoon?1.05:1)*(!oven&&prey?1.10:1);}
 public static double belt(boolean night,boolean fullMoon){return night?(fullMoon?.11:.08):0;}
 public static int cooldown(int base,boolean rage){return rage?(int)Math.ceil(base*.85):base;}
 public enum Move{
  IDLE(0,0,0),MELEE(8,8,22),SLAM(18,22,120),ASH(12,10,140),TRAIL(10,28,160),EMBER(10,10,100),
  CLAW(5,6,18),POUNCE(10,14,90),COMBO(8,26,110),HOWL(20,8,200),DODGE(1,10,50);
  public final int windup,recovery,cooldown;Move(int w,int r,int c){windup=w;recovery=r;cooldown=c;}
 }
}
