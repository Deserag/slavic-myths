package org.slavicmyths.water;

/** Server-only encounter clock. Being close is not equivalent to attacking her. */
public final class RusalkaEncounter {
 public static final int OBSERVE=40,INTRO_GRACE=100,CLOSE_WARNING=40;
 public enum Vocal {INTRO,LOOP,INTENSE}
 public static Vocal vocal(int elapsed,boolean intense){return elapsed<60?Vocal.INTRO:intense?Vocal.INTENSE:Vocal.LOOP;}
 private int watching;private long songStart=-1,closeSince=-1;
 public boolean observe(){watching=Math.min(OBSERVE,watching+20);return watching>=OBSERVE;}
 public void begin(long now){songStart=now;closeSince=-1;}
 public void reset(){watching=0;songStart=closeSince=-1;}
 public boolean warning(long now,double distanceSquared){
  if(songStart<0||now-songStart<INTRO_GRACE||distanceSquared>=6.25){closeSince=-1;return false;}
  if(closeSince<0)closeSince=now;return true;
 }
 public boolean hostile(long now){return closeSince>=0&&now-closeSince>=CLOSE_WARNING;}
}
