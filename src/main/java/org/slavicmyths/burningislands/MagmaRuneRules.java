package org.slavicmyths.burningislands;

/** Owner-approved family policy shared by native socket validation and damage callback. */
public final class MagmaRuneRules {
 public static boolean rune(String id){return "magma_1".equals(id)||"magma_2".equals(id);}
 public static int tier(boolean first,boolean second){return second?2:first?1:0;}
 public static int damage(int tier){return tier==2?4:tier==1?2:0;}
 public static boolean ready(int tier,float received,boolean validAttacker,boolean reflecting,long now,long ready){return tier>0&&tier<=2&&received>0&&Float.isFinite(received)&&validAttacker&&!reflecting&&now>=ready;}
 public static boolean conflict(java.util.List<String> installed,String next){return rune(next)&&installed.stream().anyMatch(MagmaRuneRules::rune);}
 private MagmaRuneRules(){}
}
