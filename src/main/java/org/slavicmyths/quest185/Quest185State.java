package org.slavicmyths.quest185;
import net.minecraft.nbt.*;
/** Durable story policy, separate from existing trust and NPC story records. */
public final class Quest185State {
 public static final String[] IDS={"investigate_1","investigate_2","investigate_3","investigate_4","investigate_5","investigate_6","wolf_tracks","wolf_fear","wolf_loyalty"};
 public static final int[] XP={150,200,350,250,300,800,100,175,300};
 public enum Status{LOCKED,AVAILABLE,ACTIVE,READY,COMPLETED}
 public static CompoundTag quest(CompoundTag n,int i){String key=IDS[i];if(!n.contains(key,10))n.put(key,new CompoundTag());return n.getCompound(key);}
 public static boolean done(CompoundTag n,int i){return quest(n,i).getBoolean("Claimed");}
 public static boolean mainReady(CompoundTag n,int i,int dust,boolean trophies){return switch(i){case 0->n.getInt("MorKills")>=3||n.getBoolean("LegacyHornProof");case 1->dust>=3;case 2->n.getBoolean("Horn")||n.getBoolean("Likho")&&n.getBoolean("Ovinnik")&&(trophies||n.getBoolean("TrophiesVerified"));case 3->n.getBoolean("Horn")||n.getBoolean("Poloz");case 4->n.getBoolean("WarningRead");case 5->n.getBoolean("Poloz");default->false;};}
 public static Status status(CompoundTag n,int i,int dust,boolean trophies,int wolfTrust,boolean oldWolfDone){if(done(n,i))return Status.COMPLETED;
  if(i<6){if(!n.getBoolean("IntroDone")||i>0&&!done(n,i-1))return Status.LOCKED;return mainReady(n,i,dust,trophies)?Status.READY:Status.ACTIVE;}
  if(wolfTrust<2||!n.getBoolean("IntroDone")||i>6&&!done(n,i-1)||i==8&&!(oldWolfDone||wolfTrust>=3))return Status.LOCKED;
  if(!quest(n,i).getBoolean("Accepted"))return Status.AVAILABLE;
  boolean ready=i==6?n.getBoolean("TraceMob")&&n.getBoolean("TraceBlock"):i==7?n.getInt("BasicKills")-quest(n,i).getInt("StartKills")>=4:quest(n,i).getInt("TrainingKills")>=3;
  return ready?Status.READY:Status.ACTIVE;
 }
 public static boolean admits(java.util.UUID token,java.util.UUID request,int sequence,int next,long now,long expiry){return token.equals(request)&&sequence>=0&&sequence<Integer.MAX_VALUE&&next==sequence+1&&now<=expiry;}
 public static String inviter(boolean yaga,boolean bayun,long yagaAt,long bayunAt){return yaga&&(!bayun||yagaAt>=bayunAt)?"yaga":bayun?"bayun":"";}
 public static boolean claim(CompoundTag n,int i){if(done(n,i))return false;quest(n,i).putBoolean("Claimed",true);return true;}
 public static boolean earlyReady(CompoundTag n,boolean eye){return !n.getBoolean("LikhoResearchRewardClaimed")&&(n.getBoolean("Likho")||n.getBoolean("LegacyEyeProof")||n.getBoolean("Horn"))&&(eye||n.getBoolean("Horn")||n.getBoolean("EyeShown"));}
 public static boolean claimEarly(CompoundTag n,boolean eye){if(!earlyReady(n,eye))return false;n.putBoolean("LikhoResearchRewardClaimed",true);n.putBoolean("Cleansing",true);n.putBoolean("EarthAccepted",true);return true;}
 public static boolean earthReady(CompoundTag n,int count){return n.getBoolean("Cleansing")&&!n.getBoolean("EarthRestorationRewardClaimed")&&count>=20;}
 public static boolean claimEarth(CompoundTag n,int count){if(!earthReady(n,count))return false;n.putBoolean("EarthRestorationRewardClaimed",true);return true;}
 private Quest185State(){}
}
