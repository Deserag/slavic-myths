package org.slavicmyths.mor;

/** Pure policy shared by runtime and offline boundary tests. All durations are game ticks. */
public final class MorRules {
    public static final int TRANSFORM=50;
    public static int transformTick(int remaining){return Math.max(0,remaining-1);}
    public static boolean canApply(int kind,int state,boolean alive,boolean protectedEntity){return kind>=1&&kind<=6&&state==0&&alive&&!protectedEntity;}
    public static boolean clonesNeeded(boolean clone,boolean summoned,float hp,float max){return !clone&&!summoned&&max>0&&hp<=max*.5F;}
    public static boolean flaskRange(double squared){return squared<=9;}
    public static boolean release(int skill,int tick){return switch(skill){case 1->tick==24;case 2,3->tick==20;case 4->tick==19||tick==25;case 5->tick==32;case 6->tick==16;case 7->tick==22||tick==27||tick==32;case 8->tick>=18&&tick<=28;case 9->tick==30;default->false;};}
    public static boolean natural(boolean overworld,long dayTime,double spawnDistanceSquared,boolean allowedReason,int roll) {
        return overworld && dayTime/24000>=7 && spawnDistanceSquared>160*160 && allowedReason && roll<4;
    }
    public static float health(float current,float oldMax,float newMax) {
        return Math.min(newMax,Math.max(0,oldMax>0?current/oldMax*newMax:0));
    }
    public static boolean noon(boolean overworld,long time){long t=Math.floorMod(time,24000);return overworld&&t>=5800&&t<=6600;}
    public static int windup(int skill){return switch(skill){case 1->24;case 2,3->20;case 4->14;case 5->10;case 6->16;case 7->22;case 8->18;case 9->30;default->0;};}
    public static int duration(int skill){return switch(skill){case 1,2->32;case 3->28;case 4->37;case 5->42;case 6->24;case 7->41;case 8->40;case 9->42;default->0;};}
    public static int cooldown(int skill){return switch(skill){case 1,8->240;case 2->360;case 3->400;case 4->220;case 5->260;case 6->320;case 7->180;default->0;};}
    private MorRules(){}
}
