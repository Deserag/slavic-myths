package org.slavicmyths.yaga;

/** Access only: creative never awards a combat victory or advances Yaga's trust. */
public final class YagaAccessRules {
    public static boolean allowed(boolean creative, int savedStage, boolean likho, boolean tugarin, boolean nightingale) {
        return creative || savedStage >= 1 || likho || tugarin || nightingale;
    }
    public static boolean victory(String id) {
        return id.equals("name_your_misfortune") || id.equals("steppe_champion") || id.equals("defeat_nightingale");
    }
    private YagaAccessRules() {}
}
