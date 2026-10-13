package org.slavicmyths.industry;
import java.util.List;
/** Pure admission and stacking rules shared by live code and offline checks. */
public final class IndustryRules {
    public static double speed(List<String> runes,boolean ring){return (ring?1.12:1)*(runes.contains("mining_2")?1.20:runes.contains("mining_1")?1.10:1);}
    public static int fortune(int level,boolean rune,boolean silk){return rune&&!silk?Math.min(4,Math.max(0,level)+1):level;}
    public static boolean canFinish(boolean refining,boolean matches,int metal,int flux,int output,int capacity,boolean compatible){return matches&&metal>0&&(!refining||flux>0)&&output>=0&&output<capacity&&compatible;}
    public static int restockPeriod(long dayTime){long time=Math.floorMod(dayTime,24000);return time>=14000?2:time>=2000?1:0;}
    private IndustryRules(){}
}
