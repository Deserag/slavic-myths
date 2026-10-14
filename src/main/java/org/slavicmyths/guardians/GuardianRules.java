package org.slavicmyths.guardians;
/** All timings are server ticks; local 1.9.4 balance only. */
public final class GuardianRules {
 public static final int IDLE=0,AWAKEN=1,COMBAT=2,MELEE=3,PULSE=4,LOCKDOWN=5,EXHAUSTED=6,SLAM=7,WHIP=8,WHIRL=9,PRISON=10,FLOOD=11,SEEK=12,HEAL=13,INTERRUPTED=14,PURIFY=15,CLEANSED=16;
 public static final int AWAKEN_TICKS=60,MELEE_WINDUP=11,MELEE_RECOVERY=9,PULSE_WINDUP=30,PULSE_COOLDOWN=280,SLAM_WINDUP=20,SLAM_COOLDOWN=180,LOCK_WARNING=24,LOCK_ACTIVE=40,LOCK_WAVES=3,LOCK_COOLDOWN=500,EXHAUST=40,WHIP_WINDUP=16,WHIP_GAP=10,WHIP_COOLDOWN=140,WHIRL_WARNING=24,WHIRL_ACTIVE=80,WHIRL_COOLDOWN=280,PRISON_WARNING=20,PRISON_MAX=80,PRISON_COOLDOWN=400,VALVE_USE=10,HEAL_WARNING=40,HEAL_TICK=20,HEAL_BLOCK=400,FLOOD_WARNING=24,FLOOD_ACTIVE=140,PURIFY_SCENE=60,ECHO_LIFE=300;
 public static final float FIRE_HP=900,WATER_HP=800,FIRE_ARMOR=7,WATER_ARMOR=4,FIRE_MELEE=5,WATER_MELEE=3,SLAM_DAMAGE=4,ZONE_DAMAGE=2,WHIP_DAMAGE=3,WHIRL_DAMAGE=2,HEAL_AMOUNT=5,ECHO_HP=25,ECHO_DAMAGE=2;
 public static final double FIRE_SPEED=.21,WATER_SPEED=.26,WHIRL_RADIUS=2.5,WHIRL_PULL=.065,PRISON_DRAG=.65,FLOOD_DRAG=.82;
 public static final int HEAL_ACTIVE=200,HEAL_COOLDOWN=200,SEEK_LIMIT=100,FLOOD_COOLDOWN=340,FLOOD_STEP=50,ABSENT_RESET=200;
 public static float maximum(boolean water){return water?WATER_HP:FIRE_HP;}
 public static float threshold(boolean water){return maximum(water)*.05f;}
 public static float pulse(double distance){return distance<=3?8:distance<=6?5:distance<=10?2:0;}
 public static int lockWarning(boolean finalPhase){return finalPhase?18:LOCK_WARNING;}
 /** Three wide strips, one safe strip per wave; routes have no terrain alterations. */
 public static int safeStrip(int pattern,int wave){return switch(Math.floorMod(pattern,3)){case 0->wave;case 1->2-wave;default->wave==1?0:1;};}
 public static int strip(double coordinate,double min,double size){return Math.clamp((int)((coordinate-min)/(size/3)),0,2);}
 public static boolean healAllowed(int valveMask,long now,long blockedUntil){return valveMask!=3&&now>=blockedUntil;}
 public static int prisonHits(int hits){return Math.min(3,hits+1);}
 public static int floodDelay(int region){return region*FLOOD_STEP;}
 public static boolean migrateRecord(net.minecraft.nbt.CompoundTag n){
  if(n.getInt(org.slavicmyths.combat.CreatureHealthBalance.KEY)>=org.slavicmyths.combat.CreatureHealthBalance.VERSION)return false;
  boolean water=n.getBoolean("Spring");float max=maximum(water);
  if(n.contains("Health"))n.putFloat("Health",org.slavicmyths.combat.CreatureHealthBalance.proportional(n.getFloat("Health"),water?450:500,max));
  if(n.getString("Stage").equals("PURIFICATION"))n.putFloat("Health",threshold(water));
  else if(n.getString("Stage").equals("CLEARED"))n.putFloat("Health",max);
  org.slavicmyths.combat.CreatureHealthBalance.mark(n);return true;
 }
 private GuardianRules(){}
}
