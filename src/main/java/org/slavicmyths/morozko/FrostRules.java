package org.slavicmyths.morozko;

/** Pure policy: HP, timers and admission shared by the server and offline tests. */
public final class FrostRules {
 public static final float BOSS_HP=1400,DOUBLE_HP=50,MIRROR_HP=15,MELEE=10,BOLT=8;
 public static final int MIRROR_LIFE=600,MIRROR_CD=3600,TRIAL_CD=2400,WIN_CD=3600;
 public static final int STREAM_CD=200,ICICLE_CD=400,WAVE_CD=320;
 public static final int STREAM_DURATION=60,STREAM_HEARTBEAT_GRACE=20;
 public static final double BOUNDARY=40,AURA=8;
 public static float floor(float max){return max*.05f;}
 public static float clampedDamage(float hp,float max,float incoming){return Math.max(0,Math.min(incoming,hp-floor(max)));}
 public static boolean spikeActive(int ageAfterWarning){return ageAfterWarning>=5&&ageAfterWarning<=10;}
 public static int phase(float hp,float max){return hp<=floor(max)?4:hp<max*.3f?3:hp<max*.6f?2:1;}
 public static int tier(double cold){return cold>=100?4:cold>=75?3:cold>=50?2:cold>=25?1:0;}
 public static double rate(double distance){return distance<=3?5:distance<=6?3:distance<=8?1.5:0;}
 public static boolean eligible(double damage,int activeTicks,boolean attacked){return damage>=1||activeTicks>=300&&attacked;}
 public record Roll(boolean mirror,boolean resistance,boolean staff){}
 public static Roll roll(boolean first,java.util.random.RandomGenerator random){return roll(first,random.nextBoolean(),random.nextDouble());}
 public static Roll roll(boolean first,boolean resistance,double staffRoll){return new Roll(first,resistance,staffRoll<.15);}
 private FrostRules(){}
}
