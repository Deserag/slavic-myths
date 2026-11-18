package org.slavicmyths.firebird;

import net.minecraft.util.RandomSource;

/** Numeric rules shared by the encounter ledger and offline acceptance tests. */
public final class FirebirdRules {
    public static boolean night(long time){long phase=Math.floorMod(time,24000);return phase>=13000&&phase<23000;}
    public static boolean window(long time){long phase=Math.floorMod(time,24000);return phase>=13000&&phase<23000;}
    public static long nextNight(long day,RandomSource r){return day+2+r.nextInt(3);}
    public static int resource(int roll){if(roll<0||roll>=100)throw new IllegalArgumentException("roll");return roll<10?0:roll<40?1:2;}
    public static int delay(boolean first,RandomSource r){return first?600+r.nextInt(601):1200+r.nextInt(801);}
    private FirebirdRules(){}
}
