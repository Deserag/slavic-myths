package org.slavicmyths.desert;
public final class IndrikRules {
 public static final int IDLE=0,WALK=1,RUN=2,CLAW=3,TAIL=4,QUAKE=5,BURROW=6,CHARGE=7,MINERAL=8,ROAR=9,ENRAGED=10,DEATH=11,HURT=12;
 public static final int[] LENGTH={40,32,20,40,48,64,120,48,58,60,40,20,10};
 public static final int[] RELEASE={0,0,0,22,27,37,94,15,30,30,0,0,0};
 public static final int[] COOLDOWN={0,0,0,55,95,180,300,160,125,240,0,0,0};
 public static final float HP=1500, INTERNAL_HP=1024;
 public static float logicalHealth(float health,float max){return health/max*HP;}
 public static float internalDamage(float amount,float max){return amount*max/HP;}
 public static final float SCALE=.25F;
 public static int phase(float fraction){return fraction>.70F?1:fraction>.35F?2:3;}
 public static boolean underground(float p){return p>=.22F&&p<=.78F;}
 public static int charges(int phase){return Math.clamp(phase,1,3);}
 private IndrikRules(){}
}
