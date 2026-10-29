package org.slavicmyths.nethersmithing;
import java.util.*;
public final class NetherWeaponRules {
 public static final List<String> IDS=List.of("navya_ostroga","ashen_greatsword","shadow_throwing_knife","volcanic_heart_flail","infernal_bow");
 public static float raw(String id){return switch(id){case "navya_ostroga"->10;case "ashen_greatsword"->15;case "shadow_throwing_knife"->6;case "volcanic_heart_flail"->13;default->0;};}
 public static float speed(String id){return switch(id){case "navya_ostroga"->1.15F;case "ashen_greatsword","volcanic_heart_flail"->.9F;case "shadow_throwing_knife"->2;default->1;};}
 public static int durability(String id){return switch(id){case "navya_ostroga","volcanic_heart_flail"->1800;case "ashen_greatsword"->1950;case "shadow_throwing_knife"->1650;default->1200;};}
 public static int cooldown(String id){return switch(id){case "ashen_greatsword","volcanic_heart_flail"->360;case "infernal_bow"->200;default->240;};}
 public static int charge(String id){return switch(id){case "navya_ostroga"->10;case "ashen_greatsword"->16;case "volcanic_heart_flail"->18;default->24;};}
 public static double bowBase(double raw,double speed){return Math.max(0,raw-.00001)/Math.max(.01,speed);}
 private NetherWeaponRules(){}
}
