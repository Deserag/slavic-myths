package org.slavicmyths.wolf;
/** Shared server hit/audio timestamps and client pose contract. */
public final class WolfTimeline {
 public enum State { IDLE_UNMET,GREETING,WAITING_DECISION,TRIAL_PREPARE,TRIAL_ACTIVE,TRIAL_SUMMONING,TRIAL_COMPLETE_CINEMATIC,POST_TRIAL_NPC,TRIAL_ABORTED }
 public enum Move { NONE(0),BITE(23),DOUBLE_CLAW(47),POUNCE(40),FLANK_DASH(41),HOWL_SUMMON(132);public final int duration;Move(int ticks){duration=ticks;} }
 public static final int HOWL_SOUND=31,PACK_SPAWN=110,PACK_ACTIVATE=118,PREPARE=30,END=70,HOWL_DELAY=600,HOWL_COOLDOWN=2000,MAX_SUMMONS=2;
 public static final float TRIAL_HP=500,END_HP=50;
 public static boolean hit(Move move,int tick){return switch(move){case BITE->tick==12;case DOUBLE_CLAW->tick==14||tick==33;case POUNCE,FLANK_DASH->tick==27;default->false;};}
 private WolfTimeline(){}
}
