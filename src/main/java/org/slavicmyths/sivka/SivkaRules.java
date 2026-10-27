package org.slavicmyths.sivka;

/** Server transport limits. Speeds use blocks/second, accelerations blocks/second squared. */
public final class SivkaRules {
    public static final int TEMP_TICKS=72000, FEED_DELAY=24, CALL_DELAY=160, DEPART_DELAY=500;
    public static final float WIDTH=1.65F, HEIGHT=2.85F, HEALTH=80;
    public static final double GROUND_JUMP=.60, AIR_SPEED=40, AIR_ACCELERATION=10, AIR_BRAKING=24;
    public static final String[] FOODS={"minecraft:apple","minecraft:wheat","minecraft:carrot","slavicmyths:blueberry","slavicmyths:blueberry_pancakes","minecraft:honey_bottle","minecraft:golden_apple"};
    public static double approach(double current,double target,double step){return current+Math.clamp(target-current,-step,step);}
    public static boolean ready(double actualSpeed,int sustainedTicks,boolean ground,boolean sprint){return ground&&sprint;}
    public static double physicalWave(double phase){return .025*Math.sin(phase*Math.PI*2);}
    private SivkaRules(){}
}
