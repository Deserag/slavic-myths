package org.slavicmyths.hunt;

/** Passive Likho focus-gaze debuffs and per-player cooldown, in game ticks. */
public final class LikhoGazeRules {
 public static final String VISUAL_READY_TAG="LikhoGazeVisualReady";
 public static final int WEAKNESS_TICKS=120,BLINDNESS_TICKS=25,NAUSEA_TICKS=70,VISUAL_COOLDOWN_TICKS=240;
 public static boolean eligible(double distance,boolean lineOfSight){return Double.isFinite(distance)&&distance<=10&&distance>=0&&lineOfSight;}
 public static boolean visualReady(long now,long until){return now>=until||until-now>VISUAL_COOLDOWN_TICKS;}
 private LikhoGazeRules(){}
}
