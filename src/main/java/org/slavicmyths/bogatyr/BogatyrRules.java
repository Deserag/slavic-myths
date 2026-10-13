package org.slavicmyths.bogatyr;

/** Pure server-clock constants shared by simulation, renderer and checks. */
public final class BogatyrRules {
    public enum State {
        IDLE, FOLLOW, GUARD, PURSUE, TAUNT_CAST, SHIELD_RAISE, SHIELD_HOLD, SHIELD_RECOVER,
        PROTECTOR_DASH, MACE_WINDUP, SHOCKWAVE_CAST, UNYIELDING, RETREAT_START,
        RETREAT_MOVING, HEAL_DRINK, RECOVERY, RETURNING, DOWN_FOR_ENCOUNTER,
        ACQUIRE_TARGET, CHASE, FIST_WINDUP, FIST_SLAM, STOMP_TELEGRAPH, STOMP_PULSE,
        GRAB_START, GRAB_HOLD, GRAB_THROW, FEED_SUMMON, FEED_GRAB, FEED_CHEW,
        BREATH_WINDUP, BREATH_ACTIVE, CHARGE_WINDUP, CHARGE_RUNNING, CONTEST,
        STUNNED, RAGE_TRANSITION, DEATH
    }
    public static final int HERO_HP=280, BOSS_HP=900, RECOVERY_TICKS=1100;
    public static boolean rage(float hp){return hp<=315;}
    public static boolean retreat(float hp){return hp<=42;}
    public static boolean unyielding(float hp){return hp<=84;}
    // Calibrated against vanilla CombatRules: 20 armor / 8 toughness. Ordinary mitigation remains.
    public static float stompRaw(int ring){return switch(ring){case 0->26;case 1->22;default->20;};}
    public static float diamondNet(float raw){return raw*(1-Math.clamp(20-raw/4,4,20)/25);}
    public static boolean recovering(State s){return s==State.RETREAT_START||s==State.RETREAT_MOVING||s==State.HEAL_DRINK||s==State.RECOVERY||s==State.DOWN_FOR_ENCOUNTER;}
    private BogatyrRules(){}
}
