package org.slavicmyths.client;

/** Continuous windup, four-tick blade sweep, then recovery to the resting grip. */
public final class NightSwingPose {
 public static float motion(float age,int mode,int combo,int duration){
  float hit=mode==0?(combo==2?7:5):10,begin=hit-2,end=hit+2;
  if(age<=0||age>=duration)return 0;
  if(age<begin)return -age/begin;
  if(age<end)return -1+2*(age-begin)/4;
  return Math.max(0,1-(age-end)/(duration-end));
 }
 public static float horizontal(float age,int mode,int combo,int duration){
  float hit=mode==0?(combo==2?7:5):10,begin=hit-2,end=hit+2;
  if(age<=0||age>=duration)return 0;
  if(age<begin)return age/begin;
  if(age<=end)return 1;
  return Math.max(0,1-(age-end)/(duration-end));
 }
 private NightSwingPose(){}
}
