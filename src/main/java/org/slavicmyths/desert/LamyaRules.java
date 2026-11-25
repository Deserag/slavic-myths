package org.slavicmyths.desert;
public final class LamyaRules {
 public static final int IDLE=0,WALK=1,RUN=2,FIRE=3,SOLAR=4,WIND=5,CLAW=6,SWEEP=7,STING=8,DROUGHT=9,FIRE_WIND=10,SUN_WIND=11,FIRE_SUN=12,ULTIMATE=13,SPAWN=14,PHASE=15,HURT=16,DEATH=17;
 public static final int[] LENGTH={48,36,22,86,84,80,52,62,64,100,96,100,96,140,70,55,10,20};
 public static final int[] COOLDOWN={0,0,0,115,140,110,55,95,100,180,180,180,180,440,0,0,0,0};
 public static final String[] SOUND={"ambient","crawl","crawl","fire","solar","wind","claw","tail_sweep","tail_sting","drought","combo_fire_wind","combo_sun_wind","combo_fire_sun","ultimate","spawn","phase","hurt","death"};
 public static final float HP=2200, INTERNAL_HP=1024;
 public static int phase(float fraction){return fraction>.70F?1:fraction>.35F?2:3;}
 public static boolean attack(int a){return a>=FIRE&&a<=ULTIMATE;}
 public static int mask(int a){return switch(a){case FIRE->1;case SOLAR,DROUGHT->2;case WIND->4;case FIRE_WIND->5;case SUN_WIND->6;case FIRE_SUN->3;case ULTIMATE->7;default->0;};}
 public static boolean active(int a,int age){return attack(a)&&age>=release(a)&&age<=end(a);}
 public static int release(int a){return Math.round(LENGTH[a]*.46F);}
 public static int end(int a){return Math.round(LENGTH[a]*.72F);}
 private LamyaRules(){}
}
