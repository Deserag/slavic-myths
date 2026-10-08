package org.slavicmyths.rpg;

import java.util.List;

/** Pure rules shared by runtime, presentation and offline checks. No registry initialization. */
public final class RareRuneRules {
    public static final List<String> IDS=List.of("floating_weapon","flight","rare_protection","death","berserker","sacrifice");
    public static final List<String> ACTIVE=List.of("floating_weapon","flight","sacrifice");
    public static int cooldown(String id){return switch(id){case "floating_weapon"->700;case "flight"->900;case "rare_protection"->1200;case "death"->400;case "sacrifice"->800;default->0;};}
    public static int duration(String id){return switch(id){case "floating_weapon"->120;case "flight"->100;case "rare_protection"->400;case "sacrifice"->160;default->0;};}
    public static int tier(double health,double max){if(max<=0||health<=0)return 0;double f=health/max;return f<=.15?3:f<=.30?2:f<=.50?1:0;}
    public static double damage(int tier){return Math.clamp(tier,0,3)*.10;}
    public static double speed(int tier){return Math.clamp(tier,0,3)*.05;}
    public static double vulnerability(int tier){return switch(tier){case 1->.05;case 2->.12;case 3->.20;default->0;};}
    public static boolean canPay(double hp,double price){return Double.isFinite(hp)&&hp>price&&price>0;}
    public static boolean execute(double targetHp,double playerHp,boolean immune){return !immune&&targetHp>0&&targetHp<=5&&canPay(playerHp,12);}
    public static boolean ready(long now,long until){return until<=now;}
    public static int remainingShields(int shields){return Math.max(0,Math.min(3,shields)-1);}
    public static int echoCycle(int age){return age<13?-1:(age-13)%28;}
    public static double defenseMultiplier(int tier){return 1/(1-vulnerability(tier));}
    public static int selected(int old,int count){return count==0?0:Math.floorMod(old,count);}
    private RareRuneRules(){}
}
