package org.slavicmyths.rpg;

import java.util.Set;

/** World-independent boundaries, shared with offline verification. */
public final class CraftRuneRules {
    public static final Set<String> IDS=Set.of("unbreakable_iv","perun_rage_iv","forest_spirit_iv","mastery_iv");
    public static final int DEFENSE_CD=1000,RAGE_CD=900,RAGE_DURATION=100,SERIES_WINDOW=160;
    public static final int FOREST_CD=1200,QUIET=120,HEAL_INTERVAL=60,HEALS=4;
    public static boolean defense(double damage,boolean combat){return combat&&damage>=8;}
    public static double healthDamage(double incoming,double absorption){return Math.max(0,incoming-Math.max(0,absorption));}
    public static double defendedDamage(double incoming,double absorption){return incoming-healthDamage(incoming,absorption)*.25;}
    public static double mining(boolean one,boolean two,boolean depths,boolean mastery){
        return 1+Math.min(mastery?.35:.30,(two?.20:one?.10:0)+(depths?.20:0)+(mastery?.25:0));
    }
    public static int fortune(int base,boolean fortune,boolean mastery,boolean silk){return silk||!fortune&&!mastery?base:Math.min(4,Math.max(0,base)+(fortune?1:0)+(mastery?1:0));}
    public static double damageFactor(double existing,double oath,boolean rage){return (1+Math.min(RuneBalance.DAMAGE_CAP,existing+oath+(rage?.16:0)))/(1+existing);}
    public static double attackFactor(double factor,boolean rage){return rage?Math.min(1+RuneBalance.ATTACK_BONUS_CAP,factor*1.10):factor;}
    public static boolean forest(double hp,double max,long now,long lastDamage,long cooldown){return max>0&&hp>0&&hp<=max*.5&&now-lastDamage>=QUIET&&now>=cooldown;}
    public static final class Series {
        public int hits;public long last,until;
        public boolean hit(long now,boolean strong,boolean ready){
            if(!strong||!ready||now<until)return false;
            if(hits>0&&now-last>SERIES_WINDOW)hits=0;
            last=now;
            if(++hits<4)return false;
            hits=0;until=now+RAGE_DURATION;return true;
        }
        public void clear(){hits=0;until=0;}
    }
    public static final class Healing {
        public int remaining;public long next;
        public void start(long now){remaining=HEALS;next=now+HEAL_INTERVAL;}
        public boolean pulse(long now){if(remaining<=0||now<next)return false;remaining--;next+=HEAL_INTERVAL;return true;}
        public void stop(){remaining=0;}
    }
    private CraftRuneRules(){}
}
