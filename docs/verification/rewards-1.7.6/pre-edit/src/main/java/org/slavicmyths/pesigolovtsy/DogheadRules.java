package org.slavicmyths.pesigolovtsy;
import java.util.Locale;
/** Shared authoritative clip timings and starter balance, not a claim of gameplay acceptance. */
public final class DogheadRules {
 public enum Role {
  HUNTER(40,.26F,.82F,2.1F,20,6),SCOUT(28,.31F,.70F,1.9F,28,5),LEADER(50,.22F,.95F,2.3F,24,12);
  public final int hp,sight,xp;public final float speed,width,height;
  Role(int hp,float speed,float width,float height,int sight,int xp){this.hp=hp;this.speed=speed;this.width=width;this.height=height;this.sight=sight;this.xp=xp;}
  public String id(){return name().toLowerCase(Locale.ROOT);}
 }
 public enum Clip {
  READY(0,0),HUNTER_SPOT(12,0),HUNTER_BARK(20,8),THRUST(24,10),SWEEP(28,13),HUNTER_GUARD(16,0),HUNTER_HURT(10,0),HUNTER_DEATH(26,0),
  SCOUT_SPOT(10,5),BOW_EQUIP(10,0),BOW(32,22),KNIFE_SWAP(9,0),KNIFE(16,9),DODGE(18,0),SCOUT_HURT(8,0),SCOUT_DEATH(22,0),
  LEADER_SPOT(14,0),HOWL(28,15),CHOP(38,21),CLEAVE(34,19),LEADER_GUARD(18,0),LEADER_HURT(14,0),LEADER_DEATH(32,0),
  HEAR_BARK(8,0),HEAR_HOWL(12,0),SNIFF(30,0),RETREAT(14,0),VICTORY(30,0);
  public final int duration,marker;Clip(int d,int m){duration=d;marker=m;}
  public boolean attack(){return this==THRUST||this==SWEEP||this==KNIFE||this==BOW||this==CHOP||this==CLEAVE;}
  public boolean death(){return this==HUNTER_DEATH||this==SCOUT_DEATH||this==LEADER_DEATH;}
 }
 public static Clip hurt(Role r){return switch(r){case HUNTER->Clip.HUNTER_HURT;case SCOUT->Clip.SCOUT_HURT;case LEADER->Clip.LEADER_HURT;};}
 public static Clip death(Role r){return switch(r){case HUNTER->Clip.HUNTER_DEATH;case SCOUT->Clip.SCOUT_DEATH;case LEADER->Clip.LEADER_DEATH;};}
 public static boolean interrupts(Clip clip,int tick,float actualDamage){return !clip.death()&&(!(clip==Clip.CHOP)&&!(clip==Clip.CLEAVE)||tick<17&&actualDamage>=4);}
 public static boolean night(long day){long t=Math.floorMod(day,24000);return t>=12000&&t<23500;}
 public static final class Clock {
  public Clip clip=Clip.READY;public int age,nonce;public boolean fired;
  public void begin(Clip c){clip=c;age=0;fired=false;nonce++;}
  public boolean tick(){age++;if(!fired&&clip.marker>0&&age==clip.marker){fired=true;return true;}return false;}
  public boolean ended(){return clip!=Clip.READY&&!clip.death()&&age>=clip.duration;}
 }
 private DogheadRules(){}
}
