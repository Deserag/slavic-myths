package org.slavicmyths.marinka;
/** Server action clock; all hit frames are ticks, independent of client FPS. */
public enum WitchAction {
    IDLE(0),FOLLOW(0),SIDESTEP(8),PARRY(13),COMBO(30),THRUST(18),SWEEP(25),WARD(21),UNRAVEL(22),
    TRANSFORM(44),AUROCHS(0),BULL_CHARGE(28),REVERT(30),RETREAT(25),DRINK(63),RECOVER(160),RALLY(18),SALUTE(34),INCAPACITATED(0),
    INTRO(70),TRACE(29),FIRE(22),LURE(40),SHADOW(18),VEIL(17),CRACK(12),BLINK(16),BIRD_CHANGE(15),BIRD_FLIGHT(26),BIRD_RETURN(13),
    CURSE(47),HERD(24),RAGE(42),EXHAUSTED(25),DEFEAT(51),CLONE_BREAK(9);
    public final int ticks;WitchAction(int ticks){this.ticks=ticks;}
}
