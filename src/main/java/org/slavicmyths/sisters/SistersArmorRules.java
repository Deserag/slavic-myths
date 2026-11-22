package org.slavicmyths.sisters;

/** Shared calculations keep armor and temporary rune grants from restoring one another's stale state. */
public final class SistersArmorRules {
    public static boolean flightBaseline(boolean mayfly,boolean armorGranted){return mayfly&&!armorGranted;}
    public static boolean afterRune(boolean baseline,boolean armorFull){return baseline||armorFull;}
    public static double speedSupplement(int potionAmplifier,boolean full,boolean aura){
        double existing=potionAmplifier<0?1:1+.2*(potionAmplifier+1);
        double desired=full?(aura?1.8:1.6):1;
        return Math.max(0,desired/existing-1);
    }
    private SistersArmorRules(){}
}
