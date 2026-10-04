package org.slavicmyths.kurgan;

/** Shared fixed combat data, independent of registries and client classes. */
public final class KurganFighter {
    public enum Kind {
        UPYR("upyr",40,6,.31,28,.1,2,.65F,1.9F),
        NAV("nav",32,5,.30,30,.2,0,.6F,2.1F),
        DRUZHINNIK("kurgan_druzhinnik",60,7,.25,26,.35,8,.8F,2F),
        VOEVODA("kurgan_voevoda",150,9,.27,34,.5,12,1F,2.3F),
        VOLKHV("buried_volkhv",150,6,.24,36,.2,6,.7F,2.1F),
        PRINCE("unresting_prince",350,11,.28,40,.7,16,1.1F,2.6F);
        public final String id;public final double hp,damage,speed,range,resistance,armor;public final float width,height;
        Kind(String id,double hp,double damage,double speed,double range,double resistance,double armor,float width,float height){this.id=id;this.hp=hp;this.damage=damage;this.speed=speed;this.range=range;this.resistance=resistance;this.armor=armor;this.width=width;this.height=height;}
        public boolean boss(){return ordinal()>=3;}
    }
    public enum Move {
        IDLE(0,0,0), CLAW(5,5,16), SWORD(6,6,18), AXE(6,6,16), TOUCH(5,5,18),
        LEAP(10,10,90), BITE(10,10,140), SHIFT(8,6,90), BASH(10,8,100), RIPOSTE(5,6,50),
        CHARGE(12,20,90), SWEEP(16,12,125), BOLT(8,4,22), CLONES(14,6,120),
        SEAL(28,8,115), SUMMON(16,10,120), COMBO(6,26,16), HEAVY(18,20,110),
        GRAB(12,12,200), RUSH(10,10,80);
        public final int windup,recovery,cooldown;
        Move(int windup,int recovery,int cooldown){this.windup=windup;this.recovery=recovery;this.cooldown=cooldown;}
    }
    public static int phase(Kind kind,double fraction){return kind==Kind.PRINCE?(fraction<=.3000001?2:fraction<=.6500001?1:0):kind==Kind.VOEVODA&&fraction<=.5000001?1:0;}
    public static int basicInterval(Kind kind,int phase){return kind==Kind.VOEVODA?(phase>0?14:16):kind==Kind.PRINCE?(phase==2?12:phase==1?14:16):kind==Kind.UPYR?16:kind==Kind.VOLKHV?22:18;}
}
