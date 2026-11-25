package org.slavicmyths.desert;
public final class SandKingRules {
 public static final int LEVEL=7, IDLE=0,WALK=1,STING=2,CLAW=3,JUMP=4,COMBO=5,SLAM=6;
 public static final int[] COMBO_TICKS={4,13,21,29,38,46};
 public static int phase(float ratio){return ratio<=.35F?3:ratio<=.70F?2:1;}
 public static int duration(int a){return switch(a){case STING->36;case CLAW->24;case JUMP->56;case COMBO->50;case SLAM->77;default->1;};}
 public static int cooldown(int a,int phase){int base=switch(a){case STING->200;case CLAW->60;case JUMP->300;case COMBO->360;case SLAM->800;default->0;};return phase==3&&a!=SLAM?(int)(base*.8):base;}
 public static int recovery(int a){return switch(a){case JUMP->40;case COMBO->50;case SLAM->70;default->10;};}
 public static boolean canUse(int a,int phase){return a!=COMBO&&a!=SLAM||a==COMBO&&phase>=2||a==SLAM&&phase>=3;}
 public static double speed(int phase){return .27*(phase==3?1.25:phase==2?1.15:1);}
 private SandKingRules(){}
}
