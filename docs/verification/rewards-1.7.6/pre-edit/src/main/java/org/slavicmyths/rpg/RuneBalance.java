package org.slavicmyths.rpg;

import java.util.*;

/** Pure balance and stacking rules shared by server effects, menus, tooltips and offline checks. */
public final class RuneBalance {
    public record Definition(String id,int tier,String base){}
    public static final List<Definition> DEFINITIONS=List.of(new Definition("strength",1,"iron"),new Definition("speed",1,"gold"),new Definition("resilience",1,"iron"),new Definition("heat",1,"gold"),new Definition("crushing",2,"diamond"),new Definition("wind",2,"perunite"),new Definition("blood",2,"perunite"),new Definition("fortitude",2,"diamond"));
    public static final double STRENGTH=.06,SPEED_ATTACK=.06,SPEED_MOVE=.04,RESILIENCE_TOUGHNESS=.5,SHIELD_EFFICIENCY=.05;
    public static final double FIRE_CHANCE=.15,FIRE_REDUCTION=.08,CRUSHING_DAMAGE=.18,CRUSHING_SLOW=.10;
    public static final double WIND_MOVE=.08,WIND_JUMP=.15,WIND_PROJECTILE=.15,WIND_MOMENTUM=.03;
    public static final double BLOOD_CHANCE=.20,BLEED_HP=.5,FORT_TOUGHNESS=1,FORT_KB=.03,FORT_SLOW=.015;
    public static final int FIRE_SECONDS=3,BLEED_DURATION=120,BLEED_INTERVAL=40,RUNE_BLEED_MAX=2,NATIVE_BLEED_MAX=3;
    public static final double DAMAGE_CAP=.60,ATTACK_BONUS_CAP=.35,ATTACK_PENALTY_CAP=.50,MIN_ATTACK_SPEED=.10;
    public static final double MOVEMENT_CAP=.35,MOVEMENT_PENALTY_CAP=.15,TOUGHNESS_CAP=4,KB_CAP=.20,FIRE_CAP=.40;
    public static final double PROC_CAP=.50,JUMP_CAP=.35,PROJECTILE_CAP=.35,SHIELD_CAP=.25,MOMENTUM_CAP=.08;
    public static Definition definition(String id){return DEFINITIONS.stream().filter(d->d.id.equals(id)).findFirst().orElse(null);}
    public static boolean modern(String id){return definition(id)!=null;}
    /** Beyond the third copy, use the third coefficient; equipment-wide caps bound 12 armor sockets. */
    public static double effectiveMultiplier(int copyIndex){return copyIndex<1?0:copyIndex==1?1:copyIndex==2?.70:.45;}
    public static double penaltyMultiplier(int copyIndex){return copyIndex<1?0:copyIndex==1?1:copyIndex==2?.60:.35;}
    public static double effective(int copies){if(copies<=0)return 0;return copies==1?1:copies==2?1.70:2.15+(copies-3)*.45;}
    public static double penalty(int copies){if(copies<=0)return 0;return copies==1?1:copies==2?1.60:1.95+(copies-3)*.35;}
    public static double bonus(double base,int copies,double cap){return Math.min(cap,base*effective(copies));}
    public static double attackFactor(int speed,int crushing){return 1+bonus(SPEED_ATTACK,speed,ATTACK_BONUS_CAP)-Math.min(ATTACK_PENALTY_CAP,CRUSHING_SLOW*penalty(crushing));}
    public static double safeAttackFactor(double nativeSpeed,int speed,int crushing){double factor=attackFactor(speed,crushing);if(factor<1&&nativeSpeed>0)factor=Math.max(factor,Math.min(1,MIN_ATTACK_SPEED/nativeSpeed));return factor;}
    public static double movement(int speed,int wind,int fort){return Math.min(MOVEMENT_CAP,SPEED_MOVE*effective(speed)+WIND_MOVE*effective(wind))-Math.min(MOVEMENT_PENALTY_CAP,FORT_SLOW*penalty(fort));}
    public static double toughness(int resilience,int fort){return Math.min(TOUGHNESS_CAP,RESILIENCE_TOUGHNESS*effective(resilience)+FORT_TOUGHNESS*effective(fort));}
    public static int bleedStacks(int nativeStacks,int runeStacks){return Math.max(Math.clamp(nativeStacks,0,NATIVE_BLEED_MAX),Math.clamp(runeStacks,0,RUNE_BLEED_MAX));}
    public static String format(double value){return java.math.BigDecimal.valueOf(value).setScale(3,java.math.RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();}
    private RuneBalance(){}
}
