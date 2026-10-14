package org.slavicmyths.rpg;

import java.util.List;

/** Pure rules shared by server dispatch, descriptions and offline boundary tests. */
public final class AdvancedRuneRules {
    public static final List<String> IDS=List.of("counterstrike_iii","hunter_iii","last_stand_iii","depths_iii","veles_oath_iv","unbreakable_iv","perun_rage_iv","forest_spirit_iv","mastery_iv");
    public static final int COUNTER_WINDOW=80,COUNTER_CD=400,HUNTER_CD=140,LAST_CD=900,OATH_WINDOW=200;
    public static final double COUNTER_HP=2,HUNTER_RANGE=12,HUNTER_BONUS=.12,LAST_THRESHOLD=.35,LAST_REDUCTION=.12,DEPTH_BONUS=.20,MINING_CAP=.30,OATH_STEP=.02;
    public static double mining(boolean one,boolean two,boolean depths){return 1+Math.min(MINING_CAP,(two?.20:one?.10:0)+(depths?DEPTH_BONUS:0));}
    public static boolean lastStand(double hp,double max,double damage,boolean combat){return combat&&damage>0&&max>0&&hp<=max*LAST_THRESHOLD;}
    public static boolean hunter(double distanceSquared){return distanceSquared>=HUNTER_RANGE*HUNTER_RANGE;}
    public static double oath(int marks,double existing){return (1+Math.min(RuneBalance.DAMAGE_CAP,existing+Math.clamp(marks,0,5)*OATH_STEP))/(1+existing);}
    private AdvancedRuneRules(){}
}
