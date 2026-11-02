package org.slavicmyths.morovoy;

/** Pure deterministic gate rules; safe to exercise with Java 21 without loading Minecraft. */
public final class MorovoyZmeyRules {
 public static final float EFFECTIVE_HP=2200f;
 public static final double WAKE_RADIUS=9.25; // approx seven blocks beyond the visible body
 public static boolean shouldWake(double distanceSquared){return Double.isFinite(distanceSquared)&&distanceSquared<=WAKE_RADIUS*WAKE_RADIUS;}
 public static boolean secondPhase(float health,float max){return max>0&&health>0&&health<=max*.5f;}
 public static boolean hitsAt(int action,int age){return switch(action){
  case 1,2->age==18;
  case 3,4->age>=18&&age<=55&&age%4==0;
  case 5->age==20||age==30;
  case 6->age==22;
  case 7->age==24;
  case 8,9->age==20;
  case 10->age==17;
  case 11->age==35;
  case 12->age>=12&&age<=70&&age%10==0;
  default->false;
 };}
 public static int fieldLife(int requested){return Math.clamp(requested,20,120);}
 private MorovoyZmeyRules(){}
}
