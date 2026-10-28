package org.slavicmyths.burningislands;

/** Constant-size reward aggregation: preserve total cores and 4–6 for every remaining victory. */
public final class MagmaRewardRules {
 public static int nextCores(long wins,long cores){
  if(wins<=0||wins>Long.MAX_VALUE/6||cores<wins*4||cores>wins*6)return 0;
  return (int)Math.clamp(cores-4*(wins-1),4,6);
 }
 private MagmaRewardRules(){}
}
