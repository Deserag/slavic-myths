package org.slavicmyths.desert;
/** Shared deterministic combat constants. Tick = 1/20 second. */
public final class HalaRules {
 public static final int IDLE=0,WALK=1,BEAM=2,VOLLEY=3,ORB=4,PILLARS=5,FLASH=6,DIVE=7,CLAW=8,ULTIMATE=9,HURT=10,DEATH=11;
 public static final int[] LENGTH={40,24,128,54,150,90,76,100,26,160,10,20};
 public static final int[] RELEASE={0,0,50,20,60,40,30,40,14,72,0,0};
 public static final int[] COOLDOWN={0,0,180,100,240,180,180,440,40,700,0,0};
 public static final float HP=800;
 public static final double AURA_RADIUS=10;
 public static final int AURA_DELAY=200;
 public static boolean auraPulse(int elapsed){return elapsed>=AURA_DELAY&&(elapsed-AURA_DELAY)%20==0;}
 public static int phase(float health){return health>.7F?1:health>.35F?2:3;}
 public static int stage(int heat){return heat<=0?0:heat<40?1:heat<80?2:3;}
 public static int addHeat(int heat,int amount){return Math.clamp(heat+amount,0,100);}
 public static int cool(int heat,boolean water){return Math.max(0,heat-(water?12:4));}
 public static float outgoing(int stage,float roll){return stage<2?1:stage==2?.75F:roll<.25F?0:.55F;}
 public static float healing(int stage){return stage==1?.9F:stage==2?.75F:stage==3?.6F:1;}
 public static float vulnerability(int stage){return stage==1?1.05F:stage==2?1.1F:stage==3?1.2F:1;}
 private HalaRules(){}
}
