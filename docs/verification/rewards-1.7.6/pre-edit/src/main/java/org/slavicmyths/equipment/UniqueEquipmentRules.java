package org.slavicmyths.equipment;

/** Balance and deterministic decisions, independent of Minecraft/bootstrap. */
public final class UniqueEquipmentRules {
    // 0.82 blocks/tick gives ~4.154 blocks at the apex with vanilla gravity/drag.
    public static final double SPEED = .40, JUMP = .82 / .42 - 1, FALL = .60;
    public static final int WIND_CHARGE_TICKS = 160, WIND_STEP_TICKS = 10, WIND_DECAY = 5;
    public static final double WIND_BASE_IMPULSE = .82;
    private static final double[] WIND_IMPULSES = windImpulses();
    /** Vanilla 1.21.1 vertical recurrence, evaluated only while rising. */
    public static double windApex(double impulse) {
        double height=0;
        for(int i=0;i<200 && impulse>0;i++){height+=impulse;impulse=(impulse-.08)*.98;}
        return height;
    }
    private static double[] windImpulses() {
        double[] values=new double[WIND_CHARGE_TICKS/WIND_STEP_TICKS+1];values[0]=WIND_BASE_IMPULSE;
        double base=windApex(WIND_BASE_IMPULSE);
        for(int step=1;step<values.length;step++) {
            double target=base*(1+2.0*step/(values.length-1)),low=WIND_BASE_IMPULSE,high=2;
            for(int i=0;i<40;i++){double mid=(low+high)/2;if(windApex(mid)<target)low=mid;else high=mid;}
            values[step]=high;
        }
        return values;
    }
    public static int windStep(int charge){return Math.clamp(charge,0,WIND_CHARGE_TICKS)/WIND_STEP_TICKS;}
    public static double windImpulse(int step){return WIND_IMPULSES[Math.clamp(step,0,WIND_IMPULSES.length-1)];}
    public static final double LOW_HEALTH = .35, STANCE_REDUCTION = .15, STANCE_KB = .15;
    public static final double CONTROL_DURATION = .65, STRONG_KB = .4, INDOMITABLE_KB = .35;
    public static final int KB_DURATION = 100, KB_COOLDOWN = 300;
    public static final int SIGHT_RADIUS = 24, SIGHT_DURATION = 100, SIGHT_COOLDOWN = 800;
    public static final float REFLECT_CHANCE = .25F, REFLECT_MULTIPLIER = 1.25F;
    public static boolean stance(float health, float max) { return health > 0 && max > 0 && health <= max * LOW_HEALTH; }
    public static int controlDuration(int ticks) { return ticks < 0 ? ticks : Math.max(1, (int)Math.ceil(ticks * CONTROL_DURATION)); }
    public static boolean ready(long now, long ready) { return now >= ready; }
    public static float reflected(boolean blocked, float damage, boolean validOwner, boolean recursion, float roll) {
        return blocked && damage > 0 && validOwner && !recursion && roll >= 0 && roll < REFLECT_CHANCE ? damage * REFLECT_MULTIPLIER : 0;
    }
    /** Preserve fraction worn, rounded against free repair. Custom max-damage still wins. */
    public static int upgradedDamage(int damage, int oldMax, int newMax) {
        if(damage <= 0 || oldMax <= 0 || newMax <= 0) return Math.max(0,damage);
        return Math.min(newMax, (int)Math.ceil((double)damage * newMax / oldMax));
    }
    private UniqueEquipmentRules() { }
}
