package org.slavicmyths.water;
/** Wire IDs and fixed timings shared by server action state, native pose and offline checks. */
public final class RusalkaActions {
 public static final int IDLE=0,OBSERVE=1,SONG_START=2,LURE=3,BREAK=4,WINDUP=5,GRAB=6,RECOVERY=7,DIVE=8,INTENSE=9,WARNING=10;
 public static final int TELEGRAPH=12,ACTIVE=1,RECOVER=7,DIVE_COOLDOWN=160,DIVE_WARN=8,DIVE_MOVE=10;
 public static int remaining(long deadline,long now,int limit){return (int)Math.max(0,Math.min(limit,deadline-now));}
 public static boolean diveDistance(double squared){return Double.isFinite(squared)&&squared>=16&&squared<=64;}
 public static boolean safeDivePath(double length,java.util.function.DoublePredicate wetClear){
  if(!Double.isFinite(length)||length<1||length>3.8)return false;
  int samples=(int)Math.ceil(length/.38);
  for(int i=0;i<=samples;i++)if(!wetClear.test(length*i/samples))return false;
  return true;
 }
 public record Memory(long silentUntil,long nextDive,int watching){}
 public static Memory readMemory(net.minecraft.nbt.CompoundTag tag,long now){return new Memory(now+remaining(tag.getLong("RusalkaSilentUntil"),now,120),now+remaining(tag.getLong("RusalkaNextDive"),now,DIVE_COOLDOWN),Math.clamp(tag.getInt("RusalkaWatching"),0,100));}
 public static void writeMemory(net.minecraft.nbt.CompoundTag tag,long silent,long dive,int watching){tag.putLong("RusalkaSilentUntil",silent);tag.putLong("RusalkaNextDive",dive);tag.putInt("RusalkaWatching",watching);}
 private RusalkaActions(){}
}
